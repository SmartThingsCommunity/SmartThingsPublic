/**
 *  Copyright 2015 lgkahn v2.0
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
 *  Econet ebv105 water valve .. as modified from standard smartthings switch device type.
 *  under door control instead of switches as the states more closely match a valve..
 * so for smartapps youd use doors to close and open , also needs valve capability so it can
 * be selected and closed from smart home monitor, and finally needs contact sensor capability
 * which other drivers were lacking so you can check state and do your own alerts from 
 * notify me when smart app. Also correct signature to map to eBV105
 * tested by me for both notify when app, and also leak detection shut-off in smart home monitor.
 *
 * also I looked at Sidney Johnsons device type, so I also give him credit.
 * v2 noticed there is a light on the valve, so added capability indicator back to allow
 * you to set it on either when valve open or closed.
 * v2 added switch capability and alias so it can be controlled for ifttt and amazon echo etc.

 */
metadata {
	definition (name: "Econet EBV105 Water Valve", namespace: "lgkapps", author: "lgkahn") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Door Control"
        capability "Contact Sensor"
        capability "Valve"
        capability "Indicator"
        capability "switch"


	}

	// simulator metadata
	simulator {
		status "open":  "command: 2003, payload: FF"
		status "closed": "command: 2003, payload: 00"

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"
	}

        fingerprint deviceId: "0x1001", inClusters: "0x5E 0x86 0x72 0x73 0x85 0x59 0x25 0x20 0x27 0x70" 

    	// tile definitions
        
	tiles {
		standardTile("contact", "device.contact", width: 3, height: 3, canChangeIcon: true) {
			state "open", label: '${name}', action: "door control.close", icon: "st.valves.water.open", backgroundColor: "#00cccc", nextState:"closing"
			state "closed", label: '${name}', action: "door control.open", icon: "st.valves.water.closed", backgroundColor: "#cc0000", nextState:"opening"
			state "opening", label: '${name}', action: "door control.close", icon: "st.valves.water.open", backgroundColor: "#53a730"
			state "closing", label: '${name}', action: "door control.open", icon: "st.valves.water.closed", backgroundColor: "#cccc00"
		}

		standardTile("valve", "device.valve", width: 1, height: 1, canChangeIcon: true) {
			state "open", label: '${name}', icon: "st.valves.water.open", backgroundColor: "#00cccc"
			state "closed", label: '${name}', icon: "st.valves.water.closed", backgroundColor: "#cc0000"
		}
		
/*
	// tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", label: '${name}', action: "valve.close", icon: "st.valves.water.open", backgroundColor: "#79b821", nextState: closing
				attributeState "opening", label: '${name}', action: "valve.close", icon: "st.valves.water.open", backgroundColor: "#79b821"
				attributeState "closing", label: '${name}', action: "valve.open", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
                attributeState "closed", label: '${name}', action: "valve.open", icon: "st.valves.water.closed", backgroundColor: "#cc0000", nextState: ppening
			}
		}
  */ 
        standardTile("indicator", "device.indicatorStatus", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
			state "when on", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when off", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
			state "never", action:"indicator.indicatorWhenOff", icon:"st.indicators.never-lit"
		}


		standardTile("refresh", "device.switch", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "contact"
		details(["contact","refresh"])
	}
}

def parse(String description) {
  //log.debug "in parse desc = $description"
	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
  //log.debug "in binary report cmd = $cmd"
  if (cmd.value == 0)
    {
     log.debug "sending closed"
     sendEvent(name: "contact", value: "closed")
     sendEvent(name: "door", value: "closed")
     sendEvent(name: "valve", value: "closed")
     sendEvent(name: "switch", value: "off")
     }
    else {
     log.debug "sending open"
      sendEvent(name: "contact", value: "open")
      sendEvent(name: "door", value: "open")
      sendEvent(name: "valve", value: "open")
      sendEvent(name: "switch", value: "on")
        }  
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def value = "when off"
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	[name: "indicatorStatus", value: value, display: false]
}


def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	if (state.manufacturer != cmd.manufacturerName) {
		updateDataValue("manufacturer", cmd.manufacturerName)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on()
{
	open()
}

def off()
{
	close()
}

def open() {
//log.debug "in open"
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	], 6000)

}

def close() {
//log.debug "in close"

	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	], 6000)

}

def poll() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	])
}

def refresh() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	])
}


def indicatorWhenOn() {
	sendEvent(name: "indicatorStatus", value: "when open", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
}

def indicatorWhenOff() {
log.debug "in when off"
	sendEvent(name: "indicatorStatus", value: "when closed", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()
}

def indicatorNever() {
	sendEvent(name: "indicatorStatus", value: "never", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 3, size: 1).format()
}



