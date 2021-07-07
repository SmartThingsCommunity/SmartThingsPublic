definition(
    name: "A.I. Thermostat Manager",
    namespace: "ELFEGE",
    author: "ELFEGE",

    description: "This A.I. will manage a thermostat using inputs from different sources and help you save a lot of power",

    category: "Green Living",
    iconUrl: "https://www.philonyc.com/assets/penrose.jpg",
    iconX2Url: "https://www.philonyc.com/assets/penrose.jpg",
    iconX3Url: "https://www.philonyc.com/assets/penrose.jpg", 
    image: "https://www.philonyc.com/assets/penrose.jpg"
)


preferences {

    page name: "MainPage"
    page name: "settings"
    page name: "contactSensors"
    page name: "powerSaving"

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

        section("") {
            href "settings", title: "Thermostat", description: ""
            href "contactSensors", title: "Contacts sensors", description: ""
            href "powerSaving", title: "Save power", description: ""
        }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
        section("modes")
        {
            input(name: "modes", type: "mode", title:"set for specific modes", required:true, multiple:true, submitOnChange: true)
            input(name: "savingModes", type: "mode", title: "select modes under wich you wish to save power", required:false, multiple: true, submitOnChange: true)
            if(savingModes)
            {
                paragraph "Note that saving modes will increase cooling and lower heating set points"
            }
            if(modes)
            {
                input(name: "setThermostatOutOfMode", type: "enum", title: "When out of modes, set $Thermostat to this mode:", required: false, options: ["heat", "cool", "auto", "off"])
                if(setThermostatOutOfMode == "auto")
                {
                    paragraph "BEWARE THAT AUTO MODE IS THE OVERRIDE MODE. OPERATIONS WILL NOT RESUME UNTIL YOU SET $Thermostat BACK 'heat', 'cool' or 'off'!"
                }
            }
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

        section("Select the thermostat you want to control") { 

            input(name: "Thermostat", type: "capability.thermostat", title: "select a thermostat", required: true, multiple: false, description: null, submitOnChange:true)


            input(name: "outdoor", type:"capability.temperatureMeasurement",
                  title: "Adjust temperature with outside's weather",
                  description: "Select a weather station", 
                  required: false,
                  submitOnChange:true)
            input(name: "altSensor", type:"capability.temperatureMeasurement", title: "use a separate temperature sensor", required: false, submitOnChange:true)
            if(altSensor)
            {
                input(name: "offset", type: "number", range: "-20..20", title: "Optional: an offset for $altSensor temperature reports", required: false, defaultValue: 0)
            }
            if(!outdoor){
                input(name: "modes", type: "mode", title: "select the modes under which you want to control this thermostat", required: false, multiple: true, submitOnChange:true)

                if(modes){
                    int s = modes.size()
                    int i = 0
                    for(s!=0;i<s;i++)
                    {
                        input(name: "desired${modes[i].toString()}", type: "number", title: "Desired temperature when in ${modes[i]} mode", required: true, description: null)
                    }
                }
                else 
                {

                    input(name: "desired", type: "number", title: "Desired temperature", required: true, description: null)
                }
            }

            else 
            {
                paragraph "these are only indicative as basis for further A.I. calculations"
                input(name: "desiredoffset", type: "number", range: "-20..20", title: "Optional: Set Point Offset", required: false, defaultValue: 0, description: null)
            }
        }
    }
}

def contactSensors() {

    def pageProperties = [
        name:       "contactSensors",
        title:      "Contact sensors",
        nextPage:   null,
        install: false,
        uninstall: true
    ]

    dynamicPage(pageProperties) {

        log.debug "Thermostat = $Thermostat"

        section("Select at least one contact sensor per thermostat, if required") { 

            if(!Thermostat){       
                def message = "You haven't selected any thermostat yet. Return to main page"
                paragraph "$message"
                log.debug "$message"
            }
            else 
            {
                input(name: "contact", type:"capability.contactSensor", title: "Turn off ${Thermostat} when these contacts are open", multiple: true, required: false)

            }
        }
    }
}

def powerSaving() {

    def pageProperties = [
        name:       "powerSaving",
        title:      "Save power",
        nextPage:   null,
        install: false,
        uninstall: true
    ]

    dynamicPage(pageProperties) {

        section("Select motion parameters") { 
            if(Thermostat){             
                input(name: "motionSensors", type:"capability.motionSensor", 
                      title: "Turn off ${Thermostat} when these sensors reported no motion for a certain period of time", 
                      multiple: true,
                      submitOnChange:true,
                      required: false)
                input(name: "motionModes", type:"mode", 
                      title: "Use motion only in these modes", 
                      multiple: true,
                      submitOnChange:true,
                      required: false)
                input(name: "windows", type:"capability.switch", 
                      title: "Turn on a switch when it's too hot inside and temperature is nice outside", 
                      description: "select a switch",
                      multiple:true,
                      required: false, 
                      submitOnChange: true) 
                if(windows)
                {
                    input(name: "windowDelay", type:"number", 
                          title: "turn off after a certain time", 
                          description: "time in seconds",
                          required: false)
                    input(name: "windowsModes", type:"mode", 
                          title: "Use $windows only in these modes", 
                          multiple: true,
                          submitOnChange:true,
                          required: false)

                }
            }
            else 
            {
                def message = "You haven't selected any thermostat yet. Return to main page"
                paragraph "$message"
                log.debug "$message"
            }
        }
        section("Inactive Motion Time Limit")
        {
            if(motionSensors)
            {
                input(name: "noMotionTime", type: "number", required: true, title: "Lenght of time without motion", description: "Time in minutes")
            }
        }

    }
}



def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "updated with settings: ${settings}"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize()
{
    state.lastMessage = now()
    state.heatMode = 0
    state.coolMode = 0
    state.lastCoolMode = now()
    state.lastHeatMode = now()
    state.learnBase = [:]
    state.megaBase = []
    state.learnedDesiredTemp = 72
    state.learnedOutsideTemp = 50
    state.lastNeed = "cool"
    state.desired = 72
    state.closeTime = now() as long

        state.alreadyClosed = false
    state.alreadyOpen = false
    state.openByApp = true
    state.closedByApp = true

    log.debug "subscribing to events..."

    subscribe(location, "mode", ChangedModeHandler) 

    subscribe(Thermostat, "temperature", temperatureHandler)

    subscribe(Thermostat, "heatingSetpoint", setPointHandler)
    subscribe(Thermostat, "coolingSetpoint", setPointHandler)

    subscribe(Thermostat, "thermostatMode", temperatureHandler)

    subscribe(outdoor, "temperature", temperatureHandler)
    boolean hasfeelsLike = outdoor?.hasAttribute("feelsLike")
    boolean hasFeelsLike = outdoor?.hasAttribute("FeelsLike") // uppercase F
    boolean hasRealFeel = outdoor?.hasAttribute("RealFeel")
    boolean hasrealFeel = outdoor?.hasAttribute("realFeel")

    if(hasfeelsLike)
    {
        subscribe(outdoor, "feelsLike", temperatureHandler)
        log.debug "$outdoor has feelsLike attribute"
        state.FeelString = "feelsLike"
    }
    else if(hasFeelsLike)
    {
        subscribe(outdoor, "FeelsLike", temperatureHandler)
        log.debug "$outdoor has hasFeelsLike attribute (uppercase F)"
        state.FeelString = "FeelsLike"
    }
    else if(hasRealFeel)
    {
        subscribe(outdoor, "RealFeel", temperatureHandler)
        log.debug "$outdoor has RealFeel attribute"
        state.FeelString = "RealFeel"
    }
    else  if(hasrealFeel)
    {    
        subscribe(outdoor, "realFeel", temperatureHandler)
        //log.debug "$outdoor has realFeel attribute"
        state.FeelString = "realFeel"
    }

    else 
    {
        state.FeelString = null
        log.debug "$outdoor has NO REAL FEEL attribute"
    }


    if(altSensor)
    {
        subscribe(altSensor, "temperature", temperatureHandler)
    }



    if(contact){
        log.debug "subscribing $contact"
        subscribe(contact, "contact.open", contactHandler)
        subscribe(contact, "contact.closed", contactHandler)
        log.debug "$contact subscribed to events"
    }
    else 
    {
        log.debug "NO CONTACTS SELECTED! ----------------------------------"
    }

    /// MOTION SUBSCRIPTION
    if(motionSensors)
    {
        subscribe(motionSensors, "motion", motionSensorHandler)
        log.debug "$motionSensors subscribed to events"
    }
    else 
    {
        log.debug "$Thermostat has no motion sensor attached"
    }

    if(criticalSensors)
    {
        subscribe(criticalSensors, "temperature", temperatureHandler)

        log.debug "$criticalSensors subscribed to events"
    }

    schedule("0 0/1 * * * ?", eval)
    schedule("0 0/5 * * * ?", Poll)
    Poll()
    eval()

    log.info "---------------------------------------END OF INITIALIZATION --------------------------------------------"

}

def ChangedModeHandler(evt)
{
    log.debug "Home is now in ${evt.value} mode"

    if(evt.value in modes || evt.value in savingModes)
    {
        log.debug "new mode is within parameters..."
        boolean inWindowsMode = location.currentMode in windowsModes
        if(!inWindowsMode)
        {
            windows?.off()
            closeWindows()
        }
    }
    else 
    {
        log.debug "Mode is outside operations"
        if(setThermostatOutOfMode)
        {
            log.debug "Thermostat is now set to $setThermostatOutOfMode"
            Thermostat.setThermostatMode("setThermostatOutOfMode")
        }
    }

}


def setPointHandler(evt){

    def thisTemp = evt.value 
    def eventName = evt.name
    thisTemp = thisTemp.toInteger()

    def currMode = location.currentMode
    def outsideTemp = outdoor.currentValue("temperature")
    def thermMode = Thermostat.currentValue("thermostatMode")
    boolean override = thermMode == "auto" //// OVERRIDE BOOLEAN 
    boolean NoLearnMode = state.NoLearnMode

    def humidity = getHumidity()

    log.debug """ --LEARNING-- state.NoLearnMode = $state.NoLearnMode evt.source = $evt.source && evt.name = $evt.name && evt.value = $evt.value"""

    def comfort = null

    boolean consistent = (state.lastNeed == "heat" && evt.name == "heatingSetpoint") || (state.lastNeed in "cool" && evt.name == "coolingSetpoint")


    if(!NoLearnMode && !override && consistent)
    {
        log.debug "learning new desired temperature.."
        state.learnedDesiredTemp = thisTemp
        state.learnedOutsideTemp = outsideTemp
        //recordData(thisTemp, outsideTemp, humidity, currMode)
    }
    else if(override)
    {
        log.debug "NOT LEARNING DUE TO OVERRIDE MODE"
    }
    else if(!consistent)
    {
        log.debug "NOT LEARNING from this input ($evt.name: $evt.value) because it isn't consistent with current thermostat's mode"
    }
    else if(NoLearnMode)
    {
        log.debug "NOT LEARNING from this input because NoLearnMode has been called"
    }
    state.NoLearnMode = false
}

def motionSensorHandler(evt)
{
    log.debug """$evt.device returns $evt.value

        """
}

def temperatureHandler(evt)
{
    log.debug "$evt.device returns ${evt.name}:${evt.value}"

    // measure consistency
    // count how many changes of operating state have occured in the last 10 minutes
    // if more than x, then we're having erratic ocscillations due to mid-season 
    if(evt.value == "cool")
    {
        state.coolMode = state.coolMode + 1
        state.lastCoolMode = now()  as long
            }
    else if(evt.value == "heat")
    {
        state.heatMode = state.heatMode + 1
        state.lastHeatMode = now()  as long
            }
    long lastCoolMode = state.lastCoolMode as long
        long lastHeatMode = state.lastHeatMode as long
        long timeLimit = 1000 * 60 * 10 as long
        if(now() - lastCoolMode > timeLimit)
    {
        log.debug "reseting lastCoolMode"
        state.lastCoolMode = now() as long
            }
    if(now() - lastHeatMode > timeLimit)
    {
        log.debug "reseting lastHeatMode"
        state.lastHeatMode = now() as long
            }

    eval()
}

def contactHandler(evt)
{
    log.debug "$evt.device returns $evt.value"

    if(evt.value == "open")
    {
        Thermostat.setThermostatMode("off")
    }
    eval()
}

def recordData(temp, outsideTemp, humidity, mode)
{
    def list = [outsideTemp,humidity,mode]
    log.debug """
        recording: desired: ${temp} when outside temp is: $outsideTemp when mode is $mode and with ${humidity}% outside humidity 
        as list: $list
        """

    state.learnBase = ["$temp": "$list"]
    def learnBase = state.learnBase
    def megaBase = state.megaBase
    megaBase << "$learnBase"
    state.megaBase = megaBase
    log.debug """
        state.learnBase = $state.learnBase
        state.megaBase = $state.megaBase
        megaBase = $megaBase
        """
}

def eval()
{
    state.learnBase = [:] // not in use yet so do not increment
    state.megaBase = [] // not in use yet so do not increment
    runIn(10, eval) // FOR DEBUG ONLY

    log.debug "START"

    //runIn(2, main)
    main()

}

def main()
{
    float outsideTemp = outdoor.currentValue("temperature")
    def feel = outsideTemp
    if(state.FeelString != null)
    {
        def feelString = state.FeelString
        //log.defbug """gettting value for $state.FeelString attribute: feelString = $feelString"""
        feel = outdoor.currentValue(feelString).toFloat()
    }
    if(state.windowsStatus == null)
    {
        state.windowsStatus = "alreadyopen"
    }
    def thermMode = Thermostat.currentValue("thermostatMode")
    boolean override = thermMode == "auto"  // auto 
    state.override = override

    def currMode = location.currentMode
    boolean inRegularModes = currMode in modes
    boolean Active = motionTest()
    boolean inSavingModes = currMode in savingModes || (inRegularModes && !Active) // if there's been no activity around motion sensors, then run in power saving mode
    def savingmodeMSG = "-------"
    state.criticalTempH = 70
    state.criticalTempC = 77

    float desired = state.learnedDesiredTemp.toInteger() // getDesiredTemp().toFloat()
    //log.debug "INFO 1: desired = $desired"
    //desired = desired + adjustWithHumidity(state.lastNeed) 
    //log.debug "INFO 2: desired = $desired && adjustWithHumidity(state.lastNeed)  = ${adjustWithHumidity(state.lastNeed)}"
    state.desired = desired

    def currHSP = Thermostat.currentValue("heatingSetpoint")
    def currCSP = Thermostat.currentValue("coolingSetpoint")
    float currTemp = Thermostat.currentValue("temperature")
    def humidity = getHumidity()
    boolean humidityOk = humidity < 65 && feel > 60
    log.debug "humidityOk = $humidityOk ($humidity < 65 && $feel > 60)"
    float currTempAtThermostat = Thermostat.currentValue("temperature")
    def CurrentContacts = contact?.currentValue("contact")
    def Open = CurrentContacts.findAll { val ->
        val == "open" ? true : false} // contactsOpen defined below with contactTimer


    boolean WAreOpen = false
    if(windows?.hasCapability("contact"))
    {
        def WindowsContacts = windows?.currentValue("contact")
        def WOpen = WindowsContacts.findAll { val ->
            val == "open" ? true : false}
        WAreOpen = WOpen.size() > 0
    }
    int duration = 0
    if(windowDelay)
    {
        duration = windowDelay
    }
    boolean okToOpen = false
    long openTime = 1000 * 60 * 10 // 10 minutes open time (if outside is cold, or if inside remains hot)
    def contactEvents = contact.collect{ it.eventsSince(new Date(now() - openTime)) }.flatten()
    def openEvents = contactEvents?.findAll{it.name == "open"}
    boolean contactTimerOk = !contactsOpen || openEvents.size() < 1 || outsideTemp >= 70
    boolean contactsOpen = Open.size() >= 1 && contactTimerOk 
    log.debug "Found ${contactEvents.size()} contact events in the last ${openTime/1000/60} minutes"
    log.debug "Found ${openEvents.size()} OPEN contact events in the last ${openTime/1000/60} minutes"

    boolean critical = currTemp <= 68 || currTemp >= 80
    boolean thermodeIsOk

    def need = "ERROR OR OVERRIDE"


    boolean okToChangeToCool = state.coolMode <= 4 || thermMode == "off"
    boolean okToChangeToHeat = state.heatMode <= 4 || thermMode == "off"

    boolean inWindowsMode = true // always true by default
    if(windowsModes){inWindowsMode = currMode in windowsModes}

    int Amplitude = desired - outsideTemp 
    boolean AmplitudeHeatOk = Amplitude >= 10
    boolean AmplitudeCoolOk = Amplitude <= 10

    def timeSinceLastHeatModeChange = (now() - state.lastHeatMode)/1000/60 
    def timeSinceLastCoolModeChange = (now() - state.lastCoolMode)/1000/60 

    if(inRegularModes || inSavingModes)
    {
        if(!override)
        {
            if(inSavingModes) // can be triggered by !Active during regular modes!
            {
                if(outsideTemp > 60)
                {
                    if(windows && currTemp >= 68 && currTemp <= desired + 5 && humidity < 75) // windows modes not accounted for here
                    {
                        state.closeTime = 1000* 60 * 20 // superior to time threshold,allows to open them despite having been closed recently
                        state.needWindowsOpen = true
                        need = "off"
                        if(!windows.hasCapability("contact"))
                        {
                            WAreOpen = true  // if no contact capability, assume it worked (contrary to what default value is set for above)
                        }

                        if(WAreOpen && state.windowsStatus == "alreadyopen") // prevent repeated requests
                        {
                            log.debug "$Thermostat kept to $need because state.windowsStatus returns '${state.windowsStatus}' 5547fde"

                        }
                        else if(!WAreOpen && state.windowsStatus == "alreadyopen")
                        {
                            state.closedByApp = true // allow new attempt
                            openWindows(0, "5467f") // windows are off while shoudl be on, turn them on again (it may take 5 minutes) 
                            need = "off" // inconsistency, so declare error
                            log.debug "INCONSISTENCY"
                        }

                    }
                    else // if cool needed but not ok to open windows
                    {
                        state.needWindowsOpen = false
                        closeWindows()
                        need = "cool"
                        if(!Active || !motionSensors)
                        {
                            state.desired = 78
                        }
                        else 
                        {
                            state.desired = 75
                        }
                        if(getHumidity() >= 80)
                        {
                            state.desired = state.desired - 2 // important for electronic equipment 
                        }
                        runIn(3, setCoolingSP)
                    }
                } // if no windows, while in saving mode, heat
                else 
                {
                    need = "heat"
                    if(!Active || !motionSensors)
                    {
                        state.desired = 66
                    }
                    else 
                    {
                        state.desired = 69
                    }
                    runIn(1, setHeatingSP)
                    closeWindows()
                }
                if(contactsOpen && !critical){
                    need = "off"
                }
                if(!thermodeIsOk)
                {
                    state.NoLearnMode = true
                    while(state.NoLearnMode == false) // if and only if nolearn is true, to prevent false learning input
                    {
                        // when some devices turn on, they get back to a specific SP, we don't want it to be recored as new value by A.I.
                        state.NoLearnMode = true
                        log.info "Waiting for database to update properly 854221dgh"
                    }
                    Thermostat.setThermostatMode(need) 
                    log.debug "$Thermostat set to $need -----------------------------------"

                    state.NoLearnMode = false
                }
                else 
                {
                    log.debug "$Thermostat already set to $need 9855ss"
                }

            }
            else if(inRegularModes)
            {  
                log.info "standard modes evaluation"
                if(altSensor)
                {
                    float B4 = currTemp
                    currTemp = altSensor.currentValue("temperature") + offset.toFloat()
                    log.debug "Alternative Sensor is: $altSensor and it returns ${currTemp}F (before round: ${B4})"

                    // previous attempts to adjust Set points with amplitude between altsensor and thermostat's returned temp value
                    // failed because they override user's settings and A.I. learning process... making the whole thing veeery annoying
                    // despite being efficient
                }


                okToChangeToCool = state.coolMode <= 4 || thermMode == "off"
                okToChangeToHeat = state.heatMode <= 4 || thermMode == "off"

                Amplitude = desired - outsideTemp 
                AmplitudeHeatOk = Amplitude >= 10
                AmplitudeCoolOk = Amplitude <= 10

                if(currTemp < desired && AmplitudeHeatOk && !contactsOpen) 
                // if the amplitude of the difference 
                // between desired temp and outside temperature is superior or equal to 10 between 
                //  then heating is needed
                {
                    if(okToChangeToHeat)
                    {
                        need = "heat"
                        state.lastNeed = need
                        log.debug "$Thermostat will be set to $need 4578fsd"
                    }
                    else 
                    {
                        log.debug "too many operating mode changes occured recently, not changing"
                        need = Thermostat.currentValue("thermostatMode") 
                    }
                }
                else if(currTemp > desired && AmplitudeCoolOk && !contactsOpen) // if the amplitude of the difference between desired temperature
                    //  and outside temperature is inferior or equal to 10, 
                // then this is when cooling starts to be needed over heating
                {
                    if(okToChangeToCool)
                    {
                        need = "cool"
                        state.lastNeed = need
                        log.debug "$Thermostat will be set to $need 8755h"
                    }
                    else 
                    {
                        log.debug "too many cooling set points changes occured recently, not changing"
                        need = Thermostat.currentValue("thermostatMode") 
                    }
                }
                else 
                { 
                    need = "off" // inside temperature is already ok or contacts open, turn off thermostat   
                    log.debug "$Thermostat kept $need 554dfza"
                }


                okToOpen = feel < 75 && feel >= 55 && humidityOk && !critical && currTemp <= desired + 3 && currTemp >= desired - 1 //&& !WAreOpen
                //log.info "okToOpen = $okToOpen"
                if(windows && inWindowsMode)
                {
                    log.debug "Evaluating the windows situation"

                    if(okToOpen) // basic windows conditions
                    {

                        need = "off"


                        log.debug "$Thermostat kept to $need because state.windowsStatus returns: '${state.windowsStatus}' 455712fhgjnz"
                        if(state.windowsStatus != "alreadyopen")
                        {
                            openWindows(duration, "78er")
                            state.needWindowsOpen = true // this is just for debug... 
                        }
                        else  // further opening
                        {
                            long lastOpen = state.openTime 
                            long timeLimit = 1000 * 60 * 10
                            if(now() - lastOpen > timeLimit && currTemp > desired)
                            {
                                log.debug "further opening windows"
                                state.alreadyOpen = false
                                openWindows(30, "54sdf") // will reset state.openTime
                            }
                            need = "off"
                        }
                    }
                    else 
                    {
                        log.debug "windows are to be closed 5578rff"
                        state.needWindowsOpen = false // this is just for debug... 
                        closeWindows()
                    }
                }
                else 
                {
                    log.debug "No windows management"
                    state.needWindowsOpen = false
                }

                if(contactsOpen && !critical && !override)
                {
                    need = "off"
                    log.debug "$Thermostat is off because some windows are open"
                }
                else if(contactsOpen && critical)
                {
                    if(currTemp <= 66 - 4 && !override)
                    {
                        need = "heat"
                    }
                    if(currTemp >= 80 && !override)
                    {
                        need = "cool"
                    }

                    log.debug "CRITICAL TEMP, closing windows! (if any)"
                    state.closedByApp = true
                    windows?.off()
                }
                if(need == "ERROR")
                {
                    log.debug "DISCREPANCY ERROR -- set to COOL until further events..."
                    need = "cool"
                }


                //log.debug "$Thermostat needs to be set to $need and critical returns $critical"

                thermodeIsOk = thermMode == need

                if(!thermodeIsOk)
                {
                    state.NoLearnMode = true
                    while(state.NoLearnMode == false) // if and only if nolearn is true, to prevent false learning input
                    {
                        state.NoLearnMode = true
                        log.info "Waiting for database to update properly 9974kioy"
                    }

                    Thermostat.setThermostatMode(need)
                    //log.debug "$Thermostat set to $need -----------------------------------"
                    state.NoLearnMode = false
                }
                else 
                {
                    log.debug "$Thermostat already set to $need 3587dzz"
                }

                if(need in ["heat", "cool"] && state.lastNeed != null)
                {
                    state.lastNeed = need
                }

                if(thermMode == "heat")
                {

                    if(currHSP != desired)
                    {
                        state.desired = desired
                        runIn(3, setHeatingSP)
                    }
                    else 
                    {
                        log.debug "$Thermostat HeatingSetpoint already set to $desired"
                    }
                }
                else if(thermMode == "cool")
                {
                    if(currCSP != desired)
                    {
                        state.desired = desired
                        runIn(3,setCoolingSP)

                    }
                    else 
                    {
                        // log.debug "$Thermostat CoolingSetpoint already set to $desired"
                    }
                }
            }
        }
        else 
        {
            log.debug """


            OVERRIDE MODE...doing nothing


            """
        }

    }
    else 
    {
        log.debug """
                LOCATION IS NOT IN ANY OF THE DESIGNATED OPERATING MODES... doing nothing
                current mode: ${location.currentMode} / active modes: $modes  &  $savingModes
                """
    }

    if(inSavingModes){
        savingmodeMSG = """
    ******************POWER SAVING MODE******************************************
    ******************************POWER SAVING MODE******************************
    *******************************************POWER SAVING MODE*****************
    """
    }
    log.trace """
        --------------- END ---------------

            ${savingmodeMSG}

        currMode = $currMode
            modes are: $modes
            inRegularModes = $inRegularModes
            savingModes are: $savingModes
            inSavingModes = ${inSavingModes}
            inMotionMode = ${currMode in motionModes}
            inWindowsMode = $inWindowsMode

            need = $need
            state.lastNeed = $state.lastNeed
            thermMode = $thermMode
            current operation = ${Thermostat.currentValue("thermostatOperatingState")}
            thermodeIsOk = $thermodeIsOk
            state.heatMode = $state.heatMode
            state.coolMode = $state.coolMode
            state.lastCoolMode = $state.lastCoolMode
            state.lastHeatMode = $state.lastHeatMode 
            elapsed timeSinceLastHeatModeChange = ${timeSinceLastHeatModeChange} minutes
            elapsed timeSinceLastCoolModeChange = ${timeSinceLastCoolModeChange} minutes

            okToChangeToCool = $okToChangeToCool
            okToChangeToHeat = $okToChangeToHeat

            state.desired = $state.desired
            desired = $desired    
            currTemp = $currTemp 
            currTempAtThermostat = $currTempAtThermostat
            outsideTemp = $outsideTemp
            humidity = $humidity
            humidityOk = $humidityOk
            feel = $feel
            okToOpen = $okToOpen
            override = $override
            critical = $critical
            state.criticalTempH = $state.criticalTempH
            state.criticalTempC = $state.criticalTempC    

            Amplitude = $Amplitude
            AmplitudeCoolOk = $AmplitudeCoolOk
            AmplitudeHeatOk = $AmplitudeHeatOk
        """
    log.trace """
            currHSP = $currHSP
            currCSP = $currCSP

            altSensor = ${altSensor}

            contacts = $contact
            contactsOpen = $contactsOpen
            WAreOpen = $WAreOpen
            contactTimerOk = $contactTimerOk
            state.needWindowsOpen = $state.needWindowsOpen

        noMotionTime = $noMotionTime
            motionSensors = $motionSensors
            inMotionMode = ${currMode in motionModes}
            Active = $Active

            state.openByApp = $state.openByApp
            state.closedByApp = $state.closedByApp
            state.alreadyClosed = $state.alreadyClosed 
            state.alreadyOpen = $state.alreadyOpen

        state.NoLearnMode = $state.NoLearnMode
            state.learnBase = $state.learnBase
            state.megaBase = $state.megaBase
            megaBase = $megaBase

            --------------- END ---------------
            """

}

def setHeatingSP()
{
    def desired = state.learnedDesiredTemp
    state.NoLearnMode = true
    if(Thermostat.currentValue("coolingSetpoint") != desired)
    {
        while(state.NoLearnMode == false) // if and only if nolearn is true, to prevent false learning input
        {
            state.NoLearnMode = true
            log.info "Waiting for database to update properly 756324dfg"
        }
        Thermostat.setHeatingSetpoint(desired)
        state.NoLearnMode = false
        log.debug """
      $Thermostat HeatingSetpoint set to $desired
      """


    }
    else 
    {
        log.debug "$Thermostat already set to $desired 4478rrtt"
    }

}
def setCoolingSP()
{
    state.NoLearnMode = true
    def desired = state.learnedDesiredTemp
    if(Thermostat.currentValue("coolingSetpoint") != desired)
    {
        while(state.NoLearnMode != true) // if and only if nolearn is true, to prevent false learning input
        {
            state.NoLearnMode = true
            log.info "Waiting for database to update properly 8874ffghy"
        }
        Thermostat.setCoolingSetpoint(desired)
        log.debug """
      $Thermostat CoolingSetpoint set to $desired
      """
        state.NoLearnMode = false


    }
    else 
    {
        log.debug "$Thermostat already set to $desired 4478rrtt"
    }

}

def getDesiredTemp()
{

    def CurrMode = location.currentMode
    float result = 72

    if(outdoor)
    {
        //log.debug "getting desired data from $outdoor sensor"
        result = state.learnedDesiredTemp.toInteger() // getComfort()
    }
    else 
    {
        int s = modes.size()
        int i = 0
        for(s!=0;i<s;i++)
        {
            log.debug "loop $i"
            if(CurrMode == modes[i])
            {
                log.debug "FOUND mode ${modes[i]}"
                def desiredstring = "desired${modes[i].toString()}"
                result = settings.find{it.key == desiredstring}.value
            }

        }

    }
    def beforeadjustments = result 

    if(altSensor && offset.toFloat() != 0) // if the alt sensor offset is not set to zero
    {
        // then make sure the desired matches the offset so unit doesn't stop running based on its own temperature measurement
        result = result + offset.toFloat()  // add also eventual desired offset
    }
    if(desiredoffset){
        result = result + desiredoffset.toFloat()
    }

    if(result <= 68)
    {
        result = 72
        log.debug "INCONSISTENT A.I..."
    }

    log.debug """
            currMode = $CurrMode // $modes
          before adjustments = ${beforeadjustments.toInteger()}
            offset = $offset
            desiredoffset = $desiredoffset
            getdesired() returns $result"""


    return result

}

def getComfort()
{

    def outsideTemp = outdoor.currentValue("temperature")
    def learnedDesiredTemp = state.learnedDesiredTemp.toInteger()
    def humidity = getHumidity()
    float newComfort

    if(outsideTemp < 60)
    ///////////////REVERSE SQUARE HEATING FUNCTION ///////////////// 
    {
        def xa = 34 // x1 reference value to be multiplied  
        def x = outsideTemp // outside temperature is the variable
        def y = null // value to be found
        def b = 10 // b is the multiplier 
        def c = 72 // c is a constant as the average comfort setting (y tends to c to infinity)

        // y = bx / x² + c
        newComfort = (b*xa) / (x*x) + humidity // reverse square function + humidity as variable (instead of a constant...) 
        float beforeRound = newComfort

        newComfort = newComfort.round().toInteger()
        log.info "linear newComfort HEAT is $newComfort"
    }
    else 
        ////////////////// LINEAR COOLING FUNCTION ///////////////// 
    {

        //outsideTemp = 90 // for test only 
        //humidity = 80
        def comfortHumidity = linearCoolHumidity(humidity)
        def comfortOutside = linearCoolOutsideTemp(outsideTemp)
        newComfort = (comfortHumidity + comfortOutside + learnedDesiredTemp)/3

        // learnedDesiredTemp 
        log.info """"
        linear newComfort COOL is $newComfort
        TEST LOG = ${LogCoolAll(comfortHumidity, comfortOutside)*learnedDesiredTemp}
        Math.log10 = ${Math.log10(LogCoolAll(comfortHumidity, comfortOutside)*learnedDesiredTemp)}
        """
    }

    newComfort = newComfort.round().toInteger()

    log.trace """
            ******************************************************************************************
            humidity = $humidity
            state.learnedDesiredTemp = $state.learnedDesiredTemp
            state.learnedOutsideTemp = $state.learnedOutsideTemp
            newComfort = $newComfort
            ******************************************************************************************
            """

    return newComfort
    //return 71
}

def linearCoolHumidity(humidity)
{
    def xa = 98 // humidity 
    def ya = 70 // desired temp for humidity a 
    def xb = 40  // humidity 
    def yb = 75 // desired temp for humidity b  
    def coef = (yb-ya)/(xb-xa)
    // solve intercept, b
    def b = ya - coef * xa // solution to ya = coef*xa + b // HSPSet = coef*outsideTemp + b //
    //b = ya - coef * outsideTemp  //
    def y = coef * humidity + b 
    log.debug "linearCoolHumidity returns $y"
    return y
}
def linearCoolOutsideTemp(outsideTemp)
{
    def xa = 110 // outsideTemp 
    def ya = 78 // desired temp for humidity a 
    def xb = 70  // outsideTemp 
    def yb = 70 // desired temp for humidity b  
    def coef = (yb-ya)/(xb-xa)
    // solve intercept, b
    def b = ya - coef * xa // solution to ya = coef*xa + b // HSPSet = coef*outsideTemp + b //
    //b = ya - coef * outsideTemp  //
    def y = coef * outsideTemp + b 
    log.debug "linearCoolOutsideTemp returns $y"
    return y
}
def LogCoolAll(comfortHumidity, comfortOutside)
{
    log.debug """
    comfortHumidity = $comfortHumidity
    comfortOutside = $comfortOutside
    """
    def y = Math.log(comfortHumidity) / Math.log(comfortOutside) 
    log.debug "LogCoolAll returns $y"
    return y
}
def adjustWithHumidity(need)
{
    log.debug "humidity adjustments..."
    float newComfortHumid
    def humidity = getHumidity()
    def outsideTemp = outdoor.currentValue("temperature")

    // now, implement humidity as a new variable

    if(outsideTemp >= 68) // cooling conditions
    {
        if(humidity > 60)
        {
            newComfortHumid = -1
        }
        if(humidity > 70)
        {
            newComfortHumid = -1
        }
        if(humidity >= 80)
        {
            newComfortHumid = -1
        }
    }
    else // heating
    {
        if(humidity > 70)
        {
            newComfortHumid = 1
        }
    }

    log.debug "Humidity adjustment returns $newComfortHumid as value to be added"
    //return newComfortHumid

    return newComfortHumid 

}

def getHumidity()
{
    boolean outDoorHasHumidity = outdoor.hasAttribute("humidity")
    boolean insideHasHumidity  = Thermostat.hasAttribute("humidity")
    def humidity
    if(insideHasHumidity)
    {
        humidity = Thermostat.currentValue("humidity")
    }
    else if(outDoorHasHumidity)
    {
        humidity = outdoor.currentValue("humidity")
    }
    else
    {
        humidity = 50
    }

    return humidity
}

boolean motionTest() 
{
    def events = []
    if(motionSensors && location.currentMode in motionModes)
    {
        long deltaMinutes = noMotionTime * 1000 * 60   
        events = motionSensors.collect{ it.eventsSince(new Date(now() - deltaMinutes)) }.flatten()
        log.debug "found ${events.size()} motion ${if(events.size() == 1){'event'}else{'events'}} in the last ${if(noMotionTime == 1){'minute'}else{"${noMotionTime} minutes"}}"
        return events.size() >= 1
    } 
    else 
    {
        return true
    }
}

def stopWindows()
{
    log.debug "stopping $windows"
    windows.stop()

}


def Poll()
{
    boolean thermPoll = Thermostat.hasCommand("poll")
    boolean thermRefresh = Thermostat.hasCommand("refresh") 
    boolean outsidePoll = outdoor.hasCommand("poll")
    boolean outsideRefresh = outdoor.hasCommand("refresh")
    boolean override = state.override
    if(location.currentMode in modes && !override)
    {
        if(thermRefresh){
            Thermostat.refresh()
            log.debug "refreshing $Thermostat"
        }
        else if(thermPoll){
            Thermostat.poll()
            log.debug "polling $Thermostat"
        }
        if(outsideRefresh){
            outdoor.refresh()
            log.debug "refreshing $outdoor"
        }
        else if(outsidePoll){
            outdoor.poll()
            log.debug "polling $outdoor"
        }

    }
    else if(override)
    {
        log.debug "not polling devices due to override mode"
    }

}

def openWindows(int duration, String cmdOrigin)
{
    def windowsStatus = "NA"

    boolean WAreOpen = false
    if(windows?.hasCapability("contact"))
    {
        log.info "$windows have contact capability"
        def WindowsContacts = windows?.currentValue("contact")
        def WOpen = WindowsContacts.findAll { val -> val == "open" ? true : false}
        WAreOpen = WOpen.size() > 0
    }
    else // use default contacts instead
    {
        def CurrentContacts = contact?.currentValue("contact")
        def Open = CurrentContacts.findAll { val -> val == "open" ? true : false} 
        WAreOpen = Open.size() > 0
    }

    long lastClosed = state.closeTime as long
        long timeLimit = 1000 * 60 * 15 as long
        boolean timeToOpenOk = now() - lastClosed > timeLimit && !WAreOpen
    state.needWindowsOpen = timeToOpenOk

    def cur = new Date(now()-lastClosed).format('mm:ss',location.timeZone)
    def msg="Time Elapsed since last closing Event: $cur"

    log.debug """ARE WINDOWS OPEN NOW ? ${if(WAreOpen){"YES"}else{"NO"}} (openWindows())
    WAreOpen = $WAreOpen
    timeToOpenOk = $timeToOpenOk
    $msg
    cmd Origin is: $cmdOrigin
    WAreOpen = $WAreOpen
    """

    if(timeToOpenOk)
    {
        if(state.alreadyOpen && !WAreOpen){
            windows.on()
            state.windowsStatus = "alreadyopen"
            state.openTime = now() as long
                state.alreadyClosed = false
            state.alreadyOpen = true
            state.closedByApp = false
            state.openByApp = true
            log.debug "opening $windows for a duration of $duration "
            windowsStatus = "open"
            if(duration != 0)
            {
                runIn(duration, stopWindows)
            }
        }
        else 
        {
            log.debug "Windows are already open"
        }

    }
    else 
    {
        log.debug "Windows have been closed too recently, not opening"
    }
}

def closeWindows()
{
    if(!state.closeTime)
    {
        state.closeTime = now() as long
            }
    boolean WAreOpen = false
    if(windows?.hasCapability("contact"))
    {
        def WindowsContacts = windows?.currentValue("contact")
        def WOpen = WindowsContacts.findAll { val ->
            val == "open" ? true : false}
        WAreOpen = WOpen.size() > 0
    }
    else // use default contacts instead
    {
        def CurrentContacts = contact?.currentValue("contact")
        def Open = CurrentContacts.findAll { val ->
            val == "open" ? true : false} 
        WAreOpen = Open.size() > 0
    }

    // unavoidable major safety: we don't want windows to close on a, for example, fire emergency situation... 
    // so we give a 15 min delay before trying to close them again if they're still open after a closing request
    long lastOpen = state.openTime as long
        long timeLimit = 1000 * 60 * 15 as long
        boolean timeToCloseOk = now() - lastOpen > timeLimit && WAreOpen
    state.needWindowsOpen = !timeToCloseOk

    def cur = new Date(now()-lastOpen).format('mm:ss',location.timeZone)
    def msg="Time Elapsed since last opening Event: $cur"

    log.debug """
            ARE WINDOWS OPEN NOW ? ${if(WAreOpen){"YES"}else{"NO"}} (closeWindows)
          	timeToCloseOk = $timeToCloseOk
            msg = $msg
            WAreOpen = $WAreOpen
            """

    if(timeToCloseOk){ // timeToCloseOk means enough time since LAST OPENING has passed
        //if(state.openByApp) // major safety
        //{
        if(!state.alreadyClosed && WAreOpen)
        {
            windows.off()
            state.windowsStatus = "closed"
            state.closeTime = now() as long
                state.alreadyClosed = true
            state.alreadyOpen = false
            state.closedByApp = true
            state.openByApp = false
            log.debug "closing $windows"
        }
        else 
        {
            log.debug "$windows already closed"
        }
        /* }
    else 
        {
            log.debug "windows were not open by this app"
        }*/
    }
    else 
    {
        log.debug "Windows have been opened too recently, not closing"
    }
}


//**************

def send(msg){
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage) {
            log.debug("sending push message")
            sendPushMessage(msg)
        }
        if (phone) {
            log.debug("sending text message")
            sendSms(phone, msg)
        }
    }

    log.info msg
}

/* //log.debug "outsideTemp is ${outsideTemp}F (getComfortH())"
        def xa
        def ya 
        def xb
        def yb 

        def modeNeeded = "heat"

        if(outsideTemp >= 60)
        {
            modeNeeded = "cool"
            xa = 10 //outside temp a
            ya = 75 //  desired temp at temp xa 

            xb = 50   //outside temp b
            yb = 70 // desired at temp xb  
        }
        else // it's cold... 
        {
            modeNeeded = "heat"
            xa = 30 //outside temp a
            ya = 74 //  desired temp at temp xa 

            xb = 50   //outside temp b
            yb = 70 // desired at temp xb  
        }
        float b = 0.00
        float slope = 0.00

        // get the slope
        slope = (yb-ya)/(xb-xa) 
        // solve intercept, b
        b = ya - slope * xa // solution to ya = coef*xa + b // 
        float newComfort = slope * outsideTemp + b 

        */