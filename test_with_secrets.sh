#!/bin/bash

# export SBT_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

./sbt "run -Dconfig.file=../hbbTVPluginSecrets/application_test.conf"