/**
 *  DirectTV Controller
 *
 *  Copyright 2017 Lenny Cunningham
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
 *  DirecTV
 *     Works on DirecTV DVRs and Remote Genies
 *  DirecTV References:
 *     http://www.sbcatest.com/TechUpdates/DTV-MD-0359-DIRECTV%20SHEF%20Public%20Beta%20Command%20Set-V1.0.pdf
 *     http://forums.solidsignal.com/docs/DTV-MD-0359-DIRECTV_SHEF_Command_Set-V1.3.C.pdf
 *     http://whitlockjc.github.io/directv-remote-api/
 *     http://whitlockjc.github.io/directv-remote-api/js/docs/dtv.remote.api.html
 *	Forked from John Morse MoSoftDirecTV Device Handler; added some additional features.
 */

metadata {
	definition (name: "DirecTV Controller", namespace: "LennyZ71", author: "lenny.cunningham@gmail.com") {
		capability "Switch"
        
        command "list"
        command "guide"
        command "exit"
        command "pause"
        command "rew"
        command "replay"
        command "stop"
        command "advance"
        command "ffwd"
        command "record"
        command "play"
        command "active"
        command "back"
        command "menu"
        command "info"
        command "up"
        command "down"
        command "left"
        command "right"
        command "select"
        command "red"
        command "green"
        command "yellow"
        command "blue"
        command "chanup"
        command "chandown"
        command "prev"
        command "dash"
        command "enter"
      	}

    simulator {
        // TODO-: define status and reply messages here
    }

	tiles(scale: 2) {
    	valueTile("getTuned", "device.floatAsText", width: 4, height: 2) {
        	state "val", label: '${currentValue}', defaultState: "'Getting tuned"
        }        
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
            state "on", label: '${name}', action:"switch.off", backgroundColor: "#79b821", icon:"st.Electronics.electronics18"
            state "off", label: '${name}', action:"switch.on", backgroundColor: "#ffffff", icon:"st.Electronics.electronics18"
        }
        standardTile("chup", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'CH Up', action:"chanup", icon:"st.thermostat.thermostat-up"
        }
        standardTile("chdown", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'CH Down', action:"chandown", icon:"st.thermostat.thermostat-down"
        }
        standardTile("prev", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Prev', action:"prev", icon:""
        }
        standardTile("guide", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Guide', action:"guide", icon:""
        }
        standardTile("list", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'List', action:"list", icon:""
        }
        standardTile("exit", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Exit', action:"exit", icon:""
        }
        standardTile("up", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Up', action:"up", icon:"st.thermostat.thermostat-up"
        }
        standardTile("down", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Down', action:"down", icon:"st.thermostat.thermostat-down"
        }
        standardTile("left", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Left', action:"left", icon:"st.thermostat.thermostat-left"
        }
        standardTile("right", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Right', action:"right", icon:"st.thermostat.thermostat-right"
        }
        standardTile("select", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Select', action:"select", icon:""
        }
        standardTile("menu", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Menu', action:"menu", icon:""
        }
        standardTile("record", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Record', action:"record", icon:""
        }        
        standardTile("red", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Red', action:"red", icon:"st.colors.red"
        }
        standardTile("info", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Info', action:"info", icon:""
        }        
        standardTile("replay", "device.switch", width: 3, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"replay", icon:"st.secondary.refresh-icon"
        }        
        standardTile("advance", "device.switch", width: 3, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"advance", icon:"st.sonos.next-btn"
        }        
        standardTile("rew", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"rew", icon:"st.sonos.previous-btn"
        }        
        standardTile("play", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"play", icon:"st.sonos.play-btn"
        }        
        standardTile("ffwd", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"ffwd", icon:"st.sonos.next-btn"
        }        

        main "switch"
        
		details([
        "switch",
        "guide","menu","list",
        "record","up","exit",
        "left","select","right",
        "red","down","info",
        "chup","chdown","prev",
        "replay","advance",
        "rew","play","ffwd",
        ])
	}
    
    preferences {
		input("destIp", "text", title: "IP", description: "The device IP", displayDuringSetup: true, required:true)
		input("destPort", "number", title: "Port", description: "The port you wish to connect", displayDuringSetup: true, required:true)
    	input("destAddr", "number", title: "Client Addr", description: "Enter 0 if controlling main unit. Enter MAC address without colons of unit to control a remote unit.", displayDuringSetup: true, required:true)
	}
}

def installed() {
    log.debug "DirecTV device"
    log.debug "installed : device : ${device} hub : ${device.hub}"

    if (device.hub)
    {
        def ip = device.hub.getDataValue("localIP");
        log.debug "device.hub.ip = ${ip}"
        def port = device.hub.getDataValue("localSrvPortTCP")
        log.debug "device.hub.port = ${port}"
    }
	
    def hub = location.hubs[0]

    log.debug "id: ${hub.id}"
    log.debug "zigbeeId: ${hub.zigbeeId}"
    log.debug "zigbeeEui: ${hub.zigbeeEui}"

    // PHYSICAL or VIRTUAL
    log.debug "type: ${hub.type}"

    log.debug "name: ${hub.name}"
    log.debug "firmwareVersionString: ${hub.firmwareVersionString}"
    log.debug "localIP: ${hub.localIP}"
    log.debug "localSrvPortTCP: ${hub.localSrvPortTCP}"
}

//  There is no real parser for this device
// ST cannot interpret the raw packet return, and thus we cannot 
//  do anything with the return data.  
//  http://community.smartthings.com/t/raw-tcp-socket-communications-with-sendhubcommand/4710/10
//
def parse(description) {
	log.debug "Parsing '${description}'"
    def msg = parseLanMessage(description)

    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    def data = msg.data
    
    log.debug "Data '${data}'"
}

def on() {
	sendEvent(name: "switch", value: 'on')
	sendKeyPress("poweron")
}

def off() {
	sendEvent(name: "switch", value: 'off')
	sendKeyPress("poweroff")
}

def list() {
    sendEvent(name:"Command", value: "list", displayed: true)         
	sendKeyPress("list")
}

def guide() {
    sendEvent(name:"Command", value: "guide", displayed: true)         
	sendKeyPress("guide")
}

def exit() {
    sendEvent(name:"Command", value: "exit", displayed: true)         
	sendKeyPress("exit")
}

def pause() {
    sendEvent(name:"Command", value: "pause", displayed: true)         
	sendKeyPress("pause")
}

def rew() {
    sendEvent(name:"Command", value: "rew", displayed: true)         
	sendKeyPress("exit")
}

def replay() {
    sendEvent(name:"Command", value: "replay", displayed: true)         
	sendKeyPress("replay")
}

def stop() {
    sendEvent(name:"Command", value: "stop", displayed: true)         
	sendKeyPress("stop")
}

def advance() {
    sendEvent(name:"Command", value: "advance", displayed: true)         
	sendKeyPress("advance")
}

def ffwd() {
    sendEvent(name:"Command", value: "ffwd", displayed: true)         
	sendKeyPress("ffwd")
}

def record() {
    sendEvent(name:"Command", value: "record", displayed: true)         
	sendKeyPress("record")
}

def play() {
    sendEvent(name:"Command", value: "play", displayed: true)         
	sendKeyPress("play")
}

def active() {
    sendEvent(name:"Command", value: "active", displayed: true)         
	sendKeyPress("active")
}

def back() {
    sendEvent(name:"Command", value: "back", displayed: true)         
	sendKeyPress("record")
}

def menu() {
    sendEvent(name:"Command", value: "menu", displayed: true)         
	sendKeyPress("menu")
}

def info() {
    sendEvent(name:"Command", value: "info", displayed: true)         
	sendKeyPress("info")
}

def up() {
    sendEvent(name:"Command", value: "up", displayed: true)         
	sendKeyPress("up")
}

def down() {
    sendEvent(name:"Command", value: "down", displayed: true)         
	sendKeyPress("down")
}

def left() {
    sendEvent(name:"Command", value: "left", displayed: true)         
	sendKeyPress("left")
}

def right() {
    sendEvent(name:"Command", value: "right", displayed: true)         
	sendKeyPress("right")
}

def select() {
    sendEvent(name:"Command", value: "select", displayed: true)         
	sendKeyPress("select")
}

def red() {
    sendEvent(name:"Command", value: "red", displayed: true)         
	sendKeyPress("red")
}

def green() {
    sendEvent(name:"Command", value: "green", displayed: true)         
	sendKeyPress("green")
}

def yellow() {
    sendEvent(name:"Command", value: "yellow", displayed: true)         
	sendKeyPress("yellow")
}

def blue() {
    sendEvent(name:"Command", value: "blue", displayed: true)         
	sendKeyPress("blue")
}

def chanup() {
    sendEvent(name:"Command", value: "chanup", displayed: true)         
	sendKeyPress("chanup")
}

def chandown() {
    sendEvent(name:"Command", value: "chandown", displayed: true)         
	sendKeyPress("chandown")
}

def prev() {
    sendEvent(name:"Command", value: "prev", displayed: true)         
	sendKeyPress("prev")
}

def dash() {
    sendEvent(name:"Command", value: "dash", displayed: true)         
	sendKeyPress("dash")
}

def enter() {
    sendEvent(name:"Command", value: "enter", displayed: true)         
	sendKeyPress("enter")
}

def sendKeyPress(key) {
	def requestString = "/remote/processKey?key=${key}&hold=keyPress"
    log.debug "sendKeyPress('${requestString}')"
    request(requestString)
}

def request(body) {
    def hubAction = new physicalgraph.device.HubAction(
   	 		'method' : 'GET',
    		'path' : "${body} HTTP/1.1\r\n\r\n",
        	'headers' : [ HOST: "$destIp:$destPort" ],
            'query' : [clientAddr: "$destAddr"]
		) 
    return hubAction
}

def getTuned() {
    def result = new physicalgraph.device.HubAction(
   	 		'method' : 'GET',
    		'path' : "/tv/getTuned HTTP/1.1\r\n\r\n",
        	'headers' : [ HOST: "$destIp:$destPort" ],
            'query' : [clientAddr: "$destAddr"]
		) 
    return result
}
