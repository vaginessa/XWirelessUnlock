#!/bin/sh
NUM_JAVA_FILES=`find . -name *.java | wc -l`
NUM_JAVA_LINES=`find . -name *.java -exec cat {} \; | wc -l`

NUM_XML_FILES=`find . -name *.xml | wc -l`
NUM_XML_LINES=`find . -name *.xml -exec cat {} \; | wc -l`

printf "$(basename `pwd`):\n$NUM_JAVA_LINES lines in $NUM_JAVA_FILES java files.\n$NUM_XML_LINES lines in $NUM_XML_FILES xml files.\n"

