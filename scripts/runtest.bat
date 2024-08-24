@echo off
setlocal

REM Set classpath to include JUnit, Hamcrest, and JaCoCo libraries
set CLASSPATH=.;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar;lib\jacocoagent.jar

REM Compile the source files and place the compiled classes in the bin directory
javac -cp %CLASSPATH% -d bin src\main\java\SnakeGameLogic.java src\test\java\SnakeGameLogicTest.java

REM Run the tests with JaCoCo agent
java -javaagent:lib\jacocoagent.jar=destfile=jacoco.exec -cp %CLASSPATH%;bin org.junit.runner.JUnitCore SnakeGameLogicTest

REM Generate the code coverage report
java -jar lib\jacococli.jar report jacoco.exec --classfiles bin --sourcefiles src\main\java --html report --name SnakeGameProject

endlocal
