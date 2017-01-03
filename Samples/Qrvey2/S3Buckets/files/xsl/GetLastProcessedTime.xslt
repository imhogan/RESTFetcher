<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xsl fn xd"
    version="2.0">
    
    
    <xd:doc scope="stylesheet">
        <xd:desc>
            <xd:p><xd:b>Author:</xd:b> Ian Hogan</xd:p>
            <xd:p>This stylesheet gets the latest processed time.
            </xd:p>
        </xd:desc>
    </xd:doc>

    <xsl:output method="xml" indent="yes"/>
    
    <xd:doc>
        <xd:desc>Debug flag.</xd:desc>
    </xd:doc>
    <xsl:param name="Debug">Y</xsl:param>
    

    <xd:doc>
        <xd:desc>If this is supplied it is used as the result.</xd:desc>
    </xd:doc>
    <xsl:param name="FixedResult"></xsl:param>
    
    <xd:doc>
        <xd:desc>The URI of the responses.</xd:desc>
    </xd:doc>
    <xsl:param name="LatestRespondentsURI"></xsl:param>
    
    <xsl:variable name="LatestRespondentsAbsURI">
        <xsl:call-template name="GetAbsoluteURI">
            <xsl:with-param name="SourceURI" select="$LatestRespondentsURI"/>
        </xsl:call-template>
    </xsl:variable>
    <xd:doc>
        <xd:desc>Load the responses or use an empty sequence if there is none.</xd:desc>
    </xd:doc>
    <xsl:variable name="Responses" select="if ($LatestRespondentsAbsURI != '' and fn:doc-available($LatestRespondentsAbsURI)) then fn:doc($LatestRespondentsAbsURI) else ()"></xsl:variable>
    
    <xd:doc>
        <xd:desc>The URI of the last response time.</xd:desc>
    </xd:doc>
    <xsl:param name="LastResponseTimeURI"></xsl:param>
    
    <xsl:variable name="LastResponseTimeAbsURI">
        <xsl:call-template name="GetAbsoluteURI">
            <xsl:with-param name="SourceURI" select="$LastResponseTimeURI"/>
        </xsl:call-template>
    </xsl:variable>
    <xd:doc>
        <xd:desc>Load the responses or use an empty sequence if there is none.</xd:desc>
    </xd:doc>
    <xsl:variable name="LastResponseTime" select="if ($LastResponseTimeAbsURI != '' and fn:doc-available($LastResponseTimeAbsURI)) then fn:doc($LastResponseTimeAbsURI) else ()"></xsl:variable>
    
    <xd:doc>
        <xd:desc>This template does all the work.</xd:desc>
    </xd:doc>
    <xsl:template match="/">
        <ResponsesModifiedTime LastUpdated="{fn:current-dateTime()}" Comment="There are {count(//MetaData/Item[@Name='modifyDate'])} responses.">
            
            <!-- Handle each question in the summary file. -->
            <xsl:choose>
                <xsl:when test="$FixedResult != ''"><xsl:value-of select="$FixedResult"/></xsl:when>
                <xsl:when test="//MetaData/Item[@Name='modifyDate']">
                    <xsl:for-each select="//MetaData/Item[@Name='modifyDate']">
                        <xsl:sort select="." order="descending"/>
                        <xsl:message>Response with email <xsl:value-of select="../Item[@Name='email']"/> was modified at <xsl:value-of select="."/>.</xsl:message>
                        <xsl:if test="$Debug = 'Y'">
                            <xsl:comment>Response with email <xsl:value-of select="../Item[@Name='email']"/> was modified at <xsl:value-of select="."/>.</xsl:comment>
                        </xsl:if>
                        <xsl:choose>
                            <xsl:when test="position() = 1">
                                <xsl:value-of select="fn:format-dateTime(xs:dateTime(.) + xs:dayTimeDuration('PT0.001S'), '[Y]-[M,2]-[D,2]T[H01]:[m01]:[s01].[f001]Z',  'en', (),())"/>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="$LastResponseTime//ResponsesModifiedTime">
                    <xsl:message>Using Last ResponsesModifiedTime <xsl:value-of select="$LastResponseTime//ResponsesModifiedTime"/> from file updated <xsl:value-of select="$LastResponseTime//ResponsesModifiedTime/@LastUpdated"/>.</xsl:message>
                    <xsl:value-of select="$LastResponseTime//ResponsesModifiedTime"/>
                    <!--<xsl:value-of select="fn:format-dateTime(xs:dateTime($LastResponseTime//ResponsesModifiedTime/@LastUpdated) + xs:dayTimeDuration('PT1S'), '[Y]-[M,2]-[D,2] [H01]:[m01]:[s01]',  'en', (),())"/>
                    <xsl:value-of select="fn:format-dateTime(xs:dateTime(replace($LastResponseTime//ResponsesModifiedTime,' ','T')) + xs:dayTimeDuration('PT1S'), '[Y]-[M,2]-[D,2] [H01]:[m01]:[s01]',  'en', (),())"/>-->
                </xsl:when>
                <xsl:otherwise>2016-01-01 00:00:00</xsl:otherwise>
            </xsl:choose>
        </ResponsesModifiedTime>
    </xsl:template>
    
    <xsl:template name="GetAbsoluteURI">
        <xsl:param name="SourceURI"></xsl:param>
        <xsl:choose>
            <xsl:when test="fn:doc-available($SourceURI)"><xsl:value-of select="$SourceURI"/></xsl:when>
            <xsl:when test="fn:base-uri(/) != ''"><xsl:value-of select="fn:resolve-uri($SourceURI,fn:base-uri(/))"/></xsl:when>
            <xsl:when test="fn:base-uri() != ''"><xsl:value-of select="fn:resolve-uri($SourceURI,fn:base-uri())"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="$SourceURI"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>