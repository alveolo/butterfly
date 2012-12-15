package org.alveolo.butterfly.saxon.xpath.functions;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.saxon.Controller;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.StructuredQName;

import org.springframework.web.servlet.support.RequestContext;


@SuppressWarnings("serial")
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
			RequestContext rc = (RequestContext) controller.getParameter(CoreConstants.REQUEST_CONTEXT_PARAM);

			Map<String, Object> model = (rc != null) ? rc.getModel() : getModel(controller);
			if (model != null) {
				return model.get(name);
			}

			HttpServletRequest request = (HttpServletRequest)
					controller.getParameter(CoreConstants.SERVLET_REQUEST_PARAM);

			return request.getAttribute(name);
		}

		@SuppressWarnings("unchecked")
		private Map<String, Object> getModel(Controller controller) {
			return (Map<String, Object>) controller.getParameter(CoreConstants.MODEL_PARAM);
		}
	}
}
