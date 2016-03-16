/*
===============================================================================
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy 
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 *  License for the specific language governing permissions and limitations 
 *  under the License.
===============================================================================
 *  Purpose: SmartPower Outlet DTH File
 *
 *  Filename: SmartPower-Outlet.src/SmartPower-Outlet.groovy
 *
 *  Change History:
 *  1. 20160117 TW - Update/Edit to support i18n translations
===============================================================================
 */

metadata {
	// Automatically generated. Make future change here.
	definition (name: "SmartPower Outlet", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"

		// indicates that device keeps track of heartbeat (in state.heartbeat)
		attribute "heartbeat", "string"

		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019", manufacturer: "CentraLite",  model: "3200", deviceJoinName: "Outlet"
		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019", manufacturer: "CentraLite",  model: "3200-Sgb", deviceJoinName: "Outlet"
		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019", manufacturer: "CentraLite",  model: "4257050-RZHAC", deviceJoinName: "Outlet"
		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019"
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

	preferences {
		section {
			image(name: 'educationalcontent', multiple: true, images: [
				"http://cdn.device-gse.smartthings.com/Outlet/US/OutletUS1.jpg",
				"http://cdn.device-gse.smartthings.com/Outlet/US/OutletUS2.jpg"
				])
		}
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			tileAttribute ("power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'${currentValue} W'
			}
		}

		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	// save heartbeat (i.e. last time we got a message from device)
	state.heartbeat = Calendar.getInstance().getTimeInMillis()

	def finalResult = zigbee.getKnownDescription(description)

	//TODO: Remove this after getKnownDescription can parse it automatically
	if (!finalResult && description!="updated")
		finalResult = getPowerDescription(zigbee.parseDescriptionAsMap(description))

	if (finalResult) {
		log.info "final result = $finalResult"
		if (finalResult.type == "update") {
			log.info "$device updates: ${finalResult.value}"
		}
		else if (finalResult.type == "power") {
			def value = (finalResult.value as Integer)/10
			createEvent(name: "power", value: value, descriptionText: '{{ device.displayName }} power is {{ value }} Watts', translatable: true )
			/*
				Dividing by 10 as the Divisor is 10000 and unit is kW for the device. AttrId: 0302 and 0300. Simplifying to 10
				power level is an integer. The exact power level with correct units needs to be handled in the device type
				to account for the different Divisor value (AttrId: 0302) and POWER Unit (AttrId: 0300). CLUSTER for simple metering is 0702
			*/
		}
		else {
        	if ( finalResult.value == "on" )
				createEvent(name: finalResult.type, value: finalResult.value, descriptionText: '{{ device.displayName }} is On', translatable: true)
            else
            	createEvent(name: finalResult.type, value: finalResult.value, descriptionText: '{{ device.displayName }} is Off', translatable: true)
		}
	}
	else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug zigbee.parseDescriptionAsMap(description)
	}
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def refresh() {
	sendEvent(name: "heartbeat", value: "alive", displayed:false)
	zigbee.onOffRefresh() + zigbee.refreshData("0x0B04", "0x050B")
}

def configure() {
	zigbee.onOffConfig() + powerConfig() + refresh()
}

//power config for devices with min reporting interval as 1 seconds and reporting interval if no activity as 10min (600s)
//min change in value is 01
def powerConfig() {
	[
		"zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 0x0B04 {${device.zigbeeId}} {}", "delay 200",
		"zcl global send-me-a-report 0x0B04 0x050B 0x29 1 600 {05 00}",				//The send-me-a-report is custom to the attribute type for CentraLite
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500"
	]
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

//TODO: Remove this after getKnownDescription can parse it automatically
def getPowerDescription(descMap) {
	def powerValue = "undefined"
	if (descMap.cluster == "0B04") {
		if (descMap.attrId == "050b") {
			if(descMap.value!="ffff")
				powerValue = zigbee.convertHexToInt(descMap.value)
		}
	}
	else if (descMap.clusterId == "0B04") {
		if(descMap.command=="07"){
			return	[type: "update", value : "power (0B04) capability configured successfully"]
		}
	}

	if (powerValue != "undefined"){
		return	[type: "power", value : powerValue]
	}
	else {
		return [:]
	}
}