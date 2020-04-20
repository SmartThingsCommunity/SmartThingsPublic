/**
 *  ts7670
 *
 *  Copyright 2017 anıl emre durak
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
	definition (name: "ts7670", namespace: "mavi proje", author: "anıl emre durak") {
		capability "Acceleration Sensor"
		capability "Dust Sensor"
		capability "Filter State"
		capability "Filter Status"

		attribute "33K series", "string"

		fingerprint endpointId: "33088", profileId: "Devices", deviceId: "Ts7670", deviceVersion: "2.0", inClusters: "1", outClusters: "1", noneClusters: "0"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'acceleration' attribute
	// TODO: handle 'dustLevel' attribute
	// TODO: handle 'fineDustLevel' attribute
	// TODO: handle 'filterLifeRemaining' attribute
	// TODO: handle 'filterStatus' attribute
	// TODO: handle '33K series' attribute

}