/**
 *  
 *
 *  BeSense Z-Wave Motion Sensor
 *
 *  
 */

metadata {
	definition (name: "BeSense Motion Sensor", namespace: "BeSense Sensors", author: "BeSense") {
		capability "Motion Sensor"
		capability "Sensor"
		capability "Battery"
        capability "Tamper Alert"

    
    

		
		fingerprint mfr: "0214", prod: "0002", model: "0002", deviceJoinName: "Besense Motion Sensor"  // Besense motion sensor
		
	}

	simulator {
		status "inactive": "command: 3003, payload: 00"
		status "active": "command: 3003, payload: FF"
        
       
	}
	
	tiles(scale: 1) {
		standardTile("motion", "device.motion", width: 3, height: 3, canChangeIcon: true) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#ea0f46", nextState:"inactive")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#27CC73", nextState:"active")
			
        }
        	
			
        
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state("battery", label:'${currentValue}% battery', unit:"")
		}
		
		main "motion"
		details(["motion", "battery", "tamper"])
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description)
	} else {
		def cmd = zwave.parse(description, [0x20: 1, 0x30:1, 0x80: 1, 0x84: 1, 0x71: 6, 0x9C: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		} else {
			result = createEvent(value: description, descriptionText: description, isStateChange: false)
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd)
{
def map = [:]
map.name = "tamper"
map.value = cmd.alarmLevel == 255 ? "tampered" : "secured"
if (map.value == "tampered") {
map.value = "tampered"
map.descriptionText = "$device.displayName has been tampered with"
map.isStateChange = "true"
map.display = "true"
log.debug "tampered: $map"
}
else {
map.value = "secured"
map.descriptionText = "$device.displayName is secured"
map.isStateChange = "true"
map.display = "true"
log.debug "secure: $map"

   	}
    
createEvent(map)
}

def sensorValueEvent(value) {
	if (value) {
		createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
	} else {
		createEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion has stopped")
	}
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


def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

	if (!state.lastbat || (new Date().time) - state.lastbat > 53*60*60*1000) {
		result << response(zwave.batteryV1.batteryGet())
	} else {
		result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
	result
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
	state.lastbat = new Date().time
	[createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}