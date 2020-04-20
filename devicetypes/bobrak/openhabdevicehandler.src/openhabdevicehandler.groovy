/**
 *  OpenHAB Bridge
 *
 *  Authors
 *   - st.john.johnson@gmail.com
 *   - jeremiah.wuenschel@gmail.com
 *   - rjraker@gmail.com - 1/30/17 - modified to work with OpenHAB
 *
 *  Copyright 2016
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

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

metadata {
    definition (name: "OpenHabDeviceHandler", namespace: "bobrak", author: "St. John Johnson, Jeremiah Wuenschel and Bob Raker") {
        capability "Notification"
    }

    preferences {
        input("ip", "string",
            title: "OpenHAB IP Address",
            description: "OpenHAB IP Address",
            required: true,
            displayDuringSetup: true
        )
        input("port", "string",
            title: "OpenHAB Port",
            description: "OpenHAB Port",
            required: true,
            displayDuringSetup: true
        )
        input("mac", "string",
            title: "OpenHAB MAC Address",
            description: "MAC Address of OpenHAB server",
            required: true,
            displayDuringSetup: true
        )
    }

    simulator {}

    tiles {
        valueTile("basic", "device.ip", width: 3, height: 2) {
            state("basic", label:'OK')
        }
        main "basic"
    }
}

// Store the MAC address as the device ID so that it can talk to SmartThings
def setNetworkAddress() {
    // Setting Network Device Id
    def hex = "$settings.mac".toUpperCase().replaceAll(':', '')
    if (device.deviceNetworkId != "$hex") {
        device.deviceNetworkId = "$hex"
        log.debug "Device Network Id set to ${device.deviceNetworkId}"
    }
}

// Parse events from OpenHAB
def parse(String description) {
    setNetworkAddress()

    def msg = parseLanMessage(description)
    log.debug "Msg '${msg}'"

    if (msg.header.contains(' /update ')) {
        msg.data.path = "update"
     } else if (msg.header.contains(' /state ')) {
        msg.data.path = "state"
     } else if (msg.header.contains(' /discovery ')) {
        msg.data = [path: "discovery"]
     } else {
        if ( msg.status == 204 ) {
            // This would be a response from OpenHAB to the last message - it return 204 since there is nothing to do
            return
        }
        log.error "received a request with an unknown path: ${msg.header}"
        return
     }
     
     log.debug "Creating event with message: ${msg.data}"
     // Setting parameter isStateChange to true causes the event to be propigated even if the state has not changed.
     return createEvent(name: 'message', value: new JsonOutput().toJson(msg.data), isStateChange: true)
}

def installed() {
    def ip = device.hub.getDataValue("localIP")
    def port = device.hub.getDataValue("localSrvPortTCP")
    log.debug "HTTP Bridge device handler installed. Listening on ${ip} + ":" + ${port}"
}

// Send message to OpenHAB
def deviceNotification(message) {
    if (device.hub == null) {
        log.error "Hub is null, must set the hub in the device settings so we can get local hub IP and port"
        return
    }
    
    log.debug "Sending '${message}' to device"
    setNetworkAddress()

    def slurper = new JsonSlurper()
    def parsed = slurper.parseText(message)
    
    def headers = [:]
    headers.put("HOST", "$ip:$port")
    headers.put("Content-Type", "application/json")

    def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: parsed.path,
        headers: headers,
        body: parsed.body
    )
    hubAction
}