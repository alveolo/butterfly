package org.alveolo.butterfly.fixup.spring;

import java.io.IOException;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;

import org.springframework.oxm.Marshaller;
import org.springframework.util.Assert;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;


/**
 * Copy of Spring 3.0 MarshallingSource that accepts XMLReader configuration properties.
 *
 * Workaround for bug... JAXBSource does it well, but Spring one is not fully emulates this :(
 */
public class MarshallingSource extends SAXSource {
	private final Marshaller marshaller;

	private final Object content;

	public MarshallingSource(Marshaller marshaller, Object content) {
		super(new MarshallingXMLReader(marshaller, content), new InputSource());
		Assert.notNull(marshaller, "'marshaller' must not be null");
		Assert.notNull(content, "'content' must not be null");
		this.marshaller = marshaller;
		this.content = content;
	}

	public Marshaller getMarshaller() {
		return this.marshaller;
	}

	public Object getContent() {
		return this.content;
	}

	@Override
	public void setInputSource(InputSource inputSource) {
		throw new UnsupportedOperationException("setInputSource is not supported");
	}

	@Override
	public void setXMLReader(XMLReader reader) {
		throw new UnsupportedOperationException("setXMLReader is not supported");
	}

	private static class MarshallingXMLReader implements XMLReader {
		private final Marshaller marshaller;
		private final Object content;
		private DTDHandler dtdHandler;
		private ContentHandler contentHandler;
		private EntityResolver entityResolver;
		private ErrorHandler errorHandler;
		private LexicalHandler lexicalHandler;

		private MarshallingXMLReader(Marshaller marshaller, Object content) {
			Assert.notNull(marshaller, "'marshaller' must not be null");
			Assert.notNull(content, "'content' must not be null");
			this.marshaller = marshaller;
			this.content = content;
		}

		@Override
		public void setContentHandler(ContentHandler contentHandler) {
			this.contentHandler = contentHandler;
		}

		@Override
		public ContentHandler getContentHandler() {
			return this.contentHandler;
		}

		@Override
		public void setDTDHandler(DTDHandler dtdHandler) {
			this.dtdHandler = dtdHandler;
		}

		@Override
		public DTDHandler getDTDHandler() {
			return this.dtdHandler;
		}

		@Override
		public void setEntityResolver(EntityResolver entityResolver) {
			this.entityResolver = entityResolver;
		}

		@Override
		public EntityResolver getEntityResolver() {
			return this.entityResolver;
		}

		@Override
		public void setErrorHandler(ErrorHandler errorHandler) {
			this.errorHandler = errorHandler;
		}

		@Override
		public ErrorHandler getErrorHandler() {
			return this.errorHandler;
		}

		protected LexicalHandler getLexicalHandler() {
			return this.lexicalHandler;
		}

		@Override
		public boolean getFeature(String name) throws SAXNotRecognizedException {
			if ("http://xml.org/sax/features/namespaces".equals(name)) {
				return true;
			}
			if ("http://xml.org/sax/features/namespace-prefixes".equals(name)) {
				return false;
			}
			if ("http://xml.org/sax/features/validation".equals(name)) {
				return false;
			}
			throw new SAXNotRecognizedException(name);
		}

		@Override
		public void setFeature(String name, boolean value) throws SAXNotRecognizedException {
			if ("http://xml.org/sax/features/namespaces".equals(name) && value) {
				return;
			}
			if ("http://xml.org/sax/features/namespace-prefixes".equals(name) && !value) {
				return;
			}
			if ("http://xml.org/sax/features/validation".equals(name) && !value) {
				return;
			}
			throw new SAXNotRecognizedException(name);
		}

		@Override
		public Object getProperty(String name) throws SAXNotRecognizedException {
			if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
				return lexicalHandler;
			}
			throw new SAXNotRecognizedException(name);
		}

		@Override
		public void setProperty(String name, Object value) throws SAXNotRecognizedException {
			if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
				this.lexicalHandler = (LexicalHandler) value;
			}
			throw new SAXNotRecognizedException(name);
		}

		@Override
		public void parse(InputSource input) throws SAXException {
			parse();
		}

		@Override
		public void parse(String systemId) throws SAXException {
			parse();
		}

		private void parse() throws SAXException {
			SAXResult result = new SAXResult(getContentHandler());
			result.setLexicalHandler(getLexicalHandler());
			try {
				this.marshaller.marshal(this.content, result);
			} catch (IOException ex) {
				SAXParseException saxException = new SAXParseException(ex.getMessage(), null, null, -1, -1, ex);
				ErrorHandler errorHandler = getErrorHandler();
				if (errorHandler == null) {
					throw saxException;
				}
				errorHandler.fatalError(saxException);
			}
		}
	}
}
