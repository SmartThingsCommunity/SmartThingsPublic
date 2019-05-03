/**
 *  Its too humid!
 *
 *  Copyright 2014 Brian Critchlow
 *  Based on Its too cold code by SmartThings
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
    name: "Humidity Alert!",
    namespace: "docwisdom",
    author: "mc - Brian Critchlow",
    description: "Notify me when the humidity rises above or falls below the given threshold. It will turn on a switch when it rises above the first threshold and off when it falls below the second threshold.",
    category: "Convenience",
    iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather9-icn",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather9-icn?displaySize=2x",
    pausable: true
)


preferences {
	section("Monitor the humidity of:") {
		input "humiditySensor1", "capability.relativeHumidityMeasurement"
	}
	section("When the humidity rises above:") {
		input "humidity1", "number", title: "Percentage ?"
	}
    section("When the humidity falls below:") {
		input "humidity2", "number", title: "Percentage ?"
	}
    section( "Notifications" ) {
        input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
        input "phone1", "phone", title: "Send a Text Message?", required: false
    }
	section("Control this switch:") {
		input "switch1", "capability.switch", required: false
	}
    //to build over ride switch
    //section("over ride when off:") {
	//	input "switch2", "capability.switch", required: false
	//}
}

def installed() {
	subscribe(humiditySensor1, "humidity", humidityHandler)
    subscribe(switch1, "switch", switchstate)
    //subscribe(switch2, "switch", switchstate)
}

def updated() {
	unsubscribe()
	subscribe(humiditySensor1, "humidity", humidityHandler)
    subscribe(switch1, "switch", switchstate)
    //subscribe(switch2, "switch", switchstate)
}

def switchstate(evt){
//if switch 2
	state.sstate = evt.value
log.debug "swith: ${evt.value}"
    if (evt.value == "off" && state.appstate == "on"){
    	log.trace "swith is '${evt.value}' , but App is still '${state.appstate}' turning on - Should be on event and app on"
    	switch1?.on()
    }
}

def humidityHandler(evt) {
log.debug "Reported humidity: ${evt.value}, set point upper: ${settings.humidity1}, set point lower: ${settings.humidity2}"
	def currentHumidity = Double.parseDouble(evt.value.replace("%", ""))
//High ------------------------
	if (currentHumidity >= settings.humidity1) {
		if (state.sstate == "on" && state.appstate == "on"){ // everything on - Don't send a continuous stream of text messages
        	log.trace "Humid High, Switch is ${state?.sstate}, App State is ${state.appstate} everything on already"
        }
		else if (state.sstate == "on" && state.appstate == "off"){ //fan on for some other reason
        	log.trace "Humid High, Switch is ${state.sstate}, App State is ${state.appstate} switch on but app not send message anyway turning app on"
            send("${humiditySensor1.label} sensed high humidity level of ${evt.value}")
            state.appstate = "on"
        }
        else { //app off and/or switch off
        	state.appstate = "on"
			log.trace "Humidity High, Rose Above ${settings.humidity1}:  sending SMS and activating ${settings?.switch1} Switch is ${state.sstate}, App State is ${state.appstate}"
			send("${humiditySensor1.label} sensed high humidity level of ${evt.value}")
            switch1?.on()
		}
	log.debug "end of high hum"
	}
//Low -------------------
    else if (currentHumidity <= settings.humidity2) {
		if (state.sstate == "off" && state.appstate == "off"){
			log.debug "low Hum Switch is ${state.sstate}, App State is ${state.appstate} no action"
        }
		
        else if (state.sstate == "on" && state.appstate == "on") {
        	log.trace "Humidity Fell Below ${settings.humidity2}:  sending SMS and turning off ${settings?.switch1}"
			state.appstate = "off"
			send("${humiditySensor1.label} sensed LOW humidity level of ${evt.value} and de-activating ${settings?.switch1}")
            switch1?.off()
        }
            
        else if (state.sstate == "off" && state.appstate == "on"){
        	log.trace "Humid Low, Switch is ${state.sstate}, App State is ${state.appstate} turning app off and sending off message"
            state.appstate = "off"
            send("${humiditySensor1.label} sensed LOW humidity level of ${evt.value}")
        }
        else {
        	log.trace "Humid Low, Switch is ${state.sstate}, App State is ${state.appstate} , No action"
        }
	}
// middle --------------------------
    else {
    	if (state.sstate == "off" && state.appstate == "on"){
        	log.trace "Humid Middle, ${settings.humidity1} - '${currentHumidity}' - ${settings.humidity2}, Switch is ${state.sstate}, App State is ${state.appstate} turning on again"
        	switch1?.on()
     	}
        else {
        	log.trace "Humid Middle, Switch is ${state.sstate}, App State is ${state.appstate} , No action"
        }
	}
    log.debug "end of hume event"
}

private send(msg) {
    if ( sendPushMessage != "No" ) {
        log.debug( "sending push message" )
        sendPush( msg )
    }

    if ( phone1 ) {
        log.debug( "sending text message" )
        sendSms( phone1, msg )
    }

    log.debug msg
}