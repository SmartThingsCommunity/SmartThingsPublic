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
//Thing is used while a device is in the process of being joined

metadata {
	definition (name: "Thing", namespace: "smartthings", author: "SmartThings") {
	}

	// simulator metadata
	simulator {
		// Not Applicable to Thing Device
	}

	// UI tile definitions
	tiles {
		standardTile("thing", "device.thing", width: 2, height: 2) {
			state(name:"default", icon: "st.unknown.thing.thing-circle", label: "Please Wait")
		}

		main "thing"
		details "thing"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	// None
}
