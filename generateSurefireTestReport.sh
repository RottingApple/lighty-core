#!/bin/bash
for surefireFile in $(find -type f -name "*.txt" | grep -i surefire-reports); do
	cat $surefireFile >> ./docs/SurefireTestReport.txt
done