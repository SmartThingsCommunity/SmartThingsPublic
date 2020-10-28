/**
Copyright Sinopé Technologies
1.3.0
SVN-571
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
**/

import physicalgraph.zigbee.zcl.DataType

metadata {

	preferences {
        input("trace", "bool", title: "Trace", description: "Set it to true to enable tracing")
		// input("logFilter", "number", title: "Trace level", range: "1..5",
		// 	description: "1= ERROR only, 2= <1+WARNING>, 3= <2+INFO>, 4= <3+DEBUG>, 5= <4+TRACE>")
    }
    
    definition (name: "VA4200WZ-VA4200ZB Sinope Valve", namespace: "Sinope Technologies", author: "Sinope Technologies",  ocfDeviceType: "oic.d.watervalve") {
        capability "Configuration"
        capability "Refresh"
        capability "Actuator"
        capability "Valve"
        capability "Battery"
        capability "Power Source"
        capability "Health Check"
        
        fingerprint  manufacturer: "Sinope Technologies", model: "VA4200WZ", deviceJoinName: "Sinope Valve", mnmn:"SmartThings", vid:"SmartThings-smartthings-ZigBee_Valve" //VA4200WZ
        fingerprint  manufacturer: "Sinope Technologies", model: "VA4200ZB", deviceJoinName: "Sinope Valve", mnmn:"SmartThings", vid:"SmartThings-smartthings-ZigBee_Valve" //VA4200ZB
        fingerprint  manufacturer: "Sinope Technologies", model: "VA4220ZB", deviceJoinName: "Sinope Valve", mnmn:"SmartThings", vid:"SmartThings-smartthings-ZigBee_Valve" //VA4220ZB
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"valve", type: "generic", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.valve", key: "PRIMARY_CONTROL") {
                attributeState "open", label: '${name}', action: "valve.close", icon: "st.valves.water.open", backgroundColor: "#00A0DC", nextState:"closing"
                attributeState "closed", label: '${name}', action: "valve.open", icon: "st.valves.water.closed", backgroundColor: "#ffffff", nextState:"opening"
                attributeState "opening", label: '${name}', action: "valve.close", icon: "st.valves.water.open", backgroundColor: "#00A0DC", nextState:"closing"
                attributeState "closing", label: '${name}', action: "valve.open", icon: "st.valves.water.closed", backgroundColor: "#ffffff", nextState:"opening"
            }
            tileAttribute ("powerSource", key: "SECONDARY_CONTROL") {
                attributeState "powerSource", label:'Power Source: ${currentValue}'
            }
        }

        valueTile("battery", "device.battery", inactiveLabel:false, decoration:"flat", width:2, height:2) {
            state "battery", label:'${currentValue}% battery', unit:""
        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["valve"])
        details(["valve", "battery", "refresh"])
    }
}

def open() {
    zigbee.on()
}

def close() {
    zigbee.off()
}

def refresh() {
    traceEvent(settings.logFilter, "refresh called", settings.trace, get_LOG_DEBUG())
    def cmds = []
    cmds += zigbee.readAttribute(0x0006, 0x0000)//refresh on/off
    cmds += zigbee.readAttribute(0x0000, 0x0007)//refresh power source
    cmds += zigbee.readAttribute(0x0001, 0x0021)//refresh battery percentage remaining
    cmds += zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null)//configure reporting on/off min: 0sec, max 600sec
    cmds += zigbee.configureReporting(0x0001, 0x0021, 0x20, 60, 60*60, 1)//configure reporting battery percentage remaining min: 6sec, max 1hour
    return sendZigbeeCommands(cmds)
}

def configure() {
    traceEvent(settings.logFilter, "Configuring Reporting and Bindings", settings.trace, get_LOG_DEBUG())

    //allow 15 minutes withour receiving on/off state
	sendEvent(name: "checkInterval", value: 15*60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    refresh()
}

def installed() {
	traceEvent(settings.logFilter, "installed>Device is now Installed", settings.trace)
	initialize()
}
def initialize(){
	traceEvent(settings.logFilter, "device is initializing", settings.trace)
	runEvery15Minutes(refreshPowerSource)//the POWER_SOURCE attribute is not reportable.
    runIn(10,refreshPowerSource)
    refresh()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	traceEvent(settings.logFilter, "Ping()", settings.trace, get_LOG_DEBUG())
	return refresh()
}

// Parse incoming device messages to generate events
def parse(String description) {
    traceEvent(settings.logFilter, "description is $description", settings.trace, get_LOG_DEBUG())
    def result = []    
    def event = zigbee.getEvent(description)
    if(event){
   		if(event.name == "switch") {
            event.name = "valve"
            if(event.value == "on") {
                event.value = "open"
            }
            else if(event.value == "off") {
                event.value = "closed"
            }
            sendEvent(name: "checkInterval", value: 15*60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
        }
        sendEvent(event)
    }
    else{
        Map map = [:]
        if (description?.startsWith('catchall:')) {
            map = parseCatchAllMessage(description)
        }
        else if (description?.startsWith('read attr -')) {
            map = parseReportAttributeMessage(description)
        }

        if(map){
            result += createEvent(map)
            if(map.additionalAttrs){
                    def additionalAttrs = map.additionalAttrs
                    additionalAttrs.each{allMaps ->
                        result += createEvent(allMaps)
                    }
            }
        }
    }
    
	return result
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
	if (shouldProcessMessage(cluster)) {
    	traceEvent(settings.logFilter, "parseCatchAllMessage > $cluster", settings.trace) 
		switch(cluster.clusterId) {
        	case 0x0000://power source
            	// 0x07 - configure reporting
                if (cluster.command != 0x07) {
					resultMap = getPowerSourceResult(cluster.data.last())
				}
            	break
			case 0x0001://battery percentage remaining
				// 0x07 - configure reporting
				if (cluster.command != 0x07) {
					resultMap = getBatteryResult(cluster.data.last())
				}
				break
            case 0x0006://on/off
            	//0x07 - configure reporting
				if (cluster.command != 0x07) {
					resultMap = getOnOffResult(cluster.data.last())
				}
                break
        }
    }
    return resultMap
}

private boolean shouldProcessMessage(cluster) {
	// 0x0B is default response indicating message got through
	boolean ignoredMessage = cluster.profileId != 0x0104 ||
		cluster.command == 0x0B ||
		(cluster.data.size() > 0 && cluster.data.first() == 0x3e)//the 0x3e catch  undesired bind request
	return !ignoredMessage
}

private Map parseReportAttributeMessage(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
	traceEvent(settings.logFilter, "Desc Map: $descMap" + cluster, settings.trace, get_LOG_DEBUG())

	Map resultMap = [:]
	if (descMap.cluster == "0000" && descMap.attrId == "0007") {
		resultMap = getPowerSourceResult(descMap.value)
	}
    else if (descMap.cluster == "0001" && descMap.attrId == "0021") {
        resultMap = getBatteryResult(zigbee.convertHexToInt(descMap.value))
	}
    else if (descMap.cluster == "0006" && descMap.attrId == "0000") {
        resultMap = getOnOffResult(descMap.value)
	}
	return resultMap
}

private Map getBatteryResult(rawValue) {
    traceEvent(settings.logFilter, "Battery rawValue = ${rawValue}" + cluster, settings.trace, get_LOG_DEBUG())

	def result = [:]
    result.name = 'battery'
    result.descriptionText = "{{ device.displayName }} battery was {{ value }}%"

    int batteryPercent = rawValue / 2
    result.value = Math.min(100, batteryPercent)

	return result
}

private Map getOnOffResult(rawValue) {
    traceEvent(settings.logFilter, "On/Off rawValue = ${rawValue}" + cluster, settings.trace, get_LOG_DEBUG())

	Map result = [:]
    result.name = 'valve'
    result.descriptionText = "{{ device.displayName }} state was {{ value }}"
    if(rawValue == "0000"){
        result.value == "off"
    }
    else{
        result.value == "on"
    }
    
    List<Map> addAttribsList = []
    Map addAttrib = [:]
    
    addAttrib.name = 'valve'
	addAttrib.descriptionText = "{{ device.displayName }} state was {{ value }}"
	addAttrib.value = result.value
	addAttribsList += addAttrib
	result.additionalAttrs = addAttribsList
    
	return result
}

private Map getPowerSourceResult(rawValue) {
	traceEvent(settings.logFilter, "powerSource rawValue = ${rawValue}" + cluster, settings.trace, get_LOG_DEBUG())
	def result = [:]
    result.name = 'powerSource'
    result.translatable = true
    result.descriptionText = "{{ device.displayName }} powerSource was {{ value }}%"
	if(rawValue == "0081" || rawValue == "0082"){
    	result.value = "mains"
    }
    else if(rawValue == "0003"){
    	result.value = "battery"
    }
    else if(rawValue == "0004"){
    	result.value = "dc"
    }
    else{
    	result.value = "unknown"
    }
	return result
}

def refreshPowerSource(){
    def cmds = []
    cmds += zigbee.readAttribute(0x0000, 0x0007)//read power source attribute
	return sendZigbeeCommands(cmds)
}

void sendZigbeeCommands(cmds, delay = 1000) {
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

	if (displayEvent || traceLevel < 4) {
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
		}
	}
}