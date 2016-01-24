/**
 *  Auto Lock
 *
 *  Copyright 2015 Vikash Varma
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

// Automatically generated. Make future change here.
definition(
    name: "Lock Automation",
 	namespace: "vvarma",
    author: "Vikash Varma",
    description: "Manage authorized users and send notification when lock is unlocked. Automatically lock it based on elapsed time or when no motion detected near the lock",
    category: "Safety & Security",
    iconUrl: "http://ecx.images-amazon.com/images/I/51lIeDU229L._SL1500_.jpg",
    iconX2Url: "http://ecx.images-amazon.com/images/I/51lIeDU229L._SL1500_.jpg",
)
import groovy.json.JsonSlurper

preferences {
	page (name: "mainPage", title: "Automatically locks a deadbolt or lever lock using motion sensor or time elapsed", nextPage: "page2", uninstall: true) {
    	section {
			input "lockDevice", "capability.lock", title:"Which Lock?"
        	input "lockOpt", "enum", title: "Preference", metadata: [values: ["Motion", "Time"]], multiple:false  
            input "maxusers", "number", title: "Maximum authorized users", required:true
            input "pushNotify", "boolean" , title:"Send push notification when unlocked", default: true
		}
    }
     
	page (name: "page2", nextPage: "page3",  title: "Automatically locks a deadbolt or lever lock using motion sensor or time elapsed", uninstall: true)
    page(name: "page3", title: "Name and Mode settings",  uninstall: true, install: true) {
            section([mobileOnly:true]) {
                label title: "Assign a name", required: false
                mode title: "Set for specific mode(s)", required: false
            } 
       }
}
def page2() {
    dynamicPage(name: "page2") {
    	section("Set $lockOpt Preference") {
    		if (lockOpt == "Motion" ) {  		
				input "motion1", "capability.motionSensor", title:"Lock door after no motion"			
       		} else {
				input "minutesLater", "number", title: "Lock door after how many minutes?"
        	}
        }
        
        for (int i = 1; i <= settings.maxusers; i++) {
    		section("Add User #${i}") {        	
        		input "user${i}", "string", title: "Username", required:true
        		input "code${i}", "password", title: "Code", required:true
    		}
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
    state.lockStatus = 1 // 0 = locked; 1 = unlocked
    subscribe(lockDevice, "lock.locked", lockEvent)
    if (lockOpt == 'Time') {
    	subscribe(lockDevice, "lock.unlocked", unlockEvent)
    } else {
    	subscribe(motion1, "motion", motionHandler)
    }
    
    deleteLockCode()   
    state.maxusers = 0
    for (int i=0; i< settings.maxusers; i++) {
    	runIn(i*180, setLockCode, [overwrite: false])
    }
}


def setLockCode() {
	//log.trace "setLockCode start: state.maxuser=$state.maxusers"
    if (state.maxusers < settings.maxusers ) {
    	state.maxusers = state.maxusers + 1 
		def lockCode = settings."code${state.maxusers}"
        def username = settings."user${state.maxusers}"
    	lockCode = lockCode +""
		def msg = "$lockDevice added user $state.maxusers, name = $username, code = $lockCode"
    	log.debug msg
    	lockDevice.setCode(state.maxusers, lockCode)
    } else {
    	log.debug "end scheduling,state.maxuser=$state.maxusers, settings.maxusers=$settings.maxusers"
    }
    //log.trace "setLockCode end: state.maxuser=$state.maxusers"
}

def unlockEvent(evt) {
	log.debug "Lock ${lockDevice} was: ${evt.value}"
	state.lockStatus = 1
    def delay = minutesLater * 60 
    log.debug "Locking $lockDevice.displayName in ${minutesLater} minutes"
    runIn(delay, lockDoor)
          
    def data = []
    def unlockmsg = ""
    if (evt.data != null) {
		data = new JsonSlurper().parseText(evt.data)
        if (data.usedCode <= settings.maxusers) {
        	def u = settings."user${data.usedCode}"
        	unlockmsg = "$lockDevice was unlocked by $u"
        } else {
        	unlockmsg = "$lockDevice was unlocked by unknown user"
        }
    } else {
    	unlockmsg = "$lockDevice was unlocked by app"
        
    }
    log.debug "pushNotify=$pushNotify | unlockmsg=$unlockmsg"
    if (pushNotify) {
        sendPush(unlockmsg);
    }  else {
    	log.debug "noPush"
    
    }
}

def lockEvent(evt) {
	state.lockStatus = 0
    log.debug "$lockDevice.displayName is locked"
    unschedule( lockDoor ) 
}


def motionHandler(evt) {
	if (evt.value == "inactive") {
		lockDoor(evt)
	} 
}

def lockDoor(evt) {
	if ( state.lockStatus ) {
 	    sendPush("$app.name is locking $lockDevice.displayName")
 		lockDevice.lock()
        state.lockStatus = 0
    } else {
    	log.debug "$lolockDeviceck1.displayName already locked"
    }
}

def deleteLockCode() {
	log.debug "in deleteLockCode"
	for (int i = settings.maxusers + 1 ; i <= state.maxusers; i++)  {
    	lockDevice.deleteCode(i)
        log.debug "Deleting code $i"
    }
}

def checkCodeSetup() {

}
