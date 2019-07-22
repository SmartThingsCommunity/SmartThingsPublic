/**
 *  Copyright 2015 SmartThings
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
 *	Tesla Service Manager
 *
 *	Author: juano23@gmail.com
 *	Date: 2013-08-15
 */

definition(
    name: "Tesla (Connect)",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Integrate your Tesla car with SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/tesla-app%402x.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/tesla-app%403x.png",
    singleInstance: true
)

preferences {
	page(name: "loginToTesla", title: "Tesla")
	page(name: "selectCars", title: "Tesla")
}

def loginToTesla() {
	def showUninstall = username != null && password != null
	return dynamicPage(name: "loginToTesla", title: "Connect your Tesla", nextPage:"selectCars", uninstall:showUninstall) {
		section("Log in to your Tesla account:") {
			input "username", "text", title: "Username", required: true, autoCorrect:false
			input "password", "password", title: "Password", required: true, autoCorrect:false
		}
		section("To use Tesla, SmartThings encrypts and securely stores your Tesla credentials.") {}
	}
}

def selectCars() {
	def loginResult = forceLogin()

	if(loginResult.success)
	{
		def options = carsDiscovered() ?: []

		return dynamicPage(name: "selectCars", title: "Tesla", install:true, uninstall:true) {
			section("Select which Tesla to connect"){
				input(name: "selectedCars", type: "enum", required:false, multiple:true, options:options)
			}
		}
	}
	else
	{
		log.error "login result false"
        return dynamicPage(name: "selectCars", title: "Tesla", install:false, uninstall:true, nextPage:"") {
			section("") {
				paragraph "Please check your username and password"
			}
		}
	}
}


def installed() {
	log.debug "Installed"
	initialize()
}

def updated() {
	log.debug "Updated"

	unsubscribe()
	initialize()
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

def initialize() {

	if (selectCars) {
		addDevice()
	}

	// Delete any that are no longer in settings
	def delete = getChildDevices().findAll { !selectedCars }
	log.info delete
    //removeChildDevices(delete)
}

//CHILD DEVICE METHODS
def addDevice() {
    def devices = getcarList()
    log.trace "Adding childs $devices - $selectedCars"
	selectedCars.each { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			def newCar = devices.find { (it.dni) == dni }
			d = addChildDevice("smartthings", "Tesla", dni, null, [name:"Tesla", label:"Tesla"])
			log.trace "created ${d.name} with id $dni"
		} else {
			log.trace "found ${d.name} with id $key already exists"
		}
	}
}

private removeChildDevices(delete)
{
	log.debug "deleting ${delete.size()} Teslas"
	delete.each {
		state.suppressDelete[it.deviceNetworkId] = true
		deleteChildDevice(it.deviceNetworkId)
		state.suppressDelete.remove(it.deviceNetworkId)
	}
}

def getcarList() {
	def devices = []

	def carListParams = [
		uri: "https://portal.vn.teslamotors.com/",
        path: "/vehicles",
		headers: [Cookie: getCookieValue(), 'User-Agent': validUserAgent()]
	]
    
	httpGet(carListParams) { resp ->
		log.debug "Getting car list"
		if(resp.status == 200) {
            def vehicleId = resp.data.id.value[0].toString()
            def vehicleVIN = resp.data.vin[0]
            def dni = vehicleVIN + ":" + vehicleId
 			def name = "Tesla [${vehicleId}]"
            // CHECK HERE IF MOBILE IS ENABLE
            // path: "/vehicles/${vehicleId}/mobile_enabled",
            // if (enable)
            devices += ["name" : "${name}", "dni" : "${dni}"]
            // else return [errorMessage:"Mobile communication isn't enable on all of your vehicles."]
		} else if(resp.status == 302) {
        	// Token expired or incorrect
			singleUrl = resp.headers.Location.value
		} else {
			// ERROR
			log.error "car list: unknown response"
		}        
	}
    return devices
}

Map carsDiscovered() {
	def devices =  getcarList()
    log.trace "Map $devices"    
	def map = [:]
	if (devices instanceof java.util.Map) {
		devices.each {
			def value = "${it?.name}"
			def key = it?.dni
			map["${key}"] = value
		}
	} else { //backwards compatable
		devices.each {
			def value = "${it?.name}"
			def key = it?.dni
			map["${key}"] = value
		}
	}
	map
}

def removeChildFromSettings(child) {
	def device = child.device
	def dni = device.deviceNetworkId
	log.debug "removing child device $device with dni ${dni}"
	if(!state?.suppressDelete?.get(dni))
	{
		def newSettings = settings.cars?.findAll { it != dni } ?: []
		app.updateSetting("cars", newSettings)
	}
}

private forceLogin() {
	updateCookie(null)
	login()
}


private login() {
	if(getCookieValueIsValid()) {
		return [success:true]
	}
	return doLogin()
}

private doLogin() {
	def loginParams = [
		uri: "https://portal.vn.teslamotors.com",
        path: "/login",
		contentType: "application/x-www-form-urlencoded",
		body: "user_session%5Bemail%5D=${username}&user_session%5Bpassword%5D=${password}"
	]

	def result = [success:false]
	
    try {
    	httpPost(loginParams) { resp ->
            if (resp.status == 302) {
                log.debug "login 302 json headers: " + resp.headers.collect { "${it.name}:${it.value}" }
                def cookie = resp?.headers?.'Set-Cookie'?.split(";")?.getAt(0)
                if (cookie) {
                    log.debug "login setting cookie to $cookie"
                    updateCookie(cookie)                
                    result.success = true
                } else {
                    // ERROR: any more information we can give?
                    result.reason = "Bad login"
                }
            } else {
                // ERROR: any more information we can give?
                result.reason = "Bad login"
            }
    	}        
	} catch (groovyx.net.http.HttpResponseException e) {
			result.reason = "Bad login"
	}
	return result
}

private command(String dni, String command, String value = '') {
	def id = getVehicleId(dni)
    def commandPath
	switch (command) {
		case "flash":
    		commandPath = "/vehicles/${id}/command/flash_lights"
            break;
		case "honk":
    		commandPath = "/vehicles/${id}/command/honk_horn"  
            break;
		case "doorlock":
    		commandPath = "/vehicles/${id}/command/door_lock"  
            break;            
		case "doorunlock":
    		commandPath = "/vehicles/${id}/command/door_unlock"  
            break;  
		case "climaon":
    		commandPath = "/vehicles/${id}/command/auto_conditioning_start"  
            break;            
		case "climaoff":
    		commandPath = "/vehicles/${id}/command/auto_conditioning_stop"  
            break;             
		case "roof":
    		commandPath = "/vehicles/${id}/command/sun_roof_control?state=${value}" 
            break;    
		case "temp":
    		commandPath = "/vehicles/${id}/command/set_temps?driver_temp=${value}&passenger_temp=${value}"
            break;              
		default:
			break; 
    }   
    
	def commandParams = [
		uri: "https://portal.vn.teslamotors.com",
		path: commandPath,
		headers: [Cookie: getCookieValue(), 'User-Agent': validUserAgent()]
	]

	def loginRequired = false

	httpGet(commandParams) { resp ->

		if(resp.status == 403) {
			loginRequired = true
		} else if (resp.status == 200) {
			def data = resp.data
            sendNotification(data.toString())
		} else {
			log.error "unknown response: ${resp.status} - ${resp.headers.'Content-Type'}"
		}
	}
	if(loginRequired) { throw new Exception("Login Required") }
}

private honk(String dni) {
	def id = getVehicleId(dni)
	def honkParams = [
		uri: "https://portal.vn.teslamotors.com",
		path: "/vehicles/${id}/command/honk_horn",
		headers: [Cookie: getCookieValue(), 'User-Agent': validUserAgent()]
	]

	def loginRequired = false

	httpGet(honkParams) { resp ->

		if(resp.status == 403) {
			loginRequired = true
		} else if (resp.status == 200) {
			def data = resp.data
		} else {
			log.error "unknown response: ${resp.status} - ${resp.headers.'Content-Type'}"
		}
	}

	if(loginRequired) {
		throw new Exception("Login Required")
	}
}

private poll(String dni) {
	def id = getVehicleId(dni)
	def pollParams1 = [
		uri: "https://portal.vn.teslamotors.com",
		path: "/vehicles/${id}/command/climate_state",
		headers: [Cookie: getCookieValue(), 'User-Agent': validUserAgent()]
	]

	def childDevice = getChildDevice(dni)
    
	def loginRequired = false

	httpGet(pollParams1) { resp ->

		if(resp.status == 403) {
			loginRequired = true
		} else if (resp.status == 200) {
			def data = resp.data
            childDevice?.sendEvent(name: 'temperature', value: cToF(data.inside_temp).toString())
            if (data.is_auto_conditioning_on)            
            	childDevice?.sendEvent(name: 'clima', value: 'on')
            else
                childDevice?.sendEvent(name: 'clima', value: 'off')
            childDevice?.sendEvent(name: 'thermostatSetpoint', value: cToF(data.driver_temp_setting).toString())            
		} else {
			log.error "unknown response: ${resp.status} - ${resp.headers.'Content-Type'}"
		}
	}

	def pollParams2 = [
		uri: "https://portal.vn.teslamotors.com",
		path: "/vehicles/${id}/command/vehicle_state",
		headers: [Cookie: getCookieValue(), 'User-Agent': validUserAgent()]
	]

	httpGet(pollParams2) { resp ->
		if(resp.status == 403) {
			loginRequired = true
		} else if (resp.status == 200) {
			def data = resp.data
            if (data.sun_roof_percent_open == 0)
            	childDevice?.sendEvent(name: 'roof', value: 'close')
			else if (data.sun_roof_percent_open > 0 && data.sun_roof_percent_open < 70)
            	childDevice?.sendEvent(name: 'roof', value: 'vent')
			else if (data.sun_roof_percent_open >= 70 && data.sun_roof_percent_open <= 80)                
            	childDevice?.sendEvent(name: 'roof', value: 'comfort')
            else if (data.sun_roof_percent_open > 80 && data.sun_roof_percent_open <= 100)
            	childDevice?.sendEvent(name: 'roof', value: 'open')           
            if (data.locked)            
            	childDevice?.sendEvent(name: 'door', value: 'lock')
            else
                childDevice?.sendEvent(name: 'door', value: 'unlock')
		} else {
			log.error "unknown response: ${resp.status} - ${resp.headers.'Content-Type'}"
		}
	}

	def pollParams3 = [
		uri: "https://portal.vn.teslamotors.com",
		path: "/vehicles/${id}/command/charge_state",
		headers: [Cookie: getCookieValue(), 'User-Agent': validUserAgent()]
	]

	httpGet(pollParams3) { resp ->
		if(resp.status == 403) {
			loginRequired = true
		} else if (resp.status == 200) {
			def data = resp.data
            childDevice?.sendEvent(name: 'connected', value: data.charging_state.toString())
            childDevice?.sendEvent(name: 'miles', value: data.battery_range.toString())
            childDevice?.sendEvent(name: 'battery', value: data.battery_level.toString())
		} else {
			log.error "unknown response: ${resp.status} - ${resp.headers.'Content-Type'}"
		}
	}

	if(loginRequired) {
		throw new Exception("Login Required")
	}
}

private getVehicleId(String dni) {
    return dni.split(":").last()
}

private Boolean getCookieValueIsValid()
{
	// TODO: make a call with the cookie to verify that it works
	return getCookieValue()
}

private updateCookie(String cookie) {
	state.cookie = cookie
}

private getCookieValue() {
	state.cookie
}

def cToF(temp) {
    return temp * 1.8 + 32
}

private validUserAgent() {
	"curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8x zlib/1.2.5"
}