package org.alveolo.butterfly.saxon.xpath.bind;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Value;

import org.alveolo.butterfly.saxon.xpath.functions.CoreConstants;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.support.RequestContext;


@SuppressWarnings("serial")
public abstract class AbstractPropertyCall extends ExtensionFunctionCall {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public SequenceIterator call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
		String path = arguments[0].next().getStringValue();

		int dotPos = path.indexOf('.');
		if (dotPos < 0) {
			return EmptySequence.getInstance().iterate();
		}

		Controller controller = context.getController();
		RequestContext rc = (RequestContext) controller.getParameter(CoreConstants.REQUEST_CONTEXT_PARAM);

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
			return EmptyIterator.getInstance();
		}

		JPConverter converter = JPConverter.allocate(res.getClass(), context.getConfiguration());
		return Value.getIterator(converter.convert(res, context));
	}

	protected abstract Object call(Class<?> type, String propertyName);
}
