/**
 *  Dual Relay Adapter (i.e. Enerwave ZWN-RSM2 Adapter, Monoprice Dual Relay, Philio PAN04)
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
   name: "Dual Relay Adapter",
   namespace: "erocm123",
   author: "Eric Maycock",
   description: "Associates Dual Relay Switch Modules with one or two standard SmartThings 'virtual switch' devices for compatibility with standard control and automation techniques",
   category: "My Apps",
   iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
   iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
  section("Configuration:") {
    input "rsm", "capability.switch", title: "Which Dual Relay Module?", multiple: false, required: true
    input "switch1", "capability.switch", title: "Virtual Switch to link to Switch 1?", multiple: false, required: true
    input "switch2", "capability.switch", title: "Virtual Switch to link to Switch 2?", multiple: false, required: false
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
  subscribe(rsm,     "switch1", rsmHandler)
  subscribe(rsm,     "switch2", rsmHandler)
  subscribeToCommand(switch1, "on", switchHandler)
  subscribeToCommand(switch1, "off", switchHandler)
  subscribeToCommand(switch2, "on", switchHandler)
  subscribeToCommand(switch2, "off", switchHandler)
}

def switchHandler(evt) {
  log.debug "switchHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
  switch (evt.deviceId) {
    case switch1.id:
      switch (evt.value) {
        case 'on':
          //log.debug "switch 1 on"
          rsm.on1()
          break
        case 'off':
          //log.debug "switch 1 off"
          rsm.off1()
          break
        }
        break
    case switch2.id:
      switch (evt.value) {
        case 'on':
          //log.debug "switch 2 on"
          rsm.on2()
          break
        case 'off':
          //log.debug "switch 2 off"
          rsm.off2()
          break
        }
        break
    default:
      pass
  }
}

def rsmHandler(evt) {
  log.debug "rsmHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
  if (evt.name == "switch1") {
    switch (evt.value) {
      case 'on':
        switch1.onPhysical()
        break
      case 'off':
        switch1.offPhysical()
        break
    }
  }
  else if (evt.name == "switch2") {
    switch (evt.value) {
      case 'on':
        switch2.onPhysical()
        break
      case 'off':
        switch2.offPhysical()
        break
    }
  }
}