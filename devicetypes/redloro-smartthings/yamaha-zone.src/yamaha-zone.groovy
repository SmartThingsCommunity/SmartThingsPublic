/**
 *  SmartThings Device Handler: Yamaha Zone
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
  definition (name: "Yamaha Zone", namespace: "redloro-smartthings", author: "redloro@gmail.com") {

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
    command "source0"
    command "source1"
    command "source2"
    command "source3"
    command "source4"
    command "source5"
    command "mutedOn"
    command "mutedOff"
    command "partyModeOn"
    command "partyModeOff"
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

    // row
    //controlTile("volume", "device.volume", "slider", height: 1, width: 6, range:"(0..100)") {
    controlTile("volume", "device.volume", "slider", height: 1, width: 6, range:"(-80..16)") {
      state "volume", label: "Volume", action:"music Player.setLevel", backgroundColor:"#ffffff"
    }

    // row
    standardTile("0", "device.source0", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 1", action:"source0", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 1", action:"source0", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("1", "device.source1", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 2", action:"source1", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 2", action:"source1", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("2", "device.source2", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 3", action:"source2", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 3", action:"source2", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("3", "device.source3", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 4", action:"source3", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 4", action:"source3", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("4", "device.source4", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 5", action:"source4", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 5", action:"source4", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }
    standardTile("5", "device.source5", decoration: "flat", width: 2, height: 2) {
      state("off", label:"Source 6", action:"source5", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff")
      state("on", label:"Source 6", action:"source5", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff")
    }

    // row
    standardTile("muted", "device.muted", decoration: "flat", width: 2, height: 2) {
      state("off", label:'Muted', action:"mutedOn", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff", nextState:"on")
      state("on", label:'Muted', action:"mutedOff", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff", nextState:"off")
    }
    standardTile("partyMode", "device.partyMode", decoration: "flat", width: 2, height: 2, inactiveLabel: false) {
      state("off", label:'Party Mode', action:"partyModeOn", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff", nextState:"on")
      state("on", label:'Party Mode', action:"partyModeOff", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff", nextState:"off")
    }
    standardTile("refresh", "device.status", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
      state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
    }

    // Defines which tile to show in the overview
    main "state"

    // Defines which tile(s) to show when user opens the detailed view
    details([
      "state",
      "volume",
      "0","1","2","3","4","5",
      "muted", "partyMode","refresh"
    ])
  }

  preferences {
    input name: "source0", type: "text", title: "Source 1", defaultValue: "AV1"
    input name: "source1", type: "text", title: "Source 2", defaultValue: "AV2"
    input name: "source2", type: "text", title: "Source 3", defaultValue: "AV3"
    input name: "source3", type: "text", title: "Source 4", defaultValue: "AV4"
    input name: "source4", type: "text", title: "Source 5", defaultValue: "AV5"
    input name: "source5", type: "text", title: "Source 6", defaultValue: "AV6"
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
  def zone = getZone()
  def cmd = (zone != "Zone_B") ?
    "<YAMAHA_AV cmd=\"PUT\"><${zone}><Power_Control><Power>On</Power></Power_Control></${zone}></YAMAHA_AV>" :
    "<YAMAHA_AV cmd=\"PUT\"><Main_Zone><Power_Control><Zone_B_Power>On</Zone_B_Power></Power_Control></Main_Zone></YAMAHA_AV>"
  sendCommand(cmd)
  sendEvent(name: "switch", value: "on")
}
def off() {
  def zone = getZone()
  def cmd = (zone != "Zone_B") ?
    "<YAMAHA_AV cmd=\"PUT\"><${zone}><Power_Control><Power>Standby</Power></Power_Control></${zone}></YAMAHA_AV>" :
    "<YAMAHA_AV cmd=\"PUT\"><Main_Zone><Power_Control><Zone_B_Power>Standby</Zone_B_Power></Power_Control></Main_Zone></YAMAHA_AV>"
  sendCommand(cmd)
  sendEvent(name: "switch", value: "off")
}
def setLevel(value) {
  def zone = getZone()
  //def int volLevel = (Math.round(value * 9 / 5) * 5 - 800).intValue()
  def int volLevel = (value * 10).intValue()
  def cmd = (zone != "Zone_B") ?
    "<YAMAHA_AV cmd=\"PUT\"><${zone}><Volume><Lvl><Val>${volLevel}</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Volume></${zone}></YAMAHA_AV>" :
    "<YAMAHA_AV cmd=\"PUT\"><Main_Zone><Volume><Zone_B><Lvl><Val>${volLevel}</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Zone_B></Volume></Main_Zone></YAMAHA_AV>"
  sendCommand(cmd)
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
  def zone = getZone()
  def cmd = (zone != "Zone_B") ?
    "<YAMAHA_AV cmd=\"PUT\"><${zone}><Volume><Mute>On</Mute></Volume></${zone}></YAMAHA_AV>" :
    "<YAMAHA_AV cmd=\"PUT\"><Main_Zone><Volume><Zone_B><Mute>On</Mute></Zone_B></Volume></Main_Zone></YAMAHA_AV>"
  sendCommand(cmd)
  sendEvent(name: "muted", value: "on")
}
def mutedOff() {
  def zone = getZone()
  def cmd = (zone != "Zone_B") ?
    "<YAMAHA_AV cmd=\"PUT\"><${zone}><Volume><Mute>Off</Mute></Volume></${zone}></YAMAHA_AV>" :
    "<YAMAHA_AV cmd=\"PUT\"><Main_Zone><Volume><Zone_B><Mute>Off</Mute></Zone_B></Volume></Main_Zone></YAMAHA_AV>"
  sendCommand(cmd)
  sendEvent(name: "muted", value: "off")
}
def partyModeOn() {
  sendCommand("<YAMAHA_AV cmd=\"PUT\"><System><Party_Mode><Mode>On</Mode></Party_Mode></System></YAMAHA_AV>")
  sendEvent(name: "partyMode", value: "on")
}
def partyModeOff() {
  sendCommand("<YAMAHA_AV cmd=\"PUT\"><System><Party_Mode><Mode>Off</Mode></Party_Mode></System></YAMAHA_AV>")
  sendEvent(name: "partyMode", value: "off")
}
def refresh() {
  def zone = getRealZone();
  sendCommand("<YAMAHA_AV cmd=\"GET\"><${zone}><Basic_Status>GetParam</Basic_Status></${zone}></YAMAHA_AV>")
  sendCommand("<YAMAHA_AV cmd=\"GET\"><System><Party_Mode><Mode>GetParam</Mode></Party_Mode></System></YAMAHA_AV>")
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
  //log.debug "source: "+settings."source${id}"
  def zone = getRealZone();
  sendCommand("<YAMAHA_AV cmd=\"PUT\"><${zone}><Input><Input_Sel>"+settings."source${id}"+"</Input_Sel></Input></${zone}></YAMAHA_AV>")
  setSourceTile(settings."source${id}")
}

def setSourceTile(name) {
  sendEvent(name: "source", value: "Source: ${name}")
  for (def i = 0; i < 6; i++) {
    if (name == settings."source${i}") {
      sendEvent(name: "source${i}", value: "on")
    }
    else {
      sendEvent(name: "source${i}", value: "off")
    }
  }
}

def zone(evt) {
  def val = ""
  def zone = getZone()

  /*
  * Zone On/Off
  */
  val = (zone != "Zone_B") ?
    evt.Basic_Status.Power_Control.Power.text() :
    evt.Basic_Status.Power_Control.Zone_B_Power_Info.text()
  if (val) {
    sendEvent(name: "switch", value: (val == "On") ? "on" : "off")
  }

  /*
  * Zone Volume
  */
  val = (zone != "Zone_B") ?
    evt.Basic_Status.Volume.Lvl.Val.text() :
    evt.Basic_Status.Volume.Zone_B.Lvl.Val.text()
  if (val) {
    def int volLevel = val.toInteger() ?: -250
    //sendEvent(name: "volume", value: ((volLevel + 800) / 9).intValue())
    sendEvent(name: "volume", value: (volLevel / 10).intValue())
  }

  /*
  * Zone Muted
  */
  val = (zone != "Zone_B") ? 
    evt.Basic_Status.Volume.Mute.text() :
    evt.Basic_Status.Volume.Zone_B.Mute.text()
  if (val) {
    sendEvent(name: "muted", value: (val == "On") ? "on" : "off")
  }

  /*
  * Zone Source
  */
  if (evt.Basic_Status.Input.Input_Sel.text()) {
    setSourceTile(evt.Basic_Status.Input.Input_Sel.text())
  }

  /*
  * Party Mode
  */
  if (evt.Party_Mode.Mode.text()) {
    sendEvent(name: "partyMode", value: (evt.Party_Mode.Mode.text() == "On") ? "on" : "off")
  }
}

private sendCommand(body) {
  parent.sendCommand(body)
}

private getRealZone() {
  def zone = getZone();
  if (zone == "Zone_B") {
    return "Main_Zone";
  }
  return zone;
}

private getZone() {
  return new String(device.deviceNetworkId).tokenize('|')[1]
}
