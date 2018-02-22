/**
 *  Broadlink RM
 *
 *  Copyright 2016 Beckyr
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
    name: "Broadlink LAN interface",
    namespace: "smartthings",
    author: "Beckyr",
    description: "Control Broadlink RM Devices using Hub and LAN",
    category: "Convenience",
iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") 
    {
appSetting "BLURL"
appSetting "BLMac"
 }
import groovy.json.*
preferences {
	page(name: "Page1", title: "Broadlink Switch Importer", install: true, uninstall: true){
    section("Make sure you have entered ayour local URL:port for the RM Plugin Bridge, along with the broadlink mac address, into the app settings") {
//	input "switchDevices", "device.broadlink", title: "devices to use", multiple: true
}
}}

def installed() {
   	initialize()
}

def updated() {
	initialize()
}

//For now this only works with one hub.
def initialize() {
	subscribe(location, null, lanResponseHandler, [filterEvents:false])
	def url1=appSettings.BLURL.toString()
	def hubaction = new physicalgraph.device.HubAction(
			method: "GET",
            path: "/codes",
            headers: [HOST: "${url1}"],
               )
            sendHubCommand(hubaction)
          }
          
def lanResponseHandler(evt) {
	def macID=appSettings.BLMac.toString()
	def urlID=appSettings.BLURL.toString()
    def myhubId = location.hubs[0].id
    log.debug "hub id is ${myhubId}"
	log.debug "In response handler"
    def description = evt.description
    def parsedEvent = parseLanMessage(description)
    def text = parsedEvent.body
    def json = new JsonSlurper().parseText(text)
	json.each {
		def codename = "$it.name"
        codename=codename.toLowerCase()
		if(codename == "on") {
			def DevID = "BL-$it.remoteName"
			try {
                def existing = getChildDevice("${DevID}")
        		if(!existing) {
                    log.debug "adding device $it.remoteName with $codename"
					def d = addChildDevice("smartthings", "broadlinkSwitch", "${DevID}", location.hubs[0].id, [label:"$it.remoteName", name:"$it.remoteName", completedSetup: true])
                    d.sendEvent(name:"BLmac", value:macID)
                    d.sendEvent(name:"BLURL", value: urlID)
                    d.sendEvent(name:"onCodeID", value: "${it.id}")
                }
				else {
                   existing.sendEvent(name: "onCodeID", value: "${it.id}")
                }
        	} catch (e) {
							log.error "Error creating device: ${e}"
        	}
		}
        if(codename == "off") {
			def DevID = "BL-$it.remoteName"
			try {
                def existing = getChildDevice("${DevID}")
            	if(!existing) {
	               def d = addChildDevice("smartthings", "broadlinkSwitch", "${DevID}", location.hubs[0].id, [label:"$it.remoteName", name:"$it.remoteName", completedSetup: true])
                   d.sendEvent(name: "BLmac", value: macID)
                   d.sendEvent(name:"BLURL", value: urlID)
                   d.sendEvent(name: "offCodeID", value: "${it.id}")
            	}
				else {
                   existing.sendEvent(name: "offCodeID", value: "${it.id}")
                }
        	} catch (e) {
				log.error "Error creating device: ${e}"
        		}
		}
		} 
	    def children = getChildDevices()
        children.each {
        log.debug "device name: ${it.name}, URL: ${it.currentValue('BLURL')}, mac: ${it.currentValue('BLmac')}, onCodeID: ${it.currentOnCodeID}), offCodeID: ${it.currentOffCodeID}"
        }
}