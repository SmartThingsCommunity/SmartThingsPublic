/**
*  OSRAM Lightify Dimming Switch with Brightness Adjustment
*
*  Copyright 2016 Smartthings Comminuty
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*  Thanks to Michael Hudson for OSRAM Lightify Dimming Switch device.
*  Also borrowed pieces from Virtual Dimmer and GE/Jasco Dimmer
*  Thanks nsweet68 & adamoutler for their work
* 
*/

// Define device
metadata {
	definition (name: "OSRAM Lightify Dimmer with Brightness Control", namespace: "claytonraymond2004", author: "claytonraymond2004@gmail.com") {
		capability "Switch"
		capability "Switch Level"
		capability "Configuration"
		capability "Refresh"
		capability "Battery"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'On', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'Off', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'Turning On', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'Turning Off', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label:'${currentValue}% Battery'
        }
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "switch"
		details(["switch", "level", "battery","levelSliderControl","refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {

  Map map = [:]
  log.debug "parse description: $description"
  if (description?.startsWith('catchall:')) {
    // call parseCatchAllMessage to parse the catchall message received
    map = parseCatchAllMessage(description)
  } else if (description?.startsWith('read')) {
    // call parseReadMessage to parse the read message received
    map = parseReadMessage(description)
  } else {
    log.debug "Unknown message received: $description"
  }
  //return event unless map is not set
  return map 
}

// Configure device initially
def configure() {
  log.debug "Confuguring Reporting and Bindings."
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

// Refresh Battery State
def refresh() {
  sendEvent(name: 'battery', value: state.battery)
  def refreshCmds = [
    zigbee.readAttribute(0x0001, 0x0020)
  ]
  //when refresh button is pushed, read updated status
  return refreshCmds
}

// Reads battery values
private Map parseReadMessage(String description) {
  // Create a map from the message description to make parsing more intuitive
  def msg = zigbee.parseDescriptionAsMap(description)
  //def msg = zigbee.parse(description)
  if (msg.clusterInt==1 && msg.attrInt==32) {
    // call getBatteryResult method to parse battery message into event map
    def result = getBatteryResult(Integer.parseInt(msg.value, 16))
  } else {
    log.debug "Unknown read message received, parsed message: $msg"
  }
  // return map used to create event
  return result
}

// Converts battery message into event map
private Map getBatteryResult(rawValue) {
  def linkText = getLinkText(device)
  def result = [
    name: 'battery',
    value: state.battery
  ]
  def volts = rawValue / 10
  def descriptionText
  if (rawValue == 0) {
     state.battery="unknown"
  } else {
    if (volts > 3.5) {
      result.descriptionText = "${linkText} battery has too much power (${volts} volts)."
      state.battery="overvoltage"
    } else if (volts > 0){
      def minVolts = 2.1
      def maxVolts = 3.0
      def pct = (volts - minVolts) / (maxVolts - minVolts)
      result.value = Math.min(100, (int) pct * 100)
      state.battery="${result.value}"
      result.descriptionText = "${linkText} battery was ${result.value}%"
    }
  }
  log.debug "Parse returned ${result?.descriptionText}"
  return result
}

// Parse a catchall message from the switch
private Map parseCatchAllMessage(String description) {
  // Parse message 
  Map msg = zigbee.parseDescriptionAsMap(description)
  log.debug "Parse CatchAll $msg"
  
  switch(msg.clusterInt) {
  	// 1 is if it's a battery update
    case 1:
    	log.info 'Battery Update'
    	def result = getBatteryResult(Integer.parseInt(msg.value, 16))
    	break
    // 6 is if a button is pressed
    case 6:
        log.info 'Button Press'
    	handleButtonPress(msg)
    	break
    // 8 is if a button is held
    case 8:
    	log.info 'Button Hold'
        handleButtonHeld(msg)
        break
  }
}

// Handles a pressed button
def handleButtonPress(Map msg) {
    if (msg.commandInt == 1) {
        on()
    } else {
        off()
    }
}

// Turns on switch
def on() {
	log.info "Switch On"
	sendEvent(name: "switch", value: "on")
    if (state.dimmer < 1) {
    	setLevel(1)
    }
}

// Turns off switch
def off() {
	log.info "Switch Off"
	sendEvent(name: "switch", value: "off")
}

// Sets level of switch
def setLevel(val) {
	// Check that val is within acceptable ranges
    if (val < 0){
    	val = 0
    }
    if( val > 100){
    	val = 100
    }
    log.info "Setting level to $val"
    state.dimmer = val
    
    // Turn off or on as needed and adjust level
    if (val == 0){
    	off()
    	sendEvent(name:"level",value:val)
    } else {
    	on()
    	sendEvent(name:"level",value:val)
    	sendEvent(name:"switch.setLevel",value:val) // had to add this to work if apps subscribed to setLevel event. "Dim With Me" was one.
    }
}

// Handles a held button
def handleButtonHeld(Map msg) {
     switch (Integer.parseInt(msg.command)) {
         case 1:
            log.debug("Button held: Lowering brightness")
            state.dimming = true
            state.lastHeld = "down"
            return adjustBrightness(false, state.dimmer)
            break
         case 3:
            log.debug("Button Released: Stop adjusting brightness")
            state.dimming = false
            return state
            break
         case 5:
            log.debug("Button held: Raising brightness")
            state.lastHeld = "up"
            state.dimming = true
            return adjustBrightness(true, state.brightness)
            break
         default:
            log.error("Unhandled button held event: " + msg)
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
void adjustBrightness(final boolean up, double level) {
     log.debug("Adjusting brightness " + (up ? "up" : "down") + " from current " + state.brightness)
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
}

/**
 * performs a recursive brightness adjustment based on state.brightening while state.dimming is true
 */
void executeBrightnessAdjustmentUntilButtonReleased() {
    if (state.dimming) {
        if (state.brightening) {
            state.brightness = state.brightness + 20
        } else {
            state.brightness = state.brightness - 20
        }
        if (state.brightness > 100) {
            state.brightness = 100
            state.dimming = false
        }
        if (state.brightness < 1){
            state.brightness = 1
            state.dimming = false
        }
        setLevel(state.brightness)
        runIn(1, executeBrightnessAdjustmentUntilButtonReleased)
    }
}