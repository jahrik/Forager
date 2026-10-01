#!/bin/bash
# usage: revert.sh NAME "TEST FILTER" ; edit step supplied by function apply_revert in env file $3
set -u
W=/home/zynergy-labs/Zynergy/forager-wt/photo-decode-dispatcher
NAME=$1; FILTER=$2; EDIT=$3
cd $W
df -m / | tail -1
mkdir -p /tmp/pdd/save-$NAME; 
# save copies of every file the edit touches
for f in $(cat /tmp/pdd/$EDIT.files); do mkdir -p /tmp/pdd/save-$NAME/$(dirname $f); cp -p $f /tmp/pdd/save-$NAME/$f 2>/dev/null || echo MISSING > /tmp/pdd/save-$NAME/$f.absent; done
bash /tmp/pdd/$EDIT.sh
rm -f app/build/test-results/testDebugUnitTest/*.xml
timeout 580 ./gradlew --no-daemon :app:testDebugUnitTest $FILTER > /tmp/pdd/rev-$NAME.log 2>&1; echo "exit $?" >> /tmp/pdd/rev-$NAME.log
echo "e: lines: $(grep -c '^e: ' /tmp/pdd/rev-$NAME.log)"
mkdir -p /tmp/pdd/xml-$NAME; cp app/build/test-results/testDebugUnitTest/*.xml /tmp/pdd/xml-$NAME/ 2>/dev/null
# restore from saved copies, never from git
for f in $(cat /tmp/pdd/$EDIT.files); do if [ -e /tmp/pdd/save-$NAME/$f.absent ]; then rm -f $f; else cp -p /tmp/pdd/save-$NAME/$f $f; fi; done
git status -s; echo "diff vs HEAD (should be empty): $(git diff --stat | wc -l)"
