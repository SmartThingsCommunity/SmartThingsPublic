/**
 *  Z-Wave Garage Door Opener
 *
 *  Copyright 2014 SmartThings
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
 */
metadata {
	definition (name: "Telguard GDC1", namespace: "fortrezz", author: "FortrezZ, LLC") {
		capability "Actuator"
		capability "Door Control"
		capability "Garage Door Control"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Sensor"
        capability "Switch"

        fingerprint deviceId: "0x1000", inClusters: "0x72, 0x86, 0x25"
	}

	simulator {
		status "closed": "command: 9881, payload: 00 66 03 00"
		status "opening": "command: 9881, payload: 00 66 03 FE"
		status "open": "command: 9881, payload: 00 66 03 FF"
		status "closing": "command: 9881, payload: 00 66 03 FC"
		status "unknown": "command: 9881, payload: 00 66 03 FD"

		reply "988100660100": "command: 9881, payload: 00 66 03 FC"
		reply "9881006601FF": "command: 9881, payload: 00 66 03 FE"
	}

	tiles {
		standardTile("toggle", "device.door", width: 2, height: 2) {
			state("unknown", label:'${name}', action:"refresh.refresh", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', action:"door control.open", icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821", nextState:"opening")
			state("open", label:'${name}', action:"door control.close", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e", nextState:"closing")
			state("opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e")
			state("closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e")
		}
		standardTile("open", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'open', action:"door control.open", icon:"st.doors.garage.garage-opening"
		}
		standardTile("close", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'close', action:"door control.close", icon:"st.doors.garage.garage-closing"
		}
		standardTile("refresh", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.off", backgroundColor:"#79b821", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.off", backgroundColor:"#79b821", nextState:"turningOff"
            state "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
            state "offline", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ff0000"
        }

		main "toggle"
		details(["toggle", "open", "close", "refresh"])
	}
}

def parse(String description) {
	def results = []
    log.debug(description)
	if (description.startsWith("Err")) {
	    results << createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			results = zwaveEvent(cmd)
		}
	}
	log.debug "\"$description\" parsed to ${results.inspect()}"
	return results
}

def open() {
    sendEvent(name: "door", value: "opening", descriptionText: "$device.displayName: opening")
    delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.basicV1.basicGet().format()
    ], 15000)
}

def close() {
    sendEvent(name: "door", value: "closing", descriptionText: "$device.displayName: closing")
	zwave.basicV1.basicSet(value: 0x00).format()
}

def on() {
	open()
}

def off() {
	close()
}

def refresh() {
	zwave.basicV1.basicGet().format()
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	def map = [name: "door"]
    def map2 = [name: "switch"]
    if (cmd.value == 0xFF)
    {
    	map.value = "open"
        map2.value = "on"
        sendEvent(map2)
    }
    else if (cmd.value == 0x00)
    {
    	map.value = "closed"
        map2.value = "off"
        sendEvent(map2)
    }
    else
    {
    	map.value = "unknown"
    }
	
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def map = [name: "door"]
    def map2 = [name: "switch"]
    if (cmd.value == 0xFF)
    {
    	map.value = "open"
        map2.value = "on"
        sendEvent(map2)
    }
    else if (cmd.value == 0x00)
    {
    	map.value = "closed"
        map2.value = "off"
        sendEvent(map2)
    }
    else
    {
    	map.value = "unknown"
    }
	
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	def text = "$device.displayName: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
	createEvent(descriptionText: text, isStateChange: false)
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
	def msg = cmd.status == 0 ? "try again later" :
	          cmd.status == 1 ? "try again in $cmd.waitTime seconds" :
	          cmd.status == 2 ? "request queued" : "sorry"
	createEvent(displayed: true, descriptionText: "$device.displayName is busy, $msg")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(displayed: false, descriptionText: "$device.displayName: $cmd")
}