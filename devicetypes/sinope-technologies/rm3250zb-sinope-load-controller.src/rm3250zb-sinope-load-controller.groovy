/**

Copyright Sinopé Technologies 2019
1.1.0
SVN-571
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
**/
 
metadata {

	preferences {
        input("trace", "bool", title: "Trace", description: "Set it to true to enable tracing")
		input("logFilter", "number", title: "Trace level", range: "1..5",
			description: "1= ERROR only, 2= <1+WARNING>, 3= <2+INFO>, 4= <3+DEBUG>, 5= <4+TRACE>")
    }
    
    definition (name: "RM3250ZB Sinope Load Controller", namespace: "Sinope Technologies", author: "Sinope Technologies", , ocfDeviceType: "oic.d.switch") {

        capability "Refresh"
        capability "Switch"        
        capability "Configuration"
        capability "Actuator"
        capability "Power Meter"
        capability "Health Check"

		fingerprint manufacturer: "Sinope Technologies", model: "RM3250ZB", deviceJoinName: "RM3250ZB"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true)
        {
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL")
            {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
   			tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
        		attributeState "power", label:'actual load: ${currentValue} Watts'
    		}
      	}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        main "switch"
        details(["switch", "refresh"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    traceEvent(settings.logFilter, "Description is $description", settings.trace, get_LOG_DEBUG())
    def event = zigbee.getEvent(description)
    
    if (event) {
        traceEvent(settings.logFilter, "Event name is $event.name", settings.trace, get_LOG_DEBUG())
        if (event.name == "power") {
            def powerValue
            powerValue = (event.value as Integer) 
            sendEvent(name: "power", value: powerValue)
        }
        else {
	        sendEvent(event)
            sendEvent(name: "checkInterval", value: 30*60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
        }
    }
    else {
        traceEvent(settings.logFilter, "DID NOT PARSE MESSAGE for description", settings.trace, get_LOG_WARN())
    }
}

def off() {
    return zigbee.off()
}

def on() {
    return zigbee.on()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	traceEvent(settings.logFilter, "Ping()", settings.trace, get_LOG_DEBUG())
	return refresh()
}

def refresh() {
    traceEvent(settings.logFilter, "Refresh.", settings.trace, get_LOG_DEBUG())
    return zigbee.readAttribute(0x0006, 0x0000) + //read on/off
    zigbee.readAttribute(0x0B04, 0x050B) + //read active power
    zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null) + //configure reporting of on/off
    zigbee.configureReporting(0x0B04, 0x050B, 0x29, 30, 599, 0x64) // configure reporting of active power
}

def configure() {
    traceEvent(settings.logFilter, "Configuring Reporting and Bindings.", settings.trace, get_LOG_DEBUG())

    //allow 30 minutes without receiving on/off state
	sendEvent(name: "checkInterval", value: 30*60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	return  zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null) + // configure reporting of active power
    zigbee.configureReporting(0x0B04, 0x050B, 0x29, 30, 599, 0x64) + //configure reporting of on/off
    zigbee.readAttribute(0x0006, 0x0000) + //read on/off
    zigbee.readAttribute(0x0B04, 0x050B) //read active power
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