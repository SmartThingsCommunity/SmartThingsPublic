definition(
    name: " MULTI Thermostat Manager 6.0",
    namespace: "ELFEGE",
    author: "ELFEGE",
    description:  " All-in-One thermostat management : This App is meant to allow a certain level of A.I. in the management of your thermostats so you can combine several units running with different settings (for example for different rooms) and with different values for when you're away, at home, at night or in deep night. It also allows (tested) to save money on your power bill when using the 'fan only' feature, which will save a lot of cooling power. How it works: Select up to 3 thermostats each one set with its temperature setpoints, which can differ with each one of your home location mode (home/night/away + extras). It also contains an optional virtual thermostat, manages thermostats turnoff when windows or doors are open with a heat threshold above which HVAC will run on 'fan only', and a critical low antifreeze set point for safety. Does not interfere with manual settings so you still can change your AC desired temp., this app won't change it until there's a change of Home Mode, while it'll still refresh the units you didn't manually modify", 
    category: "Green Living",
    iconUrl: "http://elfege.com/penrose.jpg",
    iconX2Url: "http://elfege.com/penrose.jpg"
)

preferences {
    page name:"pageSetup"
    page name:"Thermostats"
    page name:"Modes"   
    page name:"TemperaturesSettings"
    page name:"VirtualThermostat"
    page name:"Options"
    page name:"EnergySaving"
    page name:"RefreshRates"   
    //page name:"Notifications"  

}

def pageSetup() {

    def pageProperties = [
        name:       "pageSetup",
        title:      "Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

    return dynamicPage(pageProperties) {
        section("About this App:"){
            paragraph "It allows you to select up to 3 thermostats set with individual temperature setpoints for each of them, by room, by mode (heat/cool/off/fan circulate/on) and by home location mode (home/night/away + one extra optional home mode). It also allows for an optional virtual thermostat and a keep-me-cozyII-like option. It also manages thermostats turnoff when windows or doors are open ; a critical low antifreeze set point for safety." 
            //paragraph image: "http://elfege.com/penrose.jpg",
        }
        section("Setup Menu") {
            href "Thermostats", title: "Choose your thermostats", description: ""
            href "Modes", title: "Define Home, Night and Away (and more) modes", description: ""
            href "TemperaturesSettings", title: "Define Temperatures for each Thermostat and Mode", description: ""
            href "VirtualThermostat", title: "(optional) Run a Virtual Thermostat (for a back up heater or AC)", description: ""
            href "Options", title: "Choose options such as central sensor, neutral interval to turn off units, doors sensors, outside sensor...", description: ""
            href "EnergySaving", title: "Save some Energy", description: ""
            //href "RefreshRates", title: "Force Smartthings to be more reactive ", description: ""
            //href "Notifications", title: "Notifications / push messages", description: "", state:greyedOut()
        }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

def Thermostats() {
    def thermostat = [
        name: "thermostat", 
        type: "capability.thermostat", 
        title: "First Thermostat",
        required: true, 
        multiple: false 
    ]

    def thermostat2 = [ 
        name: "thermostat2", 
        type: "capability.thermostat", 
        title: "Second Thermostat", 
        submitOnChange: true,
        required: false, 
        multiple: false
    ]

    def thermostat3 = [
        name: "thermostat3", 
        type: "capability.thermostat", 
        title: "Third Thermostat", 
        submitOnChange: true,
        required: false, 
        multiple: false
    ] 

    def pageName = "Thermostats"

    def pageProperties = [
        name:       "Thermostats",
        title:      "Thermostats Selection",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {

        section("Select the first thermostat you want to control"){
            input thermostat
        }

        section (""){
            input(name: "MoreThermostats", type: "bool", title: "Do you have more than 1 thermostat?", options: ["true","false"], 
                  submitOnChange: true, required: true, default: false)
        }
        if (MoreThermostats) {
            section {
                input(name: "thermostat2", type: "capability.thermostat", title: "choose a second thermostat", required: true)
                input(name: "thermostat3", type: "capability.thermostat", title: "choose a third thermostat", required: true)
            }
        }
    }
}

def Modes() {

    def MainMode = [
        name: "MainMode", 
        type : "mode", 
        title: "Pick a Home Mode", 
        multiple: false, 
        required: true
    ]

    def MainMode2a = [
        name: "MainMode2a", 
        type : "mode", 
        title: "Second Main Mode", 
        multiple: false, 
        required: false
    ]
    def MainMode2b = [
        name: "MainMode2b", 
        type : "mode", 
        title: "Third Main Mode", 
        multiple: false, 
        required: false
    ]

    def NightMode = [
        name: "NightMode", 
        type : "mode", 
        title: "Pick a Night Mode", 
        multiple: false, 
        required: true
    ]
    def NightMode2a = [
        name: "NightMode2a", 
        type : "mode", 
        title: "Pick auxiliary Night Mode", 
        multiple: false, 
        required: false
    ]
    def NightMode2b = [
        name: "NightMode2b", 
        type : "mode", 
        title: "Pick auxiliary Night Mode", 
        multiple: false, 
        required: false
    ]

    def AWAYMODE = [
        name: "AWAYMODE", 
        type : "mode", 
        title: "Pick an Away Mode", 
        multiple: false, 
        required: true
    ]

    def SPECIALMODE = [
        name: "SPECIALMODE", 
        type : "mode", 
        title: "(optional) Pick a special mode for specific type of activity (such as when you're watching tv or reading)", 
        multiple: false, 
        required: false
    ]
    def DEEPNIGHT = [
        name: "DEEPNIGHT", 
        type : "mode", 
        title: "(optional) Pick a second special mode for specific type of activity (such as deep sleep or working out...)", 
        multiple: false, 
        required: false
    ]


    def pageName = "Modes"

    def pageProperties = [
        name:       "Modes",
        title:      "Select Modes",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {

        section("Pick the mode that will define your thermostats' settings during the Day and when you're at home"){
            input MainMode
        }
        section("(optional) Another Mode To work as Home mode?"){
            input MainMode2a
        }
        section("(optional) Another Mode To work as Home mode?"){
            input MainMode2b
        }
        section("Pick the mode that will define your thermostats' settings at Night"){
            input NightMode
        }
        section("Pick the mode that will define your thermostats' settings at Night"){
            input NightMode2a
        }
        section("Pick the mode that will define your thermostats' settings at Night"){
            input NightMode2b
        }
        section("Pick the mode that will define your thermostats' settings when you're Away"){
            input AWAYMODE
        }
        section("Optional Special Mode (MUST NOT BE ANY OF PREVIOUSLY PICKED"){
            input SPECIALMODE
        }
        section("Second Optional Special Mode (MUST NOT BE ANY OF PREVIOUSLY PICKED"){
            input DEEPNIGHT
        }


    }
}

def TemperaturesSettings() {

    def HSAWAY = [
        name: "HSAWAY", 
        type : "decimal", 
        title: "choose Heating temperature for $AWAYMODE mode (will apply to all thermostats)", 
        multiple: false, 
        required: true
    ]

    def CSAWAY = [
        name: "CSAWAY", 
        type : "decimal", 
        title: "choose Cooling temperature for $AWAYMODE mode (will apply to all thermostats)", 
        multiple: false, 
        required: true
    ]

    // if in Main Mode (Home Mode) apply these settings instead

    def heatingSetpoint = [
        name: "heatingSetpoint", type: "decimal", title: "$MainMode Heating temp. for $thermostat", required: true
    ]
    def heatingSetpoint2 = [
        name: "heatingSetpoint2", type: "decimal", title: "$MainMode Heating temp. for $thermostat2", required: false
    ]
    def heatingSetpoint3 = [
        name: "heatingSetpoint3", type: "decimal", title: "$MainMode Heating temp. for $thermostat3", required: false 
    ]

    def coolingSetpoint = [
        name: "coolingSetpoint", type: "decimal", title: "$MainMode Cooling temp. for $thermostat", required: true
    ]
    def coolingSetpoint2 = [ 
        name: "coolingSetpoint2", type: "decimal", title: "$MainMode Cooling temp. for $thermostat2", required: false
    ]
    def coolingSetpoint3 = [ 
        name: "coolingSetpoint3", type: "decimal", title: "$MainMode Cooling temp. for $thermostat3", required: false 
    ]

    // if in NightMode apply these settings instead

    def heatingSetpointBIS = [
        name: "heatingSetpointBIS", type: "decimal", title: "$NightMode Heating Temp for $thermostat", required: true 
    ]
    def heatingSetpoint2BIS = [ 
        name: "heatingSetpoint2BIS", type: "decimal", title: "$NightMode Heating Temp for $thermostat2", required: false 
    ]
    def heatingSetpoint3BIS = [ 
        name: "heatingSetpoint3BIS",type: "decimal", title: "$NightMode Heating Temp for $thermostat3", required: false 
    ]
    def coolingSetpointBIS = [
        name: "coolingSetpointBIS", type: "decimal", title: "$NightMode cooling Temp for $thermostat", required: true 
    ]
    def coolingSetpoint2BIS = [ 
        name: "coolingSetpoint2BIS", type: "decimal", title: "$NightMode cooling Temp for $thermostat2", required: false 
    ]
    def coolingSetpoint3BIS = [ 
        name: "coolingSetpoint3BIS",type: "decimal", title: "$NightMode cooling Temp for $thermostat3", required: false 
    ]  

    // SPECIAL MODE TEMPERATURE SETTINGS

    def heatingSetpointSPEC = [
        name: "heatingSetpointSPEC", type: "decimal", title: "$SPECIALMODE Heat Temp for $thermostat", required: false 
    ]
    def heatingSetpointSPEC2 = [ 
        name: "heatingSetpointSPEC2", type: "decimal", title: "$SPECIALMODE Heat Temp for $thermostat2", required: false 
    ]
    def heatingSetpointSPEC3 = [ 
        name: "heatingSetpointSPEC3",type: "decimal", title: "$SPECIALMODE Heat Temp for $thermostat3", required: false 
    ]
    def coolingSetpointSPEC = [
        name: "coolingSetpointSPEC", type: "decimal", title: "$SPECIALMODE cooling Temp for $thermostat", required: false 
    ]
    def coolingSetpointSPEC2 = [ 
        name: "coolingSetpointSPEC2", type: "decimal", title: "$SPECIALMODE cooling Temp for $thermostat2", required: false 
    ]
    def coolingSetpointSPEC3 = [ 
        name: "coolingSetpointSPEC3",type: "decimal", title: "$SPECIALMODE cooling Temp for $thermostat3", required: false 
    ]  

    // DEEP NIGHT MODE TEMPERATURE SETTINGS

    def heatingSetpointDEEP = [
        name: "heatingSetpointDEEP", type: "decimal", title: "$DEEPNIGHT MODE Heat Temp for $thermostat", required: false 
    ]
    def heatingSetpointDEEP2 = [ 
        name: "heatingSetpointDEEP2", type: "decimal", title: "$DEEPNIGHT MODE Heat Temp for $thermostat2", required: false 
    ]
    def heatingSetpointDEEP3 = [ 
        name: "heatingSetpointDEEP3",type: "decimal", title: "$DEEPNIGHT MODE Heat Temp for $thermostat3", required: false 
    ]
    def coolingSetpointDEEP = [
        name: "coolingSetpointDEEP", type: "decimal", title: "$DEEPNIGHT MODE cooling Temp for $thermostat", required: false 
    ]
    def coolingSetpointDEEP2 = [ 
        name: "coolingSetpointDEEP2", type: "decimal", title: "$DEEPNIGHT MODE cooling Temp for $thermostat2", required: false 
    ]
    def coolingSetpointDEEP3 = [ 
        name: "coolingSetpointDEEP3",type: "decimal", title: "$DEEPNIGHT MODE cooling Temp for $thermostat3", required: false 
    ]  

    def pageName = "TemperaturesSettings"

    def pageProperties = [
        name:       "TemperaturesSettings",
        title:      "Temperatures Setup",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {

        section("Set the unique heating tempertaure to apply to all thermostats when you're Away"){
            input HSAWAY
        }
        section("Set the unique cooling tempertaure to apply to all thermostats when you're Away"){
            input CSAWAY
        }

        section("Set the desired heating themperatures for $MainMode"){
            input heatingSetpoint

            if(thermostat2){
                input(name: "heatingSetpoint2", type: "decimal", title: "$MainMode temperature on $thermostat2", required: true)

            }
            if(thermostat3){
                input(name: "heatingSetpoint3", type: "decimal", title: "$MainMode temperature on $thermostat3", required: true)
            }
        }

        section("Set the desired cooling themperatures for $MainMode"){
            input coolingSetpoint
            if(thermostat2){
                input(name: "coolingSetpoint2", type: "decimal", title: "$MainMode temperature on $thermostat2", required: true)

            }
            if(thermostat3){
                input(name: "coolingSetpoint3", type: "decimal", title: "$MainMode temperature on $thermostat3", required: true)
            }
        }
        section("Set the desired heating themperatures for $NightMode"){
            input heatingSetpointBIS
            if(thermostat2){
                input(name: "heatingSetpointBIS2", type: "decimal", title: "$NightMode heat temperature on $thermostat2", required: true)

            }
            if(thermostat3){
                input(name: "heatingSetpointBIS3", type: "decimal", title: "$NightMode heat temperature on $thermostat3", required: true)
            }
        }
        section("Set the desired cooling themperatures for $NightMode"){
            input coolingSetpointBIS
            if(thermostat2){
                input(name: "coolingSetpointBIS2", type: "decimal", title: "$NightMode cooling temperature on $thermostat2", required: true)

            }
            if(thermostat3){
                input(name: "coolingSetpointBIS3", type: "decimal", title: "$NightMode cooling temperature on $thermostat3", required: true)
            }
        }
        section("Set the desired heating themperatures for $SPECIALMODE"){
            input heatingSetpointSPEC
            if(thermostat2){
                input(name: "heatingSetpointSPEC2", type: "decimal", title: "$SPECIALMODE temperature on $thermostat2", required: true)
            }
            if(thermostat3){
                input(name: "heatingSetpointSPEC3", type: "decimal", title: "$SPECIALMODE temperature on  $thermostat3", required: true)
            }
        }
        section("Set the desired cooling themperatures for $SPECIALMODE"){
            input coolingSetpointSPEC
            if(thermostat2){
                input(name: "coolingSetpointSPEC2", type: "decimal", title: "$SPECIALMODE temperature on  $thermostat2", required: true)
            }
            if(thermostat3){
                input(name: "coolingSetpointSPEC3", type: "decimal", title: "$SPECIALMODE temperature on $thermostat3", required: true)
            }
        }
        section("Set the desired heating themperatures for $DEEPNIGHT MODE "){
            input heatingSetpointDEEP
            if(thermostat2){
                input(name: "heatingSetpointDEEP2", type: "decimal", title: "$DEEPNIGHT temperature on  $thermostat2", required: true)
            }
            if(thermostat3){
                input(name: "heatingSetpointDEEP3", type: "decimal", title: "$DEEPNIGHT temperature on $thermostat3", required: true)
            }
        }
        section("Set the desired cooling themperatures for $DEEPNIGHT MODE"){
            input coolingSetpointDEEP
            if(thermostat2){
                input(name: "coolingSetpointDEEP2", type: "decimal", title: "$DEEPNIGHT temperature on $thermostat2", required: true)
            }
            if(thermostat3){
                input(name: "coolingSetpointDEEP3", type: "decimal", title: "$DEEPNIGHT temperature on $thermostat3", required: true)
            }
        }


    }
}

def VirtualThermostat() {
    def Choose = [
        name: "Choose", type: "bool", title: "Use A Virtual Thermostat?", options: ["true","false"], required: true, default: false
    ]

    def VirtualThermostat = [
        name:"VirtualThermostat", type: "capability.temperatureMeasurement", title: "Choose a temperature sensor", required: false
    ]
    def VirtualSetPoint = [
        name: "VirtualSetPoint",type: "decimal", title: "Desired Temperature?", required: false 
    ]
    def switches = [
        name: "switches", type: "capability.switch", title: "Turn on/off These Switches", required: false, multiple: true, description: null
    ]
    def operatingMode = [
        name: "operatingMode", type: "enum", title: "Heating or cooling?", options: ["heat","cool"], required: false
    ]

    def pageName = "VirtualThermostat"

    def pageProperties = [
        name:       "VirtualThermostat",
        title:      "Virtual Thermostat",
        nextPage:   "pageSetup"
    ]


    return dynamicPage(pageProperties) {

        section("Do you need to use an extra independent heater or cooler using a virtual thermostat ? "){
            input Choose
        }
        section("Choose a temperature sensor"){
            input VirtualThermostat
        }
        section("Set a desired temperature"){
            input VirtualSetPoint
        }
        section("Select Switches to control"){
            input switches
        }

        section("Is it for a cooler or a heater ?"){
            input operatingMode
        }
    }

}

def Options() {

    def turnOffwindows = [
        name: "turnOffwindows", type:"bool", title: "turn off everything if windows are open?", default: true
    ] 
    def contact = [
        name: "contact", type:"capability.contactSensor", title: "select windows / contacts", multiple: true, required: false
    ] 
    def LowTempLimit = [ 
        name: "LowTempLimit" ,type:"decimal", title: "But Never go Below This Temperature:", required: false
    ]
    def HighTempLimit = [
        name: "HighTempLimit" ,type:"decimal", title: "And run HVAC fans if average temp is higher than:", required: false
        // this also applies to fanonly mode (all doors / windows closed) as a threshold to allow fancirculate to alternate with actual cooling
    ]
    def ExceptionThermostat = [ 
        name: "ExceptionThermostat", type : "capability.thermostat", title: "Make an exception for this unit", required: false
    ]

    def HighTempLimitException = [ 
        name: "HighTempLimitException" ,type:"decimal", title: "have its fan on if its temp is higher than: ", required: false

    ]

    def ExceptNight= [ 
        name: "ExceptNight", type : "bool", title: "However, don't run Fans if it's home is in $NightMode or $DEEPNIGHT mode", default: false
    ]

    def pageName = "Options"

    def pageProperties = [
        name:       "Options",
        title:      "Optional Settings",
        nextPage:   "pageSetup"
    ]


    return dynamicPage(pageProperties) {
        section(""){
            paragraph "here you will choose different optional features such as average temperature management, turning off units when windows are open "
        }
        section("Turn off all units some windows or doors are open"){
            input turnOffwindows
        }

        section("Pick windows / contacts"){
            input contact
        }
        section("But Never let the house go below this temperature (emergency heating)"){
            input LowTempLimit
        }
        section("And run HVAC fans if average temp is higher than this temperature"){
            input HighTempLimit
        }
        section("Make an exception for this unit"){
            input ExceptionThermostat
        }
        section("have its fan on if its temp is higher than: "){
            input HighTempLimitException
        }
        section("However, don't run Fans if home is in $NightMode or $DEEPNIGHT mode (and windows are open)"){
            input ExceptNight
        }
    }
}

def EnergySaving() {

    def OutsideSensor = [
        name: "OutsideSensor", type: "capability.temperatureMeasurement", title: "Choose an outside temperature sensor that will allow your AC to run on Fan Only when it's cool enough outside", required: false
    ]  
    def limitOutsideTemp = [
        name: "limitOutsideTemp" , type: "decimal", title: "Choose a threshold (default is 2)", required: false
    ]
    def ByPassFanCirculate = [
        name: "ByPassFanCirculate" , type: "bool", title: "Don't run this feature?", defaut: false
    ]

    def pageName = "EnergySaving"

    def pageProperties = [
        name:       "EnergySaving",
        title:      "Eco Friendly Options",
        nextPage:   "pageSetup"
    ]


    return dynamicPage(pageProperties) {
        section(""){
            paragraph "Save energy by running your units on fan only when outside temperature is low enough (when windows are closed heat might be generated byyour house's inertia)"
        }
        section("Select an outside temperature sensor"){
            input OutsideSensor
        }   
        section("How big must the difference of temperature be for your units to run on Fan Only?"){
            input limitOutsideTemp
        }
        section("HDo you wish to ignore this feature (AC at all times)?"){
            input ByPassFanCirculate
        }
    }
}

//////////////////////////////////////SETUP AND UPDATE///////////////////////////////

def installed() {	 
    log.debug "enter installed, state: $state"	
    log.debug "Installation function is RESETING STATE COUNT"

    subscribeToEvents()

}

def updated() {
    log.debug "enter updated, state: $state"   
    log.debug "Current mode = ${location.mode}" 

    state.evaluateMustNotRun =  false
    state.CriticalTemp = false
    state.AllunitsMessage = 0
    state.TurnOffDoors = 0

    unschedule()
    unsubscribe()

    subscribeToEvents()
}

def subscribeToEvents() {

    subscribe(thermostat, "temperature", temperatureHandler)
    subscribe(thermostat2, "temperature", temperatureHandler)
    subscribe(thermostat3, "temperature", temperatureHandler)
    subscribe(ExceptionThermostat, "temperature", temperatureHandler)


    subscribe(VirtualThermostat, "LowTempLimit", temperatureHandler)
    subscribe(VirtualThermostat, "temperature", temperatureHandler)
    subscribe(VirtualThermostat, "switches", temperatureHandler)

    subscribe(thermostat, "thermostatMode", temperatureHandler)
    subscribe(thermostat2, "thermostatMode", temperatureHandler)
    subscribe(thermostat3, "thermostatMode", temperatureHandler)
    subscribe(ExceptionThermostat, "thermostatMode", temperatureHandler)

    subscribe(thermostat, "Mode", temperatureHandler)

    subscribe(contact, "contact", contactHandler)
    subscribe(contact, "contact.open", contactHandler)
    subscribe(contact, "contact.closed", contactHandler)

    subscribe(OutsideSensor, "temperature", temperatureHandler)

    subscribe(location, "routineExecuted", routineChanged)
    subscribe(location, "mode", ChangedModeHandler)	
    //subscribe(location, ChangedModeHandler)


    if (state.modeStartTime == null) {
        state.modeStartTime = 0
    }   

    variables()
}

////////////////////////////EVENT HANDLERS////////////////////////////////////////

def routineChanged(evt) {
    log.debug "routineChanged: $evt"

    // name will be "routineExecuted"
    log.debug "evt name: ${evt.name}"

    // value will be the ID of the SmartApp that created this event
    log.debug "evt value: ${evt.value}"

    // displayName will be the name of the routine
    // e.g., "I'm Back!" or "Goodbye!"
    log.debug "evt displayName: ${evt.displayName}"

    // descriptionText will be the name of the routine, followed by the action
    // e.g., "I'm Back! was executed" or "Goodbye! was executed"
    log.debug "evt descriptionText: ${evt.descriptionText}"


}

def temperatureHandler(evt) { 
    logtrace()

    state.evaluateMustNotRun = true 

    CriticalTemp()  //  get the averageTemp value needed for FanCirculate Evaluation
    TooHotOutside() // set limit to fancirculate criteria

    log.debug "temperatureHandler(evt) running"

    if(OutsideSensor){
        if(NeedFanOnly()){
            FanCirculate()
            log.debug "TemperatureHandler running FanCirculate"
        }
    }

    AverageTemp()
    limitOutsideTempValue()

    // evaluate() // running evaluate with this handler renders useless any manual setting by users. 
    // must not run variables() either from here, same reason but we need evaluation of temp for FanCirculate()
    // so setting a variable to indicate that this command comes from mode handler so variables() can run but not evaluate()


    variables()
}

def ChangedModeHandler(evt) {
    state.CountNeedToCoolMessage = 0
    state.RunEvaluateAgain = false 
    state.evaluateMustNotRun = false
    variables()

    state.modeStartTime = now() 

    log.debug "Current mode = ${location.mode}"
    log.debug "mode changed to ${evt.value}"

    def message = "Home is in ${evt.value} mode, applying settings accordingly"
    log.info message
    send(message)

    def CurrMode = location.currentMode

    state.countmessageHeat = 0

    log.debug "now evaluating"
    AverageTemp()
    limitOutsideTempValue()
    RunVirtualThermostat()

}

def contactHandler(evt) {
    state.evaluateMustNotRun = false

    log.trace "state.ct = $state.ct, stat.CoolSet = $state.CoolSet, state.ct2 = $state.ct2, stat.CoolSet2 = $state.CoolSet2, state.ct3 = $state.ct3, stat.CoolSet3 = $state.CoolSet3"
    state.CountNeedToCoolMessage = 0
    state.countmessageHeat = 0
    state.messageDoors = 0 
    state.messageFansOn = 0
    state.TurnOffDoors = 0 // this variable is for allowing manual control of units despite open contacts. 

    log.debug "$evt.device is now $evt.value"

    if(contact.latestValue("contact").contains("open")) {
        if (CriticalTemp()) {
            log.debug "IT IS TOO COLD turning units back on" 
            JustHeat()           
        }
        else { 
            log.debug "It is NOT too cold and doors are open so shutting down units command is confirmed" 
            TurnOffDoors()   
        }
    } 
    else if (doorsOk()) {

        def messageBackOn = "$evt.device is now $evt.value, turning back on all thermostats"
        log.info messageBackOn
        send(messageBackOn)

        variables() // variables runs evaluate()

    }      
}

def coolingSetpointHandler(evt) {
    // for backward compatibility with existing subscriptions
    log.debug "coolingSetpointHandler()"

}

def heatingSetpointHandler(evt) {
    // for backward compatibility with existing subscriptions
    log.debug "heatingSetpointHandler()"

}

////////////////////////MAIN EVAL LOOPS///////////////////////////////

def variables() {

    state.threshold = 1
    state.tm = thermostat.currentThermostatMode
    state.ct = thermostat.currentTemperature
    state.fanMode = thermostat.currentthermostatFanMode
    state.TSH = thermostat.currentHeatingSetpoint
    state.TSC = thermostat.currentCoolingSetpoint

    if(thermostat2){
        state.tm2 = thermostat2.currentThermostatMode
        state.ct2 = thermostat2.currentTemperature
        state.fanMode2 = thermostat2.currentthermostatFanMode
        state.TSH2 = thermostat3.currentHeatingSetpoint 
        state.TSC2 = thermostat2.currentCoolingSetpoint
    }
    else {
        state.tm2 = state.tm
        state.ct2 = state.ct
        state.fanMode2 = state.fanMode
        state.TSH2 = state.TSH
        state.TSC2 = state.TSC 
    }

    if(thermostat3){
        state.tm3 = thermostat3.currentThermostatMode
        state.ct3 = thermostat3.currentTemperature
        state.fanMode3 = thermostat3.currentthermostatFanMode
        state.TSH3 = thermostat3.currentHeatingSetpoint
        state.TSC3 = thermostat3.currentCoolingSetpoint

    }
    else {
        state.tm3 = state.tm
        state.ct3 = state.ct
        state.fanMode3 = state.fanMode
        state.TSH3 = state.TSH
        state.TSC3 = state.TSC 
    }

    if(OutsideSensor) {
        state.outsideTemp = OutsideSensor.currentTemperature
    }

    log.debug "variables ________________________________________________ successfully updated!"

    if(state.evaluateMustNotRun != true ){
        state.evaluateMustNotRun = true // so it runs only once until new qualifying event

        evaluate()
    }
    else {
        log.debug "evaluate is not running because variables() was run by temperatureHandler -- however checking if FANONLY is needed"
        FanCirculate()
    }


}

def evaluate() {


    log.debug "EVALUATING _________________________________________________________ EVALUATING"


    def CurrMode = location.currentMode
    def message = "Settings for ${CurrMode} mode"
    log.info message

    if (CurrMode == NightMode || CurrMode == NightMode2a || CurrMode == NightMode2b) { 
        def verifc = " verif: Night-------------------------"
        send(message + verifc)
        NightMode() 
    } 
    else if (CurrMode == MainMode || CurrMode == MainMode2a || CurrMode == MainMode2b) { 
        def verifc = " verif: Home----------------------"
        send(message + verifc)

        MainMode()
    } 
    else if (CurrMode == AWAYMODE) { 
        def verifc = " verif: AWAY-----------Virt. Therm. is off-----------"
        send(message + verifc)
        switches.off() // making sure virtual thermostat is not running in AWAYMODE

        AWAYMODE()
    }
    else if (CurrMode == SPECIALMODE) { 
        send(message)
        SPECIALMODE()
    }
    else if (CurrMode == DEEPNIGHT) {
        send(message)
        DEEPNIGHT() 
    } 
    log.debug "Checking if Virtual Thermostat was selected"
    RunVirtualThermostat()
    log.debug "Evaluation successful"
}

def doublechecktemps(){

    // check that all thermostats have received commands for required settings

    if(thermostat.currentCoolingSetpoint != state.CoolSet){

        thermostat.setCoolingSetpoint(state.CoolSet)
        thermostat.setHeatingSetpoint(state.HeatSet)
        log.debug "$thermostat was not set properly. Fixing this now."

    }
    if(thermostat2){
        if(thermostat2.currentCoolingSetpoint != state.CoolSet2){
            thermostat2.setCoolingSetpoint(state.CoolSet2)
            thermostat2.setHeatingSetpoint(state.HeatSet3)
            log.debug "$thermostat2 was not set properly. Fixing this now."
        }
    }
    if(thermostat3){
        if(thermostat3.currentCoolingSetpoint != state.CoolSet3){
            thermostat3.setCoolingSetpoint(state.CoolSet3)
            thermostat3.setHeatingSetpoint(state.HeatSet3)
            log.debug "$thermostat3 was not set properly. Fixing this now."
        }
    }
    log.debug "DOUBLE CHECK OK"
}

////////////////////PRIVATE LOOPS/////////////////////////////////////
private send(msg) {
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone) {
            log.debug("sending text message")
            sendSms(phone, msg)
        }
    }

    log.debug msg
}

private doorsOk() {
    def result = true

    // if temp are still above too cold limit
    if(!contact) {
        // If contact sensors were not selected, we want the programm to still run tehrefore we define function doorsOk() as true
        log.debug "No windows or doors contacts were selected"
        log.debug "contact sensors were not selected, tehrefore we define function doorsOk() as true so the evaluation loop can continue"
        result = true      
    }
    else if (contact.latestValue("contact").contains("open")) {
        result = false
    } 
    else {
        result = true 
    }
    log.debug "all doors are closed : $result"
    return result


}

private RunVirtualThermostat(){

    if(Choose == true) {
        def CurrMode = location.currentMode

        if (doorsOk() && CriticalTemp() == false) {
            log.debug "(virt thermo) doors are closed"

            if (CurrMode != AWAYMODE) {

                def CurVirT = VirtualThermostat.currentTemperature
                def VirtualThreshold = 1.0

                log.debug "VIRTUAL THERMOSTAT OPTION WAS SELECTED BY USER"      
                log.debug "Virtual Thermostat Operating Mode is ${operatingMode}"
                log.debug "Virtual Thermostat SetPoint is ${VirtualSetPoint} "
                log.debug "Virtual Thermostat Operating Mode is ${operatingMode} "
                log.debug "Virtual Thermostat Current Temperature is ${CurVirT}  "

                if (operatingMode == "cool") { 
                    // Air Conditioner 
                    if ( CurVirT > VirtualSetPoint ) {
                        switches.on()
                    } else {
                        log.debug "Turning Off Virtual Thermostat's switches"
                        switches.off()
                    }
                    // Heater 
                } else if  (operatingMode == "heat") {
                    if ( CurVirT < VirtualSetPoint ) {
                        switches.on()
                    } else { 
                        log.debug "Turning Off Virtual Thermostat's switches"
                        switches.off()
                    }
                }
            }
            else { 
                log.debug "Home is in ${location.currentMode} mode so Virtual Thermostat is NOT RUNNING - turning switches off" 
                switches.off()
            }
        } 
        else if (CriticalTemp() && CurrMode != AWAYMODE) { 
            log.debug "Some windows or doors are open but temp is below critical so Virtual Thermostat IS RUNNING - turning switches on" 
            switches.on()
        }
        else { 
            log.debug "Virtual Thermostat is NOT RUNNING - turning switches off" 
            switches.off()
        }
    } 
    else {
        log.debug "Virtual Thermostat option WAS NOT SELECTED by user - turning switches off" 
        switches.off()
    }
}

private TurnOffDoors() { 

    state.countmessageHeat = 0

    if(state.TurnOffDoors == 0) {
        log.debug "state.CriticalTemp is : ${state.CriticalTemp}, TURNING OFF ALL UNITS"
        thermostat.off()
        thermostat.fanAuto()
        if(thermostat2){
            thermostat2.off()
            thermostat2.fanAuto()
        }
        else { def thermostat2 = "not selected"
             }
        if(thermostat3){
            thermostat3.off()
            thermostat3.fanAuto()
        }
        else { def thermostat3 = "not selected"
             }
        def messageDoors = "I turned off ${thermostat},  ${thermostat2} and ${thermostat3} because some doors or windows are open"
        log.info messageDoors
        state.TurnOffDoors = state.TurnOffDoors + 1 // variable allowing to turn units on manually RESET by contactHandler

        if (state.messageDoors == 0) {
            send(messageDoors)
            state.messageDoors = state.messageDoors + 1           
        }
    }
    else { 
        log.debug "Doors are open so NOT RUNNING TURNOFF DOORS again until next contact event" // allowing to turn units on manually 
    }
}

private setToAuto() { 
    log.debug "running setToAuto"
    thermostat.setThermostatMode("auto")
    if(thermostat2){
        thermostat2.setThermostatMode("auto")
    }
    if(thermostat3){
        thermostat3.setThermostatMode("auto") 
    }
}

private logtrace() { 


    log.trace("evaluate: $thermostat , Fan Mode: $state.fanMode, mode: $state.tm -- temp: $state.ct, heatSET: $state.HeatSet, coolSET: $state.CoolSet, coolCURR = $state.TSC" )
    if(thermostat2){
        log.trace("evaluate: $thermostat2, Fan Mode: $state.fanMode2, mode: $state.tm2 -- temp: $state.ct2, heatSET: $state.HeatSet2, coolSET: $state.CoolSet2, coolCURR = $state.TSC2")
    }
    if(thermostat3){
        log.trace("evaluate: $thermostat3, Fan Mode: $state.fanMode3, mode: $state.tm3 -- temp: $state.ct3, heatSET: $state.HeatSet3, coolSET: $state.CoolSet3, coolCURR = $state.TSC3")
    }

    if(OutsideSensor) {
        log.trace("evaluate: $OutsideSensor, temp: $state.outsideTemp")
    }
}

/////////////////////////////////// TEMPERATURE AND ENERGY MANAGEMENT//////////////////
private needToCool() { 
    log.debug "Outside temp is $state.outsideTemp"

    def result = null
    if(needToHeat() != true){
        if(state.outsideTemp + 2 < state.CoolSet ) {
            // this is another function to avoid having A.C. running while it's not warm outside
            // works only if user has an outside temp sensor though... 
            if(OutsideSensor) {
                result = false
                log.debug "all thermostats will be set to heat"
                setToHeat()
                if(state.CountNeedToCoolMessage == 0){
                    def message = "Outside temp is $state.outsideTemp, all thermostats will be set to heat"
                    log.info message
                    send(message)
                    state.CountNeedToCoolMessage = state.CountNeedToCoolMessage + 1
                }
            }
        }
        else if(state.ct >= state.CoolSet || state.ct2 >= state.CoolSet2 || state.ct3 >= state.CoolSet3) 
        {
            result = true
            log.debug "NEED TO COOL"
            state.CountNeedToCoolMessage = 0
        }

        else { 
            result = false
            log.debug "NO NEED TO COOL"
        }

    }
    else {
        result = false 
        log.debug "NeedToCool is set to FALSE because NEEDTOHEAT is TRUE"
        state.CountNeedToCoolMessage = 0
    }
    return result

}


private setToHeat() {
    // triggered by needToCool when too cold outside despite inside's temp inertia. 
    // this is another function to avoid having A.C. running while it's not warm outside
    // works only if user has an outside temp sensor though... 

    thermostat.setThermostatMode("heat")
    thermostat.setThermostatFanMode("auto")
    log.debug "$thermostat set to heat"
    if(thermostat2){
        thermostat2.setThermostatMode("heat")
        thermostat2.setThermostatFanMode("auto")
        log.debug "$thermostat2 set to heat"
    }
    if(thermostat3){
        thermostat3.setThermostatMode("heat")
        thermostat3.setThermostatFanMode("auto")
        log.debug "$thermostat3 set to heat"
    }
}

private needToHeat() { 

    def result = false

    if(state.ct <= state.HeatSet || state.ct2 <= state.HeatSet2 || state.ct3 <= state.HeatSet3 ) 
    {
        result = true
    }
    if(OutsideSensor){ 
        if(state.outsideTemp < state.HeatSet && state.AverageTemp < state.HeatSet)
        result = true
    } else if(state.AverageTemp < state.HeatSet) {
        result = true
    }
    log.debug "NEEDTOHEAT result is : $result"
    return result
}

private NeedFanOnly() {
    def result = null
    if(OutsideSensor) {
        if(TooHotOutside()){
            result = false // do not need fancirculate (warning this bool has reversed values compared to limitOUtsideTempPassed loops)        

        } 
        else if(state.AverageTemp  >  78 ){
            // to avoid accumulating too much heat threshold is set to 84 by default so at that temp fancicrulate won't work
            result = false

        }
        else {
            result = true

        }
        log.debug "NEEDFANONLY result: $result"
        return result 

    }
    else { 
        log.debug "FanCirculate option not running because it was not picked by user"
    }
}

private TooHotOutside() {

    def result = null

    limitOutsideTempValue()

    if(ByPassFanCirculate == true) {
        result = true
        log.debug "User chose to bypass FanCirculate and have AC running at all times"
    }
    else if(state.AverageTemp  < 78 /* set a heat threshold */ && needToHeat() == false /* avoid setting to cool when set for heat*/ ){
        // this loop is to avoid fancirculate to run despite uncomfortably high temps.

        log.debug "state.limitOutsideTemp is $state.limitOutsideTemp"
        log.debug "state.outsideTemp is $state.outsideTemp"

        state.ThresholdTempSum = state.AverageTemp + state.limitOutsideTemp as int

            log.debug "state.ThresholdTempSum is : $state.ThresholdTempSum"

        if(state.outsideTemp >= state.ThresholdTempSum  ) {
            result = true 
            log.debug "Outside Temperature is TOO HIGH to run fancirculate"  
        } 
        else { 
            result = false 
            log.debug "Outside Temperature is LOW ENOUGH for Fancirculate"

        }
        log.debug "Is the temperature Outside Too High to run Fancirculate? : $result"
        return result 

    }
    return result
}

private limitOutsideTempValue() {
    if(limitOutsideTemp) {
        state.limitOutsideTemp = limitOutsideTemp as int
            } 
    else { 
        state.limitOutsideTemp = 2
    }    

}

private JustHeat() {
    log.debug "JUSTHEAT is running"
    // set to heat if too cold
    if(doorsOk()) {
        if(needToHeat()) {
            thermostat.setThermostatMode("heat")
            thermostat.setThermostatMode("fanAuto")
            if(thermostat2){
                thermostat2.setThermostatMode("heat")
                thermostat2.setThermostatMode("fanAuto")
            }
            if(thermostat3){
                thermostat3.setThermostatMode("heat")
                thermostat3.setThermostatMode("fanAuto")
            }
        }
    }
    else { 
        log.debug "some doors are open, so skipping JustHeat" 
    }
}

private JustCool() { 

    log.debug "JUSTCOOL is running"

    if (doorsOk()) {
        if (needToCool()) {            
            Cool()
            log.debug "Setting thermostats to cool because average temperature is too high"   
        }
        else {
            log.debug "No need to cool, so just setting HVAC to Auto"
            setToAuto()
        }
    }
    log.debug "some doors are open, so skipping JustCool" 
}

private FanCirculate() {

    def result = false
    state.countmessageHeat = 0
    def CurrMode = location.currentMode
    def message = "..."

    log.debug "outsideTemp is $state.outsideTemp (FanCirculate())"
    log.debug "HighTempLimit for inside's average temp is $HighTempLimit"

    if(doorsOk()){
        if(TooHotOutside()) {  
            if(needToCool()) {
                log.debug "running cool() from fancirculate"
                Cool()   
                result = false // means "No fanCirculate option not running because not needed"
                state.AllunitsMessage = 0
                FansOff()
            }
        }
        else  {
            result = true
            // setting all units to fan only
            message = "All units set to FAN ONLY"
            log.info message 
            if(state.AllunitsMessage == 0){
                send(message)
                state.AllunitsMessage = state.AllunitsMessage + 1
            }
            FansOn()
        }
        log.debug "IS FanCirculate NEEDED ? : $result"
        return result
    }
    else if(ExceptionThermostat){

        if(ExceptionThermostat.currentTemperature > HighTempLimitException){

            if(ExceptNight){
                log.debug "ExceptNight was picked by user. If night then $thermostat 's fan won't run"
                if(CurrMode != NightMode && CurrMode != DEEPNIGHT){
                    ExceptionThermostat.setThermostatFanMode("on")
                }
                else {
                    log.debug "Exception Fan Mode NOT running because it's night."
                }
            } 
            else {
                ExceptionThermostat.setThermostatFanMode("on")
            }


            state.Fancirculate = true
            log.debug "Fan running on Exception Thermostat"
        }
        else {
            state.Fancirculate = false
            log.debug "Turning OFF fan on Exception Thermostat because temperature is OK"
            FansOff()
        }
    }


    if(state.AverageTemp >= HighTempLimit) {

        if(CurrMode != NighMode && CurrMode != DEEPNIGHT){
            FansOn()
            message = "Running Fans despite the fact that windows are open because average temp is superior to ${HighTempLimit}°"
            if(state.messageFansOn == 0){
                send(message)
                state.messageFansOn = state.messageFansOn + 1        
            }
        }
        else {
            FansOff() 
            state.messageFansOn = 0
        }
    }
    else {
        FansOff() 
        state.messageFansOn = 0
    }
}

private FansOff(){
    log.debug "turning fans off"

    if(ExceptionThermostat){
        if(state.Fancirculate == false){
            log.debug "All fans are set back to AUTO, NO EXCEPTION because even ${ExceptionThermostat}'s temperature is OK"
            thermostat.setThermostatFanMode("auto")
            if(thermostat2){
                thermostat2.setThermostatFanMode("auto")
            }
            if(thermostat3){
                thermostat3.setThermostatFanMode("auto")
            }
        }
        else if(ExceptionThermostat.currentTemperature == thermostat.currentTemperature){
            log.debug "turning off all fans but not $ExceptionThermostat"
            if(thermostat2){
                thermostat2.setThermostatFanMode("auto")
            }
            if(thermostat3){
                thermostat3.setThermostatFanMode("auto")
            }
        }
        else if(ExceptionThermostat.currentTemperature == thermostat2.currentTemperature){
            log.debug "turning off all fans but not $ExceptionThermostat"
            thermostat.setThermostatFanMode("auto")
            if(thermostat3){
                thermostat3.setThermostatFanMode("auto")
            }
        }
        else if(ExceptionThermostat.currentTemperature == thermostat3.currentTemperature){
            log.debug "turning off all fans but not $ExceptionThermostat"
            if(thermostat2){
                thermostat2.setThermostatFanMode("auto")
            }
            thermostat.setThermostatFanMode("auto")
        }
        else {
            log.debug "user seems to have picked a different thermostat as an Exception, so stopping all fans for all 3 main units"
            thermostat.setThermostatFanMode("auto")
            if(thermostat2){
                thermostat2.setThermostatFanMode("auto")
            }
            if(thermostat3){
                thermostat3.setThermostatFanMode("auto")
            }
        }

    } 
    else { 
        log.debug "all fans are turned off"
        thermostat.setThermostatFanMode("auto")
        if(thermostat2){
            thermostat2.setThermostatFanMode("auto")
        }
        if(thermostat3){
            thermostat3.setThermostatFanMode("auto")
        }
    }
}

private FansOn(){ 

    def CurrMode = location.currentMode

    if(doorsOk() == false){
        if(ExceptNight == true){
            if(CurrMode == NightMode || CurrMode == DEEPNIGHT){
                // if this option was selected by user
                // if mode is night
                // if windows are open
                log.debug "Home is in $CurrMode, windows are open and user doesn't want fans to run at night so fans are not being managed"
            }
        }
    }

    else {

        log.debug "turning fans on"
        thermostat.setThermostatMode("off")
        if(thermostat2){
            thermostat2.setThermostatMode("off")
        }
        if(thermostat3){
            thermostat3.setThermostatMode("off")
        }

        thermostat.setThermostatFanMode("on")
        if(thermostat2){
            thermostat2.setThermostatFanMode("on")
        }
        if(thermostat3){
            thermostat3.setThermostatFanMode("on")
        }


        if(doorsOk()){
            //runIn(60*5, evaluate)// needed to avoid being stuck on fan only mode
            state.evaluateMustNotRun = false // allowing variables() to run evaluate()
        }
        else{
            log.debug "doors are open (FansOn()) evaluating need for Fans"
            variables()
            state.evaluateMustNotRun = true // forbiding variables() to run evaluate() because windows are open
            evaluateFansWindowsOpen() // specific to windows open mode so we don't use main evaluate loop (too many risks for errors such as compressor running while windows open)
        }
    } 

}

private evaluateFansWindowsOpen() {

    if(state.AverageTemp < HighTempLimit) {
        def message = "average temp is no longer superior to ${HighTempLimit}°"             
        send(message)
        FansOff() 
        //unschedule(evaluateFansWindowsOpen)
    }
    else {
        log.debug "Average Temperature is still high so Fans are still running"
    }
}

private AverageTemp(){


    def sumtemps = state.ct + state.ct2 + state.ct3
    log.debug "sumtemps is: ${sumtemps}"
    def divider = 1

    if (thermostat && !thermostat2 && !thermostat3) {
        divider = 1
    } else if (thermostat && thermostat2 && !thermostat3) {
        divider = divider + 1
    } else if (thermostat && thermostat2 && thermostat3) {
        divider = divider + 2
    }

    int AverageTemp = sumtemps/divider

    state.AverageTemp = AverageTemp // used in loops such as TooHotOutside()
}

private CriticalTemp() {

    AverageTemp()

    def result = false
    state.CriticalTemp = false
    log.debug "Low Emergency Limit Temperature is : ${LowTempLimit}" 
    log.debug "state.AverageTemp is : ${state.AverageTemp}"


    if(state.AverageTemp <= LowTempLimit) {
        def messageCRITICALTEMP = "Average temperature is below  ${LowTempLimit} !!!! Turning on all heating units!!! "
        log.info messageCRITICALTEMP
        send(messageCRITICALTEMP)
        state.CriticalTemp = true
        result = true

        thermostat.setThermostatMode("heat")
        thermostat.setThermostatFanMode("auto")
        if(thermostat2){
            thermostat2.setThermostatMode("heat")
            thermostat2.setThermostatFanMode("auto")
        }
        if(thermostat3){
            thermostat3.setThermostatMode("heat")
            thermostat3.setThermostatFanMode("auto")
        }

    }

    log.debug "Critical temp value is: $result"
    return result
}

private Cool() {

    if(doorsOk() || !turnOffwindows){
        log.debug "running Cool"
        thermostat.setThermostatMode("cool")
        thermostat.setThermostatFanMode("fanAuto")

        if(thermostat2){
            thermostat2.setThermostatMode("cool")
            thermostat2.setThermostatFanMode("fanAuto")
        }
        if(thermostat3){
            thermostat3.setThermostatMode("cool")
            thermostat3.setThermostatFanMode("fanAuto")
        }
    }
    else { 
        log.debug "doors are open so ignoring Cool() command and making sure units are off"
        thermostat3.setThermostatMode("off")
        thermostat2.setThermostatMode("off")
        thermostat.setThermostatMode("off")
    }
}

private SettingsHeat() {

    thermostat.setHeatingSetpoint(state.HeatSet)
    if(thermostat2){
        thermostat2.setHeatingSetpoint(state.HeatSet2)
    }
    if(thermostat3){
        thermostat3.setHeatingSetpoint(state.HeatSet3)
    }
    log.debug "thermostats are now set to desired heating"

    JustHeat() // Will run heat if required

}

private SettingsCool() { 

    runIn(60, doublechecktemps)

    thermostat.setCoolingSetpoint(state.CoolSet)
    if(thermostat2){
        thermostat2.setCoolingSetpoint(state.CoolSet2)

    }
    if(thermostat3){
        thermostat3.setCoolingSetpoint(state.CoolSet3)
    }
    log.debug "thermostats are now set to desired cooling  " 

    // set to cool if too hot      
    if  (OutsideSensor) {
        FanCirculate() // will check if outside is cooler than inside and if so run on fans only (if cooling is needed), else will run AC if outside temperature is too high, if cooling is required             
    }
    else { 
        JustCool() // will run AC without checking outside temperature, if cooling is required

    }
}

////////////////////////////////// LOCATION MODES //////////////////////////////
private MainMode() {
    log.debug "MainModeS loop running. Current mode is Home, adjusting VARIABLES accordingly (MainModeS loop)"

    state.HeatSet = heatingSetpoint
    state.CoolSet = coolingSetpoint
    if(thermostat2){
        state.CoolSet2 = coolingSetpoint2
        state.HeatSet2 = heatingSetpoint2
    }
    if(thermostat3){
        state.CoolSet3 = coolingSetpoint3
        state.HeatSet3 = heatingSetpoint3
    }

    if (doorsOk() && state.CriticalTemp == false) {

        log.debug "now running temp SettingsHeat loop"
        SettingsHeat()

        log.debug "now running temp SettingsCool loops"
        SettingsCool()            

    } 
    else if (doorsOk() == false && turnOffwindows == true){ 
        state.messageDoors = 0
        state.TurnOffDoors = 0
        TurnOffDoors()
    }   

}
private NightMode() {
    log.debug "NightModes loop running"
    log.debug "Current mode is Night, adjusting VARIABLES accordingly (NightModeS loop)"
    state.CoolSet = coolingSetpointBIS
    state.HeatSet = heatingSetpointBIS
    if(thermostat2){
        state.CoolSet2 = coolingSetpointBIS2 
        state.HeatSet2 = heatingSetpointBIS2
    }
    if(thermostat3){
        state.CoolSet3 = coolingSetpointBIS3
        state.HeatSet3 = heatingSetpointBIS3
    }

    if (doorsOk() && state.CriticalTemp == false) {


        log.debug "now running temp SettingsHeat loop"
        SettingsHeat()

        log.debug "now running temp SettingsCool loop"
        SettingsCool()            
    }
    else if (doorsOk() == false && turnOffwindows == true){ 
        state.messageDoors = 0
        state.TurnOffDoors = 0
        TurnOffDoors()
    }
    else if (doorsOk() == false && turnOffwindows == true){ 
        state.messageDoors = 0
        state.TurnOffDoors = 0
        TurnOffDoors()
    }   
    logtrace()
}
private AWAYMODE() {
    log.debug "Home is in Away Mode, setting one temp for all thermostats (AWAYMODE Loop)"
    state.CoolSet = CSAWAY
    state.HeatSet = HSAWAY
    if(thermostat2){
        state.CoolSet2 = CSAWAY
        state.HeatSet2 = HSAWAY
    }
    if(thermostat3){
        state.CoolSet3 = CSAWAY
        state.HeatSet3 = HSAWAY
    }

    if (doorsOk() && state.CriticalTemp == false) {


        log.debug "now running temp SettingsHeat loop"
        SettingsHeat()

        log.debug "now running temp SettingsCool loops"
        SettingsCool()            
    }
    else if (doorsOk() == false && turnOffwindows == true){ 
        state.messageDoors = 0
        state.TurnOffDoors = 0
        TurnOffDoors()
    }   
}
private SPECIALMODE() {
    log.debug "Current mode is SPECIALMODE, adjusting VARIABLES accordingly -----------------------------------------------------------------"
    state.CoolSet = coolingSetpointSPEC
    state.HeatSet = heatingSetpointSPEC
    if(thermostat2){
        state.CoolSet2 = coolingSetpointSPEC2 
        state.HeatSet2 = heatingSetpointSPEC2
    }
    if(thermostat3){
        state.CoolSet3 = coolingSetpointSPEC3
        state.HeatSet3 = heatingSetpointSPEC3
    }
    if (doorsOk() && state.CriticalTemp == false) {
        log.debug "now running temp SettingsHeat loop"
        SettingsHeat()

        log.debug "now running temp SettingsCool loops"
        SettingsCool()            
    }
    else if (doorsOk() == false && turnOffwindows == true){ 
        state.messageDoors = 0
        state.TurnOffDoors = 0
        TurnOffDoors()
    }   
}
private DEEPNIGHT() {
    log.debug "Current mode is DEEPNIGHT, adjusting VARIABLES accordingly (DEEPNIGHT loop)"
    state.CoolSet = coolingSetpointDEEP
    state.HeatSet = heatingSetpointDEEP

    if(thermostat2){
        state.CoolSet2 = coolingSetpointDEEP2
        state.HeatSet2 = heatingSetpointDEEP2
    }
    if(thermostat3){
        state.CoolSet3 = coolingSetpointDEEP3
        state.HeatSet3 = heatingSetpointDEEP3
    }

    if (doorsOk() && state.CriticalTemp == false) {


        log.debug "now running temp SettingsHeat loop"
        SettingsHeat()

        log.debug "now running temp SettingsCool loops"
        SettingsCool()            
    }
    else if (doorsOk() == false && turnOffwindows == true){ 
        state.messageDoors = 0
        state.TurnOffDoors = 0
        TurnOffDoors()
    }   
}

