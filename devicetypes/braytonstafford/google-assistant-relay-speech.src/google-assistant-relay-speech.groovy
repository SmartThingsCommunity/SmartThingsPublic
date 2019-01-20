/**
 *  Google Assistant Relay Speech
 *
 *  Copyright 2018 Brayton Stafford
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
	preferences {
		section("Google Assistant Relay Host Information") {
			input "garHost", "string", title: "Google Assistant Relay Hostname or IP Address", multiple: false, required: true
			input "garPort", "number", title: "Google Assistant Relay Port", multiple: false, required: true
		}
	}

	definition (name: "Google Assistant Relay Speech", namespace: "braytonstafford", author: "Brayton Stafford") {
		capability "Speech Synthesis"
		capability "Notification"
	}

	tiles(scale: 2) {
        // multi-line text (explicit newlines)
        standardTile("multiLine", "device.multiLine", width: 3, height: 2) {
            state "multiLine", label: "Go to settings to configure the hostname/IP and port for your Google Assistant Relay", defaultState: true
        }

	}
}

def parse(String description) {
	// log.debug "Parsing '${description}'"

}

def speak(message) {
	// log.debug "Executing 'speak'"
	try {
		def myJson = "{ \"command\": \"${message}\",\"broadcast\": true }"

		def headers = [:]
		headers.put("HOST", "$garHost:$garPort")
		headers.put("Content-Type", "application/json")

		//log.debug "The Header is $headers"

		def method = "POST"

		def path = "/assistant"

		try {
			def hubAction = new physicalgraph.device.HubAction(
					[
							method: method,
							path: path,
							body: myJson,
							headers: headers
					]
			)

			// log.debug hubAction
			sendHubCommand(hubAction)
		}
		catch (Exception e) {
			log.error "Hit Exception $e on $hubAction"
		}
	} catch (Exception e) {
		log.error "An error occurred while doing things: ${e}"
	}
}


def deviceNotification(message) {
	speak(message)
}

def installed() {
	sendEvent(name: "multiLine", value: "Click the settings to configure\nthe hostname/IP and port for\nyour Google Assistant Relay")
}