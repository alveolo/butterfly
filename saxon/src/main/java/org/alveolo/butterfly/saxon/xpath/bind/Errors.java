package org.alveolo.butterfly.saxon.xpath.bind;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.StructuredQName;

import org.springframework.web.servlet.support.BindStatus;


public class Errors extends BindFunctionDefinition {
	private static final StructuredQName qName =
			new StructuredQName(BindConstants.PREFIX, BindConstants.NAMESPACE, "errors");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ErrorsCall();
	}

	private static class ErrorsCall extends BindFunctionCall {
		@Override
		protected Object call(BindStatus status, String path) {
			return status.getErrorMessagesAsString(", ");
		}
	}
}
