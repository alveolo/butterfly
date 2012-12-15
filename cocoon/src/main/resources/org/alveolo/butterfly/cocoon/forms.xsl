<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" exclude-result-prefixes="bind fn xs" extension-element-prefixes="core i18n"
	xmlns:bind="http://alveolo.org/cocoon/bind"
	xmlns:core="http://alveolo.org/cocoon/core"
	xmlns:i18n="http://alveolo.org/cocoon/i18n"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="@* | node()">
	<xsl:copy copy-namespaces="no">
		<xsl:apply-templates select="@* | node()"/>
	</xsl:copy>
</xsl:template>

<xsl:template match="@bind:*"/>


<xsl:template match="form">
	<xsl:copy copy-namespaces="no">
		<xsl:if test="not(@method)">
			<xsl:attribute name="method">post</xsl:attribute>
		</xsl:if>

		<xsl:apply-templates select="@* | node()"/>
	</xsl:copy>
</xsl:template>


<xsl:template match="label/@for"/>
<xsl:template match="label">
	<xsl:copy copy-namespaces="no">
		<xsl:variable name="for" as="attribute()?">
			<xsl:choose>
				<xsl:when test="@for">
					<xsl:sequence select="@for"/>
				</xsl:when>
				<xsl:when test="@bind:path">
					<xsl:attribute name="for" select="fn:translate(bind:expression(bind:context-path(.)), '[]', '')"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>

		<xsl:sequence select="$for"/>

		<xsl:apply-templates select="@*"/>

		<xsl:choose>
			<xsl:when test="node()">
				<!-- Ignore content: use any comment for empty content, use text to set non-empty content -->
				<xsl:apply-templates select="node()"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="i18n:message(fn:concat('label.', bind:property(bind:context-path(.))))"/>
				<xsl:text>: </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:copy>
</xsl:template>


<xsl:template match="output">
	<span>
		<xsl:call-template name="bind:emit-id"/>
		<xsl:attribute name="class">form-value</xsl:attribute>

		<xsl:apply-templates select="@*"/>

		<xsl:sequence select="bind:value(bind:context-path(.))"/>
	</span>
</xsl:template>


<xsl:template match="input/@id"/>
<xsl:template match="input/@name"/>
<xsl:template match="input/@value"/>
<xsl:template match="input/@size"/>
<xsl:template match="input/@maxlength"/>
<xsl:template match="input">
	<xsl:copy copy-namespaces="no">
		<xsl:call-template name="bind:emit-id"/>

		<xsl:variable name="name" as="attribute()?">
			<xsl:choose>
				<xsl:when test="@name">
					<xsl:sequence select="@name"/>
				</xsl:when>
				<xsl:when test="@bind:path">
					<xsl:attribute name="name" select="bind:expression(bind:context-path(.))"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>

		<xsl:sequence select="$name"/>

		<xsl:if test="not(@type eq 'password')">
			<xsl:choose>
				<xsl:when test="@value">
					<!-- Ignore bound value, provided value is used instead -->
					<xsl:sequence select="@value"/>
				</xsl:when>
				<xsl:when test="@bind:path">
					<xsl:attribute name="value" select="bind:value(bind:context-path(.))"/>
				</xsl:when>
				<xsl:when test="$name">
					<xsl:attribute name="value" select="core:request-parameter($name)"/>
				</xsl:when>
			</xsl:choose>
		</xsl:if>

		<xsl:choose>
			<xsl:when test="@maxlength">
				<xsl:sequence select="@maxlength | @size"/>
			</xsl:when>
			<xsl:when test="@bind:path">
				<xsl:variable name="max" select="bind:max-size(bind:context-path(.))"/>
				<xsl:choose>
					<xsl:when test="@size">
						<xsl:sequence select="@size"/>
					</xsl:when>
					<xsl:when test="$max gt 64">
						<xsl:attribute name="size">64</xsl:attribute>
					</xsl:when>
					<xsl:when test="$max">
						<xsl:attribute name="size" select="$max"/>
					</xsl:when>
				</xsl:choose>
				<xsl:if test="$max">
					<xsl:attribute name="maxlength" select="$max"/>
				</xsl:if>
			</xsl:when>
		</xsl:choose>

		<xsl:apply-templates select="@*"/>
	</xsl:copy>
</xsl:template>


<xsl:template match="textarea/@id"/>
<xsl:template match="textarea/@name"/>
<xsl:template match="textarea/@maxlength"/>
<xsl:template match="textarea">
	<xsl:copy copy-namespaces="no">
		<xsl:call-template name="bind:emit-id"/>

		<xsl:variable name="name" as="attribute()?">
			<xsl:choose>
				<xsl:when test="@name">
					<xsl:sequence select="@name"/>
				</xsl:when>
				<xsl:when test="@bind:path">
					<xsl:attribute name="name" select="bind:expression(bind:context-path(.))"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>

		<xsl:sequence select="$name"/>

		<xsl:choose>
			<xsl:when test="@maxlength">
				<xsl:sequence select="@maxlength"/>
			</xsl:when>
			<xsl:when test="@bind:path">
				<xsl:variable name="max" select="bind:max-size(bind:context-path(.))"/>
				<xsl:if test="$max">
					<xsl:attribute name="maxlength" select="$max"/>
				</xsl:if>
			</xsl:when>
		</xsl:choose>

		<xsl:apply-templates select="@*"/>

		<xsl:choose>
			<xsl:when test="node()">
				<!-- Ignore bound value: use any comment for empty content, use text to set non-empty content -->
				<xsl:apply-templates select="node()"/>
			</xsl:when>
			<xsl:when test="@bind:path">
				<xsl:sequence select="bind:value(bind:context-path(.))"/>
			</xsl:when>
			<xsl:when test="$name">
				<xsl:sequence select="core:request-parameter($name)"/>
			</xsl:when>
		</xsl:choose>
	</xsl:copy>
</xsl:template>


<xsl:template match="select/@id"/>
<xsl:template match="select/@name"/>
<xsl:template match="select">
	<xsl:copy copy-namespaces="no">
		<xsl:call-template name="bind:emit-id"/>

		<xsl:variable name="name" as="attribute()?">
			<xsl:choose>
				<xsl:when test="@name">
					<xsl:sequence select="@name"/>
				</xsl:when>
				<xsl:when test="@bind:path">
					<xsl:attribute name="name" select="bind:expression(bind:context-path(.))"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>

		<xsl:sequence select="$name"/>

		<xsl:apply-templates select="@* | node()">
			<xsl:with-param name="bind:value" as="xs:string?" tunnel="yes">
				<xsl:choose>
					<xsl:when test="@bind:path">
						<xsl:sequence select="bind:value(bind:context-path(.))"/>
					</xsl:when>
					<xsl:when test="$name">
						<xsl:sequence select="core:request-parameter($name)"/>
					</xsl:when>
				</xsl:choose>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:copy>
</xsl:template>


<xsl:template match="option/@selected"/>
<xsl:template match="option">
	<xsl:param name="bind:value" tunnel="yes"/>

	<xsl:copy copy-namespaces="no">
		<xsl:choose>
			<xsl:when test="@selected">
				<!-- Ignore bound value: use selected="" to force no selection, use seleced="selected" to force selection -->
				<xsl:if test="string-length(@selected)">
					<xsl:sequence select="@selected"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="$bind:value">
				<xsl:if test="$bind:value eq @value">
					<xsl:attribute name="selected">selected</xsl:attribute>
				</xsl:if>
			</xsl:when>
		</xsl:choose>

		<xsl:apply-templates select="@* | node()"/>
	</xsl:copy>
</xsl:template>


<xsl:template match="errors">
	<span>
		<xsl:apply-templates select="@*"/>
		<xsl:value-of select="bind:errors(bind:context-path(.))"/>
	</span>
</xsl:template>


<xsl:template name="bind:emit-id">
	<xsl:choose>
		<xsl:when test="@id">
			<!-- Ignore bound id, provided id is used instead -->
			<xsl:sequence select="@id"/>
		</xsl:when>
		<xsl:when test="@bind:path">
			<xsl:attribute name="id" select="fn:translate(bind:expression(bind:context-path(.)), '[]', '')"/>
		</xsl:when>
	</xsl:choose>
</xsl:template>


<xsl:function name="bind:context-path" as="xs:string">
	<xsl:param name="el" as="element()"/>

	<xsl:choose>
		<xsl:when test="$el/@bind:model and $el/@bind:path">
			<xsl:sequence select="string-join(($el/@bind:model, $el/@bind:path), '.')"/>
		</xsl:when>
		<xsl:when test="$el/@bind:model">
			<xsl:sequence select="$el/@bind:model"/>
		</xsl:when>
		<xsl:when test="$el/@bind:path">
			<xsl:sequence select="string-join((bind:context-path($el/..), $el/@bind:path), '.')"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:sequence select="bind:context-path($el/..)"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:function>

</xsl:stylesheet>
