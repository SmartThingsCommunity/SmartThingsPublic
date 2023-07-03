/**
 *  MQTT Bridge
 *
 * 	Authors
 *   - st.john.johnson@gmail.com
 *   - jeremiah.wuenschel@gmail.com
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
    definition (name: "MQTT Bridge", namespace: "stj", author: "St. John Johnson and Jeremiah Wuenschel") {
        capability "Notification"
    }

    preferences {
        input("ip", "string",
            title: "MQTT Bridge IP Address",
            description: "MQTT Bridge IP Address",
            required: true,
            displayDuringSetup: true
        )
        input("port", "string",
            title: "MQTT Bridge Port",
            description: "MQTT Bridge Port",
            required: true,
            displayDuringSetup: true
        )
        input("mac", "string",
            title: "MQTT Bridge MAC Address",
            description: "MQTT Bridge MAC Address",
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

// Parse events from the Bridge
def parse(String description) {
    setNetworkAddress()

    log.debug "Parsing '${description}'"
    def msg = parseLanMessage(description)

    return createEvent(name: "message", value: new JsonOutput().toJson(msg.data))
}

// Send message to the Bridge
def deviceNotification(message) {
    if (device.hub == null)
    {
        log.error "Hub is null, must set the hub in the device settings so we can get local hub IP and port"
        return
    }
    
    log.debug "Sending '${message}' to device"
    setNetworkAddress()

    def slurper = new JsonSlurper()
    def parsed = slurper.parseText(message)
    
    if (parsed.path == '/subscribe') {
        parsed.body.callback = device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
    }

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