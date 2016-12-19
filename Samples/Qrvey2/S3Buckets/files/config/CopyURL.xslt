<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    exclude-result-prefixes="xsl fn xd"
    version="2.0">
    
    
    <xd:doc scope="stylesheet">
        <xd:desc>
            <xd:p><xd:b>Author:</xd:b> Ian Hogan</xd:p>
            <xd:p>This stylesheet copies the contents of SourceURI to the output.
            </xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:output method="xml" indent="yes"/>
<xsl:param name="SourceURI"/>
    <xsl:variable name="SourceAbsURI">
        <xsl:call-template name="GetAbsoluteURI">
            <xsl:with-param name="SourceURI" select="$SourceURI"/>
        </xsl:call-template>
    </xsl:variable>
    <xd:doc>
        <xd:desc>Load the source file or use an empty sequence if there is none.</xd:desc>
    </xd:doc>
    <xsl:variable name="SourceFile" select="if (fn:doc-available($SourceAbsURI)) then fn:doc($SourceAbsURI) else ()"></xsl:variable>
<xsl:template match="/">
	<xsl:apply-templates select="$SourceFile/*"/>
</xsl:template>
<xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>           
        </xsl:copy>
    </xsl:template>
    <xsl:template name="GetAbsoluteURI">
        <xsl:param name="SourceURI"></xsl:param>
        <xsl:choose>
            <xsl:when test="fn:base-uri(/) != ''"><xsl:value-of select="fn:resolve-uri($SourceURI,fn:base-uri(/))"/></xsl:when>
            <xsl:when test="fn:base-uri() != ''"><xsl:value-of select="fn:resolve-uri($SourceURI,fn:base-uri())"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="$SourceURI"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
