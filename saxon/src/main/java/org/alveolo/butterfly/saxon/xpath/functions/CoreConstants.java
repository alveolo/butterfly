package org.alveolo.butterfly.saxon.xpath.functions;

import net.sf.saxon.om.StructuredQName;


public interface CoreConstants {
	String PREFIX = "core", NAMESPACE = "http://alveolo.org/cocoon/core";

	// Spring objects
	StructuredQName APPLICATION_CONTEXT_PARAM = new StructuredQName(PREFIX, NAMESPACE, "context");
	StructuredQName MODEL_PARAM = new StructuredQName(PREFIX, NAMESPACE, "model");

	StructuredQName REQUEST_CONTEXT_PARAM = new StructuredQName(PREFIX, NAMESPACE, "request");

	// Servlet objects
	StructuredQName SERVLET_CONTEXT_PARAM  = new StructuredQName(PREFIX, NAMESPACE, "servletContext");
	StructuredQName SERVLET_REQUEST_PARAM  = new StructuredQName(PREFIX, NAMESPACE, "servletRequest");
	StructuredQName SERVLET_RESPONSE_PARAM = new StructuredQName(PREFIX, NAMESPACE, "servletResponse");
	StructuredQName SERVLET_SESSION_PARAM  = new StructuredQName(PREFIX, NAMESPACE, "servletSession");
}
