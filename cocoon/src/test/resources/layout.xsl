<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" extension-element-prefixes="core sec"
	xmlns:core="http://alveolo.org/cocoon/core"
	xmlns:sec="http://alveolo.org/cocoon/security"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="classpath:org/alveolo/butterfly/cocoon/forms.xsl"/>

<xsl:output method="html" indent="no" version="5.0"
		doctype-public="-//W3C//DTD HTML 4.01//EN" doctype-system="http://www.w3.org/TR/html4/strict.dtd"/>

<xsl:template match="page">
	<html>
		<body>
			<xsl:apply-templates select="content/node()"/>
		</body>
	</html>
</xsl:template>

</xsl:stylesheet>
