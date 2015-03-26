package org.alveolo.butterfly.saxon.xpath.bind;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.StructuredQName;


@SuppressWarnings("serial")
public class Property extends BindFunctionDefinition {
	private static final StructuredQName qName =
			new StructuredQName(BindConstants.PREFIX, BindConstants.NAMESPACE, "property");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new PropertyCall();
	}

	private static class PropertyCall extends AbstractPropertyCall {
		@Override
		protected Object call(Class<?> type, String propertyName) {
			return type.getSimpleName() + "." + propertyName;
		}
	}
}
