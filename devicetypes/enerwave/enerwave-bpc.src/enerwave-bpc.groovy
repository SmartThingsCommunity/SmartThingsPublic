metadata {
	// Automatically generated. Make future change here.
	definition (name: "Enerwave-BPC", namespace: "enerwave", author: "enerwave") {
		capability "Motion Sensor"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"

		fingerprint deviceId: "0x2001", inClusters: "0x30,0x80,0x84,0x70,0x85,0x72,0x86"
	}

	simulator {
		// messages the device returns in response to commands it receives
		status "motion (basic)"     : "command: 2001, payload: FF"
		status "no motion (basic)"  : "command: 2001, payload: 00"
		status "motion (binary)"    : "command: 3003, payload: FF"
		status "no motion (binary)" : "command: 3003, payload: 00"

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
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main(["motion"])
		details(["motion", "battery", "configure"])
	}
	preferences { 
        input "PirTime", "number", title: "PirTime(0-255)", description:"1"
    }
}

// Parse incoming device messages to generate events
def parse(String description)
{
	def result = []
	def cmd = zwave.parse(description, [0x30: 1, 0x84: 2])
	if (cmd) {
		if( cmd.CMD == "8407" ) { 			
        	if(state.configChange == 1){		
            	//if(((settings.PirTime?:("1")) as short) < 127){
					result << response(zwave.configurationV1.configurationSet(parameterNumber: 0, size:1, configurationValue:[(settings.PirTime?:("1")) as int]).format())
                //}
                state.configChange = 0
			}
			//result << "relay 500"
            result << response(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId).format())
			result << response(zwave.batteryV1.batteryGet().format())
			result << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
			
        }
		result << createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result}"
	return result
}

// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	[descriptionText: "${device.displayName} woke up", isStateChange: false]
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

def updated() {
	log.debug "Updata"
    
    state.configChange = 1;
	[:]
}

def configure() {
	state.configChange = 1;
	/*delayBetween([
		// send binary sensor report instead of basic set for motion
		zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: 2).format(),

		// send no-motion report 15 seconds after motion stops
		zwave.configurationV1.configurationSet(parameterNumber: 3, size: 2, scaledConfigurationValue: 15).format(),

		// send all data (temperature, humidity, illuminance & battery) periodically
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 225).format(),

		// set data reporting period to 5 minutes
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 300).format()
	])*/
	[:]
}
