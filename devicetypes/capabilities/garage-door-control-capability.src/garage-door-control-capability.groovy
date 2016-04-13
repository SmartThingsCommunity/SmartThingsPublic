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
	definition (name: "Garage Door Control Capability", namespace: "capabilities", author: "smartthings") {
		capability "Garage Door Control"
	}

	simulator {
		status "unknown": "garageDoorControl:unknown"
		status "closed": "garageDoorControl:closed"
        status "open": "garageDoorControl:open"
        status "closing":"garageDoorControl:closing"
        status "opening": "garageDoorControl:opening"
	}

	tiles {
		standardTile("garageDoorControl", "device.garageDoorControl", width: 2, height: 2) {
			state("unknown", label:'${name}', backgroundColor:"#ffffff")
			state("closed", label:'${name}', backgroundColor:"#ffffff")
			state("open", label:'${name}', backgroundColor:"#ffffff")
			state("closing", label:'${name}', backgroundColor:"#ffffff")
			state("opening", label:'${name}', backgroundColor:"#ffffff")
		}

		main "garageDoorControl"
		details "garageDoorControl"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}