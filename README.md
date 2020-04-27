# Realtime Odds Demo

This is a demo to show how to use Solace PS+ broker to deliver real-time odds event to the mqtt client in different mode:

1. **FULL** mode: the client will receive each event with full information of the Match
1. **DELTA** mode: In most cases, the client will only receive delta messages, it will consume much less bandwidth (less than 1/10)  compare to the FULL mode
1. **SEQUENCE** mode, it will show out the sequence number of each event, and if the web app detects event lost, it will ask the broker to resend events

![](./webapp.png)

## Solace PS+ Broker Setup

1. Enable Mqtt service
2. Setup a Distributed Cache with name `MatchCache` to listen on topic "t1/match/>"

## How to build the demo

```bash
cd cacheProxy
gradle build
cd ..
cd webapp
yarn install
yarn build
cd ..
```

## How to run the demo

### Start the cache proxy

```bash
cd cacheProxy
./run_match.sh
+ java -cp ./build/libs/cacheproxy-0.1.jar cacheproxy.Proxy -h localhost -u cache@Match -w cache -m 20
13:28:07.312 main INFO  [BaseApp]: Proxy / JCSMP 10.8.0
13:28:07.312 main INFO  [BaseApp]: ===================================================
13:28:07.666 main DEBUG [Proxy]: Start to connect session
13:28:07.776 main DEBUG [Proxy]: Create message consumer
13:28:07.812 main DEBUG [Proxy]: Listen on the cache request topic: cacheproxy/request
13:28:07.819 main DEBUG [Proxy]: Connected. Awaiting message...
----------------------------Press Ctrl+C to exit ...----------------------------
```

### Start the WebApp

You could start the in develop mode with `yarn start` or setup a web server to serve the `./build` folder

### Publish sample data

First, please update the `SDKPERF` var inside `./pubMatchs.sh` to the sdkperf tools of your machine, then run ``./pubMatchs.sh times`. It will send all Match information every two seconds until it is sent `times` times.