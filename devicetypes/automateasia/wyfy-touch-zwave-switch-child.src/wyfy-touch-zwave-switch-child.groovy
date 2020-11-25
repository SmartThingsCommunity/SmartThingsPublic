/**
 *  WYFY TOUCH ZWAVE SWITCH CHILD
 *
 *  Copyright 2020 Edwin Tan Poh Heng
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
	definition (name: "WYFY TOUCH ZWAVE SWITCH CHILD", namespace: "AutomateAsia", author: "Edwin Tan Poh Heng") {
		capability "Switch"	
		capability "Refresh"	
		capability "Health Check"
	}
}

/*
######################
## Command handlers ##
######################
*/

def on() {
	parent.childOn(device.deviceNetworkId)
}

def off() {
	parent.childOff(device.deviceNetworkId)
}

def refresh() {
	parent.childRefresh(device.deviceNetworkId)
}

def ping() {
	parent.childRefresh(device.deviceNetworkId)
}

/*
###############################
## Device Lifecycle handlers ##
###############################
*/

def installed(){
	sendEvent(name: "checkInterval", value: 1920, displayed: false, data: [protocol: "zwave", hubHardwareId: parent.hubID])
}