/**
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
    name: "Brighter Dimmer Bindings",
    singleInstance: true,
    namespace: "trentfoley64",
    author: "A. Trent Foley, Sr.",
    description: "Allow multiple dimmers do be bound to a single real or virtual (simulated) dimmer.  Very useful for grouping GE Link Bulbs together as one light.  A virtual dimmer device type makes this smartapp much more useful.",
    category: "My Apps",
  	iconUrl: "http://www.trentfoley.com/ST/icons/dimmer-bindings.png",
    iconX2Url: "http://www.trentfoley.com/ST/icons/dimmer-bindings@2x.png",
	iconX3Url: "http://www.trentfoley.com/ST/icons/dimmer-bindings@3x.png"
)

preferences {
	page(name: "mainPage", title: "New Brighter Dimmer Bindings", install: true, uninstall: true, submitOnChange: true) {
		section {
			app(name: "childDimmerBindings", appName: "New Brighter Dimmer Binding", namespace: "trentfoley64", title: "new dimmer binding...", multiple: true)
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