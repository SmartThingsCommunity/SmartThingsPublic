/**
 *  Copyright 2021 SmartThings
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
    definition (name: "Zigbee Energy Meter Power Consumption Report", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", ocfDeviceType: "x.com.st.d.energymeter", vid: "SmartThings-smartthings-zigbee-energy-meter-powerconsumption-report") {
        capability "Energy Meter"
        capability "Power Meter"
        capability "Refresh"
        capability "Health Check"
        capability "Sensor"
        capability "Configuration"
		capability "Power Consumption Report"
        
        fingerprint profileId: "0104", deviceId:"0053", inClusters: "0000, 0003, 0004, 0B04, 0702", outClusters: "0019", manufacturer: "eZEX", model: "E240-KR080Z0-HA", deviceJoinName: "Energy Monitor" //Smart Sub-meter(CT Type)
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
            
                        def currentEnergy = zigbee.convertHexToInt(it.value)
                        def currentPowerConsumption = device.currentState("powerConsumption")?.value
                        Map previousMap = currentPowerConsumption ? new groovy.json.JsonSlurper().parseText(currentPowerConsumption) : [:]
                        def deltaEnergy = calculateDelta (currentEnergy, previousMap)
                        Map reportMap = [:]
                        reportMap["energy"] = currentEnergy
                        reportMap["deltaEnergy"] = deltaEnergy 
                        sendEvent("name": "powerConsumption", "value": reportMap.encodeAsJSON(), displayed: false)
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
    // this device will send instantaneous demand and current summation delivered every 12 minute
    sendEvent(name: "checkInterval", value: 2 * 60 + 10 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    log.debug "Configuring Reporting"
    return refresh() +
           zigbee.simpleMeteringPowerConfig() +
           zigbee.configureReporting(zigbee.SIMPLE_METERING_CLUSTER, ATTRIBUTE_READING_INFO_SET, DataType.UINT48, 1, 600, 1) + 
           zigbee.electricMeasurementPowerConfig()
}

private getActivePowerDivisor() { 10 }
private getPowerDivisor() { 1000 }
private getEnergyDivisor() { 1000 }

BigDecimal calculateDelta (BigDecimal currentEnergy, Map previousMap) {
	if (previousMap?.'energy' == null) {
		return 0;
	}
	BigDecimal lastAcumulated = BigDecimal.valueOf(previousMap ['energy']);
	return currentEnergy.subtract(lastAcumulated).max(BigDecimal.ZERO);
}