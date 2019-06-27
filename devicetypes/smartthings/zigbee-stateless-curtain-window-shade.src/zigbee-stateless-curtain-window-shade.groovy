/**
 *
 *	Copyright 2019 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 */
import physicalgraph.zigbee.zcl.DataType
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
metadata {
    definition(name: "Zigbee Stateless Curtain Window Shade", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.blind", mnmn: "SmartThings", vid: "generic-curtaion") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"
        capability "Switch Level"
        capability "Stateless Curtain Power Button"

        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0102", outClusters: "000A", manufacturer: "Feibit Co.Ltd", model: "FTB56-ZT218AK1.6", deviceJoinName: "Wistar Curtain Motor(CMJ)" // SY-IoT201-BD
    }
}

private getCLUSTER_WINDOW_COVERING() { 0x0102 }
private getATTRIBUTE_CURRENT_LEVEL() { 0x0000 }


// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description:- ${description}"

    def resultMap = zigbee.getEvent(description)
    log.debug "resultMap:- ${resultMap}"
    if(resultMap){
        if(resultMap.name == "level"){
            sendEvent([name:resultMap.name, value:(100 - resultMap.value)])
        }
    }else{
        Map descMap = zigbee.parseDescriptionAsMap(description)
        log.debug "descMap:- ${descMap}"
        if (descMap?.clusterInt == zigbee.LEVEL_CONTROL_CLUSTER && descMap.value) {
            def valueInt = Math.round((zigbee.convertHexToInt(descMap.value)) / 255 * 100)
            sendEvent(name: "level", value: (100 - valueInt))
        }
    }
}

def close() {
    log.info "close()"
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x01)
}

def open() {
    log.info "open()"
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x00)
}

def setLevel(data, rate=null) {
    log.info "setLevel() enter ${data} ${rate}"
    if(data == null){
        data = 0
    }
    data = Math.round((100 - data) * 255 / 100)
    if(rate == null){
        zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, ATTRIBUTE_CURRENT_LEVEL, zigbee.convertToHexString(data, 2))
    }else{
		rate = (rate > 100) ? 100 : rate
		rate = convertToHexString(Math.round((100 - rate) * 255 / 100))
		command(zigbee.LEVEL_CONTROL_CLUSTER, 0x04, rate)
    }
}

def pause() {
    log.info "pause()"
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x02)
}

def setButton(value){
    log.info "setButton ${value}"
    if(value == "pause"){
        pause()
    }else if(value == "open"){
        open()
    }else if(value == "close"){
        close()
    }
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def refresh() {
    log.info "refresh()"
    return zigbee.readAttribute(zigbee.LEVEL_CONTROL_CLUSTER, ATTRIBUTE_CURRENT_LEVEL)
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
    log.info "configure()"
    sendEvent(name: "checkInterval", value: 10 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "availableCurtainPowerButtons", value: ["open", "pause", "close"])
    return zigbee.configureReporting(zigbee.LEVEL_CONTROL_CLUSTER, ATTRIBUTE_CURRENT_LEVEL, DataType.BITMAP16, 30, 60 * 30, null) + refresh()
}