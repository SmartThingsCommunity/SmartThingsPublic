import physicalgraph.zigbee.clusters.iaszone.ZoneStatus


metadata {
        definition (name: "GatorSystem HomeWatcher", namespace: "GS", author: "GS_coder000") {

	capability "Motion Sensor"
	capability "Battery"
	capability "Contact Sensor"
    capability "Presence Sensor"
      
    command "stopMotion"  
    command "resetOccupancy"
      
        fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000, 0500", outClusters: "0502", manufacturer: "GatorSystem", model: "GSHW01"
      }

       tiles(scale: 2) {

       
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
		  
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'intrusion', icon:"st.motion.motion.active",  action: "stopMotion", backgroundColor:"#53a7c0"
				attributeState "inactive", label:'monitoring', icon:"st.motion.motion.inactive", action: "refresh", backgroundColor:"#ffffff"
			}
		}
        
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        valueTile("occupancy", "device.presence", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "presence", label:'${currentValue}', unit:""
		}		
        valueTile("contact", "device.contact", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "contact", label:'${currentValue}', unit:""
		}	

      main(["motion"])
      details(["motion", "battery", "occupancy", "contact"])
	}
}

def parse(String description) {
	log.debug "${device.displayName} description: $description"

	Map map = [:]
    List list = []
	if (description?.startsWith('catchall:')) { //raw commands that smartthings does not or cannot interpret
	  list = parseCatchAllMessage(description)    
	}
	else if (description?.startsWith('read attr -')) {
	  map = parseReportAttributeMessage(description)
	}
	else if (description?.startsWith('zone status')) {
	  map = parseIasMessage(description)
	}

	log.debug "Parse returned map $map"
    log.debug "Parse returned list $list"
    
    if (map != null) {    
		def result = map ? createEvent(map) : null
        return result
    } else {
   		return list
    }
}

private List parseCatchAllMessage(String description) {
	List result = []
	return result
}

private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	log.debug "Desc Map: $descMap"

	Map resultMap = [:]
	if (descMap.cluster == "0500" && descMap.attrId == "0002") {
	  def value = descMap.value.endsWith("01") ? "active" : "inactive"  //01: alarm 1; 02: alarm 2; 
		resultMap = getMotionResult(value)
	}

	return resultMap
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	Map resultMap = [:]

    def seconds01 = 60
    def seconds02 = 120
    if (zs.isAlarm1Set()) {
    	runIn(seconds02, stopMotion) 
    	resultMap = getMotionResult('active')  
    } else if (zs.isBatterySet()){
    	resultMap = getBatteryResult(10)
    } else if (description.contains('0x8000')) {
	    runIn(seconds01, resetOccupancy)    
    	resultMap = getMotionResult('occupied') 
    } else if (description.contains('0x4000')) {    
    	resultMap = getMotionResult('openned') 
    } else if (description.contains('0x2000')) {    
    	resultMap = getMotionResult('closed')  	 	
    } else if (description.contains('0x1000')) {
    	resultMap = getBatteryResult(100)
    } else if (description.contains('0x0800')) {
    	resultMap = getBatteryResult(0)
    }  
}

private Map getBatteryResult(rawValue) {
	log.debug "Battery rawValue = ${rawValue}"
    sendEvent(name:"battery", value:rawValue)
	return result
}

private Map getMotionResult(value) {
	if (value == 'active') {
        log.debug 'intrusion'
        String descriptionText = "{{ device.displayName }} detected intrusion"
        return [
            name: 'motion',
            value: value,
            descriptionText: descriptionText,
            translatable: true
        ]
    } else if (value == 'occupied') {
    	log.debug 'detected occupancy'
        String descriptionText = "{{ device.displayName }} detected occupant"
		return [
            name: 'presence',
            value: "present",
            descriptionText: descriptionText,
            translatable: true
        ]
    } else if (value == 'openned') {
    	log.debug 'detected window openned'
        String descriptionText = "{{ device.displayName }} detected window openned"
	    return [
            name: 'contact',
            value: "open",
            descriptionText: descriptionText,
            translatable: true
        ]
    } else if (value == 'closed') {
    	log.debug 'detected  window closed'
        String descriptionText = "{{ device.displayName }} detected window closed"
        return [
            name: 'contact',
            value: "closed",
            descriptionText: descriptionText,
            translatable: true
        ]
    }
}

def installed() {
	initialize()
}
def updated() {
	initialize()
}

def initialize() {
	sendEvent(name:"motion", value:"inactive")
	sendEvent(name:"battery", value:"100")
    sendEvent(name:"presence", value:"not present")
	sendEvent(name:"contact", value:"closed")
}


def stopMotion() {
	if (device.currentState('motion')?.value == "active") {
		sendEvent(name:"motion", value:"inactive", isStateChange: true)
		log.debug "${device.displayName} reset to monitoring; motion cleared manually"
	}
}


def resetOccupancy() {
	def seconds = 60
    if (device.currentState('presence')?.value == "present") {
		sendEvent(name:"presence", value:"not present", isStateChange: true)
		log.debug "${device.displayName} reset to sensing after ${seconds} seconds"
	}
}