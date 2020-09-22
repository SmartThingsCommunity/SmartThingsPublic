/**
 *  Copyright 2020 SmartThings
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
	definition (name: "Qubino 3 Phase Meter", namespace: "qubino", author: "SmartThings", ocfDeviceType: "x.com.st.d.energymeter", mcdSync: true) {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Sensor"
		capability "Health Check"
		capability "Refresh"

		fingerprint mfr: "0159", prod: "0007", model: "0054", deviceJoinName: "Qubino Energy Monitor" //Qubino 3 Phase Meter
		// zw:L type:3103 mfr:0159 prod:0007 model:0054 ver:1.00 zwv:4.61 lib:03 cc:5E,55,86,73,56,98,9F,72,5A,70,60,85,8E,59,32,6C,7A epc:4
	}

	// tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"power", type: "generic", width: 6, height: 4){
			tileAttribute("device.power", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue} W')
			}
			tileAttribute("device.energy", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue} kWh')
			}
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat",width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat",width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main (["power","energy"])
		details(["power","energy","refresh", "configure"])
	}
}

def installed() {
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	state.numberOfMeters = 3

	if (!childDevices) {
		addChildMeters(state.numberOfMeters)
	}

	response(refresh())
}

def updated() {
	response(refresh())
}

def ping() {
	refresh()
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(versions)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep = null) {
	log.debug "Multichannel command ${cmd}" + (ep ? " from endpoint $ep" : "")
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	def encapsulatedCommand = cmd.encapsulatedCommand()
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, endpoint = null) {
	handleMeterReport(cmd, endpoint)
}

private handleMeterReport(cmd, endpoint) {
	def event = createMeterEventMap(cmd)
	if (endpoint && endpoint > 1) {
		String childDni = "${device.deviceNetworkId}:${endpoint - 1}"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		child?.sendEvent(event)
	} else {
		createEvent(event)
	}
}

private createMeterEventMap(cmd) {
	def eventMap = [:]
	if (cmd.scale == 0) {
		eventMap.name = "energy"
		eventMap.value = cmd.scaledMeterValue
		eventMap.unit = "kWh"
	} else if (cmd.scale == 2) {
		eventMap.name = "power"
		eventMap.value = Math.round(Math.abs(cmd.scaledMeterValue))
		eventMap.unit = "W"
	}
	eventMap
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	log.warn "Not handled Z-Wave command: ${cmd}"
	[:]
}

private encap(cmd, endpoint = null) {
	if (cmd) {
		if (endpoint) {
			cmd = zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint).encapsulate(cmd)
		}

		if (zwaveInfo.zw.contains("s")) {
			zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		} else {
			cmd.format()
		}
	}
}

def refresh() {
	delayBetween([
			encap(zwave.meterV3.meterGet(scale: 0)),
			encap(zwave.meterV3.meterGet(scale: 2))
	])
}

def configure() {
	log.debug "configure() has been called"
	encap(zwave.configurationV1.configurationSet(parameterNumber: 42, size: 2, scaledConfigurationValue: 1800)) // Report energy consumption every 30 minutes
}

private addChildMeters(numberOfMeters) {
	for (def endpoint : 1..numberOfMeters) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"
			def componentLabel = device.displayName + " ${endpoint}"
			addChildDevice("smartthings", "Child Energy Meter", childDni, device.getHub().getId(), [
					completedSetup	: true,
					label			: componentLabel,
					isComponent		: true,
					componentName 	: "endpointMeter$endpoint",
					componentLabel	: "Endpoint Meter $endpoint"
			])
		} catch (Exception e) {
			log.warn "Exception: ${e}"
		}
	}
}

private getMeterId(deviceNetworkId) {
	def split = deviceNetworkId?.split(":")
	return (split.length > 1) ? split[1] as Integer : null
}

private childRefresh(deviceNetworkId) {
	def meterId = getMeterId(deviceNetworkId)
	if (switchId != null) {
		sendHubCommand delayBetween([
				encap(zwave.meterV3.meterGet(scale: 0), meterId),
				encap(zwave.meterV3.meterGet(scale: 2), meterId)
		])
	}
}