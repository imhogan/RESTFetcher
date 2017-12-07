<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    exclude-result-prefixes="xsl xs fn xd"
    version="2.0">
    <xd:doc scope="stylesheet">
        <xd:desc>
            <xd:p><xd:b>Created on:</xd:b> Jan 3, 2017</xd:p>
            <xd:p><xd:b>Author:</xd:b> Ian</xd:p>
            <xd:p></xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:output indent="yes"></xsl:output>
    <xsl:param name="TimeFilter"></xsl:param>
    <xsl:param name="FullValues">Y</xsl:param>
    <xsl:param name="Debug">N</xsl:param>
    <xd:doc>
        <xd:desc>The URI of the styles file.</xd:desc>
    </xd:doc>
    <xsl:param name="QrveyURI">Qrvey.xml</xsl:param>
    
    <xsl:variable name="QrveyAbsURI">
        <xsl:call-template name="GetAbsoluteURI">
            <xsl:with-param name="SourceURI" select="$QrveyURI"/>
        </xsl:call-template>
    </xsl:variable>
    <xd:doc>
        <xd:desc>Load the styles or use an empty sequence if there is none.</xd:desc>
    </xd:doc>
    <xsl:variable name="Qrvey" select="if (fn:doc-available($QrveyAbsURI)) then fn:doc($QrveyAbsURI) else ()"></xsl:variable>
    
    <xsl:template match="/">
        <Dataset>
            <xsl:for-each select="//Items[$TimeFilter = '' or modifyDate >= $TimeFilter]">
                <DataRow>
                    <xsl:if test="$Debug='Y'">
                        <xsl:comment>Qrvey is <xsl:value-of select="$Qrvey//qrvey/settings/description"/></xsl:comment>
                    </xsl:if>
                    <xsl:copy-of select="*[local-name()!='answers']"/>
                    <xsl:for-each select="answers">
                        <xsl:variable name="QuestionId" select="id"/>
                        <xsl:choose>
                            <xsl:when test="data = type">
                                <xsl:for-each select="*[lower-case(local-name())=lower-case(../type)]/*">
                                    <xsl:if test="$Debug='Y'">
                                        <xsl:comment>Answer is <xsl:value-of select="normalize-space(current())"/></xsl:comment>
                                        <xsl:comment>Answer ID is <xsl:value-of select="$Qrvey//answer[.=normalize-space(current())]/@answerid"/></xsl:comment>
                                    </xsl:if>
                                    <xsl:variable name="AnswerId">
                                        <xsl:choose>
                                            <xsl:when test="data_ansid != ''"><xsl:value-of select="data_ansid"/></xsl:when>
                                            <xsl:otherwise><xsl:value-of select="$Qrvey//answer[.=normalize-space(current())]/@answerid"/></xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:variable>
                                    <xsl:element name="{../../id}.{local-name()}">
                                        <xsl:value-of select="normalize-space(.)"/>
                                    </xsl:element>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:for-each select="data">
                                    <xsl:variable name="DataPosition" select="position()"/>
                                    <xsl:if test="$Debug='Y'">
                                        <xsl:comment>Answer is <xsl:value-of select="normalize-space(.)"/></xsl:comment>
                                        <xsl:comment>Answer ID is <xsl:value-of select="$Qrvey//answer[.=normalize-space(.)]/@answerid"/></xsl:comment>
                                    </xsl:if>
                                    <xsl:variable name="AnswerId">
                                        <xsl:choose>
                                            <xsl:when test="../data_ansid[position()=$DataPosition] != ''"><xsl:value-of select="../data_ansid[position()=$DataPosition]"/></xsl:when>
                                            <xsl:otherwise><xsl:value-of select="$Qrvey//answer[.=normalize-space(current())]/@answerid"/></xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:variable>
                                    <xsl:variable name="Apos">'</xsl:variable>
                                    <xsl:element name="{../id}.{replace(replace(replace(replace(normalize-space(.),' ','_'),'/','_'),$Apos,'_'),'&amp;','_')}">
                                        <xsl:choose>
                                            <xsl:when test="$FullValues = 'Y'">
                                                <xsl:value-of select="normalize-space(.)"/>
                                            </xsl:when>
                                            <xsl:otherwise>Y</xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:element>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="otherField">
                            <xsl:element name="{$QuestionId}.Other"><xsl:value-of select="normalize-space(otherField)"/></xsl:element>
                        </xsl:if>
                    </xsl:for-each>
                </DataRow>
            </xsl:for-each>
        </Dataset>
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