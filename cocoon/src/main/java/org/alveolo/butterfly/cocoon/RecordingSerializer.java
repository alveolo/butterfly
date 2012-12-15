package org.alveolo.butterfly.cocoon;

import java.util.ArrayList;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.sax.AbstractSAXSerializer;
import org.apache.cocoon.sax.util.SAXConsumerAdapter;
import org.apache.cocoon.xml.sax.DefaultLexicalHandler;
import org.apache.cocoon.xml.sax.SAXBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;


public class RecordingSerializer extends AbstractSAXSerializer {
	private static final Logger LOG = LoggerFactory.getLogger(RecordingSerializer.class);

	private ArrayList<PrefixMapping> namespaces = new ArrayList<PrefixMapping>();

	private ContentHandler originalContentHandler;
	private LexicalHandler originalLexicalHandler;

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		namespaces.add(new PrefixMapping(prefix, uri));
		contentHandler.startPrefixMapping(prefix, uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		for (int i = namespaces.size() - 1; i >= 0; i--) {
			if (namespaces.get(i).prefix.equals(prefix)) {
				namespaces.remove(i);
				contentHandler.endPrefixMapping(prefix);
				return;
			}
		}

		throw new SAXException("Namespace for prefix '" + prefix + "' not found.");
	}

	protected String findPrefixMapping(String uri) {
		for (PrefixMapping pm : namespaces) {
			if (pm.uri.equals(uri)) {
				return pm.prefix;
			}
		}

		return null;
	}

	protected void setRecorder(ContentHandler recorder) {
		if (originalContentHandler != null) {
			throw new ProcessingException("Only one recorder can be set.");
		}

		originalContentHandler = contentHandler;
		originalLexicalHandler = lexicalHandler;

		Object adapter;
		if (recorder instanceof LexicalHandler) {
			adapter = recorder;
		} else {
			SAXConsumerAdapter saxConsumerAdapter = new SAXConsumerAdapter();
			saxConsumerAdapter.setContentHandler(recorder);
			adapter = saxConsumerAdapter;
		}

		contentHandler = recorder;

		lexicalHandler = (recorder instanceof LexicalHandler)
				? (LexicalHandler) adapter : DefaultLexicalHandler.NULL_HANDLER;
	}

	protected ContentHandler removeRecorder() {
		ContentHandler recorder = contentHandler;

		contentHandler = originalContentHandler;
		lexicalHandler = originalLexicalHandler;

		originalContentHandler = null;
		originalLexicalHandler = null;

		return recorder;
	}

	public void startSerializingRecording(ContentHandler recorder) throws SAXException {
		setRecorder(recorder);
		contentHandler.startDocument();
		sendStartPrefixMapping();
	}

	public void endSerializingRecording() throws SAXException {
		sendEndPrefixMapping();
		contentHandler.endDocument();
		removeRecorder();
	}

	public void startSAXRecording() throws SAXException {
		setRecorder(new SAXBuffer());
		sendStartPrefixMapping();
	}

	public SAXBuffer endSAXRecording() throws SAXException {
		sendEndPrefixMapping();
		return (SAXBuffer) removeRecorder();
	}

	public void startTextRecording() throws SAXException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Start text recording");
		}

		setRecorder(new TextRecorder());
		sendStartPrefixMapping();
	}

	public String endTextRecording() throws SAXException {
		sendEndPrefixMapping();

		TextRecorder recorder = (TextRecorder) removeRecorder();
		String text = recorder.getText();

		if (LOG.isDebugEnabled()) {
			LOG.debug("End text recording. Text=" + text);
		}

		return text;
	}

	protected void sendStartPrefixMapping() throws SAXException {
		for (PrefixMapping pm : namespaces) {
			contentHandler.startPrefixMapping(pm.prefix, pm.uri);
		}
	}

	protected void sendEndPrefixMapping() throws SAXException {
		for (int i = namespaces.size() - 1; i >= 0; i--) {
			contentHandler.endPrefixMapping(namespaces.get(i).prefix);
		}
	}
}
