/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus

metadata {
    definition (name: "Xiaomi Aqara Water Sensor", namespace: "MaverickASC", author: "MaverickASC") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"
        capability "Battery"
		capability "Water Sensor"
        
        command "enrollResponse"

        fingerprint inClusters: "0000 0003 0001", outClusters: "0019", manufacturer: "LUMI", model: "lumi.sensor_wleak.aq1", deviceJoinName: "Aqara Water Leak Sensor"
    }

	simulator {
	}

	preferences {
		section {
			image(name: 'educationalcontent', multiple: true, images: [
					"http://cdn.device-gse.smartthings.com/Moisture/Moisture1.png",
					"http://cdn.device-gse.smartthings.com/Moisture/Moisture2.png",
					"http://cdn.device-gse.smartthings.com/Moisture/Moisture3.png"
			])
		}
	}

    tiles(scale: 2) {
        multiAttributeTile(name: "water", type: "generic", width: 6, height: 4) {
			tileAttribute("device.water", key: "PRIMARY_CONTROL") {
				attributeState "dry", label: "Dry", icon: "st.alarm.water.dry", backgroundColor: "#ffffff"
				attributeState "wet", label: "Wet", icon: "st.alarm.water.wet", backgroundColor: "#00A0DC"
			}
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")
			}
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "default", label:'${currentValue}% battery', unit:"",
            backgroundColors: [
							[value: 10, color: "#bc2323"],
							[value: 25, color: "#f1d801"],
							[value: 50, color: "#44b621"]
					]
		}
        
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        
        main "water"
        details(["water", "battery", "refresh","configure"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {

	log.debug "AQUARA-WS RAW: $description"
    Map result = [:]
    Map map = [:]
	
	if ((description?.startsWith('enroll request')) || description?.startsWith('zbjoin:') || description?.startsWith('non-TV event zbjoin:')) {
		List cmds = enrollResponse()
		log.debug "AQUARA-WS ENROLL RESPONSE: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	} else {
	
		if (description?.startsWith('catchall: ')) {
		   map = parseCatchAllMessage(description)
		}
		else if (description?.startsWith('zone status')) {
		   map = parseIasMessage(description)
		}
		else if (description?.startsWith('read attr -')) {
			map = parseReportAttributeMessage(description)
		}
		else {
			log.debug "AQUARA-WS UNKNOWN: $description"			// TODO: Never seen this type of event. Please report when this kind of event occurs
		}
		
		log.debug "AQUARA-WS RESULT: $map"
		result = map ? createEvent(map) : [:]
    }

    def now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    sendEvent(name: "lastCheckin", value: now)

	return result
}


// ******* FUNCTIONS RELATED TO PRIMARY FUNCTION - WATER SENSOR ************

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
	return zs.isAlarm1Set() ? getMoistureResult('wet') : getMoistureResult('dry')
}

private Map getMoistureResult(value) {
	
	def descriptionText
	if (value == "wet")
		descriptionText = "${device.displayName} is wet"
	else
		descriptionText = "${device.displayName} is dry"
	return [
			name           : 'water',
			value          : value,
			descriptionText: descriptionText,
			translatable   : true
	]
}

// ******* FUNCTIONS RELATED TO SECONDARY FUNCTION - BATTERY ************

private Map parseCatchAllMessage(String description) {
	def result = [:]
	def cluster = zigbee.parse(description)
    
    if (cluster) {
		switch(cluster.clusterId) {
			case 0x0000:
			result = getBatteryResult(cluster.data.get(6)) 
 			break
		}
	}
    
    log.debug "AQUARA-WS RESULT: $result"
	return result
}

private Map parseReportAttributeMessage(String description) {

	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	log.debug "AQUARA-WS DM: $descMap"

	Map resultMap = [:]
	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		resultMap = getBatteryResult(Integer.parseInt(descMap.value, 16))
	} else if (descMap.cluster == "0000" && descMap.attrId == "0005") {
    	// [raw:XXXXXXXX…1000, dni:6C0D, endpoint:01, cluster:0000, size:7c, attrId:0005, encoding:42, value:XXXXXXXX…1000]
		// IGNORE this as according to Zigbee Cluster information https://gist.github.com/manup/d5e81c349df0cf4d82eadd9689211d60#file-general-xml-L149 
        // the attribute 0005 of cluster 0000 is just model identifier 
		log.debug "AQUARA-WS: Ignoring Model Identifier cluster"
	} else {
    	log.debug "AQUARA-WS: Please report this event to the developer with these details. DM: $descMap "
    }

	return resultMap
}

private Map getBatteryResult(rawValue) {
	
    def result = [:]
    def linkText = getLinkText(device)
    def battLevel = Math.round(rawValue * 100 / 255)

    result.name = 'battery'
	result.translatable = true
	result.descriptionText = "${device.displayName} battery was ${battLevel}%"
    result.value = Math.min(100, battLevel)

	return result
}

// ******* FUNCTIONS FOR REFRESHING DEVICE STATE ************

def refresh() {

	log.debug "AQUARA-WS: Refreshing Battery"
    
	def refreshCmds = [
		"st rattr 0x${device.deviceNetworkId} 1 1 0x00", "delay 2000",
		"st rattr 0x${device.deviceNetworkId} 1 1 0x20", "delay 2000"
	]
    
	return refreshCmds + enrollResponse()
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	
	log.debug "AQUARA-WS: Configuring Reporting and Bindings."
    
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)

	device.endpointId = 1
	def configureCmds = [
    	"delay 1000",
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 2000"
	]
    
	return configureCmds + zigbee.batteryConfig() + refresh() // send refresh cmds as part of config
}

def enrollResponse() {
	log.debug "AQUARA-WS: Sending Enroll Response"
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
    
    def enrollCmds = [
		//Resending the CIE in case the enroll request is sent before CIE is written
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 2000",
		//Enroll Response
		"raw 0x500 {01 23 00 00 00}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 2000"
	]
    
    return enrollCmds
}

// ***************** SUPPORTING FUNCTIONS *************************

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

// ***************** END OF FUNCTIONS *************************