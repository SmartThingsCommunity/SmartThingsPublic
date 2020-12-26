/**
 *  LeviosaController
 *
 *  Copyright 2018 Anton Titkov
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
metadata {
	definition (name: "LeviosaController", namespace: "atlascoder", author: "Anton Titkov") {
		capability "Window Shade"
        command "raise"
        command "lower"
        command "stop"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2){
    
    	standardTile("Open", null, width: 2, height: 2, decoration: "flat"){
        	state "interim", label: "Open", icon: "https://s3.us-east-2.amazonaws.com/leviosa-pub/open.png", action: "open", backgroundColor: "#f1d801"
        }
    	standardTile("Title", "device.windowShade", width: 2, height: 2){
        	state "interim", label: null, icon: "st.Home.home9", backgroundColor: "#ffffff"
        }
    	standardTile("Close", null, width: 2, height: 2, decoration: "flat"){
        	state "interim", label: "Close", icon: "https://s3.us-east-2.amazonaws.com/leviosa-pub/close.png", action: "close", backgroundColor: "#1e9cbb"
        }
    	standardTile("Raise", null, width: 2, height: 2, decoration: "flat"){
        	state "interim", label: "Raise", icon: "https://s3.us-east-2.amazonaws.com/leviosa-pub/up.png", action: "raise", backgroundColor: "#e86d13"
        }
    	standardTile("Stop", null, width: 2, height: 2, decoration: "flat"){
        	state "interim", label: "Stop", icon: "https://s3.us-east-2.amazonaws.com/leviosa-pub/pause.png", action: "stop", backgroundColor: "#cccccc"
        }
    	standardTile("Lower", null, width: 2, height: 2, decoration: "flat"){
        	state "interim", label: "Down", icon: "https://s3.us-east-2.amazonaws.com/leviosa-pub/down.png", action: "lower", backgroundColor: "#90d2a7"
        }
	}
    
    main("Title")
}

// parse events into attributes
def parse(physicalgraph.device.HubResponse description) {
	log.debug "Parsing '${description.toString()}'"
	// TODO: handle 'windowShade' attribute

}

void postCommand(String cmd) {
    int group = getDataValue('dni').split('-')[3][2..3].toInteger()
	log.debug "Executing '${cmd}' for ${group} for ${getHostAddress()}"
    
    try {
        def actn =  new physicalgraph.device.HubAction(
            method: "POST",
            path: "/command/${cmd}/${group}",
            headers: [
                "HOST": "${getHostAddress()}:80",
                "Content-Type": "text/xml"
            ],
            null,
            [callback: parse]
        )
    	sendHubCommand(actn)
    } 
    catch (e) {
	    log.error "something went wrong: $e"
	}

}

// handle commands
def open() {
	postCommand('open')
}

def close() {
	postCommand('close')
}

def presetPosition() {
	log.debug "Executing 'presetPosition'"
	// TODO: handle 'presetPosition' command
}

def raise() {
	postCommand('up')
}

def lower() {
	postCommand('down')
}

def stop() {
	postCommand('stop')
}

def setLevel(String value) {
	int level = value.toInteger()
	log.debug "setLevel(${value})"
    if (level >= 80) {
    	postCommand('open');
    }
    else if (level > 50) {
    	postCommand('up');
    }
    else if (level > 20) {
    	postCommand('down');
    }
    else {
    	postCommand('close');
    }
}

private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        log.warn "Can't figure out ip and port for device: ${device.id}"
    }

    log.debug "Using IP: $ip and port: $port for device: ${device.id}"
    return convertHexToIP(ip)
}

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}