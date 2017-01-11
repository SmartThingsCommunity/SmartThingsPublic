/**
 *  Copyright 2016 Ryan Finnie
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
	definition (name: "Z-Wave Repeater", namespace: "rfinnie", author: "Ryan Finnie") {
		capability "Polling"
		capability "Refresh"
		capability "Health Check"

		fingerprint mfr:"0086", prod:"0004", model:"0025", deviceJoinName: "Aeon Labs DSD37-ZWUS Repeater"
		fingerprint mfr:"0086", prod:"0104", model:"0075", deviceJoinName: "Aeotec Range Extender 6"
		fingerprint mfr:"0109", prod:"2012", model:"1203", deviceJoinName: "Vision Security Repeater ZR1202US"
		fingerprint mfr:"0109", prod:"2012", model:"1206", deviceJoinName: "Vision Z-Wave Repeater"
		fingerprint mfr:"0246", prod:"0001", model:"0001", deviceJoinName: "Iris Smart Plug Z-Wave Repeater"
		fingerprint mfr:"5254", prod:"1000", model:"8140", deviceJoinName: "Remotec ZRP-100US / BW8140US Z-Wave Repeater"
	}

	tiles(scale: 2) {
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 6, height: 4) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "refresh"
		details(["refresh"])
	}
}

def updated(){
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def parse(String description) {
	log.debug "description is $description"
	def result = []
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else {
		def cmd = zwave.parse(description, [0x86: 1, 0x72: 2, 0x73: 1])
		if (cmd) {
			result += zwaveEvent(cmd)
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	def protocol = "${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
	def library = "${cmd.zWaveLibraryType}"
	log.debug "Firmware: $fw - Protocol: $protocol - Library: $library"
	updateDataValue("fw", fw)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "MSR: $msr"
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}


def zwaveEvent(physicalgraph.zwave.commands.powerlevelv1.PowerlevelReport cmd) {
	log.debug "Radio power level: $cmd"
}

def ping() {
	refresh()
}

def poll() {
	refresh()
}

def refresh() {
	delayBetween([
		zwave.versionV1.versionGet().format(),
		zwave.manufacturerSpecificV2.manufacturerSpecificGet().format(),
		zwave.powerlevelV1.powerlevelGet().format()
	], 200)
}
