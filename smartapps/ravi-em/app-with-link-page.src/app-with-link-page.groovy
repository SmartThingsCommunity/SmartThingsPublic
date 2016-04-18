/**
 *  App with Link page
 *
 *  Copyright 2016 Ravi Dubey
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
    name: "App with Link page",
    namespace: "Ravi-em",
    author: "Ravi Dubey",
    description: "Page having link and sub pages.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "hrefPage",nextPage: "page2")
	page(name: "switchPage")
    page(name:"page2",install: false, uninstall: true)
	}
    
def hrefPage() {
    dynamicPage(name: "hrefPage", title: "Manage Switches", uninstall: true) 
    {
        section() {
            href(name: "href",
            title: "Switch Listing page",
            required: false,
            page: "switchPage")
        }
   	}
}

def switchPage() {
    dynamicPage(name: "switchPage", title: "Show Switch Status") {
        section("Switch List") {
            paragraph getAllDevices();
        }
    }
}

def page2() {
    dynamicPage(name: "page2", title: "Choose Devices") {
        section() {
            //input "themotion", "capability.motionSensor", required:true, title:"Where?"
            input "theswitch", "capability.switch", required:true, title:"Which light?",multiple:true
            //input "thelock", "capability.lock", required:true, title:"Which lock?",multiple:true
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

def getAllDevices()
{	
    log.debug "getAllDevices is called";
    log.debug "in for loop:"
    def deviceStatusTxt = " Device 1 -- On \n Device 2 -- On \n Device 3 -- Off"
    def STxt=""
    def Txt=""
    
    def currSwitches = theswitch.currentSwitch
	def onSwitches = currSwitches.findAll { switchVal ->
	switchVal == "on" ? true : false
	}
    
	Txt = "${onSwitches.size()} out of ${theswitch.size()} switches are on \n \n"
    
    for (swt in theswitch) {       
        def nm = swt.displayName;
        def st= swt.currentSwitch        
        
        log.debug "current switch: $st \n"
        
        if(st!='null')        
        {
            log.debug "$nm.value"
            log.debug "$st"
        	STxt=STxt+nm.value+"--"+st+"\n"        
        }
        else if(st=='null')        
        {
            log.debug "$nm.value"
            log.debug "Null"
        	STxt=STxt+nm.value+"-- Null \n"        
        }
        //STxt=STxt+st.value
     }
    STxt = Txt + STxt;
    return STxt;
}
// TODO: implement event handlers