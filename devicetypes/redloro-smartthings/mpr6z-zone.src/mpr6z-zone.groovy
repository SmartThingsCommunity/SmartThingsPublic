/**
 *  SmartThings Device Handler: Monoprice 6-Zone Amplifier Zone
 *
 *  Author: tcjennings@hotmail.com based on Russound RNET Zone by redloro@gmail.com
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
  definition (name: "MPR6Z Zone", namespace: "redloro-smartthings", author: "tcjennings@hotmail.com") {

    /**
     * List our capabilties. Doing so adds predefined command(s) which
     * belong to the capability.
     */
    capability "Music Player"
    capability "Switch"
    capability "Refresh"
    capability "Polling"

    /**
     * Define all commands, ie, if you have a custom action not
     * covered by a capability, you NEED to define it here or
     * the call will not be made.
     *
     * To call a capability function, just prefix it with the name
     * of the capability, for example, refresh would be "refresh.refresh"
     */
    command "source1"
    command "source2"
    command "source3"
    command "source4"
    command "source5"
    command "source6"
    command "partyModeOn"
    command "partyModeOff"
    command "allOff"
    command "muteOn"
    command "muteOff"
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
    multiAttributeTile(name:"state", type:"generic", width:6, height:4) {
      tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        attributeState "on", label:'On', action:"switch.off", icon:"st.Electronics.electronics16", backgroundColor:"#79b821", nextState:"off"
        attributeState "off", label:'Off', action:"switch.on", icon:"st.Electronics.electronics16", backgroundColor:"#ffffff", nextState:"on"
      }
      tileAttribute ("source", key: "SECONDARY_CONTROL") {
        attributeState "source", label:'${currentValue}'
      }
    }

    // Row 1
    controlTile("volume", "device.volume", "slider", height: 1, width: 6, range:"(0..38)") {
      state "volume", label: "Volume", action:"music Player.setLevel", backgroundColor:"#00a0dc"
    }

    // Row 2-3
    standardTile("1", "device.source1", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 1", action:"source1", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 1", action:"source1", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("2", "device.source2", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 2", action:"source2", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 2", action:"source2", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("3", "device.source3", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 3", action:"source3", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 3", action:"source3", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("4", "device.source4", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 4", action:"source4", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 4", action:"source4", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("5", "device.source5", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 5", action:"source5", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 5", action:"source5", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("6", "device.source6", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 6", action:"source6", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 6", action:"source6", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }

    // Row 4
	standardTile("mute", "device.mute", decoration: "flat", width: 2, height: 2) {
      state("off", label:'Mute',  action:"muteOn", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state( "on", label:'Mute', action:"muteOff", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-mute.png", backgroundColor:"#ffffff")
    }
    standardTile("partyMode", "device.partyMode", decoration: "flat", width: 2, height: 2, inactiveLabel: false) {
      state "off", label:'Party Mode', action:"partyModeOn", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff"
      state "on", label:'Party Mode', action:"partyModeOff", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-party.png", backgroundColor:"#ffffff"
    }
    standardTile("alloff", "device.status", decoration: "flat", width: 2, height: 2, inactiveLabel: false) {
      state "default", label:"All Off", action:"allOff", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-power.png", backgroundColor:"#ffffff"
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
      "1","2","3","4","5","6",
      "mute","partyMode","alloff",
      "refresh"
    ])
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
def on() { sendCommand(["state": 1], false) }
def off() { sendCommand(["state": 0], false) }
def source1() { sendCommand(["source": 1], true) }
def source2() { sendCommand(["source": 2], true) }
def source3() { sendCommand(["source": 3], true) }
def source4() { sendCommand(["source": 4], true) }
def source5() { sendCommand(["source": 5], true) }
def source6() { sendCommand(["source": 6], true) }
def setLevel(value) { sendCommand(["volume": value.intValue()], true) }
def muteOn() { sendCommand(["mute": 1], false) }
def muteOff() { sendCommand(["mute": 0], false) }
def partyModeOn() { parent.partyMode(["state": 1, "master": getZone(), "source": getSource(), "volume": getVolume()]) }
def partyModeOff() { partyMode(["state": 0]) }
def allOff() { sendCommand(["all": 0], false) }
def refresh() { sendCommand([], false) }
/**************************************************************************/

/**
 * Called every so often (every 5 minutes actually) to refresh the
 * tiles so the user gets the correct information.
 */
def poll() {
  refresh()
}

def zone(evt) {
  /*
  * Zone On/Off state (00 = OFF or 01 = ON)
  */
  if (evt.containsKey("state")) {
    //log.debug "setting state to ${result.state}"
    sendEvent(name: "switch", value: (evt.state == 1) ? "on" : "off")

    //turn off party mode
    if (evt.state == 0) {
      partyMode(["state": 0])
    }
  }

  /*
  * Zone Volume level (00 - 38)
  */
  if (evt.containsKey("volume")) {
    //log.debug "setting volume to ${result.volume}"
    sendEvent(name: "volume", value: evt.volume)
  }

  /*
  * MPR6Z doesn't have a Loudness setting
  * Zone Loudness (0x00 = OFF, 0x01 = ON )
  if (evt.containsKey("loudness")) {
    //log.debug "setting loudness to ${result.loudness}"
    sendEvent(name: "loudness", value: (evt.loudness == 1) ? "on" : "off")
  }
  */
  if (evt.containsKey("mute")) {
    sendEvent(name: "mute", value: (evt.mute == 1) ? "on" : "off")
  }

  /*
  * Zone Source selected (1-6)
  */
  if (evt.containsKey("source")) {
    //log.debug "setting source to ${result.source}"
    for (def i = 1; i < 7; i++) {
      if (i == evt.source) {
        state.source = i
        sendEvent(name: "source${i}", value: "on")
        sendEvent(name: "source", value: "Source ${i}: ${evt.sourceName}")
      }
      else {
        sendEvent(name: "source${i}", value: "off")
      }
    }
  }
}

def partyMode(evt) {
  // ["state": "", "master": "", "source": "", "volume": ""]
  //log.debug "ZONE${getZone()} partyMode(${evt})"
  if (evt.containsKey("state")) {
    sendEvent(name: "partyMode", value: (evt.state == 1) ? "on" : "off")
    if (evt.state == 1) {
      sendCommand(["state": 1], false)
    }
  } else {
    // exit if partyMode is off
    if (getPartyMode() == 0) {
      return
    }
  }

  if (evt.containsKey("volume")) {
    sendCommand(["volume": evt.volume], false)
  }

  if (evt.containsKey("source")) {
    sendCommand(["source": evt.source], false)
  }
}

private sendCommand(evt, broadcast) {
  //log.debug "ZONE${getZone()} sendCommand(${evt}, ${broadcast})"

  // send command to partyMode
  if (broadcast && getPartyMode()) {
    parent.partyMode(evt)
    return
  }

  // send command to Monoprice
  def part = ""
  if (evt.size() == 1) {
    part = "/${evt.keySet()[0]}/${evt.values()[0]}"
  }

  //log.debug "ZONE${getZone()} calling parent.sendCommand"
  parent.sendCommand("/plugins/mpr-sg6z/zones/${getZone()}${part}")
}

private getPartyMode() {
  return (device.currentState("partyMode").getValue() == "on") ? 1 : 0;
}

private getVolume() {
  return device.currentState("volume").getValue().toInteger()
}

private getSource() {
    for (def i = 1; i < 7; i++) {
      if (device.currentState("source${i}").getValue()  == "on") {
        return i
      }
    }
}

private getZone() {
  return new String(device.deviceNetworkId).tokenize('|')[1].replace('zone', '')
}
