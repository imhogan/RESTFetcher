<xsl:stylesheet version = '2.0'
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
    xmlns:fn='http://www.w3.org/2005/xpath-functions'
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xd="http://schemas.microsoft.com/xmltools/2002/xmldiff"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <!-- 
	
	A simple XSL:FO sample for RESTFetcher processing of Qrvey API data.
	
	This takes the Qrvey response with resident details and creates a welcome letter. 
	
	TODO: Complete this stylesheet!
	
	Author Ian Hogan, Ian_MacDonald_Hogan@yahoo.com
	
-->
    
    <!-- Results will be XML -->
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
    
    <xsl:preserve-space elements="Note"/>
    
    <!-- Processing parameters. -->
    <xsl:param name="ApplicationTitle">RESTFetcher Qrvey Example</xsl:param>
    <xsl:param name="ApplicationSubTitle">Produced by Ian Hogan, Ian_MacDonald_Hogan@yahoo.com.</xsl:param>
    <xsl:param name="ReportLogoURL">../images/Logo.png</xsl:param>
    <xsl:param name="ReportLogoScale">100%</xsl:param>
    <xsl:param name="EntityImageURIPattern">../images/-EntityType-.png</xsl:param>
    <xsl:param name="EntityImageArchivedURIPattern">../images/_-EntityType-.png</xsl:param>
    <xsl:param name="EntityIconScale">16px</xsl:param>
    <xsl:param name="IconImageURIPattern">../images/-iconPath-</xsl:param>
    <xsl:param name="Debug">N</xsl:param>
    
    <!-- Define format attribute sets. -->
    <xsl:attribute-set name="HeaderStyle">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">9pt</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="stdPadding">
        <xsl:attribute name="padding-top">1pt</xsl:attribute>
        <xsl:attribute name="padding-left">4pt</xsl:attribute>
        <xsl:attribute name="padding-right">4pt</xsl:attribute>
        <xsl:attribute name="padding-bottom">1pt</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="stdBorders">
        <xsl:attribute name="border-top-style">solid</xsl:attribute>
        <xsl:attribute name="border-left-style">solid</xsl:attribute>
        <xsl:attribute name="border-right-style">solid</xsl:attribute>
        <xsl:attribute name="border-bottom-style">solid</xsl:attribute>
        <xsl:attribute name="border-top-color">black</xsl:attribute>
        <xsl:attribute name="border-left-color">black</xsl:attribute>
        <xsl:attribute name="border-right-color">black</xsl:attribute>
        <xsl:attribute name="border-bottom-color">black</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="sectionHeader">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">9pt</xsl:attribute>
        <xsl:attribute name="border-bottom-style">solid</xsl:attribute>
        <xsl:attribute name="text-align">left</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="subSectionHeader">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">9pt</xsl:attribute>
        <xsl:attribute name="text-align">left</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="tableHeader">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">9pt</xsl:attribute>
        <xsl:attribute name="text-align">left</xsl:attribute>
        <xsl:attribute name="background-color">#E8E8E8</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="subTableHeaderCell">
        <xsl:attribute name="background-color">Beige</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="subTableHeader">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">9pt</xsl:attribute>
        <xsl:attribute name="text-align">left</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="subTable" use-attribute-sets="stdBorders stdPadding">
        <xsl:attribute name="text-align">left</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="blockNotes" use-attribute-sets="stdBorders stdPadding">
        <xsl:attribute name="text-align">left</xsl:attribute>
        <xsl:attribute name="background-color">#E8E8E8</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="cellSubTableHeader" use-attribute-sets="stdBorders stdPadding">
        <xsl:attribute name="text-align">left</xsl:attribute>
        <xsl:attribute name="background-color">#E8E8E8</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="cellLabel">
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">9pt</xsl:attribute>
        <xsl:attribute name="text-align">left</xsl:attribute>
        <xsl:attribute name="padding-left">3pt</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="cellText">
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">8pt</xsl:attribute>
        <xsl:attribute name="text-align">left</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="cellNumeric">
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">9pt</xsl:attribute>
        <xsl:attribute name="text-align">center</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="pageHeader">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">10pt</xsl:attribute>
        <xsl:attribute name="background-color">red</xsl:attribute>
        <xsl:attribute name="color">white</xsl:attribute>
        <xsl:attribute name="text-align">center</xsl:attribute>
        <xsl:attribute name="padding-top">1pt</xsl:attribute>
        <xsl:attribute name="padding-bottom">1pt</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="pageFooter">
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">9pt</xsl:attribute>
        <xsl:attribute name="font-style">italic</xsl:attribute>
    </xsl:attribute-set>
    
    <!-- Define format constants. -->
    <xsl:variable name="LineHeight"           select="'4mm'"              />
    <xsl:variable name="HalfLineHeight"       select="'2mm'"              />
    <xsl:variable name="DoubleLineHeight"     select="'8mm'"              />
    
    <!-- Start with the root node. -->
    <xsl:template match="/">
        <fo:root>
            
            <fo:layout-master-set>
                <fo:simple-page-master  master-name="A4Landscape"
                    margin-right="1.5cm"
                    margin-left="1cm"
                    margin-bottom="1cm"
                    margin-top="1cm"
                    page-width="29.7cm"
                    page-height="21cm">
                    <fo:region-body margin-top="3.5cm" margin-bottom="1.2cm"/>
                    <fo:region-before extent="3.5cm"/>
                    <fo:region-after extent="1cm"/>
                </fo:simple-page-master>
                
                <fo:simple-page-master  master-name="A4Portrait"
                    margin-right="1.5cm"
                    margin-left="1cm"
                    margin-bottom="1cm"
                    margin-top="1cm"
                    page-width="21cm"
                    page-height="29.7cm">
                    <fo:region-body margin-top="1cm" margin-bottom="1.2cm"/>
                    <fo:region-before extent="1cm"/>
                    <fo:region-after extent="1cm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            
            <fo:declarations>
                <x:xmpmeta xmlns:x="adobe:ns:meta/">
                    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <rdf:Description rdf:about=""
                            xmlns:dc="http://purl.org/dc/elements/1.1/">
                            <!-- Dublin Core properties go here -->
                            <dc:title><xsl:value-of select="$ApplicationTitle"/></dc:title>
                            <dc:creator>Ian Hogan - Ian_MacDonald_Hogan@yahoo.com</dc:creator>
                            <dc:description>Sample Qrvey Response report generated by RESTFetcher.</dc:description>
                        </rdf:Description>
                        <rdf:Description rdf:about=""
                            xmlns:xmp="http://ns.adobe.com/xap/1.0/">
                            <!-- XMP properties go here -->
                            <xmp:CreatorTool>RESTFetcher by Ian_MacDonald_Hogan@yahoo.com.</xmp:CreatorTool>
                        </rdf:Description>
                    </rdf:RDF>
                </x:xmpmeta>
            </fo:declarations>      

            <fo:page-sequence master-reference="A4Landscape">
                <fo:static-content flow-name="xsl-region-before">
                    <fo:table table-layout="fixed" width="100%">
                        <fo:table-column column-width="60%"/>
                        <fo:table-column column-width="40%"/>
                        <fo:table-body>
                            <xsl:variable name="HeaderImageURL">
                                <xsl:if test="$ReportLogoURL != ''">
                                    <xsl:for-each select="/*[1]">
                                        <xsl:value-of select="fn:resolve-uri($ReportLogoURL)"/>
                                    </xsl:for-each>
                                </xsl:if>
                            </xsl:variable>
                            <xsl:variable name="HeaderImageScale" select="$ReportLogoScale"/>
                            <xsl:if test="$HeaderImageURL != ''">
                                <fo:table-row>
                                    <fo:table-cell number-columns-spanned="2">
                                        <fo:block>
                                            <fo:basic-link external-destination="https://github.com/imhogan" show-destination="new">
                                                <fo:external-graphic src="{$HeaderImageURL}" width="{$HeaderImageScale}" content-width="scale-to-fit" content-height="{$HeaderImageScale}"/>
                                            </fo:basic-link>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </xsl:if>
                            <fo:table-row>
                                <fo:table-cell xsl:use-attribute-sets="pageHeader" text-align="left">
                                    <fo:block>
                                        <fo:basic-link external-destination="https://www.qrvey.com/" show-destination="new">
                                            <xsl:value-of select="$ApplicationTitle"/>
                                        </fo:basic-link>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell xsl:use-attribute-sets="pageHeader" text-align="right">
                                    <fo:block>
                                        <xsl:value-of select="$ApplicationSubTitle"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>
                </fo:static-content>
                <fo:static-content flow-name="xsl-region-after">
                    <fo:table table-layout="fixed" width="100%">
                        <fo:table-column column-width="80%"/>
                        <fo:table-column column-width="20%"/>
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell xsl:use-attribute-sets="pageFooter" text-align="left">
                                    <fo:block>
                                        <xsl:value-of select="fn:format-dateTime(fn:current-dateTime(),'[MNn] [D], [Y]', 'en', (), ())"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell xsl:use-attribute-sets="pageFooter" text-align="right">
                                    <fo:block>
                                        Page <fo:page-number/> of <fo:page-number-citation ref-id="terminator"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>
                </fo:static-content>
                
                <fo:flow flow-name="xsl-region-body">
                    <xsl:for-each select="//DataRow">
                        <!-- TODO: Emit a simple page with the unit number, resident details and welcome message. -->
                        <fo:block-container width="100%" keep-together.within-page="5">
                            <fo:table>
                                <fo:table-header>
                                    <fo:table-row>
                                        <fo:table-cell>
                                            <fo:block xsl:use-attribute-sets="sectionHeader" padding-top="{$LineHeight}">
                                                <xsl:call-template name="EmitTemplateTitle">
                                                    <xsl:with-param name="Template" select="."/>
                                                </xsl:call-template>
                                            </fo:block>
                                            <fo:block xsl:use-attribute-sets="subSectionHeader" padding-top="{$LineHeight}">
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </fo:table-header>
                                <fo:table-body>
                                    <fo:table-row>
                                        <fo:table-cell>
                                            <fo:block>
                                                <xsl:call-template name="EmitProperiesTable">
                                                    <xsl:with-param name="Properties" select="*[not(contains(',templateID,name,questions,',concat(',',local-name(),',')))]"/>
                                                    <xsl:with-param name="Type" select="' Properties'"/>
                                                </xsl:call-template>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </fo:table-body>
                            </fo:table>
                            
                        </fo:block-container>
                        
                    </xsl:for-each>
                    <xsl:if test="position()=last()">
                        <fo:block id="terminator"/>
                    </xsl:if>
                </fo:flow>
            </fo:page-sequence>
            
        </fo:root>
    </xsl:template>
    
    <xsl:template name="EmitTemplateTitle">
        <xsl:param name="Template"/>
        <xsl:for-each select="$Template">
            <fo:external-graphic src="{fn:resolve-uri(replace($EntityImageURIPattern,'-EntityType-','Survey'))}" width="{$EntityIconScale}" content-width="scale-to-fit" content-height="{$EntityIconScale}"/>
            <xsl:value-of select="concat('  Template: ',templateID,' - ',name)"/>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="EmitProperiesTable">
        <xsl:param name="Properties"/>
        <xsl:param name="Type"/>
        <fo:block xsl:use-attribute-sets="subSectionHeader" padding-top="{$HalfLineHeight}" padding-bottom="{$HalfLineHeight}">
            <xsl:value-of select="$Type"/>
        </fo:block>
        
        <fo:table table-layout="fixed" width="100%">
            <fo:table-column column-width="20%"/>
            <fo:table-column column-width="80%"/>
            <fo:table-header>
                <fo:table-row>
                    <fo:table-cell xsl:use-attribute-sets="cellSubTableHeader">
                        <fo:block xsl:use-attribute-sets="subTableHeader">
                            Property
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="cellSubTableHeader">
                        <fo:block xsl:use-attribute-sets="subTableHeader">
                            Value
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <xsl:for-each select="$Properties">
                    <fo:table-row keep-together="5">
                        <xsl:if test="@Advanced='Y' or @Type='hidden'">
                            <xsl:attribute name="background-color">#FFE6CD</xsl:attribute>
                        </xsl:if>
                        <fo:table-cell xsl:use-attribute-sets="subTable">
                            <fo:block xsl:use-attribute-sets="subTableHeader">
                                <xsl:value-of select="local-name()"/>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="subTable">
                            <fo:block xsl:use-attribute-sets="subTableHeader">
                                <xsl:value-of select="."/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:for-each>
                <xsl:if test="not($Properties)">
                    <fo:table-row>
                        <fo:table-cell number-columns-spanned="3">
                            <fo:block xsl:use-attribute-sets="subTableHeader">
                                No fields.
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
            </fo:table-body>
        </fo:table>
    </xsl:template>
    
    <xsl:template match="menuItem">
        <fo:inline font-style="italic" color="red"><xsl:apply-templates select="* | text()"/></fo:inline>
    </xsl:template>
    
    <xsl:template match="button">
        <fo:inline font-weight="bold"><xsl:apply-templates select="* | text()"/></fo:inline>
    </xsl:template>
    
    <xsl:template match="icon">
        <fo:external-graphic src="{replace($IconImageURIPattern,'-iconPath-',@Path)}" width="{@Scale}" content-width="scale-to-fit" content-height="{@Scale}"/>
    </xsl:template>

    <xsl:template match="text()">
        <xsl:value-of select="."/>
    </xsl:template>
    
</xsl:stylesheet>

