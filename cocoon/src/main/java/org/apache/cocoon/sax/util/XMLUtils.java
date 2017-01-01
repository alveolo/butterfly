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
package org.apache.cocoon.sax.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URLConnection;
import java.util.Properties;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.cocoon.pipeline.PipelineException;
import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.util.URLConnectionUtils;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.xml.sax.EmbeddedSAXPipe;
import org.apache.cocoon.xml.sax.SAXBuffer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Utilities for the usage of an {@link XMLReader} together with a {@link SAXConsumer}.
 */
public abstract class XMLUtils {
    private static final SAXTransformerFactory SAX_TRANSFORMER_FACTORY = (SAXTransformerFactory) TransformerFactory
            .newInstance();

    /**
     * Empty attributes immutable object.
     */
    public static final Attributes EMPTY_ATTRIBUTES = new ImmutableAttributesImpl();

    /**
     * Serialize the content of a {@link SAXBuffer} into an {@link OutputStream}.
     *
     * @param outputStream OutputStream to use.
     * @param saxBuffer A SaxBuffer containing the SAX events.
     */
    public static void toOutputStream(final OutputStream outputStream, final SAXBuffer saxBuffer) {
        try {
            TransformerHandler transformerHandler = SAX_TRANSFORMER_FACTORY.newTransformerHandler();
            Properties properties = new Properties();
            properties.put("method", "xml");
            transformerHandler.getTransformer().setOutputProperties(properties);
            transformerHandler.setResult(new StreamResult(outputStream));

            transformerHandler.startDocument();
            saxBuffer.toSAX(new EmbeddedSAXPipe(transformerHandler));
            transformerHandler.endDocument();
        } catch (Exception e) {
            throw new ProcessingException("Can't stream the provided SaxBuffer.", e);
        }
    }

    /**
     * Use a SAX parser and read the content of an {@link InputStream} into an {@link SAXConsumer}.
     *
     * @param inputStream InputStream containing the content to be parsed.
     * @param contentHandler The ContentHandler to use.
     * @throws IOException if the {@link InputStream} can't be used.
     * @throws SAXException if the {@link InputStream} can't be parsed.
     */
    public static void toSax(final InputStream inputStream, final ContentHandler contentHandler) throws IOException,
            SAXException {
        createXMLReader(contentHandler).parse(new InputSource(new BufferedInputStream(inputStream)));
    }

    /**
     * Use a SAX parser and read the content of an {@link InputStream} into an {@link SAXConsumer}.
     *
     * @param string A String to be parsed.
     * @param contentHandler The ContentHandler to use.
     * @throws IOException if the {@link InputStream} can't be used.
     * @throws SAXException if the {@link InputStream} can't be parsed.
     */
    public static void toSax(final String string, final ContentHandler contentHandler) throws IOException, SAXException {
        createXMLReader(contentHandler).parse(new InputSource(new StringReader(string)));
    }

    /**
     * Stream a {@link URLConnection} into an {@link SAXConsumer} by using
     * {@link XMLUtils#toSax(InputStream, ContentHandler)}. The {@link URLConnection} is closed after
     * streaming.
     *
     * @param urlConnection The {@link URLConnection} to be streamed.
     * @param contentHandler An {@link ContentHandler} as target.
     */
    public static void toSax(final URLConnection urlConnection, final ContentHandler contentHandler) {
        try {
            InputStream inputStream = urlConnection.getInputStream();
            XMLUtils.toSax(inputStream, contentHandler);
        } catch (PipelineException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessingException("Can't parse url connection " + urlConnection.getURL(), e);
        } finally {
            URLConnectionUtils.closeQuietly(urlConnection);
        }
    }

    public static XMLReader createXMLReader(final ContentHandler contentHandler) {
        XMLReader xmlReader;
        try {
            xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setContentHandler(contentHandler);
            if ( contentHandler instanceof LexicalHandler ) {
                xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", contentHandler);
            }

            xmlReader.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    return new InputSource(new ByteArrayInputStream("".getBytes()));
                }
            });
        } catch (SAXException e) {
            throw new ProcessingException("Cannot create and prepare an XMLReader.", e);
        }

        return xmlReader;
    }
}
