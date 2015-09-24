package org.alveolo.butterfly.saxon.xpath.security;

import net.sf.saxon.Controller;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

import org.alveolo.butterfly.saxon.xpath.functions.AttributeFunctionCall;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.security.core.context.SecurityContextHolder;


public class Authentication extends ExtensionFunctionDefinition {
	private static final StructuredQName qName =
			new StructuredQName(SecurityConstants.PREFIX, SecurityConstants.NAMESPACE, "authentication");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
				SequenceType.SINGLE_STRING, // property
		};
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.ANY_SEQUENCE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new AuthenticationCall();
	}

	private static class AuthenticationCall extends AttributeFunctionCall {
		@Override
		protected Object getAttribute(Controller controller, String name) {
			org.springframework.security.core.Authentication auth =
					SecurityContextHolder.getContext().getAuthentication();

			if (auth == null) {
				return null;
			}

			if (auth.getPrincipal() == null) {
				return null;
			}

			BeanWrapper accessor = PropertyAccessorFactory.forBeanPropertyAccess(auth);

			return accessor.isReadableProperty(name) ? accessor.getPropertyValue(name) : null;
		}
	}
}
