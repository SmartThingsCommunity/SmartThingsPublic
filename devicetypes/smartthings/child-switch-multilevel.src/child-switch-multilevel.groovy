/**
 *  Copyright 2020 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition(name: "Child Switch Multilevel", namespace: "smartthings", author: "SmartThings") {
		capability "Switch Level"
		capability "Actuator"
		capability "Sensor"
	}

	tiles(scale: 2) {
		valueTile("level", "device.level", width: 4, height: 1) {
			state "level", label: 'Level: ${currentValue}% ', defaultState: true
		}

		main "level"
		details(["level"])
	}
}

def installed() {
	parent.multilevelChildInstalled(device.deviceNetworkId)
}

def setLevel(level) {
	def currentLevel = Integer.parseInt(device.currentState("level").value)
	parent.setLevelChild(level, device.deviceNetworkId, currentLevel)
}
