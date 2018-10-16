/**
 *            Copyright 2018 SmartThings
 *
 *            Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *            in compliance with the License. You may obtain a copy of the License at:
 *
 *                            http://www.apache.org/licenses/LICENSE-2.0
 *
 *            Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *            on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *            for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition(name: "Z-Wave Open Close For Lock Child", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-contact-3", ocfDeviceType: "x.com.st.d.sensor.contact") {
		capability "Contact Sensor"
		capability "Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Health Check"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "contact", type: "generic", width: 6, height: 4) {
			tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
				attributeState("open", slabel: '${name}', icon: "st.contact.contfact.open", backgroundColor: "#e86d13")
				attributeState("closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		main "contact"
		details(["contact", "battery"])
	}
}

def sendCommand(cmd) {
	parent.sendCommand(name: "battery", value: cmd.batteryLevel)
}
