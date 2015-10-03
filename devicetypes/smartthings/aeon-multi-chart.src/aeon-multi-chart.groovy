metadata {
	// Automatically generated. Make future change here.
	definition (name: "Aeon Multi + Chart", author: "Mark West") {
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Configuration"
		capability "Illuminance Measurement"
		capability "Sensor"
		capability "Battery"

		fingerprint deviceId: "0x2001", inClusters: "0x30,0x31,0x80,0x84,0x70,0x85,0x72,0x86"
	}

	simulator {
		// messages the device returns in response to commands it receives
		status "motion (basic)"     : "command: 2001, payload: FF"
		status "no motion (basic)"  : "command: 2001, payload: 00"
		status "motion (binary)"    : "command: 3003, payload: FF"
		status "no motion (binary)" : "command: 3003, payload: 00"

		for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1).incomingMessage()
		}

		for (int i = 0; i <= 100; i += 20) {
			status "humidity ${i}%": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 0, sensorType: 5).incomingMessage()
		}

		for (int i = 0; i <= 100; i += 20) {
			status "luminance ${i} lux": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 0, sensorType: 3).incomingMessage()
		}
		for (int i = 200; i <= 1000; i += 200) {
			status "luminance ${i} lux": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 0, sensorType: 3).incomingMessage()
		}

		for (int i = 0; i <= 100; i += 20) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
				batteryLevel: i).incomingMessage()
		}
	}

	tiles {
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
			state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
		}
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
        chartTile(name: "temperatureChart", attribute: "device.temperature")
		valueTile("3axis", "device.threeAxis", decoration: "flat", wordWrap: false) {
			state("threeAxis", label:'${currentValue}', unit:"", backgroundColor:"#ffffff")
		}
        chartTile(name: "humidityChart", attribute: "device.humidity")
		valueTile("3axis", "device.threeAxis", decoration: "flat", wordWrap: false) {
			state("threeAxis", label:'${currentValue}', unit:"", backgroundColor:"#ffffff")
		}
        chartTile(name: "illuminanceChart", attribute: "device.illuminance")
		valueTile("3axis", "device.threeAxis", decoration: "flat", wordWrap: false) {
			state("threeAxis", label:'${currentValue}', unit:"", backgroundColor:"#ffffff")
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "humidity", label:'${currentValue}% humidity', unit:""
		}
		valueTile("illuminance", "device.illuminance", inactiveLabel: false) {
			state "luminosity", label:'${currentValue} ${unit}', unit:"lux"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main(["motion", "temperature", "humidity", "illuminance"])
		details(["motion", "temperature","temperatureChart","humidityChart","illuminanceChart", "humidity", "illuminance", "battery", "configure"])
	}
}

// Parse incoming device messages to generate events
def parse(String description)
{
	def result = []
	def cmd = zwave.parse(description, [0x31: 2, 0x30: 1, 0x84: 1])
	if (cmd) {
		if( cmd.CMD == "8407" ) { result << new physicalgraph.device.HubAction(zwave.wakeUpV1.wakeUpNoMoreInformation().format()) }
		result << createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result}"
	return result
}

def getVisualizationData(attribute) {	
	log.debug "getChartData for $attribute"
	def keyBase = "measure.${attribute}"
    log.debug "getChartData state = $state"
	
	def dateBuckets = state[keyBase]
	
	//convert to the right format
	def results = dateBuckets?.sort{it.key}.collect {[
		date: Date.parse("yyyy-MM-dd", it.key),
		average: it.value.average,
		min: it.value.min,
		max: it.value.max
		]}
	
	log.debug "getChartData results = $results"
	results
}

// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	[descriptionText: "${device.displayName} woke up", isStateChange: false]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = "temperature"
			break;
		case 3:
			// luminance
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			map.name = "illuminance"
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

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	def map = [:]
	map.value = cmd.sensorValue ? "active" : "inactive"
	map.name = "motion"
	if (map.value == "active") {
		map.descriptionText = "$device.displayName detected motion"
	}
	else {
		map.descriptionText = "$device.displayName motion has stopped"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	def map = [:]
	map.value = cmd.value ? "active" : "inactive"
	map.name = "motion"
	if (map.value == "active") {
		map.descriptionText = "$device.displayName detected motion"
	}
	else {
		map.descriptionText = "$device.displayName motion has stopped"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Catchall reached for cmd: ${cmd.toString()}}"
	[:]
}

def configure() {
	delayBetween([
		// send binary sensor report instead of basic set for motion
		zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: 2).format(),

		// send no-motion report 15 seconds after motion stops
		zwave.configurationV1.configurationSet(parameterNumber: 3, size: 2, scaledConfigurationValue: 15).format(),

		// send all data (temperature, humidity, illuminance & battery) periodically
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 225).format(),

		// set data reporting period to 5 minutes
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 300).format()
	])
}

