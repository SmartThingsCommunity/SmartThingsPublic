/**
 *  Temperature Switch
 *
 *  Copyright 2015 Alex Fu
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
    name: "Temperature Switch",
    namespace: "alexfu",
    author: "Alex Fu",
    description: "Trigger a switch when a temperature sensor has passed a specific threshold",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
  section("Temperature sensor:") {
    input "thermometer", "capability.temperatureMeasurement", required: true
  }
  section("Use this switch:") {
    input "myswitch", "capability.switch", required: true
  }
  section("Turn on switch when temperature rises above:") {
    input "ceilingTemperature", "decimal", required: true
  }
  section("Turn off switch when temperature drops below:") {
    input "floorTemperature", "decimal", required: true
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
  subscribe(thermometer, "temperature", onTemperatureChanged)
}

def onTemperatureChanged(evt) {
  if (evt.doubleValue < floorTemperature && myswitch.currentSwitch != "off") {
    log.info "Turning switch off"
    myswitch.off()
  }
  if (evt.doubleValue > ceilingTemperature && myswitch.currentSwitch != "on") {
    log.info "Turning switch on"
    myswitch.on()
  }
}
