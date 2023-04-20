#!/bin/bash
PRGDIR=$(dirname "$0")
kill $(cat "${PRGDIR}"/app.pid)
rm -rf "${PRGDIR}"/app.pid
echo "EAIP RSocket Server Stopped."
