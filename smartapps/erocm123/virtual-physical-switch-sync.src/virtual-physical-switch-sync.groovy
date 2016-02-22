/**
 *  Virtual / Physical Switch Sync (i.e. Enerwave ZWN-RSM2 Adapter, Monoprice Dual Relay, Philio PAN04)
 *
 *  Copyright 2016 Eric Maycock (erocm123)
 * 
 *  Special thanks to Joel Tamkin for the bulk of this code. Special thanks to Justin Ellison for other additions and "cleanups"
 * 
 *  2016-01-13: erocm123 - Added "on/offPhysical()" code (as suggested by Justin Ellison) to prevent the app from turning lights on/off when quickly flicking switch.
 *                         Must be using the "Simulated Switch" device type for this to work right.
 *  2015-10-29: erocm123 - I removed the scheduled refreshes for my Philio PAN04 as it supports instant status updates with my custom device type
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
definition(
   name: "Virtual / Physical Switch Sync",
   namespace: "erocm123",
   author: "Eric Maycock",
   description: "Keeps multi switch devices like the Aeon Smartstrip, Monoprice Dual Relay, and Philio PAN04 in sync with their virtual switches",
   category: "My Apps",
   iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
   iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
  section("Configuration:") {
    input "physical", "capability.switch", title: "Which Dual Relay Module?", multiple: false, required: true
    input "virtual1", "capability.switch", title: "Virtual Switch to link to Switch 1?", multiple: false, required: true
    input "virtual2", "capability.switch", title: "Virtual Switch to link to Switch 2?", multiple: false, required: false
    input "virtual3", "capability.switch", title: "Virtual Switch to link to Switch 3?", multiple: false, required: true
    input "virtual4", "capability.switch", title: "Virtual Switch to link to Switch 4?", multiple: false, required: false
  }
}

def installed() {
  log.debug "Installed with settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
  unsubscribe()
  initialize()
}

def initialize() {
  log.debug "Initializing Dual Relay Adapter v1.0.1"
  subscribe(physical,     "switch1", physicalHandler)
  subscribe(physical,     "switch2", physicalHandler)
  subscribe(physical,     "switch3", physicalHandler)
  subscribe(physical,     "switch4", physicalHandler)
  subscribeToCommand(virtual1, "on", virtualHandler)
  subscribeToCommand(virtual1, "off", virtualHandler)
  subscribeToCommand(virtual2, "on", virtualHandler)
  subscribeToCommand(virtual2, "off", virtualHandler)
  subscribeToCommand(virtual3, "on", virtualHandler)
  subscribeToCommand(virtual3, "off", virtualHandler)
  subscribeToCommand(virtual4, "on", virtualHandler)
  subscribeToCommand(virtual4, "off", virtualHandler)
}

def virtualHandler(evt) {
  log.debug "switchHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
  switch (evt.deviceId) {
    case virtual1.id:
      switch (evt.value) {
        case 'on':
          //log.debug "switch 1 on"
          physical.on1()
          break
        case 'off':
          //log.debug "switch 1 off"
          physical.off1()
          break
        }
        break
    case virtual2.id:
      switch (evt.value) {
        case 'on':
          //log.debug "switch 2 on"
          physical.on2()
          break
        case 'off':
          //log.debug "switch 2 off"
          physical.off2()
          break
        }
        break
        case virtual3.id:
      switch (evt.value) {
        case 'on':
          //log.debug "switch 2 on"
          physical.on3()
          break
        case 'off':
          //log.debug "switch 2 off"
          physical.off3()
          break
        }
        break
        case virtual4.id:
      switch (evt.value) {
        case 'on':
          //log.debug "switch 2 on"
          physical.on4()
          break
        case 'off':
          //log.debug "switch 2 off"
          physical.off4()
          break
        }
        break
    default:
      pass
  }
}

def physicalHandler(evt) {
  log.debug "rsmHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
  if (evt.name == "switch1") {
    switch (evt.value) {
      case 'on':
        virtual1.onPhysical()
        break
      case 'off':
        virtual1.offPhysical()
        break
    }
  }
  else if (evt.name == "switch2") {
    switch (evt.value) {
      case 'on':
        virtual2.onPhysical()
        break
      case 'off':
        virtual2.offPhysical()
        break
    }
  }
  else if (evt.name == "switch3") {
    switch (evt.value) {
      case 'on':
        virtual3.onPhysical()
        break
      case 'off':
        virtual3.offPhysical()
        break
    }
    }
    else if (evt.name == "switch4") {
    switch (evt.value) {
      case 'on':
        virtual4.onPhysical()
        break
      case 'off':
        virtual4.offPhysical()
        break
    }
    }
}