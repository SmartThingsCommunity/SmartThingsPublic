/**
*  Device Type Definition File   
*
*  Device Type:        Ecolink Flood Freeze Sensor
*  File Name:          
 *  Initial Release:    2 / 13 / 2018
*  @author:            Ecolink
*  @version:           1.0
*
*  Copyright 2014 SmartThings
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
*  Manufacturer ID : 01 4A  : Ecolink
*  Product Type ID : 00 05  : Flood Freeze
*  Product ID      : 00 10  : 10
*
*  Raw Description : zw:S type:0701 mfr:014A prod:0005 model:0010 ver:10.09 zwv:4.38 lib:06 cc:5E,86,72,5A,73,80,30,71,85,59,84 role:06 ff:8C05 ui:8C05
*
* @param none
*
* @return none
*/

metadata {
                definition (name: "Ecolink Z-Wave Flood Freeze Sensor", namespace: "Ecolink", author: "Ecolink") 
    {
                                capability "Water Sensor"
                                capability "Temperature Measurement"
                                capability "Battery"
        capability "Tamper Alert"
        capability "Sensor"

                                //fingerprint deviceId: "0x0701", inClusters: "0x5E,0x86,0x72,0x73,0x80,0x71,0x85,0x59,0x84,0x30,0x70,0xEF,0x20", model: "0001", prod: "0004"
        fingerprint mfr: "014A", prod: "0005",  model: "0010"
                }
                
                simulator {
                                // messages the device returns in response to commands it receives
                                status "motion (basic)"     : "command: 2001, payload: FF"
                                status "no motion (basic)"  : "command: 2001, payload: 00"

                                for (int i = 0; i <= 100; i += 20) {
                                                status "temperature ${i}F": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                                                                scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1).incomingMessage()
                                }

                                for (int i = 200; i <= 1000; i += 200) {
                                                status "luminance ${i} lux": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                                                                scaledSensorValue: i, precision: 0, sensorType: 3).incomingMessage()
                                }

                                for (int i = 0; i <= 100; i += 20) {
                                                status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
                                                                batteryLevel: i).incomingMessage()
                                }
                }
    
                tiles(scale :2) 
    {
                                multiAttributeTile(name:"water", type: "generic", width: 6, height: 4)
        {
                                                tileAttribute("device.water", key: "PRIMARY_CONTROL") 
            {
                                                                attributeState("dry", label: 'Dry', icon:"st.alarm.water.dry", backgroundColor:"#FFFFFF")
                                                                attributeState("wet", label: 'Wet', icon:"st.alarm.water.wet", backgroundColor:"#00A0DC")
                                                }
                                }
                                standardTile("temperature", "device.temperature", width: 2, height: 2) 
        {
                                                state "22", label: 'Normal', icon:"st.Weather.weather14", backgroundColor:"#ffffff", defaultState: true
                                                state "72", label: 'Normal', icon:"st.Weather.weather14", backgroundColor:"#ffffff"
                                                state "0", label: 'Freezing', icon:"st.Weather.weather7", backgroundColor:"#00A0DC"
                                                state "32", label: 'Freezing', icon:"st.Weather.weather7", backgroundColor:"#00A0DC"
        }
                                standardTile("tamper", "device.tamper", width: 2, height: 2) 
        {
                                                state("secure", label:"secure", icon:"st.locks.lock.locked",   backgroundColor:"#FFFFFF")                                 // clear 
                                                state("tampered", label:"tampered", icon:"st.locks.lock.unlocked", backgroundColor:"#FF6633")      // red 
                                }
                                valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) 
        {
                                                state "battery", label:'${currentValue}% battery', unit:""
                                }

                                main "water"
                                details(["water", "tamper", "battery", "temperature"])
                }
}

def parse(String description) {
                def result = []
                if (description.startsWith("Err")) {
                    result = createEvent(descriptionText:description)
                } else {
                                def cmd = zwave.parse(description, [0x20: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1])
                                if (cmd) {
                                                result = zwaveEvent(cmd)
                                } else {
                                                result = createEvent(value: description, descriptionText: description, isStateChange: false)
                                }
                }
                return result
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
                def result = []
                if (cmd.notificationType == 0x05) {
                                if (cmd.event == 0x02) {
                        result << createEvent(name: "water", value: "wet", descriptionText: "$device.displayName detected water")
                                                }
                                else if (cmd.event == 0x04) {
                    result << createEvent(name: "water", value: "dry", descriptionText: "$device.displayName no longer detected water")
                    }
        else {
                log.warn "Unknown Water Notification report ${cmd.event}"
                }
                                }
                else if (cmd.notificationType == 0x07) {
        if (cmd.event == 0x03) {
            result << createEvent(name: "tamper", value: "tampered", descriptionText: "$device.displayName covering was removed", isStateChange: true)
            }
                                else if (cmd.event == 0x00) {
                                                result << createEvent(name: "tamper", value: "secure", descriptionText: "$device.displayName covering was closed", isStateChange: true)
            }
        else {
            log.warn "Unknown tamper notification: ${cmd.event}"
                }
        }
                else if (cmd.notificationType == 0x08) {
                                if (cmd.event == 0x0B) {
            log.warn "Critical battery, replace immediately"
            result << createEvent(name: "battery", unit: "%", value: 1, descriptionText: "${device.displayName} has a critical battery", isStateChange: true)
            }
                                else if (cmd.event == 0x0A) {
            log.warn "${device.displayName} has a low battery"
            }
        else {
            log.warn "Unknown battery notification: ${cmd.notificationType}"
                }
                }
                else {
        log.warn "Unknown Notification Type: ${cmd.notificationType}"
        }
                result
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd)
{
    log.trace "SensorBinaryReport: $cmd"

    def map = [:]
    if (cmd.sensorType == 0x06) {
                                // ignore, handled wtih notification command class
                }
                else if (cmd.sensorType == 0x07) {
        def eventValue
        if (cmd.sensorValue >= 0) {
            tempValue = getTemperatureScale() == "F" ? 32 : 0   	// freezing
                }
        else {
            tempValue = getTemperatureScale() == "F" ? 72 : 22 		// normal
                }                
        map = [name: "temperature", value: tempValue, descriptionText: "$device.displayName is ${cmd.sensorValue ? "freezing" : "normal"}"]
        }
                else {
        log.warn "Unknown Sensor report ${cmd.sensorType}"
                                }
        
    createEvent(map) // Return the map result
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
                def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
                if (!state.lastbat || (new Date().time) - state.lastbat > 53*60*60*1000) {
                                result << response(zwave.batteryV1.batteryGet())
                } else {
                                result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
                }
                result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
                def map = [ name: "battery", unit: "%" ]
                if (cmd.batteryLevel == 0xFF) {
                                map.value = 1
                                map.descriptionText = "${device.displayName} has a low battery"
                                map.isStateChange = true
                } else {
                                map.value = cmd.batteryLevel
                }
                state.lastbat = new Date().time
                [createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
                def result = []

                def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
                log.debug "msr: $msr"
                updateDataValue("MSR", msr)

                if (msr == "0086-0002-002D") {  //Set wakeup interval
                                result << response(zwave.wakeUpV1.wakeUpIntervalSet(seconds:4*3600, nodeid:zwaveHubNodeId))
                }
                result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
                result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
                log.warn "Unknown Z-Wave Command: $cmd"
}