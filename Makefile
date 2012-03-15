.PHONY: clean plain

WordCountJ: src/WordCountJ.java
	javac src/WordCountJ.java -d bin/

# to run with two threads and results to out2.txt:
# make plain N=2 OUT=out2.txt 
plain: WordCountJ
	java -cp bin/ WordCountJ $(N) output/$(OUT) input/WC_input/*

clean:
	rm -f bin/*
	rm -f output/*
