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
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition (name: "Zigbee Power Meter", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", ocfDeviceType: "x.com.st.d.energymeter", vid: "SmartThings-smartthings-Aeon_Home_Energy_Meter") {
        capability "Energy Meter"
        capability "Power Meter"
        capability "Refresh"
        capability "Health Check"
        capability "Sensor"
        capability "Configuration"
		capability "Power Consumption Report"

        fingerprint profileId: "0104", deviceId:"0053", inClusters: "0000, 0003, 0004, 0B04, 0702", outClusters: "0019", manufacturer: "", model: "E240-KR080Z0-HA", deviceJoinName: "Energy Monitor" //Smart Sub-meter(CT Type)
        fingerprint profileId: "0104", deviceId:"0007", inClusters: "0000,0003,0702", outClusters: "000A", manufacturer: "Develco", model: "ZHEMI101", deviceJoinName: "frient Energy Monitor" // frient External Meter Interface (develco) 02 0104 0007 00 03 0000 0003 0702 01 000A
        fingerprint profileId: "0104", manufacturer: "Develco Products A/S", model: "EMIZB-132", deviceJoinName: "frient Energy Monitor" // frient Norwegian HAN (develco) 02 0104 0053 00 06 0000 0003 0020 0702 0704 0B04 03 0003 000A 0019
        fingerprint profileId: "0104", manufacturer: "ShinaSystem", model: "PMM-300Z1", deviceJoinName: "SiHAS Energy Monitor" // SIHAS Power Meter 01 0104 0000 01 05 0000 0004 0003 0B04 0702 02 0004 0019
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

def getATTRIBUTE_READING_INFO_SET() { 0x0000 }
def getATTRIBUTE_HISTORICAL_CONSUMPTION() { 0x0400 }
def getATTRIBUTE_ACTIVE_POWER() { 0x050B }

def parse(String description) {
    log.debug "description is $description"
    def event = zigbee.getEvent(description)
    if (event) {
        log.info event
        if (event.name == "power") {
            def descMap = zigbee.parseDescriptionAsMap(description)
            log.debug "event : Desc Map: $descMap"
            if (descMap.clusterInt == zigbee.ELECTRICAL_MEASUREMENT_CLUSTER && descMap.attrInt == ATTRIBUTE_ACTIVE_POWER) {
                event.value = event.value/activePowerDivisor
                event.unit = "W"
            } else {
                event.value = event.value/powerDivisor
                event.unit = "W"
            }
        } else if (event.name == "energy") {
            event.value = event.value/(energyDivisor * 1000)
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
                    if (it.clusterInt == zigbee.SIMPLE_METERING_CLUSTER && it.attrInt == ATTRIBUTE_HISTORICAL_CONSUMPTION) {
                        log.debug "meter"
                        map.name = "power"
                        map.value = zigbee.convertHexToInt(it.value)/powerDivisor
                        map.unit = "W"
                    }
                    if (it.clusterInt == zigbee.ELECTRICAL_MEASUREMENT_CLUSTER && it.attrInt == ATTRIBUTE_ACTIVE_POWER) {
                        log.debug "meter"
                        map.name = "power"
                        map.value = zigbee.convertHexToInt(it.value)/activePowerDivisor
                        map.unit = "W"
                    }
                    if (it.clusterInt == zigbee.SIMPLE_METERING_CLUSTER && it.attrInt == ATTRIBUTE_READING_INFO_SET) {
                        log.debug "energy"
                        map.name = "energy"
                        map.value = zigbee.convertHexToInt(it.value)/(energyDivisor * 1000)
                        map.unit = "kWh"

						if (isEZEX()) {
							def currentEnergy = zigbee.convertHexToInt(it.value) / 1000
							def prevPowerConsumption = device.currentState("powerConsumption")?.value
							Map previousMap = prevPowerConsumption ? new groovy.json.JsonSlurper().parseText(prevPowerConsumption) : [:]
							def deltaEnergy = calculateDelta(currentEnergy, previousMap)
							def currentTimestamp = Calendar.getInstance().timeInMillis
							def prevTimestamp = device.currentState("powerConsumption")?.date?.time
							if (prevTimestamp == null) prevTimestamp = 0L
							def timeDiff = currentTimestamp - prevTimestamp
							log.debug "currentTimestamp= $currentTimestamp, prevTimestamp= $prevTimestamp, timeDiff= $timeDiff"
							log.debug "deltaEnergy= $deltaEnergy"
							if (deltaEnergy < 0) {
								Map reportMap = [:]
								reportMap["energy"] = currentEnergy
								reportMap["deltaEnergy"] = 0
								sendEvent("name": "powerConsumption", "value": reportMap.encodeAsJSON(), displayed: false)
							} else {
								if (timeDiff >= 15 * 60 * 1000) {
									Map reportMap = [:]
									reportMap["energy"] = currentEnergy
									if (timeDiff < 24 * 60 * 60 * 1000) {
										reportMap["deltaEnergy"] = deltaEnergy
									} else {
										reportMap["deltaEnergy"] = 0
									}
									sendEvent("name": "powerConsumption", "value": reportMap.encodeAsJSON(), displayed: false)
								}
							}
						}
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

def resetEnergyMeter() {
	log.debug "resetEnergyMeter: not implemented"
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
           zigbee.readAttribute(zigbee.SIMPLE_METERING_CLUSTER, ATTRIBUTE_READING_INFO_SET) + 
           zigbee.simpleMeteringPowerRefresh()
}

def configure() {
    // this device will send instantaneous demand and current summation delivered every 1 minute
    sendEvent(name: "checkInterval", value: 2 * 60 + 10 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    log.debug "Configuring Reporting"
    return refresh() +
           zigbee.simpleMeteringPowerConfig() +
           zigbee.configureReporting(zigbee.SIMPLE_METERING_CLUSTER, ATTRIBUTE_READING_INFO_SET, DataType.UINT48, 1, 600, 1) + 
           zigbee.electricMeasurementPowerConfig()
}

private getActivePowerDivisor() { isPMM300Z1() ? 1 : 10 }
private getPowerDivisor() { (isFrientSensor() || isPMM300Z1()) ? 1 : 1000 }
private getEnergyDivisor() { (isFrientSensor() || isPMM300Z1()) ? 1 : 1000 }

private Boolean isFrientSensor() {
	device.getDataValue("manufacturer") == "Develco Products A/S" ||
		device.getDataValue("manufacturer") == "Develco"
}

private Boolean isPMM300Z1() {
    device.getDataValue("model") == "PMM-300Z1"
}

private Boolean isEZEX() {
	device.getDataValue("model") == "E240-KR080Z0-HA"
}

BigDecimal calculateDelta(BigDecimal currentEnergy, Map previousMap) {
	if (previousMap?.'energy' == null) {
		log.debug "prevEnergy is null"
		return 0
	}
	BigDecimal lastAccumulated = BigDecimal.valueOf(previousMap['energy'])
	log.debug "currentEnergy= $currentEnergy, prevEnergy= $lastAccumulated"
	return currentEnergy.subtract(lastAccumulated)
}