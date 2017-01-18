/**
 *  Required PlantLink Connector
 *  This SmartApp forwards the raw data of the deviceType to myplantlink.com
 *  and returns it back to your device after calculating soil and plant type.
 *
 *  Copyright 2015 Oso Technologies
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
import groovy.json.JsonBuilder
import java.util.regex.Matcher
import java.util.regex.Pattern
 
definition(
    name: "PlantLink Connector",
    namespace: "Osotech",
    author: "Oso Technologies",
    description: "This SmartApp connects to myplantlink.com and forwards the device data to it so it can calculate easy to read plant status for your specific plant's needs.",
    category:  "Convenience",
    iconUrl:   "https://dashboard.myplantlink.com/images/apple-touch-icon-76x76-precomposed.png",
    iconX2Url: "https://dashboard.myplantlink.com/images/apple-touch-icon-120x120-precomposed.png",
    iconX3Url: "https://dashboard.myplantlink.com/images/apple-touch-icon-152x152-precomposed.png"
) {
    appSetting "client_id"
    appSetting "client_secret"
    appSetting "https_plantLinkServer"
}

preferences {
    page(name: "auth", title: "Step 1 of 2", nextPage:"deviceList", content:"authPage")
    page(name: "deviceList", title: "Step 2 of 2", install:true, uninstall:false){
        section {
            input "plantlinksensors", "capability.sensor", title: "Select PlantLink sensors", multiple: true
        }
    }
}

mappings {
    path("/swapToken") {
        action: [
                GET: "swapToken"
        ]
    }
}

def authPage(){
    if(!atomicState.accessToken){
        createAccessToken()
        atomicState.accessToken = state.accessToken
    }

	def redirectUrl = oauthInitUrl()
    def uninstallAllowed = false
    def oauthTokenProvided = false
    if(atomicState.authToken){
        uninstallAllowed = true
        oauthTokenProvided = true
    }

    if (!oauthTokenProvided) {
        return dynamicPage(name: "auth", title: "Step 1 of 2", nextPage:null, uninstall:uninstallAllowed) {
            section(){
                href(name:"login",
                     url:redirectUrl, 
                     style:"embedded",
                     title:"PlantLink", 
                     image:"https://dashboard.myplantlink.com/images/PLlogo.png", 
                     description:"Tap to login to myplantlink.com")
            }
        }
    }else{
        return dynamicPage(name: "auth", title: "Step 1 of 2 - Completed", nextPage:"deviceList", uninstall:uninstallAllowed) {
	        section(){
               paragraph "You are logged in to myplantlink.com, tap next to continue", image: iconUrl
 			   href(url:redirectUrl, title:"Or", description:"tap to switch accounts")
            }
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def uninstalled() {
    if (plantlinksensors){
        plantlinksensors.each{ sensor_device ->
            sensor_device.setInstallSmartApp("needSmartApp")
        }
    }
}

def initialize() {
    unsubscribe()
    if (plantlinksensors){
        plantlinksensors.each{ sensor_device ->
        	subscribe(sensor_device, "moisture_status", moistureHandler)
        	subscribe(sensor_device, "battery_status", batteryHandler)
            sensor_device.setInstallSmartApp("connectedToSmartApp")
        }
    }
}

def updatePlantNameIfNeeded(plant, expected_plant_name){
    def plant_put_params = [
		uri : appSettings.https_plantLinkServer,
        headers : ["Content-Type": "application/json", "Authorization": "Bearer ${atomicState.authToken}"],
        contentType : "application/json"
    ]
    if (plant.name != expected_plant_name) {
        log.debug "renaming plant ${plant.key} - ${expected_plant_name}"
        plant_put_params["path"] = "/api/v1/plants/${plant.key}"
        def plant_put_body_map = [
			name: expected_plant_name
        ]
        def plant_put_body_json_builder = new JsonBuilder(plant_put_body_map)
        plant_put_params["body"] = plant_put_body_json_builder.toString()
        httpPut(plant_put_params) { plant_put_response ->
            parse_api_response(plant_put_response, 'updating plant name')
        }
    }
}

def moistureHandler(event){
	log.debug "moistureHandler - ${event.value}"
    
    def expected_plant_name = "${event.displayName} (ST)"
    def device_serial = getDeviceSerialFromEvent(event)
    def device_battery = atomicState["battery${device_serial}"]
    if ( device_battery == null){
    	log.error "Missing Battery Voltage - next cycle should have it"
    }else{
    	// {"type":"link","signal":"0x00","zigbeedeviceid":"0022A3000003D75A","created":1458843686,"moisture":"0x1987"}
    	def appendedEventWithBatteryInfo = event.value.replace('}',",\"battery\":\"${device_battery}\"}")
        log.debug "payload - ${appendedEventWithBatteryInfo}"
        def measurement_post_params = [
            uri: appSettings.https_plantLinkServer,
            path: "/api/v1/smartthings/links/${device_serial}/measurements",
            headers: ["Content-Type": "application/json", "Authorization": "Bearer ${atomicState.authToken}"],
            contentType: "application/json",
            body: appendedEventWithBatteryInfo
        ]

        httpPost(measurement_post_params) { measurement_post_response ->
            if (parse_api_response(measurement_post_response, 'creating moisture measurement') && measurement_post_response.data.size() >0){
                def measurement = measurement_post_response.data[0]
                def plant =  measurement.plant

                updatePlantNameIfNeeded(plant, expected_plant_name)

                plantlinksensors.each{ sensor_device ->
                    if (sensor_device.id == event.deviceId){
                        sensor_device.setStatusIcon(plant.status)
                        if (plant.last_measurements && plant.last_measurements[0].moisture){
                            sensor_device.setPlantFuelLevel(plant.last_measurements[0].moisture * 100 as int)
                        }

                        if (plant.last_measurements && plant.last_measurements[0].battery){
                            sensor_device.setBatteryLevel(plant.last_measurements[0].battery * 100 as int)
                        }
                    }
                }
            }
        }
    }
}

def batteryHandler(event){
    def expected_plant_name = "SmartThings - ${event.displayName}"
    def device_serial = getDeviceSerialFromEvent(event)
    atomicState["battery${device_serial}"] = getDeviceBatteryFromEvent(event)
}

def getDeviceSerialFromEvent(event){
    def pattern = /.*"zigbeedeviceid"\s*:\s*"(\w+)".*/
    def match_result = (event.value =~ pattern)
    return match_result[0][1]
}

def getDeviceBatteryFromEvent(event){
    def pattern = /.*"battery"\s*:\s*"(\w+)".*/
    def match_result = (event.value =~ pattern)
    return match_result[0][1]
}

def oauthInitUrl(){
	atomicState.oauthInitState = UUID.randomUUID().toString()
    def oauthParams = [
            response_type: "code",
            client_id: appSettings.client_id,
            state: atomicState.oauthInitState,
            redirect_uri: buildRedirectUrl()
    ]
    return appSettings.https_plantLinkServer + "/oauth/oauth2/authorize?" + toQueryString(oauthParams)
}

def buildRedirectUrl(){
    return getServerUrl() + "/api/token/${atomicState.accessToken}/smartapps/installations/${app.id}/swapToken"
}

def swapToken(){
	log.debug "PlantLink Connector - OAuth Token"
    def code = params.code
    def oauthState = params.state
    def stcid = appSettings.client_id
    def postParams = [
            method: 'POST',
            uri: appSettings.https_plantLinkServer,
            path: "/api/v1/oauth-token",
            query: [grant_type:'authorization_code', code:params.code, client_id:stcid,
                    client_secret:appSettings.client_secret, redirect_uri: buildRedirectUrl()],
    ]

    def jsonMap
    httpPost(postParams) { resp ->
        jsonMap = resp.data
    }

    atomicState.refreshToken = jsonMap.refresh_token
    atomicState.authToken = jsonMap.access_token

    def html = """
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
	.container {
		padding:25px;
	}
	.flex1 {
    	width:33%;
		float:left;
		text-align: center;
	}
	p {
		font-size: 2em;
		font-family: Verdana, Geneva, sans-serif;
		text-align: center;
		color: #777;
	}
</style>
</head>
<body>
	<div class="container">
		<div class="flex1"><img src="https://dashboard.myplantlink.com/images/PLlogo.png" alt="PlantLink" height="75"/></div>
		<div class="flex1"><img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected to"  height="25" style="padding-top:25px;" /></div>
		<div class="flex1"><img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings" height="75"/></div>
		<br clear="all">
  </div>
  <div class="container">
		<p>Your PlantLink Account is now connected to SmartThings!</p>
		<p style="color:green;">Click <strong>Done</strong> at the top right to finish setup.</p>
	</div>
</body>
</html>
"""
    render contentType: 'text/html', data: html
}

private refreshAuthToken() {
	log.debug "PlantLink Connector - Refresh OAuth"
    def stcid = appSettings.client_id
    def refreshParams = [
            method: 'POST',
            uri: appSettings.https_plantLinkServer,
            path: "/api/v1/oauth-token",
            query: [grant_type:'refresh_token', code:"${atomicState.refreshToken}", client_id:stcid,
                    client_secret:appSettings.client_secret],
    ]
    try{
        def jsonMap
        httpPost(refreshParams) { resp ->
            if(resp.status == 200){
                jsonMap = resp.data
                if (resp.data) {
                    atomicState.refreshToken = resp?.data?.refresh_token
                    atomicState.authToken = resp?.data?.access_token
                    if (data?.action && data?.action != "") {
                        log.debug data.action
                        "{data.action}"()
                        data.action = ""
                    }
                }
                data.action = ""
            }else{
                log.debug "PlantLink Server - ${resp.status} : ${resp.status.code}"
            }
        }
    }
    catch(Exception e){
        log.debug "PlantLink Connector - OAuth Refresh Failed: " + e
    }
}

def parse_api_response(resp, message) {
    if (resp.status == 200) {
        return true
    } else {
        log.error "sent ${message} Json & got http status ${resp.status} - ${resp.status.code}"
        if (resp.status == 401) {
            refreshAuthToken()
            return false
        } else {
            debugEvent("Plantlink Error: ${resp.status} - ${resp.status.code}", true)
            return false
        }
    }
}

def getServerUrl() { return getApiServerUrl() }

def debugEvent(message, displayEvent) {
    def results = [
            name: "appdebug",
            descriptionText: message,
            displayed: displayEvent
    ]
    log.debug "Generating AppDebug Event: ${results}"
    sendEvent (results)
}

def toQueryString(Map m){
    return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}