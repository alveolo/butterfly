package org.alveolo.butterfly.saxon.xpath.bind;

import org.alveolo.butterfly.saxon.xpath.ButterflyFunctionCall;
import org.alveolo.butterfly.saxon.xpath.functions.CoreConstants;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;

import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;


public abstract class BindFunctionCall extends ButterflyFunctionCall {
	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		RequestContext rc = getParameter(context, CoreConstants.REQUEST_CONTEXT_PARAM);

		String path = arguments[0].head().getStringValue();
		BindStatus status = new BindStatus(rc, path, false);

		Object res = call(status, path);
		if (res == null) {
			return EmptySequence.getInstance();
		}

		return JPConverter.allocate(res.getClass(), null, context.getConfiguration())
				.convert(res, context);
	}

	protected abstract Object call(BindStatus status, String path);
}
