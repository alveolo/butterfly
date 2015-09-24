package org.alveolo.butterfly.saxon.xpath.functions;

import org.alveolo.butterfly.saxon.xpath.ButterflyFunctionCall;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;


public abstract class AttributeFunctionCall extends ButterflyFunctionCall {
	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		String name = arguments[0].head().getStringValue();

		Object attribute = getAttribute(context.getController(), name);
		if (attribute == null) {
			return EmptySequence.getInstance();
		}

		JPConverter converter = JPConverter.allocate(attribute.getClass(), null, context.getConfiguration());

		return converter.convert(attribute, context);
	}

	protected abstract Object getAttribute(Controller controller, String name);
}
