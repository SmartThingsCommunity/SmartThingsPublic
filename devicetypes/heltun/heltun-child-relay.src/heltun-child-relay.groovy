/**
 * 
 *
 *  Copyright 2021 Sarkis Kabrailian
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "Heltun Child Relay", namespace: "HELTUN", author: "Sarkis Kabrailian", cstHandler: true, ocfDeviceType: "oic.d.switch") {
		capability "Switch"
		capability "Power Meter"
		capability "Refresh"
		capability "Health Check"
	}
}

def ping() {
	parent.refresh()
}

def on() {
	parent.childOn(device.deviceNetworkId)
}

def off() {
	parent.childOff(device.deviceNetworkId)
}

def refresh() {
	parent.refresh()
}