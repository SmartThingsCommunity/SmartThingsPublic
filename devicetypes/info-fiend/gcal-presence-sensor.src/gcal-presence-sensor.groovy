/**
 *  Copyright 2017 Anthony Pastor
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
 * Updates:
 *
 * 20170422.1 - added Health Check 
 * 20170306.1 - Scheduling updated
 * 				Fixed Event Trigger with no search string.
 *				Added AskAlexa Message Queue compatibility
 *                
 * 20170302.1 - Initial release 
 *
 */
 
metadata {
	// Automatically generated. Make future change here.
	definition (name: "GCal Presence Sensor", namespace: "info_fiend", author: "anthony pastor") {

        capability "Presence Sensor"
		capability "Sensor"
        capability "Polling"
		capability "Refresh"
        capability "Switch"
        capability "Actuator"
        capability "Health Check"

		command "arrived"
		command "departed"
        command "present"
		command "away"       
        
        attribute "calendar", "json_object"
        attribute "calName", "string"
        attribute "eventSummary", "string"
        attribute "arriveTime", "number"
        attribute "departTime", "number"
        attribute "startMsg", "string"
        attribute "endMsg", "string"  
        attribute "deleteInfo", "string"  
	}

	simulator {
		status "present": "presence: present"
		status "not present": "presence: not present"
	}

	tiles(scale: 2) {
		// You only get a presence tile view when the size is 3x3 otherwise it's a value tile
		standardTile("presence", "device.presence", width: 3, height: 3, canChangeBackground: true, inactiveLabel: false, canChangeIcon: true) {
			state("present", label:'${name}', icon:"st.presence.tile.mobile-present", action:"departed", backgroundColor:"#53a7c0")
			state("not present", label:'${name}', icon:"st.presence.tile.mobile-not-present", action:"arrived", backgroundColor:"#CCCC00")
		}
        
		standardTile("notPresentBtn", "device.fake", width: 3, height: 2, decoration: "flat") {
			state("default", label:'AWAY', backgroundColor:"#CCCC00", action:"departed")
		}

		standardTile("presentBtn", "device.fake", width: 3, height: 2, decoration: "flat") {
			state("default", label:'HERE', backgroundColor:"#53a7c0", action:"arrived")
		}

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width:3, height: 3) {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
                
        valueTile("summary", "device.eventSummary", inactiveLabel: false, decoration: "flat", width: 6, height: 3) {
            state "default", label:'${currentValue}'
        }
		
        valueTile("deleteInfo", "device.deleteInfo", inactiveLabel: false, decoration: "flat", width: 6, height: 2) {
            state "default", label:'To remove this device from ST - delete the corresponding GCal Search Trigger.'
        }


		main("presence")
		details([
			"summary", "presence", "refresh", "deleteInfo" 	//"notPresentBtn", "presentBtn",
		]) 
	}

        
       
}

def installed() {
    log.trace "GCalPresenceSensor: installed()"
    sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"LAN\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${device.hub.hardwareID}\"}")
    
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "presence", value: "not present", isStateChange: true)

    initialize()
}

def updated() {
    log.trace "GCalPresenceSensor: updated()"
	initialize()
}

def initialize() {
	log.trace "GCalPresenceSensor: initialize()"
    refresh()
}

def parse(String description) {

}

def arrived() {
	log.trace "arrived():"
	present()
}

def present() {
	log.trace "present()"
    sendEvent(name: "switch", value: "on")
    sendEvent(name: 'presence', value: 'present', isStateChange: true)
    
    def departTime = new Date( device.currentState("departTime").value )	
    log.debug "Scheduling Close for: ${departTime}"
    sendEvent("name":"departTime", "value":departTime)
    parent.scheduleEvent("depart", departTime, [overwrite: true])
    
    //AskAlexaMsg    
	def askAlexaMsg = device.currentValue("startMsg")	
	parent.askAlexaStartMsgQueue(askAlexaMsg)     
}


// refresh status
def refresh() {
	log.trace "refresh()"
    
    parent.refresh() // reschedule poll
    poll() // and do one now
    
}

def departed() {
	log.trace "departed():"
	away()
}

def away() {
	log.trace "away():"
    
	sendEvent(name: "switch", value: "off")
    sendEvent(name: 'presence', value: 'not present', isStateChange: true)   
    
     //AskAlexaMsg
	def askAlexaMsg = device.currentValue("endMsg")	
	parent.askAlexaEndMsgQueue(askAlexaMsg)   
   	
}

def poll() {
    log.trace "poll()"
    def items = parent.getNextEvents()
    try {
    
	    def currentState = device.currentValue("presence") ?: "not present"
    	def isPresent = currentState == "present"
	    log.debug "isPresent is currently: ${isPresent}"
    
        // START EVENT FOUND **********
    	if (items && items.items && items.items.size() > 0) {        
	        //	Only process the next scheduled event 
    	    def event = items.items[0]
        	def title = event.summary
            
            def calName = "GCal Primary"
            if ( event.organizer.displayName ) {
            	calName = event.organizer.displayName
           	}
            
        	log.debug "We Haz Eventz! ${event}"

	        def start
    	    def end
            def type = "E"
            
        	if (event.start.containsKey('date')) {
        	//	this is for all-day events            	
				type = "All-day e"   				             
    	        def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd")
        	    sdf.setTimeZone(TimeZone.getTimeZone(items.timeZone))
            	start = sdf.parse(event.start.date)
    	        end = new Date(sdf.parse(event.end.date).time - 60)
	        } else {
			//	this is for timed events            	            
        	    def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
            	sdf.setTimeZone(TimeZone.getTimeZone(items.timeZone))
	            start = sdf.parse(event.start.dateTime)
        	    end = sdf.parse(event.end.dateTime)
	        }            		            
        
	        def eventSummary = "Event: ${title}\n\n"
			eventSummary += "Calendar: ${state.calName}\n\n"            
    	    def startHuman = start.format("EEE, hh:mm a", location.timeZone)
        	eventSummary += "Arrives: ${startHuman}\n"
	        def endHuman = end.format("EEE, hh:mm a", location.timeZone)
    	    eventSummary += "Departs: ${endHuman}\n\n"
        	
            def startMsg = "${title} arrived at: " + startHuman
            def endMsg = "${title} departed at: " + endHuman
        	    	    

            if (event.description) {
	            eventSummary += event.description ? event.description : ""
    		}    	
            	       
			sendEvent("name":"eventSummary", "value":eventSummary, isStateChange: true)   
            
			//Set the closeTime and endMeg before opening an event in progress
	        //Then use in the open() call for scheduling close and askAlexaMsgQueue

            sendEvent("name":"departTime", "value":end)           
        	sendEvent("name":"endMsg", "value":endMsg)
                    
			sendEvent("name":"arriveTime", "value":start)           
			sendEvent("name":"startMsg", "value":startMsg)

      		// ALREADY IN EVENT?	        	                   
	           // YES
        	if ( start <= new Date() ) {
        		log.debug "Already in event ${title}."
	        	if (!isPresent) {                     
            		log.debug "Not Present, so arriving."                    
                    open()                     
                }   
            	
				// NO                
	        } else {
            	log.debug "Event ${title} still in future."
                
                if (isPresent) { 
	                log.debug "Presence incorrect, so departing."	                
    	            departed()                                         
              	}
                    
                log.debug "SCHEDULING ARRIVAL: parent.scheduleEvent(arrive, ${start}, '[overwrite: true]' )."
        		parent.scheduleEvent("arrive", start, [overwrite: true])
                
			}           
        // END EVENT FOUND *******


        // START NO EVENT FOUND ******
    	} else {
        	log.trace "No events - set all atributes to null."
	    	
            sendEvent("name":"eventSummary", "value":"No events found", isStateChange: true)
            
	    	if (isPresent) { 
            	   
                log.debug "Presence incorrect, so departing."
                departed()                                          
            } else { 
				parent.unscheduleEvent("open")   
    	    } 
    	}
        // END NO EVENT FOUND
        
    } catch (e) {
    	log.warn "Failed to do poll: ${e}"
    }
}

def version() {
	def text = "20170422.1"
}