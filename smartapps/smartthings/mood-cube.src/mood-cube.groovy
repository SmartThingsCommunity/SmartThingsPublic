/**
 *  Mood Cube
 *
 *  Copyright 2014 Samsung Electronics Co., LTD.
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

/************
 * Metadata *
 ************/
definition(
	name: "Mood Cube",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Set your lighting by rotating a cube containing a SmartSense Multi",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png",
	pausable: true
)

/**********
 * Setup  *
 **********/
preferences {
	page(name: "mainPage", title: "", nextPage: "scenesPage", uninstall: true) {
		section("Use the orientation of this cube") {
			input "cube", "capability.threeAxis", required: false, title: "SmartSense Multi sensor"
		}
		section("To control these lights") {
			input "lights", "capability.switch", multiple: true, required: false, title: "Lights, switches & dimmers"
		}
		section([title: " ", mobileOnly:true]) {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
		}
	}
	page(name: "scenesPage", title: "Scenes", install: true, uninstall: true)
	page(name: "scenePage", title: "Scene", install: false, uninstall: false, previousPage: "scenesPage")
	page(name: "devicePage", install: false, uninstall: false, previousPage: "scenePage")
	page(name: "saveStatesPage", install: false, uninstall: false, previousPage: "scenePage")
}


def scenesPage() {
	log.debug "scenesPage()"
	def sceneId = getOrientation()
	dynamicPage(name:"scenesPage") {
		section {
			for (num in 1..6) {
				href "scenePage", title: "${num}. ${sceneName(num)}${sceneId==num ? ' (current)' : ''}", params: [sceneId:num], description: "", state: sceneIsDefined(num) ? "complete" : "incomplete"
			}
		}
		section {
			href "scenesPage", title: "Refresh", description: ""
		}
	}
}

def scenePage(params=[:]) {
	log.debug "scenePage($params)"
	def currentSceneId = getOrientation()
	def sceneId = params.sceneId as Integer ?: state.lastDisplayedSceneId
	state.lastDisplayedSceneId = sceneId
	dynamicPage(name:"scenePage", title: "${sceneId}. ${sceneName(sceneId)}") {
		section {
			input "sceneName${sceneId}", "text", title: "Scene Name", required: false
		}

		section {
			href "devicePage", title: "Show Device States", params: [sceneId:sceneId], description: "", state: sceneIsDefined(sceneId) ? "complete" : "incomplete"
		}

        section {
            href "saveStatesPage", title: "Record Current Device States", params: [sceneId:sceneId], description: ""
        }
	}
}

def devicePage(params) {
	log.debug "devicePage($params)"

	getDeviceCapabilities()

	def sceneId = params.sceneId as Integer ?: state.lastDisplayedSceneId

	dynamicPage(name:"devicePage", title: "${sceneId}. ${sceneName(sceneId)} Device States") {
		section("Lights") {
			lights.each {light ->
				input "onoff_${sceneId}_${light.id}", "boolean", title: light.displayName
			}
		}

		section("Dimmers") {
			lights.each {light ->
				if (state.lightCapabilities[light.id] in ["level", "color"]) {
					input "level_${sceneId}_${light.id}", "enum", title: light.displayName, options: levels, description: "", required: false
				}
			}
		}

		section("Colors (hue/saturation)") {
			lights.each {light ->
				if (state.lightCapabilities[light.id] == "color") {
					input "color_${sceneId}_${light.id}", "text", title: light.displayName, description: "", required: false
				}
			}
		}
	}
}

def saveStatesPage(params) {
	saveStates(params)
	devicePage(params)
}


/*************************
 * Installation & update *
 *************************/
def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe cube, "threeAxis", positionHandler
}


/******************
 * Event handlers *
 ******************/
def positionHandler(evt) {

	def sceneId = getOrientation(evt.xyzValue)
	log.trace "orientation: $sceneId"

	if (sceneId != state.lastActiveSceneId) {
		restoreStates(sceneId)
	}
	else {
		log.trace "No status change"
	}
	state.lastActiveSceneId = sceneId
}


/******************
 * Helper methods *
 ******************/
private Boolean sceneIsDefined(sceneId) {
	def tgt = "onoff_${sceneId}".toString()
	settings.find{it.key.startsWith(tgt)} != null
}

private updateSetting(name, value) {
	app.updateSetting(name, value)
	settings[name] = value
}

private closestLevel(level) {
	level ? "${Math.round(level/5) * 5}%" : "0%"
}

private saveStates(params) {
	log.trace "saveStates($params)"
	def sceneId = params.sceneId as Integer
	getDeviceCapabilities()

	lights.each {light ->
    	light.refresh()
		def type = state.lightCapabilities[light.id]

		updateSetting("onoff_${sceneId}_${light.id}", light.currentValue("switch") == "on")

		if (type == "level") {
			updateSetting("level_${sceneId}_${light.id}", closestLevel(light.currentValue('level')))
		}
		else if (type == "color") {
			updateSetting("level_${sceneId}_${light.id}", closestLevel(light.currentValue('level')))
			updateSetting("color_${sceneId}_${light.id}", "${light.currentValue("hue")}/${light.currentValue("saturation")}")
		}
	}
}


private restoreStates(sceneId) {
	log.trace "restoreStates($sceneId)"
	getDeviceCapabilities()

	lights.each { light ->
		def type = state.lightCapabilities[light.id]

		def isOn = settings."onoff_${sceneId}_${light.id}"
		log.debug "${light.displayName} is '$isOn'"
		if (isOn == "true") {
			light.on()
		} else if (isOn == "false") {
			light.off()
		}

		if (type != "switch") {
			def level = switchLevel(sceneId, light)

			if (type == "level") {
				log.debug "${light.displayName} level is '$level'"
				if (level != null) {
					light.setLevel(level)
				}
			}
			else if (type == "color") {
				def segs = settings."color_${sceneId}_${light.id}"?.split("/")
				if (segs?.size() == 2) {
					def hue = segs[0].toInteger()
					def saturation = segs[1].toInteger()
					log.debug "${light.displayName} color is level: $level, hue: $hue, sat: $saturation"
					if (level != null) {
						light.setColor(level: level, hue: hue, saturation: saturation)
					}
					else {
						light.setColor(hue: hue, saturation: saturation)
					}
				}
				else {
					log.debug "${light.displayName} level is '$level'"
					if (level != null) {
						light.setLevel(level)
					}
				}
			}
			else {
				log.error "Unknown type '$type'"
			}
		}


	}
}

private switchLevel(sceneId, light) {
	def percent = settings."level_${sceneId}_${light.id}"
	if (percent) {
		percent[0..-2].toInteger()
	}
	else {
		null
	}
}

private getDeviceCapabilities() {
	def caps = [:]
	lights.each {
		if (it.hasCapability("Color Control")) {
			caps[it.id] = "color"
		}
		else if (it.hasCapability("Switch Level")) {
			caps[it.id] = "level"
		}
		else {
			caps[it.id] = "switch"
		}
	}
	state.lightCapabilities = caps
}

private getLevels() {
	def levels = []
	for (int i = 0; i <= 100; i += 5) {
		levels << "$i%"
	}
	levels
}

private getOrientation(xyz=null) {
	def value = xyz ?: cube.currentValue("threeAxis")
    
    def asbX = Math.abs(value.x)
    def asbY = Math.abs(value.y)
    def asbZ = Math.abs(value.z)
    def max = Math.max(Math.max(asbX, asbY), asbZ)

	def orientation = 0
    if (max == asbX) {
    	if (value.x > 0) {
			orientation = 1
        } else {
			orientation = 2
        }
    } else if (max == asbY) {
    	if (value.y > 0) {
			orientation = 3
        } else {
			orientation = 4
        }
    } else if (max == asbZ) {
        if (value.z > 0) {
			orientation = 5
        } else {
			orientation = 6
        }
    }
	
	orientation
}

private sceneName(num) {
	final names = ["UNDEFINED","One","Two","Three","Four","Five","Six"]
	settings."sceneName${num}" ?: "Scene ${names[num]}"
}
