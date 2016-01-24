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
 * modified by lgkahn v1 for control of the zw15r switch (no monitoring) 
 * for these the blue light control is backwards 
 */
metadata {
	definition (name: "Enerwave ZW15R Outlet", namespace: "smartthings", author: "LGKahn") {
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"
        capability "Indicator"
       
		fingerprint inClusters: "0x25,0x32"
	}


	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"


		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"

	}

	// tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
	
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		standardTile("indicatorStatus", "device.indicatorStatus", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorWhenOff", icon:"st.indicators.lit-when-on"
		
		}
		main(["switch"])
		details(["switch","indicatorStatus","refresh"])
	}
}

def parse(String description) {
	def result = null
   // log.debug "in parse desc = $description"
    
	if(description == "updated") return 
	def cmd = zwave.parse(description, [0x20: 1, 0x32: 1, 0x72: 2])
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	return result
}




def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	def evt = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
	if (evt.isStateChange) {
		[evt, response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])]
	} else {
		evt
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {

   // log.debug "in config report  got indicatorstatus = $state.currentIndicatorStatus"	
	[name: "indicatorStatus", value: state.currentIndicatorStatus, display: true]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
log.debug "in manuf specific report"
	def result = []
    
		

	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "$device.displayName: Unhandled: $cmd"
	[:]
}

def on() {
log.debug "in on"
	[
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000"
	]
}

def off() {
log.debug "in off"
	[
    	zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000"
	]
}

def poll() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def refresh() {
	def value = "when off"
    log.debug "in refresh for when on got indicatorstatus = $state.currentIndicatorStatus"
    sendEvent(name: "indicatorStatus", value: state.currentIndicatorStatus, display: true)
	
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
    
  
}

def configure() {
log.debug "In configure"
}

def indicatorWhenOn() {
log.debug "in when on"
 state.currentIndicatorStatus = "when on"
	sendEvent(name: "indicatorStatus", value: "when on", display: true)
	zwave.configurationV1.configurationSet(parameterNumber: 0x01, size: 1, scaledConfigurationValue: 1).format()
	}

def indicatorWhenOff() {
log.debug "in when off"
 state.currentIndicatorStatus = "when off"
	sendEvent(name: "indicatorStatus", value: "when off", display: true)	  
	zwave.configurationV1.configurationSet(parameterNumber: 0x01, size: 1, scaledConfigurationValue: 0).format()
	}


def updated()
{
log.debug "in updated"
}