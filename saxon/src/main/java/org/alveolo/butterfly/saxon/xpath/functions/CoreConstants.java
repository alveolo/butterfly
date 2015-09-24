package org.alveolo.butterfly.saxon.xpath.functions;

import net.sf.saxon.s9api.QName;


public interface CoreConstants {
	String PREFIX = "core", NAMESPACE = "http://alveolo.org/cocoon/core";

	// Spring objects
	QName APPLICATION_CONTEXT_PARAM = new QName(PREFIX, NAMESPACE, "context");
	QName MODEL_PARAM = new QName(PREFIX, NAMESPACE, "model");

	QName REQUEST_CONTEXT_PARAM = new QName(PREFIX, NAMESPACE, "request");

	// Servlet objects
	QName SERVLET_CONTEXT_PARAM  = new QName(PREFIX, NAMESPACE, "servletContext");
	QName SERVLET_REQUEST_PARAM  = new QName(PREFIX, NAMESPACE, "servletRequest");
	QName SERVLET_RESPONSE_PARAM = new QName(PREFIX, NAMESPACE, "servletResponse");
	QName SERVLET_SESSION_PARAM  = new QName(PREFIX, NAMESPACE, "servletSession");
}
