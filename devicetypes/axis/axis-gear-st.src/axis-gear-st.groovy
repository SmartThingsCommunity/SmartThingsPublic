import groovy.json.JsonOutput

metadata {
	definition (name: "AXIS Gear ST", namespace: "axis", author: "AXIS Labs", ocfDeviceType: "oic.d.blind", vid: "generic-shade-3") {  
		capability "Window Shade"
		capability "Window Shade Preset"
		capability "Switch Level"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "Actuator"
		capability "Configuration"
		
		// added in for Google Assistant Operability
		capability "Switch"		
		
		//Custom Commandes to achieve 25% increment control
		command "ShadesUp"
		command "ShadesDown"
		
		// command to stop blinds
		command "stop"
		command "getversion"
		
		fingerprint profileID: "0104", manufacturer: "AXIS", model: "Gear", deviceJoinName: "AXIS Window Treatment" //AXIS Gear
		fingerprint profileId: "0104", deviceId: "0202", inClusters: "0000, 0003, 0006, 0008, 0102, 0020, 0001", outClusters: "0019", manufacturer: "AXIS", model: "Gear", deviceJoinName: "AXIS Window Treatment" //AXIS Gear
		fingerprint endpointID: "01, C4", profileId: "0104, C25D", deviceId: "0202", inClusters: "0000, 0003, 0006, 0008, 0102, 0020, 0001", outClusters: "0019", manufacturer: "AXIS", model: "Gear", deviceJoinName: "AXIS Window Treatment" //AXIS Gear

		//ClusterIDs: 0000 - Basic; 0006 - On/Off; 0008 - Level Control; 0102 - Window Covering;
		//Updated 2017-06-21
		//Updated 2017-08-24 - added power cluster 0001 - added battery, level, reporting, & health check
		//Updated 2018-01-04 - Axis Inversion & Increased Battery Reporting interval to 1 hour (previously 5 mins)
		//Updated 2018-01-08 - Updated battery conversion from [0-100 : 00 - 64] to [0-100 : 00-C8] to reflect firmware update
		//Updated 2018-11-01 - added in configure reporting for refresh button, close when press on partial shade icon, update handler to parse between 0-254 as a percentage
		//Updated 2019-06-03 - modified to use Window Covering Cluster Commands and versioning tile and backwards compatibility (firmware and app), fingerprinting enabled
		//Updated 2019-08-09 - minor changes and improvements, onoff state reporting fixed
		//Updated 2019-11-11 - minor changes
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"windowShade", type: "lighting", width: 3, height: 3) {
			tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
				attributeState("open",  label: 'Open',  action:"close", icon:"http://i.imgur.com/4TbsR54.png", backgroundColor:"#ffcc33", nextState: "closing")
				attributeState("partially open", label: 'Partially Open', action:"close", icon:"http://i.imgur.com/vBA17WL.png", backgroundColor:"#ffcc33", nextState: "closing")
				attributeState("closed", label: 'Closed', action:"open",  icon:"http://i.imgur.com/mtHdMse.png", backgroundColor:"#bbbbdd", nextState: "opening")
				attributeState("opening", label: 'Opening', action: "stop", icon: "http://i.imgur.com/vBA17WL.png", backgroundColor: "#ffcc33", nextState: "stopping")
				attributeState("closing", label: 'Closing', action: "stop", icon: "http://i.imgur.com/vBA17WL.png", backgroundColor: "#bbbbdd", nextState: "stopping") 
				attributeState("stopping", label: 'Stopping',  icon: "http://i.imgur.com/vBA17WL.png", backgroundColor: "#ff7777") 
				attributeState("stoppingNS", label: 'Stopping Not Supported',  icon: "http://i.imgur.com/vBA17WL.png", backgroundColor: "#ff7777") 
				attributeState("unknown", label: 'Configuring.... Please Wait', icon:"http://i.imgur.com/vBA17WL.png", backgroundColor: "#ff7777") 
			}
			tileAttribute ("device.level", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "ShadesUp")
				attributeState("VALUE_DOWN", action: "ShadesDown")
			}
		}
		//Added a "doubled" state to toggle states between positions
		standardTile("main", "device.windowShade"){
			state("open", label:'Open', action:"close", icon:"http://i.imgur.com/St7oRQl.png", backgroundColor:"#ffcc33", nextState: "closing")
			state("partially open", label:'Partial', action:"close",  icon:"http://i.imgur.com/y0ZpmZp.png", backgroundColor:"#ffcc33", nextState: "closing")
			state("closed", label:'Closed', action:"open", icon:"http://i.imgur.com/SAiEADI.png", backgroundColor:"#bbbbdd", nextState: "opening")
			state("opening", label: 'Opening', action: "stop", icon: "http://i.imgur.com/y0ZpmZp.png", backgroundColor: "#ffcc33", nextState: "stopping")
			state("closing", label: 'Closing', action: "stop", icon: "http://i.imgur.com/y0ZpmZp.png", backgroundColor: "#bbbbdd", nextState: "stopping")
			state("stopping", label: 'Stopping',  icon: "http://i.imgur.com/y0ZpmZp.png", backgroundColor: "#ff7777") 
			state("stoppingNS", label: 'Stopping Not Supported',  icon: "http://i.imgur.com/y0ZpmZp.png", backgroundColor: "#ff7777") 
			state("unknown", label: 'Configuring', icon:"http://i.imgur.com/y0ZpmZp.png", backgroundColor: "#ff7777") 
		}
		controlTile("mediumSlider", "device.level", "slider",decoration:"flat",height:2, width: 2, inactiveLabel: true) {
			state("level", action:"switch level.setLevel")
		}
		standardTile("contPause", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "stop", label:"", icon:'st.sonos.stop-btn', action:'stop'
		}
		valueTile("battery", "device.battery", inactiveLabel:false, decoration:"flat", width:2, height:1) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("refresh", "device.refresh", inactiveLabel:false, decoration:"flat", width:2, height:1) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("version", "device.version", inactiveLabel:false, decoration:"flat", width:4, height:2) {
			state "version", label:'Version: ${currentValue}', unit:"", action: 'getversion'
		}
		standardTile("home", "device.level", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Preset", action:"presetPosition", icon:"st.Home.home2"
		}
		preferences {
			input "preset", "number", title: "Preset position", description: "Set the window shade preset position", defaultValue: 50, required: false, displayDuringSetup: true, range:"1..100"
		}
		
		main(["main"])
		details(["windowShade", "mediumSlider", "contPause", "home", "version", "battery", "refresh"])
	}
}

//Declare Clusters
private getCLUSTER_BASIC() {0x0000}
private getBASIC_ATTR_SWBUILDID() {0x4000}

private getCLUSTER_POWER() {0x0001}
private getPOWER_ATTR_BATTERY() {0x0021}

private getCLUSTER_ONOFF() {0x0006}
private getONOFF_ATTR_ONOFFSTATE() {0x0000}

private getCLUSTER_LEVEL() {0x0008}
private getLEVEL_ATTR_LEVEL() {0x0000}
private getLEVEL_CMD_STOP() {0x03}

private getCLUSTER_WINDOWCOVERING() {0x0102}
private getWINDOWCOVERING_ATTR_LIFTPERCENTAGE() {0x0008}
private getWINDOWCOVERING_CMD_OPEN() {0x00}
private getWINDOWCOVERING_CMD_CLOSE() {0x01}
private getWINDOWCOVERING_CMD_STOP() {0x02}
private getWINDOWCOVERING_CMD_GOTOLIFTPERCENTAGE() {0x05}

private getMIN_WINDOW_COVERING_VERSION() {1093}

//Custom command to increment blind position by 25 %
def ShadesUp() {
	def shadeValue = device.latestValue("level") as Integer ?: 0 
	
	if (shadeValue < 100) {
		shadeValue = Math.min(25 * (Math.round(shadeValue / 25) + 1), 100) as Integer
	}
	else { 
		shadeValue = 100
	}
	//sendEvent(name:"level", value:shadeValue, displayed:true)
	setLevel(shadeValue)
	//sendEvent(name: "windowShade", value: "opening")
}

//Custom command to decrement blind position by 25 %
def ShadesDown() {
	def shadeValue = device.latestValue("level") as Integer ?: 0 
	
	if (shadeValue > 0) {
		shadeValue = Math.max(25 * (Math.round(shadeValue / 25) - 1), 0) as Integer
	}
	else { 
		shadeValue = 0
	}
	//sendEvent(name:"level", value:shadeValue, displayed:true)
	setLevel(shadeValue)
	//sendEvent(name: "windowShade", value: "closing")
}

def stop() {
	log.info "stop()"
	def shadeState = device.latestValue("windowShade")
	if (shadeState == "opening" || shadeState == "closing") {
		if (state.currentVersion >= MIN_WINDOW_COVERING_VERSION){
			sendEvent(name: "windowShade", value: "stopping")
			return zigbee.command(CLUSTER_WINDOWCOVERING, WINDOWCOVERING_CMD_STOP)
		}
		else {
			sendEvent(name: "windowShade", value: "stoppingNS")
			return zigbee.readAttribute(CLUSTER_LEVEL, LEVEL_ATTR_LEVEL, [delay:5000])
		}
	}
	else {
		if (state.currentVersion >= MIN_WINDOW_COVERING_VERSION){
			return zigbee.readAttribute(CLUSTER_WINDOWCOVERING, WINDOWCOVERING_ATTR_LIFTPERCENTAGE)    
		}
		else {
			sendEvent(name: "windowShade", value: "stoppingNS")
			return zigbee.readAttribute(CLUSTER_LEVEL, LEVEL_ATTR_LEVEL, [delay:5000])  
		}
	}
}

def pause() {
	stop()
}

//Send Command through setLevel()
def on() {
	log.info "on()"
	sendEvent(name: "windowShade", value: "opening")
	sendEvent(name: "switch", value: "on")
	
	if (state.currentVersion >= MIN_WINDOW_COVERING_VERSION) {
		zigbee.command(CLUSTER_WINDOWCOVERING, WINDOWCOVERING_CMD_OPEN)
	}
	else {
		setLevel(100)
	}
}

//Send Command through setLevel()
def off() {
	log.info "off()"
	sendEvent(name: "windowShade", value: "closing")
	sendEvent(name: "switch", value: "off")
	close()
	//zigbee.off()
}

//Command to set the blind position (%) and log the event
def setLevel(value, rate=null) {
	log.info "setLevel ($value)"
	
	Integer currentLevel = state.level
	
	def i = value as Integer
	sendEvent(name:"level", value: value, displayed:true)
	
	if ( i == 0) {
		sendEvent(name: "switch", value: "off")
	}
	else {
		sendEvent(name: "switch", value: "on")
	}
	
	if (i > currentLevel) {
		sendEvent(name: "windowShade", value: "opening")
	}
	else if (i < currentLevel) {
		sendEvent(name: "windowShade", value: "closing")
	}
	//setWindowShade(i)
	
	if (state.currentVersion >= MIN_WINDOW_COVERING_VERSION){
		zigbee.command(CLUSTER_WINDOWCOVERING,WINDOWCOVERING_CMD_GOTOLIFTPERCENTAGE, zigbee.convertToHexString(100-i,2))
	}
	else {
		zigbee.setLevel(i)
	}
}

//Send Command through setLevel()
def open() {
	log.info "open()"
	sendEvent(name: "windowShade", value: "opening")
	if (state.currentVersion >= MIN_WINDOW_COVERING_VERSION){
		zigbee.command(CLUSTER_WINDOWCOVERING, WINDOWCOVERING_CMD_OPEN)
	}
	else {
		setLevel(100)
	}     
}
//Send Command through setLevel()
def close() {
	log.info "close()"
	sendEvent(name: "windowShade", value: "closing")
	if (state.currentVersion >= MIN_WINDOW_COVERING_VERSION){
		zigbee.command(CLUSTER_WINDOWCOVERING, WINDOWCOVERING_CMD_CLOSE)
	}
	else {
		setLevel(0)
	}
}

def presetPosition() {
	log.info "presetPosition()"
	setLevel(preset ?: state.preset ?: 50)
}

//Reporting of Battery & position levels
def ping(){
	log.debug "Ping() "
	return refresh()
}

//Set blind State based on position (which shows appropriate image) 
def setWindowShade(value) {
	if ((value>0)&&(value<99)){
		sendEvent(name: "windowShade", value: "partially open", displayed:true)
	}
	else if (value >= 99) {
		sendEvent(name: "windowShade", value: "open", displayed:true)
	}
	else {
		sendEvent(name: "windowShade", value: "closed", displayed:true)
	}
}

//Refresh command
def refresh() {
	log.debug "parse() refresh"
	def cmds_refresh = null
	
	if (state.currentVersion >= MIN_WINDOW_COVERING_VERSION){
		cmds_refresh = zigbee.readAttribute(CLUSTER_WINDOWCOVERING, WINDOWCOVERING_ATTR_LIFTPERCENTAGE)
	}
	else {
		cmds_refresh = zigbee.readAttribute(CLUSTER_LEVEL, LEVEL_ATTR_LEVEL)

	}
	
	cmds_refresh = cmds_refresh + 
					zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY) +
					zigbee.readAttribute(CLUSTER_BASIC, BASIC_ATTR_SWBUILDID)
	
	log.info "refresh() --- cmds: $cmds_refresh"
	
	return cmds_refresh
}

def getversion () {
	//state.currentVersion = 0
	sendEvent(name: "version", value: "Checking Version ... ")     
	return zigbee.readAttribute(CLUSTER_BASIC, BASIC_ATTR_SWBUILDID)
}

//configure reporting
def configure() {   
	state.currentVersion = 0
	sendEvent(name: "windowShade", value: "unknown")
	log.debug "Configuring Reporting and Bindings."
	sendEvent(name: "checkInterval", value: (2 * 60 * 60 + 10 * 60), displayed: true, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	sendEvent(name: "supportedWindowShadeCommands", value: JsonOutput.toJson(["open", "close", "pause"]), displayed: false)

	def attrs_refresh = zigbee.readAttribute(CLUSTER_BASIC, BASIC_ATTR_SWBUILDID) +
						zigbee.readAttribute(CLUSTER_WINDOWCOVERING, WINDOWCOVERING_ATTR_LIFTPERCENTAGE) +
						zigbee.readAttribute(CLUSTER_ONOFF, ONOFF_ATTR_ONOFFSTATE) +
						zigbee.readAttribute(CLUSTER_LEVEL, LEVEL_ATTR_LEVEL) +
						zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY)
						
	def cmds = zigbee.configureReporting(CLUSTER_WINDOWCOVERING, WINDOWCOVERING_ATTR_LIFTPERCENTAGE, 0x20, 1, 3600, 0x00) + 
				zigbee.configureReporting(CLUSTER_ONOFF, ONOFF_ATTR_ONOFFSTATE, 0x10, 1, 3600, 0x00) +
				zigbee.configureReporting(CLUSTER_LEVEL, LEVEL_ATTR_LEVEL, 0x20, 1, 3600, 0x00) +
				zigbee.configureReporting(CLUSTER_POWER, POWER_ATTR_BATTERY, 0x20, 1, 3600, 0x01)
				
	log.info "configure() --- cmds: $cmds"
	return attrs_refresh + cmds
}

def parse(String description) {
	log.trace "parse() --- description: $description"
	
	Map map = [:]

	def event = zigbee.getEvent(description)
	if (event && description?.startsWith('on/off')) {
		log.trace "sendEvent(event)"
		sendEvent(event)
	}
	
	else if ((description?.startsWith('read attr -')) || (description?.startsWith('attr report -'))) {
		map = parseReportAttributeMessage(description)
		def result = map ? createEvent(map) : null
		log.debug "parse() --- returned: $result"
		return result
	}	
}

private Map parseReportAttributeMessage(String description) {
	Map descMap = zigbee.parseDescriptionAsMap(description)
	Map resultMap = [:]
	if (descMap.clusterInt == CLUSTER_POWER && descMap.attrInt == POWER_ATTR_BATTERY) {
		resultMap.name = "battery"
		def batteryValue = Math.round((Integer.parseInt(descMap.value, 16))/2)
		log.debug "parseDescriptionAsMap() --- Battery: $batteryValue"
		if ((batteryValue >= 0)&&(batteryValue <= 100)){
			resultMap.value = batteryValue
		}
		else {
			resultMap.value = 0
		}
	}
	else if (descMap.clusterInt == CLUSTER_WINDOWCOVERING && descMap.attrInt == WINDOWCOVERING_ATTR_LIFTPERCENTAGE && state.currentVersion >= MIN_WINDOW_COVERING_VERSION) {
		//log.debug "parse() --- returned windowcovering :$state.currentVersion "
		resultMap.name = "level"
		def levelValue = 100 - Math.round(Integer.parseInt(descMap.value, 16))
		//Set icon based on device feedback for the  open, closed, & partial configuration
		resultMap.value = levelValue
		state.level = levelValue
		setWindowShade(levelValue)
	}
	else if (descMap.clusterInt == CLUSTER_LEVEL && descMap.attrInt == LEVEL_ATTR_LEVEL) {
		//log.debug "parse() --- returned level :$state.currentVersion "
		def currentLevel = state.level 
		
		resultMap.name = "level"
		def levelValue = Math.round(Integer.parseInt(descMap.value, 16))
		def levelValuePercent = Math.round((levelValue/255)*100)
		//Set icon based on device feedback for the  open, closed, & partial configuration
		resultMap.value = levelValuePercent
		state.level = levelValuePercent
		
		if (state.currentVersion >= MIN_WINDOW_COVERING_VERSION) {
			//Integer currentLevel = state.level
			sendEvent(name:"level", value: levelValuePercent, displayed:true)
			
			if (levelValuePercent > currentLevel) {
				sendEvent(name: "windowShade", value: "opening")
			} else if (levelValuePercent < currentLevel) {
				sendEvent(name: "windowShade", value: "closing")
			}
		}
		else {
			setWindowShade(levelValuePercent)
		}
	}
	else if (descMap.clusterInt == CLUSTER_BASIC && descMap.attrInt == BASIC_ATTR_SWBUILDID) {
		resultMap.name = "version"
		def versionString = descMap.value
		
		StringBuilder output = new StringBuilder("")
		StringBuilder output2 = new StringBuilder("")
		
		for (int i = 0; i < versionString.length(); i += 2) {
			String str = versionString.substring(i, i + 2)
			output.append((char) (Integer.parseInt(str, 16)))   
			if (i > 19) {
				output2.append((char) (Integer.parseInt(str, 16)))
			}
		} 
		
		def current = Integer.parseInt(output2.toString())
		state.currentVersion = current
		resultMap.value = output.toString()   
	}
	else {
		log.debug "parseReportAttributeMessage() --- ignoring attribute"
	}
	return resultMap
}
