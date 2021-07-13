/*
 *  Copyright 2017 SmartThings
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
 *  ZigBee Ceiling Fan Light
 *
 *  Author: SAMSUNG LED
 *  Date: 2021-06-30
 */

import physicalgraph.zigbee.zcl.DataType
import groovy.json.JsonOutput

metadata {
	definition(name: "ITM Fan Child", namespace: "SAMSUNG LED", author: "SAMSUNG LED", ocfDeviceType: "oic.d.fan") {
		capability "Actuator"
        	capability "Configuration"
        	capability "Refresh"
        	capability "Switch"        
        	capability "Fan Speed"		
    }    
}

def off() {
	setFanSpeed(0)
}

def on() {
	setFanSpeed(1)
}

def setFanSpeed(speed) {
	log.debug "child setFanSpeed $speed"	
	if (speed as Integer == 0) {
        	sendEvent(name: "switch", value: "off", displayed: true, isStateChange: true)
    	} else {
        	sendEvent(name: "switch", value: "on", displayed: true, isStateChange: true)
    	}	
	parent.setFanSpeed(speed, device)
}

def parse(String description) {
	log.debug "[Child] - PARSE IN Child: $description"
}

void refresh() {
	log.debug "[Child] - refresh()"
	parent.refresh(device)
	parent.childRefresh(device.deviceNetworkId)
}

def ping() {
	log.debug "[Child] - ping()"
	parent.ping(device)
}

def configure() {
	log.debug "[Child] - configure()"   
}

def updated() {
	log.debug "[Child] - updated()"
}

def installed() {
	log.debug "[Child] - installed"
	configure()
}

def uninstalled() {
	log.debug "[Child] - uninstalled()"	
}
