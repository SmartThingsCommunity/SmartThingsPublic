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
	definition(name: "Preferences Collection", namespace: "smartthings", author: "SmartThings") {
		// This is a no capability device type handler. Its only purpose is to store preferences
		// for devices which change their handler.
	}
}

private void allocatePreference(key) {
	state."$key" = [:]
}

private void setPreferenceValue(key, value) {
	state."$key".value = value
}

private getPreferenceValue(key) {
	try {
		state."$key".value
	} catch (Exception e) {
		return null
	}
}

private void setPreferenceStatus(key, status) {
	state."$key".status = status
}

private getPreferenceStatus(key) {
	try {
		state."$key".status
	} catch (Exception e) {
		return null
	}
}