/**
 *  Home API
 *
 *  Copyright 2015 Jeff Blaisdell
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
definition(
    name: "Home API",
    namespace: "jeff-blaisdell",
    author: "Jeff Blaisdell",
    description: "A simple app to expose home as an api.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "Home API", displayLink: ""])


preferences {
	section("Allow External Service to Control These Things...") {
	    input "thermometers", "capability.temperatureMeasurement", title: "Which thermometers?", multiple: false, required: true
        input "beacons", "capability.presenceSensor", title: "Which presence sensors?", multiple: false, required: true        
	}
}

mappings {
	path("/thermometers") {
		action: [
			GET: "listThermometers"
		]
	}
	path("/thermometers/:id") {
		action: [
			GET: "showThermometer"
		]
	}
}

def installed() {

}

def updated() {

}

def listThermometers() {
	return thermometers.collect { device(it, 'temperature') }
}

def showThermometer() {
	def thermometer = thermometers.find { it.id == params.id }
    if (!thermometer) {
	    httpError(404, "Thermometer not found")
        return
    }
    return device(thermometer, 'temperature')
}

private device(it, name) {
	if (it) {
		def s = it.currentState(name)
		[id: it.id, label: it.displayName, name: it.displayName, state: s]
    }
}