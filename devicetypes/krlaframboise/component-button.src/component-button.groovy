/**
 *  Component Button v1.0 (CHILD DEVICE)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.0 (03/02/2020)
 *      - Initial Release
 *
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
	definition (name: "Component Button", namespace: "krlaframboise", author: "Kevin LaFramboise", ocfDeviceType: "x.com.st.d.remotecontroller") {
		capability "Button"
		capability "Sensor"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "button", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.button", key: "PRIMARY_CONTROL") {
				attributeState "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			}
		}
		main "button"
		details(["button"])
	}
}

def installed() {
	log.debug "installed()..."	
}

def updated() {
	log.debug "updated()..."
}

def uninstalled() {
	log.warn "uninstalled()..."
}