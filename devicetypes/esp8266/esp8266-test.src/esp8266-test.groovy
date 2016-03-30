/**
 *  Copyright 2015 Charles Schwer
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
 *	Arduino Security System
 *
 *	Author: cschwer
 *	Date: 2015-10-27
 */
 preferences {
    input("ip", "string", title:"IP Address", description: "192.168.1.150", required: true, displayDuringSetup: true)
    input("port", "string", title:"Port", description: "8000", defaultValue: 8000 , required: true, displayDuringSetup: true)
	input("mac", "text", title: "MAC Addr", description: "mac")
}

 metadata {
	definition (name: "esp8266 Test", namespace: "esp8266", author: "MikeD") {
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        	capability "Contact Sensor"
        	capability 	"Motion Sensor"
	}

	// simulator metadata
	simulator {}

	// UI tile definitions
	tiles {
		valueTile("temperature", "device.temperature", width: 1, height: 1){
            state "temperature", label: '${currentValue}°F', unit:"",
            	backgroundColors: [
                    [value: 25, color: "#202040"],
                    [value: 30, color: "#202080"]
                ]
		}

		standardTile("refresh", "device.backdoor", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "temperature"
		details (["temperature", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	def msg = parseLanMessage(description)
	def headerString = msg.header

	if (!headerString) {
		log.debug "headerstring was null for some reason :("
    }

	def result = []
	def bodyString = msg.body
    def value = "";
	if (bodyString) {
        log.debug bodyString
        // default the contact and motion status to closed and inactive by default
        def allContactStatus = "closed"
        def allMotionStatus = "inactive"
        def json = msg.json;
        json?.house?.door?.each { door ->
            value = door?.status == 1 ? "open" : "closed"
            log.debug "${door.name} door status ${value}"
            // if any door is open, set contact to open
            if (value == "open") {
				allContactStatus = "open"
			}
			result << creatEvent(name: "temperature", value: allContactStatus)
        }


		
		//result << createEvent(name: "motion", value: allMotionStatus)
    }
    result
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}
/*
private getHostAddress() {
    def ip = settings.ip
    def port = settings.port

	log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}*/

def refresh() {
	
    if(device.deviceNetworkId!=settings.mac) {
    	log.debug "setting device network id"
    	device.deviceNetworkId = settings.mac;
    }
	log.debug "Executing Arduino 'poll'" 
    poll()
}

def poll() {
	log.debug "Executing 'poll' ${getHostAddress()}"
	setDeviceNetworkId(ip,port)
    log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    new physicalgraph.device.HubAction(
    	method: "GET",
    	path: "/getstatus",
    	headers: [
        	HOST: "${getHostAddress()}"
    	]
	)
}

private setDeviceNetworkId(ip,port){
  	def iphex = convertIPtoHex(ip)
  	def porthex = convertPortToHex(port)
  	//device.deviceNetworkId = "$iphex:$porthex"
  	//log.debug "Device Network Id set to ${iphex}:${porthex}"
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