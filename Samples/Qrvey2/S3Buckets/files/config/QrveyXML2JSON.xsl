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
            <xd:p><xd:b>Created on:</xd:b> Dec 16, 2016</xd:p>
            <xd:p><xd:b>Author:</xd:b> Ian</xd:p>
            <xd:p></xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:output method="text" indent="no" xml:space="default" />
    
    <xd:doc>
        <xd:desc>Debug flag.</xd:desc>
    </xd:doc>
    <xsl:param name="Debug">Y</xsl:param>
        
    <xsl:param name="StyleUser">ss</xsl:param>
    <xsl:param name="QrveyUser">qq</xsl:param>
    <xsl:param name="StyleCollectionName">OurVillage</xsl:param>
    <xd:doc>
        <xd:desc>The URI of the styles file.</xd:desc>
    </xd:doc>
    <xsl:param name="StylesURI">QrveyStyles.xml</xsl:param>
        
    <xsl:variable name="StylesAbsURI">
        <xsl:call-template name="GetAbsoluteURI">
            <xsl:with-param name="SourceURI" select="$StylesURI"/>
        </xsl:call-template>
    </xsl:variable>
    <xd:doc>
        <xd:desc>Load the styles or use an empty sequence if there is none.</xd:desc>
    </xd:doc>
    <xsl:variable name="Styles" select="if (fn:doc-available($StylesAbsURI)) then fn:doc($StylesAbsURI) else ()"></xsl:variable>
    
    <xsl:variable name="StandardAnswers">
        <Question Type="SLIDEBAR">
            <answer answer="left"/>
            <answer answer="right"/>
        </Question>
    </xsl:variable>
    
    <xsl:template match="/">
        <xsl:for-each select="qrvey">
<xsl:text>{</xsl:text>
    <xsl:text>"name": "</xsl:text><xsl:value-of select="@name"/><xsl:text>"</xsl:text>
    <xsl:text>,"description": "</xsl:text><xsl:value-of select="settings/description"/><xsl:text>"</xsl:text>
    <xsl:text>,"introPage": </xsl:text><xsl:value-of select="settings/properties/@introPage"/>
    <xsl:text>,"totalTime": </xsl:text><xsl:value-of select="sum(questions/question/@time) + number(concat('0',@extraTime))"/>
    <xsl:for-each select="$Styles//styleCollection[@name=$StyleCollectionName]/style">
        <xsl:text>"</xsl:text><xsl:value-of select="@type"/><xsl:text>": {</xsl:text>
             <xsl:text>"name": "</xsl:text><xsl:value-of select="@name"/><xsl:text>"</xsl:text>
             <xsl:call-template name="elements2JSON">
                    <xsl:with-param name="name">style</xsl:with-param>
                    <xsl:with-param name="elements" select="*"/>
             </xsl:call-template>
            <xsl:text>,"userid": "</xsl:text><xsl:value-of select="$StyleUser"/>"
            <xsl:text>,"styleid": </xsl:text><xsl:value-of select="@id"/>
            <xsl:text>,"newChanges": </xsl:text><xsl:value-of select="@newChanges"/>
         <xsl:text>}</xsl:text>
    </xsl:for-each>
    <xsl:text>,</xsl:text><xsl:call-template name="emitQuestions">
        <xsl:with-param name="questionsGroup" select="questions"/>
    </xsl:call-template>
    <xsl:text>,"totalQuestions": </xsl:text><xsl:value-of select="count(questions/question)"/>
    <xsl:text>,"results_visibility": "</xsl:text><xsl:value-of select="settings/properties/@results_visibility"/><xsl:text>"</xsl:text>
    <xsl:text>,"respondent_email": "</xsl:text><xsl:value-of select="settings/properties/@respondent_email"/><xsl:text>"</xsl:text>
    <xsl:text>,"emailNotifications": </xsl:text><xsl:value-of select="settings/properties/@emailNotifications"/>
    <xsl:call-template name="elements2JSON">
        <xsl:with-param name="name">thankYouPage</xsl:with-param>
        <xsl:with-param name="elements" select="settings/thankYouPage/*"/>
    </xsl:call-template>
    <xsl:call-template name="elements2JSON">
        <xsl:with-param name="name">duration</xsl:with-param>
        <xsl:with-param name="elements" select="settings/duration/*"/>
    </xsl:call-template>
    <xsl:if test="$Debug='Y'">
        <xsl:text>,"debug":{</xsl:text>
        <xsl:text>"StylesAbsURI": "</xsl:text><xsl:value-of select="$StylesAbsURI"/><xsl:text>"</xsl:text>
        <xsl:text>,"fn:doc-available(StylesAbsURI)": "</xsl:text><xsl:value-of select="if (fn:doc-available($StylesAbsURI)) then 'T' else 'F'"/><xsl:text>"</xsl:text>
        <xsl:text>,"test2": "</xsl:text><xsl:value-of select="count($Styles//styleCollection)"/><xsl:text>"</xsl:text>
        <xsl:text>}</xsl:text>
    </xsl:if>
    <xsl:text>}</xsl:text>    
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="question">
        <xsl:variable name="mainOrOptional">
            <xsl:choose>
                <xsl:when test="@_required = 'true'">main</xsl:when>
                <xsl:otherwise>optional</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="hasPaths">
            <xsl:choose>
                <xsl:when test="answer/route">true</xsl:when>
                <xsl:otherwise>false</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:text>{</xsl:text>
            <xsl:call-template name="attributes2JSON">
                <xsl:with-param name="attributes" select="@*[not(starts-with(local-name(),'_'))]"/>
                <xsl:with-param name="autoIdField">id</xsl:with-param>
            </xsl:call-template>
        <xsl:choose>
            <xsl:when test="answer">
                <xsl:text>,</xsl:text><xsl:call-template name="emitAnswers">
                    <xsl:with-param name="question" select="."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$StandardAnswers/Question[@Type=current()/@type]">
                <xsl:text>,</xsl:text><xsl:call-template name="emitStandardAnswers">
                    <xsl:with-param name="question" select="$StandardAnswers/Question[@Type=current()/@type]"/>
                    <xsl:with-param name="idbase" select="generate-id()"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>,"answers": [</xsl:text>
                    <xsl:text>{</xsl:text>
                         <xsl:text>"answer":"</xsl:text><xsl:value-of select="@type"/><xsl:text>"</xsl:text>
                        <xsl:text>,"answerid":"</xsl:text><xsl:value-of select="generate-id(@type)"/><xsl:text>"</xsl:text>
                    <xsl:text>}</xsl:text>
                <xsl:text>]</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
            <xsl:text>,"question": "</xsl:text><xsl:value-of select="$mainOrOptional"/>"
            <xsl:text>,"hasPaths": </xsl:text><xsl:value-of select="$hasPaths"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

    <xsl:template match="answer">
        <xsl:text>{</xsl:text>
            <xsl:call-template name="attributes2JSON">
                <xsl:with-param name="attributes" select="@*[not(starts-with(local-name(),'_'))]"/>
                <xsl:with-param name="autoIdField">answerid</xsl:with-param>
            </xsl:call-template>
            <xsl:if test="route">
                <xsl:text>,"route": {</xsl:text>
                    <xsl:call-template name="attributes2JSON">
                        <xsl:with-param name="attributes" select="route/@*[not(starts-with(local-name(),'_'))]"/>
                        <xsl:with-param name="autoIdField">id</xsl:with-param>
                    </xsl:call-template>
                    ,<xsl:call-template name="emitQuestions">
                        <xsl:with-param name="questionsGroup" select="route"/>
                    </xsl:call-template>
                <xsl:text>}</xsl:text>
            </xsl:if>
        <xsl:text>}</xsl:text>
    </xsl:template>
    

    <xsl:template name="emitQuestions">
        <xsl:param name="questionsGroup"/>
        <xsl:text>"questions": {</xsl:text>
            <xsl:text>"data": [</xsl:text>
            <xsl:for-each select="$questionsGroup/question">
                <xsl:if test="position() &gt; 1">,</xsl:if>
                <xsl:apply-templates select="."/>
            </xsl:for-each>  
            <xsl:text>]</xsl:text>
        }
    </xsl:template>

    <xsl:template name="emitAnswers">
        <xsl:param name="question"/>
        <xsl:text>"answers": [</xsl:text>
            <xsl:for-each select="$question/answer">
                <xsl:if test="position() &gt; 1">,</xsl:if>
                <xsl:apply-templates select="."/>
            </xsl:for-each> 
        <xsl:text>]</xsl:text>
    </xsl:template>
    
    <xsl:template name="emitStandardAnswers">
        <xsl:param name="question"/>
        <xsl:param name="idbase"></xsl:param>
        <xsl:text>"answers": [</xsl:text>
        <xsl:for-each select="$question/answer">
            <xsl:if test="position() &gt; 1">,</xsl:if>
            <xsl:text>{</xsl:text>
            <xsl:call-template name="attributes2JSON">
                <xsl:with-param name="attributes" select="@*[not(starts-with(local-name(),'_'))]"/>
                <xsl:with-param name="autoIdField">answerid</xsl:with-param>
                <xsl:with-param name="idvalue" select="concat($idbase,string(position()))"/>
            </xsl:call-template>
            <xsl:text>}</xsl:text>
        </xsl:for-each> 
        <xsl:text>]</xsl:text>
    </xsl:template>
    
    <xsl:template name="elements2JSON">
        <xsl:param name="name"/>
        <xsl:param name="elements"/>
        <xsl:param name="sep">,</xsl:param>
        <xsl:param name="autoIdField"/>
        <xsl:if test="$elements">
            <xsl:value-of select="$sep"/>"<xsl:value-of select="$name"/><xsl:text>": {</xsl:text>
            <xsl:for-each select="$elements">
                <xsl:variable name="value">
                    <xsl:choose>
                        <xsl:when test=".='_AUTOID_'"><xsl:value-of select="generate-id()"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:if test="position() &gt; 1">,</xsl:if>"<xsl:value-of select="local-name()"/>":"<xsl:value-of select="$value"/>"
            </xsl:for-each>
            <xsl:if test="$autoIdField != '' and not($elements[local-name()=$autoIdField])">
                <xsl:if test="$elements">,</xsl:if>"<xsl:value-of select="$autoIdField"/>":"<xsl:value-of select="generate-id()"/>"
            </xsl:if>
            <xsl:text>}</xsl:text>    
        </xsl:if>
    </xsl:template>

    <xsl:template name="attributes2JSON">
        <xsl:param name="attributes"/>
        <xsl:param name="autoIdField"/>
        <xsl:param name="idvalue"/>
        <xsl:if test="$attributes">
            <xsl:for-each select="$attributes">
                <xsl:variable name="value">
                    <xsl:choose>
                        <xsl:when test=".='_AUTOID_' and $idvalue != ''"><xsl:value-of select="$idvalue"/></xsl:when>
                        <xsl:when test=".='_AUTOID_'"><xsl:value-of select="generate-id()"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:if test="position() &gt; 1">,</xsl:if>"<xsl:value-of select="local-name()"/>":"<xsl:value-of select="$value"/>"
            </xsl:for-each>
            <xsl:if test="$autoIdField != '' and not($attributes[local-name()=$autoIdField])">
                <xsl:if test="$attributes">,</xsl:if>"<xsl:value-of select="$autoIdField"/>":"<xsl:choose>
                    <xsl:when test="$idvalue != ''"><xsl:value-of select="$idvalue"/></xsl:when>
                    <xsl:otherwise><xsl:value-of select="generate-id()"/></xsl:otherwise>
                </xsl:choose>"
            </xsl:if>
        </xsl:if>
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