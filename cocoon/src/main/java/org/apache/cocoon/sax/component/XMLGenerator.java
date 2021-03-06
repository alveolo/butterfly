/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.sax.component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.apache.cocoon.pipeline.PipelineException;
import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.sax.AbstractSAXGenerator;
import org.apache.cocoon.sax.AbstractSAXProducer;
import org.apache.cocoon.sax.util.XMLUtils;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.cocoon.xml.sax.SAXBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * General purpose SAX generator that produces SAX events from different sources.
 */
public class XMLGenerator extends AbstractSAXGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(XMLGenerator.class);

    private transient Starter generator;

    public XMLGenerator() {
        this((URL) null);
    }

    public XMLGenerator(final byte[] bytes) {
        this.generator = new ByteArrayGenerator(bytes == null ? null : bytes.clone());
    }

    public XMLGenerator(final byte[] bytes, final String encoding) {
        this.generator = new ByteArrayGenerator(bytes == null ? null : bytes.clone(), encoding);
    }

    public XMLGenerator(final File file) {
        this.generator = new FileGenerator(file);
    }

    public XMLGenerator(final InputStream inputStream) {
        this.generator = new InputStreamGenerator(inputStream);
    }

    public XMLGenerator(final Node node) {
        this.generator = new NodeGenerator(node);
    }

    public XMLGenerator(final SAXBuffer saxBuffer) {
        this.generator = new SAXBufferGenerator(saxBuffer);
    }

    public XMLGenerator(final String xmlString) {
        this.generator = new StringGenerator(xmlString);
    }

    public XMLGenerator(final URL url) {
        this.generator = new URLGenerator(url);
    }

    @Override
    public void execute() {
        this.generator.execute();
    }

    @Override
    public void setConfiguration(final Map<String, ? extends Object> configuration) {
        ((URLGenerator) this.generator).setSource((URL) configuration.get("source"));
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "internalGenerator=" + this.generator);
    }

    private class ByteArrayGenerator extends AbstractSAXGenerator {
        private final transient byte[] bytes;
        private final transient String encoding;

        public ByteArrayGenerator(final byte[] bytes) {
            this(bytes, null);
        }

        public ByteArrayGenerator(final byte[] bytes, final String encoding) {
            if (bytes == null) {
                throw new SetupException("A byte array has to be passed.");
            }

            this.bytes = bytes.clone();
            this.encoding = encoding;
        }

        @Override
        public void execute() {
            try {
                LOG.debug("Using a byte array as source to produce SAX events.");

                if (this.encoding == null) {
                    XMLUtils.toSax(new ByteArrayInputStream(this.bytes), XMLGenerator.this.getSAXConsumer());
                } else {
                    XMLUtils.toSax(new String(this.bytes, this.encoding), XMLGenerator.this.getSAXConsumer());
                }
            } catch (PipelineException e) {
                LOG.error("Pipeline expcetion thrown", e);
                throw e;
            } catch (UnsupportedEncodingException e) {
                throw new ProcessingException("The encoding " + this.encoding + " is not supported.", e);
            } catch (Exception e) {
                throw new ProcessingException("Can't parse byte array " + Arrays.toString(this.bytes), e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this,
                    "bytes=" + Arrays.toString(this.bytes), "encoding=" + this.encoding);
        }
    }

    private class FileGenerator extends AbstractSAXGenerator {
        private final transient File file;

        public FileGenerator(final File file) {
            if (file == null) {
                throw new SetupException("A file has to be passed.");
            }

            this.file = file;
        }

        @Override
        public void execute() {
            try {
                LOG.debug("Using file {} as source to produce SAX events.", this.file.getAbsolutePath());

                XMLUtils.toSax(new FileInputStream(this.file), XMLGenerator.this.getSAXConsumer());
            } catch (PipelineException e) {
                LOG.error("Pipeline expcetion thrown", e);
                throw e;
            } catch (Exception e) {
                throw new ProcessingException("Can't read or parse file " + this.file.getAbsolutePath(), e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "file=" + this.file);
        }
    }

    private class InputStreamGenerator extends AbstractSAXGenerator {
        private final transient InputStream inputStream;

        public InputStreamGenerator(final InputStream inputStream) {
            if (inputStream == null) {
                throw new SetupException("An input stream has to be passed.");
            }

            this.inputStream = inputStream;
        }

        @Override
        public void execute() {
            try {
                LOG.debug("Using input stream {} as source to produce SAX events.", this.inputStream);

                XMLUtils.toSax(this.inputStream, XMLGenerator.this.getSAXConsumer());
            } catch (PipelineException e) {
                LOG.error("Pipeline expcetion thrown", e);
                throw e;
            } catch (Exception e) {
                throw new ProcessingException("Can't read or parse file " + this.inputStream, e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "inputStream=" + this.inputStream);
        }
    }

    private class NodeGenerator extends AbstractSAXGenerator {
        private final transient Node node;

        public NodeGenerator(final Node document) {
            if (document == null) {
                throw new SetupException("A DOM document has to be passed.");
            }

            this.node = document;
        }

        @Override
        public void execute() {
            LOG.debug("Using a DOM node to produce SAX events.");

            final DOMStreamer streamer = new DOMStreamer(XMLGenerator.this.getSAXConsumer());
            try {
                streamer.stream(this.node);
            } catch (SAXException e) {
                throw new SetupException("Can't stream DOM node " + this.node, e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "node=" + this.node);
        }
    }

    private class SAXBufferGenerator extends AbstractSAXGenerator {
        private final transient SAXBuffer saxBuffer;

        public SAXBufferGenerator(final SAXBuffer saxBuffer) {
            if (saxBuffer == null) {
                throw new SetupException("A SAXBuffer has to be passed.");
            }

            this.saxBuffer = saxBuffer;
        }

        @Override
        public void execute() {
            LOG.debug("Using a SAXBuffer to produce SAX events.");

            try {
                this.saxBuffer.toSAX(XMLGenerator.this.getSAXConsumer());
            } catch (SAXException e) {
                throw new ProcessingException("Can't stream " + this
                        + " into the content handler.", e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "saxBuffer=" + this.saxBuffer);
        }
    }

    private class StringGenerator extends AbstractSAXProducer implements Starter {
        private final transient String xmlString;

        public StringGenerator(final String xmlString) {
            if (xmlString == null) {
                throw new SetupException("An XML string has to be passed.");
            }

            this.xmlString = xmlString;
        }

        @Override
        public void execute() {
            try {
                LOG.debug("Using a string to produce SAX events.");

                XMLUtils.toSax(this.xmlString, XMLGenerator.this.getSAXConsumer());
            } catch (PipelineException e) {
                LOG.error("Pipeline exception thrown", e);
                throw e;
            } catch (Exception e) {
                throw new ProcessingException("Can't parse the XML string.", e);
            }
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "xmlString=" + this.xmlString);
        }
    }

    private class URLGenerator extends AbstractSAXGenerator {
        private transient URL source;

        public URLGenerator(final URL source) {
            this.source = source;
        }

        @Override
        public void execute() {
            if (this.source == null) {
                throw new ProcessingException(this.getClass().getSimpleName() + " has no source.");
            }

            LOG.debug("Using the URL {} to produce SAX events.", this.source.toExternalForm());

            try {
                XMLUtils.toSax(this.source.openConnection(), XMLGenerator.this.getSAXConsumer());
            } catch (IOException e) {
                throw new ProcessingException("Can't open connection to " + this.source.toExternalForm(), e);
            }
        }

        public void setSource(final URL source) {
            this.source = source;
        }

        @Override
        public String toString() {
            return StringRepresentation.buildString(this, "source=" + this.source);
        }
    }
}
