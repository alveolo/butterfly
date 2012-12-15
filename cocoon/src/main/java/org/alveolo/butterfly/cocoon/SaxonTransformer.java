package org.alveolo.butterfly.cocoon;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.alveolo.butterfly.saxon.xpath.SaxonConfiguration;
import org.alveolo.butterfly.saxon.xpath.functions.CoreConstants;
import org.alveolo.butterfly.saxon.xpath.functions.Message;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.sax.component.TraxErrorListener;
import org.apache.cocoon.sax.util.SAXConsumerAdapter;
import org.apache.cocoon.sax.util.ValidityValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.xml.sax.SAXException;


public class SaxonTransformer extends AbstractSAXTransformer implements ApplicationContextAware {
	private static final Logger LOG = LoggerFactory.getLogger(SaxonTransformer.class);

	private static final Map<String, ValidityValue<TransformerTracker>> XSLT_CACHE =
			new ConcurrentHashMap<String, ValidityValue<TransformerTracker>>();

	private static final SAXTransformerFactory GENERIC_FACTORY = createNewSAXTransformerFactory();

	private static final Pattern XSLT_PARAMETER_NAME_PATTERN = Pattern.compile("[a-zA-Z_][\\w\\-\\.]*");

	private ApplicationContext context;

	private Map<String, Object> parameters;
	private Map<String, Object> configuration;

	private URL source;

	private Templates templates;

	/**  Empty constructor, used in sitemap. */
	public SaxonTransformer() {}

	/**
	 * Creates a new transformer reading the XSLT from the URL source.
	 *
	 * @param source the XSLT URL source
	 */
	public SaxonTransformer(final URL source) {
		this(source, null);
	}

	/**
	 * Creates a new transformer reading the XSLT from the URL source and
	 * setting the TransformerFactory attributes.
	 *
	 * This constructor is useful when users want to perform XSLT transformation
	 * using <a href="http://xml.apache.org/xalan-j/xsltc_usage.html">xsltc</a>.
	 *
	 * @param source the XSLT URL source
	 * @param attributes the Transformer Factory attributes
	 */
	public SaxonTransformer(final URL source, final Map<String, Object> attributes) {
		loadXSLT(source, attributes);
	}

	@Override
	public void setApplicationContext(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public void setup(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setConfiguration(final Map<String, ? extends Object> configuration) {
		try {
			source = (URL) configuration.get("source");
		} catch (ClassCastException cce) {
			throw new SetupException("The configuration value of 'source' can't be cast to java.net.URL.", cce);
		}

		if (source != null) {
			Object attributesObj = configuration.get("attributes");
			if (attributesObj != null && attributesObj instanceof Map) {
				loadXSLT(source, (Map) attributesObj);
			} else {
				loadXSLT(source, null);
			}
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Impossible to load XSLT parameters from '" + source +
						"' source, make sure it is NOT null and is a valid URL");
			}
		}

		this.configuration = new HashMap<String, Object>(configuration);
	}

	private void loadXSLT(final URL source, final Map<String, Object> attributes) {
		if (source == null) {
			throw new IllegalArgumentException("The parameter 'source' mustn't be null.");
		}

		this.source = source;
		templates = null;

		long lastModified = 0;
		try {
			URLConnection connection = source.openConnection();
			connection.setDoInput(false);
			lastModified = connection.getLastModified();
		} catch (IOException e) {}

		TransformerTracker tracker = null;

		// check the XSLT is in the cache first
		if (XSLT_CACHE.containsKey(source.toExternalForm())) {
			// get the XSLT directly from the cache
			ValidityValue<TransformerTracker> cacheEntry = XSLT_CACHE.get(source.toExternalForm());
			if (cacheEntry.getLastModified() == lastModified) {
				tracker = cacheEntry.getValue();
				if (tracker.hasChanged()) {
					tracker = null;
				}
			}

			if (tracker == null) {
				XSLT_CACHE.remove(source.toExternalForm());
			}
		}

		if (tracker == null) {
			// XSLT has to be parsed
			Source urlSource = new StreamSource(source.toExternalForm());

			SAXTransformerFactory transformerFactory = createNewSAXTransformerFactory();
			if (attributes != null && !attributes.isEmpty()) {
				for (Entry<String, Object> attribute : attributes.entrySet()) {
					transformerFactory.setAttribute(attribute.getKey(), attribute.getValue());
				}
			}

			try {
				tracker = new TransformerTracker(transformerFactory, urlSource);

				// store the XSLT into the cache for future reuse
				ValidityValue<TransformerTracker> cacheEntry =
						new ValidityValue<TransformerTracker>(tracker, lastModified);

				XSLT_CACHE.put(source.toExternalForm(), cacheEntry);
			} catch (TransformerConfigurationException e) {
				throw new SetupException("Impossible to read XSLT from '" + source.toExternalForm() +
						"', see nested exception", e);
			}
		}

		templates = tracker.templates;
	}

	@Override
	protected void setSAXConsumer(final SAXConsumer consumer) {
		TransformerHandler handler = createTransformerHandler();

		if (consumer instanceof SaxonSerializer) {
			// serializer will finish setup of handler result
			SaxonSerializer serializer = (SaxonSerializer) consumer;
			serializer.setTransformerHandler(handler);
		} else {
			final SAXResult result = new SAXResult();
			result.setHandler(consumer);
			// According to TrAX specification, all TransformerHandlers are LexicalHandlers
			result.setLexicalHandler(consumer);
			handler.setResult(result);
		}

		SAXConsumerAdapter saxConsumerAdapter = new SAXConsumerAdapter();
		saxConsumerAdapter.setContentHandler(handler);

		super.setSAXConsumer(saxConsumerAdapter);
	}

	private TransformerHandler createTransformerHandler() {
		try {
			TransformerHandler transformerHandler = GENERIC_FACTORY.newTransformerHandler(templates);

			final Transformer transformer = transformerHandler.getTransformer();

			if (configuration != null) {
				for (Entry<String, Object> entry : configuration.entrySet()) {
					String name = entry.getKey();

					// is valid XSLT parameter name
					if (XSLT_PARAMETER_NAME_PATTERN.matcher(name).matches()) {
						transformer.setParameter(name, entry.getValue());
					}
				}
			}

			transformer.setErrorListener(new TraxErrorListener(LOG, source.toExternalForm()));

			transformer.setParameter(CoreConstants.APPLICATION_CONTEXT_PARAM, context);

			Map<String, Object> model = HttpContextHelper.getModel(parameters);
			transformer.setParameter(CoreConstants.MODEL_PARAM, model);

			if (parameters != null) try {
				ServletContext servletContext = HttpContextHelper.getServletContext(parameters);
				transformer.setParameter(CoreConstants.SERVLET_CONTEXT_PARAM, servletContext);

				HttpServletRequest servletRequest = HttpContextHelper.getRequest(parameters);
				HttpServletResponse servletResponse = HttpContextHelper.getResponse(parameters);

				if (servletRequest != null) {
					transformer.setParameter(Message.LOCALE_PARAM, RequestContextUtils.getLocale(servletRequest));

					transformer.setParameter(CoreConstants.SERVLET_REQUEST_PARAM, servletRequest);
					transformer.setParameter(CoreConstants.SERVLET_SESSION_PARAM, servletRequest.getSession(false));

					// add support for form binding
					transformer.setParameter(CoreConstants.REQUEST_CONTEXT_PARAM,
							new RequestContext(servletRequest, servletResponse, servletContext, model));
				}

				if (servletResponse != null) {
					transformer.setParameter(CoreConstants.SERVLET_RESPONSE_PARAM, servletResponse);
				}
			} catch (IllegalStateException e) {
				LOG.debug("Not a Servlet request!", e);

//				@SuppressWarnings({"rawtypes", "unchecked"})
//				HashMap<String, Object> model = (HashMap) parameters.get("model");
//
//				HttpServletRequest servletRequest = new VirtualRequest();
//				servletRequest.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
//				RequestContext requestContext = new RequestContext(servletRequest, model);
//
//				transformer.setParameter(CoreConstants.REQUEST_CONTEXT_PARAM, requestContext);
			}

			return transformerHandler;
		} catch (TransformerConfigurationException ex) {
			throw new SetupException("Could not initialize transformer handler.", ex);
		}
	}

	/**
	 * Utility method to create a new transformer factory.
	 *
	 * @return a new transformer factory
	 */
	private static SAXTransformerFactory createNewSAXTransformerFactory() {
		TransformerFactoryImpl factory = new TransformerFactoryImpl(new SaxonConfiguration());

		return factory;
	}

	@Override
	public String toString() {
		return StringRepresentation.buildString(this, "src=" + source);
	}

	@Override
	public String endTextRecording() throws SAXException {
		// TODO Auto-generated method stub
		return super.endTextRecording();
	}
}
