#!/bin/sh

ONLY_REPORT_FAILURES=0

# TOOL_DIR should be set to the directory of srt_run.sh.
: ${TOOL_DIR:?"Need to set TOOL_DIR environment variable."}

TESTER=testerpy/tester.py
TOOL=$TOOL_DIR/srt_run.sh

# Competition mode tests
$TESTER $TOOL tests/comp $ONLY_REPORT_FAILURES -mode comp

