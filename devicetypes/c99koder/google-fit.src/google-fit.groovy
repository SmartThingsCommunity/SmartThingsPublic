/**
 *  Google Fit
 *
 *  Copyright 2016 Sam Steele
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
	definition (name: "Google Fit", namespace: "c99koder", author: "Sam Steele") {
		capability "Polling"
		capability "Refresh"
		capability "Step Sensor"
        attribute "weight", "number"
        attribute "weight_string", "string"
	}

	tiles(scale: 2) {
		standardTile("steps", "device.steps", width: 4, height: 4, canChangeIcon: false, canChangeBackground: false) {
            state("steps", label: '${currentValue} Steps', icon:"st.Health & Wellness.health11", backgroundColor: "#ffffff")                     
        }
        standardTile("weight", "device.weight_string", width: 2, height: 2, canChangeIcon: false, canChangeBackground: false) {
            state("weight_string", label: '${currentValue}', icon:"st.Bath.bath2", backgroundColor: "#ffffff")
        }
		standardTile("refresh", "device.switch", width: 2, height: 2, decoration: "flat") {
			state "icon", action:"refresh.refresh", icon:"st.secondary.refresh", defaultState: true
		}
        main "steps"
        details(["steps", "weight", "refresh"])
	}
}

def parse(String description) {}

def poll() {
	def steps = parent.getSteps()
    
    if(steps) {
    	sendEvent("name":"steps", "value":steps)
        if(parent.getStepsGoal())
	        sendEvent("name":"goal", "value":parent.getStepsGoal())
        else
    	    sendEvent("name":"goal", "value":0)
    } else {
    	log.debug "No Google Fit steps data available"
    }

	def weight = parent.getWeight()
    
    if(weight) {
    	sendEvent("name":"weight", "value":weight)
        if(parent.isMetric()) {
    		sendEvent("name":"weight_string", "value":String.format("%.2f kg",weight))
        } else {
        	weight *= 2.20462;
	    	sendEvent("name":"weight_string", "value":String.format("%.2f lbs",weight))
        }
    } else {
    	log.debug "No Google Fit weight data available"
    }
}

def refresh() {
    poll()
}