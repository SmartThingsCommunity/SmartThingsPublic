/**
*    SmartThings Lightify Dimmer Switch support
*    Copyright (C) 2016  Adam Outler
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/

/**
* Command reference 
* on  'catchall: 0104 0006 01 01 0140 00 3A68 01 00 0000 01 00 '
* off  'catchall: 0104 0006 01 01 0140 00 3A68 01 00 0000 00 00 '
* held up   'catchall: 0104 0008 01 01 0140 00 3A68 01 00 0000 05 00 0032'
* held down  'catchall: 0104 0008 01 01 0140 00 3A68 01 00 0000 01 00 0132'
* released   'catchall: 0104 0008 01 01 0140 00 3A68 01 00 0000 03 00 '
*/


/**
*sets up fingerprint for autojoin
*sets up commands for polling and others
*sets up capabilities
*sets up variables
*/
metadata {
 definition(name: "Lightify Dimming Switch- Zigbee", namespace: "adamoutler", author: "Adam Amber House") {
  capability "Battery"
  capability "Button"
  capability "Switch Level"
  attribute "State Array", "string"
  attribute 'Awesomeness Level', "string"
  attribute 'state', "string"
  command "refresh"
  command "poll"
  command "toggle"
  fingerprint profileId: "0104", deviceId: "0001", inClusters: "0000, 0001, 0003, 0020, 0402, 0B05", outClusters: "0003, 0006, 0008, 0019", manufacturer: "OSRAM", model: "LIGHTIFY Dimming Switch", deviceJoinName: "OSRAM Lightify Dimming Switch"
 }


 simulator {
  // Simulations are for loosers, work in production :D
 }


preferences {
  section("Device1") {
   input("device1", "string", title: "Device Network ID 1", description: "The Device Network Id", defaultValue: "", type: "capability.switch", required: false, displayDuringSetup: false)
   input("end1", "string", title: "Device Endpoint ID 1", description: "endpointId from Data Section of device", defaultValue: "", required: false, displayDuringSetup: false)
  }
  section("Device2") {
   input("device2", "string", title: "Device Network ID 2", description: "The Device Network Id", defaultValue: "", type: "capability.switch", required: false, displayDuringSetup: false)
   input("end2", "string", title: "Device Endpoint ID 2", description: "endpointId from Data Section of device", defaultValue: "", required: false, displayDuringSetup: false)
  }
  section("Device3") {
   input("device3", "string", title: "Device Network ID 3", description: "The Device Network Id", defaultValue: "", type: "capability.switch", required: false, displayDuringSetup: false)
   input("end3", "string", title: "Device Endpoint ID 3", description: "endpointId from Data Section of device", defaultValue: "", required: false, displayDuringSetup: false)
  }
  section("Device4") {
   input("device4", "string", title: "Device Network ID 4", description: "The Device Network Id", defaultValue: "", type: "capability.switch", required: false, displayDuringSetup: false)
   input("end4", "string", title: "Device Endpoint ID 4", description: "endpointId from Data Section of device", defaultValue: "", required: false, displayDuringSetup: false)
  }
  section("Device5") {
   input("device5", "string", title: "Device Network ID 5", description: "The Device Network Id", defaultValue: "", type: "capability.switch", required: false, displayDuringSetup: false)
   input("end5", "string", title: "Device Endpoint ID 5", description: "endpointId from Data Section of device", defaultValue: "", required: false, displayDuringSetup: false)
  }
 }


/**
*UI tile definitions
*/
 tiles(scale: 2) {
  standardTile("button", "device.state", width: 6, height: 4) {
   state "off", label: 'Off', action: "toggle", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
   state "on", label: "On", action: "toggle", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
   state "turningOn", label: 'Turning on', icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
   state "turningOff", label: 'Turning off', icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
  }
  valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
   state "battery", label: 'battery ${currentValue}%'
  }
  valueTile("brightness", "device.level", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
   state "brightness", label: 'brightness\n${currentValue}%'
  }
  standardTile("refresh", "device.button", decoration: "flat", width: 2, height: 2) {
   state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
  }
  controlTile("levelSliderControl", "device.level", "slider", height: 2, width: 6, inactiveLabel: false, range:"(1..100)") {
    state "level", action:"setLevel"
  }
  main "button"
  details(["button", "levelSliderControl", "switch", "battery", "brightness", "refresh"])
 }
}

/**
*returns a map representing important states
*/
private Map getStatus() {
 return [name: 'button',
  brightness: state.brightness,
  battery: state.battery,
  value: state.value,
  level: state.level,
  lastAction: state.lastAction,
  on: state.on,
  data: '',
  descriptionText: "$device.displayName button $state.buttonNumber was $value"
 ]
}


/**
 *Gets a list of devices in [address,endpoint] format compiled from inputs above
 *returns: Arraylist<[address,endpoint]>
 */
final ArrayList < String[] > getDevices() {
 String[] devs = [settings.device1, device2, device3, device4, device5]
 if (devs == [null, null, null, null, null]) log.error("------No devices configured in $device preferences--------")
 String[] ends = [end1, end2, end3, end4, end5]
 ArrayList < String[] > list = new ArrayList < > ([])
 for (int i = 0; i < 4; i++) {
  if (devs[i] != null) {
   list.add([devs[i], ends[i]])
  }
 }
 return list
}

/**
*actions to be taken once the device is installed
*/
def installed() {
 log.info(device.name + " installed!!!")
 state.on = false
 state.lastAction = 0
 state.brightness = 0
 state.buttonNumber = 1
 state.value = "unknown"
 state.lastHeld = "none"
 state.battery = 100
 state.dimming = false
 reportOnState(getOnState())
}

/**
*Parse events into attributes.
*/
def parse(String msgFromST) {https://graph-na02-useast1.api.smartthings.com/ide/device/editor/2ef74e51-ce01-44d1-8b1a-8d08c55f44e8#
 if (msgFromST?.startsWith('catchall:')) {
  def value = handleMessage(msgFromST)
  fireCommands(value.command)
 } else if (msgFromST.startsWith("read")) {
  if (msgFromST.contains("attrId: 0020,")) return batteryHandler(zigbee.parseDescriptionAsMap(msgFromST))
  log.error('unrecognized command:' + msgFromST)
 } else {
  log.error('unrecognized command:' + msgFromST)
 }
 return getStatus()
}


/**
*fire commands into the hub
*commands - an array of string commands to be fired
*/
private fireCommands(List commands) {


  if (commands != null && commands.size() > 0) {
  log.trace("Executing commands-- state:" + state)
  for (String value : commands){
     log.trace("Executing commands: " + value )
   sendHubCommand([value].collect {new physicalgraph.device.HubAction(it)
   
  
  })
  }
 }
}


Map handleMessage(String msgFromST) {
 Map msg = zigbee.parseDescriptionAsMap(msgFromST)
 switch (Integer.parseInt(msg.clusterId)) {
  case 1: //battery message              WHY IS THIS HERE AND IN THE PARSE FUNCTION?
   def returnval = batteryHandler(msg)
   return returnval
   break
  case 6: //button press
   def returnval = handleButtonPress(msg)
   updateButtonState("pressed")
   return returnval
   break
  case 8:
   handleButtonHeld(msg)
   break
  default:
   log.error("Unhandled message: " + msg)
   break
 }
}

/**
* This is the handler for button presses.  Map is routed here once a button press has been detected
*/
def Map handleButtonPress(Map msg) {
 switch (msg.command) {
  case "01":
   def returnval = on()
   return returnval
   break
  case "03":
   bothButtonsPressed()
   break
  case "00":
   def returnval = off()
   return returnval
   break
  default:
   log.error(getLinkText(device)+" got unknown button press command: " + msg.command)
   return "error"
   break
 }
}

/**
*this is the handler for button held events. Map is routed here once a button held event has been detected
*/
def Map handleButtonHeld(Map msg) {
 switch (Integer.parseInt(msg.command)) {
  case 1:
   log.debug("Button held- Lowering brightness commanded")
   state.dimming = true
   updateButtonState("down held")
   state.lastHeld = "down"
   return adjustBrightness(false, state.brightness)
   break
  case 3:
   log.debug("stop brightness commanded")
   updateButtonState(state.lastHeld + " released")
   state.dimming = false
   return state
   break
  case 5:
   log.debug("Button held- raising brightness commanded")
   state.lastHeld = "up"
   updateButtonState("up held")
   state.dimming = true
   return adjustBrightness(true, state.brightness)
   break
  default:
   log.error("Unhandled message: " + msg)
   break
 }
 return msg
}



/**
 * adjusts brightness up/down depending on the value of the up boolean true is up, false is down
 * continues to adjust until state.dimming is changed
 * up- true if we are adjusting brightness up, false if down
 * level - first level commanded
 */
def Map adjustBrightness(final boolean up, double level) {
 Map result
 log.debug("adjusting brightness" + (up?"up" : "down") + " from current " + state.brightness)
 if (state.dimming) {
  //increase or decrease brightness
  if (up) {
   state.brightening = true
  } else {
   state.brightening = false
  }
  state.brightness = (int) level
  executeBrightnessAdjustmentUntilButtonReleased()
 } else {
  log.debug("Final brightness adjusted to " + state.brightness)
 }

 sendEvent(name: "brightness", value: state.brightness)
 return getStatus()
}

/**
 * performs a recursive brightness adjustment based on state.brightening while state.dimming is true
 */
def executeBrightnessAdjustmentUntilButtonReleased() {
 if (state.dimming) {
  if (state.brightening) {
   state.brightness = state.brightness + 20
  } else {
   state.brightness = state.brightness - 20
  }
  if (state.brightness > 100) state.brightness = 100
  if (state.brightness < 1) state.brightness = 1
  setLevel((double) state.brightness, 1000)
  reportOnState(true) //Manage and report states
  runIn(1, executeBrightnessAdjustmentUntilButtonReleased)
 }
}


/**
* gets the current brightness in hex format
* length - the length of the number after leading 0's have been applied.  This will likely be "2"
*/
String getBrightnessHex(int length) {
 StringBuilder sb = new StringBuilder(Integer.toHexString((Integer) Math.round(state.brightness * 2.55)))
 if (sb.size() > length) return sb.toString()
 for (int i = sb.size(); i < length; i++) {
  sb.insert(0, '0')
 }
 return sb.toString()
}

/**
*formats a decimal value with leading 0's
*value - the value to be formatted
*length - the size of the number after leading 0's have been added
*/ 
String formatNumber(int value, int length) {
 return String.format("%" + length + "d", value);
}

/**
*performs configuration and bindings
*/
def configure() {
 log.debug "Configuring Reporting and Bindings."
 installed()
 def configCmds = [
  // Bind the outgoing on/off cluster from remote to hub, so the hub receives messages when On/Off buttons pushed
  "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}",
  // Bind the outgoing level cluster from remote to hub, so the hub receives messages when Dim Up/Down buttons pushed
  "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0008 {${device.zigbeeId}} {}",

  // Bind the incoming battery info cluster from remote to hub, so the hub receives battery updates
  "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0001 {${device.zigbeeId}} {}",
 ]
 return configCmds
}



/**
*handles battery messages
*/
private Map batteryHandler(Map rawValue) {
  int value = Integer.valueOf(rawValue.value, 16)
  def linkText = getLinkText(device)
  def result = [ name: 'battery', value: state.battery ]
  def volts = Integer.valueOf(rawValue.value, 16) / 10
  def descriptionText
  if (rawValue == 0) {
   state.battery = "unknown"
  } else {
   if (volts > 3.5) {
    result.descriptionText = "${linkText} battery has too much power (${volts} volts)."
    state.battery = "overvoltage"
   } else if (volts > 0) {
    def minVolts = 2.1
    def maxVolts = 3.0
    def pct = (volts - minVolts) / (maxVolts - minVolts)
    state.battery = Math.min(100, (int) pct * 100)
    result.battery = state.battery
    result.descriptionText = "${linkText} battery was ${result.value}%"
   }
  }
  sendEvent(name: 'battery', value: state.battery, units: "%")
  log.debug "${result?.descriptionText}"
  return result
 }
 
 
 /**
  * handle level commands,
  * level=desired level
  * duration=desired time-to-level
  */
def setLevel(Double level, Double duration) {
 if (level > 100 || level < 0 || duration < 0 || duration > 9999) {
  log.debug("Maximum parameters (level 0-100, duration 0-9999)-- commanded level:" + level + " commanded duration:" + duration)
  return
 }
 log.info("Brightness commanded to " + level + "%")
 state.brightness = (int) level
 def result = createStCommand(" 8 4 {" + getBrightnessHex(2) + " " + formatNumber((int) duration, 4) + "}")
 if (state.on != "on") on()
 fireCommands(result.command) //send it to the hub for processing
 reportOnState(true)
}

/**
*sets level using a 1-second duration
*level= percent
*/
def setLevel(Double level) {
 setLevel(level, 1000)
}

/**
*Refresh support.  Causes battery status update and others
*/ 
def refresh() {
 log.debug("Executing Refresh")
 def refreshCmds = [
  zigbee.readAttribute(0x0001, 0x0020)
 ]
 reportOnState(getOnState())
 //when refresh button is pushed, read updated status
 return refreshCmds
}

/**
* Returns true if the switch is on.  false if off.
*/
Boolean getOnState(){
    return (state.on == "on")
}

/**
*poll support, forces an update of states
*/
def poll() {
 sendEvent(name: 'brightness', value: state.brightness, units: "%")
 reportOnState(getOnState())
}

/**
*takes a command and data, generates an "st cmd" array 
*command- string representing the command and data to be send to the device
*returns the command for all devices on the switch
*/
def Map createStCommand(String command) {
 List<String> output = []
 LinkedHashMap result = getStatus()
 for (item in getDevices()) {
  output.add("st cmd 0x" + item[0] + " 0x" + item[1] + " " + command)
 }
 result['command'] = (List)output.flatten()
 return result
}

/**
 * Handles event updates.  All updates go here. 
 */
def reportOnState(boolean on) {
 String onValue = (on ? "on" : "off")
 sendEvent(name: 'state', type:"thing", value:onValue)
 sendEvent(name: 'battery', value: state.battery)
 sendEvent(name: 'level', value: state.brightness)
 sendEvent(name: 'button', value: state.value)
 sendEvent(name: 'numberOfButtons', value: 5)
 sendEvent(name: 'State Array', value: state)
 sendEvent(name: 'Awesomeness Level', value: "over 9000")
 
 state.on = onValue
}

def updateButtonState(def value) {
 state.value = value
 sendEvent(name: "button", value: value, unit: "")
}

/**
*commands the opposite of the current state
*if on, turn off, if off, turn on.
*/
def toggle() {
 Map command
 if (state.on == "on") {
  log.debug(device.displayName + " toggled on")
  command = off()
 } else {
  command = on()
  log.debug(device.displayName + " toggled off")
 }
 log.debug(command)
 fireCommands(command.command)
}

/**
*detects the current state versus commanded
*if curret is off, and commanded is off, returns true and same for inverse
*/
boolean doubleTapped(boolean commanded) {
 boolean on=(state.on=="on")
 if (on && commanded|| !on && !commanded) return true
 return false
}

/**
*turns light on. 
*if already on, commands max
*/
Map on() {
 log.debug(device.displayName + " commanded on")
 if (doubleTapped(true)) setLevel(100, 1000)
 reportOnState(true)
 return createStCommand("6 1 {}")
}

/**
*turns light off
*if already off, commands on to minimum
*/
Map off() {
 log.debug(device.displayName + " commanded off")
 if (doubleTapped(false)){
    def value=on()
    setLevel(1, 1000)
    return value
 }
 reportOnState(false)
 return createStCommand("6 0 {}")
}


/**
*Action to be taken when both buttons are pressed at the same time
*/
def bothButtonsPressed() {
 log.error("Both Buttons Pressed" + state)
 state.lastAction = 100
 configure()
 return getStatus()
}
