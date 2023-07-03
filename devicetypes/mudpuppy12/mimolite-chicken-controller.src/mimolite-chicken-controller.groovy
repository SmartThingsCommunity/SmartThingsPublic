/**
 *  MimoLite Chicken Coop Light Controller
 *
 *
 *  This device type file will configure a Fortrezz MimoLite Wireless Interface/Bridge Module as a chicken coop Light switch and
 *  door position reporter.
 *
 *
 *  This device type will also set the atttibute "powered" to "powerOn" or "powerOff" accordingly. This uses
 *  the alarm capability of the MimoLite and the status will be displayed to the user on a secondary tile. User
 *  can subscribe to the status of this atttribute to be notified when power drops out.
 *
 *  This device type implements a "Configure" action tile which will set the relay switch to 'latching' with no timeout, disable the input to
 *  relay mapping, and turn on the powerout alarm.
 *
 *  This device is used to switch on 12v solar lighting at the chicken coop, and detect if the coop door is open or closed.
 */
 
metadata {
	definition (name: "MimoLite Chicken Controller", namespace: "Mudpuppy12", author: "Dennis DeMarco") {
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Contact Sensor"
		capability "Relay Switch"
		attribute "powered", "string"

        fingerprint deviceId: "0x1000", inClusters: "0x72,0x86,0x71,0x30,0x31,0x35,0x70,0x85,0x25,0x03"
	}
    
	simulator {
		status "on": "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"

		// status messages
		status "open": "command: 2001, payload: FF"
		status "closed": "command: 2001, payload: 00"
	}

	// UI tile definitions 
	tiles {
    
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
            state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        }
        
        standardTile("contact", "device.contact", inactiveLabel: false) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
		}
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("powered", "device.powered", inactiveLabel: false) {
			state "powerOn", label: "Power On", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "powerOff", label: "Power Off", icon: "st.switches.switch.off", backgroundColor: "#ffa81e"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		main ("switch")
		details(["switch","contact", "powered", "refresh", "configure"])
	}
}

def parse(String description) {
log.debug "description is: ${description}"

	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x30: 1, 0x70: 1])
        
    if (cmd.CMD == "7105") {				//Mimo sent a power loss report
    	log.debug "Device lost power"
    	sendEvent(name: "powered", value: "powerOff", descriptionText: "$device.displayName lost power")
    } else {
    	sendEvent(name: "powered", value: "powerOn", descriptionText: "$device.displayName regained power")
    }
    
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def sensorValueEvent(Short value) {
	if (value) {
        sendEvent(name: "contact", value: "open",descriptionText: "$device.displayName is open")
	} else {
        sendEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off", type: "digital"]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	sensorValueEvent(cmd.sensorValue)
}



def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd)
{
    log.debug "We lost power" //we caught this up in the parse method. This method not used.
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def configure() {
	log.debug "Configuring...." //setting up to monitor power alarm and actuator duration
	delayBetween([
		zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(),
        zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 11, size: 1).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 11).format()
	])
    
    	delayBetween([
		zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(),
        zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 3).format()
	])
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}
def poll() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}

def refresh() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}