<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:core="http://alveolo.org/cocoon/core"
		xmlns:i18n="http://alveolo.org/cocoon/i18n"
		extension-element-prefixes="core i18n">

	<xsl:output method="html" encoding="UTF-8" media-type="text/html" indent="no" doctype-public="HTML"/>

	<xsl:param name="bar"/>

	<xsl:template match="/">
		<html>
			<head>
				<title>title</title>
			</head>
			<body>
				<input name="xyz"/>
				<p><xsl:value-of select="i18n:message('message', 'a', 'and', 'b')"/></p>
				<p><xsl:copy-of select="core:marshall($bar)"/></p>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
