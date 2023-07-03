/**
 *  Aeon HEM - Xively
 *
 *  Copyright 2014 Dan Anghelescu
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  for the specific language governing permissions and limitations under the License.
 *
 *
 *
 *  Genesys: Based off of Aeon Smart Meter Code sample provided by SmartThings (2013-05-30), Aeon Home Energy Meter v2 by Barry A. Burke, and Xively Logger by Patrick Stuart  Built on US model
 *           may also work on international versions (currently reports total values only)
 */


// Automatically generated. Make future change here.
definition (
                name: "Aeon HEM - Xively",
                namespace: "smartthings",
                author: "Dan Anghelescu",
                description: "Aeon HEM - Xively Logger",
                category: "My Apps",
                iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.Electronics.electronics13-icn?displaySize",
                iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.Electronics.electronics13-icn?displaySize=2x")

preferences {
    section("Log devices...") {
        input "energymeters", "capability.EnergyMeter", title: "Energy Meter", required: false, multiple: true
        input "thermostats", "capability.thermostat", title: "Thermostat", required: false, multiple: true
        input "weatherstations", "capability.temperatureMeasurement", title: "Outside Temperature", required: false, multiple: true 

    }

    section ("Xively Info") {
        input "xi_apikey", "text", title: "Xively API Key"
        input "xi_feed", "number", title: "Xively Feed ID"
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
    state.clear()
        unschedule(checkSensors)
        schedule("0 */5 * * * ?", "checkSensors")
        subscribe(app, appTouch)
}

def appTouch(evt) {
    log.debug "appTouch: $evt"
    checkSensors()
}



def checkSensors() {

    def logitems = []
    for (t in settings.energymeters) {
        logitems.add([t.displayName, "energymeter.energy", t.latestValue("energy"), "KilowattHours", "kWh"] )
        state[t.displayName + ".energy"] = t.latestValue("energy")
    }
    for (t in settings.thermostats) {
    	logitems.add([t.displayName, "inside.temperature", t.latestValue("temperature"), "Farenheight", "°F"] )
        state[t.displayName + ".temperature"] = t.latestValue("temperature")

/*        
		if ( t.currentValue("thermostatOperatingState")  ==  "cooling") {
                logitems.add([t.displayName, "thermostatState", 1 ])
                state[t.displayName + ".thermostatOperatingState"] = 1
         }
        if ( t.currentValue("thermostatOperatingState")  ==  "heating") {
                logitems.add([t.displayName, "thermostatState", 2 ])
                state[t.displayName + ".thermostatOperatingState"] = 2
        }
        if ( t.currentValue("thermostatOperatingState")  ==  "idle") {
                logitems.add([t.displayName, "thermostatState", 0 ])
                state[t.displayName + ".thermostatOperatingState"] = 0
        }
*/
    }
    for (t in settings.energymeters) {
        logitems.add([t.displayName, "energymeter.power", t.latestValue("power"), "Watts", "W"] )
        state[t.displayName + ".power"] = t.latestValue("power")
    }
    for (t in settings.energymeters) {
        logitems.add([t.displayName, "energymeter.volts", t.latestValue("volts"), "Volts", "V"] )
        state[t.displayName + ".volts"] = t.latestValue("volts")
    }
    for (t in settings.energymeters) {
        logitems.add([t.displayName, "energymeter.amps", t.latestValue("amps"), "Amps", "A"] )
        state[t.displayName + ".amps"] = t.latestValue("amps")
    }
    for (t in settings.weatherstations) {
    	logitems.add([t.displayName, "outside.temperature", t.latestValue("temperature"), "Farenheight", "°F"] )
        state[t.displayName + ".temperature"] = t.latestValue("temperature")
    }
    logField2(logitems)

}

private getFieldMap(channelInfo) {
    def fieldMap = [:]
    channelInfo?.findAll { it.key?.startsWith("field") }.each { fieldMap[it.value?.trim()] = it.key }
    return fieldMap
}


private logField2(logItems) {
    def fieldvalues = ""
    log.debug logItems


    def xivelyinfo = ""
    logItems.eachWithIndex() { item, i ->
    def channelname = item[0].replace(" ","_") + "_" + item[1]
    xivelyinfo += "{\"id\":\"${channelname}\",\"current_value\":\"${item[2]}\",\"unit\":{\"label\":\"${item[3]}\",\"symbol\":\"${item[4]}\"}}"
    if (i.toInteger() + 1 < logItems.size())
    {
    xivelyinfo += ","
    }

    }
    log.debug xivelyinfo
    def uri = "https://api.xively.com/v2/feeds/${xi_feed}.json"
    def json = "{\"version\":\"1.0.0\",\"datastreams\":[${xivelyinfo} ]}"

    def headers = [
        "X-ApiKey" : "${xi_apikey}"
    ]

    def params = [
        uri: uri,
        headers: headers,
        body: json
    ]
    log.debug params.body
    httpPutJson(params) {response -> parseHttpResponse(response)}
}

def parseHttpResponse(response) {
    log.debug "HTTP Response: ${response}"
}

def captureState(theDevice) {
    def deviceAttrValue = [:]
    for ( attr in theDevice.supportedAttributes ) {
        def attrName = "${attr}"
        def attrValue = theDevice.currentValue(attrName)
        deviceAttrValue[attrName] = attrValue
    }
    return deviceAttrValue
}