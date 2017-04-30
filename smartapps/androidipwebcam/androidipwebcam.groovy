/** Android IP Camera 02
 *
 *  Author: Rob Landry
 * 
 *  URL: http://github.com/roblandry/android-ip-camera.device
 * 
 *  Date: 3/6/15
 *  
 *  Version: 1.0.1
 * 
 *  Description: This is a custom device type. This works with the Android IP Camera app. It allows you to take photos, 
 *  record video, turn on/off the led, focus, overlay, and night vision. It displays various sensors including battery 
 *  level, humidity, temperature, and light (lux). The sensor data is all dependent on what your phone supports.
 * 
 *  Copyright: 2015 Rob Landry
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

preferences
{
	input("username",	"text",		title: "Camera username",	description: "Username for web login")
	input("password",	"password",	title: "Camera password",	description: "Password for web login")
	input("url",		"text",		title: "IP or URL of camera",	description: "Do not include http://")
	input("port",		"text",		title: "Port",			description: "Port")
}

metadata {
	definition (name: "Android IP Camera Beta", author: "Alan Trussell", namespace: "alt") {
		capability "Image Capture"
		capability "Switch"
		capability "Actuator"
		capability "Battery"
       		capability "Illuminance Measurement"
		capability "Temperature Measurement"

		command "ledOn"
		command "ledOff"
		command "focusOn"
		command "focusOff"
		command "overlayOn"
		command "overlayOff"
		command "nightVisionOn"
		command "nightVisionOff"
		command "refresh"


	}

	tiles {
		carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

		standardTile("camera", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: true) {
			state("default", label: '', action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF")
		}

		standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false, decoration: "flat") {
			state("take", label: 'Take Photo', action: "Image Capture.take", icon: "st.camera.take-photo", nextState:"taking")
			state("taking", label: 'Taking...', action: "Image Capture.take", icon: "st.camera.take-photo", backgroundColor: "#79b821")
		}

		standardTile("record", "device.switch", width: 1, height: 1) {
			state("recordOff", label: 'Record Off', action:"switch.on", icon:"st.switches.light.off", backgroundColor: "#ffffff")
			state("recordOn", label: 'Record On', action:"switch.off", icon:"st.switches.light.on", backgroundColor: "#79b821")
		}




		standardTile("led", "device.led", width: 1, height: 1) {
			state("ledOff", label: 'Led Off', action:"ledOn", icon:"st.switches.light.off", backgroundColor: "#ffffff")
			state("ledOn", label: 'Led On', action:"ledOff", icon:"st.switches.light.on", backgroundColor: "#79b821")
		}

		standardTile("focus", "device.focus", width: 1, height: 1) {
			state("focusOff", label: 'Focus Off', action:"focusOn", icon:"st.switches.light.off", backgroundColor: "#ffffff")
			state("focusOn", label: 'Focus On', action:"focusOff", icon:"st.switches.light.on", backgroundColor: "#79b821")
		}

		standardTile("overlay", "device.overlay", width: 1, height: 1) {
			state("overlayOff", label: 'Overlay Off', action:"overlayOn", icon:"st.switches.light.off", backgroundColor: "#ffffff")
			state("overlayOn", label: 'Overlay On', action:"overlayOff", icon:"st.switches.light.on", backgroundColor: "#79b821")
		}

		standardTile("nightVision", "device.nightVision", width: 1, height: 1) {
			state("nightVisionOff", label: 'Night Vision Off', action:"nightVisionOn", icon:"st.switches.light.off", backgroundColor: "#ffffff")
			state("nightVisionOn", label: 'Night Vision On', action:"nightVisionOff", icon:"st.switches.light.on", backgroundColor: "#79b821")
		}


		valueTile("battery", "device.battery", decoration: "flat", width: 1, height: 1) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		
        valueTile("light", "device.illuminance", decoration: "flat") {
			state("light", label:'${currentValue} lux', unit:"${unit}")
		}
		main "camera"
		details(["cameraDetails","camera","take","record","led","focus","overlay","nightVision","battery"])
	}
}


def parseCameraResponse(def response) {
	if(response.headers.'Content-Type'.contains("image/jpeg")) {
		def imageBytes = response.data

		if(imageBytes) {
			storeImage(getPictureName(), imageBytes)
		}
	} else {
		log.error("${device.label} could not capture an image.")
	}
}

private getPictureName() {
	def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
	"image" + "_$pictureUuid" + ".jpg"
}

private take() {
	log.info("${device.label} taking photo")

	httpGet("http://${username}:${password}@${url}:${port}/photo_save_only.jpg"){
		httpGet("http://${username}:${password}@${url}:${port}/photo.jpg"){
			response -> log.info("${device.label} image captured")
			parseCameraResponse(response)
		}
	}
}

def on(theSwitch="record") {
	def sUrl
	switch ( theSwitch ) {
		case "led":
			sUrl = "enabletorch"
			break

		case "focus":
			sUrl = "focus"
			break

		case "overlay":
			sUrl = "settings/overlay?set=on"
			break

		case "nightVision":
			sUrl = "settings/night_vision?set=on"
			break

		default:
			sUrl = "/startvideo?force=1"
	}

	httpGet("http://${username}:${password}@${url}:${port}/${sUrl}"){
		response -> log.info("${device.label} ${theSwitch} On")
		sendEvent(name: "${theSwitch}", value: "${theSwitch}On")
	}

}

def off(theSwitch="record") {
	def sUrl
	switch ( theSwitch ) {
		case "led":
			sUrl = "disabletorch"
			break

		case "focus":
			sUrl = "nofocus"
			break

		case "overlay":
			sUrl = "settings/overlay?set=off"
			break

		case "nightVision":
			sUrl = "settings/night_vision?set=off"
			break

		default:
			sUrl = "stopvideo?force=1"
	}

	httpGet("http://${username}:${password}@${url}:${port}/${sUrl}"){
		response -> log.info("${device.label} ${theSwitch} Off")
		sendEvent(name: "${theSwitch}", value: "${theSwitch}Off")
	}

}

def ledOn() { on("led") }

def ledOff() { off("led") }

def focusOn() { on("focus") }

def focusOff() { off("focus") }

def overlayOn() { on("overlay") }

def overlayOff() { off("overlay") }

def nightVisionOn() { on("nightVision") }

def nightVisionOff() { off("nightVision") }

def installed() { runPeriodically(20*60, poll) }

def configure() { poll() }

def poll() { refresh() }

def refresh() { getSensors() }

def cToF(temp) {
	return temp * 1.8 + 32
}

def getSensors() {

	def params = [
		uri: "http://${username}:${password}@${url}:${port}",
		path: "/sensors.json",
		contentType: 'application/json'
	]

	log.debug "Params = ${params}"

	def theSensor
	def theUnit
	def theData

	try {
		httpGet(params) { 
			response -> log.debug "Start httpGet"
			response.data.each {
				key,value -> theSensor = key
				theUnit = value.unit
				if (value.data[0][1].size() == 1) {
					theData = value.data[0][1].first() 
					if (theSensor == "battery_level") {theSensor = "battery"}
					if (theSensor == "ambient_temp") {
						theSensor = "temperature"
						theUnit = "F"
						theData = cToF(theData as Integer)
                    }
					if (theSensor == "light") {
						theSensor = "light"
						theUnit = "lux"
					}
					
					log.info "name: ${theSensor}, unit: ${theUnit}, value: ${theData as Integer}"
					sendEvent(name:"${theSensor}", unit:"${theUnit}", value: theData as Integer)
				} else { theData = value.data[0][1] }
				log.debug "${theSensor}: ${theUnit} ${theData}"
			}
		}
	}
	catch(e) { log.debug "$e" }
}


