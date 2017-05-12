/**
 *  Copyright 2017 Ian Lindsay
 *
 *  Code snippets taken from Tim Slagle
 *  https://community.smartthings.com/u/tslagle13
 *  here:
 *  https://community.smartthings.com/t/generic-camera-device-using-local-connection-new-version-now-available/3269/75?u=l0kiscot
 *
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
  input("devicekey", "text",     title: "Device Key",   description: "Your OpenGarage.io device key")
  input("ipadd",       "text",     title: "IP address", description: "The IP address of your OpenGarage.io unit")
  input("port",     "text",     title: "Port",       description: "The port of your OpenGarage.io unit")
}

metadata {
  definition (name: "OpenGarage.io Handler", namespace: "littlegumSmartHome", author: "Ian Lindsay") {
		capability "Door Control"
		capability "Garage Door Control"
		capability "Refresh"
	}

	tiles (scale: 2){
        standardTile("garagedoor", "device.garagedoor", width: 6, height: 4) {
  			state "open", label: '${name}', action: "close", icon: "st.doors.garage.garage-open", backgroundColor: "#e54444"
  			state "closed", label: '${name}', action: "open", icon: "st.doors.garage.garage-closed", backgroundColor: "#79b821"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 6, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main("garagedoor")
		details(["garagedoor", "refresh"])
	}
    simulator {
        // simulator metadata
    }
}

def installed() {
    initialize()
}

def initialize() {
	log.debug "initialize triggered"
    // initialize state
    state.doorStatus =  1 // 1 means open, 0 means closed
	api("getstatus", [])
}

def open() {
    log.debug "Executing 'close'"
    api("openclose", [])
}

def close() {
	log.debug "Executing 'close'"
    api("openclose", [])
}

def refresh() {
	log.debug "Refreshing Values "
    
    api("getstatus", [])
}

def api(method, args = [], success = {}) {
  def methods = [
    "getstatus": [gdipadd: "${ipadd}", gdport:"${port}", gdpath:"/jc", gdtype: "get"],
    "openclose": [gdipadd: "${ipadd}", gdport:"${port}", gdpath:"/cc?dkey=${devicekey}&click=1", gdtype: "get"]
  ]

  def request = methods.getAt(method)

  doRequest(request.gdipadd, request.gdport, request.gdpath, request.gdtype, success)
}

private doRequest(gdipadd, gdport, gdpath, gdtype, success) {
  log.debug(gdipadd)

  //if(type == "post") {
  //  httpPost(uri , "", success)
  //}

  //else if(type == "get") {
  //  httpGet(uri, success)
  //}
  
    def host = gdipadd
    def hosthex = convertIPToHex(host)
    def porthex = Long.toHexString(Long.parseLong((gdport)))
    if (porthex.length() < 4) { porthex = "00" + porthex }
    
    //log.debug "Port in Hex is $porthex"
    //log.debug "Hosthex is : $hosthex"
    device.deviceNetworkId = "$hosthex:$porthex" 
    
     //log.debug "The device id configured is: $device.deviceNetworkId"

    //def path = gdpath //"/SnapshotJPEG?Resolution=640x480&Quality=Clarity"
    log.debug "path is: $gdpath"
    
    def headers = [:] //"HOST:" + getHostAddress() + ""
    headers.put("HOST", "$host:$gdport")
  
  try {
        def hubAction = new physicalgraph.device.HubAction(
        	method: method,
        	path: gdpath,
        	headers: headers
            )  
    }
    catch (Exception e) 
    {
        log.debug "Hit Exception on $hubAction"
        log.debug e
    }
  
}

def parse(description) {

    def msg = parseLanMessage(description)
	
    log.debug msg
    
    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    //def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    //def xml = msg.xml                // => any XML included in response body, as a document tree structure
    //def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)
	
    def slurper = new groovy.json.JsonSlurper()
 	def json = slurper.parseText(msg.body)
 
    log.debug json
    
    def result
	log.debug "before state.doorStatus: $state.doorStatus"
    
    // open / close event
	if(json.result){
    	if(state.doorStatus){
        	log.debug "door open - so closing"
        	state.doorStatus = 0
            result = createEvent(name: "garagedoor", value: "closed")
        } else {
        	log.debug "door closed - so opening"
        	state.doorStatus = 1
            result = createEvent(name: "garagedoor", value: "open")
        }
     }
    //status update request
    if(json.mac){
    	if(json.door){
        	log.debug "door is open - refreshing setting"
        	state.doorStatus = 1
            result = createEvent(name: "garagedoor", value: "open")
        } else {
        	log.debug "door is closed - refreshing setting"
        	state.doorStatus = 0
            result = createEvent(name: "garagedoor", value: "closed")
        }
    }
    
    log.debug "after state.doorStatus: $state.doorStatus"
    
    return result
}

private Long converIntToLong(ipAddress) {
	long result = 0
	def parts = ipAddress.split("\\.")
    for (int i = 3; i >= 0; i--) {
        result |= (Long.parseLong(parts[3 - i]) << (i * 8));
    }

    return result & 0xFFFFFFFF;
}

private String convertIPToHex(ipAddress) {
	return Long.toHexString(converIntToLong(ipAddress));
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}
private String convertHexToIP(hex) {
log.debug("Convert hex to ip: $hex") //	a0 00 01 6
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
    log.debug device.deviceNetworkId
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}
