package org.alveolo.butterfly.saxon.xpath.functions;

import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.value.SequenceType;


public abstract class AttributeFunctionDefinition extends ExtensionFunctionDefinition {
	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
				SequenceType.SINGLE_STRING, // name
		};
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.ANY_SEQUENCE;
	}
}
