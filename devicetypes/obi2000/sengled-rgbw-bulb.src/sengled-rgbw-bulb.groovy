/**
 *  Copyright 2017 SmartThings
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
 *  Author: SmartThings
 *  Date: 2016-01-19
 *
 *  This DTH should serve as the generic DTH to handle RGBW ZigBee HA devices
 */
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition (name: "Sengled RGBW Bulb", namespace: "Obi2000", author: "Obi2000") {

        capability "Actuator"
        capability "Color Control"
        capability "Color Temperature"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"
        capability "Health Check"
        capability "Light"

        attribute "colorName", "string"
        command "setGenericName"

        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0702,0B05,FC03", outClusters: "0019", manufacturer: "sengled", model: "E11-N1EA", deviceJoinName: "Sengled Element RGBW Bulb"

    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action:"color control.setColor"
            }
        }
        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2700..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }
        valueTile("colorName", "device.colorName", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "colorName", label: '${currentValue}'
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["switch"])
        details(["switch", "colorTempSliderControl", "colorName", "refresh"])
    }
}

//Globals
private getATTRIBUTE_HUE() { 0x0000 }
private getATTRIBUTE_SATURATION() { 0x0001 }
private getATTRIBUTE_X() { 0x0003 }
private getATTRIBUTE_Y() { 0x0004 }
private getHUE_COMMAND() { 0x00 }
private getSATURATION_COMMAND() { 0x03 }
private getMOVE_TO_HUE_AND_SATURATION_COMMAND() { 0x06 }
private getCOLOR_CONTROL_CLUSTER() { 0x0300 }
private getATTRIBUTE_COLOR_TEMPERATURE() { 0x0007 }

def logDebug(msg) {
	// log.debug msg
}

def logTrace(msg) {
	// log.trace msg
}



// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description is $description"

    def event = zigbee.getEvent(description)
    if (event) {
        log.debug event
        if (event.name=="level" && event.value==0) {}
        else {
            if (event.name=="colorTemperature") {
                setGenericName(event.value)
            }
            sendEvent(event)
        }
    }
    else {
        def zigbeeMap = zigbee.parseDescriptionAsMap(description)
        def cluster = zigbee.parse(description)

        if (zigbeeMap?.clusterInt == COLOR_CONTROL_CLUSTER) {
            if(zigbeeMap.attrInt == ATTRIBUTE_X){  //Yxy X Attribute
                state.X = zigbee.convertHexToInt(zigbeeMap.value) / 65536
                state.colorXReported = true
                updateColor()
            }
            else if(zigbeeMap.attrInt == ATTRIBUTE_Y){ //Yxy Y Attribute
                state.Y= zigbee.convertHexToInt(zigbeeMap.value) / 65536
                state.colorYReported = true
                updateColor()
            }
        }
        else if (cluster && cluster.clusterId == 0x0006 && cluster.command == 0x07) {
            if (cluster.data[0] == 0x00){
                log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
                sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
            }
            else {
                log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
            }
        }
        else {
            log.info "DID NOT PARSE MESSAGE for description : $description"
            log.debug zigbeeMap
        }
    }
}

def updateColor() {
if (state.colorXReported == true  && state.colorYReported == true)
	{
    	state.colorXReported = false
    	state.colorYReported = false

		def rgb = colorXy2Rgb(state.X, state.Y)
//	  log.debug  Math.round(rgb.red * 255).intValue()
//	  log.debug  Math.round(rgb.green * 255).intValue()
//	  log.debug  Math.round(rgb.blue * 255).intValue()
	    def hsv = colorRgb2Hsv(rgb.red, rgb.green, rgb.blue)
	    hsv.hue = Math.round(hsv.hue * 100).intValue()
	  	hsv.saturation = Math.round(hsv.saturation * 100).intValue()
	  	hsv.level = Math.round(hsv.level * 100).intValue()
		sendEvent(name: "hue", value: hsv.hue, descriptionText: "Color has changed")
		sendEvent(name: "saturation", value: hsv.saturation, descriptionText: "Color has changed", displayed: false)
		
        sendEvent(name: "colorName", value: getColorName(hsv.hue,hsv.saturation))
	}
}

def on() {
    zigbee.on()
}

def off() {
    zigbee.off()
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return zigbee.onOffRefresh()
}

def refresh() {
  state.colorXReported = false
  state.colorYReported = false
    zigbee.onOffRefresh() +
    zigbee.levelRefresh() +
    zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_COLOR_TEMPERATURE) +
    zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_X) +
    zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_Y) +
    zigbee.onOffConfig(0, 300) +
    zigbee.levelConfig()
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    // OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
    refresh()
}

def setColorTemperature(value) {
    setGenericName(value)
    value = value as Integer
    def tempInMired = (1000000 / value) as Integer
    def finalHex = zigbee.swapEndianHex(zigbee.convertToHexString(tempInMired, 4))

    zigbee.command(COLOR_CONTROL_CLUSTER, 0x0A, "$finalHex 0000") +
    zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_COLOR_TEMPERATURE)
}

//Naming based on the wiki article here: http://en.wikipedia.org/wiki/Color_temperature
def setGenericName(value){
    if (value != null) {
        def genericName = "White"
        if (value < 3300) {
            genericName = "Soft White"
        } else if (value < 4150) {
            genericName = "Moonlight"
        } else if (value <= 5000) {
            genericName = "Cool White"
        } else if (value >= 5000) {
            genericName = "Daylight"
        }
        sendEvent(name: "colorName", value: genericName)
    }
}

def setLevel(value) {
    zigbee.setLevel(value)
}

private getScaledHue(value) {
    zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
}

private getScaledSaturation(value) {
    zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
}

def setColor(red, green, blue) {
  logDebug "setColor: RGB ($red, $green, $blue)"

   
  def xy = colorRgb2Xy(red, green, blue);
  
  logTrace "setColor: xy ($xy.x, $xy.y)"
  
  def intX = Math.round(xy.x*65536).intValue() // 0..65279
  def intY = Math.round(xy.y*65536).intValue() // 0..65279
  
  logTrace "setColor: xy ($intX, $intY)"

  def strX = DataType.pack(intX, DataType.UINT16, 1);
  def strY = DataType.pack(intY, DataType.UINT16, 1);
  
  zigbee.command(0x0300, 0x07, strX, strY, "0a00")+
  zigbee.readAttribute(COLOR_CONTROL_CLUSTER, 0x0003) +
  zigbee.readAttribute(COLOR_CONTROL_CLUSTER, 0x0004) 
}

def setColor(Map colorMap) {
  state.colorXReported = false
  state.colorYReported = false
  
  logDebug "setColor: $colorMap"
 
  def rgb
  
  if(colorMap.containsKey("red") && colorMap.containsKey("green") && colorMap.containsKey("blue")) {
    rgb = [ red : colorMap.red.intValue() / 255, green: colorMap.green.intValue() / 255, blue: colorMap.blue.intValue() / 255 ]
  }
  else if(colorMap.containsKey("hue") && colorMap.containsKey("saturation")) {
  	rgb = colorHsv2Rgb(colorMap.hue / 100, colorMap.saturation / 100)
  }
  else {
    log.warn "Unable to set color $colorMap"
  }

  logTrace "setColor: RGB ($red, $green, $blue)"
  
  setColor(rgb.red, rgb.green, rgb.blue)
}

def setHue(hue) {
  logDebug "setHue: $hue"
  setColor([ hue: hue, saturation: device.currentValue("saturation") ])
}

def setSaturation(saturation) {
  logDebug "setSaturation: $saturation"
  setColor([ hue: device.currentValue("hue"), saturation: saturation ])
}

def installed() {
        if ((device.currentState("level")?.value == null) || (device.currentState("level")?.value == 0)) {
            sendEvent(name: "level", value: 100)
        }
}


def colorRgb2Xy(r, g, b) {

  logTrace "> Color RGB: ($r, $g, $b)"
  
  r = colorGammaAdjust(r)
  g = colorGammaAdjust(g)
  b = colorGammaAdjust(b)

  // sRGB, Reference White D65
  // D65	0.31271	0.32902
  //  R  0.64000 0.33000
  //  G  0.30000 0.60000
  //  B  0.15000 0.06000
  def M = [
	[  0.649926,  0.103455,  0.197109 ],
	[  0.234327,  0.743075,  0.022598 ],
	[  0.0000000,  0.053077,  1.035763 ]
  ]

  def X = r * M[0][0] + g * M[0][1] + b * M[0][2]
  def Y = r * M[1][0] + g * M[1][1] + b * M[1][2]
  def Z = r * M[2][0] + g * M[2][1] + b * M[2][2]
  
 logTrace "> Color XYZ: ($X, $Y, $Z)"
  
  def x = X / (X + Y + Z)
  def y = Y / (X + Y + Z)
  
  logTrace "> Color xy: ($x, $y)"

  [x: x, y: y]
}


def colorGammaAdjust(component) {
  return (component > 0.04045) ? Math.pow((component + 0.055) / (1.0 + 0.055), 2.4) : (component / 12.92)
}


def colorHsv2Rgb(h, s) {
	logTrace "< Color HSV: ($h, $s, 1)"
    
	def r
    def g
    def b
    
    if (s == 0) {
        r = 1
        g = 1
        b = 1
    }
    else {
        def region = (6 * h).intValue()
        def remainder = 6 * h - region

        def p = 1 - s
        def q = 1 - s * remainder
        def t = 1 - s * (1 - remainder)

		if(region == 0) {
            r = 1
            g = t
            b = p
        }
        else if(region == 1) {
            r = q
            g = 1
            b = p
        }
        else if(region == 2) {
            r = p
            g = 1
            b = t
        }
        else if(region == 3) {
            r = p
            g = q
            b = 1
        }
        else if(region == 4) {
            r = t
            g = p
            b = 1
        }
        else {
            r = 1
            g = p
            b = q
        }
	}
    
	logTrace "< Color RGB: ($r, $g, $b)"
  
	[red: r, green: g, blue: b]
}




def colorXy2Rgb(cordx, cordy) {

//  log.debug "< Color xy: ($cordx, $cordy)"
  
  def Y = 1;
  def X = (Y / cordy) * cordx;
  def Z = (Y / cordy) * (1.0 - cordx - cordy);  

//  log.debug "< Color XYZ: ($X, $Y, $Z)"

  
  
  
  // sRGB, Reference White D65
def M = [
	[	1.6117568186730657,		-0.2028048928758147,	-0.30229771656510396],
	[	-0.5090571453688465,	1.4119135834154535,		0.06607044440522163],
	[	0.02608630169714719,	-0.07235259153584557,	0.9620860940411118]
]

  def r = X * M[0][0] + Y * M[0][1] + Z * M[0][2]
  def g = X * M[1][0] + Y * M[1][1] + Z * M[1][2]
  def b = X * M[2][0] + Y * M[2][1] + Z * M[2][2]

  def max = max(r, g, b)
  r = colorGammaRevert(r / max)
  g = colorGammaRevert(g / max)
  b = colorGammaRevert(b / max)
  
  logTrace "< Color RGB: ($r, $g, $b)"
  
  [red: r, green: g, blue: b]
}



def min(first, ... rest) {
  def min = first;
  for(next in rest) {
    if(next < min) min = next
  }
  
  min
}

def max(first, ... rest) {
  def max = first;
  for(next in rest) {
    if(next > max) max = next
  }
  
  max
}



def colorGammaRevert(component) {
  return (component <= 0.0031308) ? 12.92 * component : (1.0 + 0.055) * Math.pow(component, (1.0 / 2.4)) - 0.055;
}



def colorRgb2Hsv(r, g, b)
{
	logTrace "> Color RGB: ($r, $g, $b)"
  
	def min = min(r, g, b)
	def max = max(r, g, b)
	def delta = max - min
    
    def h
    def s
    def v = max

    if (delta == 0) {
    	h = 0
        s = 0
    }
    else {
		s = delta / max
        if (r == max) h = ( g - b ) / delta			// between yellow & magenta
		else if(g == max) h = 2 + ( b - r ) / delta	// between cyan & yellow
		else h = 4 + ( r - g ) / delta				// between magenta & cyan
        h /= 6

		if(h < 0) h += 1
    }

    logTrace "> Color HSV: ($h, $s, $v)"
    
    return [ hue: h, saturation: s, level: v ]
}



//input Hue Integer values; returns color name for saturation 100%
private getColorName(hueValue,satValue){
    if(hueValue>360 || hueValue<0)
        return

    hueValue = Math.round(hueValue / 100 * 360)

 //   log.debug "hue value is $hueValue"

    def colorName = "Color Mode"
    if(satValue <= 10){
        colorName = "White"
    }
    else if (hueValue>=301 && satValue <= 50){
        colorName = "Pink"
    }
    else if (hueValue>=252 && hueValue <=256 && satValue <= 55){
        colorName = "Lavender"
    }
    else if (hueValue>=327 && hueValue <=335  && satValue <= 59){
        colorName = "Hot Pink"
    }
    else if (hueValue>=0 && hueValue <= 4){
        colorName = "Red"
    }
    else if (hueValue>=5 && hueValue <=21 ){
        colorName = "Brick Red"
    }
    else if (hueValue>=22 && hueValue <=30 ){
        colorName = "Safety Orange"
    }
    else if (hueValue>=31 && hueValue <=34 ){
        colorName = "Dark Orange"
    }
    else if (hueValue>=35 && hueValue <=40 ){
        colorName = "Orange"
    }
    else if (hueValue>=41 && hueValue <=49 ){
        colorName = "Amber"
    }
    else if (hueValue>=50 && hueValue <=56 ){
        colorName = "Gold"
    }
    else if (hueValue>=57 && hueValue <=65 ){
        colorName = "Yellow"
    }
    else if (hueValue>=66 && hueValue <=83 ){
        colorName = "Electric Lime"
    }
    else if (hueValue>=84 && hueValue <=93 ){
        colorName = "Lawn Green"
    }
    else if (hueValue>=94 && hueValue <=112 ){
        colorName = "Bright Green"
    }
    else if (hueValue>=113 && hueValue <=135 ){
        colorName = "Lime"
    }
    else if (hueValue>=136 && hueValue <=166 ){
        colorName = "Spring Green"
    }
    else if (hueValue>=167 && hueValue <=171 ){
        colorName = "Turquoise"
    }
    else if (hueValue>=172 && hueValue <=187 ){
        colorName = "Aqua"
    }
    else if (hueValue>=188 && hueValue <=203 ){
        colorName = "Sky Blue"
    }
    else if (hueValue>=204 && hueValue <=217 ){
        colorName = "Dodger Blue"
    }
    else if (hueValue>=218 && hueValue <=223 ){
        colorName = "Navy Blue"
    }
    else if (hueValue>=224 && hueValue <=251 ){
        colorName = "Blue"
    }
    else if (hueValue>=252 && hueValue <=256 ){
        colorName = "Han Purple"
    }
    else if (hueValue>=257 && hueValue <=274 ){
        colorName = "Electric Indigo"
    }
    else if (hueValue>=275 && hueValue <=289 ){
        colorName = "Electric Purple"
    }
    else if (hueValue>=290 && hueValue <=300 ){
        colorName = "Orchid Purple"
    }
    else if (hueValue>=301 && hueValue <=315 ){
        colorName = "Magenta"
    }
    else if (hueValue>=316 && hueValue <=326 ){
        colorName = "Hot Pink"
    }
    else if (hueValue>=327 && hueValue <=335 ){
        colorName = "Deep Pink"
    }
    else if (hueValue>=336 && hueValue <=339 ){
        colorName = "Raspberry"
    }
    else if (hueValue>=340 && hueValue <=352 ){
        colorName = "Crimson"
    }
    else if (hueValue>=353 && hueValue <=360 ){
        colorName = "Red"
    }

    colorName
}