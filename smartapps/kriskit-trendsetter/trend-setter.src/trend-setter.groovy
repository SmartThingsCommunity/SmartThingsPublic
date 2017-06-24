/**
 *  Trend Setter
 *
 *  Copyright 2015 Chris Kitch
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
    name: "Trend Setter",
    namespace: "kriskit.trendSetter",
    author: "Chris Kitch",
    description: "Uses virtual child devices to group other devices together and perform commands and aggregate data from the group.",
    category: "My Apps",
    iconUrl: "https://cdn.rawgit.com/Kriskit/SmartThingsPublic/master/smartapps/kriskit/trendsetter/icon.png",
    iconX2Url: "https://cdn.rawgit.com/Kriskit/SmartThingsPublic/master/smartapps/kriskit/trendsetter/icon@2x.png",
    iconX3Url: "https://cdn.rawgit.com/Kriskit/SmartThingsPublic/master/smartapps/kriskit/trendsetter/icon@3x.png",
    singleInstance: true)


preferences {
    page(name: "mainPage")
}

def mainPage() {
	dynamicPage(name: "mainPage", title: "Your Groups", install: true, uninstall: true, submitOnChange: true) {
        section {
        	app(name: "groups", appName: "Group", namespace: "kriskit.trendSetter", title: "Create Group...", multiple: true)
        }
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