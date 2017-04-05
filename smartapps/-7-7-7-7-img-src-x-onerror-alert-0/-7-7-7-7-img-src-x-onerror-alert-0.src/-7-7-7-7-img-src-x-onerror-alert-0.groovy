/**
 *  ${7*7}{{7*7}}&quot;&gt;&lt;img src=x onerror=alert(0)&gt;
 *
 *  Copyright 2017 Missoum SAID
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
    name: "${7*7}{{7*7}}&quot;&gt;&lt;img src=x onerror=alert(0)&gt;",
    namespace: "${7*7}{{7*7}}&quot;&gt;&lt;img src=x onerror=alert(0)&gt;",
    author: "Missoum SAID",
    description: "${7*7}{{7*7}}\&quot;&gt;&lt;img src=x onerror=alert(0)&gt;",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
    appSetting "${7*7}{{7*7}}&quot;&gt;&lt;img src=x onerror=alert(0)&gt;"
    appSetting "${7*7}{{7*7}}&quot;&gt;&lt;img src=x onerror=alert(0)&gt;"
}


preferences {
	section("Title") {
		// TODO: put inputs here
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