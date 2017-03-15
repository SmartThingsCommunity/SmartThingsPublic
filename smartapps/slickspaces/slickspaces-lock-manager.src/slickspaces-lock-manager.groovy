/**
 *  Slickspaces Lock Manager
 *
 *  Copyright 2016 Mathew Hunter
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
 *  
 *
 */
definition(
    name: "Slickspaces Lock Manager",
    parent: "Slickspaces:Slickspaces v0.25",
    namespace: "Slickspaces",
    author: "Mathew Hunter",
    description: "Allows integration with Slickspaces rental management suite www.slickspaces.com",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)
    
import groovy.json.JsonSlurper

preferences {
	page(name: "SetupLockManager", Title: "Setup Lock Manager", uninstall: true, install: true){
    	section("Access Code Locks") {
			input "myLocks","capability.lockCodes", title: "Select Locks for Access Codes", required: true, multiple: true, submitOnChange: true
		}
    	section("Slickspaces Secrets shh"){
    		input (name:"slicksecret", type:"password")
    	}
    	section{
    		href(name: "toLockRoutines", page: "lockRoutinesPage", title: "Lock Action")
    	}
       	section("Unlock Action"){
    		href(name: "toUnlockRoutines", page: "unlockRoutinesPage", title: "Unlock Action")
    	}
    }
    page(name:"lockRoutinesPage", title: "Which Routine Would You Like to Run on Lock?", uninstall:false, install: false)
    page(name:"unlockRoutinesPage", title: "Which Routine Would You Like to Run on Lock?", uninstall:false, install: false)
}

def lockRoutinesPage(){
    dynamicPage(name: "lockRoutinesPage"){
    	section("lockAction"){
        	def routines = location.getHelloHome()?.getPhrases()*.label
        	log.debug "routines are ${routines}"
            if (routines){
            	routines.sort()
            	input "lockRoutine", "enum", title: "Lock Action", options: routines, multiple: false, required: false, submitOnChange: true, refreshAfterSelection: true
    		}
        }
    }
    
}

def unlockRoutinesPage(){
    dynamicPage(name: "unlockRoutinesPage", title: "Which Routine Would You Like to Run on Unlock?"){
    	section("unlockAction"){
        	def routines = location.getHelloHome()?.getPhrases()*.label
			if(routines){
            	routines.sort()
            	input "unlockRoutine", "enum", title: "Unlock Action", options: routines, multiple: false, required: false, submitOnChange: true, refreshAfterSelection: true
    		}
        }
    }
	
}


def getLockUsers(id){
	def lockID = id
    def targetLock = myLocks.find{ it.id == lockID }
    def lockCodes = []
    return [state.codeData]
}


def setLockCode(targetLock, codeID, code){
	//dates are provided in java millisecond time, we need to convert to date for groovy
	log.debug "setting lock code for ${targetLock} ${code} ${date}"
    def myLock = myLocks.find { it.id == targetLock }
    myLock.setCode(codeID,code)
    return [codeID:codeID, code:code]
}

def deleteLockCode(lockID, codeID){
    def targetLock = myLocks.find { it.id == lockID }
    targetLock.deleteCode(codeID.toInteger())
    return [codeID:codeID]
}

def deleteAllLockCodes(lockID){
    def targetLock = myLocks.find { it.id == lockID }
    1.upto(30){
    	targetLock.deleteCode(it)
    }
    return [status:'success']
}

def storeUserCodes(evt) {
	//this is where I need to modify the codes to be stored in slickspaces db send token with request etc.
	def codeData = new JsonSlurper().parseText(evt.data)
    log.info "poll triggered with ${codeData}"
    state.codeData = [codeData] 
}

def pollLocks(id) {
	log.info "polling lock for ${id}"
  	def targetLock = myLocks.find{ it.id == id }
  	targetLock.poll()
  	return [completed: "lock: ${targetLock.id}"]
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
	subscribe(myLocks, "reportAllCodes", storeUserCodes, [filterEvents:false])
    subscribe(myLocks, "lock", lockHandler, [filterEvents:false])
	// TODO: subscribe to attributes, devices, locations, etc.
}

def lockHandler(evt) {
log.debug "lock event fired"
	def codeData = new JsonSlurper().parseText(evt.data)
 log.debug "${codeData}"
 	if (codeData.usedCode){
		log.info "${codeData.usedCode}"
    	//schlagg
    	if (codeData.microDeviceTile){
        	if (codeData.usedCode.isNumber() && codeData.microDeviceTile.icon == "st.locks.lock.locked") {
    			location.helloHome?.execute(settings.lockRoutine)
    		}
    		if (codeData.usedCode.isNumber() && codeData.microDeviceTile.icon == "st.locks.lock.unlocked") {
    			location.helloHome?.execute(settings.unlockRoutine)
    		}
           
        }
       log.debug "did I break in my previous if statement?" 
        //weiser + yale
        if (!codeData.microDeviceTile){
        	log.debug "microDeviceTile was empty"
        	if(codeData.usedCode.isNumber()){
            	//unlocked
                log.debug "running home"
                location.helloHome?.execute(settings.unlockRoutine)
            }
        }
        	
    }
        //locking with keypad	
    if (codeData.lockedByKeypad == 1) {
    	log.info "lockedByKeypad"
        location.helloHome?.execute(settings.lockRoutine)
    }
    
    
    
}


// TODO: implement event handlers