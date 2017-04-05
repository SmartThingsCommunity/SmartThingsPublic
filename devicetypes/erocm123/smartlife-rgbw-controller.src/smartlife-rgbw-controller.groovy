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
        capability "Health Check"
        
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
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())
	}

	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
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
       standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"http://cdn.device-icons.smartthings.com/secondary/configure@2x.png"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }
        
        standardTile("red", "device.red", height: 1, width: 1, inactiveLabel: false, decoration: "flat", canChangeIcon: false) {
            state "off", label:"R", action:"redOn", icon:"st.illuminance.illuminance.dark", backgroundColor:"#cccccc"
            state "on", label:"R", action:"redOff", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FF0000"
        }
        controlTile("redSliderControl", "device.redLevel", "slider", height: 1, width: 4, inactiveLabel: false) {
			state "redLevel", action:"setRedLevel"
		}
        valueTile("redValueTile", "device.redLevel", height: 1, width: 1) {
        	state "redLevel", label:'${currentValue}%'
        }     
        
        standardTile("green", "device.green", height: 1, width: 1, inactiveLabel: false, decoration: "flat", canChangeIcon: false) {
            state "off", label:"G", action:"greenOn", icon:"st.illuminance.illuminance.dark", backgroundColor:"#cccccc"
            state "on", label:"G", action:"greenOff", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00FF00"
        }
        controlTile("greenSliderControl", "device.greenLevel", "slider", height: 1, width: 4, inactiveLabel: false) {
			state "greenLevel", action:"setGreenLevel"
		}
        valueTile("greenValueTile", "device.greenLevel", height: 1, width: 1) {
        	state "greenLevel", label:'${currentValue}%'
        }    
        
        standardTile("blue", "device.blue", height: 1, width:1, inactiveLabel: false, decoration: "flat", canChangeIcon: false) {
            state "off", label:"B", action:"blueOn", icon:"st.illuminance.illuminance.dark", backgroundColor:"#cccccc"
            state "on", label:"B", action:"blueOff", icon:"st.illuminance.illuminance.bright", backgroundColor:"#0000FF"
        }
        controlTile("blueSliderControl", "device.blueLevel", "slider", height: 1, width: 4, inactiveLabel: false) {
			state "blueLevel", action:"setBlueLevel"
		}
        valueTile("blueValueTile", "device.blueLevel", height: 1, width: 1) {
        	state "blueLevel", label:'${currentValue}%'
        }  
        
        standardTile("white1", "device.white1", height: 1, width: 1, inactiveLabel: false, decoration: "flat", canChangeIcon: false) {
            state "off", label:"W1", action:"white1On", icon:"st.illuminance.illuminance.dark", backgroundColor:"#cccccc"
            state "on", label:"W1", action:"white1Off", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        controlTile("white1SliderControl", "device.white1Level", "slider", height: 1, width: 4, inactiveLabel: false) {
			state "white1Level", action:"setWhite1Level"
		}
        valueTile("white1ValueTile", "device.white1Level", height: 1, width: 1) {
        	state "white1Level", label:'${currentValue}%'
        } 
        standardTile("white2", "device.white2", height: 1, width: 1, inactiveLabel: false, decoration: "flat", canChangeIcon: false) {
            state "off", label:"W2", action:"white2On", icon:"st.illuminance.illuminance.dark", backgroundColor:"#cccccc"
            state "on", label:"W2", action:"white2Off", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        controlTile("white2SliderControl", "device.white2Level", "slider", height: 1, width: 4, inactiveLabel: false) {
			state "white2Level", action:"setWhite2Level"
		}
        valueTile("white2ValueTile", "device.white2Level", height: 1, width: 1) {
        	state "white2Level", label:'${currentValue}%'
        } 
        valueTile("ip", "ip", width: 2, height: 1) {
    		state "ip", label:'IP Address\r\n${currentValue}'
		}
        valueTile("firmware", "firmware", width: 2, height: 1) {
    		state "firmware", label:'Firmware ${currentValue}'
		}
        
        
        (1..6).each { n ->
			standardTile("switch$n", "switch$n", canChangeIcon: true, width: 2, height: 2, decoration: "flat") {
				state "off", label: "Program\n$n", action: "on$n", icon: "st.switches.switch.off", backgroundColor: "#cccccc"
                state "on", label: "Program\n$n", action: "off$n", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
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
             "refresh", "configure", "ip", "firmware" ])
}

def installed() {
	log.debug "installed()"
	configure()
}

def configure() {
    logging("configure()", 1)
    def cmds = []
    cmds = update_needed_settings()
    if (cmds != []) cmds
}

def updated()
{
    logging("updated()", 1)
    def cmds = [] 
    cmds = update_needed_settings()
    sendEvent(name: "checkInterval", value: 12 * 60 * 2, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID], displayed: false)
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    if (cmds != []) response(cmds)
}

private def logging(message, level) {
    if (logLevel != "0"){
    switch (logLevel) {
       case "1":
          if (level > 1)
             log.debug "$message"
       break
       case "99":
          log.debug "$message"
       break
    }
    }
}

def getDefault(){
    if(settings.dcolor == "Previous") {
        return "Previous"
    } else if(settings.dcolor == "Random") {
        return "${transition == "false"? "d~" : "f~"}${getHexColor(settings.dcolor)}"
    } else if(settings.dcolor == "Custom") {
        return "${transition == "false"? "d~" : "f~"}${settings.custom}"
    } else if(settings.dcolor == "Soft White" || settings.dcolor == "Warm White") {
        if (settings.level == null || settings.level == "0") {
            return "${transition == "false"? "x~" : "w~"}${getDimmedColor(getHexColor(settings.dcolor), "100")}"
        } else {
            return "${transition == "false"? "x~" : "w~"}${getDimmedColor(getHexColor(settings.dcolor), settings.level)}"
        }
    } else if(settings.dcolor == "W1") {
        if (settings.level == null || settings.level == "0") {
            return "${transition == "false"? "x~" : "w~"}${getDimmedColor(getHexColor(settings.dcolor), "100")}"
        } else {
            return "${transition == "false"? "x~" : "w~"}${getDimmedColor(getHexColor(settings.dcolor), settings.level)}"
        }
    } else if(settings.dcolor == "W2") {
        if (settings.level == null || settings.level == "0") {
            return "${transition == "false"? "z~" : "y~"}${getDimmedColor(getHexColor(settings.dcolor), "100")}"
        } else {
            return "${transition == "false"? "z~" : "y~"}${getDimmedColor(getHexColor(settings.dcolor), settings.level)}"
        }
    } else {
        if (settings.level == null || settings.dcolor == null){
           return "Previous"
        } else if (settings.level == null || settings.level == "0") {
            return "${transition == "false"? "d~" : "f~"}${getDimmedColor(getHexColor(settings.dcolor), "100")}"
        } else {
            return "${transition == "false"? "d~" : "f~"}${getDimmedColor(getHexColor(settings.dcolor), settings.level)}"
        }
    }
}

def parse(description) {
    def map = [:]
    def events = []
    def cmds = []
    
    if(description == "updated") return
    def descMap = parseDescriptionAsMap(description)

    if (!state.mac || state.mac != descMap["mac"]) {
		log.debug "Mac address of device found ${descMap["mac"]}"
        updateDataValue("mac", descMap["mac"])
	}
    if (state.mac != null && state.dni != state.mac) state.dni = setDeviceNetworkId(state.mac)
    
    def body = new String(descMap["body"].decodeBase64())
    log.debug body
    
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    if (result.containsKey("type")) {
        if (result.type == "configuration")
            events << update_current_properties(result)
    }
    if (result.containsKey("power")) {
        events << createEvent(name: "switch", value: result.power)
        toggleTiles("all")
    }
    if (result.containsKey("rgb")) {
       events << createEvent(name:"color", value:"#$result.rgb")

       // only store the previous value if the response did not come from a power-off command
       if (result.power != "off")
         state.previousRGB = result.rgb
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

       // only store the previous value if the response did not come from a power-off command
       if (result.power != "off")
          state.previousW1 = result.w1
    }
    if (result.containsKey("w2")) {
       events << createEvent(name:"white2Level", value: Integer.parseInt(result.w2,16)/255 * 100 as Integer, displayed: false)
       if ((Integer.parseInt(result.w2,16)/255 * 100 as Integer) > 0 ) {
          events << createEvent(name:"white2", value: "on", displayed: false)
       } else {
    	  events << createEvent(name:"white2", value: "off", displayed: false)
       }

       // only store the previous value if the response did not come from a power-off command
       if (result.power != "off")
          state.previousW2 = result.w2
    }
    if (result.containsKey("version")) {
       events << createEvent(name:"firmware", value: result.version + "\r\n" + result.date, displayed: false)
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
    
    if (!device.currentValue("ip") || (device.currentValue("ip") != getDataValue("ip"))) events << createEvent(name: 'ip', value: getDataValue("ip"))

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
    getAction("/on?transition=$transition")
}

def off() {
	log.debug "off()"
    getAction("/off?transition=$transition")
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
    def validValue = true
    
    if ((value.saturation != null) && (value.hue != null)) {
        def hue = (value.hue != null) ? value.hue : 13
		def saturation = (value.saturation != null) ? value.saturation : 13
		def rgb = huesatToRGB(hue as Integer, saturation as Integer)
        value.hex = rgbToHex([r:rgb[0], g:rgb[1], b:rgb[2]])
    } 
    
    if (value.hue == 5 && value.saturation == 4) {
       log.debug "setting color Soft White - Default"
       def whiteLevel = getWhite(value.level)
       uri = "/w1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    }
    /* Letting White - Concentrate adjust RGB values
    else if (value.hue == 63 && value.saturation == 28) {
       log.debug "setting color White - Concentrate"
       def whiteLevel = getWhite(value.level)
       uri = "/w1?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    } */
    else if (value.hue == 63 && value.saturation == 43) {
       log.debug "setting color Daylight - Energize"
       def whiteLevel = getWhite(value.level)
       uri = "/w2?value=${whiteLevel}"
       state.previousColor = "${whiteLevel}"
    }
    else if (value.hue == 79 && value.saturation == 7) {
       log.debug "setting color Warm White - Relax"
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
       if (!value.hex ==~ /^\#([A-Fa-f0-9]){6}$/) {
           log.debug "$value.hex is not valid"
           validValue = false
       } else {
           def rgb = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
           def myred = rgb[0] < 40 ? 0 : rgb[0]
           def mygreen = rgb[1] < 40 ? 0 : rgb[1]
           def myblue = rgb[2] < 40 ? 0 : rgb[2]
           def dimmedColor = getDimmedColor(rgbToHex([r:myred, g:mygreen, b:myblue]))
           uri = "/rgb?value=${dimmedColor}"
       }
    }
    else if (value.white) {
       uri = "/w1?value=${value.white}"
    }
    else if (value.aLevel) {
    	def actions = []
        if (channels == "true") {
           def skipColor = false
           // Handle white channel dimmers if they're on or were not previously off (excluding power-off command)
           if (device.currentValue("white1") == "on" || state.previousW1 != "00") {
              actions.push(setWhite1Level(value.aLevel))
              skipColor = true
           }
           if (device.currentValue("white2") == "on" || state.previousW2 != "00") {
              actions.push(setWhite2Level(value.aLevel))
              skipColor = true
           }
        if (skipColor == false) {
        log.debug state.previousRGB
           // if the device is currently on, scale the current RGB values; otherwise scale the previous setting
           uri = "/rgb?value=${getDimmedColor(device.latestValue("switch") == "on" ? device.currentValue("color").substring(1) : state.previousRGB)}"
           actions.push(getAction("$uri&channels=$channels&transition=$transition"))
        }
        } else {
           // Handle white channel dimmers if they're on or were not previously off (excluding power-off command)
           if (device.currentValue("white1") == "on" || state.previousW1 != "00")
              actions.push(setWhite1Level(value.aLevel))
           if (device.currentValue("white2") == "on" || state.previousW2 != "00")
              actions.push(setWhite2Level(value.aLevel))
        
           // if the device is currently on, scale the current RGB values; otherwise scale the previous setting
           uri = "/rgb?value=${getDimmedColor(device.latestValue("switch") == "on" ? device.currentValue("color").substring(1) : state.previousRGB)}"
           actions.push(getAction("$uri&channels=$channels&transition=$transition"))
        }
        return actions
    }
    else {
       // A valid color was not chosen. Setting to white
       uri = "/w1?value=ff"
    }
    
    if (uri != null && validValue != false) getAction("$uri&channels=$channels&transition=$transition")

}

private getDimmedColor(color, level) {
   if(color.size() > 2){
      def scaledColor = getScaledColor(color)
      def rgb = scaledColor.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
    
      def r = hex(rgb[0] * (level.toInteger()/100))
      def g = hex(rgb[1] * (level.toInteger()/100))
      def b = hex(rgb[2] * (level.toInteger()/100))

      return "${r + g + b}"
   }else{
      color = Integer.parseInt(color, 16)
      return hex(color * (level.toInteger()/100))
   }
}

private getDimmedColor(color) {
   if (device.latestValue("level")) {
      getDimmedColor(color, device.latestValue("level"))
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
    getAction("/status")
}

def ping() {
    log.debug "ping()"
    refresh()
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

// huesatToRGB Changed method provided by daved314
def huesatToRGB(float hue, float sat) {
	if (hue <= 100) {
		hue = hue * 3.6
    }
    sat = sat / 100
    float v = 1.0
    float c = v * sat
    float x = c * (1 - Math.abs(((hue/60)%2) - 1))
    float m = v - c
    int mod_h = (int)(hue / 60)
    int cm = Math.round((c+m) * 255)
    int xm = Math.round((x+m) * 255)
    int zm = Math.round((0+m) * 255)
    switch(mod_h) {
    	case 0: return [cm, xm, zm]
       	case 1: return [xm, cm, zm]
        case 2: return [zm, cm, xm]
        case 3: return [zm, xm, cm]
        case 4: return [xm, zm, cm]
        case 5: return [cm, zm, xm]
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
        sendEvent(name: 'ip', value: ip)
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

private getAction(uri){ 
  updateDNI()
  def userpass
  log.debug uri
  if(password != null && password != "") 
    userpass = encodeCredentials("admin", password)
    
  def headers = getHeader(userpass)

  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  )
  return hubAction    
}

private postAction(uri, data){ 
  updateDNI()
  
  def userpass
  
  if(password != null && password != "") 
    userpass = encodeCredentials("admin", password)
  
  def headers = getHeader(userpass)
  
  def hubAction = new physicalgraph.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  return hubAction    
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
    if (uri != null) return getAction(uri)
}

def setProgram(value, program){
   state."program${program}" = value
}

def hex2int(value){
   return Integer.parseInt(value, 10)
}

def redOn() {
	log.debug "redOn()"
    getAction("/r?value=ff&channels=$channels&transition=$transition")
}
def redOff() {
	log.debug "redOff()"
    getAction("/r?value=00&channels=$channels&transition=$transition")
}

def setRedLevel(value) {
	log.debug "setRedLevel: ${value}"
    def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	level = hex(level)
    getAction("/r?value=$level&channels=$channels&transition=$transition")
}
def greenOn() {
	log.debug "greenOn()"
    getAction("/g?value=ff&channels=$channels&transition=$transition")
}
def greenOff() {
	log.debug "greenOff()"
    getAction("/g?value=00&channels=$channels&transition=$transition")
}

def setGreenLevel(value) {
	log.debug "setGreenLevel: ${value}"
    def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	level = hex(level)
    getAction("/g?value=$level&channels=$channels&transition=$transition")
}
def blueOn() {
	log.debug "blueOn()"
    getAction("/b?value=ff&channels=$channels&transition=$transition")
}
def blueOff() {
	log.debug "blueOff()"
    getAction("/b?value=00&channels=$channels&transition=$transition")
}

def setBlueLevel(value) {
	log.debug "setBlueLevel: ${value}"
    def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	level = hex(level)
    getAction("/b?value=$level&channels=$channels&transition=$transition")
}
def white1On() {
	log.debug "white1On()"
    getAction("/w1?value=ff&channels=$channels&transition=$transition")
}
def white1Off() {
	log.debug "white1Off()"
    getAction("/w1?value=00&channels=$channels&transition=$transition")
}

def setWhite1Level(value) {
	log.debug "setwhite1Level: ${value}"
    def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	def whiteLevel = hex(level)
    getAction("/w1?value=$whiteLevel&channels=$channels&transition=$transition")
}
def white2On() {
	log.debug "white2On()"
    getAction("/w2?value=ff&channels=$channels&transition=$transition")
}
def white2Off() {
	log.debug "white2Off()"
    getAction("/w2?value=00&channels=$channels&transition=$transition")
}

def setWhite2Level(value) {
	log.debug "setwhite2Level: ${value}"
    def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	def whiteLevel = hex(level)
    getAction("/w2?value=$whiteLevel&channels=$channels&transition=$transition")
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
    case "W1":
    color = "ff"
    break;
    case "W2":
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

def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
   
    configuration.Value.each
    {
        if(it.@hidden != "true" && it.@disabled != "true"){
        switch(it.@type)
        {   
            case ["number"]:
                input "${it.@index}", "number",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items
            break
            case ["password"]:
                input "${it.@index}", "password",
                    title:"${it.@label}\n" + "${it.Help}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "text":
               input "${it.@index}", "text",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
        }
        }
    }
}

 /*  Code has elements from other community source @CyrilPeponnet (Z-Wave Parameter Sync). */

def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    currentProperties."${cmd.name}" = cmd.value

    if (settings."${cmd.name}" != null)
    {
        if (convertParam("${cmd.name}", settings."${cmd.name}").toString() == cmd.value)
        {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
        }
    }
    state.currentProperties = currentProperties
}


def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
     
    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    
    cmds << getAction("/configSet?name=haip&value=${device.hub.getDataValue("localIP")}")
    cmds << getAction("/configSet?name=haport&value=${device.hub.getDataValue("localSrvPortTCP")}")
    
    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "lan" && it.@disabled != "true"){
            if (currentProperties."${it.@index}" == null)
            {
               if (it.@setonly == "true"){
                  logging("Setting ${it.@index} will be updated to ${convertParam("${it.@index}", it.@value)}", 2)
                  cmds << getAction("/configSet?name=${it.@index}&value=${convertParam("${it.@index}", it.@value)}")
               } else {
                  isUpdateNeeded = "YES"
                  logging("Current value of setting ${it.@index} is unknown", 2)
                  cmds << getAction("/configGet?name=${it.@index}")
               }
            }
            else if ((settings."${it.@index}" != null || it.@hidden == "true") && currentProperties."${it.@index}" != (settings."${it.@index}"? convertParam("${it.@index}", settings."${it.@index}".toString()) : convertParam("${it.@index}", "${it.@value}")))
            { 
                isUpdateNeeded = "YES"
                logging("Setting ${it.@index} will be updated to ${convertParam("${it.@index}", settings."${it.@index}")}", 2)
                cmds << getAction("/configSet?name=${it.@index}&value=${convertParam("${it.@index}", settings."${it.@index}")}")
            } 
        }
    }
    
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

def convertParam(name, value) {
	switch (name){
        case "dcolor":
            getDefault()
        break
        default:
        	value
        break
    }
}

def configuration_model()
{
'''
<configuration>
<Value type="password" byteSize="1" index="password" label="Password" min="" max="" value="" setting_type="preference" fw="">
<Help>
</Help>
</Value>
<Value type="list" byteSize="1" index="pos" label="Boot Up State" min="0" max="2" value="0" setting_type="lan" fw="">
<Help>
Default: Off
</Help>
    <Item label="Off" value="0" />
    <Item label="On" value="1" />
</Value>
<Value type="list" byteSize="1" index="transition" label="Default Transition" min="0" max="1" value="0" setting_type="preference" fw="">
<Help>
Default: Fade
</Help>
    <Item label="Fade" value="true" />
    <Item label="Flash" value="false" />
</Value>
<Value type="list" byteSize="1" index="dcolor" label="Default Color" min="" max="" value="" setting_type="lan" fw="">
<Help>
Default: Previous
</Help>
    <Item label="Previous" value="Previous" />
    <Item label="Soft White - Default" value="Soft White" />
    <Item label="White - Concentrate" value="White" />
    <Item label="Daylight - Energize" value="Daylight" />
    <Item label="Warm White - Relax" value="Warm White" />
    <Item label="Red" value="Red" />
    <Item label="Green" value="Green" />
    <Item label="Blue" value="Blue" />
    <Item label="Yellow" value="Yellow" />
    <Item label="Orange" value="Orange" />
    <Item label="Purple" value="Purple" />
    <Item label="Pink" value="Pink" />
    <Item label="Cyan" value="Random" />
    <Item label="W1" value="W1" />
    <Item label="W2" value="W2" />
    <Item label="Custom" value="Custom" />
</Value>
<Value type="text" byteSize="1" index="custom" label="Custom Color in Hex" min="" max="" value="" setting_type="preference" fw="">
<Help>
(ie ffffff)
If \"Custom\" is chosen above as the default color. Default level does not apply if custom hex value is chosen.
</Help>
</Value>
<Value type="number" byteSize="1" index="level" label="Default Level" min="1" max="100" value="" setting_type="preference" fw="">
<Help>
</Help>
</Value>
<Value type="boolean" byteSize="1" index="channels" label="Mutually Exclusive RGB / White.\nOnly allow one or the other" min="" max="" value="false" setting_type="preference" fw="">
<Help>
</Help>
</Value>
<Value type="list" byteSize="1" index="transitionspeed" label="Transition Speed" min="1" max="3" value="1" setting_type="lan" fw="">
<Help>
Default: Slow
</Help>
    <Item label="Slow" value="1" />
    <Item label="Medium" value="2" />
    <Item label="Fast" value="3" />
</Value>
<Value type="number" byteSize="1" index="autooff" label="Auto Off" min="0" max="65536" value="0" setting_type="lan" fw="" disabled="true">
<Help>
Automatically turn the switch off after this many seconds.
Range: 0 to 65536
Default: 0 (Disabled)
</Help>
</Value>
<Value type="list" index="logLevel" label="Debug Logging Level?" value="0" setting_type="preference" fw="">
<Help>
</Help>
    <Item label="None" value="0" />
    <Item label="Reports" value="1" />
    <Item label="All" value="99" />
</Value>
</configuration>
'''
}