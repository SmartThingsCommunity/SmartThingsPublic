/**
 *  Copyright 2015 SmartThings
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
 *  Text Me When It Opens
 *
 *  Author: SmartThings
 */
definition(
    name: "Call WebService When open",
    namespace: "au.com.healthconnex",
    author: "Fredy Rincon",
    description: "Call a webservice once the sensor is open",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
	section {
		input "humiditySensor", "capability.relativeHumidityMeasurement"
	}
}

def installed()
{
	sendIfHumidityTooLow()
	runEvery5Minutes(sendIfHumidityTooLow)
	/*subscribe(contact1, "contact.open", contactOpenHandler)
    subscribe(contact1, "contact.closed", contactCloseHandler)*/
}

def sendIfHumidityTooLow()
{
	def currentState = humiditySensor.currentState("temperature")

    	log.debug "temperature is too low! ${currentState.value}"
        callWebPostService("${currentState.value}")
        //postTemperature("${currentState.value}")

}

def sendHumidity()
{	
	def currentState = humiditySensor.currentState("temperature")
   
	log.debug "humidity value as a string: ${currentState.value}"
	log.debug "time this humidity record was created: ${currentState.date}"
	//
	//
}

/*
def updated()
{
sendHumidity()
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
    subscribe(contact1, "contact.closed", contactCloseHandler)
}
*/
def contactCloseHandler(evt) {
	def message = "deviceId:$evt.deviceId,name:$evt.name,value:$evt.value" 
	log.trace "$evt.value: $evt, $settings"
	log.debug "$contact1 was Closed, calling webservice" 
    callWebPostService("FromClose:" + message)
}

def contactOpenHandler(evt) {
	def message = "deviceId:$evt.deviceId,name:$evt.name,value:$evt.value" 
	log.trace "$evt.value: $evt, $settings"
	log.debug "$contact1 was opened, calling webservice" 
    
    def currentState = contact1.currentState("temperature")
log.debug "temperature value as a string: ${currentState.value}"
log.debug "time this temperature record was created: ${currentState.date}"

    callWebService("FromOpen:" + message)
}

def callWebService(String stringJson) {
	
    def params = [
    	uri: "https://hackathonapi.qa.mycaremanager.com.au/api/values?id=" + stringJson,
        contentType: 'application/json'
	 ]
    
	try { 
    	httpGet(params) { response ->
        	log.debug "Start httpGet"
    		if (response.data) {
                log.debug "Response Data = ${response.data}"
        		log.debug "Response Status = ${response.status}"

                response.headers.each {
  					log.debug "header: ${it.name}: ${it.value}"
				}
        	}
        	if(response.status == 200) {
	        	log.debug "Request was OK"
    		}
        	else {
        		log.error "Request got http status ${response.status}"
        	}
        }
	} catch (e) {
    	log.error "something went wrong: $e, the response type is $e.response.contentType" 
	}
}

def postTemperature(String tempJson) {
	def params = [
		uri: "https://hackathonapi.qa.mycaremanager.com.au/api/values",
		body: [
		param1: ["temperature": "${tempJson}",
				subparam2: "${tempJson}"],
		param2: "${tempJson}"
		]
	]
try {
	httpPostJson(params) { resp ->
	resp.headers.each {
			log.debug "${it.name} : ${it.value}"
			}
    log.debug "response contentType: ${resp. contentType}"
            log.debug "${body}"
	}
} catch (e) {
log.debug "something went wrong: $e"
}

}

def callWebPostService(String stringJson) {
	def params = [
    	uri: "https://hackathonapi.qa.mycaremanager.com.au/api/Temperature",
        body: [Data : "${stringJson}"],
		contentType:"application/json",

	]

    try {
    	httpPost(params) { resp ->
            log.debug "Start httpPost"
        	resp.headers.each {
            log.debug "${it.name} : ${it.value}"
        }
        log.debug "response contentType: ${resp.contentType}"
        log.debug "${params}"
    }
	} catch (e) {
    	log.debug "something went wrong: $e"
	}
}

/*
httpPostJson(uri: deviceInfo.callbackUrl, path: '',  body: [evt: [deviceId: evt.deviceId, name: evt.name, value: evt.value]]) {
        log.debug "Event data successfully posted"
    }
*/