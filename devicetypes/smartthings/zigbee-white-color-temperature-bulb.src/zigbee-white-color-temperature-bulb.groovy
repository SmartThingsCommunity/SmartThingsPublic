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
 *  ZigBee White Color Temperature Bulb
 *
 *  Author: SmartThings
 *  Date: 2015-09-22
 */

metadata {
    definition (name: "ZigBee White Color Temperature Bulb", namespace: "smartthings", author: "SmartThings") {

        capability "Actuator"
        capability "Color Temperature"
        capability "Configuration"
        capability "Health Check"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"

        attribute "colorName", "string"
        command "setGenericName"

        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters: "0019"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04", outClusters: "0019"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY BR Tunable White", deviceJoinName: "SYLVANIA Smart BR30 Tunable White"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY RT Tunable White", deviceJoinName: "SYLVANIA Smart RT5/6 Tunable White"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Classic A60 TW", deviceJoinName: "OSRAM LIGHTIFY LED Classic A60 Tunable White"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY A19 Tunable White", deviceJoinName: "SYLVANIA Smart A19 Tunable White"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Classic B40 TW - LIGHTIFY", deviceJoinName: "OSRAM LIGHTIFY Classic B40 Tunable White"
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2700..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }
        valueTile("colorName", "device.colorName", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "colorName", label: '${currentValue}'
        }

        main(["switch"])
        details(["switch", "colorTempSliderControl", "colorName", "refresh"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description is $description"
    def event = zigbee.getEvent(description)
    if (event) {
        if (event.name=="level" && event.value==0) {}
        else {
            if (event.name=="colorTemperature") {
                setGenericName(event.value)
            }
            sendEvent(event)
        }
    }
    else {
        Map bindingTable = parseBindingTableResponse(description)
        if (bindingTable) {
            List<String> cmds = []
            bindingTable.table_entries.inject(cmds) { acc, entry ->
                // The binding entry is not for our hub and should be deleted
                if (entry["dstAddr"] != zigbeeEui) {
                    acc.addAll(removeBinding(entry.clusterId, entry.srcAddr, entry.srcEndpoint, entry.dstAddr, entry.dstEndpoint))
                }
                acc
            }
            // There are more entries that we haven't examined yet
            if (bindingTable.numTableEntries > bindingTable.startIndex + bindingTable.numEntriesReturned) {
                def startPos
                if (cmds) {
                    log.warn "Removing binding entries for other devices: $cmds"
                    // Since we are removing some entries, we should start in the same spot as we just read since values
                    // will fill in the newly vacated spots
                    startPos = bindingTable.startIndex
                } else {
                    // Since we aren't removing anything we move forward to the next set of table entries
                    startPos = bindingTable.startIndex + bindingTable.numEntriesReturned
                }
                cmds.addAll(requestBindingTable(startPos))
            }
            sendHubCommand(cmds.collect { it ->
                new physicalgraph.device.HubAction(it)
            }, 2000)
        } else {
            def cluster = zigbee.parse(description)

            if (cluster && cluster.clusterId == 0x0006 && cluster.command == 0x07) {
                if (cluster.data[0] == 0x00) {
                    log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
                    sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
                }
                else {
                    log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
                }
            }
            else {
                log.warn "DID NOT PARSE MESSAGE for description : $description"
                log.debug "${cluster}"
            }
        }
    }
}

def parseBindingTableResponse(description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
    if (descMap["clusterInt"] == 0x8033) {
        def header_field_lengths = ["transactionSeqNo": 1, "status": 1, "numTableEntries": 1, "startIndex": 1, "numEntriesReturned": 1]
        def field_values = [:]
        def data = descMap["data"]
        header_field_lengths.each { k, v ->
            field_values[k] = Integer.parseInt(data.take(v).join(""), 16);
            data = data.drop(v);
        }

        List<Map> table = []
        if (field_values.numEntriesReturned) {
            def table_entry_lengths = ["srcAddr": 8, "srcEndpoint": 1, "clusterId": 2, "dstAddrMode": 1]
            for (def i : 0..(field_values.numEntriesReturned - 1)) {
                def entryMap = [:]
                table_entry_lengths.each { k, v ->
                    def val = data.take(v).reverse().join("")
                    entryMap[k] = val.length() < 8 ? Integer.parseInt(val, 16) : val
                    data = data.drop(v)
                }

                switch (entryMap.dstAddrMode) {
                    case 0x01:
                        entryMap["dstAddr"] = data.take(2).reverse().join("")
                        data = data.drop(2)
                        break
                    case 0x03:
                        entryMap["dstAddr"] = data.take(8).reverse().join("")
                        data = data.drop(8)
                        entryMap["dstEndpoint"] = Integer.parseInt(data.take(1).join(""), 16)
                        data = data.drop(1)
                        break
                }
                table << entryMap
            }
        }
        field_values["table_entries"] = table
        return field_values
    }
    return [:]
}

def requestBindingTable(startPos=0) {
    return ["zdo mgmt-bind 0x${zigbee.deviceNetworkId} $startPos"]
}

def removeBinding(cluster, srcAddr, srcEndpoint, destAddr, destEndpoint) {
    return ["zdo unbind unicast 0x${zigbee.deviceNetworkId} {${srcAddr}} $srcEndpoint $cluster {${destAddr}} $destEndpoint"]
}


def off() {
    zigbee.off()
}

def on() {
    zigbee.on()
}

def setLevel(value) {
    zigbee.setLevel(value)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return zigbee.onOffRefresh()
}

def refresh() {
    zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.colorTemperatureRefresh() + zigbee.onOffConfig(0, 300) + zigbee.levelConfig() + zigbee.colorTemperatureConfig()
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    // Device-Watch allows 3 check-in misses from device (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    sendEvent(name: "checkInterval", value: 3 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    refresh() + requestBindingTable(0) + ["delay 2000"]
}

def setColorTemperature(value) {
    setGenericName(value)
    zigbee.setColorTemperature(value)
}

//Naming based on the wiki article here: http://en.wikipedia.org/wiki/Color_temperature
def setGenericName(value){
    if (value != null) {
        def genericName = "White"
        if (value < 3300) {
            genericName = "Soft White"
        } else if (value < 4150) {
            genericName = "Moonlight"
        } else if (value <= 5000) {
            genericName = "Cool White"
        } else if (value >= 5000) {
            genericName = "Daylight"
        }
        sendEvent(name: "colorName", value: genericName)
    }
}
