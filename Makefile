HADOOP_HOME=/opt/hadoop
CLASSPATH=${HADOOP_HOME}/hadoop-core-1.0.1.jar:${HADOOP_HOME}/lib/commons-cli-1.2.jar
INPUT=input/WC_input/*

.PHONY: clean plain

WordCountJ: src/WordCountJ.java
	javac src/WordCountJ.java -d bin/

WordCountMR: src/WordCountMR.java	
	javac -classpath ${CLASSPATH} -d bin src/WordCountMR.java
	jar -cvf bin/WordCountMR.jar -C bin/ .

# to run with two threads and results to out2.txt:
# make plain N=2 OUT=out2.txt 
plain: WordCountJ
	java -cp bin/ WordCountJ $(N) output/$(OUT) $(INPUT)

clean:
	rm -f bin/*
	rm -f output/*
