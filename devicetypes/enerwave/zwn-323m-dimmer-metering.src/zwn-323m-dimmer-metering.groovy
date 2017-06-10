metadata {			
	// Automatically generated. Make future change here.		
	definition (name: "ZWN-323M Dimmer Metering", namespace: "enerwave", author: " enerwave ") {		
		capability "Energy Meter"	
	    capability "Switch Level"		
		capability "Actuator"	
		capability "Switch"	
		capability "Power Meter"	
        capability "Polling"			
		capability "Refresh"	
		capability "Sensor"	
        capability "Configuration"
        
        attribute "myEnergyCost","STRING"
        //attribute "powerReportIntervalcfg" ,"NUMBER"
        attribute "configState","ENUM",["configComplete","configing","configFail"]
		command "reset"
        command "test"
        command "getKWhCost"
        command "getLightDirection"
		command "getPowerReportInterval"
		command "getEnergyReportInterval"
		command "getPowerReportByChange"
		command "getPowerReportChangeInterval"
        		
        fingerprint deviceId: "0x1101", inClusters: " 0x25,0x27,0x70,0x86,0x72,0x32,0x85,0x31,0x26"				
	}		
			
	simulator {		
		status "on":  "command: 2003, payload: FF"	
		status "off": "command: 2003, payload: 00"	
		status "09%": "command: 2003, payload: 09"	
		status "10%": "command: 2003, payload: 0A"	
		status "33%": "command: 2003, payload: 21"	
		status "66%": "command: 2003, payload: 42"	
		status "99%": "command: 2003, payload: 63"	
			
		for (int i = 0; i <= 10000; i += 1000) {	
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
			scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()	

		}	
		for (int i = 0; i <= 100; i += 10) {	
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
			scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()

		}	
			
		// reply messages	
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"	
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"	
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"	
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"	
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"	
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"	
	}		
			
	tiles {			    
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
		}
		valueTile("power", "device.power", decoration: "flat") {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat") {
			state "default", label:'${currentValue} kWh'
		}
        valueTile("myEnergyCost","myEnergyCost",decoration: "flat") {	
			state "default", label:'cost: ${currentValue}'
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
		valueTile("lightLevel", "device.level", decoration: "flat") {
			state "default", label:'${currentValue} %'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat") {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("configState", "configState",width: 2 ,decoration: "flat") {
			//state "default", label:'Power Report Every ${currentValue} Min'
            state "configComplete", label:"configComplete"
            state "configing", label:"configing"
            state "configFail", label:"configFail"
		}
        standardTile("configure", "configState", inactiveLabel: false,decoration: "flat") {
        	state "default", label:"", action:"configuration.configure", icon:"st.secondary.configure"
			//state "configComplete", label:"", action:"configuration.configure", icon:"st.secondary.configure"
            //state "configing", label:"",  icon:"st.secondary.configure"
            //state "configFail", label:"", action:"configuration.configure", icon:"st.secondary.configure"
		}

		main "switch"
		details(["switch", "power", "energy","myEnergyCost","reset", "refresh", "levelSliderControl", "lightLevel", "configState","configure"])
	}		
	preferences { 
        input "kWhCost", "number", title: "\$/kWh (1)", description:"1"
        //input "switches", "capability.switch", multiple: true
        input "lightDirection","enum",options: ["same load", "diff load"], title: "light direction", description:"diff load"
        input "powerReportInterval", "number", title: "powerReportInterval (Min) 0~255", description:"0"
        input "energyReportInterval","number", title: "energyReportInterval (Min) 0~255", description:"0"
        input "powerReportByChange","enum",options: ["YES", "NO"], title: "if power report by change", description:"YES"
        input "powerReportChangeInterval","number",options: ["YES", "NO"], title: "power report change interval(0.1W) 0~255", description:"10"
    } 	
}		
        			
def parse(String description) {			
	def item1 = [		
		canBeCurrentState: false,	
		linkText: getLinkText(device),	
		isStateChange: false,	
		displayed: false,	
		descriptionText: description,	
		value:  description	
	]		
	def result		
	def cmd = zwave.parse(description, [0x20:1,0x26:1,0x70:1,0x32:1,0x85:2])		
	if (cmd) {		
		result = createEvent(cmd, item1)	
	}		
	else {		
		item1.displayed = displayed(description, item1.isStateChange)	
		result = [item1]	
	}		
	log.debug "Parse returned ${result?.descriptionText}"		
	result		
}


def createEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, Map item1) {			
	def result = doCreateEvent(cmd, item1)		
	for (int i = 0; i < result.size(); i++) {		
		result[i].type = "physical"	
	}		
	result		
}			
			
def createEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, Map item1) {			
	def result = doCreateEvent(cmd, item1)		
	for (int i = 0; i < result.size(); i++) {		
		result[i].type = "physical"	
	}		
	result		
}			
			
def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStartLevelChange cmd, Map item1) {			
	[]		
}			
			
def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd, Map item1) {			
	[response(zwave.basicV1.basicGet())]		
}			
			
def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd, Map item1) {			
	def result = doCreateEvent(cmd, item1)		
	for (int i = 0; i < result.size(); i++) {		
		result[i].type = "physical"	
	}		
	result		
}			
			
def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd, Map item1) {			
	def result = doCreateEvent(cmd, item1)		
	result[0].descriptionText = "${item1.linkText} is ${item1.value}"		
	result[0].handlerName = cmd.value ? "statusOn" : "statusOff"		
	for (int i = 0; i < result.size(); i++) {		
		result[i].type = "digital"	
	}		
	result		
}			
			
def doCreateEvent(physicalgraph.zwave.Command cmd, Map item1) {			
	def result = [item1]		
			
	item1.name = "switch"		
	item1.value = cmd.value ? "on" : "off"		
	item1.handlerName = item1.value		
	item1.descriptionText = "${item1.linkText} was turned ${item1.value}"		
	item1.canBeCurrentState = true		
	item1.isStateChange = isStateChange(device, item1.name, item1.value)		
	item1.displayed = item1.isStateChange		
			
			
		def item2 = new LinkedHashMap(item1)	
		item2.name = "level"	
		item2.value = cmd.value as String	
		item2.unit = "%"	
		item2.descriptionText = "${item1.linkText} dimmed ${item2.value} %"	
		item2.canBeCurrentState = true	
		item2.isStateChange = isStateChange(device, item2.name, item2.value)	
		item2.displayed = false	
		result << item2	
			
	result		
}			
			
def createEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd, Map item1) {			
	def dispValue
    def newValue
    def retValue = []
    
    if (cmd.scale == 0) {		
            newValue = cmd.scaledMeterValue
            //if (newValue != state.energyValue) {			
                //dispValue = String.format("%5.2f",newValue)+"\nkWh" 					
                state.energyValue = newValue              
                dispValue = (newValue *  getKWhCost())
                retValue << createEvent(name: "myEnergyCost", value: dispValue as String, unit: "")
                log.debug "energyCost ${dispValue as String}"	
            //} 
            retValue << createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh") 
            retValue
            
	}	        			
	else if (cmd.scale == 2) {				
		createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
	}	
}

def createEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd, Map item1) {			
	def retValue = []
     
    if (cmd.parameterNumber == 1){
    	state.lightDirection = (cmd.configurationValue[0] == 0?"diff load":"same load")
        log.debug "lightDirection ${cmd.configurationValue[0] == 0?"diff load":"same load"}"
    }
    else if (cmd.parameterNumber == 8) {		           
        //retValue << createEvent(name: "powerReportIntervalcfg", value: cmd.configurationValue[0], unit: "") 
        state.powerReportInterval = (cmd.configurationValue[0]) as String
        log.debug "powerReportIntervalcfg ${cmd.configurationValue[0]}"
	}	
    else if (cmd.parameterNumber == 10) {		           
        state.energyReportInterval = (cmd.configurationValue[0]) as String
        log.debug "energyReportInterval ${cmd.configurationValue[0]}"
	}
    else if (cmd.parameterNumber == 11) {		           
        state.powerReportByChange = (cmd.configurationValue[0] == 0?"NO":"YES")
        log.debug "powerReportByChange ${cmd.configurationValue[0] == 0?"NO":"YES"}"
	}
    else if (cmd.parameterNumber == 12) {		           
        state.powerReportChangeInterval = (cmd.configurationValue[0]) as String
        log.debug "powerReportChangeInterval ${cmd.configurationValue[0]}"
	}
    retValue
}

def createEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd, Map item1) {			
	def retValue = []
    //def id = cmd.nodeIds[0]
    for(Short id:cmd.nodeId){ 
    	if(id == zwaveHubNodeId){
        	state.hubNodeAss = 1
        }
    }	 
    retValue
}
		
def createEvent(physicalgraph.zwave.Command cmd,  Map map) {			
	// Handles any Z-Wave commands we aren't interested in		
	log.debug "UNHANDLED COMMAND $cmd"		
}			
			
def on() {			
	log.info "on"
    def result=[]
	result<<zwave.basicV1.basicSet(value: 0xFF).format()
    result<<"delay 5000"
    result<<zwave.switchMultilevelV1.switchMultilevelGet().format()
    result<<"delay 1000"
    result<<zwave.meterV2.meterGet(scale:2).format()
    result
}			
			
def off() {			
	def result=[]
	result<<zwave.basicV1.basicSet(value: 0x00).format()
    result<<"delay 5000"
    result<<zwave.switchMultilevelV1.switchMultilevelGet().format()
    result<<"delay 1000"
    result<<zwave.meterV2.meterGet(scale:2).format()
    result			
}			
			
def setLevel(value) {			
    def level = Math.min(value as Integer, 99)
    def result=[]
    result<<sendEvent(name: "level", value: level)
	result<<zwave.basicV1.basicSet(value: level).format()
    result<<"delay 5000"
    result<<zwave.switchMultilevelV1.switchMultilevelGet().format()
    result<<"delay 1000"
    result<<zwave.meterV2.meterGet(scale:2).format()
    result			
}			
			
def setLevel(value, duration) {			
    def level = Math.min(value as Integer, 99)			
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)		
	zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format()		
}			
			
def poll() {
	delayBetween([				
	//zwave.switchMultilevelV1.switchMultilevelGet().format(),		
    zwave.meterV2.meterGet(scale:0).format(),			
	zwave.meterV2.meterGet(scale:2).format(),
    //zwave.configurationV1.configurationGet(parameterNumber: 1).format(),
    //zwave.configurationV1.configurationGet(parameterNumber: 8).format(),
    //zwave.configurationV1.configurationGet(parameterNumber: 10).format(),
    //zwave.configurationV1.configurationGet(parameterNumber: 11).format(),
    //zwave.configurationV1.configurationGet(parameterNumber: 12).format()
],200)		
}			
			
def refresh(){ 
	delayBetween([				
	zwave.switchMultilevelV1.switchMultilevelGet().format(),		
    zwave.meterV2.meterGet(scale:0).format(),			
	zwave.meterV2.meterGet(scale:2).format(),
    zwave.configurationV1.configurationGet(parameterNumber: 1).format(),
    zwave.configurationV1.configurationGet(parameterNumber: 8).format(),
    zwave.configurationV1.configurationGet(parameterNumber: 10).format(),
    zwave.configurationV1.configurationGet(parameterNumber: 11).format(),
    zwave.configurationV1.configurationGet(parameterNumber: 12).format()
],200)
}			
			
def updated() {
	log.debug "kWhCost Updata"
    
    def dispValue
    //if(device.currentValue("energy")){
    	dispValue =((device.currentValue("energy")?:0) * getKWhCost())
    //}
    sendEvent(name: "myEnergyCost", value: dispValue as String, unit: "")
    response(configure())
}

def reset() {		
	sendEvent(name: "myEnergyCost", value: "0" as String, unit: "")	
	delayBetween( [		
		zwave.meterV2.meterReset().format(),	
		zwave.meterV2.meterGet(scale: 0).format()	
	],1000)
}			



def getKWhCost(){
	(settings.kWhCost?:1.0)as double
}

def getLightDirection(){
	settings.lightDirection?:"diff load"
}

def getPowerReportInterval(){
	settings.powerReportInterval?:"0"
}

def getEnergyReportInterval(){
	settings.energyReportInterval?:"0"
}

def getPowerReportByChange(){
	settings.powerReportByChange?:"YES"
}

def getPowerReportChangeInterval(){
	settings.powerReportChangeInterval?:"10"
}

def test(){
	if(state.lightDirection == getLightDirection()
    	&& state.powerReportInterval == getPowerReportInterval()
        && state.energyReportInterval == getEnergyReportInterval()
        && state.powerReportByChange == getPowerReportByChange()
        && state.powerReportChangeInterval == getPowerReportChangeInterval()
       	&& state.hubNodeAss == 1){
    	sendEvent(name: "configState", value: "configComplete")
    }
    else{
    	sendEvent(name: "configState", value: "configFail")
    }
	log.debug "test"
}

def configure() {
	def cmd = []
    def sendConfigTime = 0
    cmd<<zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId).format()
    cmd<<zwave.associationV1.associationGet(groupingIdentifier:1).format()
    if(state.lightDirection != getLightDirection()){
    	cmd<<zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: (getLightDirection() == "same load"?1:0)).format()
        cmd<<zwave.configurationV1.configurationGet(parameterNumber: 1).format()
        sendConfigTime += 1
    }
    if(state.powerReportInterval != getPowerReportInterval()){
    	cmd<<zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: getPowerReportInterval()as int).format()
        cmd<<zwave.configurationV1.configurationGet(parameterNumber: 8).format()
        sendConfigTime += 1
    }
    if(state.energyReportInterval != getEnergyReportInterval()){
    	cmd<<zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, scaledConfigurationValue: getEnergyReportInterval()as int).format()
        cmd<<zwave.configurationV1.configurationGet(parameterNumber: 10).format()
        sendConfigTime += 1
    }
    if(state.powerReportByChange != getPowerReportByChange()){
    	cmd<<zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1, scaledConfigurationValue: (getPowerReportByChange() == "NO"?0:1)).format()
        cmd<<zwave.configurationV1.configurationGet(parameterNumber: 11).format()
        sendConfigTime += 1
    }
    if(state.powerReportChangeInterval != getPowerReportChangeInterval()){
    	cmd<<zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1, scaledConfigurationValue: getPowerReportChangeInterval()as int).format()
        cmd<<zwave.configurationV1.configurationGet(parameterNumber: 12).format()
        sendConfigTime += 1
    }
    //if(sendConfigTime){
    	sendEvent(name: "configState", value:"configing")
    	runIn(sendConfigTime + 2, 'test')
    //}
    delayBetween(cmd,200)
	log.debug cmd
    //log.debug lightDirection.class
	cmd
}
