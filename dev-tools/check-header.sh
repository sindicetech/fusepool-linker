#! /bin/bash

###
### Check file headers
###

# Get the current directory
DIR=$(cd $(dirname "$0"); pwd)


# Get the files: *.java" but exclude the "package-info.java" files
FILES=$(find $DIR/../*/src/ -type f \( -name "*.java" -o -name "*.flex" ! -name "package-info.java" \))

# Replace the Copyright headers with the correct one
COPYRIGHT=$(cat $DIR/copyright | sed 's/\//\\\//g')

for file in $FILES; do
    PACKAGE_LINE_NUMBER=$(cat $file | grep -n -m 1 '^package .*;' | cut -f1 -d:)
    cat $DIR/copyright > $file.tmp
    if [ "$PACKAGE_LINE_NUMBER" -gt 1 ]; then
        ONE_BEFORE=$((PACKAGE_LINE_NUMBER - 1))
        # remove everything between beginning of the file and package specification and append the rest to $file.tmp
        cat $file | sed "1,${ONE_BEFORE}d" >> $file.tmp
    else
        # there's nothing between the first line and package specification so append the whole file
        cat $file >> $file.tmp
    fi
    mv $file.tmp $file
done

# Remove comments with either @project, @email and @author javadocs
for file in $FILES; do
  perl -0777 -i -pe 's/^\/\*\*(?:(?!\*\/$).)*@(project|email)(?:(?!\*\/$).)*\*\/$//ism' $file
  sed -i '/@author/d' $file
done
