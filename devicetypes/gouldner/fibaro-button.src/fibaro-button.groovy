/**
 *  Copyright 2017 Ronald Gouldner
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
 *  Based on Version 0.9.1 Author: AdamV but largely overhauled
 *
 *  Version: 1.2 
 *  Author: Ronald Gouldner
 *      Features
 *              - Has Correct fingerprint so should be selected as the device handler for your 
 *                Fibaro Button if added to your ide before you include the button
 *              - Implemented Battery Status (Note activates on Wake and seems to get sent on occasion
 *                button is pressed.  4 press manual wake should update the button in a few clicks)
 *              - Reports last action (Pressed, Held (while being held) and Released (following a hold)
 *              - Reports which button was last pressed
 *              - Reports Firmware Version and Device Handler Version in tiles  NOTE: To populate
 *                the Device version you must do the following.
 *                    - press button 4 time (wakes the device)
 *                    - immediately tap "Configure" tile
 *               - Reports ManufacturerSpecificReport, DeviceSpecificReport, VersionReport, BatteryReport to LOGS
 *                 when you configure with 4 button press and configure tile press
 *
 *  Finger Print info
 *  zw:Ss type:1801 mfr:0072 prod:0501 model:0F0F cc:5E,59,73,80,56,7A,98 sec:5B,85,84,5A,86,72,71,70,8E,9C secOut:26
 *
 */
 
metadata {
    definition (name: "Fibaro Button", namespace: "gouldner", author: "Ronald Gouldner") {
        capability "Actuator"
        capability "Battery"
        capability "Button"
        capability "Configuration"
        capability "Holdable Button" 
        capability "Refresh" 
        
        command "describeAttributes"
        
        attribute "numberOfButtons", "number"
        attribute "buttonClicks", "enum", ["one click", "two clicks", "three clicks", "four clicks", "five clicks", "hold start", "hold release"]
        attribute "holdLevel", "number"

        // http://docs.smartthings.com/en/latest/device-type-developers-guide/definition-metadata.html#fingerprinting
        // fingerprint mfg:0072, prod:0501, model:0F0F NOTE: This finger print works with one of my buttons but not the other
        // for some reason Fibar Group has used two different mfg codes and two different model numbers for units that look largly the same
        // one was a blue button one was brown not sure if model reflects color but mfg changed also
        // so changing the finger print to an accurate mapping of supported command classes
        /*
        *  Finger Print info, example raw description note I have two buttons and mfr and model are different for each one
        *  zw:Ss type:1801 mfr:0072 prod:0501 model:0F0F cc:5E,59,73,80,56,7A,98 sec:5B,85,84,5A,86,72,71,70,8E,9C secOut:26 
        *
        */
        //fingerprint deviceId: "0x1801", inClusters: "0x5E, 0x59, 0x73, 0x80, 0x56, 0x7A, 0x98", outClusters: "0x26"
        fingerprint deviceId: "0x03", inClusters: "0x00, 0x5B, 0x03, 0x06, 0x83, 0x01", outClusters: "0x26"
    }

    simulator {
        // need to implement simulator commands I built using a physical device to test
    }

    tiles (scale: 2) {      
        multiAttributeTile(name:"buttonClicks", type:"generic", width:6, height:4) {
            tileAttribute("device.buttonClicks", key: "PRIMARY_CONTROL"){
                attributeState "default", label:'Fibaro Button', backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "hold start", label: "Held", backgroundColor: "#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "hold release", label: "Released", backgroundColor: "#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "one click", label: "Button 1", backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "two clicks", label: "Button 2", backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "three clicks", label: "Button 3", backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "four clicks", label: "Button 4", backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
                attributeState "five clicks", label: "Button 5", backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
            }
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
                attributeState "battery", label:'Battery: ${currentValue}%', unit:"%"
            } 
        }
        standardTile("button", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", label:'Fibaro', backgroundColor:"#44b621", icon:"st.unknown.zwave.remote-controller"
            state "held", label: "Held", backgroundColor: "#44b621", icon:"st.unknown.zwave.remote-controller"
            state "released", label: "Released", backgroundColor: "#44b621", icon:"st.unknown.zwave.remote-controller"
            state "pushed", label: "Pushed", backgroundColor: "#44b621", icon:"st.unknown.zwave.remote-controller"
        }
        
        valueTile("configure", "device.button", width: 2, height: 2, decoration: "flat") {
            state "default", backgroundColor: "#ffffff", action: "configure", icon:"st.secondary.configure"
        }
        
        standardTile("dhversion", "device.dhversion", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
            state "dhversion", label: 'Device Handler v1.2'
        }
        
        standardTile("version", "device.version", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
            state "version", label: 'Fibaro Button Ver:${currentValue}'
        }
        
        valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:"%"
		}
        
        valueTile("numberOfButtons", "device.numberOfButtons", decoration: "flat", width: 2, height: 2){
			state "numberOfButtons", label:'${currentValue}', unit:""
		}
        
        main "buttonClicks"
        details(["buttonClicks","button","battery", "configure","version","dhversion","numberOfButtons"])
    }
}

def parse(String description) {
    log.debug ("Parsing description:$description")
    def event
    def results = []

    def numberOfButtonsVal = device.currentValue("numberOfButtons")
    if ( !state.numberOfButtons || !numberOfButtonsVal) {
        log.debug ("setting number of buttons currently not set numberOfButtons:${state.numberOfButtons} numberOfButtons:${numberOfButtonsVal}")
        state.numberOfButtons = "5"
        event = createEvent(name: "numberOfButtons", value: "5", displayed: false)
        if (event) {
            results += event
        }
    }
    
    //log.debug("RAW command: $description")
    if (description.startsWith("Err")) {
        log.debug("An error has occurred")
    } else { 
        def cmd = zwave.parse(description)
        log.debug "Parsed Command: $cmd"
        if (cmd) {
            event = zwaveEvent(cmd)
            if (event) {
                results += event
            }
        }
    }
    return results
}

def describeAttributes(payload) {
    payload.attributes = [
        [ name: "holdLevel", type: "number", range:"1..100", capability: "button" ],
        [ name: "buttonClicks", type: "enum",  options: ["one click", "two clicks", "three clicks", "four clicks", "five clicks", "hold start", "hold release"], momentary: true, capability: "button" ],
    ]
    return null
}

// Devices that support the Security command class can send messages in an
// encrypted form; they arrive wrapped in a SecurityMessageEncapsulation
// command and must be unencapsulated
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
        log.debug ("SecurityMessageEncapsulation cmd:$cmd")
        def encapsulatedCommand = cmd.encapsulatedCommand([0x98: 1, 0x20: 1])

        // can specify command class versions here like in zwave.parse
        if (encapsulatedCommand) {
            log.debug ("SecurityMessageEncapsulation encapsulatedCommand:$encapsulatedCommand")
            return zwaveEvent(encapsulatedCommand)
        }
        log.debug ("No encalsulatedCommand Processed")
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    log.debug("Button Woke Up!")
    def event = createEvent(descriptionText: "${device.displayName} woke up", displayed: false)
    def cmds = []
    // request battery
    // cmds += zwave.batteryV1.batteryGet()
    // let wakeup go back to sleep
    cmds += zwave.wakeUpV1.wakeUpNoMoreInformation()
    
    [event, encapSequence(cmds, 500)]
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.debug "VersionReport cmd:$cmd"
    def ver = cmd.applicationVersion + "." + cmd.applicationSubVersion
    sendEvent(name: "version", value: ver, descriptionText: "Fibaro Button Version $ver", isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    log.debug("Crc16Encap: $cmd")
    log.debug( "Data: $cmd.data")
    log.debug( "Payload: $cmd.payload")
    log.debug( "command: $cmd.command")
    log.debug( "commandclass: $cmd.commandClass")
    def versions = [0x31: 3, 0x30: 2, 0x84: 2, 0x9C: 1, 0x70: 2]
    // def encapsulatedCommand = cmd.encapsulatedCommand(versions)
    def version = versions[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    if (!encapsulatedCommand) {
        log.debug "Could not extract command from $cmd"
    } else {
        zwaveEvent(encapsulatedCommand)
        // log.debug("UnsecuredCommand: $encapsulatedCommand")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    log.debug( "CentralSceneNotification: $cmd")
    // log.debug( "keyAttributes: $cmd.keyAttributes")
    // log.debug( "sceneNumber: $cmd.sceneNumber")
    // log.debug( "sequenceNumber: $cmd.sequenceNumber")
    // log.debug( "payload: $cmd.payload")
    
    if ( cmd.keyAttributes == 0 ) {
        Integer button = 1
        sendEvent(name: "buttonClicks", value: "one click", descriptionText: "$device.displayName button was clicked once", isStateChange: true)
        sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        log.debug( "Button was pushed once" )
    }
    if ( cmd.keyAttributes == 3 ) {
        Integer button = 2
        sendEvent(name: "buttonClicks", value: "two clicks", descriptionText: "$device.displayName button was pushed twice", isStateChange: true)
        sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        log.debug( "Button was pushed twice" )
    }
    if ( cmd.keyAttributes == 4 ) {
        Integer button = 3
        sendEvent(name: "buttonClicks", value: "three clicks", descriptionText: "$device.displayName button was pushed three times", isStateChange: true)
        sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        log.debug( "Button was pushed three times" )
    }
    if ( cmd.keyAttributes == 5 ) {
        Integer button = 4
        sendEvent(name: "buttonClicks", value: "four clicks", descriptionText: "$device.displayName button was pushed four times", isStateChange: true)
        sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        log.debug( "Button was pushed four times" )
    }
    if ( cmd.keyAttributes == 6 ) {
        Integer button = 5
        sendEvent(name: "buttonClicks", value: "five clicks", descriptionText: "$device.displayName button was pushed FIVE times", isStateChange: true)
        sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        log.debug( "Button was pushed five times" )
    }
    if ( cmd.keyAttributes == 2 ) {
        // Fixed Errors by added data for button number and commented out button clicks and button events
        // 
        Integer button = 1
        sendEvent(name: "buttonClicks", value: "hold start", descriptionText: "$device.displayName button is released", isStateChange: true)
        sendEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true)
        log.debug( "Button held" )
    }
    if ( cmd.keyAttributes == 1 ) {
        sendEvent(name: "buttonClicks", value: "hold release", descriptionText: "$device.displayName button is released", isStateChange: true)
        sendEvent(name: "button", value: "released", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true)
        log.debug( "Button released" )
    } 
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    log.debug("BatteryReport: $cmd")
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	state.lastBatteryReport = new Date().time	    	
	def isNew = (device.currentValue("battery") != val)    
	def result = []
	result << createEvent(name: "battery", value: val, unit: "%", display: isNew, isStateChange: isNew)	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.debug("V2 ConfigurationReport cmd: $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug("V1 ConfigurationReport cmd: $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
    log.debug("DeviceSpecificReport cmd: $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.debug("ManufacturerSpecificReport cmd: $cmd")
}

def configure() {
    log.debug "Executing 'configure'"

    device.currentValue("battery")
    if (!device.currentValue("version")) {
        sendEvent(name: "version", value: 'unknown', descriptionText: "Fibaro Button Version $ver", isStateChange: true)
    }
    
    def cmds = []
    cmds += zwave.wakeUpV2.wakeUpIntervalSet(seconds:21600, nodeid: zwaveHubNodeId)
    // Not reporting back currently but leaving these in anyway.
    cmds += zwave.manufacturerSpecificV2.manufacturerSpecificGet()
    cmds += zwave.manufacturerSpecificV2.deviceSpecificGet()
    cmds += zwave.versionV1.versionGet()
    cmds += zwave.batteryV1.batteryGet()
    cmds += zwave.associationV2.associationSet(groupingIdentifier:1, nodeId: [zwaveHubNodeId])
    cmds += zwave.wakeUpV2.wakeUpNoMoreInformation()
    return encapSequence(cmds, 500)
}

private encapSequence(commands, delay=200) {
        delayBetween(commands.collect{ encap(it) }, delay)
}

private secure(physicalgraph.zwave.Command cmd) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crc16(physicalgraph.zwave.Command cmd) {
        //zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
    "5601${cmd.format()}0000"
}

private encap(physicalgraph.zwave.Command cmd) {
    def secureClasses = [0x5B, 0x85, 0x84, 0x5A, 0x86, 0x72, 0x71, 0x70 ,0x8E, 0x9C]
    //todo: check if secure inclusion was successful
    //if not do not send security-encapsulated command
    if (secureClasses.find{ it == cmd.commandClassId }) {
        secure(cmd)
    } else {
        crc16(cmd)
    }
}