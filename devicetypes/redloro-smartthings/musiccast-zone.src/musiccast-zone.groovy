/**
 *  SmartThings Device Handler: Yamaha MusicCast Zone
 *
 *  Author: redloro@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
  definition (name: "MusicCast Zone", namespace: "redloro-smartthings", author: "redloro@gmail.com") {

    /**
     * List our capabilties. Doing so adds predefined command(s) which
     * belong to the capability.
     */
    capability "Music Player"
    capability "Switch"
    capability "Switch Level"
    capability "Refresh"
    capability "Polling"
    capability "Sensor"
    capability "Actuator"

    /**
     * Define all commands, ie, if you have a custom action not
     * covered by a capability, you NEED to define it here or
     * the call will not be made.
     *
     * To call a capability function, just prefix it with the name
     * of the capability, for example, refresh would be "refresh.refresh"
     */
    command "source0"
    command "source1"
    command "source2"
    command "source3"
    command "source4"
    command "source5"
    command "mutedOn"
    command "mutedOff"
    command "zone"
  }

  /**
   * Define the various tiles and the states that they can be in.
   * The 2nd parameter defines an event which the tile listens to,
   * if received, it tries to map it to a state.
   *
   * You can also use ${currentValue} for the value of the event
   * or ${name} for the name of the event. Just make SURE to use
   * single quotes, otherwise it will only be interpreted at time of
   * launch, instead of every time the event triggers.
   */
  tiles(scale: 2) {
    multiAttributeTile(name:"state", type:"lighting", width:6, height:4) {
      tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        attributeState "on", label:'On', action:"switch.off", icon:"st.Electronics.electronics16", backgroundColor:"#79b821", nextState:"off"
        attributeState "off", label:'Off', action:"switch.on", icon:"st.Electronics.electronics16", backgroundColor:"#ffffff", nextState:"on"
      }
      tileAttribute ("source", key: "SECONDARY_CONTROL") {
        attributeState "source", label:'${currentValue}'
      }
    }

    // row
    controlTile("volume", "device.volume", "slider", height: 1, width: 6, range:"(0..100)") {
      state "volume", label: "Volume", action:"music Player.setLevel", backgroundColor:"#00a0dc"
    }

    // row
    standardTile("0", "device.source0", decoration: "flat", width: 2, height: 2) {
      state("off", label:"AV1", action:"source0", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"AV1", action:"source0", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("1", "device.source1", decoration: "flat", width: 2, height: 2) {
      state("off", label:"AV2", action:"source1", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"AV2", action:"source1", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("2", "device.source2", decoration: "flat", width: 2, height: 2) {
      state("off", label:"AV3", action:"source2", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"AV3", action:"source2", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("3", "device.source3", decoration: "flat", width: 2, height: 2) {
      state("off", label:"AV4", action:"source3", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"AV4", action:"source3", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("4", "device.source4", decoration: "flat", width: 2, height: 2) {
      state("off", label:"AV5", action:"source4", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"AV5", action:"source4", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("5", "device.source5", decoration: "flat", width: 2, height: 2) {
      state("off", label:"AV6", action:"source5", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"AV6", action:"source5", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }

    // row
    standardTile("muted", "device.muted", decoration: "flat", width: 2, height: 2) {
      state("off", label:'Muted', action:"mutedOn", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff", nextState:"on")
      state("on", label:'Muted', action:"mutedOff", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-mute.png", backgroundColor:"#ffffff", nextState:"off")
    }
    standardTile("refresh", "device.status", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
      state "default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh-icon", backgroundColor:"#ffffff"
    }

    // Defines which tile to show in the overview
    main "state"

    // Defines which tile(s) to show when user opens the detailed view
    details([
      "state",
      "volume",
      "0","1","2","3","4","5",
      "muted","refresh"
    ])
  }

  preferences {
    input name: "source0", type: "text", title: "Source 1", defaultValue: "hdmi"
    input name: "source1", type: "text", title: "Source 2", defaultValue: "tv"
    input name: "source2", type: "text", title: "Source 3", defaultValue: "analog"
    input name: "source3", type: "text", title: "Source 4", defaultValue: "pandora"
    input name: "source4", type: "text", title: "Source 5", defaultValue: "spotify"
    input name: "source5", type: "text", title: "Source 6", defaultValue: "airplay"
  }
}

/**************************************************************************
 * The following section simply maps the actions as defined in
 * the metadata into onAction() calls.
 *
 * This is preferred since some actions can be dealt with more
 * efficiently this way. Also keeps all user interaction code in
 * one place.
 *
 */
def on() {
  sendCommand("/${getZone()}/setPower?power=on")
  sendEvent(name: "switch", value: "on")
}
def off() {
  sendCommand("/${getZone()}/setPower?power=standby")
  sendEvent(name: "switch", value: "off")
}
def setLevel(value) {
  sendCommand("/${getZone()}/setVolume?volume=${value}")
  sendEvent(name: "volume", value: value)
}
def source0() {
  setSource(0)
}
def source1() {
  setSource(1)
}
def source2() {
  setSource(2)
}
def source3() {
  setSource(3)
}
def source4() {
  setSource(4)
}
def source5() {
  setSource(5)
}
def mutedOn() {
  sendCommand("/${getZone()}/setMute?enable=true")
  sendEvent(name: "muted", value: "on")
}
def mutedOff() {
  sendCommand("/${getZone()}/setMute?enable=false")
  sendEvent(name: "muted", value: "off")
}
def refresh() {
  sendCommand("/${getZone()}/getStatus")
}
/**************************************************************************/

/**
 * Called every so often (every 5 minutes actually) to refresh the
 * tiles so the user gets the correct information.
 */
def poll() {
  refresh()
}

def parse(String description) {
  return
}

def setSource(id) {
  //log.debug "source: "+getSourceName(id)
  sendCommand("/${getZone()}/setInput?input="+getSourceName(id)+"&mode=autoplay_disabled")
  setSourceTile(getSourceName(id))
}

def getSourceName(id) {
  if (settings) {
    return settings."source${id}"
  } else {
    return ['hdmi', 'tv', 'analog', 'pandora', 'spotify', 'airplay'].get(id)
  }
}

def setSourceTile(name) {
  sendEvent(name: "source", value: "Source: ${name}")
  for (def i = 0; i < 6; i++) {
    if (name == getSourceName(i)) {
      sendEvent(name: "source${i}", value: "on")
    }
    else {
      sendEvent(name: "source${i}", value: "off")
    }
  }
}

def zone(evt) {
  /*
  * Zone On/Off
  */
  if (evt.power) {
    sendEvent(name: "switch", value: (evt.power == "on") ? "on" : "off")
  }

  /*
  * Zone Volume
  */
  if (evt.volume) {
    def int volLevel = evt.volume.toInteger()
    sendEvent(name: "volume", value: volLevel)
  }

  /*
  * Zone Muted
  */
  if (evt.mute) {
    sendEvent(name: "muted", value: (evt.mute == true) ? "on" : "off")
  }

  /*
  * Zone Source
  */
  if (evt.input) {
    setSourceTile(evt.input)
  }
}

private sendCommand(body) {
  parent.sendCommand(body)
}

private getZone() {
  return new String(device.deviceNetworkId).tokenize('|')[2]
}