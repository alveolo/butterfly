package org.alveolo.butterfly.saxon.xpath.functions;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.value.Value;


@SuppressWarnings("serial")
public abstract class AttributeFunctionCall extends ExtensionFunctionCall {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public SequenceIterator call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
		String name = arguments[0].next().getStringValue();

		Object attribute = getAttribute(context.getController(), name);
		if (attribute == null) {
			return EmptyIterator.getInstance();
		}

		JPConverter converter = JPConverter.allocate(attribute.getClass(), context.getConfiguration());

		return Value.getIterator(converter.convert(attribute, context));
	}

	protected abstract Object getAttribute(Controller controller, String name);
}
