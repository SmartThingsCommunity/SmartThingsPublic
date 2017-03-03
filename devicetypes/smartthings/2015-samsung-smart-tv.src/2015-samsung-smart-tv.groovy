/**
 *  2015 Samsung Smart TV
 *
 *  Copyright 2015 Min-kyu Park
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
	definition (name: "2015 Samsung Smart TV", namespace: "smartthings", author: "Min-kyu Park") {
	 	capability "Switch"
		capability "Button"
		capability "Refresh"
        
        attribute "volume", "string"
        
        command "volumeUp"
		command "volumeDown"
	}

	tiles {
        standardTile("switch", "device.switch", width:1, height:1) {
        	state "on", label:'${name}', action: "switch.off", icon: "st.Electronics.electronics15", backgroundColor: "#3A935A"
			state "off", label:'${name}', action: "switch.on", icon: "st.Electronics.electronics15", backgroundColor: "#ffffff"
		}

		valueTile("volume", "device.volume", width: 1, height: 1) {
            state "volume", label:'${currentValue}\nvol', backgroundColor: "#1C808A"
    	}
        
		standardTile("volumeUp", "device.button", width:1, height:1, decoration:"flat") {
            state "default", label:"vol", action:"volumeUp", icon:"st.custom.buttons.add-icon"
        }

		standardTile("volumeDown", "device.button", width:1, height:1, decoration:"flat") {
            state "default", label:"vol", action:"volumeDown", icon:"st.custom.buttons.subtract-icon"
        }

        standardTile("refresh", "device.refresh", width:1, height:1, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
      
        main(["switch"])

        details(["switch", "volumeUp", "volumeDown", "refresh"])	}
}

def parse(String description) {
	def parsedEvent = parseLanMessage(description)
    def results
    if(parsedEvent.body) {
    	def type = parsedEvent.headers."Content-Type"
		if(type?.contains("json"))
        {
        	def parsedBody = parseJson(parsedEvent.body)
			results = createEvent(name: "${parsedBody.type}", value: "${parsedBody.value}")
        }
    }
    results
}

def on() {
    // To Do : make SmartThings Hub generate magic packet to turn on TV
}

def off() {
    sendRequest("set", "power", "off")
}

def volumeUp() {
	sendRequest("set", "volume", "up")
}

def volumeDown() {
	sendRequest("set", "volume", "down")
}

def refresh() {
	String mac = device.hub.getDataValue("macAddress")
    mac = mac.toLowerCase()
	sendRequest("get", "all", mac)
}

def sendRequest( String request, String type, String value ) {
    String body
    if( type == "notification" || type == "camera" )
    {
    	body = "{\"request\":\"" + request + "\",\"type\":\"" + type + "\",\"value\":" + value + "}"
    }
    else
    {
    	body = "{\"request\":\"" + request + "\",\"type\":\"" + type + "\",\"value\":\"" + value + "\"}"
    }
    
	def httpRequest = [
      	method:		"POST",
        path: 		"/smartthings/tvfeature",
        body : 		"${body}\n",

        headers:[
        			Host: getHostAddress(),
                    /Content-Type/: "application/json",
        		],
        ]
    
	def hubAction = new physicalgraph.device.HubAction(httpRequest)
    return hubAction
}

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

    return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private Integer convertHexToInt(hex) {
 	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
 	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}