package org.alveolo.butterfly.saxon.xpath.bind;

import net.sf.saxon.Configuration;


public class BindHelper {
	public static void registerFunctions(Configuration configuration) {
		configuration.registerExtensionFunction(new MaxSize());
		configuration.registerExtensionFunction(new Errors());
		configuration.registerExtensionFunction(new Expression());
		configuration.registerExtensionFunction(new Property());
		configuration.registerExtensionFunction(new Value());
	}
}
