/**
 *
 *  Copyright 2021 Jose Augusto Baranauskas
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
definition (
    name: "Fan Automation",
    namespace: "baranauskas",
    author: "Jose Augusto Baranauskas",
    version: "1.0 (2021-03-25)",
    singleInstance: true,
    description: "Create fans automation based on temperature sensors",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
        page(name: "parentPage")
        page(name: "aboutPage")
}

def parentPage() {
	return dynamicPage(name: "parentPage", title: "", nextPage: "", install: true, uninstall: true) {
        section("Create a new fan automation.") {
            app(name: "FanAutomationChild", appName: "Fan Automation" , namespace: "baranauskas", title: "Press here for new Fan Automation", multiple: true)
        }
        section("About") {
          	paragraph textHelp()
   		  }
    }
}

def aboutPage() {
	dynamicPage(name: "aboutPage", title: none, install: true, uninstall: true) {
     	section("About") {
        	paragraph textHelp()
 		}
	}
}

private def textHelp() {
	def text =
		"This smartapp provides automatic control of ceiling fan speeds using"+
    " average temperature sensors based on a temperature threshold"+
    " changing each speed automatically in differential degree increments.\n\n"+
    "A (virtual) switch is used to enable/disable fan automation. Notifications"+
    " can be enable for automation events."
}