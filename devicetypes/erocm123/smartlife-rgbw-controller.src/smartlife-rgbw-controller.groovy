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
 *  Date: 2016-12-10
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
        
        command "redOn"
        command "redOff"
        command "greenOn"
        command "greenOff"
        command "blueOn"
        command "blueOff"
        command "white1On"
        command "white1Off"
        command "white2On"
        command "white2Off"
        
 		command "setRedLevel"
        command "setGreenLevel"
        command "setBlueLevel"
        command "setWhite1Level"
        command "setWhite2Level"
	}

	simulator {
	}
    
    preferences {
        
        input("password", "password", title:"Password", required:false, displayDuringSetup:true)
        input("transition", "enum", title:"Default Transition", required:false, displayDuringSetup:true, options:
        [["true":"fade"],["false":"flash"]])
        input("channels", "boolean", title:"Mutually Exclusive RGB & White.\nOnly allow one or the other", required:false, displayDuringSetup:true)
        input("powerOnState", "enum", title:"Boot Up State", description: "State when power is applied", required: false, displayDuringSetup: false, options: [[0:"Off"],[1:"On"]/*,[2:"Previous State"]*/])
        
		input("color", "enum", title: "Default Color", required: false, multiple:false, value: "Previous", options: [
                    ["Previous":"Previous"],
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink","Cyan","Random","Custom"])
                    
        input "custom", "text", title: "Custom Color in Hex (ie ffffff)\r\nIf \"Custom\" is chosen above", submitOnChange: false, required: false
		
		
        input("level", "enum", title: "Default Level", required: false, value: 100, options: [[0:"Previous"],[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]])

        //input("override", "boolean", title:"Override detected IP Address", required: false, displayDuringSetup: false)
        //input("ip", "string", title:"IP Address", description: "192.168.1.150", required: false, displayDuringSetup: false)
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
        
        standardTile("red", "device.red", height: 1, width: 1, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"R", action:"redOn", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "on", label:"R", action:"redOff", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FF0000"
        }
        controlTile("redSliderControl", "device.redLevel", "slider", height: 1, width: 4, inactiveLabel: false) {
			state "redLevel", action:"setRedLevel"
		}
        valueTile("redValueTile", "device.redLevel", decoration: "flat", height: 1, width: 1) {
        	state "redLevel", label:'${currentValue}%'
        }     
        
        standardTile("green", "device.green", height: 1, width: 1, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"G", action:"greenOn", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "on", label:"G", action:"greenOff", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00FF00"
        }
        controlTile("greenSliderControl", "device.greenLevel", "slider", height: 1, width: 4, inactiveLabel: false) {
			state "greenLevel", action:"setGreenLevel"
		}
        valueTile("greenValueTile", "device.greenLevel", decoration: "flat", height: 1, width: 1) {
        	state "greenLevel", label:'${currentValue}%'
        }    
        
        standardTile("blue", "device.blue", height: 1, width:1, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"B", action:"blueOn", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "on", label:"B", action:"blueOff", icon:"st.illuminance.illuminance.bright", backgroundColor:"#0000FF"
        }
        controlTile("blueSliderControl", "device.blueLevel", "slider", height: 1, width: 4, inactiveLabel: false) {
			state "blueLevel", action:"setBlueLevel"
		}
        valueTile("blueValueTile", "device.blueLevel", decoration: "flat", height: 1, width: 1) {
        	state "blueLevel", label:'${currentValue}%'
        }  
        
        standardTile("white1", "device.white1", height: 1, width: 1, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"W1", action:"white1On", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "on", label:"W1", action:"white1Off", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        controlTile("white1SliderControl", "device.white1Level", "slider", height: 1, width: 4, inactiveLabel: false) {
			state "white1Level", action:"setWhite1Level"
		}
        valueTile("white1ValueTile", "device.white1Level", decoration: "flat", height: 1, width: 1) {
        	state "white1Level", label:'${currentValue}%'
        } 
        standardTile("white2", "device.white2", height: 1, width: 1, inactiveLabel: false, canChangeIcon: false) {
            state "off", label:"W2", action:"white2On", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "on", label:"W2", action:"white2Off", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        controlTile("white2SliderControl", "device.white2Level", "slider", height: 1, width: 4, inactiveLabel: false) {
			state "white2Level", action:"setWhite2Level"
		}
        valueTile("white2ValueTile", "device.white2Level", decoration: "flat", height: 1, width: 1) {
        	state "white2Level", label:'${currentValue}%'
        } 
        
        
        (1..6).each { n ->
			standardTile("switch$n", "switch$n", canChangeIcon: true, width: 2, height: 2) {
				state "off", label: "$n", action: "on$n", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
                state "on", label: "$n", action: "off$n", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			}
		}
    }

	main(["switch"])
	details(["switch", "levelSliderControl",
             "red", "redSliderControl", "redValueTile", 
             "green", "greenSliderControl", "greenValueTile",
             "blue", "blueSliderControl", "blueValueTile",
             "white1", "white1SliderControl", "white1ValueTile",
             "white2", "white2SliderControl", "white2ValueTile",
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
    def responses = []
    if (ip != null) state.dni = setDeviceNetworkId(ip, "80")
    state.hubIP = device.hub.getDataValue("localIP")
    state.hubPort = device.hub.getDataValue("localSrvPortTCP")
    responses << configureInstant(state.hubIP, state.hubPort, powerOnState)
    responses << configureDefault()
    return response(responses)
}

def configureDefault(){
    if(settings.color == "Previous") {
        return postAction("/config?dcolor=Previous")
    } else if(settings.color == "Random") {
        return postAction("/config?dcolor=f~${getHexColor(settings.color)}")
    } else if(settings.color == "Custom") {
        return postAction("/config?dcolor=f~${settings.custom}")
    } else if(settings.color == "Soft White" || settings.color == "Warm White") {
        if (settings.level == null || settings.level == "0") {
            return postAction("/config?dcolor=w~${getDimmedColor(getHexColor(settings.color), "100")}")
        } else {
            return postAction("/config?dcolor=w~${getDimmedColor(getHexColor(settings.color), settings.level)}")
        }
    } else {
        if (settings.level == null || settings.color == null){
           return postAction("/config?dcolor=Previous")
        } else if (settings.level == null || settings.level == "0") {
            return postAction("/config?dcolor=f~${getDimmedColor(getHexColor(settings.color), "100")}")
        } else {
            return postAction("/config?dcolor=f~${getDimmedColor(getHexColor(settings.color), settings.level)}")
        }
    }
}

def configureInstant(ip, port, pos){
    return postAction("/config?haip=${ip}&haport=${port}&pos=${pos}")
}

def parse(description) {
    def map = [:]
    def events = []
    def cmds = []
    
    if(description == "updated") return
    def descMap = parseDescriptionAsMap(description)
    
    if (!state.configSuccess || state.configSuccess == "false") cmds << configureInstant(device.hub.getDataValue("localIP"), device.hub.getDataValue("localSrvPortTCP"), powerOnState)
    
    if (!state.mac || state.mac != descMap["mac"]) {
		log.debug "Mac address of device found ${descMap["mac"]}"
        updateDataValue("mac", descMap["mac"])
	}
    if (state.mac != null && state.dni != state.mac) state.dni = setDeviceNetworkId(state.mac)
    
    def body = new String(descMap["body"].decodeBase64())
    log.debug body
    
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    if (result.containsKey("power")) {
        events << createEvent(name: "switch", value: result.power)
        toggleTiles("all")
    }
    if (result.containsKey("rgb")) {
       events << createEvent(name:"color", value:"#$result.rgb")
    }
    if (result.containsKey("r")) {
       events << createEvent(name:"redLevel", value: Integer.parseInt(result.r,16)/255 * 100 as Integer, displayed: false)
       if ((Integer.parseInt(result.r,16)/255 * 100 as Integer) > 0 ) {
          events << createEvent(name:"red", value: "on", displayed: false)
       } else {
    	  events << createEvent(name:"red", value: "off", displayed: false)
       }
    }
    if (result.containsKey("g")) {
       events << createEvent(name:"greenLevel", value: Integer.parseInt(result.g,16)/255 * 100 as Integer, displayed: false)
       if ((Integer.parseInt(result.g,16)/255 * 100 as Integer) > 0 ) {
          events << createEvent(name:"green", value: "on", displayed: false)
       } else {
    	  events << createEvent(name:"green", value: "off", displayed: false)
       }
    }
    if (result.containsKey("b")) {
       events << createEvent(name:"blueLevel", value: Integer.parseInt(result.b,16)/255 * 100 as Integer, displayed: false)
       if ((Integer.parseInt(result.b,16)/255 * 100 as Integer) > 0 ) {
          events << createEvent(name:"blue", value: "on", displayed: false)
       } else {
    	  events << createEvent(name:"blue", value: "off", displayed: false)
       }
    }
    if (result.containsKey("w1")) {
       events << createEvent(name:"white1Level", value: Integer.parseInt(result.w1,16)/255 * 100 as Integer, displayed: false)
       if ((Integer.parseInt(result.w1,16)/255 * 100 as Integer) > 0 ) {
          events << createEvent(name:"white1", value: "on", displayed: false)
       } else {
    	  events << createEvent(name:"white1", value: "off", displayed: false)
       }
    }
    if (result.containsKey("w2")) {
       events << createEvent(name:"white2Level", value: Integer.parseInt(result.w2,16)/255 * 100 as Integer, displayed: false)
       if ((Integer.parseInt(result.w2,16)/255 * 100 as Integer) > 0 ) {
          events << createEvent(name:"white2", value: "on", displayed: false)
       } else {
    	  events << createEvent(name:"white2", value: "off", displayed: false)
       }
    }

    if (result.containsKey("success")) {
       if (result.success == "true") state.configSuccess = "true" else state.configSuccess = "false" 
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
    //if (cmds != [] && events != null) return [events, response(cmds)] else if (cmds != []) return response(cmds) else return events
    return events
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
    postAction("/on?transition=$transition")
}

def off() {
	log.debug "off()"
    postAction("/off?transition=$transition")
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

    if ( !(value.hex) && ((value.saturation) || (value.hue))) {
        def hue = (value.hue != null) ? value.hue : 13
		def saturation = (value.saturation != null) ? value.saturation : 13
		def rgb = huesatToRGB(hue as Integer, saturation as Integer)
        value.hex = rgbToHex([r:rgb[0], g:rgb[1], b:rgb[2]])
    }
    
    if (value.hue == 23 && value.saturation == 56) {
       log.debug "setting color Soft White"
       def whiteLevel = getWhite(value.level)
       uri = "/w1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    }
    else if (value.hue == 52 && value.saturation == 19) {
       log.debug "setting color White"
       def whiteLevel = getWhite(value.level)
       uri = "/w1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    } 
    else if (value.hue == 53 && value.saturation == 91) {
       log.debug "setting color Daylight"
       def whiteLevel = getWhite(value.level)
       uri = "/w1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    } 
    else if (value.hue == 20 && value.saturation == 80) {
       log.debug "setting color Warm White"
       def whiteLevel = getWhite(value.level)
       uri = "/w1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    } 
    else if (value.colorTemperature) {
       log.debug "setting color with color temperature"
       def whiteLevel = getWhite(value.level)
       uri = "/w1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    }
	else if (value.hex) {
       log.debug "setting color with hex"
       def rgb = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
       def myred = rgb[0] < 40 ? 0 : rgb[0]
       def mygreen = rgb[1] < 40 ? 0 : rgb[1]
       def myblue = rgb[2] < 40 ? 0 : rgb[2]
       def dimmedColor = getDimmedColor(rgbToHex([r:myred, g:mygreen, b:myblue]))
       uri = "/rgb?value=${dimmedColor.substring(1)}"
    }
    else if (value.white) {
       uri = "/w1?value=${value.white}"
    }
    else if (value.aLevel) {
       uri = "/rgb?value=${getDimmedColor(device.currentValue("color")).substring(1)}"
    }
    else {
       // A valid color was not chosen. Setting to white
       uri = "/w1?value=ff"
    }
    
    if (uri != null) postAction("$uri&channels=$channels&transition=$transition")

}

private getDimmedColor(color, level) {
   if(color.size() > 2){
      def rgb = color.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
      def myred = rgb[0]
      def mygreen = rgb[1]
      def myblue = rgb[2]
    
      color = rgbToHex([r:myred, g:mygreen, b:myblue])
      def c = hexToRgb(color)
    
      def r = hex(c.r * (level.toInteger()/100))
      def g = hex(c.g * (level.toInteger()/100))
      def b = hex(c.b * (level.toInteger()/100))

      return "${r + g + b}"
   }else{
      color = Integer.parseInt(color, 16)
      return hex(color * (level.toInteger()/100))
   }

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

private encodeCredentials(username, password){
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    return userpass
}

private postAction(uri){ 
  log.debug "uri ${uri}"
  updateDNI()
  
  def userpass
  
  if(password != null && password != "") 
    userpass = encodeCredentials("admin", password)
    
  def headers = getHeader(userpass)
  
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
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
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

private getHeader(userpass = null){
    def headers = [:]
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
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

def redOn() {
	log.debug "redOn()"
    postAction("/r?value=ff&channels=$channels&transition=$transition")
}
def redOff() {
	log.debug "redOff()"
    postAction("/r?value=00&channels=$channels&transition=$transition")
}

def setRedLevel(value) {
	log.debug "setRedLevel: ${value}"
    def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	level = hex(level)
    postAction("/r?value=$level&channels=$channels&transition=$transition")
}
def greenOn() {
	log.debug "greenOn()"
    postAction("/g?value=ff&channels=$channels&transition=$transition")
}
def greenOff() {
	log.debug "greenOff()"
    postAction("/g?value=00&channels=$channels&transition=$transition")
}

def setGreenLevel(value) {
	log.debug "setGreenLevel: ${value}"
    def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	level = hex(level)
    postAction("/g?value=$level&channels=$channels&transition=$transition")
}
def blueOn() {
	log.debug "blueOn()"
    postAction("/b?value=ff&channels=$channels&transition=$transition")
}
def blueOff() {
	log.debug "blueOff()"
    postAction("/b?value=00&channels=$channels&transition=$transition")
}

def setBlueLevel(value) {
	log.debug "setBlueLevel: ${value}"
    def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	level = hex(level)
    postAction("/b?value=$level&channels=$channels&transition=$transition")
}
def white1On() {
	log.debug "white1On()"
    postAction("/w1?value=ff&channels=$channels&transition=$transition")
}
def white1Off() {
	log.debug "white1Off()"
    postAction("/w1?value=00&channels=$channels&transition=$transition")
}

def setWhite1Level(value) {
	log.debug "setwhite1Level: ${value}"
    def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	def whiteLevel = hex(level)
    postAction("/w1?value=$whiteLevel&channels=$channels&transition=$transition")
}
def white2On() {
	log.debug "white2On()"
    postAction("/w2?value=ff&channels=$channels&transition=$transition")
}
def white2Off() {
	log.debug "white2Off()"
    postAction("/w2?value=00&channels=$channels&transition=$transition")
}

def setWhite2Level(value) {
	log.debug "setwhite2Level: ${value}"
    def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	def whiteLevel = hex(level)
    postAction("/w2?value=$whiteLevel&channels=$channels&transition=$transition")
}

private getHexColor(value){
def color = ""
  switch(value){
    case "Previous":
    color = "Previous"
    break;
    case "White":
    color = "ffffff"
    break;
    case "Daylight":
    color = "ffffff"
    break;
    case "Soft White":
    color = "ff"
    break;
    case "Warm White":
    color = "ff"
    break;
    case "Blue":
    color = "0000ff"
    break;
    case "Green":
    color = "00ff00"
    break;
    case "Yellow":
    color = "ffff00"
    break;
    case "Orange":
    color = "ff5a00"
    break;
    case "Purple":
    color = "5a00ff"
    break;
    case "Pink":
    color = "ff00ff"
    break;
    case "Cyan":
    color = "00ffff"
    break;
    case "Red":
    color = "ff0000"
    break;
    case "Off":
    color = "000000"
    break;
    case "Random":
    color = "xxxxxx"
    break;
}
   return color
}