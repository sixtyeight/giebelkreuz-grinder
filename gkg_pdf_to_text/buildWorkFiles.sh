#!/bin/bash

echo
echo "Giebelkreuz Grinder(tm) is starting to convert your pdf files now"
echo

echo "setup work dir"
echo
mkdir work 2> /dev/null
rm ./work/* 2> /dev/null

find . -name "*.pdf" -print0 | while read -d $'\0' INPUTFILE 
do
	SANITIZEDFILE=`echo "${INPUTFILE}" | sed "s/ /_/g" | cut -c 3-` 
	echo "copying '${INPUTFILE}' to '${SANITIZEDFILE}'"
	cp "${INPUTFILE}" "./work/${SANITIZEDFILE}"

	OUTPUTFILE="${SANITIZEDFILE}.txt"
	echo "converting '${INPUTFILE}' to '${OUTPUTFILE}'"

	java -jar pdfbox-app-1.8.11.jar ExtractText "${INPUTFILE}" "./work/${OUTPUTFILE}"

	COMPACTFILE=`echo "${OUTPUTFILE}.compacted.txt"` 
	echo "compacting '${OUTPUTFILE}' to '${COMPACTFILE}'"
	./compact.sh "./work/${OUTPUTFILE}" > "./work/${COMPACTFILE}"
	
	echo
done

echo "all files processed"
echo

echo "merging all raw converted files into  _converted_complete.txt"
echo
cat ./work/*.pdf.txt > ./work/_converted_complete.txt

echo "merging all compacted files into  _compacted_complete.txt"
echo
cat ./work/*.compacted.txt > ./work/_compacted_complete.txt

echo "done, all files in subdirectory work available"
echo
