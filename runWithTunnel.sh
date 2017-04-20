#! /bin/bash

echo "Start pagekite.py Remote:"
echo "http:\\echoirc.pagekite.me"
echo "Check: pagekite.log"

pagekite.py 9000 echoirc.pagekite.me > pagekite.log &
