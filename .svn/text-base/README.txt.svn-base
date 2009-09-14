Installation:
By default uses a local Oracle XE database

1) create 'test_user' in local oracle database 
	(or edit src/main/webapp/WEB-INF/jetty-env.xml to point to a different database) 

2) run create.sql script.

3) Install oracle DB driver into local maven repository
If installed on windows driver will be here: 
C:\oraclexe\app\oracle\product\10.2.0\server\jdbc\lib\ojdbc14.jar
Run maven install:
>mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc -Dversion=10.2.0.1.0 -Dpackaging=jar -Dfile=C:\oraclexe\app\oracle\product\10.2.0\server\jdbc\lib\ojdbc14.jar

4) Start jetty webservice on port 9090
>mvn jetty:run

5) Open UserDetails-soapui-project.xml with SoapUI.

