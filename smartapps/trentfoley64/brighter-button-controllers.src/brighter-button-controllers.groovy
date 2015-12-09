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
    name: "Brighter Button Controllers",
    singleInstance: true,
    namespace: "trentfoley64",
    author: "A. Trent Foley, Sr.",
    description: "Controller for remote controls up to 4 buttons.  Handles two states: pushed and held.  Allows for control of dimmers, switches, locks, etc",
    category: "My Apps",
  	iconUrl: "http://www.trentfoley.com/ST/icons/brighter-button-controller.png",
    iconX2Url: "http://www.trentfoley.com/ST/icons/brighter-button-controller@2x.png",
	iconX3Url: "http://www.trentfoley.com/ST/icons/brighter-button-controller@3x.png"
)

preferences {
	page(name: "mainPage", title: "Brighter Button Controllers", install: true, uninstall: true, submitOnChange: true) {
		section {
			app(name: "childButtonControllers", appName: "New Brighter Button Controller", namespace: "trentfoley64", title: "new button controller...", multiple: true)
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
            log.info "Installed Button Controllers: ${child.label}"
    }
}