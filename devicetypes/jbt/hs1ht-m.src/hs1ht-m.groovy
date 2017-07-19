/**
*  Copyright (c) 2016 Tibor Jakab-Barthi
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
NON-HUE EVENT desc: 02 0104 0302 00 05 0000 0003 0405 0001 0009 01 0019
Location: desc: 02 0104 0302 00 05 0000 0003 0405 0001 0009 01 0019
NON-HUE EVENT desc: 01 0104 0302 00 05 0000 0003 0402 0001 0009 01 0019
Location: desc: 01 0104 0302 00 05 0000 0003 0402 0001 0009 01 0019
NON-HUE EVENT ep_cnt:2, ep:01 02
Location: ep_cnt:2, ep:01 02
NON-HUE EVENT Thing added
Location: Thing added
NON-HUE EVENT join
Location: join
NON-HUE EVENT zwStatus:include search:Z-Wave include search started
Location: zwStatus:include search:Z-Wave include search started


catchall: 0104 0006 02 01 0100 00 C291 00 00 0000 0B 01 0601
catchall: 0000 8021 00 00 0100 00 C291 00 00 0000 00 2E 00

*/

metadata {
    definition (name: "HS1HT-M", namespace: "jbt", author: "Tibor Jakab-Barthi") {
        capability "Configuration"
        capability "Relative Humidity Measurement"
        capability "Refresh"
        capability "Sensor"
        capability "Temperature Measurement"

        fingerprint endpointId: "01", profileId: "0104", deviceId: "0302", inClusters: "0000,0001,0003,0009,0402", outClusters: "0019", deviceJoinName: "Temp"
        fingerprint endpointId: "02", profileId: "0104", deviceId: "0302", inClusters: "0000,0001,0003,0009,0405", outClusters: "0019", deviceJoinName: "Humi"
        //fingerprint profileId: "0104", deviceId: "0302", inClusters: "0000,0001,0003,0009,0405", outClusters: "0019"
        //fingerprint profileId: "0104", deviceId: "0302", inClusters: "0000, 0001, 0003, 0009, 0405", outClusters: "0019", deviceJoinName: "HS1HT-M 1"
        //fingerprint profileId: "0104", deviceId: "0302", inClusters: "00, 05, 0000, 0003, 0402, 0001, 0009, 01", outClusters: "0019", deviceJoinName: "HS1HT-M 2"
        //fingerprint profileId: "0104", inClusters: "0302, 00, 05, 0000, 0003, 0402, 0001, 0009, 01", outClusters: "0019", deviceJoinName: "HS1HT-M 3"
        //fingerprint profileId: "0104", inClusters: "0000, 0001, 0003, 0009, 0402, 0405"
        //fingerprint profileId: "0104", deviceId: "0302", inClusters: "0000,0001,0003,0009,0402,0405", deviceJoinName: "HS1HT-M 4"
        //fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008"
        //fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY A19 ON/OFF/DIM", deviceJoinName: "OSRAM LIGHTIFY LED Smart Connected Light"
        //fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FF00", outClusters: "0019", manufacturer: "MRVL", model: "MZ100", deviceJoinName: "Wemo Bulb"
        //fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05", outClusters: "0019", manufacturer: "OSRAM SYLVANIA", model: "iQBR30", deviceJoinName: "Sylvania Ultra iQ"
    }

    preferences {
        section {
            input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter '-5'. If 3 degrees too cold, enter '+3'.", displayDuringSetup: false, type: "paragraph", element: "paragraph"
            input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
        }
    }

    tiles {
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state("temperature", label:'${currentValue}°',
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
        valueTile("temperatureMain", "device.temperature", width: 1, height: 1) {
            state("temperature", label: '${currentValue}°', unit: "C", icon: "st.Weather.weather2", backgroundColor:"#EC6E05")
        }

        valueTile("humidity", "device.humidity") {
            state "humidity", label:'${currentValue}%', unit:""
        }
        standardTile("refresh", "device.temperature", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main (["temperatureMain"])
        details(["temperature", "humidity", "refresh"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    // catchall: 0104 0006 02 01 0100 00 C291 00 00 0000 0B 01 0601

    log.debug "HS1HT parse '$description'"

    def results

    if (description?.startsWith("catchall:")) {
        def map = parseCatchAllMessage(description)
        results = map ? createEvent(map) : null
    }
    else if (description == "updated") {
        results = parseOtherMessage(description)
    } 

    log.debug "Parse returned '${results}'"

    return results
}

private Map parseCatchAllMessage(String description) {
    Map resultMap = [:]
    def cluster = zigbee.parse(description)
    log.debug cluster

    switch(cluster.clusterId) {
        case 0x0402:
            log.debug "Temperature"

            String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
            def value = getTemperature(temp)
            resultMap = getTemperatureResult(value)
        break

        case 0x0405:
            log.debug "Humidity"

            // catchall: 0104 0405 02 01 0100 00 262A 00 00 0000 01 01 00000021D71C
            // catchall: 0104 0405 02 01 0100 00 384E 00 00 0000 01 01 000000219E15

            //String humidity = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
            String humidity = cluster.hex1(cluster.data[-2])

            log.debug "Humidity $humidity"

            //def value = getTemperature(temp)
            resultMap = [
                name: "humidity",
                value: humidity,
                descriptionText: "{{ device.displayName }} was {{ value }}%",
                translatable: true
            ]
        break

        case 0x8021:
            log.debug "Bind response"

            if (cluster.data.size() == 0) {
                log.error "Bind result not present."
            }
            else {
                def bindResult = cluster.data[0]

                if (bindResult == 0x00) {
                    log.debug "Bind: Success."
                }
                else if (bindResult == 0x82) {
                    log.debug "Bind: Invalid endpoint."
                }
                else if (bindResult == 0x84) {
                    log.debug "Bind: Not supported."
                }
                else if (bindResult == 0x8C) {
                    log.debug "Bind: Bind table full."
                }
                else {
                    log.debug "Bind: '${cluster.data}'."
                }
            }
        break
    }

    return resultMap
}

def getTemperature(value) {
    def celsius = Integer.parseInt(value, 16).shortValue() / 100

    if(getTemperatureScale() == "C"){
        return celsius
    } else {
        return celsiusToFahrenheit(celsius) as Integer
    }
}

private Map getTemperatureResult(value) {
    log.debug "getTemperatureResult"

    if (tempOffset) {
        def offset = tempOffset as int
        def v = value as int
        value = v + offset
    }

    def descriptionText = temperatureScale == 'C' ? '{{ device.displayName }} was {{ value }}°C': '{{ device.displayName }} was {{ value }}°F'

    return [
        name: "temperature",
        value: value,
        descriptionText: descriptionText,
        translatable: true
    ]
}

private Map parseOtherMessage(description) {
    log.debug "HS1HT parseOtherMessage '$description'"

    def name = null
    def value = description
    def linkText = getLinkText(device)
    def descriptionText = description
    def handlerName = description
    def isStateChange = isStateChange(device, name, value)

    def results = [
        name: name,
        value: value,
        unit: null,
        linkText: linkText,
        descriptionText: descriptionText,
        handlerName: handlerName,
        isStateChange: isStateChange,
        displayed: displayed(description, isStateChange)
    ]

	log.debug "parseOtherMessage results for '$device': '$results'"

    return results
}

def refresh() {
    log.debug "HS1HT refresh."

    def refreshCommands = [
    	"st rattr 0x${device.deviceNetworkId} 0x01 0x0402 0x0000", "delay 200",
    	"st rattr 0x${device.deviceNetworkId} 0x02 0x0405 0x0000", "delay 200"
    ]

    //def x = zigbee.readAttribute(0x0402, 0x0000)
    log.debug refreshCommands

    return refreshCommands
}

def configure() {
    log.debug "configure: binding device.deviceNetworkId '${device.deviceNetworkId}' device.zigbeeId '${device.zigbeeId}'."

    def configureCommands = [
		"zdo bind 0x${device.deviceNetworkId} 01 0xFF 0x402 {${device.zigbeeId}} {}", "delay 500",
        "zcl global send-me-a-report 0x402 0 0x29 30 60 {0}", "delay 200",
		"zdo bind 0x${device.deviceNetworkId} 02 0xFF 0x405 {${device.zigbeeId}} {}", "delay 500"
    ]

    log.debug "configureCommands $configureCommands"

    return configureCommands
}