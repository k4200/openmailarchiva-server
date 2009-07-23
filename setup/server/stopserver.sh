# MAILARCHIVA_HOME must point to your mailarchiva home directory
MAILARCHIVA_HOME=/usr/local/mailarchiva
export CATALINA_HOME=$MAILARCHIVA_HOME/server
export JAVA_OPTS="-Dfile.encoding=UTF-8"
export PATH=$PATH:$JAVA_HOME/bin
sh $MAILARCHIVA_HOME/server/bin/shutdown.sh

