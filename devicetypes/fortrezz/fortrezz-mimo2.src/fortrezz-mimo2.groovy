/**
 *  MIMO2 Device Handler
 *
 *  Copyright 2016 FortrezZ, LLC
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
	definition (name: "FortrezZ MIMO2+", namespace: "fortrezz", author: "FortrezZ, LLC") {
		capability "Alarm"
		capability "Contact Sensor"
		capability "Switch"
		capability "Voltage Measurement"
        capability "Configuration"
        capability "Refresh"
        
        attribute "powered", "string"
        attribute "relay", "string"
        
        attribute "relay2", "string"
        attribute "contact2", "string"
        attribute "voltage2", "string"
        
		command "on"
		command "off"
        command "on2"
        command "off2"
        
        fingerprint deviceId: "0x2100", inClusters: "0x5E,0x86,0x72,0x5A,0x59,0x71,0x98,0x7A"
	}
    
    preferences {

        input ("RelaySwitchDelay", "decimal", title: "Delay between relay switch on and off in seconds. Only Numbers 0 to 3 allowed. 0 value will remove delay and allow relay to function as a standard switch:\nRelay 1", description: "Numbers 0 to 3.1 allowed.", defaultValue: 0, required: false, displayDuringSetup: true)
        input ("RelaySwitchDelay2", "decimal", title: "Relay 2", description: "Numbers 0 to 3.1 allowed.", defaultValue: 0, required: false, displayDuringSetup: true)
        input ("Sig1AD", "bool", title: "Switch off for digital, on for analog:\nSIG1", required: false, displayDuringSetup: true)
        input ("Sig2AD", "bool", title: "SIG2", required: false, displayDuringSetup: true)
        } // the range would be 0 to 3.1, but the range value would not accept 3.1, only whole numbers (i tried paranthesis and fractions too. :( )

       
	tiles {
         standardTile("switch", "device.switch", width: 2, height: 2) {
            state "on", label: "Relay 1 On", action: "off", icon: "http://swiftlet.technology/wp-content/uploads/2016/06/Switch-On-104-edit.png", backgroundColor: "#53a7c0"            
			state "off", label: "Relay 1 Off", action: "on", icon: "http://swiftlet.technology/wp-content/uploads/2016/06/Switch-Off-104-edit.png", backgroundColor: "#ffffff"

        }
         standardTile("switch2", "device.switch2", width: 2, height: 2, inactiveLabel: false) {
            state "on", label: "Relay 2 On", action: "off2", icon: "http://swiftlet.technology/wp-content/uploads/2016/06/Switch-On-104-edit.png", backgroundColor: "#53a7c0"
			state "off", label: 'Relay 2 Off', action: "on2", icon: "http://swiftlet.technology/wp-content/uploads/2016/06/Switch-Off-104-edit.png", backgroundColor: "#ffffff"
        }
        standardTile("anaDig1", "device.anaDig1", inactiveLabel: false) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
            state "val", label:'${currentValue}v', unit:"", defaultState: true
		}
        standardTile("anaDig2", "device.anaDig2", inactiveLabel: false) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
            state "val", label:'${currentValue}v', unit:"", defaultState: true
		}
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("powered", "device.powered", inactiveLabel: false) {
			state "powerOn", label: "Power On", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "powerOff", label: "Power Off", icon: "st.switches.switch.off", backgroundColor: "#ffa81e"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        standardTile("blank", "device.blank", inactiveLabel: true, decoration: "flat") {
        	state("blank", label: '')
        }
		main (["switch"])
		details(["switch", "anaDig1", "blank", "switch2", "anaDig2", "blank", "configure", "refresh", "powered"])
	}
}

// parse events into attributes
def parse(String description) {
	def result = null
	def cmd = zwave.parse(description)
    
    if (cmd.CMD == "7105") {				//Mimo sent a power loss report
    	log.debug "Device lost power"
    	sendEvent(name: "powered", value: "powerOff", descriptionText: "$device.displayName lost power")
    } else {
    	sendEvent(name: "powered", value: "powerOn", descriptionText: "$device.displayName regained power")
    }
	if (cmd) {
    	def eventReturn = zwaveEvent(cmd)
        if(eventReturn in physicalgraph.device.HubMultiAction) {
        	result = eventReturn
        }
        else {
        	result = createEvent(eventReturn)
        }
	}
    log.debug "Parse returned ${result} $cmd.CMD"
	return result
}

def updated() { // neat built-in smartThings function which automatically runs whenever any setting inputs are changed in the preferences menu of the device handler
    
    if (state.count == 1) // this bit with state keeps the function from running twice ( which it always seems to want to do) (( oh, and state.count is a variable which is nonVolatile and doesn't change per every parse request.
    {
        state.count = 0
        log.debug "Settings Updated..."
        return response(delayBetween([
            configure(), // the response() function is used for sending commands in reponse to an event, without it, no zWave commands will work for contained function
            refresh()
            ], 200))
    }
    else {state.count = 1}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) // basic set is essentially our digital sensor for SIG1 and SIG2 - it doesn't use an endpoint so we are having it send a multilevelGet() for SIG1 and SIG2 to see which one triggered.
{
	log.debug "sent a BasicSet command"
	return response(refresh())
}

def zwaveEvent(int endPoint, physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) // event to get the state of the digital sensor SIG1 and SIG2
{
	log.debug "sent a sensorBinaryReport command"
	return response(refresh())
}

def zwaveEvent(int endPoint, physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) // event for seeing the states of relay 1 and relay 2
{
	def map = [:] // map for containing the name and state fo the specified relay
    if (endPoint == 3)
    {
    	if (cmd.value) // possible values are 255 and 0 (0 is false)
    		{map.value = "on"}
    	else
    		{map.value = "off"}
        map.name = "switch"
        log.debug "sent a SwitchBinary command $map.name $map.value" // the map is only used for debug messages. not for the return command to the device
        return [name: "switch", value: cmd.value ? "on" : "off"]
    }
    else if (endPoint == 4)
    {
    	if (cmd.value)
    		{map.value = "on"}
    	else
    		{map.value = "off"}
        map.name = "switch2"
        sendEvent(name: "relay2", value: "$map.value")
        log.debug "sent a SwitchBinary command $map.name $map.value" // the map is only used for debug messages. not for the return command to the device
        return [name: "switch2", value: cmd.value ? "on" : "off"]
    }
}
   
def zwaveEvent (int endPoint, physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) // sensorMultilevelReport is used to report the value of the analog voltage for SIG1
{
	def map = [:]
    def stdEvent = [:]
    def voltageVal = CalculateVoltage(cmd.scaledSensorValue) // saving the scaled Sensor Value used to enter into a large formula to determine actual voltage value
    if (endPoint == 1) //endPoint 1 is for SIG1
    {
    	if (state.AD1 == false) // state.AD1 is  to determine which state the anaDig1 tile should be in (either analogue or digital mode)
        {
        	map.name = "anaDig1"
            stdEvent.name = "contact"
            if (voltageVal < 2) { // DK changed to 2v to follow LED behavior
            	map.value = "closed"
                stdEvent.value = "closed"
            }
            else
            {
            	map.value = "open"
                stdEvent.value = "open"
            } 
        }
        else //or state.AD1 is true for analogue mode
        {
        	map.name = "anaDig1"
            stdEvent.name = "voltage"
        	map.value = voltageVal
            stdEvent.value = voltageVal
        	map.unit = "v"
            stdEvent.unit = "v"
        }
    }
    else if (endPoint == 2) // endpoint 2 is for SIG2
    {
        if (state.AD2 == false)
        {
        	map.name = "anaDig2"
            stdEvent.name = "contact2"
            if (voltageVal < 2) {
            	map.value = "closed"
                stdEvent.value = "closed"
            }
            else
            {
            	map.value = "open"
                stdEvent.value = "open"
            } 
        }
        else
        {
        	map.name = "anaDig2"
            stdEvent.name = "voltage2"
        	map.value = voltageVal
            stdEvent.value = voltageVal
        	map.unit = "v"
            stdEvent.unit = "v"
        }
    }
	log.debug "sent a SensorMultilevelReport $map.name $map.value"
    sendEvent(stdEvent)
    return map
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) { //standard security encapsulation event code (should be the same on all device handlers)
    def encapsulatedCommand = cmd.encapsulatedCommand()
    // can specify command class versions here like in zwave.parse
    if (encapsulatedCommand) {
        return zwaveEvent(encapsulatedCommand)
    }
}

// MultiChannelCmdEncap and MultiInstanceCmdEncap are ways that devices
// can indicate that a message is coming from one of multiple subdevices
// or "endpoints" that would otherwise be indistinguishable
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand()
    log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")

    if (encapsulatedCommand) {
        return zwaveEvent(cmd.sourceEndPoint, encapsulatedCommand)
    }
}

def zwaveEvent(int endPoint, physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	
    log.debug "sent an Association Report"
    log.debug " ${cmd.groupingIdentifier}"
    //return [:]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
     log.debug("Un-parsed Z-Wave message ${cmd}")
	return [:]
}

def CalculateVoltage(ADCvalue) // used to calculate the voltage based on the collected Scaled sensor value of the multilevel sensor event
{
    def volt = (((2.396*(10**-17))*(ADCvalue**5)) - ((1.817*(10**-13))*(ADCvalue**4)) + ((5.087*(10**-10))*(ADCvalue**3)) - ((5.868*(10**-7))*(ADCvalue**2)) + ((9.967*(10**-4))*(ADCvalue)) - (1.367*(10**-2)))
	return volt.round(1)
}
	

def configure() {
	log.debug "Configuring...." 
    def sig1
    def sig2
    if (Sig1AD == true)
    {	sig1 = 0x01
        state.AD1 = true}
    else if (Sig1AD == false) 
    {	sig1 = 0x40
    	state.AD1 = false}
    if (Sig2AD == true)
    {	sig2 = 0x01
    	state.AD2 = true}
    else if (Sig2AD == false) 
    {	sig2 = 0x40
    	state.AD2 = false}
    
	def delay = (RelaySwitchDelay*10).toInteger() // the input which we get from the user is a string and is in seconds while the MIMO2 configuration requires it in 100ms so - change to integer and multiply by 10  
    def delay2 = (RelaySwitchDelay2*10).toInteger() // the input which we get from the user is a string and is in seconds while the MIMO2 configuration requires it in 100ms so - change to integer and multiply by 10
	if (delay > 31) 
    {
        log.debug "Relay 1 input ${delay / 10} set too high. Max value is 3.1"
        delay = 31
    }
    if (delay < 0) 
    {
        log.debug "Relay 1 input ${delay / 10} set too low. Min value is 0"
        delay = 0
    }
    if (delay2 > 31) 
    {
    	log.debug "Relay 2 input ${delay2 / 10} set too high. Max value is 3.1"
    	delay2 = 31
    }
     if (delay2 < 0) 
    {
    	log.debug "Relay 2 input ${delay2 / 10} set too low. Min value is 0"
    	delay = 0
    } 

    
    
    return delayBetween([
        encap(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]), 0),
        encap(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]), 0),

        encap(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]), 1),
        encap(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]), 2),
        encap(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]), 3),
        encap(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]), 4),

        secure(zwave.configurationV1.configurationSet(configurationValue: [sig1], parameterNumber: 3, size: 1)), // sends a multiLevelSensor report every 30 seconds for SIG1
        secure(zwave.configurationV1.configurationSet(configurationValue: [sig2], parameterNumber: 9, size: 1)), // sends a multiLevelSensor report every 30 seconds for SIG2
        secure(zwave.configurationV1.configurationSet(configurationValue: [delay], parameterNumber: 1, size: 1)), // configurationValue for parameterNumber means how many 100ms do you want the relay
        																										// to wait before it cycles again / size should just be 1 (for 1 byte.)
        secure(zwave.configurationV1.configurationSet(configurationValue: [delay2], parameterNumber: 2, size: 1)),
        
    ], 200)
}

def on() {	
	return encap(zwave.basicV1.basicSet(value: 0xff), 3) // physically changes the relay from on to off and requests a report of the relay
    // oddly, smartThings automatically sends a switchBinaryGet() command whenever the above basicSet command is sent, so we don't need to send one here.
}

def off() {
    return encap(zwave.basicV1.basicSet(value: 0x00), 3) // physically changes the relay from on to off and requests a report of the relay
		// oddly, smartThings automatically sends a switchBinaryGet() command whenever the above basicSet command is sent, so we don't need to send one here.
}

def on2() {
   return encap(zwave.basicV1.basicSet(value: 0xff), 4)
        // oddly, smartThings automatically sends a switchBinaryGet() command whenever the above basicSet command is sent, so we don't need to send one here.
}

def off2() {
    return encap(zwave.basicV1.basicSet(value: 0x00), 4)
        // oddly, smartThings automatically sends a switchBinaryGet() command whenever the above basicSet command is sent, so we don't need to send one here.
}

def refresh() {
	log.debug "Refresh"
	return delayBetween([
         encap(zwave.sensorMultilevelV5.sensorMultilevelGet(), 1),// requests a report of the anologue input voltage for SIG1
         encap(zwave.sensorMultilevelV5.sensorMultilevelGet(), 2),// requests a report of the anologue input voltage for SIG2
         encap(zwave.switchBinaryV1.switchBinaryGet(), 3), //requests a report of the relay to make sure that it changed for Relay 1
         encap(zwave.switchBinaryV1.switchBinaryGet(), 4), //requests a report of the relay to make sure that it changed for Relay 2
       ],200)
}

def refreshZWave() {
	log.debug "Refresh (Z-Wave Response)"
	return delayBetween([
         encap(zwave.sensorMultilevelV5.sensorMultilevelGet(), 1),// requests a report of the anologue input voltage for SIG1
         encap(zwave.sensorMultilevelV5.sensorMultilevelGet(), 2),// requests a report of the anologue input voltage for SIG2
         encap(zwave.switchBinaryV1.switchBinaryGet(), 3), //requests a report of the relay to make sure that it changed for Relay 1
         encap(zwave.switchBinaryV1.switchBinaryGet(), 4) //requests a report of the relay to make sure that it changed for Relay 2
       ],200)
}

private secureSequence(commands, delay=200) { // decided not to use this
	return delayBetween(commands.collect{ secure(it) }, delay)
}

private secure(physicalgraph.zwave.Command cmd) { //take multiChannel message and securely encrypts the message so the device can read it
	return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private encap(cmd, endpoint) { // takes desired command and encapsulates it by multiChannel and then sends it to secure() to be wrapped with another encapsulation for secure encryption
	if (endpoint) {
		return secure(zwave.multiChannelV3.multiChannelCmdEncap(bitAddress: false, sourceEndPoint:0, destinationEndPoint: endpoint).encapsulate(cmd))
	} else {
		return secure(cmd)
	}
}