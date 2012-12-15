package org.alveolo.butterfly.cocoon;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.SaxonOutputKeys;

import org.alveolo.butterfly.saxon.xpath.SaxonConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class JavaMailSerializer extends RecordingSerializer {
	private static final String NAMESPACE = "http://alveolo.org/cocoon/javamail";

	private final SAXTransformerFactory factory = createTransformerFactory();

	private final MimeMessage message;

	private final List<MimePart> partStack = new LinkedList<MimePart>();

	private List<String> nsPrefixes = new LinkedList<String>();

	private ByteArrayOutputStream contentBuffer;

	public JavaMailSerializer(MimeMessage message) {
		this.message = message;
	}

	protected MimePart getPart() {
		return partStack.get(partStack.size() - 1);
	}

	@Override
	public void startDocument() throws SAXException {
		partStack.add(message);
	}

	@Override
	public void endDocument() throws SAXException {
		partStack.remove(partStack.size() - 1);
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		if (NAMESPACE.equals(uri)) {
			nsPrefixes.add(prefix);
			return;
		}

		super.startPrefixMapping(prefix, uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		if (nsPrefixes.remove(prefix)) {
			return;
		}
		super.endPrefixMapping(prefix);
	}

	@Override
	public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
		if (NAMESPACE.equals(uri)) {
			if ("header".equals(loc)) {
				try {
					getPart().addHeader(a.getValue("name"), a.getValue("value"));
				} catch (MessagingException e) {
					throw new SAXException(e);
				}
				return;
			}

			if ("subject".equals(loc)) {
				if (getPart() != message) {
					throw new IllegalStateException("Subject is not expected here!");
				}
				startTextRecording();
				return;
			}

			if ("content".equals(loc)) {
				if (contentBuffer != null) {
					throw new IllegalStateException("Recursive content is not allowed!");
				}

				contentBuffer = new ByteArrayOutputStream();

				try {
					TransformerHandler handler = factory.newTransformerHandler();
					Transformer transformer = handler.getTransformer();
					transformer.setOutputProperty(OutputKeys.METHOD, "html");
					transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
					transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					transformer.setOutputProperty(SaxonOutputKeys.INDENT_SPACES, "1");
					transformer.setOutputProperty(SaxonOutputKeys.LINE_LENGTH, "10000");

					handler.setResult(new StreamResult(contentBuffer));
					startSerializingRecording(handler);
				} catch (TransformerConfigurationException e) {
					throw new SAXException(e);
				}
			}
		} else {
			super.startElement(uri, loc, raw, a);
		}
	}

	@Override
	public void endElement(String uri, String loc, String raw) throws SAXException {
		if (NAMESPACE.equals(uri)) {
			if ("subject".equals(loc)) {
				String text = endTextRecording();
				try {
					message.setSubject(text);
				} catch (MessagingException e) {
					throw new SAXException(e);
				}
				return;
			}

			if ("content".equals(loc)) {
				endSerializingRecording();
				byte[] content = contentBuffer.toByteArray();
				contentBuffer = null;

				// TODO: Use Content-Type from source document instead of hardcoded HTML/UTF8
				DataSource ds = new ByteArrayDataSource(content, "text/html; charset=UTF-8");
				DataHandler dh = new DataHandler(ds);

				try {
					getPart().setDataHandler(dh);
				} catch (MessagingException e) {
					throw new SAXException(e);
				}

				return;
			}
		} else {
			super.endElement(uri, loc, raw);
		}
	}

	// TODO: Multipart message composition, attachments and embedding images/styles into e-mail.

	private static TransformerFactoryImpl createTransformerFactory() {
		TransformerFactoryImpl tf = new TransformerFactoryImpl(new SaxonConfiguration());
		tf.getConfiguration().setProcessor(tf);
		return tf;
	}
}
