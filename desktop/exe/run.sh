#!/bin/bash
#
# Gaia Sandbox start script.
#
# invokation: run.sh
#
DIR=`dirname "$0"`
LIB_PATH="$DIR/gaiasandbox.jar"

#
# Choose java - use JAVA_HOME, if set.
#
if [ "$JAVA_HOME" != "" ]; then
	JAVA="$JAVA_HOME/bin/java"
else
	JAVA="java"
fi

#
# Settings.
#
# Properties file
OPTS="$OPTS -Dproperties.file=$DIR/conf/global.properties"

# Memory
OPTS="$OPTS -Xmx512m"
# GC info
OPTS="$OPTS -XX:+UseParallelGC"
# GC debug only
#OPTS="$OPTS -verbose:gc -XX:+PrintGCDetails"
# JIT compiler debug only
#OPTS="$OPTS -XX:+PrintCompilation"
# Even more JIT compiler debugging - This produces a huge XML log file (for JITWatch)
#OPTS="$OPTS -XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+LogCompilation -XX:+PrintAssembly"

#
# Run.
#
CMD="$JAVA $OPTS -cp $LIB_PATH gaia.cu9.ari.gaiaorbit.GaiaSandboxDesktop $@"
echo "$CMD"
$CMD
