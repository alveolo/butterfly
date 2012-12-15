package org.alveolo.butterfly.cocoon;

import java.io.File;
import java.net.URI;


class Dependency {
	File file;
	long lastMofified;

	Dependency(URI uri) {
		file = new File(uri);
		lastMofified = file.lastModified();
	}

	boolean hasChanged() {
		return file.lastModified() != lastMofified;
	}
}
