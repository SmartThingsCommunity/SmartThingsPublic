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
	definition (name: "Door Control Capability", namespace: "capabilities", author: "smartthings") {
		capability "Door Control"
	}

	simulator {
		status "unknown": "doorControl:unknown"
		status "closed": "doorControl:closed"
        status "open": "doorControl:open"
        status "closing":"doorControl:closing"
        status "opening": "doorControl:opening"
	}

	tiles {
		standardTile("doorControl", "device.doorControl", width: 2, height: 2) {
			state("unknown", label:'${name}', backgroundColor:"#ffffff")
			state("closed", label:'${name}', backgroundColor:"#ffffff")
			state("open", label:'${name}', backgroundColor:"#ffffff")
			state("closing", label:'${name}', backgroundColor:"#ffffff")
			state("opening", label:'${name}', backgroundColor:"#ffffff")
		}

		main "doorControl"
		details "doorControl"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}