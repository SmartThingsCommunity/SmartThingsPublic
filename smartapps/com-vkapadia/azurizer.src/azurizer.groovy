/**
 *  Azurizer
 *
 *  Copyright 2017 Vinay Kapadia
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

include 'asynchttp_v1'


definition(
    name: "Azurizer",
    namespace: "com.vkapadia",
    author: "Vinay Kapadia",
    description: "Sends a device update message to the cloud. It will POST a JSON object to the URL you set in the options. The object looks like {\"Device\":\"Device Label\",\"Value\":\"on/off\",\"Key\":\"your key\"}. The Device is the label of the device that changed. The Value is the new value of the device. The Key is whatever key you specify in the options, used to ensure that you code only responds to requests sent by this smartapp.",
    category: "My Apps",
    iconUrl: "https://kapadiahome.blob.core.windows.net/files/azurizer.png",
    iconX2Url: "https://kapadiahome.blob.core.windows.net/files/azurizer.png",
    iconX3Url: "https://kapadiahome.blob.core.windows.net/files/azurizer.png")


preferences {
	section("Function") {
		input "funcurl", "string", required: true, title: "URL"
        input "funckey", "string", required: true, title: "Key"
	}
    section("Devices to Track") {
        input "switches", "capability.switch", multiple: true, required: false
        input "sensors", "capability.sensor", multiple: true, required: false
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(switches, "switch", switchesHandler)
}

def switchesHandler(evt) {
    log.debug "${evt.device.label} set to ${evt.value}"

    def params = [
        uri: funcurl,
        body: [
            Device: evt.device.label,
            Value: evt.value,
            Key: funckey
        ]
    ]
    
    asynchttp_v1.post(processResponse, params)
}

def processResponse(response, data) {
    def status = response.status
    
    switch (status) {
        case 200:
            log.debug "200 returned"
            break
        case 304:
            log.debug "304 returned"
            break
        default:
            log.warn "no handling for response with status $status"
            break
    }
}