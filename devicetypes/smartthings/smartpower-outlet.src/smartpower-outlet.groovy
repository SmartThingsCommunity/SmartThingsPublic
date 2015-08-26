/**
 *	CentraLite Switch
 *
 *	Author: SmartThings
 *	Date: 2013-12-02
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "SmartPower Outlet", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"

		// indicates that device keeps track of heartbeat (in state.heartbeat)
		attribute "heartbeat", "string"

		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			}
			tileAttribute ("power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'${currentValue} W'
			}
		}
		
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
		main "switch"
		details(["switch","refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "Parse description $description"
	def name = null
	def value = null

	// save heartbeat (i.e. last time we got a message from device)
	state.heartbeat = Calendar.getInstance().getTimeInMillis()

	if (description?.startsWith("read attr -")) {
		def descMap = parseDescriptionAsMap(description)
		log.debug "Read attr: $description"
		if (descMap.cluster == "0006" && descMap.attrId == "0000") {
			name = "switch"
			value = descMap.value.endsWith("01") ? "on" : "off"
		} else if (descMap.cluster.equalsIgnoreCase("0B04") && descMap.attrId.equalsIgnoreCase("050b")) {
			def reportValue = descMap.value
			name = "power"
			//power divisor is 10
			value = Integer.parseInt(reportValue, 16) / 10
		}
	} else if (description?.startsWith("on/off:")) {
		log.debug "Switch command"
		name = "switch"
		value = description?.endsWith(" 1") ? "on" : "off"
	}

	def result = createEvent(name: name, value: value)
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

// Commands to device
def on() {
	'zcl on-off on'
}

def off() {
	'zcl on-off off'
}

def meter() {
	"st rattr 0x${device.deviceNetworkId} 1 0xB04 0x50B"
}

def refresh() {
	sendEvent(name: "heartbeat", value: "alive", displayed:false)
	[
			"st rattr 0x${device.deviceNetworkId} 1 0xB04 0x50B"
	]
}

def configure() {
	def configCmds = [
			"zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 200",
			"zdo bind 0x${device.deviceNetworkId} 1 1 0xB04 {${device.zigbeeId}} {}", "delay 200"
	]
	return configCmds + refresh() // send refresh cmds as part of config
}
