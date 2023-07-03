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
 */
metadata {
	definition (name: "Window Shade Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Window Shade"
	}

	// simulator metadata
	simulator {
		// status messages
		status "closing": "windowShade:closing"
        status "close": "windowShade:close"
		status "open": "windowShade:open"
        status "opening": "windowShade:opening"
        status "partially open": "windowShade:partially open"
        status "unknown": "windowShade:unknown"

		// reply messages
		reply "closing": "windowShade:closing"
        reply "close": "windowShade:close"
		reply "open": "windowShade:open"
        reply "opening": "windowShade:opening"
        reply "partially open": "windowShade:partially open"
        reply "unknown": "windowShade:unknown"
	}

	// UI tile definitions
	tiles {
		standardTile("windowShade", "device.windowShade", width: 2, height: 2, canChangeIcon: true) {
            state "close", label: '${name}', action: "windowShade.close", backgroundColor: "#000000", nextState: "opening"
			state "closing", label: '${name}', action: "windowShade.close", backgroundColor: "#00039a", nextState: "closed"
			state "open", label: '${name}', action: "windowShade.open", backgroundColor: "#ffffff", nextState: "closing"
            state "opening", label: '${name}', action: "windowShade.open", backgroundColor: "#ffedc2", nextState: "open"
            state "partially open", label: '${name}', action: "windowShade.partially open", backgroundColor: "#0a5eff"
            state "unknown", label: '${name}', action: "windowShade.unknown", backgroundColor: "#ffa500"
		}
		main "windowShade"
		details "windowShade"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def open() {
	'open'
}

def close() {
	'close'
}

def presetPosition() {
    'partially open'
}
