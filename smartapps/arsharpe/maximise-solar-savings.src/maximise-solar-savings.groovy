/**
 *  Energy Saver
 *
 *  Copyright 2014 SmartThings
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
 */

/*

min day time solar power = 230w for 3000 w-pk system
    
    Previous state      New state after initial override   Return state after override timeout      Notes
    ------------------------------------------------------------------------------------------------------------------------------------------------------------
    Off (Auto)         On (Manual-overide)             On (Auto)                        In order to minimise relay switching, returns on On (Auto) and then system can update state if necessary
    On (Auto)         On (Manual-overide)               On (Auto)                        In order to minimise relay switching, returns on On (Auto) and then system can update state if necessary
    Off (Man)         On (Man)                     On (Man)                        Keeps on
    On (Man)         On (Man)                     <Previous state>                  Not applicable - device is already on and no override required

    Design
      New state
         If auto switch to On (manual-override)
          If already off under manual control just turn on switch

      Return state
         Just clear override mode.
               Auto will re-calculate switch state as necessary
                Man remains on
               Displayed state will be updated on next sample
            

--- FEATURES ---

Supported power meters specification
   high frequency (e.g. >1 kHz) mains power sampling (to support triac/thyristor controlled devices)
   power notification period < 3 secs (to maximise cost savings)

Supported load types
   Soft/delayed start
    High power start (through clamp meters)
   Battery storage system (needs power monitoring in the control switch)
      When supplying power to mains, system removes inverter output power from household power before calculating net power
       When battery charging is active, it is considered a normal (uncontrolled ??) load

Supported switches
   With power monitoring (as per "Supported power meters specification")
    Without power monitoring (Note - gives degraded performance for loads with power that varies)

Control algorithms
   Predefined default power (No need to run load to measure power and support switches without load)
    Continually measures load power if available - @todo Remove this feature?
   Ensures devices run for a minimum amount of time
   Auto shutdown when load draws less than a defined minimum power
    Alarms for manual control
        Upper and lower net-power threshold alarms - TESTED
      SMS and push notifications

Dashboard
    To Google Sheet
    Notification of change in any device status

Logging
    To OpenCMS
   CSV logging to simulator "logs" window
      Log to Google Sheet

Work with other SmartApps that control device status
  
*/

/*============== Configuration =======================*/

definition(
    name: "Maximise Solar Self Consumption",
    namespace: "arsharpe",
    author: "Robert Sharpe",
    description: "Automatically control loads to use free solar electricity by maximising self consumption.  Uses Load Shifting processes.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png",
    pausable: true
){
   appSetting "setting1"
}

preferences {
   section ("Measure power using ..."){
      input(name: "solarMeter",        type: "capability.powerMeter", title: "This meter for solar", required: true, multiple: false, description: null)
      input(name: "householdMeter", type: "capability.powerMeter", title: "This meter for household", required: true, multiple: false, description: null)
   }
    
    section ("Battery Storage System"){
    /*
        input(
           name: "battStorage_enabled", 
            title: "Automatically control", 
            description: null, 
            type: "bool", defaultValue: true
        ) */
        input(
           name: "battStorage_mode",
            type: "enum", 
            title: "Mode", 
            options: [
               "off": "Off",
                "on": "On",
                "chargingOnly":"Charging only",
                "outputOnly": "Output only"
            ]
        )
        
      input(
           name: "battStorage_inverter_switch",
            title: "Inverter controlled by this switch",
            description: "Select switch controlling the inverter",
            type: "capability.switch", required: false, multiple: false
        )
      input(
           name: "battStorage_charger30A_switch",
            title: "30A battery charger controlled by this switch",
            description: "Select switch controlling the 30A charger",
            type: "capability.switch", required: false, multiple: false
        )
      input(
           name: "battStorage_charger6A_switch",
            title: "6A battery charger controlled by this switch",
            description: "Select switch controlling the 6A charger",
            type: "capability.switch", required: false, multiple: false
        )
        input(
           name: "battStorage_installed_capacity_wh",
            title: "Installed battery capacity (in watt-hours)",
            description: "Enter the capacity in watt-hours",
            type: "number", required: false, defaultValue: 1000
        )
        input(
           name: "battStorage_protectBattery", 
            title: "Limit charging and discharging", 
            description: null, 
            type: "bool", defaultValue: true
        )
        input(
           name: "battStorage_startingBatteryLevel", 
            title: "Start with this battery level (blank for no change or 0-100)", 
            description: null, 
            type: "number", required: false, defaultValue: 100
        )
   }


   section ("EVSE"){
        input(
           name: "evse_enabled", 
            title: "Automatically control this load", 
            description: "Select switch controlling this load",
            type: "bool", defaultValue: false
        )
      input(
           name: "evse_switch",
            title: "Powered by this switch",
            description: "Is controlled by this switch",
            type: "capability.switch", required: false, multiple: false
        )
      input(
           name: "evse_solar_switch",
            title: "Enabled solar control switch",
            description: "Is controlled by this switch",
            type: "capability.switch", required: false, multiple: false
        )
      input(
           name: "evse_max_power_switch",
            title: "Max power switch",
            description: "Is controlled by this switch",
            type: "capability.switch", required: false, multiple: false
        )
        input(
           name: "evse_startupDuration_secs", 
            title: "Takes this time to startup and the power to settle (in seconds)",
            description: "Enter time in seconds.",
            type: "number", required: false
        )
        input(
           name: "evse_maxPower",
            title: "Draws this much power just after startup (in watts)",
            description: "Enter the power in watts",
            type: "number", required: false
        )
        input(
           name: "evse_minOnTime_secs", 
            title: "The minimum duration the device needs to be on (in seconds)",
            description: "Enter the duration in seconds",
            type: "number", required: false
        )
   }
/*
   section ("System"){
        input(
           name: "reservedPower", 
            title: "Minimum excess power to turn on loads  (in watts).",
            description: "Enter power in watts",
            type: "number", required: false, defaultValue: 1000
        )
   } */
   section ("To control load 1 (highest priority)"){
        input(
           name: "switch1_enabled", 
            title: "Automatically control this load", 
            description: null, 
            type: "bool", defaultValue: true
        )
      input(
           name: "switch1",
            title: "Powered by this switch",
            description: "Select switch controlling this load",
            type: "capability.switch", required: false, multiple: false
        )
        input(
           name: "switch1_startupDuration_secs", 
            title: "Takes this time to startup and the power to settle (in seconds)",
            description: "Enter time in seconds.",
            type: "number", required: false, defaultValue: 5
        )
        input(
           name: "switch1_maxPower",
            title: "Draws this much power just after startup (in watts)",
            description: "Enter the power in watts",
            type: "number", required: false, defaultValue: 150
        )
        input(
           name: "switch1_minOnTime_secs", 
            title: "The minimum duration the device needs to be on (in seconds)",
            description: "Enter the duration in seconds",
            type: "number", required: false, defaultValue: 0
        )
   }
   section ("To control load 2"){
        input(
           name: "switch2_enabled", 
            title: "Automatically control this load", 
            description: "Select switch controlling this load",
            type: "bool", defaultValue: true
        )
      input(
           name: "switch2",
            title: "Powered by this switch",
            description: "Is controlled by this switch",
            type: "capability.switch", required: false, multiple: false
        )
        input(
           name: "switch2_startupDuration_secs", 
            title: "Takes this time to startup and the power to settle (in seconds)",
            description: "Enter time in seconds.",
            type: "number", required: false, defaultValue: 20
        )
        input(
           name: "switch2_maxPower",
            title: "Draws this much power just after startup (in watts)",
            description: "Enter the power in watts",
            type: "number", required: false, defaultValue: 600
        )
        input(
           name: "switch2_minOnTime_secs", 
            title: "The minimum time the device should be on (in seconds)",
            description: "Time for the device to be on.",
            type: "number", required: false, defaultValue: 0
        )
   }
   section ("To control load 3"){
        input(
           name: "switch3_enabled", 
            title: "Automatically control this load", 
            description: "Select switch controlling this load",
            type: "bool", defaultValue: true
        )
      input(
           name: "switch3",
            title: "Powered by this switch",
            description: "Is controlled by this switch",
            type: "capability.switch", required: false, multiple: false
        )
        input(
           name: "switch3_startupDuration_secs", 
            title: "Takes this time to startup and the power to settle (in seconds)",
            description: "Enter time in seconds.",
            type: "number", required: false, defaultValue: 0
        )
        input(
           name: "switch3_maxPower",
            title: "Draws this much power just after startup (in watts)",
            description: "Enter the power in watts",
            type: "number", required: false, defaultValue: 800
        )
        input(
           name: "switch3_minOnTime_secs", 
            title: "The minimum duration the device needs to be on (in seconds)",
            description: "Enter the duration in seconds",
            type: "number", required: false, defaultValue: 300
        )
        
   }
   section ("To control load 4"){
        input(
           name: "switch4_enabled", 
            title: "Automatically control this load", 
            description: "Select switch controlling this load",
            type: "bool", defaultValue: false
        )
      input(
           name: "switch4",
            title: "Powered by this switch",
            description: "Is controlled by this switch",
            type: "capability.switch", required: false, multiple: false
        )
        input(
           name: "switch4_startupDuration_secs", 
            title: "Takes this time to startup and the power to settle (in seconds)",
            description: "Enter time in seconds.",
            type: "number", required: false
        )
        input(
           name: "switch4_maxPower",
            title: "Draws this much power just after startup (in watts)",
            description: "Enter the power in watts",
            type: "number", required: false
        )
        input(
           name: "switch4_minOnTime_secs", 
            title: "The minimum duration the device needs to be on (in seconds)",
            description: "Enter the duration in seconds",
            type: "number", required: false
        )
   }
    
   section("Send net power alarms"){
        input(name: "netPowerAlarmsEnabled", type: "bool", title: "When enabled", description: null, defaultValue: false)
        input(name: "upperThreshold", type: "number", title: "Net power exceeds this power (in watts)", required: false, description: "in watts", defaultValue: 400)
        input(name: "lowerThreshold", type: "number", title: "Net power falls below this power (in watts)", required: false, description: "in watts", defaultValue: -400)
        input("recipients", "contact", title: "Send notifications to") {
            input(name: "pushNotification", type: "bool", title: "Using a push notification", description: null, defaultValue: true)
        }
    }

}

// ######################## SYSTEM COMMANDS ##########################
def installed() {
   log.info "Installed with settings: ${settings}"
    
    log.info "Data: Time,Household Power,Solar Power,Last Net Power"
//    solarMeter_power_handler({value: 123})
   initialize()
}

def initialize() {
   def loads = getLoads()
     
   log.debug "battStorage_mode = " + battStorage_mode
    
   // Required devices
   subscribe(householdMeter,  "power", handle_powerChange)
//    subscribe(solarMeter,      "power", handle_powerChange)
    
   // Optional devices
   // @todo Handle if this device isn't configured
   for(def d=0; d<loads.size; d++){
      subscribe(loads[d].switch, "power", handle_powerChange)
   }

   setState_normal()
   atomicState.alarmsMuted = false
   atomicState.inverterSwitchingDeadbandPower = 100 // watts
    
   loads_init()
   battStorage_init()
   evse_init()
    
//   log.info "initialise: atomicState.dev.lastPower = " + atomicState.dev.lastPower
}

def updated() {
   log.debug "Updated with settings: ${settings}"
   unsubscribe()
   initialize()
}

/*======================== LOCAL COMMANDS ========================*/
def handle_powerChange(evt){
   def loads = getLoads()
    
   trace_entering("handle_powerChange()")

   load_handleMinOnTimes()
    
    battStorage_cron()
    
   if( !handle_selfConsumption(loads) ){
       handle_alarms()
    }

   log_status()
    
   handle_dataLogging()
    
   trace_exiting("handle_powerChange()")
}

def log_status(){
   def NL = "\n"
    def info = ""
    def data = summaryData()
  
    info = info + NL    // Start "SYSTEM:" at left column
   info = info + "SYSTEM:" + NL
//    info = info + "... State: " + atomicState.state + NL
   info = info + "... Power: "
      if(data.excessPower > 0)   info = info + "exporting " + data.excessPower + " w"
       if(data.drawingMainsPower) info = info + "importing " + data.netPower + " w"
       info = info + " (solar = " + data.solarPower + " w, house = " + data.householdPower + " w)" + NL

/*
    info = info + "... Energy :" + NL
    info = info + "....... Today .... : consumed = ? wh, exported = ? wh" + NL
    info = info + "....... This month : consumed = ? wh, exported = ? wh" + NL
    info = info + "....... This year. : consumed = ? wh, exported = ? wh" + NL
    info = info + "....... Forever .. : consumed = ? wh, exported = ? wh" + NL */
    info = info + NL
    info = info + "DEVICES:" + NL
   info = info + NL
    info = info     + "Generators:" + NL
   info = info + log_entry("Solar", "Fixed", data.solarPower + " w", "") + NL
   if(battStorage_inverter_switch.currentSwitch == "on") {
      def state_data = battStorage_state_data()
      info = info + log_entry(
           "Battery storage system", 
            state_data.state, 
            state_data.power + " w ?",
            state_data.additionalInfo
      ) + NL
   }
   info = info + NL

   //log_status_entries_byPriority()
   info = info + "ON Devices:" + NL

   // Baseload
   info = info + log_entry("Baseload", "Manual", getBaseload() + " w ?", "") + NL
   def reservedPowerValue = (reservedPower != null) ? reservedPower : 0 
//    info = info + log_entry("PWM devices", "Manual", reservedPower + " w", "") + NL    // Always on
        
   // Battery Storage
   if(battStorage_charging() ) {
      def state_data = battStorage_state_data()
      info = info + log_entry("Battery storage system", state_data.state, state_data.power + " w ?", state_data.additionalInfo) + NL
   }
        
   info = info + status_entries_byState_text("on")
   info = info + NL
    
   info = info + "OFF Devices:" + NL
        
   // Battery storage
   if(!battStorage_charging() && battStorage_inverter_switch.currentSwitch == "off") {
      def state_data = battStorage_state_data()
      info = info + log_entry("Battery storage system", state_data.state, "", state_data.additionalInfo) + NL
   }

   info = info + status_entries_byState_text("off") + NL
   info = info + NL
   info = info + "==================================================================" + NL
   info = info + NL

   log.info(info)
    
}

def getBaseload(){
	return 395
}

def log_entry(name, state, power, additionalInfo){
   return "... " + addPadding(name, tabPos()) + power + " | " + state + " | " + additionalInfo
}

def handle_dataLogging() {
    def householdPower = householdMeter.currentState("power").value as Float
    def solarPower = solarMeter.currentState("power").value as Float
    def netPower = householdPower - solarPower

   Date date = new Date()

//    log.debug "Data: " + householdPower + "," + solarPower + "," + netPower + "," + source
// @todo - Add automatic detection of source 
//    log.info "netPower = " + netPower + "w (from " + source + " update)"
}


def handle_selfConsumption(loads){
   trace_entering("handle_selfConsumption()")
    
    def data = summaryData()
    int householdPower = data.householdPower as int
    int solarPower = data.solarPower as int
    int netPower = data.netPower as int
    int excessPower = data.excessPower
    boolean drawingMainsPower = data.drawingMainsPower
    int usablePower = data.usablePower
    

    // Ignore any power changes while one or more devices are starting
    if(atomicState.state == "switching") {
       log_algorithm("currently switching - ignoring")
        trace_exiting("handle_selfConsumption()")
       return true
    }
    
     def completed = false
    if(netPower > atomicState.inverterSwitchingDeadbandPower/2) {
      completed = loads_turnOffLowestPriorityLoad()
        if(!completed){
//              log.debug "battStorage_configured() = " + battStorage_configured() + ", battStorage_inverterEnabled() = " + battStorage_inverterEnabled()
           if(battStorage_configured() && battStorage_inverterEnabled()){
              log.debug("battStorage_mode = " + battStorage_mode + ", battStorage_inverter.currentSwitch = " + battStorage_inverter_switch.currentSwitch + ", netPower = " + netPower + ", atomicState.inverterSwitchingDeadbandPower/2 = " + atomicState.inverterSwitchingDeadbandPower/2)
                log.debug("battStorage_inverter_okToTurnOn = " + battStorage_inverter_okToTurnOn())
            if(netPower > atomicState.inverterSwitchingDeadbandPower/2 && battStorage_inverter_okToTurnOn()) {
                  log_algorithm("Turning inverter on")
                    sendMessage("Turning inverter on")
                    battStorage_inverter_on()
                   completed = true
              }
            }
        }
    }
    if(netPower < -50) {   // there is excess power
       /*
         if(battStorage_configured() && battStorage_inverter_enabled()){
         if(battStorage_inverter_switch.currentSwitch == "on" && excessPower > atomicState.inverterSwitchingDeadbandPower/2){  // turn inverter off
                 sendMessage("Turning inverter off")
             battStorage_inverter_off()
                completed = true
         }
        }
        if(!completed && usablePower > 0) loads_turnOnHighPriorityLoad(usablePower)
        */
        loads_turnOnHighPriorityLoad(usablePower)
   }
    
   trace_exiting("handle_selfConsumption()")
    return false
}  // end def

private battStorage_inverter_okToTurnOn(){
    def ok = true
    ok = ok && (battStorage_mode == "on" || battStorage_mode == "outputOnly")
    ok = ok && battStorage_inverter_switch.currentSwitch == "off" 
    ok = ok && ( (battStorage_battery_level() > 0.8) || !battStorage_protectBattery)
    return ok
}

def summaryData(){
    int householdPower = householdMeter.currentState("power").value as Float
    def solarPower = solarMeter.currentState("power").value as Float
    int netPower = householdPower - solarPower
    int excessPower = (netPower < 0) ? -netPower : 0 as Integer
    boolean drawingMainsPower = netPower > 0

   int reservedPowerValue = 0
   if(reservedPower != null) reservedPowerValue = reservedPower
    int usablePower = excessPower - reservedPowerValue
    if(usablePower < 0) usablePower = 0


    return [
      householdPower: householdPower,
      solarPower: solarPower,
        netPower: netPower,
        excessPower: excessPower,
        drawingMainsPower: drawingMainsPower,
        usablePower: usablePower
    ]
}

def addPadding(labelInfo, tabPos){
    String padding = "___________________________________________________________________"
   def len_ = labelInfo.length()> tabPos ? 1 : tabPos - Math.round(labelInfo.length()*0.74)
   return labelInfo + padding.substring(1, len_ as Integer) + " "
}


def status_entries_byState_text(state){
    def loads = getLoads()
    def info = ""
    def additionalInfo = ""

   // Display high priority items first (by doing in reverse order because log display is newest at top)
   for(def d=loads.size-1; d>=0; d--){
      def dev = loads[d].switch
      def devMore = loads[d].devMore
      def power = ""

      if( (dev == null) || (dev.currentSwitch != state) ) continue
    
      // Add power info
      if(dev.currentSwitch == "on"){
         power = devMore.maxPower //currentPower_withUnits(logs[d])

         // Add remaining on time
         if(minOnTime_secs != null){
            if(load_getMinOnTimeStartedAt_epoch(d) != 0)  {
               additionalInfo = additionalInfo + ", minimum remaining on time = " + (minOnTime_secs - onSeconds(d)) + " s"
            }
         }

      } else {  // not currently on
        
         // Add "waiting for usable power of " ... if necessary
         if(devMore.maxPower != null && devMore.enabled){
            additionalInfo = additionalInfo + "waiting for usable power of " + devMore.maxPower + " w"
         }
      }

      info = info + log_entry(dev.label, devMore.enabled ? " Auto" : " Manual", power, additionalInfo) + "\n"

   } // end for each load

   return info
}


def currentPower_withUnits(dev, solarPower = null){
    def devMore = dev.devMore

    
   // Use actual power if available
     if(dev.currentState("power") != null){
         return dev.switch.currentState("power") + " w"
      }    

return devMore.maxPower   // @todo Handle device power

    // EVSE
    if(evse_isKnownDevice(dev)){
       if(solarPower == null){
           debug.error("No solar power supplied to currentPower_withUnits()")
      }
         evse_currentPower(data.solarPower)
       info = info + NL
   }

    additionalInfo = additionalInfo + devMore.maxPower + "w ?" 

    return currentPower(dev)
}

def tabPos() { return 40 }

private onSeconds(d){
   if(load_getMinOnTimeStartedAt_epoch(d) != 0){
       def retValue = Math.round((now() - load_getMinOnTimeStartedAt_epoch(d))/1000)
      return retValue
    }else{
       return 0
    }
}


// ########################### Alarm Module ######################################

/**
Check if netPower value has transition above upperThreshold or below lowerThreshold and issue alarms if configured.
*/
def handle_alarms(){
   trace_entering("handle_alarms()")
    
    def householdPower = householdMeter.currentState("power").value as Float
    def solarPower = solarMeter.currentState("power").value as Float
    def netPower = householdPower - solarPower

   def msg
    
   // Only create alarms if they are enabled and not muted
   if(netPowerAlarmsEnabled == false || atomicState.alarmsMuted) return
    
   // Check if above upper threshold
    int upperThresholdValue = (upperThreshold == null) ? 9999 : upperThreshold
    if (netPower > upperThresholdValue) {
       // e.g. Net power (152 w) > upper threshold (120 w)"
      msg = "Net power (${netPower} w) > upper threshold (${upperThreshold} w)"
    }
    
//   log.debug "netPower = ${netPower}, lowerThreshold = ${lowerThreshold}, upperThreshold = ${upperThreshold}"

   // Handle if below lower threshold
    int lowerThresholdValue = (lowerThreshold == null) ? -9999 : lowerThreshold
   if (netPower < lowerThresholdValue) { 
      // e.g. Net power (152 w) > upper threshold (120 w)"
      msg = "Net power (${netPower} w) < lower threshold (${lowerThreshold} w)."
   }

    if(msg != null){
       sendMessage(msg + " (muting for " + muteDuration() + " secs)") // Tell user so they can do it manually for the moment
       muteAlarms()
    }

   trace_exiting("handle_alarms()")
}

def muteAlarms(){
   atomicState.alarmsMuted = true
    runIn(muteDuration(), unmuteAlarms)
}

def unmuteAlarms(){
   trace_entering("unmuteAlarms()")
   atomicState.alarmsMuted = false
    trace_exiting("unmuteAlarms()")
}

def muteDuration(){
   return 30
}

// ###################################### General #################################

def setState_normal(){
    setState_("normal")
}

def setState_switching(){
    setState_("switching")
}

def setState_(newState){
    log_algorithm "State changed from: '" + atomicState.state + "' to '" + newState + "'"
   atomicState.state = newState
}

def sendMessage(msg) {
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    } else {
    /*
      if (sms) {
           log.trace "Sending sms ..."
            sendSms(sms, msg)
        } */

        if (pushNotification == true) {
            sendPush(msg)
        }
    }
}

// ################################# Array of loads ############################
private loads_init(){
   trace_entering("loads_init")
    
   def loads = getLoads()
    
   for(def d=loads.size-1; d>=0; d--){
       def dev = loads[d].switch
    
       // Clear minOnTime status
      load_setMinOnTimeStartedAt_epoch(d, 0)
        
        if(dev == null){
           setExpectedState(d, "off")
            continue;
        }
        
        setExpectedState(d, dev.currentState)
    }

   trace_exiting("loads_init")
}

private loads_turnOnHighPriorityLoad(excessPower){
   trace_entering("loads_turnOnHighPriorityLoad()")

   log_algorithm("Excess power of " + excessPower + " w, processing ...")
    
   // Check to turn on Battery Storage System first
    if(battStorage_chargingEnabled()){
       // @todo Need to handle the request for more charging power.
        log_algorithm("Battery available for charging - checking if more than " + battStorage_startingPower() + " w to start charging")
          if(excessPower > battStorage_startingPower()){
             log_algorithm("Starting charger")
            battStorage_charger_on(excessPower)
            // then action completed  @todo Check for race condition
              trace_exiting("loads_turnOnHighPriorityLoad()")
              return true
        } else {
             log_algorithm("Not enough power to start charging")
        }
    }
    
   log_algorithm("Excess power of " + excessPower + " w, processing ...")

   def loads = getLoads()
    
      for(def d=0; d<loads.size; d++){
           def dev = loads[d].switch
            def devMore = loads[d].devMore
            int switchPower = 0
            def startupDuration_secs = devMore.startupDuration_secs
            
            if(dev == null) continue
            
            if(startupDuration_secs == null) startupDuration_secs = 0
            
            if(devMore.maxPower != null) switchPower = devMore.maxPower
            
            // @todo If previous start-up power available use that
            
          boolean enoughSolarPowerForSwitch = (switchPower*1.1 < excessPower)
         if(devMore.enabled && (dev.currentSwitch == "off") && enoughSolarPowerForSwitch) {
                   log_algorithm("found '" + dev.label + "' that will draw " + switchPower +"w.  Turning on ...")
                    
                    load_on(d)
                    
               trace_exiting("loads_turnOnHighPriorityLoad()")
                   return true
           }
        } // end for loads
//      log_algorithm("no devices that will draw less than " + excessPower + "w to turn on")

   trace_exiting("loads_turnOnHighPriorityLoad()")
}

private   loads_turnOffLowestPriorityLoad() {
   trace_entering("loads_turnOffLowestPriorityLoad()")
    
   def completed = false
   def loads = getLoads()
    
   for(def d=loads.size-1; d>=0; d--){
          def dev = loads[d].switch
        def devMore = loads[d].devMore     

        if(dev == null) continue

      if(devMore.enabled && dev.currentSwitch == "on" && !load_inMinOnTime(d)){  // Turn it off                
              load_off(d)               
            completed = true
            break;
          }
    }

   if(!completed && battStorage_chargingEnabled() && battStorage_charging()){
       log_algorithm("Turning charging off");
         battStorage_charger_off()
        completed = true        
    }
    
   if(!complete) log_algorithm("No devices to switch off")
    
    trace_exiting("loads_turnOffLowestPriorityLoad()")
    return completed
}

private load_on(d){
   trace_entering("load_on()")
    
   def loads = getLoads()
    def dev = loads[d].switch
      log_algorithm("Load " + d + " ('" + dev.label + "') - turning on ")

   // Handle start-up time
   setState_switching()
   sendMessage("Switching '" + dev.label + "' on")

   load_setMinOnTimeStartedAt_epoch(d, now())
    setExpectedState(d, "on")
      dev.on()

   runIn(startupDuration_secs + getOtherResponseTime(), setState_normal)

   trace_exiting("load_on()")
}

private getOtherResponseTime(){
    return 5
}

private load_off(d){   // @todo - Check that this is re-entrant with load_on
   def dev = getLoads()[d].switch
      log_algorithm("turning load '" + dev.label + "' off ...")

     setState_switching()        
   sendMessage("Switching '" + dev.label + "' off")
   load_setMinOnTimeStartedAt_epoch(d, 0)
    setExpectedState(d, "off")

   dev.off()

   runIn(4 /* EV response time */ + getOtherResponseTime(), setState_normal)   // @tood Add turn off time (eg EVSE = 5 seconds)
}

private load_handleMinOnTimes(){
// @todo Not working correctly
   trace_entering("load_handleMinOnTimes()")
    
   /*
       if device is on when not expected to be on then set minOnTimeStarted time
        
    */
        
    def loads = getLoads()
   for(def d=loads.size-1; d>=0; d--){
          def dev = loads[d].switch
        def devMore = loads[d].devMore
      def minOnTimeStartedAt_epoch = load_getMinOnTimeStartedAt_epoch(d)
        
        if(minOnTimeStartedAt_epoch == null){
           load_setMinOnTimeStartedAt_epoch(d, 0)
        }
        if(dev == null) continue

      // Handle unexpected switch states
        if(dev.currentSwitch != getExpectedState(d)){
           if(dev.currentSwitch == "on"){
               log_algorithm "Load " + d + " - manually switched on"
               load_setMinOnTimeStartedAt_epoch(d, now())
                setExpectedState(d, "on")
            }

         if(dev.currentSwitch == "off"){  // Device may have reset and started up in 'off' state
               log_algorithm "Load " + d + " - expected to be '" + getExpectedState(d) + "' but was 'off'" 
               load_setMinOnTimeStartedAt_epoch(d, 0)
                setExpectedState(d, "off")
            }
      }
        
      // Detect and handle end of min on time period
//        log_algorithm "Load " + d + " - onSeconds(d) = " + onSeconds(d)
      if(onSeconds(d) > devMore.minOnTime_secs){
              log_algorithm "Load " + d + " - at end of minOnTime"
              load_setMinOnTimeStartedAt_epoch(d, 0)
        }
           
        // Detect and handle manual switch off during override
      if( (dev.currentSwitch == "off") && (minOnTimeStartedAt_epoch > 0) ){
              log_algorithm "Load " + d + " - manually switched off during minOnTime"
            load_setMinOnTimeStartedAt_epoch(d, 0)  // so simulate as it has just been turned on 
        }
        
    }

   trace_exiting("load_handleMinOnTimes()")
}

private getExpectedState(d){
   switch(d){
       case 0: return atomicState.expectedState0; break;
       case 1: return atomicState.expectedState1; break;
       case 2: return atomicState.expectedState2; break;
       case 3: return atomicState.expectedState3; break;
       case 4: return atomicState.expectedState4; break;
        default: log.error("getExpectedState(" + d + ")"); break;
     }
}

private setExpectedState(d, state){
   switch(d){
       case 0: atomicState.expectedState0 = state; break;
       case 1: atomicState.expectedState1 = state; break;
       case 2: atomicState.expectedState2 = state; break;
       case 3: atomicState.expectedState3 = state; break;
       case 4: atomicState.expectedState4 = state; break;
     }
}

private load_inMinOnTime(d){
   def minOnTimeStartedAt_epoch = load_getMinOnTimeStartedAt_epoch(d)
    def beingOverridden = false
    def minOnTime_secs = getLoads()[d].devMore.minOnTime_secs
    def secsLeft = 0
    
    if(minOnTime_secs == null) {
       log_algorithm("Load " + d + " minOnTime_Secs = null, setting to 0 (" + getLoads()[d] + ")")
       minOnTime_secs = 0
    }

   if(minOnTimeStartedAt_epoch > 0){  // then is currently being overridden
      // Check and handle end of minOnTime
        secsLeft = minOnTime_secs  - (now() - minOnTimeStartedAt_epoch)/1000
      beingOverridden = secsLeft > 0
      if(secsLeft < 0) {  // then has come to end of minimum time
         beingOverridden = false
      }
   }

//   log_algorithm("Load " + d + " beingOverriden = " + beingOverridden + " (minOntime_secs = " + minOnTime_secs + ", secsLeft = " + secsLeft + ", minOnTimeStartedAt_epoch = " + minOnTimeStartedAt_epoch + ")")
   return beingOverridden
}        

def load_setMinOnTimeStartedAt_epoch(d, epoch){
   log_algorithm("Load " + d + " - Setting minOnTime epoch start to " + epoch)
   switch(d+1){
       case 1: atomicState.switch1_minOnTimeStartAt_epoch = epoch; break
        case 2: atomicState.switch2_minOnTimeStartAt_epoch = epoch; break
      case 3: atomicState.switch3_minOnTimeStartAt_epoch = epoch; break
        case 4: atomicState.switch4_minOnTimeStartAt_epoch = epoch; break
        case 5: atomicState.switch5_minOnTimeStartAt_epoch = epoch; break
   }
}

def load_getMinOnTimeStartedAt_epoch(d){
      switch(d+1){
       case 1: return atomicState.switch1_minOnTimeStartAt_epoch; break
       case 2: return atomicState.switch2_minOnTimeStartAt_epoch; break
       case 3: return atomicState.switch3_minOnTimeStartAt_epoch; break
       case 4: return atomicState.switch4_minOnTimeStartAt_epoch; break
       case 5: return atomicState.switch5_minOnTimeStartAt_epoch; break
   }
    log.error("load_getMinOnTimeStartedAt_epoch: Device " + d + " not found")
}   

private getLoads(){
   return [
           [
               switch:switch1,
              devMore: [
                   maxPower: switch1_maxPower, 
                    enabled: switch1_enabled, 
                    startupDuration_secs: switch1_startupDuration_secs, 
                    minOnTime_secs: switch1_minOnTime_secs
               ]
            ],[
               switch:switch2,
               devMore: [
                   maxPower: switch2_maxPower, 
                    enabled: switch2_enabled, 
                    startupDuration_secs: switch2_startupDuration_secs, 
                    minOnTime_secs: switch2_minOnTime_secs
                ]
            ],
           [
               switch:switch3,
              devMore: [
                   maxPower: switch3_maxPower, 
                   enabled: switch3_enabled, 
                   startupDuration_secs: switch3_startupDuration_secs, 
                    minOnTime_secs: switch3_minOnTime_secs
                ]
            ],[
               switch:switch4,
               devMore: [
                   maxPower: switch4_maxPower, 
                    enabled: switch4_enabled, 
                    startupDuration_secs: switch4_startupDuration_secs, 
                    minOnTime_secs: switch4_minOnTime_secs
                ]
            ],[
               switch:switch5,
               devMore: [
                   maxPower: switch5_maxPower, 
                    enabled: switch5_enabled, 
                    startupDuration_secs: switch5_startupDuration_secs, 
                    minOnTime_secs: switch5_minOnTime_secs,
                ]
            ]
        ]
}

// ############################################# Battery Storage #####################################
/*

All update on the fly (without change individual device status)

    Preverse device status if already enabled.
    If state changed from enabled to disabled, make sure all devices are turned off
*/

/* Standard Interface ---
   _init()
   _off()
   _startingPower()
   _on(power)
   _currentPower(solarPower) */


def battStorage_init(){

   //atomicState.battStorage_availableEnergy_wh = 0.1

    if(!battStorage_configured()){
       log.error("battStorage_init() - Not fully configured")
        return
    }

   // Create stored variables as necessary
   if(atomicState.battStorage_lastNow == null) {
       atomicState.battStorage_lastNow = 0
   }
   if(atomicState.battStorage_inverterMins == null) {
       atomicState.battStorage_inverterMins = 0
   }

   // Reset battery energy if necessary
   if(atomicState.battStorage_availableEnergy_wh == null || battStorage_startingBatteryLevel){
       log_algorithm("battStorage_init() - Resetting battery storage system to " + battStorage_startingBatteryLevel + "%")
       atomicState.battStorage_availableEnergy_wh = battStorage_installed_capacity_wh * (battStorage_startingBatteryLevel / 100)
   }
    
    
   // Handle new enabled values
   //     NB: Important use case 
   // 
   // Previous   New      Action
   // ------------------------
   // Off      Off      Nothing (because custom testing might be in progress)
   // Off      On      Nothing (system will sort itself out)
   // On      Off      Turn everything off  <===== only thing that needs action
   // On      On      Nothing (because custom testing might be in progress)
   // (Do not use helper functions as they will block actions if Battery Storage System is disabled)
   if(atomicState.battStorage_previousEnabled && !battStorage_enabled()){
      log_algorithm("battStorage_init() - Battery storage system has been turned off.  Turning off devices.")
      battStorage_inverter_switch.off()
      battStorage_charger6A_switch.off()
      battStorage_charger30A_switch.off()
   }
   atomicState.battStorage_previousEnabled = battStorage_enabled()
   
   // Synchronize device status with current config
   if(!battStorage_chargingEnabled()) {
      log_algorithm("battStorage_init() - Charging not enabled.  Turning off chargers.")
      battStorage_charger6A_switch.off()
      battStorage_charger30A_switch.off()
   }
   if(!battStorage_inverterEnabled()) {
      log_algorithm("battStorage_init() - Inverter not enabled.  Turning off inverter.")
   	  battStorage_inverter_switch.off()
   }
   
}

def battStorage_off(){
}

def battStorage_startingPower(){
   return Math.min(battStorage_30Acharger_minPower(), 150)
}

/* @todo - how to handle charger and inverter control */
def battStorage_charger_on(maxPower){
   def completed = false
    
      log_algorithm("battStorage_charger_on(" + maxPower + ")")
   if(battStorage_failedEnabledCheck("charge battStorage_charger_on(" + maxPower + ")")) return

   // Turn on the most suitable charger.
    if(battStorage_charger30A_switch.currentSwitch == "on") {  // This is the best charger, so leave it on
       log_algorithm("battStorage_charger_on() - Best charger already on")
       return false  // No change
    }
   if(maxPower > battStorage_30Acharger_minPower() ){
       battStorage_ensureDeviceIsOff(battStorage_charger6A_switch)
       battStorage_charger30A_switch.on()
        completed = true
    }else if( (battStorage_startingPower() == 150) && (maxPower > 150) ){
      battStorage_ensureDeviceIsOff(battStorage_charger30A_switch)
       battStorage_charger6A_switch.on()
        completed = true
    }

   if(completed){
       log_algorithm("battStorage_charger_on() - Turned charger on")

      // Make sure inverter is off, so that it doesn't hunt with battery charger
        battStorage_ensureDeviceIsOff(battStorage_inverter_switch)

       // Turn on battery state tracking
       atomicState.battStorage_lastNow = now()
    }
}

/** 
 * + = consuming
 * - = generating
 */
private battStorage_currentPower(){
   def data = summaryData()
   def p = 0
    
   if(battStorage_charger6A_switch.currentSwitch == "on") p = 150
   if(battStorage_charger30A_switch.currentSwitch == "on") p = battStorage_30Acharger_minPower()

   if(battStorage_inverter_switch.currentSwitch == "on"){
      // PhotWater = 0
      // Pinverter = PknownDevices - Psolar   

      def pInverterMax = 350
      def pBaseload = getBaseload()   // Day-time
      def pOnDevices = pBaseload   // @todo Total up all devices that are on, not just baseline
      def pSolar = data.solarPower

      // Calculate power
      p = pOnDevices - pSolar
      if(p > pInverterMax) p = pInverterMax
      p = -Math.round(p)
   }

   p = Math.round(p)
   log_algorithm("battStorage_currentPower() = " + Math.round(p))
        
   return p
}

private battStorage_chargingEnabled(){
    def enabled = (battStorage_mode == "on") || (battStorage_mode == "chargingOnly")
    enabled = enabled && (!battStorage_charged() || !battStorage_protectBattery)
    
    return enabled
}

private battStorage_inverterEnabled(){
   def enabled = (battStorage_mode == "on") || (battStorage_mode == "outputOnly")
    log.debug "battStorage_inverterEnabled() = " + enabled
    return enabled
}

// ---------------------- Local Routines -----------------------
/**
 *   Returns if this module is correctly configured or not
 */
private battStorage_configured(){
   def configured = true
    
    if(battStorage_inverter_switch == null){
       log.error("battStorage: Inverter not configured")
        configured = false
    }
    
    if((battStorage_charger30A_switch == null) && (battStorage_charger6A_switch == null) ){
       log.error("battStorage: Charger not configured")
        configured = false
    } 
    if(battStorage_installed_capacity_wh == null){
       log.error("battStorage: Installed capacity not configured")
        configured = false
    }
    
    return configured
}

/**
 * Return textual representation of status 
 *
 * Example status values
 *      "Off (Battery = 99 %)"
 *      "Idle (Battery = 99 %, waiting for usable excess power of 400 w)"
 *      "Charging (Battery = 99 %, charging at 323 w)"
 *     "Outputting (Battery = 99 %, outputting at 481 w)"
 *     "Not fully configured (Battery = 99 %)"
 *
 */
private battStorage_state_data(){  // aka battStorage_status()
   def state = ""
   def power = ""
   def additionalInfo = ""

   if(!battStorage_configured()) return "Not fully configured"

   state = battStorage_enabled() ? "Auto" : "Manual"
   power = battStorage_currentPower()
   if(!battStorage_charging() && (battStorage_inverter_switch.currentSwitch == "off")){
      if(battStorage_enabled()){
         state = "Idle"
         // Assume battery is between charged and discharged
         additionalInfo = "waiting for usable excess power of more than " + battStorage_startingPower() + " w"   // @todo - This needs checking
         if(battStorage_charged()){
            state = state + " (Charged)"
            additionalInfo = "waiting to supply mains power."   // Overwrite assumption
         }
         if(battStorage_discharged()){
            state = state + " (Discharged)"
         }
      }
   }

   if(additionalInfo.length() > 0) additionalInfo = additionalInfo + " | "
   additionalInfo = additionalInfo + "Battery = " + Math.round(battStorage_battery_level()*100*10)/10 + " %"
   
   if(!battStorage_protectBattery){
      additionalInfo = additionalInfo + " | WARNING: "
      if(battStorage_discharged() && (battStorage_inverter_switch.currentSwitch == "on")){
         additionalInfo = additionalInfo + "Battery being discharged below recommended 80%"
      } else if(battStorage_charged() && battStorage_charging()){
         additionalInfo = additionalInfo + "Battery being overcharged"
      } 
      additionalInfo = additionalInfo + " - battery protection disabled"
   }

   return [state: state, power:power, additionalInfo: additionalInfo]
}

private battStorage_enabled(){
   return battStorage_mode != "off"
}

private battStorage_battery_level(){
   return Math.round(atomicState.battStorage_availableEnergy_wh / battStorage_installed_capacity_wh * 100 * 10)/100/10
}

private battStorage_inverter_on(){
   if(battStorage_failedEnabledCheck("battStorage_inverter_on()")) return
    
   // Make sure charger is off, so that it doesn't hunt with battery charger
   if(battStorage_charging()){
       log.debug("battStorage_inverter_on() - Turning all chargers off to allow inverter to turn on")
       battStorage_charger30A_switch.off()
       battStorage_charger6A_switch.off()
      }

   battStorage_inverter_switch.on()

   // Turn on battery state tracking
    atomicState.battStorage_lastNow = now()
}

private battStorage_inverter_off(){
   if(battStorage_failedEnabledCheck("battStorage_inverter_off()")) return
    
    battStorage_inverter_switch.off()

    // Turn off battery state tracking
    battStorage_cron()
    atomicState.battStorage_lastNow = 0
}


private battStorage_charger_off(){
   if(battStorage_failedEnabledCheck("battStorage_charger_off()")) return

   battStorage_ensureDeviceIsOff(battStorage_charger6A_switch)
    battStorage_ensureDeviceIsOff(battStorage_charger30A_switch)
    
    // Turn off battery state tracking
    battStorage_cron()  // Update battery energy level
    atomicState.battStorage_lastNow = 0
}

// Must be callable from battStorage_init(), so must work with any number of configured charger switches
private battStorage_charging(){
   def charging = false
    
   if(battStorage_charger30A_switch != null){
       charging = charging || battStorage_charger30A_switch.currentSwitch == "on"
    }
    if(battStorage_charger6A_switch != null){
       charging = charging || battStorage_charger6A_switch.currentSwitch == "on"
    }
   return charging
}

private battStorage_charged(){    
   log.debug("atomicState.battStorage_availableEnergy_wh = " + atomicState.battStorage_availableEnergy_wh)
   return atomicState.battStorage_availableEnergy_wh >= battStorage_installed_capacity_wh
}

private battStorage_discharged(){
   return battStorage_battery_level() < 0.8
}


private battStorage_cron(){
   def data = summaryData()
   def inverter_on = (battStorage_inverter_switch != null) ? (battStorage_inverter_switch.currentSwitch == "on") : false
   def charging = battStorage_charging()
   def batteryEfficiency = 0.85
   def oneSideOfBatteryEfficiency = Math.sqrt(batteryEfficiency)
   def invEfficiency = 0.8
   def chgEfficiency = 0.8
        
   if(!battStorage_configured()) return

   def p = battStorage_currentPower()
   log.debug("battStorage_cron() - p = " + p + ", charging = " + charging + ", inverter_on = " + inverter_on + ", battStorage_charged() = " + battStorage_charged())
    
   if(charging || inverter_on){
       // Handle if just started charging or outputting
       if(atomicState.battStorage_lastNow == 0){
           log.info("battStorage_cron() - Started tracking energy use")
           atomicState.battStorage_lastNow = now()
            atomicState.battStorage_inverterMins = 0
        }

       float inc_secs = (now() - atomicState.battStorage_lastNow)/1000
        float inc_wh = 0, inc_ws = 0

      if(charging){
          inc_ws = chgEfficiency * (p * inc_secs) * oneSideOfBatteryEfficiency
      }

      if(battStorage_inverter_switch.currentSwitch == "on"){
         inc_ws = (p * inc_secs)  / invEfficiency / oneSideOfBatteryEfficiency
      }

        inc_wh = inc_ws / 3600
        log_algorithm("battStorage_cron() - inc_ws = " + inc_ws + ", inc_wh = " + inc_wh)

        // Update battery energy level 
          atomicState.battStorage_availableEnergy_wh = atomicState.battStorage_availableEnergy_wh + inc_wh
        if(battStorage_charged()){
           atomicState.battStorage_availableEnergy_wh = battStorage_installed_capacity_wh
           log_algorithm("battStorage_cron() - Battery full.")
        }
        atomicState.battStorage_lastNow = now()
    } else {
       atomicState.battStorage_lastNow = 0
    }

   // Don't discharge more than the the defined depth-of-discharge
    def usable_installed_capacity_wh = 0.8 * battStorage_installed_capacity_wh   // @todo 
    
//    log.debug("battStorage_cron() - battStorage_availableEnergy_wh = " + battStorage_availableEnergy_wh + ", battStorage_installed_capacity_wh = " + battStorage_installed_capacity_wh)
   if( battStorage_protectBattery && battStorage_discharged()){
       log_algorithm("battStorage_cron() - Can't discharge, battery below depth-of-discharge")
       // @todo Turn inverter off
    }
    
}

// ---------------- Local --------------

private battStorage_failedEnabledCheck(action){
   if(battStorage_mode == null || battStorage_mode == "off"){
         log.warn("battStorage_failedEnabledCheck() - Battery Storage System is disabled - ignoring " + action )
        return true
    }
    return false
}

private float battStorage_30Acharger_minPower(){
   return 500   // @todo - Fix battery level calculation
    
   def dod = 1 - battStorage_battery_level()
    def p = 75.7 + 734*dod + 6891*(dod**2)
    if(p > 450) p = 450
//    log_algorithm("battStorage_30Acharger_minPower() = " + p + ", battStorage_battery_level() = " + battStorage_battery_level() + ", dod = " + dod)
   return p
}


private battStorage_ensureDeviceIsOff(device){
    if(device.currentSwitch == "on") device.off()
}


// ---------------- Testing ------------
private test_battStorage_projected30AChargingPower(){
    // Perform some initial tests
    // Calculate in reverse order to display in increasing order in log
    /*
   log.debug "Max charging power for 30 mins = " + battStorage_projected30AChargingPower(30)
   log.debug "Max charging power for 2.1 mins = " + battStorage_projected30AChargingPower(2.1)
   log.debug "Max charging power for 2 mins = " + battStorage_projected30AChargingPower(2)
    log.debug "Max charging power fully charged for 1.9 mins = " + battStorage_projected30AChargingPower(0.4)
   log.debug "Max charging power fully charged for 0 mins = " + battStorage_projected30AChargingPower(0)
    */
}

// ################################## EVSE ######################################

/* Standard Interface ---
   _init()
   _off()
   _startingPower()
   _on(power)
   _currentPower(solarPower) */


def evse_init(){
}

def evse_off(){
}

def evse_startingPower(){
   return evse_calcPower(0)
}

def evse_on(power){
   if(power > 1400){
      evse_max_power_switch.on()
    } else if(power > evse_getMinPower()){
      evse_solar_switch.on()
    }
}

def evse_currentPower(solarPower){
   return evse_calcPower(solarSolar)
}

// ------------------------------ Local Routines ----------------------------
def evse_getMinPower(){
   625
}

private evse_calcPower(solarSolar){
   def minEvsePower = evse_getMinPower()
    def maxEvsePower = 1500   // At 110v  @todo Needs to automatically detect voltage
    
   if(solarPower == null) return null

    if(evse_solar_switch.currentSwitch == "on")
    {
          def solarPower = solar_sw.currentState("power").value as Float   // @todo - May need to check for correct config of solar power
            def evsePower = minEvsePower + (0.8*(solarPower-minEvsePower) > 0 ? 0.8*(solarPower-minEvsePower) : 0)
           evsePower = (evsePower > maxEvsePower) ? maxEvsePower : evsePower

           return evsePower
   }
    if(evse_max_power_switch.currentSwitch == "on")
    {
           return maxEvsePower;
    }
    
    return 0
}

// ################################# Logging ####################################
def trace_entering(procName){
//   log.trace("Entering: " + procName)
}

def trace_exiting(procName){
//   log.trace("Exiting:" + procName)
}

def log_algorithm(msg){
   log.info("___" + msg)
}

