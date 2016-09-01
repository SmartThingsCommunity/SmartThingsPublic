definition(
    name:        "Arlo - Auto Arm/Disarm",
    namespace:   "mbmccormick",
    author:      "Matt McCormick",
    description: "Automatically change Arlo camera modes based on SmartThings modes.",
    category:    "Safety & Security",
    iconUrl:     "https://bitbucket-assetroot.s3.amazonaws.com/c/photos/2015/Jun/01/3175286666-5-hummingbird-logo_avatar.png",
    iconX2Url:   "https://bitbucket-assetroot.s3.amazonaws.com/c/photos/2015/Jun/01/3175286666-5-hummingbird-logo_avatar.png",
    iconX3Url:   "https://bitbucket-assetroot.s3.amazonaws.com/c/photos/2015/Jun/01/3175286666-5-hummingbird-logo_avatar.png"
)

preferences {
    page(name: "page1", nextPage: "page2", uninstall: true) {
        section("Enter the login information for your Arlo Account below.") {
            input(name: "strArloEmail", type: "email", title: "Email")
            input(name: "strArloPassword", type: "password", title: "Password")
        }
    }
    
    page(name: "page2", nextPage: "page3", uninstall: false)
    
    page(name: "page3", uninstall: false, install: true) {
        section("Select the SmartThings mode in which you want to Arm your Arlo devices.") {
            input(name: "lstArmedMode", type: "mode", title: "Armed Mode", multiple: true)
        }
        
        section("Select the SmartThings mode in which you want to Disarm your Arlo devices.") {
            input(name: "lstDisarmedMode", type: "mode", title: "Disarmed Mode", multiple: true)
        }
    }
}

def page2() {
    dynamicPage(name: "page2") {
        section("Select the Arlo devices you wish to automate.") {
            input(name: "lstArloDevices", type: "enum", title: "Arlo Devices", options: getArloDevicesOptions(), multiple: true)
        }
    }
}

List getArloDevicesOptions() {
    def lstDevices = []
    
    for (objDevice in getArloDevices()) {
        lstDevices << objDevice.deviceName
    }
    
    return lstDevices
}

Map getArloRequestHeaders() {
    def strAuthToken
    def lstAuthCookies = []

    try {
        httpPostJson(
            [
                uri:  "https://arlo.netgear.com",
                path: "/hmsweb/login",
                body: [
                    email:    settings.strArloEmail,
                    password: settings.strArloPassword
                ]
            ]
        ) { objResponse ->
            if (objResponse.data.success == true) {
                strAuthToken    = objResponse.data.data.token
                objResponse.headers.each {
                    if (it.name == "Set-Cookie") {
                        lstAuthCookies << it.value.split(";").getAt(0)
                    }
                }
            } else {
                log.error "Failed to login to Arlo account. Response: ${objResponse.data}"
                
                return false
            }
        }
    } catch (objException) {
        log.error "Failed to login to Arlo account. Exception: ${objException}"
        
        raise objException
    }
    
    return [
        Authorization: strAuthToken,
        Cookie:        lstAuthCookies.join(";")
    ]
}

Map getArloDevice(strDeviceName = "") {
    for (objDevice in getArloDevices()) {
        if (objDevice.deviceName == strDeviceName) {
            return objDevice
        }
    }
    
    log.error "Unable to find Arlo device \"${strDeviceName}\"."
    
    return false
}

List getArloDevices(strDeviceName = "") {
    def lstDevices = []
    
    try {
        httpGet(
            [
                uri:     "https://arlo.netgear.com",
                path:    "/hmsweb/users/devices",
                headers: getArloRequestHeaders()
            ]
        ) { objResponse ->
            if (objResponse.data.success == true) {
                for (objDevice in objResponse.data.data) {
                    if (objDevice.deviceType == "basestation" ||
                        objDevice.deviceType == "arloq") {
                        lstDevices << objDevice
                    }
                }
            } else {
                log.error "Failed to retrieve list of devices. Response: ${objResponse.data}"
                
                return false
            }
        }
    } catch (objException) {
        log.error "Failed to retrieve list of devices. Exception: ${objException}"
        
        raise objException
    }
    
    return lstDevices
}

Boolean setArloModeArmed(Map mapArloDevice) {
    return setArloMode(mapArloDevice, "mode1")
}

Boolean setArloModeDisarmed(Map mapArloDevice) {
    return setArloMode(mapArloDevice, "mode0")
}

Boolean setArloMode(Map mapArloDevice, String strArloModeId) {
    try {
        httpPostJson(
            [
                uri:     "https://arlo.netgear.com",
                path:    "/hmsweb/users/devices/notify/${mapArloDevice.deviceId}",
                headers: [
                    xcloudId: mapArloDevice.xCloudId
                ] + getArloRequestHeaders(),
                body: [
                    action:     "set",
                    from:       "SmartThings",
                    properties: [
                        active: strArloModeId
                    ],
                    active:          strArloModeId,
                    publishResponse: false,
                    resource:        "modes",
                    responseUrl:     "",
                    to:              mapArloDevice.deviceId,
                    transId:         ""
                ]
            ]
        ) { objResponse ->
            if (objResponse.data.success == true) {
                return true
            } else {
                log.error "Failed to set Arlo mode for ${mapArloDevice.xCloudId}. Response: ${objResponse.data}"

                return false
            }
        }
    } catch (objException) {
        log.error "Failed to set Arlo mode for ${mapArloDevice.xCloudId}. Exception: ${objException}"

        raise objException
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    
    initialize()
}

def initialize() {
    subscribe(location, "mode", onModeChanged)
}

def onModeChanged(mapEvent) {
    log.debug("SmartThings mode changed to: ${mapEvent.value}")
    
    if (settings.lstArmedMode?.contains(mapEvent.value)) {
    	for (strDeviceName in settings.lstArloDevices) {
        	def mapArloDevice = getArloDevice(strDeviceName)
                
            if (setArloModeArmed(mapArloDevice)) {
                sendNotificationEvent("Arlo mode for ${mapArloDevice.deviceName} changed to Armed.")
            } else {
                sendNotificationEvent("Failed to change Arlo mode for ${mapArloDevice.deviceName} to Armed!")
            }
        }
    } else if (settings.lstDisarmedMode?.contains(mapEvent.value)) {
        for (strDeviceName in settings.lstArloDevices) {
        	def mapArloDevice = getArloDevice(strDeviceName)
                
            if (setArloModeDisarmed(mapArloDevice)) {
                sendNotificationEvent("Arlo mode for ${mapArloDevice.deviceName} changed to Disarmed.")
            } else {
                sendNotificationEvent("Failed to change Arlo mode for ${mapArloDevice.deviceName} to Disarmed!")
            }
        }
    }
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    
    unsubscribe()    
    initialize()
}