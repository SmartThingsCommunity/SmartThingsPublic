/**
 *  Xiaomi Light (v.0.0.3)
 *
 * MIT License
 *
 * Copyright (c) 2018 fison67@nate.com
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
 *
 *
 * Version. 0.0.2 >> Add Timer by fison67
*/

import groovy.json.JsonSlurper

metadata {
	definition (name: "Xiaomi Light", namespace: "fison67", author: "fison67") {
        capability "Switch"						//"on", "off"
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
		capability "Color Control"
        capability "Switch Level"
        capability "Health Check"
        capability "Light"

        attribute "lastOn", "string"
        attribute "lastOff", "string"
        
        attribute "lastCheckin", "Date"
         
        command "setTimeRemaining"
        command "stop"
	}
    
	preferences {
		input name:	"smooth", type:"enum", title:"Select", options:["On", "Off"], description:"", defaultValue: "On"
        input name: "duration", title:"Duration" , type: "number", required: false, defaultValue: 500, description:""
	}

	simulator {
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'\n${name}', action:"switch.off", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfNjgg/MDAxNTIyMTUzOTg0NzMx.YZwxpTpbz-9oqHVDLhcLyOcdWvn6TE0RPdpB_D7kWzwg.97WcX3XnDGPr5kATUZhhGRYJ1IO1MNV2pbDvg8DXruog.PNG.shin4299/Yeelight_tile_on.png?type=w580", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'\n${name}', action:"switch.on", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfMTA0/MDAxNTIyMTUzOTg0NzIz.62-IbE4S7wAOxe3hufTJctU8mlQmrIUQztDaSTnf3kog.sxe2rqceUxFEPqrfYZ_DLkjxM5IPSotCqhErG87DI0Mg.PNG.shin4299/Yeelight_tile_off.png?type=w580", backgroundColor:"#ffffff", nextState:"turningOn"
                
                attributeState "turningOn", label:'\n${name}', action:"switch.off", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfMTA0/MDAxNTIyMTUzOTg0NzIz.62-IbE4S7wAOxe3hufTJctU8mlQmrIUQztDaSTnf3kog.sxe2rqceUxFEPqrfYZ_DLkjxM5IPSotCqhErG87DI0Mg.PNG.shin4299/Yeelight_tile_off.png?type=w580", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'\n${name}', action:"switch.ofn", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfNjgg/MDAxNTIyMTUzOTg0NzMx.YZwxpTpbz-9oqHVDLhcLyOcdWvn6TE0RPdpB_D7kWzwg.97WcX3XnDGPr5kATUZhhGRYJ1IO1MNV2pbDvg8DXruog.PNG.shin4299/Yeelight_tile_on.png?type=w580", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Updated: ${currentValue}')
            }
            
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action:"setColor"
            }
		}
		multiAttributeTile(name:"switch2", type: "lighting"){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'ON', action:"switch.off", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfMjY3/MDAxNTIyMTUzOTg0NzMx.eymIqPh2CSLBt1h5rgVRyqZWaBgm-AXOiRe3crmav1Ug.4ZSrZUCtOjWYraxmPAWV9RoLe0Rnnw1XRB54a5gNLs0g.PNG.shin4299/Yeelight_main_on.png?type=w580", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'OFF', action:"switch.on", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfODQg/MDAxNTIyMTUzOTg0NzIw.61z5mx6FESuZ_PGX9lLn4SE62-DwhdwvZKLuoxwRQQYg.iyatTTFzMSQ8X_BAxMTqsd9mp2QSmArqO5jAKhkctUEg.PNG.shin4299/Yeelight_main_off.png?type=w580", backgroundColor:"#ffffff", nextState:"turningOn"
                
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfODQg/MDAxNTIyMTUzOTg0NzIw.61z5mx6FESuZ_PGX9lLn4SE62-DwhdwvZKLuoxwRQQYg.iyatTTFzMSQ8X_BAxMTqsd9mp2QSmArqO5jAKhkctUEg.PNG.shin4299/Yeelight_main_off.png?type=w580", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.ofn", icon:"https://postfiles.pstatic.net/MjAxODAzMjdfMjY3/MDAxNTIyMTUzOTg0NzMx.eymIqPh2CSLBt1h5rgVRyqZWaBgm-AXOiRe3crmav1Ug.4ZSrZUCtOjWYraxmPAWV9RoLe0Rnnw1XRB54a5gNLs0g.PNG.shin4299/Yeelight_main_on.png?type=w580", backgroundColor:"#ffffff", nextState:"turningOn"

			}
        }
        valueTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
            state "default", label:'', action:"refresh", icon:"st.secondary.refresh"
        }        
        valueTile("lastOn_label", "", decoration: "flat") {
            state "default", label:'Last\nON'
        }
        valueTile("lastOn", "device.lastOn", decoration: "flat", width: 3, height: 1) {
            state "default", label:'${currentValue}'
        }
        valueTile("lastOff_label", "", decoration: "flat") {
            state "default", label:'Last\nOFF'
        }
        valueTile("lastOff", "device.lastOff", decoration: "flat", width: 3, height: 1) {
            state "default", label:'${currentValue}'
        }
        
        valueTile("timer_label", "device.leftTime", decoration: "flat", width: 2, height: 1) {
            state "default", label:'Set Timer\n${currentValue}'
        }
        
        controlTile("time", "device.timeRemaining", "slider", height: 1, width: 1, range:"(0..120)") {
	    	state "time", action:"setTimeRemaining"
		}
        
        standardTile("tiemr0", "device.timeRemaining") {
			state "default", label: "OFF", action: "stop", icon:"st.Health & Wellness.health7", backgroundColor:"#c7bbc9"
		}
        
        main (["switch2"])
        details(["switch", "refresh", "lastOn_label", "lastOn", "lastOff_label","lastOff", "timer_label", "time", "tiemr0" ])       
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

def setStatus(params){
//	log.debug "Status >> ${params}"
    def now = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)
 	switch(params.key){
    case "power":
        if(params.data == "true"){
            sendEvent(name:"switch", value: "on")
            sendEvent(name: "lastOn", value: now)
        } else {
            sendEvent(name:"switch", value: "off")
            sendEvent(name: "lastOff", value: now)
        }
    	break;
    case "color":
    	def colors = params.data.split(",")
        String hex = String.format("#%02x%02x%02x", colors[0].toInteger(), colors[1].toInteger(), colors[2].toInteger());  
    	sendEvent(name:"color", value: hex )
    	break;
    case "brightness":
    	sendEvent(name:"level", value: params.data )
    	break;
    }
    
    sendEvent(name: "lastCheckin", value: now)
}
def refresh(){
	log.debug "Refresh"
    def options = [
     	"method": "GET",
        "path": "/devices/get/${state.id}",
        "headers": [
        	"HOST": state.app_url,
            "Content-Type": "application/json"
        ]
    ]
    sendCommand(options, callback)
}

def setLevel(brightness){
	log.debug "setBrightness >> ID(${state.id}), val=${brightness}"
    
    if(brightness == 0){
    	off()
    }else{
        def body = [
            "id": state.id,
            "cmd": "brightness",
            "data": brightness,
        	"subData": getDuration()
        ]
        def options = makeCommand(body)
        sendCommand(options, null)

    	setPowerByStatus(true)
    }
    
}

def setColor(color){
	log.debug "setColor >> ${state.id} >> ${color.hex}"
    
    def body = [
        "id": state.id,
        "cmd": "color",
        "data": color.hex,
        "subData": getDuration()
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
    
    setPowerByStatus(true)
}

def on(){
	log.debug "Off >> ${state.id}"
    def body = [
        "id": state.id,
        "cmd": "power",
        "data": "on"
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def off(){
	log.debug "Off >> ${state.id}"
	def body = [
        "id": state.id,
        "cmd": "power",
        "data": "off"
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}


def updated() {}

def callback(physicalgraph.device.HubResponse hubResponse){
	def msg
    try {
        msg = parseLanMessage(hubResponse.description)
		def jsonObj = new JsonSlurper().parseText(msg.body)
        log.debug jsonObj
        
     	String hex = String.format("#%02x%02x%02x", jsonObj.state.colorRGB.red, jsonObj.state.colorRGB.blue, jsonObj.state.colorRGB.green);
        sendEvent(name:"color", value: hex )
        sendEvent(name:"level", value: jsonObj.properties.brightness)
        sendEvent(name:"switch", value: jsonObj.properties.power == true ? "on" : "off")
	    
        def now = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)
        sendEvent(name: "lastCheckin", value: now)
    } catch (e) {
        log.error "Exception caught while parsing data: "+e;
    }
}


def sendCommand(options, _callback){
	def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: _callback])
    sendHubCommand(myhubAction)
}

def makeCommand(body){
	def options = [
     	"method": "POST",
        "path": "/control",
        "headers": [
        	"HOST": state.app_url,
            "Content-Type": "application/json"
        ],
        "body":body
    ]
    return options
}

def msToTime(duration) {
    def seconds = (duration%60).intValue()
    def minutes = ((duration/60).intValue() % 60).intValue()
    def hours = ( (duration/(60*60)).intValue() %24).intValue()

    hours = (hours < 10) ? "0" + hours : hours
    minutes = (minutes < 10) ? "0" + minutes : minutes
    seconds = (seconds < 10) ? "0" + seconds : seconds

    return hours + ":" + minutes + ":" + seconds
}

def stop() { 
	unschedule()
	state.timerCount = 0
	updateTimer()
}

def timer(){
	if(state.timerCount > 0){
    	state.timerCount = state.timerCount - 30;
        if(state.timerCount <= 0){
        	if(device.currentValue("switch") == "on"){
        		off()
            }
        }else{
        	runIn(30, timer)
        }
        updateTimer()
    }
//	log.debug "Left Time >> ${state.timerCount}"
}

def updateTimer(){
    def timeStr = msToTime(state.timerCount)
    sendEvent(name:"leftTime", value: "${timeStr}")
    sendEvent(name:"timeRemaining", value: Math.round(state.timerCount/60))
}

def processTimer(second){
	if(state.timerCount == null){
    	state.timerCount = second;
    	runIn(30, timer)
    }else if(state.timerCount == 0){
		state.timerCount = second;
    	runIn(30, timer)
    }else{
    	state.timerCount = second
    }
//    log.debug "Left Time >> ${state.timerCount} seconds"
    updateTimer()
}

def setTimeRemaining(time) { 
	if(time > 0){
        log.debug "Set a Timer ${time}Mins"
        processTimer(time * 60)
        setPowerByStatus(true)
    }
}

def setPowerByStatus(turnOn){
	if(device.currentValue("switch") == (turnOn ? "off" : "on")){
        if(turnOn){
        	on()
        }else{
        	off()
        }
    }
}

def getDuration(){
	def smoothOn = settings.smooth == "" ? "On" : settings.smooth
    def duration = 500
    if(smoothOn == "On"){
        if(settings.duration != null){
            duration = settings.duration
        }
    }
    return duration
}