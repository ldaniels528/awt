#!/usr/bin/env bash
#
# This script builds and packages an application distributable.
#

rm -rf awt.js
mkdir awt.js
cp ./target/scala-2.11/awt-nodejs-opt.* ./awt.js/
cp -r bower.json package.json server.js public notes.txt ./awt.js/
zip awt.zip -r awt.js
rm -rf awt.js
