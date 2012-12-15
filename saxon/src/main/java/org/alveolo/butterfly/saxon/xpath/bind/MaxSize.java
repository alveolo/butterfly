package org.alveolo.butterfly.saxon.xpath.bind;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Size;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.EmptySequence;


@SuppressWarnings("serial")
public class MaxSize extends BindFunctionDefinition {
	private static final StructuredQName qName = new StructuredQName("", BindConstants.NAMESPACE, "max-size");

	@Override
	public StructuredQName getFunctionQName() {
		return qName;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new MaxSizeCall();
	}

	private static class MaxSizeCall extends AbstractPropertyCall {
		private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();

		@Override
		protected Object call(Class<?> type, String propertyName) {
			BeanDescriptor typeConstraints = FACTORY.getValidator().getConstraintsForClass(type);
			PropertyDescriptor descriptor = typeConstraints.getConstraintsForProperty(propertyName);
			if (descriptor == null) {
				return EmptySequence.getInstance().iterate();
			}

			Set<ConstraintDescriptor<?>> constraints = descriptor.getConstraintDescriptors();
			for (ConstraintDescriptor<?> constraint : constraints) {
				Annotation annotation = constraint.getAnnotation();
				if (annotation instanceof Size) {
					Size size = (Size) annotation;

					int max = size.max();
					if (max == Integer.MAX_VALUE) {
						break; // ignore default max value
					}

					return max;
				}
			}

			return null;
		}
	}
}
