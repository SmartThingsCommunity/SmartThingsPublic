/**
 *  Copyright 2015 SmartThings
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
 *	SmartPower Outlet (CentraLite)
 *
 *	Author: SmartThings
 *	Date: 2015-08-23
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Iris Smart Plug", namespace: "blebson", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"
		
		// indicates that device keeps track of heartbeat (in state.heartbeat)
		attribute "heartbeat", "string"     
		attribute "timerStart", "number"
		
		attribute "energyDisplay", "string"
		attribute "elapsedTimeDisplay", "string"
	
		command "resetEnergyUsage"
			
		fingerprint profileId: "0104", inClusters: "0000 0003 0004 0005 0006 0B04 0B05 FC03", outClusters: "0019", manufacturer: "CentraLite",  model: "3210-L", deviceJoinName: "Outlet"
		//fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019", manufacturer: "CentraLite",  model: "3200", deviceJoinName: "Outlet"
		//fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019", manufacturer: "CentraLite",  model: "3200-Sgb", deviceJoinName: "Outlet"
		//fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019", manufacturer: "CentraLite",  model: "4257050-RZHAC", deviceJoinName: "Outlet"
		//fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019"
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
        
        section("Reporting Intervals") {
        	input "intervalMin", "number", title: "Minimum interval between reports [s]", defaultValue: 5, range: "1..600"
        	input "intervalMax", "number", title: "Maximum interval between reports [s]", defaultValue: 600, range: "1..600"
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
        
		valueTile("energyDisplay", "device.energyDisplay", width: 5, height: 1, decoration: "flat") {
			state "default", label:'Energy used: ${currentValue}', unit: "kWh"
        	}
        	
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
		standardTile("resetUsage", "command.resetEnergyUsage", decoration: "flat", width: 1, height: 1){
			state "default", action: "resetEnergyUsage", label:'Reset kWh', icon:"st.Health & Wellness.health7"
		}        
        
		valueTile("elapsedTimeDisplay", "device.elapsedTimeDisplay", decoration: "flat", width: 5, height: 1){
			state "default", label: 'Time: ${currentValue}', unit: "h"
		}      

		standardTile("configure", "device.switch", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label:"", action:"configure", icon:"st.secondary.configure"
		}

		main "switch"
		details(["switch","energyDisplay","resetUsage","power","elapsedTimeDisplay","refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	// save heartbeat (i.e. last time we got a message from device)
	state.heartbeat = Calendar.getInstance().getTimeInMillis()
	if (state.timerStart == null)
	{
		state.timerStart = Calendar.getInstance().getTimeInMillis()
		state.energy = 0.0
	}

	def finalResult = zigbee.getKnownDescription(description)

	//TODO: Remove this after getKnownDescription can parse it automatically
	if (!finalResult && description!="updated")
		finalResult = getPowerDescription(zigbee.parseDescriptionAsMap(description))

	if (finalResult) {
		log.info finalResult
		if (finalResult.type == "update") {
			log.info "$device updates: ${finalResult.value}"
		}
		else if (finalResult.type == "power") {
			def powerValue = (finalResult.value as Integer)/10           
			sendEvent(name: "power", value: powerValue, isStateChange: true, displayed: false) // note: stateChange = true added so the energy calculation can work properly
			/*
				Dividing by 10 as the Divisor is 10000 and unit is kW for the device. AttrId: 0302 and 0300. Simplifying to 10
				power level is an integer. The exact power level with correct units needs to be handled in the device type
				to account for the different Divisor value (AttrId: 0302) and POWER Unit (AttrId: 0300). CLUSTER for simple metering is 0702
			*/           
			calculateAndShowEnergy()
		}
		else {
			sendEvent(name: finalResult.type, value: finalResult.value)
		}
	}
	else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug zigbee.parseDescriptionAsMap(description)
	}
}

def calculateAndShowEnergy()
{
    def recentEvents = device.statesSince("power", new Date()-1, [max: 2]).collect {[value: it.value as float, date: it.date]}        	
    def deltaT = (recentEvents[0].date.getTime() - recentEvents[1].date.getTime()) // time since last "power" event in milliseconds
    deltaT = deltaT / 3600000 // convert to hours
    
    def energyValue = device.currentValue("energy") 
    if(energyValue != null) {
    	energyValue += (recentEvents[1].value * deltaT) / 1000 // energy used since last "power" event in kWh 
    }
    
    sendEvent(name: "energy", value: energyValue, displayed: false)
    sendEvent(name: "energyDisplay", value: String.format("%6.3f kWh",energyValue), displayed: false)

    def currentTime = Calendar.getInstance().getTimeInMillis()   
    def timeDifference = ((long)currentTime - state.timerStart)/1000; // in seconds
    int h = (int) (timeDifference / (3600));
    int m = (int) ((timeDifference - (h * 3600)) / 60);
    int s = (int) (timeDifference - (h * 3600) - m * 60);
    int d = (int) h >= 24 ? h / 24 : 0
    h = d > 0 ? h % 24 : h

    sendEvent(name: "elapsedTimeDisplay", value: String.format("%dd %02d:%02d:%02d", d, h, m, s), displayed: false)
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def resetEnergyUsage() {
	sendEvent(name: "energy", value: 0.0, displayed: false)
	state.timerStart = Calendar.getInstance().getTimeInMillis()
	sendEvent(name: "energyDisplay", value: String.format("%6.3f kWh",0.0), displayed: false)
	sendEvent(name: "elapsedTimeDisplay", value: String.format("%dd %02d:%02d:%02d", 0, 0, 0, 0), displayed: false)
}

def refresh() {
	sendEvent(name: "heartbeat", value: "alive", displayed:false)
	return zigbee.onOffRefresh() + zigbee.refreshData("0x0B04", "0x050B")
}

def configure() {
	log.debug "Configuring..."
	return zigbee.onOffConfig() + powerConfig() + refresh()
}

def updated() {
	response(configure())
}

//power config for devices with min reporting interval as 1 seconds and reporting interval if no activity as 10min (600s)
//min change in value is 01
def powerConfig() {
	[
		"zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 0x0B04 {${device.zigbeeId}} {}", "delay 200",
		"zcl global send-me-a-report 0x0B04 0x050B 0x29 ${intervalMin ?: 5} ${intervalMax ?: 600} {05 00}",				//The send-me-a-report is custom to the attribute type for CentraLite
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