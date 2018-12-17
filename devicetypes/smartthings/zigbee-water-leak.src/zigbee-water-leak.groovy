/*
 *  Copyright 2018 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 *
 *  Water Leak Sensor
 *
 *  Author: Fen Mei/f.mei@samsung.com
 *
 *  Date:2018-12-14
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition(name: "Zigbee Water Leak Sensor", namespace: "smartthings", author: "SmartThings", vid: "SmartThings-smartthings-Xiaomi_Aqara_Leak_Sensor-2", mnmn:"SmartThings", ocfDeviceType: "x.com.st.d.sensor.moisture") {
        capability "Configuration"
        capability "Refresh"
        capability "Water Sensor"
        capability "Sensor"
        capability "Health Check"
        capability "Battery"

        fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000, 0003, 0500", manufacturer: "LUMI", model: "lumi.flood.agl02", deviceJoinName: "LUMI Water Leak Sensor"
    }

    simulator {

        status "dry": "zone status 0x0020 -- extended status 0x00"
        status "wet": "zone status 0x0021 -- extended status 0x00"

        for (int i = 0; i <= 90; i += 10) {
            status "battery 0021 0x${i}": "read attr - raw: 8C900100010A21000020C8, dni: 8C90, endpoint: 01, cluster: 0001, size: 0A, attrId: 0021, result: success, encoding: 20, value: ${i}"
        }
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"water", type: "generic", width: 6, height: 4){
            tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
                attributeState "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
                attributeState "wet", icon:"st.alarm.water.wet", backgroundColor:"#00a0dc"
            }
        }

        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label:'${currentValue}% battery', unit:""
        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main "water"
        details(["water", "battery", "refresh"])
    }
}

def parse(String description) {
	Map map = [:]

	List listMap = []
	List listResult = []

	log.debug "parse: Parse message: ${description}"

	if (description?.startsWith("enroll request")) {
		List cmds = enrollResponse()

		log.debug "parse: enrollResponse() ${cmds}"
		listResult = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	else {
		if (description?.startsWith("zone status")) {
			listMap = parseIasMessage(description)
		}
		else if (description?.startsWith("catchall:")) {
			map = parseCatchAllMessage(description)
		}

		// Create events from map or list of maps, whichever was returned
		if (listMap) {
			for (msg in listMap) {
				listResult << createEvent(msg)
			}
		}
		else if (map) {
			listResult << createEvent(map)
		}
	}

	log.debug "parse: listResult ${listResult}"
	return listResult
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)

	if (shouldProcessMessage(cluster)) {
		def msgStatus = cluster.data[2]

		log.debug "parseCatchAllMessage: msgStatus: ${msgStatus}"
		if (msgStatus == 0) {
			switch(cluster.clusterId) {
				case 0x0500:
                                        Map descMap = zigbee.parseDescriptionAsMap(description)
					if (descMap?.attrInt == 0x0002) {
						resultMap.name = "water"
						def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
						resultMap.value = zs.isAlarm1Set() ? "wet":"dry"
					}
					break
				default:
					break
			}
		}
		else {
			log.debug "parseCatchAllMessage: Message error code: Error code: ${msgStatus}    ClusterID: ${cluster.clusterId}    Command: ${cluster.command}"
		}
	}

	return resultMap
}

private boolean shouldProcessMessage(cluster) {
	// 0x0B is default response indicating message got through
	// 0x07 is bind message
	boolean ignoredMessage = cluster.profileId != 0x0104 ||
			cluster.command == 0x0B ||
			cluster.command == 0x07 ||
			(cluster.data.size() > 0 && cluster.data.first() == 0x3e)

	return !ignoredMessage
}

private List parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	log.debug "parseIasMessage: $description"

	List resultListMap = []
	Map resultMap_battery = [:]
	Map resultMap_sensor = [:]

	resultMap_sensor.name = "water"
	resultMap_sensor.value = zs.isAlarm1Set() ? "wet":"dry"

	// Check each relevant bit, create map for it, and add to list
	log.debug "parseIasMessage: Battery Status ${zs.battery}"
	log.debug "parseIasMessage: Trouble Status ${zs.trouble}"
	log.debug "parseIasMessage: Sensor Status ${zs.alarm1}"

        //LUMI IAS sensor report battery ok or low instead of battery percentage. 
        if(device.getDataValue("manufacturer") == "LUMI") {
	    	 if (zs.isTroubleSet()) {
	    		 resultMap_battery.name = "battery"
	    		 resultMap_battery.value = 50
	    	 }
	    	 else {
	    		 if (zs.isBatterySet()) {
	    			 // to generate low battery notification by the platform
	    			 resultMap_battery.name = "battery"
	    			 resultMap_battery.value = 5
	    		 }
	    		 else {
	    			 resultMap_battery.name = "battery"
	    			 resultMap_battery.value = 50
	    		 }
	    	 }

	    	 resultListMap << resultMap_battery
    	}
	resultListMap << resultMap_sensor

	return resultListMap
}

def ping() {
    refresh()
}

def refresh() {
    log.debug "Refreshing Values"
    def refreshCmds = []
    refreshCmds += readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)

    return refreshCmds
}

def installed(){
    log.debug "call installed()"
    sendEvent(name: "checkInterval", value: 20 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def configure() {
    sendEvent(name: "checkInterval", value: 20 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
                    
    return zigbee.enrollResponse() + zigbee.iasZoneConfig(30, 60 * 30)
}

