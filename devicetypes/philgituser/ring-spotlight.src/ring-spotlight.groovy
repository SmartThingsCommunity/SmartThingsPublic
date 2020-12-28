/**
 *
 *  Ring Spotlight Cam Light Control
 * 
 *  Needs the username/password and the device ID of the device. 
 *  This needs to be sourced by an API call to
 *  https://api.ring.com/clients_api/ring_devices?api_version=9&auth_token={{auth_token}}
 *  
 *  I used https://github.com/davglass/doorbot as a guide to build the requests.
 *  Author: Philip
 *
 */
metadata {
	definition (name: "Ring Spotlight", namespace: "philgituser", author: "Philip") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "off"
		}
		main "button"
		details "button"
	}
    preferences {
        input "username", "email", title: "Ring Username", description: "Email used to login to Ring.com", displayDuringSetup: true, required: true
        input "password", "password", title: "Ring Password", description: "Password you login to Ring.com", displayDuringSetup: true, required: true
        //Not sure there is a better way to do this than ask? 
        input "deviceid", "text", title: "Device ID", description: "The numeric value that identifies the device", displayDuringSetup: true, required: true
    }
}

def parse(String description) {
}

def authenticate() 
{
	def s = "${username}:${password}"
	String encodedUandP = s.bytes.encodeBase64()
    
    def token = "EMPTY"
    def params = [
    	uri: "https://oauth.ring.com",
    	path: "/oauth/token",
        headers: [
            "User-Agent": "iOS"
    	],
        requestContentType: "application/json",
        body: "{\"client_id\": \"ring_official_ios\",\"grant_type\": \"password\",\"password\": \"${password}\",\"scope\": \"client\",\"username\": \"${username}\"}"
	]
    try {
        httpPost(params) { resp ->
            log.debug "POST response code: ${resp.status}"
            
            log.debug "response data: ${resp.data}"
            token = resp.data.access_token
        }
    } catch (e) {
        log.error "HTTP Exception Received on POST: $e"
        log.error "response data: ${resp.data}"
        return
        
    }
    
    params = [
    	uri: "https://api.ring.com",
    	path: "/clients_api/session",
        headers: [
        	Authorization: "Bearer ${token}",
            "User-Agent": "iOS"
    	],
        requestContentType: "application/x-www-form-urlencoded",
        body: "device%5Bos%5D=ios&device%5Bhardware_id%5D=a565187537a28e5cc26819e594e28213&api_version=9"
	]

    try {
        httpPost(params) { resp ->
            log.debug "POST response code: ${resp.status}"
            
            log.debug "response data: ${resp.data}"
            token = resp.data.profile.authentication_token
        }
    } catch (e) {
        log.error "HTTP Exception Received on POST: $e"
        log.error "response data: ${resp.data}"
        return
        
    }
    
    log.debug "Authenticated, Token Found."
    return token
}

def on() {
	
    log.debug "Attempting to Switch On."
    def token = authenticate()
    //Send Command to Turn On
    def paramsforPut = [
    	uri: "https://api.ring.com",
    	path: "/clients_api/doorbots/${deviceid}/floodlight_light_on",
        query: [
        	api_version: "9",
            "auth_token": token
    	]
	]
    try {
        httpPut(paramsforPut) { resp ->
        }
    } catch (e) {
        //ALWAYS seems to throw an exception?
        //Platform bug maybe? 
        log.debug "HTTP Exception Received on PUT: $e"
    }
    sendEvent(name: "switch", value: "on")
}

def off() {

    log.debug "Attempting to Switch Off"
    def token = authenticate()
    
    //Send Command to Turn Off
    def paramsforPut = [
    	uri: "https://api.ring.com",
    	path: "/clients_api/doorbots/${deviceid}/floodlight_light_off",
        query: [
        	api_version: "9",
            "auth_token": token
    	]
	]
    try {
        httpPut(paramsforPut) { resp ->
            //log.debug "PUT response code: ${resp.status}"
        }
    } catch (e) {
        //ALWAYS seems to throw an exception?
        //Platform bug maybe? 
        log.debug "HTTP Exception Received on PUT: $e"
    }
	log.debug "Switched OFF!"
    sendEvent(name: "switch", value: "off")
}