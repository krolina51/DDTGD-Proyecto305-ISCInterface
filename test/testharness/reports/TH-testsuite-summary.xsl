<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">

<html>

  <head>
  
    <title>Testharness results</title>

	 <link rel="StyleSheet" href="TH-report.css"/>
	 
  </head>
  
  <body>

    <h1>Testharness results</h1>

	 <hr size="1" />

    <h2>Summary</h2>

    <table class="details" border="0" cellpadding="5" cellspacing="2" width="40%">
      <tr>
        <th>Tests</th>
		  <xsl:if test="testsuites/@failures &gt; 0">
           <th class="Failure">Fail</th>
		  </xsl:if>	  
		  <xsl:if test="testsuites/@failures = 0">
           <th>Fail</th>
		  </xsl:if>	
		  <xsl:if test="testsuites/@errors &gt; 0">
           <th class="Error">Error</th>
		  </xsl:if>	  
		  <xsl:if test="testsuites/@errors = 0">
           <th>Error</th>
		  </xsl:if>	  
        <th>Success</th>
        <th>Time(s)</th>
      </tr>
      <tr>
  	     <td><xsl:value-of select="testsuites/@tests"/></td>
		  <xsl:if test="testsuites/@failures &gt; 0">
		    <td class="Failure"><xsl:value-of select="testsuites/@failures"/></td>
		  </xsl:if>	  
		  <xsl:if test="testsuites/@failures = 0">
		    <td><xsl:value-of select="testsuites/@failures"/></td>
		  </xsl:if>	  
		  <xsl:if test="testsuites/@errors &gt; 0">
  	       <td class="Error"><xsl:value-of select="testsuites/@errors"/></td>
		  </xsl:if>	  
		  <xsl:if test="testsuites/@errors = 0">
  	       <td><xsl:value-of select="testsuites/@errors"/></td>
		  </xsl:if>	  
  	     <td><xsl:value-of select="testsuites/@success"/></td>
  	     <td><xsl:value-of select="testsuites/@time"/></td>
      </tr>
    </table>

	 <hr size="1" />

    <h2>Suites</h2>

    <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
      <tr>
        <th>Name</th>
        <th>Tests</th>
		  <xsl:if test="testsuites/@failures &gt; 0">
           <th class="Failure">Fail</th>
		  </xsl:if>	  
		  <xsl:if test="testsuites/@failures = 0">
           <th>Fail</th>
		  </xsl:if>	
		  <xsl:if test="testsuites/@errors &gt; 0">
          <th class="Error">Error</th>
		  </xsl:if>	 
		  <xsl:if test="testsuites/@errors = 0">
          <th>Error</th>
		  </xsl:if>	 
        <th>Success</th>
        <th>Time(s)</th>
      </tr>
      <xsl:for-each select="testsuites/testsuite">
      <tr>
  	     <td>
			 <a>
			   <xsl:attribute name="href">TH-<xsl:value-of select="@name"/>.xml</xsl:attribute>
			   <xsl:choose>
				  <xsl:when test="@failures &gt; 0">
			       <font class="Failure"><xsl:value-of select="@name"/></font>
				  </xsl:when>
				  <xsl:otherwise>
		          <xsl:if test="@errors &gt; 0">
			          <font class="Error"><xsl:value-of select="@name"/></font>
					 </xsl:if>
		          <xsl:if test="@errors = 0">
			          <xsl:value-of select="@name"/>
					 </xsl:if>
				  </xsl:otherwise>
				</xsl:choose>
		    </a>		
		  </td>
  	     <td><xsl:value-of select="@tests"/></td>
		  <xsl:if test="@failures &gt; 0">
  	       <td class="Failure"><xsl:value-of select="@failures"/></td>
		  </xsl:if>	
		  <xsl:if test="@failures = 0">
  	       <td><xsl:value-of select="@failures"/></td>
		  </xsl:if>	
		  <xsl:if test="@errors &gt; 0">
   	     <td class="Error"><xsl:value-of select="@errors"/></td>
		  </xsl:if>	  
		  <xsl:if test="@errors = 0">
   	     <td><xsl:value-of select="@errors"/></td>
		  </xsl:if>	  
  	     <td><xsl:value-of select="@success"/></td>
  	     <td><xsl:value-of select="@time"/></td>
      </tr>
      </xsl:for-each>
    </table>
 
 </body>

</html>

</xsl:template>

</xsl:stylesheet>

