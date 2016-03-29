metadata {
	definition (name: "Z-Wave Garage Door Sensor Button", namespace: "LeBlaaanc", author: "Chris LeBlanc") {
		capability "Garage Door Control"
		capability "Contact Sensor"
		capability "Actuator"
		capability "Sensor"
		capability "Battery"

		attribute "status", "string"
		attribute "buttonPress", "string"

		command "actuate"
	}

	tiles {
		standardTile("status", "device.status", width: 2, height: 2) {
			state("open", label:'${name}', icon:"st.doors.garage.garage-open", action: "actuate", backgroundColor:"#ffa81e", nextState:"closing")
			state("closed", label:'${name}', icon:"st.doors.garage.garage-closed", action: "actuate", backgroundColor:"#79b821", nextState:"opening")
			state("opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e")
			state("closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e")
		}
		standardTile("contact", "device.contact") {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main(["status","contact", "acceleration"])
		details(["status","contact", "acceleration"])
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err 106")) {
		if (state.sec) {
			log.debug description
		} else {
			result = createEvent(
				descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				isStateChange: true,
			)
		}
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "parsed '$description' to $result"
	return result
}

def updated() {
	def cmds = []
	if (!state.MSR) {
		cmds = [
			zwave.manufacturerSpecificV2.manufacturerSpecificGet().format(),
			"delay 1200",
			zwave.wakeUpV1.wakeUpNoMoreInformation().format()
		]
	} else if (!state.lastbat) {
		cmds = []
	} else {
		cmds = [zwave.wakeUpV1.wakeUpNoMoreInformation().format()]
	}
	response(cmds)
}

def configure() {
	delayBetween([
		zwave.manufacturerSpecificV2.manufacturerSpecificGet().format(),
		batteryGetCommand()
	], 6000)
}

def sensorValueEvent(value) {
	def results = []
	if (value) {
		results << createEvent(name: "contact", value: "open", descriptionText: "$device.displayName sensor is open")
        results << createEvent(name: "status", value: "open", unit: "")
        results << createEvent(name: "door", value: "open", unit: "")
	} else {
		results << createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
        results << createEvent(name: "status", value: "closed", unit: "")
        results << createEvent(name: "door", value: "closed", unit: "")
	}
    
    results
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
	def result = []
	if (cmd.notificationType == 0x06 && cmd.event == 0x16) {
		result << sensorValueEvent(1)
	} else if (cmd.notificationType == 0x06 && cmd.event == 0x17) {
		result << sensorValueEvent(0)
	} else if (cmd.notificationType == 0x07) {
		if (cmd.v1AlarmType == 0x07) {  // special case for nonstandard messages from Monoprice door/window sensors
			result << sensorValueEvent(cmd.v1AlarmLevel)
		} else if (cmd.event == 0x01 || cmd.event == 0x02) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x03) {
			result << createEvent(descriptionText: "$device.displayName covering was removed", isStateChange: true)
			result << response(zwave.wakeUpV1.wakeUpIntervalSet(seconds:4*3600, nodeid:zwaveHubNodeId))
			if(!state.MSR) result << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
		} else if (cmd.event == 0x05 || cmd.event == 0x06) {
			result << createEvent(descriptionText: "$device.displayName detected glass breakage", isStateChange: true)
		} else if (cmd.event == 0x07) {
			if(!state.MSR) result << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
			result << createEvent(name: "motion", value: "active", descriptionText:"$device.displayName detected motion")
		}
	} else if (cmd.notificationType) {
		def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
		result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, displayed: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	def event = createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
	def cmds = []
	if (!state.MSR) {
		cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds:4*3600, nodeid:zwaveHubNodeId).format()
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
		cmds << "delay 1200"
	}
	if (!state.lastbat || now() - state.lastbat > 53*60*60*1000) {
		cmds << batteryGetCommand()
	} else {
		cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
	}
	[event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbat = now()
	[createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)

	if (msr == "011A-0601-0901") {  // Enerwave motion doesn't always get the associationSet that the hub sends on join
		result << response(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
	} else if (!device.currentState("battery")) {
		if (msr == "0086-0102-0059") {
			result << response(zwave.securityV1.securityMessageEncapsulation().encapsulate(zwave.batteryV1.batteryGet()).format())
		} else {
			result << response(batteryGetCommand())
		}
	}

	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x85: 2, 0x70: 1])
	// log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		state.sec = 1
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

def batteryGetCommand() {
	def cmd = zwave.batteryV1.batteryGet()
	if (state.sec) {
		cmd = zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd)
	}
	cmd.format()
}

def actuate() {
	log.debug "Sending button press event"
	sendEvent(name: "buttonPress", value: "true", isStateChange: true, unit: "")
}

def open() {
	if (device.currentValue("status") == "closed") {
		log.debug "Sending button press event to open door"
		sendEvent(name: "buttonPress", value: "true", isStateChange: true, unit: "")
	}
	else {
		log.debug "Not opening door since it is already open or transitioning"
	}
}

def close() {
	if (device.currentValue("status") == "open") {
		log.debug "Sending button press event to close door"
		sendEvent(name: "buttonPress", value: "true", isStateChange: true, unit: "")
	}
	else {
		log.debug "Not closing door since it is already closed or transitioning"
	}
}