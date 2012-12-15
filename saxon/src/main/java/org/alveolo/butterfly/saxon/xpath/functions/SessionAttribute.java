package org.alveolo.butterfly.saxon.xpath.functions;

import javax.servlet.http.HttpSession;

import net.sf.saxon.Controller;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.StructuredQName;


@SuppressWarnings("serial")
public class SessionAttribute extends AttributeFunctionDefinition {
	private static final StructuredQName qName = new StructuredQName("", CoreConstants.NAMESPACE, "session-attribute");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new SessionAttributeCall();
	}

	private static class SessionAttributeCall extends AttributeFunctionCall {
		@Override
		protected Object getAttribute(Controller controller, String name) {
			HttpSession session = (HttpSession) controller.getParameter(CoreConstants.SERVLET_SESSION_PARAM);
			return (session == null) ? null : session.getAttribute(name);
		}
	}
}
