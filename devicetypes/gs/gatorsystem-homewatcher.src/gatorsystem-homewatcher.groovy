import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
metadata {
	definition (name: "GatorSystem HomeWatcher", namespace: "GS", author: "GS_coder000") {
	capability "Motion Sensor"
	capability "Battery"
	capability "Contact Sensor"
	capability "Presence Sensor"   
	// Raw Description
        fingerprint deviceJoinName: "GatorSystem Multipurpose Sensor", manufacturer: "GatorSystem", model: "GSHW01"
	}
}
def parse(String description) {
	log.debug "${device.displayName} description: $description"
	Map map = [:]
	if (description?.startsWith('catchall:')) { //raw commands that smartthings does not or cannot interpret
	  map = zigbee.parseDescriptionAsMap(description)   
	}
	else if (description?.startsWith('read attr -')) {
	  map = zigbee.parseDescriptionAsMap(description)
	}
	else if (description?.startsWith('zone status')) {
	  map = parseIasMessage(description)
	}
	log.debug "Parse returned map $map"
	if (map != null) {    
		def result = map ? createEvent(map) : null
        	return result
	}
}
private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	Map resultMap = [:]
	def seconds01 = 60
    	def seconds02 = 120
	def resultOne = '0x8000'
	def resultTwo = '0x4000'
	def resultThree = '0x2000'
	def resultFour = '0x1000'
	def resultFive = '0x0800'
    	if (zs.isAlarm1Set()) {
    		runIn(seconds02, stopMotion) 
    		resultMap = getMotionResult('active')  
 	} else if (zs.isBatterySet()){
    		resultMap = getBatteryResult(10)
	} else if (description.contains(resultOne)) {
		runIn(seconds01, resetOccupancy)    
    		resultMap = getMotionResult('occupied') 
    	} else if (description.contains(resultTwo)) {    
    		resultMap = getMotionResult('openned') 
    	} else if (description.contains(resultThree)) {    
    		resultMap = getMotionResult('closed')  	 	
    	} else if (description.contains(resultFour)) {
    		resultMap = getBatteryResult(100)
    	} else if (description.contains(resultFive)) {
    		resultMap = getBatteryResult(0)
    	}  
}
private Map getBatteryResult(rawValue) {
	log.debug "Battery rawValue = ${rawValue}"
    	createEvent(name:"battery", value:rawValue)
}
private Map getMotionResult(value) {
	if (value == 'active') {
        log.debug 'intrusion'
        String descriptionText = "{{ device.displayName }} detected intrusion"
        createEvent(
            name: 'motion',
            value: value,
            descriptionText: descriptionText,
            translatable: true
        )
    } else if (value == 'occupied') {
    	log.debug 'detected occupancy'
        String descriptionText = "{{ device.displayName }} detected occupant"
	createEvent(
            name: 'presence',
            value: "present",
            descriptionText: descriptionText,
            translatable: true
	)
    } else if (value == 'openned') {
    	log.debug 'detected window openned'
        String descriptionText = "{{ device.displayName }} detected window openned"
	createEvent(
            name: 'contact',
            value: "open",
            descriptionText: descriptionText,
            translatable: true
        )
    } else if (value == 'closed') {
    	log.debug 'detected  window closed'
        String descriptionText = "{{ device.displayName }} detected window closed"
        createEvent(
            name: 'contact',
            value: "closed",
            descriptionText: descriptionText,
            translatable: true
	)
    }
}
def installed() {
	initialize()
}
def initialize() {
	sendEvent(name:"motion", value:"inactive")
	sendEvent(name:"battery", value:"100")
        sendEvent(name:"presence", value:"not present")
	sendEvent(name:"contact", value:"closed")
}
