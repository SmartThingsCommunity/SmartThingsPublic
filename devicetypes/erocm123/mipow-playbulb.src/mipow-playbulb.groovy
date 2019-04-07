/**
 *  Copyright 2015 Eric Maycock
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
 *  MiPow PlayBulb
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2016-01-27
 */

import groovy.json.JsonSlurper

metadata {
	definition (name: "MiPow PlayBulb", namespace: "erocm123", author: "Eric Maycock", vid:"generic-rgbw-color-bulb") {
		capability "Switch Level"
		capability "Actuator"
        capability "Color Control"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        
        command "setWhiteLevel"
        command "fadeOn"
        command "flashOn"
        command "rainbowfadeOn"
        command "rainbowflashOn"
        command "candleOn"
        command "fadeOff"
        command "flashOff"
        command "rainbowfadeOff"
        command "rainbowflashOff"
        command "candleOff"
        
        command "reset"

	}

	simulator {
	}
    
    preferences {
        input("ip", "string", title:"IP Address", description: "10.37.20.150", defaultValue: "10.37.20.150" ,required: true, displayDuringSetup: true)
        input("port", "string", title:"Port", description: "88", defaultValue: "88" , required: true, displayDuringSetup: true)
        input("deviceId", "string", title:"Device ID", description: "99", defaultValue: "99" , required: true, displayDuringSetup: true)
	}

	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
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
        standardTile("fade", "device.fade", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
            state "off", label:"Fade", action:"fadeOn", icon:"st.illuminance.illuminance.dark", backgroundColor:"#cccccc"
            state "on", label:"Fade", action:"fadeOff", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00a0dc"
        }
        standardTile("flash", "device.flash", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
            state "off", label:"Flash", action:"flashOn", icon:"st.illuminance.illuminance.dark", backgroundColor:"#cccccc"
            state "on", label:"Flash", action:"flashOff", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00a0dc"
        }
        standardTile("candle", "device.candle", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
            state "off", label:"Candle", action:"candleOn", icon:"st.illuminance.illuminance.dark", backgroundColor:"#cccccc"
            state "on", label:"Candle", action:"candleOff", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00a0dc"
        }
        standardTile("rainbowfade", "device.rainbowfade", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
            state "off", label:"Rainbow\nFade", action:"rainbowfadeOn", icon:"st.illuminance.illuminance.dark", backgroundColor:"#cccccc"
            state "on", label:"Rainbow\nFade", action:"rainbowfadeOff", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00a0dc"
        }
        standardTile("rainbowflash", "device.rainbowflash", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
            state "off", label:"Rainbow\nFlash", action:"rainbowflashOn", icon:"st.illuminance.illuminance.dark", backgroundColor:"#cccccc"
            state "on", label:"Rainbow \nFlash", action:"rainbowflashOff", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00a0dc"
        }
    }

	main(["switch"])
	details(["switch", "levelSliderControl",
             "whiteSliderControl", "whiteValueTile",
             "fade", "flash", "candle",
             "rainbowflash", "rainbowfade", "refresh" ])
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
    state.previousColor="00ffffff"
    state.previousEffect="00000000"
    if (ip != null && port != null) state.dni = setDeviceNetworkId(ip, port)
}

def parse(description) {
	//log.debug "Parsing: ${description}"
    def map = [:]
    def descMap = parseDescriptionAsMap(description)
    //log.debug "descMap: ${descMap}"
    
    if (!state.MAC || state.MAC != descMap["mac"]) {
		log.debug "Mac address of device found ${descMap["mac"]}"
        updateDataValue("MAC", descMap["mac"])
	}
    
    def body = new String(descMap["body"].decodeBase64())
    
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    log.debug "result: ${result}"
    
    if (result.containsKey("power")) {
        sendEvent(name: "switch", value: result.power)
    }
    if (result.containsKey("color")) {
       if (result.color.substring(0,2) != "00") {
          sendEvent(name: "color", value: "#ffffff")
          sendEvent(name: "whiteLevel", value: Integer.parseInt(result.color.substring(0,2),16)/255 * 100 as Integer)
       } else {
          sendEvent(name: "color", value: getScaledColor(result.color.substring(2)))
          sendEvent(name: "whiteLevel", value: 0)
       } 
       //toggleTiles("all")
    }
    if (result.containsKey("effect")) {
        if (result.effect == "00000000") {
            toggleTiles("all")
        }
        else {
            toggleTiles(getEffectName(result.effect))
		    sendEvent(name: getEffectName(result.effect), value: "on")
        }
        if (result.effect == "ff000100") {
           state.previousEffect="00000000"
        } else {
           state.previousEffect="${result.effect}"
        }
    }
}

private toggleTiles(value) {
   def tiles = ["fade", "flash", "rainbowfade", "rainbowflash", "candle"]
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

private getEffectName(effect) {
	switch (effect) {
		case "01000f0f": return "fade"; break
		case "00000f0f": return "flash"; break
		case "03000f0f": return "rainbowfade"; break
		case "02000f0f": return "rainbowflash"; break
        case "04000100": return "candle"; break
	}
}

def on() {
	log.debug "on()"
    if (state.previousColor != "00000000" || state.previousEffect != "00000000") {
       if (state.previousEffect != "00000000") {
          setColor(effect: state.previousEffect)
       }
       else {
          setColor(pow: state.previousColor)
          //setColor(pow: "ff000000")
       }
    }
    else {
       setColor(white: "ff")
    }
}

def off() {
	log.debug "off()"
	def uri = "/playbulb.php?device=${deviceId}&setting=off"
    postAction(uri)
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
    if ( value.level > 1 ) {
		sendEvent("name":"level", "value": value.level)
    } 

    if (value.hue == 5 && value.saturation == 4) {
       log.debug "setting color Soft White"
       def whiteLevel = getWhite(value.level)
       if (state.previousEffect != "00000000") {
          uri = "/playbulb.php?device=${deviceId}&setting=${whiteLevel}000000${state.previousEffect}"
       } else {
          uri = "/playbulb.php?device=${deviceId}&setting=${whiteLevel}000000"
       }
       state.previousColor = "${whiteLevel}000000"
    }
    else if (value.hue == 79 && value.saturation == 7) {
       log.debug "setting color White"
       def whiteLevel = getWhite(value.level)
       if (state.previousEffect != "00000000") {
          uri = "/playbulb.php?device=${deviceId}&setting=${whiteLevel}000000${state.previousEffect}"
       } else {
          uri = "/playbulb.php?device=${deviceId}&setting=${whiteLevel}000000"
       }
       state.previousColor = "${whiteLevel}000000"
    } 
    else if (value.hue == 53 && value.saturation == 91) {
       log.debug "setting color Daylight"
       def whiteLevel = getWhite(value.level)
       if (state.previousEffect != "00000000") {
          uri = "/playbulb.php?device=${deviceId}&setting=${whiteLevel}000000${state.previousEffect}"
       } else {
          uri = "/playbulb.php?device=${deviceId}&setting=${whiteLevel}000000"
       }
       state.previousColor = "${whiteLevel}000000"
    } 
    else if (value.hue == 20 && value.saturation == 80) {
       log.debug "setting color Warm White"
       def whiteLevel = getWhite(value.level)
       if (state.previousEffect != "00000000") {
          uri = "/playbulb.php?device=${deviceId}&setting=${whiteLevel}000000${state.previousEffect}"
       } else {
          uri = "/playbulb.php?device=${deviceId}&setting=${whiteLevel}000000"
       }
       state.previousColor = "${whiteLevel}000000"
    } 
    else if (value.colorTemperature) {
       log.debug "setting color with color temperature"
	   def whiteLevel = getWhite(value.level)
       if (state.previousEffect != "00000000") {
          uri = "/playbulb.php?device=${deviceId}&setting=${whiteLevel}000000${state.previousEffect}"
       } else {
          uri = "/playbulb.php?device=${deviceId}&setting=${whiteLevel}000000"
       }
       state.previousColor = "${whiteLevel}000000"
    }
	else if (value.pow) {
       log.debug "setting color with MiPow setting"
       uri = "/playbulb.php?device=${deviceId}&setting=${value.pow}"
       state.previousColor = "${value.pow}"
    }
	else if (value.hex) {
       log.debug "setting color with hex"
       def rgb = hexToRgb(getScaledColor(value.hex.substring(1)))
       def myred = rgb.r
       def mygreen = rgb.g
       def myblue = rgb.b
       def dimmedColor = getDimmedColor(rgbToHex([r:myred, g:mygreen, b:myblue]))
       if (state.previousEffect == "00000000") {
          uri = "/playbulb.php?device=${deviceId}&setting=00${dimmedColor.substring(1)}"
       } else {
          if (state.previousEffect != "01000f0f") {
             uri = "/playbulb.php?device=${deviceId}&setting=00${dimmedColor.substring(1) + state.previousEffect}"
          } else {
             uri = "/playbulb.php?device=${deviceId}&setting=00${rgbToHex([r:myred, g:mygreen, b:myblue]).substring(1) + state.previousEffect}"
          }
       }
       
       state.previousColor = "00${dimmedColor.substring(1)}"
    }
    else if (value.white) {
       if (state.previousEffect != "00000000") {
          uri = "/playbulb.php?device=${deviceId}&setting=${value.white}000000${state.previousEffect}"
       } else {
          uri = "/playbulb.php?device=${deviceId}&setting=${value.white}000000"
       }
       state.previousColor = "${value.white}000000"
    }
    else if (value.effect) {
       log.debug "setting effect with ${value.effect}"
       def effectColor = state.previousColor
       log.debug "${effectColor}"
       if (value.effect == "01000f0f") {
          //Fade function only works with set colors
          if (state.previousColor.substring(0,2) == "00") {
             def rgb = getScaledColor(state.previousColor.substring(2)).findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
             def myred = rgb[0] >=128 ? 255 : 0
             def mygreen = rgb[1] >=128 ? 255 : 0
             def myblue = rgb[2] >=128 ? 255 : 0
             effectColor = "00${rgbToHex([r:myred, g:mygreen, b:myblue]).substring(1)}"
          } else {
             effectColor = "ff000000"
          }
       }
       if (value.effect != "00000000") {
          uri = "/playbulb.php?device=${deviceId}&setting=${effectColor + value.effect}"
       } else {
          if (state.previousEffect == "04000100") {
             uri = "/playbulb.php?device=${deviceId}&setting=${state.previousColor}ff000100"
          } else {
             uri = "/playbulb.php?device=${deviceId}&setting=${state.previousColor}"
          }
       }
    }
    else if (value.aLevel) {
       if (state.previousColor.substring(2) == "000000") {
          state.previousColor = "00ffffff"
       }
       if (state.previousEffect != "00000000") {
          uri = "/playbulb.php?device=${deviceId}&setting=00${getDimmedColor(state.previousColor.substring(2)).substring(1) + state.previousEffect}"
       } else {
          uri = "/playbulb.php?device=${deviceId}&setting=00${getDimmedColor(state.previousColor.substring(2)).substring(1)}"
       }
       state.previousColor = "00${getDimmedColor(state.previousColor.substring(2)).substring(1)}"
    }
    else {
       // A valid color was not chosen. Setting to white
       uri = "/playbulb.php?device=${deviceId}&setting=ff000000"
       state.previousColor = "ff000000"
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
    def uri = "/playbulb.php?device=${deviceId}&refresh=true"
    postAction(uri)
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

private postAction(uri){ 
  log.debug "uri ${uri}"
  updateDNI()
  def headers = getHeader()
  //log.debug("headders: " + headers) 
  
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
	return "${ip}:${port}"
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
	//log.debug "Getting headers"
    def headers = [:]
    headers.put("HOST", getHostAddress())
    //log.debug "Headers are ${headers}"
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

def fadeOn() {
	log.debug "fadeOn()"
	setColor(effect: "01000f0f")
}
def fadeOff() {
	log.debug "fadeOff()"
	setColor(effect: "00000000")
}
def flashOn() {
	log.debug "flashOn()"
	setColor(effect: "00000f0f")
}
def flashOff() {
	log.debug "flashOff()"
	setColor(effect: "00000000")
}
def candleOn() {
	log.debug "candleOn()"
	setColor(effect: "04000100")
}
def candleOff() {
	log.debug "candleOff()"
	setColor(effect: "00000000")
}
def rainbowfadeOn() {
	log.debug "rainbowfadeOn()"
	setColor(effect: "03000f0f")
}
def rainbowfadeOff() {
	log.debug "rainbowfadeOff()"
	setColor(effect: "00000000")
}
def rainbowflashOn() {
	log.debug "fainbowflashOn()"
	setColor(effect: "02000f0f")
}
def rainbowflashOff() {
	log.debug "fainbowflashOff()"
	setColor(effect: "00000000")
}
