#!/bin/sh

export JAVA_HOME=/usr/lib/sdk/openjdk21
export PATH=$JAVA_HOME/bin:$PATH

exec /usr/lib/sdk/openjdk21/bin/java \
    -XX:+UseZGC -Xms128m \
    -Djava.library.path=/app/share/hyperlap2d/lib \
    -cp "/app/share/hyperlap2d/lib/*" \
    games.rednblack.editor.Main "$@"