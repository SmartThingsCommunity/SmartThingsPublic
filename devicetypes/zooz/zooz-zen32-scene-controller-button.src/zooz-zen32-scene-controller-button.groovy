/*
*  Zooz ZEN32 Scene Controller Button
*
*  Changelog:
*
*    2022-03-17
*      - Requested changes
*    2022-02-27
*      - Publication Release
*
*  Copyright 2022 Zooz
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*/

metadata {
	definition (
		name: "Zooz ZEN32 Scene Controller Button",
		namespace: "Zooz",
		author: "Kevin LaFramboise (krlaframboise)",
		ocfDeviceType: "x.com.st.d.remotecontroller",
		mnmn: "SmartThingsCommunity",
		vid: "63601248-c681-3458-b5d6-ab1f482b2d71"
	) {
		capability "Sensor"
		capability "Button"
		capability "Refresh"
		capability "platemusic11009.zoozLedColor"
		capability "platemusic11009.zoozLedBrightness"
		capability "platemusic11009.zoozLedMode"
	}

	preferences() {}
}

def parse(String description) {
	log.debug "parse(${description})..."
	return []
}

def installed() {
	log.debug "installed()..."
	initialize()
}

def updated() {
	log.debug "updated().."
	initialize()
}

void initialize() {
	if (!device.currentValue("numberOfButtons")) {
		sendEvent(name: "numberOfButtons", value: 1)
		sendEvent(name: "supportedButtonValues", value: ["pushed", "held", "pushed_2x", "pushed_3x", "pushed_4x", "pushed_5x"].encodeAsJSON())
		sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1])
		sendEvent(name: "ledMode", value: "onWhenOff")
		sendEvent(name: "ledBrightness", value: "medium")
		sendEvent(name: "ledColor", value: "white")
	}
}

def refresh() {
	log.debug "refresh()..."
	parent.childRefresh(device.deviceNetworkId)
}

def setLedMode(mode) {
	log.debug "setLedMode(${mode})..."
	parent.childSetLedMode(device.deviceNetworkId, mode)
}

def setLedColor(color) {
	log.debug "setLedColor(${color})..."
	parent.childSetLedColor(device.deviceNetworkId, color)
}

def setLedBrightness(brightness) {
	log.debug "setLedBrightness(${brightness})..."
	parent.childSetLedBrightness(device.deviceNetworkId, brightness)
}