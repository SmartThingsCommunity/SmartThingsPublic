/**
 *  powwow
 *
 *  Copyright 2014 Jeff's Account
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
    name: "powwow（Connect)",
    namespace: "smartthings",
    author: "SmartThings",
    description: "powwow Service Manager",
	category: "SmartThings Labs",
    iconUrl: "https://smartdotdot.com/img/icon_1x.png",
    iconX2Url: "https://smartdotdot.com/img/icon_2x.png",
    oauth: [displayName: "powwow", displayLink: "powwow"],
    singleInstance: true
) {
	appSetting "clientId"
	appSetting "clientSecret"
}

preferences {
	page(name: "Credentials", title: "powwow Authentication", content: "authPage", nextPage: "auth", install: true)
    page(name: "auth", title: "auth", nextPage: null, content: "authPage", install: true)
   // page(name: "finnish", title: "connected",nextPage:null, content: "connected", install: true)
   // page(name: "listUsersPage", title: "Select Life360 Users", content: "listUsers", install: true)
}



mappings {

	path("/placecallback") {
		action: [
              POST: "placeEventHandler",
              GET: "placeEventHandler"
		]
	}
    
    path("/receiveToken") {
		action: [
            POST: "receiveToken",
            GET: "receiveToken"
		]
	}
}

def authPage(){
    log.debug "authPage()"
    
    def description = "powwow Credentials Already Entered."
    
    def uninstallOption = false
    if (app.installationState == "COMPLETE")
       uninstallOption = true

	if(!state.powwowAccessToken)
    {
	    log.debug "about to create access token"
		createAccessToken()
        description = "Click to enter powwow Credentials."

		def redirectUrl = oauthInitUrl()
    
		return dynamicPage(name: "Credentials", title: "powwow", uninstall: uninstallOption, install:false) {
		    section {
    			href url:redirectUrl, style:"embedded", required:false, title:"powwow", description:description
		    }
   	 	}
    }
    else{
		return dynamicPage(name: "auth", title: "Import your powwow devices", uninstall: true) {
				section("") {
                    paragraph "The device that has been imported into your account. If you want to add or delete the device, go to powwow Smart Home app for operation"
            }
		}
    }
}

def receiveToken() {
log.debug "receiveToken()"
//state.life360AccessToken = params.access_token
	def result = null
    try {
        httpPost(uri: "https://smartdotdot.com/alexa/getToken?grant_type=authorization_code&code="+ params.code) {response -> 
             result = response
        }
        if (result.data.access_token) {
            state.powwowAccessToken = result.data.access_token //  "1ac97bb8-4579-417b-b77f-d1790d0ed073"//
        }
    }
    catch (e) {
    	//render contentType: 'text/html', data: "powwow initializeLife360Connection, error: "
        log.error "powwow initializeLife360Connection, error: $e"
    }
    def html = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=640">
<title>Withings Connection</title>
<style type="text/css">
	@font-face {
		font-family: 'Swiss 721 W01 Thin';
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot');
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot?#iefix') format('embedded-opentype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.woff') format('woff'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.ttf') format('truetype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.svg#swis721_th_btthin') format('svg');
		font-weight: normal;
		font-style: normal;
	}
	@font-face {
		font-family: 'Swiss 721 W01 Light';
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot');
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot?#iefix') format('embedded-opentype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.woff') format('woff'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.ttf') format('truetype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.svg#swis721_lt_btlight') format('svg');
		font-weight: normal;
		font-style: normal;
	}
	.container {
		width: 560px;
		padding: 40px;
		/*background: #eee;*/
		text-align: center;
	}
	img {
		vertical-align: middle;
	}
	img:nth-child(2) {
		margin: 0 30px;
	}
	p {
		font-size: 2.2em;
		font-family: 'Swiss 721 W01 Thin';
		text-align: center;
		color: #666666;
		padding: 0 40px;
		margin-bottom: 0;
	}
/*
	p:last-child {
		margin-top: 0px;
	}
*/
	span {
		font-family: 'Swiss 721 W01 Light';
	}
</style>
</head>
<body>
	<div class="container">
		<img src="https://smartdotdot.com/img/icon_200px.png" alt="powwow icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
		<p>Your powwow Account is now connected to SmartThings!</p></div>
</body>
</html>
"""
	render contentType: 'text/html', data: html
}
def oauthInitUrl(){
    log.debug "oauthInitUrl"
    def stcid = getSmartThingsClientId();
    
	// def oauth_url = "https://api.life360.com/v3/oauth2/authorize?client_id=pREqugabRetre4EstetherufrePumamExucrEHuc&response_type=token&redirect_uri=http%3A%2F%2Fwww.smartthings.com"

 	state.oauthInitState = UUID.randomUUID().toString()
    
 	def oauthParams = [
    	response_type: "token", 
        client_id: stcid,  
        redirect_uri: buildRedirectUrl() 
    ]

	return "https://smartdotdot.com/alexa/authorize?" + toQueryString(oauthParams)
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def getSmartThingsClientId() {
   return "pREqugabRetre4EstetherufrePumamExucrEHuc"
}

def getServerUrl() { getApiServerUrl() }

//control device
def Object deviceSwitch(para,sw,pct1,pct2){
	    def url = "https://smartdotdot.com/smartthingCtr/control?token="+state.powwowAccessToken+"&data="+para+"&ID="+para.split("_")[1]+"&sw="+sw+"&pct1="+pct1+"&pct2="+pct2
    	def result = null
		httpGet(uri: url, headers: ["Authorization": "Bearer ${state.powwowAccessToken}" ]) {response -> 
    	 	result = response
		}
        return result
}
def String deviceSwitch(para,sw){
	    def url = "https://smartdotdot.com/smartthingCtr/control?token="+state.powwowAccessToken+"&data="+URLEncoder.encode(para)+"&sw="+sw+"&pct1="+0+"&pct2="+0
    	def result = null
		httpGet(uri: url, headers: ["Authorization": "Bearer ${state.powwowAccessToken}" ]) {response -> 
    	 	result = response
		}
        return result.data.data
}


//Query Device
def Object QueryDevice(para){
 	def url = "https://smartdotdot.com/smartthingCtr/QueryDevice?token="+state.powwowAccessToken+"&datas="+URLEncoder.encode(para)
    def result = null
	httpGet(uri: url, headers: ["Authorization": "Bearer ${state.powwowAccessToken}" ]){response -> 
    	result = response
	}
    return result
}



def buildRedirectUrl(){
    log.debug "buildRedirectUrl"
    // /api/token/:st_token/smartapps/installations/:id/something
    
	return serverUrl + "/api/token/${state.accessToken}/smartapps/installations/${app.id}/receiveToken"
}




//
// This method is no longer used - was part of the initial username/password based authentication that has now been replaced
// by the full OAUTH web flow
//

def installed() {
 		log.debug "installed()+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
        //def url = "https://smartdotdot.com/ledScene/getSceneSubsetList?installed=1&token="+state.powwowAccessToken
        def url = "https://smartdotdot.com/smartthingCtr/getSceneSubsetList?installed=1&token="+state.powwowAccessToken
       
    	def result = null
       httpGet(uri: url, headers: ["Authorization": "Bearer ${state.powwowAccessToken}" ]) {response -> 
    	 	result = response
		}
    		def plugtemp = result.data.data
            plugtemp.each {plug->
         
			log.debug "plug name："+plug.name+"="+plug.data+"="+plug.type
        	//deleteChildDevice(childDevice.deviceNetworkId)        onLineState     /1:排插 2，组 3，单座，4wifi组，5wifi单座。
            def childDevice= getChildDevice(plug.type+"_"+plug.data)
            log.debug "childDevice：before======"+childDevice

            if(!childDevice){
           	 log.debug "+++++++++++++++++"+qw
                if(plug.type==1) {
                	childDevice = addChildDevice("powwow", "powwow WI-FI Samrt Surge Protector", plug.type+"_"+plug.data,null,[name:plug.name,completedSetup: true])
                   }
                if(plug.type==4) {
					childDevice = addChildDevice("powwow", "powwow WI-FI Smart LED Bulb", plug.type+"_"+plug.data,null,[name:plug.name,completedSetup: true])
                  }
                if(plug.type==5) {
					childDevice = addChildDevice("powwow", "powwow WI-FI mini Smart Plug", plug.type+"_"+plug.data,null,[name:plug.name,completedSetup: true])
                  }
            }
        	if (childDevice)
        	{
        		// log.debug "Child Device Successfully Created"
 			//	generateInitialEvent (member, childDevice)
			
            	// build the icon name form the L360 Avatar URL
                // URL Format: https://www.life360.com/img/user_images/b4698717-1f2e-4b7a-b0d4-98ccfb4e9730/Maddie_Hagins_51d2eea2019c7.jpeg
                // SmartThings Icon format is: L360.b4698717-1f2e-4b7a-b0d4-98ccfb4e9730.Maddie_Hagins_51d2eea2019c7
                          // build the icon name from the avatar URL
                	//log.debug "Avatar URL = ${member.avatar}"

                    // def icon = "st.Lighting.light1"
               		def icon=serverUrl+plug.url
                    log.debug "Icon = ${icon}"
					//////////////////////////////////////////////////childDevice?.sendEvent(name:"acceleration",value:"inactive",unit:"")
            		// set the icon on the device
					childDevice.setIcon("presence","present",icon)
					childDevice.setIcon("presence","not present",icon)
					childDevice.save()
                                        // build the icon name from the avatar URL
                                        //log.debug "Avatar URL = ${member.avatar}"
                    //                    def urlPathElements = member.avatar.tokenize("/")
                    //                    def fileElements = urlPathElements[5].tokenize(".")
                                        // def icon = "st.Lighting.light1"
                    //                    def icon="l360.${urlPathElements[4]}.${fileElements[0]}"
                   //                     log.debug "Icon = ${icon}"
                                        // set the icon on the device
                    //                    childDevice.setIcon("presence","present",icon)
                    //                    childDevice.setIcon("presence","not present",icon)
					//					  childDevice.save()
			}
		
		}
}



def getPiggyBankUpdate(childDevice)
{
	apiGet("/piggy_banks/" + childDevice.device.deviceNetworkId) { response ->
		def status = response.data.data
		def alertData = status.triggers

		if (( alertData.enabled ) && ( state.lastCheckTime )) {
			if ( alertData.triggered_at[0].toInteger() > state.lastCheckTime ) {
				childDevice?.sendEvent(name:"acceleration",value:"active",unit:"")
			} else {
				childDevice?.sendEvent(name:"acceleration",value:"inactive",unit:"")
			}
		}

		childDevice?.sendEvent(name:"goal",value:dollarize(status.savings_goal),unit:"")

		childDevice?.sendEvent(name:"balance",value:dollarize(status.balance),unit:"")

		def now = new Date()
		def longTime = now.getTime()/1000
		state.lastCheckTime = longTime.toInteger()
	}
}



def updated() {
log.debug "updated()+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
        def url = "https://smartdotdot.com/smartthingCtr/getSceneSubsetList?updated=1&token="+ state.powwowAccessToken
    	def result = null
       
		httpGet(uri: url, headers: ["Authorization": "Bearer ${state.powwowAccessToken}" ]) {response -> 
    	 	result = response
		}
    		def plugtemp = result.data.data
            plugtemp.each {plug->
         
			log.debug "plug name："+plug.name+"="+plug.data+"="+plug.type
        	//deleteChildDevice(childDevice.deviceNetworkId)        onLineState
            def childDevice= getChildDevice(plug.type+"_"+plug.data)
            log.debug "childDevice：before======"+childDevice

            if(!childDevice){
           	 log.debug "+++++++++++++++++"+qw
                if(plug.type==1) {
                	childDevice = addChildDevice("powwow", "powwow WI-FI Samrt Surge Protector", plug.type+"_"+plug.data,null,[name:plug.name,completedSetup: true])
                   }
                if(plug.type==3) {
					childDevice = addChildDevice("powwow", "powwow WI-FI Smart LED Bulb", plug.type+"_"+plug.data,null,[name:plug.name,completedSetup: true])
                  }
                if(plug.type==4) {
					childDevice = addChildDevice("powwow", "powwow WI-FI mini Smart Plug", plug.type+"_"+plug.data,null,[name:plug.name,completedSetup: true])
                  }
            }
           	//	DeviceWrapper addChildDevice(String namespace, String typeName, String deviceNetworkId, hubId, Map properties)
            //	childDevice = addChildDevice("powwow", "powwow mini smart plug", plug.type+"_"+plug.data,null,[name:plug.name,completedSetup: true])
            log.debug "childDevice： after======"+childDevice+"/r"
            //def childDevice = addChildDevice("smartthings", "powwow smart plug", "${app.id}.${plug.id}",null,[name:member.firstName, completedSetup: true])
        	// save the memberId on the device itself so we can find easily later
        	// childDevice.setMemberId(member.id)
        	if (childDevice)
        	{
                    // def icon = "st.Lighting.light1"
               		def icon="http://smartdotdot.com"+plug.url
                    log.debug "Icon = ${icon}"

            		// set the icon on the device
					childDevice.setIcon("presence","present",icon)
					childDevice.setIcon("presence","not present",icon)
					childDevice.save()

			}
		
		}
}