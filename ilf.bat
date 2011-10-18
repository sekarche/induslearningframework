@echo off

set ILF_MEMORY=1G
set ILF_CLASSPATH=ilf_0.1.2.jar;lib/mysql/mysql-connector-java-5.0.7-bin.jar;lib/weka-3.5.6/weka.jar;lib/apache/commons-dbcp-1.2.2.jar;lib/apache/commons-pool-20070730.jar;lib/junit/junit-4.5.jar;lib/apache/httpclient/commons-httpclient-3.1.jar;lib/apache/httpclient/commons-logging-1.1.1.jar;lib/apache/httpclient/commons-codec-1.3.jar;lib/sesame/openrdf-sesame-2.3.2-onejar.jar;lib/virtuoso/virt_sesame2.jar;lib/jena-2.6.4/slf4j-api-1.5.8.jar;lib/jena-2.6.4/slf4j-log4j12-1.5.8.jar;lib/jena-2.6.4/log4j-1.2.13.jar

set CMD=%1
shift

if "%CMD%"=="integration" shift & set ILF_CLASSPATH=ilf_0.1.2.jar;lib/mysql/mysql-connector-java-5.0.7-bin.jar;lib/weka-3.5.6/weka.jar;lib/apache/commons-dbcp-1.2.2.jar;lib/apache/commons-pool-20070730.jar;lib/junit/junit-4.5.jar;lib/iif/iif_0.1.1.jar;lib/iif/lib/commons-lang-2.2.jar;lib/iif/lib/junit-4.4.jar;lib/iif/lib/log4j-1.2.15.jar;lib/iif/lib/mysql-connector-java-5.0.7-bin.jar;lib/iif/lib/Zql.jar;lib/iif/lib/pellet_jars/aterm-java-1.6.jar;lib/iif/lib/pellet_jars/owlapi-bin.jar;lib/iif/lib/pellet_jars/owlapi-src.jar;lib/iif/lib/pellet_jars/pellet-cli.jar;lib/iif/lib/pellet_jars/pellet-core.jar;lib/iif/lib/pellet_jars/pellet-datatypes.jar;lib/iif/lib/pellet_jars/pellet-dig.jar;lib/iif/lib/pellet_jars/pellet-el.jar;lib/iif/lib/pellet_jars/pellet-explanation.jar;lib/iif/lib/pellet_jars/pellet-jena.jar;lib/iif/lib/pellet_jars/pellet-modularity.jar;lib/iif/lib/pellet_jars/pellet-owlapi.jar;lib/iif/lib/pellet_jars/pellet-pellint.jar;lib/iif/lib/pellet_jars/pellet-query.jar;lib/iif/lib/pellet_jars/pellet-rules.jar;lib/iif/lib/pellet_jars/pellet-test.jar;lib/iif/lib/pellet_jars/relaxngDatatype.jar;lib/iif/lib/pellet_jars/servlet.jar;lib/iif/lib/pellet_jars/xsdlib.jar;lib/iif/lib/matheval/meval.jar; & set CMD=%1


set CLASS=
if "%CMD%"=="decision-tree" set CLASS=airldm2.classifiers.trees.Id3SimpleClassifier
if "%CMD%"=="naive-bayes" set CLASS=airldm2.classifiers.bayes.NaiveBayesClassifier
if "%CMD%"=="rbc" set CLASS=airldm2.classifiers.rl.RelationalBayesianClassifier
if "%CMD%"=="remote-rbc" set CLASS=airldm2.classifiers.rl.RemoteRBCClassifier


if not "%CLASS%" == "" goto gotClass

echo IndusLearningFramework Classifiers: 
echo   decision-tree    evaluates Decision Tree Classifier on propositional data
echo   naive-bayes      evaluates Naive Bayes Classifier on propositional data
echo   rbc              evaluates Relational Bayesian Classifier on RDF data
echo   remote-rbc       uses Relational Bayesian Classifier to learn and classify remote RDF data

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
