package org.alveolo.butterfly.saxon.xpath.bind;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;

import org.alveolo.butterfly.saxon.xpath.functions.CoreConstants;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.support.RequestContext;


@SuppressWarnings("serial")
public abstract class AbstractPropertyCall extends ExtensionFunctionCall {
	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		String path = arguments[0].head().getStringValue();

		int dotPos = path.indexOf('.');
		if (dotPos < 0) {
			return EmptySequence.getInstance();
		}

		Controller controller = context.getController();
		RequestContext rc = (RequestContext) controller.getParameter(CoreConstants.REQUEST_CONTEXT_PARAM.getClarkName());

		BindingResult errors = (BindingResult) rc.getErrors(path.substring(0, dotPos));
		String expression = path.substring(dotPos + 1);

		Class<?> type;
		String propertyName;

		dotPos = expression.lastIndexOf('.');
		if (dotPos < 0) {
			type = errors.getTarget().getClass();
			propertyName = expression;
		} else {
			type = errors.getFieldType(expression.substring(0, dotPos));
			propertyName = expression.substring(dotPos + 1);
		}

		Object res = call(type, propertyName);
		if (res == null) {
			return EmptySequence.getInstance();
		}

		JPConverter converter = JPConverter.allocate(res.getClass(), context.getConfiguration());
		return converter.convert(res, context);
	}

	protected abstract Object call(Class<?> type, String propertyName);
}
