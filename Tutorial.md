Hiberpcml is a small framework based on annotations to invoke RPG Programs, inspired by JPA model, you can check the project in google code.

I'm going to explain its use from scratch.

Installation

If you use maven, add this dependency in your maven project:


&lt;project&gt;


> ...
> 

&lt;dependencies&gt;


> > 

&lt;dependency&gt;


> > > 

&lt;groupId&gt;

com.googlecode.hiberpcml

&lt;/groupId&gt;


> > > 

&lt;artifactId&gt;

hiberpcml

&lt;/artifactId&gt;


> > > 

&lt;version&gt;

1.0

&lt;/version&gt;


> > > 

&lt;type&gt;

jar

&lt;/type&gt;



> > 

&lt;/dependency&gt;



> 

&lt;/dependencies&gt;


> ...


&lt;/project&gt;


Alternatively you can download binary file from here, also you need jt400.jar. Add these two files to your classpath. After this, you are ready to use the API.

Usage

we'll asume we want to invoke this pcml file [DEMO01.PCML]:


&lt;pcml version="4.0"&gt;


> <!-- RPG program: DEMO01-->
> <!-- created: 2011-06-04-11.41.58 -->
> <!-- source: MYLIBSRC/MYRPGLESRC(DEMO01) -->
> <!-- 3900 -->
> 

&lt;program name="DEMO01" path="/QSYS.LIB/MYLIBEXE.LIB/DEMO01.PGM"&gt;


> > 

&lt;data name="FRSTNO" type="packed" length="9" precision="0" usage="input"/&gt;


> > 

&lt;data name="SCNDNO" type="packed" length="9" precision="0" usage="input"/&gt;


> > 

&lt;data name="RESULT" type="packed" length="9" precision="0" usage="output"/&gt;



> 

&lt;/program&gt;




&lt;/pcml&gt;


Every pcml program should be parsed into a POJO Class. every element within program tag should be represented by a Field in the class, based on this table.

This process can be done manually, however, there is a Generator tool to make it automatically. You can get the tool via Downloads. Detailed explanation of its use can be get in the wiki.

Edit the .pcml file by adding label tag to every element in order to customize the field names in the Class:
...


&lt;program name="DEMO01" path="/QSYS.LIB/MYLIBEXE.LIB/DEMO01.PGM" label="MyDemoImpl"&gt;


> 

&lt;data name="FRSTNO" type="packed" length="9" precision="0" usage="input" label="firstNumber"/&gt;


> 

&lt;data name="SCNDNO" type="packed" length="9" precision="0" usage="input" label="secondNumber"/&gt;


> 

&lt;data name="RESULT" type="packed" length="9" precision="0" usage="output" label="result"/&gt;




&lt;/program&gt;


...
Label assigned to program tag defines the name for the class. Asuming you have your src folder in /home/hiberpcml-test/src/ , execute below command to generate the classes:

java -jar generator-1.0.jar -p com.pcml.impl -t /home/hiberpcml-test/src DEMO01.PCML
com\pcml\impl\MyDemoImpl.java
META-INF\DEMO01.PCML

With this command we are creating the classes for DEMO01.PCML in the package com.pcml.impl, into /home/hiberpcml-test/src directory.
// File /home/hiberpcml-test/src/com/pcml/impl/MyDemoImpl.java
package com.pcml.impl;

import java.math.BigDecimal;
import com.googlecode.hiberpcml.Data;
import com.googlecode.hiberpcml.Program;
import com.googlecode.hiberpcml.UsageType;

@Program(programName = "DEMO01", documentName = "META-INF.DEMO01")
public class MyDemoImpl {

> @Data(pcmlName = "FRSTNO", usage = UsageType.INPUT)
> private BigDecimal firstNumber = new BigDecimal("0");
> @Data(pcmlName = "SCNDNO", usage = UsageType.INPUT)
> private BigDecimal secondNumber = new BigDecimal("0");
> @Data(pcmlName = "RESULT", usage = UsageType.OUTPUT)
> private BigDecimal result = new BigDecimal("0");

> //....
> //Getter And Setter
> //...
}
Once we've created this classes, let's invoke it with next Main.java file:
public static void main(String arg) {
> > DemoImpl pcml = new DemoImpl();
> > pcml.setFirstNumber(new BigDecimal(50));
> > pcml.setSecondNumber(new BigDecimal(150));
> > SessionManager manager = new SessionManager();
> > Properties properties = new Properties();
> > properties.setProperty("as400.pcml.host","192.168.0.1");
> > properties.setProperty("as400.pcml.user","MY\_USER");
> > properties.setProperty("as400.pcml.password","MY\_PASSWORD");
> > manager.setConfiguration(properties);
> > manager.invoke(pcml);
> > System.out.println("Result: " + pcml.getResult());

> }
Execute your Main File to see output.

RPG to WebService

if you want to publish your RPG Programs as Web Services, just add webMethodName attribute for pcml tag, and add -w option in the command line:


&lt;pcml version="4.0" webMethodName="webMethod"&gt;


> <!-- RPG program: DEMO01-->
> <!-- created: 2011-06-04-11.41.58 -->
> <!-- source: MYLIBSRC/MYRPGLESRC(DEMO01) -->
> <!-- 3900 -->


&lt;program name="DEMO01" path="/QSYS.LIB/MYLIBEXE.LIB/DEMO01.PGM" label="MyDemoImpl"&gt;


> > 

&lt;data name="FRSTNO" type="packed" length="9" precision="0" usage="input" label="firstNumber"/&gt;


> > 

&lt;data name="SCNDNO" type="packed" length="9" precision="0" usage="input" label="secondNumber"/&gt;


> > 

&lt;data name="RESULT" type="packed" length="9" precision="0" usage="output" label="result"/&gt;




&lt;/program&gt;




&lt;/pcml&gt;


Then execute:

java -jar generator-1.0.jar -w myServiceName myName -p com.pcml.impl -t /home/hiberpcml-test/src DEMO01.PCML
com\pcml\impl\MyDemoImpl.java
com\pcml\impl\MyName.java
META-INF\DEMO01.PCML

There are some considerations for this command:
-w option receives two params: serviceName() and name().
you must initialize SessionManager object with the proper params, as above example.
every element with usage=input will be received as param in the web service.
usage=output element will be the return of the method. If exists more than one element marked as output, a class with suffix Response will be generated (e.g. MyDemoImplResponse).
If you send a directory as param instead of a single file(DEMO01.PCML), Generator will parse any .pcml file and will add one web method per each pcml.
Type of every field depends of type attribute in the pcml data element.


> Now you can add generated classes to your classpath and start to call and/or publish your RPG programs.