/**
 *  AzureEventGridConnector
 *
 *  Copyright 2018 ZIV RAFALOVICH; KALYAN KONA 
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
definition(
    name: "AzureEventGridConnector",
    namespace: "azureConnector",
    author: "ZIV RAFALOVICH; KALYAN KONA ",
    description: "Connect smartThings apps to Azure by sending events to Azure event grid. In turn, you can consumed them using Azure serverless capabilities such as Logic Apps and Azure funcitons. ",
    category: "SmartThings Labs",
    iconUrl: "https://azure.microsoft.com/svghandler/dns/?width=600&amp;amp;height=315",
    iconX2Url: "https://azure.microsoft.com/svghandler/dns/?width=600&amp;amp;height=315",
    iconX3Url: "https://azure.microsoft.com/svghandler/dns/?width=600&amp;amp;height=315") {
    appSetting "EventGridURL"
    appSetting "EventGridKey"
}

preferences {
	// For this sample, we register for switches only. You may add any other type of devices.
    section("Switches") {
        input "switches", "capability.switch", title: "Switches", multiple: true
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
    subscribe(switches, "switch", switchHandler)    
}

def switchHandler(evt) {
    if (evt.value == "on") {
        sendEventToEventGrid(evt.deviceId, evt.displayName, 'switch', 'on')
    } else if (evt.value == "off") {
        sendEventToEventGrid(evt.deviceId, evt.displayName, 'switch', 'off')
    } else {
        sendEventToEventGrid(evt.deviceId, evt.displayName, 'switch', evt.value)
    }
}

def sendEventToEventGrid(sensorId, sensorName, sensorType, value) {
    log.debug "sendEventToEventGrid ${sensorId} at ${value}"
    def cleanedSensorId = sensorId.replace(" ", "")
	def date = new Date()
    def tz = TimeZone.getTimeZone('GMT')
    def curdate = date.format('yyyy-MM-dd',tz) + "T" + date.format('HH:mm:ss', tz) + "+00:00"

	Random random = new Random()
    def id = random.nextInt(10 ** 4)
    def params = [
        uri: "${appSettings.EventGridURL}",
        body: "[{id:${id}, subject:\"sensorreading\", eventType:\"sensorreading\",eventTime:\"${curdate}\", dataVersion:\"1.0\", data: { sensorid : \"${cleanedSensorId}\", sensorName : \"${sensorName}\", sensorType : \"${sensorType}\", value : \"${value}\" }}]",
        contentType: "application/json; charset=utf-8",
        headers: ["aeg-sas-key": "${appSettings.EventGridKey}"],
    ]
    log.debug "post params: ${params}"
	try {
        httpPost(params) { resp -> 
            log.debug "response message ${resp}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

