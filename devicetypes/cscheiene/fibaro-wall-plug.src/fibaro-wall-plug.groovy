/**
 *  Copyright 2015 SmartThings
 * 
 * 	Based on original implementation by SmartThings but fixed some of the issues.
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
	definition (name: "Fibaro Wall Plug", namespace: "cscheiene", author: "SmartThings,gpsmith,cscheiene") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Actuator"
		capability "Switch"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "reset"

		fingerprint deviceId: "0x1000", inClusters: "0x72,0x86,0x70,0x85,0x8E,0x25,0x73,0x32,0x31,0x7A", outClusters: "0x25,0x32,0x31"
                fingerprint mfr:"010F", prod:"0600", model:"1000"
	}

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}



		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"

	}

	tiles (scale: 2) {
    
      	multiAttributeTile(name:"switch", type:"lighting", width:6, height:4, canChangeIcon: true) {
    		tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
              attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
              attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
    		}
        }

    	valueTile("power", "device.power", decoration: "flat", width: 3, height: 2, canChangeIcon: true) {
        	state "power", label:'${currentValue} W'
    	}
		valueTile("energy", "device.energy", decoration: "flat", width: 3, height: 2) {
			state "default", label:'${currentValue} kWh'
		}        
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2,) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","energy","power","refresh","reset","configure"])  
        //details(["switch","energy","reset","refresh","configure"])
	}
    preferences {
        input name: "par16", type: "number", description: "Enter number", required: true,
        title: "Remember device status after power failure. Default: 1\n\n" +
        "0 - Wall Plug does not memorize its state after a power failure. Connected device will be off after the power supply is reconnected\n" +
        "1 - Wall Plug memorizes its state after a power failure."
        
        input name: "par40", type: "number", description: "Enter number", required: true,
        title: "Immediate power report. Default: 80\n\n" +
        "Available settings: 1 - 100 (%)."
                
        input name: "par42", type: "number", description: "Enter number", required: true,
        title: "Standard power reporting. Default: 15\n\n" +
        "Available settings: 1 - 100 (%)"
                        
        input name: "par43", type: "number", description: "Enter number", required: true,
        title: "Standard power reporting frequency. Default: 30\n\n" +
        "Available settings: 1 - 254 (s)"
        
        input name: "par61", type: "number", description: "Enter number", required: true,
        title: "LED ring illumination colour when device is ON. Default: 1\n\n" +
        "Available settings:\n" +
        "0 - LED ring illumination colour changes in predefined steps, depending on power consumption changes\n" +
        "1 - LED ring illumination colour changes continuously, using full spectrum of available colorus, depending on power consumption changes\n" +
        "2 - White illumination\n" +
		"3 - Red illumination\n" +
		"4 - Green illumination\n" +
		"5 - Blue illumination\n" +
		"6 - Yellow illumination\n" +
		"7 - Cyan (Greenish blue) illumination\n" + 
        "8 - Magenta (Purplish red) illumination\n" + 
        "9 - illumination turned off completely"
        
        input name: "par62", type: "number", description: "Enter number", required: true,
        title: "LED ring illumination colour when device is OFF. Default: 8\n\n" +
        "Available settings:\n" +
        "0 - LED ring is illuminated with a color corresponding to the last measured power, before the controlled device was turned off\n" +
        "1 - White illumination\n" +
		"2 - Red illumination\n" +
		"3 - Green illumination\n" +
		"4 - Blue illumination\n" +
		"5 - Yellow illumination\n" +
		"6 - Cyan (Greenish blue) illumination\n" + 
        "7 - Magenta (Purplish red) illumination\n" + 
        "8 - illumination turned off completely"
        
        input name: "par70", type: "number", description: "Enter number", required: true,
        title: "Oveload safety switch. Default: 65 535 (6 553,5W)\n\n" +
        "Available settings:\n" +
        "10 - 65 535 (1W - 6 553,5W)\n" +
        "Value higher than 32 000 (3 200W) turns the overload safety switch off, i.e. this functionality is turned off by default."        
    }
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x32: 1, 0x72: 2])
	if (cmd) {
		log.debug cmd
		result = createEvent(zwaveEvent(cmd))
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
	if (cmd.scale == 0) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
	} else if (cmd.scale == 1) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
	} else if (cmd.scale == 2) {
		createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    if (state.debug) log.debug "SensorMultilevelReport(sensorType:${cmd.sensorType}, scale:${cmd.scale}, precision:${cmd.precision}, scaledSensorValue:${cmd.scaledSensorValue}, sensorValue:${cmd.sensorValue}, size:${cmd.size})"
    def map = [ value: cmd.scaledSensorValue, displayed: true]
    switch(cmd.sensorType) {
        case physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport.SENSOR_TYPE_POWER_VERSION_2: 	// 4
            map.name = "power"
            map.unit = cmd.scale ? "BTU/h" : "W"
            map.value = Math.round(cmd.scaledSensorValue)
            break;
        default:
            map.name = "unknown sensor ($cmd.sensorType)"
            break;
    }
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	[
		name: "switch", value: cmd.value ? "on" : "off", type: "physical"
	]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	[
		name: "switch", value: cmd.value ? "on" : "off", type: "digital"
	]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	//log.debug "Unhandled: ${cmd}"
    [:]
}

def on() {
	[
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000",
		zwave.meterV2.meterGet(scale: 2).format()
	]
}

def off() {
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
		zwave.meterV2.meterGet().format()
	])
}

def refresh() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def reset() {
	return [
		zwave.meterV2.meterReset().format(),
		zwave.meterV2.meterGet().format()
	]
}

def updated() {
response(configure())
}

def configure() {

	log.debug "Sending Configuration to device"
	delayBetween([   
        zwave.configurationV1.configurationSet(parameterNumber: 16, size: 1, scaledConfigurationValue: par16.toInteger()).format(),     // Remember device status after power failure     
        zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: par40.toInteger()).format(),     // Immediate power report. Available settings: 1 - 100 (%). Default 80
        zwave.configurationV1.configurationSet(parameterNumber: 42, size: 1, scaledConfigurationValue: par42.toInteger()).format(), 	// Standard power reporting. Available settings: 1 - 100 (%). Default 15
        zwave.configurationV1.configurationSet(parameterNumber: 43, size: 1, scaledConfigurationValue: par43.toInteger()).format(), 	// Standard power reporting frequency. Available settings: 1 - 254 (s) Default 30
        zwave.configurationV1.configurationSet(parameterNumber: 61, size: 1, scaledConfigurationValue: par61.toInteger()).format(),     // LED Ring when ON
        zwave.configurationV1.configurationSet(parameterNumber: 62, size: 1, scaledConfigurationValue: par62.toInteger()).format(),     // LED Ring when OFF
        zwave.configurationV1.configurationSet(parameterNumber: 70, size: 2, scaledConfigurationValue: par70.toInteger()).format(),     // Oveload safety switch         
        zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format(),
        zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format(),
        zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(),
	], 1500)
}