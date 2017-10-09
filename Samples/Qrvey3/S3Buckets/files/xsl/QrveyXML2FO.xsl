<xsl:stylesheet version = '2.0'
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
    xmlns:fn='http://www.w3.org/2005/xpath-functions'
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xd="http://schemas.microsoft.com/xmltools/2002/xmldiff"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <!-- 
	
	A simple XSL:FO sample for RESTFetcher processing of Qrvey API data.
	
	This takes a Qrvey in XML format and produces a paper based layout of the Qrvey. 
	
	Author Ian Hogan, Ian_MacDonald_Hogan@yahoo.com
	
-->
    
    <!-- Results will be XML -->
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
    
    <xsl:preserve-space elements="Note"/>
    
    <!-- Processing parameters. -->
    <xsl:param name="ApplicationTitle">Qrvey Printable Format</xsl:param>
    <xsl:param name="ApplicationSubTitle">
        <xsl:call-template name="ReplaceParameters"><xsl:with-param name="value" select="//qrvey/@name"/></xsl:call-template>
    </xsl:param>
    <xsl:param name="PageLogoURL"/>
    <xsl:param name="PageLogoScale">100%</xsl:param>
    <xsl:param name="EntityImageURIPattern"/>
    <xsl:param name="EntityImageArchivedURIPattern"/>
    <xsl:param name="EntityIconScale">16px</xsl:param>
    <xsl:param name="IconImageURIPattern">../images/-iconPath-</xsl:param>
    <xsl:param name="ParametersListURI"/>
    <xsl:param name="Debug">No</xsl:param>
    
    <!-- Define format attribute sets. -->
    <xsl:attribute-set name="HeaderStyle">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">9pt</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="stdPadding">
        <xsl:attribute name="padding-top">4pt</xsl:attribute>
        <xsl:attribute name="padding-left">4pt</xsl:attribute>
        <xsl:attribute name="padding-right">4pt</xsl:attribute>
        <xsl:attribute name="padding-bottom">4pt</xsl:attribute>
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
    <xsl:attribute-set name="optionNotes">
        <xsl:attribute name="text-align">left</xsl:attribute>
        <xsl:attribute name="font-style">italic</xsl:attribute>
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
    <xsl:attribute-set name="questionText">
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">10pt</xsl:attribute>
        <xsl:attribute name="text-align">left</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="questionNumber">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="font-family">Lucida Sans</xsl:attribute>
        <xsl:attribute name="font-size">10pt</xsl:attribute>
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

    <xsl:variable name="ParametersListAbsURI">
        <xsl:call-template name="GetAbsoluteURI">
            <xsl:with-param name="SourceURI" select="$ParametersListURI"/>
        </xsl:call-template>
    </xsl:variable>
    <xd:doc>
        <xd:desc>Load the styles or use an empty sequence if there is none.</xd:desc>
    </xd:doc>
    <xsl:variable name="Parameters" select="if (fn:doc-available($ParametersListAbsURI)) then fn:doc($ParametersListAbsURI) else ()"></xsl:variable>
    
    <xsl:variable name="QuestionIndex">
        <xsl:for-each select="//question">
            <question id="{@id}">
                <xsl:number level="any"></xsl:number>
            </question>
        </xsl:for-each>
    </xsl:variable>
    
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
                            <dc:description>Qrvey Printable Form.</dc:description>
                        </rdf:Description>
                        <rdf:Description rdf:about=""
                            xmlns:xmp="http://ns.adobe.com/xap/1.0/">
                            <!-- XMP properties go here -->
                            <xmp:CreatorTool>RESTFetcher by Ian_MacDonald_Hogan@yahoo.com.</xmp:CreatorTool>
                        </rdf:Description>
                    </rdf:RDF>
                </x:xmpmeta>
            </fo:declarations>      

            <fo:page-sequence master-reference="A4Portrait">
                <fo:static-content flow-name="xsl-region-before">
                    <fo:table table-layout="fixed" width="100%">
                        <fo:table-column column-width="60%"/>
                        <fo:table-column column-width="40%"/>
                        <fo:table-body>
                            <xsl:variable name="HeaderImageURL">
                                <xsl:if test="$PageLogoURL != ''">
                                    <xsl:for-each select="/*[1]">
                                        <xsl:value-of select="fn:resolve-uri($PageLogoURL)"/>
                                    </xsl:for-each>
                                </xsl:if>
                            </xsl:variable>
                            <xsl:variable name="HeaderImageScale" select="$PageLogoScale"/>
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
                                        <!-- TODO: Link to particular Qrvey? -->
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
                    <xsl:if test="$Debug='Yes'">
                        <fo:block-container>
                            <fo:table>
                                <fo:table-column column-width="50%"/>
                                <fo:table-column column-width="50%"/>
                                <fo:table-header>
                                    <fo:table-row>
                                        <fo:table-cell><fo:block>ID</fo:block></fo:table-cell>
                                        <fo:table-cell><fo:block>Number</fo:block></fo:table-cell>
                                    </fo:table-row>
                                </fo:table-header>
                                <fo:table-body>
                                    <xsl:for-each select="$QuestionIndex//question">
                                        <fo:table-row>
                                            <fo:table-cell><fo:block><xsl:value-of select="@id"/></fo:block></fo:table-cell>
                                            <fo:table-cell><fo:block><xsl:value-of select="."/></fo:block></fo:table-cell>
                                        </fo:table-row>
                                    </xsl:for-each>
                                </fo:table-body>
                            </fo:table>
                        </fo:block-container>
                    </xsl:if>
                    <xsl:for-each select="//question">
                        <fo:block-container width="100%" keep-together.within-page="5">
                            <fo:table>
                                <fo:table-column column-width="10%"/>
                                <fo:table-column column-width="90%"/>
                                <fo:table-body>
                                    <fo:table-row xsl:use-attribute-sets="stdBorders">
                                        <fo:table-cell xsl:use-attribute-sets="questionNumber stdPadding">
                                            <fo:block>
                                                <xsl:value-of select="$QuestionIndex//question[@id=current()/@id]"/>.
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="questionText stdPadding">
                                            <fo:block-container>
                                                <fo:block padding-after="2mm">
                                                    <xsl:call-template name="ReplaceParameters"><xsl:with-param name="value" select="@text"/></xsl:call-template>
                                                    <fo:inline xsl:use-attribute-sets="optionNotes" margin-left="2mm">
                                                        (<xsl:choose>
                                                            <xsl:when test="@type='MULTIPLE_CHOICE'">Please select one or more options.</xsl:when>
                                                            <xsl:when test="@type='SINGLE_CHOICE' or @type='YES_NO'">Please select one or more options.</xsl:when>
                                                            <xsl:otherwise>Please enter your answer in the box below.</xsl:otherwise>
                                                        </xsl:choose>)
                                                    </fo:inline>
                                                    <!--<fo:inline xsl:use-attribute-sets="blockNotes">
                                                        <xsl:choose>
                                                            <xsl:when test="@_required='true'">This question must be completed.</xsl:when>
                                                            <xsl:otherwise>This question is optional.</xsl:otherwise>
                                                        </xsl:choose>
                                                    </fo:inline>-->
                                                </fo:block>
                                                <xsl:choose>
                                                    <xsl:when test="answer">
                                                        <fo:table>
                                                            <fo:table-body>
                                                                <!--<fo:table-row>
                                                                    <fo:table-cell>
                                                                        <fo:block>
                                                                            <xsl:choose>
                                                                                <xsl:when test="@type='MULTIPLE_CHOICE'">Please select one or more options</xsl:when>
                                                                                <xsl:otherwise>Please select just one option.</xsl:otherwise>
                                                                            </xsl:choose>
                                                                        </fo:block>
                                                                    </fo:table-cell>
                                                                </fo:table-row>-->
                                                                <fo:table-row>
                                                                    <fo:table-cell>
                                                                        <fo:block>
                                                                            <fo:table>
                                                                                <fo:table-column column-width="5%"/>
                                                                                <fo:table-column column-width="5%"/>
                                                                                <fo:table-column column-width="60%"/>
                                                                                <fo:table-column column-width="30%"/>
                                                                                <fo:table-body>
                                                                                    <xsl:for-each select="answer">
                                                                                        <xsl:sort select="if(../@_sort_options='true') then text()[normalize-space()][1] else position()"></xsl:sort>
                                                                                        <fo:table-row>
                                                                                            <fo:table-cell text-align="right" padding-right="2mm">
                                                                                                <fo:block>
                                                                                                    <xsl:value-of select="position()"/>.
                                                                                                </fo:block>
                                                                                            </fo:table-cell>
                                                                                            <fo:table-cell xsl:use-attribute-sets="stdBorders">
                                                                                                <fo:block>
                                                                                               </fo:block>
                                                                                            </fo:table-cell>
                                                                                            <fo:table-cell padding-left="2mm">
                                                                                                <fo:block>
                                                                                                    <xsl:call-template name="ReplaceParameters">
                                                                                                        <xsl:with-param name="value">
                                                                                                            <xsl:for-each select="text()[normalize-space(.)]">
                                                                                                                <xsl:if test="position() &gt; 1"><xsl:text> </xsl:text></xsl:if>
                                                                                                                <xsl:value-of select="normalize-space(.)"/>
                                                                                                            </xsl:for-each>
                                                                                                        </xsl:with-param>
                                                                                                    </xsl:call-template>
                                                                                                    <xsl:if test="$Debug='Y'">
                                                                                                        (<xsl:value-of select="if(../@_sort_options='true') then text()[normalize-space()][1] else position()"/>)
                                                                                                    </xsl:if>
                                                                                                </fo:block>
                                                                                            </fo:table-cell>
                                                                                            <fo:table-cell>
                                                                                                <fo:block>
                                                                                                    <xsl:choose>
                                                                                                        <xsl:when test="route">Go to <xsl:value-of select="$QuestionIndex//question[@id=current()/route/question[1]/@id]"/>.</xsl:when>
                                                                                                        <xsl:when test="../answer[route] and $QuestionIndex//question[@id=current()/../following-sibling::question[1]/@id] != ''">Go to <xsl:value-of select="$QuestionIndex//question[@id=current()/../following-sibling::question[1]/@id]"/>.</xsl:when>
                                                                                                        <xsl:when test="../answer[route]">All finished.</xsl:when>
                                                                                                        <xsl:otherwise></xsl:otherwise>
                                                                                                    </xsl:choose>
                                                                                                </fo:block>
                                                                                            </fo:table-cell>
                                                                                        </fo:table-row>
                                                                                    </xsl:for-each>
                                                                                </fo:table-body>
                                                                            </fo:table>
                                                                        </fo:block>
                                                                    </fo:table-cell>
                                                                </fo:table-row>
                                                            </fo:table-body>
                                                        </fo:table>
                                                    </xsl:when>
                                                    <xsl:when test="@type='YES_NO'">
                                                        <fo:table>
                                                            <fo:table-body>
                                                                <fo:table-row>
                                                                    <fo:table-cell>
                                                                        <fo:block>
                                                                            <fo:table>
                                                                                <fo:table-column column-width="5%"/>
                                                                                <fo:table-column column-width="5%"/>
                                                                                <fo:table-column column-width="60%"/>
                                                                                <fo:table-column column-width="30%"/>
                                                                                <fo:table-body>
                                                                                    <xsl:for-each select="tokenize('Yes,No',',')">
                                                                                        <fo:table-row>
                                                                                            <fo:table-cell text-align="right" padding-right="2mm">
                                                                                                <fo:block>
                                                                                                    <xsl:value-of select="position()"/>.
                                                                                                </fo:block>
                                                                                            </fo:table-cell>
                                                                                            <fo:table-cell xsl:use-attribute-sets="stdBorders">
                                                                                                <fo:block>
                                                                                                </fo:block>
                                                                                            </fo:table-cell>
                                                                                            <fo:table-cell padding-left="2mm">
                                                                                                <fo:block>
                                                                                                    <xsl:value-of select="."/>
                                                                                                </fo:block>
                                                                                            </fo:table-cell>
                                                                                            <fo:table-cell>
                                                                                                <fo:block>
                                                                                                </fo:block>
                                                                                            </fo:table-cell>
                                                                                        </fo:table-row>
                                                                                    </xsl:for-each>
                                                                                </fo:table-body>
                                                                            </fo:table>
                                                                        </fo:block>
                                                                    </fo:table-cell>
                                                                </fo:table-row>
                                                            </fo:table-body>
                                                        </fo:table>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <fo:table>
                                                            <fo:table-column column-width="70%"/>
                                                            <fo:table-column column-width="30%"/>
                                                            <fo:table-body>
                                                                <fo:table-row>
                                                                    <fo:table-cell>
                                                                        <xsl:call-template name="EmitDataEntryField">
                                                                            <xsl:with-param name="FieldType" select="@type"/>
                                                                        </xsl:call-template>
                                                                    </fo:table-cell>
                                                                    <fo:table-cell>
                                                                        <fo:block>
                                                                            <xsl:choose>
                                                                                <xsl:when test="ancestor::question/following-sibling::question">Go to <xsl:value-of select="$QuestionIndex//question[@id=current()/ancestor::question/following-sibling::question[1]/@id]"/>.</xsl:when>
                                                                                <xsl:otherwise>All finished.</xsl:otherwise>
                                                                            </xsl:choose>
                                                                        </fo:block>
                                                                    </fo:table-cell>
                                                                </fo:table-row>
                                                            </fo:table-body>
                                                        </fo:table>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </fo:block-container>
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
    
    <xsl:template name="EmitDataEntryField">
        <xsl:param name="FieldType"/>
        <xsl:param name="FieldWidth">80</xsl:param>
        <xsl:param name="FieldHeight">5</xsl:param>
        <xsl:param name="FieldCharacter"> </xsl:param>
        <fo:block xsl:use-attribute-sets="stdBorders" padding-top="{concat($FieldHeight,'mm')}" margin-right="2mm">
            <xsl:sequence select="string-join((for $i in 1 to $FieldWidth return $FieldCharacter),'')"/>
        </fo:block>
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
        <xsl:param name="TableType"/>
        <fo:block xsl:use-attribute-sets="subSectionHeader" padding-top="{$HalfLineHeight}" padding-bottom="{$HalfLineHeight}">
            <xsl:value-of select="$TableType"/>
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
                                <xsl:call-template name="ReplaceParameters"><xsl:with-param name="value" select="."/></xsl:call-template>
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
        <xsl:call-template name="ReplaceParameters"><xsl:with-param name="value" select="."/></xsl:call-template>
    </xsl:template>
    
    <xsl:template name="GetAbsoluteURI">
        <xsl:param name="SourceURI"></xsl:param>
        <xsl:choose>
            <xsl:when test="fn:base-uri(/) != ''"><xsl:value-of select="fn:resolve-uri($SourceURI,fn:base-uri(/))"/></xsl:when>
            <xsl:when test="fn:base-uri() != ''"><xsl:value-of select="fn:resolve-uri($SourceURI,fn:base-uri())"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="$SourceURI"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="ReplaceParameters">
        <xsl:param name="value" select="."/>
        <xsl:param name="paramPos">1</xsl:param>
        <xsl:choose>
            <xsl:when test="count($Parameters//Parameter) > $paramPos">
                <xsl:call-template name="ReplaceParameters">
                    <xsl:with-param name="value" select="replace($value,concat('[\$]\{',$Parameters//Parameter[position() = $paramPos]/@Name,'\}'),normalize-space($Parameters//Parameter[position() = $paramPos]))"/>
                    <xsl:with-param name="paramPos" select="$paramPos + 1"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$Parameters//Parameter[$paramPos]">
                <xsl:value-of select="replace($value,concat('[\$]\{',$Parameters//Parameter[position() = $paramPos]/@Name,'\}'),normalize-space($Parameters//Parameter[position() = $paramPos]))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$value"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

