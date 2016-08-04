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
	definition (name: "Consumable Capability", namespace: "capabilities", author: "Bob@SmartThings") {
		capability "Consumable"
	}

	simulator {
    	//["missing","good","replace","maintenance_required","order"]
		status "missing": "consumable:missing"
		status "good": "consumable:good"
        status "replace": "consumable:replace"
        status "maintenance_required":"consumable:maintenance_required"
        status "order": "consumable:order"
	}

	tiles {
		standardTile("consumable", "device.consumable", width: 2, height: 2) {
			state("missing", label:'${name}', backgroundColor:"#ffffff")
			state("good", label:'${name}', backgroundColor:"#53a7c0")
			state("replace", label:'${name}', backgroundColor:"#ffffff")
			state("maintenance_required", label:'${name}', backgroundColor:"#ffffff")
			state("order", label:'${name}', backgroundColor:"#ffffff")
		}

		main "consumable"
		details "consumable"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}