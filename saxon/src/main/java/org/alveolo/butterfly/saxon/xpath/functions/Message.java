package org.alveolo.butterfly.saxon.xpath.functions;

import java.util.Locale;

import org.alveolo.butterfly.saxon.xpath.ButterflyFunctionCall;
import org.springframework.context.MessageSource;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;


public class Message extends ExtensionFunctionDefinition {
	public static final String I18N_PREFIX = "i18n", I18N_NAMESPACE = "http://alveolo.org/cocoon/i18n";
	public static final QName LOCALE_PARAM = new QName(I18N_PREFIX, I18N_NAMESPACE, "locale");

	private static final StructuredQName qName = new StructuredQName(I18N_PREFIX, I18N_NAMESPACE, "message");

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

	private static class MessageFunctionCall extends ButterflyFunctionCall {
		@Override
		public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
			String code = arguments[0].head().getStringValue();

			Object[] args = new Object[arguments.length-1];
			for (int i = 0; i < args.length; i++) {
				// TODO: support integers/floats/dates/times
				args[i] = arguments[i+1].head().getStringValue();
			}

			Locale locale = getParameter(context, LOCALE_PARAM);
			if (locale == null) {
				locale = Locale.getDefault();
			}

			MessageSource source = getParameter(context, CoreConstants.APPLICATION_CONTEXT_PARAM);

			return StringValue.makeStringValue(source.getMessage(code, args, locale));
		}
	}
}
