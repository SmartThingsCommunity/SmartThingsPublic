/**
 *  Konnected Temperature & Humidity Sensor (DHT)
 *
 *  Copyright 2018 Konnected Inc (https://konnected.io)
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

metadata {
  definition (name: "Konnected Temperature & Humidity Sensor (DHT)", namespace: "konnected-io", author: "konnected.io", mnmn: "SmartThings", vid: "generic-humidity") {
    capability "Temperature Measurement"
    capability "Relative Humidity Measurement"
  }

  preferences {
    input name: "pollInterval", type: "number", title: "Polling Interval (minutes)",
      defaultValue: defaultPollInterval(),
      description: "Frequency of sensor updates"
  }

  tiles {
    multiAttributeTile(name:"main", type:"thermostat", width:6, height:4) {
        tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
            attributeState "temperature", label:'${currentValue}°F', unit: "°F", backgroundColors: [
                    // Celsius Color Range
                    [value:  0, color: "#153591"],
                    [value:  7, color: "#1E9CBB"],
                    [value: 15, color: "#90D2A7"],
                    [value: 23, color: "#44B621"],
                    [value: 29, color: "#F1D801"],
                    [value: 33, color: "#D04E00"],
                    [value: 36, color: "#BC2323"],
                    // Fahrenheit Color Range
                    [value: 40, color: "#153591"],
                    [value: 44, color: "#1E9CBB"],
                    [value: 59, color: "#90D2A7"],
                    [value: 74, color: "#44B621"],
                    [value: 84, color: "#F1D801"],
                    [value: 92, color: "#D04E00"],
                    [value: 96, color: "#BC2323"]
                ]
        }
        tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
            attributeState("humidity", label:'${currentValue}%', unit:"%", defaultState: true)
        }
    }
    main "main"
    details "main"
  }
}

def updated() {
  parent.updateSettingsOnDevice()
}

// Update state sent from parent app
def updateStates(states) {
  def temperature = new BigDecimal(states.temp)
  if (location.getTemperatureScale() == 'F') {
  	temperature = temperature * 9 / 5 + 32
  }
  sendEvent(name: "temperature", value: temperature.setScale(1, BigDecimal.ROUND_HALF_UP), unit: location.getTemperatureScale())

  def humidity
  if (states.humi) {
    humidity = new BigDecimal(states.humi)
    sendEvent(name: "humidity", value: humidity.setScale(0, BigDecimal.ROUND_HALF_UP), unit: '%')
  }

  log.debug "Temperature: $temperature, Humidity: $humidity"
}

def pollInterval() {
  return pollInterval.isNumber() ? pollInterval : defaultPollInterval()
}

def defaultPollInterval() {
  return 3 // minutes
}