 /* 
  *  Copyright 2018 SmartThings 
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
  *  Author : Fen Mei / f.mei@samsung.com 
  *  Date : 2018-07-06
  */ 
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "Orvibo Temperature&Humidity Sensor Endpoint",namespace: "smartthings", author: "SmartThings", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, mnmn: "SmartThings", vid: "generic-humidity") {
		capability "Configuration"
		capability "Sensor"
                capability "Relative Humidity Measurement"
		capability "Refresh"
		capability "Health Check"

		//fingerprint profileId: "0104",deviceId: "0302", inClusters: "0000,0001,0003,0402", outClusters: ""
        fingerprint endpoint: "2", profileId: "0104",deviceId: "0302", inClusters: "0000,0003,0405", outClusters: ""
	}
	
	preferences {
		input title: "Humidity Offset", description: "This feature allows you to correct any humidity variations by selecting an offset. Ex: If your sensor consistently reports a humidity that's 6% higher then a similiar calibrated sensor, you'd enter \"-6\".", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		input "humidityOffset", "number", title: "Humidity Offset in Percent", description: "Adjust humidity by this percentage", range: "*..*", displayDuringSetup: false
	}

	tiles(scale: 2) {
		valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
			state "humidity", label: '${currentValue}% humidity', unit: ""
		}
        
		main "humidity"
		details(["humidity"])
	}

}

def parse(String description) {
	log.debug "description: $description"

	Map map = zigbee.getEvent(description)
	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
		List cmds = zigbee.enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	
	return result
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	zigbee.readAttribute(0x0405, 0x0000)
}

def refresh() {
	log.debug "Refreshing Values"
	def refreshCmds = []
		refreshCmds += zigbee.readAttribute(0x0405, 0x0000)
		zigbee.enrollResponse()

	return refreshCmds
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value:1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting"

	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	
	return refresh() + refresh() // send refresh cmds as part of config
}
