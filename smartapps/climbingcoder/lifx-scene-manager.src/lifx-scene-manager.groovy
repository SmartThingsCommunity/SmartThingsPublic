/**
 *  LifX Scene Manager
 *
 *  Copyright 2016 Patrick Killian
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
    name: "LifX Scene Manager",
    namespace: "climbingcoder",
    author: "Patrick Killian",
    description: "When your mode changes, automatically activate a Lifx lighting scene with the same name",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Configuration") {
		input(name:"apikey", type:"text", title:"LifX API Token", required:true)
        input(name:"fadeTime", type:"number", title:"Fade Time", description:"Seconds of fade between scenes", required:true)
	}
    section("Getting a LifX API Token") {
        paragraph "1. Go to https://cloud.lifx.com/sign_in and sign in with your LifX account"
        paragraph "2. Click on your email address and then choose settings from the drop down menu"
        paragraph "3. Click 'Generate Token' and give your token any name"
        paragraph "4. Copy your token to the required field above and save it somewhere else for reference"
    }
}

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
	subscribe(location, "mode", modeChangeHandler)
}


def modeChangeHandler(evt) {
    log.debug "mode changed to ${evt.value}"
    def sceneUUID = ""
    sceneUUID = findMatchingLifxScene("${evt.value}")
    if (sceneUUID == "") {
    	log.debug "No matching scene found"
    } else {
    	activateLifxScene("${sceneUUID}")
    }
}


/**
* Returns the UUID of a LifX scene that matches
*   the passed in sceneName by obtaining a list
*   of all available scenes and comparing them
*   to the passed in name
*
* @param sceneName - the name of the desired scene
*
* @return UUID - the UUID of any matching scenes found
*/
def findMatchingLifxScene(sceneName) {
	log.debug ("Getting list of LifX scenes")
	def params = [
    	headers: ["Authorization": "Bearer ${settings.apikey}"],
    	uri: "https://api.lifx.com/v1/scenes",
        body: []
	]
    def uuid = ""

	try {
   		httpGet(params) { resp ->
        	log.debug "response status code: ${resp.status}"
            resp.data.each {
            	log.debug("Found scene '${it.name}'")
            	if ("${it.name}" == sceneName) {
                	uuid = "${it.uuid}"
                }
            }
    	}
	} catch (e) {
    	log.error "something went wrong: $e"
	} finally {
    	return uuid
    }
}



/**
* Calls the Lifx http endpoint to activate a scene with
*  the given UUID
*
* @param UUID - the UUID of the scene to activate
*/
def activateLifxScene(UUID) {
	log.debug "Activating scene with UUID '${UUID}'"
	def params = [
    	headers: ["Authorization": "Bearer ${settings.apikey}"],
    	uri: "https://api.lifx.com/v1/scenes/scene_id:${UUID}/activate",
        body: [
        	duration: "${settings.fadeTime}"
        ]
	]

	try {
   		httpPut(params) { resp ->
        	log.debug "response status code: ${resp.status}"
    	}
	} catch (e) {
    	log.error "something went wrong: $e"
	}
}


