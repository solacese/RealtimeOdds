#!/bin/bash

SDKPERF=/Users/ichen/devprojects/sdkperf-jcsmp-8.4.1.8/sdkperf_java.sh
CURDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

function pub {
    echo match$1-$2.json
    $SDKPERF -cip=localhost -cu=default@Match -cp=default \
        -ptl="t1/match/$(($1+1))/full" \
        -pal=$CURDIR/samples/MatchDetails_demo/match$1/match$1-$2.json \
        -mr=10 -mn=1 -q > /dev/null &
}

function pubAllMatchs {
    pub 0 $1
    pub 1 $1
    pub 2 $1
    pub 3 $1
}

function pubAllMatchsInLoop {
    
    for ((i=0;i<$1;i++))
    do
        date
        pubAllMatchs $((i % 6)) 
        sleep 2
    done
}

pubAllMatchsInLoop $1
