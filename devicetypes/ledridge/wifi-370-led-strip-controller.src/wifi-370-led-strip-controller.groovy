/**
 *  RGBWW LED Controller
 *
 *  Copyright 2017 Ph4r
 *
 */
metadata {
	definition (name: "WiFi 370 LED Strip Controller", namespace: "ledridge", author: "Ph4r") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        
		command "setRed"
        command "setGreen"
        command "setBlue"
        command "setAdjustedColor"
        command "softwhite"
        command "daylight"
        command "warmwhite"
        command "red"
        command "green"
        command "blue"
        command "cyan"
        command "magenta"
        command "orange"
        command "purple"
        command "black"
        command "yellow"
        command "white"
        command "setWhiteLevel"
        command "setSpeed"
        command "Fade7"
        command "Strobe7"
        command "Jump7"
        command "FadeRed"
        command "StrobeRed"
        command "FadeGreen"
        command "StrobeGreen"
        command "FadeBlue"
        command "StrobeBlue"
        command "setCoolWhite"
        command "setWarmWhite"
        command "user1"
        command "user2"
        command "user3"
        command "refresh"
     }
    
     tiles(scale: 2)  {

        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.color", key: "SECONDARY_CONTROL") {
				attributeState "color", label:'Color${currentValue}'
			}
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}
        /*
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 4, inactiveLabel: false, range:"(0..100)") {
            state "level", action:"switch level.setLevel"
        }
        */
        controlTile("rlSliderControl", "device.rl", "slider", height: 1, width: 4, range:"(0..255)", inactiveLabel: false) {
            state "rl", label:'Red', action:"setRed"
        }
        valueTile("rl", "device.rl", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state "rl", label: 'RL\n${currentValue}'
        }
        controlTile("glSliderControl", "device.gl", "slider", height: 1, width: 4, range:"(0..255)", inactiveLabel: false) {
            state "gl", action:"setGreen"
        }
        valueTile("gl", "device.gl", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state "gl", label: 'GL\n${currentValue}'
        }
        controlTile("blSliderControl", "device.bl", "slider", height: 1, width: 4, range:"(0..255)", inactiveLabel: false) {
            state "bl", action:"setBlue"
        }
        valueTile("bl", "device.bl", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state "bl", label: 'BL\n${currentValue}'
        }
        /*
        valueTile("level", "device.level", height: 2, width: 3, inactiveLabel: false, decoration: "flat") {
            state "level", label: 'Level\n${currentValue}%'
        }
        controlTile("saturationSliderControl", "device.saturation", "slider", height: 1, width: 4, inactiveLabel: false) {
            state "saturation", action:"color control.setSaturation"
        }
        valueTile("saturation", "device.saturation", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state "saturation", label: 'Sat ${currentValue}%'
        }
        controlTile("hueSliderControl", "device.hue", "slider", height: 1, width: 4, inactiveLabel: false) {
            state "hue", action:"color control.setHue"
        }
        valueTile("hue", "device.hue", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state "hue", label: 'Hue ${currentValue}%'
        }
        */
        controlTile("rgbSelector", "device.color", "color", height: 4, width: 4, inactiveLabel: false) {
            state "color", action:"setAdjustedColor"
		}
        standardTile("white", "device.white", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offwhite", label:"White", action:"white", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onwhite", label:"White", action:"white", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("red", "device.red", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offred", label:"red", action:"red", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onred", label:"red", action:"red", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FF0000"
        }
		standardTile("softwhite", "device.softwhite", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offsoftwhite", label:"soft white", action:"softwhite", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onsoftwhite", label:"soft white", action:"softwhite", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFF1E0"
        }
        /*
        standardTile("daylight", "device.daylight", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offdaylight", label:"daylight", action:"daylight", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "ondaylight", label:"daylight", action:"daylight", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFB"
        }
        */
        standardTile("warmwhite", "device.warmwhite", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offwarmwhite", label:"warm white", action:"warmwhite", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onwarmwhite", label:"warm white", action:"warmwhite", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFF4E5"
        }
        standardTile("black", "device.black", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offblack", label:"black", action:"black", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onblack", label:"black", action:"black", icon:"st.illuminance.illuminance.bright", backgroundColor:"#000000"
        }
        /*
        standardTile("yellow", "device.yellow", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offyellow", label:"yellow", action:"yellow", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onyellow", label:"yellow", action:"yellow", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFF00"
        }
        */
        standardTile("magenta", "device.magenta", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offmagenta", label:"magenta", action:"magenta", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onmagenta", label:"magenta", action:"magenta", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FF00FF"
        }
        standardTile("green", "device.green", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offgreen", label:"green", action:"green", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "ongreen", label:"green", action:"green", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00FF00"
        }
        standardTile("blue", "device.blue", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offblue", label:"blue", action:"blue", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onblue", label:"blue", action:"blue", icon:"st.illuminance.illuminance.bright", backgroundColor:"#0000FF"
        }
        standardTile("cyan", "device.cyan", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offcyan", label:"cyan", action:"cyan", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "oncyan", label:"cyan", action:"cyan", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00FFFF"
        }
        standardTile("orange", "device.orange", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offorange", label:"orange", action:"orange", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onorange", label:"orange", action:"orange", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FF6600"
        }
        standardTile("purple", "device.purple", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offpurple", label:"purple", action:"purple", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onpurple", label:"purple", action:"purple", icon:"st.illuminance.illuminance.bright", backgroundColor:"#BF00FF"
        }
        controlTile("speedSliderControl", "device.speed", "slider", height: 1, width: 4, range:"(1..100)", inactiveLabel: false) {
            state "speed", label:'Speed', action:"setSpeed"
        }
        valueTile("speed", "device.speed", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state "speed", label: 'Speed\n${currentValue}'
        }
        standardTile("Fade7", "device.Fade7", height: 1, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offFade7", label:"Fade7", action:"Fade7", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onFade7", label:"Fade7", action:"Fade7", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("Strobe7", "device.Strobe7", height: 1, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offStrobe7", label:"Strobe7", action:"Strobe7", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onStrobe7", label:"Strobe7", action:"Strobe7", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("Jump7", "device.Jump7", height: 1, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offJump7", label:"Jump7", action:"Jump7", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onJump7", label:"Jump7", action:"Jump7", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("FadeRed", "device.FadeRed", height: 1, width: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offFadeRed", label:"FadeRed", action:"FadeRed", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onFadeRed", label:"FadeRed", action:"FadeRed", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("StrobeRed", "device.StrobeRed", height: 1, width: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offStrobeRed", label:"StrobeRed", action:"StrobeRed", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onStrobeRed", label:"StrobeRed", action:"StrobeRed", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("FadeGreen", "device.FadeGreen", height: 1, width: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offFadeGreen", label:"FadeGreen", action:"FadeGreen", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onFadeGreen", label:"FadeGreen", action:"FadeGreen", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("StrobeGreen", "device.StrobeGreen", height: 1, width: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offStrobeGreen", label:"StrobeGreen", action:"StrobeGreen", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onStrobeGreen", label:"StrobeGreen", action:"StrobeGreen", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("FadeBlue", "device.FadeBlue", height: 1, width: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offFadeBlue", label:"FadeBlue", action:"FadeBlue", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onFadeBlue", label:"FadeBlue", action:"FadeBlue", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("StrobeBlue", "device.StrobeBlue", height: 1, width: 1, inactiveLabel: false, canChangeIcon: false) {
            state "offStrobeBlue", label:"StrobeBlue", action:"StrobeBlue", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onStrobeBlue", label:"StrobeBlue", action:"StrobeBlue", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        controlTile("warmWhiteSliderControl", "device.warmWhite", "slider", height: 1, width: 4, range:"(0..255)", inactiveLabel: false) {
            state "warmWhite", label:'Warm White', action:"setWarmWhite"
        }
        valueTile("warmWhite", "device.warmWhite", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state "warmWhite", label: 'WW\n${currentValue}'
        }
	controlTile("coolWhiteSliderControl", "device.coolWhite", "slider", height: 1, width: 4, range:"(0..255)", inactiveLabel: false) {
            state "coolWhite", label:'Cool White', action:"setCoolWhite"
        }
        valueTile("coolWhite", "device.coolWhite", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state "coolWhite", label: 'CW\n${currentValue}'
        }
        standardTile("user1", "device.user1", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offuser1", label:"user1", action:"user1", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onuser1", label:"user1", action:"user1", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("user2", "device.user2", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offuser2", label:"user2", action:"user2", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onuser2", label:"user2", action:"user2", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
        standardTile("user3", "device.user3", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "offuser3", label:"user3", action:"user3", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
            state "onuser3", label:"user3", action:"user3", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
        }
               

        main(["switch"])
        //details(["switch", "rgbSelector"])

    }
    
    preferences {
        input("ip", "string", title:"Controller IP Address", description: "Controller IP Address", defaultValue: "192.168.1.69", required: false, displayDuringSetup: true)
        input("port", "string", title:"Controller Port", description: "Controller Port", defaultValue: 5577 , required: false, displayDuringSetup: true)
        //input("username", "string", title:"Controller Username", description: "Controller Username", defaultValue: admin, required: false, displayDuringSetup: true)
        //input("password", "password", title:"Controller Password", description: "Controller Password", defaultValue: nimda, required: false, displayDuringSetup: true)
        input(name:"CStyle", type:"enum", title: "Controller Style", options: ["RGBWW", "RGBW", "RGB+WW", "RGB", "RGB-LW12"], description: "Enter Controller Style", defaultValue: "RGBWW" , required: false, displayDuringSetup: true)
        //input("userPref1", "string", title:"User Button 1 Name", description: "User Button 1 Name" , required: false, displayDuringSetup: false)
        //input("userPref1C", "string", title:"User Button 1 Color", description: "User Button 1 Color" , required: false, displayDuringSetup: false)
        //input("userPref2", "string", title:"User Button 2 Name", description: "User Button 2 Name" , required: false, displayDuringSetup: false)
        //input("userPref2C", "string", title:"User Button 2 Color", description: "User Button 2 Color" , required: false, displayDuringSetup: false)
        //input("userPref3", "string", title:"User Button 3 Name", description: "User Button 3 Name" , required: false, displayDuringSetup: false)
        //input("userPref3C", "string", title:"User Button 3 Color", description: "User Button 3 Color" , required: false, displayDuringSetup: false)
    }
}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []
	def map = description
	if (description instanceof String)  {
		log.debug "WiFi 370 LED Strip stringToMap - ${map}"
		map = stringToMap(description)
	}
	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}
	results
}

// handle commands
def getSaturation() {
	def valueNow = device.latestValue("saturation")
	if (valueNow == null) { 
		valueNow = 0
		sendEvent(name: "saturation", value: valueNow)
	}
	valueNow
}

def getHue() {
	def valueNow = device.latestValue("hue")
	if (valueNow == null) { 
		valueNow = 0
		sendEvent(name: "hue", value: valueNow)
	}
	valueNow
}

def getColor() {
	def valueNow = device.latestValue("color")
	if (valueNow == null) { 
		valueNow = "#FFFFFF"
		sendEvent(name: "color", value: valueNow)
	}
	valueNow
}

def getSpeed() {
	def valueNow = device.latestValue("speed")
	if (valueNow == null) { 
		valueNow = 80
		sendEvent(name: "speed", value: valueNow)
	}
	valueNow
}

def getLevel() {
	def valueNow = device.latestValue("level")
	if (valueNow == null) { 
		valueNow = 100
		sendEvent(name: "level", value: valueNow)
	}
	valueNow
}

def getSwitch() {
	def valueNow = device.latestValue("switch")
	if (valueNow == null) { 
		valueNow = "off"
		sendEvent(name: "switch", value: valueNow)
	}
	valueNow
}

def getRL() {
	def valueNow = device.latestValue("rl")
	if (valueNow == null) { 
		valueNow = 0
		sendEvent(name: "rl", value: valueNow)
	}
	valueNow
}

def getGL() {
	def valueNow = device.latestValue("gl")
	if (valueNow == null) { 
		valueNow = 0
		sendEvent(name: "gl", value: valueNow)
	}
	valueNow
}

def getBL() {
	def valueNow = device.latestValue("bl")
	if (valueNow == null) { 
		valueNow = 0
		sendEvent(name: "bl", value: valueNow)
	}
	valueNow
}

def getWarmWhite() {
	def valueNow = device.latestValue("warmWhite")
	if (valueNow == null) { 
		valueNow = 0
		sendEvent(name: "warmWhite", value: valueNow)
	}
	valueNow
}

def getCoolWhite() {
	def valueNow = device.latestValue("coolWhite")
	if (valueNow == null) { 
		valueNow = 0
		sendEvent(name: "coolWhite", value: valueNow)
	}
	valueNow
}

def on() {
	sendEvent(name: "switch", value: "on")
    sendPower(true)
}

def off() {
	sendEvent(name: "switch", value: "off")
    sendPower(false)
}

def sendPower2(state) {
    def hosthex = convertIPtoHex(ip);
    def porthex = convertPortToHex(port);
    def target = "$hosthex:$porthex";
    device.deviceNetworkId = target;
    
    //byte[] body = buildTLSClientHello();
	byte[] body = [0x71, 0x24, 0x0F, 0xA4]
	if (CStyle == "RGB-LW12")
    {
		body = [0xCC, 0x23, 0x33]
	}
	
    log.debug "${body.length} ${bytesToHex(body)}";
    String strBody = new String(body, "ISO-8859-1");

    sendHubCommand(new physicalgraph.device.HubAction(strBody, physicalgraph.device.Protocol.LAN, getDataValue("mac")));
}


def sendPower(state) {
	def hosthex = convertIPtoHex(ip);
    def porthex = convertPortToHex(port);
    def target = "$hosthex:$porthex";
    device.deviceNetworkId = target;
    
    byte[] bytes = [0x71, 0x24, 0x0F, 0xA4]
    if (state) { // 71 23 0f a3 on
    	bytes = [0x71, 0x23, 0x0F, 0xA3]
    } else {	 //71 24 0f a4 off
    	bytes = [0x71, 0x24, 0x0F, 0xA4]
	}
	
	String body = bytes.encodeHex()
	
	byte[] bytesLW12 = [0xCC, 0x24, 0x33]
    if (state) { // 71 23 0f a3 on
    	bytesLW12 = [0xCC, 0x23, 0x33]
    } else {	 //71 24 0f a4 off
    	bytesLW12 = [0xCC, 0x24, 0x33]
	}
	
	if (CStyle == "RGB-LW12")
    {
		body = bytesLW12.encodeHex()
	}
    
	String sData = new String(bytes, "ISO-8859-1");
    byte[] bytes2 = [0xA4]
    String sData2 = new String(bytes2, "ISO-8859-1");
    String pure = "\u7123\u0FA4"
    log.debug "${sData}:${sData2}:${pure}:${body}"
    //sendHubCommand(new physicalgraph.device.HubAction(sData, physicalgraph.device.Protocol.LAN, getDataValue("mac"))); //"0A0A0A15:15C9"
    //sendHubCommand(new physicalgraph.device.HubAction(sData2, physicalgraph.device.Protocol.LAN, getDataValue("mac"))); //"0A0A0A15:15C9"
    //sendHubCommand(new physicalgraph.device.HubAction(pure, physicalgraph.device.Protocol.LAN, getDataValue("mac"))); //"0A0A0A15:15C9"
    sendHubCommand(new physicalgraph.device.HubAction(body.toString(), physicalgraph.device.Protocol.LAN, getDataValue("mac"))); //"0A0A0A15:15C9"
    
}

def sendRGB() {
	if (CStyle == "RGB-LW12")
	{
		sendRGBLW12()
		return
	}
	def hosthex = convertIPtoHex(ip);
    def porthex = convertPortToHex(port);
    def target = "$hosthex:$porthex";
    device.deviceNetworkId = target;
    
	byte[] byteHeader = [0x31]
    byte[] byteFooter = [0x0F]
    
    if (CStyle == "RGB")
    {
        byteFooter = [0x0F]
    }
    else if (CStyle == "RGB+WW")
    {
    	byteFooter = [0xF0, 0x0F]  // First byte is a filter F0 passes RGB, 0F passes WW/CW
        sendWhites() 
    }
    else // (CStyle == "RGBWW") || (CStyle == "RGBW")
    {
    	byteFooter = [0x00, 0x0F]  // First byte is a filter 00 passes all at once
    }
    
    int RL = getRL().toInteger()
    int GL = getGL().toInteger()
    int BL = getBL().toInteger()
    int warmWhite = getWarmWhite().toInteger()
    int coolWhite = getCoolWhite().toInteger()
    def level = getLevel()
    log.debug "${RL}:${GL}:${BL}::${warmWhite}:${coolWhite}@${level}+${CStyle}"
    
    String bodyHeader = byteHeader.encodeHex()
    String bodyFooter = byteFooter.encodeHex()
    String bodyMain = bodyHeader + hex(RL) + hex(GL) + hex(BL) + hex(warmWhite * level/100) + hex(coolWhite * level/100) + bodyFooter
    
    def byteMain = bodyMain.decodeHex()
    def checksum = 0
    
    byteMain.each {
    	checksum += it;
    }
    checksum = checksum & 0xFF
    String checksumHex = Integer.toHexString(checksum)
    //log.debug "${checksum}:${checksumHex}"
    
    String body = bodyMain + checksumHex
    
    sendHubCommand(new physicalgraph.device.HubAction(body.toString(), physicalgraph.device.Protocol.LAN, getDataValue("mac"))); //"0A0A0A15:15C9"    
}

def sendRGBLW12() {
	def hosthex = convertIPtoHex(ip);
    def porthex = convertPortToHex(port);
    def target = "$hosthex:$porthex";
    device.deviceNetworkId = target;
    
	byte[] byteHeader = [0x56]
    byte[] byteFooter = [0xAA]
    
    int RL = getRL().toInteger()
    int GL = getGL().toInteger()
    int BL = getBL().toInteger()
    def level = getLevel()
    log.debug "${RL}:${GL}:${BL}@${level}+${CStyle}"
    
    String bodyHeader = byteHeader.encodeHex()
    String bodyFooter = byteFooter.encodeHex()
    String bodyMain = bodyHeader + hex(RL) + hex(GL) + hex(BL) + bodyFooter
    
    sendHubCommand(new physicalgraph.device.HubAction(bodyMain.toString(), physicalgraph.device.Protocol.LAN, getDataValue("mac"))); //"0A0A0A15:15C9"    
}

def sendWhites() {  // Need to maintain this, so that whites can be changed while an animation is running
	def hosthex = convertIPtoHex(ip);
    def porthex = convertPortToHex(port);
    def target = "$hosthex:$porthex";
    device.deviceNetworkId = target;
    
	byte[] byteHeader = [0x31, 0x00, 0x00, 0x00]
    byte[] byteFooter = [0x0F, 0x0F]
    
    int warmWhite = getWarmWhite().toInteger()
    int coolWhite = getCoolWhite().toInteger()
    def level = getLevel()
    log.debug "${warmWhite}:${coolWhite}@${level}"
    
    String bodyHeader = byteHeader.encodeHex()
    String bodyFooter = byteFooter.encodeHex()
    String bodyMain = bodyHeader + hex(warmWhite * level/100) + hex(coolWhite * level/100) + bodyFooter
    
    def byteMain = bodyMain.decodeHex()
    def checksum = 0
    
    byteMain.each {
    	checksum += it;
    }
    checksum = checksum & 0xFF
    String checksumHex = Integer.toHexString(checksum)
    //log.debug "${checksum}:${checksumHex}"
    
    String body = bodyMain + checksumHex
    
    sendHubCommand(new physicalgraph.device.HubAction(body.toString(), physicalgraph.device.Protocol.LAN, getDataValue("mac"))); //"0A0A0A15:15C9"
    
}

def refresh() {
	sendStatus()
}

def parseStatus(physicalgraph.device.HubResponse hubResponse) {
	log.debug "Entered parseStatus()..."
    def body = hubResponse.body
    def desc = hubResponse.description
    def xml = hubResponse.xml
    def json = hubResponse.json
    def data = hubResponse?.data
    def headers  = hubResponse.headers 
    def error = hubResponse.error
    log.debug "hubResponse:${hubResponse}"
    log.debug "body:${body}"
    log.debug "desc:${desc}"
    log.debug "xml:${xml}"
    log.debug "json:${json}"
    log.debug "data:${data}"
    log.debug "headers:${headers}"
    log.debug "error:${error}"
    // No entries actually contain the payload that is returned
}

def sendStatus() {  // To request Status of current Bulb
    def hosthex = convertIPtoHex(ip);
    def porthex = convertPortToHex(port);
    def target = "$hosthex:$porthex";
    device.deviceNetworkId = target;
    
    byte[] byteHeader = [0x81, 0x8A, 0x8B]
    
    String bodyHeader = byteHeader.encodeHex()
    String bodyMain = bodyHeader
    
    def byteMain = bodyMain.decodeHex()
    def checksum = 0
    
    byteMain.each {
    	checksum += it;
    }
    checksum = checksum & 0xFF
    String checksumHex = Integer.toHexString(checksum)
    log.debug "Sending Status Request"
    
    String body = bodyMain + checksumHex
    
    byte[] bytesLW12 = [0xEF, 0x01, 0x77]
    if (CStyle == "RGB-LW12")
    {
		body = bytesLW12.encodeHex()
	}
    
	sendHubCommand(new physicalgraph.device.HubAction(body.toString(), physicalgraph.device.Protocol.LAN, getDataValue("mac"), [callback: parseStatus]));
    
}

def setLevel(level) {
	log.trace "setLevel($level)"
    
	if (level == 0) { off() }
	else if (getSwitch() == "off") { on() }
    
    def colorMap = [hex: getColor(), level: level]
	setColor(colorMap)
}

def setCoolWhite(level) {
	log.trace "setCoolWhite($level)"
       
    sendEvent(name: "coolWhite", value: level)
	sendWhites()
}

def setWarmWhite(level) {
	log.trace "setWarmWhite($level)"
       
    sendEvent(name: "warmWhite", value: level)
	sendWhites()
}

def setRed(level) {
	log.trace "setRed($level)"
    
    def changed = hex(level)
    def hex = getColor()
    def hexColor = hex.take(1) + changed + hex.substring(3)
    
	def colorMap = [hex: hexColor]
	setColor(colorMap)
}

def setGreen(level) {
	log.trace "setGreen($level)"
    
    def changed = hex(level)
    def hex = getColor()
    def hexColor = hex.take(3) + changed + hex.substring(5)
    
	def colorMap = [hex: hexColor]
	setColor(colorMap)
}

def setBlue(level) {
	log.trace "setBlue($level)"
    
    def changed = hex(level)
    def hex = getColor()
    def hexColor = hex.take(5) + changed
    
	def colorMap = [hex: hexColor]
	setColor(colorMap)
}

def setSaturation(percent) {
	log.debug "Executing 'setSaturation'"
	sendEvent(name: "saturation", value: percent)
    def colorMap = [hue: getHue() as Integer, saturation: getSaturation() as Integer]
	setColor(colorMap)
}

def setHue(percent) {
	log.debug "Executing 'setHue'"
	sendEvent(name: "hue", value: percent)
    def colorMap = [hue: getHue() as Integer, saturation: getSaturation() as Integer]
	setColor(colorMap)
}

def setColor(value) {
	log.debug "setColor: ${value}"
    
    if (value.size() < 8)
    	toggleTiles("off")

    if (( value.size() == 2) && (value.hue != null) && (value.saturation != null)) { //assuming we're being called from outside of device (App)
    	def rgb = hslToRGB(value.hue, value.saturation, 0.5)
        def level = getLevel()
        value.hex = rgbToHex(rgb)
        value.rh = hex(rgb.r * level/100)
        value.gh = hex(rgb.g * level/100)
        value.bh = hex(rgb.b * level/100)
    }
    
    if ((value.size() == 3) && (value.hue != null) && (value.saturation != null) && (value.level)) { //user passed in a level value too from outside (App)
    	def rgb = hslToRGB(value.hue, value.saturation, value.level)
        def level = getLevel()
        value.hex = rgbToHex(rgb)
        value.rh = hex(rgb.r * level/100)
        value.gh = hex(rgb.g * level/100)
        value.bh = hex(rgb.b * level/100)       
    }
    
    if (( value.size() == 3) && (value.hue != null) && (value.saturation != null) && (value.hex != null)) { //assuming we're being called from outside of device (App)
    	def rgb = hslToRGB(value.hue, value.saturation, 0.5)
        def level = getLevel()
        value.hex = rgbToHex(rgb)
        value.rh = hex(rgb.r * level/100)
        value.gh = hex(rgb.g * level/100)
        value.bh = hex(rgb.b * level/100)
    }
    
    if (( value.size() == 4) && (value.hue != null) && (value.saturation != null) && (value.level) && (value.hex != null)) { //assuming we're being called from outside of device (App AKA-WebCore)
     	def rgb = hslToRGB(value.hue, value.saturation, value.level)
         def level = getLevel()
         value.hex = rgbToHex(rgb)
         value.rh = hex(rgb.r * level/100)
         value.gh = hex(rgb.g * level/100)
         value.bh = hex(rgb.b * level/100)
     }
     
     if (( value.size() == 1) && (value.hex)) { //being called from outside of device (App) with only hex
		def rgbInt = hexToRgb(value.hex)
        def level = getLevel()
        value.rh = hex(rgbInt.r * level/100)
        value.gh = hex(rgbInt.g * level/100)
        value.bh = hex(rgbInt.b * level/100)
    }
    
    if (( value.size() == 2) && (value.hex) && (value.level)) { //being called from outside of device (App) with only hex and level
		def rgbInt = hexToRgb(value.hex)
        sendEvent(name: "level", value: value.level)
        value.rh = hex(rgbInt.r * value.level/100)
        value.gh = hex(rgbInt.g * value.level/100)
        value.bh = hex(rgbInt.b * value.level/100)
    }
    
    if (( value.size() == 1) && (value.colorName)) { //being called from outside of device (App) with only color name
        def colorData = getColorData(value.colorName)
        setColor(colorData)
        return
    }
    
    if (( value.size() == 2) && (value.colorName) && (value.level)) { //being called from outside of device (App) with only color name and level
		def colorData = getColorData(value.colorName)
        sendEvent(name: "level", value: value.level)
        setColor(colorData)
        return
    }
    
    if (( value.size() == 3) && (value.red != null) && (value.green != null) && (value.blue != null)) { //being called from outside of device (App) with only color values (0-255)
        def level = getLevel()
        value.rh = hex(value.red * level/100)
        value.gh = hex(value.green * level/100)
        value.bh = hex(value.blue * level/100)
        value.hex = "#${value.rh}${value.gh}${value.bh}"
    }

    if (( value.size() == 4) && (value.red != null) && (value.green != null) && (value.blue != null) && (value.level)) { //being called from outside of device (App) with only color values (0-255) and level
        sendEvent(name: "level", value: value.level)
        value.rh = hex(value.red * value.level/100)
        value.gh = hex(value.green * value.level/100)
        value.bh = hex(value.blue * value.level/100)
        value.hex = "#${hex(value.red)}${hex(value.green)}${hex(value.blue)}"
    }
    if (!value.rh && !value.gh && !value.bh) {
    	def level = getLevel()
        value.rh = hex(value.red * level/100)
        value.gh = hex(value.green * level/100)
        value.bh = hex(value.blue * level/100)
    }
    
	sendEvent(name: "hue", value: value.hue, displayed: false)
	sendEvent(name: "saturation", value: value.saturation, displayed: false)
	sendEvent(name: "color", value: value.hex, displayed: false)
	if (value.level) {
		sendEvent(name: "level", value: value.level)
	}
	if (value.switch) {
		sendEvent(name: "switch", value: value.switch)
	}
	sendEvent(name: "rl", value: Integer.parseInt(value.rh,16))   
    sendEvent(name: "gl", value: Integer.parseInt(value.gh,16))   
    sendEvent(name: "bl", value: Integer.parseInt(value.bh,16))   
    
    sendRGB()
}

def setAdjustedColor(value) {
	log.debug "setAdjustedColor: ${value}"
    
    toggleTiles("off") //turn off the hard color tiles

    def level = getLevel()
    value.level = level

	def c = hexToRgb(value.hex) 
	value.rh = hex(c.r * (level/100))
	value.gh = hex(c.g * (level/100))
	value.bh = hex(c.b * (level/100))
    value.hex = "#${value.rh}${value.gh}${value.bh}"
	
    setColor(value)  
}

private hubGet(def apiCommand) {
	//Setting Network Device Id
    def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(port)
    device.deviceNetworkId = "$iphex:$porthex"
    log.debug "Device Network Id set to ${iphex}:${porthex}"

	log.debug("Executing hubaction on " + getHostAddress())
    def uri = ""
    if(hdcamera == "true") {
    	uri = "/cgi-bin/CGIProxy.fcgi?" + getLogin() + apiCommand
	}
    else {
    	uri = apiCommand + getLogin()
    }
    log.debug uri
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
        path: uri,
        headers: [HOST:getHostAddress()]
    )
    if(device.currentValue("hubactionMode") == "s3") {
        hubAction.options = [outputMsgToS3:true]
        sendEvent(name: "hubactionMode", value: "local");
    }
	hubAction
}

//Parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

private getHostAddress() {
	return "${ip}:${port}"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

// Build random block of 32bit integers
byte[] buildRandomData(size) {
  ByteArrayOutputStream out = new ByteArrayOutputStream();    
  writeInt(out, (int)Math.floor(new Date().getTime() / 1000), 32);
  for(def i = 0; i < size - 4; i++)
    out.write((int)(Math.random() * 0xFF));
  out.flush();
  return out.toByteArray();
}

// Write int of varying bit-size (8bit, 16bit, 24bit, 32bit, etc)
public static void writeInt(ByteArrayOutputStream out, int value, int bits) {
  for(def i = bits - 8; i >= 0; i-=8) {
    out.write((byte) (0xFF & (value >> i)));
  }
}

// Return hex-string interpretation of byte array
public static String bytesToHex(byte[] bytes) {
  final char[] hexArray = "0123456789ABCDEF".toCharArray();
  char[] hexChars = new char[bytes.length * 2];
  for ( int j = 0; j < bytes.length; j++ ) {
    int v = bytes[j] & 0xFF;
    hexChars[j * 2] = hexArray[v >>> 4];
    hexChars[j * 2 + 1] = hexArray[v & 0x0F];
  }
  return new String(hexChars);
}

def colorNameToRgb(color) {

	final colors = [
        [name:"Soft White",	r: 255, g: 241, b: 224	],
        [name:"Daylight", 	r: 255, g: 255, b: 251	],
        [name:"Warm White", r: 255, g: 244, b: 229	],
        
        [name:"Red", 		r: 255, g: 0,	b: 0	],
		[name:"Green", 		r: 0, 	g: 255,	b: 0	],
        [name:"Blue", 		r: 0, 	g: 0,	b: 255	],
        
		[name:"Cyan", 		r: 0, 	g: 255,	b: 255	],
        [name:"Magenta", 	r: 255, g: 0,	b: 33	],       
        [name:"Orange", 	r: 255, g: 102, b: 0	],
        
        [name:"Purple", 	r: 170, g: 0,	b: 255	],
		[name:"Yellow", 	r: 255, g: 255, b: 0	],
		[name:"Black", 		r: 0, 	g: 0, 	b: 0	],
        [name:"White", 		r: 255, g: 255, b: 255	]
	]
    
    def colorData = [:]    
    colorData = colors.find { it.name == color }
    
    colorData
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

def hexToRgb(colorHex) {
	def rrInt = Integer.parseInt(colorHex.substring(1,3),16)
    def ggInt = Integer.parseInt(colorHex.substring(3,5),16)
    def bbInt = Integer.parseInt(colorHex.substring(5,7),16)
    
    def colorData = [:]
    colorData = [r: rrInt, g: ggInt, b: bbInt]
    colorData
}

def rgbToHex(rgb) {
    def r = hex(rgb.r)
    def g = hex(rgb.g)
    def b = hex(rgb.b)
    def hexColor = "#${r}${g}${b}"
    
    hexColor
}

/**
 * Converts an HSL color value to RGB. Conversion formula
 * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
 * Assumes h, s, and l are contained in the set [0, 1] and
 * returns r, g, and b in the set [0, 255].
 *
 * @param h       The hue
 * @param s       The saturation
 * @param l       The lightness
 * @return int array, the RGB representation
 */
def hslToRGB(double h, double s, double l){
    double r, g, b;
    //log.debug "hsl ${h}, ${s}, ${l}"
    if (h>1) { h = h/100f }
    if (s>1) { s = s/100f }
    if (l>1) { l = l/100f }
    //log.debug "hsl-adj ${h}, ${s}, ${l}"

    if (s == 0f) {
        r = g = b = l; // achromatic
    } else {
        double q = l < 0.5f ? l * (1 + s) : l + s - l * s;
        double p = 2 * l - q;
        log.debug "pqh ${p}, ${q}, ${h}"
        r = hueToRgb(p, q, h + 1f/3f);
        g = hueToRgb(p, q, h);
        b = hueToRgb(p, q, h - 1f/3f);
    }
    //log.debug "rgb-first ${r}, ${g}, ${b}"
    int ri = (r * 255)
    int gi = (g * 255)
    int bi = (b * 255)
    //log.debug "rgb-final ${ri}, ${gi}, ${bi}"
    def rgb = [:]
    rgb = [r: ri, g: gi, b: bi]
    rgb;
}

/** Helper method that converts hue to rgb */
def hueToRgb(double p, double q, double t) {
	//log.debug "pqt ${p}, ${q}, ${t}"
    if (t < 0f)
        t += 1f;
    if (t > 1f)
        t -= 1f;
    if (t < 1f/6f)
        return p + (q - p) * 6f * t;
    if (t < 1f/2f)
        return q;
    if (t < 2f/3f)
        return p + (q - p) * (2f/3f - t) * 6f;
    return p;
}

def rgbToHSL(rgb) {
	def r = rgb.r / 255
    def g = rgb.g / 255
    def b = rgb.b / 255
    def h = 0
    def s = 0
    def l = 0
    
    def var_min = [r,g,b].min()
    def var_max = [r,g,b].max()
    def del_max = var_max - var_min
    
    l = (var_max + var_min) / 2
    
    if (del_max == 0) {
            h = 0
            s = 0
    } else {
    	if (l < 0.5) { s = del_max / (var_max + var_min) } 
        else { s = del_max / (2 - var_max - var_min) }

        def del_r = (((var_max - r) / 6) + (del_max / 2)) / del_max
        def del_g = (((var_max - g) / 6) + (del_max / 2)) / del_max
        def del_b = (((var_max - b) / 6) + (del_max / 2)) / del_max

        if (r == var_max) { h = del_b - del_g } 
        else if (g == var_max) { h = (1 / 3) + del_r - del_b } 
        else if (b == var_max) { h = (2 / 3) + del_g - del_r }
        
		if (h < 0) { h += 1 }
        if (h > 1) { h -= 1 }
	}
    def hsl = [:]    
    hsl = [h: h * 100, s: s * 100, l: l]
    
    hsl
}

def getColorData(colorName) {
	log.debug "getColorData: ${colorName}"
    
    def colorRGB = colorNameToRgb(colorName)
    def colorHex = rgbToHex(colorRGB)
	def colorHSL = rgbToHSL(colorRGB)
    def level = getLevel()
        
    def colorData = [:]
    colorData = [hue: colorHSL.h, 
    			 saturation: colorHSL.s, 
                 level: level, 
                 red: colorRGB.r, 
                 green: colorRGB.g,
                 blue: colorRGB.b,
                 rh: hex(colorRGB.r * (level/100)),
                 gh: hex(colorRGB.g * (level/100)),
                 bh: hex(colorRGB.b * (level/100)),
                 hex: colorHex,
                 alpha: 1]
     
     colorData                 
}

def doColorButton(colorName) {
    log.debug "doColorButton: '${colorName}()'"

    if (getSwitch() == "off") { on() }

    def level = getLevel()

    toggleTiles(colorName.toLowerCase().replaceAll("\\s",""))
    
    if (colorName.take(1) == "a") {
    	doAnimations (colorName.substring(1))
	}
	else {
        def c = getColorData(colorName)
        setColor(c)
    }
}

def doUserButton(UserNumber) {
    log.debug "doUserButton: '${UserNumber}()'"

    if (getSwitch() == "off") { on() }

    int warmWhite = getWarmWhite().toInteger()
    int coolWhite = getCoolWhite().toInteger()
    def level = getLevel()
    
    sendEvent(name: "warmWhite", value: warmWhite)
	sendEvent(name: "coolWhite", value: coolWhite)
    
    toggleTiles(UserNumber.toLowerCase().replaceAll("\\s",""))
    
    def c = getColorData(UserNumber)
    setColor(c)
}

def toggleTiles(color) {
	state.colorTiles = []
	if ( !state.colorTiles ) {
    	state.colorTiles = ["softwhite","daylight","warmwhite","red","green","blue","cyan","magenta","orange","purple","yellow","black","white","Fade7","Strobe7","Jump7","FadeRed","StrobeRed","FadeGreen","StrobeGreen","FadeBlue","StrobeBlue","user1","user2","user3"]
    }
    
    def cmds = []
    
    state.colorTiles.each({
    	if ( it == color ) {
        	log.debug "Turning ${it} on"
            device.displayName + " was closed"
            cmds << sendEvent(name: it, value: "on${it}", display: True, descriptionText: "${device.displayName} ${color} is 'ON'", isStateChange: true)
        } else {
        	//log.debug "Turning ${it} off"
        	cmds << sendEvent(name: it, value: "off${it}", displayed: false)
        }
    })
    
    delayBetween(cmds, 2500)
}

def setSpeed(level) {
	log.trace "setSpeed($level)"
    
    sendEvent(name: "speed", value: level)
}

def doAnimations(animation) {
	log.trace "doAnimations($animation)"
    def hosthex = convertIPtoHex(ip);
    def porthex = convertPortToHex(port);
    def target = "$hosthex:$porthex";
    device.deviceNetworkId = target;
    
	byte[] byteHeader = [0x61]
    byte[] byteFooter = [0x0F]
    byte[] commandSpeed= [0x10] 
    
    def speed = getSpeed().toInteger()
    
    if (speed > 0) {
    	def var1 = (speed-100)
        def var2 = var1 * (-1f/3f)
        def var3 = (Math.round(var2)-1)
        commandSpeed[0] = var3.abs()
    }
    else {
    	commandSpeed[0] = 16
    }
    String commandSpeedStr = commandSpeed.encodeHex()
    // speed guess in decimal: =ABS(ROUND((speed-100)*(-1f/3f),0)-1) 
    
    def animationFound = animationSwitch(animation)
    String commandStr
    if (animationFound?.command) { commandStr = animationFound.command } else { commandStr = '25' }
    
    log.debug "Animate ${animation}:${commandStr}@${speed}%=${commandSpeed[0]}:${commandSpeedStr}"
    String bodyHeader = byteHeader.encodeHex()
    String bodyFooter = byteFooter.encodeHex()
    String bodyMain = bodyHeader + commandStr + commandSpeedStr + bodyFooter
    
    def byteMain = bodyMain.decodeHex()
    def checksum = 0
    
    byteMain.each {
    	checksum += it;
    }
    checksum = checksum & 0xFF
    String checksumHex = Integer.toHexString(checksum)
    
    String body = bodyMain + checksumHex
    
    sendHubCommand(new physicalgraph.device.HubAction(body.toString(), physicalgraph.device.Protocol.LAN, getDataValue("mac"))); //"0A0A0A15:15C9"
    
}

def animationSwitch(val) {

	final animations = [
        [name:"Fade7",		command: '25'],
        [name:"FadeRed",	command: '26'],
        [name:"FadeGreen",	command: '27'],
        [name:"FadeBlue",	command: '28'],
        [name:"FadeYellow",	command: '29'],
        [name:"FadeCyan",	command: '2a'],
        [name:"FadePurple",	command: '2b'],
        [name:"FadeWhite",	command: '2c'],
        [name:"FadeRedGreen",	command: '2d'],
        [name:"FadeRedBlue",	command: '2e'],
        [name:"FadeGreenBlue",	command: '2f'],
        [name:"Strobe7",	command: '30'],
        [name:"StrobeRed",	command: '31'],
        [name:"StrobeGreen",	command: '32'],
        [name:"StrobeBlue",	command: '33'],
        [name:"StrobeYellow",	command: '34'],
        [name:"StrobeCyan",	command: '35'],
        [name:"StrobePurple",	command: '36'],
        [name:"StrobeWhite",	command: '37'],
        [name:"Jump7",		command: '38'],
	]
	
    def animationData = [:]    
    animationData = animations.find { it.name == val }
    
    animationData
}

// rows of buttons
def softwhite() { doColorButton("Soft White") }
def daylight()  { doColorButton("Daylight") }
def warmwhite() { doColorButton("Warm White") }

def red() 		{ doColorButton("Red") }
def green() 	{ doColorButton("Green") }
def blue() 		{ doColorButton("Blue") }

def cyan() 		{ doColorButton("Cyan") }
def magenta()	{ doColorButton("Magenta") }
def orange() 	{ doColorButton("Orange") }

def purple()	{ doColorButton("Purple") }
def yellow() 	{ doColorButton("Yellow") }
def black() 	{ doColorButton("Black") }
def white() 	{ doColorButton("White") }

def Fade7() 	{ doColorButton("aFade7") }
def Strobe7() 	{ doColorButton("aStrobe7") }
def Jump7() 	{ doColorButton("aJump7") }

def FadeRed() 		{ doColorButton("aFadeRed") }
def StrobeRed() 	{ doColorButton("aStrobeRed") }

def FadeGreen() 	{ doColorButton("aFadeGreen") }
def StrobeGreen() 	{ doColorButton("aStrobeGreen") }

def FadeBlue() 		{ doColorButton("aFadeBlue") }
def StrobeBlue() 	{ doColorButton("aStrobeBlue") }

def FadeWhite() 	{ doColorButton("aFadeWhite") }
def StrobeWhite() 	{ doColorButton("aStrobeWhite") }

def user1()		{ refresh() }
def user2()		{ doUserButton("White") }
def user3()		{ doUserButton("White") }