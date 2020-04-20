/**
 *  Mi Connector (v.0.0.1)
 *
 * MIT License
 *
 * Copyright (c) 2018 fison67@nate.com
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
 
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field



definition(
    name: "Mi Connector",
    namespace: "fison67",
    author: "fison67",
    description: "A Connector between Xiaomi and ST",
    category: "My Apps",
    iconUrl: "https://github.com/fison67/mi_connector/raw/master/icon.png",
    iconX2Url: "https://github.com/fison67/mi_connector/raw/master/icon.png",
    iconX3Url: "https://github.com/fison67/mi_connector/raw/master/icon.png",
    oauth: true
)

preferences {
   page(name: "mainPage")
   page(name: "monitorPage")
   page(name: "langPage")
}


def mainPage() {
	def languageList = ["English", "Korean"]
    dynamicPage(name: "mainPage", title: "Home Assistant Manage", nextPage: null, uninstall: true, install: true) {
   		section("Request New Devices"){
        	input "address", "string", title: "Server address", required: true
            input(name: "selectedLang", title:"Select a language" , type: "enum", required: true, options: languageList, defaultValue: "English", description:"Language for DTH")
        	href url:"http://${settings.address}", style:"embedded", required:false, title:"Management", description:"This makes you easy to setup"
        }
        
       	section() {
            paragraph "View this SmartApp's configuration to use it in other places."
            href url:"${apiServerUrl("/api/smartapps/installations/${app.id}/config?access_token=${state.accessToken}")}", style:"embedded", required:false, title:"Config", description:"Tap, select, copy, then click \"Done\""
       	}
    }
}

def langPage(){
	dynamicPage(name: "langPage", title:"Select a Language") {
    	section ("Select") {
        	input "Korean",  title: "Korean", multiple: false, required: false
        }
    }
}


def installed() {
    log.debug "Installed with settings: ${settings}"
    
    if (!state.accessToken) {
        createAccessToken()
    }
    
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    // Unsubscribe from all events
    unsubscribe()
    // Subscribe to stuff
    initialize()
}

def updateLanguage(){
    log.debug "Languge >> ${settings.selectedLang}"
    def list = getChildDevices()
    list.each { child ->
        try{
        	child.setLanguage(settings.selectedLang)
        }catch(e){
        	log.error "DTH is not supported to select language"
        }
    }
}

def initialize() {
	log.debug "initialize"
    
    def options = [
     	"method": "POST",
        "path": "/settings/smartthings",
        "headers": [
        	"HOST": settings.address,
            "Content-Type": "application/json"
        ],
        "body":[
            "app_url":"${apiServerUrl}/api/smartapps/installations/",
            "app_id":app.id,
            "access_token":state.accessToken
        ]
    ]
    log.debug options
    def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: null])
    sendHubCommand(myhubAction)
    
    updateLanguage()
}

def dataCallback(physicalgraph.device.HubResponse hubResponse) {
    def msg, json, status
    try {
        msg = parseLanMessage(hubResponse.description)
        status = msg.status
        json = msg.json
        log.debug "${json}"
        state.latestHttpResponse = status
    } catch (e) {
        logger('warn', "Exception caught while parsing data: "+e);
    }
}

def getDataList(){
    def options = [
     	"method": "GET",
        "path": "/requestDevice",
        "headers": [
        	"HOST": settings.address,
            "Content-Type": "application/json"
        ]
    ]
    def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: dataCallback])
    sendHubCommand(myhubAction)
}

def addDevice(){
	def id = params.id
    def type = params.type
    def data = params.data
    if(data){
    	data = new JsonSlurper().parseText(data)
    }
    
    log.debug("Try >> ADD Xiaomi Device id=${id}, type=${type}")
    log.debug("Data >> ${data}")
	
    def dni = "mi-connector-" + id.toLowerCase()
    
    def chlid = getChildDevice(dni)
    if(!child){
        def dth = null
        def name = null

        if(params.type == "zhimi.airpurifier.m1" || params.type == "zhimi.airpurifier.v1" || params.type == "zhimi.airpurifier.v2" || params.type ==  "zhimi.airpurifier.v3" || params.type ==  "zhimi.airpurifier.v6" || params.type ==  "zhimi.airpurifier.ma2"){
            dth = "Xiaomi Air Purifier";
            name = "Xiaomi Air Purifier";
        }else if(params.type == "lumi.gateway.v2"){
        	dth = "Xiaomi Gateway";
            name = "Xiaomi Gateway V2";
        }else if(params.type == "lumi.gateway.v3"){
        	dth = "Xiaomi Gateway";
            name = "Xiaomi Gateway V3";
        }else if(params.type == "lumi.magnet" || params.type == "lumi.magnet.aq2"){
        	dth = "Xiaomi Door";
            name = "Xiaomi Door";
        }else if(params.type == "lumi.motion" || params.type == "lumi.motion.aq2"){
        	dth = "Xiaomi Motion";
            name = "Xiaomi Motion";
        }else if(params.type == "lumi.switch"){
        	dth = "Xiaomi Button Ori";
            name = "Xiaomi Button Ori";
        }else if(params.type == "lumi.switch.v2"){
        	dth = "Xiaomi Button AQ";
            name = "Xiaomi Button AQ";
        }else if(params.type == "lumi.86sw1"){
        	dth = "Xiaomi Button SW1";
            name = "Xiaomi Button SW1";
        }else if(params.type == "lumi.86sw2"){
        	dth = "Xiaomi Button SW";
            name = "Xiaomi Button SW";
        }else if(params.type == "lumi.cube"){
        	dth = "Xiaomi Cube";
            name = "Xiaomi Cube";
        }else if(params.type == "zhimi.humidifier.v1" || params.type == "zhimi.humidifier.ca1"){
        	dth = "Xiaomi Humidifier";
            name = "Xiaomi Humidifier";
        }else if(params.type == "zhimi.fan.v3"){	
        	dth = "Xiaomi Fan";	
            name = "Xiaomi Fan";	
        }else if(params.type == "zhimi.airmonitor.v1"){	
        	dth = "Xiaomi Air Monitor";	
            name = "Xiaomi Air Monitor";				
       	}else if(params.type == "yeelink.light.color1"){
        	dth = "Xiaomi Light";
            name = "Xiaomi Light";
        }else if(params.type == "yeelink.light.strip1"){
        	dth = "Xiaomi Light Strip";
            name = "Xiaomi Light Strip";
        }else if(params.type == "yeelink.light.lamp1" || params.type == "yeelink.light.mono1"){
        	dth = "Xiaomi Light Mono";
            name = "Xiaomi Light Mono";
        }else if(params.type == "philips.light.sread1" || params.type == "philips.light.bulb"){
        	dth = "Xiaomi Light";
            name = "Philips Light";
        }else if(params.type == "rockrobo.vacuum.v1" || params.type == "roborock.vacuum.s5"){
        	dth = "Xiaomi Vacuums";
            name = "Xiaomi Vacuums";
        }else if(params.type == "qmi.powerstrip.v1" || params.type == "zimi.powerstrip.v2"){
        	dth = "Xiaomi Power Strip";
            name = "Xiaomi Power Strip";
        }else if(params.type == "chuangmi.plug.v1" || params.type == "chuangmi.plug.v2" || params.type == "chuangmi.plug.m1" || params.type == "lumi.plug"){
        	dth = "Xiaomi Power Plug";
            name = "Xiaomi Power Plug";
        }else if(params.type == "lumi.ctrl_neutral1" || params.type == "lumi.ctrl_ln1" ){
        	dth = "Xiaomi Wall Switch";
            name = "Xiaomi Wall Switch";
        }else if(params.type == "lumi.ctrl_neutral2" || params.type == "lumi.ctrl_ln2"){
        	dth = "Xiaomi Wall Switch";
            name = "Xiaomi Wall Switch";
        }else if(params.type == "lumi.sensor_ht"){
        	dth = "Xiaomi Sensor HT";
            name = "Xiaomi Sensor HT";
        }else if(params.type == "zhimi.airmonitor.v1"){
        	dth = "Xiaomi Air Monitor";
            name = "Xiaomi Air Monitor";
        }else if(params.type == "lumi.weather"){
        	dth = "Xiaomi Weather";
            name = "Xiaomi Weather";
        }else if(params.type == "lumi.smoke"){
        	dth = "Xiaomi Smoke Detector";
            name = "Xiaomi Smoke Dectector";
	}else if(params.type == "lumi.gas"){
		dth = "Xiaomi Gas Detector";
            name = "Xiaomi Gas Dectector";
	}else if(params.type == "lumi.water"){
		dth = "Xiaomi Water Detector";
            name = "Xiaomi Water Dectector";
	}
        
        if(dth == null){
        	log.warn("Failed >> Non exist DTH!!! Type >> " + type);
            def resultString = new groovy.json.JsonOutput().toJson("result":"nonExist")
            render contentType: "application/javascript", data: resultString
        }else if(params.type == "lumi.ctrl_neutral1" || params.type == "lumi.ctrl_ln1"){
        	try{
                def childDevice = addChildDevice("fison67", dth, (dni + "-1"), location.hubs[0].id, [
                    "label": name + "1"
                ])    
                childDevice.setInfo(settings.address, id, "1")
                
                try{ childDevice.refresh() }catch(e){}
                try{ childDevice.setLanguage(settings.selectedLang) }catch(e){}
                
                log.debug "Success >> ADD Device : ${type} DNI=${dni}"
                def resultString = new groovy.json.JsonOutput().toJson("result":"ok")
                render contentType: "application/javascript", data: resultString
            }catch(e){
                log.error "Failed >> ADD Device Error : ${e}"
                def resultString = new groovy.json.JsonOutput().toJson("result":"fail")
                render contentType: "application/javascript", data: resultString
            }
        }else if(params.type == "lumi.ctrl_neutral2" || params.type == "lumi.ctrl_ln2"){
        	try{
            	def index = 1;
            	for (def i = 0; i <2; i++) {
                	def childDevice = addChildDevice("fison67", dth, (dni + "-" + index), location.hubs[0].id, [
                        "label": name + index
                    ])    
                    childDevice.setInfo(settings.address, id, index.toString())
                    
                	try{ childDevice.refresh() }catch(e){}
                	try{ childDevice.setLanguage(settings.selectedLang) }catch(e){}
                    
                    log.debug "Success >> ADD Device : ${type} DNI=${dni}"
                    index += 1
                }
                def resultString = new groovy.json.JsonOutput().toJson("result":"ok")
                render contentType: "application/javascript", data: resultString
            	
            }catch(e){
                log.error "Failed >> ADD Device Error : ${e}"
                def resultString = new groovy.json.JsonOutput().toJson("result":"fail")
                render contentType: "application/javascript", data: resultString
            }
        }else{
            try{
                def childDevice = addChildDevice("fison67", dth, dni, location.hubs[0].id, [
                    "label": name
                ])    
                childDevice.setInfo(settings.address, id)
                log.debug "Success >> ADD Device : ${type} DNI=${dni}"
         
                try{ childDevice.refresh() }catch(e){}
                try{ childDevice.setLanguage(settings.selectedLang) }catch(e){}
                
                def resultString = new groovy.json.JsonOutput().toJson("result":"ok")
                render contentType: "application/javascript", data: resultString
            }catch(e){
                console.log("Failed >> ADD Device Error : " + e);
                def resultString = new groovy.json.JsonOutput().toJson("result":"fail")
                render contentType: "application/javascript", data: resultString
            }
    	}
    }
    
}

def updateDevice(){
//	log.debug "Mi >> ${params.type} (${params.key}) >> ${params.cmd}"
    def id = params.id
    def dni = "mi-connector-" + id.toLowerCase()
    def chlid = getChildDevice(dni)
    if(chlid){
		chlid.setStatus(params)
    }
    def resultString = new groovy.json.JsonOutput().toJson("result":true)
    render contentType: "application/javascript", data: resultString
}

def deleteDevice(){
	def id = params.id
    def dni = "mi-connector-" + id.toLowerCase()
    
    log.debug "Try >> DELETE child device(${dni})"
    def result = false
    
    def chlid = getChildDevice(dni)
    if(!child){
    	try{
            deleteChildDevice(dni)
            result = true
    		log.debug "Success >> DELETE child device(${dni})"
        }catch(err){
			log.error("Failed >> DELETE child Device Error!!! ${dni} => " + err);
        }
    }
    
    def resultString = new groovy.json.JsonOutput().toJson("result":result)
    render contentType: "application/javascript", data: resultString
}

def getDeviceList(){
	def list = getChildDevices();
    def resultList = [];
    list.each { child ->
 //       log.debug "child device id $child.deviceNetworkId with label $child.label"
        def dni = child.deviceNetworkId
        resultList.push( dni.substring(13, dni.length()) );
    }
    
    def configString = new groovy.json.JsonOutput().toJson("list":resultList)
    render contentType: "application/javascript", data: configString
}

def authError() {
    [error: "Permission denied"]
}

def renderConfig() {
    def configJson = new groovy.json.JsonOutput().toJson([
        description: "Mi Connector API",
        platforms: [
            [
                platform: "SmartThings Mi Connector",
                name: "Mi Connector",
                app_url: apiServerUrl("/api/smartapps/installations/"),
                app_id: app.id,
                access_token:  state.accessToken
            ]
        ],
    ])

    def configString = new groovy.json.JsonOutput().prettyPrint(configJson)
    render contentType: "text/plain", data: configString
}

mappings {
    if (!params.access_token || (params.access_token && params.access_token != state.accessToken)) {
        path("/config")                         { action: [GET: "authError"] }
        path("/list")                         	{ action: [GET: "authError"]  }
        path("/update")                         { action: [POST: "authError"]  }
        path("/add")                         	{ action: [POST: "authError"]  }
        path("/delete")                         { action: [POST: "authError"]  }

    } else {
        path("/config")                         { action: [GET: "renderConfig"]  }
        path("/list")                         	{ action: [GET: "getDeviceList"]  }
        path("/update")                         { action: [POST: "updateDevice"]  }
        path("/add")                         	{ action: [POST: "addDevice"]  }
        path("/delete")                         { action: [POST: "deleteDevice"]  }
    }
}