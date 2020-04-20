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
	definition (name: "DirecTV Controller", namespace: "lennyz71", author: "lenny.cunningham@gmail.com") {
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Music Player"
        
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
    	multiAttributeTile(name: "mediaMulti", type:"mediaPlayer", width:6, height:4) {
			tileAttribute("device.status", key: "PRIMARY_CONTROL") {
				attributeState("paused", label:"Paused",)
				attributeState("playing", label:"Playing")
				attributeState("stopped", label:"Stopped")
			}
			tileAttribute("device.status", key: "MEDIA_STATUS") {
				attributeState("paused", label:"Paused", action:"music Player.play", nextState: "playing")
				attributeState("playing", label:"Playing", action:"music Player.pause", nextState: "paused")
				attributeState("stopped", label:"Stopped", action:"music Player.play", nextState: "playing")
			}
			tileAttribute("device.status", key: "PREVIOUS_TRACK") {
				attributeState("status", action:"music Player.previousTrack", defaultState: true)
			}
			tileAttribute("device.status", key: "NEXT_TRACK") {
				attributeState("status", action:"music Player.nextTrack", defaultState: true)
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState("level", action:"music Player.setLevel")
			}
			tileAttribute ("device.mute", key: "MEDIA_MUTED") {
				attributeState("unmuted", action:"music Player.mute", nextState: "muted")
				attributeState("muted", action:"music Player.unmute", nextState: "unmuted")
			}
			tileAttribute("device.getTuned", key: "MARQUEE") {
				attributeState("val", label:"${currentValue}", defaultState: true)
			}
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
        standardTile("guide", "device.switch", width: 1, height: 1, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Guide', action:"guide", icon:""
        }
        standardTile("list", "device.switch", width: 1, height: 1, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'List', action:"list", icon:""
        }
        standardTile("exit", "device.switch", width: 1, height: 1, , decoration: "flat", canChangeIcon: false) {
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
        standardTile("record1", "device.switch", width: 1, height: 1, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Record', action:"record", icon:""
        }
        standardTile("red", "device.switch", width: 1, height: 1, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Red', action:"red", icon:"st.colors.red"
        }
        standardTile("green", "device.switch", width: 1, height: 1, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Red', action:"red", icon:"st.colors.green"
        }
        standardTile("yellow", "device.switch", width: 1, height: 1, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Red', action:"red", icon:"st.colors.yellow"
        }
        standardTile("blue", "device.switch", width: 1, height: 1, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Red', action:"red", icon:"st.colors.blue"
        }
        standardTile("info", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Info', action:"info", icon:""
        }        
        standardTile("replay", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"replay", icon:"st.secondary.refresh-icon"
        }        
        standardTile("advance", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Advance', action:"advance", icon:""
        }        
        standardTile("rew", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"rew", icon:"st.sonos.previous-btn"
        }        
        standardTile("play", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"play", icon:"st.sonos.play-btn"
        }
        standardTile("stop", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"play", icon:"st.sonos.stop-btn"
        }
        standardTile("active", "device.switch", width: 1, height: 1, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Active', action:"active", icon:""
        }
        standardTile("ffwd", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"ffwd", icon:"st.sonos.next-btn"
        }
        standardTile("refresh", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'REFRESH', action:"refresh", icon:""
        }
        standardTile("back", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"back", icon:"st.sonos.back-btn"
        }
        standardTile("pause", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"pause", icon:"st.sonos.pause-btn"
        }
        standardTile("dash", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Dash', action:"dash", icon:"st.sonos.dash-btn"
        }
        standardTile("enter", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'Enter', action:"enter", icon:"st.sonos.enter-btn"
        }
        standardTile("pause1", "device.switch", width: 1, height: 1, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"pause", icon:"st.sonos.pause-btn"
        }
        standardTile("1", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'1', action:"1", icon:""
        }
        standardTile("2", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'2', action:"2", icon:""
        }
        standardTile("3", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'3', action:"3", icon:""
        }
        standardTile("4", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'4', action:"4", icon:""
        }
        standardTile("5", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'5', action:"5", icon:""
        }
        standardTile("6", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'6', action:"6", icon:""
        }
        standardTile("7", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'7', action:"7", icon:""
        }
        standardTile("8", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'8', action:"8", icon:""
        }
        standardTile("9", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'9', action:"9", icon:""
        }
        standardTile("0", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'0', action:"0", icon:""
        }
        standardTile("blank1x1", "device.switch", width: 1, height: 1, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"", icon:""
        }
        standardTile("blank1x2", "device.switch", width: 2, height: 1, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"", icon:""
        }
        standardTile("blank2x2", "device.switch", width: 2, height: 2, , decoration: "flat", canChangeIcon: false) {
        	state "default", label:'', action:"", icon:""
        }

        main "switch"
        
		details([
        "mediaMulti",
        "blank2x2","switch","refresh",
        "replay","stop","advance",
        "rew","play","ffwd",
        "blank1x1","pause1","blank1x2","record1","blank1x1",
        "blank1x1","guide","active","list","exit","blank1x1",
        "blank2x2","up","blank2x2",
        "left","select","right",
        "blank2x2","down","blank2x2",
        "back","menu","info",
        "blank1x1","red","green","yellow","blue","blank1x1",
        "blank2x2","blank2x2","chup",
        "blank2x2","blank2x2","chdown",
        "blank1x1","blank1x1","blank1x1","blank1x1","blank1x1","blank1x1",
        "1","2","3",
        "4","5","6",
        "7","8","9",
        "dash","0","enter"
        ])
	}
    
    preferences {
		input("destIp", "text", title: "IP", description: "The device IP", displayDuringSetup: true, required:true)
		input("destPort", "number", title: "Port", description: "The port you wish to connect", displayDuringSetup: true, required:true)
    	input("destAddr", "string", title: "Client MAC Addr (w/o :)", description: "Enter 0 if controlling main unit. Enter MAC address without colons of unit to control a remote unit.", displayDuringSetup: true, required:true)
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

def refresh() {
	getTuned()
}


//  There is no real parser for this device
//  ST cannot interpret the raw packet return, and thus we cannot 
//  do anything with the return data.  
//  http://community.smartthings.com/t/raw-tcp-socket-communications-with-sendhubcommand/4710/10
//
def parse(description) {
	log.debug "Parsing '${description}'"
    def msg = parseLanMessage(description)
    //log.debug "MSG: '${msg}'"

    def headersAsString = msg.header // => headers as a string
    //log.debug "headerAsString '${headerAsString}'"
    
    def headerMap = msg.headers      // => headers as a Map
    //log.debug "headerMap '${headerMap}'"
    
    def body = msg.body              // => request body as a string
    //log.debug "body '${body}'"
    
    def status = msg.status          // => http status code of the response
    //log.debug "status '${status}'"
    
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    log.debug "json '${json.title}'"
    sendEvent(name: "getTuned", value: "${json.title}")
    
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    //log.debug "xml '${xml}'"
    
    def data = msg.data    
    //log.debug "Data '${data}'"
    
    
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
	def requestString = "/remote/processKey?key=${key}&hold=keyPress&clientAddr=$destAddr"
    log.debug "sendKeyPress('${requestString}')"
    return request(requestString)
}

def request(body) {
    def hubAction = new physicalgraph.device.HubAction(
   	 		'method' : 'GET',
    		'path' : "${body} HTTP/1.1\r\n\r\n",
        	'headers' : [ 
            	HOST: "$destIp:$destPort"
             ]
		) 
    return hubAction
}

def getTuned() {
	log.trace 'getTuned'
    def result = new physicalgraph.device.HubAction(
    		'headers' : [ HOST: "$destIp:$destPort" ],
   	 		'method' : 'GET',
    		'path' : "/tv/getTuned?clientAddr=$destAddr",
            //query : [clientAddr: "$destAddr"]
		) 
    return result
}

/* Helper functions to get the network device ID */
private String NetworkDeviceId(){
    def iphex = convertIPtoHex('192.168.1.240').toUpperCase()
    def porthex = convertPortToHex(8080)
    log.info "DeviceId Info:  $iphex:$porthex"
    //addChildDevice("lennyz71", "DirecTV Genie Client", "$iphex:$porthex", device.hub.id, [label:"DirecTV $iphex", name:"DirecTV $iphex"])
    return "$iphex:$porthex" 
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    //log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    //log.debug hexport
    return hexport
}

// gets the address of the Hub
private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

// gets the address of the device
private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    log.debug "Using IP: $ip and port: $port for device: ${device.id}"
    return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private Integer convertHexToInt(hex) {
    def converted = Integer.parseInt(hex,16)
    log.info "Converted Port ${converted}"
    return converted
}

private String convertHexToIP(hex) {
    def converted = [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
	log.info "Converted IP ${converted}"
    return converted
}