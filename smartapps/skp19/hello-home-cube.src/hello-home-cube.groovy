/**
 *  Hello Home Cube
 *
 *  Copyright 2015 skp19
 *
 *
 */

/************
 * Metadata *
 ************/
definition(
	name: "Hello Home Cube",
	namespace: "skp19",
	author: "skp19",
	description: "Run a Hello Home action by rotating a cube containing a SmartSense Multi",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

import groovy.json.JsonSlurper

/**********
 * Setup  *
 **********/
preferences {
	page(name: "mainPage", title: "", nextPage: "scenesPage", uninstall: true) {
		section("Use the orientation of this cube") {
			input "cube", "capability.threeAxis", required: false, title: "SmartSense Multi sensor"
		}
		section([title: " ", mobileOnly:true]) {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
		}
	}
	page(name: "scenesPage", title: "Scenes", install: true, uninstall: true)
}


def scenesPage() {
	log.debug "scenesPage()"
	def sceneId = getOrientation()
	dynamicPage(name:"scenesPage") {
    	def phrases = location.helloHome?.getPhrases()*.label
		section {
			phrases.sort()
			input name: "homeAction1", type: "enum", title: "${1}. ${sceneName(1)}${sceneId==1 ? ' (current)' : ''}", required: false, options: phrases
            input name: "homeAction2", type: "enum", title: "${2}. ${sceneName(2)}${sceneId==2 ? ' (current)' : ''}", required: false, options: phrases
            input name: "homeAction3", type: "enum", title: "${3}. ${sceneName(3)}${sceneId==3 ? ' (current)' : ''}", required: false, options: phrases
            input name: "homeAction4", type: "enum", title: "${4}. ${sceneName(4)}${sceneId==4 ? ' (current)' : ''}", required: false, options: phrases
            input name: "homeAction5", type: "enum", title: "${5}. ${sceneName(5)}${sceneId==5 ? ' (current)' : ''}", required: false, options: phrases
            input name: "homeAction6", type: "enum", title: "${6}. ${sceneName(6)}${sceneId==6 ? ' (current)' : ''}", required: false, options: phrases
		}
		section {
			href "scenesPage", title: "Refresh", description: ""
		}
	}
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
		runHomeAction(sceneId)
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

private runHomeAction(sceneId) {
	log.trace "runHomeAction($sceneId)"
    
    //RUN HELLO HOME ACTION
	def homeAction
	if (sceneId == 1) {
    	homeAction = homeAction1
    }
    if (sceneId == 2) {
    	homeAction = homeAction2
    }
    if (sceneId == 3) {
    	homeAction = homeAction3
    }
    if (sceneId == 4) {
    	homeAction = homeAction4
    }
    if (sceneId == 5) {
    	homeAction = homeAction5
    }
    if (sceneId == 6) {
    	homeAction = homeAction6
    }

	if (homeAction) {
	    location.helloHome.execute(homeAction)
        log.trace "Running Home Action: $homeAction"
    }
    else {
    	log.trace "No Home Action Defined for Current State"
    }
}

private getOrientation(xyz=null) {
	final threshold = 250

	def value = xyz ?: cube.currentValue("threeAxis")

	def x = Math.abs(value.x) > threshold ? (value.x > 0 ? 1 : -1) : 0
	def y = Math.abs(value.y) > threshold ? (value.y > 0 ? 1 : -1) : 0
	def z = Math.abs(value.z) > threshold ? (value.z > 0 ? 1 : -1) : 0

	def orientation = 0
	if (z > 0) {
		if (x == 0 && y == 0) {
			orientation = 1
		}
	}
	else if (z < 0) {
		if (x == 0 && y == 0) {
			orientation = 2
		}
	}
	else {
		if (x > 0) {
			if (y == 0) {
				orientation = 3
			}
		}
		else if (x < 0) {
			if (y == 0) {
				orientation = 4
			}
		}
		else {
			if (y > 0) {
				orientation = 5
			}
			else if (y < 0) {
				orientation = 6
			}
		}
	}

	orientation
}

private sceneName(num) {
	final names = ["UNDEFINED","One","Two","Three","Four","Five","Six"]
	settings."sceneName${num}" ?: "Scene ${names[num]}"
}