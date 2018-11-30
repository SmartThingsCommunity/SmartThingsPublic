/**
 *  Tesla Connect
 *
 *  Copyright 2018 Trent Foley
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
    name: "Tesla Connect",
    namespace: "trentfoley",
    author: "Trent Foley",
    description: "Integrate your Tesla car with SmartThings.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/tesla-app%402x.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/tesla-app%403x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Partner/tesla-app%403x.png",
    singleInstance: true
)

preferences {
	page(name: "loginToTesla", title: "Tesla")
	page(name: "selectVehicles", title: "Tesla")
}

def loginToTesla() {
	def showUninstall = email != null && password != null
	return dynamicPage(name: "loginToTesla", title: "Connect your Tesla", nextPage:"selectVehicles", uninstall:showUninstall) {
		section("Log in to your Tesla account:") {
			input "email", "text", title: "Email", required: true, autoCorrect:false
			input "password", "password", title: "Password", required: true, autoCorrect:false
		}
		section("To use Tesla, SmartThings encrypts and securely stores your Tesla credentials.") {}
	}
}

def selectVehicles() {
	try {
		refreshAccountVehicles()

		return dynamicPage(name: "selectVehicles", title: "Tesla", install:true, uninstall:true) {
			section("Select which Tesla to connect"){
				input(name: "selectedVehicles", type: "enum", required:false, multiple:true, options:state.accountVehicles)
			}
		}
	} catch (Exception e) {
    	log.error e
        return dynamicPage(name: "selectVehicles", title: "Tesla", install:false, uninstall:true, nextPage:"") {
			section("") {
				paragraph "Please check your username and password"
			}
		}
	}
}

def getChildNamespace() { "trentfoley" }
def getChildName() { "Tesla" }
def getServerUrl() { "https://owner-api.teslamotors.com" }
def getClientId () { "81527cff06843c8634fdc09e8ac0abefb46ac849f38fe1e431c2ef2106796384" }
def getClientSecret () { "c7257eb71a564034f9419ee651c7d0e5f7aa6bfbd18bafb5c5c033b093bb2fa3" }
def getUserAgent() { "trentacular" }

def getAccessToken() {
	if (!state.accessToken) {
		refreshAccessToken()
	}
	state.accessToken
}

private convertEpochSecondsToDate(epoch) {
	return new Date(epoch * 1000);
}

def refreshAccessToken() {
	log.debug "refreshAccessToken"
	try {
        if (state.refreshToken) {
        	log.debug "Found refresh token so attempting an oAuth refresh"
            try {
                httpPostJson([
                    uri: serverUrl,
                    path: "/oauth/token",
                    headers: [ 'User-Agent': userAgent ],
                    body: [
                        grant_type: "refresh_token",
                        client_id: clientId,
                        client_secret: clientSecret,
                        refresh_token: state.refreshToken
                    ]
                ]) { resp ->
                    state.accessToken = resp.data.access_token
                    state.refreshToken = resp.data.refresh_token
                }
            } catch (groovyx.net.http.HttpResponseException e) {
            	log.warn e
                state.accessToken = null
                if (e.response?.data?.status?.code == 14) {
                    state.refreshToken = null
                }
            }
        }

        if (!state.accessToken) {
        	log.debug "Attemtping to get access token using password" 
            httpPostJson([
                uri: serverUrl,
                path: "/oauth/token",
                headers: [ 'User-Agent': userAgent ],
                body: [
                    grant_type: "password",
                    client_id: clientId,
                    client_secret: clientSecret,
                    email: email,
                    password: password
                ]
            ]) { resp ->
            	log.debug "Received access token that will expire on ${convertEpochSecondsToDate(resp.data.created_at + resp.data.expires_in)}"
                state.accessToken = resp.data.access_token
                state.refreshToken = resp.data.refresh_token
            }
        }
    } catch (Exception e) {
        log.error "Unhandled exception in refreshAccessToken: $e"
    }
}

private authorizedHttpRequest(path, method, Closure closure, attempt = 0) {
    log.debug "authorizedHttpRequest ${path} attempt ${attempt}"
    try {
    	def requestParameters = [
            uri: serverUrl,
            path: path,
            headers: [
                'User-Agent': userAgent,
                Authorization: "Bearer ${accessToken}"
            ]
        ]
    
    	if (method == "GET") {
            httpGet(requestParameters) { resp -> closure(resp) }
        } else if (method == "POST") {
        	httpPost(requestParameters) { resp -> closure(resp) }
        } else {
        	log.error "Invalid method ${method}"
        }
    } catch (groovyx.net.http.HttpResponseException e) {
        if (e.response?.data?.status?.code == 14) {
        	if (attempt < 3) {
                refreshAccessToken()
                authorizedHttpRequest(path, mehod, closure, attempt++)
            } else {
            	log.error "Failed after 3 attempts to perform request: ${path}"
            }
        }
    }
}

private refreshAccountVehicles() {
	log.debug "refreshAccountVehicles"

	state.accountVehicles = [:]

	authorizedHttpRequest("/api/1/vehicles", "GET", { resp ->
    	log.debug "Found ${resp.data.response.size()} vehicles"
        resp.data.response.each { vehicle ->
        	log.debug "${vehicle.id}: ${vehicle.display_name}"
        	state.accountVehicles[vehicle.id] = vehicle.display_name
        }
    })
}


def installed() {
	log.debug "Installed"
	initialize()
}

def updated() {
	log.debug "Updated"

	unsubscribe()
	initialize()
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	log.debug "deleting ${delete.size()} vehicles"
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def initialize() {
    ensureDevicesForSelectedVehicles()
    removeNoLongerSelectedChildDevices()
}

private ensureDevicesForSelectedVehicles() {
	if (selectedVehicles) {
        selectedVehicles.each { dni ->
            def d = getChildDevice(dni)
            if(!d) {
                def vehicleName = state.accountVehicles[dni]
                device = addChildDevice("trentfoley", "Tesla", dni, null, [name:"Tesla ${dni}", label:vehicleName])
                log.debug "created device ${device.label} with id ${dni}"
                device.initialize()
            } else {
                log.debug "device for ${d.label} with id ${dni} already exists"
            }
        }
    }
}

private removeNoLongerSelectedChildDevices() {
	// Delete any that are no longer in settings
	def delete = getChildDevices().findAll { !selectedVehicles }
	removeChildDevices(delete)
}

private transformVehicleResponse(resp) {
	return [
        vehicleState: resp.data.response.state,
        motion: resp.data.response.in_service ? "active" : "inactive",
        vin: resp.data.response.vin
    ]
}

def refresh(child) {
    def data = [:]
	def id = child.device.deviceNetworkId
    authorizedHttpRequest("/api/1/vehicles/${id}", "GET", { resp ->
    	data = transformVehicleResponse(resp)
    })
    
    if (data.vehicleState == "online") {
    	authorizedHttpRequest("/api/1/vehicles/${id}/vehicle_data", "GET", { resp ->
            data["chargeState"] = [
                battery: resp.data.response.charge_state.battery_level
            ]
        })
    }
    
    return data
}

def wake(child) {
	def data = [:]
	def id = child.device.deviceNetworkId
    authorizedHttpRequest("/api/1/vehicles/${id}/wake_up", "POST", { resp ->
    	data = transformVehicleResponse(resp)
    })
    
    return data
}