package org.alveolo.butterfly.saxon.xpath.bind;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.StructuredQName;

import org.springframework.web.servlet.support.BindStatus;


@SuppressWarnings("serial")
public class Expression extends BindFunctionDefinition {
	private static final StructuredQName qName =
			new StructuredQName(BindConstants.PREFIX, BindConstants.NAMESPACE, "expression");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExpressionCall();
	}

	private static class ExpressionCall extends BindFunctionCall {
		@Override
		protected Object call(BindStatus status, String path) {
			return status.getExpression();
		}
	}
}
