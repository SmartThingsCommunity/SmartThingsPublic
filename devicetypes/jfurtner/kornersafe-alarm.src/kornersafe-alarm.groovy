/**
 *  KornerSafe
 *
 *  Copyright 2016 Jamie Furtner
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
metadata {
	definition (name: "KornerSafe Alarm", namespace: "jfurtner", author: "Jamie Furtner") {
		capability "Contact Sensor"
		capability "Polling"
		capability "Relay Switch"
	}

	attribute 'username', 'string'
    attribute 'password', 'string'
    
	simulator {		
	}

	tiles(scale:2) {
    	standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
        	state 'off', label: 'Disarmed', action: 'on', icon: 'st.locks.lock.unlocked', backgroundColor: '#44ff44'
            state 'on', label: 'Armed', action: 'off', icon: 'st.locks.lock.locked', backgroundColor: '#ff4444'
        }
        
        valueTile('motion', 'device.motion', width:1, height:1) {
        	state 'val', label: '${currentValue}'
        }
        
        standardTile('refresh', 'device.refresh', width:1, height:1) {
        	state 'refresh', label: 'Refresh', action: 'pooling.poll', icon: 'st.secondary.refresh'
        }
	}
}

// parse events into attributes
def parse(String description) {
	// nothing to do here
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	// TODO: handle 'poll' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def refresh() {
	log.debug "Executing refresh"
}

def login() {
	def params = [
    	//uri: 'https://myhome.kornersafe.com/#/account/login',
        //body: 
    ]
}