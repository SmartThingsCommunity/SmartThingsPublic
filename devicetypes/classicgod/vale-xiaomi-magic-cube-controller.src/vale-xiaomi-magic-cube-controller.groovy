/**
 *  "Xiaomi Magic Cube Controller"
 *
 *  Author: Artur Draga
 *	
 * 	Based on code by: Oleg "DroidSector" Smirnov
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition (name: "Vale Xiaomi Magic Cube Controller", namespace: "ClassicGOD", author: "Artur Draga") {
		capability "Actuator"
		capability "Button"
		capability "Configuration"
		capability "Battery"
		capability "Three Axis" //Simulated!
		capability "Sensor"

		attribute "face", "number"
		attribute "angle", "number"

		command "setFace0"
		command "setFace1"
		command "setFace2"
		command "setFace3"
		command "setFace4"
		command "setFace5"

		command "flip90"
		command "flip180"
		command "slide"
		command "knock"
		command "rotateR"
		command "rotateL"
		command "shake"

	}

	simulator {
	}

	tiles (scale: 2){
		//button tiles!
		standardTile("face0", "device.face", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Face 0', action:"setFace0", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/def_face.png", backgroundColor: "#ffffff"
			state "0", label:'Face 0', action:"setFace0", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/def_face.png", backgroundColor: "#00a0dc"
		}
		standardTile("face1", "device.face", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Face 1', action:"setFace1", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/def_face.png", backgroundColor: "#ffffff"
			state "1", label:'Face 1', action:"setFace1", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/def_face.png", backgroundColor: "#00a0dc"
		}
		standardTile("face2", "device.face", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Face 2', action:"setFace2", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/def_face.png", backgroundColor: "#ffffff"
			state "2", label:'Face 2', action:"setFace2", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/def_face.png", backgroundColor: "#00a0dc"
		}
		standardTile("face3", "device.face", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Face 3', action:"setFace3", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/bat_face.png", backgroundColor: "#ffffff"
			state "3", label:'Face 3', action:"setFace3", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/bat_face.png", backgroundColor: "#00a0dc"
		}
		standardTile("face4", "device.face", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Face 4', action:"setFace4", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/def_face.png", backgroundColor: "#ffffff"
			state "4", label:'Face 4', action:"setFace4", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/def_face.png", backgroundColor: "#00a0dc"
		}
		standardTile("face5", "device.face", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Face 5', action:"setFace5", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face.png", backgroundColor: "#ffffff"
			state "5", label:'Face 5', action:"setFace5", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face_s.png", backgroundColor: "#00a0dc"
		}
		//function tiles!
		standardTile("flip90", "device.button", decoration: "flat", width: 2, height: 2) { state "default", label: "90°", action: "flip90", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/90.png", backgroundColor: "#ffffff" }
		standardTile("flip180", "device.button", decoration: "flat", width: 2, height: 2) { state "default", label: "180°", action: "flip180", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/180.png", backgroundColor: "#ffffff" }
		standardTile("rotateL", "device.button", decoration: "flat", width: 2, height: 2) { state "default", label: "rotate left", action: "rotateL", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/rotate_left.png",  backgroundColor: "#ffffff" }
		standardTile("rotateR", "device.button", decoration: "flat", width: 2, height: 2) { state "default", label: "rotate right", action: "rotateR", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/rotate_right.png",  backgroundColor: "#ffffff" }
		standardTile("slide", "device.button", decoration: "flat", width: 2, height: 2) { state "default", label: "slide", action: "slide", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/slide.png",  backgroundColor: "#ffffff" }
		standardTile("knock", "device.button", decoration: "flat", width: 2, height: 2) { state "default", label: "knock", action: "knock", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/knock.png",  backgroundColor: "#ffffff" }
		standardTile("shake", "device.button", decoration: "flat", width: 2, height: 2) { state "default", label: "shake" , action: "shake", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/shake.png",  backgroundColor: "#ffffff" }
		valueTile("battery", "device.battery", decoration: "flat", width: 4, height: 2) { state "val", label: '${currentValue}% battery', backgroundColor: "#ffffff" }
		standardTile("faceMain", "device.face", decoration: "flat", width: 2, height: 2) { 
			state "default", label:'Face: ${currentValue} ', icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/cube_icon.png", backgroundColor: "#ffffff"
		}

	   main(["faceMain"])
	   details(["flip90","face0","flip180","face4","face5","face1","rotateL","face3","rotateR","slide","face2","knock","battery","shake"])
	}

	preferences {
		input (
			name: "cubeMode",
			title: "Cube Mode",
			description: "Select how many button events should be sent by the device handler.\n\nSimple presents just 7 buttons on basic gestures (shake,flip 90, flip 180, slide, knock, rotate R, rotate L).\n\nAdvanced presents 36 buttons for separate actions on every face (activate, slide, knock, rotate R, rotate L, shake).\n\nCombined ofers both for total of 43 buttons. ",
			type: "enum",
			options: [
				0: "Simple - 7 buttons", 
				1: "Advanced - 36 buttons", 
				2: "Combined - 43 buttons"
			],
			required: false
		)
	}
}

def setFace0() { setFace(0) }
def setFace1() { setFace(1) }
def setFace2() { setFace(2) }
def setFace3() { setFace(3) }
def setFace4() { setFace(4) }
def setFace5() { setFace(5) }
def flip90() { 
	def flipMap = [0:5, 1:2, 2:0, 3:2, 4:5, 5:3]
	flipEvents(flipMap[device.currentValue("face") as Integer], "90") 
}
def flip180() { 
	def flipMap = [0:3, 1:4, 2:5, 3:0, 4:1, 5:2]
	flipEvents(flipMap[device.currentValue("face") as Integer], "180") 
}
def rotateL() { rotateEvents(-90) }
def rotateR() { rotateEvents(90) }
def slide() { slideEvents(device.currentValue("face") as Integer) }
def knock() { knockEvents(device.currentValue("face") as Integer) }
def shake() { shakeEvents() }

def setFace(Integer faceId) {
	def Integer prevFaceId = device.currentValue("face")
	if (prevFaceId == faceId) {
		flipEvents(faceId, "0")
	} else if ((prevFaceId == 0 && faceId == 3)||(prevFaceId == 1 && faceId == 4)||(prevFaceId == 2 && faceId == 5)||(prevFaceId == 3 && faceId == 0)||(prevFaceId == 4 && faceId == 1)||(prevFaceId == 5 && faceId == 2)){
		flipEvents(faceId, "180")
	} else {
		flipEvents(faceId, "90")
	}
}

def parse(String description) {
	def value = zigbee.parse(description)?.text


	if (description?.startsWith('catchall:')) {
		parseCatchAllMessage(description)
	}
	else if (description?.startsWith('read attr -')) {
		parseReportAttributeMessage(description)
	}


	if (description?.startsWith('enroll request')) {
		List cmds = enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	return result
}

// not tested
private parseCatchAllMessage(String description) {
	def cluster = zigbee.parse(description)
	if (shouldProcessMessage(cluster)) {
		switch(cluster.clusterId) {
			case 0x0000:
			log.debug "battery! " + cluster.data + " : " + cluster.data.get(6) + " : " + cluster
			getBatteryResult(cluster.data.get(6))
			break
		}
	}
}

// not tested
private boolean shouldProcessMessage(cluster) {
	boolean ignoredMessage = cluster.profileId != 0x0104 ||
	cluster.command == 0x0B ||
	cluster.command == 0x07 ||
	(cluster.data.size() > 0 && cluster.data.first() == 0x3e)
	return !ignoredMessage
}

private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		getBatteryResult(Integer.parseInt(descMap.value, 16))
	}
	else if (descMap.cluster == "0012" && descMap.attrId == "0055") { // Shake, flip, knock, slide
		getMotionResult(descMap.value)  
	} 
	else if (descMap.cluster == "000C" && descMap.attrId == "ff05") { // Rotation (90 and 180 degrees)
		getRotationResult(descMap.value)
	} else { log.debug descMap }

}

def String hexToBinOld(String thisByte) {
	return String.format("%8s", Integer.toBinaryString(Integer.parseInt(thisByte,16))).replace(' ', '0')
}

def String hexToBin(String thisByte, Integer size = 8) {
	String binaryValue = new BigInteger(thisByte, 16).toString(2);
	return String.format("%${size}s", binaryValue).replace(' ', '0')
}
private Map parseCustomMessage(String description) {
	Map resultMap = [:]
	return resultMap
}

private getBatteryResult(rawValue) {
	def battLevel = Math.round(rawValue * 100 / 255)

	if (battLevel > 100) {
		battLevel = 100
	}

   	if (battLevel) { sendEvent( [ name: "battery", value: battLevel, descriptionText: "$device.displayName battery is ${battLevel}%", isStateChange: true] ) }
}

private Map getMotionResult(String value) {
	String motionType = value[0..1]
	String binaryValue = hexToBin(value[2..3])
	Integer sourceFace = Integer.parseInt(binaryValue[2..4],2)
	Integer targetFace = Integer.parseInt(binaryValue[5..7],2)

	if (motionType == "00") {
		switch(binaryValue[0..1]) {
			case "00": 
				if (targetFace==0) { shakeEvents() }
				break
			case "01": 
				flipEvents(targetFace, "90")
				break
			case "10": 
				flipEvents(targetFace, "180")
				break
		}
	} else if (motionType == "01") {
		slideEvents(targetFace)
	} else if (motionType == "02") {
		knockEvents(targetFace)
	}
}

private Map getRotationResult(value) {
	Integer angle = Math.round(Float.intBitsToFloat(Long.parseLong(value[0..7],16).intValue()));
	rotateEvents(angle)
}

def Map shakeEvents() {
	if (!settings.cubeMode || settings.cubeMode in ['0','2'] ) {
		sendEvent([ 
			name: "button", 
			value: "pushed", 
			data: [buttonNumber: 1, face: device.currentValue("face")], 
			descriptionText: (settings.cubeMode == '0') ? "$device.displayName was shaken" : null, 
			isStateChange: true,
			displayed: (settings.cubeMode == '0') ? true : false
	   ])
	} 

	if (settings.cubeMode in ['1','2'] ){
		sendEvent([ 
			name: "button", 
			value: "pushed", 
			data: [buttonNumber: (device.currentValue("face") as Integer) + ((settings.cubeMode == '1') ? 31 : 38), 
			face: device.currentValue("face")], 
			descriptionText: "$device.displayName was shaken (Face # ${device.currentValue("face")}).", 
			isStateChange: true])
	}
}

def flipEvents(Integer faceId, String flipType) {
	if (flipType == "0") {
		sendEvent( [name: 'face', value: -1, isStateChange: false] )
		sendEvent( [name: 'face', value: faceId, isStateChange: false] )
	} else if (flipType == "90") {
		if (settings.cubeMode in ['0','2']) {
			sendEvent( [
				name: 'button', 
				value: "pushed" , 
				data: [buttonNumber: 2, face: faceId], 
				descriptionText: (settings.cubeMode == '0') ? "$device.displayName detected $flipType degree flip" : null, 
				isStateChange: true,
				displayed: (settings.cubeMode == '0') ? true : false
			] )
		} 
	} else if (flipType == "180") {
		if (settings.cubeMode in ['0','2']) {
			sendEvent( [
				name: 'button', 
				value: "pushed" , 
				data: [buttonNumber: 3, face: faceId], 
				descriptionText: (settings.cubeMode == '0') ? "$device.displayName detected $flipType degree flip" : null, 
				isStateChange: true,
				displayed: (settings.cubeMode == '0') ? true : false
			] )
		} 
	}
	sendEvent( [name: 'face', value: faceId, isStateChange: true, displayed: false ] )
	if (settings.cubeMode in ['1','2']) {
		sendEvent( [
			name: "button", 
			value: "pushed", 
			data: [buttonNumber: faceId+((settings.cubeMode == '1') ? 1 : 8), face: faceId], 
			descriptionText: "$device.displayName was fliped to face # $faceId", 
			isStateChange: true
	   ] )
	}
	switch (faceId) {
		case 0: sendEvent( [ name: "threeAxis", value: "0,-1000,0", isStateChange: true, displayed: false] ); break
		case 1: sendEvent( [ name: "threeAxis", value: "-1000,0,0", isStateChange: true, displayed: false] ); break
		case 2: sendEvent( [ name: "threeAxis", value: "0,0,1000", isStateChange: true, displayed: false] ); break
		case 3: sendEvent( [ name: "threeAxis", value: "1000,0,0", isStateChange: true, displayed: false] ); break
		case 4: sendEvent( [ name: "threeAxis", value: "0,1000,0", isStateChange: true, displayed: false] ); break
		case 5: sendEvent( [ name: "threeAxis", value: "0,0,-1000", isStateChange: true, displayed: false] ); break
	}
}

def Map slideEvents(Integer targetFace) {
	if ( targetFace != device.currentValue("face") as Integer ) { log.info "Stale face data, updating."; setFace(targetFace) }
	if (!settings.cubeMode || settings.cubeMode in ['0','2'] ) {
		sendEvent( [ 
			name: "button", 
			value: "pushed", 
			data: [buttonNumber: 4, face: targetFace], 
			descriptionText: (settings.cubeMode == '0') ? "$device.displayName detected slide motion." : null,
			isStateChange: true, 
			displayed: (settings.cubeMode == '0') ? true : false
		]  )
	}

	if ( settings.cubeMode in ['1','2'] ) {
		sendEvent( [ 
			name: "button", 
			value: "pushed", 
			data: [buttonNumber: targetFace+((settings.cubeMode == '1') ? 7 : 14), face: targetFace], 
			descriptionText: "$device.displayName was slid with face # $targetFace up.", 
			isStateChange: true
		] ) }
}

def knockEvents(Integer targetFace) {
	if ( targetFace != device.currentValue("face") as Integer ) { log.info "Stale face data, updating."; setFace(targetFace) }
	if (!settings.cubeMode || settings.cubeMode in ['0','2'] ) {
		sendEvent( [ 
			name: "button", 
			value: "pushed", 
			data: [buttonNumber: 5, face: targetFace], 
			descriptionText: (settings.cubeMode == '0') ? "$device.displayName detected knock motion." : null,
			isStateChange: true, 
			displayed: (settings.cubeMode == '0') ? true : false
		] )
	}
	if ( settings.cubeMode in ['1','2'] ) {
		sendEvent( [ 
			name: "button", 
			value: "pushed", 
			data: [buttonNumber: targetFace+((settings.cubeMode == '1') ? 13 : 20), face: targetFace], 
			descriptionText: "$device.displayName was knocked with face # $targetFace up", 
			isStateChange: true
		] )
	 }
}

def rotateEvents(Integer angle) {
	sendEvent( [ name: "angle", value: angle, isStateChange: true, displayed: false] )
	if ( angle > 0 ) {
		if (!settings.cubeMode || settings.cubeMode in ['0','2'] ) {
			sendEvent( [ 
				name: "button", 
				value: "pushed", 
				data: [buttonNumber: 6, face: device.currentValue("face"), angle: angle], 
				descriptionText: (settings.cubeMode == '0') ? "$device.displayName was rotated right." : null, 
				isStateChange: true, 
				displayed: (settings.cubeMode == '0') ? true : false
			] )
		}
		if ( settings.cubeMode in ['1','2'] ) {
			sendEvent( [ 
				name: "button", 
				value: "pushed", 
				data: [buttonNumber: (device.currentValue("face") as Integer) + ((settings.cubeMode == '1') ? 19 : 26), face: device.currentValue("face")], 
				descriptionText: "$device.displayName was rotated right (Face # ${device.currentValue("face")}).", 
				isStateChange: true
			] )
		}
	} else {
		if (!settings.cubeMode || settings.cubeMode in ['0','2'] ) {
			sendEvent( [ 
				name: "button", 
				value: "pushed", 
				data: [buttonNumber: 7, face: device.currentValue("face"), angle: angle], 
				descriptionText: (settings.cubeMode == '0') ? "$device.displayName was rotated left." : null, 
				isStateChange: true, 
				displayed: (settings.cubeMode == '0') ? true : false
			] )
		}
		if ( settings.cubeMode in ['1','2'] ) {
			sendEvent( [ 
				name: "button", 
				value: "pushed", 
				data: [buttonNumber: (device.currentValue("face") as Integer) + ((settings.cubeMode == '1') ? 25 : 32), face: device.currentValue("face")], 
				descriptionText: "$device.displayName was rotated left (Face # ${device.currentValue("face")}).", 
				isStateChange: true
			] )
		}
	}
}

def configure() {
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	def configCmds = []
	return configCmds + refresh() // send refresh cmds as part of config
}

def enrollResponse() {
	log.debug "Sending enroll response"
}

def reset() {

}

def initialize() {
	sendState()
}

def poll() {
	//sendState() 
}

def sendState() {
	sendEvent(name: "numberOfButtons", value: 7)
}

def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	switch(settings.cubeMode) {
		case "1": sendEvent(name: "numberOfButtons", value: 36); break
		case "2": sendEvent(name: "numberOfButtons", value: 43); break
		default: sendEvent(name: "numberOfButtons", value: 7); break
	}
	state.lastUpdated = now()
}