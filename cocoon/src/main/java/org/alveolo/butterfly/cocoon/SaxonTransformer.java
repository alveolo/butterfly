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
import javax.servlet.http.HttpSession;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.alveolo.butterfly.saxon.xpath.ButterflyConfiguration;
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

import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.jaxp.TransformerHandlerImpl;
import net.sf.saxon.jaxp.TransformerImpl;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmExternalObject;
import net.sf.saxon.s9api.XsltTransformer;


public class SaxonTransformer extends AbstractSAXTransformer implements ApplicationContextAware {
	private static final Logger LOG = LoggerFactory.getLogger(SaxonTransformer.class);

	private static final Map<String, ValidityValue<TransformerTracker>> XSLT_CACHE =
			new ConcurrentHashMap<String, ValidityValue<TransformerTracker>>();

	private static final TransformerFactoryImpl GENERIC_FACTORY = createNewSAXTransformerFactory();

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
	public SaxonTransformer(ApplicationContext context, URL source) {
		setApplicationContext(context);
		loadXSLT(source, null);
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
	public void setConfiguration(Map<String, ? extends Object> configuration) {
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

	private void loadXSLT(URL source, Map<String, Object> attributes) {
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

			TransformerFactoryImpl transformerFactory = createNewSAXTransformerFactory();
			if (attributes != null && !attributes.isEmpty()) {
				for (Entry<String, Object> attribute : attributes.entrySet()) {
					transformerFactory.setAttribute(attribute.getKey(), attribute.getValue());
				}
			}

			try {
				tracker = new TransformerTracker(context, transformerFactory, urlSource);

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
	protected void setSAXConsumer(SAXConsumer consumer) {
		TransformerHandler handler = createTransformerHandler();

		if (consumer instanceof SaxonSerializer) {
			// serializer will finish setup of handler result
			SaxonSerializer serializer = (SaxonSerializer) consumer;
			serializer.setTransformerHandler(handler);
		} else {
			SAXResult result = new SAXResult();
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
			TransformerHandlerImpl transformerHandler =
					(TransformerHandlerImpl) GENERIC_FACTORY.newTransformerHandler(templates);

			TransformerImpl impl = (TransformerImpl) transformerHandler.getTransformer();
			XsltTransformer xslt = impl.getUnderlyingXsltTransformer();
			Controller controller = (Controller) xslt.getUnderlyingController();

			if (configuration != null) {
				for (Entry<String, Object> entry : configuration.entrySet()) {
					String name = entry.getKey();

					// is valid XSLT parameter name
					if (XSLT_PARAMETER_NAME_PATTERN.matcher(name).matches()) {
						xslt.setParameter(new QName(name), new XdmExternalObject(entry.getValue()));
					}
				}
			}

			controller.setErrorListener(new TraxErrorListener(LOG, source.toExternalForm()));

			if (context != null) {
				xslt.setParameter(CoreConstants.APPLICATION_CONTEXT_PARAM,
						new XdmExternalObject(context));
			}

			Map<String, Object> model = HttpContextHelper.getModel(parameters);
			xslt.setParameter(CoreConstants.MODEL_PARAM, new XdmExternalObject(model));

			if (parameters != null) try {
				ServletContext servletContext = HttpContextHelper.getServletContext(parameters);
				xslt.setParameter(CoreConstants.SERVLET_CONTEXT_PARAM,
						new XdmExternalObject(servletContext));

				HttpServletRequest servletRequest = HttpContextHelper.getRequest(parameters);
				HttpServletResponse servletResponse = HttpContextHelper.getResponse(parameters);

				if (servletRequest != null) {
					controller.setURIResolver(new SaxonResolver(servletRequest, servletResponse));

					xslt.setParameter(Message.LOCALE_PARAM,
							new XdmExternalObject(RequestContextUtils.getLocale(servletRequest)));

					xslt.setParameter(CoreConstants.SERVLET_REQUEST_PARAM,
							new XdmExternalObject(servletRequest));

					HttpSession session = servletRequest.getSession(false);
					if (session != null) {
						xslt.setParameter(CoreConstants.SERVLET_SESSION_PARAM,
								new XdmExternalObject(session));
					}

					// add support for form binding
					xslt.setParameter(CoreConstants.REQUEST_CONTEXT_PARAM, new XdmExternalObject(
							new RequestContext(servletRequest, servletResponse, servletContext, model)));
				}

				if (servletResponse != null) {
					xslt.setParameter(CoreConstants.SERVLET_RESPONSE_PARAM,
							new XdmExternalObject(servletResponse));
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
	private static TransformerFactoryImpl createNewSAXTransformerFactory() {
		TransformerFactoryImpl factory = new TransformerFactoryImpl(new ButterflyConfiguration());

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
