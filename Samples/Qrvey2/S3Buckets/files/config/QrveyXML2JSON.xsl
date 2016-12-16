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
    <xsl:output method="text"/>
    
    <xd:doc>
        <xd:desc>Debug flag.</xd:desc>
    </xd:doc>
    <xsl:param name="Debug">N</xsl:param>
        
    <xsl:param name="StyleUser"></xsl:param>
    <xsl:param name="QrveyUser"></xsl:param>
    <xsl:param name="StyleCollectionName"></xsl:param>
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
    
    <xsl:template match="Qrvey">
{
     "name": "<xsl:value-of select="@name"/>"
    ,"description": "<xsl:value-of select="settings/description"/>"
    ,"introPage": <xsl:value-of select="settings/properties/introPage"/>
    ,"totalTime": <xsl:value-of select="sum(number(questions/question/@time))"/>
    <xsl:for-each select="$Styles/styleCollection[@name=$StyleCollectionName]/style">
        "<xsl:value-of select="@type"/>": {
             "name": "<xsl:value-of select="@name"/>"
             <xsl:call-template name="elements2JSON">
                    <xsl:with-param name="name">style</xsl:with-param>
                    <xsl:with-param name="elements" select="*"/>
             </xsl:call-template>
            ,"userid": "<xsl:value-of select="$StyleUser"/>"
            ,"styleid": <xsl:value-of select="@id"/>
            ,"newChanges": <xsl:value-of select="@newChanges"/>
         }
    </xsl:for-each>
    ,"questions": {
        "data": [
        <xsl:for-each select="questions/question">
            <xsl:if test="position() &gt; 1">,</xsl:if>{
                <xsl:call-template name="attributes2JSON">
                    <xsl:with-param name="attributes" select="@*"/>
                </xsl:call-template>
            }
        </xsl:for-each>
        <!--  
            {
                "text": "Single choice question",
                "type": "SINGLE_CHOICE",
                "answers": [
                    {
                        "answer": "MC option 1",
                        "answerid": "UZO_TBT7a0"
                    },
                    {
                        "answer": "MC option 2",
                        "answerid": "UZO_TBT7a1"
                    }
                ],
                "id": "UZO_TBT7",
                "question": "main",
                "time": 6
            },
            {
                "text": "Single choice with Other option",
                "type": "SINGLE_CHOICE",
                "answers": [
                    {
                        "answer": "answer 1",
                        "answerid": "UZO_TBT7a0"
                    },
                    {
                        "answer": "answer 2",
                        "answerid": "UZO_TBT7a1"
                    }
                ],
                "id": "BZD9HPIA",
                "question": "main",
                "time": 6,
                "otherField": true
            },
            {
                "text": "Single choice optional question",
                "type": "SINGLE_CHOICE",
                "answers": [
                    {
                        "answer": "answer 1",
                        "answerid": "UZO_TBT7a0"
                    },
                    {
                        "answer": "answer 2",
                        "answerid": "UZO_TBT7a1"
                    }
                ],
                "id": "JGCOSEU3",
                "question": "optional",
                "time": 6
            },
            {
                "text": "HEADLINE",
                "type": "HEADLINE",
                "content": "Headline text - Start MultiSelection questions",
                "answers": [
                    {
                        "answer": "HEADLINE"
                    }
                ],
                "id": "3HT0KYEL",
                "question": "main",
                "time": 0
            },
            {
                "text": "Multiple choice with Multiple selections",
                "type": "MULTIPLE_CHOICE",
                "answers": [
                    {
                        "answer": "answer 1",
                        "answerid": "UZO_TBT7a0"
                    },
                    {
                        "answer": "answer 2",
                        "answerid": "UZO_TBT7a1"
                    }
                ],
                "id": "JDCBAMZQ",
                "question": "main",
                "time": 6,
                "hasPaths": false
            },
            {
                "text": "Multiple choice with multi selection and Other",
                "type": "MULTIPLE_CHOICE",
                "answers": [
                    {
                        "answer": "Answer 1",
                        "answerid": "UZO_TBT7a0"
                    },
                    {
                        "answer": "Answer 2",
                        "answerid": "UZO_TBT7a1"
                    }
                ],
                "id": "Q_IVDTFV",
                "question": "main",
                "time": 6,
                "otherField": true,
                "hasPaths": false
            },
            {
                "text": "Single choice with Paths",
                "type": "SINGLE_CHOICE",
                "answers": [
                    {
                        "answer": "answer 1",
                        "answerid": "UZO_TBT7a0",
                        "route": {
                            "name": "path1",
                            "questions": {
                                "data": [
                                    {
                                        "text": "Single choice inside path",
                                        "type": "SINGLE_CHOICE",
                                        "answers": [
                                            {
                                                "answer": "answer path 1",
                                                "answerid": "Q6PZBUREar0"
                                            },
                                            {
                                                "answer": "answer path 2",
                                                "answerid": "Q6PZBUREar1"
                                            }
                                        ],
                                        "id": "Q6PZBURE",
                                        "question": "main",
                                        "time": 2
                                    }
                                ]
                            },
                            "id": 0
                        }
                    },
                    {
                        "answer": "answer 2",
                        "answerid": "UZO_TBT7a1",
                        "route": {
                            "name": "path2",
                            "questions": {
                                "data": [
                                    {
                                        "text": "Multiple choice with other inside path",
                                        "type": "MULTIPLE_CHOICE",
                                        "answers": [
                                            {
                                                "answer": "answer 1",
                                                "answerid": "OCLIJC90ar0"
                                            },
                                            {
                                                "answer": "answer 2",
                                                "answerid": "OCLIJC90ar1"
                                            }
                                        ],
                                        "id": "OCLIJC90",
                                        "question": "main",
                                        "time": 6,
                                        "otherField": true,
                                        "hasPaths": false
                                    }
                                ]
                            },
                            "id": 1
                        }
                    }
                ],
                "id": "H03RMX4Q",
                "question": "main",
                "time": 6,
                "hasPaths": true,
                "otherField": false
            },
            {
                "text": "HEADLINE",
                "type": "HEADLINE",
                "content": "Headline text - Start other questions",
                "answers": [
                    {
                        "answer": "HEADLINE"
                    }
                ],
                "id": "WZA2BS_2",
                "question": "main",
                "time": 0
            },
            {
                "text": "Rating question",
                "id": "CXMVTXNA",
                "type": "RATING",
                "question": "main",
                "answers": [
                    {
                        "answer": "RATING",
                        "answerid": "CXMVTXNAa0"
                    }
                ],
                "rating": 5,
                "time": 6
            },
            {
                "text": "Short text question",
                "id": "R4KBYFH2",
                "type": "TEXTFIELD",
                "question": "main",
                "answers": [
                    {
                        "answer": "TEXTFIELD",
                        "answerid": "R4KBYFH2a0"
                    }
                ],
                "maxChar": 120,
                "time": 6
            },
            {
                "text": "Long text question",
                "id": "X5D50POL",
                "type": "LONGTEXT",
                "question": "main",
                "answers": [
                    {
                        "answer": "LONGTEXT",
                        "answerid": "X5D50POLar0"
                    }
                ],
                "maxChar": 500,
                "time": 6
            },
            {
                "text": "Yes No question",
                "id": "H6P_ATV6",
                "type": "YES_NO",
                "question": "main",
                "answers": [
                    {
                        "answer": "Yes",
                        "answerid": "H6P_ATV6a0"
                    },
                    {
                        "answer": "No",
                        "answerid": "H6P_ATV6a1"
                    }
                ],
                "time": 6
            },
            {
                "text": "Yes no question with Paths and path only on YES",
                "id": "26V5BV0A",
                "type": "YES_NO",
                "question": "main",
                "answers": [
                    {
                        "answer": "Yes",
                        "answerid": "26V5BV0Aa0",
                        "route": {
                            "name": "Yes",
                            "questions": {
                                "data": [
                                    {
                                        "text": "Single choice optional question inside path",
                                        "id": "YCNJHBS_",
                                        "type": "SINGLE_CHOICE",
                                        "question": "optional",
                                        "answers": [
                                            {
                                                "answer": "answer  1",
                                                "answerid": "YCNJHBS_ar0"
                                            },
                                            {
                                                "answer": "answer 2",
                                                "answerid": "YCNJHBS_ar1"
                                            }
                                        ],
                                        "time": 6
                                    },
                                    {
                                        "text": "Another Yes no question inside path",
                                        "id": "178HQJS9",
                                        "type": "YES_NO",
                                        "question": "main",
                                        "answers": [
                                            {
                                                "answer": "Yes",
                                                "answerid": "178HQJS9ar0"
                                            },
                                            {
                                                "answer": "No",
                                                "answerid": "178HQJS9ar1"
                                            }
                                        ],
                                        "time": 6
                                    }
                                ]
                            },
                            "id": 0
                        }
                    },
                    {
                        "answer": "No",
                        "answerid": "26V5BV0Aa1"
                    }
                ],
                "time": 6,
                "hasPaths": true,
                "otherField": false
            },
            {
                "text": "Ranking question",
                "id": "_8OS3SRN",
                "type": "RANKING",
                "question": "main",
                "answers": [
                    {
                        "answer": "option 1",
                        "answerid": "_8OS3SRNa0"
                    },
                    {
                        "answer": "Option 2",
                        "answerid": "_8OS3SRNa1"
                    },
                    {
                        "answer": "Option 3",
                        "answerid": "_8OS3SRNaYXC5LQAB"
                    }
                ],
                "time": 6
            },
            {
                "text": "Date question",
                "id": "B3APVUNT",
                "type": "DATE",
                "question": "main",
                "answers": [
                    {
                        "answer": "DATE",
                        "answerid": "B3APVUNTa0"
                    }
                ],
                "time": 6
            },
            {
                "text": "Slide bar with 3 steps",
                "id": "ZFTVRQ6Q",
                "type": "SLIDEBAR",
                "question": "main",
                "answers": [
                    {
                        "answer": "Left",
                        "answerid": "ZFTVRQ6Qa0"
                    },
                    {
                        "answer": "Right",
                        "answerid": "ZFTVRQ6Qa1"
                    }
                ],
                "sliderVal": "3",
                "middleValue": 2,
                "time": 6
            },
            {
                "text": "Slide bar with 9 steps. (only allowed 3,5,7,9)",
                "id": "6YN6VFSO",
                "type": "SLIDEBAR",
                "question": "main",
                "answers": [
                    {
                        "answer": "Left",
                        "answerid": "6YN6VFSOa0"
                    },
                    {
                        "answer": "Right",
                        "answerid": "6YN6VFSOa1"
                    }
                ],
                "sliderVal": "9",
                "middleValue": 5,
                "time": 6
            },
            {
                "text": "Expression without categories (upto 20 words)",
                "id": "N71MX8HP",
                "type": "EXPRESSION",
                "question": "main",
                "answers": [
                    {
                        "answer": "EXPRESSION",
                        "answerid": "N71MX8HPa0"
                    }
                ],
                "activeCat": false,
                "words": [
                    {
                        "text": "Calm"
                    },
                    {
                        "text": "Happy"
                    },
                    {
                        "text": "Really happy"
                    },
                    {
                        "text": "Test"
                    },
                    {
                        "text": "Sad"
                    },
                    {
                        "text": "Very sad"
                    }
                ],
                "multiple": false,
                "time": 6
            },
            {
                "text": "Expression with categories (10 expressions per category)",
                "id": "XZCRQO81",
                "type": "EXPRESSION",
                "question": "main",
                "answers": [
                    {
                        "answer": "EXPRESSION",
                        "answerid": "XZCRQO81a0"
                    }
                ],
                "activeCat": true,
                "positive": [
                    {
                        "text": "Calm"
                    },
                    {
                        "text": "Happy"
                    },
                    {
                        "text": "Very happy"
                    }
                ],
                "negative": [
                    {
                        "text": "Sad"
                    },
                    {
                        "text": "Challenging"
                    },
                    {
                        "text": "Very sad"
                    }
                ],
                "multiple": false,
                "time": 6
            },
            {
                "text": "Numeric (General)",
                "id": "ES4V355J",
                "type": "NUMERIC",
                "question": "main",
                "answers": [
                    {
                        "answer": "NUMERIC",
                        "answerid": "ES4V355Ja0"
                    }
                ],
                "allowDecimals": false,
                "hasMinMax": false,
                "time": 6,
                "numberType": "General"
            },
            {
                "text": "Numeric (Number) with allow decimals",
                "id": "0SCPCBAM",
                "type": "NUMERIC",
                "question": "main",
                "answers": [
                    {
                        "answer": "NUMERIC",
                        "answerid": "0SCPCBAMa0"
                    }
                ],
                "allowDecimals": true,
                "hasMinMax": false,
                "time": 6,
                "numberType": "Number"
            },
            {
                "text": "Numeric with Currency",
                "id": "FJT8MNJR",
                "type": "NUMERIC",
                "question": "main",
                "answers": [
                    {
                        "answer": "NUMERIC",
                        "answerid": "FJT8MNJRa0"
                    }
                ],
                "allowDecimals": false,
                "hasMinMax": false,
                "time": 6,
                "numberType": "Currency"
            },
            {
                "text": "Numeric with Percentage (allow decimals)",
                "id": "8XM4QBYZ",
                "type": "NUMERIC",
                "question": "main",
                "answers": [
                    {
                        "answer": "NUMERIC",
                        "answerid": "8XM4QBYZa0"
                    }
                ],
                "allowDecimals": true,
                "hasMinMax": false,
                "time": 6,
                "numberType": "Percentage"
            },
            {
                "text": "Image question",
                "id": "E12IGH4Z",
                "type": "IMAGE",
                "question": "main",
                "answers": [
                    {
                        "answer": "Bike",
                        "answerid": "E12IGH4Za0",
                        "imageUrl": "https://qrveyuserfiles.s3.amazonaws.com/dev/Zl2kkVD/1481224196233.jpg",
                        "imageStoredName": "1481224196233.jpg",
                        "imageName": "bike.jpeg"
                    },
                    {
                        "answer": "Boats",
                        "answerid": "E12IGH4Za1",
                        "imageUrl": "https://qrveyuserfiles.s3.amazonaws.com/dev/Zl2kkVD/1481224201461.jpg",
                        "imageStoredName": "1481224201461.jpg",
                        "imageName": "boats.jpeg"
                    }
                ],
                "time": 6
            },
            {
                "text": "Image with multi selection  (No Labels)",
                "id": "ZLH13F1G",
                "type": "IMAGE",
                "question": "main",
                "answers": [
                    {
                        "answerid": "ZLH13F1Ga0",
                        "imageUrl": "https://cdn.qrvey.com/images/earn-money.png"
                    },
                    {
                        "answerid": "ZLH13F1Ga1",
                        "imageUrl": "https://cdn.qrvey.com/images/join.png"
                    },
                    {
                        "answerid": "ZLH13F1GaCNGYTX7Q",
                        "imageUrl": "https://cdn.qrvey.com/images/share-or-embed.png"
                    }
                ],
                "time": 6,
                "allowMultiple": true
            },
            {
                "text": "Image with Paths",
                "id": "JFO6NLH6",
                "type": "IMAGE",
                "question": "main",
                "answers": [
                    {
                        "answer": "share",
                        "answerid": "JFO6NLH6a0",
                        "imageUrl": "https://cdn.qrvey.com/images/share-or-embed.png",
                        "route": {
                            "name": "path1",
                            "id": 0,
                            "questions": {
                                "data": [
                                    {
                                        "text": "Short text",
                                        "id": "SL2H4NS1",
                                        "type": "TEXTFIELD",
                                        "question": "main",
                                        "answers": [
                                            {
                                                "answer": "TEXTFIELD",
                                                "answerid": "SL2H4NS1ar0"
                                            }
                                        ],
                                        "maxChar": 120,
                                        "time": 6
                                    }
                                ]
                            }
                        }
                    },
                    {
                        "answer": "Planes",
                        "answerid": "JFO6NLH6a1",
                        "imageUrl": "https://qrveyuserfiles.s3.amazonaws.com/dev/Zl2kkVD/1481224402092.jpg",
                        "imageStoredName": "1481224402092.jpg",
                        "imageName": "plane.jpeg",
                        "route": {
                            "name": "Path2",
                            "id": 1,
                            "questions": {
                                "data": [
                                    {
                                        "text": "Numeric with Number and allow decimals",
                                        "id": "NQTI2MQT",
                                        "type": "NUMERIC",
                                        "question": "main",
                                        "answers": [
                                            {
                                                "answer": "NUMERIC",
                                                "answerid": "NQTI2MQTar0"
                                            }
                                        ],
                                        "allowDecimals": true,
                                        "hasMinMax": false,
                                        "time": 6,
                                        "numberType": "Number"
                                    }
                                ]
                            }
                        }
                    }
                ],
                "time": 6,
                "hasPaths": true,
                "otherField": false
            }
        ]
    }
    -->
    ,"totalQuestions": <xsl:value-of select="count(questions/question)"/>
    ,"results_visibility": "<xsl:value-of select="settings/properties/@results_visibility"/>"
    ,"respondent_email": "<xsl:value-of select="settings/properties/@respondent_email"/>"
    ,"emailNotifications": <xsl:value-of select="settings/properties/@emailNotifications"/>
    <xsl:call-template name="elements2JSON">
        <xsl:with-param name="name">thankYouPage</xsl:with-param>
        <xsl:with-param name="elements" select="settings/thankYouPage/*"/>
    </xsl:call-template>
    <xsl:call-template name="elements2JSON">
        <xsl:with-param name="name">duration</xsl:with-param>
        <xsl:with-param name="elements" select="settings/duration/*"/>
    </xsl:call-template>
}    
    </xsl:template>
    
    <xsl:template name="elements2JSON">
        <xsl:param name="name"/>
        <xsl:param name="elements"/>
        <xsl:param name="sep">,</xsl:param>
        <xsl:if test="$elements">
            <xsl:value-of select="$sep"/>"<xsl:value-of select="$name"/>": {
            <xsl:for-each select="$elements">
                <xsl:variable name="value">
                    <xsl:choose>
                        <xsl:when test=".='_AUTOID_'"><xsl:value-of select="generate-id()"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:if test="position() &gt; 1">,</xsl:if>"<xsl:value-of select="local-name()"/>":"<xsl:value-of select="$value"/>"
            </xsl:for-each>
            }    
        </xsl:if>
    </xsl:template>

    <xsl:template name="attributes2JSON">
        <xsl:param name="attributes"/>
        <xsl:if test="$attributes">
            <xsl:for-each select="$attributes">
                <xsl:variable name="value">
                    <xsl:choose>
                        <xsl:when test=".='_AUTOID_'"><xsl:value-of select="generate-id()"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:if test="position() &gt; 1">,</xsl:if>"<xsl:value-of select="local-name()"/>":"<xsl:value-of select="$value"/>"
            </xsl:for-each>
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