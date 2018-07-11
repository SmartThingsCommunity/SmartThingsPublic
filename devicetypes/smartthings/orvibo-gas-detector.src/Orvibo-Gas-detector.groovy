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
		capability "Refresh"
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0003,0500,0009", outClusters: "0019",manufacturer: "Heiman",model:"d0e857bfd54f4a12816295db3945a421"
	}

	simulator {
		status "active": "zone status 0x0001 -- extended status 0x00"
		status "alarm" : "read attr - raw: E91D0100090C000000210000, dni: E91D, endpoint: 01, cluster: 0009, size: 0C, attrId: 0000, result: success, encoding: 21, value: 0001"
		status "off" : "read attr - raw: E91D0100090C000000210000, dni: E91D, endpoint: 01, cluster: 0009, size: 0C, attrId: 0000, result: success, encoding: 21, value: 0000"
	}

	tiles {
		standardTile("smoke", "device.smoke", width: 2, height: 2) {
			state("clear", label:"Clear", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
			state("detected", label:"Smoke!", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
		}
		standardTile("alarm", "device.alarm", width: 2, height: 2) {
			state "off", label:'off', action:'alarm.siren', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"	
			state "siren", label:'siren!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"		
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		main "smoke"
		details(["smoke","alarm","refresh"])
	}
}
def installed(){
	log.debug "installed"
	refresh()
}
def parse(String description) {
	log.debug "description(): $description"
	def map = zigbee.getEvent(description)
	if(!map){
		if (description?.startsWith('zone status')) {
			map = parseIasMessage(description)
		}else if(description?.startsWith('read attr')){
			map = parseAlarmMessage(description)
		}else{
			map = zigbee.parseDescriptionAsMap(description)
		}
	}
	log.debug "Parse returned $map"
	def result = map ? createEvent(map) : [:]
	if (description?.startsWith('enroll request')) {
		List cmds = zigbee.enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it)}
	}
	return result
}
def parseAlarmMessage(String description){
	def descMap = zigbee.parseDescriptionAsMap(description)
	def map = [:]
	if (descMap?.clusterInt == 0x0009 && descMap.value) {
		map = getAlarmResult(descMap.value == "0000" ? false :true)
	}
	return map;
}
def parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	return getDetectedResult(zs.isAlarm1Set() || zs.isAlarm2Set())
}
def getDetectedResult(value) {
	def detected = value ? 'detected': 'clear'
	String descriptionText = "${device.displayName} smoke ${detected}" 
	return [name:'smoke',
			value: detected,
			descriptionText:descriptionText,
			translatable:true]
}
def getAlarmResult(value) {
	def alarm = value ? 'siren': 'off'
	String descriptionText = "${device.displayName} alarm  ${alarm}" 
	return [name:'alarm',
			value: alarm,
			descriptionText:descriptionText,
			translatable:true]
}
def siren() {
	sendEvent(name: "alarm", value: "siren")
}
def off() {
	sendEvent(name: "alarm", value: "off")
}
def refresh() {
	log.debug "Refreshing Values"
	def refreshCmds = []
	refreshCmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER,zigbee.ATTRIBUTE_IAS_ZONE_STATUS) +
					zigbee.readAttribute(0x0009,0x0000) 
	return refreshCmds
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping "
	refresh()
}
def configure() {
	log.debug "configure"
	sendEvent(name: "checkInterval", value:20 * 60 + 2*60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}
