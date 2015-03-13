# Introduction #

Generator is a tool to parse automatically .pcml into POJO Classes according with [syntax](http://publib.boulder.ibm.com/infocenter/iseries/v5r4/index.jsp?topic=%2Frzahh%2Fpcmlpgtg.htm). It also supports WebService creation using pcml metadata.


# Installation #

Download lastest version of [generator](http://code.google.com/p/hiberpcml/downloads/list). This package includes all required dependencies to run.

you can run these commands on linux console:

<pre>
$ wget http://hiberpcml.googlecode.com/file/generator-1.0-SNAPSHOT.zip<br>
<br>
$ unzip generator-1.0-SNAPSHOT.zip<br>
Archive:  generator-1.0-SNAPSHOT.zip<br>
inflating: lib/codemodel-2.4.jar<br>
inflating: lib/commons-cli-1.2.jar<br>
inflating: lib/hiberpcml-1.0.jar<br>
inflating: lib/jt400-6.7.jar<br>
inflating: lib/junit-3.8.1.jar<br>
inflating: generator-1.0.jar<br>
</pre>

# Usage #

without parameters you can see command help:
<pre>
$ java -jar generator-1.0.jar<br>
usage: generator [options] FILE|DIRECTORY.<br>
-p,--package <package>                  Java Package of the generated<br>
classes<br>
-t,--target <target>                    target of the generated classes<br>
-w,--webservice <serviceName service>   build webservice classes<br>
user@machine:~/test$<br>
<br>
</pre>

  * `-p, --package` option defines java package where all generated classes will be part of.
  * `-t, --target` option sets the path where all generated files will be saved.
  * `-w, --webservice`, optional parameter to create additional classes to publish pcml(s) as Web Service(s). it receives 2 params:
    * `serviceName`: see [serviceName()](http://download.oracle.com/javaee/5/api/javax/jws/WebService.html#serviceName%28%29)
    * `name`: see [name()](http://download.oracle.com/javaee/5/api/javax/jws/WebService.html#name%28%29)

`FILE|DIRECTORY`: if `param` is a file, generator will parse it, if is a directory, generator will parse all .pcml files and parse them. It won't do a recursive search.

## Custom variable names ##

you can set your own variable/method names instead of using default "cryptic" names. edit pcml file by adding `label` attribute in each xml element, as shown below:

```
<pcml version="4.0">
   <!-- RPG program: DEMO01-->
   <!-- created: 2011-06-04-11.41.58 -->
   <!-- source: MYLIBSRC/MYRPGLESRC(DEMO01) -->
   <!-- 3900 -->
   <program name="DEMO01" path="/QSYS.LIB/MYLIBEXE.LIB/DEMO01.PGM" label="MyDemoImpl">
      <data name="FRSTNO" type="packed" length="9" precision="0" usage="input" label="firstNumber"/>
      <data name="SCNDNO" type="char" length="1200" usage="input" label="secondNumber"/>
      <data name="RESULT" type="char" length="20" usage="output" label="result"/>
   </program>
</pcml>
```

you can check full list of AdditionalAttributes

## See Also ##
  * GettingStarted
  * https://www.ibm.com/developerworks/mydeveloperworks/blogs/johnarevalo/tags/hiberpcml?lang=en
  * AdditionalAttributes