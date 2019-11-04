/**
 *  CoopBoss H3Vx
 *	02/29/16	Fixed app crash with Android by changing the syntax of default state in tile definition.
 *				Fixed null value errors during join process.  Added 3 new commands to refresh data.
 *
 *	01/18/16	Masked invalid temperature reporting when TempProbe1 is below 0C
 *				Added setBaseCurrentNE, readBaseCurrentNE, commands as well as baseCurrentNE attribute.
 *
 *  Copyright 2016 John Rucker
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *  Icon location = http://scripts.3dgo.net/smartthings/icons/
 */
metadata {
	definition (name: "CoopBoss H3Vx", namespace: "JohnRucker", author: "John.Rucker@Solar-Current.com") {
        capability "Refresh"
        capability "Polling"
        capability "Sensor"
        capability "Actuator"
        capability "Configuration"
		capability "Temperature Measurement"
        capability "Door Control"
  		capability "Switch"

        command "closeDoor"
        command "closeDoorHiI"
        command "openDoor"
        command "autoCloseOn"
        command "autoCloseOff"
        command "autoOpenOn"
        command "autoOpenOff"
        command "setCloseLevelTo"
        command "setOpenLevelTo"
        command "setSensitivityLevel"
        command "Aux1On"
        command "Aux1Off"
        command "Aux2On"
        command "Aux2Off"
        command "updateTemp1"
        command "updateTemp2"
        command "updateSun"
        command "setNewBaseCurrent"
        command "setNewPhotoCalibration"
        command "readNewPhotoCalibration"
        command "readBaseCurrentNE"
        command "setBaseCurrentNE"
        command "updateSensitivity"
        command "updateCloseLightLevel"
        command "updateOpenLightLevel"

        attribute "doorState","string"
        attribute "currentLightLevel","number"
        attribute "closeLightLevel","number"
        attribute "openLightLevel","number"
        attribute "autoCloseEnable","string"
        attribute "autoOpenEnable","string"
        attribute "TempProb1","number"
        attribute "TempProb2","number"
        attribute "dayOrNight","string"
        attribute "doorSensitivity","number"
        attribute "doorCurrent","number"
        attribute "doorVoltage","number"
        attribute "Aux1","string"
        attribute "Aux2","string"
        attribute "coopStatus","string"
        attribute "baseDoorCurrent","number"
        attribute "photoCalibration","number"
        attribute "baseCurrentNE","string"

    	fingerprint profileId: "0104", inClusters: "0000,0101,0402", manufacturer: "Solar-Current", model: "Coop Boss"

	}

	// simulator metadata
	simulator {
    }


	preferences {
		input "tempOffsetCoop", "number", title: "Coop Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
		input "tempOffsetOutside", "number", title: "Outside Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
	}


	// UI tile definitions
	tiles {
		standardTile("doorCtrl", "device.doorState", width: 1, height: 1, canChangeIcon: true){
			state "unknown", label: '${name}', action:"openDoor", icon: "st.Outdoor.outdoor20", nextState:"Sent"
			state "open", label: '${name}', action:"closeDoor", icon: "st.Outdoor.outdoor20", backgroundColor: "#0000ff" , nextState:"Sent"
			state "opening", label: '${name}', action:"closeDoor", icon: "st.Outdoor.outdoor20", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', action:"openDoor", icon: "st.Outdoor.outdoor20", backgroundColor: "#79b821", nextState:"Sent"
			state "closing", label: '${name}', action:"openDoor", icon: "st.Outdoor.outdoor20", backgroundColor: "#ffa81e"
			state "jammed", label: '${name}', action:"closeDoorHiI", icon: "st.Outdoor.outdoor20", backgroundColor: "#ff0000", nextState:"Sent"
			state "forced close", label: 'forced\rclose', action:"openDoor", icon: "st.Outdoor.outdoor20", backgroundColor: "#ff8000", nextState:"Sent"
			state "Sent", label: 'wait', icon: "st.motion.motion.active", backgroundColor: "#ffa81e"
		}

		standardTile("dtlsDoorCtrl", "device.doorState", width: 2, height: 2){
			state "unknown", label: '${name}', action:"openDoor", icon: "st.secondary.tools", nextState:"Sent"
			state "open", label: '${name}', action:"closeDoor", icon: "st.doors.garage.garage-open", backgroundColor: "#0000ff", nextState:"Sent"
			state "opening", label: '${name}', action:"closeDoor", icon: "st.doors.garage.garage-opening", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', action:"openDoor", icon: "st.doors.garage.garage-closed", backgroundColor: "#79b821", nextState:"Sent"
			state "closing", label: '${name}', action:"openDoor", icon: "st.doors.garage.garage-closing", backgroundColor: "#ffa81e"
			state "jammed", label: '${name}', action:"closeDoorHiI", icon: "st.doors.garage.garage-open", backgroundColor: "#ff0000", nextState:"Sent"
			state "forced close", label: "forced", action:"openDoor", icon: "st.doors.garage.garage-closed", backgroundColor: "#ff8000", nextState:"Sent"
			state "Sent", label: 'wait', icon: "st.motion.motion.active", backgroundColor: "#ffa81e"
		}

		standardTile("autoClose", "device.autoCloseEnable", width: 1, height: 1){
			state "on", label: 'Auto', action:"autoCloseOff", icon: "st.doors.garage.garage-closing", backgroundColor: "#79b821"
			state "off", label: 'Auto', action:"autoCloseOn", icon: "st.doors.garage.garage-closing"
		}

		standardTile("autoOpen", "device.autoOpenEnable", width: 1, height: 1){
			state "on", label: 'Auto', action:"autoOpenOff", icon: "st.doors.garage.garage-opening", backgroundColor: "#79b821"
			state "off", label: 'Auto', action:"autoOpenOn", icon: "st.doors.garage.garage-opening"
		}

		valueTile("TempProb1", "device.TempProb1", width: 1, height: 1, decoration: "flat"){
        state defaultState: true, label:'Outside\r${currentValue}°', unit:"F"}

		valueTile("TempProb2", "device.TempProb2", width: 1, height: 1, decoration: "flat"){
        state defaultState: true, label:'Coop\r${currentValue}°', unit:"F"}

		valueTile("currentLevel", "device.currentLightLevel", width: 1, height: 1, decoration: "flat") {
        state defaultState: true, label:'Sun\r${currentValue}'}

        valueTile("dayOrNight", "device.dayOrNight", decoration: "flat",  inactiveLabel: false, width: 3, height: 1) {
        state defaultState: true, label:'${currentValue}.'
        }

        controlTile("SetClSlider", "device.closeLightLevel", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
        state "closeLightLevel", action:"setCloseLevelTo", backgroundColor:"#d04e00"
        }

        valueTile("SetClValue", "device.closeLightLevel", decoration: "flat",  inactiveLabel: false, width: 1, height: 1) {
        state defaultState: true, label:'Close\nSunlight\n${currentValue}'
        }

        controlTile("SetOpSlider", "device.openLightLevel", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
        state "openLightLevel", action:"setOpenLevelTo", backgroundColor:"#d04e00"
        }

        valueTile("SetOpValue", "device.openLightLevel", decoration: "flat",  inactiveLabel: false, width: 1, height: 1) {
        state defaultState: true, label:'Open\nSunlight\n${currentValue}'
        }

        controlTile("SetSensitivitySlider", "device.doorSensitivity", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
        state "openLightLevel", action:"setSensitivityLevel", backgroundColor:"#d04e00"
        }

        valueTile("SetSensitivityValue", "device.doorSensitivity", decoration: "flat",  inactiveLabel: false, width: 1, height: 1) {
        state defaultState: true, label:'Door\nSensitivity\n${currentValue}'
        }

        standardTile("refresh", "device.refresh", decoration: "flat", inactiveLabel: false) {
		state defaultState: true, label:'All', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}

		main "doorCtrl"
		details (["dtlsDoorCtrl", "autoClose", "autoOpen",  "TempProb1", "TempProb2", "currentLevel", "dayOrNight",
        "SetClSlider", "SetClValue", "SetOpSlider", "SetOpValue", "SetSensitivitySlider", "SetSensitivityValue", "refresh"])
	}
}

// Parse incoming device messages to generate events  def parse(String description) {
def parse(String description) {
	//log.debug "description: $description"
	Map map = [:]
	if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	}
	else if (description?.startsWith('read attr -')) {
		map = parseReportAttributeMessage(description)
	}
	else if (description?.startsWith('temperature: ') || description?.startsWith('humidity: ')) {
		map = parseCustomMessage(description)
	}
    log.debug map
	//return map ? createEvent(map) : null
    sendEvent(map)
    callUpdateStatusTxt()
}

private Map parseCatchAllMessage(String description) {
    Map resultMap = [:]
    def cluster = zigbee.parse(description)
    log.debug cluster
    if (cluster.clusterId == 0x0402) {
        switch(cluster.sourceEndpoint) {

            case 0x39:		// Endpoint 0x39 is the temperature of probe 1
            String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
            resultMap.name = "TempProb1"
            def celsius = Integer.valueOf(temp,16).shortValue()
            if (celsius ==  -32768){									// This number is used to indicate an error in the temperature reading
                resultMap.value = "---"
            }else{
                celsius = celsius / 100									// Temperature value is sent X 100.
                resultMap.value = celsiusToFahrenheit(celsius)
                if (tempOffsetOutside) {
                    def offset = tempOffsetOutside as int
					resultMap.value = resultMap.value + offset
                }
            }
            sendEvent(name: "temperature", value: resultMap.value, displayed: false)		// set the temperatureMeasurment capability to temperature
            break

            case 0x40:													// Endpoint 0x40 is the temperature of probe 2
            String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
            resultMap.name = "TempProb2"
            def celsius = Integer.valueOf(temp,16).shortValue()
            //resultMap.descriptionText = "Prob2 celsius value = ${celsius}"
            if (celsius ==  -32768){									// This number is used to indicate an error in the temperature reading
                resultMap.value = "---"
            }else{
            	celsius = celsius / 100									// Temperature value is sent X 100.
                resultMap.value = celsiusToFahrenheit(celsius)
                if (tempOffsetCoop) {
                    def offset = tempOffsetCoop as int
					resultMap.value = resultMap.value + offset
                }
            }
            break
    	}
	}

    if (cluster.clusterId == 0x0101 && cluster.command == 0x0b) {		// This is a default response to a command sent to cluster 0x0101 door control
    	//log.debug "Default Response Data = $cluster.data"
        switch(cluster.data) {

        case "[10, 0]":		// 0x0a turn auto close on command verified
        resultMap.name = "autoCloseEnable"
        resultMap.value = "on"
        break

        case "[11, 0]":		// 0x0b turn auto close off command verified
        resultMap.name = "autoCloseEnable"
        resultMap.value = "off"
        break

        case "[12, 0]":		// 0x0C turn auto open on command verified
        resultMap.name = "autoOpenEnable"
        resultMap.value = "on"
        break

        case "[13, 0]":		// 0x0d turn auto open off command verified
        resultMap.name = "autoOpenEnable"
        resultMap.value = "off"
        break


        case "[20, 0]":		// 0x14 Aux1 On command verified
        log.info "verified Aux1 On"
    	sendEvent(name: "switch", value: "on", displayed: false)
        resultMap.name = "Aux1"
        resultMap.value = "on"
        break

        case "[21, 0]":		// 0x15 Aux1 Off command verified
        log.info "verified Aux1 Off"
    	sendEvent(name: "switch", value: "off", displayed: false)
        resultMap.name = "Aux1"
        resultMap.value = "off"
        break

        case "[22, 0]":		// 0x16 Aux2 On command verified
        log.info "verified Aux2 On"
        resultMap.name = "Aux2"
        resultMap.value = "on"
        break

        case "[23, 0]":		// 0x17 Aux2 Off command verified
        log.info "verified Aux2 Off"
        resultMap.name = "Aux2"
        resultMap.value = "off"
        break

        }
    }
    return resultMap
}

private Map parseReportAttributeMessage(String description) {
    Map resultMap = [:]
    def descMap = parseDescriptionAsMap(description)
    //log.debug "read attr descMap --> $descMap"
    if (descMap.cluster == "0101" && descMap.attrId == "0003") {
        resultMap.name = "doorState"
        if (descMap.value == "00"){
            resultMap.value = "unknown"
            sendEvent(name: "door", value: "unknown", displayed: false)
        }else if(descMap.value == "01"){
            resultMap.value = "closed"
            sendEvent(name: "door", value: "closed", displayed: false)
        }else if(descMap.value == "02"){
            resultMap.value = "open"
            sendEvent(name: "door", value: "open", displayed: false)
        }else if(descMap.value == "03"){
            resultMap.value = "jammed"
        }else if(descMap.value == "04"){
            resultMap.value = "forced close"
        }else if(descMap.value == "05"){
            resultMap.value = "forced close"
        }else if(descMap.value == "06"){
            resultMap.value = "closing"
            sendEvent(name: "door", value: "closing", displayed: false)
        }else if(descMap.value == "07"){
            resultMap.value = "opening"
            sendEvent(name: "door", value: "opening", displayed: false)
        }else if(descMap.value == "08"){
            resultMap.value = "fault"
        }else {
            resultMap.value = "unknown"
        }
        resultMap.descriptionText = "Door State Changed to ${resultMap.value}"

    } else if (descMap.cluster == "0101" && descMap.attrId == "0400") {
        resultMap.name = "currentLightLevel"
        resultMap.value = (Integer.parseInt(descMap.value, 16))
        resultMap.displayed = false
        def cLL = device.currentState("closeLightLevel")?.value as int
        def oLL = device.currentState("openLightLevel")?.value as int
        def nLL = resultMap.value as int
        if (nLL < cLL){
           	sendEvent(name: "dayOrNight", value: "The current sunlight level is ${nLL}, it must be > ${oLL} for an auto open", displayed: false)
        }else {
            sendEvent(name: "dayOrNight", value: "The current sunlight level is ${nLL}, it must be < ${cLL} for an auto close", displayed: false)
        }

    } else if (descMap.cluster == "0101" && descMap.attrId == "0401") {
        resultMap.name = "closeLightLevel"
        resultMap.value = (Integer.parseInt(descMap.value, 16))

    } else if (descMap.cluster == "0101" && descMap.attrId == "0402") {
        resultMap.name = "openLightLevel"
        resultMap.value = (Integer.parseInt(descMap.value, 16))

    } else if (descMap.cluster == "0101" && descMap.attrId == "0403") {
        resultMap.name = "autoCloseEnable"
        if (descMap.value == "01"){resultMap.value = "on"}
        else{resultMap.value = "off"}

    } else if (descMap.cluster == "0101" && descMap.attrId == "0404") {
        resultMap.name = "autoOpenEnable"
        if (descMap.value == "01"){resultMap.value = "on"}
        else{resultMap.value = "off"}

    } else if (descMap.cluster == "0101" && descMap.attrId == "0405") {
        resultMap.name = "doorCurrent"
        resultMap.value = (Integer.parseInt(descMap.value, 16))
        resultMap.value = resultMap.value * 0.001

     } else if (descMap.cluster == "0101" && descMap.attrId == "0408") {
        resultMap.name = "doorSensitivity"
        resultMap.value = (100 - Integer.parseInt(descMap.value, 16))

    } else if (descMap.cluster == "0101" && descMap.attrId == "0409") {
        resultMap.name = "baseDoorCurrent"
        resultMap.value = (Integer.parseInt(descMap.value, 16))
        resultMap.value = resultMap.value * 0.001

    } else if (descMap.cluster == "0101" && descMap.attrId == "040a") {
        resultMap.name = "doorVoltage"
        resultMap.value = (Integer.parseInt(descMap.value, 16))
        resultMap.value = resultMap.value * 0.001

    } else if (descMap.cluster == "0101" && descMap.attrId == "040b") {
        resultMap.name = "Aux1"
        if(descMap.value == "01"){
        resultMap.value = "on"
    	sendEvent(name: "switch", value: "on", displayed: false)
        }else{
        resultMap.value = "off"
    	sendEvent(name: "switch", value: "off", displayed: false)
        }

    } else if (descMap.cluster == "0101" && descMap.attrId == "040c") {
        resultMap.name = "Aux2"
        if(descMap.value == "01"){
        resultMap.value = "on"
        }else{
        resultMap.value = "off"
        }
    } else if (descMap.cluster == "0101" && descMap.attrId == "040d") {
        resultMap.name = "photoCalibration"
        resultMap.value = (Integer.parseInt(descMap.value, 16))
    } else if (descMap.cluster == "0101" && descMap.attrId == "040e") {
        resultMap.name = "baseCurrentNE"
        resultMap.value = (Integer.parseInt(descMap.value, 16))
    }
    return resultMap
}

private Map parseCustomMessage(String description) {
    //log.info "ParseCustomMessage called with ${description}"
	Map resultMap = [:]
    if (description?.startsWith('temperature: ')) {
        resultMap.name = "temperature"
    	def rawT = (description - "temperature: ").trim()
        resultMap.descriptionText = "Temperature celsius value = ${rawT}"
        def rawTint = Float.parseFloat(rawT)
        if (rawTint > 65){
        	resultMap.name = null
            resultMap.value = null
            resultMap.descriptionText = "Temperature celsius value = ${rawT} is invalid not updating"
            log.warn "Invalid temperature value detected! rawT = ${rawT}, description = ${description}"
        }else if (rawT ==  -32768){											// This number is used to indicate an error in the temperature reading
            resultMap.value = "ERR"
        }else{
            resultMap.value = celsiusToFahrenheit(rawT.toFloat()) as Float
            sendEvent(name: "TempProb1", value: resultMap.value, displayed: false)		// Workaround for lack of access to endpoint information for Temperature report
        }
	}
    resultMap.displayed = false
    log.info "Temperature reported = ${resultMap.value}"
    return resultMap
}

def parseDescriptionAsMap(description) {
    (description - "read attr - ").split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
}

// Added for Temeperature parse
def getFahrenheit(value) {
	def celsius = Integer.parseInt(value, 16)
	return celsiusToFahrenheit(celsius) as Integer
}

// Private methods
def callUpdateStatusTxt(){
	def cTemp = device.currentState("TempProb1")?.value
    def cLight = 0
    def testNull = device.currentState("currentLightLevel")?.value
    if (testNull != null){
    cLight = device.currentState("currentLightLevel")?.value as int
    }
	updateStatusTxt(cTemp, cLight)
}

def updateStatusTxt(currentTemp, currentLight){
	//log.info "called updateStatusTxt with ${currentTemp}, ${currentLight}"
    def cTmp = currentTemp
    def cLL = 10
    def oLL = 10

    def testNull = device.currentState("closeLightLevel")?.value
    if (testNull != null){
    	cLL = device.currentState("closeLightLevel")?.value as int
	}

    testNull = device.currentState("openLightLevel")?.value
    if (testNull != null){
    	oLL = device.currentState("openLightLevel")?.value as int
    }

    def aOpnEn = device.currentState("autoOpenEnable")?.value
    def aClsEn = device.currentState("autoCloseEnable")?.value

        if (currentLight < cLL){
        	if (aOpnEn == "on"){
           		sendEvent(name: "dayOrNight", value: "Sun must be > ${oLL} to auto open", displayed: false)
            	sendEvent(name: "coopStatus", value: "Sunlight ${currentLight} open at ${oLL}. Coop ${cTmp}°", displayed: false)
                }else{
           		sendEvent(name: "dayOrNight", value: "Auto Open is turned off.", displayed: false)
            	sendEvent(name: "coopStatus", value: "Sunlight ${currentLight} auto open off. Coop ${cTmp}°", displayed: false)
                }
        }else {
        	if (aClsEn == "on"){
            	sendEvent(name: "dayOrNight", value: "Sun must be < ${cLL} to auto close", displayed: false)
            	sendEvent(name: "coopStatus", value: "Sunlight ${currentLight} close at ${cLL}. Coop ${cTmp}°", displayed: false)
                }else{
           		sendEvent(name: "dayOrNight", value: "Auto Close is turned off.", displayed: false)
            	sendEvent(name: "coopStatus", value: "Sunlight ${currentLight} auto close off. Coop ${cTmp}°", displayed: false)
                }
        }
}

// Commands to device
def on() {
	log.debug "on calling Aux1On"
    Aux1On()
}

def off() {
	log.debug "off calling Aux1Off"
    Aux1Off()
}

def close() {
	log.debug "close calling closeDoor"
	closeDoor()
}

def open() {
	log.debug "open calling openDoor"
	openDoor()
}

def Aux1On(){
	log.debug "Sending Aux1 = on command"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x14 {}"
}

def Aux1Off(){
	log.debug "Sending Aux1 = off command"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x15 {}"
}

def Aux2On(){
	log.debug "Sending Aux2 = on command"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x16 {}"
}

def Aux2Off(){
	log.debug "Sending Aux2 = off command"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x17 {}"
}

def openDoor() {
	log.debug "Sending Open command"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x1 {}"
}

def closeDoor() {
	log.debug "Sending Close command"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x0 {}"
}

def closeDoorHiI() {
	sendEvent(name: "doorState", value: "closing")
	log.debug "Sending High Current Close command"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x4 {}"
}

def autoOpenOn() {
	log.debug "Setting Auto Open On"
	sendEvent(name: "autoOpenEnable", value: "on")
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x0C {}"
}

def autoOpenOff() {
	log.debug "Setting Auto Open Off"
	sendEvent(name: "autoOpenEnable", value: "off")
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x0D {}"
}

def autoCloseOn() {
	log.debug "Setting Auto Close On"
	sendEvent(name: "autoCloseEnable", value: "on")
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x0A {}"
}

def autoCloseOff() {
	log.debug "Setting Auto Close Off"
	sendEvent(name: "autoCloseEnable", value: "off")
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x0B {}"
}

def setOpenLevelTo(cValue) {
	def cX = cValue
	log.debug "Setting Open Light Level to ${cX} Hex = 0x${Integer.toHexString(cX)}"

    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x0101 0x402 0x23 {${Integer.toHexString(cX)}}"
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x402"											// Read light value
    cmd

}

def setCloseLevelTo(cValue) {
	def cX = cValue
	log.debug "Setting Close Light Level to ${cX} Hex = 0x${Integer.toHexString(cX)}"

    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x0101 0x401 0x23 {${Integer.toHexString(cX)}}"
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x401"											// Read light value
    cmd

}

def setSensitivityLevel(cValue) {
	def cX = 100 - cValue
	log.debug "Setting Door sensitivity level to ${cX} Hex = 0x${Integer.toHexString(cX)}"

    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x0101 0x408 0x23 {${Integer.toHexString(cX)}}"		// Write attribute.  0x23 is a 32 bit integer value.
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x408"											// Read attribute
    cmd
}

def setNewBaseCurrent(cValue) {
	def cX = cValue as int
	log.info "Setting new BaseCurrent to ${cX} Hex = 0x${Integer.toHexString(cX)}"

    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x0101 0x409 0x23 {${Integer.toHexString(cX)}}"		// Write attribute.  0x23 is a 32 bit integer value.
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x409"											// Read attribute
    cmd
}

def setNewPhotoCalibration(cValue) {
	def cX = cValue as int
	log.info "Setting new Photoresister calibration to ${cX} Hex = 0x${Integer.toHexString(cX)}"

    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x0101 0x40D 0x2B {${Integer.toHexString(cX)}}"		// Write attribute.  0x2B is a 32 bit signed integer value.
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x40D"											// Read attribute
    cmd
}

def readNewPhotoCalibration() {
	log.info "Requesting current Photoresister calibration "

    def cmd = []
	cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x40D"											// Read attribute
    cmd
}

def readBaseCurrentNE() {
	log.info "Requesting base current never exceed setting "

    def cmd = []
	cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x40E"											// Read attribute
    cmd
}

def setBaseCurrentNE(cValue) {
	def cX = cValue as int
	log.info "Setting new base Current Never Exceed to ${cX} Hex = 0x${Integer.toHexString(cX)}"

    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x0101 0x40E 0x23 {${Integer.toHexString(cX)}}"		// Write attribute.  0x23 is a 32 bit unsigned integer value.
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x40E"											// Read attribute
    cmd
}

def poll(){
	log.debug "Polling Device"
    def cmd = []
	cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0003"   // Read Door State
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0400"	// Read Current Light Level
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x39 0x0402 0x0000"    // Read probe 1 Temperature
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x40 0x0402 0x0000"    // Read probe 2 Temperature

    cmd
}

def updateTemp1() {
	log.debug "Sending attribute read request for Temperature Probe1"
    def cmd = []
    cmd << "st rattr 0x${device.deviceNetworkId} 0x39 0x0402 0x0000"    // Read Current Temperature from Coop Probe 1
    cmd
}

def updateTemp2() {
	log.debug "Sending attribute read request for Temperature Probe2"
    def cmd = []
    cmd << "st rattr 0x${device.deviceNetworkId} 0x40 0x0402 0x0000"    // Read Current Temperature from Coop Probe 2
    cmd
}


def updateSun() {
	log.debug "Sending attribute read request for Sun Light Level"
    def cmd = []
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0400"	// Read Current Light Level
    cmd
}

def updateSensitivity() {
	log.debug "Sending attribute read request for door sensitivity"
    def cmd = []
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0408"	// Read Door sensitivity
    cmd
}

def updateCloseLightLevel() {
	log.debug "Sending attribute read close light level"
    def cmd = []
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0401"
    cmd
}

def updateOpenLightLevel() {
	log.debug "Sending attribute read open light level"
    def cmd = []
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0402"
    cmd
}

def refresh() {
	log.debug "sending refresh command"
    def cmd = []
	cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0003"   // Read Door State
    cmd << "delay 150"
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0400"	// Read Current Light Level
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0401"	// Read Door Close Light Level
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0402"	// Read Door Open Light Level
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0403"	// Read Auto Door Close Settings
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0404"	// Read Auto Door Open Settings
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x39 0x0402 0x0000"    // Read Current Temperature from Coop Probe 1
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0408"    // Object detection sensitivity
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x40 0x0402 0x0000"    // Read Current Temperature from Coop Probe 2
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0405"    // Current required to close door
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x040B"    // Aux1 Status
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x040C"    // Aux2 Status
    cmd << "delay 150"

    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x409"		// Read Base current

    cmd
}

def configure() {
    log.debug "Binding SEP 0x38 DEP 0x01 Cluster 0x0101 Lock cluster to hub"
    log.debug "Binding SEP 0x39 DEP 0x01 Cluster 0x0402 Temperature cluster to hub"
    log.debug "Binding SEP 0x40 DEP 0x01 Cluster 0x0402 Temperature cluster to hub"

    def cmd = []
    cmd << "zdo bind 0x${device.deviceNetworkId} 0x38 0x01 0x0101 {${device.zigbeeId}} {}"			// Bind to end point 0x38 and the lock cluster
    cmd << "delay 150"
    cmd << "zdo bind 0x${device.deviceNetworkId} 0x39 0x01 0x0402 {${device.zigbeeId}} {}"    		// Bind to end point 0x39 and the temperature cluster
    cmd << "delay 150"
    cmd << "zdo bind 0x${device.deviceNetworkId} 0x40 0x01 0x0402 {${device.zigbeeId}} {}"    		// Bind to end point 0x40 and the temperature cluster
    cmd << "delay 1500"

    log.info "Sending ZigBee Configuration Commands to Coop Control"
    return cmd + refresh()
}
