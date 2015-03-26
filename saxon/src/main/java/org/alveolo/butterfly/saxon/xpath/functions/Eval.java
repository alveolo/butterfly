package org.alveolo.butterfly.saxon.xpath.functions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.EnvironmentAccessor;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.web.servlet.support.RequestContext;


@SuppressWarnings("serial")
public class Eval extends ExtensionFunctionDefinition {
	private static final StructuredQName qName =
			new StructuredQName(CoreConstants.PREFIX, CoreConstants.NAMESPACE, "eval");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
				SequenceType.SINGLE_STRING, // expression
		};
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.ANY_SEQUENCE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new EvalCall();
	}

	private static class EvalCall extends ExtensionFunctionCall {
		private static final String EVALUATION_CONTEXT_ATTRIBUTE = "org.alveolo.butterfly.saxon.xpath.EVALUATION_CONTEXT";

		private final ExpressionParser expressionParser = new SpelExpressionParser();

		@Override
		public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
			Expression expression = expressionParser.parseExpression(arguments[0].head().getStringValue());

			Object result = expression.getValue(getEvaluationContext(context.getController()));

			if (result == null) {
				return EmptySequence.getInstance();
			}

			JPConverter converter = JPConverter.allocate(result.getClass(), context.getConfiguration());

			return converter.convert(result, context);
		}

		private EvaluationContext getEvaluationContext(Controller controller) {
			HttpServletRequest request = (HttpServletRequest) controller.getParameter(
					CoreConstants.SERVLET_REQUEST_PARAM.getClarkName());

			EvaluationContext evaluationContext =
					(EvaluationContext) request.getAttribute(EVALUATION_CONTEXT_ATTRIBUTE);
			if (evaluationContext != null) {
				return evaluationContext;
			}

			RequestContext rc = (RequestContext) controller.getParameter(
					CoreConstants.REQUEST_CONTEXT_PARAM.getClarkName());

			StandardEvaluationContext context = new StandardEvaluationContext();
			context.addPropertyAccessor(new PagePropertyAccessor(request));
			context.addPropertyAccessor(new MapAccessor());
			context.addPropertyAccessor(new EnvironmentAccessor());
			context.setBeanResolver(new BeanFactoryResolver(rc.getWebApplicationContext()));

			ConversionService conversionService = (ConversionService)
					request.getAttribute(ConversionService.class.getName());

			if (conversionService != null) {
				context.setTypeConverter(new StandardTypeConverter(conversionService));
			}

			request.setAttribute(EVALUATION_CONTEXT_ATTRIBUTE, evaluationContext);

			return context;
		}
	}

	private static class PagePropertyAccessor implements PropertyAccessor {
		private final HttpServletRequest request;

		public PagePropertyAccessor(HttpServletRequest request) {
			this.request = request;
		}

		@Override
		public Class<?>[] getSpecificTargetClasses() {
			return null;
		}

		@Override
		public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
			return (target == null && findAttribute(name) != null);
		}

		@Override
		public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
			return new TypedValue(findAttribute(name));
		}

		@Override
		public boolean canWrite(EvaluationContext context, Object target, String name) {
			return false;
		}

		@Override
		public void write(EvaluationContext context, Object target, String name, Object newValue) {
			throw new UnsupportedOperationException();
		}

		private Object findAttribute(String name) {
			Object value = request.getAttribute(name);
			if (value != null) {
				return value;
			}

			HttpSession session = request.getSession(false);
			if (session != null) {
				value = session.getAttribute(name);
				if (value != null) {
					return value;
				}
			}

			return request.getServletContext().getAttribute(name);
		}
	}
}
