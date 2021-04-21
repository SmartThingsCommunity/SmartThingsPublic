/*
 *  Copyright 2021 SmartThings
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
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus 
import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition (name: "SiHAS Zigbee Remote Control", namespace: "shinasys", author: "SHINA SYSTEM",  runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: true, mcdSync: true) {
        capability "Battery"
	    capability "Button"
        capability "Holdable Button"
        capability "Configuration"
        capability "Sensor"        
        capability "Health Check"
        
        fingerprint inClusters: "0000,0001,0003,0020", outClusters: "0003,0004,0006,0019", manufacturer: "ShinaSystem", model: "MSM-300Z", deviceJoinName: "SiHAS Remote Control", ocfDeviceType: "x.com.st.d.remotecontroller", mnmn: "SmartThings", vid: "generic-4-button"
		fingerprint inClusters: "0000,0001,0003,0020,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "BSM-300Z", deviceJoinName: "SiHAS Button", ocfDeviceType: "x.com.st.d.remotecontroller", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Button"
    }
}

private getPOWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE() { 0x0020 }
private getCLUSTER_GROUPS() { 0x0004 }

private List<Map> collectAttributes(Map descMap) {
    List<Map> descMaps = new ArrayList<Map>()
    descMaps.add(descMap)
    if (descMap.additionalAttrs) {
        descMaps.addAll(descMap.additionalAttrs)
    }
    return  descMaps
}

def parse(String description) {
	log.debug "Parsing message from device: $description"
    
    Map map = zigbee.getEvent(description)
    if (map) {
       sendEvent(map)
       //log.debug "sendEvent $event"
    } else {
        if (description?.startsWith('zone status')) {
            map = parseIasMessage(description)
        } else if ((description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))) {
            Map descMap = zigbee.parseDescriptionAsMap(description)
            if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
                List<Map> descMaps = collectAttributes(descMap)
                def battMap = descMaps.find { it.attrInt == POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE }
                if (battMap) {
                    map = getBatteryResult(Integer.parseInt(battMap.value, 16))
                }
            } else if (	descMap?.clusterInt == zigbee.ONOFF_CLUSTER ) {
				map = parseButtonMessage(descMap)
			}
        }
        
        def result = []
        if (map) {
            //log.debug "Creating event: ${map}"
            result = createEvent(map)
        } else if (isBindingTableMessage(description)) {
            Integer groupAddr = getGroupAddrFromBindingTable(description)
            if (groupAddr != null) {
                List cmds = addHubToGroup(groupAddr)
                result = cmds?.collect { new physicalgraph.device.HubAction(it) }
            } else {
                groupAddr = 0x0000
                List cmds = addHubToGroup(groupAddr) +
                        zigbee.command(CLUSTER_GROUPS, 0x00, "${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr, 4))} 00")
                result = cmds?.collect { new physicalgraph.device.HubAction(it) }
            }
        }
        return result
    }
}

private Map getBatteryResult(rawValue) {
    def linkText = getLinkText(device)
    def result = [:]
    def volts = rawValue / 10
    if (!(rawValue == 0 || rawValue == 255)) {
        result.name = 'battery'
        result.translatable = true
        def minVolts =  2.3
        def maxVolts =  3.1
        // Get the current battery percentage as a multiplier 0 - 1
        def curValVolts = Integer.parseInt(device.currentState("battery")?.value ?: "100") / 100.0
        // Find the corresponding voltage from our range
        curValVolts = curValVolts * (maxVolts - minVolts) + minVolts
        // Round to the nearest 10th of a volt
        curValVolts = Math.round(10 * curValVolts) / 10.0
        // Only update the battery reading if we don't have a last reading,
        // OR we have received the same reading twice in a row
        // OR we don't currently have a battery reading
        // OR the value we just received is at least 2 steps off from the last reported value
        if (state?.lastVolts == null || state?.lastVolts == volts || device.currentState("battery")?.value == null || Math.abs(curValVolts - volts) > 0.1) {
            def pct = (volts - minVolts) / (maxVolts - minVolts)
            def roundedPct = Math.round(pct * 100)
            if (roundedPct <= 0)
                roundedPct = 1
            result.value = Math.min(100, roundedPct)
        } else {
            // Don't update as we want to smooth the battery values, but do report the last battery state for record keeping purposes
            result.value = device.currentState("battery").value
        }
        result.descriptionText = "${device.displayName} battery was ${result.value}%"
        state.lastVolts = volts
    }
    return result
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
    if (zs.isAlarm1Set() && zs.isAlarm2Set()) {
       	return getZoneButtonResult('held')
    } else if (zs.isAlarm1Set()) {
        return getZoneButtonResult('pushed')
    } else if (zs.isAlarm2Set()) {
        return getZoneButtonResult('double')
    } else { 
    }    
}

private Map getZoneButtonResult(value) {
    def descriptionText
    if (value == "pushed")
        descriptionText = "${ device.displayName } was pushed"
    else if (value == "held")
        descriptionText = "${ device.displayName } was held"
    else
        descriptionText = "${ device.displayName } was pushed twice"
    return [
            name           : 'button',
            value          : value,
            descriptionText: descriptionText,
            translatable   : true,
            isStateChange  : true,
            data           : [buttonNumber: 1]
    ]
}

private channelNumber(String dni) 
{
   dni.split(":")[-1] as Integer
}

private sendButtonEvent(buttonNumber, buttonState) {
	def child = childDevices?.find { channelNumber(it.deviceNetworkId) == buttonNumber }
	if (child) {
		def descriptionText = "$child.displayName was $buttonState"
        child?.sendEvent([name: "button", value: buttonState, data: [buttonNumber: 1], descriptionText: descriptionText, isStateChange: true, displayed: true])        
	} else {
    	log.debug "Child device $buttonNumber not found!"
    }
}

private Map parseButtonMessage(Map descMap)
{
    def buttonState = ""
    def buttonNumber = descMap.sourceEndpoint.toInteger()
    Map result = [:]
      
    if (buttonNumber != 0) {
      	switch (descMap.commandInt) {
           	case 0:
               	buttonState = "pushed"
               	break
           	case 1:
               	buttonState = "double"
                break
           	case 2:
               	buttonState = "held"
               	break
       	}
        def descriptionText = "${getButtonName(buttonNumber)} was $buttonState"
        result = [name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true, displayed: false]
         
        sendButtonEvent(buttonNumber, buttonState)        	
    }
    result
}

private getButtonLabel(buttonNum) {
	def label = "Button ${buttonNum}"
	return label
}

private getButtonName(buttonNum) {
   return "${device.displayName} " + getButtonLabel(buttonNum)
}

private void createChildButtonDevices(numberOfButtons) {
	state.oldLabel = device.label

	for (i in 1..numberOfButtons) {
		def child = addChildDevice("smartthings", "Child Button", "${device.deviceNetworkId}:${i}", device.hubId,
		                            [completedSetup: true, label: getButtonName(i), 
		                             isComponent: true, componentName: "button$i", componentLabel: getButtonLabel(i)])
		                             
        child.sendEvent(name: "supportedButtonValues", value: ["pushed","held","double"].encodeAsJSON(), displayed: false)
        child.sendEvent(name: "numberOfButtons", value: 1, displayed: false)
        child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
	}
}

def installed() {
    def numberOfButtons
    
    if (isBSM300()) {
        numberOfButtons = 1
    } else if (isMSM300()) {
        numberOfButtons = 4
    } else return
    
    if (numberOfButtons > 1) {
		createChildButtonDevices(numberOfButtons)
	} 
    
    sendEvent(name: "supportedButtonValues", value: ["pushed","held","double"].encodeAsJSON(), displayed: false)
    sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
    
    if (numberOfButtons == 1) {
    	sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
    } else {    
    	numberOfButtons.times {
        	sendEvent(name: "button", value: "pushed", data: [buttonNumber: it+1], displayed: false)
        }
    }
    // These devices don't report regularly so they should only go OFFLINE when Hub is OFFLINE
    sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
}

def updated() {
	if (childDevices && device.label != state.oldLabel) {
		childDevices.each {
			def newLabel = getButtonName(channelNumber(it.deviceNetworkId))
            it.setLabel(newLabel)
            log.debug "new label = $newLabel"
		}
		state.oldLabel = device.label
	}
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE)
}

def refresh() {
    def refreshCmds = []
    updated()
    refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE)
    if( isBSM300() ) {
    	refreshCmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)        
    }
    return refreshCmds
}

def configure() {
    def configCmds = []    

    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    //sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    //sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
    
    // battery minReport 30 seconds, maxReportTime 6 hrs by default
    configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE, DataType.UINT8, 30, 21600, 0x01/*100mv*1*/)
    //configCmds += zigbee.enrollResponse() 
    
    if (isMSM300()) {    	
    	configCmds += zigbee.addBinding(zigbee.ONOFF_CLUSTER, ["destEndpoint":0x01])
    	configCmds += zigbee.addBinding(zigbee.ONOFF_CLUSTER, ["destEndpoint":0x02])
		configCmds += zigbee.addBinding(zigbee.ONOFF_CLUSTER, ["destEndpoint":0x03])
		configCmds += zigbee.addBinding(zigbee.ONOFF_CLUSTER, ["destEndpoint":0x04])
    }
    else if (isBSM300()) {
    	configCmds += zigbee.addBinding(zigbee.IAS_ZONE_CLUSTER, ["destEndpoint":0x01])
    }
    
    configCmds += readDeviceBindingTable() // Need to read the binding table to see what group it's using
    
    return refresh() + configCmds
}

private Integer getGroupAddrFromBindingTable(description) {
	log.info "Parsing binding table - '$description'"
	def btr = zigbee.parseBindingTableResponse(description)
	def groupEntry = btr?.table_entries?.find { it.dstAddrMode == 1 }
	if (groupEntry != null) {
		log.info "Found group binding in the binding table: ${groupEntry}"
		Integer.parseInt(groupEntry.dstAddr, 16)
	} else {
		log.info "The binding table does not contain a group binding"
		null
	}
}

private List addHubToGroup(Integer groupAddr) {
	["st cmd 0x0000 0x01 ${CLUSTER_GROUPS} 0x00 {${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr,4))} 00}", "delay 200"]
}

private List readDeviceBindingTable() {
	["zdo mgmt-bind 0x${device.deviceNetworkId} 0", "delay 200"]
}

private Boolean isBSM300() {
    device.getDataValue("model") == "BSM-300Z"
}

private Boolean isMSM300() {
    device.getDataValue("model") == "MSM-300Z"
}