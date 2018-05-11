/**
 *  TumThings: Devices to MySql
 *
 *  Author:   Anucha Promwungkwa
 *			  TumThings's Developer
 *		      2015-12-20
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
    name: "Devices to MySql",
    namespace: "TumThings",
    author: "Anucha Promwungkwa",
    description: "Smart Home Project (2015), Energy Monitoring & AC control, Devices to Mysql",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
 
preferences {
    section("Log devices...") {
        input "contacts", "capability.contactSensor", title: "Doors open/close", required: false, multiple: true
        input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required:false, multiple: true
        input "humidity", "capability.relativeHumidityMeasurement", title: "Humidity Meters", multiple: true, required: false
        input "powerMeter", "capability.powerMeter", title: "Power Meters", multiple: true, required: false
        input "direction", "capability.sensor", title: "TVOC sensor", multiple: true, required: false
        input "co2level", "capability.sensor", title: "CO2 sensor", multiple: true, required: false
        input "luminance", "capability.sensor", title: "Dust sensor", multiple: true, required: false
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
        subscribe(temperatures, "temperature", handleTemperatureEvent)
        subscribe(humidity, "humidity", handleHumidityEvent)
        subscribe(powerMeter, "power", handlePowerMeterEvent)
        subscribe(direction, "direction", handleDirectionEvent)
        subscribe(co2level, "co2level", handleCO2Event)
        subscribe(luminance, "luminance", handleLuminanceEvent)
        subscribe(contacts, "contact", handleContactEvent)
}

def handleTemperatureEvent(evt) {
        sendValue(evt) { it.toString() }
}

def handleHumidityEvent(evt) {
        sendValue(evt) { it.toString() }
}

def handlePowerMeterEvent(evt) {
        sendValue(evt) { it.toString() }
}

def handleDirectionEvent(evt) {
        sendValue(evt) { it.toString() }
}

def handleCO2Event(evt) {
        sendValue(evt) { it.toString() }
}

def handleLuminanceEvent(evt) {
        sendValue(evt) { it.toString() }
}

def handleContactEvent(evt) {
        sendValue(evt) { it == "open" ? "true" : "false" }
}
 
private sendValue(evt, Closure convert) {
        def locId    = evt.locationId
        def loc      = evt.location
        def did      = evt.deviceId
        def displayName  = evt.displayName
        def isodate  = evt.isoDate
        def sensor   = evt.name
        //def value    = convert(evt.value)
        def value    = evt.floatValue
        def unit     = evt.unit
        
        if (unit=='%'){
           unit = 'Percent'
        }

        log.debug "Location = ${loc}"
        log.debug "DispName = ${displayName}"
        log.debug "Sensor   = ${sensor}"
        
        def url = "http://119.59.102.211/data/ST/?st=${locId}&ss=${did}&dt=${isodate}&sn=${sensor}&value=${value}&un=${unit}"
		log.debug "${url}"
        
        def putParams = [
                uri: url,
                body: []]
 
        httpPut(putParams) { response ->
                if (response.status != 200 ) {
                        log.debug "GroveStreams logging failed, status = ${response.status}"
                }
        }
}