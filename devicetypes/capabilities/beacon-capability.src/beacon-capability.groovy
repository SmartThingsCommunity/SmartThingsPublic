/**
 *  Beacon
 *
 *  Copyright 2015 Amol Mundayoor
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
	definition (name: "Beacon", namespace: "capabilities", author: "Amol Mundayoor") {
		capability "Beacon"
	}

	simulator {
		status "present":"beacon:present"
        status "not present":"beacon:not present"
        
        reply "present":"beacon:present"
        reply "not present":"beacon:not present"
	}

	tiles {
		standardTile("mainTile","device.beacon",width:2,height:2) {
        	state "present", backgroundColor:"#00933B"
            state "not present", backgroundColor:"#F90101"
        }
        main "mainTile"
        details "mainTile"
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	def pair = description.split(":")
    createEvent(name: pair[0].trim(), value: pair[1].trim())
}