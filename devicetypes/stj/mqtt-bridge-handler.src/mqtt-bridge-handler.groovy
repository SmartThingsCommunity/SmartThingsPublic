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
    definition (name: "MQTT Bridge Handler", namespace: "stj", author: "St. John Johnson and Jeremiah Wuenschel", runLocally: true, executeCommandsLocally: true) {
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

// test method for getting some 
def getConf(String cid) {
	switch (cid) {
    	case 'ip':
        	return "$settings.ip"
        	break
    	case 'port':
        	return "$settings.port"
        	break
    	case 'mac':
        	return "$settings.mac"
        	break
        default:
        	return 'Invalid config id'
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
	log.debug "MBH: parse: '${description}'"
    setNetworkAddress()

    log.debug "MBH: Parsing '${description}'"
    def msg = parseLanMessage(description)

    return createEvent(name: "message", value: new JsonOutput().toJson(msg.data))
}

// the below calledBackHandler() is triggered when the device responds to the sendHubCommand() with "device_description.xml" resource
void calledBackHandler(physicalgraph.device.HubResponse hubResponse) {
    log.debug "Entered calledBackHandler()..."
    def body = hubResponse.xml
    def devices = getDevices()
    def device = devices.find { it?.key?.contains(body?.device?.UDN?.text()) }
    if (device) {
        device.value << [name: body?.device?.roomName?.text(), model: body?.device?.modelName?.text(), serialNumber: body?.device?.serialNum?.text(), verified: true]
    }
    log.debug "device in calledBackHandler() is: ${device}"
    log.debug "body in calledBackHandler() is: ${body}"
}

// Send message to the Bridge
def deviceNotification(message) {
	log.debug "MBH: deviceNotification: '${message}'"
    if (device.hub == null) {
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

	// this action will "POST" the "parsed.body" to the MQTT server "parsed.path"
    log.debug "MBH: deviceNotification(): parsed.path='${parsed.path}'"
    log.debug "MBH: deviceNotification(): parsed.body='${parsed.body}'"
    def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: parsed.path,
        headers: headers,
        body: parsed.body,
        callback: calledBackHandler
    )
    hubAction()
    return 1

}