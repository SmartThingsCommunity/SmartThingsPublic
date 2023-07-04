/**
 *  Tvon
 *
 *  Copyright 2019 Александр Варавва
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "Tvon", namespace: "Tvon", author: "Александр Варавва", cstHandler: true) {
		capability "Stateless Power Button"
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
	// TODO: handle 'availablePowerButtons' attribute

}

// handle commands
def setButton() {
	log.debug "Executing 'setButton'"
	// TODO: handle 'setButton' command
}