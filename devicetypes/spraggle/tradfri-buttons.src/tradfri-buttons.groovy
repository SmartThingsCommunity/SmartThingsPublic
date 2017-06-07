/**
 *  Tradfri Buttons
 *
 *  Copyright 2017 Keith Spragg
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
	definition (name: "Tradfri Buttons", namespace: "Spraggle", author: "Keith Spragg") {

		fingerprint profileId: "0104", deviceId: "0810", inClusters: "0000, 0001, 0003, 0009, 0B05, 1000", outClusters: "0003, 0004, 0005, 0006, 0008, 0019, 1000", manufacturer: "IKEA of Sweden", model: "lumi.sensor_switch", deviceJoinName: ""
        command getClusters
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("thing", "device.thing", width: 2, height: 2) {
			state(name:"default", icon: "st.unknown.thing.thing-circle", label: "Please Wait")
		}
         standardTile("refresh", "device.image", inactiveLabel: false, decoration: "flat") {
          state "refresh", action:"getClusters", icon:"st.secondary.refresh"
        }


		main "thing"
		details(["thing","refresh"])
	}

}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}
def getClusters() { 
     "zdo active 0x${device.deviceNetworkId}" 
       log.debug "Get Clusters Called";
}