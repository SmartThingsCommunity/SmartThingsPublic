/**

 * Nue Switch Binder

 *

 *  Copyright 2016 Michael Hudson

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

    name: "Nue 2-Way Switch Binder",

    namespace: "3A",

    author: "Kevin X",

    description: "Use to bind a light switch in ST to the buttons on a Nue Scene Switch",

    category: "Convenience",

    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",

    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",

    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")




preferences {

  section("Select Switch One to bind..."){

    input(name: "switch1", type: "capability.switch", title: "Switch One", required: true)

  }

  section("Select Switch Two to bind"){

    input(name: "switch2", type: "capability.switch", title: "Switch Two", multiple: true, required: true)

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

  subscribe(switch1, "switch", switchOneHandler)

  subscribe(switch2, "switch", switchTwoHandler)

}




def switchOneHandler(evt) { 

    if (evt.value == "on") {

    	log.debug "switch 2 ON"           

 		switch2.on()

    } else // if (evt.value == null) 

    {

        log.debug "switch 2 OFF"           

 		switch2.off()

 	}

}


def switchTwoHandler(evt) {

    if (evt.value == "on") {

    	log.debug "switch 1 ON"           

 		switch1.on()

    } else //if (evt.value == null) 

    {

        log.debug "switch 1 OFF"           

 		switch1.off()

 	}

}