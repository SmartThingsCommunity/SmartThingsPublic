/* 
A very simple Blink System Manager using IFTTT
**************************** DISCLAIMER ****************************
THIS DEVICE HANDLER CAN NOT GUARANTEE PERFORMANCE WITH THE BLINK
SENSOR.  NO GUARANTEE OF PERFORMANCE FOR ANY USAGE IS PROVIDED.
**************************** DISCLAIMER ****************************
**DISCLAIMER**
Licensed under the Apache License, Version 2.0 (the "License"); you 
may not use this  file except in compliance with the License. You may 
obtain a copy of the License at:
		http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
implied. See the License for the specific language governing 
permissions and limitations under the License.
DESCRIPTION:
This handler is designed to arm and disarm the Blink System using
IFTTT triggers generated when a device turns on or off.  It requires
two IFTTT Channels be set up using this device and the Blink Camera:
1.	Arm the Blink System when this device is turned on in SmartThings.
2.	Disarm the Blink System when this device is turned off in SmartThings.
ISSUE:
NO FEEDBACK.  This handler does not receive feedback on the actual blink system state.
The only Blink Camera trigger is if a camera detects motion.
I addressed this by sending the on state every five minutes using the
Refresh capability.  Since I use "isStateChage: true", this makes this
system a bother with apps like Action Tiles.
*/ 

metadata {
    definition (name: "Blink System through IFTTT", namespace: "djg", author: "djgutheinz") {
        capability "Switch"
        capability "Refresh"
        capability "Sensor"
    }

	tiles(scale: 2) {
        standardTile("switch", "device.switch", width: 4, height: 2, canChangeIcon: true, decoration: "flat") {
            state "off", label: "Disarmed", action: "switch.on", backgroundColor: "#ffffff"
            state "on", label: "Armed", action: "switch.off", backgroundColor: "#00a0dc"
	}
/*
*/
		standardTile("refresh", "device.switch", width: 2, height: 2,  decoration: "flat") {
			state ("default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh")
		}

 		main "switch"
		details(["switch", "refresh"])
	}
}

def initialize() {
	updated()
}

def updated() {
	runEvery5Minutes(refresh)
}

def on() {
//	Arm blink by setting the state of the Blink System
//	This will cause the defined IFTTT action to trigger.
	log.info "${device.name} has been armed."
	sendEvent(name: "switch", value: "on")
	log.info "${device.label} set to ${device.currentValue("switch")} and is Armed"
//    runIn(30, on)
}

def off() {
//	Disarm blink by setting the state of the Blink System
//	This will cause the defined IFTTT action to trigger.
	sendEvent(name: "switch", value: "off")
	log.info "${device.label} set to ${device.currentValue("switch")} and is disarmed"
//    runIn(30, off)
}

def refresh() {
//	Re-affirm the current state with Blink (through IFTTT), just in case
//	it did not work the first time.
	def blinkState = device.currentValue("switch")
	sendEvent(name: "switch", value: "${blinkState}")
	log.info "${device.label} state reset to ${device.currentValue("switch")}"
}