/**
 *
 *	Copyright 2019 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 */

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "Rooms Beautiful Curtain", namespace: "Rooms Beautiful", author: "Alex Feng", ocfDeviceType: "oic.d.blind", mnmn: "SmartThings", vid: "generic-shade-4") {
		capability "Actuator"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Window Shade"
		capability "Health Check"

		attribute("replay", "enum")
		attribute("battLife", "enum")

		//command "pause"
		command "cont"

		fingerprint profileId: "0104", inClusters: "0000, 0001, 0003, 0006, FC00, DC00, 0102,", deviceJoinName: "Curtain", manufacturer: "Rooms Beautiful",  model: "C001"
	}

	preferences {
		input name: "invert", type: "bool", title: "Invert Direction", description: "Invert Curtain Direction", defaultValue: false, displayDuringSetup: false, required: true
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"windowShade", type: "generic", width: 6, height: 4) {
			tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
				attributeState "open", label: 'Open', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "closing"
				attributeState "closed", label: 'Closed', action: "open", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "opening"
				attributeState "opening", label: 'Opening', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "opening"
				attributeState "closing", label: 'Closing', action: "open", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "closing"
			}
			tileAttribute ("device.battLife", key: "SECONDARY_CONTROL") {
				attributeState "full", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/full.png"
				attributeState "medium", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/medium.png"
				attributeState "low", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/low.png"
				attributeState "dead", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/dead.png"
			}
		}
		standardTile("contPause", "device.replay", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "pause", label: "Pause", icon:'https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/pause.png', action:'pause', backgroundColor:"#e86d13", nextState: "cont"
			state "cont", label: "Cont.", icon:'https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/play.png', action:'cont', backgroundColor:"#90d2a7", nextState: "pause"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "windowShade"
		details(["windowShade", "contPause", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	// FYI = event.name refers to attribute name & not the tile's name

	def linkText = getLinkText(device)
	def event = zigbee.getEvent(description)
	def descMap = zigbee.parseDescriptionAsMap(description)
	def value
	def attrId

	if (event) {
		if(!descMap.attrId){
			sendEvent(name: "replay", value: "pause")
			//log.warn "${linkText} - Replay set to: PAUSE"
		}

		sendEvent(event)
		log.debug "${linkText} - On/Off: ${event.value}"
		if(event.name == "switch" || event.name == "windowShade"){
			if(event.value == "on"){
				sendEvent(name: "windowShade", value: "open")
			} else {
				sendEvent(name: "windowShade", value: "closed")
			}
		}
	}
	else {
		if(descMap.attrId) {
			if(descMap.clusterInt != 0xDC00){
				value = Integer.parseInt(descMap.value, 16)
				attrId = Integer.parseInt(descMap.attrId, 16)
			}
		}

		switch(descMap.clusterInt) {
			case 0x0001:
				if(attrId == 0x0020)
					handleBatteryEvent(value)
				break;
			case 0x0102:
				log.debug "${linkText} - Replay: ${device.currentState("replay").value}"
				break;
			case 0xFC00:
				if (description?.startsWith('read attr -'))
					log.info "${linkText} - Inverted: ${value}"
				else
					log.debug "${linkText} - Inverted set to: ${invert}"
				break;
			case 0xDC00:
				value = descMap.value
				def shortAddr = value.substring(4)
				def lqi = zigbee.convertHexToInt(value.substring(2, 4))
				def rssi = (byte)zigbee.convertHexToInt(value.substring(0, 2))
				log.info "${linkText} - Parent Addr: ${shortAddr} **** LQI: ${lqi} **** RSSI: ${rssi}"
				break;
			default:
				log.warn "${linkText} - DID NOT PARSE MESSAGE for description: $description"
				log.debug descMap
				break;
		}
	}
}

def close() {
	zigbee.off()
}

def open() {
	zigbee.on()
}

def pause() {
	zigbee.command(0x0102, 0x02) +
	sendEvent(name: "replay", value: "cont")
}

def cont() {
	zigbee.command(0x0102, 0x02) +
	sendEvent(name: "replay", value: "pause")
}

private handleBatteryEvent(volts) {
	def linkText = getLinkText(device)

	//log.warn "Value of adc: ${volts}"

	if (volts > 30 || volts < 20) {
		log.debug "${linkText} - Ignoring invalid value for voltage (${volts/10}V)"
	}
	else {
		def batteryMap = [30:"full", 29:"full", 28:"full", 27:"medium", 26:"low", 25:"dead"]

		def value = batteryMap[volts]
		if(value != null){
			def minVolts = 25
			def maxVolts = 30
			def pct = (volts - minVolts) / (maxVolts - minVolts)
			def roundedPct = Math.round(pct * 100)
			def percent = Math.min(100, roundedPct)

			sendEvent(name: "battery", value: percent)
			sendEvent(name: "battLife", value: value)
			log.debug "${linkText} - Batt: ${value} **** Volts: ${volts/10}v **** Percent: ${percent}%"
		}
	}
}

def refresh() {
	zigbee.onOffRefresh() +
	zigbee.readAttribute(0x0001, 0x0020) +

	// For Diagnostics
	zigbee.readAttribute(0xFC00, 0x0000) +
	zigbee.readAttribute(0xDC00, 0x0000)
}

def ping() {
	return refresh()
}

// Don't do Device-Watch to prevent 20-30 min read attribute
def configure() {
	return refresh()
}

def installed() {
	sendEvent(name: "supportedWindowShadeCommands", value: JsonOutput.toJson(["open", "close", "pause"]), displayed: true)
	sendEvent(name: "battery", value: 100)
	sendEvent(name: "battLife", value: "full")
	response(refresh())
}

/*def updated() {
	// Needed because updated() is being called twice
	def time
	if(state.updatedDate == null){
		time = now()
	}
	else{
		time = now() - state.updatedDate
	}
	state.updatedDate = now()
	
	log.trace ("Time: ${time}")
	
	//	Updated [Tested on 12/27/18] - Don't need if statement
	//	Doesn't occur twice anymore
	
	//if (time < 100 ){	// Smaller value (e.g. 100) means less likely to occur twice
		if (invert.value == false)
			response(normal())
		else if(invert.value == true)
			response(reverse())
	//}
}*/

def updated() {
	runIn(2, "finishConfiguration", [overwrite: true])
}

def finishConfiguration() {
	if (invert.value == false)
		response(normal())
	else if(invert.value == true)
		response(reverse())
}

def normal() {
	if(device.currentState("windowShade").value == "open"){
		sendEvent(name: "windowShade", value: "closed")
		log.warn ("normal-close")
		zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x00)
	}
	else{
		sendEvent(name: "windowShade", value: "open")
		log.warn ("normal-open")
		zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x00)
	}
}

def reverse() {
	if(device.currentState("windowShade").value == "open"){
		sendEvent(name: "windowShade", value: "closed")
		log.warn ("reverse-close")
		zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x01)
	}
	else{
		sendEvent(name: "windowShade", value: "open")
		log.warn ("reverse-open")
		zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x01)
	}
}
