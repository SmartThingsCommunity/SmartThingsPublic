/**
 *  Carbon Monoxide Detector
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
	definition (name: "Carbon Monoxide Detector", namespace: "capabilities", author: "Amol Mundayoor") {
		capability "Carbon Monoxide Detector"
	}

	simulator {
		// TODO: define status and reply messages here
        status 'detected':'carbonMonoxide:detected'
        status 'undetected':'carbonMonoxide:undetected'
        
        reply 'detected':'carbonMonoxide:detected'
        reply 'undetected':'carbonMonoxide:undetected'
	}

	tiles {
		standardTile("mainTile","device.carbonMonoxide",width:2,height:2) {
        	state "detected", backgroundColor:"#F90101"
            state "undetected", backgroundColor:"#00933B"
        }
        main "mainTile"
        details "mainTile"
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'carbonMonoxide' attribute
    def pair = description.split(":")
	createEvent(name:"carbonMonoxide",value:pair[1])
}

/**def detected() {
	'detected'
}

def undetected() {
	'undetected'
}**/