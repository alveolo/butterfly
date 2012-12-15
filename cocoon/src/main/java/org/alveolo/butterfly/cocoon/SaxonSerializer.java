package org.alveolo.butterfly.cocoon;

import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.sax.util.SAXConsumerAdapter;


/**
 * Can only be used right after of SaxonTransformer. Moves serialization responsibility to preceding transformer
 * and enables configuration of output properties through xsl:output instruction of corresponding stylesheet.
 */
public class SaxonSerializer extends SAXConsumerAdapter implements SAXPipelineComponent, Finisher {
	private TransformerHandler handler;

	public SaxonSerializer() {}

	@Override
	public void setup(Map<String, Object> parameters) {}

	@Override
	public void setConfiguration(Map<String, ? extends Object> configuration) {}

	@Override
	public void finish() {}

	public void setTransformerHandler(TransformerHandler handler) {
		this.handler = handler;
	}

	/**
	 * More efficient method than using separate serializer in case
	 * of Saxon transformation is the last one in the pipeline.
	 */
	@Override
	public void setOutputStream(OutputStream outputStream) {
		if (handler == null) {
			// We are not really serializing XSLT stylesheet directly, but still want Saxon to do the serialization
			// Use identity Saxon transform if we cannot delegate serialization to last Saxon transformer
			// TODO: somehow configure serialization parameters in this case
			try {
				handler = new TransformerFactoryImpl().newTransformerHandler();
			} catch (TransformerConfigurationException e) {
				throw new IllegalStateException(e);
			}

			setContentHandler(handler);
		}

		handler.setResult(new StreamResult(outputStream));
	}

	@Override
	public String getContentType() {
		Properties props = handler.getTransformer().getOutputProperties();

		String method = props.getProperty(OutputKeys.METHOD);
		if (method == null) {
			method = "xml";
		}

		String type = props.getProperty(OutputKeys.MEDIA_TYPE);
		if (type == null) {
			type = method.equals("html") ? "text/html"
				: method.equals("text") ? "text/plain"
				: "text/xml";
		}

		String charset = props.getProperty(OutputKeys.ENCODING);
		if (charset == null) {
			charset = "UTF-8";
		}

		return type + "; charset=" + charset;
	}

	@Override
	public String toString() {
		return StringRepresentation.buildString(this);
	}
}
