/**
 *  Copyright 2019 SmartThings, RBoy Apps
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
	definition(name: "Child Contact Sensor", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-contact", ocfDeviceType: "x.com.st.d.sensor.contact") {
		capability "Contact Sensor"
		capability "Sensor"
		capability "Health Check"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "contact", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
				attributeState("open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
				attributeState("closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC")
			}
		}

		main "contact"
		details(["contact"])
	}
}

def installed() {
	configure()
}

def updated() {
	configure()
}

def configure() {
	parent.configureChild()
	refresh()
}

def ping() {
	refresh()
}

def refresh() {
	parent.refreshChild()
}

def uninstalled() {
	parent.deleteChild()
}
