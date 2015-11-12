/**
 *  PlotWatt Logger 
 *
 *  Copyright 2015 Brian Wilson
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
 *  Please go here for help and setup instructions: https://github.com/bdwilson/PlotWatt-SmartThings-Logger 
 *
 */
definition(
    name: "PlottWatt Logger",
    namespace: "bdwilson",
    author: "Brian Wilson",
    description: "PlotWatter Logger",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Log devices...") {
        input "power", "capability.powerMeter", title: "Power", required: false, multiple: true
    }

    section ("PlotWatt API ID...") {
        input "channelId", "text", title: "PlotWatt API ID"
    }

    section ("PlotWatt panel id...") {
        input "channelKey", "text", title: "Panel id"
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(power, "power", handlePowerEvent)
}

def handlePowerEvent(evt) {
    logField(evt,"power") { it.toString() }
}


private logField(evt, field, Closure c) {
    def value = c(evt.value)
    float watts = value.toFloat()
    def kwatts = watts/1000
    def now = Calendar.instance
    def date = now.time
    def millis = date.time
    def secs = millis/1000
    secs = secs.toInteger()
    def body = "${channelKey},${kwatts},${secs}"
    def userpassascii = "${channelId}:"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def headers = [:]
    headers.put("Authorization", userpass)

    def uri = "http://plotwatt.com/api/v2/push_readings"
    def params = [
         uri: uri,
         body: body,
         headers: headers
    ] 
    log.debug "Posting Body: ${body} to ${uri}"
    httpPost(params) {response -> parseHttpResponse(response)}
}

def parseHttpResponse(response) {
	log.debug "Request was successful, $response.status"
}