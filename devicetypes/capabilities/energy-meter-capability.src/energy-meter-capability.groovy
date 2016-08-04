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
	definition (name: "Energy Meter Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Energy Meter"
	}

	simulator {
		for (i in [0,5,10,15,20,30,40,50,100,200,300,400,600,800,1000]) {
			status "${i}": "energy:${i}"
		}
	}

	tiles {
		valueTile("energyMeter", "device.energy", width: 2, height: 2) {
			state "energyMeter", label:'${currentValue} ${unit}', unit:"Watts"
		}
		main(["energyMeter"])
		details(["energyMeter"])
	}
}

// Parse incoming device messages to generate events
def parse(String description)
{
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}