package org.alveolo.butterfly.saxon.xpath.functions;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.saxon.Controller;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.StructuredQName;

import org.springframework.web.servlet.support.RequestContext;


public class ModelAttribute extends AttributeFunctionDefinition {
	private static final StructuredQName qName = new StructuredQName("", CoreConstants.NAMESPACE, "attribute");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ModelAttributeCall();
	}

	private static class ModelAttributeCall extends AttributeFunctionCall {
		@Override
		protected Object getAttribute(Controller controller, String name) {
			RequestContext rc = getParameter(controller, CoreConstants.REQUEST_CONTEXT_PARAM);

			Map<String, Object> model;
			if (rc != null) {
				model = rc.getModel();
			} else {
				model = getParameter(controller, CoreConstants.MODEL_PARAM);
			}

			if (model != null) {
				return model.get(name);
			}

			HttpServletRequest request = getParameter(controller, CoreConstants.SERVLET_REQUEST_PARAM);
			return request.getAttribute(name);
		}
	}
}
