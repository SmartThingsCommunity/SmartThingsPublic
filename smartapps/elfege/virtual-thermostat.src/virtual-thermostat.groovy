/**
*  Copyright 2017 ELFEGE
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
*  Virtual Thermostat
*
*  Author: SmartThings
*/
definition(
    name: "Virtual Thermostat ++",
    namespace: "ELFEGE",
    author: "ELFEGE",
    description: "Control a space heater or window air conditioner in conjunction with any temperature sensor, like a SmartSense Multi.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
    page name: "pageSetup"
    page name: "settings"
    page name: "TemperaturesByMode"
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
            paragraph "Manage a virtual Thermostat"
        }
        section("Setup Menu") {
            href "settings", title: "Choose settings", description: ""
            href "TemperaturesByMode", title: "Set different temperatures depending on location mode", description: ""
        }

        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
            mode(title: "Set for specific mode(s)")

            paragraph 
            """BEWARE!!!! You'll be given the opprotunity to select different modes in the settings' page; you should select modes here IF AND ONLY IF 
you do not wish that emergency settings run outside of these operating modes. Beware that this means that if your heaters run in override mode and 
they don't have a built-in thermostat, this could cause sever overheating while sleeping or in your absence."""
        }
    }
}
def settings(){

    def pageProperties = [
        name:       "settings",
        title:      "Settings",
        nextPage:   "pageSetup"
    ]

    dynamicPage(pageProperties) {

        section("Choose a temperature sensor... "){
            input "sensor", "capability.temperatureMeasurement", title: "Sensor"
        }
        section("Select the heater or air conditioner outlet(s)... "){
            input "outlets", "capability.switch", title: "Outlets", multiple: true
        }
        section("Select 'heat' for a heater and 'cool' for an air conditioner..."){
            input "mode", "enum", title: "Heating or cooling?", options: ["heat","cool"]
        }
        section("Run only when there's been movement from (optional, leave blank to not require motion)..."){
            input "motion", "capability.motionSensor", title: "Motion", required: false, submitOnChange: true

            if(motion){
                input "minutes", "number", title: "Minutes", required: true, description: "write 0 if no required"
            }
        }
        section(""){
            input "Modes", "mode", title: "Run this app only in these modes", required: true, multiple: true, submitOnChange: true
        }
        section("Allow Manual Override"){
            input "override", "bool", title: "Override by user", default: false, required: false, submitOnChange: true
            if(override && mode == "heat"){
                input "timer", "number", title: "If I override and turned on the outlets, then turn them back off within this time", 
                    required: true, range: "1..60", description: "time in minutes" 
                input "safetyTooHot", "decimal", title: "But turned it back off immediately if temperature rises above this threshold:", required: true, range: "60..90", description: "mandatory precaution"
                paragraph "Be advised that this threshold also applies to normal operation, so in any case your room's temperature will never rise above this value"
            }
            else  if(override && mode == "cool"){
                input "timer", "number", title: "If I override and turned on the outlets, then turn them back off within this time", 
                    required: true, range: "1..60", description: "time in minutes" 
                input "safetyTooCold", "decimal", title: "But turned it back off immediately if temperature falls below this threshold:", required: true, range: "50..80", description: "mandatory precaution"
                paragraph "Be advised that this threshold also applies to normal operation, so in any case your room's temperature will never fall below this value"
            }
        }
        section("Contacts Sensor"){
            input(name: "contacts", type: "capability.contactSensor", title: "Optional: turn off all switches when these contact sensors are opened", required: false, multiple: true, submitOnChange: true)
        }
        section("EMERGENCY"){
            input(name: "emergency", type: "number", title: "In any case, do not allow the room's temperature to go below this threshold", required: false, default: 50, submitOnChange: true)
            paragraph """this fonction applies litterally in all cases and therefore triggers the override features, even if did not select them 
and even when location is not in the operating modes. You can, however, make the entire app to not run at all 
when outside of specified modes by using the built-in smartthings mode selector (home page of this app), but beware of the risks described there"""

            input(name: "EmergTimer", type: "number" , title:"for how long should I apply emergency override?", description: "set a time in minutes", required: false)
            paragraph "Recommended, especially if you selected modes in the first main setup page"

        }
    }
}
def TemperaturesByMode(){

    def pageProperties = [
        name:       "TemperaturesByMode",
        title:      "Set Temperatures by Mode",
        nextPage:   "pageSetup"
    ]

    dynamicPage(pageProperties) {
        def ModesSize = Modes.size()
        log.debug "Selected Modes are $Modes"

        def Mode0 = Modes[0]
        def Mode1 = Modes[1]
        def Mode2 = Modes[2]
        def Mode3 = Modes[3]
        def Mode4 = Modes[4]
        def Mode5 = Modes[5]	

        section("Set the desired temperatures..."){
            input "desireTempMode0", "decimal", title: "Desired Temp in $Mode0 mode", required: true
            if(ModesSize > 1){
                input "desireTempMode1", "decimal", title: "Desired Temp in $Mode1 mode"
            }
            if(ModesSize > 2){
                input "desireTempMode2", "decimal", title: "Desired Temp in $Mode2 mode"
            }
            if(ModesSize > 3){
                input "desireTempMode3", "decimal", title: "Desired Temp in $Mode3 mode"
            }
            if(ModesSize > 4){
                input "desireTempMode4", "decimal", title: "Desired Temp in $Mode4 mode"
            }
            if(ModesSize > 5){
                input "desireTempMode5", "decimal", title: "Desired Temp in $Mode5 mode"
            }
        }
        section("If This contact Sensor is closed, in any case keep room at this temperature:"){
            input "KeepWithContact", "bool", title: "true?", default: false, required: false, submitOnChange: true
            if(KeepWithContact){
                input "ExceptionContact", "capability.contactSensor", title: "Select a Sensor", required: true, submitOnChange: true, multiple: true
                input "ExceptionTemp", "decimal", title: "Desired Temp for when $ExceptionContact is closed"
            }
        }
    }
}

def installed(){
    log.debug "installed with settings: $settings"
    init()
}
def updated(){
    log.debug "updated with settings: $settings"
    unschedule()
    unsubscribe()

    init()

}
def init(){
    subscribe(sensor, "temperature", temperatureHandler)
    subscribe(outlets, "switch", switchHandler)
    subscribe(location, "mode", ChangedModeHandler)	

    if(contacts){
        subscribe(contacts, "contact.open", contactHandler)
        subscribe(contacts, "contact.closed", contactHandler)

        if(KeepWithContact){
            subscribe(ExceptionContact, "contact.open", ExceptionContactHandler)
            subscribe(ExceptionContact, "contact.closed", ExceptionContactHandler)
        }
        log.debug "subscribed to contact events"
    }
    if (motion) {
        subscribe(motion, "motion", motionHandler)
        log.debug "subscribed to motion events"
    }
    atomicState.HandlerIsScheduled = false
    atomicState.override = false
    atomicState.ByApp = true


    def scheduledTime = 1
    atomicState.HandlerIsScheduled = true
    schedule("0 0/$scheduledTime * * * ?", temperatureHandler)
    log.debug "temperatureHandler scheduled to run every $scheduledTime minutes"


    def SelectedModes = Modes.findAll { AllModes ->
        AllModes in Modes ? true : false
    }
    log.debug "${SelectedModes.size()} out of ${Modes.size()} Modes are selected"

    def ModesSize = SelectedModes.size()

    log.debug "Mode Size is $ModesSize"

    def Mode0 = Modes[0]
    def Mode1 = Modes[1]
    def Mode2 = Modes[2]
    def Mode3 = Modes[3]
    def Mode4 = Modes[4]
    def Mode5 = Modes[5]

    log.trace "Mode0: $Mode0, Mode1: $Mode1, Mode2: $Mode2, Mode3: $Mode3, Mode4: $Mode4, Mode5: $Mode5, "



    temperatureHandler()

}

def ExceptionContactHandler(evt){
    log.info "$evt.device is $evt.value (Exception Contact)"  

    atomicState.overrideWhileWindowsOpen = false
    atomicState.override = false

    desiredTemp()

}
def ExceptionContactClosed(){
    def Closed = false
    if(KeepWithContact){

        def CurrentContacts = ExceptionContact.latestValue("contact")
        def ClosedContacts = CurrentContacts.findAll { AllcontactsClosed ->
            AllcontactsClosed == "closed" ? true : false
        }
        log.debug "${ClosedContacts.size()} exception contact out of ${ExceptionContact.size()} are closed"

        Closed = ClosedContacts.size() == ExceptionContact.size()
        log.debug "Exception Contact Closed?($Closed)"
    }

    return Closed
}
def contactHandler(evt){

    log.info "$evt.device is $evt.value"  


    atomicState.overrideWhileWindowsOpen = false
    atomicState.override = false
    Switches()
}
def AllClosed(){
    def AllClosed = true
    def CurrentContacts = contacts.latestValue("contact")
    def ClosedContacts = CurrentContacts.findAll { AllcontactsClosed ->
        AllcontactsClosed == "closed" ? true : false
    }
    log.debug "${ClosedContacts.size()} windows/doors out of ${contacts.size()} are closed"

    AllClosed = ClosedContacts.size() == contacts.size()
    log.debug "All Closed?($AllClosed)"

    return AllClosed
}

def ChangedModeHandler(evt){
    log.debug "Current Mode is $evt.value" 
    def CurrMode = location.currentMode

    if(CurrMode in Modes){
        def scheduledTime = 1
        atomicState.HandlerIsScheduled = true
        schedule("0 0/$scheduledTime * * * ?", temperatureHandler)
        log.debug "temperatureHandler scheduled to run every $scheduledTime minutes"
        temperatureHandler()
    }
    else {
        unschedule(temperatureHandler)
    }

    log.debug "Selected Modes are $Modes"

    def Mode0 = Modes[0]
    def Mode1 = Modes[1]
    def Mode2 = Modes[2]
    def Mode3 = Modes[3]
    def Mode4 = Modes[4]
    def Mode5 = Modes[5]

    log.trace "Mode0: $Mode0, Mode1: $Mode1, Mode2: $Mode2, Mode3: $Mode3, Mode4: $Mode4, Mode5: $Mode5, "
    desiredTemp()
}
def switchHandler(evt){

    log.debug "event SOURCE is $evt.source"

    log.debug "$evt.device is $evt.value"
    log.debug "atomicState.override = $atomicState.override"
    log.debug "atomicState.ByApp = $atomicState.ByApp"

    def CurrMode = location.currentMode
    def ModeOk = CurrMode in Modes
    def desiredTemp = desiredTemp()

    def AllClosed = AllClosed 

    // is this an override action? (manual push of a button)
    if(override){

        // override while windows open
        if(evt.value == "on" && !AllClosed){
            // this is an on override. Keep on but turn off on schedule"
            atomicState.overrideWhileWindowsOpen = true
            log.debug "atomicState.overrideWhileWindowsOpen = $atomicState.overrideWhileWindowsOpen"
            log.info "OVERRIDE OUTSIDE WHILE WINDOWS OPEN... $outlets will turn off in $timer minutes"
            def timer = timer * 60
            runIn(timer, OffOutSideOfMode)

        }

        // override while modes ok
        if(override){
            if(ModeOk){
                if(evt.value == "on" && atomicState.override == false && atomicState.ByApp == false){

                    log.debug "OVERRIDE MODE $outlets will turn off in $timer minutes"
                    atomicState.override = true

                    log.debug "atomicState.override = $atomicState.override"
                    log.debug "atomicState.ByApp = $atomicState.ByApp"
                    def timer = timer * 60
                    runIn(timer, justshutoff)
                }
                else if(evt.value == "off" && atomicState.override == true){

                    atomicState.override = false
                    log.debug "END OF OVERRIDE"
                }
            }
            /// outside of modes

            else {
                if(evt.value == "off" && atomicState.override == true){

                    atomicState.override = false
                    log.debug "END OF OVERRIDE"
                }

                else if(evt.value == "on" && atomicState.override == false && atomicState.ByApp == false){
                    // this is an on override. Keep on but turn off on schedule"
                    atomicState.overrideWhileOutOfMode = true
                    log.debug "atomicState.overrideWhileOutOfMode = $atomicState.overrideWhileOutOfMode"
                    log.info "OVERRIDE OUTSIDE OF MODE... $outlets will turn off in $timer minutes"
                    def timer = timer * 60
                    runIn(timer, OffOutSideOfMode)

                }       

            }
        }
    } 
    else { 
        log.debug "NO OVERRIDE OPTION"
        atomicState.override = false
        atomicState.ByApp = true
    }
    OnSwitches() 
}
def OnSwitches() {
    def currSwitches = outlets.currentSwitch

    def onSwitches = currSwitches.findAll { switchVal ->
        switchVal == "on" ? true : false
    }
    log.debug "${onSwitches.size()} out of ${outlets.size()} switches are on"
    atomicState.totalOutlets = outlets.size()
    atomicState.switchVal = onSwitches.size()

    log.debug "atomicState.switchVal = $atomicState.switchVal"

    return onSwitches.size()
}
def temperatureHandler(evt){

    def CurrMode = location.currentMode

    def CurrTemp = sensor.currentValue("temperature") as double
        atomicState.currentTemp = CurrTemp
    //log.debug "$sensor event is $evt.value" // this handler run according to a schedule so evt.value would return an error
    log.debug "Current Temperature is ${atomicState.currentTemp}°F"

    Switches()

    if(mode == "heat" && CurrTemp > safetyTooHot){
        log.debug "Current Temperature is: $CurrTemp. EMERGENCY SHUT DOWN"
        OffOutSideOfMode()  
    }
    else if(mode == "cool" && CurrTemp < safetyTooCold){
        log.debug "Current Temperature is: $CurrTem. EMERGENCY SHUT DOWN"
        OffOutSideOfMode()  
    }
    if(emergency){
        if(mode == "heat"){
            if(atomicState.currentTemp < emergency){
                outlets?.on()
                atomicState.override = true
                if(EmergTimer){
                    def timer = EmergTimer * 60
                    runIn(timer, justshutoff)
                }
            }
        }
        else  if(mode == "cool"){
            if(atomicState.currentTemp < emergency){
                outlets?.off()
                atomicState.override = true
                if(EmergTimer){
                    def timer = EmergTimer * 60
                    runIn(timer, justTurnOn)
                }
            }

        }
    }
    //testloop()
}
def motionHandler(evt){

    log.debug "Motion is $evt.value"
    if(evt.value == "active"){
        atomicState.motion = true
        Switches()
    }
    else if (evt.value == "inactive") {

        def isActive = hasBeenRecentMotion()
        log.debug "Has been active within the last $minutes minute($isActive)"

        if (isActive) {

            atomicState.motion = true
            // switches will be triggered by temperature handler 

        }
        else {
            atomicState.motion = false
        }
    }
}

def Switches(){
    polls()
    def CurrMode = location.currentMode
    def AllClosed = AllClosed()
    def ExceptionContactClosed = ExceptionContactClosed()
    def desiredTemp = desiredTemp()
    def CurrTemp = sensor.currentValue("temperature") as double
        def TotalOnSwitches = OnSwitches()
        def AllOff = TotalOnSwitches == 0

        def totalOutlets = outlets.size()
        log.info "TotalOnSwitches = $TotalOnSwitches ; totalOutlets = $totalOutlets && Current Temperature = $CurrTemp && atomicState.ByApp = $atomicState.ByApp"

    if(!motion){
        atomicState.motion = true
    }

    if(AllClosed){
        if(CurrMode in Modes || ExceptionContactClosed){
            if(atomicState.motion == true){
                if (mode == "cool") {
                    // air conditioner
                    log.debug "evaluating cool. Desired temperature is $desiredTemp"    
                    if (CurrTemp > desiredTemp) {
                        if(atomicState.override == false){
                            if(TotalOnSwitches != totalOutlets){
                                // if just out of override the app needs to know that not all outlets were turned back on, so it turns back on the remaining ones
                                atomicState.ByApp = true
                                log.debug "atomicState.ByApp = $atomicState.ByApp"

                                outlets?.on()
                                log.debug "$outlets turned ON"

                            }
                            else {
                                log.debug "outlets already on, so doing nothing" 
                            }

                        }
                        else {log.debug "App in override mode, doing nothing"}
                    }
                    else {
                        if(atomicState.override == false){
                            atomicState.ByApp = true
                            log.debug "atomicState.ByApp = $atomicState.ByApp"
                            if(!AllOff){
                                outlets?.off()                   
                                log.debug "$outlets turned OFF"
                            }
                            else {
                                log.debug "$outlets ALREADY turned OFF"
                            }
                        }
                        else {
                            log.debug "App in override mode, doing nothing"
                        }
                    }
                }
                else {
                    // heater
                    log.debug "evaluating heat. Desired temperature is $desiredTemp"
                    if (CurrTemp < desiredTemp) {
                        if(atomicState.override == false){


                            log.debug "switchVal = $switchVal"
                            if(switchVal != totalOutlets){
                                // if out of override the app needs to know that not all outlets were turned back on, so it turns back on all the others
                                atomicState.ByApp = true
                                log.debug "atomicState.ByApp = $atomicState.ByApp"
                                outlets?.on()
                                log.debug "$outlets turned ON"
                            }
                            else {
                                log.debug "outlets already on, so doing nothing"
                            }

                        }
                        else {log.debug "App in override mode, doing nothing"}
                    }
                    else {
                        log.debug "temperature is above heat threshold"
                        if(atomicState.override == false){
                            if(switchVal != 0){
                                atomicState.ByApp = true
                                log.debug "atomicState.ByApp = $atomicState.ByApp"
                                if(!AllOff){
                                    outlets?.off()                   
                                    log.debug "$outlets turned OFF"
                                }
                                else {
                                    log.debug "$outlets ALREADY turned OFF"
                                }
                            }
                            else {
                                log.debug "$outlets ALREADY OFF"
                            }
                        }
                        else {
                            log.debug "App in override mode, doing nothing"
                        }
                    }
                }
            }
            else {
                if(motion){
                    log.debug "no motion within time frame, turning off $outlets"
                    if(atomicState.override == false){       
                        if(atomicState.switchVal != 0){
                            atomicState.ByApp = true
                            outlets?.off()
                        }
                        else {
                            log.debug "$outlets ALREADY OFF"
                        }
                    } 
                    else {
                        log.debug "App in override mode, doing nothing"
                    }
                }
                else {
                    log.debug "Motion detection not selected by user, doing nothing"
                }
            }   
        }
        else if(atomicState.overrideWhileOutOfMode == true){
            log.debug "Out of Modes OVERRIDE. Not turning off plugs"
        }
        else {
            log.debug "Not in $Modes mode, making sure all outlets are off"
            if(atomicState.override == false){
                if(atomicState.switchVal != 0){             
                    atomicState.ByApp = true
                    outlets?.off()
                }
                else { 
                    if(atomicState.HandlerIsScheduled != false){
                        log.debug "unschedule()"
                        unschedule()
                        atomicState.HandlerIsScheduled = false
                    }
                    log.debug "Waiting for events"
                }
            }
            else {
                log.debug "App in override mode, doing nothing"
            }

        }
    }
    else { 
        // contacts are open
        if(switchVal != 0){
            if(atomicState.overrideWhileWindowsOpen == false){
                atomicState.ByApp = true
                log.debug "atomicState.ByApp = $atomicState.ByApp"
                outlets?.off()
                log.debug "$outlets turned OFF"
            }
            else { log.debug "Override while windows open is active" }
        }
    }
    runIn(10,ResetValues)
}
def hasBeenRecentMotion(){
    def isActive = false

    if (motion && minutes) {
        def deltaMinutes = minutes * 60000 as Long

        def motionEvents = motion.eventsSince(new Date(now() - deltaMinutes))
        log.trace "Found ${motionEvents?.size() ?: 0} events in the last $minutes minutes"
        if (motionEvents.find { it.value == "active" }) {
            isActive = true

        }
    }
    else {
        isActive = true
    }
    return isActive
}
def OffOutSideOfMode(){
    atomicState.overrideWhileOutOfMode = false
    atomicState.ByApp = true
    log.debug "atomicState.overrideWhileWindowsOpen = $atomicState.overrideWhileWindowsOpen"
    log.debug "atomicState.overrideWhileOutOfMode = $atomicState.overrideWhileOutOfMode"
    outlets?.off()

}
def desiredTemp(){
    def ExceptClosed = ExceptionContactClosed()
    log.debug "ExceptClosed?($ExceptClosed)"

    def AllClosed = AllClosed()

    def CurrMode = location.currentMode

    def desiredTemp = 70 // default value in case the user missed this setting 

    def CurrTemp = sensor.currentTemperature

    log.info "Current temperature is $CurrTemp"


    if(ExceptClosed){
        desiredTemp = ExceptionTemp
        log.debug "Keeping Temperature at $ExceptionTemp despite mode change because $ExceptionContact is CLOSED"
        atomicState.override = false
    }
    else {

        if(CurrMode in Modes[0]){
            desiredTemp = desireTempMode0
            log.debug "Desired temp is now : $desiredTemp ----a-----"
        } 
        else if(CurrMode in Modes[1]){
            desiredTemp = desireTempMode1
            log.debug "Desired temp is now : $desiredTemp ----b-----"
        } 
        else if(CurrMode in  Modes[2]){
            desiredTemp = desireTempMode2
            log.debug "Desired temp is now : $desiredTemp -----c----"
        } 
        else if(CurrMode in  Modes[3]){
            desiredTemp = desireTempMode3
            log.debug "Desired temp is now : $desiredTemp ----d-----"
        } 
        else if(CurrMode in  Modes[4]){
            desiredTemp = desireTempMode4
            log.debug "Desired temp is now : $desiredTemp ---e------"
        } 
        else if(CurrMode in  Modes[5]){
            desiredTemp = desireTempMode5
            log.debug "Desired temp is now : $desiredTemp ---f------"
        }
        else { 
            log.debug "Home is not in any of the modes selected by user"
        }
    }
    return desiredTemp
}
def justshutoff(){

    atomicState.overrideWhileWindowsOpen = false
    atomicState.override = false
    atomicState.ByApp = true
    outlets?.off()

}
def justTurnOn(){
    atomicState.overrideWhileWindowsOpen = false
    atomicState.override = false
    atomicState.ByApp = true
    outlets?.on()


}
def testloop(){

    def CurrMode = location.currentMode

    def Mode0 = Modes[0]
    def Mode1 = Modes[1]
    def Mode2 = Modes[2]
    def Mode3 = Modes[3]
    def Mode4 = Modes[4]
    def Mode5 = Modes[5]
    /// goal is to generate a map of all temps defined by users according to modes. 


    // this works
    def ModesMap = CurrMode ?: ["$Modes":"$desiredTemp"]

    log.debug "TEMPERATURE MODE (TEST) = $ModesMap"
    // now we have to find a way to generate a map of temps defined by the user in settings
    def AlldesiredTemps = settings.findAll(){it.key ==~ /desireTempMode[0-9]/}.sort{it.key}
    def ModesMapFULL = ["$Modes":"$AlldesiredTemps"]
    log.info "-----------------------------------------------------------------------------------------AlldesiredTemps = $AlldesiredTemps"
    log.info "-----------------------------------------------------------------------------------------ModesMapFULL = $ModesMapFULL"

    // now assign a key number to each mode corresponding to desiredTemps 





    // so we then can do something like this :  def ModesTemps = temps ?: ["$Modes":"$desiredTemp"]

    //def test =  settings.findAll(){it.key ==~ /d[a-b]+[A-Z]+\b/}.sort{it.key}
    //log.info "(TEST) = $test"
    //settings.findAll(){it.key ==~ /g[0-9]+/}.sort{it.key}
    //assert "desireTempMode1" ==~ "$test"
    //desireTempMode" =~ ".*(d.*e).*").matches() 
    //return settings.findAll(){it.key ==~ /g[0-9]+/}.sort{it.key}
    // def test = settings.findAll(){it.key ==~ /g[0-9]+/}.sort{it.key}

}

def ResetValues(){
    log.debug "RESET"
    atomicState.ByApp = false

}

def polls(){

    def poll = sensor.hasCommand("poll")
    def refresh = sensor.hasCommand("refresh")
    if(poll){
        sensor.poll()
        log.debug "polling $sensor"
    }
    else if(refresh){
        sensor.refresh()
        log.debug "refreshing $sensor"
    }
    else { 
        log.debug "$Thermostat_2 does not support either poll() nor refresh() commands"
    }
}
