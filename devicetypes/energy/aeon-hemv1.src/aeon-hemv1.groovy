/**
 *  Aeon Home Energy Meter V1
 *
 *  Author: SmartThings
 *  Modified by: PsychoBob
 *  Date: 2015-07-03
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Aeon HEMv1", namespace: "Energy", author: "PsychoBob") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Sensor"
        capability "Refresh"        
        capability "Polling"
        capability "Battery"
        
        attribute "energyCost", "string" 

		command "reset"
        //command "configure"

		fingerprint deviceId: "0x2101", inClusters: " 0x70,0x31,0x72,0x86,0x32,0x80,0x85,0x60"
	}

	// simulator metadata
	simulator {
		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} ": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}
	}

	// tile definitions
	tiles {
		valueTile("power", "device.power") {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat") {
			state "default", label:'${currentValue}'
        }
        valueTile("energyCost", "device.energyCost", decoration: "flat") {
            state "default", label: '${currentValue}'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat") {
			state "default", label:'reset', action:"reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["power","energy","energyCost"])
		details(["power","energy","energyCost", "reset","refresh", "configure"])
	        }
                preferences {
                     input "kWhCost", "string", title: "\$/kWh (0.16)", defaultValue: "0.16" as String
                }
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x31: 1, 0x32: 1, 0x60: 3])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {

    def dispValue
    def newValue
    
	if (cmd.scale == 0) {	
        newValue = cmd.scaledMeterValue
        [name: "energy", value: cmd.scaledMeterValue, unit: ""]
        dispValue = String.format("%5.2f",newValue)
        sendEvent(name: "energy", value: dispValue as String, unit: "")
        state.energyValue = newValue
        BigDecimal costDecimal = newValue * ( kWhCost as BigDecimal)
        def costDisplay = String.format("%5.2f",costDecimal)
        sendEvent(name: "energyCost", value: "\$${costDisplay}", unit: "")
    }
    else if (cmd.scale == 1) {
        [name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]
    }
    else {
        [name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"]
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def refresh() {
	delayBetween([
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def reset() {
	sendEvent(name: "energyCost", value: "Cost\n--", unit: "")
    return [
            zwave.meterV2.meterReset().format(),
            zwave.meterV2.meterGet(scale: 0).format()
   ]
}

def configure() {
	def cmd = delayBetween([
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4).format(),   // combined power in watts
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 20).format(), // every 20s
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8).format(),   // combined energy in kWh
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 20).format(), // every 20s
		zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 0).format(),    // no third report
		zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 20).format() // every 20s
	])
	log.debug cmd
	cmd
}