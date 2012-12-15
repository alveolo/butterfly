<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:mail="http://alveolo.org/cocoon/javamail"
		exclude-result-prefixes="mail">

	<xsl:template match="/">
		<mail:message>
			<mail:header name="X-Header" value="Test Value"/>
			<mail:subject encoding="UTF-8">Test | Тест</mail:subject>
			<mail:content type="text/html" encoding="UTF-8">
				<html>
					<head>
						<title>Test | Тест</title>
					</head>
					<body>
						<p>Test | Тест</p>
						<p>This is the very very long paragraph text that is supposed to show line wrapping while serializing it into MIME...</p>
					</body>
				</html>
			</mail:content>
		</mail:message>
	</xsl:template>

</xsl:stylesheet>
