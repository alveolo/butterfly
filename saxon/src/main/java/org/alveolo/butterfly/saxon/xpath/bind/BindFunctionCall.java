package org.alveolo.butterfly.saxon.xpath.bind;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;

import org.alveolo.butterfly.saxon.xpath.functions.CoreConstants;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;


@SuppressWarnings("serial")
public abstract class BindFunctionCall extends ExtensionFunctionCall {
	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		Controller controller = context.getController();
		RequestContext rc = (RequestContext) controller.getParameter(
				CoreConstants.REQUEST_CONTEXT_PARAM.getClarkName());

		String path = arguments[0].head().getStringValue();
		BindStatus status = new BindStatus(rc, path, false);

		Object res = call(status, path);

		if (res == null) {
			return EmptySequence.getInstance();
		}

		JPConverter converter = JPConverter.allocate(res.getClass(), context.getConfiguration());

		return converter.convert(res, context);
	}

	protected abstract Object call(BindStatus status, String path);
}
