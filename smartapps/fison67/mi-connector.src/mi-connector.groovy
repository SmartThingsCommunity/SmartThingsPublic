/**
 *  Mi Connector (v.0.0.36)
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
   page(name: "remoteDevicePage")
   page(name: "remoteDeviceNextPage")
   page(name: "versionPage")
}


def mainPage() {
	def languageList = ["English", "Korean"]
    dynamicPage(name: "mainPage", title: "Mi Connector", nextPage: null, uninstall: true, install: true) {
   		section("Request New Devices"){
        	input "address", "text", title: "Server address", required: true, description:"ex)192.168.0.100:30000"
            input(name: "selectedLang", title:"Select a language" , type: "enum", required: true, options: languageList, defaultValue: "English", description:"Language for DTH")
            input "externalAddress", "text", title: "External network address", required: false
        	href url:"http://${settings.address}", style:"embedded", required:false, title:"Local Management", description:"This makes you easy to setup"
        	href url:"http://${settings.externalAddress}", style:"embedded", required:false, title:"External Management", description:"This makes you easy to setup"
        }
        
        section() {
          	href "remoteDevicePage", title: "Remote Device Mapping", description:""
       	}
        
       	section() {
            paragraph "View this SmartApp's configuration to use it in other places."
            href url:"${apiServerUrl("/api/smartapps/installations/${app.id}/config?access_token=${state.accessToken}")}", style:"embedded", required:false, title:"Config", description:"Tap, select, copy, then click \"Done\""
       	}
        
        section() {
          	href "versionPage", title: "Software Version", description:""
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

def versionPage(){
	
	def options = [
     	"method": "GET",
        "path": "/settings/version",
        "headers": [
        	"HOST": settings.address,
            "Content-Type": "application/json"
        ]
    ]
    def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: versionCallBack])
    sendHubCommand(myhubAction)

	dynamicPage(name: "versionPage", title:"", refreshInterval:5) {
        section {
            paragraph "Version: " + state.dockerVersion
        }
        
    }
}

def remoteDevicePage(){
    def resultList = [];
    getChildDevices().each { child ->
        try{
        	def irDevice = child.isIRRemoteDevice()
            if(irDevice){
            	def networkID = child.deviceNetworkId
            	resultList.push(child.label + " [" + networkID.substring(13, networkID.length()) + "]")
            }
        }catch(err){
        }
    }
    
	dynamicPage(name: "remoteDevicePage", title:"", nextPage:"remoteDeviceNextPage") {
        
    	section ("Remote Device Settings") {
        	input(name: "selectedDevice", type: "enum", title: "Select a device", required: false, options: resultList ,submitOnChange: true)
        }
        
        if(selectedDevice){
        	try{
        		state.selectedDeviceNetworkID = selectedDevice.split(" \\[")[1].split("]")[0]
            }catch(err){}
            
            section {
                input(name: "selectedMonitorType", type: "enum", title: "Type", options: ["Contact", "Power Meter", "Presence"], description: null, multiple: false, required: false, submitOnChange: true)
            }
            
            if (selectedMonitorType) {
                state.selectedMonitorType = selectedMonitorType
                if(selectedMonitorType == "Contact"){
                    section {
                        input(name: "selectedContactDevice", type: "capability.contactSensor", title: "Select a device", required: true, submitOnChange: true)
                        input(name: "selectedContactDefault", type: "enum", options: ["open", "closed"], title: "What is the ON status?", required: true, submitOnChange: true)
                    }

                    if(selectedContactDevice){
                        state.selectedContactDevice = selectedContactDevice.deviceNetworkId
                    }
                }else if(selectedMonitorType == "Power Meter"){
                    section {
                        input(name: "selectedPowerMeterDevice", type: "capability.powerMeter", title: "Select a device", required: true, submitOnChange: true)
                        input(name: "selectedPowerMeterLevelMin", type: "decimal", title: "Minumun", required: true, submitOnChange: true)
                        input(name: "selectedPowerMeterLevelMax", type: "decimal", title: "Maximum", required: true, submitOnChange: true)
                    }
                    if(selectedPowerMeterDevice){
                        state.selectedPowerMeterDevice = selectedPowerMeterDevice.deviceNetworkId
                    }
                }else if(selectedMonitorType == "Presence"){
                	section {
                        input(name: "selectedPresenceDevice", type: "capability.presenceSensor", title: "Select a device", required: true, submitOnChange: true)
                    }
                    if(selectedPresenceDevice){
                        state.selectedPresenceDevice = selectedPresenceDevice.deviceNetworkId
                    }
                }
            }
            
            
        }
    }
}

def remoteDeviceNextPage(){
	dynamicPage(name: "remoteDeviceNextPage", title:"") {
        section {
            paragraph "Complete"
        }
        
		if(selectedContactDevice && selectedContactDefault){
            def item = [:]
            item['default'] = selectedContactDefault
            addMonitorDevice(selectedContactDevice, state.selectedDeviceNetworkID, "contact", item)
        }
        if(selectedPowerMeterDevice && selectedPowerMeterLevelMax && selectedPowerMeterLevelMin){
            def item = [:]
            item['max'] = selectedPowerMeterLevelMax
            item['min'] = selectedPowerMeterLevelMin
            addMonitorDevice(selectedPowerMeterDevice, state.selectedDeviceNetworkID, "power", item)
        }
        if(selectedPresenceDevice){
        	def item = [:]
            addMonitorDevice(selectedPresenceDevice, state.selectedDeviceNetworkID, "presence", item)
        }
        
    	app.updateSetting("selectedDevice", "")
    	app.updateSetting("selectedMonitorType", "")
        app.updateSetting("selectedContactDevice", "")
   	 	app.updateSetting("selectedPowerMeterDevice", "")
   	 	app.updateSetting("selectedPowerMeterLevelMax", "")
   	 	app.updateSetting("selectedPowerMeterLevelMin", "")
   	 	app.updateSetting("selectedPresenceDevice", "")
        
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

	if(settings.address.split(":").size() != 2){
    	throw new Exception("Address must be with port number!!!.");
    }
    // Unsubscribe from all events
//    unsubscribe()
    // Subscribe to stuff
    initialize()
}

def addMonitorDevice(target, remoteDevice, attr, data){
	log.debug "IR Mapping >> " + target.deviceNetworkId + " >> Target(" + state.selectedDeviceNetworkID + ") Attr >> " + attr
	// Init
	if(state.monitorMap == null){
    	state.monitorMap = [:]
    }
    // Add
    def item = [:]
    item['id'] = target.deviceNetworkId
    item['data'] = data
    state.monitorMap[remoteDevice] = item
    
    log.debug state.monitorMap
    
    unsubscribe(target)
    subscribe(target, attr, stateChangeHandler)
}

def stateChangeHandler(event){
    def deviceNetworkID = event.getDevice().deviceNetworkId
    setStateRemoteDevice(event.name, event.value, getDeviceToNotifyList(deviceNetworkID) )
}

def setStateRemoteDevice(eventName, eventValue, list){
	log.debug "setStateRemoteDevice >> " + eventName + " [" + eventValue + "]"
	for(item in list){
        def targetRemoteDevice = getChildDevice(item.id)
        if(targetRemoteDevice){
            if(eventName == "contact"){
                targetRemoteDevice.setStatus( eventValue == "open" ? (item.data.default == "open" ? "on" : "off") : (item.data.default == "open" ? "off" : "on") )
            }else if(eventName == "power"){
            	targetRemoteDevice.setStatus( (item.data.min <= Float.parseFloat(eventValue) && Float.parseFloat(eventValue) <= item.data.max) ? "on" : "off" )
            }else if(eventName == "presence"){
            	targetRemoteDevice.setStatus( eventValue == "present" ? "on" : "off" )
            }
        }
    }
}

/**
* deviceNetworkID : Reference Device. Not Remote Device
*/
def getDeviceToNotifyList(deviceNetworkID){
	def list = []
	state.monitorMap.each{ targetNetworkID, _data -> 
        if(deviceNetworkID == _data.id){
        	def item = [:]
            item['id'] = 'mi-connector-' + targetNetworkID
            item['data'] = _data.data
            list.push(item)
        }
    }
    return list
}

def updateLanguage(){
    log.debug "Languge >> ${settings.selectedLang}"
    if(state.prvSelectedLangs != settings.selectedLang){
        def list = getChildDevices()
        list.each { child ->
            try{
                child.setLanguage(settings.selectedLang)
            }catch(e){
                log.error "DTH is not supported to select language"
            }
        }
    }
    state.prvSelectedLangs = settings.selectedLang
}

def updateExternalNetwork(){
	log.debug "External Network >> ${settings.externalAddress}"
    if(state.prvExternalAddress != settings.externalAddress){
        def list = getChildDevices()
        list.each { child ->
            try{
                child.setExternalAddress(settings.externalAddress)
            }catch(e){
                log.error "DTH is not supported to select external address"
            }
        }
    }
    state.prvExternalAddress = settings.externalAddress
}

def initialize() {
	log.debug "initialize"
    
    def options = [
     	"method": "POST",
        "path": "/settings/api/smartthings",
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
    
    def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: null])
    sendHubCommand(myhubAction)
    
    updateLanguage()
    updateExternalNetwork()
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

        if(params.type == "zhimi.airpurifier.m1" || params.type == "zhimi.airpurifier.v1" || params.type == "zhimi.airpurifier.v2" || params.type ==  "zhimi.airpurifier.v3" || params.type ==  "zhimi.airpurifier.v6" || params.type ==  "zhimi.airpurifier.v7" || params.type ==  "zhimi.airpurifier.m2" || params.type ==  "zhimi.airpurifier.ma2" || params.type ==  "zhimi.airpurifier.mc1" || params.type == "zhimi.airpurifier.sa2" || params.type == "zhimi.airpurifier.ma4"){
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
        }else if(params.type == "lumi.cube" || params.type == "lumi.cube.aq2"){
        	dth = "Xiaomi Cube";
            name = "Xiaomi Cube";
        }else if(params.type == "zhimi.humidifier.v1" || params.type == "zhimi.humidifier.ca1" || params.type == "zhimi.humidifier.cb2"){
        	dth = "Xiaomi Humidifier";
            name = "Xiaomi Humidifier";
        }else if(params.type == "shuii.humidifier.jsq001"){
        	dth = "Xiaomi Humidifier 3";
            name = "Xiaomi Humidifier 3";
       	}else if(params.type == "deerma.humidifier.mjjsq"){
        	dth = "Xiaomi Humidifier 4";
            name = "Xiaomi Humidifier 4";
       	}else if(params.type == "zhimi.fan.v1" || params.type == "zhimi.fan.v2" || params.type == "zhimi.fan.v3" || params.type == "zhimi.fan.sa1" || params.type == "zhimi.fan.za1" || params.type == "zhimi.fan.za3" || params.type == "zhimi.fan.za4" || params.type == "dmaker.fan.p5"){	
        	dth = "Xiaomi Fan";	
            name = "Xiaomi Fan";	
        }else if(params.type == "yeelink.light.color1" || params.type == "yeelink.light.color2" || params.type == "yeelink.light.bslamp1" || params.type == "yeelink.light.bslamp2"){
        	dth = "Xiaomi Light";
            name = "Xiaomi Light";
        }else if(params.type == "yeelink.light.strip1" || params.type == "yeelink.light.strip2"){
        	dth = "Xiaomi Light Strip";
            name = "Xiaomi Light Strip";
        }else if(params.type == "yeelink.light.lamp1" || params.type == "yeelink.light.mono1" || params.type == "yeelink.light.ct2"){
        	dth = "Xiaomi Light Mono";
            name = "Xiaomi Light Mono";
        }else if(params.type == "philips.light.downlight"){
        	dth = "Xiaomi Philips Downlight";
            name = "Xiaomi Philips Downlight";
        }else if(params.type == "philips.light.sread1" || params.type == "philips.light.bulb"){
        	dth = "Xiaomi Light";
            name = "Philips Light";
        }else if(params.type == "philips.light.moonlight"){
        	dth = "Xiaomi Philips Bedside Lamp";
            name = "Xiaomi Philips Bedside Lamp";
        }else if(params.type == "rockrobo.vacuum.v1" || params.type == "roborock.vacuum.c1" || params.type == "roborock.vacuum.m1s" || params.type == "roborock.vacuum.s6" || params.type == "viomi.vacuum.v7"){
        	dth = "Xiaomi Vacuums";
            name = "Xiaomi Vacuums";
        }else if(params.type == "roborock.vacuum.s5" || params.type == "roborock.vacuum.s6"){
        	dth = "Xiaomi Vacuums2";
            name = "Xiaomi Vacuums2";
        }else if(params.type == "qmi.powerstrip.v1" || params.type == "zimi.powerstrip.v2"){
        	dth = "Xiaomi Power Strip";
            name = "Xiaomi Power Strip";
        }else if(params.type == "chuangmi.plug.v1" || params.type == "chuangmi.plug.v2" || params.type == "chuangmi.plug.v3" || params.type == "chuangmi.plug.m1"|| params.type == "chuangmi.plug.m2" || params.type == "chuangmi.plug.m3" || params.type == "lumi.plug"){
        	dth = "Xiaomi Power Plug";
            name = "Xiaomi Power Plug";
        }else if(params.type == "chuangmi.plug.v3"){
        	dth = "Xiaomi Power Plug2";
            name = "Xiaomi Power Plug2";
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
        }else if(params.type == "cgllc.airmonitor.b1" || params.type == "cgllc.airmonitor.s1"){
        	dth = "Xiaomi Air Detector";
            name = "Xiaomi Air Detector";
        }else if(params.type == "lumi.weather"){
        	dth = "Xiaomi Weather";
            name = "Xiaomi Weather";
        }else if(params.type == "lumi.gas"){
			dth = "Xiaomi Gas Detector";
            name = "Xiaomi Gas Dectector";
		}else if(params.type == "lumi.smoke"){
        	dth = "Xiaomi Smoke Detector";
            name = "Xiaomi Smoke Detector";
        }else if(params.type == "yeelink.light.ceiling1" || params.type == "yeelink.light.ceiling2"|| params.type == "yeelink.light.ceiling3"|| params.type == "yeelink.light.ceiling4" || params.type == "yeelink.light.ceiling5"|| params.type == "yeelink.light.ceiling6"|| params.type == "yeelink.light.ceiling7"|| params.type == "yeelink.light.ceiling8"|| params.type == "yeelink.light.ceiling9"){
        	dth = "Xiaomi Light Ceiling";
            name = "Xiaomi Light Ceiling";
        }else if(params.type == "philips.light.ceiling" || params.type == "philips.light.zyceiling"){
        	dth = "Xiaomi Philips Light Ceiling";
            name = "Xiaomi Philips Light Ceiling";
        }else if(params.type == "lumi.curtain"){
        	dth = "Xiaomi Curtain";
            name = "Xiaomi Curtain";
        }else if(params.type == "lumi.curtain.b1"){
        	dth = "Xiaomi Curtain2";
            name = "Xiaomi Curtain B1";
        }else if(params.type == "lumi.water"){
			dth = "Xiaomi Water Detector";
            name = "Xiaomi Water Dectector";
		}else if(params.type == "miband"){
			dth = "Xiaomi Mi band";
            name = "Xiaomi Miband";
		}else if(params.type == "ble.flora"){
			dth = "Xiaomi Flora";
            name = "Xiaomi Flora";
		}else if(params.type == "ble.floraPot"){
			dth = "Xiaomi Flora Pot";
            name = "Xiaomi Flora Pot";
		}else if(params.type == "chuangmi.ir.v2" || params.type == "chuangmi.remote.h102a03" || params.type == "chuangmi.remote.v2"){
			dth = "Xiaomi Remote";
            name = "Xiaomi Remote";
		}else if(params.type == "virtual.remote.tv"){
        	dth = "Xiaomi Remote TV";
            name = "Xiaomi Remote TV";
        }else if(params.type == "virtual.remote.custom"){
        	dth = "Xiaomi Remote Custom";
            name = "Xiaomi Remote Custom";
        }else if(params.type == "virtual.remote.air"){
        	dth = "Xiaomi Remote Air Conditioner";
            name = "Xiaomi Remote Air Conditioner";
        }else if(params.type == "virtual.ping"){
        	dth = "Xiaomi Virtual Device";
            name = "Xiaomi Virtual Device";
        }else if(params.type == "lumi.acpartner.v3"){
        	dth = "Xiaomi Gateway2";
            name = "Xiaomi Gateway2";
        }else if(params.type == "ble.mitemperature" || params.type == "ble.einktemperature"){
        	dth = "Xiaomi Bluetooth Weather";
            name = "Xiaomi Bluetooth Weather";
        }else if(params.type == "lumi.vibration"){
        	dth = "Xiaomi Vibration Sensor"
            name = "Xiaomi Vibration Sensor"
        }else if(params.type == "zhimi.heater.za1"){
        	dth = "Xiaomi Heater"
            name = "Xiaomi Heater"
        }else if(params.type == "zhimi.airfresh.va2"){
        	dth = "Xiaomi Air Fresh"
            name = "Xiaomi Air Fresh"
        }else if(params.type == "air.fan.ca23ad9"){
        	dth = "Xiaomi Circulator"
            name = "Xiaomi Circulator"
        }else if(params.type == "nwt.derh.wdh318efw1"){
        	dth = "Xiaomi Dehumidifier"
            name = "Xiaomi Dehumidifier"
        }	
        
        
        if(dth == null){
        	log.warn("Failed >> Non exist DTH!!! Type >> " + type);
            def resultString = new groovy.json.JsonOutput().toJson("result":"nonExist")
            render contentType: "application/javascript", data: resultString
        }else if(params.type == "lumi.ctrl_neutral1" || params.type == "lumi.ctrl_ln1"){
        	try{
                def childDevice = addChildDevice("fison67", dth, (dni + "-1"), getLocationID(), [
                    "label": name + "1"
                ])    
                childDevice.setInfo(settings.address, id, "1")
                
                try{ childDevice.refresh() }catch(e){}
                try{ childDevice.setLanguage(settings.selectedLang) }catch(e){}
                try{ childDevice.setExternalAddress(settings.externalAddress) }catch(e){}
                
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
                	def childDevice = addChildDevice("fison67", dth, (dni + "-" + index), getLocationID(), [
                        "label": name + index
                    ])    
                    childDevice.setInfo(settings.address, id, index.toString())
                    
                	try{ childDevice.refresh() }catch(e){}
                	try{ childDevice.setLanguage(settings.selectedLang) }catch(e){}
                	try{ childDevice.setExternalAddress(settings.externalAddress) }catch(e){}
                    
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
        }else if(params.type == "virtual.remote.tv" || params.type == "virtual.remote.custom" || params.type == "virtual.remote.air"){
     		dni = "mi-connector-" + id.toLowerCase() + "-" + new Date().getTime()
        	def childDevice = addChildDevice("fison67", dth, dni, getLocationID(), [
                "label": name
            ])    
            childDevice.setInfo(settings.address, id)
            childDevice.setData(data)
            try{ childDevice.setLanguage(settings.selectedLang) }catch(e){}
            try{ childDevice.setExternalAddress(settings.externalAddress) }catch(e){}
            
            def resultString = new groovy.json.JsonOutput().toJson("result":"ok")
            render contentType: "application/javascript", data: resultString
        }else{
            try{
                def childDevice = addChildDevice("fison67", dth, dni, getLocationID(), [
                    "label": name
                ])    
                childDevice.setInfo(settings.address, id)
                log.debug "Success >> ADD Device : ${type} DNI=${dni}"
         
                try{ childDevice.refresh() }catch(e){}
                try{ childDevice.setLanguage(settings.selectedLang) }catch(e){}
                try{ childDevice.setExternalAddress(settings.externalAddress) }catch(e){}
                
                def resultString = new groovy.json.JsonOutput().toJson("result":"ok")
                render contentType: "application/javascript", data: resultString
            }catch(e){
                log.error "Failed >> ADD Device Error : ${e}"
                def resultString = new groovy.json.JsonOutput().toJson("result":"fail")
                render contentType: "application/javascript", data: resultString
            }
    	}
    }
    
}

def _getServerURL(){
     return settings.address
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

def getLocationID(){
	def locationID = null
    try{ locationID = location.hubs[0].id }catch(err){}
    return locationID
}

def versionCallBack(physicalgraph.device.HubResponse hubResponse) {
    def msg, json, status
    try {
        msg = parseLanMessage(hubResponse.description)
        log.debug "${msg.body}"
        state.dockerVersion = msg.body
    } catch (e) {
        logger('warn', "Exception caught while parsing data: "+e);
    }
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