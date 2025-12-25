#!/bin/sh
exec /usr/lib/sdk/openjdk21/bin/java \
    -XX:+UseZGC -Xms128m \
    -Djava.library.path=/app/share/hyperlap2d/lib \
    -cp "/app/share/hyperlap2d/lib/*" \
    games.rednblack.editor.Main "$@"