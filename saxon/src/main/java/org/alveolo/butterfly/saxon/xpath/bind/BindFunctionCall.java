package org.alveolo.butterfly.saxon.xpath.bind;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Value;

import org.alveolo.butterfly.saxon.xpath.functions.CoreConstants;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;


@SuppressWarnings("serial")
public abstract class BindFunctionCall extends ExtensionFunctionCall {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public SequenceIterator call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
		Controller controller = context.getController();
		RequestContext rc = (RequestContext) controller.getParameter(CoreConstants.REQUEST_CONTEXT_PARAM);

		String path = arguments[0].next().getStringValue();
		BindStatus status = new BindStatus(rc, path, false);

		Object res = call(status, path);

		if (res == null) {
			return EmptySequence.getInstance().iterate();
		}

		JPConverter converter = JPConverter.allocate(res.getClass(), context.getConfiguration());

		return Value.getIterator(converter.convert(res, context));
	}

	protected abstract Object call(BindStatus status, String path);
}
