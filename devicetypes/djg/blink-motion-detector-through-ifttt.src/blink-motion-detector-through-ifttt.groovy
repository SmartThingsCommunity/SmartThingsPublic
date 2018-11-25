/* 
A very simple motion alarm using IFTTT and the Blink Camera System
**************************** DISCLAIMER ****************************
THIS DEVICE HANDLER CAN NOT GUARANTEE PERFORMANCE WITH THE BLINK
SENSOR.  NO GUARANTEE OF PERFORMANCE FOR ANY USAGE IS PROVIDED.
**************************** DISCLAIMER ****************************
**DISCLAIMER**
Copyright 2017 Dave Gutheinz
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
This handler is designed to alert SmartThings that one or many Blink
Cameras have detected motion (thusly setting the proper state in
SmartThings for alarms, etc.)  To work, an IFTTT Channel must be created
between SmartThings and Blink that turns ON the camera device when
motion is detected.  You must create a separate channel for each
blink camera.
Additionally, you could install this device multiple time (one for each
camera) or a single time (for an aggragate alert).
After the alarm is set, the camera is system is reset in 60 seconds
to off.  You will still have to clear the security alert in the sytem.
Note:  Refresh intentionally left off.
*/ 

metadata {
    definition (name: "Blink Motion Detector through IFTTT", namespace: "djg", author: "djgutheinz") {
        capability "Switch"
        capability "Refresh"
        capability "Motion Sensor"
        capability "Sensor"
    }

	tiles(scale: 2) {
		standardTile("switch", "switch", width: 6, height: 4, canChangeIcon: true){
			state "off", label:'OK', backgroundColor: "#ffffff"
			state "on", label: 'Motion', backgroundColor: "#e86d13", action: "off"
		}
 		main("switch")
		details(["switch"])
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
//	Use the switch capability to turn "on" the motion alert.
//	Triggered by the IFTTT Action
	log.info "${device.label} has detected motion."
	sendEvent(name: "switch", value: "on")
    sendEvent(name: "motion", value: "active")
    runIn(60, off)
}

def off() {
//	Turns off the alert (operator only) and set flag to no
//	motion
	log.info "${device.label} motion flag has been cleared."
	sendEvent(name: "switch", value: "off")
    sendEvent(name: "motion", value: "inactive")
}