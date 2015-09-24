package org.alveolo.butterfly.saxon.xpath.functions;

import javax.servlet.http.HttpServletRequest;

import org.alveolo.butterfly.saxon.xpath.ButterflyFunctionCall;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;


public class RequestParameter extends ExtensionFunctionDefinition {
	private static final StructuredQName qName =
			new StructuredQName(CoreConstants.PREFIX, CoreConstants.NAMESPACE, "request-parameter");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
				SequenceType.SINGLE_STRING, // name
		};
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE);
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new RequestParameterCall();
	}

	private static class RequestParameterCall extends ButterflyFunctionCall {
		@Override
		public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
			String name = arguments[0].head().getStringValue();

			String[] parameters = getParameter(context.getController(), name);
			if (parameters == null) {
				return EmptySequence.getInstance();
			}

			return JPConverter.allocate(parameters.getClass(), null, context.getConfiguration())
					.convert(parameters, context);
		}

		private String[] getParameter(Controller controller, String name) {
			HttpServletRequest request = getParameter(controller, CoreConstants.SERVLET_REQUEST_PARAM);
			return request.getParameterValues(name);
		}
	}
}
