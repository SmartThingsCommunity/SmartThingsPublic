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
        //capability "Door Control" //open close
        capability "Refresh" // refresh command
        capability "Switch Level" // attribute: level (integer, setter: setLevel), command setLevel(level)
        capability "Switch"
        // capability "Window Shade" // windowShade.value ( closed, closing, open, opening, partially open, unknown ), methods: close(), open(), presetPosition()
    
	    attribute "IP", "string"
	}


	preferences {
    	// @TODO: change to MAC address or other more stable addressing method
        input("ip", "string", title:"IP", description:"Shelly IP Address", defaultValue:"" , required: false, displayDuringSetup: true)
	}

	tiles(scale: 2) {
        // @TODO change tile to more appropriate form
        // st.Home.home30 http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png
        // http://scripts.3dgo.net/smartthings/icons/#Home
        // https://community.smartthings.com/t/where-are-the-tile-icons/40086/26
        // st.Home.home30
		// st.Home.home9 -< questa è una tenda
		// st.Transportation.transportation13 (su)
		// st.Transportation.transportation14 (giù)
		// st.doors.garage.garage-closed
		// st.doors.garage.garage-closing
		// st.doors.garage.garage-opening
		// st.doors.garage.garage-open
		// st.switches.switch.on
		// st.switches.switch.off

		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on",           label:'Open', action:"switch.off", icon:"st.doors.garage.garage-open",  backgroundColor:"#00a0dc" //, nextState:"closing"
		    state "off",        label:'Closed', action:"switch.on", icon:"st.doors.garage.garage-closed",  backgroundColor:"#00a0dc" //, nextState:"closing"
		}

/*
		// windowshade not implemented
		standardTile("widowshade", "device.windowShade.value", width: 2, height: 2, canChangeIcon: true) {
			state "open",           label:'Open', action:"window shade.close", icon:"st.doors.garage.garage-open",  backgroundColor:"#00a0dc" //, nextState:"closing"
			state "partially open", label:'${name}', action:"window shade.close", icon:"st.doors.garage.garage-open",  backgroundColor:"#00a0dc" //, nextState:"closing"
		    state "unknown",        label:'Unknown', action:"window shade.close", icon:"st.doors.garage.garage-closed",  backgroundColor:"#00a0dc" //, nextState:"closing"
            state "closing",        label:'${name}', action:"window shade.close", icon:"st.doors.garage.garage-closing",  backgroundColor:"#00a0dc" //, nextState:"opening"
            state "closed",         label:'${name}', action:"window shade.open",  icon:"st.doors.garage.garage-closed", backgroundColor:"#ffffff" //, nextState:"opening"
		    state "opening",        label:'${name}', action:"window shade.open",  icon:"st.doors.garage.garage-opening", backgroundColor:"#ffffff" //, nextState:"closing"
		}
*/

        standardTile ("level", "device.level", width: 2, height: 1) {
			state "level", action:"switch level.setLevel"
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Refresh Position', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		details(["switch", "level", "refresh"])
	}
}

def installed() {
    // @TODO: not refreshing when application is opened
	refresh()
    //runEvery30Minutes(refresh)
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

    return [evt1, evt2]
}



def on() {
    log.debug "Executing 'on'"
    runIn(25, refresh)
    sendRollerCommand "go=open"
}
  
def off() {
    log.debug "Executing 'off'"
    runIn(25, refresh)
    sendRollerCommand "go=close"
}

def presetPosition() {
    log.debug "Executing 'presetPosition'"
}
  
def setLevel(value) {
    log.debug "Executing setLevel value with $value"
    runIn(25, refresh)
    sendRollerCommand "go=to_pos&roller_pos="+value
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


