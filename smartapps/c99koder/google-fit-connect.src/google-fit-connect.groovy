/**
 *  Google Fit (Connect)
 *
 *  Copyright 2016 Sam Steele
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
    name: "Google Fit (Connect)",
    namespace: "c99koder",
    author: "Sam Steele",
    description: "Connect your Google Fit account to track your steps and weight",
    category: "Health & Wellness",
    iconUrl: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health12-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health12-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health12-icn@3x.png",
    singleInstance: true) {
    appSetting "clientId"
    appSetting "clientSecret"
}

mappings {
	path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
	path("/oauth/callback") {action: [GET: "callback"]}
}

preferences {
	page(name: "authentication", title: "Google Fit", content: "mainPage", submitOnChange: true, install: true)
}

def mainPage() {
	if(!atomicState.accessToken) {
        atomicState.authToken = null
        atomicState.accessToken = createAccessToken()
    }
    
    return dynamicPage(name: "authentication", uninstall: true) {
        if (!atomicState.authToken) {
            def redirectUrl = "https://graph.api.smartthings.com/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${getApiServerUrl()}"
            
            section("Google Authentication") {
                paragraph "Tap below to log in to Google and authorize SmartThings access."
                href url:redirectUrl, style:"embedded", required:true, title:"", description:"Click to enter credentials"
            }
        } else {
            section("Options") {
            	input "useMetricUnits", "bool", title:"Use Metric Units", required: false, defaultValue: true
            	input "theStepsGoal", "number", title:"Steps Goal", required: false
            }
        }
    }
}

def oauthInitUrl() {
   atomicState.oauthInitState = UUID.randomUUID().toString()

   def oauthParams = [
      response_type: "code",
      scope: "https://www.googleapis.com/auth/fitness.activity.read https://www.googleapis.com/auth/fitness.body.read",
      client_id: getAppClientId(),
      state: atomicState.oauthInitState,
      access_type: "offline",
      redirect_uri: "https://graph.api.smartthings.com/oauth/callback"
   ]

   redirect(location: "https://accounts.google.com/o/oauth2/v2/auth?" + toQueryString(oauthParams))
}

def callback() {
	def postParams = [
		uri: "https://www.googleapis.com",
		path: "/oauth2/v4/token",
		requestContentType: "application/x-www-form-urlencoded; charset=utf-8",
		body: [
			code: params.code,
			client_secret: getAppClientSecret(),
			client_id: getAppClientId(),
			grant_type: "authorization_code",
			redirect_uri: "https://graph.api.smartthings.com/oauth/callback"
		]
	]

	def jsonMap
	try {
		httpPost(postParams) { resp ->
			log.debug "resp callback"
			log.debug resp.data
			atomicState.refreshToken = resp.data.refresh_token
            atomicState.authToken = resp.data.access_token
            atomicState.last_use = now()
			jsonMap = resp.data
		}
	} catch (e) {
		log.error "something went wrong: $e"
		return
	}

	if (atomicState.authToken) {
        def message = """
                <p>Your account is now connected to SmartThings!</p>
                <p>Click 'Done' to finish setup.</p>
        """
        displayMessageAsHtml(message)
        getChildDevice(atomicState.childDeviceID)?.poll()
	} else {
        def message = """
            <p>There was an error connecting your account with SmartThings</p>
            <p>Please try again.</p>
        """
        displayMessageAsHtml(message)
	}
}

def isTokenExpired() {
    if (atomicState.last_use == null || now() - atomicState.last_use > 3600) {
    	return refreshAuthToken()
    }
    return false
}

def displayMessageAsHtml(message) {
    def html = """
        <!DOCTYPE html>
        <html>
            <head>
            </head>
            <body>
                <div>
                    ${message}
                </div>
            </body>
        </html>
    """
    render contentType: 'text/html', data: html
}

private refreshAuthToken() {
    if(!atomicState.refreshToken) {
        log.warn "Can not refresh OAuth token since there is no refreshToken stored"
    } else {
        def stcid = getAppClientId()

        def refreshParams = [
            method: 'POST',
            uri   : "https://www.googleapis.com",
            path  : "/oauth2/v3/token",
            body : [
                refresh_token: "${atomicState.refreshToken}", 
                client_secret: getAppClientSecret(),
                grant_type: 'refresh_token', 
                client_id: getAppClientId()
            ],
        ]

        try {
            httpPost(refreshParams) { resp ->
                if(resp.data) {
                    log.debug resp.data
                    atomicState.authToken = resp?.data?.access_token
					atomicState.last_use = now()
                    
                    return true
                }
            }
        }
        catch(Exception e) {
            log.debug "caught exception refreshing auth token: " + e
        }
    }
    return false
}

def toQueryString(Map m) {
   return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def getAppClientId() { appSettings.clientId }
def getAppClientSecret() { appSettings.clientSecret }

def getSteps() {
	refreshAuthToken()
    
    Calendar date = new GregorianCalendar(location.timeZone)
    long endTime = date.getTimeInMillis()
    
	date.set(Calendar.HOUR_OF_DAY, 0)
	date.set(Calendar.MINUTE, 0)
	date.set(Calendar.SECOND, 0)
	date.set(Calendar.MILLISECOND, 0)
    long startTime = date.getTimeInMillis()

    def body = [
        aggregateBy: [[dataTypeName: "com.google.step_count.delta", dataSourceId: "derived:com.google.step_count.delta:com.google.android.gms:estimated_steps"]],
        bucketByTime: [durationMillis: endTime - startTime],
        startTimeMillis: startTime,
        endTimeMillis: endTime
    ]
   
    def path = "/fitness/v1/users/me/dataset:aggregate"
    def params = [
        uri: "https://www.googleapis.com",
        path: path,
        headers: ["Authorization": "Bearer ${atomicState.authToken}"],
        body: body
    ]

    def result = null
    try {
        httpPostJson(params) { resp ->
            result = resp.data
        }
    } catch (e) {
        log.debug "error: ${path}"
        log.debug e
        if (refreshAuthToken()) {
            return getSteps()
        } else {
            log.error "fatality"
        }
    }
    
    if(result?.bucket) {
    	for(int i = 0; i < result.bucket.size(); i++) {
        	if(result.bucket[i].dataset) {
                for(int j = 0; j < result.bucket[i].dataset.size(); j++) {
                    if(result.bucket[i].dataset[j].point) {
                        for(int k = 0; k < result.bucket[i].dataset[j].point.size(); k++) {
                            if(result.bucket[i].dataset[j].point[k].value) {
                                for(int l = 0; l < result.bucket[i].dataset[j].point[k].value.size(); l++) {
                                    if(result.bucket[i].dataset[j].point[k].value[l].intVal) {
                                    	return result.bucket[i].dataset[j].point[k].value[l].intVal;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

	log.debug "Unable to parse result: ${result}"
	return 0
}

def getWeight() {
	refreshAuthToken()
    
    Calendar date = new GregorianCalendar(location.timeZone)
    long endTime = date.getTimeInMillis()
    
	date.add(Calendar.WEEK_OF_YEAR, -6)
    long startTime = date.getTimeInMillis()

    def body = [
        aggregateBy: [[dataTypeName: "com.google.weight"]],
        bucketByTime: [durationMillis: endTime - startTime],
        startTimeMillis: startTime,
        endTimeMillis: endTime
    ]
   
    def path = "/fitness/v1/users/me/dataset:aggregate"
    def params = [
        uri: "https://www.googleapis.com",
        path: path,
        headers: ["Authorization": "Bearer ${atomicState.authToken}"],
        body: body
    ]

    def result = null
    try {
        httpPostJson(params) { resp ->
            result = resp.data
        }
    } catch (e) {
        log.debug "error: ${path}"
        log.debug e
        if (refreshAuthToken()) {
            return getSteps()
        } else {
            log.error "fatality"
        }
    }

	if(result?.bucket) {
    	def fpVals = result.bucket.dataset?.point?.value?.fpVal
        if(fpVals)
	    	return fpVals[0][0][0]?.reverse()[0]
    }

    log.debug "Unable to parse result: ${result}"
	return 0
}

def isMetric() {
	return useMetricUnits
}

def getStepsGoal() {
	return theStepsGoal
}

def setupChildDevice() {
	if(!atomicState.childDeviceID) {
    	atomicState.childDeviceID = UUID.randomUUID().toString()
    }
    
    if(!getChildDevice(atomicState.childDeviceID)) {
    	if(!addChildDevice("c99koder", "Google Fit", atomicState.childDeviceID, null, [name: "Google Fit ${atomicState.childDeviceID}", label:"Google Fit", completedSetup: true])) {
        	log.error "Failed to add child device"
        }
    }
}

def installed() {
	setupChildDevice()
}

def updated() {
	setupChildDevice()
}

def uninstalled() {
    revokeAccess()
}

def revokeAccess() {
	refreshAuthToken()
	
	if (!atomicState.authToken) {
    	return
    }
    
	try {
    	def uri = "https://accounts.google.com/o/oauth2/revoke?token=${atomicState.authToken}"
        log.debug "Revoke: ${uri}"
		httpGet(uri) { resp ->
			log.debug "resp"
			log.debug resp.data
    		revokeAccessToken()
            atomicState.accessToken = atomicState.refreshToken = atomicState.authToken = null
		}
	} catch (e) {
		log.debug "something went wrong: $e"
	}
}