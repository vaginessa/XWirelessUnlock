#!/bin/sh
NUM_JAVA_FILES=`find src -name *.java | wc -l`
NUM_JAVA_LINES=`find src -name *.java -exec cat {} \; | wc -l`

NUM_XML_FILES=`find src -name *.xml | wc -l`
NUM_XML_LINES=`find src -name *.xml -exec cat {} \; | wc -l`

printf "$(basename `pwd`):\n$NUM_JAVA_LINES lines in $NUM_JAVA_FILES java files.\n$NUM_XML_LINES lines in $NUM_XML_FILES xml files.\n"
