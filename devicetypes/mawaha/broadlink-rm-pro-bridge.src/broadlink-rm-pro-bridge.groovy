/**
 *  Broadlink RM Bridge Device Handler
 *
 *  Copyright 2018 MaWaHa
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
	
    definition (name: "Broadlink RM Pro Bridge", namespace: "mawaha", author: "Matthew Hadwen") {
		capability "Switch"
        attribute "learn", "enum", ["start", "learning", "stop"]
        attribute "store", "enum", ["start", "storing", "stop"]
		command "startLearn"
		command "endLearn"
        command "startStore"
        command "endStore"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Activate', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: 'Active', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
        standardTile("learn", "learn", decoration: "flat") {
        	state "stop", label: 'Learn', action: "startLearn", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        	state "learning", label: 'Learning', icon: "st.switches.switch.on", backgroundColor: "#ea581e"
            state "start", label: 'Start', action: "endLearn", icon: "st.switches.switch.on", backgroundColor: "#79b821"
        }
        standardTile("store", "store", decoration: "flat") {
        	state "stop", label: 'Store', action: "startStore", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "storing", label: 'Storing', action: "endStore", icon: "st.switches.switch.on", backgroundColor: "#ea581e"
            state "start", label: 'Storing', action: "endStore", icon: "st.switches.switch.on", backgroundColor: "#79b821"
        }
        main "switch"
		details(["switch", "learn", "store"])
	}

    preferences {
    	input "host", "text", required: true, title: "Hostname", description: "The ip address of your RM Bridge", displayDuringSetup: true
    	input "mac", "text", required: true, title: "MAC Address", description: "The mac address of your RM Bridge", displayDuringSetup: true
    	input "username", "text", title: "Username", description: "Username [optional]", displayDuringSetup: true
    	input "password", "text", title: "Password", description: "Password [optional]", displayDuringSetup: true
    }
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def logResponse(res) {
	log.debug(res.body)
}

def toJSON(String str) {
    def slurp = new JsonSlurper()
    return slurp.parseText(str)
}

def stringify(Map input) {
	return JsonOutput.toJson(input)
}

def on() {
	sendEvent(name: "switch", value: "on")
	activate()
}

def off(res) {
	sendEvent(name: "switch", value: "off")
}

def callback(res) {
	sendEvent(name: "switch", value: "off")

    def slurper = new JsonSlurper()
    def jsonRes = slurper.parseText(res.body)
}

def startLearn(res) {
	sendEvent(name: "learn", value: "start")
    learn()
}

def endLearn(res) {
	sendEvent(name: "learn", value: "stop")
}

private learn() {

	return new physicalgraph.device.HubAction([
		method: "POST",
        headers: [
        	HOST: "${host}",
            "Content-Type": "application/json"
        ],
        body: stringify([
            api_id: 1002,
            command: learn_code,
            mac: "${mac}"
        ])
    ], device.deviceNetworkId, [callback: endLearn])
}

def startStore() {
	sendEvent(name: "store", value: "start")
    storing()
}

def endStore(res) {
	sendEvent(name: "store", value: "stop")
}

def stored(res) {
    def json = toJSON(res.body)
    def data = json.data
    def deviceName = device.label.toLowerCase().replaceAll(' ', '')

	return sendHubCommand(new physicalgraph.device.HubAction([
		method: "POST",
        headers: [
        	HOST: "${host}",
            "Content-Type": "application/json"
        ],
        body: stringify ([
                api_id: 1007,
                command: "add_code",
                list: [[
                    data: data,
                    mac: "${mac}",
                    name: deviceName
                ]]
            ]),
    ], device.deviceNetworkId, [callback: endStore]))
}

private storing() {
    def deviceName = device.label.toLowerCase().replaceAll(' ', '')

	return new physicalgraph.device.HubAction([
		method: "POST",
        headers: [
        	HOST: "${host}",
            "Content-Type": "application/json"
        ],
        body: stringify([
            api_id: 1003,
            command: get_code,
            mac: "${mac}",
            name: deviceName
        ]),
    ], device.deviceNetworkId, [callback: stored])
}

private activate() {

    def userpassascii= "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def deviceName = device.label.toLowerCase().replaceAll(' ', '')

 	return new physicalgraph.device.HubAction([
		method: "GET",
        path: "/code/${deviceName}",
        headers: [
        	HOST: "${host}",
            AUTHORIZATION: "${userpass}"
        ],
    ], device.deviceNetworkId, [callback: off])
}

private listCodes() {

    def userpassascii= "yourusername:yourpassword"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()

	return new physicalgraph.device.HubAction([
		method: "POST",
        headers: [
        	HOST: "${host}",
            "Content-Type": "application/json"
        ],
        body: stringify([
            api_id: 1006,
            command: "list_codes"
        ])
    ], device.deviceNetworkId, [callback: callback])
}