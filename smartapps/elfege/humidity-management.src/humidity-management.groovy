/**
*  Copyright 2015 SmartThings
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
*  Curling Iron
*
*  Author: SmartThings
*  Date: 2013-03-20
*/
definition(
    name: "HUMIDITY MANAGEMENT",
    namespace: "elfege",
    author: "elfege",
    description: "Set a dimmer according to humidity level",
    category: "Convenience",
    iconUrl: "http://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561cb268b9638e8ba6c23/1512332763339/?format=1500w",
    iconX2Url: "http://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561cb268b9638e8ba6c23/1512332763339/?format=1500w",
    iconX3Url: "http://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561cb268b9638e8ba6c23/1512332763339/?format=1500w",

)

preferences {
    page(name: "settings", title: "Set preferences", uninstal: true, install: true)
}

def settings(){
    dynamicPage(name: "settings", title: "Set preferences", uninstal: true, install: true){

        section("Set a dimmer...") {
            input "dimmer", "capability.switchLevel", title: "pick a dimmer", required:true, multiple: true, submitOnChange: true      
        }
        section("Select Humidity Sensor") {
            input "sensor", "capability.relativeHumidityMeasurement", title: "pick a sensor", required:true, multiple: false, submitOnChange: true
        }
        section("Adjust ${dimmer}'s level when a switch is turned on") {
            input "altswt", "capability.switch", title: "select a switch", required:false, multiple: true, submitOnChange: true   
            if(altswt){
                input "tempLevel", "number", title: "set a value", required: true, range: "0..100"
            }
        }
        section("Manage modes") {
            input "Modes", "mode", title: "select modes", required:false, multiple: true, submitOnChange: true
            if(Modes){
                int i = 0
                int s = Modes.size()
                for(s != 0; i < s; i++){
                    input "ModeLevel${i}", "number", title: "set a maximum dimmer setting for when your location is in ${Modes[i]} mode", required: true, range: "0..100"
                }

            }
        }
        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: false, uninstall: true
        }
    }
}

def installed() {
    subscribeToEvents()

}

def updated() {
    unsubscribe()
    subscribeToEvents()
    unschedule()

}

def subscribeToEvents() {
    if(altswt){
        subscribe(altswt, "switch", switchHandler)
    }
    subscribe(dimmer, "level", dimmersHandler)
    subscribe(sensor, "humidity", humidityHandler)

    log.debug "eval checking schedule to run every minute"
    schedule("0 0/1 * * * ?", eval)
}

def dimmersHandler(evt) {

    log.debug "$evt.device set to $evt.value"

}

def switchHandler(evt){
    log.debug "$evt.device turned $evt.value"
    eval()
}

def humidityHandler(evt){
    log.debug "$evt.device returns ${evt.value}% humidity"
    //dimmer.setLevel(evt.value)
}

def eval(){
    // set dimmer to the same level as humidity
    def val = sensor.currentValue("humidity")
    def valRec = val


    if(altswt && "on" in altswt?.currentValue("switch")){
        log.debug "$altswt is on, so now dimmer setting is $tempLevel"
        val = tempLevel
    }
    else if(Modes){
        if(location.currentMode in Modes){
            log.debug "Home is in one the specified modes: ${location.currentMode}"
            int i = 0
            while("${location.currentMode}" != "${Modes[i]}")
            {
                i++
                    }
            def foundMode = Modes[i]
            def ModeLv = "ModeLevel${i}"
            ModeLv = settings.find{it.key == ModeLv}.value
            log.debug "Dimmer value for ${Modes[i]} is $ModeLv"
            val = ModeLv
        }
    }
    /// exception to the exceptions... 
    if(valRec >= 70 && ((Modes && location.currentMode in Modes) || (altswt && "on" in altswt?.currentValue("switch")))){
        log.debug "Humidity is too high, adding speed despite modes or switch exceptions"
        val += 20;
        if(altswt && "on" in altswt?.currentValue("switch")){
            if(valRec >= 75){
                val = valRec // .
                log.debug "someone's inside probably taking a shower... "
            } 
            else {
                val -= 20 // 
                log.debug "someone's inside, probably without taking a shower"
            }
        }
    }

    log.debug "humidity = ${sensor.currentValue("humidity")} | $dimmer set to $val"

    dimmer.setLevel(val)

}