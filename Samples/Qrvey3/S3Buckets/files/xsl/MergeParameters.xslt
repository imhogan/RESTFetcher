<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    exclude-result-prefixes="xsl fn xd"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="OldParamsURI"/>
    <xsl:variable name="OldParamsAbsURI">
        <xsl:call-template name="GetAbsoluteURI">
            <xsl:with-param name="SourceURI" select="$OldParamsURI"/>
        </xsl:call-template>
    </xsl:variable>
    <xd:doc>
        <xd:desc>Load the source file or use an empty sequence if there is none.</xd:desc>
    </xd:doc>
    <xsl:variable name="OldParamsFile" select="if (fn:doc-available($OldParamsAbsURI)) then fn:doc($OldParamsAbsURI) else ()"></xsl:variable>
    <xsl:template match="/">
        <xsl:variable name="MyNames" select="//Parameters/Parameter/@Name"/>
        <Parameters OldParamsURI="{$OldParamsURI}">
            <xsl:attribute name="MyNames">
                <xsl:for-each select="$MyNames">
                    <xsl:value-of select="."/>
                    <xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each>
            </xsl:attribute>
            <xsl:copy-of select="//Parameter | $OldParamsFile//Parameter[not(@Name = $MyNames)]"></xsl:copy-of>
        </Parameters>
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