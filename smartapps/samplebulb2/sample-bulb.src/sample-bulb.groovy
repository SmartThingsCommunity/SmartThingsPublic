/**
 *  sample bulb
 *
 *  Copyright 2017 venkatesh s
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
    name: "sample bulb",
    namespace: "sampleBulb2",
    author: "venkatesh s",
    description: "sjdbsjk",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
  section ("Allow external service to control these things...") {
    input "bulbs", "capability.lock", multiple: true, required: true
  }
}

def listLocks() {

    def resp = []
    bulbs.each {
        resp << [name: it.displayName, value: it.currentValue("bulb")]
    }
    return resp
}


mappings {
  path("/bulbs") {
    action: [
      GET: "listBulbs"
    ]
  }
  path("/bulbs/:command") {
    action: [
      PUT: "updateBulbs"
    ]
  }
}

void updateLocks() {
    // use the built-in request object to get the command parameter
    def command = params.command

    // all switches have the comand
    // execute the command on all switches
    // (note we can do this on the array - the command will be invoked on every element
    switch(command) {
        case "on":
            bulbs.lock()
            break
        case "off":
            bulbs.unlock()
            break
        default:
            httpError(400, "$command is not a valid command for all switches specified")
    }

}

def installed() {}

def updated() {}
