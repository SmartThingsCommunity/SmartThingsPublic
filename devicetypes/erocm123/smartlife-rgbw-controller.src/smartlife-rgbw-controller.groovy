/**
 *  Copyright 2016 Eric Maycock
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
 *  SmartLife RGBW Controller
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2016-04-30
 */

import groovy.json.JsonSlurper

metadata {
	definition (name: "SmartLife RGBW Controller", namespace: "erocm123", author: "Eric Maycock") {
		capability "Switch Level"
		capability "Actuator"
        capability "Color Control"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        
        (1..6).each { n ->
			attribute "switch$n", "enum", ["on", "off"]
			command "on$n"
			command "off$n"
		}
        
        command "reset"
        command "setProgram"
        command "setWhiteLevel"

	}

	simulator {
	}
    
    preferences {
        input("ip", "string", title:"IP Address", description: "192.168.1.150", required: false, displayDuringSetup: true)
        //input("port", "string", title:"Port", description: "88", defaultValue: "88" , required: true, displayDuringSetup: true)
        //input("deviceId", "string", title:"Device ID", description: "99", defaultValue: "99" , required: true, displayDuringSetup: true)
	}

	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
        }

		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("configure", "device.configure", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        controlTile("whiteSliderControl", "device.whiteLevel", "slider", height: 2, width: 4, inactiveLabel: false) {
			state "whiteLevel", action:"setWhiteLevel"
		}
        valueTile("whiteValueTile", "device.whiteLevel", decoration: "flat", height: 2, width: 2) {
        	state "whiteLevel", label:'${currentValue}%', backgroundColor:"#FFFFFF"
        } 
        (1..6).each { n ->
			standardTile("switch$n", "switch$n", canChangeIcon: true, width: 2, height: 2) {
				state "on", label: "$n", action: "off$n", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				state "off", label: "$n", action: "on$n", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}
    }

	main(["switch"])
	details(["switch", "levelSliderControl",
             "whiteSliderControl", "whiteValueTile",
             "switch1", "switch2", "switch3",
             "switch4", "switch5", "switch6",
             "refresh", "configure" ])
}

def installed() {
	log.debug "installed()"
	configure()
}

def updated() {
	log.debug "updated()"
    configure()
}

def configure() {
	log.debug "configure()"
	log.debug "Configuring Device For SmartThings Use"
    state.program1 = "l-0000ff100,l-ff0000100&repeat=-1"
    state.program2 = "f-00ff004000,f-ff00004000,f-0000ff4000&repeat=5"
    state.program3 = "f-0000ff2000,f-ff00002000&repeat=10"
    state.program4 = null
    state.program5 = null
    state.program6 = null
    if (ip != null) state.dni = setDeviceNetworkId(ip, "80")
    state.hubIP = device.hub.getDataValue("localIP")
    state.hubPort = device.hub.getDataValue("localSrvPortTCP")
    response(configureInstant(state.hubIP, state.hubPort))
}

def configureInstant(ip, port){
    return [postAction("/config?haip=${ip}&haport=${port}")]
}

def parse(description) {
	//log.debug "Parsing: ${description}"
    def map = [:]
    def events = []
    def descMap = parseDescriptionAsMap(description)
    //log.debug "descMap: ${descMap}"
    
    if (!state.mac || state.mac != descMap["mac"]) {
		log.debug "Mac address of device found ${descMap["mac"]}"
        updateDataValue("mac", descMap["mac"])
	}
    if (state.mac != null && state.dni != state.mac) state.dni = setDeviceNetworkId(state.mac)
    
    def body = new String(descMap["body"].decodeBase64())
    log.debug body
    
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    //log.debug "result: ${result}"
    
    if (result.containsKey("power")) {
        events << createEvent(name: "switch", value: result.power)
        toggleTiles("all")
    }
    if (result.containsKey("color")) {
       if (result.color.size() > 2) {
          events << createEvent(name:"color", value:"$result.color")
          events << createEvent(name: "whiteLevel", value: 0)
       } else {
          events << createEvent(name: "whiteLevel", value: Integer.parseInt(result.color,16)/255 * 100 as Integer)
       }
    }
    if (result.containsKey("white1")) {
       events << createEvent(name: "whiteLevel", value: Integer.parseInt(result.white1,16)/255 * 100 as Integer)
    }
    if (result.containsKey("program")) {
        if (result.running == "false") {
            toggleTiles("all")
        }
        else {
            toggleTiles("switch$result.program")
            events << createEvent(name:"switch$result.program", value: "on")
        }
    }
    if (events != null) return events
}

private toggleTiles(value) {
   def tiles = ["switch1", "switch2", "switch3", "switch4", "switch5", "switch6"]
   tiles.each {tile ->
      if (tile != value) sendEvent(name: tile, value: "off")
   }
}

private getScaledColor(color) {
   def rgb = color.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
   def maxNumber = 1
   for (int i = 0; i < 3; i++){
     if (rgb[i] > maxNumber) {
	    maxNumber = rgb[i]
     }
   }
   def scale = 255/maxNumber
   for (int i = 0; i < 3; i++){
     rgb[i] = rgb[i] * scale
   }
   def myred = rgb[0]
   def mygreen = rgb[1]
   def myblue = rgb[2]
   return rgbToHex([r:myred, g:mygreen, b:myblue])
}

def on() {
	log.debug "on()"
    if (state.previousColor != null && state.previousColor != "000000") {
       if(state.previousColor.size() == 2) setColor(white: "$state.previousColor")
       else setColor(hex: "#$state.previousColor")
    }
    else {
       setColor(white: "ff")
    }
}

def off() {
	log.debug "off()"
    postAction("/off")
}

def setLevel(level) {
	setLevel(level, 1)
}

def setLevel(level, duration) {
	log.debug "setLevel() level = ${level}"
     if(level > 100) level = 100
     if (level == 0) { off() }
	 else if (device.latestValue("switch") == "off") { on() }
	sendEvent(name: "level", value: level)
    sendEvent(name: "setLevel", value: level, displayed: false)
	setColor(aLevel: level)
}
def setSaturation(percent) {
	log.debug "setSaturation($percent)"
	setColor(saturation: percent)
}
def setHue(value) {
	log.debug "setHue($value)"
	setColor(hue: value)
}
def getWhite(value) {
	log.debug "getWhite($value)"
	def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	return hex(level)
}
def setColor(value) {
    log.debug "setColor being called with ${value}"
    def uri

    if ( !(value.hex) && (value.saturation) && (value.hue)) {
		def rgb = huesatToRGB(value.hue as Integer, value.saturation as Integer)
        value.hex = rgbToHex([r:rgb[0], g:rgb[1], b:rgb[2]])
    } 
    if (value.hue == 23 && value.saturation == 56) {
       log.debug "setting color Soft White"
       def whiteLevel = getWhite(value.level)
       uri = "/white1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    }
    else if (value.hue == 52 && value.saturation == 19) {
       log.debug "setting color White"
       def whiteLevel = getWhite(value.level)
       uri = "/white1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    } 
    else if (value.hue == 53 && value.saturation == 91) {
       log.debug "setting color Daylight"
       def whiteLevel = getWhite(value.level)
       uri = "/white1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    } 
    else if (value.hue == 20 && value.saturation == 80) {
       log.debug "setting color Warm White"
       def whiteLevel = getWhite(value.level)
       uri = "/white1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    } 
    else if (value.colorTemperature) {
       log.debug "setting color with color temperature"
       def whiteLevel = getWhite(value.level)
       uri = "/white1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    }
	else if (value.hex) {
       log.debug "setting color with hex"
       def rgb = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
       //def myred = rgb[0] >=128 ? 255 : 0
       //def mygreen = rgb[1] >=128 ? 255 : 0
       //def myblue = rgb[2] >=128 ? 255 : 0
       def myred = rgb[0] < 40 ? 0 : rgb[0]
       def mygreen = rgb[1] < 40 ? 0 : rgb[1]
       def myblue = rgb[2] < 40 ? 0 : rgb[2]
       //def rgb = hexToRgb(getScaledColor(value.hex.substring(1)))
       //def myred = rgb.r
       //def mygreen = rgb.g
       //def myblue = rgb.b
       def dimmedColor = getDimmedColor(rgbToHex([r:myred, g:mygreen, b:myblue]))
       uri = "/color?value=${dimmedColor.substring(1)}"
       state.previousColor = "${dimmedColor.substring(1)}"
    }
    else if (value.white) {
       uri = "/white1?value=${value.white}"
       state.previousColor = "${value.white}"
    }
    else if (value.aLevel) {
       uri = "/color?value=${getDimmedColor(state.previousColor).substring(1)}"
       state.previousColor = "${getDimmedColor(state.previousColor).substring(1)}"
    }
    else {
       // A valid color was not chosen. Setting to white
       uri = "/white1?value=ff"
       state.previousColor = "ff"
    }
    
    if (uri != null) postAction(uri)

}

private getDimmedColor(color) {
   if (device.latestValue("level")) {
      def newLevel = device.latestValue("level")
      def colorHex = getScaledColor(color)
      def rgb = colorHex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
      def myred = rgb[0]
      def mygreen = rgb[1]
      def myblue = rgb[2]
    
      colorHex = rgbToHex([r:myred, g:mygreen, b:myblue])
      def c = hexToRgb(colorHex)
    
      def r = hex(c.r * (newLevel/100))
      def g = hex(c.g * (newLevel/100))
      def b = hex(c.b * (newLevel/100))

      return "#${r + g + b}"
   } else {
      return color
   }
}

def reset() {
	log.debug "reset()"
	setColor(white: "ff")
}
def refresh() {
	log.debug "refresh()"
    postAction("/status")
}

def setWhiteLevel(value) {
	log.debug "setwhiteLevel: ${value}"
    def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	if ( value > 0 ) {
    	if (device.latestValue("switch") == "off") { on() }
        sendEvent(name: "white", value: "on")
    } else {
    	sendEvent(name: "white", value: "off")
    }
	def whiteLevel = hex(level)
    setColor(white: whiteLevel)
}

def hexToRgb(colorHex) {
	def rrInt = Integer.parseInt(colorHex.substring(1,3),16)
    def ggInt = Integer.parseInt(colorHex.substring(3,5),16)
    def bbInt = Integer.parseInt(colorHex.substring(5,7),16)
    
    def colorData = [:]
    colorData = [r: rrInt, g: ggInt, b: bbInt]
    colorData
}

def huesatToRGB(float hue, float sat) {
	while(hue >= 100) hue -= 100
	int h = (int)(hue / 100 * 6)
	float f = hue / 100 * 6 - h
	int p = Math.round(255 * (1 - (sat / 100)))
	int q = Math.round(255 * (1 - (sat / 100) * f))
	int t = Math.round(255 * (1 - (sat / 100) * (1 - f)))
	switch (h) {
		case 0: return [255, t, p]
		case 1: return [q, 255, p]
		case 2: return [p, 255, t]
		case 3: return [p, q, 255]
		case 4: return [t, p, 255]
		case 5: return [255, p, q]
	}
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}
def rgbToHex(rgb) {
    def r = hex(rgb.r)
    def g = hex(rgb.g)
    def b = hex(rgb.b)
    def hexColor = "#${r}${g}${b}"
    
    hexColor
}

def sync(ip, port) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    if (ip && ip != existingIp) {
        updateDataValue("ip", ip)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
    }
}

private postAction(uri){ 
  log.debug "uri ${uri}"
  updateDNI()
  def headers = getHeader()
  
  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  )
  hubAction    
}

private setDeviceNetworkId(ip, port = null){
    def myDNI
    if (port == null) {
        myDNI = ip
    } else {
  	    def iphex = convertIPtoHex(ip)
  	    def porthex = convertPortToHex(port)
        
        myDNI = "$iphex:$porthex"
    }
    log.debug "Device Network Id set to ${myDNI}"
    return myDNI
}

private updateDNI() { 
    if (device.deviceNetworkId != state.dni) {
        device.deviceNetworkId = state.dni
    }
}

private getHostAddress() {
    if(getDeviceDataByName("ip") && getDeviceDataByName("port")){
        return "${getDeviceDataByName("ip")}:${getDeviceDataByName("port")}"
    }else{
	    return "${ip}:80"
    }
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private getHeader(){
    def headers = [:]
    headers.put("HOST", getHostAddress())
    return headers
}

def toAscii(s){
        StringBuilder sb = new StringBuilder();
        String ascString = null;
        long asciiInt;
                for (int i = 0; i < s.length(); i++){
                    sb.append((int)s.charAt(i));
                    sb.append("|");
                    char c = s.charAt(i);
                }
                ascString = sb.toString();
                asciiInt = Long.parseLong(ascString);
                return asciiInt;
}

def on1() { onOffCmd(1, 1) }
def on2() { onOffCmd(1, 2) }
def on3() { onOffCmd(1, 3) }
def on4() { onOffCmd(1, 4) }
def on5() { onOffCmd(1, 5) }
def on6() { onOffCmd(1, 6) }

def off1(p=null) { onOffCmd((p == null ? 0 : p), 1) }
def off2(p=null) { onOffCmd((p == null ? 0 : p), 2) }
def off3(p=null) { onOffCmd((p == null ? 0 : p), 3) }
def off4(p=null) { onOffCmd((p == null ? 0 : p), 4) }
def off5(p=null) { onOffCmd((p == null ? 0 : p), 5) }
def off6(p=null) { onOffCmd((p == null ? 0 : p), 6) }

def onOffCmd(value, program) {
    log.debug "onOffCmd($value, $program)"
    def uri
    if (value == 1){
       if(state."program${program}" != null) {
          uri = "/program?value=${state."program${program}"}&number=$program"
       }    
    } else if(value == 0){
       uri = "/stop"
    } else {
       uri = "/off"
    }
    if (uri != null) return postAction(uri)
}

def setProgram(value, program){
   state."program${program}" = value
}

def hex2int(value){
   return Integer.parseInt(value, 10)
}