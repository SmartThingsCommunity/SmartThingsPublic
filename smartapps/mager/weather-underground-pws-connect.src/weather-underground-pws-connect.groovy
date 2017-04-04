/**
 *  Weather Underground PWS Connect
 *
 *  Copyright 2015 Andrew Mager
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

// This imports the Java class "DecimalFormat"
import java.text.DecimalFormat
 
definition(
    name: "Weather Underground PWS Connect",
    namespace: "mager",
    author: "Andrew Mager",
    description: "Connect your SmartSense Temp/Humidity sensor to your Weather Underground Personal Weather Station.",
    category: "Green Living",
    iconUrl: "http://i.imgur.com/HU0ANBp.png",
    iconX2Url: "http://i.imgur.com/HU0ANBp.png",
    iconX3Url: "http://i.imgur.com/HU0ANBp.png",
    oauth: true,
    singleInstance: true)


preferences {
    section("Select a sensor") {
        input "temp", "capability.temperatureMeasurement", title: "Temperature", required: true
        input "humidity", "capability.relativeHumidityMeasurement", title: "Humidity", required: true
    }
    section("Configure your Weather Underground credentials") {
        input "weatherID", "text", title: "Weather Station ID", required: true
        input "password", "password", title: "Weather Underground password", required: true
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

    /*
      Check to see if the sensor is reporting temperature, then run the updateCurrentWeather
      every 10 minutes
    */
    if (temp.currentTemperature) {
        runEvery5Minutes(updateCurrentWeather)
    }
}


/*
  Updates the Weather Underground Personal Weather Station (PWS) Upload Protocol
  Reference: http://wiki.wunderground.com/index.php/PWS_-_Upload_Protocol
*/
def updateCurrentWeather() {

    // Logs of the current data from the sensor
    log.trace "Temp: " + temp.currentTemperature
    log.trace "Humidity: " + humidity.currentHumidity
    log.trace "Dew Point: " + calculateDewPoint(temp.currentTemperature, humidity.currentHumidity)

    // Builds the URL that will be sent to Weather Underground to update your PWS
    def params = [
        uri: "http://weatherstation.wunderground.com",
        path: "/weatherstation/updateweatherstation.php",
        query: [
            "ID": weatherID,
            "PASSWORD": password,
            "dateutc": "now",
            "tempf": temp.currentTemperature,
            "humidity": humidity.currentHumidity,
            "dewptf": calculateDewPoint(temp.currentTemperature, humidity.currentHumidity),
            "action": "updateraw",
            "softwaretype": "SmartThings"
        ]
    ]
    
    try {
        // Make the HTTP request using httpGet()
        httpGet(params) { resp -> // This is how we define the "return data". Can also use $it.
            log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }

}

// Calculates dewpoint based on temperature and humidity
def calculateDewPoint(t, rh) {
    def dp = 243.04 * ( Math.log(rh / 100) + ( (17.625 * t) / (243.04 + t) ) ) / (17.625 - Math.log(rh / 100) - ( (17.625 * t) / (243.04 + t) ) ) 
    // Format the response for Weather Underground
    return new DecimalFormat("##.##").format(dp)
}
