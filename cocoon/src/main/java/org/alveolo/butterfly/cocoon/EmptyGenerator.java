package org.alveolo.butterfly.cocoon;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.sax.AbstractSAXGenerator;
import org.apache.cocoon.sax.SAXConsumer;
import org.xml.sax.helpers.AttributesImpl;


public class EmptyGenerator extends AbstractSAXGenerator {
	private static final String ELEM_ROOT = "empty";

	@Override
	public void execute() {
		SAXConsumer consumer = getSAXConsumer();
		try {
			consumer.startDocument();

			consumer.startElement("", ELEM_ROOT, ELEM_ROOT, new AttributesImpl());
			consumer.endElement("", ELEM_ROOT, ELEM_ROOT);

			consumer.endDocument();
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
	}
}
