package org.alveolo.butterfly.saxon.xpath;

import net.sf.saxon.Configuration;
import net.sf.saxon.trans.LicenseException;

import org.alveolo.butterfly.saxon.xpath.bind.BindHelper;
import org.alveolo.butterfly.saxon.xpath.functions.Eval;
import org.alveolo.butterfly.saxon.xpath.functions.Marshall;
import org.alveolo.butterfly.saxon.xpath.functions.Message;
import org.alveolo.butterfly.saxon.xpath.functions.ModelAttribute;
import org.alveolo.butterfly.saxon.xpath.functions.RequestAttribute;
import org.alveolo.butterfly.saxon.xpath.functions.RequestParameter;
import org.alveolo.butterfly.saxon.xpath.functions.SessionAttribute;
import org.alveolo.butterfly.saxon.xpath.security.Authentication;


/**
 * Configures extension functions.
 *
 * Also enables some 'licensed' features of Saxon that are present in Open Source HE edition,
 * but are disabled for some reason. Whatever the reason but as it is OSS and license allows
 * this - just enabling it.
 */
public class SaxonConfiguration extends Configuration {
	private static final long serialVersionUID = 1L;

	public SaxonConfiguration() {
		// core
		registerExtensionFunction(new Marshall());
		registerExtensionFunction(new ModelAttribute());
		registerExtensionFunction(new RequestAttribute());
		registerExtensionFunction(new RequestParameter());
		registerExtensionFunction(new SessionAttribute());
		registerExtensionFunction(new Eval());

		// bind
		BindHelper.registerFunctions(this);

		// i18n
		registerExtensionFunction(new Message());

		// security
		registerExtensionFunction(new Authentication());
	}

	@Override
	public void checkLicensedFeature(int feature, String name) throws LicenseException {
		if (feature == LicenseFeature.PROFESSIONAL_EDITION && "custom serialization".equals(name)) {
			return; // allow setting up indent attribute to xsl:output, useful for debugging
		}

		super.checkLicensedFeature(feature, name);
	}
}
