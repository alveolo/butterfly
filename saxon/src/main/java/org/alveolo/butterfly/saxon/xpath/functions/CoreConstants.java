package org.alveolo.butterfly.saxon.xpath.functions;


public interface CoreConstants {
	String NAMESPACE = "http://alveolo.org/cocoon/core";

	// Spring objects
	String APPLICATION_CONTEXT_PARAM = NAMESPACE + "/context";
	String MODEL_PARAM = NAMESPACE + "/model";
	String REQUEST_CONTEXT_PARAM = NAMESPACE + "/request";

	// Servlet objects
	String SERVLET_CONTEXT_PARAM  = NAMESPACE + "/servlet/context";
	String SERVLET_REQUEST_PARAM  = NAMESPACE + "/servlet/request";
	String SERVLET_RESPONSE_PARAM = NAMESPACE + "/servlet/response";
	String SERVLET_SESSION_PARAM  = NAMESPACE + "/servlet/session";
}
