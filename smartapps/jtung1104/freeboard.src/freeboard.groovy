/**
 *  Freeboard
 *
 *  Copyright 2016 Justin Tung
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
    name: "Freeboard",
    namespace: "JTung1104",
    author: "Justin Tung",
    description: "See all the information from all your smart devices in one sexy dashboard.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "", displayLink: "www.freeboard.io"])


preferences {
    section ("Allow external service to control these things...") {
    input "switches", "capability.switch", multiple: true, required: true
    input "motionSensors", "capability.motionSensor", required: true, multiple: true
    input "contactSensors", "capability.contactSensor", required: true, multiple: true
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
	// TODO: subscribe to attributes, devices, locations, etc.
}

// TODO: implement event handlers

mappings {
  path("/switches") {
    action: [
      GET: "listSwitches"
    ]
 }
}

def listSwitches() {
    def resp = []
    
    switches.each {
      def result = []
      
      it.supportedAttributes.each { attr ->
      	result << it["${attr}State"]
      }
      
      resp << [name: it.displayName, id: it.id, label: it.label, states: result]
    }
    
    motionSensors.each {
      def result = []
  	  
      it.supportedAttributes.each { attr ->
      	result << it["${attr}State"]
      }
      
      resp << [name: it.displayName, states: result, id: it.id, label: it.label]
    }
    
    temperatureSensors.each {
      def result = []
      
      it.supportedAttributes.each { attr ->
      	result << it["${attr}State"]
      }
      
      resp << [name: it.displayName, temperature: it.temperatureState, id: it.id, label: it.label]
    }
 
 	contactSensors.each {
      def result = []
      
      it.supportedAttributes.each { attr ->
      	result << it["${attr}State"]
      }
      
      resp << [name: it.displayName, temperature: it.temperatureState, id: it.id, label: it.label]
    }
    
    resp
 }

def updateSwitches() {}