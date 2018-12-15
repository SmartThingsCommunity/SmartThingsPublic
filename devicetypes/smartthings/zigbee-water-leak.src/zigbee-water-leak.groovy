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
    log.debug "description: $description"

    def result
    Map map = zigbee.getEvent(description)

    if (!map) {
        if (description?.startsWith('zone status')) {
            map = getMoistureResult(description)
        } else if (description?.startsWith('enroll request')) {
            List cmds = zigbee.enrollResponse()
            log.debug "enroll response: ${cmds}"
            result = cmds?.collect { new physicalgraph.device.HubAction(it) }
        } else {
            Map descMap = zigbee.parseDescriptionAsMap(description)
            log.debug "${descMap}"
            if (descMap?.clusterInt == 0x0500 && descMap.attrInt == 0x0002) {
            	if (device.getDataValue("manufacturer") == "LUMI") {
                	sendBatteryResult(description)
                }
                map = getMoistureResult(description)
            } 
        }
    }
    
    if (map&&!result) {
        if (description?.startsWith('zone status')) {
            if (device.getDataValue("manufacturer") == "LUMI") {
                sendBatteryResult(description)
            }
		}
        result = createEvent(map)
    }
    
    log.debug "Parse returned $result"

    result
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
    sendEvent(name: "checkInterval", value: 30 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def configure() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
       
    return zigbee.enrollResponse() + zigbee.iasZoneConfig(30, 60 * 30)
}

def getMoistureResult(description) {
    ZoneStatus zs = zigbee.parseZoneStatus(description)
    def value = zs.isAlarm1Set()?"wet":"dry"
    [
        name           : 'water',
        value          : value,
        descriptionText: "${device.displayName} is $value",
        translatable   : true
    ]
}

/*
 * LUMI IAS sensors use cluster 0x0500, attr 0x0002, the third bit to report battery normal or low.
 * To adjust it,dicuessed with UI metadata developer, send battery 50% to indicate battery normal, 5% to indicate battery low.
 */

def sendBatteryResult(description) {
    def result = [:]
    ZoneStatus zs = zigbee.parseZoneStatus(description)
    def value = zs.isBatterySet()?5:50
    
    result.name = 'battery'
    result.value = value
    result.descriptionText = "${device.displayName} battery value is ${value}"
    result.translatable = true
    
    sendEvent(result)
}
