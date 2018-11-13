definition(
    name: "A.I. Thermostat Manager",
    namespace: "ELFEGE",
    author: "ELFEGE",

    description: """
This A.I. adjusts your thermostats to your liking based on your habits and other inputs(please, allow a couple days for it to fully learn from your inputs)
""" ,
    category: "Green Living",

    iconUrl: "https://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561f5a803bb6e85354945/1506107894030/penrose.jpg",
    iconX2Url: "https://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561f5a803bb6e85354945/1506107894030/penrose.jpg",
    image: "https://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561f5a803bb6e85354945/1506107894030/penrose.jpg"
)

preferences {

    page name: "MainPage"
    page name: "settings"
    page name: "AI"
    page name: "comfortSettings"
    page name: "SaveModes"

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
            href "SaveModes", title: "Save Power When Away", description: ""

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

            input(name: "comfortH", type: "number", required: true, title: "Set a maximum heating temperature", submitOnChange: true)
            input(name: "comfortL", type: "number", required: true, title: "Set a minimum cooling temperature", submitOnChange: true)


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
                    state.learnedHSP = [:] 
                    state.learnedCSP = [:] 
                    state.HSPMode = [:] 
                    state.CSPMode = [:]
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
                              submitOnChange: true)

                        paragraph "power consumption based set points won't work when outside temperature is below 34°F"
                    }
                }
            }
        }
    }
}
def SaveModes(){

    def pageProperties = [
        name:       "SaveModes",
        title:      "Power Saving Modes",
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

def installed() {
    log.debug "Installed with settings: ${settings}"

    // A.I. learning base maps, must not be reset anywhere else but at install 
    state.learnedHSP = [:] 
    state.learnedCSP = [:] 
    state.HSPMode = [:] 
    state.CSPMode = [:] 

    initialize()
}
def updated() {
    log.debug "updated with settings: ${settings}"
    unsubscribe()
    unschedule()
    initialize()
}
def initialize() {
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
    eval()

}

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


    eval()
    adjust()
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
def setpointChangeHandler(evt){

    log.debug "$evt.device $evt.name $evt.value -------source: $evt.source -- evt.name = $evt.name" 

    if(state.SPreqFromApp == false){
        int i = 0
        int s = Thermostats.size()
        def thisTherm = null
        def thisTemp = null
        def thisHSP = null
        def thisCSP = null

        //state.learnedHSP = [:] 
        //state.learnedCSP = [:] 
        // state.HSPMode = [:] 
        // state.CSPMode = [:] 

        def CurrMode = location.currentMode

        thisTherm = evt.device
        thisTemp = evt.value

        if(evt.name == "heatingSetpoint"){
            thisHSP = thisTherm.currentValue("heatingSetpoint")
            state.learnedHSP << ["${thisTherm}" : "${thisHSP}"] 
            state.HSPMode."${CurrMode}" = state.learnedHSP 
        }
        else if(evt.name == "coolingSetpoint"){
            thisCSP = thisTherm.currentValue("coolingSetpoint")
            state.learnedCSP << ["${thisTherm}" : "${thisCSP}"] 
            state.CSPMode."${CurrMode}" = state.learnedCSP 
        }


        log.trace """

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

    adjust()
}

def adjust(){
    boolean ModeOk = location.currentMode in OkToAdjustModes
    boolean contactsOpen = "open" in contacts.currentValue("contact")
    def comfort = getComfortH()

    boolean inSavingMode = location.currentMode in saveModes

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
            def tooColdOutside = false

            for(s > 0; i < s; i++){
                thisAppliance = Thermostats.find{it.displayName == "${ApplianceWithPwMeter[i]}"}            
                thisHSP = thisAppliance.currentValue("heatingSetpoint")
                thisCSP = thisAppliance.currentValue("coolingSetpoint")
                pwmeter = settings.find{it.key == "powerMeter${i.toString()}"}?.value

                log.debug "$thisAppliance is related to this power meter: $pwmeter"


                // check if user selected a third party sensor
                def useAltSensor = state.AltSensorMap.find{it.toString().contains("$thisAppliance")} as Boolean
                //log.debug "$thisTherm was found in this map: $state.AltSensorMap ? >> $useAltSensor -"

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

                // now, if it's a HeatPump, check outside's temperature isn't too low
                if(HeatPumps){
                    def ThisHeatPump = HeatPumps.find{it.toString().contains("$thisAppliance")} as Boolean
                    if(ThisHeatPump){
                        def outside = outsidetemp.currentValue("temperature")
                        if(outside < 34){
                            tooColdOutside = true
                            log.debug "$thisAppliance is a Heat Pump and it is too cold outside."
                        }
                        else {
                            //tooColdOutside = false // false is default value
                            log.debug "$thisAppliance is a Heat Pump but it isn't too cold outside."
                        }
                    }
                }

                boolean needAdjust = false

                def thermMode = thisAppliance.currentValue("thermostatMode")
                if(thermMode == "heat"){
                    if(pwmeter.currentValue("power") < 200 && thisTemp < thisHSP){
                        needAdjust = true
                    }
                }
                else if(thermMode == "cool"){
                    if(pwmeter.currentValue("power") < 200 && thisTemp > thisCSP){
                        needAdjust = true
                    }
                }

                if(needAdjust){
                    if(thisAppliance.currentValue("thermostatMode") == "heat" && !tooColdOutside && ModeOk){                
                        resetSPreq() // make sure A.I. learns new value                   
                        // if current temperature is below heat comfort + 2
                        log.debug "thisTemp = $thisTemp, comfort = ${comfort}"
                        if(thisTemp < comfort){
                            // we're too far below comfort zone
                            log.debug "adjusting HSP because $pwmeter returns a value lower than expected"
                            // increase the setpoint
                            thisAppliance.setHeatingSetpoint(thisHSP + 1)
                        }

                    }
                    else if(thisAppliance.currentValue("thermostatMode") == "cool"){
                        log.debug "adjusting HSP because $pwmeter returns a value lower than expected"
                        resetSPreq() // make sure A.I. learns new value

                        // if current temperature is above cooling comfort + 2
                        if(thisTemp > comfort){

                            thisAppliance.setcoolingSetpoint(comfort)
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
        }
    }

}

def eval(){

    log.info """
	state.SPreqFromApp = $state.SPreqFromApp
    state.learnedHSP = $state.learnedHSP
    state.learnedCSP = $state.learnedCSP
    state.HSPMode = $state.HSPMode
    state.CSPMode = $state.CSPMode
    """


    def contactsOpen = "open" in contacts.currentValue("contact")

    log.debug "contacts are: $contacts, some are open: $contactsOpen"

    if(contactsOpen){

        if(criticalSensor){
            if(criticalSensor.currentValue("temperature") <= threshold){
                log.debug "It's too cold, not turning off thermostats"
                setThermostats()
            }
            else {
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
        }
    }

    else {

        setThermostats()

    }
}
def setThermostats(){
    boolean inSavingMode = location.currentMode in saveModes
    if(!inSavingMode){
        def i = 0;
        def s = Thermostats.size()
        def thisTherm = null
        def thisTemp = null
        def thisHSP = null
        def thisCSP = null

        def outside = outsidetemp.currentValue("temperature").toInteger()
        //log.debug "outside temperature is: $outside"

        def CurrMode = location.currentMode

        def recordedHSP = state.HSPMode.find{it.key == "$CurrMode"}?.value // extract the map of HSPs' for this mode
        def recordedCSP = state.CSPMode.find{it.key == "$CurrMode"}?.value // extract the map of CSPs' for this mode

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
            thisHSP = thisHSP.toInteger()
            thisCSP = thisCSP.toInteger()
            thisTemp = thisTemp.toDouble()

            log.debug """
        thisHSP at setthermostats() is: $thisHSP
        thisCSP at setthermostats() is: $thisCSP
        """

            // check that this is not an appliance managed throuhg its power consumption criteria
            def alreadyManaged = ApplianceWithPwMeter.find{it.toString().contains("$thisTherm")} as Boolean

            def keepOff = keepOff(i, thisTemp, thisHSP, thisCSP, outside)
            log.debug "keepOff $keepOff"

            // Now, assess needs: do we currently need heat or cooling ? 
            // if heat, then check that current temperature is ok, if not, set to heat
            // and the same goes with cooling
            log.debug "outside: $outside lowTemp = $lowTemp"
            if(keepOff){
                if(reqMode != "do nothing" && !useAltSensor && !alreadyManaged){
                    if(thisTherm.currentValue("thermostatMode") != reqMode){
                        thisTherm.setThermostatMode(reqMode)

                        state.SPreqFromApp = true // do not allow the app to learn from its own cmds
                        runIn(5, resetSPreq)
                        setCSPs()
                        setHSPs()
                    }

                }

                // if "do nothing" was selected by user or this useAltSensor is true, we need to turn off 
                // a thermostat that is managed throuhg alternate sensor 
                // when desired temperature has been reached
                // providing it's not a thermostat managed through power consumption criteria
                else if(!alreadyManaged){

                    log.debug "turning off $thisTherm because its related temperature sensor returns a temperature of $thisTemp --//**"

                    if(thisTherm.currentValue("thermostatMode") != "off"){
                        thisTherm.setThermostatMode("off")
                    }

                }
            }
            else if(outside <= lowTemp){ // if it's cold outside
                log.debug "heating system" 
                // heating is needed
                if(thisTherm.currentValue("thermostatMode") != "heat"){

                    thisTherm.setThermostatMode("heat")
                    log.debug "$thisTherm set to heat"

                }
                else {
                    log.debug "$thisTherm already set to heat"
                }
                state.SPreqFromApp = true // do not allow the app to learn from its own cmds
                runIn(5, resetSPreq)
                setHSPs()

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
                runIn(5, resetSPreq)
                setCSPs()
            }


        }

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

    def outside = outsidetemp.currentValue("temperature")

    def xa = 70// outside temperature 
    def ya = 70 // min desired HSP 
    def xb = 0 //  outside temperature 
    def yb = 80 // max desired HSP 

    def coef = (yb-ya)/(xb-xa)

    def b = ya - coef * xa // solution to ya = coef*xa + b 

    int newComfortH = coef*outside + b 
    log.debug "coef = $coef, b = $b, outside temp = $outside, newComfortH = $newComfortH"

    return newComfortH

}
def setHSPs(){

    def CurrMode = location.currentMode
    boolean inSavingMode = CurrMode in saveModes

    if(!inSavingMode){
        log.debug "setting HSPs"

        int i = 0
        int s = Thermostats.size()
        def thisTherm = null
        def HSP = null
        def thisHSP = null
        def thisTemp = null
        log.debug "comfort = ${getComfortH()}"
        def comfort = getComfortH() 

        log.debug "inSavingMode: $inSavingMode"

        def recorded = state.HSPMode.find{it.key == "$CurrMode"}?.value

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

            log.debug "recorded HSP for $thisTherm = $HSP ------- ($thisHSP = $HSP)"

            // check if user selected a third party sensor
            def useAltSensor = state.AltSensorMap.find{it.toString().contains("$thisTherm")} as Boolean
            // also make sure to avoid redundency by checking if the related appliance is not
            // one managed by power measurement feature, which also adds / retracts values to the HSP
            def alreadyManaged = ApplianceWithPwMeter.find{it.toString().contains("$thisTherm")} as Boolean

            if(useAltSensor && !alreadyManaged){
                log.debug "Altsensor HSP management......................................."
                def StringSensor = state.AltSensorMap.find{it.key == "$thisTherm"}.value
                // this is a string, so go get the device object from settings
                def theAltSensor = settings.find{it.toString().contains(StringSensor)}.value
                def AltSensorTemp = theAltSensor.currentValue("temperature")
                log.debug "third party sensor $theAltSensor returns a temperature of ${AltSensorTemp.toInteger()}°"

                // there's an alternate sensor controlling this device, so increase HSP if temperature is still below required
                // if current temperature is below current HSP (thisHSP), increase this HSP by adding 1 to current temperature used as HSP
                if(AltSensorTemp < comfort){
                    if("$thisHSP" != "${AltSensorTemp.toInteger() + 1}"){ 
                        // note that comfort can't be the HSP here because thermostat might return a value equals or superior to comfort
                        // and so it won't heat while the alternate sensor returns a lower value thand desired

                        thisTherm.setHeatingSetpoint(AltSensorTemp.toInteger() + 1)
                        log.debug "$thisTherm HSP adjusted to fit its alternate sensor values (${AltSensorTemp.toInteger()} + 1)"
                    }
                    else {
                        log.debug "${thisTherm}'s HSP already set to $HSP"
                    }
                }
                else if(AltSensorTemp >= comfort){
                    if("$thisHSP" != "${thisHSP}"){

                        thisTherm.setHeatingSetpoint(comfort)

                        log.debug "$thisTherm HSP adjusted to fit comfort values"
                    }
                    else {
                        log.debug "${thisTherm}'s HSP already set to $thisHSP"
                    }
                }
                else {
                    if("$thisHSP" != "$AltSensorTemp"){

                        thisTherm.setHeatingSetpoint(AltSensorTemp.toInteger())

                        log.debug "$thisTherm HSP set to its alternate sensor's current value: ${AltSensorTemp.toInteger()}"
                    }
                    else {
                        log.debug "${thisTherm}'s HSP already set to $HSP"
                    }
                }

            }
            else if("$thisHSP" != "$HSP"){
                state.SPreqFromApp = true
                //log.debug "state.SPreqFromApp set to TRUE -----------------" 

                thisTherm.setHeatingSetpoint(HSP) 

            }
            else {
                log.debug "${thisTherm}'s HSP already set to $HSP"
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
def resetSPreq(){
    state.SPreqFromApp = false // 
    log.debug "state.SPreqFromApp reset to FALSE"
}
def resetModeJustChanged(){
    state.modeJustChanged = false
    log.debug "resetting recent mode change, app can resume setting HSPs from learning database"
}


