import groovy.transform.Field

@Field final Map      BLACK = [name: "Black", rgb: "#000000", h: 0, s: 0, l: 0]

@Field final IntRange PERCENT_RANGE = (0..100)

@Field final IntRange HUE_RANGE = PERCENT_RANGE
@Field final Integer  HUE_STEP = 5
@Field final IntRange SAT_RANGE = PERCENT_RANGE
@Field final Integer  SAT_STEP = 20
@Field final Integer  HUE_SCALE = 1000
@Field final Integer  COLOR_OFFSET = HUE_RANGE.getTo() * HUE_SCALE

@Field final IntRange COLOR_TEMP_RANGE = (2200..7000)
@Field final Integer  COLOR_TEMP_DEFAULT = COLOR_TEMP_RANGE.getFrom() + ((COLOR_TEMP_RANGE.getTo() - COLOR_TEMP_RANGE.getFrom())/2)
@Field final Integer  COLOR_TEMP_STEP = 50 // Kelvin
@Field final List     COLOR_TEMP_EXTRAS = []
@Field final List     COLOR_TEMP_LIST = buildColorTempList(COLOR_TEMP_RANGE, COLOR_TEMP_STEP, COLOR_TEMP_EXTRAS)

@Field final Map MODE = [
    COLOR:	"Color",
    WHITE:	"White",
    OFF: 	"Off"
]
metadata {
	//Based on work by Brett Sheleski for Tasomota-Power

	definition(name: "Better Simulated RGBW Bulb", namespace: "davindameron", ocfDeviceType: "oic.d.smartplug", author: "Davin Dameron", mnmn:"SmartThings", vid:"generic-rgbw-color-bulb") {
		capability "Polling"
		capability "Refresh"
		capability "Switch"
		capability "Color Control"
        capability "Color Temperature"
        capability "Switch Level"

        command "reload"
        command "updateStatus"
        command "ringpush"
     	command "loopOn"
    	command "loopOff"
    	command "setLoopRate", ["number"]
        
        attribute "colorLabel", "string"
        attribute "tempLabel", "string"
        attribute "dimmerLabel", "string"
       
	}

	// UI tile definitions
	tiles(scale: 2) {

	standardTile("switch", "device.switch", decoration: "flat", width: 3, height: 3, canChangeIcon: true) {
	    state "off", label:'${name}', action: "switch.on", icon: "st.Lighting.light11", backgroundColor:"#ffffff"
	    state "on", label:'${name}', action: "switch.off", icon: "st.Lighting.light11", backgroundColor:"#00a0dc"
	}        
	
	standardTile("refresh", "device.switch", width: 3, height: 3, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Refresh', action:"refresh", icon:"st.secondary.refresh"
		}
        
	controlTile("rgbSelector", "device.color", "color", height: 3, width: 2,
	            inactiveLabel: false) {
	    state "color", action: "color control.setColor", label:'Ring Color'
	}

	controlTile("levelSliderControl", "device.level", "slider",
            height: 3, width: 2) {
    	state "level", action:"switch level.setLevel", label:'Ring Level'
	}

    controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 2, height: 3, inactiveLabel: false, range: "(2200..7000)") {
        state "colorTemperature", action: "setColorTemperature", label:"Color Temp"
    }
    
	main "switch"
		details(["switch", "refresh", "ringswitch", "rgbSelector", "levelSliderControl", "colorTempSliderControl"])
	}

    
    preferences {
	input(name: "debugLogging", type: "boolean", title: "Turn on debug logging?", displayDuringSetup:true, required: false)
	}
}


def doLogging(value){
	def debugLogging = debugLogging ?: settings?.debugLogging ?: device.latestValue("debugLogging");
	if (debugLogging=="true")
	{
		doLogging value;
	}
}

def installed(){
	doLogging "installed()"
}

def updated(){
	doLogging "updated()"
}

def reload(){
	doLogging "reload()"
}

def poll() {
	doLogging "POLL"
}

def refresh() {
	doLogging "refresh()"
}




def setColorTemperature(kelvin) {
    doLogging "executing 'setColorTemperature' ${kelvin}K"
    sendEvent(name: "colorTemperature", value: kelvin)
}


def on(){
    setPower("on")
}

def off(){
    setPower("off")
}


def setPower(power){
	doLogging "Setting power to: $power [${device.currentValue("switch")}]"
    if(power != device.currentValue("switch"))
    {
		def on = power == "on";
    	setSwitchState(on);
    }

}

def setLevel(level){
	doLogging "Setting level to: $level"
    sendEvent(name:"level", value:level);

}



private Map buildColorHSMap(hue, saturation) {
	doLogging "Executing 'buildColorHSMap(${hue}, ${saturation})'"
    Map colorHSMap = [hue: 0, saturation: 0]
    try {
        colorHSMap.hue = hue.toFloat().toInteger()
        colorHSMap.saturation = saturation.toFloat().toInteger()
    } catch (NumberFormatException nfe) {
        doLogging "Couldn't transform one of hue ($hue) or saturation ($saturation) to integers: $nfe"
    }
    doLogging colorHSMap
    return colorHSMap
}

private List buildColorTempList(IntRange kRange, Integer kStep, List kExtras) {
    List colorTempList = [kRange.getFrom()] // start with range lower bound
    Integer kFirstNorm = kRange.getFrom() + kStep - (kRange.getFrom() % kStep) // find the first value within thr range which is a factor of kStep
    colorTempList += (kFirstNorm..kRange.getTo()).step(kStep) // now build the periodic list
    colorTempList << kRange.getTo() // include range upper bound
    colorTempList += kExtras // add in extra values
    return colorTempList.sort().unique() // sort and de-dupe
}

private Integer boundInt(Number value, IntRange theRange) {
    value = Math.max(theRange.getFrom(), value)
    value = Math.min(theRange.getTo(), value)
    return value.toInteger()
}

def setSaturation(saturationPercent) {
    doLogging "Executing 'setSaturation' ${saturationPercent}/100"
    Integer currentHue = device.currentValue("hue")
    setColor(currentHue, saturationPercent)
    // setColor will call done() for us
}

def setHue(huePercent) {
    doLogging "Executing 'setHue' ${huePercent}/100"
    Integer currentSaturation = device.currentValue("saturation")
    setColor(huePercent, currentSaturation)
    // setColor will call done() for us
}

def setColor(Integer huePercent, Integer saturationPercent) {
    doLogging "Executing 'setColor' from separate values hue: $huePercent, saturation: $saturationPercent"
    //Map colorHSMap = buildColorHSMap(huePercent, saturationPercent)
    setColor(buildColorHSMap(huePercent, saturationPercent)) // call the capability version method overload
}

def setColor(String rgbHex) {
    doLogging "Executing 'setColor' from hex $rgbHex"
    if (hex == "#000000") {
        // setting to black? turn it off.
        off()
    } else {
        List hsvList = colorUtil.hexToHsv(rgbHex)
        Map colorHSMap = buildColorHSMap(hsvList[0], hsvList[1])
        setColor(colorHSMap) // call the capability version method overload
    }
}

def setColor(Map colorHSMap) {
    doLogging "Executing 'setColor(Map)' ${colorHSMap}"
    Integer boundedHue = boundInt(colorHSMap?.hue?:0, PERCENT_RANGE)
    Integer boundedSaturation = boundInt(colorHSMap?.saturation?:0, PERCENT_RANGE)
    String rgbHex = colorUtil.hsvToHex(boundedHue, boundedSaturation)
    doLogging "bounded hue and saturation: $boundedHue, $boundedSaturation; hex conversion: $rgbHex"

	sendEvent(name: "hue", value: boundedHue)
    sendEvent(name: "saturation", value: boundedSaturation)
    sendEvent(name: "color", value: rgbHex)

    setPower("on")
}


def updateStatus(status){

}

def setSwitchState(on){
	doLogging "Setting switch to ${on ? 'ON' : 'OFF'}";

	sendEvent(name: "switch", value: on ? "on" : "off", displayed: true);
}


def ping() {
	doLogging "ping()"
	return refresh()
}