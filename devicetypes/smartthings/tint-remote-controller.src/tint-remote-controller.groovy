/**
 *  Copyright 2019 SmartThings
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
	definition (name: "Tint Remote Controller", namespace: "smartthings", author: "SmartThings", mcdSync: true, ocfDeviceType: "x.com.st.d.remotecontroller") {

		capability "Actuator"
		capability "Color Control"
		capability "Color Temperature"
		capability "Configuration"
		capability "Switch"
		capability "Switch Level"
		capability "Health Check"
		capability "Light"

		// Muller Licht
        fingerprint inClusters: "0000, 0003, 1000", outClusters: "0003, 0004, 0006, 0008, 0300, 1000, 0019, 0000", manufacturer: "MLI",  model: "ZBT-Remote-ALL-RGBW", deviceJoinName: "Tint Remote Control"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"color control.setColor"
			}
		}
		controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2700..6500)") {
			state "colorTemperature", action:"color temperature.setColorTemperature"
		}
		valueTile("colorName", "device.colorName", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "colorName", label: '${currentValue}'
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		details(["switch", "colorTempSliderControl", "colorName", "refresh"])
	}
}

private getGROUPS_CLUSTER() { 0x0004 }
private getADD_GROUP_COMMAND() { 0x00 }
private getMOVE_TO_COLOR_COMMAND() { 0x07 }
private getON_OFF_ATTRIBUTE() { 0x0000 }
private getMOVE_TO_COLOR_TEMPERATURE_COMMAND() { 0x0A }
private getSTEP_COMMAND() { 0x02 }
private getWRITE_ATTRIBUTES_COMMAND() { 0x02 }

def parse(String description) {
    def event = zigbee.getEvent(description)
	def result = []
    if (event) {
        log.debug "Creating event: ${event}"
       result = createEvent(event)
    } else {
        if (isAttrOrCmdMessage(description)) {
            result = parseAttrCmdMessage(description)
        } else {
            log.warn "Unhandled message came in"
        }
    }
	log.debug "Parse returned: ${result}"
	return result
}

private parseAttrCmdMessage(description) {
    def descMap = zigbee.parseDescriptionAsMap(description)
    if (descMap.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.attrInt == BATTERY_VOLTAGE_ATTRIBUTE) {
        getBatteryEvent(zigbee.convertHexToInt(descMap.value))
    } else if (descMap.clusterInt == zigbee.ONOFF_CLUSTER) {
        getOnOffEvent()
    } else if (descMap.clusterInt == zigbee.COLOR_CONTROL_CLUSTER && descMap.commandInt == MOVE_TO_COLOR_COMMAND) {
        getColorEvent(descMap)
    } else if (descMap.clusterInt == zigbee.COLOR_CONTROL_CLUSTER && descMap.commandInt == MOVE_TO_COLOR_TEMPERATURE_COMMAND) {
		getColorTemperatureEvent(descMap)
	} else if (descMap.clusterInt == zigbee.LEVEL_CONTROL_CLUSTER && descMap.commandInt == STEP_COMMAND) {
		getLevelEvent(descMap)
	} else if (descMap.clusterInt == zigbee.BASIC_CLUSTER && descMap.commandInt == WRITE_ATTRIBUTES_COMMAND) {
		getButtonEvent(descMap)
	} else {
		log.debug "Unhandled message came in, its description map: ${descMap}"
	}
}

private isAttrOrCmdMessage(description) {
    (description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))
}

private getOnOffEvent() {
    state.onOffValue = (state.onOffValue == "off" ? "on" : "off")
    createEvent(name: "switch", value: state.onOffValue)
}

private getColorEvent(descMap) {
    def CurrentX = Integer.parseInt(descMap.data[1] + descMap.data[0], 16)
    def CurrentY = Integer.parseInt(descMap.data[3] + descMap.data[2], 16)
    def HS = color_xyYtoHSV(CurrentX, CurrentY)
	return [
		createEvent(name: "hue", value: HS.H * 100),
		createEvent(name: "saturation", value: 100)
	]
}

private getColorTemperatureEvent(descMap) {
	def miredTemp = Integer.parseInt(descMap.data[1] + descMap.data[0], 16)
	def colorTemperature = 1_000_000 / miredTemp
	colorTemperature = colorTemperature < 2750 ? 2700 : (colorTemperature > 6500 ? 6500 : colorTemperature)
	return createEvent(name: "colorTemperature", value: colorTemperature)
}

private getLevelEvent(descMap) {
	def diff = descMap.data[0] == "00" ? 10 : -10 //0x00 Level up / 0x01 Level down
	state.currentLevel = state.currentLevel + diff
	state.currentLevel = Math.min(state.currentLevel, 100)
	state.currentLevel = Math.max(state.currentLevel, 1)
	return createEvent(name: "level", value: state.currentLevel)
}

private getButtonEvent(descMap) {
	def childId = getButtonNumber(descMap.data[3])
	def description = "Button no. ${childId} was ${value}"
	def event = createEvent(name: "button", value: "pushed", descriptionText: description, data: [buttonNumber: childId], isStateChange: true)
	sendEventToChild(childId, event)
	return event
}

def on() {
	state.onOffValue = "on"
	createEvent(name: "switch", value: "on")
}

def off() {
	state.onOffValue = "off"
	createEvent(name: "switch", value: "off")
}

def ping() {
	refresh()
}

def refresh() {
	return zigbee.readAttribute(zigbee.ONOFF_CLUSTER, ON_OFF_ATTRIBUTE)
}

def configure() {
	def groupsList = [0x4004, 0x4005, 0x4006]
		groupsList.each {
			sendHubCommand(addHubToGroup(it))
		}
	sendEvent(name: "checkInterval", value: (2 * 60 + 10) * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	return zigbee.onOffConfig(5, 3600)
}

def setColorTemperature(value) {
	sendEvent(name: "colorTemperature", value: value)
}

def setLevel(value) {
	state.currentLevel = value
	sendEvent(name: "level", value: state.currentLevel)
}

def setColor(value){
	sendEvent(name: "hue", value: value.hue)
	sendEvent(name: "saturation", value: value.saturation)
}

def installed() {
	def numberOfButtons = 6
	sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
	addChildButtons(numberOfButtons)
	state.currentLevel = 100
	sendEvent(name: "level", value: 100, displayed: false)
	state.onOffValue = "off"
	createEvent(name: "switch", value: "off", displayed: false)
	createEvent(name: "colorTemperature", value: 2700, displayed: false)
	sendEvent(name: "saturation", value: 100, descriptionText: "Color has changed", displayed: false)
}

private color_xyYtoHSV(CurrentX, CurrentY) { 
    // Sources:
    // https://en.wikipedia.org/wiki/CIE_1931_color_space
    // http://wiki.nuaj.net/index.php/Color_Transforms
    // Note: X, Y, Z are tristimulus values, where CurrentX and CurrentY are Zigbee'ss own representation of normalized x and y
    def x = CurrentX / 65536
    def y = CurrentY / 65536
    def z = 1 - x - y
    def Y = 1
    def X = (Y / y) * x
    def Z = (Y / y) * z
    def RGB = colorXYZtoRGB(X, Y, Z)
    colorRGBtoHSV(RGB)
}

private colorXYZtoRGB(X, Y, Z) {
    def R = X * 3.2406 + Y * -1.5372 + Z * -0.4986
    def G = X * -0.9689 + Y * 1.8758 + Z * 0.0415
    def B = X * 0.0557 + Y * -0.2040 + Z * 1.0570
	R = R > 0.0031308 ? 1.055*Math.pow(R, 1/2.4) - 0.055 : 12.92*R;
    G = G > 0.0031308 ? 1.055*Math.pow(G, 1/2.4) - 0.055 : 12.92*G;
    B = B > 0.0031308 ? 1.055*Math.pow(B, 1/2.4) - 0.055 : 12.92*B;
    clipAndScaleRGBifNedded(R, G, B)
}

private clipAndScaleRGBifNedded(R, G, B) {
	def max = [R, G, B].max()
	R = R / max
	G = G / max
	B = B / max
	R = R < 0 ? 0 : R
	G = G < 0 ? 0 : G
	B = B < 0 ? 0 : B
	[R: R, G: G, B: B]
}

private colorRGBtoHSV(RGB) {
    def min = RGB.values().min()
    def max = RGB.values().max()
    def delta = max - min
    def H, S
    if (delta == 0) {
        H = 0
        S = 0
    } else {
        S = delta / max
        def delta_R = ( ( ( max - RGB.R ) / 6 ) + ( max / 2 ) ) / max
        def delta_G = ( ( ( max - RGB.G ) / 6 ) + ( max / 2 ) ) / max
        def delta_B = ( ( ( max - RGB.B ) / 6 ) + ( max / 2 ) ) / max
        if (RGB.R == max) {
            H = delta_B - delta_G
        } else if (RGB.G == max) {
            H = ( 1 / 3 ) + delta_R - delta_B
        } else if (RGB.B == max) {
            H = ( 2 / 3 ) + delta_G - delta_R
        }

        if (H < 0) {
            H += 1
        } else if (H > 1) {
            H -= 1
        }
    }
    [H: H, S: S]
}

private addChildButtons(numberOfButtons) {
	for(def endpoint : 1..numberOfButtons) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"
			def componentLabel = (device.displayName.endsWith(' 1') ? device.displayName[0..-2] : (device.displayName + " ")) + "${endpoint}"
			def child = addChildDevice("Child Button", childDni, device.getHub().getId(), [
					completedSetup: true,
					label         : componentLabel,
					isComponent   : true,
					componentName : "button$endpoint",
					componentLabel: "Button $endpoint"
			])
			child.sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)
		} catch(Exception e) {
			log.warn "Exception during child devices initialization: ${e}"
		}
	}
}

def sendEventToChild(buttonNumber, event) {
	String childDni = "${device.deviceNetworkId}:$buttonNumber"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	child?.sendEvent(event)
}

private addHubToGroup(groupAddr) {
	def cmd = ["st cmd 0x0000 0x01 ${GROUPS_CLUSTER} 0x00 {${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr,4))} 00}", "delay 200"]
	cmd += zigbee.command(GROUPS_CLUSTER, ADD_GROUP_COMMAND, "${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr, 4))} 00")
	return cmd
}

private isTintRemoteController() {
	(device.getDataValue("manufacturer") == "MLI") && (device.getDataValue("model") == "ZBT-Remote-ALL-RGBW")
}

private getButtonNumber(data) {
	def mapping = [
		"03" : 1,
		"01" : 2,
		"02" : 3,
		"06" : 4,
		"04" : 5,
		"05" : 6
	]
	return mapping[data]
}

private getSupportedButtonValues() {
	return ["pushed"]
}