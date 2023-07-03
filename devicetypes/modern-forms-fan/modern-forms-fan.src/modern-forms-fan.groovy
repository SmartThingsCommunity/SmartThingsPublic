/**
 *  Windermier Fan
 *
 *  Copyright 2017 Austin Nelson
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
    definition (name: "Modern Forms Fan", namespace: "modern-forms-fan", author: "Eric Stef") {
        //attribute "fanSpeed", "enum", [1..6]
        
        command "fanOn"
        command "fanOff"
        command "lightOn"
        command "lightOff"
        command "fanSpeed", ["number"]
        command "lightBrightness"
        command "fanDirection"
        command "summer"
        command "winter"
        command "refresh"
    }


    simulator {
        // TODO: define status and reply messages here
    }

    tiles {                
        standardTile("refresh", "refresh", width: 6, height: 1, canChangeIcon: true) {
            state "off", label: 'refresh', action: "refresh", backgroundColor: "#ffffff"
        }
        
        multiAttributeTile(name:"fan", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute ("fanOn", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"fanOff", icon:"st.Lighting.light24", backgroundColor:"#00a0dc", nextState:"off"
                attributeState "off", label:'${name}', action:"fanOn", icon:"st.Lighting.light24", backgroundColor:"#ffffff", nextState:"on"
            }
            tileAttribute ("fanSpeed", key: "SLIDER_CONTROL") {
                attributeState "level", action:"fanSpeed", range: "(40..60)"
            }
        }
        
        multiAttributeTile(name:"light", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute ("lightOn", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"lightOff", icon:"st.Lighting.light21", backgroundColor:"#f1d801", nextState:"off"
                attributeState "off", label:'${name}', action:"lightOn", icon:"st.Lighting.light21", backgroundColor:"#ffffff", nextState:"on"
            }
            tileAttribute ("lightBrightness", key: "SLIDER_CONTROL") {
                attributeState "level", action:"lightBrightness"
            }
        }
        
         standardTile("direction", "fanDirection", width: 6, height: 1) {
            state "off", label: "summer", icon: "st.Weather.weather14", backgroundColor: "#f1d801", action: "summer", nextState: "on"
            state "on", label: "winter", icon: "st.Weather.weather7", backgroundColor: "#1e9cbb", action: "winter", nextState: "off"
        }
        
        main "refresh"
        details (["fan", "light", "direction"])
    }
}

def parse(String description) {
    log.debug 'parse'
}

def generateEvent(Map map) {
    log.debug 'generateEvent'
    def fanOn = map.fanOn ? "on" : "off";
    def lightOn = map.lightOn ? "on": "off";
    def direction = (map.fanDirection == "forward") ? "off" : "on";
    
    sendEvent(name: "fanOn", value: fanOn, isStateChange: true)
    sendEvent(name: "fanSpeed", value: map.fanSpeed, isStateChange: true)
    sendEvent(name: "lightOn", value: lightOn, isStateChange: true)
    sendEvent(name: "lightBrightness", value: map.lightBrightness, isStateChange: true)
    sendEvent(name: "fanDirection", value: direction, isStateChange: true)
    return null
}

def fanOn() {
    log.debug 'fanOn'
    sendCmdToCloud('fanOn', true)
}

def fanOff() {
    log.debug 'fanOff'
    sendCmdToCloud('fanOn', false)
}

def lightOn() {
    log.debug 'lightOn'
    sendCmdToCloud('lightOn', true)
}

def lightOff() {
    log.debug 'lightOff'
    sendCmdToCloud('lightOn', false)
}

def fanSpeed(value) {
    log.debug 'fanSpeed'
    def val = map(value, 0, 100, 1, 6);
    log.debug val
    sendCmdToCloud('fanSpeed', val)
}

def lightBrightness(value) {
    sendCmdToCloud('lightBrightness', value)
}

def summer() {
    log.debug 'summer'
    sendCmdToCloud('fanDirection', 'reverse')
}

def winter() {
    log.debug 'winter'
    sendCmdToCloud('fanDirection', 'forward')
}

def refresh() {
    log.debug 'refresh'
    parent.pollHandler()
}

private sendCmdToCloud(key, value) {
    parent.lambda(device.toString(), key, value)
    
    parent.pollBecause(this)
}

private long map(long x, long in_min, long in_max, long out_min, long out_max)
{
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}