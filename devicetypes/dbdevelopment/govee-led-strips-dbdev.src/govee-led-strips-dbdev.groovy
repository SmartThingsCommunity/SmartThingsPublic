/**
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
	definition (
		name: "Govee LED Strips (DBDEV)",
		namespace: "dbdevelopment",
		author: "Dean Berman",
		description: "Govee LED Strips Integration",
		category: "My Apps",
	) {

		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		// capability "Power Meter"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

		command "setAdjustedColor"
		command "reset"
		command "refresh"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'Power level: ${currentValue}W', icon: "st.Appliances.appliances17"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}

		multiAttributeTile(name:"switchNoPower", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}

		multiAttributeTile(name:"switchNoSlider", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'The power level is currently: ${currentValue}W', icon: "st.Appliances.appliances17"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}

		multiAttributeTile(name:"switchNoSliderOrColor", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'The light is currently consuming this amount of power: ${currentValue}W', icon: "st.Appliances.appliances17"
			}
		}

		valueTile("color", "device.color", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "color", label: '${currentValue}'
		}

		standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "reset", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single", defaultState: true
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "refresh", label:"", action:"refresh.refresh", icon:"st.secondary.refresh", defaultState: true
		}

		main(["switch"])
		details(["switch", "switchNoPower", "switchNoSlider", "switchNoSliderOrColor", "color", "refresh", "reset"])
	}
}

preferences {
	section("Govee LED Device") {
		// TODO: put inputs here
        input("apikey", "apikey", title: "Govee API Key", description: "Designated Govee API Key for accessing their system", required: true)
        input("deviceID", "deviceid", title: "Govee KED Device ID", description: "Device ID for the Govee LED Lights", required: true)
        input("modelNum", "modelnum", title: "Govee LED Model", description: "Model number of the Govee LED Lights", required: true)
	}
}

// parse events into attributes
def parse(description) {
	log.trace "Executing 'parse($description)'..."
	
	def results = []
	def map = description
	if (description instanceof String)  {
		log.debug "Hue Bulb stringToMap - ${map}"
		map = stringToMap(description)
	}
	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}
	results
}

// handle commands
def on() {
	log.trace "Executing 'on()'..."
	
	callURL('devicecontrol-power', "on")
	
	sendEvent(name: "switch", value: "on")
}

def off() {
	log.trace "Executing 'off()'..."
	
	callURL('devicecontrol-power', "off")
	
	sendEvent(name: "switch", value: "off")
}

def nextLevel() {
	log.trace "Executing 'nextLevel()'..."
	
	def level = device.latestValue("level") as Integer ?: 0
	if (level <= 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(level)
}

def setLevel(percent, rate = null) {
	log.trace "Executing 'setLevel($percent, this)'..."
	
	sendEvent(name: "level", value: percent)
	def power = Math.round(percent / 1.175) * 0.1
	
	callURL('devicecontrol-brightness', percent)
	
	// sendEvent(name: "power", value: power)
}

def setSaturation(percent) {
	log.trace "Executing 'setSaturation($percent)'..."
	
	sendEvent(name: "saturation", value: percent)
}

def setHue(percent) {
	log.trace "Executing 'setHue($percent)'..."
	
	sendEvent(name: "hue", value: percent)
}

def setColor(value) {
	log.trace "Executing 'setColor($value)'..."
	
	if (value.hue) { sendEvent(name: "hue", value: value.hue)}
	if (value.saturation) { sendEvent(name: "saturation", value: value.saturation)}
	if (value.hex) { sendEvent(name: "color", value: value.hex)}
	if (value.level) { sendEvent(name: "level", value: value.level)}
	if (value.switch) { sendEvent(name: "switch", value: value.switch)}
	
	def rgb
	// state.staged << value.subMap("hue", "saturation") // stage ST hue and saturation attributes
	def hex = colorUtil.hsvToHex(Math.round(value.hue) as int, Math.round(value.saturation) as int) // convert to hex
	log.trace "HEX=${hex}"
	// state.staged << [color: hex] // stage ST RGB color attribute
	rgb = colorUtil.hexToRgb(hex) // separate RGB elements for zwave setter
	callURL('devicecontrol-rgb', rgb)
}

def reset() {
	log.trace "Executing 'reset()'..."
	
	setAdjustedColor([level:100, hex:"#90C638", saturation:56, hue:23])
}

def setAdjustedColor(value) {
	log.trace "Executing 'setAdjustedColor($value)'..."
	
	if (value) {
		log.trace "setAdjustedColor: ${value}"
		def adjusted = value + [:]
		adjusted.hue = adjustOutgoingHue(value.hue)
		// Needed because color picker always sends 100
		adjusted.level = null
		setColor(adjusted)
	}
}

def installed() {
	log.trace "Executing 'installed()'.."
	refresh()
}

def updated() {
	log.trace "Executing 'updated()'.."
	refresh()
	
	// unschedule()
	// runEvery1Minutes(refresh)
	// runIn(2, refresh)
}

def poll() {
	log.trace "Executing 'poll()'..."
	
	refresh()
}

def refresh() {
	log.trace "Executing 'refresh()'..."
	
	def ret = callURL('devicestate', '')
	
	// Handling the power state
	def power = ret.data["properties"][1]["powerState"]
	// log.debug "POWER=${power}"
	sendEvent(name: "switch", value: power)
	
	// Handling the brightness level
	def brightness = ret.data["properties"][2]["brightness"]
	// log.debug "BRIGHTNESS=${brightness}"
	sendEvent(name: "level", value: brightness)
	
	// Handling the color
	def rgb = [ret.data["properties"][3]["color"]["r"], ret.data["properties"][3]["color"]["g"], ret.data["properties"][3]["color"]["b"]]
	// log.debug "RGB=${rgb}"
	def hex  = colorUtil.rgbToHex(rgb[0], rgb[1], rgb[2])
	// log.debug "HEX=${hex}"
	sendEvent(name: "color", value: hex)
	
	unschedule()
	// Set it to run every 5 minutes
	// runEvery5Minutes(refresh)
	// Set it to run once a minute (continuous polling)
	runEvery1Minute(refresh)
}

def adjustOutgoingHue(percent) {
	log.trace "Executing 'adjustOutgoingHue($percent)'..."
	
	def adjusted = percent
	if (percent > 31) {
		if (percent < 63.0) {
			adjusted = percent + (7 * (percent -30 ) / 32)
		}
		else if (percent < 73.0) {
			adjusted = 69 + (5 * (percent - 62) / 10)
		}
		else {
			adjusted = percent + (2 * (100 - percent) / 28)
		}
	}
	log.info "percent: $percent, adjusted: $adjusted"
	adjusted
}












// TODO: implement event handlers
def callURL(apiAction, details) {
	log.trace "Executing 'callURL($apiAction, $details)'..."
	
	// log.trace "[SETTINGS] APIKEY=${settings.apikey}, ID=${settings.deviceID}, MODEL=${settings.modelNum}"
	
    def params
	if(apiAction == 'devices') {
        params = [
            method: 'GET',
            uri   : "https://developer-api.govee.com",
            path  : '/v1/devices',
			headers: ["Govee-API-Key": settings.apikey, "Content-Type": "application/json"],
        ]
	} else if(apiAction == 'devicestate') {
        params = [
            method: 'GET',
            uri   : "https://developer-api.govee.com",
            path  : '/v1/devices/state',
			headers: ["Govee-API-Key": settings.apikey, "Content-Type": "application/json"],
			query: [device: settings.deviceID, model: settings.modelNum],
        ]
	} else if(apiAction == 'devicecontrol-power') {
        params = [
            method: 'PUT',
            uri   : "https://developer-api.govee.com",
            path  : '/v1/devices/control',
			headers: ["Govee-API-Key": settings.apikey, "Content-Type": "application/json"],
			contentType: "application/json",
			body: [device: settings.deviceID, model: settings.modelNum, cmd: ["name": "turn", "value": details]],
        ]
	} else if(apiAction == 'devicecontrol-brightness') {
        params = [
            method: 'PUT',
            uri   : "https://developer-api.govee.com",
            path  : '/v1/devices/control',
			headers: ["Govee-API-Key": settings.apikey, "Content-Type": "application/json"],
			contentType: "application/json",
			body: [device: settings.deviceID, model: settings.modelNum, cmd: ["name": "brightness", "value": details]],
        ]
	} else if(apiAction == 'devicecontrol-rgb') {
        params = [
            method: 'PUT',
            uri   : "https://developer-api.govee.com",
            path  : '/v1/devices/control',
			headers: ["Govee-API-Key": settings.apikey, "Content-Type": "application/json"],
			contentType: "application/json",
			body: [device: settings.deviceID, model: settings.modelNum, cmd: ["name": "color", "value": ["r": details[0], "g": details[1], "b": details[2]]]],
        ]
    }
    
	/*
    log.debug params
    log.debug "APIACTION=${apiAction}"
    log.debug "METHOD=${params.method}"
    log.debug "URI=${params.uri}${params.path}"
    log.debug "HEADERS=${params.headers}"
    log.debug "QUERY=${params.query}"
    log.debug "BODY=${params.body}"
	//*/
	
	try {
		if(params.method == 'GET') {
			httpGet(params) { resp ->
				//log.debug "RESP="
				//log.debug "HEADERS="+resp.headers
				//log.debug "DATA="+resp.data
				
				log.debug "response.data="+resp.data
				
				return resp.data
			}
		} else if(params.method == 'PUT') {
			httpPutJson(params) { resp ->
				//log debug "RESP="
				//log.debug "HEADERS="+resp.headers
				//log.debug "DATA="+resp.data
				
				log.debug "response.data="+resp.data
				
				return resp.data
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "callURL() >>>>>>>>>>>>>>>> ERROR >>>>>>>>>>>>>>>>"
		log.error "Error: e.statusCode ${e.statusCode}"
		log.error "${e}"
		log.error "callURL() <<<<<<<<<<<<<<<< ERROR <<<<<<<<<<<<<<<<"
		
		return 'unknown'
	}
}