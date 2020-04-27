import React, { useState } from "react";
import Container from '@material-ui/core/Container';

import * as MQTT from 'paho-mqtt';
import LoginCtrl from './LoginCtrl'
import MatchSubscription from './MatchSubscription'
import MatchTable from './MatchTable'
import DeltaTable from './DeltaTable'

// Global variables
var mqttClient;
var testCase = "FULL"
var replyToTopic = "";
const cacheRequestTopic = "cacheproxy/request";
const utf8decoder = new TextDecoder();
const topicsMap = new Map([["FULL", "t1/match/+/full"],
["DELTA", "t1/match/+/delta"],
["SEQUENCE", "t1/match/1/delta"]])
var _gCurrentMatchList = []
var _gCurrentDeltaList = []
var _lastSeq = 0

export default function App() {
  const [status, setStatus] = useState({
    isConnect: false,
    text: "No Connection"
  });
  const [isStart, setIsStart] = useState(false);
  const [matchList, setMatchList] = useState([]);
  const [deltaList, setDeltaList] = useState([]);
  var host = "localhost"
  var port = 9001
  var userName = "web"
  var password = "password"

  function connect(host, port, userName, password) {
    console.log("connect() is called")
    if ('string' == typeof port) {
      port = parseInt(port)
    }
    mqttClient = new MQTT.Client(host, port, "");
    mqttClient.onConnectionLost = onConnectionLost;
    mqttClient.onMessageArrived = onMessageArrived;
    mqttClient.onConnected = onConnected;
    var options = {
      timeout: 3,
      onFailure: onFailure,
      userName: userName,
      password: password,
    };
    mqttClient.connect(options);
  }

  function disConnect() {
    console.log("disConnect() is called")
    if (status.isConnect) {
      mqttClient.disconnect()
    }
  }

  function onFailure(response) {
    setStatus({
      isConnect: false,
      text: "Connection Failure: " + response.errorMessage
    });
  }

  function onConnectionLost(response) {
    setStatus({
      isConnect: false,
      text: "Connection Lost: " + response.errorMessage
    });
    setIsStart(false)
  }

  function onConnected(reconnect, URI) {
    // Subscribe client to the special Solace topic for requesting a unique
    // Reply-to destination for the MQTT client
    mqttClient.subscribe("$SYS/client/reply-to");
    setStatus({
      isConnect: true,
      text: "Connected to " + URI
    });
    setIsStart(false)
    _gCurrentMatchList = []
    _gCurrentDeltaList = []
    setMatchList(_gCurrentMatchList)
    setDeltaList(_gCurrentDeltaList)
  }

  // called when a message arrives
  function onMessageArrived(message) {
    // if its '$SYS/client/reply-to', request for cache
    if (message.destinationName === '$SYS/client/reply-to') {
      replyToTopic = message.payloadString;
      mqttClient.subscribe(replyToTopic)
      return
    }
    var json = JSON.parse(utf8decoder.decode(message.payloadBytes));
    if (json.isDelta) {
      if (testCase === "DELTA") {
        onDeltaMessage(json)
      } else {
        onSequenceaMessage(json)
      }
    } else {
      onFullMatchMessage(json)
    }
  }

  function onFullMatchMessage(match) {
    var index = _gCurrentMatchList.findIndex((m) => m.matchNum === match.matchNum)
    if (index >= 0) {
      _gCurrentMatchList.splice(index, 1)
    }
    _gCurrentMatchList.push(match)
    setMatchList([..._gCurrentMatchList])
  }

  function onDeltaMessage(delta) {
    //console.log(`onDeltaMessage(${delta.matchNum}), matchList.length=${matchList.length}`)
    
    var index = _gCurrentMatchList.findIndex((m) => m.matchNum === delta.matchNum)
    if (index >= 0) {
      var match = _gCurrentMatchList[index];
      match.matchNum = delta.matchNum
      match.matchStatus = delta.matchStatus
      match.statuslastupdated = delta.statuslastupdated
      match.hadodds.H = delta.hadodds.H
      match.hadodds.A = delta.hadodds.A
      match.hadodds.D = delta.hadodds.D

      setMatchList([..._gCurrentMatchList])
    } else {
      // no related full match info, ask for cache of this match
      // console.log(`index:${index} -> requestSingleCachedMatchs(${delta.matchNum})`)
      var requestTopic = "t1/match/" + delta.matchNum + "/full"
      publishCacheRequest(requestTopic);
    }
  }

  function onSequenceaMessage(delta) {
    var index = _gCurrentDeltaList.findIndex((d)=> d.sequenceInt === delta.sequenceInt)
    if (index >= 0){
      return
    }
    
    _gCurrentDeltaList.push(delta);
    if (_gCurrentDeltaList.length > 20){
      _gCurrentDeltaList.shift()
    }

    if(!delta.isReSend){
      var curSeq = delta.sequenceInt;
      if (curSeq - _lastSeq !== 1){
        console.log(`Msg Lost! Last Seq=${_lastSeq} Current Seq=${curSeq}`)
        // ask for lost messages from cache
        publishCacheRequest(topicsMap.get(testCase), true)
      }
      _lastSeq = curSeq;
    }
    setDeltaList([..._gCurrentDeltaList])
  }

  function start(testCaseToDo) {
    testCase = testCaseToDo
    console.log("start: " + testCase)

    if (testCase === "FULL" || testCase === "DELTA"){
      // At first, ask for cached full matchs
      publishCacheRequest(topicsMap.get("FULL"))
    }
    mqttClient.subscribe(topicsMap.get(testCase))
    setIsStart(true)
  }

  function publishCacheRequest(requestTopic, isReSend=false) {
    var req = {
      protocol: "mqtt",
      topic: requestTopic,
      replyTo: replyToTopic,
      isReSend: isReSend
    }
    mqttClient.publish(cacheRequestTopic, JSON.stringify(req))
  }


  function stop() {
    mqttClient.unsubscribe(topicsMap.get(testCase))
    setIsStart(false)
    // clean the Match and Delta list
    _gCurrentMatchList = [];
  }

  return (
    <Container maxWidth="md">
      <LoginCtrl host={host} port={port} userName={userName} password={password}
        status={status} connect={connect} disConnect={disConnect} />
      <MatchSubscription testCase={testCase} topicsMap={topicsMap}
        isStart={isStart} status={status} start={start} stop={stop} />
      {testCase==="SEQUENCE"
      ?<DeltaTable deltaList={deltaList}/>
      :<MatchTable matchList={matchList} />
      }
    </Container>
  );
}
