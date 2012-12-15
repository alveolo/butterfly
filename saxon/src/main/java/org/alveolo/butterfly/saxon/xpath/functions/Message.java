package org.alveolo.butterfly.saxon.xpath.functions;

import java.util.Locale;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.springframework.context.MessageSource;


@SuppressWarnings("serial")
public class Message extends ExtensionFunctionDefinition {
	public static final String I18N_NAMESPACE = "http://alveolo.org/cocoon/i18n";
	public static final String LOCALE_PARAM = I18N_NAMESPACE + "/locale";

	private static final StructuredQName qName = new StructuredQName("", I18N_NAMESPACE, "message");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public int getMinimumNumberOfArguments() {
		return 1;
	}

	@Override
	public int getMaximumNumberOfArguments() {
		return Integer.MAX_VALUE;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
				SequenceType.SINGLE_STRING, // key
				SequenceType.SINGLE_ATOMIC, // zero or more arguments
		};
	}

	@Override
	public boolean trustResultType() {
		return true;
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_STRING;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new MessageFunctionCall();
	}

	private static class MessageFunctionCall extends ExtensionFunctionCall {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public SequenceIterator call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
			String code = arguments[0].next().getStringValue();

			Object[] args = new Object[arguments.length-1];
			for (int i = 0; i < args.length; i++) {
				// TODO: support integers/floats/dates/times
				args[i] = arguments[i+1].next().getStringValue();
			}

			Locale locale = getLocale(context);

			MessageSource source = getMessageSource(context);
			String message = source.getMessage(code, args, locale);
			return SingletonIterator.makeIterator(StringValue.makeStringValue(message));
		}

		private MessageSource getMessageSource(XPathContext context) {
			return (MessageSource) context.getController().getParameter(CoreConstants.APPLICATION_CONTEXT_PARAM);
		}

		private Locale getLocale(XPathContext context) {
			Locale locale = (Locale) context.getController().getParameter(LOCALE_PARAM);
			return (locale == null) ? Locale.getDefault() : locale;
		}
	}
}
