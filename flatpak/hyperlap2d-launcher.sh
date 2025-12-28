#!/bin/sh
export JAVA_HOME=/app/jre
export PATH=$JAVA_HOME/bin:$PATH

exec /app/jre/bin/java \
    -XX:+UseZGC -Xms128m \
    -Djava.library.path=/app/share/hyperlap2d/lib \
    -cp "/app/share/hyperlap2d/lib/*" \
    games.rednblack.editor.Main "$@"