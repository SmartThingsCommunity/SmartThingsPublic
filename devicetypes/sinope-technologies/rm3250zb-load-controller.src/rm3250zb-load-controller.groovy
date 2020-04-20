/**
Copyright Sinopé Technologies
1.0.0
SVN-427
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
**/
 
metadata {

	preferences {
        input("trace", "bool", title: "Trace", description: "Set it to true to enable tracing")
		input("logFilter", "number", title: "Trace level", range: "1..5",
			description: "1= ERROR only, 2= <1+WARNING>, 3= <2+INFO>, 4= <3+DEBUG>, 5= <4+TRACE>")
    }
    
    definition (name: "RM3250ZB Load Controller", namespace: "Sinope Technologies", author: "Sinope Technologies") {
        capability "Refresh"
        capability "Switch"        
        capability "Configuration"
        capability "Actuator"
        capability "Power Meter"  

        attribute "power", "number"
        attribute "load", "number"

		fingerprint profileId: "0104", deviceId: "0002", inClusters: "0000, 0003, 0004, 0005, 0006, 0B04, 0B05, FF01", manufacturer: "Sinope Technologies", model: "RM3250ZB", deviceJoinName: "RM3250ZB"
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

        valueTile("load", "device.load", decoration: "flat", width: 2, height: 2) {
            state "load", label:'${currentValue} W'
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        main "switch"
        details(["switch", "load", "refresh"])
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
        powerValue = (event.value as Integer)            	//TODO: The divisor value needs to be set as part of configuration
        sendEvent(name: "power", value: powerValue)
      }
      else {
	      sendEvent(event)
    }
  }
  else {
		if (description?.startsWith("read attr -")) {
            traceEvent(settings.logFilter, "Description start with read attr -", settings.trace, get_LOG_DEBUG())
          	def mymap = zigbee.parseDescriptionAsMap(description)
          	if (mymap) {
              traceEvent(settings.logFilter, "Mymap is $mymap", settings.trace, get_LOG_DEBUG())
              traceEvent(settings.logFilter, "Cluster is $mymap.cluster and Attribute is $mymap.attrId", settings.trace, get_LOG_DEBUG())
          
              if(mymap.cluster == "FF01" && mymap.attrId == "0060"){
              	def loadValue
                loadValue = zigbee.convertHexToInt(mymap.value)
                traceEvent(settings.logFilter, "Load connected id $loadValue Watt", settings.trace, get_LOG_DEBUG())
                def name = "load"
                def value = loadValue
                sendEvent(name: "load", value: loadValue)
              }
			}
    	}

  	}
}

def off() {
    return zigbee.off() +
    "delay 3000" +
    zigbee.readAttribute(0x0B04, 0x050B)
}

def on() {
    return zigbee.on() +
    "delay 3000" +
    zigbee.readAttribute(0x0B04, 0x050B) +
    zigbee.readAttribute(0xFF01, 0x0060)
}

def refresh() {
    return zigbee.readAttribute(0x0006, 0x0000) + zigbee.readAttribute(0x0B04, 0x050B) + zigbee.readAttribute(0xFF01, 0x0060) +
        zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null) + zigbee.configureReporting(0x0B04, 0x050B, 0x29, 60, 599, 0x64)
}

def configure() {
    traceEvent(settings.logFilter, "Configuring Reporting and Bindings.", settings.trace, get_LOG_DEBUG())
	return  zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null) + zigbee.configureReporting(0x0B04, 0x050B, 0x29, 60, 599, 0x64) +
    	zigbee.readAttribute(0x0006, 0x0000) + zigbee.readAttribute(0x0B04, 0x050B) + zigbee.readAttribute(0xFF01, 0x0060)
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