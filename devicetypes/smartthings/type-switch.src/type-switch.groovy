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
	definition(name: "Type Switch", namespace: "smartthings", author: "SmartThings") {
		// This is a no capability device type handler.
		// It serves as a workaround for OneApp's inability to refresh Device Page after changes in child devices list.
		// Currently, device page is defined right after device's inclusion and also after DTH change, and that second
		// behaviour is utilized here.
	}
}

def updated() {
	runIn(2, "switchDTH")
}

private switchDTH() {
	setDeviceType(getState("deviceTypeName"))
}

private getState(key) {
	String childDni = "${device.deviceNetworkId}:1"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	child?.getState(key)
}