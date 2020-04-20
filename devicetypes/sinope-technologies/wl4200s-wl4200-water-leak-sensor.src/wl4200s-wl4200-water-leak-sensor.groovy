/**
Copyright Sinopé Technologies
1.0.0
SVN-433
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
**/

import physicalgraph.zigbee.clusters.iaszone.ZoneStatus


metadata {
	definition (name: "WL4200S-WL4200 Water Leak Sensor",namespace: "Sinope Technologies", author: "Sinope Technologies") {
		capability "Configuration"
		capability "Battery"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Water Sensor"
		capability "Health Check"
		capability "Sensor"

		command "enrollResponse"
        
        attribute "sensor", "enum", ["disconnected", "connected"]
        attribute "temperature", "number"
        attribute "battery", "number"

        fingerprint endpoint: "1",profileId: "0104", inClusters: "0000,0001,0003,0402,0500,0B05,FF01", outClusters: "0003,0019", manufacturer: "Sinope Technologies", model: "WL4200"
        fingerprint endpoint: "1",profileId: "0104", inClusters: "0000,0001,0003,0402,0500,0B05,FF01", outClusters: "0003,0019", manufacturer: "Sinope Technologies", model: "WL4200S"
	}

	simulator {

	}

	preferences {
		section {
            input("trace", "bool", title: "Trace", description: "Set it to true to enable tracing")
			input("logFilter", "number", title: "Trace level", range: "1..5",
				description: "1= ERROR only, 2= <1+WARNING>, 3= <2+INFO>, 4= <3+DEBUG>, 5= <4+TRACE>")
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"water", type: "generic", width: 6, height: 4){
			tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
				attributeState "dry", label: "Dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
				attributeState "wet", label: "Wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
			}
            tileAttribute ("sensor", key: "SECONDARY_CONTROL") {
                attributeState "disconnected", label:'Probe is ${currentValue}'
                attributeState "connected", label:''
            }
		}
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["water", "temperature"])
		details(["water", "temperature", "battery", "refresh"])
	}
}

def parse(String description) {
	traceEvent(settings.logFilter, "description is $description", settings.trace, get_LOG_DEBUG())
    def map = []
	if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	}
	else if (description?.startsWith('read attr -')) {
		map = parseReportAttributeMessage(description)
	}
	else if (description?.startsWith('temperature: ')) {
		map = parseCustomMessage(description)
	}
	else if (description?.startsWith('zone status')) {
		map = parseIasMessage(description)
	}

    traceEvent(settings.logFilter, "Parse returned $map", settings.trace, get_LOG_DEBUG())
    
    def result = []
    if(map){
    	result += createEvent(map)
        if(map.additionalAttrs){
        		def additionalAttrs = map.additionalAttrs
    			additionalAttrs.each{allMaps ->
    				result += createEvent(allMaps)
    			}
        }
    }
    
    if (description?.startsWith('enroll request')) {
		List cmds = enrollResponse()
        traceEvent(settings.logFilter, "enroll response: ${cmds}", settings.trace, get_LOG_DEBUG())
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	return result
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
	if (shouldProcessMessage(cluster)) {
		switch(cluster.clusterId) {
			case 0x0001:
				// 0x07 - configure reporting
				if (cluster.command != 0x07) {
					resultMap = getBatteryResult(cluster.data.last())
				}
				break

            case 0x0402:
				if (cluster.command == 0x07) {
					if (cluster.data[0] == 0x00){
                        traceEvent(settings.logFilter, "TEMP REPORTING CONFIG RESPONSE" + cluster, settings.trace, get_LOG_DEBUG())
						resultMap = [name: "checkInterval", value: 60*60*24, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
					}
					else {
                        traceEvent(settings.logFilter, "TEMP REPORTING CONFIG FAILED- error code:${cluster.data[0]}", settings.trace, get_LOG_WARN())
					}
				}
				else {
					// temp is last 2 data values. reverse to swap endian
					String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
					def value = getTemperature(temp)
					resultMap = getTemperatureResult(value)
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
		(cluster.data.size() > 0 && cluster.data.first() == 0x3e)
	return !ignoredMessage
}

private Map parseReportAttributeMessage(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
	traceEvent(settings.logFilter, "Desc Map: $descMap" + cluster, settings.trace, get_LOG_DEBUG())

	Map resultMap = [:]
	if (descMap.cluster == "0402" && descMap.attrId == "0000") {
		def value = getTemperature(descMap.value)
		resultMap = getTemperatureResult(value)
	}
    else if (descMap.cluster == "0001" && descMap.attrId == "0021") {
        resultMap = getBatteryResult(zigbee.convertHexToInt(descMap.value))
	}

	return resultMap
}

private Map parseCustomMessage(String description) {
	Map resultMap = [:]
	if (description?.startsWith('temperature: ')) {
		def value = zigbee.parseHATemperatureValue(description, "temperature: ", getTemperatureScale())
		resultMap = getTemperatureResult(value)
	}
	return resultMap
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
    Map descMap = [:]
    List<Map> AddAttribs = []
    descMap += zs.isAlarm1Set() ? getMoistureResult('wet') : getMoistureResult('dry')
    AddAttribs += zs.isAlarm2Set() ? getProbeResult('disconnected') : getProbeResult('connected')
    descMap.additionalAttrs = AddAttribs
    return descMap
}

def getTemperature(value) {
	traceEvent(settings.logFilter, "getTemperature rawValue = ${value}" + cluster, settings.trace, get_LOG_DEBUG())
	def celsius = Integer.parseInt(value, 16).shortValue() / 100
	if(getTemperatureScale() == "C"){
		return Math.round(celsius)
	} else {
		return Math.round(celsiusToFahrenheit(celsius))
	}
}

private Map getBatteryResult(rawValue) {
    traceEvent(settings.logFilter, "Battery rawValue = ${rawValue}" + cluster, settings.trace, get_LOG_DEBUG())

	def result = [:]
		result.name = 'battery'
		result.translatable = true
		result.descriptionText = "{{ device.displayName }} battery was {{ value }}%"

        int batteryPercent = rawValue / 2
		result.value = Math.min(100, batteryPercent)

	return result
}

private Map getTemperatureResult(value) {
    traceEvent(settings.logFilter, "TEMP" + cluster, settings.trace, get_LOG_DEBUG())
    def descriptionText
    if ( temperatureScale == 'C' )
    	descriptionText = '{{ device.displayName }} was {{ value }}°C'
    else
    	descriptionText = '{{ device.displayName }} was {{ value }}°F'

	return [
		name: 'temperature',
		value: value,
		descriptionText: descriptionText,
		translatable: true,
		unit: temperatureScale
	]
}

private Map getMoistureResult(value) {
	traceEvent(settings.logFilter, "water", settings.trace, get_LOG_DEBUG())
    def descriptionText
    if ( value == "wet" )
    	descriptionText = '{{ device.displayName }} is wet'
    else
    	descriptionText = '{{ device.displayName }} is dry'
	return [
		name: 'water',
		value: value,
		descriptionText: descriptionText,
        translatable: true
	]
}

private Map getProbeResult(value) {
	traceEvent(settings.logFilter, "probe", settings.trace, get_LOG_DEBUG())
    def descriptionText
    if ( value == "disconnected" )
    	descriptionText = 'the probe of { device.displayName } is disconnected'
    else
    	descriptionText = 'the probe of { device.displayName } is connected'
	return [
		name: 'sensor',
		value: value,
		descriptionText: descriptionText,
        translatable: true
	]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	traceEvent(settings.logFilter, "ping", settings.trace, get_LOG_DEBUG())
    return zigbee.readAttribute(0x0402, 0x0000)
}

def installed() {
	traceEvent(settings.logFilter, "installed>Device is now Installed", settings.trace)
    initialize()
}

void initialize() {  
	traceEvent(settings.logFilter, "initialize", settings.trace)
    refresh()
}

def refresh() {  
	traceEvent(settings.logFilter, "refresh", settings.trace)
    def cmds = []
    cmds += zigbee.readAttribute(0x0402, 0x0000)//temperature
    cmds += zigbee.readAttribute(0x0001, 0x0021)//battery percentage
	return cmds + enrollResponse()
}

def configure() {
	traceEvent(settings.logFilter, "configure", settings.trace)
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 60*60*24, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	//return refresh() + zigbee.batteryConfig() + zigbee.temperatureConfig(30, 300) // send refresh cmds as part of config
    def cmds = []
    cmds += zigbee.configureReporting(0x0001, 0x0021, 0x20, 30, 43200, 1)		//battery percentage min: 30sec, max: 12h, minimum change: 1%
    cmds += zigbee.configureReporting(0x0402, 0x0000, 0x29, 30, 3600, 300)		//temperature min: 30sec, max:10min, minimum change: 3.0C  
    cmds += zigbee.configureReporting(0x0001, 0x003E, 0x1b, 30, 3600, 1)		//battery Alarm State
    return refresh()+sendZigbeeCommands(cmds)
}

def enrollResponse() {
	traceEvent(settings.logFilter, "Sending enroll response", settings.trace)
    traceEvent(settings.logFilter, "Sending enroll response" + cluster, settings.trace, get_LOG_DEBUG())
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	[
		//Resending the CIE in case the enroll request is sent before CIE is written
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 2000",
		//Enroll Response
		"raw 0x500 {01 23 00 00 00}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 2000"
	]
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private String swapEndianHex(String hex) {
	reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
	int i = 0;
	int j = array.length - 1;
	byte tmp;
	while (j > i) {
		tmp = array[j];
		array[j] = array[i];
		array[i] = tmp;
		j--;
		i++;
	}
	return array
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

void sendZigbeeCommands(cmds, delay = 1000) {
	cmds.removeAll { it.startsWith("delay") }
	// convert each command into a HubAction
	cmds = cmds.collect { new physicalgraph.device.HubAction(it) }
	sendHubCommand(cmds, delay)
}