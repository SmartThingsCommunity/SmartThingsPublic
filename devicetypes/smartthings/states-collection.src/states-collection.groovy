/**
 *  Copyright 2020 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition(name: "States Collection", namespace: "smartthings", author: "SmartThings") {
		// This is a no capability device type handler. Its only purpose is to store important states and settings
		// for devices which change their handler.
	}
}

private void setState(key, value) {
	state."$key" = value
}

private getState(key) {
	state."$key"
}