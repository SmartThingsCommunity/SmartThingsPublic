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
    definition (name: "Zigbee Metering Plug", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.smartplug", mnmn: "SmartThings",  vid: "generic-switch-power-energy") {
        capability "Energy Meter"
        capability "Power Meter"
        capability "Actuator"
        capability "Switch"
        capability "Refresh"
        capability "Health Check"
        capability "Sensor"
        capability "Configuration"

        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B04, 0702, FC82", outClusters: "0003, 000A, 0019", manufacturer: "LDS", model: "ZB-ONOFFPlug-D0000",  deviceJoinName: "Outlet" //Smart Plug
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B04, 0702, FC82", outClusters: "0003, 000A, 0019", manufacturer: "LDS", model: "ZB-ONOFFPlug-D0005",  deviceJoinName: "Outlet" //Smart Plug
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702, 0B04", outClusters: "0003", manufacturer: "REXENSE", model: "HY0105", deviceJoinName: "HONYAR Outlet" //HONYAR Smart Outlet (USB)
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702, 0B04", outClusters: "0003", manufacturer: "REXENSE", model: "HY0104", deviceJoinName: "HONYAR Outlet" //HONYAR Smart Outlet
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0009, 0702, 0B04", outClusters: "0003, 0019", manufacturer: "HEIMAN", model: "E_Socket", deviceJoinName: "HEIMAN Outlet" //HEIMAN Smart Outlet
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B04, 0702, FC82", outClusters: "0003, 000A, 0019", manufacturer: "sengled", model: "E1C-NB7",  deviceJoinName: "Sengled Outlet" //Sengled Smart Plug with Energy Tracker
        fingerprint manufacturer: "DAWON_DNS", model: "PM-B430-ZB", deviceJoinName: "Dawon Outlet" // DAWON DNS Smart Plug PM-B430-ZB (10A), raw description: 01 0104 0051 01 07 0000, 0004, 0003, 0006, 0019, 0702, 0B04 07 0000, 0004, 0003, 0006, 0019, 0702, 0B04
        fingerprint manufacturer: "DAWON_DNS", model: "PM-B530-ZB", deviceJoinName: "Dawon Outlet" // DAWON DNS Smart Plug PM-B530-ZB (16A), raw description: 01 0104 0051 01 07 0000, 0004, 0003, 0006, 0019, 0702, 0B04 07 0000, 0004, 0003, 0006, 0019, 0702, 0B04
        fingerprint manufacturer: "DAWON_DNS", model: "PM-C140-ZB", deviceJoinName: "Dawon Outlet" // DAWON DNS In-Wall Outlet PM-C140-ZB, raw description: 01 0104 0051 01 0A 0000 0002 0003 0004 0006 0019 0702 0B04 0008 0009 0A 0000 0002 0003 0004 0006 0019 0702 0B04 0008 0009
        fingerprint profileId: "0104", inClusters: "0000, 0002, 0003, 0006, 0702, 0B04", outClusters: "0003, 0019", manufacturer: "DAWON_DNS", model: "PM-B540-ZB",  deviceJoinName: "Dawon Outlet" // DAWON DNS Smart Plug
        fingerprint profileId: "0104", inClusters: "0000, 0002, 0003, 0006, 0702, 0B04", outClusters: "0003, 0019", manufacturer: "DAWON_DNS", model: "ST-B550-ZB",  deviceJoinName: "Dawon Outlet" // DAWON DNS Smart Plug
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0006, 0702, 0B04", outClusters: "0003, 0019", manufacturer: "DAWON_DNS", model: "PM-C150-ZB",  deviceJoinName: "Dawon Outlet" // DAWON DNS In-Wall Outlet
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0006, 0702, 0B04", outClusters: "0003, 0019", manufacturer: "DAWON_DNS", model: "PM-C250-ZB",  deviceJoinName: "Dawon Outlet" // DAWON DNS In-Wall Outlet
	}

    tiles(scale: 2){
        multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
                tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                        attributeState("on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc")
                        attributeState("off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff")
                }
        }
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
                state "default", label:'${currentValue} W'
        }
        valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
                state "default", label:'${currentValue} kWh'
        }
        standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
                state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
                state "default", label:'reset kWh', action:"reset"
        }

        main(["switch"])
        details(["switch","power","energy","refresh","reset"])
    }
}

def getATTRIBUTE_READING_INFO_SET() { 0x0000 }
def getATTRIBUTE_HISTORICAL_CONSUMPTION() { 0x0400 }

def parse(String description) {
    log.debug "description is $description"
    def event = zigbee.getEvent(description)
    def descMap = zigbee.parseDescriptionAsMap(description)

    if (event) {
        log.info "event enter:$event"
        if (event.name == "switch" && !descMap.isClusterSpecific && descMap.commandInt == 0x0B) {
            log.info "Ignoring default response with desc map: $descMap"
            return [:]
        } else if (event.name== "power") {
            event.value = event.value/getPowerDiv()
            event.unit = "W"
        } else if (event.name== "energy") {
            event.value = event.value/getEnergyDiv()
            event.unit = "kWh"
        }
        log.info "event outer:$event"
        sendEvent(event)
    } else {
        List result = []
        log.debug "Desc Map: $descMap"

        List attrData = [[clusterInt: descMap.clusterInt ,attrInt: descMap.attrInt, value: descMap.value]]
        descMap.additionalAttrs.each {
            attrData << [clusterInt: descMap.clusterInt, attrInt: it.attrInt, value: it.value]
        }

        attrData.each {
                def map = [:]
                if (it.value && it.clusterInt == zigbee.SIMPLE_METERING_CLUSTER && it.attrInt == ATTRIBUTE_HISTORICAL_CONSUMPTION) {
                        log.debug "power"
                        map.name = "power"
                        map.value = zigbee.convertHexToInt(it.value)/getPowerDiv()
                        map.unit = "W"
                }
                else if (it.value && it.clusterInt == zigbee.SIMPLE_METERING_CLUSTER && it.attrInt == ATTRIBUTE_READING_INFO_SET) {
                        log.debug "energy"
                        map.name = "energy"
                        map.value = zigbee.convertHexToInt(it.value)/getEnergyDiv()
                        map.unit = "kWh"
                }

                if (map) {
                        result << createEvent(map)
                }
                log.debug "Parse returned $map"
        }
        return result
    }
}

def off() {
    def cmds = zigbee.off()
    if (device.getDataValue("model") == "HY0105") {
        cmds += zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", [destEndpoint: 0x02])
    }
    return cmds
}


def on() {
    def cmds = zigbee.on()
    if (device.getDataValue("model") == "HY0105") {
        cmds += zigbee.command(zigbee.ONOFF_CLUSTER, 0x01, "", [destEndpoint: 0x02])
    }
    return cmds
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def refresh() {
    log.debug "refresh"
    zigbee.onOffRefresh() +
    zigbee.electricMeasurementPowerRefresh() +
    zigbee.readAttribute(zigbee.SIMPLE_METERING_CLUSTER, ATTRIBUTE_READING_INFO_SET)
}

def configure() {
    // this device will send instantaneous demand and current summation delivered every 1 minute
    sendEvent(name: "checkInterval", value: 2 * 60 + 10 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    log.debug "Configuring Reporting"
    return refresh() +
    	   zigbee.onOffConfig() +
           zigbee.configureReporting(zigbee.SIMPLE_METERING_CLUSTER, ATTRIBUTE_READING_INFO_SET, DataType.UINT48, 1, 600, 1) +
           zigbee.electricMeasurementPowerConfig(1, 600, 1) +
           zigbee.simpleMeteringPowerConfig()
}

private int getPowerDiv() {
    isSengledOutlet() ? 10 : 1
}

private int getEnergyDiv() {
    if (isDawonOutlet()) {
        1000
    } else if (isSengledOutlet()) {
        10000
    } else {
        100
    }
}

private boolean isSengledOutlet() {
    device.getDataValue("model") == "E1C-NB7"
}

private boolean isDawonOutlet() {
    device.getDataValue("manufacturer") == "DAWON_DNS"
}
