/**
 *
 *  Copyright 2021 Jose Augusto Baranauskas
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
definition (
    name: "Fan Automation",
    namespace: "baranauskas",
    author: "Jose Augusto Baranauskas",
    version: "1.0 (2021-03-25)",
    description: "Create fans automation based on temperature sensors",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
    section("Which Sensor, Threshold, and Differential Temperature?") {
        input "tempSensors",   "capability.temperatureMeasurement",
              required: true,  title: "Which Temperature Sensor?", multiple: true
        input "tempThreshold", "number", range: "-10.0..50.0",
              required: true,  title: "Minimum Temperature Threshold to start automnation"
        input "tempDelta", "number", range: "0.5..10.0",
              required: true,  title: "Fan Differential Temperature"
    }
    section("Which switch controls automation?") {
        input "isActiveSwitch", "capability.switch",
              required: true,  title: "Which switch?", multiple: false
    }
    section("Which fans?") {
        input "fans", "capability.fanSpeed",
              required: true,  title: "Which fans?", multiple: true
        input "fansMaxSpeed", "decimal", range: "1..5",
              required: true,  title: "Number of fan speeds (do not count off speed)"
    }
    section("Send Push Notification?") {
        input "sendPush", "bool", required: false,
              title: "Send Push Notification when changed?"
    }
}

def installed() {
    log.debug "installed() with settings: ${settings}"
    updated()
}

def updated() {
  	log.debug "updated() with settings: ${settings}"
  	unsubscribe()
  	initialize()
}

def initialize() {
    log.debug "initialize"
    if ( ! ( tempSensors && tempThreshold && isActiveSwitch && fans ) ) {
       log.debug "Please check your settings"
       return
    }
    subscribe(tempSensors, "temperature", temperatureHandler)
    subscribe(isActiveSwitch, "switch", switchHandler)
    log.debug "isActiveSwitch ${isActiveSwitch.displayName} is ${isActiveSwitch.currentSwitch}"
    tempSensors.each{ tempSensor ->
      log.debug "tempSensor ${tempSensor.displayName} is ${tempSensor.currentTemperature}"
    }
    handleSwitch( isActiveSwitch.currentSwitch )
}

def switchHandler( evt ) {
    log.debug "switchHandler called evt.value = ${evt.value}"
    handleSwitch( evt.value )
}

def handleSwitch( sw ) {
    if ( sw == "on"  )
       enableAutomation()
    else // "off"
       disableAutomation()

   fanSpeedMap()
}

def enableAutomation() {
    state.isActive = true
    state.lastFanSpeed = 0
    def msg = "Fan automation enabled"
    sendMsg( msg )
    def temp = avgTemperature()
    handleTemperature( temp )
}

def disableAutomation() {
    state.isActive = false
    def msg = "Fan automation disabled"
    sendMsg( msg )
    fans.setFanSpeed( 0 )
}

def temperatureHandler( evt ) {
//	log.debug "temperatureHandler evt.doubleValue : $evt.doubleValue"
  if ( state.isActive  ) {
//    def temp = tempSensor.currentTemperature
    def temp = avgTemperature()
    handleTemperature( temp )
  }
}

def avgTemperature() {
    double temp = 0
    int    n = 0
    tempSensors.each{ tempSensor ->
       temp += tempSensor.currentTemperature
       n++
    }
    return (n == 0) ? 0 : ( temp / n )
}

def handleTemperature( temp ) {
    def fanSpeed = fanSpeedFromTemperature( temp )
    log.debug "handleTemperature temp = ${temp}, fanSpeed = ${fanSpeed}, state.lastFanSpeed = ${state.lastFanSpeed}"
    if ( fanSpeed != state.lastFanSpeed ) {
       def msg = "Now ${temp}°, changing fans speed from ${state.lastFanSpeed} to ${fanSpeed}"
       sendMsg( msg )
       state.lastFanSpeed = fanSpeed
       fans.setFanSpeed( fanSpeed )
    }
}

def fanSpeedFromTemperature( temp ) {
    if ( ! temp )
       return 0
    int    maxSpeed  = settings.fansMaxSpeed  ?: 3
    double deltaTemp = settings.tempDelta ?: 1.0

    for(int speed = maxSpeed; speed >= 1; speed--) {
      if ( temp >= tempThreshold + (speed - 1) * deltaTemp )
        return speed
    }
    return 0
}

def fanSpeedMap() {
    int    maxSpeed  = settings.fansMaxSpeed  ?: 3
    double deltaTemp = settings.tempDelta ?: 1.0

    def temps = [:]
    for(int speed = 1; speed <= maxSpeed; speed++) {
       temps <<  [ "${speed}" : ( tempThreshold + (speed - 1) * deltaTemp ) ]
    }
    log.debug "fanSpeedMap: ${temps}"
    return temps
}

def sendMsg( msg ) {
    log.debug msg
    if ( settings.sendPush )
       sendPush( msg )
}
