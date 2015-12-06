/**
 *  Rule
 *
 *  Copyright 2015 A. Trent Foley
 *
 *  Version 1.0.0  5 Dec 2015
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
    name: "Dimmer Bindings",
    singleInstance: true,
    namespace: "trentfoley64",
    author: "A. Trent Foley, Sr.",
    description: "Allow multiple dimmers do be bound to a single real or virtual (simulated) dimmer.  Very useful for grouping GE Link Bulbs together as one light.  A virtual dimmer device type makes this smartapp much more useful.",
    category: "My Apps",
  	iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic@2x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic@3x.png"
)

preferences {
	page(name: "mainPage", title: "Dimmer Bindings", install: true, uninstall: true, submitOnChange: true) {
		section {
			app(name: "childDimmerBindings", appName: "_Dimmer Binding", namespace: "trentfoley64", title: "Create New Dimmer Binding...", multiple: true)
		}
	}
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    childApps.each {child ->
            log.info "Installed Bindings: ${child.label}"
    }
}