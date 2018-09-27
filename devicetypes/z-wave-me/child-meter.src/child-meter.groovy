/**
 *  Child Meter
 *
 *  Copyright 2018 Alexander Belov, Z-Wave.Me
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
	definition (name: "Child Meter", namespace: "z-wave-me", author: "Alexander Belov") {
		capability "Refresh"
		
		command "reset"
						
		attribute "meterType", "String"				
		attribute "lastUpdated", "String"
	}

	tiles(scale: 2) {
		standardTile("logo", "device.logo", inactiveLabel: true, decoration: "flat", width: 1, height: 1) {
			state "default", label:'', icon: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@2x.png"
		}
		valueTile("lastUpdated", "device.lastUpdated", decoration: "flat", width: 5, height: 1) {
			state "default", label:'Last updated ${currentValue}'
		}
		multiAttributeTile(name: "meter", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.meter", key: "PRIMARY_CONTROL") {
				attributeState "meterValue", label: '${currentValue}', icon: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@2x.png", backgroundColor: "#0000FF"
			}

			tileAttribute("device.refresh", inactiveLabel: false, key: "SECONDARY_CONTROL") {
				attributeState "refresh", label: '', action:"refresh.refresh", icon:"st.secondary.refresh"
			}
		}
		standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "reset", label: '', action:"reset", icon:"st.secondary.refresh"
		}
		valueTile("resetLabel", "device.resetLabel", decoration: "flat", width: 5, height: 1) {
			state "default", label:'Reset meter'
		}
		standardTile("typePic", "device.typePic", inactiveLabel: true, decoration: "flat", width: 1, height: 1) {
			state "default", label:'', icon: "http://cdn.device-icons.smartthings.com/Home/home1-icn@2x.png"
		}
		valueTile("meterType", "device.meterType", decoration: "flat", width: 5, height: 1) {
			state "default", label:'${currentValue}'
		}
	}
}


def parse(def description) {
	def cmd = zwave.parse(description)
	
	if (description.startsWith("Err")) {
		createEvent(descriptionText: description, isStateChange:true)
	} else if (description != "updated") {
		zwaveEvent(cmd)
		
		def nowDay = new Date().format("MMM dd", location.timeZone)
		def nowTime = new Date().format("h:mm a", location.timeZone)
		sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterSupportedReport cmd) {
    state.scale = cmd.scaleSupported
	setLabels(cmd.meterType, cmd.scaleSupported, null)
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	setLabels(cmd.meterType, cmd.scale, cmd.rateType)
	sendEvent(name: "meter", value: "${cmd.scaledMeterValue} ${state.typeLetter}")
}

def installed() {
   	parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.meterV3.meterSupportedGet().format()))
	sendEvent(name: "meterType", value: 'Waiting for device report')
}

def reset() {
	log.debug parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.meterV3.meterReset().format()))
}

def refresh() {
	if (state.scale) {
		parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.meterV3.meterGet().format()))
	} else {
		parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.meterV3.meterSupportedGet().format()))
		log.debug "Can't execute refresh. Waiting for Meter Supported Report Command"
	}
}

def setLabels(def meterType, def scale, def rateType) {	
	switch (meterType) {
		case 1:
			state.type = "Electric meter"
			if (scale == 0)
				state.typeLetter = "kwh"
			else if (scale == 1)
				state.typeLetter = "kvah"
			else if (scale == 2)
				state.typeLetter = "w"
			else if (scale == 3)
				state.typeLetter = "p.c."
			else if (scale == 4) 
				state.typeLetter = "v"
			else if (scale == 5)
				state.typeLetter = "a"
			else if (scale == 6)
				state.typeLetter = "p.f."
		   	break
			
		case 2:
		//TODO: convert
			state.type = "Gas meter"
			if (scale == 0)
				state.typeLetter = "m³"
			else if (scale == 1)
				state.typeLetter = "ft³"
			else if (scale == 3)
				state.typeLetter = "p.c."
			break
			
		case 3:
		//TODO: convert
			state.type = "Water meter"
			if (scale == 0)
				state.typeLetter = "m³"
			else if (scale == 1)
				state.typeLetter = "ft³"
			else if (scale == 2)
				state.typeLetter = "gal"
			else if (scale == 3)
				state.typeLetter = "p.c."
		   	break
			
		default:
			log.debug "Wrong meterType:'${meterType}'"
	}
	
	// not used in Z-Uno
	if (rateType) {
		switch (rateType) {
			case 0:
				state.rateType = "Unspecified"
				break
			case 1:
				state.rateType = "Import (consumed)"
				break
			case 2:
				state.rateType = "Export (produced)"
				break

			default:
				log.debug "Wrong rateType:'${rateType}'"
		}
	}
	
  	sendEvent(name: "meterType", value: state.type)
}