package org.alveolo.butterfly.saxon.xpath.functions;

import javax.servlet.http.HttpServletRequest;

import net.sf.saxon.Controller;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.StructuredQName;


@SuppressWarnings("serial")
public class RequestAttribute extends AttributeFunctionDefinition {
	private static final StructuredQName qName = new StructuredQName("", CoreConstants.NAMESPACE, "request-attribute");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new RequestAttributeCall();
	}

	private static class RequestAttributeCall extends AttributeFunctionCall {
		@Override
		protected Object getAttribute(Controller controller, String name) {
			HttpServletRequest request = (HttpServletRequest)
					controller.getParameter(CoreConstants.SERVLET_REQUEST_PARAM);

			return request.getAttribute(name);
		}
	}
}
