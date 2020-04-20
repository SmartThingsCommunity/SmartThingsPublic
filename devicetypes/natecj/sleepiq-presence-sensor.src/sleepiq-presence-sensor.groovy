/**

 *  SleepIQ Presence Sensor

 *

 *  Copyright 2015 Nathan Jacobson

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

  definition (name: "SleepIQ Presence Sensor", namespace: "natecj", author: "Nathan Jacobson") {

    capability "Presence Sensor"

    capability "Switch"

    capability "Polling"



    command "arrived"

    command "departed"

    

    attribute "bedId", "String"

    attribute "mode", "enum", ["Both", "Either", "Left", "Right"]



    command "setStatus", ["string"]

    command "setBedId", ["string"]

    command "setMode", ["string"]

  }



  simulator {

    status "present": "presence: present"

    status "not present": "presence: not present"

	status "on": "switch: on"

    status "off": "switch: not off"

  }



/*

  preferences {

    section("Settings:") {

      input("mode", title: "Mode", "enum", required: false, defaultValue: "Either", options: ["Left", "Right", "Both", "Either"], description: "The side(s) of the bed to monitor")

    }

  }

*/



  tiles {

    standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {

      state("not present", label:'not present', icon:"st.presence.tile.not-present", backgroundColor:"#ffffff", action:"arrived")

	  state("present", label:'present', icon:"st.presence.tile.present", backgroundColor:"#53a7c0", action:"departed")

    }

    standardTile("refresh", "device.poll", inactiveLabel: false, decoration: "flat") {

      state "default", action:"polling.poll", icon:"st.secondary.refresh"

    }

    valueTile("bedId", "device.bedId", width: 3, height: 1) {

      state "default", label: '${currentValue}'

    }

    valueTile("mode", "device.mode", width: 1, height: 1) {

      state "default", label: '${currentValue}'

    }

    

    main "presence"

    details(["presence", "refresh", "mode", "bedId"])

  }

}



def installed() {

  log.trace 'installed()'

}



def updated() {

  log.trace 'updated()'

}



def poll() {

  log.trace "poll()"

  parent.refreshChildDevices()

}



def parse(String description) {

  log.trace "parse() - Description: ${description}"

  def results = []

  /*

  def pair = description.split(":")

  results = createEvent(name: pair[0].trim(), value: pair[1].trim())

  //results = createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")

  */

  log.debug "parse() - Results: ${results.inspect()}"

  results

}



def arrived() {

  log.trace "arrived()"

  sendEvent(name: "presence", value: "present")

  sendEvent(name: "switch", value: "on")

}



def departed() {

  log.trace "departed()"

  sendEvent(name: "presence", value: "not present")

  sendEvent(name: "switch", value: "off")

}



def on() {

  log.trace "on()"

  arrived()

}



def off() {

  log.trace "off()"

  departed()

}



def setStatus(val) {

  log.trace "setStatus($val)"

  if (val) {

    arrived()

  } else {

    departed()

  }

}



def setBedId(val) {

  log.trace "setBedId($val)"

  sendEvent(name: "bedId", value: val)

}



def setMode(val) {

  log.trace "setMode($val)"

  sendEvent(name: "mode", value: val)

}