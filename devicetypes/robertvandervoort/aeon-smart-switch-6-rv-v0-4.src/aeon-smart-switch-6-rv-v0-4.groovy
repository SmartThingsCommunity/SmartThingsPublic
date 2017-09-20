/*
 * V 0.4 of Aeon Smart Switch 6 code 10/5/2015
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * some code used from various SmartThings device type and metering code from ElasticDev
 *
 * change log:
 * v 0.2 - added support for secure inclusion and command encapsulation
 * v 0.3 - added brightness control and zwave power level node report reporting support (6 second button press)
 * v 0.4 - added color control for the night light behavior
*/

 metadata {
	definition (name: "Aeon Smart Switch 6 - RV v0.4", namespace: "robertvandervoort", author: "Robert Vandervoort") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Actuator"
		capability "Switch"
        capability "Color Control"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "reset"

		fingerprint deviceId: "0x1001", inClusters: "0x98"
        fingerprint inClusters: "0x25,0x26,0x27,0x32,0x2C,0x2B,0x33,0x85,0x56,0x59,0x5A,0x5E,0x70,0x72,0x73,0x85,0x86", outClusters: "0x82"
	}
	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

	for (int i = 0; i <= 10000; i += 1000) {
	    status "power  ${i} W": 
		    new physicalgraph.zwave.Zwave().meterV3.meterReport(scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
	}
	for (int i = 0; i <= 100; i += 10) {
	    status "energy  ${i} kWh":
		    new physicalgraph.zwave.Zwave().meterV3.meterReport(scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
	}
		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"

	}

	// tile definitions
	tiles (scale: 2) {
    	multiAttributeTile(name:"main", type:"generic", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
            	attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            }
            tileAttribute("device.power", key: "SECONDARY_CONTROL") {
                attributeState "default",label:'${currentValue} W'
            }
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh', action:"poll"
		}
		valueTile("current", "device.current", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} A', action:"poll"
		}
        valueTile("voltage", "device.voltage", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} V', action:"poll"
		}
        controlTile("rgbSelector", "device.color", "color", height: 2, width: 2, inactiveLabel: false) {
			state "color", action:"setColor"
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "main","energy","current","voltage"
		details(["main","energy","current","voltage","rgbSelector","reset","refresh","configure"])
	}
	
    preferences { 
		input "ledBehavior", "integer", 
			title: "LED Behavior",
            description: "0=energy tracking, 1=momentary status, 2=night light",
			defaultValue: 0, 
			displayDuringSetup: true,
            range: "0..2"
		input "monitorInterval", "integer", 
			title: "Monitoring Interval", 
			description: "The time interval in seconds for sending device reports", 
			defaultValue: 60, 
            range: "1..4294967295‬",
			required: false, 
			displayDuringSetup: true
		input "ledBrightness", "integer",
        	title: "LED Brightness",
            description: "Set the % brightness of indicator LEDs",
            defaultValue: 50,
            range: "0..100",
            required: false,
            displayduringSetup: true
	}
}

def updated()
{
	if (state.sec && !isConfigured()) {
		// in case we miss the SCSR
		response(configure())
	}
}

def parse(String description)
{
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
			descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x82: 1, 0x85: 2, 0x86: 2])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	// log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x82: 1, 0x85: 2, 0x86: 2])
	state.sec = 1
	// log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	response(configure())
}

def zwaveEvent(physicalgraph.zwave.commands.powerlevelv1.PowerlevelTestNodeReport cmd) {
	log.debug "===Power level test node report received=== ${device.displayName}: statusOfOperation: ${cmd.statusOfOperation} testFrameCount: ${cmd.testFrameCount} testNodeid: ${cmd.testNodeid}"
	def request = [
        physicalgraph.zwave.commands.powerlevelv1.PowerlevelGet()
    ]
    response(commands(request))
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.debug "---CONFIGURATION REPORT V2--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug "---CONFIGURATION REPORT V1--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    def meterTypes = ["Unknown", "Electric", "Gas", "Water"]
    def electricNames = ["energy", "energy", "power", "count",  "voltage", "current", "powerFactor",  "unknown"]
    def electricUnits = ["kWh",    "kVAh",   "W",     "pulses", "V",       "A",       "Power Factor", ""]

    //NOTE ScaledPreviousMeterValue does not always contain a value
    def previousValue = cmd.scaledPreviousMeterValue ?: 0

    def map = [ name: electricNames[cmd.scale], unit: electricUnits[cmd.scale], displayed: state.display]
    switch(cmd.scale) {
        case 0: //kWh
	    previousValue = device.currentValue("energy") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 1: //kVAh
            map.value = cmd.scaledMeterValue
            break;
        case 2: //Watts
            previousValue = device.currentValue("power") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = Math.round(cmd.scaledMeterValue)
            break;
        case 3: //pulses
            map.value = Math.round(cmd.scaledMeterValue)
            break;
        case 4: //Volts
            previousValue = device.currentValue("voltage") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 5: //Amps
            previousValue = device.currentValue("current") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 6: //Power Factor
        case 7: //Unknown
            map.value = cmd.scaledMeterValue
            break;
        default:
            break;
    }
    //Check if the value has changed by more than 5%, if so mark as a stateChange
    //map.isStateChange = ((cmd.scaledMeterValue - previousValue).abs() > (cmd.scaledMeterValue * 0.05))

    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	[name: "switch", value: cmd.value ? "on" : "off", type: "physical", displayed: true, isStateChange: true]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) 
{
    [name: "switch", value: cmd.value ? "on" : "off", type: "physical", displayed: true, isStateChange: true]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	[name: "switch", value: cmd.value ? "on" : "off", type: "digital", displayed: true, isStateChange: true]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinarySet cmd)
{
    [name: "switch", value: cmd.value ? "on" : "off", type: "digital", displayed: true, isStateChange: true]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled: $cmd"
    createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def on() {
	def request = [
		zwave.basicV1.basicSet(value: 0xFF),
	    zwave.basicV1.basicGet(),
		zwave.switchBinaryV1.switchBinaryGet()
	]
    commands(request)
}

def off() {
	def request = [
		zwave.basicV1.basicSet(value: 0x00),
        zwave.basicV1.basicGet(),
		zwave.switchBinaryV1.switchBinaryGet()
	]
    commands(request)
}

def setColor(value) {
	def result = []
	log.debug "setColor: ${value}"
	if (value.hex) {
		def c = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
        result << zwave.configurationV1.configurationSet(parameterNumber: 83, size: 3, configurationValue: [c[0], c[1], c[2]])
	}
	if(value.hex) sendEvent(name: "color", value: value.hex)
    commands(result)
}

def poll() {
	def request = [
		zwave.switchBinaryV1.switchBinaryGet(),
        zwave.basicV1.basicGet(),
		zwave.meterV3.meterGet(scale: 0),	//kWh
        zwave.meterV3.meterGet(scale: 1),	//kVAh
		zwave.meterV3.meterGet(scale: 2),	//Wattage
		zwave.meterV3.meterGet(scale: 4),	//Volts
		zwave.meterV3.meterGet(scale: 5),	//Amps
        zwave.meterV3.meterGet(scale: 6)	//Power Factor
	]
    commands(request)
}

def refresh() {
	def request = [
        zwave.basicV1.basicGet(),
		zwave.switchBinaryV1.switchBinaryGet()
	]
    commands(request)
}

def reset() {
	def request = [
		zwave.meterV3.meterReset(),
		zwave.meterV3.meterGet(scale: 0),	//kWh
        zwave.meterV3.meterGet(scale: 1),	//kVAh
		zwave.meterV3.meterGet(scale: 2),	//Wattage
		zwave.meterV3.meterGet(scale: 4),	//Volts
		zwave.meterV3.meterGet(scale: 5),	//Amps
        zwave.meterV3.meterGet(scale: 6)	//Power Factor
	]
    commands(request)
}

def configure() {
    def monitorInt = 60
		if (monitorInterval) {
			monitorInt=monitorInterval.toInteger()
		}
	def ledBehave = 0
    	if (ledBehavior) {
        	ledBehave=ledBehavior.toInteger()
        }
	def ledBright = 50
    	if (ledBrightness) {
        	ledBright=ledBrightness.toInteger()
		}
    log.debug "Sending configure commands - monitorInterval '${monitorInt}', ledBehavior '${ledBehave}'"
	def request = [
    	// Reset switch configuration to defaults
        // zwave.configurationV1.configurationSet(parameterNumber: 255, size: 1, scaledConfigurationValue: 1),
		// Enable to send notifications to associated devices (Group 1) when the state of Micro Switch’s load changed (0=nothing, 1=hail CC, 2=basic CC report)
        zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, configurationValue: [2]),
        // set LED behavior 0 energy mode 1 momentary display 2 night light
		zwave.configurationV1.configurationSet(parameterNumber: 81, size: 1, scaledConfigurationValue: ledBehave),
		// Set LED brightness
        zwave.configurationV1.configurationSet(parameterNumber: 84, size: 3, configurationValue: [ledBright,ledBright,ledBright]),
        // Which reports need to send in Report group 1
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4|2|1),
		// Which reports need to send in Report group 2
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8),
		// Which reports need to send in Report group 3
		zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 0),
		// Interval to send Report group 1
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: monitorInt),
		// Interval to send Report group 2
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 60),
		// Interval to send Report group 3
		zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 0),
        
        // get LED behavior
		zwave.configurationV1.configurationGet(parameterNumber: 81),
    	// get night light RGB value
		zwave.configurationV1.configurationGet(parameterNumber: 83),
    	// get Energy Mode/momentary indicate LED brightness value
		zwave.configurationV1.configurationGet(parameterNumber: 84),
    	// Which reports need to send in Report group 1
		zwave.configurationV1.configurationGet(parameterNumber: 101),
		// Which reports need to send in Report group 2
		zwave.configurationV1.configurationGet(parameterNumber: 102),
		// Which reports need to send in Report group 3
		zwave.configurationV1.configurationGet(parameterNumber: 103),
    	// Interval to send Report group 1
		zwave.configurationV1.configurationGet(parameterNumber: 111),
		// Interval to send Report group 2
		zwave.configurationV1.configurationGet(parameterNumber: 112),
		// Interval to send Report group 3
		zwave.configurationV1.configurationGet(parameterNumber: 113),
		
		// Can use the zwaveHubNodeId variable to add the hub to the device's associations:
		zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId)
    ]
	commands(request)
}

private setConfigured() {
	updateDataValue("configured", "true")
}

private isConfigured() {
	getDataValue("configured") == "true"
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=500) {
	delayBetween(commands.collect{ command(it) }, delay)
}