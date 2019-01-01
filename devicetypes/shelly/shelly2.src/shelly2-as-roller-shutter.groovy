/**
 *  Shelly 2 as Roller Shutter Device Handler
 *
 *  Copyright 2018 DUCCIO GASPARRI
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
 *
 *
 * Shelly http POST at http://IP/roller/0 (roller 1 does not exist) with body form-urlencoded:
 *   go=open
 *   go=close
 *   go=to_pos&roller_pos=[value 0-100]
 *
 */
 
 

metadata {
	definition (name: "Shelly 2 as Roller Shutter", namespace: "dgasparri", author: "Duccio Marco Gasparri") {
		capability "Actuator"
		capability "Sensor"
        capability "Refresh" // refresh command
        capability "Health Check"
        capability "Switch Level" // attribute: level (integer, setter: setLevel), command setLevel(level)
        capability "Switch"
        capability "Window Shade" // windowShade.value ( closed, closing, open, opening, partially open, unknown ), methods: close(), open(), presetPosition()
    
	    attribute "IP", "string"
        command "stop"
	}



	tiles(scale: 2) {
        multiAttributeTile(name:"windowShade", type: "generic", width: 6, height: 4){
            tileAttribute ("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", label:'${name}', action:"close", icon:"st.shades.shade-open", backgroundColor:"#79b821", nextState:"closing"
                attributeState "closed", label:'${name}', action:"open", icon:"st.shades.shade-closed", backgroundColor:"#ffffff", nextState:"opening"
                attributeState "partially open", label:'Open', action:"close", icon:"st.shades.shade-open", backgroundColor:"#79b821", nextState:"closing"
                attributeState "opening", label:'${name}', action:"stop", icon:"st.shades.shade-opening", backgroundColor:"#79b821", nextState:"partially open"
                attributeState "closing", label:'${name}', action:"stop", icon:"st.shades.shade-closing", backgroundColor:"#ffffff", nextState:"partially open"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"setLevel"
            }
        }

        standardTile("home", "device.level", width: 2, height: 2, decoration: "flat") {
            state "default", label: "home", action:"presetPosition", icon:"st.Home.home2"
        }

        standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh", nextState: "disabled"
            state "disabled", label:'', action:"", icon:"st.secondary.refresh"
        }


        preferences {
            input("ip", "string", title:"IP", description:"Shelly IP Address", defaultValue:"" , required: false, displayDuringSetup: true)
            input "preset", "number", title: "Posizione pre-definita (1-100)", defaultValue: 50, required: false, displayDuringSetup: false
        }

        main(["windowShade"])
        details(["windowShade", "home", "refresh"])
	}
}


def getCheckInterval() {
    // These are battery-powered devices, and it's not very critical
    // to know whether they're online or not – 12 hrs
    log.debut "getCheckInterval"
    return 4 * 60 * 60
}

def installed() {
    sendEvent(name: "checkInterval", value: checkInterval, displayed: false)
    refresh()
}

def updated() {
    if (device.latestValue("checkInterval") != checkInterval) {
        sendEvent(name: "checkInterval", value: checkInterval, displayed: false)
    }
    refresh()
}


def parse(description) {
    log.debug "Parsing result"
    log.debug "description: $description"
    
    def msg = parseLanMessage(description)

    // log.debug "Lan message $msg"
    // headers:[content-length:172, http/1.1 200 ok:null, connection:close, content-type:application/json, server:Mongoose/6.11], 
    // body:{"state":"close","power":0.00,"is_valid":true,"safety_switch":false,"stop_reason":"normal",
    //    "last_direction":"close","current_pos":46,"calibrating":false,"positioning":true}, 
    // header:HTTP/1.1 200 OK 
    
 
    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)
    
    log.debug "Position ${data.current_pos}%"
    log.debug "State ${data.state}"
    
    def evt1 = createEvent(name: "level", value: data.current_pos, displayed: false)
    def evt2 = null
    if ( data.current_pos <15 ) {
        log.debug "CreateEvent off"
        evt2 = createEvent(name: "switch", value: "off", displayed: false)
    } else {
        log.debug "CreateEvent on"
        evt2 = createEvent(name: "switch", value: "on", displayed: false)
    }

    log.debut "Parsed to ${evt1.inspect()} and ${evt2.inspect()}"
    return [evt1, evt2]
}




def open() {
    log.debug "Executing 'on'"
    sendRollerCommand "go=open"
    runIn(25, refresh)
}

def close() {
    log.debug "Executing 'off'"
    sendRollerCommand "go=close"
    runIn(25, refresh)
}

def setLevel(value, duration = null) {
    log.debug "Executing setLevel value with $value"
    sendRollerCommand "go=to_pos&roller_pos="+value
    runIn(25, refresh)
}

def presetPosition() {
    log.debug "Executing 'presetPosition'"
}

def stop() {
    log.debug "stop()"
}

def ping() {
    log.debug "Ping"
}

def refresh() {
    log.debug "Refresh - Getting Status"
    sendHubCommand(new physicalgraph.device.HubAction(
      method: "GET",
      path: "/roller/0",
      headers: [
        HOST: getHostAddress(),
        "Content-Type": "application/x-www-form-urlencoded"
      ]
    ))
}

def sendRollerCommand(action) {
    log.debug "Calling /roller/0 with $action"
    sendHubCommand(new physicalgraph.device.HubAction(
      method: "POST",
      path: "/roller/0",
      body: action,
      headers: [
        HOST: getHostAddress(),
        "Content-Type": "application/x-www-form-urlencoded"
      ]
    ))
}

/**
 *
 * From Patrick Powell GitHub patrickkpowell
 *
 */
private getHostAddress() {
    log.debug "Using IP: "+ip+" and PORT: 80 for device: {device.id}"
    device.deviceNetworkId = convertIPtoHex(ip)+":"+convertPortToHex(80)
    log.debug device.deviceNetworkId
    //return ip+":80"
    return device.deviceNetworkId
}

/**
 *
 * From Patrick Powell GitHub patrickkpowell
 *
 */
private String convertIPtoHex(ipAddress) {
    String hex = ipAddress.tokenize( '.' ).collect { String.format( '%02x', it.toInteger() ) }.join().toUpperCase()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex
}

/**
 *
 * From Patrick Powell GitHub patrickkpowell
 *
 */
private String convertPortToHex(port) {
    String hexport = port.toString().format('%04x', port.toInteger()).toUpperCase()
    log.debug hexport
    return hexport
}


