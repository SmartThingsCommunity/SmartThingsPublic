
/**
 *  Ventilation Guru
 *
 *  Copyright 2016 Tom Lawson
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
    name: "Ventilation Guru",
    namespace: "LawsonAutomation",
    author: "Tom Lawson",
    description: "This was a mistaken publication.",
    category: "Green Living",
    iconUrl: "https://raw.githubusercontent.com/lawsonautomation/icons/master/guru-60.png",
    iconX2Url: "https://raw.githubusercontent.com/lawsonautomation/icons/master/guru-120.png",
    iconX3Url: "https://raw.githubusercontent.com/lawsonautomation/icons/master/guru-120.png") 
    
preferences {
    page(name: "mainPage")
}

def mainPage() {
	state.debugMode = false
    
    dynamicPage(name: "mainPage", install: true, uninstall: true) {        
     	}
}

    
def installed() {
	LOG "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	LOG "Updated with settings: ${settings}"
    unschedule()
	unsubscribe()
	initialize()
}

// initiallization methods

def initialize() { 
}


def LOG(String text) {
	if (state.debugMode) {
    	log.debug(text)
    }
}
