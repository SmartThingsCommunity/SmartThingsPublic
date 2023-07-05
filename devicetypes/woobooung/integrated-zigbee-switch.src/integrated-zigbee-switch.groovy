/*
 *  Integrated ZigBee Switch
 * 
 *  Copyright 2020 SmartThings
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
 *  author : woobooung@gmail.com 
 */
public static String version() { return "v0.0.27a.20201219_Lidl_Mod" }
/*
 *   2020/12/19 >>> v0.0.27a_Lid Mod - Add Lidl Power Strip (by christianH21220)
 *   2020/12/13 >>> v0.0.27 - Add Tuya 4Gang switch(by bubibuhome@gmail.com)
 *   2020/12/11 >>> v0.0.26 - Add Tuya moes inner 2Gang switch(Zigbee+rf)
 *   2020/12/09 >>> v0.0.25 - Add Lonsonho Tuya Zigbee Smart Switch No Neutral 3Gang switch
 *   2020/10/24 >>> v0.0.24 - Add Zemi ZigBee 3Gang v2.5, eZex 4Gang switch, Tuya 2Gang switch
 *   2020/09/02 >>> v0.0.23 - Add Zemi ZigBee inline Switch
 *   2020/06/19 >>> v0.0.22 - Add Terncy Switch
 *   2020/06/16 >>> v0.0.21 - When changed parent's dni, replace child's dni(select settings's "Auto detecting and create device" : ON)
 *   2020/06/14 >>> v0.0.20 - Added ZigBee 3.0 USB Socket Plug
 *   2020/06/14 >>> v0.0.19 - Auto detecting and create option
 *   2020/06/14 >>> v0.0.18 - Child device type change
 *   2020/06/14 >>> v0.0.17 - Fixed health check issue
 *   2020/06/14 >>> v0.0.16 - Support All Controller switch and fixed for Tuya usb switch offline issue
 *   2020/06/13 >>> v0.0.15 - Tuya multitab support USB
 *   2020/06/01 >>> v0.0.14 - Tuya multitab without USB
 *   2020/05/29 >>> v0.0.13 - Tuya multitab with USB
 *   2020/05/27 >>> v0.0.12 - Aqara 1gang switch mapping to ZigBee Switch Power
 *   2020/05/22 >>> v0.0.11 - Added Aqara switch 1gang
 *   2020/05/14 >>> v0.0.10 - Added Zemi1gang switch
 *   2020/05/08 >>> v0.0.9  - Modified for Zemi 2gang switch
 *   2020/04/27 >>> v0.0.8  - Added Usage
 *   2020/04/26 >>> v0.0.7  - Support to old Zemi swtich version
 *   2020/04/26 >>> v0.0.6  - Added Zemi swtich, and modified child device type name
 *   2020/04/25 >>> v0.0.5  - Fixed minor issue - child device lebel
 *   2020/04/25 >>> v0.0.4  - Fixed minor issue
 *   2020/04/25 >>> v0.0.4  - Modified Child Device Lebel
 *   2020/04/25 >>> v0.0.4  - Fixed minor bugs
 *   2020/04/25 >>> v0.0.3  - Added Goqual Multi Switch & Modified refresh() function
 *   2020/04/25 >>> v0.0.2  - Added : DAWON DNS ZigBee Multi Switch 1 2 3 gang,  eZex ZigBee Multi Switch 6 gang, old Zigbee OnOff Swtich
 *   2020/04/25 >>> v0.0.1  - Initialize : Bandi ZigBee Switch, Zemi ZigBee Switch
 */

import java.lang.Math
import groovy.json.JsonOutput
/*
    > Usage <
    Example) Bandi 3gang switch model
    IDE Device Data   
      manufacturer: _TYZB01_pdevogdj
      model: TS0003
      Raw Description   01 0104 0100 00 05 0000 000A 0004 0005 0006 01 0019
       Analysis -> '01'(endpointId) '0104'(profileId) '0100'(deviceId) 00(skip) 05(skip) '0000 000A 0004 0005 0006'(inClusters) 01(skil) '0019'(outClusters)
    > Step1 - Add fingerprint
    fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 000A, 0004, 0005, 0006", outClusters: "0019", manufacturer: "_TYZB01_pdevogdj", model: "TS0003", deviceJoinName: "Bandi Zigbee Switch 1"
    > Step 2 - Add MODEL_MAP
    private getMODEL_MAP() { 
        [
            'TS0003' : 3,
            ....
            ...
            ..
        ]
    }
    > Step 3 - paring device and test
    > Step 4 - request add device infos to woobooung@gmail.com
*/

private getMODEL_MAP() { 
    [
        'TS0004' : 4,
        'TS0003' : 3,
        'TS0002' : 2,
        'TS0001' : 1,
        'TS0115' : 4,
        'TS0112' : 2,
        'TS0012' : 2,
        'TS0013' : 3,
        'TS011F' : 3,
        'PM-S340-ZB' : 3,
        'PM-S240-ZB' : 2,
        'PM-S140-ZB' : 1,
        'FNB56-ZSW03LX2.0' : 3,
        'FNB56-ZSW02LX2.0' : 2,
        'FNB56-ZSW01LX2.0' :1,
        'FB56+ZSW1GKJ2.7' : 1,
        'FB56+ZSW1HKJ2.5' : 2,
        'FB56+ZSW1IKJ2.7' : 3,
        'FB56+ZSW1IKJ2.5' : 3,
        'E220-KR4N0Z1-HA' : 4,
        'E220-KR6N0Z1-HA' : 6,
        'Lamp_01' : 1,
        'lumi.switch.b1naus01': 1,
        'lumi.switch.b2naus01': 2,
        'lumi.switch.b3naus01': 3,
        'lumi.ctrl_neutral1' : 1,
        'lumi.switch.b2laus01' : 2,
        'TERNCY-WS01-S2' : 2,
        'TERNCY-WS01-S3' : 3,
        'LXN59-2S7LX1.0' : 2
    ]
}

metadata {
    definition(name: "Integrated ZigBee Switch", namespace: "WooBooung", author: "Booung", ocfDeviceType: "oic.d.switch", vid: "generic-switch") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"
        capability "Switch"

        command "childOn", ["string"]
        command "childOff", ["string"]

        // HejHome & Bandi ZigBee Multi Switch
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 000A, 0004, 0005, 0006", outClusters: "0019", manufacturer: "_TZ3000_pdevogdj", model: "TS0003", deviceJoinName: "HejHome ZigBee Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 000A, 0004, 0005, 0006", outClusters: "0019", manufacturer: "_TZ3000_tas0zemd", model: "TS0002", deviceJoinName: "HejHome ZigBee Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 000A, 0004, 0005, 0006", outClusters: "0019", manufacturer: "_TZ3000_ddg0cycp", model: "TS0001", deviceJoinName: "HejHome ZigBee Switch"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 000A, 0004, 0005, 0006", outClusters: "0019", manufacturer: "_TYZB01_pdevogdj", model: "TS0003", deviceJoinName: "HejHome ZigBee Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 000A, 0004, 0005, 0006", outClusters: "0019", manufacturer: "_TYZB01_tas0zemd", model: "TS0002", deviceJoinName: "HejHome ZigBee Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 000A, 0004, 0005, 0006", outClusters: "0019", manufacturer: "_TYZB01_ddg0cycp", model: "TS0001", deviceJoinName: "HejHome ZigBee Switch"

        // DAWON DNS ZigBee Multi Switch
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0103", inClusters: "0000, 0004, 0003, 0006, 0019, 0002, 0009", outClusters: "0000, 0004, 0003, 0006, 0019, 0002, 0009", manufacturer: "DAWON_DNS", model: "PM-S140-ZB", deviceJoinName: "DAWON ZigBee Switch"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0103", inClusters: "0000, 0004, 0003, 0006, 0019, 0002, 0009", outClusters: "0000, 0004, 0003, 0006, 0019, 0002, 0009", manufacturer: "DAWON_DNS", model: "PM-S240-ZB", deviceJoinName: "DAWON ZigBee Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0103", inClusters: "0000, 0004, 0003, 0006, 0019, 0002, 0009", outClusters: "0000, 0004, 0003, 0006, 0019, 0002, 0009", manufacturer: "DAWON_DNS", model: "PM-S340-ZB", deviceJoinName: "DAWON ZigBee Switch 1"

        // Zemi ZigBee Multi Switch
        fingerprint endpointId: "10", profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", manufacturer: "Feibit Inc co.", model: "FB56+ZSW1GKJ2.7", deviceJoinName: "Zemi ZigBee Switch"
        fingerprint endpointId: "10", profileId: "0104", deviceId: "0002", inClusters: "0000, 0005, 0004, 0006", outClusters: "0000", manufacturer: "Feibit Inc co.", model: "FB56+ZSW1HKJ2.5", deviceJoinName: "Zemi ZigBee Switch 1"
        fingerprint endpointId: "10", profileId: "0104", deviceId: "0002", inClusters: "0000, 0003, 0004, 0005, 0006", manufacturer: "Feibit Inc co.", model: "FB56+ZSW1IKJ2.7", deviceJoinName: "Zemi ZigBee Switch 1"
        fingerprint endpointId: "12", profileId: "0104", deviceId: "0002", inClusters: "0000, 0005, 0004, 0006", manufacturer: "Feibit Inc co.", model: "FB56+ZSW1IKJ2.5", deviceJoinName: "Zemi ZigBee Switch 1"
        fingerprint endpointId: "0B", profileId: "C05E", inClusters: "0000, 0004, 0003, 0006, 0005, 1000, 0008", outClusters: "0019", manufacturer: "FeiBit", model: "FNB56-ZSW01LX2.0", deviceJoinName: "Zemi ZigBee Switch"
        fingerprint endpointId: "0B", profileId: "C05E", inClusters: "0000, 0004, 0003, 0006, 0005, 1000, 0008", outClusters: "0019", manufacturer: "FeiBit", model: "FNB56-ZSW02LX2.0", deviceJoinName: "Zemi ZigBee Switch 1"
        fingerprint endpointId: "01", profileId: "C05E", inClusters: "0000, 0004, 0003, 0006, 0005, 1000, 0008", outClusters: "0019", manufacturer: "FeiBit", model: "FNB56-ZSW03LX2.0", deviceJoinName: "Zemi ZigBee Switch 1"

        // eZex ZigBee Multi Switch
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000 0003 0004 0006", outClusters: "0006, 000A, 0019", manufacturer: "", model: "E220-KR6N0Z1-HA", deviceJoinName: "eZex ZigBee Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000 0003 0004 0006", outClusters: "0006, 000A, 0019", manufacturer: "", model: "E220-KR4N0Z1-HA", deviceJoinName: "eZex ZigBee Switch 1"

        // Aqara Switch
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 0002, 0003, 0004, 0005, 0006, 0009, 0702, 0B04", outClusters: "000A, 0019", manufacturer: "LUMI", model: "lumi.switch.b1naus01", deviceJoinName: "Aqara Switch"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 0002, 0003, 0004, 0005, 0006, 0009, 0702, 0B04", outClusters: "000A, 0019", manufacturer: "LUMI", model: "lumi.switch.b2naus01", deviceJoinName: "Aqara Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 0002, 0003, 0004, 0005, 0006, 0009, 0702, 0B04", outClusters: "000A, 0019", manufacturer: "LUMI", model: "lumi.switch.b3naus01", deviceJoinName: "Aqara Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 0002, 0003, 0004, 0005, 0006, 0009", outClusters: "000A, 0019", manufacturer: "LUMI", model: "lumi.switch.b2laus01", deviceJoinName: "Aqara Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0006", inClusters: "0000, 0003, 0001, 0002, 0019, 000A", outClusters: "0000, 000A, 0019", manufacturer: "LUMI", model: "lumi.ctrl_neutral1", deviceJoinName: "Aqara Switch"

        // Zigbee OnOff Swtich
        fingerprint endpointId: "0B", profileId: "0104", deviceId: "0100", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0000", manufacturer: "SZ", model: "Lamp_01", deviceJoinName: "Zigbee OnOff Switch"

        // Tuya multi switch
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 0004, 0005, 0006", outClusters: "0019, 000A", manufacturer: "_TZ3000_fvh3pjaz", model: "TS0012", deviceJoinName: "Tuya Multi Switch 1"
        // fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 0004, 0005, 0006", outClusters: "0019, 000A", manufacturer: "_TZ3000_pmz6mjyu", model: "TS011F", deviceJoinName: "Tuya Multi Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 000A, 0004, 0005, 0006", outClusters: "0019", manufacturer: "_TZ3000_go9rahj5", model: "TS0004", deviceJoinName: "Tuya Multi Switch 1"
        // not working
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 0004, 0005, 0006", outClusters: "0019, 000A", manufacturer: "_TZ3000_wyhuocal", model: "TS0013", deviceJoinName: "Tuya Multi Switch 1" //< - not working

        // Tuya multitab with USB
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0009", inClusters: "0000, 000A, 0004, 0005, 0006", outClusters: "0019", manufacturer: "_TYZB01_vkwryfdr", model: "TS0115", deviceJoinName: "Tuya Multi Tab Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0051", inClusters: "0000, 0003, 0004, 0005, 0006, 0702, 0B04", outClusters: "0019", manufacturer: "_TYZB01_b1ngbmlm", model: "TS0112", deviceJoinName: "Tuya USB Socket Plug 1"

        // Terncy Switch
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 0003, 0006, 0020, FCCC", outClusters: "0019", manufacturer: "Terncy", model: "TERNCY-WS01-S3", deviceJoinName: "Terncy Switch 1"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 0003, 0006, 0020, FCCC", outClusters: "0019", manufacturer: "Terncy", model: "TERNCY-WS01-S2", deviceJoinName: "Terncy Switch 1"

        // Unclear devices without model, meanufacturer
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0006, 0000, 0003", outClusters: "0019", manufacturer: "", model: "", deviceJoinName: "ZigBee Switch 1"

        // ZemiSmart zigbee in-wall switch 2gang
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0100", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019", manufacturer: "3A Smart Home DE", model: "LXN59-2S7LX1.0", deviceJoinName: "Zemi ZigBee inline Switch 1"
        
         // LIDL
         fingerprint endpointId: "01", profileId: "0104", deviceId: "010A", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "000A, 0019", manufacturer: "_TZ3000_1obwwnmq", model: "TS011F", deviceJoinName: "Lidl Strip Plug 1"
 }

    preferences {
        input name: "isAutoCreateChildDevice", type: "bool", title: "Auto detecting and create device", description: "default: true", defaultValue: true, required: true
        input name: "isCreateAllControllerSwitch", type: "bool", title: "Create All switch", description: "default: false", defaultValue: false, required: true
        input type: "paragraph", element: "paragraph", title: "Version", description: version(), displayDuringSetup: false
    }

    // simulator metadata
    simulator {
        // status messages
        status "on": "on/off: 1"
        status "off": "on/off: 0"

        // reply messages
        reply "zcl on-off on": "on/off: 1"
        reply "zcl on-off off": "on/off: 0"
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
            }
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        main "switch"
        details(["switch", "refresh"])
    }
}

def installed() {
    log.debug "installed()"
    def endpointCount = getEndpointCount()
    def model = device.getDataValue("model")
    if (endpointCount == 1) {
        // for 1 gang switch - ST Official local dth
        if (model == 'lumi.switch.b1naus01') {
            setDeviceType("ZigBee Switch Power")
        } else {
            setDeviceType("ZigBee Switch")
        }
    } else if (endpointCount > 1){
        if (model == 'FB56+ZSW1HKJ2.5' || model == 'FB56+ZSW1IKJ2.7') {
            device.updateDataValue("endpointId", "10")
        }
        // for multi switch, cloud device
        createChildDevices()
    }
    updateDataValue("onOff", "catchall")
    state.hasConfiguredHealthCheck = false
    refresh()
}

def updated() {
    log.debug "updated()"
    updateDataValue("onOff", "catchall")
    state.hasConfiguredHealthCheck = false
    refresh()
}

def parse(String description) {
    Map eventMap = zigbee.getEvent(description)
    Map eventDescMap = zigbee.parseDescriptionAsMap(description)

    if (!eventMap && eventDescMap) {
        eventMap = [:]
        if (eventDescMap?.clusterId == zigbee.ONOFF_CLUSTER) {
            eventMap[name] = "switch"
            eventMap[value] = eventDescMap?.value
        }
    }

    if (eventMap) {
        def endpointId = device.getDataValue("endpointId")
        log.debug "eventMap $eventMap | eventDescMap $eventDescMap"
        eventMap[displayed] = true
        if (eventDescMap?.sourceEndpoint == endpointId) {
            log.debug "parse - sendEvent parent $eventDescMap.sourceEndpoint"
            sendEvent(eventMap)
        } else {
            def childDevice = childDevices.find {
                it.deviceNetworkId == "$device.deviceNetworkId:${eventDescMap.sourceEndpoint}"
            }
            if (childDevice) {
                log.debug "parse - sendEvent child  $eventDescMap.sourceEndpoint"
                childDevice.sendEvent(eventMap)
            } else if (isAutoCreateChildDevice != false || getEndpointCount() == 0){
                def model = device.getDataValue("model")
                log.debug "Child device: $device.deviceNetworkId:${eventDescMap?.sourceEndpoint} was not found"
                def parentEndpointInt = zigbee.convertHexToInt(endpointId)
                if (eventDescMap?.sourceEndpoint != null) {
                    def childEndpointInt = zigbee.convertHexToInt(eventDescMap?.sourceEndpoint)
                    def childEndpointHexString = zigbee.convertToHexString(childEndpointInt, 2).toUpperCase()
                    def deviceLabel = "${device.displayName[0..-2]}"
                    def deviceIndex = Math.abs(childEndpointInt - parentEndpointInt) + 1

                    def childByEndpointId = childDevices.find {
                        it.deviceNetworkId.endsWith(":${eventDescMap.sourceEndpoint}")
                    }

                    if (childByEndpointId) {
                        log.debug "FOUND CHILD!!!!! Change dni to $device.deviceNetworkId:$childEndpointHexString"
                        childByEndpointId.setDeviceNetworkId("$device.deviceNetworkId:$childEndpointHexString")               
                    } else {
                        log.debug "NOT FOUND CHILD!!!!! Create to $deviceLabel$deviceIndex"
                        createChildDevice("$deviceLabel$deviceIndex", childEndpointHexString)
                    }
                }
            }

            if (isCreateAllControllerSwitch) {
                def allControlChildDevice = childDevices.find {
                    it.deviceNetworkId == "$device.deviceNetworkId:ALL"
                }
                if (!allControlChildDevice) {
                    def deviceLabel = "${device.displayName[0..-2]}"
                    createChildDevice("${deviceLabel}ALL", "ALL")
                }
            }
        }

        checkAllSwtichValue()
    }
}

private checkAllSwtichValue() {
    def parentSwitchValue = device.currentState("switch").value
    log.debug("checkAllSwtichValue ${device.label} : ${parentSwitchValue}")

    def allChildDeviceValue = parentSwitchValue

    def allChildDevice = null
    childDevices?.each {
        if (it.deviceNetworkId == "$device.deviceNetworkId:ALL") {
            allChildDevice = it
        } else {
            if (it.currentState("switch")?.value == "on") {
                allChildDeviceValue = "on"
            }
        }
    }

    if (allChildDevice?.currentState("switch") != allChildDeviceValue) {
        allChildDevice?.sendEvent(name: "switch", value : allChildDeviceValue)
    }
}

private getEndpointCount() {
    def model = device.getDataValue("model")
    def count = MODEL_MAP[model] ?: 0

    log.debug("getEndpointCount[$model] : $count")
    return count
}

private void createChildDevices() {
    log.debug("=============createChildDevices of $device.deviceNetworkId")
    if (!state.isCreateChildDone || isAutoCreateChildDevice != false) {
        def endpointCount = getEndpointCount()
        def endpointId = device.getDataValue("endpointId")
        def endpointInt = zigbee.convertHexToInt(endpointId)
        def deviceLabel = "${device.displayName[0..-2]}"

        for (i in 1..endpointCount - 1) {
            def endpointHexString = zigbee.convertToHexString(endpointInt + i, 2).toUpperCase()
            createChildDevice("$deviceLabel${i + 1}", endpointHexString)
        }

        def model = device.getDataValue("model")
        if ( model == 'TS0115') {
            createChildDevice("${deviceLabel}USB", "07")
        }
        state.isCreateChildDone = true
    }
}

private void createChildDevice(String deviceLabel, String endpointHexString) {
    def childDevice = childDevices.find {
        it.deviceNetworkId == "$device.deviceNetworkId:$endpointHexString"
    }
    if (!childDevice) {
        log.debug("===========Need to createChildDevice: $device.deviceNetworkId:$endpointHexString")
        addChildDevice("smartthings", "Child Switch", "$device.deviceNetworkId:$endpointHexString", device.hubId,
                       [completedSetup: true, label: deviceLabel, isComponent: false])
    } else {
        log.debug("createChildDevice: SKIP - $device.deviceNetworkId:${endpointHexString}")
    }
}

private getChildEndpoint(String dni) {
    dni.split(":")[-1] as String
}

private allChildOn() {
    log.debug "Executing 'on all' for 0x${device.deviceNetworkId}"

    childDevices.each {
        if (it.deviceNetworkId == "$device.deviceNetworkId:ALL") {
            it.sendEvent(name: "switch", value: "on") 
        } else {
            if (it.currentState("switch").value != "on") {
                it.on()
            }
        }
    }
}

private allChildOff() {
    log.debug "Executing 'off all' for 0x${device.deviceNetworkId}"
    childDevices.each {
        if (it.deviceNetworkId == "$device.deviceNetworkId:ALL") {
            it.sendEvent(name: "switch", value: "off") 
        } else {
            if (it.currentState("switch").value != "off") {
                it.off()
            }
        }
    }
}

def on() {
    log.debug("on")
    zigbee.on()
}

def off() {
    log.debug("off")
    zigbee.off()
}

def childOn(String dni) {
    log.debug("child on ${dni}")
    def childEndpoint = getChildEndpoint(dni)
    if(childEndpoint == "ALL") {
        allChildOn()
        if (device.currentState("switch") != "on") {
            zigbee.on()
        }
    } else {
        def endpointInt = zigbee.convertHexToInt(childEndpoint)
        zigbee.command(zigbee.ONOFF_CLUSTER, 0x01, "", [destEndpoint: endpointInt])
    }
}

def childOff(String dni) {
    log.debug("child off ${dni}")
    def childEndpoint = getChildEndpoint(dni)
    if(childEndpoint == "ALL") {
        allChildOff()
        if (device.currentState("switch") != "off") {
            zigbee.off()
        }
    }else {
        def endpointInt = zigbee.convertHexToInt(childEndpoint)
        zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", [destEndpoint: endpointInt])
    }
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def refresh() {
    def cmds = zigbee.onOffRefresh()
    def endpointCount = getEndpointCount()

    if (endpointCount > 1) {
        childDevices.each {
            log.debug "$it"
            def childEndpoint = getChildEndpoint(it.deviceNetworkId)
            if (childEndpoint.isNumber()) {
                log.debug "refresh $childEndpoint"
                def endpointInt = zigbee.convertHexToInt(childEndpoint)
                cmds += zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: endpointInt])
            }
        }
    } else {
        cmds += zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: 0xFF])
    }

    return cmds
}

def poll() {
    refresh()
}

def healthPoll() {
    log.debug "healthPoll()"
    def cmds = refresh()
    cmds.each { sendHubCommand(new physicalgraph.device.HubAction(it)) }
}

def configureHealthCheck() {
    Integer hcIntervalMinutes = 12
    if (!state.hasConfiguredHealthCheck) {
        log.debug "Configuring Health Check, Reporting"
        unschedule("healthPoll")
        runEvery5Minutes("healthPoll")
        def healthEvent = [name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
        // Device-Watch allows 2 check-in misses from device
        sendEvent(healthEvent)
        childDevices.each {
            def childEndpoint = getChildEndpoint(it.deviceNetworkId)
            if (childEndpoint.isNumber()) {
                it.sendEvent(healthEvent)
            } else if (childEndpoint == "ALL") {
                it.sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
            }
        }
        state.hasConfiguredHealthCheck = true
    }
}

def configure() {
    log.debug "configure()"
    configureHealthCheck()

    //other devices supported by this DTH in the future
    def cmds = zigbee.onOffConfig(0, 120)
    def endpointCount = getEndpointCount()

    if (endpointCount > 1) {
        childDevices.each {
            def childEndpoint = getChildEndpoint(it.deviceNetworkId)
            if (childEndpoint.isNumber()) {
                log.debug "configure(): $childEndpoint"
                def endpointInt = zigbee.convertHexToInt(childEndpoint)
                cmds += zigbee.configureReporting(zigbee.ONOFF_CLUSTER, 0x0000, 0x10, 0, 120, null, [destEndpoint: endpointInt])
            }
        }
    } else {
        cmds += zigbee.configureReporting(zigbee.ONOFF_CLUSTER, 0x0000, 0x10, 0, 120, null, [destEndpoint: 0xFF])
    }
    cmds += refresh()
    return cmds
}