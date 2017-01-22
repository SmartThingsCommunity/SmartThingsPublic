/**
 *  Centralite Keypad
 *
 *  Copyright 2015 Mitch Pond
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
	definition (name: "Centralite Keypad", namespace: "mitchpond", author: "Mitch Pond") {
	capability "Battery"
        capability "Configuration"
	capability "Sensor"
        capability "Temperature Measurement"
        capability "Refresh"
        capability "Lock Codes"
        capability "button"
        
        attribute "armMode", "String"
        
        command "enrollResponse"
        command "setDisarmed"
        command "setArmedAway"
        command "setArmedStay"
        command "setArmedNight"
        command "testCmd"
        command "sendInvalidKeycodeResponse"
        command "acknowledgeArmRequest"
        command "testPanelBeep"
        
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0401", inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019,0501", manufacturer: "CentraLite", model: "3400"
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0401", inClusters: "0000,0001,0003,0020,0402,0500,0501,0B05,FC04", outClusters: "0019,0501", manufacturer: "CentraLite", model: "3405-L"
	}

	tiles {
        valueTile("battery", "device.battery", decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        valueTile("temperature", "device.temperature", decoration: "flat") {
        	state "temperature", label: '${currentValue}°',
                backgroundColors:[
                        [value: 31, color: "#153591"],
                        [value: 44, color: "#1e9cbb"],
                        [value: 59, color: "#90d2a7"],
                        [value: 74, color: "#44b621"],
                        [value: 84, color: "#f1d801"],
                        [value: 95, color: "#d04e00"],
                        [value: 96, color: "#bc2323"]
                    ]
        }
        valueTile("armMode", "device.armMode", decoration: "flat") {
        	state "armMode", label: '${currentValue}'
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
        	state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
    	}
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
        	state "default", action:"configuration.configure", icon:"st.secondary.configure"
    	}
        main ("temperature")
        //TODO: armMode is in here for debug purposes. Remove later.
        details (["temperature","battery","armMode","configure","refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'";
    def results = [];
    
	//------Miscellaneous Zigbee message------//
	if (description?.startsWith('catchall:')) {
    	log.debug zigbee.parse(description);
		def message = zigbee.parse(description);
        
        //------Profile-wide command (rattr responses, errors, etc.)------//
        if (message?.isClusterSpecific == false) {
        	//------Default response------//
            if (message?.command == 0x0B) {
                if (message?.data[1] == 0x81) 
                    log.error "Device: unrecognized command: "+description;
                else if (message?.data[1] == 0x80) 
                    log.error "Device: malformed command: "+description;
            }
            //------Read attributes responses------//
            else if (message?.command == 0x01) {
            	if (message?.clusterId == 0x0402) {
					log.debug "Device: read attribute response: "+description;
                    results = parseTempAttributeMsg(message)
                }}
            else 
            	log.debug "Unhandled profile-wide command: "+description;
        }
        //------Cluster specific commands------//
        else if (message?.isClusterSpecific) {
        	//------IAS ACE------//
        	if (message?.clusterId == 0x0501) {
                if (message?.command == 0x07) {
                //---------------------------------------//
                //Not sure what the device is doing here. It doesn't look like an ACE client should be sending this.
                //Plus, the command isn't sent with a payload which doesn't seem to follow the spec.
                //I'm assuming that they're using it as a sort of heartbeat (??)
                //---------------------------------------//
                    log.debug "${device.displayName} awake and requesting status"
                    results = sendStatusToDevice();
                    log.trace results
                }
                else if (message?.command == 0x04) {
                	results = createEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$device.displayName panic button was pushed", isStateChange: true)
                }
                else if (message?.command == 0x00) {
                    results = handleArmRequest(message)
                    log.trace results
                }
        	}
            else log.debug "Unhandled cluster-specific command: "+description
        }
	}
    //------IAS Zone Enroll request------//
    else if (description?.startsWith('enroll request')) {
		List cmds = enrollResponse()
		log.debug "enroll response: ${cmds}"
		results = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
    //------Read Attribute response------//
    else if (description?.startsWith('read attr -')) {
		results = parseReportAttributeMessage(description)
	}
    else if (description?.startsWith('zone status ')) {
    	results = parseIasMessage(description)
    }
   
	return results
}

def configure() {

	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	def configCmds = [
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 500",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0x501 {${device.zigbeeId}} {}", "delay 200",
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0x500 {${device.zigbeeId}} {}", "delay 200",
        
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 1 {${device.zigbeeId}} {}", "delay 200",
		"zcl global send-me-a-report 1 0x20 0x20 30 21600 {01}",		//checkin time 6 hrs
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0x402 {${device.zigbeeId}} {}", "delay 200",
		"zcl global send-me-a-report 0x402 0 0x29 30 3600 {6400}",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500"

	]
    return configCmds + refresh() // send refresh cmds as part of config
}

def refresh() {
    List cmds = [
     			 "st rattr 0x${device.deviceNetworkId} 1 1 0x20", "delay 100",
     			 "st rattr 0x${device.deviceNetworkId} 1 0x402 0", "delay 100"
                ]
                 
     cmds += sendStatusToDevice()
     log.trace "Method: refresh(): "+cmds
     return cmds
}

//------Generate IAS Zone Enroll response------//
def enrollResponse() {

	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	log.debug "Sending enroll response"
	[
    //------IAS Zone/CIE setup------//
    "zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}",
    "send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 100",
	"raw 0x500 {01 23 00 00 00}",
	"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 100",
	]
}

private parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	//log.debug "Desc Map: $descMap"

	def results = []
    
	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		log.debug "Received battery level report"
		results = createEvent(getBatteryResult(Integer.parseInt(descMap.value, 16)))
	}
    else if (descMap.cluster == "0402" && descMap.attrId == "0000") {
		def value = getTemperature(descMap.value)
		results = createEvent(getTemperatureResult(value))
	}

	return results
}

private parseTempAttributeMsg(message) {
	byte[] temp = message.data[-2..-1].reverse()
    createEvent(getTemperatureResult(getTemperature(temp.encodeHex() as String)))
}

private Map parseIasMessage(String description) {
    List parsedMsg = description.split(' ')
    String msgCode = parsedMsg[2]
    
    Map resultMap = [:]
    switch(msgCode) {
        case '0x0020': // Closed/No Motion/Dry
        	resultMap = getContactResult('closed')
            break

        case '0x0021': // Open/Motion/Wet
        	resultMap = getContactResult('open')
            break

        case '0x0022': // Tamper Alarm
            break

        case '0x0023': // Battery Alarm
            break

        case '0x0024': // Supervision Report
        	resultMap = getContactResult('closed')
            break

        case '0x0025': // Restore Report
        	resultMap = getContactResult('open')
            break

        case '0x0026': // Trouble/Failure
            break

        case '0x0028': // Test Mode
            break
        case '0x0000':
        	resultMap = [name: "button", value: "holdRelease", data: [buttonNumber: 2], descriptionText: "$device.displayName tamper reset", isStateChange: true]
            break
        case '0x0004':
        	resultMap = [name: "button", value: "held", data: [buttonNumber: 2], descriptionText: "$device.displayName tamper alarmed", isStateChange: true]
            break;
        default:
        	log.debug "Invalid message code in IAS message: ${msgCode}"
    }
    return resultMap
}


private Map getContactResult(value) {
	log.debug 'Contact Status'
	def linkText = getLinkText(device)
	def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
	return [
		name: 'contact',
		value: value,
		descriptionText: descriptionText
	]
}

//TODO: find actual good battery voltage range and update this method with proper values for min/max
//
//Converts the battery level response into a percentage to display in ST
//and creates appropriate message for given level

private getBatteryResult(rawValue) {
	def linkText = getLinkText(device)

	def result = [name: 'battery']

	def volts = rawValue / 10
	def descriptionText
	if (volts > 3.5) {
		result.descriptionText = "${linkText} battery has too much power (${volts} volts)."
	}
	else {
		def minVolts = 2.1
		def maxVolts = 3.0
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		result.value = Math.min(100, (int) pct * 100)
		result.descriptionText = "${linkText} battery was ${result.value}%"
	}

	return result
}

private getTemperature(value) {
	def celsius = Integer.parseInt(value, 16).shortValue() / 100
	if(getTemperatureScale() == "C"){
		return celsius
	} else {
		return celsiusToFahrenheit(celsius) as Integer
	}
}

private Map getTemperatureResult(value) {
	log.debug 'TEMP'
	def linkText = getLinkText(device)
	if (tempOffset) {
		def offset = tempOffset as int
		def v = value as int
		value = v + offset
	}
	def descriptionText = "${linkText} was ${value}°${temperatureScale}"
	return [
		name: 'temperature',
		value: value,
		descriptionText: descriptionText
	]
}

//------Command handlers------//
private handleArmRequest(message){
	def keycode = new String(message.data[2..-2] as byte[],'UTF-8')
    def reqArmMode = message.data[0]
    state.lastKeycode = keycode
	log.debug "Received arm command with keycode/armMode: ${keycode}/${reqArmMode}"

	//Acknowledge the command. This may not be *technically* correct, but it works
    /*List cmds = [
                 "raw 0x501 {09 01 00 0${reqArmMode}}", "delay 200",
                 "send 0x${device.deviceNetworkId} 1 1", "delay 500"
                ]
    def results = cmds?.collect { new physicalgraph.device.HubAction(it) } + createCodeEntryEvent(keycode, reqArmMode)
	*/
    def results = createCodeEntryEvent(keycode, reqArmMode)
    log.trace "Method: handleArmRequest(message): "+results
    return results
}

def createCodeEntryEvent(keycode, armMode) {
	createEvent(name: "codeEntered", value: keycode as String, data: armMode as String, 
    			isStateChange: true, displayed: false)
}

//
//The keypad seems to be expecting responses that are not in-line with the HA 1.2 spec. Maybe HA 1.3 or Zigbee 3.0??
//
private sendStatusToDevice() {
	log.debug 'Sending status to device...'
    def armMode = device.currentValue("armMode")
    log.trace 'Arm mode: '+armMode
	def status = '00'
    if (armMode == 'disarmed') status = '00'
    else if (armMode == 'armedAway') status = '03'
    else if (armMode == 'armedStay') status = '01'
    else if (armMode == 'armedNight') status = '02'
    
    List cmds = ["raw 0x501 {09 01 04 ${status}00}",
    			 "send 0x${device.deviceNetworkId} 1 1", 'delay 100']
                 
    def results = cmds?.collect { new physicalgraph.device.HubAction(it) };
    log.trace 'Method: sendStatusToDevice(): '+results
    return results
}

def notifyPanelStatusChanged(status) {
	//TODO: not yet implemented. May not be needed.
}
//------------------------//

def setArmedAway() {
	sendEvent([name: "armMode", value: "armedAway", isStateChange: true])
    refresh()
}

def setDisarmed() {
	sendEvent([name: "armMode", value: "disarmed", isStateChange: true])
    refresh()
}

def setArmedStay() {
	sendEvent([name: "armMode", value: "armedStay", isStateChange: true])
    refresh()
}

def setArmedNight() {
	sendEvent([name: "armMode", value: "armedNight", isStateChange: true])
    refresh()
}

def acknowledgeArmRequest(armMode){
	List cmds = [
                 "raw 0x501 {09 01 00 0${armMode}}",
                 "send 0x${device.deviceNetworkId} 1 1", "delay 100"
                ]
    def results = cmds?.collect { new physicalgraph.device.HubAction(it) }
    log.trace "Method: acknowledgeArmRequest(armMode): "+results
    return results
}

def sendInvalidKeycodeResponse(){
	List cmds = [
    			 "raw 0x501 {09 01 00 04}",
                 "send 0x${device.deviceNetworkId} 1 1", "delay 100"
                ]
                 
    log.trace 'Method: sendInvalidKeycodeResponse(): '+cmds
    return (cmds?.collect { new physicalgraph.device.HubAction(it) }) + sendStatusToDevice()
}

def testPanelBeep(){
	//zigbee.command(0x0501, 0x08, "00", "00")
    List cmds = ["raw 0x501 {09 01 04 0514}",
    			 "send 0x${device.deviceNetworkId} 1 1", 'delay 100']
                 
    def results = cmds?.collect { new physicalgraph.device.HubAction(it) };
    log.trace 'Method: sendStatusToDevice(): '+results
    return results
}

//------Utility methods------//
private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private String swapEndianHex(String hex) {
	reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
	int i = 0;
	int j = array.length - 1;
	byte tmp;
	while (j > i) {
		tmp = array[j];
		array[j] = array[i];
		array[i] = tmp;
		j--;
		i++;
	}
	return array
}
//------------------------//

private testCmd(){
	//log.trace zigbee.parse('catchall: 0104 0501 01 01 0140 00 4F2D 01 00 0000 07 00 ')
    
    //test exit delay
    //log.debug device.zigbeeId
	List cmds = ["raw 0x501 {09 01 04 0000}", 'delay 200',
    			 "send 0x${device.deviceNetworkId} 1 1", 'delay 500']
    cmds
}
