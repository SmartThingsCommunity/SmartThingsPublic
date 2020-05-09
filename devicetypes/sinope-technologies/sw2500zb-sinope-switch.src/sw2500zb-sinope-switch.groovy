/**

Copyright Sinopé Technologies 2019
1.1.0
SVN-571
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
**/

metadata {

	preferences {
    	input("LedIntensityParam", "number", title:"Indicator light intensity (1..100) (default: blank)", range:"1..100", description:"optional")
        input("trace", "bool", title: "Trace", description: "Set it to true to enable tracing")
		input("logFilter", "number", title: "Trace level", range: "1..5",
			description: "1= ERROR only, 2= <1+WARNING>, 3= <2+INFO>, 4= <3+DEBUG>, 5= <4+TRACE>")
    }

    definition (name: "SW2500ZB Sinope Switch", namespace: "Sinope Technologies", author: "Sinope Technologies",  ocfDeviceType: "oic.d.switch") 
    {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
		capability "Health Check"
        

        fingerprint manufacturer: "Sinope Technologies", model: "SW2500ZB", deviceJoinName: "SW2500ZB"
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
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2)
        {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "switch"
        details(["switch", "refresh"])
    }
}

// Parse incoming device messages to generate events
def parse(String description)
{
	traceEvent(settings.logFilter, "description is $description", settings.trace, get_LOG_DEBUG())
    def event = zigbee.getEvent(description)
    if (event)
    {
		traceEvent(settings.logFilter, "Event: $event", settings.trace, get_LOG_DEBUG())
        sendEvent(event)
		sendEvent(name: "checkInterval", value: 30*60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    }
    else
    {
        traceEvent(settings.logFilter, "DID NOT PARSE MESSAGE for description : $description", settings.trace, get_LOG_WARN())
        traceEvent(settings.logFilter, zigbee.parseDescriptionAsMap(description), settings.trace, get_LOG_DEBUG())
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

def updated() {
    
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 2000) {
		state.updatedLastRanAt = now()   
		
        def cmds = []
        
		if(LedIntensityParam){
    		cmds += zigbee.writeAttribute(0xff01, 0x0052, 0x20, LedIntensityParam)//MaxIntensity On
    		cmds += zigbee.writeAttribute(0xff01, 0x0053, 0x20, LedIntensityParam)//MaxIntensity Off
   	 	}
    	else{ //set to default value
        	cmds += zigbee.writeAttribute(0xff01, 0x0052, 0x20, 50)//MaxIntensity On
    		cmds += zigbee.writeAttribute(0xff01, 0x0053, 0x20, 50)//MaxIntensity Off
    	}
        
        return sendZigbeeCommands(cmds)
		
	}
	else {
        traceEvent(settings.logFilter, "updated(): Ran within last 2 seconds so aborting", settings.trace, get_LOG_TRACE())
	}
    
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	traceEvent(settings.logFilter, "Ping()", settings.trace, get_LOG_DEBUG())
	return refresh()
}

def refresh()
{
	traceEvent(settings.logFilter, "Refresh()", settings.trace, get_LOG_DEBUG())
    def cmds = []
	cmds += zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null)
	cmds += zigbee.readAttribute(0x0006, 0x0000)
	return sendZigbeeCommands(cmds)
}

def configure()
{
    traceEvent(settings.logFilter, "Configuring Reporting and Bindings", settings.trace, get_LOG_DEBUG())

	//allow 30 min without receiving on/off report
	sendEvent(name: "checkInterval", value: 30*60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    return  zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null) +
            zigbee.readAttribute(0x0006, 0x0000)
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

private void sendZigbeeCommands(cmds, delay = 1000) {
	cmds.removeAll { it.startsWith("delay") }
	// convert each command into a HubAction
	cmds = cmds.collect { new physicalgraph.device.HubAction(it) }
	sendHubCommand(cmds, delay)
}