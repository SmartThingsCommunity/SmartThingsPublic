/**
 *  FortrezZ Flow Meter Interface
 *
 *  Copyright 2016 FortrezZ, LLC
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
 *  Based on Todd Wackford's MimoLite Garage Door Opener
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "FortrezZ MIMOlite", namespace: "fortrezz", author: "FortrezZ, LLC") {
		capability "Configuration"
		capability "Switch"
		capability "Refresh"
		capability "Contact Sensor"
        capability "Voltage Measurement"

		attribute "powered", "string"

		command "on"
		command "off"
        
        fingerprint deviceId: "0x1000", inClusters: "0x72,0x86,0x71,0x30,0x31,0x35,0x70,0x85,0x25,0x03"
	}

	simulator {
	// Simulator stuff
    
	}
    
    preferences {
       input "RelaySwitchDelay", "decimal", title: "Delay between relay switch on and off in seconds. Only Numbers 0 to 3.0 allowed. 0 value will remove delay and allow relay to function as a standard switch", description: "Numbers 0 to 3.1 allowed.", defaultValue: 0, required: false, displayDuringSetup: true
    }


	// UI tile definitions 
	tiles (scale: 2) {
        standardTile("switch", "device.switch", width: 4, height: 4, canChangeIcon: false, decoration: "flat") {
            state "on", label: "On", action: "off", icon: "http://swiftlet.technology/wp-content/uploads/2016/06/Switch-On-104-edit.png", backgroundColor: "#53a7c0"
			state "off", label: 'Off', action: "on", icon: "http://swiftlet.technology/wp-content/uploads/2016/06/Switch-Off-104-edit.png", backgroundColor: "#ffffff"
        }
        standardTile("contact", "device.contact", width: 2, height: 2, inactiveLabel: false) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
		}
        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("powered", "device.powered", width: 2, height: 2, inactiveLabel: false) {
			state "powerOn", label: "Power On", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "powerOff", label: "Power Off", icon: "st.switches.switch.off", backgroundColor: "#ffa81e"
		}
		standardTile("configure", "device.configure", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        valueTile("voltage", "device.voltage", width: 2, height: 2) {
            state "val", label:'${currentValue}v', unit:"", defaultState: true
        }
        valueTile("voltageCounts", "device.voltageCounts", width: 2, height: 2) {
            state "val", label:'${currentValue}', unit:"", defaultState: true
        }
		main (["switch"])
		details(["switch", "contact", "voltage", "powered", "refresh","configure"])
	}
}

def parse(String description) {
//log.debug "description is: ${description}"

	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x30: 1, 0x70: 1, 0x31: 5])
    
    //log.debug "command value is: $cmd.CMD"
    
    if (cmd.CMD == "7105") {				//Mimo sent a power loss report
    	log.debug "Device lost power"
    	sendEvent(name: "powered", value: "powerOff", descriptionText: "$device.displayName lost power")
    } else {
    	sendEvent(name: "powered", value: "powerOn", descriptionText: "$device.displayName regained power")
    }
    //log.debug "${device.currentValue('contact')}" // debug message to make sure the contact tile is working
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText} $cmd.CMD"
	return result
}

def updated() {
	log.debug "Settings Updated..."
    configure()
}
//notes about zwaveEvents:
// these are special overloaded functions which MUST be returned with a map similar to (return [name: "switch", value: "on"])
// not doing so will produce a null on the parse function, this will mess you up in the future.
// Perhaps can use 'createEvent()' and return that as long as a map is inside it.
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) { 
log.debug "switchBinaryReport ${cmd}"
    if (cmd.value) // if the switch is on it will not be 0, so on = true
    {
		return [name: "switch", value: "on"] // change switch value to on
    }
    else // if the switch sensor report says its off then do...
    {
		return [name: "switch", value: "off"] // change switch value to off
    }
       
}

// working on next for the analogue and digital stuff.
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) // basic set is essentially our digital sensor for SIG1
{
	log.debug "sent a BasicSet command"
    //refresh()  
    delayBetween([zwave.sensorMultilevelV5.sensorMultilevelGet().format()])// requests a report of the anologue input voltage
	[name: "contact", value: cmd.value ? "open" : "closed"]}
    //[name: "contact", value: cmd.value ? "open" : "closed", type: "digital"]}
    
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	log.debug "sent a sensorBinaryReport command"
	refresh()    
	[name: "contact", value: cmd.value ? "open" : "closed"]
}


    
def zwaveEvent (physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) // sensorMultilevelReport is used to report the value of the analog voltage for SIG1
{
	log.debug "sent a SensorMultilevelReport"
	def ADCvalue = cmd.scaledSensorValue
    sendEvent(name: "voltageCounts", value: ADCvalue)
   
    CalculateVoltage(cmd.scaledSensorValue)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
     log.debug("Un-parsed Z-Wave message ${cmd}")
	[:]
}

def CalculateVoltage(ADCvalue)
{
	 def map = [:]
     
     def volt = (((1.5338*(10**-16))*(ADCvalue**5)) - ((1.2630*(10**-12))*(ADCvalue**4)) + ((3.8111*(10**-9))*(ADCvalue**3)) - ((4.7739*(10**-6))*(ADCvalue**2)) + ((2.8558*(10**-3))*(ADCvalue)) - (2.2721*(10**-2)))

    //def volt = (((3.19*(10**-16))*(ADCvalue**5)) - ((2.18*(10**-12))*(ADCvalue**4)) + ((5.47*(10**-9))*(ADCvalue**3)) - ((5.68*(10**-6))*(ADCvalue**2)) + (0.0028*ADCvalue) - (0.0293))
	//log.debug "$cmd.scale $cmd.precision $cmd.size $cmd.sensorType $cmd.sensorValue $cmd.scaledSensorValue"
	def voltResult = volt.round(1)// + "v"
    
	map.name = "voltage"
    map.value = voltResult
    map.unit = "v"
    return map
}
	

def configure() {
	def x = (RelaySwitchDelay*10).toInteger()
    log.debug "Configuring.... " //setting up to monitor power alarm and actuator duration
    
	delayBetween([
		zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(), // 	FYI: Group 3: If a power dropout occurs, the MIMOlite will send an Alarm Command Class report 
        																							//	(if there is enough available residual power)
        zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format(), // periodically send a multilevel sensor report of the ADC analog voltage to the input
        zwave.associationV1.associationSet(groupingIdentifier:4, nodeId:[zwaveHubNodeId]).format(), // when the input is digitally triggered or untriggered, snd a binary sensor report
        zwave.configurationV1.configurationSet(configurationValue: [x], parameterNumber: 11, size: 1).format() // configurationValue for parameterNumber means how many 100ms do you want the relay
        																										// to wait before it cycles again / size should just be 1 (for 1 byte.)
        //zwave.configurationV1.configurationGet(parameterNumber: 11).format() // gets the new parameter changes. not currently needed. (forces a null return value without a zwaveEvent funciton
	])
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),	// physically changes the relay from on to off and requests a report of the relay
        refresh()// to make sure that it changed (the report is used elsewhere, look for switchBinaryReport()
       ])
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(), // physically changes the relay from on to off and requests a report of the relay
        refresh()// to make sure that it changed (the report is used elsewhere, look for switchBinaryReport()
	])
}

def refresh() {
log.debug "REFRESH!"
	delayBetween([
        zwave.switchBinaryV1.switchBinaryGet().format(), //requests a report of the relay to make sure that it changed (the report is used elsewhere, look for switchBinaryReport()
        zwave.sensorMultilevelV5.sensorMultilevelGet().format()// requests a report of the anologue input voltage

    ])
}