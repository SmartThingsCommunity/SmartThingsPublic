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
	definition (name: "Carbon Dioxide Measurement Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Carbon Dioxide Measurement"
	}

	simulator {
		for (i in [250,350,500,800,1000,2000,5000,10000,20000,40000]) {
			status "${i} parts-per-million (ppm)": "carbonDioxide:${i}"
		}
	}

	tiles {
		valueTile("carbonDioxide", "device.carbonDioxide", width: 2, height: 2) {
			state "carbonDioxide", label:'${currentValue} ${unit}', unit:"ppm"
		}
		main(["carbonDioxide"])
		details(["carbonDioxide"])
	}
}

// Parse incoming device messages to generate events
def parse(String description)
{
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}