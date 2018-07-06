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
 *  Author : jinkang zhang / jk0218.zhang@samsung.com
 *  Date : 2018-07-04
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "Orvibo Gas Detector", namespace: "smartthings", author: "SmartThings", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, mnmn: "SmartThings", vid: "generic-smoke") {
		capability "Smoke Detector"
		capability "Configuration"
		capability "Alarm"
		capability "Health Check"
		capability "Sensor"


		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0003,0500,0009", outClusters: "0019",manufacturer: "Heiman",model:"d0e857bfd54f4a12816295db3945a421"
	}

	tiles {
		standardTile("main", "device.smoke", width: 2, height: 2) {
			state("clear", label:"Clear", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
			state("detected", label:"Smoke!", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
		}

		standardTile("alarm", "device.alarm", width: 2, height: 2) {
                state "off", label:'off', action:'alarm.strobe', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
                state "strobe", label:'strobe!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
                state "siren", label:'siren!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
                state "both", label:'alarm!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
        }



        main "main"
		details(["main","alarm"])
	}
}

def installed(){
	log.debug "installed"
	configure()
}

def parse(String description) {
	log.debug "description(): $description"
	def map = zigbee.getEvent(description)
	if(!map){
		if (description?.startsWith('zone status')) {
			map = parseIasMessage(description)
		}
	}

	log.debug "Parse returned $map"
	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
        List cmds = zigbee.enrollResponse()
        log.debug "enroll response: ${cmds}"
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }

	return result
}


private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
	// Some sensor models that use this DTH use alarm1 and some use alarm2 to signify motion
	return (zs.isAlarm1Set() || zs.isAlarm2Set()) ? getDetectedResult('detected') : getDetectedResult('clear')
}

private Map getDetectedResult(value) {
	String descriptionText = value == 'detected' ? "${device.displayName} detected smoke" : "${device.displayName} smoke clear"
	if(value == detected){
		strobe()
	}else{
		off()
	}
	return [
			name           : 'smoke',
			value          : value,
			descriptionText: descriptionText,
			translatable   : true
	]
}

def strobe() {
        sendEvent(name: "alarm", value: "strobe")
}

def siren() {
        sendEvent(name: "alarm", value: "siren")
}

def both() {
        sendEvent(name: "alarm", value: "both")
}

def off() {
        sendEvent(name: "alarm", value: "off")
}


/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping "
	return zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def configure() {
	log.debug "configure"
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value:40 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting"
	def configCmds = []

	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	configCmds += zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.UINT8, 30, 21600, 0x10)
	return configCmds // send refresh cmds as part of config
}
