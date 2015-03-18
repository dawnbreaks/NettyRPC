call mvn dependency:copy-dependencies
call mvn dependency:sources
call mvn dependency:resolve -Dclassifier=javadoc

call mvn eclipse:eclipse
REM call mvn compile
pause