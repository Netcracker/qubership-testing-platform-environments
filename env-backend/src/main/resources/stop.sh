#!/bin/bash
PROCESS_ID=$( ps aux | grep environments | grep -v grep | awk '{print $2}')

if [ -n "$PROCESS_ID" ]; then
        echo "ATP-Environments Process:  ${PROCESS_ID}"
        kill ${PROCESS_ID}
        echo "ATP-Environments was stoped"
else
    echo "ATP-Environments is offline"
fi
