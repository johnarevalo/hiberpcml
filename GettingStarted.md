# Getting Started #

RPG program can be invoked from Java using a Program Call Markup Language (PCML) source file. this file follows the [standard syntax](http://publib.boulder.ibm.com/infocenter/iseries/v5r4/index.jsp?topic=%2Frzahh%2Fpcmlpgtg.htm).

This file is the input of the hiberpcml library, Briefly these are the steps to invoke the Pcml.

  * Parse .pcml file into java objects.
  * Create SessionManager.
  * Fill requested parameters.
  * Call invoke() method.

## Installation ##

For maven support, add this dependency tag:

```
<project>
    <dependencies>
        <dependency>
            <groupId>com.googlecode.hiberpcml</groupId>
            <artifactId>hiberpcml</artifactId>
            <version>1.0</version>
            <type>jar</type>
        </dependency>
    </dependencies>
</project>
```

or, you can just [download](http://code.google.com/p/hiberpcml/downloads/list) the jar library and add it to your classpath, additional required library: [jt400.jar](http://search.maven.org/remotecontent?filepath=net/sf/jt400/jt400/6.7/jt400-6.7.jar)

## Parsing the file ##

pcml syntax allows 3 basic tags, these tags are mapped to annotations:

| **pcml `<tag>`** | **Hiberpcml @annotation** |
|:-----------------|:--------------------------|
| struct | @Struct |
| data   | @Data or @Array |
| program | @Program |

lets see this .pcml file (DEMO01.PCML) :
```
<pcml version="4.0">
   <!-- RPG program: DEMO01-->
   <!-- created: 2011-06-04-11.41.58 -->
   <!-- source: MYLIBSRC/MYRPGLESRC(DEMO01) -->
   <!-- 3900 -->
   <program name="DEMO01" path="/QSYS.LIB/MYLIBEXE.LIB/DEMO01.PGM">
      <data name="FRSTNO" type="packed" length="9" precision="0" usage="input"/>
      <data name="SCNDNO" type="packed" length="9" precision="0" usage="input"/>
      <data name="RESULT" type="packed" length="9" precision="0" usage="output"/>
   </program>
</pcml>
```
now let's parse the file based on DataType table:

```java

@Program(programName = "DEMO01", documentName = "testing.data.DEMO01")
public class DemoImpl {

@Data(pcmlName = "FRSTNO", usage = UsageType.INPUT)
private BigDecimal firstNumber = new BigDecimal("0");
@Data(pcmlName = "SCNDNO", usage = UsageType.INPUT)
private BigDecimal secondNumber = new BigDecimal("0");
@Data(pcmlName = "RESULT", usage = UsageType.OUTPUT)
private BigDecimal result = new BigDecimal("0");

// ...
// Getters And Setters for each field
// ...

}
```

you could use [Generator](UsingGenerator.md) to parse your .pcml files automatically

## Creating Manager Instance ##

The class Manager takes both files, .pcml and java class, to invoke pcml:

```java

public static void main(String arg[]) {
DemoImpl pcml = new DemoImpl();
pcml.setFirstNumber(new BigDecimal(50));
pcml.setSecondNumber(new BigDecimal(150));
SessionManager manager = new SessionManager();
Properties properties = new Properties();
properties.setProperty("as400.pcml.host","192.168.0.1");
properties.setProperty("as400.pcml.user","MY_USER");
properties.setProperty("as400.pcml.password","MY_PASSWORD");
manager.setConfiguration(properties);
manager.invoke(pcml);
System.out.println("Result: " + pcml.getResult());
}
```