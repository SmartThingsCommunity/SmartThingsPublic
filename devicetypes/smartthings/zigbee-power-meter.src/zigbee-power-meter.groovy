/**
 *  Copyright 2019 SmartThings
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
metadata {
    definition (name: "Zigbee Power Meter", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", ocfDeviceType: "x.com.st.d.energymeter", vid: "SmartThings-smartthings-Aeon_Home_Energy_Meter") {
        capability "Energy Meter"
        capability "Power Meter"
        capability "Refresh"
        capability "Health Check"
        capability "Sensor"
        capability "Configuration"

        fingerprint profileId: "0104", deviceId:"0053", inClusters: "0000, 0003, 0004, 0B04, 0702", outClusters: "0019", manufacturer: "", model: "E240-KR080Z0-HA", deviceJoinName: "Energy Monitor" //Smart Sub-meter(CT Type)
        fingerprint profileId: "0104", deviceId:"0007", inClusters: "0000,0003,0702", outClusters: "000A", manufacturer: "Develco", model: "ZHEMI101", deviceJoinName: "frient Energy Monitor" // frient External Meter Interface (develco)
        fingerprint profileId: "0104", manufacturer: "Develco Products A/S", model: "EMIZB-132", deviceJoinName: "frient Energy Monitor" // frient Norwegian HAN (develco)
    }

    // tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"power", type: "generic", width: 6, height: 4){
            tileAttribute("device.power", key: "PRIMARY_CONTROL") {
                attributeState("default", label:'${currentValue} W')
            }
            tileAttribute("device.energy", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue} kWh')
            }
        }
        standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'reset kWh', action:"reset"
        }
        standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main (["power", "energy"])
        details(["power", "energy", "reset", "refresh"])
    }
}

private getFrientDivisor() { 1 }

def parse(String description) {
    log.debug "description is $description"
    def event = zigbee.getEvent(description)
    if (event) {
        log.info event
        if (event.name == "power") {
            def descMap = zigbee.parseDescriptionAsMap(description)
            log.debug "event : Desc Map: $descMap"
            if (descMap.clusterInt == 0x0B04 && descMap.attrInt == 0x050b) {
                event.value = event.value/10
                event.unit = "W"
            } else {
                event.value = event.value/(isFrientSensor() ? frientDivisor : 1000)
                event.unit = "W"
            }
        } else if (event.name == "energy") {
            event.value = event.value/(isFrientSensor() ? frientDivisor * 1000 : 1000000)
            event.unit = "kWh"
        }
        log.info "event outer:$event"
        sendEvent(event)
    } else {
        List result = []
        def descMap = zigbee.parseDescriptionAsMap(description)
        log.debug "Desc Map: $descMap"
                
        List attrData = [[clusterInt: descMap.clusterInt ,attrInt: descMap.attrInt, value: descMap.value, isValidForDataType: descMap.isValidForDataType]]
        descMap.additionalAttrs.each {
            attrData << [clusterInt: descMap.clusterInt, attrInt: it.attrInt, value: it.value, isValidForDataType: it.isValidForDataType]
        }
        attrData.each {
                def map = [:]
                if (it.isValidForDataType && (it.value != null)) {
                    if (it.clusterInt == 0x0702 && it.attrInt == 0x0400) {
                        log.debug "meter"
                        map.name = "power"
                        map.value = zigbee.convertHexToInt(it.value)/(isFrientSensor() ? frientDivisor : 1000)
                        map.unit = "W"
                    }
                    if (it.clusterInt == 0x0B04 && it.attrInt == 0x050b) {
                        log.debug "meter"
                        map.name = "power"
                        map.value = zigbee.convertHexToInt(it.value)/10
                        map.unit = "W"
                    }
                    if (it.clusterInt == 0x0702 && it.attrInt == 0x0000) {
                        log.debug "energy"
                        map.name = "energy"
                        map.value = zigbee.convertHexToInt(it.value)/(isFrientSensor() ? frientDivisor * 1000 : 1000000)
                        map.unit = "kWh"
                    }
                }
                
                if (map) {
                        result << createEvent(map)
                }
                log.debug "Parse returned $map"
        }
        return result      
    }
}


/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def refresh() {
    log.debug "refresh "
    zigbee.electricMeasurementPowerRefresh() +
           zigbee.simpleMeteringPowerRefresh()
}

def configure() {
    // this device will send instantaneous demand and current summation delivered every 1 minute
    sendEvent(name: "checkInterval", value: 2 * 60 + 10 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    log.debug "Configuring Reporting"
    return refresh() +
           zigbee.simpleMeteringPowerConfig() +
           zigbee.electricMeasurementPowerConfig()
}

private Boolean isFrientSensor() {
	device.getDataValue("manufacturer") == "Develco Products A/S" ||
		device.getDataValue("manufacturer") == "Develco"
}