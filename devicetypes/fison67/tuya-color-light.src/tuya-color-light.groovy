/**
 *  Tuya Light (v.0.0.1)
 *
 * MIT License
 *
 * Copyright (c) 2019 fison67@nate.com
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
*/

import groovy.json.JsonSlurper
import java.awt.Color

metadata {
	definition (name: "Tuya Color Light", namespace: "fison67", author: "fison67", vid: "generic-rgb-color-bulb", ocfDeviceType: "oic.d.light") {
    	capability "Switch"
    	capability "Light"
		capability "Color Control"
		capability "ColorTemperature"
		capability "Switch Level"
        capability "Refresh"
        
        attribute "lastCheckin", "Date"
        
	}

	simulator { }

	preferences {
        input name: "powerIDX", title:"Power Index" , type: "number", required: false, defaultValue: 1
        input name: "modeIDX", title:"Color Mode Index" , type: "number", required: false, defaultValue: 2
        input name: "brightnessIDX", title:"Brightness Index" , type: "number", required: false, defaultValue: 3
        input name: "colorIDX", title:"Color Index" , type: "number", required: false, defaultValue: 5
	}
    
	tiles {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfNjgg/MDAxNTIyMTUzOTg0NzMx.YZwxpTpbz-9oqHVDLhcLyOcdWvn6TE0RPdpB_D7kWzwg.97WcX3XnDGPr5kATUZhhGRYJ1IO1MNV2pbDvg8DXruog.PNG.shin4299/Yeelight_tile_on.png?type=w580", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfMTA0/MDAxNTIyMTUzOTg0NzIz.62-IbE4S7wAOxe3hufTJctU8mlQmrIUQztDaSTnf3kog.sxe2rqceUxFEPqrfYZ_DLkjxM5IPSotCqhErG87DI0Mg.PNG.shin4299/Yeelight_tile_off.png?type=w580", backgroundColor:"#ffffff", nextState:"turningOn"
                
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfMTA0/MDAxNTIyMTUzOTg0NzIz.62-IbE4S7wAOxe3hufTJctU8mlQmrIUQztDaSTnf3kog.sxe2rqceUxFEPqrfYZ_DLkjxM5IPSotCqhErG87DI0Mg.PNG.shin4299/Yeelight_tile_off.png?type=w580", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.ofn", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfNjgg/MDAxNTIyMTUzOTg0NzMx.YZwxpTpbz-9oqHVDLhcLyOcdWvn6TE0RPdpB_D7kWzwg.97WcX3XnDGPr5kATUZhhGRYJ1IO1MNV2pbDvg8DXruog.PNG.shin4299/Yeelight_tile_on.png?type=w580", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Updated: ${currentValue}',icon: "st.Health & Wellness.health9")
            }
            
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"setLevel"
            }
            
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action:"setColor"
            }
		}
        
        main(["switch"])
  		details(["switch"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def setInfo(String app_url, String id) {
	log.debug "${app_url}, ${id}"
	state.app_url = app_url
    state.id = id
}

def setStatus(data){
	log.debug data
    
    sendEvent(name: "switch", value: (data[powerIDX.toString()] ? "on" : "off"))
    
    if(timerIDX > 0){
    	def timeStr = msToTime(data[timerIDX.toString()])
    	sendEvent(name:"leftTime", value: "${timeStr}", displayed: false)
    	sendEvent(name:"time", value: Math.round(data/60), displayed: false)
    }
    if(modeIDX > 0){
    	state.colorMode = data[modeIDX.toString()]
    }
    if(colorIDX > 0){
    	def tmp = data[colorIDX.toString()]
        def val = "0x" + tmp.substring(tmp.length()-2, tmp.length())
        def brightness = Integer.decode(val)
    	sendEvent(name:"color", value: "#" + tmp.substring(0, 6)  )
        sendEvent(name:"level", value: (brightness * 100 / 255) as int, displayed: true)
        
        def rgb = hexToRGB(tmp.substring(0, 6))
        float[] hsbValues = new float[3];
		def hueSat = Color.RGBtoHSB(rgb.r, rgb.g, rgb.b, hsbValues)
    	sendEvent(name:"hue", value: (hueSat[0] * 100) as int  )
    	sendEvent(name:"saturation", value: (hueSat[1] * 100) as int)
    }
    if(brightnessIDX > 0){
    	if(state.colorMode == "white"){
            def _value = (data[brightnessIDX.toString()] /255*100 ) as int
            sendEvent(name:"level", value: _value, displayed: true)
        }
    }
  
    def now = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)
    sendEvent(name: "lastCheckin", value: now, displayed: false)
}

def setColor(color){
	log.debug "setColor >> ${color}"
    if(state.colorMode != "colour"){
    	processCommand("mode", "color", modeIDX.toString())
    }
    def hex = color.hex
    if(hex == null){
    	def rgb = huesatToRGB(color.hue as Integer, color.saturation as Integer)
        hex = "#" + Integer.toHexString(rgb[0]) + Integer.toHexString(rgb[1]) + Integer.toHexString(rgb[2])
        color["hex"] = hex
    }
	state.lastColor = hex
	def val = color
    val["brightness"] = device.currentValue("level")
    processCommand("color", val, colorIDX.toString())
}

def setLevel(brightness){
	log.debug "setLevel >> ${brightness} >> mode(" + colorMode +  ")"
    def value = (brightness * 255/100) as int
    if(state.colorMode == "white"){
    	processCommand("brightness", value, brightnessIDX.toString())
    }else if(state.colorMode == "colour"){
        def val = ["hex":state.lastColor]
        val["brightness"] = value
        processCommand("color", val, colorIDX.toString())
    }
}

def on(){
	log.debug "on"
    processCommand("power", "on", powerIDX.toString())
}

def off(){
	log.debug "off"
    processCommand("power", "off", powerIDX.toString())
}

def processCommand(cmd, data, idx){
	def body = [
        "id": state.id,
        "cmd": cmd,
        "data": data,
        "idx": idx
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def callback(physicalgraph.device.HubResponse hubResponse){
	def msg
    try {
        msg = parseLanMessage(hubResponse.description)
		def jsonObj = new JsonSlurper().parseText(msg.body)
    } catch (e) {
        log.error "Exception caught while parsing data: "+e;
    }
}

def refresh(){}

def updated(){}

def sendCommand(options, _callback){
	def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: _callback])
    sendHubCommand(myhubAction)
}

def makeCommand(body){
	def options = [
     	"method": "POST",
        "path": "/api/control",
        "headers": [
        	"HOST": state.app_url,
            "Content-Type": "application/json"
        ],
        "body":body
    ]
    return options
}

def hexToPercent(value){
	def list = [
    	"0":0, "1":0,"2":1, "3":1, "4":2, "5":2, "6":2, "7":3, "8":3, "9":4,
    	"10":4, "11":4,"12":5, "13":5, "14":5, "15":6, "16":6, "17":7, "18":7, "19":7,
    	"20":8, "21":8,"22":9, "23":9, "24":9, "25":10, "26":10, "27":11, "28":11, "29":11
    
    ]
}

def hexToRGB(value){
	return [
    	"r": Integer.decode("0x"+value.substring(0,2)), 
        "g": Integer.decode("0x"+value.substring(2,4)),
        "b": Integer.decode("0x"+value.substring(4,6))
   ]
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