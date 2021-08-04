/**
 *  Weewx Weather Station
 *
 *  Copyright 2017/2018 Jason Woodrich (@jwoodrich on GitHub)
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
	definition (name: "Weewx Weather Station", namespace: "jwoodrich", author: "Jason Woodrich") {
		capability "Relative Humidity Measurement"
		capability "Sensor"
		capability "Temperature Measurement"
        attribute "wind", "number"
        attribute "windGust", "number"
        attribute "windDirH", "string"
	}


	simulator {
	}

	tiles {
        valueTile("temperature", "device.temperature",canChangeIcon: true) {
            state("temperature", label:'${currentValue}° F',
                  backgroundColors:[
                      [value: 31, color: "#153591"],
                      [value: 44, color: "#1e9cbb"],
                      [value: 59, color: "#90d2a7"],
                      [value: 74, color: "#44b621"],
                      [value: 84, color: "#f1d801"],
                      [value: 95, color: "#d04e00"],
                      [value: 96, color: "#bc2323"]
                  ]
                 )
        }
        valueTile("hourlyRain", "device.hourlyRain", decoration: "flat") {
            state "default", label:'${currentValue} in. this hour',
                  backgroundColors:[
                      [value: 0, color: "#ffffffff"],
                      [value: 0.1, color: "#225bb7"],
                      [value: 1, color: "#054cbf"]
                  ]
        }
        valueTile("dailyRain", "device.rainToday", decoration: "flat") {
            state "default", label:'${currentValue} in. today',
                  backgroundColors:[
                      [value: 0, color: "#ffffffff"],
                      [value: 0.1, color: "#225bb7"],
                      [value: 1, color: "#054cbf"]
                  ]
        }
        valueTile("humidity", "device.humidity") {
            state("humidity", label:'${currentValue}% RH')
        }
        valueTile("wind", "device.wind") {
            state("wind", label:'${currentValue} MPH')
        }
        valueTile("windDirection", "device.windDirH", decoration: "flat") {
            state "default", label:'Wind from ${currentValue}'
        }
        valueTile("windGust", "device.windBurst") {
            state("windGust", label:'${currentValue} MPH Gust')
        }
        main("temperature")
        details(["temperature","humidity","wind","windDirection","windGust","hourlyRain","dailyRain"])
	}
}


// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}