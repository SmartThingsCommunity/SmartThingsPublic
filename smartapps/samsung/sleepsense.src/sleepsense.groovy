/**
 *  Samsung Home Appliances
 *
 *  Copyright 2015 Mobileapp
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
 
import physicalgraph.device.*;
 
definition(
    name: "SLEEPsense",
    namespace: "samsung",
    author: "Samsung Mobileapp",
    description: "SLEEPsense user configuration",
    category: "Health & Wellness",
    iconUrl: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@2x.png",
    oauth: true)

preferences {	
    page(name: "mainPage", title: "SLEEPsense automation setup", install:true, uninstall: true){
        section{
            app "SLEEPsense-inbed", 		"samsung", "SLEEPsense-in bed", 			title: "Control lights when you're in bed.", 			page: "Mainpage", multiple: true, install: true
            app "SLEEPsense-outofbed", 		"samsung", "SLEEPsense-out of bed", 		title: "Control lights when you're out of bed.", 		page: "Mainpage", multiple: true, install: true
            app "SLEEPsense-thermostate", 	"samsung", "SLEEPsense-thermostat control", title: "Control thermostat when you sleep & wake up.", 	page: "Mainpage", multiple: true, install: true
            app "SLEEPsense-awake", 		"samsung", "SLEEPsense-wake up", 			title: "When I wake up, do this.", 						page: "Mainpage", multiple: true, install: true
            app "SLEEPsense-sleep", 		"samsung", "SLEEPsense-fall asleep", 		title: "When you fall asleep, do this.", 				page: "Mainpage", multiple: true, install: true
        }
    }
}

def whitelist() {
	return [
    	"ajax.googleapis.com",
    	"d102a5bcjkdlos.cloudfront.net"
    ]
}