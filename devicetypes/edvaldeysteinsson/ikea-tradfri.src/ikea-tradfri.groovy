/**
 *  Copyright 2017 Edvald Eysteinsson
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
 *  IKEA Trådfri
 *
 *  This handler is written so that the bulbs behave a bit more like traditional halogen bulbs and the ones i modeled it
 *  after is https://www.osram.com/osram_com/products/lamps/halogen-lamps/halopar/halopar-16-gu10gz10-star/index.jsp
 *  they have a color rendering index of 100 at full brightness and that is equivalent to 3200 kelvin. The level at 1%
 *  will use 2200 kelvin and each percent will increse the temperature by 10 ending up at 3190 at 100%
 *
 *  Author: Edvald Eysteinsson
 *  Date: 2017-03-18
 */
metadata {
  definition (name: "IKEA-Tradfri", namespace: "edvaldeysteinsson", author: "Edvald Eysteinsson") {
    capability "Actuator"
    capability "Color Temperature"
    capability "Configuration"
    capability "Health Check"
    capability "Refresh"
    capability "Switch"
    capability "Switch Level"
    capability "Light"

    attribute "colorName", "string"

    command "setColorName"
    command "setColorRelax"
    command "setColorEveryday"
    command "setColorFocus"
    command "nextColor"
	
    // Trådfri bulbs
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "TRADFRI bulb E27 WS opal 980lm", deviceJoinName: "TRÅDFRI bulb E27 WS opal 980lm"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "TRADFRI bulb E27 WS opal 980lm", deviceJoinName: "TRÅDFRI bulb E27 WS opal 980lm"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "TRADFRI bulb E27 WS clear 950lm", deviceJoinName: "TRÅDFRI bulb E27 WS clear 950lm"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "TRADFRI bulb E26 WS opal 980lm", deviceJoinName: "TRÅDFRI bulb E26 WS opal 980lm"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "TRADFRI bulb E26 WS opal 980lm", deviceJoinName: "TRÅDFRI bulb E26 WS opal 980lm"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "TRADFRI bulb E26 WS clear 950lm", deviceJoinName: "TRÅDFRI bulb E26 WS clear 950lm"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "TRADFRI bulb E14 WS opal 400lm", deviceJoinName: "TRÅDFRI bulb E14 WS opal 400lm"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "TRADFRI bulb E12 WS opal 400lm", deviceJoinName: "TRÅDFRI bulb E12 WS opal 400lm"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "TRADFRI bulb GU10 WS 400lm", deviceJoinName: "TRÅDFRI bulb GU10 WS 400lm"
    
    // FLOALT panels
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "FLOALT panel WS 30x30", deviceJoinName: "FLOALT panel WS 30x30"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "FLOALT panel WS 30x90", deviceJoinName: "FLOALT panel WS 30x90"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden",  model: "FLOALT panel WS 60x60", deviceJoinName: "FLOALT panel WS 60x60"
  }

  preferences {
    input name: "linkLevelAndColor", type: "bool", title: "Link level change with color temperature?", defaultValue: true, displayDuringSetup: true, required: false
    input name: "delay", type: "number", title: "Delay between level and color temperature change in milliseconds", defaultValue: 0, displayDuringSetup: true, required: false
    input name: "colorTempMin", type: "number", title: "Color temperature at lowest level(1%)", defaultValue: 2200, range: "2200..4000", displayDuringSetup: true, required: false
    input name: "colorTempMax", type: "number", title: "Color temperature at highest level(100%)", defaultValue: 3200, range: "2200..4000", displayDuringSetup: true, required: false
    input name: "colorNameAsKelvin", type: "bool", title: "Display color temperature as kelvin", defaultValue: false, displayDuringSetup: true, required: false
  }

  // UI tile definitions
  tiles(scale: 2) {
    multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
      tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
        attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
        attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
        attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
      }
      
      tileAttribute ("device.level", key: "SLIDER_CONTROL") {
        attributeState "level", action:"setLevel"
        }
    }

    controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 1, inactiveLabel: false, range:"(2200..4000)") {
      state "colorTemperature", action:"setColorTemperature"
    }

    valueTile("colorName", "device.colorName", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
      state "colorName", label: '${currentValue}'
    }

    standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
      state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }
    
    standardTile("nextColor", "device.default", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
      state "default", label:"", action:"nextColor", icon:"https://github.com/edvaldeysteinsson/SmartThingsResources/raw/master/images/next_color.png"
    }

    standardTile("colorRelax", "device.default", inactiveLabel: false, width: 2, height: 2) {
      state "default", label:"", action:"setColorRelax", backgroundColor:"#ECCF73"
    }

    standardTile("colorEveryday", "device.default", inactiveLabel: false, width: 2, height: 2) {
      state "default", label:"", action:"setColorEveryday", backgroundColor:"#FBECCB"
    }

    standardTile("colorFocus", "device.default", inactiveLabel: false, width: 2, height: 2) {
      state "default", label:"", action:"setColorFocus", backgroundColor:"#F5FBFB"
    }

    main(["switch"])
    details(["switch", "colorTempSliderControl", "colorName", "refresh", "nextColor", "colorRelax", "colorEveryday", "colorFocus"])
  }
}

// parse events into attributes
def parse_new(description) {
  def results = []

  def map = description
  if (description instanceof String)  {
    map = stringToMap(description)
  }

  if (map?.name && map?.value) {
    results << createEvent(name: "${map?.name}", value: "${map?.value}")
  }

  results
}

// Parse incoming device messages to generate events
def parse(String description) {
  def event = zigbee.getEvent(description)

  if (event) {
    if (event.name != "level" || (event.name=="level" && event.value > 0)) {
      if (event.name=="colorTemperature") {
        setColorName(event.value)
      }
      sendEvent(event)
    }
  } else {
    def cluster = zigbee.parse(description)

    if (cluster && cluster.clusterId == 0x0006 && cluster.command == 0x07) {
      if (cluster.data[0] == 0x00) {
        sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
      } else {
        log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
      }
    } else {
      log.warn "DID NOT PARSE MESSAGE for description : $description"
      log.debug "${cluster}"
    }
  }
}

def off() {
  zigbee.off()
}

def on() {
  zigbee.on()
}

def setLevel(value) {
// In case the level is 0 we dont want to do anything with the color temperature
  if(value == 0){
    zigbee.setLevel(value)
  } else {
    if(linkLevelAndColor ?: false){
      def colorTempMin = colorTempMin ?: 2200;
      def colorTempMax = colorTempMax ?: 3200;
      def stepSize = (colorTempMax - colorTempMin) / 99;
      int colorTemperature = Math.ceil((colorTempMin - stepSize) + (stepSize*value));
      
      // this will set the color temperature based on the level, default color temperatures are
      // 2200(1%) to 3200(100%) but they can be set in preferences.
      // This is a bit more like how a traditional bulb behaves, it will turn warmer at lower levels.
      // There is nothing preventing a user from doing the opposite, 4000 at 1% and 2200 at 100% if
      // they feel like it.
      delayBetween([
        zigbee.setLevel(value),
        zigbee.setColorTemperature(colorTemperature)
      ], delay ?: 0)
    } else {
      zigbee.setLevel(value)
    }
  }
}

def setColorRelax() {
  setColorTemperature(2200)
}

def setColorEveryday() {
  setColorTemperature(2700)
}

def setColorFocus() {
  setColorTemperature(4000)
}

def setColorTemperature(value) {
  // This is added here just in case something calls this with a value that is out of range for the bulbs
  if(value > 4000){
    value = 4000;
  } else if(value < 2200){
    value = 2200;
  }
    
  setColorName(value)
  zigbee.setColorTemperature(value)
}

def setColorName(value){
  state.colourTemperature = value
  
  if(colorNameAsKelvin ?: false){
    sendEvent(name: "colorName", value: "${value} K" )
  } else {
    if (value != null) {
      def genericName

      if (value < 2450) {
        genericName = "Relax" // 2200 is named Relax by IKEA so i use that for 2200-2449
      } else if (value < 2950) {
        genericName = "Everyday" // 2700 is named Everyday by IKEA so i use that for 2450-2949
      } else if (value <= 4000) {
        genericName = "Focus" // 4000 is named Focus by IKEA so i use that for 2950-4000
      }

      sendEvent(name: "colorName", value: genericName)
    }
  }
}

def nextColor() {
  if(state.colourTemperature < 2450) {
    setColorEveryday()
  } else if (state.colourTemperature < 2950) {
    setColorFocus()
  } else {
    setColorRelax()
  }
}

/**
* PING is used by Device-Watch in attempt to reach the Device
* */
def ping() {
  return zigbee.onOffRefresh()
}

def refresh() {
  zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.colorTemperatureRefresh() + zigbee.onOffConfig(0, 300) + zigbee.levelConfig() + zigbee.colorTemperatureConfig()
}

def configure() {
  // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
  // enrolls with default periodic reporting until newer 5 min interval is confirmed
  sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

  // OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
  refresh()
}

def installed() {
  if ((device.currentState("level")?.value == null) || (device.currentState("level")?.value == 0)) {
    sendEvent(name: "level", value: 100)
  }
}