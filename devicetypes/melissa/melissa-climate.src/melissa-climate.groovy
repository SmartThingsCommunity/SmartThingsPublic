/**
 *  Melissa Climate
 *
 *  Copyright 2016 Kiril Maslenkov
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
 */
 
 
preferences {
	input "email", "text", title: "Email", description: "Your Email", required: true
	input "password", "password", title: "Password", description: "Your Melissa Password", required: true
    input "mac", "text", title: "Melissa MAC Address", description: "Melissa Mac Address", required: true
}

 
metadata {
	definition (name: "Melissa Climate", namespace: "Melissa", author: "Melissa Climate") {
        
		capability "Thermostat"
    
    
    	command temperatureUp
        command temperatureDown
        
        command sendCommand

        command switchMode
        command switchFanMode
        command switchingState
        
        command refreshApp

	}

	simulator {	}

	tiles(scale: 2) {
        multiAttributeTile(name:"status", type: "thermostat", width: 6, height: 4){  
          tileAttribute("device.temperature", key:"PRIMARY_CONTROL"){
              attributeState("default", label:'${currentValue}°', unit: "df", backgroundColor: '#4b8df8')
          }	
          
          tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
              attributeState("default", label:'${currentValue}%', unit:"%")
          }
          
          tileAttribute("device.temperature", key: "VALUE_CONTROL") {
              attributeState("VALUE_UP", action: "temperatureUp")
              attributeState("VALUE_DOWN", action: "temperatureDown")
          }
        }
        
        standardTile("mode", "device.mode", decoration: "flat", width: 3, height: 2) {
        	state "auto", action:"switchMode", label: '${name}', nextState: "cool", icon: "http://server.seemelissa.com/smartthings/icons/modes/auto-2.png"
			state "cool", action:"switchMode", label: '${name}', nextState: "heat", icon: "http://server.seemelissa.com/smartthings/icons/modes/cool.png"
			state "heat", action:"switchMode", label: '${name}', nextState: "dry", icon: "http://server.seemelissa.com/smartthings/icons/modes/heat.png"
			state "dry", action:"switchMode",  label: '${name}', nextState: "auto", icon: "http://server.seemelissa.com/smartthings/icons/modes/dry.png"
		}
        
		standardTile("fan", "device.fan", decoration: "flat", width: 3, height: 2, canChangeIcon: true, canChangeBackground: true) {
            state "auto", action:"switchFanMode", label: '${name}', nextState: "high", icon: "http://server.seemelissa.com/smartthings/icons/modes/auto-2.png"
            state "high", action:"switchFanMode", label: '${name}', nextState: "medium", icon: "http://server.seemelissa.com/smartthings/icons/fan/fan1.png"
            state "medium", action:"switchFanMode", label: '${name}', nextState: "low", icon: "http://server.seemelissa.com/smartthings/icons/fan/fan2.png"
    		state "low", action:"switchFanMode", label: '${name}', nextState: "auto", icon: "http://server.seemelissa.com/smartthings/icons/fan/fan3.png"
		}

		standardTile("switchState", "device.switchState", width: 3, height: 2, decoration: "flat", canChangeIcon: true, canChangeBackground: true) {
            state "on", label: '${name}', action: "switchingState", nextState: "off", icon: "http://server.seemelissa.com/smartthings/icons/on_off/turn-on-off-white.png", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switchingState", nextState: "on", icon: "http://server.seemelissa.com/smartthings/icons/on_off/turn-on-off.png", backgroundColor: "#ffffff"

			//state "on", label: '${name}', action: "switchState.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			//state "off", label: '${name}', action: "switchState.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}

        standardTile("send", "device.send", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"sendCommand", label: 'Send Command' 
        }
		
        standardTile("refresh", "device.send2", inactiveLabel: false, decoration: "flat", width: 3, height: 2) {
        	state "default", action:"refreshApp", icon: "st.secondary.refresh"
        }
        
    	standardTile("username", "device.username", inactiveLabel: false, decoration: "flat", width: 6, height: 2) {
        	state "default", label:'${currentValue}'
        }
        
        main(["status", "mode",  "fan", "send"])
        //details(["status", "mode", "fan", "refresh", "switchState",  "username"])
        
        details(["status", "mode", "fan", "refresh", "switchState"])
	}
}

def switchingState() {
    if (state.ac_state == "on") {
    	state.ac_state = "off"
    } else {
    	state.ac_state = "on"
    }
    sendEvent(name: "switchState", value: state.ac_state);
    sendCommand()
}

/* SWITCHING MODES Auto, Cool, Heat, Dry*/

def switchMode() {
    switch (state.mode) {
    	case "auto":
        	state.mode = "cool";
        break;
        case "cool":
        	state.mode = "heat";
        break; 
        case "heat":
        	state.mode = "dry";
        break;
		case "dry":
        	state.mode = "auto";
        break;


	}
    //sendEvent(name: "username", value: state.mode)
	sendCommand()
 //   log.debug state.mode
}

/* SWITCHING FAN MODES*/

def switchFanMode() {
    switch (state.fan) {
    	case "auto":
        	state.fan = "high";
        break;
        case "high":
        	state.fan = "medium";
        break;
        case "medium":
        	state.fan = "low";
        break;
        
        case "low":
        	state.fan = "auto";
        break;


	}
    sendEvent(name: "username", value: state.fan)
    sendCommand()
    //log.debug state.fan
}

def refreshApp() {
  def params = [
       uri:  'http://api2.seemelissa.com/user/login',
        body: [
            
            email: "k.maslenkov@sabev.at",
            password: "xxxxx"
			
            //email: settings.email,
            //password: settings.password
        ]
    ]
    try {
        httpPost(params) {resp ->
			/*
			log.debug "resp data: ${resp.data}"
            log.debug resp.data
            log.debug resp
            */
            def _try = resp.data
            def slurper = new groovy.json.JsonSlurper()
            def results = slurper.parseText("${resp.data}")
            
            
            state.token = results.Data
            
            settings.mac = "OIEQ321TGR6"
            def getParams = [
                uri: "http://api2.seemelissa.com",
                path: "/testusers/getMelissaData/${settings.mac}"
                
                //path: "/testusers/getMelissaData/OIEQ321TGR6"
                
            ]
            try {
                httpGet(getParams) {response->

                    def getResult = slurper.parseText("${response.data}")
                    
					state.temp = getResult.temp as int
                    state.humidity = getResult.humidity
                    state.codeset = getResult.codeset_id as int
                    switch (getResult.mode) {
                    	case "0":
                        	state.mode = "auto";
                        break; 
                        case "1":
                        	state.mode = "fan";
                        break;
                    	case "2":
                        	state.mode = "heat";
                        break;
                        case "3":
                        	state.mode = "cool";
                        break;
                        case "4":
                        	state.mode = "dry";
                        break;
                    }
                    switch (getResult.fan) {
                    	case "0":
                        	state.fan = "auto";
                        break; 
                        case "1":
                        	state.fan = "low";
                        break;
                    	case "2":
                        	state.fan = "medium";
                        break;
                        case "3":
                        	state.fan = "high";
                        break;
                    }
                    if (getResult.state == "0") {
                    	state.ac_state = "off"
                    } else {
                    	state.ac_state = "on"
                    }
                    sendEvent(name: "mode", value: state.mode)
                    sendEvent(name: "fan", value: state.fan)
                    sendEvent(name: "temperature", value: state.temp)
                    sendEvent(name: "humidity", value: state.humidity)
                    sendEvent(name: "switchState", value: state.ac_state)
                    
                }
            }
            catch (e) {
                log.error "error: $e"
            }
        }
    } catch (e) {
        log.error "error: $e"
    }	
}

def sendCommand () {
	log.error state
    
    def params = [
        uri:  'http://api2.seemelissa.com/testusers/sendCommand',
        body: [
            mac: "OIEQ321TGR6",
            //mac: settings.mac,
            temp: state.temp,
            token: state.token,
            fan: state.fan,
            mode: state.mode,
            ac_state: state.ac_state,
            codeset: state.codeset
        ]
    ]
    try {
        httpPost(params) {resp ->
        
        	log.debug "Send command done"
            log.debug "resp data: ${resp.data}"
            
        }
    } catch (e) {
        log.error "error: $e"
    }	
}

def updated() {
	//log.debug "Updated !!!"
    login()
}

def temperatureDown() {
    if (state.temp != 18) {
    	state.temp = state.temp - 1
    }
	sendEvent(name: "temperature", value: state.temp)
    sendCommand()
}

def temperatureUp() {
    if (state.temp != 30) {
    	state.temp = state.temp + 1
    }

	sendEvent(name: "temperature", value: state.temp)
    sendCommand()
}


def login() {
	refreshApp()
/*
    def params = [
        uri:  'http://api2.seemelissa.com/user/login',
        body: [
             email: settings.email,
             password: settings.password
        ]
    ]
    
    try {
        httpPost(params) {resp ->
            log.debug "resp data: ${resp.data}"
 			log.debug "Response Received: Status [$resp.status]"            
          
            def _try = resp.data
            def slurper = new groovy.json.JsonSlurper()
            def results = slurper.parseText("${resp.data}")
            
            
            state.token = results.Data
            state.temp = 18
            state.humidity = 40
            
            sendEvent(name: "temperature", value: state.temp)
            sendEvent(name: "humidity", value: state.humidity)
            
            sendEvent(name: "username", value: state.token)
            
            
        }
    } catch (e) {
        log.error "error: $e"
    }
*/
}


// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}