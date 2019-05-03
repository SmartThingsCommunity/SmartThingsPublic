/**
 *  yale smartapp
 *
 *  Copyright 2018 Mark Cockcroft
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
definition(
    name: "yale smartapp",
    namespace: "Mark-C-uk",
    author: "Mark Cockcroft",
    description: "manage yale commands",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	input("userName", "text", title: "Username", description: "Your username for Yale Home System")
	input("password", "password", title: "Password", description: "Your Password for Yale Home System")
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
	// TODO: subscribe to attributes, devices, locations, etc.
}
def refresh(YaleAlarmState) {
	def token = login(token)
	def getPanelMetaDataAndFullStatus = [
		uri: "https://www.yalehomesystem.co.uk/homeportal/api/panel/get_panel_mode",
		body: [id:settings.userName , password: settings.password],
		headers: ['Cookie' : "${token}"]
		]
	try {
    	httpPost(getPanelMetaDataAndFullStatus) {	response -> 
    		YaleAlarmState = response.data.message
			return responsedetails
		} 
	}
    catch (groovyx.net.http.HttpResponseException e) {
	logResponse(e.response)
    log.warn "apiGET exception respones - '${e.response}', '${e}'"
	}
}


def login(token) {
	def paramsLogin = [
		uri: "https://www.yalehomesystem.co.uk/homeportal/api/login/check_login/",
		body: [id:settings.userName , password: settings.password]
	]
	httpPost(paramsLogin) { responseLogin ->
			token = responseLogin.headers?.'Set-Cookie'?.split(";")?.getAt(0)
	}
    log.info "'$device' Logged in"
	return token
    
}

def logout(token) {
	def paramsLogout = [
		uri: "https://www.yalehomesystem.co.uk/homeportal/api/logout/",
		headers: ['Cookie' : "${token}"]
	]
	httpPost(paramsLogout) { responseLogout ->
	}
    log.info "'$device' Logged out"
}