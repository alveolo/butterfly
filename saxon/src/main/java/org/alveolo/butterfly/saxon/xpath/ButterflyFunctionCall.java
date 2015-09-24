package org.alveolo.butterfly.saxon.xpath;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.value.ObjectValue;


public abstract class ButterflyFunctionCall extends ExtensionFunctionCall {
	protected static <T> T getParameter(Controller controller, QName name) {
		@SuppressWarnings("unchecked")
		ObjectValue<T> value = (ObjectValue<T>) controller.getParameter(name.getStructuredQName());
		return (value == null) ? null : value.getObject();
	}

	protected static <T> T getParameter(XPathContext context, QName name) {
		return getParameter(context.getController(), name);
	}
}
