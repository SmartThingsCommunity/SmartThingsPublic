/**
 *  TV Channel Handler
 *
 *  Copyright 2017 Rebecca Onuschak
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
    name: "TV Channel Handler",
    namespace: "beckyricha",
    author: "Rebecca Onuschak",
    description: "Converts a TV channel device to any smart device that can send a remote commands with a number (harmony, broadlink, other...).",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {

	page( name:"intro", title:"Channels Importer", nextPage:"zero", uninstall:true, install:false) 
    {
    section("Before using this tool, you need separate 'switch' devices set up to control each digit (0-9) on your media device.  Please also run the Channel Adder SmartApp before this one.  Press Next to continue."){} 
 	}
    page( name:"zero", title:"Channels Importer", nextPage:"one", uninstall:true, install:false ) 
    {
    section("Select device for remote key 0:") 
        {input "number0", "capability.switch", multiple: false}
    }
        page( name:"one", title:"Channels Importer", nextPage:"two", uninstall:true, install:false ) 
    {
    section("Select device for remote key 1:") 
        {input "number1", "capability.switch", multiple: false}
    }
           page( name:"two", title:"Channels Importer", nextPage:"three", uninstall:true, install:false ) 
    {
    section("Select device for remote key 2:") 
        {input "number2", "capability.switch", multiple: false}
    }
            page( name:"three", title:"Channels Importer", nextPage:"four", uninstall:true, install:false ) 
    {
    section("Select device for remote key 3:") 
        {input "number3", "capability.switch", multiple: false}
    }
            page( name:"four", title:"Channels Importer", nextPage:"five", uninstall:true, install:false ) 
    {
    section("Select device for remote key 4:") 
        {input "number4", "capability.switch", multiple: false}
    }        
    page( name:"five", title:"Channels Importer", nextPage:"six", uninstall:true, install:false ) 
    {
    section("Select device for remote key 5:") 
        {input "number5", "capability.switch", multiple: false}
    }
    page( name:"six", title:"Channels Importer", nextPage:"seven", uninstall:true, install:false ) 
    {
    section("Select device for remote key 6:") 
        {input "number6", "capability.switch", multiple: false}
    }
   page( name:"seven", title:"Channels Importer", nextPage:"eight", uninstall:true, install:false ) 
    {
    section("Select device for remote key 7:") 
        {input "number7", "capability.switch", multiple: false}
    }
            page( name:"eight", title:"Channels Importer", nextPage:"nine", uninstall:true, install:false ) 
    {
    section("Select device for remote key 8:") 
        {input "number8", "capability.switch", multiple: false}
    }
            page( name:"nine", title:"Channels Importer", nextPage:"channels", uninstall:true, install:false ) 
    {
    section("Select devices for remote key 9:") 
        {input "number9", "capability.switch", multiple: false}
    }
    page( name:"channels", title:"Channels Importer", uninstall:true, install:true ) 
    {
        section("Select channels to control:") 
        {input "switches", "device.dummyswitch", multiple: true}
       }
}

def switchesHandler(evt) {
    log.debug "one of the configured switches changed states"
    def evtDevice = evt.getDevice()
    log.debug "${evtDevice.name}"
    def devID = evtDevice.currentValue("guidenum")
	if(devID.isNumber()){
    	def len = devID.length()
        log.debug "length = ${len}"
    	for (def i = 0; i <len; i++) {
    		def num=devID.substring(i,i+1)
            def mydevice = settings."number${num}"
            log.debug "the device is ${mydevice.name}"
    		mydevice.on()
    	}   
	}
}

def initialize() {
	    subscribe(switches, "switch", switchesHandler)
}
def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}