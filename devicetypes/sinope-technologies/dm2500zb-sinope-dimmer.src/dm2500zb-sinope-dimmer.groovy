/**

Copyright Sinopé Technologies 2019
1.1.0
SVN-571
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
**/

preferences {
	input("MinimalIntensityParam", "number", title:"Light bulb minimal intensity (1..10) (default: blank)", range:"1..10", description:"optional")
    // when the is at a low value, some bulbs may flicker for some technical reasons. to prevent that behaviour. writting this parameter will increase the minimal value
    // of the dimmer's become a little bit higher so the load doesn't start flickering when the level is low.

    input("LedIntensityParam", "number", title:"Indicator light intensity (1..100) (default: blank)", range:"1..100", description:"optional")
	input("trace", "bool", title: "Trace", description: "Set it to true to enable tracing")
	input("logFilter", "number", title: "Trace level", range: "1..5",
		description: "1= ERROR only, 2= <1+WARNING>, 3= <2+INFO>, 4= <3+DEBUG>, 5= <4+TRACE>")
}

metadata {
    definition (name: "DM2500ZB Sinope Dimmer", namespace: "Sinope Technologies", author: "Sinope Technologies",  ocfDeviceType: "oic.d.switch")
    {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"
        capability "Health Check"
        
        attribute "swBuild","string"// earliers versions of the DM2500ZB does not support the minimal intensity. theses dimmers can be identified by their swBuild under the value 106
        
        fingerprint manufacturer: "Sinope Technologies", model: "DM2500ZB", deviceJoinName: "Sinope Dimmer Switch" //DM2500ZB
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

def parse(String description)
{
    traceEvent(settings.logFilter, "description is $description", settings.trace, get_LOG_DEBUG())
    def event = zigbee.getEvent(description)
    traceEvent(settings.logFilter, "Event = $event", settings.trace, get_LOG_DEBUG())
	
    if(event)
    {
        if (event.name=="level" && event.value==0) {}
		else {
            traceEvent(settings.logFilter, "send event : $event", settings.trace, get_LOG_DEBUG())
			sendEvent(event)
            sendEvent(name: "checkInterval", value: 30*60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
		}
    }
    else
    {
        traceEvent(settings.logFilter, "DID NOT PARSE MESSAGE for description", settings.trace, get_LOG_WARN())
        if (description?.startsWith("read attr -")) 
        {
            def descMap = zigbee.parseDescriptionAsMap(description)
            def result = []
            result += createCustomMap(descMap)

            // In the possibility of multiple attributes being reported in the same message, all the attributes will be in the same description. the first attribute will be in the fields regularly used, 
            // but the otter attributes will be in the "additionalAttrs". they should all be treated in the following part.
            if(descMap.additionalAttrs)
            {
                def mapAdditionnalAttrs = descMap.additionalAttrs
                mapAdditionnalAttrs.each{add ->
                    traceEvent(settings.logFilter,"parse> mapAdditionnalAttributes : ( ${add} )",settings.trace)
                    add.cluster = descMap.cluster
                    result += createCustomMap(add)
                }
            }
        }
        else
        {
            traceEvent(settings.logFilter, "description did not start with 'read attr -'", settings.trace, get_LOG_WARN())
        }
    }
}

private def parseDescriptionAsMap(description)
{
    traceEvent(settings.logFilter, "parsing MAP ...", settings.trace, get_LOG_DEBUG())
	(description - "read attr - ").split(",").inject([:]) 
    {
    	map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private def createCustomMap(descMap)
{
    def result = null
	def map = [:]

        if(descMap.cluster == "0000" && descMap.attrId == "0001")
        {
            traceEvent(settings.logFilter, "Parsing SwBuild Attribute", settings.trace, get_LOG_DEBUG())
            map.name = "swBuild"
            map.value = zigbee.convertHexToInt(descMap.value)
            sendEvent(name: map.name, value: map.value)
        }
    return result
}

def updated() {
    
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 2000)
    {
		state.updatedLastRanAt = now()   
            
        def cmds = []
        if(checkSoftVersion() == true)
        {
            def MinLight = (MinimalIntensityParam)?MinimalIntensityParam.toInteger():0
            def Time = getTiming(MinLight)
            traceEvent(settings.logFilter, "Set timing to: $Time", settings.trace, get_LOG_DEBUG())
            cmds += zigbee.writeAttribute(0xff01, 0x0055, 0x21, Time)
                
        }
        else
        {
            traceEvent(settings.logFilter, "Minimal intensity is not supported by the device", settings.trace, get_LOG_DEBUG())
        }

        if(LedIntensityParam){
            cmds += zigbee.writeAttribute(0xff01, 0x0052, 0x20, LedIntensityParam)//MaxIntensity On
            cmds += zigbee.writeAttribute(0xff01, 0x0053, 0x20, LedIntensityParam)//MaxIntensity Off
        }
        else{ // set to default
            cmds += zigbee.writeAttribute(0xff01, 0x0052, 0x20, 50)//MaxIntensity On
            cmds += zigbee.writeAttribute(0xff01, 0x0053, 0x20, 50)//MaxIntensity Off
        }
        sendZigbeeCommands(cmds)

	}
	else {
        traceEvent(settings.logFilter, "updated(): Ran within last 2 seconds so aborting", settings.trace, get_LOG_TRACE())
	}
    
}

def off()
{
    zigbee.off()
}

def on()
{
    zigbee.on()
}

def setLevel(level) 
{
    traceEvent(settings.logFilter, "setLevel value = $level", settings.trace, get_LOG_DEBUG())
    zigbee.setLevel(level,0)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.onOffRefresh()
}

def refresh()
{
	def cmds = []
    cmds += zigbee.readAttribute(0x0006, 0x0000) //read on/off
    cmds += zigbee.readAttribute(0x0008, 0x0000) //read level
    cmds += zigbee.readAttribute(0x0000, 0x0001) //read software version
    cmds += zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 599, null) //configure reporting on/off
    cmds += zigbee.configureReporting(0x0008, 0x0000, 0x20, 3, 602, 0x01) //configure reporting level
    if(checkSoftVersion() == true){//if the minimal intensity is supported
    	cmds += zigbee.writeAttribute(0xff01, 0x0055, 0x21, getTiming((MinimalIntensityParam)?MinimalIntensityParam.toInteger():0))
    }
    return sendZigbeeCommands(cmds)
}

def configure()
{
    traceEvent(settings.logFilter, "Configuring Reporting and Bindings", settings.trace, get_LOG_DEBUG())

    //allow 30 minutes without reveiving any on/off report
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    return  zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 599, null) + //configure reporting on/off
            zigbee.configureReporting(0x0008, 0x0000, 0x20, 3, 602, 0x01) + //configure reporting level
            zigbee.readAttribute(0x0006, 0x0000) + //read on/off
            zigbee.readAttribute(0x0008, 0x0000) + //read level
            zigbee.readAttribute(0x0000, 0x0001) //read software version
            
}

//-- Check Settings ---------------------------------------------------------------------------------------


private int getTiming(def setting)
{//getTiming is used to get the minimal time associated with the parameter "minimalIntensityParam"
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

private boolean checkSoftVersion()
{
	def version
    def versionMin = "106" //the first version to support the minimal intensity is the version 106
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


private void sendZigbeeCommands(cmds, delay = 1000) {
	cmds.removeAll { it.startsWith("delay") }
	// convert each command into a HubAction
	cmds = cmds.collect { new physicalgraph.device.HubAction(it) }
	sendHubCommand(cmds, delay)
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