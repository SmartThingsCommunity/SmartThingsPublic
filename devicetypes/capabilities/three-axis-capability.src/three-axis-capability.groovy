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
	definition (name: "Three Axis Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Three Axis"
	}

	simulator {
		status "x,y,z: 0,0,0": "threeAxis:0,0,0"
		status "x,y,z: 1000,0,0": "threeAxis:1000,0,0"
		status "x,y,z: 0,1000,0": "threeAxis:0,1000,0"
		status "x,y,z: 0,0,1000": "xthreeAxis:0,0,1000"
		status "x,y,z: -1000,0,0": "threeAxis:-1000,0,0"
		status "x,y,z: 0,-1000,0": "threeAxis:0,-1000,0"
		status "x,y,z: 0,0,-1000": "xthreeAxis:0,0,-1000"
	}

	tiles {
		valueTile("3axis", "device.threeAxis", decoration: "flat") {
			state("threeAxis", label:'${currentValue}', unit:"", backgroundColor:"#ffffff")
		}

		main "3axis"
		details "3axis"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}