import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
metadata {
	definition (name: "GatorSystem HomeWatcher", namespace: "GS", author: "GS_coder000") {
	capability "Motion Sensor"
	capability "Battery"
	capability "Contact Sensor"
	capability "Presence Sensor"   
		
	// Raw Description 08 0104 0402 00 02 0000 0500 01 0502
	fingerprint manufacturer: "GatorSystem", model: "GSHW01", deviceJoinName: "GatorSystem Multipurpose Sensor"
	}
}

def parse(String description) {
	log.debug "${device.displayName} description: $description"
	Map map = [:]
	if (description?.startsWith('catchall:')) { //raw commands that smartthings does not or cannot interpret
		map = zigbee.parseDescriptionAsMap(description)   
	} else if (description?.startsWith('read attr -')) {
		map = zigbee.parseDescriptionAsMap(description)
	} else if (description?.startsWith('zone status')) {
		map = parseIasMessage(description)
	}
	log.debug "Parse returned map $map"
	if (map != null) {	
		createEvent(map)
	}
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	Map resultMap = [:]
	def oneMinute = 60
	def twoMinutes = 120
	def resultOccupied = '0x8000'
	def resultOpenned = '0x4000'
	def resultClosed = '0x2000'
	def resultBatteryNew = '0x1000'
	def resultBatteryOut = '0x0800'
	if (zs.isAlarm1Set()) {
		runIn(twoMinutes, stopMotion) 
		resultMap = getMotionResult('active')  
	} else if (zs.isBatterySet()) {
		resultMap = getBatteryResult(10)
	} else if (description.contains(resultOccupied)) {
		runIn(oneMinute, resetOccupancy)	
		resultMap = getMotionResult('occupied') 
	} else if (description.contains(resultOpenned)) {
		resultMap = getMotionResult('openned') 
	} else if (description.contains(resultClosed)) {	
		resultMap = getMotionResult('closed')		   
	} else if (description.contains(resultBatteryNew)) {
		resultMap = getBatteryResult(100)
	} else if (description.contains(resultBatteryOut)) {
		resultMap = getBatteryResult(0)
	}  
}

private Map getBatteryResult(rawValue) {
	log.debug "Battery rawValue = ${rawValue}"
	createEvent(name:"battery", value:rawValue)
}

private Map getMotionResult(value) {
	if (value == 'active') {
		log.debug 'detected intrusion'
		String descriptionText = "{{ device.displayName }} detected intrusion"
		createEvent(
			name: 'motion',
			value: value,
			descriptionText: descriptionText,
			translatable: false
		)
	} else if (value == 'occupied') {
		log.debug 'detected occupancy'
		String descriptionText = "{{ device.displayName }} detected occupancy"
		createEvent(
			name: 'presence',
			value: "present",
			descriptionText: descriptionText,
			translatable: false
		)
	} else if (value == 'openned') {
		log.debug 'detected window openned'
		String descriptionText = "{{ device.displayName }} detected window openned"
		createEvent(
			name: 'contact',
			value: "open",
			descriptionText: descriptionText,
			translatable: false
		)
	} else if (value == 'closed') {
		log.debug 'detected window closed'
		String descriptionText = "{{ device.displayName }} detected window closed"
		createEvent(
			name: 'contact',
			value: "closed",
			descriptionText: descriptionText,
			translatable: false
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

def stopMotion() {
	if (device.currentState('motion')?.value == "active") {
		sendEvent(name:"motion", value:"inactive", isStateChange: true)
		log.debug "${device.displayName} reset to monitoring after 120 seconds"
	}
}

def resetOccupancy() {
	if (device.currentState('presence')?.value == "present") {
		sendEvent(name:"presence", value:"not present", isStateChange: true)
		log.debug "${device.displayName} reset to sensing after 60 seconds"
	}
}
