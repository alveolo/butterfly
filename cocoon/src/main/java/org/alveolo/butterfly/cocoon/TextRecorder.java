package org.alveolo.butterfly.cocoon;

import org.apache.cocoon.xml.sax.SAXBuffer;


@SuppressWarnings("serial")
class TextRecorder extends SAXBuffer {
	private final StringBuffer buffer = new StringBuffer();

	@Override
	public void characters(char ary[], int start, int length) {
		buffer.append(ary, start, length);
	}

	public String getText() {
		return buffer.toString();
	}
}
