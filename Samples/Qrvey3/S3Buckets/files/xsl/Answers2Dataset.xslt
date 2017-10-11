<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    exclude-result-prefixes="xs xd"
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
    <xsl:param name="Debug">N</xsl:param>
    <xsl:template match="/">
        <Dataset>
            <xsl:for-each select="//Items[$TimeFilter = '' or modifyDate >= $TimeFilter]">
                <DataRow>
                    <MetaData>
                        <xsl:for-each select="*[local-name()!='answers']">
                            <Item Name="{local-name()}"><xsl:value-of select="normalize-space(.)"/></Item>
                        </xsl:for-each>
                    </MetaData>
                    <xsl:for-each select="answers">
                        <xsl:choose>
                            <xsl:when test="data = type">
                                <xsl:for-each select="*[lower-case(local-name())=lower-case(../type)]/*">
                                    <Item Name="{../../id}.{local-name()}" AnswerId="{data_ansid}"><xsl:value-of select="normalize-space(.)"/></Item>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <Item Name="{id}" AnswerId="{data_ansid}"><xsl:value-of select="normalize-space(data)"/></Item>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </DataRow>
            </xsl:for-each>
        </Dataset>
    </xsl:template>
</xsl:stylesheet>