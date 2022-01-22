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
    name: "Fan Automation Child App",
    namespace: "baranauskas",
    parent: "baranauskas:Fan Automation",
    author: "Jose Augusto Baranauskas",
    version: "1.6 (2022-01-22)",
    description: "Create fan automation based on temperature and humidity sensors",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
    section("Which fan(s)?") {
        input "fans", "capability.fanSpeed",
              required: true, multiple: true,
              title: "Which fans?"
        input "fansMaxSpeed", "number", range: "1..5",
              required: true, defaultValue: 3,
              title: "Number of fan speeds (do not count 'off' speed)"
    }
    section("Which Indoor Sensor, Threshold, and Differential Temperature?") {
        input "tempSensors",   "capability.temperatureMeasurement",
              required: true, multiple: true,
              title: "Which Indoor Temperature Sensor?"
        input "tempThreshold", "decimal", range: "-10.0..50.0",
              required: true, defaultValue: 26.5,
              title: "Minimum Temperature Threshold to start automation (°${getTemperatureScale()})"
        input "tempDelta", "decimal", range: "0.5..10.0",
              required: true,  defaultValue: 1.5,
              title: "Fan Differential Temperature (°${getTemperatureScale()})"
    }
    section("Which Outdoor Sensor Temperature? If you select this and Outdoor Temp < Indoor Temp then fan will be turned off.") {
        input "weatherSensors",   "capability.temperatureMeasurement",
              required: false, multiple: true,
              title: "Which Outdoor Temperature Sensor?"
    }
    section("General Settings") {
        input "enableAutomation", "bool",
              required: true, defaultValue: true,
              title: "Enable this automation?"
        input "isActiveSwitch", "capability.switch",
              required: true, multiple: false,
              title: "Which (virtual) switch controls automation?"
        input "enableHeatIndex", "bool",
              required: true, defaultValue: true,
              title: "Enable heat index computation from temperature and humidity sensors?"
        input "enableNotificationOnOff", "bool",
              required: false, defaultValue: false,
              title: "Send notification when automation is turned on/off by the switch?"
        input "enableNotificationChange", "bool",
              required: false, defaultValue: false,
              title: "Send notification when fan speed changes?"
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
/*    // if the user did not override the label, set the label to the default
    if ( ! overrideLabel ) {
       int nextChild = parent.childApps.size()
       def myLabel = parent.app.name + " " + nextChild
       log.debug "Automatic child app label: ${myLabel}"
       app.updateLabel( myLabel )
    }
*/
    if ( ! settings.enableAutomation ) {
       log.debug "Automation disabled in general settings"
       return
    }

    if ( ! ( tempSensors && tempThreshold && tempDelta && isActiveSwitch && fans ) ) {
       log.debug "Please check your settings"
       return
    }

    // Indoor sensors
    def numberHumiditySensors = 0
    subscribe(tempSensors,    "temperature", temperatureHandler)
    tempSensors.each{ sensor ->
      if ( sensor.hasAttribute("humidity") ) {
         numberHumiditySensors++
         subscribe(sensor,  "humidity", temperatureHandler)
      }
    }
    state.hasIndoorHumiditySensors = (numberHumiditySensors > 0)

    // Outdoor sensors
    numberHumiditySensors = 0
    if ( weatherSensors ) {
      subscribe(weatherSensors,    "temperature", temperatureHandler)
      weatherSensors.each{ sensor ->
        if ( sensor.hasAttribute("humidity") ) {
           numberHumiditySensors++
           subscribe(sensor,  "humidity", temperatureHandler)
        }
      }
    }
    state.hasOutdoorHumiditySensors = (numberHumiditySensors > 0)

    subscribe(isActiveSwitch, "switch",      switchHandler)

    log.debug "isActiveSwitch ${isActiveSwitch.displayName} is ${isActiveSwitch.currentSwitch}"

    String msg = ""
    tempSensors.each{ sensor ->
      msg += "${sensor.displayName} temp is ${sensor.currentTemperature}"
      if ( sensor.hasAttribute( "humidity") ) {
        msg += " and humidity is ${sensor.currentHumidity}"
      }
      msg += "\n"
    }
    log.debug msg

    msg = ""
    weatherSensors.each{ sensor ->
      msg += "${sensor.displayName} temp is ${sensor.currentTemperature}"
      if ( sensor.hasAttribute( "humidity") ) {
        msg += " and humidity is ${sensor.currentHumidity}"
      }
      msg += "\n"
    }
    log.debug msg

    handleSwitch( isActiveSwitch.currentSwitch )
}

def switchIsStateChange( evt ) {
  return (evt.value == "on"  && ! state.isActive) ||
         (evt.value == "off" &&   state.isActive)
}

def switchHandler( evt ) {
    log.debug "switchHandler called evt.value = ${evt.value}"
    if ( switchIsStateChange( evt ) )
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
    state.lastFanSpeed = -1
    def msg = "Fan automation enabled"
    sendMsg( msg, settings.enableNotificationOnOff )
    temperatureHandler( null )
}

def disableAutomation() {
    state.isActive = false
    def msg = "Fan automation disabled"
    sendMsg( msg, settings.enableNotificationOnOff )
    fans.setFanSpeed( 0 )
}

def temperatureHandler( evt ) {
//	log.debug "temperatureHandler evt.doubleValue : $evt.doubleValue"
  if ( state.isActive  ) {
    // Indoor
    def tempIndoor = avgSensor( tempSensors, "temperature" )
    def humIndoor  = avgSensor( tempSensors, "humidity")
    def tempUnit = getTemperatureScale() // location temp scale
    def isCelsius = ( tempUnit == "C" )
    def heatIndexIndoor = (state.hasIndoorHumiditySensors && settings.enableHeatIndex)
        ? heatIndex(tempIndoor, humIndoor, isCelsius ) : tempIndoor

    // Outdoor
    def tempOutdoor = 100
    def humOutdoor = 0
    def heatIndexOutdoor = tempOutdoor
    if ( weatherSensors ) {
       tempOutdoor = avgSensor( weatherSensors, "temperature" )
       humOutdoor  = avgSensor( weatherSensors, "humidity" )
       heatIndexOutdoor = (state.hasOutdoorHumiditySensors && settings.enableHeatIndex)
       ? heatIndex(tempOutdoor, humOutdoor, isCelsius ) : tempOutdoor
    }
    def msg = "indoor  (temp,hum,heat index)=(${tempIndoor},${humIndoor},${heatIndexIndoor}) \n" +
              "outdoor (temp,hum,heat index)=(${tempOutdoor},${humOutdoor},${heatIndexOutdoor})"
    log.debug "temperatureHandler tempUnit=${tempUnit} enableHeatIndex=${settings.enableHeatIndex}\n ${msg}"
    handleTemperature( heatIndexIndoor, heatIndexOutdoor )
  }
}

// Computes average of sensors.currentMeasurement
// These are all equivalent:
// def currentValue = myLock.currentValue("lock")
// def latestValue = myLock.latestValue("lock")
// Lock capability has "lock" attribute.
// <deviceName>.current<uppercase attribute name>:
// def anotherCurrentValue = myLock.currentLock
def avgSensor( sensors, String measurement ) {
    double measure = 0.0
    int    n = 0
    sensors.each{ sensor ->
       if( sensor.hasAttribute( measurement ) ) {
         measure += sensor.currentValue( measurement )
         n++
       }
    }
    return (n == 0) ? 0 : ( measure / n )
}

def handleTemperature( tempIndoor, tempOutdoor ) {
    def fanSpeed = fanSpeedFromTemperature( tempIndoor )
//    log.debug "handleTemperature tempIndoor = ${tempIndoor}, tempOutdoor = ${tempOutdoor}, fanSpeed = ${fanSpeed}, state.lastFanSpeed = ${state.lastFanSpeed}"
    if ( weatherSensors && tempOutdoor < tempIndoor ) {
       fanSpeed = 0
       if ( fanSpeed != state.lastFanSpeed ) {
          def msg = "Now indoor ${tempIndoor.round(2)}° but outdoor ${tempOutdoor.round(2)}°, changing fan speed from ${state.lastFanSpeed} to ${fanSpeed}"
          sendMsg( msg, settings.enableNotificationChange )
          state.lastFanSpeed = fanSpeed
          fans.setFanSpeed( fanSpeed )
       }
    }
    else
      if ( fanSpeed != state.lastFanSpeed ) {
         def msg = "Now ${tempIndoor.round(2)}°, changing fan speed from ${state.lastFanSpeed} to ${fanSpeed}"
         sendMsg( msg, settings.enableNotificationChange )
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

def sendMsg( msg, sendNotification = true ) {
    msg = app.label + ": " + msg
    log.info msg
    if ( sendNotification )
       sendPush( msg )
}

def showAttributes( sensors ) {
    sensors.each{ sensor ->
      def theAtts = sensor.getSupportedAttributes()
      String msg = ""
      theAtts.each {att ->
         msg += "${att.name}, "
      }
      log.debug "showAttributes:  ${msg}"
    }
}

def showMore( sensors, String measurement ) {
    String msg = ""
    sensors.each{ sensor ->
       def currentState = sensor.currentState( measurement )
       def value = currentState.value
       def unit = currentState.unit
       def date = currentState.date
       msg += "[${value},${unit},${date}] "
    }
    log.debug "showMore ${measurement}:  ${msg}"
}
//------------------------------------------------------------------------------
// heat index utility functions
def toFahrenheit( celsius ) { return (9.0 * celsius / 5.0 + 32) }
def toCelsius( fahrenheit ) { return (5.0 * (fahrenheit - 32) / 9.0) }

// heat index
// definition http://www.hpc.ncep.noaa.gov/html/heatindex_equation.shtml
def heatIndex( temperature = 0, humidity = 0, isCelsius = true ) {
    double t, h, heatIndex, heatIndexBase

    t = isCelsius ? toFahrenheit( temperature ) : temperature
    h = humidity ?: 0

    // Steadman's result
    heatIndex = 0.5 * (t + 61.0 + (t - 68.0) * 1.2 + h * 0.094)

    // regression equation of Rothfusz is appropriate
    if (t >= 80) {
       heatIndexBase = (-42.379                      +
                          2.04901523 * t             +
                         10.14333127         * h     +
                         -0.22475541 * t     * h     +
                         -0.00683783 * t * t         +
                         -0.05481717         * h * h +
                          0.00122874 * t * t * h     +
                          0.00085282 * t     * h * h +
                         -0.00000199 * t * t * h * h )
        // adjustment
        if (h < 13 && t <= 112) {
          heatIndex = heatIndexBase - (13 - h) / 4 * Math.sqrt( (17 - Math.abs(t - 95) ) / 17 )
        } else if (h > 85 && t <= 87) {
            heatIndex = heatIndexBase + ((h - 85) / 10) * ((87 - t) / 5)
        } else {
            heatIndex = heatIndexBase
        }
    }
    return isCelsius ? toCelsius(heatIndex) : heatIndex
}
