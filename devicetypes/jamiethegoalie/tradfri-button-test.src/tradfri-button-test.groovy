
metadata {
definition (name: "Tradfri Button Test", namespace: "jamiethegoalie", author: "jamiethegoalie") {	
capability "Battery"
capability "Button"
capability "Holdable Button"
capability "Actuator"
capability "Switch"
capability "Momentary"
capability "Configuration"
capability "Sensor"
capability "Refresh"

	attribute "lastPress", "string"
	attribute "batterylevel", "string"
	attribute "lastCheckin", "string"
fingerprint profileId: "0104", deviceId: "0810", inClusters: "0000, 0001, 0003, 0009, 0B05, 1000", outClusters: "0003, 0004, 0005, 0006, 0008, 0019, 1000", manufacturer: "IKEA of Sweden", model: "lumi.sensor_switch", deviceJoinName: ""
 command getClusters
}


tiles(scale: 2) {

	multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
		tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
       		attributeState("on", label:' push', action: "momentary.push", backgroundColor:"#53a7c0")
        	attributeState("off", label:' push', action: "momentary.push", backgroundColor:"#ffffff", nextState: "on")
		}
        tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
			attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")
        }
	}        

    valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
		state "battery", label:'${currentValue}% battery', unit:""
	}
    /** standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
    } */
	standardTile("refresh", "device.image", inactiveLabel: false, decoration: "flat") {
          state "refresh", action:"getClusters", icon:"st.secondary.refresh"
    }
    standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
	}

	main (["switch"])
	details(["switch", "battery", "refresh", "configure"])
}
}

def parse(String description) {
log.debug "Parsing '${description}'"
// send event for heartbeat 
def now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
sendEvent(name: "lastCheckin", value: now)

def results = []
// if (description?.startsWith('on/off: '))
//	results = parseCustomMessage(description)
if (description?.startsWith('catchall:')) 
results = parseCatchAllMessage(description)

return results;
}

def configure(){
log.debug "Configuring Reporting and Bindings."

// return zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null) +
// zigbee.configureReporting(0x0008, 0x0000, 0x20, 1, 3600, 0x01) +
// zigbee.readAttribute(0x0006, 0x0000) +
// zigbee.readAttribute(0x0008, 0x0000)

 zigbee.configureReporting(0x0000, 0x0000, 0x10, 0, 600, null)
 //zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null)
}

def refresh(){
log.debug "refreshing"
}

private Map parseCatchAllMessage(String description) {
Map resultMap = [:]
def cluster = zigbee.parse(description)
log.debug cluster
if (cluster) {
switch(cluster.clusterId) {
case 0x0000:
resultMap = getBatteryResult(cluster.data.last())
break

		case 0xFC02:
		log.debug 'ACCELERATION'
		break

		case 0x0402:
		log.debug 'TEMP'
			// temp is last 2 data values. reverse to swap endian
			String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
			def value = getTemperature(temp)
			resultMap = getTemperatureResult(value)
			break
	}
}

return resultMap
}

def getClusters() { 
     "zdo active 0x${device.deviceNetworkId}" 
       log.debug "Get Clusters Called";
}