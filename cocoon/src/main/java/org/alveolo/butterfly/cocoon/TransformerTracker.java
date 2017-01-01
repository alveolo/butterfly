package org.alveolo.butterfly.cocoon;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.springframework.context.ApplicationContext;

class TransformerTracker implements URIResolver {
	private final List<Dependency> dependencies = new ArrayList<Dependency>();
	private final ApplicationContext context;

	final Templates templates;

	TransformerTracker(ApplicationContext context, SAXTransformerFactory transformerFactory, Source urlSource)
	throws TransformerConfigurationException {
		this.context = context;
		transformerFactory.setURIResolver(this);
		templates = transformerFactory.newTemplates(urlSource);
	}

	boolean hasChanged() {
		for (Dependency d : dependencies) {
			if (d.hasChanged()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		try {
			URI uri;

			if (href.startsWith("classpath:")) {
				uri = context.getResource(href).getURI();
			} else {
				uri = new URI(base);

				if (href.length() > 0) {
					uri = uri.resolve(href);
				}
			}

			if (uri.toString().startsWith("file:")) {
				dependencies.add(new Dependency(uri));
			}

			return new StreamSource(uri.toString());
		} catch (URISyntaxException ignore) {
		} catch (IOException ignore) {}

		return null;
	}
}
