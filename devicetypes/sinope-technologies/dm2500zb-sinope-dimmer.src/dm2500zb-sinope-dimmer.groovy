/**
Copyright Sinopé Technologies
1.0.0
SVN-428
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
**/

preferences {
	input("MinimalIntensityParam", "number", title:"Light bulb minimal intensity (1..10) (default: blank)", range:"1..10", description:"optional")
    input("LedIntensityParam", "number", title:"Indicator light intensity (1..100) (default: blank)", range:"1..100", description:"optional")
	input("trace", "bool", title: "Trace", description: "Set it to true to enable tracing")
	input("logFilter", "number", title: "Trace level", range: "1..5",
		description: "1= ERROR only, 2= <1+WARNING>, 3= <2+INFO>, 4= <3+DEBUG>, 5= <4+TRACE>")
}

metadata {
    definition (name: "DM2500ZB Sinope Dimmer", namespace: "Sinope Technologies", author: "Sinope Technologies")
    {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"
        
        attribute "swBuild","string"//there are dimmers wo does not supprt the minimal intensity. theses dimmers can be identified by their swBuild under the value 106
        
        fingerprint profileId: "0104", inClusters: "0000 0003 0004 0005 0006 0008 ff01", manufacturer: "Sinope Technologies", model: "DM2500ZB"
    }

    tiles(scale: 2) 
    {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true)
        {
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL")
            {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") 
            {
                attributeState "level", action:"switch level.setLevel"
            }
        }

		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2)
        {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "switch"
        details(["switch","refresh"])
    }
}

//-- Parsing ---------------------------------------------------------------------------------------------

def parse(String description)
{
    traceEvent(settings.logFilter, "description is $description", settings.trace, get_LOG_DEBUG())
	def cluster = zigbee.parse(description)
    def event = zigbee.getEvent(description)
    traceEvent(settings.logFilter, "EVENT = $event", settings.trace, get_LOG_DEBUG())
	
    if(event)
    {
        traceEvent(settings.logFilter, "send event : $event", settings.trace, get_LOG_DEBUG())
        sendEvent(event)
    }
    else
    {
        traceEvent(settings.logFilter, "DID NOT PARSE MESSAGE for description", settings.trace, get_LOG_WARN())
        def mymap = zigbee.parseDescriptionAsMap(description)
        if (mymap) {
              traceEvent(settings.logFilter, "Mymap is $mymap", settings.trace, get_LOG_DEBUG())
              traceEvent(settings.logFilter, "Cluster is $mymap.cluster and Attribute is $mymap.attrId", settings.trace, get_LOG_DEBUG())
              
              if(mymap.cluster == "0000" && mymap.attrId == "0001"){
              	def SwBuild
                SwBuild = mymap.value
                SwBuild = zigbee.convertHexToInt(SwBuild)
              	sendEvent(name: "swBuild", value: SwBuild)
              }
        }
    }
}

def parseDescriptionAsMap(description)
{
    traceEvent(settings.logFilter, "parsing MAP ...", settings.trace, get_LOG_DEBUG())
	(description - "read attr - ").split(",").inject([:]) 
    {
    	map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

//-- Initialisation --------------------------------------------------------------------------------------

def updated() {
    
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 2000) {
		state.updatedLastRanAt = now()   
		
		return checkSettings() 
	}
	else {
        traceEvent(settings.logFilter, "updated(): Ran within last 2 seconds so aborting", settings.trace, get_LOG_TRACE())
	}
    
}

//-- On Off Control --------------------------------------------------------------------------------------

def off()
{
    zigbee.off()
}

def on()
{
    traceEvent(settings.logFilter, "sending on", settings.trace, get_LOG_DEBUG())
    zigbee.on()
}

//-- Level Control ---------------------------------------------------------------------------------------

def setLevel(value) 
{
    traceEvent(settings.logFilter, "primary value = $value", settings.trace, get_LOG_DEBUG())
    zigbee.setLevel(value,0)
}

//-- refresh ---------------------------------------------------------------------------------------------

def refresh()
{
	def cmds = []
    cmds += zigbee.readAttribute(0x0006, 0x0000)
    cmds += zigbee.readAttribute(0x0008, 0x0000)
    cmds += zigbee.readAttribute(0x0000, 0x0001)							//software version
    cmds += zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 599, null)
    cmds += zigbee.configureReporting(0x0008, 0x0000, 0x20, 3, 602, 0x01)
    if(checkSoftVersion() == true){
    	cmds += zigbee.writeAttribute(0xff01, 0x0055, 0x21, getTiming((MinimalIntensityParam)?MinimalIntensityParam.toInteger():0))
    }
    return sendZigbeeCommands(cmds)
}

//-- configuration ---------------------------------------------------------------------------------------

def configure()
{
    log.debug "Configuring Reporting and Bindings."
    return  zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 599, null) +
            zigbee.configureReporting(0x0008, 0x0000, 0x20, 3, 602, 0x01) +
            zigbee.readAttribute(0x0006, 0x0000) +
            zigbee.readAttribute(0x0008, 0x0000) +
            zigbee.readAttribute(0x0000, 0x0001)
            
}

//-- Check Settings ---------------------------------------------------------------------------------------

private void checkSettings()
{
	def cmds = []
	if(checkSoftVersion() == true)
    {
    	def MinLight = (MinimalIntensityParam)?MinimalIntensityParam.toInteger():0
   			def Timing = getTiming(MinLight)
            traceEvent(settings.logFilter, "Timing to: $Timing", settings.trace, get_LOG_DEBUG())
        	
        	cmds += zigbee.writeAttribute(0xff01, 0x0055, 0x21, Timing)
			
    }
    if(LedIntensityParam){
    	cmds += zigbee.writeAttribute(0xff01, 0x0052, 0x20, LedIntensityParam)//MaxIntensity On
    	cmds += zigbee.writeAttribute(0xff01, 0x0053, 0x20, LedIntensityParam)//MaxIntensity Off
    }
    else{
        cmds += zigbee.writeAttribute(0xff01, 0x0052, 0x20, 50)//MaxIntensity On
    	cmds += zigbee.writeAttribute(0xff01, 0x0053, 0x20, 50)//MaxIntensity Off
    }
    sendZigbeeCommands(cmds)
}

private int getTiming(def setting)
{
	def Timing
    	switch(setting)
    	{
    	case(1):
       		Timing = 100
       		break;
    	case(2):
       		Timing = 250
    		break;    
    	case(3):
       		Timing = 500
    		break;
    	case(4):
       		Timing = 750
    		break;
    	case(5):
       		Timing = 1000
    		break;
    	case(6):
       		Timing = 1250
    		break;
    	case(7):
       		Timing = 1500
    		break;
    	case(8):
       		Timing = 1750
    		break;
    	case(9):
       		Timing = 2000
    		break;
    	case(10):
       		Timing = 2250
    		break;
    	default:
       		Timing = 600
       		break;
    	}
        return Timing
}

private void sendZigbeeCommands(cmds, delay = 1000) {
	cmds.removeAll { it.startsWith("delay") }
	// convert each command into a HubAction
	cmds = cmds.collect { new physicalgraph.device.HubAction(it) }
	sendHubCommand(cmds, delay)
}

private boolean checkSoftVersion()
{
	def version
    def versionMin = "106"
    def Build = device.currentState("swBuild")?.value
    traceEvent(settings.logFilter, "soft version: $Build", settings.trace, get_LOG_DEBUG())
    
    if(Build > versionMin)//if the version is under 107, the minimal light intensity is not supported.
    {
        traceEvent(settings.logFilter, "intensity supported", settings.trace, get_LOG_DEBUG())
    	version = true
    }
    else
    {
        traceEvent(settings.logFilter, "intensity not supported", settings.trace, get_LOG_DEBUG())
        version = false
    }
    return version
}

private int get_LOG_ERROR() {
	return 1
}
private int get_LOG_WARN() {
	return 2
}
private int get_LOG_INFO() {
	return 3
}
private int get_LOG_DEBUG() {
	return 4
}
private int get_LOG_TRACE() {
	return 5
}

def traceEvent(logFilter, message, displayEvent = false, traceLevel = 4, sendMessage = true) {
	int LOG_ERROR = get_LOG_ERROR()
	int LOG_WARN = get_LOG_WARN()
	int LOG_INFO = get_LOG_INFO()
	int LOG_DEBUG = get_LOG_DEBUG()
	int LOG_TRACE = get_LOG_TRACE()
	int filterLevel = (logFilter) ? logFilter.toInteger() : get_LOG_WARN()
    
	if ((displayEvent) || (sendMessage)) {
		def results = [
			name: "verboseTrace",
			value: message,
			displayed: ((displayEvent) ?: false)
		]

		if ((displayEvent) && (filterLevel >= traceLevel)) {
			switch (traceLevel) {
				case LOG_ERROR:
					log.error "${message}"
					break
				case LOG_WARN:
					log.warn "${message}"
					break
				case LOG_INFO:
					log.info "${message}"
					break
				case LOG_TRACE:
					log.trace "${message}"
					break
				case LOG_DEBUG:
				default:
					log.debug "${message}"
					break
			} /* end switch*/
			if (sendMessage) sendEvent(results)
		} /* end if displayEvent*/
	}
}