/**
 *  Virtual IOT8Z
 *
 *  Copyright 2021 Luis Contreras
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
	definition (name: "IOT8-Z-child-analog-contact-switch", namespace: "Smartenit", author: "Luis Contreras", cstHandler: true, mnmn: "SmartThingsCommunity", vid: "50830b33-69d6-32a0-bebd-952eac44d074") {
		capability "Contact Sensor"
		capability "Sensor"
		capability "Switch"
		capability "monthpublic25501.analogSensor"
	}
}

// handle commands
def on() {
	parent.childOn(device.deviceNetworkId)
}

def off() {
	parent.childOff(device.deviceNetworkId)
}