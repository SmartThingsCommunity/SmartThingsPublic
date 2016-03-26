/**
 *  Nexia Doorbell Sensor
 *
 *  Copyright 2016 Darwin (DarwinsDen.com)
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
 *	Author: Darwin (DarwinsDen.com)
 *	Date: 2016-03-20
 *
 *	Changelog:
 *
 *	0.1 (03/20/2016)
 *		-	Initial Release
 *	0.2 (03/22/2016)
 *		-	handle missed button press and release events
 *	0.3 (03/25/2016)
 *		-	Reset 10 second fail timer on successful button release. Call createEvent vs sendEvent in parse
 *
 */
 
metadata {
	definition (name: "Nexia Doorbell Sensor", namespace: "darwinsden", author: "Darwin") {
		capability "Switch"
        capability "Momentary"
		capability "Battery"
		capability "Refresh"
		attribute "status", "enum", ["off", "doorbell"]
        
		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x86, 0x72, 0x5A, 0x73, 0x80, 0x70, 0x71, 0x85, 0x59, 0x84, 0x7A"
	}

	simulator {
	}

	tiles(scale: 2) {
    	multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
				tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
					attributeState "off", label: "Off", icon:"st.Home.home30", backgroundColor:"#ffffff"
					attributeState "doorbell", label: "Ringing", icon:"st.Home.home30", backgroundColor:"#53a7c0"
				}
        }

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "default", label:'${currentValue}% battery', unit:""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "status"
		details(["status", "battery", "refresh"])
	}
}

def refresh() {
	  sendEvent(name: "status", value: "off", displayed: false, isStateChange: true)
	  sendEvent(name: "switch", value: "off", displayed: false, isStateChange: true)
 }

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
      return result += createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
}

// Unexpected command received
def zwaveEvent (physicalgraph.zwave.Command cmd) {
	  log.debug("Unhandled Command: $cmd")
	  return createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def turnOffSwitch() {
	  sendEvent(name: "status", value: "off", displayed: false, isStateChange: true)
	  sendEvent(name: "switch", value: "off", displayed: false, isStateChange: true)
}

//Battery Level received
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
      def result = null
      state.lastBatteryReport = new Date().time
      def batteryLevel = cmd.batteryLevel
      log.info("Battery: $batteryLevel")
      result = createEvent(name: "battery", value: batteryLevel, unit: "%", descriptionText: "Battery%: $batteryLevel", isStateChange: true)   
      return result
}

//Notification received
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    def notificationEvent = cmd.event
    def result = []
    
    //Set switch "on" if its a button press, or if it's a button release and the button hasn't been
    //pressed in the last 10 seconds (likely a missed press or messages are out of order)
    
    if (notificationEvent == 1 || (!state.lastBellRing  || (new Date().time) - state.lastBellRing  > 10*1000)) {
	  log.debug("Button is pressed")
	  result += createEvent(name: "status", value: "doorbell", descriptionText: "Button pressed", isStateChange: true)
	  result += createEvent(name: "switch", value: "on", displayed: false, isStateChange: true)
      result += createEvent(name: "momentary", value: "pushed", displayed: false, isStateChange: true)
      state.lastBellRing = new Date().time      
      runIn(10, turnOffSwitch) //turn off switch in 10 seconds in case button release event is missed   
    } else // it's a button release and it's within 10 seconds of the button press
    {
      log.debug("Button is released")
      result += createEvent(name: "status", value: "off", descriptionText: "Button released", isStateChange: true)
	  result += createEvent(name: "switch", value: "off", displayed: false, isStateChange: true)
      state.lastBellRing = null
   } 
   return result
}

def parse(description) {
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText: description, isStateChange: true)
	} else if (description != "updated") {

        def cmd = zwave.parse(description, [0x71: 3, 0x80: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    log.debug ("Security Encapsulation")
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 1])
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}
