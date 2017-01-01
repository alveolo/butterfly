/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.sax.component;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.pipeline.util.URLConnectionUtils;
import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.sax.util.InMemoryLRUResourceCache;
import org.apache.cocoon.sax.util.SAXConsumerAdapter;
import org.apache.cocoon.sax.util.ValidityValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLTTransformer extends AbstractSAXTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(XSLTTransformer.class);

    /**
     * The memory based LRU cache for already loaded XSLTs.
     */
    private static final InMemoryLRUResourceCache<Templates> XSLT_CACHE = new InMemoryLRUResourceCache<Templates>();

    /**
     * A generic transformer factory to parse XSLTs.
     */
    private static final SAXTransformerFactory TRAX_FACTORY = createNewSAXTransformerFactory();

    /**
     * The XSLT parameters name pattern.
     */
    private static final Pattern XSLT_PARAMETER_NAME_PATTERN = Pattern.compile("[a-zA-Z_][\\w\\-\\.]*");

    private static final String SOURCE = "source";

    /**
     * The XSLT parameters reference.
     */
    private Map<String, Object> parameters;

    /**
     * The XSLT Template reference.
     */
    private Templates templates;

    /**
     * The XSLT URL source.
     */
    private URL url;

    private long lastModified;

    private Source source;

    /**
     * Empty constructor, used in sitemap.
     */
    public XSLTTransformer() {
    }

    /**
     * Creates a new transformer reading the XSLT from the URL source.
     *
     * @param url the XSLT URL source
     */
    public XSLTTransformer(final URL url) {
        this(url, null);
    }

    /**
     * Creates a new transformer reading the XSLT from the URL source and setting the TransformerFactory attributes.
     *
     * This constructor is useful when users want to perform XSLT transformation using <a
     * href="http://xml.apache.org/xalan-j/xsltc_usage.html">xsltc</a>.
     *
     * @param url the XSLT URL source
     * @param attributes the Transformer Factory attributes
     */
    public XSLTTransformer(final URL url, final Map<String, Object> attributes) {
        this.load(url, attributes);
    }

    /**
     * Creates a new transformer reading the XSLT from the source.
     *
     * @param source the XSLT URL source
     * @param lastModified timestamp
     */
    public XSLTTransformer(final Source source, final long lastModified) {
        this(source, lastModified, null);
    }

    /**
     * Creates a new transformer reading the XSLT from the Source source and setting the TransformerFactory attributes.
     *
     * This constructor is useful when users want to perform XSLT transformation using <a
     * href="http://xml.apache.org/xalan-j/xsltc_usage.html">xsltc</a>.
     *
     * @param source the XSLT source
     * @param lastModified timestamp
     * @param attributes the Transformer Factory attributes
     */
    public XSLTTransformer(final Source source, final long lastModified, final Map<String, Object> attributes) {
        super();
        this.load(source, lastModified, attributes);
    }

    /**
     * Method useful to create a new transformer reading the XSLT from the URL source and setting the Transformer
     * Factory attributes.
     *
     * This method is useful when users want to perform XSLT transformation using <a
     * href="http://xml.apache.org/xalan-j/xsltc_usage.html">xsltc</a>.
     *
     * @param url the XSLT URL source
     * @param attributes the Transformer Factory attributes
     */
    private void load(final URL url, final Map<String, Object> attributes) {
        if (url == null) {
            throw new IllegalArgumentException("The parameter 'source' mustn't be null.");
        }

        this.url = url;
        this.lastModified = URLConnectionUtils.getLastModified(this.url);

        this.load(new StreamSource(this.url.toExternalForm()), this.url.toExternalForm(), attributes);
    }

    /**
     * Method useful to create a new transformer reading the XSLT from the URL source and setting the Transformer
     * Factory attributes.
     *
     * This method is useful when users want to perform XSLT transformation using <a
     * href="http://xml.apache.org/xalan-j/xsltc_usage.html">xsltc</a>.
     *
     * @param source the XSLT source
     * @param lastModified timestamp
     * @param attributes the Transformer Factory attributes
     */
    private void load(final Source source, final long lastModified, final Map<String, Object> attributes) {
        if (source == null) {
            throw new IllegalArgumentException("The parameter 'source' mustn't be null.");
        }

        this.source = source;
        this.lastModified = lastModified;

        this.load(this.source, this.source.toString(), attributes);
    }

    private void load(final Source source, final String localCacheKey, final Map<String, Object> attributes) {
        // check the XSLT is in the cache first
        if (XSLT_CACHE.containsKey(localCacheKey)) {
            // get the XSLT directly from the cache
            final ValidityValue<Templates> cacheEntry = XSLT_CACHE.get(localCacheKey);
            if (cacheEntry.lastModified >= lastModified) {
                LOG.debug("{} local cache hit: {}", getClass().getSimpleName(), localCacheKey);

                this.templates = cacheEntry.value;
            }
        }
        if (this.templates == null) {
            LOG.debug("{} local cache miss: {}", getClass().getSimpleName(), localCacheKey);

            // XSLT has to be parsed
            final SAXTransformerFactory transformerFactory;
            if (attributes == null || attributes.isEmpty()) {
                transformerFactory = TRAX_FACTORY;
            } else {
                transformerFactory = createNewSAXTransformerFactory();
                for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                    transformerFactory.setAttribute(attribute.getKey(), attribute.getValue());
                }
            }

            try {
                this.templates = transformerFactory.newTemplates(source);

                // store the XSLT into the cache for future reuse
                LOG.debug("{} local cache put: {}", getClass().getSimpleName(), localCacheKey);

                final ValidityValue<Templates> cacheEntry =
                        new ValidityValue<Templates>(this.templates, this.lastModified);
                XSLT_CACHE.put(localCacheKey, cacheEntry);
            } catch (TransformerConfigurationException e) {
                throw new SetupException("Impossible to read XSLT from '" + source + "', see nested exception", e);
            }
        }
    }

    /**
     * Sets the XSLT parameters to be applied to XSLT stylesheet.
     *
     * @param parameters the XSLT parameters to be applied to XSLT stylesheet
     */
    public void setParameters(final Map<String, ? extends Object> parameters) {
        if (parameters == null) {
            this.parameters = null;
        } else {
            this.parameters = new HashMap<String, Object>(parameters);
        }
    }

    @Override
    public void setConfiguration(final Map<String, ? extends Object> configuration) {
        try {
            this.url = (URL) configuration.get(SOURCE);
        } catch (ClassCastException cce) {
            throw new SetupException(
                    "The configuration value of '" + SOURCE + "' can't be cast to " + URL.class.getName(), cce);
        }

        if (this.url == null) {
            LOG.debug("Impossible to load XSLT parameters from null source");
        } else {
            final Object attributesObj = configuration.get("attributes");
            if (attributesObj instanceof Map) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> attributesMap = (Map<String, Object>) attributesObj;
                this.load(this.url, attributesMap);
            } else {
                this.load(this.url, null);
            }
        }

        this.setParameters(configuration);
    }

    @Override
    protected void setSAXConsumer(final SAXConsumer consumer) {
        TransformerHandler transformerHandler;
        try {
            transformerHandler = TRAX_FACTORY.newTransformerHandler(this.templates);
        } catch (Exception e) {
            throw new SetupException("Could not initialize transformer handler.", e);
        }

        if (this.parameters != null) {
            final Transformer transformer = transformerHandler.getTransformer();

            for (Map.Entry<String, Object> entry : this.parameters.entrySet()) {
                final String name = entry.getKey();

                // is valid XSLT parameter name
                if (XSLT_PARAMETER_NAME_PATTERN.matcher(name).matches()) {
                    transformer.setParameter(name, entry.getValue());
                }
            }
        }

        final SAXResult result = new SAXResult();
        result.setHandler(consumer);
        // According to TrAX specs, all TransformerHandlers are LexicalHandlers
        result.setLexicalHandler(consumer);
        transformerHandler.setResult(result);

        final TraxErrorListener traxErrorListener = new TraxErrorListener(LOG,
                this.url == null ? this.source.toString() : this.url.toExternalForm());
        transformerHandler.getTransformer().setErrorListener(traxErrorListener);

        final SAXConsumerAdapter saxConsumerAdapter = new SAXConsumerAdapter();
        saxConsumerAdapter.setContentHandler(transformerHandler);
        super.setSAXConsumer(saxConsumerAdapter);
    }

    /**
     * Utility method to create a new transformer factory.
     *
     * @return a new transformer factory
     */
    private static SAXTransformerFactory createNewSAXTransformerFactory() {
        return (SAXTransformerFactory) TransformerFactory.newInstance();
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "src=" + (this.url == null
                ? "<" + this.source + "," + this.lastModified + ">"
                : this.url.toExternalForm()));
    }
}
