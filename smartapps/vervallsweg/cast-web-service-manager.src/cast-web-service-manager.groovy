/**
 *  Cast web - service manager
 *
 *  Copyright 2017 Tobias Haerke
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
    name: "Cast web - service manager",
    namespace: "vervallsweg",
    author: "Tobias Haerke",
    description: "Connect your Cast devices through the Cast web API to SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "https://github.com/vervallsweg/smartthings/raw/master/icn/ic_cast_grey_24dp.png",
    iconX2Url: "https://github.com/vervallsweg/smartthings/raw/master/icn/ic_cast_grey_24dp.png",
    iconX3Url: "https://github.com/vervallsweg/smartthings/raw/master/icn/ic_cast_grey_24dp.png") {
    appSetting "api"
}


preferences {
    page(name: "mainPage")
    page(name: "checkApiConnectionPage")
    page(name: "discoveryPage")
    page(name: "addDevicesPage")
    page(name: "configureDevicePage")
    page(name: "saveDeviceConfigurationPage")
    page(name: "updateServiceManagerPage")
}

def mainPage() {
    if(state.latestHttpResponse){state.latestHttpResponse = null;}
    dynamicPage(name: "mainPage", title: "Manage your Cast devices", nextPage: null, uninstall: true, install: true) {
        section("Configure web API"){
            input "apiHostAddress", "string", title: "API host address", required: true
            href "updateServiceManagerPage", title: "Check for updates", description:""
            href "checkApiConnectionPage", title: "Test API connection", description:""
            href "setupGoogleAssistant",title: "Setup the Google Assistant with cast-web to broadcast messages", required: false, style: "external", url: "http://"+apiHostAddress+"/assistant/setup/", description: ""
        }
        section("Configure Cast devices"){
            input(name: "settingsLogLevel", type: "enum", title: "Service manager log level", options: [0, 1, 2, 3, 4])
            href "discoveryPage", title:"Discover Devices", description:""//, params: [pbutton: i]
        }
        section("Installed Devices"){
            def dMap = [:]
            getChildDevices().sort({ a, b -> a["label"] <=> b["label"] }).each {
                it.getChildDevices().sort({ a, b -> a["label"] <=> b["label"] }).each {
                    logger('debug', "mainPage(), it.label: "+it.label+", it.deviceNetworkId: "+it.deviceNetworkId)
                    href "configureDevicePage", title:"$it.label", description:"", params: [dni: it.deviceNetworkId]
                }
            }
        }
    }
}

def checkApiConnectionPage() {
    dynamicPage(name:"checkApiConnectionPage", title:"Test API connection", nextPage: "mainPage", refreshInterval:10) {
        getDevices() //TODO: get root and only check status
        logger('debug', "checkApiConnectionPage(), refresh")
        
        section("Please wait for the API to answer, this might take a couple of seconds.") {
            if(state.latestHttpResponse) {
                if(state.latestHttpResponse==200) {
                    paragraph "Connected \nOK: 200"
                } else {
                    paragraph "Connection error \nHTTP response code: " + state.latestHttpResponse
                }
            }
        }
    }
}

def discoveryPage() {
    dynamicPage(name:"discoveryPage", title:"Discovery Started!", nextPage: "addDevicesPage", refreshInterval:10) {
        getDevices()
        logger('debug', "discoveryPage(), refresh")
        
        section("Please wait while we discover your Cast devices. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
            if(state.devicesMap!=null && state.devicesMap.size()>0) {
                input "selectedDevices", "enum", required:false, title:"Select Cast Device ("+ state.devicesMap.size() +" found)", multiple:true, options: state.devicesMap
                //state.selectedDevicesMap = state.devicesMap
            } else {
                input "selectedDevices", "enum", required:false, title:"Select Cast Device (0 found)", multiple:true, options: [:]
                //state.selectedDevicesMap = null
            }
        }
        
        //state.latestDeviceMap = null
    }
}

def addDevicesPage() {
    def addedDevices = addDevices(selectedDevices)
    
    dynamicPage(name:"addDevicesPage", title:"Done", nextPage: null, uninstall: false, install: true) {
        section("Devices added") {
            if( !addedDevices.equals("0") ) {
                addedDevices.each{ key, value ->
                    paragraph title: value, ""+key
                }
            } else {
                paragraph "No devices added."
            }
        }
    }
}

def addDevices(selectedDevices) {
    def addedDevices = [:]
    logger('debug', "selectedDevices: "+selectedDevices+" childDevices: " + getChildDevices().size() )
        
    if(selectedDevices && selectedDevices!=null) {
        
        if(getChildDevices().size()<1) {
            logger('debug', "No cast-web-api installed" )
            
            if(state.latestHttpMac) {
                addChildDevice("vervallsweg", "cast-web-api", ""+state.latestHttpMac, location.hubs[0].id, [
                    "label": "cast-web-api",
                    "data": [
                        "apiHost": apiHostAddress,
                        "devices": "[]"
                    ]
                ])
            } else {
                addedDevices.put('Error', "The cast-web-api doesn't retun it's MAC address. No devices were added.")
            }
        }
        
        selectedDevices.each { key ->
            logger('debug', "Selected device id: " + key + ", name: " + state.devicesMap[key] )
            addedDevices.put(key, state.devicesMap[key])
        }
        
        getChildDevices().each {
            it.updateDataValue("devices", ""+selectedDevices);
            it.updated()
        }
    }
    
    if(addedDevices==[:]) {
        return "0"
    } else {
        return addedDevices
    }
}

def configureDevicePage(dni) {
    def d
    getChildDevices().each{ api ->
        api.getChildDevices().each { device ->
            if( device.deviceNetworkId.equals(dni["dni"]) ) {
                d = device
            }
        }
    }
    logger('debug', "configureDevicePage() selected device d: " + d)
    state.configCurrentDevice = d.deviceNetworkId
    
    if(d){
        resetFormVar(d)
    
        dynamicPage(name: "configureDevicePage", title: "Configure "+d.displayName+" ("+d.deviceNetworkId+")", nextPage: "saveDeviceConfigurationPage") {
            section("Connection settings") {
                input(name: "api_host_address", type: "text", title: "cast-web-api address", defaultValue: [d.getDataValue("apiHost")], required: true)
            }
            section("Presets") {
                input(name: "presetObject", type: "text", title: "Preset object", defaultValue: [d.getDataValue("presetObject")], required: true)
                href(name: "presetGenerator",title: "Edit this preset in your browser",required: false,style: "external",url: "https://vervallsweg.github.io/smartthings/cast-web-preset-generator/preset-generator.html?"+d.getDataValue("presetObject"),description: "")
            }
        }
    } else {
        dynamicPage(name: "configureDevicePage", title: "Error", nextPage: "mainPage") {
            section("Something went wrong"){ 
                paragraph "Cannot access the device"
            }
        }
    }
}

def saveDeviceConfigurationPage() {
    def d
    getChildDevices().each{ api ->
        api.getChildDevices().each { device ->
            if( device.deviceNetworkId.equals( state.configCurrentDevice ) ) {
                d = device
            }
        }
    }
    logger('debug', "saveDeviceConfigurationPage() writing configuration for d: " + d)
    
    //d.displayName = label
    //d.updateDataValue("deviceType", device_type)
    //d.updateDataValue("pollMinutes", ""+poll_minutes)
    //d.updateDataValue("pollSecond", ""+poll_seconds)
    //d.updateDataValue("deviceAddress", device_address)
    d.updateDataValue("apiHost", api_host_address)
    d.updateDataValue("presetObject", presetObject)
    //d.updateDataValue("logLevel", ""+log_level)
    d.updated()
    
    dynamicPage(name: "saveDeviceConfigurationPage", title: "Configuration updated for: "+d.deviceNetworkId, nextPage: "mainPage") {
        section("Device name"){ paragraph ""+d.displayName }
        //section("Device type"){ paragraph ""+d.getDataValue("deviceType") }
        //section("Refresh every x minute"){ paragraph ""+d.getDataValue("pollMinutes") }
        //section("Refresh on every x second"){ paragraph ""+d.getDataValue("pollSecond") }
        //section("Cast device IP address"){ paragraph ""+d.getDataValue("deviceAddress") }
        section("Web API host address"){ paragraph ""+d.getDataValue("apiHost") }
        section("Presets"){ paragraph ""+d.getDataValue("presetObject") }
        //section("Log level"){ paragraph ""+d.getDataValue("logLevel") }
    }
}

def resetFormVar(device) {
    //if(label){ app.updateSetting("label", device.label) }
    //if(device_type){ app.updateSetting("device_type", [device.getDataValue("deviceType")]) }
    //if(poll_minutes){ app.updateSetting("poll_minutes", [device.getDataValue("pollMinutes")]) }
    //if(poll_seconds){ app.updateSetting("poll_seconds", [device.getDataValue("pollSecond")]) }
    //if(device_address){ app.updateSetting("device_address", [device.getDataValue("deviceAddress")]) }
    if(api_host_address){ app.updateSetting("api_host_address", [device.getDataValue("apiHost")]) }
    if(presetObject){ app.updateSetting("presetObject", [device.getDataValue("presetObject")]) }
    //if(log_level){ app.updateSetting("log_level", [device.getDataValue("logLevel")]) }
}

def installed() {
    logger('debug', "Installed with settings: ${settings}")

    initialize()
}

def updated() {
    logger('debug', "Updated with settings: ${settings}")

    unsubscribe()
    initialize()
}

def initialize() {
    // TODO: subscribe to attributes, devices, locations, etc.
    getChildDevices().each {
        if(it) {
            log.info "it: "+it
            try {
                it.setApiHost(apiHostAddress);
            } catch(e) {
                log.info "Yeah, probably double exec error: "+e
            }
        }
    }
}

def getDevices() {
    logger('debug', "Executing 'getDevices'")
    sendHttpRequest(apiHostAddress, '/device')
}

def sendHttpRequest(String host, String path) {
    logger('debug', "Executing 'sendHttpRequest' host: "+host+" path: "+path)
    sendHubCommand(new physicalgraph.device.HubAction("""GET ${path} HTTP/1.1\r\nHOST: $host\r\n\r\n""", physicalgraph.device.Protocol.LAN, host, [callback: hubResponseReceived]))
}

void hubResponseReceived(physicalgraph.device.HubResponse hubResponse) {
    parse(hubResponse.description)
}

def parse(description) {
    logger('debug', "Parsing '${description}'")
    
    def msg, json, status, mac
    try {
        msg = parseLanMessage(description)
        status = msg.status
        json = msg.json
        mac = msg.mac
    } catch (e) {
        logger("error", "Exception caught while parsing data: "+e)
        return null;
    }
  
    state.latestHttpResponse = status
    state.latestHttpMac = mac
    if(status==200){
        def length = 0
        logger('debug', "JSON rcvd: "+json+", JSON.size: "+json.size)
        
        def devices = [:]
        for(int i=0; i<json.size; i++) {
            logger('debug', "index "+ i +": "+json[i]['name']+", "+ json[i]['id'])
            devices.put(json[i]['id'], json[i]['name'])
        }
       
        logger('debug', "devices: " + devices)
        state.devicesMap = devices
    } else {
        state.devicesMap = [:]
    }
}

//UPDATE
def getThisVersion() {
    return '1.2.0'
}

def getLatestVersion() {
    try {
        httpGet([uri: "https://raw.githubusercontent.com/vervallsweg/smartthings/master/smartapps/vervallsweg/cast-web-service-manager.src/version.json"]) { resp ->
            logger('debug', "getLatestVersion(), response status: ${resp.status}")
            String data = "${resp.getData()}"
            logger('debug', "getLatestVersion(), data: ${data}")
            
            if(resp.status==200 && data!=null) {
                return parseJson(data)
            } else {
                return null
            }
        }
    } catch (e) {
        logger("error", "getLatestVersion(), something went wrong: "+e)
        return null
    }
}

def checkForUpdate() {
    if(getThisVersion() != getLatestVersion().version) {
        return "Update available from: " + getThisVersion() + " to: " + getLatestVersion().version
    } else {
        logger('debug', "Up to date, " + "thisVersion: " + getThisVersion() + ", latestVersion: " + getLatestVersion().version)
        return "Up to date: " + getThisVersion()
    }
}

def updateServiceManagerPage() {
    dynamicPage(name:"updateServiceManagerPage", title:"Check for updates", nextPage: nextPage) {
        section("Checked for updates") {
            paragraph "" + checkForUpdate()
        }
        section("Latest version") {
            def latestVersion = getLatestVersion()
            paragraph "Version: " + latestVersion.version
            paragraph "Type: " + latestVersion.type
            paragraph "Release date: " + latestVersion.date
            href(name: "Changelog",title: "Changelog",required: false, url: latestVersion.changelog, description: "")
        }
    }
}

//DEBUGGING
def logger(level, message) {
    def smLogLevel = 0
    if(settingsLogLevel) {
        smLogLevel = settingsLogLevel.toInteger()
    }
    if(level=="error"&&smLogLevel>0) {
        log.error message
    }
    if(level=="warn"&&smLogLevel>1) {
        log.warn message
    }
    if(level=="info"&&smLogLevel>2) {
        log.info message
    }
    if(level=="debug"&&smLogLevel>3) {
        log.debug message
    }
}