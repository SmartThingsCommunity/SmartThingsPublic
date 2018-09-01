/**
 *  Virtual Motion Detector
 *
 *  Copyright 2016 Michael Struck
 *  Version 1.0.0 8/29/16
 *
 *  Version 1.0.0 Initial release
 *
 *  Uses code from SmartThings
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
	definition (name: "Virtual Motion Detector", namespace: "MichaelStruck", author: "SmartThings") {
		capability "Motion Sensor"
		capability "Sensor"
        
		attribute "about", "string"
        
        command "active"
        command "inactive"
	}
	// simulator metadata
	simulator {
	}
	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4, canChangeIcon: true, canChangeBackground: true) {
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#CCCCCC"
			}
        }
        standardTile("motionBTN", "device.motion", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Set Motion', action: active, icon:"st.motion.motion.active"
		}
        standardTile("space", "", decoration: "flat", width: 2, height: 2) 
		standardTile("noMotionBTN", "device.nomotion", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state "default", label:'Set No Motion', action: inactive, icon:"st.contact.contact.closed"
		}
        valueTile("aboutTxt", "device.about", inactiveLabel: false, decoration: "flat", width: 6, height:2) {
            state "default", label:'${currentValue}'
		}
        main "motion"
		details (["motion","motionBTN","space", "noMotionBTN", "aboutTxt"])
	}
}
def installed() {
	showVersion()	
}
def active() {
	sendEvent(name: "motion", value: "active", isStateChange: true, display: false)
    log.debug "Motion Sensor Set to Active"
    showVersion()
}
def inactive() {
	sendEvent(name: "motion", value: "inactive", isStateChange: true, display: false)
    showVersion()
}
def showVersion(){
	def versionTxt = "${appName()}: ${versionNum()}\n"
    try {if (parent.getSwitchAbout()){versionTxt += parent.getSwitchAbout()}}
    catch(e){versionTxt +="Installed from the SmartThings IDE"}
	sendEvent (name: "about", value:versionTxt) 
}
def versionNum(){
	def txt = "1.0.0 (08/29/16)"
}
def appName(){
	def txt="Virtual Motion Detector"
}