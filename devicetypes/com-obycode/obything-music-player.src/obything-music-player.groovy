/**
 *  obything Music Player
 *
 *  Copyright 2015 obycode
 */

import groovy.json.JsonSlurper

metadata {
  definition (name: "obything Music Player", namespace: "com.obycode", author: "obycode") {
    capability "Music Player"
    capability "Refresh"
    capability "Switch"

    // These strings are comma separated lists of names
    attribute "playlists", "json_object"
    attribute "speakers", "json_object"
    attribute "playlistDescription", "string"

    // playPlaylist(String uri, speakers=null, volume=null, resume=false, restore=false)
    command "playPlaylist", ["string", "string", "number", "number", "number"]
    // playTrack(String uri, speakers=null, volume=null, resume=false, restore=false, playlist=null)
    command "playTrack", ["string", "string", "number", "number", "number", "string"]

    command "update", ["string"]
  }

  simulator {
    // TODO: define status and reply messages here
  }

  tiles {
    tiles(scale: 2) {
      multiAttributeTile(name:"richmusic", type:"lighting", width:6, height:4) {
        tileAttribute("device.status", key: "PRIMARY_CONTROL") {
          attributeState "paused", label: 'Paused', action:"music Player.play", icon:"http://obything.obycode.com/icons/obything-device.png", backgroundColor:"#D0D0D0"
          attributeState "stopped", label: 'Stopped', action:"music Player.play", icon:"http://obything.obycode.com/icons/obything-device.png", backgroundColor:"#D0D0D0"
          attributeState "playing", label:'Playing', action:"music Player.pause", icon:"http://obything.obycode.com/icons/obything-device.png", backgroundColor:"#4C4CFF"
        }
        tileAttribute("device.trackDescription", key: "SECONDARY_CONTROL") {
          attributeState "default", label:'${currentValue}'
        }
        tileAttribute("device.level", key: "SLIDER_CONTROL") {
          attributeState "level", action:"music Player.setLevel", range:"(0..100)"
        }
      }

      standardTile("nextTrack", "device.status", width: 2, height: 2, decoration: "flat") {
        state "next", label:'', action:"music Player.nextTrack", icon:"st.sonos.next-btn", backgroundColor:"#ffffff"
      }
      standardTile("playpause", "device.status", width: 2, height: 2, decoration: "flat") {
        state "default", label:'', action:"music Player.play", icon:"st.sonos.play-btn", backgroundColor:"#ffffff"
        state "playing", label:'', action:"music Player.pause", icon:"st.sonos.pause-btn", backgroundColor:"#ffffff"
        state "paused", label:'', action:"music Player.play", icon:"st.sonos.play-btn", backgroundColor:"#ffffff"
      }
      standardTile("previousTrack", "device.status", width: 2, height: 2, decoration: "flat") {
        state "previous", label:'', action:"music Player.previousTrack", icon:"st.sonos.previous-btn", backgroundColor:"#ffffff"
      }
      standardTile("refresh", "device.status", width: 2, height: 2, decoration: "flat") {
        state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
      }
      standardTile("mute", "device.mute", width: 2, height: 2, decoration: "flat") {
        state "unmuted", label:"Mute", action:"music Player.mute", icon:"st.custom.sonos.unmuted", backgroundColor:"#ffffff"
        state "muted", label:"Unmute", action:"music Player.unmute", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff"
      }
      controlTile("levelSliderControl", "device.level", "slider", height: 2, width: 4) {
        state "level", label:"Volume", action:"music Player.setLevel", backgroundColor:"#ffffff"
      }
      valueTile("currentPlaylist", "device.playlistDescription", height:2, width:6, decoration: "flat") {
        state "default", label:'${currentValue}', backgroundColor:"#ffffff"
      }

      main "richmusic"
      details(["richmusic",
      "previousTrack","nextTrack","refresh",
      "levelSliderControl","mute",
      "currentPlaylist"
      ])
    }
  }
}

// parse events into attributes
def parse(String description) {
  // log.debug "parse called with $description"
  def map = stringToMap(description)
  if (map.headers && map.body) { //got device info response
    if (map.body) {
      def bodyString = new String(map.body.decodeBase64())
      def slurper = new JsonSlurper()
      def result = slurper.parseText(bodyString)
      parseMessage(result)
    }
  }
}

def parseMessage(message) {
  log.debug "message is $message"
  if (message.containsKey("volume")) {
    log.debug "setting volume to ${message.volume}"
    sendEvent(name: "level", value: message.volume)
  }
  if (message.containsKey("mute")) {
    log.debug "setting mute to ${message.mute}"
    sendEvent(name: "mute", value: message.mute)
  }
  if (message.containsKey("status")) {
    log.debug "setting status to ${message.status}"
    sendEvent(name: "status", value: message.status)
  }
  if (message.containsKey("trackData")) {
    def json = new groovy.json.JsonBuilder(message.trackData)
    log.debug "setting trackData to ${json.toString()}"
    sendEvent(name: "trackData", value: json.toString())
  }
  if (message.containsKey("trackDescription")) {
    log.debug "setting trackDescription info to ${message.trackDescription}"
    sendEvent(name: "trackDescription", value: message.trackDescription)
  }
  if (message.containsKey("playlistData")) {
    def json = new groovy.json.JsonBuilder(message.playlistData)
    log.debug "setting playlistData to ${json.toString()}"
    sendEvent(name: "playlistData", value: json.toString())
  }
  if (message.containsKey("playlistDescription")) {
    log.debug "setting playlistDescription info to ${message.playlistDescription}"
    sendEvent(name: "playlistDescription", value: message.playlistDescription)
  }
  if (message.containsKey("playlists")) {
    def json = new groovy.json.JsonBuilder(message.playlists)
    log.debug "setting playlists to ${json.toString()}"
    sendEvent(name: "playlists",value: json.toString())
  }
  if (message.containsKey("speakers")) {
    def json = new groovy.json.JsonBuilder(message.speakers)
    log.debug "setting speakers to ${json.toString()}"
    sendEvent(name: "speakers",value: json.toString())
  }
}

// Called by service manager to send updates from device
def update(message) {
  log.debug "update: $message"
  parseMessage(message)
}

def installed() {
  // Refresh to get current state
  refresh()
}

// handle commands
def refresh() {
  log.debug "Executing 'refresh'"
  getInfo("command=refresh")
}

def on() {
  log.debug "Executing 'on' (play)"
  sendCommand("command=play")
}

def off() {
  log.debug "Executing 'off' (pause)"
  sendCommand("command=pause")
}

def play() {
  log.debug "Executing 'play'"
  sendCommand("command=play")
}

def pause() {
  log.debug "Executing 'pause'"
  sendCommand("command=pause")
}

def stop() {
  log.debug "Executing 'stop'"
  sendCommand("command=stop")
}

def nextTrack() {
  log.debug "Executing 'nextTrack'"
  sendCommand("command=next")
}

def setLevel(value) {
  log.debug "Executing 'setLevel' to $value"
  sendCommand("command=volume&level=$value")
}

// def playText(String msg) {
//   log.debug "Executing 'playText'"
//   sendCommand("say=$msg")
// }
//
def mute() {
  log.debug "Executing 'mute'"
  sendCommand("command=mute")
}

def previousTrack() {
  log.debug "Executing 'previousTrack'"
  sendCommand("command=previous")
}

def unmute() {
  log.debug "Executing 'unmute'"
  sendCommand("command=unmute")
}

def playPlaylist(String uri, speakers=null, volume=null) {
  log.trace "playPlaylist($uri, $speakers, $volume, $resume, $restore)"
  def command = "command=playlist&name=${uri}"
  if (speakers) {
    command += "&speakers=${speakers}"
  }
  if (volume) {
    command += "&volume=${volume}"
  }
  sendCommand(command)
}

def playTrack(String uri, speakers=null, volume=null, resume=false, restore=false, playlist=null) {
  log.trace "playTrack($uri, $speakers, $volume, $resume, $restore, $playlist)"
  def command = "command=track&url=${uri}"
  if (speakers) {
    command += "&speakers=${speakers}"
  }
  if (volume) {
    command += "&volume=${volume}"
  }
  if (resume) {
    command += "&resume="
  }
  else if (restore) {
    command += "&restore="
  }
  if (playlist) {
    command += "&playlist=$playlist"
  }
  sendCommand(command)
}

// def speak(text) {
//   def url = textToSpeech(text)
//   sendCommand("playTrack&track=${url.uri}&resume")
// }
//
// def beep() {
//   sendCommand("beep")
// }

// Private functions used internally
private Integer convertHexToInt(hex) {
  Integer.parseInt(hex,16)
}


private String convertHexToIP(hex) {
  [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
  def parts = device.deviceNetworkId.split(":")
  def ip = convertHexToIP(parts[0])
  def port = convertHexToInt(parts[1])
  return ip + ":" + port
}

private sendCommand(command) {
  def path = "/player&" + command

  def result = new physicalgraph.device.HubAction(
    method: "POST",
    path: path,
    headers: [
        HOST: getHostAddress()
    ],
  )
  result
}

private getInfo(command) {
  def path = "/player&" + command

  def result = new physicalgraph.device.HubAction(
    method: "GET",
    path: path,
    headers: [
        HOST: getHostAddress()
    ],
  )
  result
}
