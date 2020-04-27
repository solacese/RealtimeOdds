#!/bin/bash -x
java -cp ./build/libs/cacheproxy-0.1.jar cacheproxy.Proxy -h localhost -u cache@Match -w cache -m 20
