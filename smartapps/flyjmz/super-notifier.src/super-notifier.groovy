/*
Super Notifier
   
https://github.com/flyjmz/jmzSmartThings
 


Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
   in compliance with the License. You may obtain a copy of the License at:
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
   for the specific language governing permissions and limitations under the License.
 
   
Credits, based on work from:  
	"Notify Me When" by SmartThings dated 2013-03-20
	"Turn off after some minutes with options" by Bruce Ravenel dated 2015
	"Left It Open" by SmartThings dated 2013-05-09
  
Version History:
	1.0 - 5Sep2016, Initial Commit
    1.1 - 10Oct2016, public release
 
 */
 
definition(
	name: "Super Notifier",
	namespace: "flyjmz",
	author: "flyjmz230@gmail.com",
	description: "One stop shop for alerts",
	category: "My Apps",
	iconUrl: "https://github.com/flyjmz/jmzSmartThings/raw/master/resources/phone2x.png",
	iconX2Url: "https://github.com/flyjmz/jmzSmartThings/raw/master/resources/phone2x.png",
    singleInstance: true
)

preferences {
	page(name: "setup", title: "Super Notifier", install: true, uninstall: true, submitOnChange: true) {
    	section("") {
			app(name: "Instant Alert", appName: "Super Notifier - Instant Alert", namespace: "flyjmz", title: "Add instant alert", description: "Add an instant alert to notify as soon as something happens", multiple: true)
            app(name: "Delayed Alert", appName: "Super Notifier - Delayed Alert", namespace: "flyjmz", title: "Add delayed alert", description: "Add a delayed alert to notify when something has been left open/closed or on/off",multiple: true)
		}
	}
}

def installed() {
}

def updated() {
	unsubscribe()
}