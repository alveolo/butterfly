package org.alveolo.butterfly.saxon.xpath.bind;

import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.value.SequenceType;


@SuppressWarnings("serial")
public abstract class BindFunctionDefinition extends ExtensionFunctionDefinition {
	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
				SequenceType.SINGLE_STRING, // path
		};
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.OPTIONAL_ITEM;
	}
}
