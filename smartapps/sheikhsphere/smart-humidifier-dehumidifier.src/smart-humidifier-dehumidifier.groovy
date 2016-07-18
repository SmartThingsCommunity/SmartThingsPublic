/**
 *  Smart Humidifier/Dehumidifier
 *
 *  Copyright 2015 Sheikh Dawood
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
 
 // Source maintained at https://github.com/sheikhsphere/SmartApp-smart-humidifier-dehumidifier
 
definition(
    name: "Smart Humidifier/Dehumidifier",
    namespace: "Sheikhsphere",
    author: "Sheikh Dawood",
    description: "Turn on/off a humidifier or dehumidifier based on relative humidity from a sensor.",
    category: "Green Living",
   iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather12-icn",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather12-icn?displaySize=2x"
)


preferences {
	section("Smart Humidifier/Dehumidifier") {
        paragraph "Version 1.0.1"
    }
	section("Devices to sense and control humidity:") {
		input "humiditySensor1", "capability.relativeHumidityMeasurement", title: "Humidity Sensor", required: true
        input "switch1", "capability.switch", title: "Switch", required: true
	}
	section("Settings:") {
    	paragraph "Turn off humidifier when humidity is above the High threshold value and turn it on when it is below the Low threshold value. This works the opposite way when selecting the Dehumidifier mode."
        input(name: "humidityMode", type: "enum", title: "Mode", options: ["Humidifier","Dehumidifier"], required: true, defaultValue: "Humidifier")
		input "humidityHigh", "number", title: "High humidity Threshold value (RH%):", required: true
        input "humidityLow", "number", title: "Low humidity Threshold value (RH%):", required: true
        input "delay", "number", title: "Polling delay (minutes):", required: true, defaultValue: 30
	}
    section( "Notifications" ) {
        input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
        input "phone1", "phone", title: "Send a Text Message?", required: false
    }
}

def installed() {
    initialize()
}

def updated() {
	unsubscribe()
    try {
        unschedule()
    } catch (all) {
        log.warn ("unschedule error")
    }
    initialize()
}

def initialize() {
	state.lastSwitchStatus = null
	state.lastHumidity = null
	subscribe(humiditySensor1, "humidity", humidityHandler)
    subscribe(switch1, "switch", switchHandler)
	scheduleChecks()
    pollAndCheck()
    statusCheck()
}

def scheduleChecks() {
	def sec = Math.round(Math.floor(Math.random() * 60))
	def exp1 = "$sec */${delay} * * * ?"
    log.debug "pollAndCheck: $exp1"
	schedule(exp1, pollAndCheck)
}

def pollAndCheck() {
	tryPoll()
    state.lastSwitchStatus = switch1.latestValue("switch")
    state.lastHumidity = humiditySensor1.currentValue("humidity")
    log.debug "pollAndCheck: state.lastHumidity: $state.lastHumidity, state.lastSwitchStatus: $state.lastSwitchStatus"
    runIn(1, statusCheck)
}

def tryPoll() {
	try {
        switch1?.poll();
    } catch (all) {
        log.warn ("${switch1.label} does not support polling")
    }
}

def statusCheck() {
    if (state.lastHumidity) {
        if (state.lastHumidity >= humidityHigh) {
            if (state.lastSwitchStatus != "off" && humidityMode == "Humidifier") {
                send("It's too humid! Turning off ${switch1.label}")
                switch1?.off()
                state.lastSwitchStatus = "off"
            }
            else if (state.lastSwitchStatus != "on" && humidityMode == "Dehumidifier") {
            	send("It's too humid! Turning on ${switch1.label}")
                switch1?.on()
                state.lastSwitchStatus = "on"
            }
        }
        else if (state.lastHumidity <= humidityLow) {
            if (state.lastSwitchStatus != "on" && humidityMode == "Humidifier") {
                send("It's too dry! Turning on ${switch1.label}")
                switch1?.on()
                state.lastSwitchStatus = "on"
            }
            else if (state.lastSwitchStatus != "off" && humidityMode == "Dehumidifier") {
            	send("It's too dry! Turning off ${switch1.label}")
                switch1?.off()
                state.lastSwitchStatus = "off"
            }
        }
    }
}

def switchHandler(evt) {
    log.trace "$evt.name: $evt.value: $evt.displayName"
    state.lastSwitchStatus = evt.value
}


def humidityHandler(evt) {
	log.trace "$evt.name: state.lastHumidity: $state.lastHumidity, humidity: $evt.value, humidityHigh: $humidityHigh, humidityLow: $humidityLow. humidityMode: $humidityMode"
    state.lastHumidity = Double.parseDouble(evt.value.replace("%", ""))
	runIn(1, statusCheck)
}

private send(msg) {
    if ( sendPushMessage == "Yes" ) {
        log.debug( "Sending push message" )
        sendPush( msg )
    }

    if ( phone1 ) {
        log.debug( "Sending text message to $phone1" )
        sendSms( phone1, msg )
    }

    log.debug msg
}