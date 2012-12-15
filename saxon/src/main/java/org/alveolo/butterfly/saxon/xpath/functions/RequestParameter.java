package org.alveolo.butterfly.saxon.xpath.functions;

import javax.servlet.http.HttpServletRequest;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Value;


@SuppressWarnings("serial")
public class RequestParameter extends ExtensionFunctionDefinition {
	private static final StructuredQName qName = new StructuredQName("", CoreConstants.NAMESPACE, "request-parameter");

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

	private static class RequestParameterCall extends ExtensionFunctionCall {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public SequenceIterator call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
			String name = arguments[0].next().getStringValue();

			String[] parameters = getParameter(context.getController(), name);
			if (parameters == null) {
				return EmptyIterator.getInstance();
			}

			JPConverter converter = JPConverter.allocate(parameters.getClass(), context.getConfiguration());

			return Value.getIterator(converter.convert(parameters, context));
		}

		private String[] getParameter(Controller controller, String name) {
			HttpServletRequest request = (HttpServletRequest)
					controller.getParameter(CoreConstants.SERVLET_REQUEST_PARAM);

			return request.getParameterValues(name);
		}
	}
}
