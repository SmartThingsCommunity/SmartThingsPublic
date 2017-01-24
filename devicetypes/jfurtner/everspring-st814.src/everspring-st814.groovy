metadata {
	// originally from https://community.smartthings.com/t/anyone-using-a-temperature-sensor-other-than-multi-ones/805/20
	definition (name: "EverSpring ST814", namespace: "jfurtner", author: "@Ben") {
		capability "Battery"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
        capability "Configuration"
        capability "Alarm"
        capability "Sensor"

		fingerprint deviceId: "0x2101", inClusters: "0x31,0x60,0x86,0x72,0x85,0x84,0x80,0x70,0x20,0x71"
        //0x31	COMMAND_CLASS_SENSOR_MULTILEVEL_V2
        //0x60	COMMAND_CLASS_MULTI_CHANNEL_V2
        //0x86	COMMAND_CLASS_VERSION
        //0x72	COMMAND_CLASS_MANUFACTURER_SPECIFIC
        //0x85	COMMAND_CLASS_ASSOCIATION_V2
        //0x84	COMMAND_CLASS_WAKE_UP_V2
        //0x80	COMMAND_CLASS_BATTERY
        //0x70	COMMAND_CLASS_CONFIGURATION_V2
        //0x20	COMMAND_CLASS_BASIC
        //0x71	COMMAND_CLASS_ALARM
	}

	simulator {
		// messages the device returns in response to commands it receives

		for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1).incomingMessage()
		}

		for (int i = 0; i <= 100; i += 20) {
			status "humidity ${i}%": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 0, sensorType: 5).incomingMessage()
		}

		for (int i = 0; i <= 100; i += 20) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
				batteryLevel: i).incomingMessage()
		}
	}

	tiles {
		valueTile("temperature", "device.temperature", inactiveLabel: false) {
			state "temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "humidity", label:'${currentValue}% humidity', unit:""
		}

		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main(["temperature", "humidity"])
		details(["temperature", "humidity", "battery", "configure"])
	}
}

// Parse incoming device messages to generate events
def parse(String description)
{
    //??  POWER_APPLIED
    //0x31	SENSOR_MULTILEVEL_REPORT
    //0x71	ALARM_REPORT
    //0x80	BATTERY_REPORT_COMMAND    
    //0x84	COMMAND_CLASS_WAKE_UP_V2

    def parsedZwEvent = zwave.parse(description, [0x31: 2, 0x71: 1, 0x84: 2, 0x80: 1])
	def zwEvent = zwaveEvent(parsedZwEvent)
	def result = []

	result << createEvent( zwEvent )

	if( parsedZwEvent.CMD == "8407" ) {
		def lastStatus = device.currentState("battery")
		def ageInMinutes = lastStatus ? (new Date().time - lastStatus.date.time)/60000 : 600
		log.debug "Battery status was last checked ${ageInMinutes} minutes ago"

		if (ageInMinutes >= 600) {
			log.debug "Battery status is outdated, requesting battery report"
			result << new physicalgraph.device.HubAction(zwave.batteryV1.batteryGet().format())
		}
		result << new physicalgraph.device.HubAction(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	}

	log.debug "Parse returned ${result}"
	return result
}

// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	[descriptionText: "${device.displayName} woke up", isStateChange: false]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	log.debug "SensorMultilevelReport cmd: ${cmd.toString()}}"

	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = "temperature"
			break;
		case 5:
                        // humidity
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "%"
			map.name = "humidity"
			break;
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
	map.unit = "%"
	map.displayed = false
	map
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Catchall reached for cmd: ${cmd.toString()}}"
	[:]
}

def configure() {
	delayBetween([
       	// report in every 15 minutes
        zwave.configurationV1.configurationSet(parameterNumber: 6, size: 2, scaledConfigurationValue: 15).format(),

    	// report a temperature change of 1 degree C
        zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: 1).format(),

        // report a humidity change of 5 percent
        zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: 5).format()
	]) 
}