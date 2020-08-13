/**
 *  WiConnectScene
 *
 *  Copyright 2020 Jos&eacute; Augusto Baranauskas
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
	definition (name: "WiConnectScene", namespace: "baranauskas", author: "Jose; Augusto Baranauskas", cstHandler: true) {
		capability "Execute"
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
	// TODO: handle 'data' attribute

}

// handle commands
def execute() {
	log.debug "Executing 'execute'"
      def cmdStr = new physicalgraph.device.HubAction([
    method: "POST",
    path: "/dispositivos/727645701/acionar/0",
    headers: [
    "HOST": "192.168.1.201:3000",
    "Content-Type": "application/json",
    "Authorization": "Basic dG9rZW46YTc5NTMxOWFkMzViNjQ2YzBiYjJmY2RjZDdjNjQ3MWQ="
    ]
  ])


  try {
      log.debug "vou tentar iniciar"
      log.debug "comando antes: $cmdStr" 
      def CmdResponse = sendHubCommand(cmdStr)
      log.debug "resposta: $CmdResponse"
      log.debug "fim"
  } catch (e) {
      log.debug "something went wrong: $e"
  }
}