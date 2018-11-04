definition(
    name: "A.I. Thermostat Manager",
    namespace: "ELFEGE",
    author: "ELFEGE",

    description: """
Let this A.I. adjust your thermostats to your liking based on your habits (please, allow a couple days for it to fully learn from your inputs)
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


            input(name: "outsidetemp", type: "capability.temperatureMeasurement", title: "Select temperature a sensor", required: true, multiple: false,
                  description: "Select a temperature sensor", submitOnChange: true)   

            input(name: "lowTemp", type: "number", required: true, title: "Set thermosats to Heat when below this temperature", submitOnChange: true)

            input(name: "highTemp", type: "number", required: true, title: "Set thermosats to Cool when above this temperature", submitOnChange: true)
            ///////////////////////////////////
            input(name: "comfortL", type: "number", required: true, title: "Set lowest cooling threshold", submitOnChange: true)

            input(name: "comfortH", type: "number", required: true, title: "Set highest heating threshold", submitOnChange: true)


            input(name: "reqMode", type: "enum", required: true, title: "When a thermostat returns a temperature between $comfortL and $comfortH, set it to this mode", 
                  options:["auto", "off"])


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
                    learn()
                }
            }
            if(AI){
                input(name: "learnSP", type: "bool", title: "Check current temperature settings and use them as reference", default: false, submitOnChange: true)
                input(name: "learnSPMode", type: "bool", title: "Listen to location mode changes and record new setpoints", default: false, submitOnChange: true)
                if(outsidetemp){
                    input(name: "algebra", type: "bool", title: "Use some math", default: false, submitOnChange: true)
                    paragraph "Set points will be modified with outside's temperature"
                }
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

    // reset state.SPreqFromApp
    state.SPreqFromApp = false

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
    log.debug "$evt.device returns $evt.value degrees Farenheit"

    eval()
}
def ChangedModeHandler(evt){

    log.debug "Home is now in ${evt.value} mode"
    //runIn(5, learn)
    eval()

}

def setpointChangeHandler(evt){

    log.debug "$evt.device $evt.name $evt.value -------source: $evt.source --" 
    if(state.SPreqFromApp == false){
        runIn(3, learn) // delay requested to avoid overflow when user press several time the raise temp button
    }
    else {
        log.info "learning not triggered because cmd came from within the app"
        runIn(5, resetSPreq) 
    }

}

def resetSPreq(){
    state.SPreqFromApp = false // 
}

def contactHandler(evt){
    log.debug "$evt.device is $evt.value"
    state.value = evt.value
    eval()
}

def eval(){
    // reset state.SPreqFromApp
    state.SPreqFromApp = false

    /*log.info """
	state.SPreqFromApp = $state.SPreqFromApp
    state.learnedHSP = $state.learnedHSP
    state.learnedCSP = $state.learnedCSP
    state.HSPMode = $state.HSPMode
    state.CSPMode = $state.CSPMode
    """*/


    if("open" in contacts.currentValue("contact")){

        if(criticalSensor){
            if(criticalSensor.currentValue("temperature") <= threshold){
                log.debug "It's too cold, not turning off thermostats"
                setThermostats()
            }
        }
        else {

            // turn off all thermosats
            Thermostats.setThermostatMode("off")
        }
    }

    else {

        setThermostats()

    }
}

def setThermostats(){
    def i = 0;
    def s = Thermostats.size()
    def thisTherm = null
    def thisTemp = null
    def outside = outsidetemp.currentValue("temperature")
    //log.debug "outside temperature is: $outside"

    for(s != 0; i < s; i++){

        // define the device we're working with in this iteration of the loop
        thisTherm = Thermostats[i]
        //log.debug "$thisTherm current mode is : ${thisTherm.currentValue("thermostatMode")}"

        // check if user selected a third party sensor
        def useAltSensor = state.AltSensorMap.find{it.toString().contains("$thisTherm")} as Boolean
        //log.debug "$thisTherm was found in this map: $state.AltSensorMap ? >> $useAltSensor -"

        // get this thermostat's or its related third party sensor's current temperature
        if(useAltSensor){
            def StringSensor = state.AltSensorMap.find{it.key == "$thisTherm"}.value
            //log.debug "StringSensor is $StringSensor " //||||||||||||| settings = $settings"
            // this is a string, so go get the device object from settings

            def theAltSensor = settings.find{it.toString().contains(StringSensor)}.value
            //log.debug "theAltSensor is $theAltSensor"
            thisTemp = theAltSensor.currentValue("temperature")
            //log.debug "third party sensor $theAltSensor returns a temperature of $thisTemp"
        }
        else {
            thisTemp = thisTherm.currentValue("temperature")
            //log.debug "$thisTherm returns a temperature of $thisTemp"
        }


        // Now, assess needs: do we currently need heat or cooling ? 
        // if heat, then check that current temperature is ok, if not, set to heat
        // and the same goes with cooling
        if(outside <= lowTemp){ // if it's cold outside
            //log.debug "$thisTherm temperature is: $thisTemp"
            if(thisTemp >= comfortH){
                // we're comfortable, so set thermostat to the requested mode
                if(thisTherm.currentValue("thermostatMode") != reqMode){
                    thisTherm.setThermostatMode(reqMode)
                    //log.debug "$thisTherm set to $reqMode"
                }
                else {
                   // log.debug "$thisTherm already set to $reqMode"
                }

            }
            else {
                // heating is needed
                if(thisTherm.currentValue("thermostatMode") != "heat"){
                    thisTherm.setThermostatMode("heat")
                    //log.debug "$thisTherm set to heat"
                }
                else {
                    //log.debug "$thisTherm already set to heat"
                }

            }
        }
        else if(outside >= highTemp){ // if it's warm outside
           // log.debug "$thisTherm temperature is: $thisTemp"
            if(thisTemp <= comfortL){
                // we're comfortable, so set thermostat to the requested mode
                if(thisTherm.currentValue("thermostatMode") != reqMode){
                    thisTherm.setThermostatMode(reqMode)
                    //log.debug "$thisTherm set to $reqMode"
                }
                else {
                    //log.debug "$thisTherm already set to $reqMode"
                }

            }
            else {
                // cooling is needed
                if(thisTherm.currentValue("thermostatMode") != "cool"){
                    thisTherm.setThermostatMode("cool")
                  //  log.debug "$thisTherm set to cool"
                }
                else {
                   // log.debug "$thisTherm already set to cool"
                }

            }
        }
        else { // if it's nor cold nor warm outside
            log.debug "turning off $thisTherm because it's nor cold nor hot outside"
            if(thisTherm.currentValue("thermostatMode") != "off"){
                thisTherm.setThermostatMode("off")
            }
            else {
              //  log.debug "$thisTherm already off"
            }
        }
    }
    setHSPs()
    setCSPs()
}

def learn(){

    //runIn(10, eval) // for test only

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


    for(s != 0; i < s; i++){
        thisTherm = Thermostats[i]
        thisTemp = thisTherm.currentValue("temperature")

        thisHSP = thisTherm.currentValue("heatingSetpoint")
        thisCSP = thisTherm.currentValue("coolingSetpoint")


        log.debug """ 
        @ loop $i
        thisHSP for $thisTherm = $thisHSP
    	thisCSP for $thisTherm = $thisCSP
    	"""


        // what has been set by users upon mode changes and routines becomes new norm

        state.learnedHSP << ["${thisTherm}" : "${thisHSP}"] 
        state.learnedCSP << ["${thisTherm}" : "${thisCSP}"] 
        state.HSPMode."${CurrMode}" = state.learnedHSP 
        state.CSPMode."${CurrMode}" = state.learnedCSP 

    }

    log.trace """

    state.learnedHSP = $state.learnedHSP
    state.learnedCSP = $state.learnedCSP
    state.HSPMode = $state.HSPMode
    state.CSPMode = $state.CSPMode
    """

}

def setHSPs(){
    int i = 0
    int s = Thermostats.size()
    def thisTherm = null
    def HSP = null
    def thisHSP = null

    def CurrMode = location.currentMode


    def recorded = state.HSPMode.find{it.key == "$CurrMode"}?.value
    // if null, need to learn for current mode
    if(recorded == "null"){
        log.debug "null value, learning now"
        learn()
        recorded = state.HSPMode.find{it.key == "$CurrMode"}?.value
    }

    log.debug "recorded for $CurrMode mode = $recorded"
    
   

    for(s != 0; i < s; i++){
        thisTherm = Thermostats[i]
        thisHSP = thisTherm.currentValue("heatingSetpoint")
        log.debug """
        thisTherm = $thisTherm 
        thisHSP = $thisHSP
        """

        // value is a map of [therm:temp] for current mode
        HSP = recorded.find{it.key == "${thisTherm}"}?.value

        log.debug "recorded HSP = $HSP ------- ($thisHSP = $HSP)"


        if("$thisHSP" != "$HSP"){
            state.SPreqFromApp = true
            log.debug "state.SPreqFromApp set to TRUE -----------------" 
            thisTherm.setHeatingSetpoint(HSP) 
        }
        else {
            log.debug "${thisTherm}'s HSP already set to $HSP"
        }
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
    if(recorded == "null"){
        log.debug "null value, learning now"
        learn()
        recorded = state.CSPMode.find{it.key == "$CurrMode"}?.value
    }
    log.debug "recorded for $CurrMode mode = $extract"

    for(s != 0; i < s; i++){
        thisTherm = Thermostats[i]
        thisCSP = thisTherm.currentValue("coolingSetpoint")
        log.debug """
thisTherm = $thisTherm 
thisCSP = $thisCSP
"""

        CSP = recorded.find{it.key == "${thisTherm}"}?.value
        log.debug "recorded CSP = $CSP ------- ($thisCSP = $CSP)"
        if("$thisCSP" != "$CSP"){
            state.SPreqFromApp = true
            thisTherm.setCoolingSetpoint(CSP) 
        }
        else {
            log.debug "${thisTherm}'s CSP already set to $CSP"
        }
    }
}

