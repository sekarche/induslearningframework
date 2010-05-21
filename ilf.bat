@echo off

set ILF_MEMORY=1G
set ILF_CLASSPATH=ilf_0.1.1.jar;lib/mysql/mysql-connector-java-5.0.7-bin.jar;lib/weka-3.5.6/weka.jar;lib/apache/commons-dbcp-1.2.2.jar;lib/apache/commons-pool-20070730.jar;lib/junit/junit-4.5.jar;

set CMD=%1
shift

if "%CMD%"=="integration" shift & set ILF_CLASSPATH=ilf_0.1.1.jar;lib/mysql/mysql-connector-java-5.0.7-bin.jar;lib/weka-3.5.6/weka.jar;lib/apache/commons-dbcp-1.2.2.jar;lib/apache/commons-pool-20070730.jar;lib/junit/junit-4.5.jar;lib/iif/iif_0.1.1.jar;lib/iif/lib/commons-lang-2.2.jar;lib/iif/lib/junit-4.4.jar;lib/iif/lib/log4j-1.2.15.jar;lib/iif/lib/mysql-connector-java-5.0.7-bin.jar;lib/iif/lib/Zql.jar;lib/iif/lib/pellet_jars/aterm-java-1.6.jar;lib/iif/lib/pellet_jars/owlapi-bin.jar;lib/iif/lib/pellet_jars/owlapi-src.jar;lib/iif/lib/pellet_jars/pellet-cli.jar;lib/iif/lib/pellet_jars/pellet-core.jar;lib/iif/lib/pellet_jars/pellet-datatypes.jar;lib/iif/lib/pellet_jars/pellet-dig.jar;lib/iif/lib/pellet_jars/pellet-el.jar;lib/iif/lib/pellet_jars/pellet-explanation.jar;lib/iif/lib/pellet_jars/pellet-jena.jar;lib/iif/lib/pellet_jars/pellet-modularity.jar;lib/iif/lib/pellet_jars/pellet-owlapi.jar;lib/iif/lib/pellet_jars/pellet-pellint.jar;lib/iif/lib/pellet_jars/pellet-query.jar;lib/iif/lib/pellet_jars/pellet-rules.jar;lib/iif/lib/pellet_jars/pellet-test.jar;lib/iif/lib/pellet_jars/relaxngDatatype.jar;lib/iif/lib/pellet_jars/servlet.jar;lib/iif/lib/pellet_jars/xsdlib.jar;lib/iif/lib/matheval/meval.jar; & set CMD=%1


set CLASS=
if "%CMD%"=="decision-tree" set CLASS=airldm2.classifiers.trees.Id3SimpleClassifier
if "%CMD%"=="naive-bayes" set CLASS=airldm2.classifiers.bayes.NaiveBayesClassifier


if not "%CLASS%" == "" goto gotClass

echo IndusLearningFramework Classifiers: 
echo   decision-tree      uses Decision Tree Classifier to run on the Data
echo   naive-bayes       uses Naive Bayes Classifier to run on the Data



goto :eof


:gotClass

set ILF_ARGS=

:getArg

if "%1"=="" goto run
set ILF_ARGS=%ILF_ARGS% %1
shift
goto getArg


:run 

java -Xmx%ILF_MEMORY% -classpath %ILF_CLASSPATH% %CLASS% %ILF_ARGS%

:eof
