<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">

<html>

  <head>
  
    <title><xsl:value-of select="testsuite/@name"/></title>

	 <link rel="StyleSheet" href="TH-report.css"/>

  </head>
  
  <body>


    <h1><xsl:value-of select="testsuite/@name"/></h1>

	 <hr size="1" />

    <h2>Summary</h2>

    <table class="details" border="0" cellpadding="5" cellspacing="2" width="40%">
      <tr>
        <th>Tests</th>
		  <xsl:if test="testsuite/@failures &gt; 0">
           <th class="Failure">Fail</th>
		  </xsl:if>	  
		  <xsl:if test="testsuite/@failures = 0">
           <th>Fail</th>
		  </xsl:if>	
		  <xsl:if test="testsuite/@errors &gt; 0">
           <th class="Error">Error</th>
		  </xsl:if>	  
		  <xsl:if test="testsuite/@errors = 0">
           <th>Error</th>
		  </xsl:if>	  
        <th>Success</th>
        <th>Time(s)</th>
      </tr>
      <tr>
  	     <td><xsl:value-of select="testsuite/@tests"/></td>
  		  <xsl:if test="testsuite/@failures &gt; 0">
		    <td class="Failure"><xsl:value-of select="testsuite/@failures"/></td>
		  </xsl:if>	  
		  <xsl:if test="testsuite/@failures = 0">
		    <td><xsl:value-of select="testsuite/@failures"/></td>
		  </xsl:if>	  
		  <xsl:if test="testsuite/@errors &gt; 0">
  	       <td class="Error"><xsl:value-of select="testsuite/@errors"/></td>
		  </xsl:if>	  
		  <xsl:if test="testsuite/@errors = 0">
  	       <td><xsl:value-of select="testsuite/@errors"/></td>
		  </xsl:if>	  
  	     <td><xsl:value-of select="testsuite/@success"/></td>
  	     <td><xsl:value-of select="testsuite/@time"/></td>
      </tr>
    </table>
	
	 <p></p>
	 <p></p>
	 <a href="TH-testsuites.xml">Back to testharness results</a>
	 <p></p>
	 <p></p>
	 
	 <hr size="1" />

    <h2>Testcases</h2>

    <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
      <tr>
        <th>Name</th>
        <th>Result</th>
        <th>Time(s)</th>
        <th>SLX</th>
        <th>Description</th>
        <th>Setup</th>
        <th>Pass Criteria</th>
      </tr>
      <xsl:for-each select="testsuite/testcase">
		  <xsl:if test="count(testdoc) &gt; 0">
          <xsl:for-each select="testdoc">
            <tr>
		      <xsl:if test="position() = 1">
	           <td><xsl:value-of select="../@name"/></td>
		        <xsl:if test="../@result = 'success'">
    	          <td>success</td>
       	     </xsl:if>	  
	 	        <xsl:if test="../@result = 'fail'">
    	          <td class="Failure">fail</td>
		        </xsl:if>	  
		        <xsl:if test="../@result = 'error'">
    	          <td class="Error">error</td>
		        </xsl:if>
	           <td><xsl:value-of select="../@time"/></td>
		      </xsl:if>
		      <xsl:if test="position() &gt; 1">
		        <td/><td/><td/>
		      </xsl:if>
  	           <td><xsl:value-of select="slx"/></td>
              <td><xsl:value-of select="description"/></td>
    	        <td><xsl:value-of select="setup"/></td>
  	           <td><xsl:value-of select="pass"/></td>
		      </tr>
          </xsl:for-each>
		  </xsl:if>
		  <xsl:if test="count(testdoc) = 0">
		    <tr>
	         <td><xsl:value-of select="@name"/></td>
		      <xsl:if test="@result = 'success'">
    	        <td>success</td>
            </xsl:if>	  
	 	      <xsl:if test="@result = 'fail'">
    	        <td class="Failure">fail</td>
		      </xsl:if>	  
		      <xsl:if test="@result = 'error'">
    	        <td class="Error">error</td>
		      </xsl:if>
	         <td><xsl:value-of select="../@time"/></td>
		      <td/><td/><td/><td/>
			 </tr>
		  </xsl:if>
      </xsl:for-each>
    </table>
 
	 <p></p>
	 <p></p>
	 <a href="TH-testsuites.xml">Back to testharness results</a>
	 <p></p>
	 <p></p>

 </body>

</html>

</xsl:template>

</xsl:stylesheet>


