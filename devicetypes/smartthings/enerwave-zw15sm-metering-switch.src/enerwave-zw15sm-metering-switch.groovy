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
 * modified by lgkahn v1 for control of the on/off light
 * the lite when on/off is not the normal parameter settings of the config class and there also
 * is no never lite option.. So custom code for this device only.
 *
 */
metadata {
	definition (name: "Enerwave ZW15SM Metering Switch", namespace: "smartthings", author: "LGKahn") {
		capability "Energy Meter"
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"
        capability "Indicator"
       
		fingerprint inClusters: "0x25,0x32"
	}


preferences {
   input("ReportTime", "number", title: "Report Timeout Interval?", description: "The time in minutes after which an update is sent?", defaultValue: 3, required: false)
   input("WattageChange", "number", title: "Wattage change before reporting: 1-25?", description: "The minimum wattage change before reporting?",defaultValue: 15, required: false)
   }

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}

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
		valueTile("power", "device.power") {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy") {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat") {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		standardTile("indicatorStatus", "device.indicatorStatus", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorWhenOff", icon:"st.indicators.lit-when-on"
		
		}
		main(["switch","power","energy"])
		details(["switch","power","energy","indicatorStatus","refresh","reset"])
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



def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
//log.debug "in meter report cmd = $cmd "
	if (cmd.scale == 0) {
    	log.debug "got energy/kWh = $cmd.scaledMeterValue"
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
	} else if (cmd.scale == 1) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
	} else if (cmd.scale == 2) {
    	log.debug "got power/W = $cmd.scaledMeterValue"
		createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
	}
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

	
		result << response(delayBetween([
			zwave.meterV2.meterGet(scale: 0).format(),
			zwave.meterV2.meterGet(scale: 2).format(),
		]))

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
		"delay 3000",
		zwave.meterV2.meterGet(scale: 2).format()
	]
}

def off() {
log.debug "in off"
	[
    	zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000",
		zwave.meterV2.meterGet(scale: 2).format()
	]
}

def poll() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def refresh() {
	def value = "when off"
  //  log.debug "in refresh for when on got indicatorstatus = $state.currentIndicatorStatus"
    sendEvent(name: "indicatorStatus", value: state.currentIndicatorStatus, display: true)
	
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
    
  
}

def configure() {
if (settings.WattageChange < 1 || settings.WattageChange > 25)
 {
   settings.WattageChange = 5;
 }
 if (settings.ReportTime == null)
   settings.ReportTime = 5
 if (settings.WattageChange == null)
   settings.WattageChnage = 10;
   
log.debug "In configure timeout value = $settings.ReportTime"
log.debug "Wattage change = $settings.WattageChange"
def wattageadjust = settings.WattageChange * 10;
log.debug "Wattage change = $settings.WattageChange adjusted: $wattageadjust "

 delayBetween([
	zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: settings.ReportTime).format(),	//send meter report every x minutes.
	zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, scaledConfigurationValue: settings.ReportTime).format(),	//accumulated energy report meter interval
	zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1, scaledConfigurationValue: 1).format(),	//send only meter report when wattage change 1=meter, 2=multilevel , 3=both, 0=none 
	zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1, scaledConfigurationValue: wattageadjust).format(),//Minimum change in wattage 0-255
	zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, scaledConfigurationValue: 0).format(),// dont send multilevel report
    ])
}

def reset() {
log.debug "in reset"
			 state.currentIndicatorStatus = "when on"
	return [
		zwave.meterV2.meterReset().format(),
		zwave.meterV2.meterGet(scale: 0).format()
	]
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
configure();
}