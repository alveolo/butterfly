package org.alveolo.butterfly.cocoon;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.Model;


public class HttpContextHelper {
	private static final String HTTP_SERVLET_REQUEST_KEY = HttpServletRequest.class.getName();
	private static final String HTTP_SERVLET_RESPONSE_KEY = HttpServletResponse.class.getName();
	private static final String HTTP_SERVLET_CONTEXT_KEY = ServletContext.class.getName();
	private static final String MODEL_KEY = Model.class.getName();

	public static HttpServletRequest getRequest(Map<String, ? extends Object> parameters) {
		Object parameter = parameters.get(HTTP_SERVLET_REQUEST_KEY);
		if (parameter instanceof HttpServletRequest) {
			return (HttpServletRequest) parameter;
		}

		throw new IllegalStateException(
				"A HttpServletRequest is not available. This might indicate an invocation outside a servlet.");
	}

	public static HttpServletResponse getResponse(Map<String, ? extends Object> parameters) {
		Object parameter = parameters.get(HTTP_SERVLET_RESPONSE_KEY);
		if (parameter instanceof HttpServletResponse) {
			return (HttpServletResponse) parameter;
		}

		throw new IllegalStateException(
				"A HttpServletResponse is not available. This might indicate an invocation outside a servlet.");
	}

	public static ServletContext getServletContext(Map<String, ? extends Object> parameters) {
		Object parameter = parameters.get(HTTP_SERVLET_CONTEXT_KEY);
		if (parameter instanceof ServletContext) {
			return (ServletContext) parameter;
		}

		throw new IllegalStateException(
				"The ServletContext is not available. This might indicate an invocation outside a servlet.");
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getModel(Map<String, ? extends Object> parameters) {
		Object parameter = parameters.get(MODEL_KEY);
		if (parameter instanceof Map) {
			return (Map<String, Object>) parameter;
		}

		throw new IllegalStateException(
				"A Model is not available. This might indicate an invocation outside a cocoon view.");
	}

	public static void storeRequest(HttpServletRequest httpServletRequest, Map<String, Object> parameters) {
		parameters.put(HTTP_SERVLET_REQUEST_KEY, httpServletRequest);
	}

	public static void storeResponse(HttpServletResponse httpServletResponse, Map<String, Object> parameters) {
		parameters.put(HTTP_SERVLET_RESPONSE_KEY, httpServletResponse);
	}

	public static void storeServletContext(ServletContext servletContext, Map<String, Object> parameters) {
		parameters.put(HTTP_SERVLET_CONTEXT_KEY, servletContext);
	}

	public static void storeModel(Map<String, Object> model, Map<String, Object> parameters) {
		parameters.put(MODEL_KEY, model);
	}
}
