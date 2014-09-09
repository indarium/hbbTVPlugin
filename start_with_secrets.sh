#!/bin/bash
# description: hbbtvplugin Play App
# processname: hbbtvplugin

# User running the Play process
USER=play
USER_HOME=/opt/playApps/hbbTVPlugin

# Java home, add java and play to path
# export JAVA_HOME=$USER_HOME/java_home
# export PATH=$JAVA_HOME/bin:$USER_HOME/play_home:$PATH

# Path to the application
APP_PATH=./target/universal/stage
APP_OPTS="-Dconfig.file=../hbbTVPluginSecrets/application_prod.conf"

RETVAL=0

case "$1" in
  start)
    echo -n "Starting Play service"
    rm -f ${APP_PATH}/RUNNING_PID
    #su $USER -c "$APP_PATH/bin/hbbtvplugin $APP_OPTS >/dev/null" &
    $APP_PATH/bin/hbbtvplugin $APP_OPTS >/dev/null &
    RETVAL=$?
    ;;
  stop)
    echo -n "Shutting down Play service"
    kill `cat $APP_PATH/RUNNING_PID`
    RETVAL=$?
    ;;
esac
exit $RETVAL