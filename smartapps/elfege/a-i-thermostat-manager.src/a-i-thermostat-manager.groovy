definition(
    name: "A.I. Thermostat Manager",
    namespace: "ELFEGE",
    author: "ELFEGE",

    description: "This A.I. adjusts your thermostats to your liking using some elementary machine learning",

    category: "Green Living",
    iconUrl: "https://www.philonyc.com/assets/penrose.jpg",
    iconX2Url: "https://www.philonyc.com/assets/penrose.jpg",
    iconX3Url: "https://www.philonyc.com/assets/penrose.jpg",
    image: "https://www.philonyc.com/assets/penrose.jpg"
)

preferences {

    page name: "MainPage"
    page name: "settings"
    page name: "AI"
    page name: "comfortSettings"
    page name: "powerSaving"
    page name: "SavePowerMotion"
    page name: "alternativeSensors"


}
def MainPage(){
    def pageProperties = [
        name:       "MainPage",
        title:      "MainPage",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

    return dynamicPage(pageProperties) {

        section("Main Settings") {
            href "settings", title: "Thermostats and other devices", description: ""
            href "comfortSettings", title: "Your Comfort Zone", description: ""
            href "AI", title: "A.I. settings", description: ""
            href "powerSaving", title: "Save Power When Away", description: ""
            href "SavePowerMotion", title: "Save Power With Motion", description: ""

        }
    }
}
def settings() {

    def pageProperties = [
        name:       "settings",
        title:      "Thermostats and other devices",
        nextPage:   null,
        install: false,
        uninstall: true
    ]

    dynamicPage(pageProperties) {

        section("Select the thermostats you want to control") { 

            input(name: "Thermostats", type: "capability.thermostat", title: "select your thermostats", required: true, multiple: true, description: null, submitOnChange: true)

            input(name: "contacts", type:"capability.contactSensor", title: "Turn off all units when these contacts are open", multiple: true, required: false, submitOnChange: true)

            input(name: "criticalSensor", type: "capability.temperatureMeasurement", title: "If this sensor returns a temperature below a certain threshold, turn all units back on", required: false, multiple: false,
                  description: "Select a temperature sensor", submitOnChange: true)   
            if(criticalSensor){
                input(name: "threshold", type: "number", required: true, title: "critical temperature", description: "set a threshold value")
            }

        }
    }
}
def comfortSettings(){

    def pageProperties = [
        name:       "comfortSettings",
        title:      "Comfort zone",
        nextPage:   null,
        install: false,
        uninstall: true
    ]

    dynamicPage(pageProperties) {
        section("Set thermostats modes with outside temperature") { 


            input(name: "outsidetemp", type: "capability.temperatureMeasurement", title: "Outside temperature", required: true, multiple: false,
                  description: "Select a temperature sensor that reports outside temperature", submitOnChange: true)   

            input(name: "lowTemp", type: "number", required: true, title: "Set thermosats to Heat when below this temperature", submitOnChange: true)
            input(name: "highTemp", type: "number", required: true, title: "Set thermosats to Cool when above this temperature", submitOnChange: true)

            input(name: "reqMode", type: "enum", required: true, title: "When outside temperature returns a value between $lowTemp and $highTemp, set it to this mode", 
                  options:["auto", "off", "do nothing"])

            input(name: "comfortHigh", type: "number", required: true, title: "Set a maximum heating temperature", submitOnChange: true)
            input(name: "comfortLow", type: "number", required: true, title: "Set a minimum cooling temperature", submitOnChange: true)

            input(name: "comfortMode", type: "mode", required: false, multiple:true, title: "Select the modes under which optimized comfort settings will apply", submitOnChange: true)
            paragraph "Beware that this involves more energy consumption"          
        }

        section(){
            href "alternativeSensors", title: "Third Party Temperature Sensors", description: ""
        }
    }

}

def alternativeSensors(){
    def pageProperties = [
        name:       "alternativeSensors",
        title:      "Third Party Temperature Sensors",
        nextPage:   "comfortSettings",
        install: false,
        uninstall: true
    ]

    dynamicPage(pageProperties) {
        section(){
            input(name: "AltSensor", type: "bool", title: "Control some thermostats' with a third party sensor", required: false, default: false, submitOnChange: true)

            if(AltSensor){

                def MyThermostats = []
                Thermostats.each {MyThermostats << "$it"}

                input(name: "ThermSensor", type: "enum", title: "Which devices do you want to control this way?", multiple: true, options: MyThermostats.sort(), required: true, submitOnChange: true)

                if(ThermSensor){
                    def i = 0
                    def s = ThermSensor.size()
                    for(s > 0; i < s; i++){
                        input(name: "Sensor${i}", type: "capability.temperatureMeasurement", 
                              title: "Select a third party sensor to control ${ThermSensor[i]}",
                              required: true, multiple: false, description: "pick a sensor"
                             )
                    }
                }
            }
        }
    }
}

def AI(){

    def pageProperties = [
        name:       "AI",
        title:      "Smart Management",
        nextPage:   null,
        install: false,
        uninstall: true
    ]
    dynamicPage(pageProperties) {
        section("Artificial intelligence: define setpoints based on your lifestyle") { 
            input(name: "AI", type: "bool", title: "Observe and learn from my habits", default: false, submitOnChange: true)
            input(name: "reset", type: "bool", title: "reset all previously recorded values", default: false, submitOnChange: true)
            if(reset){
                paragraph "ARE YOU SURE YOU WANT TO DELETE ALL HEURISTICS? (make sure to close this section afterward)"
                input(name: "confirmreset", type: "bool", title: "confirm deletion of heuristics", default: false, submitOnChange: true)        
                if(confirmreset){
                    paragraph "all heuristics have now been deleted! You can now close this section"
                    // reset A.I. heuristics if requested by user
                    /*
                    state.learnedHSP = [:] 
                    state.learnedCSP = [:] 
                    state.HSPMode = [:] 
                    state.CSPMode = [:]
                    */
                    log.debug "all heuristics have been reset. Starting to learn from here"
                }
            }
            if(AI){
                input(name: "learnSP", type: "bool", title: "Check current temperature settings and use them as reference", default: false, submitOnChange: true)
                input(name: "learnSPMode", type: "bool", title: "Listen to location mode changes and record new setpoints", default: false, submitOnChange: true)
                if(outsidetemp){
                    def MyThermostats = []
                    Thermostats.each {MyThermostats << "$it"}

                    input(name: "algebra", type: "bool", title: "Use some math", default: false, submitOnChange: true)
                    if(algebra){
                        paragraph "Set points will be modified with outside's temperature"
                    }
                    input(name: "power", type: "bool", title: "Some of my appliances have a power meter", default: false, submitOnChange: true)
                    if(power){
                        input(name: "OkToAdjustModes", type: "mode", title: "Apply these heuristics only when home is in these modes", 
                              description: "It is HIGHLY recommended to not allow this feature to run while you're away or any mode meant to save power", 
                              required:true, 
                              multiple: true,
                              submitOnChange: true)
                        input(name: "ApplianceWithPwMeter", 
                              type: "enum", 
                              title: "Select the appliances that have a power meter", 
                              multiple: true, 
                              options: MyThermostats.sort(), 
                              required: true, 
                              submitOnChange: true)

                        if(ApplianceWithPwMeter){
                            def i = 0
                            def s = ApplianceWithPwMeter.size()
                            for(s > 0; i < s; i++){
                                input(name: "powerMeter${i}", type: "capability.powerMeter", 
                                      title: "Select the power meter related to ${ApplianceWithPwMeter[i]}",
                                      required: true, multiple: false, description: "pick a sensor")

                            }
                        }
                    }
                    input(name: "heatpump", type: "bool", title: "One of my devices is a heatpump", default: false, submitOnChange: true)

                    if(heatpump){
                        input(name: "HeatPumps", 
                              type: "enum", 
                              title: "Select the devices that are heat pumps (not HVACs!)", 
                              multiple: true, 
                              options: MyThermostats.sort(), 
                              required: true, 
                              submitOnChange: true
                             )

                        input(name:"criticalHeatPump", type:"number", title: "What is the critical efficiency temperature for your heat pump?", required: true,  submitOnChange:true)

                        paragraph "power consumption based set points won't work when outside temperature is below $criticalHeatPump °F"

                        input("extraAppliance", "capability.switch", 
                              title: "When outside temperature si below $criticalHeatPump °F, manage this electric heater instead", 
                              description: "select a switch to control", required: false, multiple: true, submitOnChange: true)

                    }
                }
            }
        }
    }
}
def powerSaving(){

    def pageProperties = [
        name:       "powerSaving",
        title:      "Save Power When You're Away",
        nextPage:   null,
        install: false,
        uninstall: true
    ]
    dynamicPage(pageProperties) {

        section("Save power when you're away"){
            input(name: "saveModes", type : "mode", title: "select the modes dedicated to power saving (these will bypass any A.I. setting)", required: false, multiple: true)
            if(saveModes){
                input(name: "saveModesHSP", type : "number", title: "Set a heating temperature for when you're in ${saveModes.toString()} mode", required: true)
                input(name: "saveModesCSP", type : "number", title: "Set a cooling temperature for when you're in ${saveModes.toString()} mode", required: true)
            }
        }
    }
}

def SavePowerMotion(){
    def pageProperties = [
        name:       "SavePowerMotion",
        title:      "Save Power With Motion",
        nextPage:   null,
        install: false,
        uninstall: true
    ]
    dynamicPage(pageProperties) {
        if(Thermostats.size() != 0){
            section("Adjust Temperatures With Motion"){
                def MyThermostats = []
                Thermostats.each {MyThermostats << "$it"}

                input(name: "useMotion", type: "bool", title: "Use motion sensors to adjust Thermostats settings when inactive", submitOnChange: true, default: false)

                if(useMotion){

                    // //log.debug "motion management"
                    // list all modes minus $Away
                    def i = 0
                    state.modes = []
                    def allModes = location.modes
                    def amS = allModes.size()
                    //log.debug "allModes size: = ${amS}"

                    for(amS != 0; i < amS; i++){
                        state.modes << ["${allModes[i]}"]
                    }

                    input(name: "HeatNoMotion", type: "number", title: "Substract this amount of degrees to heat setting", required: true, defaultValue: 2)
                    input(name: "CoolNoMotion", type: "number", title: "Add this amount of degrees to cooling setting", required: true, defaultValue: 2)  


                    i = 0
                    def ts = MyThermostats.size()
                    MyThermostats = MyThermostats.sort()

                    for(ts > 0; i < ts; i++){

                        paragraph """
___________________
${MyThermostats[i]}
___________________"""


                        input(name: "thermMotion${i}", type: "enum",
                              title: "For this thermostat...", 
                              description: "pick your thermostat",
                              options: MyThermostats.sort(),
                              multiple: false,
                              required: flase,
                              defaultValue: "${MyThermostats[i]}"

                             )


                        input(name: "MotionSensor${i}", type: "capability.motionSensor", 
                              multiple: true, 
                              title: "Select the sensors to use with ${MyThermostats[i]}", 
                              description: "pick a sensor", 
                              required: flase

                             )

                        input(
                            name: "MotionModes${i}", type: "mode", 
                            title: "Use motion only if home is in these modes", 
                            multiple: true, 
                            description: "select a mode", 
                            required: flase 
                        )

                    }

                    input (name:"minutesMotion", type:"number", title: "For how long there must be no motion for those settings to apply? ", 
                           range: "2..999", 
                           description: "time in minutes",
                           required: false)
                    paragraph "this timer will apply indifferently to all selected motion sensors"

                }
            }
        }
        else {
            paragraph "You haven't set any thermostat yet. Go back to main settings"
        } 
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    // A.I. learning base maps, must not be reset anywhere else but at install 

    state.learnedHSP = [:] 
    state.learnedCSP = [:] 
    state.HSPMode = [:] 
    state.CSPMode = [:] 

    setSPreq()
    /// run a first learning process if database is empty
    if(state.learnedHSP.size() == 0 || state.HSPMode == 0){

        log.debug "BUILDING LEARNING MAPS............................................................................"

        def CurrMode = location.currentMode
        def thisHSP = null
        def thisCSP = null
        def thisTherm = null
        def s = Thermostats.size()
        def i = 0
        for(s > 0; i < s; i++){

            thisTherm = Thermostats[i]
            thisHSP = thisTherm.currentValue("heatingSetpoint")
            state.learnedHSP << ["${Thermostats[i]}" : "${thisHSP}"] 
            state.HSPMode."${CurrMode}" = state.learnedHSP 

            thisCSP = thisTherm.currentValue("coolingSetpoint")
            state.learnedCSP << ["${thisTherm}" : "${thisCSP}"] 
            state.CSPMode."${CurrMode}" = state.learnedCSP 

            learn(Thermostats[i], thisHSP, "heatingSetpoint")
            learn(Thermostats[i], thisCSP, "coolingSetpoint")

        }
    }

    runIn(5, initialize)
    //initialize()
}
def updated() {
    log.debug "updated with settings: ${settings}"
    unsubscribe()
    unschedule()
    initialize()
}
def initialize() {
    // reset lists and maps
    // these lists will allow to have sets of thermostats working with sets of motion sensors
    state.thermMotionList = []              
    state.MotionModesList = []
    state.MotionSensorList = []
    // these maps' purpose is only to verify that all settings were properly arranged together 
    // and may be used as a debug ressource
    state.MotionModesAndItsThermMap = [:]
    state.SensorThermMap = [:]

    resetModeJustChanged()
    // reset state.SPreqFromApp
    resetSPreq()

    // subscribe to events
    subscribe(contacts, "contact.open", contactHandler)
    subscribe(contacts, "contact.closed", contactHandler)
    subscribe(Thermostats, "temperature", temperatureHandler)

    if(AltSensor){

        def i = 0
        def s = ThermSensor.size()
        def refSensor = null
        def refTherm = null
        state.AltSensorMap = [:]

        for(s > 0; i < s; i++){
            //refSensor = "Sensor${i.toString()}"
            refSensor = settings.find{it.key == "Sensor${i.toString()}"}?.value
            log.debug "refSensor is $refSensor"
            refTherm = ThermSensor[i]
            log.debug "refTherm is $refTherm"
            state.AltSensorMap."$refTherm" = "$refSensor" // map for corresponding thermostat
            subscribe(refSensor, "temperature", temperatureHandler)
            log.debug "Subscription for alternative Sensor ${refSensor} successful" 
        }
    }

    if(power){
        def i = 0
        def s = ApplianceWithPwMeter.size()
        def pwmeter = null

        for(s > 0; i < s; i++){
            pwmeter = settings.find{it.key == "powerMeter${i.toString()}"}?.value
            subscribe(pwmeter, "power", adjustWithPw)
            log.debug "$pwmeter succesfully subscribed to adjustWithPw handler"
        }
    }

    if(learnSPMode){
        subscribe(location, "mode", ChangedModeHandler) 
        // subscribe(Thermostats, "thermostatSetpoint", setpointChangeHandler)
        subscribe(Thermostats, "coolingSetpoint", setpointChangeHandler)
        subscribe(Thermostats, "heatingSetpoint", setpointChangeHandler)
    }



    schedule("23 0/1 * * * ?", eval)

    if(useMotion){
        SetListsAndMaps()
        MotionSub()
    }

    runIn(10, eval)

}

def MotionSub(){


    def loopV = 0

    def MotionSensorList = state.MotionSensorList
    log.debug "MotionSensorList = $state.MotionSensorList"

    def ms = MotionSensorList.size()
    for(ms > 0; loopV < ms; loopV++){  

        def TheSensor = "MotionSensor${loopV}"
        TheSensor = settings.find{it.key == TheSensor}
        TheSensor = TheSensor?.value

        subscribe(TheSensor, "motion", motionSensorHandler)
        log.debug "${TheSensor} subscribed to evt"  

    }

    log.debug "END OF SUBSCRIPTIONS"



}

///// MOTION SENSORS MAPS 
def SetListsAndMaps(){

    // recollect lists for motion sensors and associated thermostats
    def loopV = 0
    def s = Thermostats.size()
    def reftm = ""
    def refmmodes = ""
    def refms = ""

    for(s > 0; loopV < s; loopV++){

        reftm = "thermMotion${loopV.toString()}"
        refmmodes = "MotionModes${loopV.toString()}"
        refms = "MotionSensor${loopV.toString()}"  

        reftm = settings.find{it.key == "$reftm"}
        refmmodes = settings.find{it.key == "$refmmodes"}
        refms = settings.find{it.key == "$refms"}

        reftm = reftm?.value
        refmmodes = refmmodes?.value
        refms = refms?.value

        log.debug """
reftm = $reftm
refmmodes = $refmmodes
refms = $refms
"""

        if(reftm){

            state.thermMotionList << "$reftm"
            state.MotionModesList << "$refmmodes"
            state.MotionSensorList << "$refms"
            state.MotionModesAndItsThermMap << ["$reftm" : "$refmmodes"]
            state.SensorThermMap << ["$reftm" : "$refms"]
        }
        else {
            log.debug "reftm returned null ( $reftm )"
        }

    }

}
def MotionTest(){

    def thermMotionList = state.thermMotionList
    def MotionModesList = state.MotionModesList
    def MotionSensorList = state.MotionSensorList
    def MotionModesAndItsThermMap = state.MotionModesAndItsThermMap
    def SensorThermMap = state.SensorThermMap

    /*  log.info """
thermMotionList = $thermMotionList
MotionModesList = $MotionModesList
MotionSensorList = $MotionSensorList

MotionModesAndItsThermMap = $MotionModesAndItsThermMap
SensorThermMap = $SensorThermMap
"""  
*/
    def loopV = 0
    def s = MotionSensorList.size()
    def t = thermMotionList.size()
    //def s = MotionSensor.findAll{it.device != null}.sort()

    loopV = 0
    def o = null
    def i = 0
    def x = []
    def ThisSensorList = []

    i = 0
    def deltaMinutes = minutesMotion * 60000 as Long

    def motionEvents = []
    def result = [:]
    boolean Active = false

    for(t > 0; i < t; i++){

        def TheSensor = "MotionSensor${i}" // this is a list in itself since multiple = true
        def TheSensorB4 = settings.find{it.key == TheSensor}

        TheSensor = TheSensorB4?.value
        log.debug "TheSensor = $TheSensor"

        //motionEvents = TheSensor.collect{ it.eventsSince(new Date(now() - deltaMinutes)) }.flatten() // this one works but we need iteration of TheSensor

        // collect events for each sensor within the list that TheSensor object may be made of when user selected multiple sensors to relate to one thermostat
        def SensorSize = TheSensor.size()
        def iteration = 0 
        for(SensorSize > 0; iteration < SensorSize; iteration++){
            def thisItSensor = TheSensor[iteration]
            def Evts4thisiteration = thisItSensor.eventsSince(new Date(now() - deltaMinutes)) 


            Active = Evts4thisiteration.size() > 0 
            //log.debug "${Evts4thisiteration.size()} motion Events for $thisItSensor = ${Evts4thisiteration} and Active is then set to $Active"

            result << ["${TheSensor}": "$Active"] // record value for the group to which this device belongs // 

            log.debug """Found ${Evts4thisiteration.size() ?: 0} events in the last $minutesMotion minutes at ${thisItSensor} deltaMinutes = $deltaMinutes"""

            if(Active){
                log.debug "BREAK -- " // if any of the multiple devices returned motion evts then the value 'true' superceeds
                break
            }
        }
    }

    //log.debug "Active result = $result"
    return result
}
def ActiveTest(thermostat) {
    def Active = true 
    def inMotionModes = false
    def TheSensor = null


    if(useMotion){

        def thermMotionList = state.thermMotionList
        def MotionModesList = state.MotionModesList
        def MotionSensorList = state.MotionSensorList
        def MotionModesAndItsThermMap = state.MotionModesAndItsThermMap
        def SensorThermMap = state.SensorThermMap

        // since it is possible for users to deselect a motionThermostat 
        // check that the requested thermostat is one of the selected ones
        // and if not, return true at all times for this one

        def thisThermIsInTheList = thermMotionList.find{it.toString().contains("$thermostat")} != null

        //log.debug "state.thermMotionList = $state.thermMotionList and current Thermostat = $thermostat && thisThermIsInTheList = $thisThermIsInTheList"

        if(thisThermIsInTheList){

            def CurrMode = location.currentMode

            // find the sensor related to this therm
            TheSensor = SensorThermMap.find{it.key == "${thermostat}"}
            // what is its sensor value
            TheSensor = TheSensor?.value

            def MotionModes = MotionModesAndItsThermMap.find{it.key == "${thermostat}"}
            log.debug "MotionModes before value called: $MotionModes"
            MotionModes = MotionModes?.value

            inMotionModes = CurrMode in MotionModes //?.find("$CurrMode") == "$CurrMode"
            log.debug "$thermostat inMotionModes after value called: $inMotionModes"

            def ActiveMap = MotionTest() 
            log.debug "ActiveMap = $ActiveMap"
            def ActiveFind = ActiveMap.find{it.key == "${TheSensor}"}

            Active = ActiveFind?.value
            if(inMotionModes){
                Active = "$Active" == "true"
            }
            else {
                Active = true // always true when not in motion modes
            }


            /*  log.trace """
CurrMode is $CurrMode
Current Motion Sensor = $TheSensor
Current MotionModes = $MotionModes, 
$thermostat is inMotionModes?($inMotionModes),
MotionModesAndItsThermMap = $MotionModesAndItsThermMap
useMotion?($useMotion)
ActiveMap = $ActiveMap
SensorThermMap = $SensorThermMap
MotionSensorList = $MotionSensorList
Active?(from List) for $thermostat && $TheSensor = $Active
"""
*/

        }
        else {
            Active = true
            inMotionModes = true
            TheSensor = null
            log.debug "$thermostat hasn't been selected by the user as a motion managed unit"
        }

    }
    log.debug "boolean motion test for $thermostat returns $Active || inMotionModes = $inMotionModes"
    return [Active, inMotionModes, TheSensor]
}
/////////////////////////

def temperatureHandler(evt){
    log.debug "$evt.device returns ${evt.value}°"

    // refresh outside sensor to get latest values
    def poll = outsidetemp.hasCommand("poll")
    def refresh = outsidetemp.hasCommand("refresh") 

    if(refresh){
        outsidetemp.refresh()
        log.debug "refreshing $outsidetemp"
    }
    else if(poll){
        outsidetemp.poll()
        log.debug "polling $outsidetemp"
    }

    resetSPreq() // do not allow the app to learn from its own cmds
    runIn(5, setCSPs)
    runIn(5, setHSPs)

}
def ChangedModeHandler(evt){

    /* state.modeJustChanged = true
    // do not let the app learn from possible new incoming commands that'd be 
    // considered as to be learned commands since we reset with resetSPreq()
    resetSPreq()
    runIn(30, resetModeJustChanged) */

    runIn(3, setHSPs)
    runIn(4, setCSPs)

    log.debug "Home is now in ${evt.value} mode"

}
def motionSensorHandler(evt){
    log.debug "motion is $evt.value at $evt.device"  

    if(evt.value == "active"){
        log.debug "new motion at $evt.device"
        eval()
    }
    else {
        log.debug "no more motion at $evt.device"
    }
}
def setpointChangeHandler(evt){

    log.debug "$evt.device $evt.name $evt.value -------source: $evt.source -- evt.name = $evt.name" 

    learn(evt.device, evt.value, evt.name)

}

def learn(thisTherm, thisTemp, eventName){

    if(state.SPreqFromApp == false){
        int i = 0
        int s = Thermostats.size()
        //def thisTherm = null
        //def thisTemp = null
        def thisHSP = null
        def thisCSP = null

        //state.learnedHSP = [:] 
        //state.learnedCSP = [:] 
        // state.HSPMode = [:] 
        // state.CSPMode = [:] 

        def CurrMode = location.currentMode

        //thisTherm = evt.device
        //thisTemp = evt.value

        if(eventName == "heatingSetpoint"){
            thisHSP = thisTherm.currentValue("heatingSetpoint")
            state.learnedHSP << ["${thisTherm}" : "${thisHSP}"] 
            state.HSPMode."${CurrMode}" = state.learnedHSP 
        }
        else if(eventName == "coolingSetpoint"){
            thisCSP = thisTherm.currentValue("coolingSetpoint")
            state.learnedCSP << ["${thisTherm}" : "${thisCSP}"] 
            state.CSPMode."${CurrMode}" = state.learnedCSP 
        }


        log.trace """ LEARNING:

    state.learnedHSP = $state.learnedHSP
    state.learnedCSP = $state.learnedCSP
    state.HSPMode = $state.HSPMode
    state.CSPMode = $state.CSPMode
    """
    }
    else {
        log.info "learning not triggered because cmd came from within the app" 
    }
}

def contactHandler(evt){
    log.debug "$evt.device is $evt.value"
    state.value = evt.value
    eval()
}
def adjustWithPw(evt){
    log.debug "$evt.device returns ${evt.value} Watts"
    adjust()
}

def adjust(){
    log.debug "test 1"

    def currMode = location.currentMode
    boolean ModeOk = currMode in OkToAdjustModes
    boolean inSavingMode = currMode in saveModes


    boolean contactsOpen = "open" in contacts?.currentValue("contact") 
    def outside = outsidetemp.currentValue("temperature")
    if(contactsOpen && outside < 68){
        log.debug "Ignoring open contacts for it's far too cold outside"
        contactsOpen = false
    }

    //log.debug "getComfortH() = ${getComfortH()}"
    def comfort = getComfortH()
    def Active = true

    log.debug "test 2"


    log.trace "OkToAdjustModes = $OkToAdjustModes && inSavingMode = $inSavingMode && ModeOk = $ModeOk && contactsOpen = $contactsOpen"


    if(!inSavingMode){
        if(ModeOk && !contactsOpen){     

            // check power consumption of the device with power measurement
            // find the device
            def i = 0
            def s = ApplianceWithPwMeter.size()
            def pwmeter = null
            def thisAppliance = null
            def thisHSP = null
            def thisCSP = null
            def thisTemp = null
            def tooCold4Hpump = false

            for(s > 0; i < s; i++){

                thisAppliance = Thermostats.find{it.displayName == "${ApplianceWithPwMeter[i]}"}   
                def GetMotionData = ActiveTest(thisAppliance)
                log.debug "GetMotionData = $GetMotionData"
                Active = GetMotionData[0] //GetActiveValue == "true"

                def test = null
                if(Active){
                    test = "YAY!"
                }
                else {
                    test = "NAY"
                }

                log.debug "Motion for $thisAppliance returns $Active ---------------$test----"


                if(Active){
                    thisHSP = thisAppliance.currentValue("heatingSetpoint")
                    thisCSP = thisAppliance.currentValue("coolingSetpoint")
                    pwmeter = settings.find{it.key == "powerMeter${i.toString()}"}?.value

                    log.debug "$thisAppliance is related to this power meter: $pwmeter"


                    // check if user selected a third party sensor
                    def useAltSensor = state.AltSensorMap.find{it.toString().contains("$thisAppliance")} as Boolean
                    log.debug "$thisAppliance was found in this map: $state.AltSensorMap ? >> $useAltSensor -"

                    // get this thermostat's or its related third party sensor's current temperature
                    if(useAltSensor){
                        def StringSensor = state.AltSensorMap.find{it.key == "$thisAppliance"}.value
                        // this is a string, so go get the device object from settings
                        def theAltSensor = settings.find{it.toString().contains(StringSensor)}.value         
                        thisTemp = theAltSensor.currentValue("temperature")
                        log.debug "(power meter) $thisAppliance is linked to $theAltSensor as alternate sensor, which returns a temp of $thisTemp"
                    }
                    else {
                        thisTemp = thisAppliance.currentValue("temperature")
                        log.debug "$thisAppliance returns a temperature of ${thisTemp}° (power meter related device) "
                    }

                    boolean needAdjust = false

                    def thermMode = thisAppliance.currentValue("thermostatMode")
                    if(thermMode == "heat"){
                        if(pwmeter.currentValue("power") < 600 && thisTemp < thisHSP){
                            needAdjust = true
                        }
                    }
                    else if(thermMode == "cool"){
                        if(pwmeter.currentValue("power") < 600 && thisTemp > thisCSP){
                            needAdjust = true
                        }
                    }

                    tooCold4Hpump = tooColdForHeatPump(thisAppliance)
                    log.debug "$thisAppliance need adjustment: $needAdjust"


                    boolean useBoth = useboth(tooCold4Hpump, outside.toInteger())

                    if((!tooCold4Hpump || useBoth) && ModeOk){
                        if(needAdjust){
                            if(thisAppliance.currentValue("thermostatMode") == "heat" ){                
                                resetSPreq() // make sure A.I. learns new value                   
                                // if current temperature is below heat comfort
                                log.debug "thisTemp = $thisTemp, comfort = ${comfort}"
                                if(thisTemp < comfort){
                                    // we're too far below comfort zone
                                    log.debug "adjusting HSP because $pwmeter returns a value lower than expected"
                                    // increase the setpoint
                                    if(thisAppliance.currentValue("heatingSetpoint") != thisHSP + 1){
                                        thisAppliance.setHeatingSetpoint(thisHSP + 1)
                                    }
                                }
                            }
                            else if(thisAppliance.currentValue("thermostatMode") == "cool" ){
                                log.debug "adjusting HSP because $pwmeter returns a value lower than expected"
                                resetSPreq() // make sure A.I. learns new value

                                // if current temperature is above cooling comfort 
                                if(thisTemp > comfort){
                                    if(thisAppliance.currentValue("coolingSetpoint") != comfort){
                                        thisAppliance.setcoolingSetpoint(comfort)
                                    }
                                }

                            }
                        }
                        else {
                            if(thisAppliance.currentValue("thermostatMode") == "heat"){
                                // we're comfortable so get the HSP back to user's comfort zone
                                if(thisTemp > comfort){
                                    thisAppliance.setHeatingSetpoint(comfort)
                                }
                            }
                            if(thisAppliance.currentValue("thermostatMode") == "cool"){
                                // we're comfortable so get the HSP back to user's comfort zone
                                if(thisTemp < comfort){
                                    thisAppliance.setHeatingSetpoint(comfort)
                                }
                            }
                        }
                    }
                    else if(tooCold4Hpump){
                        log.debug "outside tempearture is too low, turning off $thisAppliance"
                        if(thermMode != "heat"){
                            thisAppliance.setThermostatMode("off")
                        }
                    }
                }
                else {
                    log.debug "HSP for $thisAppliance NOT MODIFIED because there's no motion in its vicinity"
                }
            }
        }
        else {
            log.debug "outside of power appliance adjustments modes or some contacts are open (contactsOpen:$contactsOpen, ModeOk:$ModeOk"
        }
    }
    else {
        log.debug "in power saving mode, not changing power meter appliances set points"
    }
}

boolean tooColdForHeatPump(device){
    // now, if it's a HeatPump, check outside's temperature isn't too low
    def outside = outsidetemp.currentValue("temperature")
    def result = false
    log.debug """
    - device = $device
    - criticalHeatPump = $criticalHeatPump
    - outside temperature = $outside
    """

    if(HeatPumps){
        def ThisHeatPump = HeatPumps.find{it.toString().contains("$device")} as Boolean
        if(ThisHeatPump){

            if(outside < criticalHeatPump){
                result = true
                log.debug "$device is a Heat Pump and it is too cold outside."
            }
        }
    }
    log.debug "tooColdForHeatPump returns $result"
    return result
}

def eval(){

    log.info """
	state.SPreqFromApp = $state.SPreqFromApp
    state.learnedHSP = $state.learnedHSP
    state.learnedCSP = $state.learnedCSP
    state.HSPMode = $state.HSPMode
    state.CSPMode = $state.CSPMode
    """



    boolean contactsOpen = "open" in contacts?.currentValue("contact") 
    def outside = outsidetemp.currentValue("temperature")
    if(contactsOpen && outside < 65){
        log.debug "Ignoring open contacts for it's far too cold outside"
        contactsOpen = false
    }
    if(criticalSensor){
        if(criticalSensor.currentValue("temperature") <= threshold){
            log.debug "It's too cold INSIDE, Ignoring open contacts, not turning off thermostats"
            contactsOpen = false
        }
    }



    log.debug "contacts are: $contacts, some are open: $contactsOpen"

    if(contactsOpen){

        // turn off all thermosats
        log.debug "turning off all thermostats"
        int i = 0
        int s = Thermostats.size()
        for(i = 0; i < s; i++){
            if(Thermostats[i].currentValue("thermostatMode") != "off"){

                Thermostats[i].setThermostatMode("off")

            }
        }
    }

    else {

        setThermostats()

    }
}

def setThermostats(){
    log.debug "thermosats setup"
    boolean inSavingMode = location.currentMode in saveModes
    log.debug "inSavingMode = $inSavingMode"

    if(!inSavingMode){
        def i = 0;
        def s = Thermostats.size()
        def thisTherm = null
        def thisTemp = null
        def thisHSP = null
        def thisCSP = null

        def outside = outsidetemp.currentValue("temperature").toInteger()
        log.debug "outside temperature is: $outside"

        def CurrMode = location.currentMode


        def recordedHSP = state.HSPMode.find{it.key == "$CurrMode"}?.value // extract the map of HSPs' for this mode
        def recordedCSP = state.CSPMode.find{it.key == "$CurrMode"}?.value // extract the map of CSPs' for this mode

        if(recordedHSP == null || recordedCSP == null){     
            resetSPreq()
            log.debug """NULL VALUES LEARNING ON
NULL recordedHSP for $CurrMode mode = $recordedHSP
NULL recordedCSP for $CurrMode mode = $recordedCSP
"""

            for(s != 0; i < s; i++){
                thisTherm = Thermostats[i]
                if(recordedHSP == null){
                    learn(thisTherm, thisTherm.currentValue("heatingSetpoint"), "heatingSetpoint")
                }
                if(recordedCSP == null){
                    learn(thisTherm, thisTherm.currentValue("coolingSetpoint"), "coolingSetpoint")
                }
            }
            i = 0
            recordedHSP = state.HSPMode.find{it.key == "$CurrMode"}?.value // extract the map of HSPs' for this mode
            recordedCSP = state.CSPMode.find{it.key == "$CurrMode"}?.value // extract the map of CSPs' for this mode
        }


        log.debug """
recordedHSP for $CurrMode mode = $recordedHSP
recordedCSP for $CurrMode mode = $recordedCSP
"""

        for(s != 0; i < s; i++){

            // define the device we're working with in this iteration of the loop
            thisTherm = Thermostats[i]
            //log.debug "$thisTherm current mode is : ${thisTherm.currentValue("thermostatMode")}"

            // check if user selected a third party sensor
            def theAltSensor = null
            def useAltSensor = state.AltSensorMap.find{it.toString().contains("$thisTherm")} as Boolean
            //log.debug "$thisTherm was found in this map: $state.AltSensorMap ? >> $useAltSensor -"

            // get this thermostat's or its related third party sensor's current temperature
            if(useAltSensor){
                def StringSensor = state.AltSensorMap.find{it.key == "$thisTherm"}.value
                //log.debug "StringSensor is $StringSensor " //||||||||||||| settings = $settings"
                // this is a string, so go get the device object from settings

                theAltSensor = settings.find{it.toString().contains(StringSensor)}.value
                //log.debug "theAltSensor is $theAltSensor --"
                thisTemp = theAltSensor.currentValue("temperature")
                log.debug "third party sensor $theAltSensor returns a temperature of $thisTemp"
            }
            else {
                thisTemp = thisTherm.currentValue("temperature")
                //log.debug "$thisTherm returns a temperature of $thisTemp"
            }

            // value is a map of [therm:temp] for current mode 
            // extract value for this thermostat
            thisHSP = recordedHSP.find{it.key == "${thisTherm}"}?.value
            thisCSP = recordedCSP.find{it.key == "${thisTherm}"}?.value
            log.debug """
        thisHSP at setThermostats() is: $thisHSP
        thisCSP at setThermostats() is: $thisCSP
        """
            thisHSP = thisHSP.toInteger()
            thisCSP = thisCSP.toInteger()
            thisTemp = thisTemp.toDouble() // these values are only for the keepOff() requests and extraAppliance (virtual thermostat) option
            // therefore, there's no real need for motion test here

            // check that this is not an appliance managed throuhg its power consumption criteria
            def alreadyManaged = ApplianceWithPwMeter.find{it.toString().contains("$thisTherm")} as Boolean

            def keepOff = keepOff(i, thisTemp, thisHSP, thisCSP, outside)
            log.debug "keepOff $keepOff"

            // Now, assess needs: do we currently need heat or cooling ? 
            // if heat, then check that current temperature is ok, if not, set to heat
            // and the same goes with cooling



            def tooCold4Hpump = tooColdForHeatPump(thisTherm)
            boolean useBoth = useboth(tooCold4Hpump, outside.toInteger())

            log.debug "outside: $outside lowTemp = $lowTemp useBoth = $useBoth tooCold4Hpump = $tooCold4Hpump"

            if(keepOff){
                if(reqMode != "do nothing" && !useAltSensor && !alreadyManaged){
                    if(thisTherm.currentValue("thermostatMode") != reqMode){
                        thisTherm.setThermostatMode(reqMode)
                    }
                }

                // if "do nothing" was selected by user or useAltSensor is true, we need to turn off 
                // a thermostat that is managed throuhg alternate sensor 
                // when desired temperature has been reached
                // providing it's not a thermostat managed through power consumption criteria
                else if(!alreadyManaged && reqMode != "do nothing"){

                    log.debug "turning off $thisTherm because its related temperature sensor returns a temperature of $thisTemp --//**"

                    if(thisTherm.currentValue("thermostatMode") != "off"){
                        thisTherm.setThermostatMode("off")
                    }

                }
            }
            else if(outside <= lowTemp || (extraAppliance && (tooCold4Hpump || useBoth))){ // if it's cold outside 
                log.debug "heating system" 
                // heating is needed

                if(!tooCold4Hpump || useBoth){
                    if(thisTherm.currentValue("thermostatMode") != "heat"){
                        thisTherm.setThermostatMode("heat")
                        log.debug "$thisTherm set to heat"
                    }
                    else {
                        log.debug "$thisTherm already set to heat"
                    }
                }
                else {
                    log.debug "$thisTherm is a heatpump and it's too cold outside or cold enough to use both appliances"
                    if(thisTherm.currentValue("thermostatMode") != "off" && !useBoth){
                        log.debug "turning off $thisTherm (heatpump low temp)"
                        thisTherm.setThermostatMode("off")
                    }
                }
                /// extra appliance management (virtual thermostat)
                if(extraAppliance && (tooCold4Hpump || useBoth)){
                    def GetMotionData = ActiveTest(thisTherm)// get motion value for the originally inteded device
                    log.debug "GetMotionData = $GetMotionData"
                    def Active = GetMotionData[0]
                    log.debug """thermostat values around $extraAppliance: 
                    - thisTherm = $thisTherm
                    - thisHSP = $thisHSP
                    - thisTemp = $thisTemp
                    - Active = $Active
                    """
                    if(thisTemp < thisHSP){

                        if(Active && "off" in extraAppliance.currentValue("switch")){
                            extraAppliance.on()
                            log.debug "turning on $extraAppliance"
                        }
                        else if(!Active){
                            log.debug "not turning on $extraAppliance because there's no motion"
                            if("on" in extraAppliance.currentValue("switch")){
                                extraAppliance.off()  
                                log.debug "$extraAppliance turned off due to absence of motion"
                            }
                        }
                        else {
                            log.debug "$extraAppliance already on"
                        }
                    }
                    else {
                        if("on" in extraAppliance.currentValue("switch")){
                            log.debug "turning off $extraAppliance because temperature is ok now"
                            extraAppliance.off()           
                        }
                        else {
                            log.debug "$extraAppliance already off"
                        }
                    }
                }

            }
            else if(outside >= highTemp){ // if it's warm outside
                log.debug "cooling system" 
                // log.debug "$thisTherm temperature is: $thisTemp"
                // cooling is needed
                if(thisTherm.currentValue("thermostatMode") != "cool"){

                    thisTherm.setThermostatMode("cool")
                    log.debug "$thisTherm set to cool"
                }
                else {
                    log.debug "$thisTherm already set to cool"
                }
                state.SPreqFromApp = true // do not allow the app to learn from its own cmds
                // runIn(5, resetSPreq)
                // setCSPs()
            }

            // in case it was too cold earlier and extraAppliance was turned on, turn it off
            if(extraAppliance && !useBoth && outside > criticalHeatPump){
                if("on" in extraAppliance.currentValue("switch")){
                    log.debug "turning off $extraAppliance since outside temperature is no longer critical for heat pump"
                    extraAppliance.off()
                }
                else {
                    log.debug "$extraAppliance already off"
                }
            }

        } // end of for loop




        resetSPreq() // do not allow the app to learn from its own cmds
        runIn(2, setCSPs)
        runIn(2, setHSPs)
    }
    else {
        log.debug "Power saving mode HSP = $saveModesHSP and CSP = $saveModesCSP"
        def ThermHSPs = Thermostats.currentValue("heatingSetpoint")
        def ListGoodHSPs = ThermHSPs.findAll{val -> val == saveModesHSP ? true : false }
        def ThermCSPs = Thermostats.currentValue("coolingSetpoint")
        def ListGoodCSPs = ThermCSPs.findAll{val -> val == saveModesCSP ? true : false }
        boolean pwSaveHSPOK = ListGoodHSPs.size() == Thermostats.size() 
        boolean pwSaveCSPOK = ListGoodCSPs.size() == Thermostats.size()

        log.debug """
        ThermHSPs = $ThermHSPs
        ListGoodHSPs = $ListGoodHSPs
        pwSaveHSPOK = $pwSaveHSPOK

        ThermCSPs = $ThermCSPs
        ListGoodCSPs = $ListGoodCSPs
        pwSaveCSPOK = $pwSaveCSPOK
        """

        if(!pwSaveHSPOK){
            log.debug "setting power saving HSPs for all thermostats"   
            Thermostats.setHeatingSetpoint(saveModesHSP)
        }
        if(!pwSaveCSPOK){
            log.debug "setting power saving CSPs for all thermostats"
            Thermostats.setCoolingSetpoint(saveModesCSP)
        }
    }
    log.debug "end of setThermostats()"
}

boolean useboth(boolean tooCold4Hpump, int outside){

    boolean result = false

    if(!tooCold4Hpump && outside < 40 && extraAppliance){
        // used both devices when below 35 outside
        result = true;
    }

    return result
}

boolean keepOff(int i, double thisTemp, int thisHSP, int thisCSP, int outside){

    def thisTherm = Thermostats[i]

    def result = false

    if(outside <= lowTemp){
        //if outside temperature makes it that we are in heating mode

        if(thisTemp >= thisHSP){
            // if desired temp has been reached, you may turn if off (or set to requested setting), providing this is requested later
            result = true
        }
    }
    else if(outside >= highTemp){
        //if outside temperature makes it that we are in cooling mode

        if(thisTemp <= thisCSP){
            // if desired temp has been reached, you may turn if off, providing this is requested later
            result = true
        }
    }  
    /*  log.trace """ in keepOff boolean, parameters are:
thisTherm = $thisTherm
thisTemp = $thisTemp
thisHSP = $thisHSP
thisCSP = $thisCSP

& returned result is: $result
"""*/
    return result
}
def getComfortH(){

    log.debug "comfortLow = $comfortLow comfortHigh = $comfortHigh"
    def outside = outsidetemp.currentValue("temperature")

    def xa = 70// outside temperature 
    def ya = comfortL.toInteger() // min desired HSP 

    def xb = 0 //  outside temperature 
    log.debug "test get comfort 3 "

    def yb = comfortHigh.toInteger() // max desired HSP 

    def coef = (yb-ya)/(xb-xa)

    def b = ya - coef * xa // solution to ya = coef*xa + b 

    int newComfortH = coef*outside + b 
    log.debug "coef = $coef, b = $b, outside temp = $outside, newComfortH = $newComfortH"

    return newComfortH

}
def setHSPs(){

    def CurrMode = location.currentMode
    boolean inSavingMode = CurrMode in saveModes
    def comfort = getComfortH() 
    log.debug "setting HSPs... inSavingMode: $inSavingMode... comfort = ${comfort}"

    if(!inSavingMode){
        int i = 0
        int s = Thermostats.size()
        def thisTherm = null
        def HSP = null
        def thisHSP = null
        def thisTemp = null
        def recorded = state.HSPMode.find{it.key == "$CurrMode"}?.value
        def Active = true

        log.debug "recorded for $CurrMode mode = $recorded"
        for(s != 0; i < s; i++){

            thisTherm = Thermostats[i]
            thisHSP = thisTherm.currentValue("heatingSetpoint")

            thisTemp = thisTherm.currentValue("temperature")
            /*log.debug """
        thisTherm = $thisTherm 
        thisHSP = $thisHSP
        """
        */

            // value is a map of [therm:temp] for current mode
            HSP = recorded.find{it.key == "${thisTherm}"}?.value



            // check if user selected a third party sensor
            def useAltSensor = state.AltSensorMap.find{it.toString().contains("$thisTherm")} as Boolean
            // also make sure to avoid redundency by checking if the related appliance is not
            // one managed by power measurement feature, which also adds / retracts values to the HSP
            def alreadyManaged = ApplianceWithPwMeter.find{it.toString().contains("$thisTherm")} as Boolean

            log.debug """recorded HSP for $thisTherm = $HSP ------- ($thisHSP = $HSP)
useAltSensor = $useAltSensor
alreadyManaged = $alreadyManaged
"""


            if(useAltSensor && !alreadyManaged && Active && inComfortMode()){
                log.debug "Altsensor HSP management......................................."
                def StringSensor = state.AltSensorMap.find{it.key == "$thisTherm"}.value
                // this is a string, so go get the device object from settings
                def theAltSensor = settings.find{it.toString().contains(StringSensor)}.value
                def AltSensorTemp = theAltSensor.currentValue("temperature")
                log.debug "third party sensor $theAltSensor returns a temperature of ${AltSensorTemp.toInteger()}°"

                // there's an alternate sensor controlling this device, so increase HSP if temperature is still below required
                // if current temperature is below current HSP (thisHSP), increase this HSP by adding 1 to current temperature used as HSP

                if(AltSensorTemp < comfort){
                    if(thisHSP.toInteger() < 80){  // 80 is currently the max heat value on the device
                        // note that comfort can't be the HSP here because thermostat might return a value equals or superior to comfort
                        // and so it won't heat while the alternate sensor returns a lower value thand desired

                        thisTherm.setHeatingSetpoint(AltSensorTemp.toInteger() + 1)
                        log.debug "$thisTherm HSP adjusted to fit its alternate sensor values (${AltSensorTemp.toInteger()} + 1)"
                    }
                    else {
                        log.debug "${thisTherm}'s HSP already set to $HSP (altsensor)"
                    }
                }
                else if(AltSensorTemp > comfort){
                    if(thisHSP.toInteger() - 1 > 68){

                        thisTherm.setHeatingSetpoint(thisHSP.toInteger() - 1)

                        log.debug "$thisTherm HSP adjusted to ${thisHSP.toInteger() - 1} "
                    }
                    else {
                        log.debug "${thisTherm}'s HSP already set too low"
                    }
                }
                else {
                    if(thisHSP.toInteger() + 1 < 80){

                        thisTherm.setHeatingSetpoint(thisHSP.toInteger() + 1)

                        log.debug "$thisTherm HSP adjusted to ${thisHSP.toInteger() + 1} "
                    }
                    else {
                        log.debug "${thisTherm}'s HSP already set too high"
                    }
                }

            }

            else {
                setSPreq() //state.SPreqFromApp set to TRUE

                def GetMotionData = ActiveTest(thisTherm)
                log.debug "GetMotionData = $GetMotionData"
                Active = GetMotionData[0] 
                if(!Active){
                    HSP = HSP.toInteger() - HeatNoMotion
                    log.debug "HSP for $ThisTherm lowered because there's no motion in its vicinity"
                }
                else if(thisHSP.toInteger() < comfort && inComfortMode()){
                    HSP = comfort
                    log.debug "${thisTherm}'s HSP adjusted to comfort setting"
                }
                if(thisHSP.toInteger() != HSP.toInteger()){
                    thisTherm.setHeatingSetpoint(HSP) 
                    log.debug "$thisTherm set to $HSP"
                }
                else {
                    log.debug "${thisTherm}'s HSP already set to $HSP -"
                }
            }
        }

    }
    else {
        log.debug "saving power mode"
        Thermostats.setHeatingSetpoint(saveModeHSP)
    }
}
def setCSPs(){

    int i = 0
    int s = Thermostats.size()
    def thisTherm = null
    def CSP = null
    def thisCSP = null

    def CurrMode = location.currentMode

    def recorded = state.CSPMode.find{it.key == "$CurrMode"}?.value // value is a map of [therm:temp] for current mode

    // log.debug "recorded for $CurrMode mode = $recorded"

    for(s != 0; i < s; i++){
        thisTherm = Thermostats[i]
        thisCSP = thisTherm.currentValue("coolingSetpoint")
        log.debug """
thisTherm = $thisTherm 
thisCSP = $thisCSP
"""

        CSP = recorded.find{it.key == "${thisTherm}"}?.value
        // log.debug "recorded CSP for $thisTherm = $CSP ------- ($thisCSP = $CSP)"
        if("$thisCSP" != "$CSP"){
            state.SPreqFromApp = true
            thisTherm.setCoolingSetpoint(CSP) 
        }
        else {
            // log.debug "${thisTherm}'s CSP already set to $CSP"
        }
    }

}

def inComfortMode(){
    boolean result = true // default for when user didn't pick this option
    def CurrMode = location.currentMode

    if(comfortMode){
        if(CurrMode in comfortMode){
            result = true
        }
        else {
            result = false
        }
    }
    log.debug "inComfortMode() returns $result"
    return result
}


def resetSPreq(){
    state.SPreqFromApp = false // 
    log.debug "state.SPreqFromApp reset to FALSE"
}
def setSPreq(){
    state.SPreqFromApp = true // 
    log.debug "state.SPreqFromApp reset to TRUE"
}

def resetModeJustChanged(){
    state.modeJustChanged = false
    log.debug "resetting recent mode change, app can resume setting HSPs from learning database"
}


