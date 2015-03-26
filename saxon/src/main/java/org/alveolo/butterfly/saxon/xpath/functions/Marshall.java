package org.alveolo.butterfly.saxon.xpath.functions;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;

import org.alveolo.butterfly.fixup.spring.MarshallingSource;
import org.springframework.context.ApplicationContext;
import org.springframework.oxm.Marshaller;


@SuppressWarnings("serial")
public class Marshall extends ExtensionFunctionDefinition {
	private static final StructuredQName qName =
			new StructuredQName(CoreConstants.PREFIX, CoreConstants.NAMESPACE, "marshall");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
				SequenceType.ANY_SEQUENCE, // name
		};
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.NODE_SEQUENCE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new RequestAttributeCall();
	}

	private static class RequestAttributeCall extends ExtensionFunctionCall {
		@Override
		public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
			List<Item> items = new ArrayList<Item>();

			Marshaller marshaller = getMarshaller(context);

			JPConverter converter = JPConverter.FromSource.INSTANCE;

			Sequence argument = arguments[0];
			SequenceIterator<? extends Item> i = argument.iterate();
			while (true) {
				Item item = i.next();
				if (item == null) {
					break;
				}

				if (item instanceof ObjectValue) {
					Object object = ((ObjectValue) item).getObject();
					MarshallingSource source = new MarshallingSource(marshaller, object);
					DocumentInfo doc = (DocumentInfo) converter.convert(source, context);
					items.add(doc);
				} else {
					throw new XPathException("Not an object!", getContainer());
				}
			}

			return new SequenceExtent<Item>(items);
		}

		private Marshaller getMarshaller(XPathContext context) {
			return (Marshaller) getApplicationContext(context).getBean("marshaller");
		}

		private ApplicationContext getApplicationContext(XPathContext context) {
			return (ApplicationContext) context.getController().getParameter(
					CoreConstants.APPLICATION_CONTEXT_PARAM.getClarkName());
		}
	}
}
