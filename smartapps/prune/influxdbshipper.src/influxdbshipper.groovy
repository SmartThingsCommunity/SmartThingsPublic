/**
 *  InfluxdbShipper
 *
 *  Copyright 2016 Prune - prune@lecentre.net
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
 * Description : 
 * This application listen to sensors and send the result metric to an Influxdb 0.9.x or newer server.
 * It is NOT compatible with Influxdb 0.8.x
 *
 * Check Influxdb docs at
 * https://docs.influxdata.com/influxdb/v0.10/introduction/
 *
 * Usage : 
 * select sensors you want to monitor
 * enter your InfluxDB URL with full informations :
 *
 * <host>:<port>
 *
 * Ex : www.myserver.ca:8086/write
 *
 * It is strongly encouraged to use HTTPS and a login/password as your Influxdb server need to be open to the world
 */
definition(
    name: "InfluxdbShipper",
    namespace: "Prune",
    author: "Prune",
    description: "stream metrics to Influxdb",
    category: "My Apps",
    iconUrl: "http://lkhill.com/wp/wp-content/uploads/2015/10/influxdb-logo.png",
    iconX2Url: "http://lkhill.com/wp/wp-content/uploads/2015/10/influxdb-logo.png",
    iconX3Url: "http://lkhill.com/wp/wp-content/uploads/2015/10/influxdb-logo.png")



preferences {
    section("Log devices...") {
        input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required:false, multiple: true
        input "humidity", "capability.relativeHumidityMeasurement", title: "Humidity", required:false, multiple: true
        input "contacts", "capability.contactSensor", title: "Doors open/close", required: false, multiple: true
        input "motions", "capability.motionSensor", title: "Motions", required: false, multiple: true
        input "presence", "capability.presenceSensor", title: "Presence", required: false, multiple: true
        input "switches", "capability.switch", title: "Switches", required: false, multiple: true
        input "energy", "capability.energyMeter", title: "Energy", required: false, multiple: true
        input "smoke", "capability.smokeDetector", title: "Smoke", required: false, multiple: true
    }

    section ("Influxdb URL...") {
        input "influxdb_url", "text", title: "Influxdb URL"
        input "influxdb_proto", "enum", title: "Protocol (http or https)", options: ["HTTP", "HTTPS"]
        input "influxdb_db", "text", title: "Influxdb DB"
        input "influxdb_login", "text", title: "Influxdb Login"
        input "influxdb_password", "password", title: "Influxdb Password"
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
    /*subscribe(temperatures, "temperature", handleTemperatureEvent)
    subscribe(contacts, "contact", handleContactEvent)
    subscribe(accelerations, "acceleration", handleAccelerationEvent)
    subscribe(motions, "motion", handleMotionEvent)
    subscribe(presence, "presence", handlePresenceEvent)
    subscribe(switches, "switch", handleSwitchEvent)
	log.debug "Done subscribing to events"
    */
    //log.debug "temperatures: ${temperatures[0].name} = ${temperatures[0].currentState("temperature").value}"
    // Send data every 5 mins
    runEvery5Minutes(updateCurrentStats)
   
}


/*
  Push data to Influx
*/
def updateCurrentStats() {

    //log.debug "Logging to Influxdb ${influxdb_url}"
    // Builds the URL that will be sent to Influxdb
    def full_url = "${influxdb_proto}://${influxdb_url}/write?db=${influxdb_db}"
    if (influxdb_login != "") {
    	full_url += "&u=${influxdb_login}&p=${influxdb_password}"
        }
        
    def full_body=""
    temperatures.eachWithIndex{ val, idx -> 
        def sensorName = val.displayName.replaceAll(" ",'\\\\ ')
    	full_body += "temp,sensor=${sensorName} value=${val.currentState("temperature").value} \n"
    }
    contacts.eachWithIndex{ val, idx -> 
    	def sensorName = val.displayName.replaceAll(" ",'\\\\ ')
        // 0=closed, 1=open
        def contactState = val.currentState("contact").value == "open" ? 1 : 0
    	full_body += "contact,sensor=${sensorName} value=${contactState} \n"
    }
    humidity.eachWithIndex{ val, idx -> 
    	def sensorName = val.displayName.replaceAll(" ",'\\\\ ')
    	full_body += "humidity,sensor=${sensorName} value=${val.currentState("humidity").value} \n"
    }
    motions.eachWithIndex{ val, idx -> 
    	def sensorName = val.displayName.replaceAll(" ",'\\\\ ')
        // 0=no motion, 1=motion detected
        def motionState = val.currentState("motion").value == "active" ? 1 : 0
        full_body += "motion,sensor=${sensorName} value=${motionState} \n"
        }
    presence.eachWithIndex{ val, idx -> 
    	def sensorName = val.displayName.replaceAll(" ",'\\\\ ')
        // 0=no presence, 1=present
        def presenceState = val.currentState("presence").value == "present" ? 1 : 0
        full_body += "presence,sensor=${sensorName} value=${presenceState} \n"
        }
    switches.eachWithIndex{ val, idx -> 
    	def sensorName = val.displayName.replaceAll(" ",'\\\\ ')
        // 0=off, 1=on
        def switchState = val.currentState("switch").value == "on" ? 1 : 0
        full_body += "switch,sensor=${sensorName} value=${switchState} \n"
        }
    energy.eachWithIndex{ val, idx -> 
    	def sensorName = val.displayName.replaceAll(" ",'\\\\ ')
        full_body += "energy,sensor=${sensorName} value=${val.currentState("energy").value} \n"
        } 
    smoke.eachWithIndex{ val, idx -> 
    	def sensorName = val.displayName.replaceAll(" ",'\\\\ ')
        // 0="clear" 1="detected" 2="tested"
        def smokeState = val.currentState("smoke").value == "detected" ? 1 : 0
        full_body += "smoke,sensor=${sensorName} value=${smokeState} \n"
        }
    def params = [
        uri: full_url,
        body: full_body
    ]
    
    try {
        // Make the HTTP request using httpGet()
        log.debug "Calling $params"
        httpPost(params) { resp -> // This is how we define the "return data". Can also use $it.
            log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}