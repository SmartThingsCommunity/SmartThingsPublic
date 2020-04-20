/**
 *  Jalal Educ
 *
 *  Copyright 2018 academie educ
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
    name: "Jalal Educ",
    namespace: "موقع التربية و التعليم في الجزائر",
    author: "academie educ",
    description: "\u0645\u0648\u0642\u0639 \u0627\u0644\u062A\u0631\u0628\u064A\u0629 \u0648 \u0627\u0644\u062A\u0639\u0644\u064A\u0645 \u0641\u064A \u0627\u0644\u062C\u0632\u0627\u0626\u0631- \u0645\u062F\u0648\u0646\u0629 \u062A\u0647\u062A\u0645 \u0628\u062F\u0631\u0648\u0633 \u0627\u0644\u062A\u0639\u0644\u064A\u0645 \u0641\u064A \u0627\u0644\u062C\u0632\u0627\u0626\u0631  \u0645\u0646 \u0627\u0644\u0645\u0631\u062D\u0644\u0629 \u0627\u0644\u0625\u0628\u062A\u062F\u0627\u0626\u064A\u0629 \u0625\u0644\u0649 \u0627\u0644\u0645\u062A\u0648\u0633\u0637 ,  \u0645\u0630\u0643\u0631\u0627\u062A \u0648\u062F\u0631\u0648\u0633 \u0648\u062D\u0644\u0648\u0644 \u0627\u0644\u062A\u0645\u0627\u0631\u064A\u0646 \u062C\u0627\u0647\u0632\u0629 \u0644\u0644\u062A\u062D\u0645\u064A\u0644 , ",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


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