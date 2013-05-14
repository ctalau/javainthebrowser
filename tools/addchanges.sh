#!/bin/bash

date=$(date +"%F")
echo "/**                                                       " >> /tmp/header
echo " * This file was changed in order to make it compilable   " >> /tmp/header
echo " * with GWT and to integrate it in the JavaInTheBrowser   " >> /tmp/header
echo " * project (http://javainthebrowser.appspot.com).         " >> /tmp/header
echo " *                                                        " >> /tmp/header
echo " * Date: $date                                            " >> /tmp/header
echo " */                                                       " >> /tmp/header

cat /tmp/header
pushd ..
changes=$(git diff --name-only javac-openjdk -- src/javac/)
for file in $changes
  do
    cat /tmp/header > /tmp/file
    tail -n +8 $file >> /tmp/file
    cat /tmp/file > $file
  done

popd

rm /tmp/header
rm /tmp/file
