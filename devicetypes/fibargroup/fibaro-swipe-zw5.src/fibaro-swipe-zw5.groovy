/**
 *  Fibaro Swipe ZW5
 *
 *  Copyright 2016 Fibar Group S.A.
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
	definition (name: "Fibaro Swipe ZW5", namespace: "fibargroup", author: "Fibar Group S.A.") {
    	capability "Actuator"
		capability "Battery"
        capability "Button"
		capability "Configuration"
		capability "Sensor"

        fingerprint deviceId: "0x1801", inClusters: "0x5E, 0x85, 0x59, 0x80, 0x5B, 0x70, 0x56, 0x5A, 0x7A, 0x72, 0x8E, 0x73, 0x98, 0x86, 0x84", outClusters: ""
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
		}
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "battery", label:'${currentValue}% battery', unit:"%"
        }
		main "button"
		details(["button", "battery"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

	if (description.startsWith("Err 106")) {
		if (state.sec) {
			result = createEvent(descriptionText:description, displayed:false)
		} else {
			result = createEvent(
				descriptionText: "FGGC failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
			)
		}
	} else if (description == "updated") {
		return null
	} else {
    	def cmd = zwave.parse(description, [0x5A: 1, 0x5B: 1, 0x72: 2, 0x80: 1, 0x84: 2, 0x86: 1])
    
    	if (cmd) {
    		log.debug "Parsed '${cmd}'"
        	zwaveEvent(cmd)
    	}
    }

}

//security
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x5A: 1, 0x5B: 1, 0x84: 2])
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

//crc16
def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd)
{
    def versions = [0x72: 2, 0x80: 1, 0x86: 1]
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		log.debug "Could not extract command from $cmd"
	} else {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {    
	if (!state.seqNumber || state.seqNumber != cmd.sequenceNumber) {
    	def sceneNumberMapping = [1: "Up", 2: "Down", 3: "Left", 4: "Right"]
    	def keyAttributesMapping = [0: "single", 3: "double"]

    	log.debug "Central Scene Notification received: sequenceNumber: ${cmd.sequenceNumber}, sceneNumber: ${cmd.sceneNumber}, keyAttributes: ${cmd.keyAttributes}"

		state.seqNumber = cmd.sequenceNumber	

		return gestureEvent(sceneNumberMapping.get(cmd.sceneNumber.intValue()), keyAttributesMapping.get(cmd.keyAttributes.intValue()))
    } else {
    	log.debug "Same Central Scene Notification received multiple times: sequenceNumber: ${cmd.sequenceNumber}, sceneNumber: ${cmd.sceneNumber}, keyAttributes: ${cmd.keyAttributes}"
    }
    
    state.seqNumber = cmd.sequenceNumber	
}

def gestureEvent(gesture, keyAttribute) {
    return createEvent(name: "button", value: "${gesture}", data: [flicks: "${keyAttribute}"], descriptionText: "${device.displayName} gesture ${gesture}(${keyAttribute})", isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) { 
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) { 
	log.debug "deviceIdData:                ${cmd.deviceIdData}"
    log.debug "deviceIdDataFormat:          ${cmd.deviceIdDataFormat}"
    log.debug "deviceIdDataLengthIndicator: ${cmd.deviceIdDataLengthIndicator}"
    log.debug "deviceIdType:                ${cmd.deviceIdType}"
    
    if (cmd.deviceIdType == 1 && cmd.deviceIdDataFormat == 1) {//serial number in binary format
		String serialNumber = "h'"
        
        cmd.deviceIdData.each{ data ->
        	serialNumber += "${String.format("%02X", data)}"
        }
        
        updateDataValue("serialNumber", serialNumber)
        log.debug "${device.displayName} - serial number: ${serialNumber}"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
    updateDataValue("version", "${cmd.applicationVersion}.${cmd.applicationSubVersion}")
    log.debug "applicationVersion:      ${cmd.applicationVersion}"
    log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	def event = createEvent(descriptionText: "${device.displayName} woke up", displayed: false)
    def cmds = []
    if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
        log.debug("Device has been configured sending >> batteryGet()")
        cmds << encap(zwave.batteryV1.batteryGet())
        cmds << "delay 1200"
    }
     
    cmds << encap(zwave.wakeUpV1.wakeUpNoMoreInformation())
    [event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel == 255 ? 1 : cmd.batteryLevel.toString()
	map.unit = "%"
	map.displayed = true
    //Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
    state.lastbatt = now()
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
	log.info "${device.displayName}: received command: $cmd - device has reset itself"
}

// handle commands
def configure() {
	log.debug "Executing 'configure'"
	
    def cmds = []

    cmds += zwave.manufacturerSpecificV1.manufacturerSpecificGet()
    cmds += zwave.manufacturerSpecificV2.deviceSpecificGet()
    cmds += zwave.versionV1.versionGet()
    //set parameter 12 to 0 to enable double flicks for gestures 1-4
    cmds += zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1, scaledConfigurationValue: 0)
    cmds += zwave.wakeUpV2.wakeUpIntervalSet(seconds:21600, nodeid:zwaveHubNodeId)//FGGC's default wake up interval
    cmds += zwave.batteryV1.batteryGet()
    cmds += zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId])
        
    cmds += zwave.wakeUpV2.wakeUpNoMoreInformation()
    
    encapSequence(cmds, 500)
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crc16(physicalgraph.zwave.Command cmd) {
	//zwave.crc16encapV1.crc16Encap().encapsulate(cmd).format()
    "5601${cmd.format()}0000"
}

private encapSequence(commands, delay=200) {
	delayBetween(commands.collect{ encap(it) }, delay)
}

private encap(physicalgraph.zwave.Command cmd) {
	def secureClasses = [0x5A, 0x5B, 0x70, 0x84, 0x85, 0x8E]

	//todo: check if secure inclusion was successful
    //if not do not send security-encapsulated command
	if (secureClasses.find{ it == cmd.commandClassId }) {
    	secure(cmd)
    } else {
    	crc16(cmd)
    }
}