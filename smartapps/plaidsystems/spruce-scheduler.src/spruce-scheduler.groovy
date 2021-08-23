/**
 *  Spruce Scheduler Pre-release V2.55 - Updated 8/2019
 *
 *
 *  Copyright 2015 Plaid Systems
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

-------v2.55-------------------
-update weather zipcode field to find lat&long if no zipcode found, works in Canada
-remove zipcode page and move entry to weatherpage
-correct behavior when no days selected
-add additional checks for missing location

-------v2.54-------------------
-update weather to use new weather api

-------v2.53.1-------------------
-ln 210: enableManual string modified
-ln 496: added code for old ST app zoneNumber number to convert to enum for app update compatibility
-ln 854: unschedule if NOT running to clear/correct manual subscription
-ln 863: weather scheduled if rain OR seasonal enabled, both off is no weather check scheduled
-ln 1083: added sync check to manual start
-ln 1538: corrected contact delay minimum fro 5s to 10s

-------v2.52---------------------
 -Major revision by BAB
 *
 */

definition(
    name: "Spruce Scheduler",
    namespace: "plaidsystems",
    author: "Plaid Systems",
    description: "Setup schedules for Spruce irrigation controller",
    category: "Green Living",
    iconUrl: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    iconX2Url: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    iconX3Url: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png",
    pausable: true
)

preferences {
    page(name: 'startPage')
    page(name: 'autoPage')
    page(name: 'weatherPage')
    page(name: 'globalPage')
    page(name: 'contactPage')
    page(name: 'delayPage')
    page(name: 'zonePage')

    page(name: 'zoneSettingsPage')
    page(name: 'zoneSetPage')
    page(name: 'plantSetPage')
    page(name: 'sprinklerSetPage')
    page(name: 'optionSetPage')

    //found at bottom - transition pages
    page(name: 'zoneSetPage1')
    page(name: 'zoneSetPage2')
    page(name: 'zoneSetPage3')
    page(name: 'zoneSetPage4')
    page(name: 'zoneSetPage5')
    page(name: 'zoneSetPage6')
    page(name: 'zoneSetPage7')
    page(name: 'zoneSetPage8')
    page(name: 'zoneSetPage9')
    page(name: 'zoneSetPage10')
    page(name: 'zoneSetPage11')
    page(name: 'zoneSetPage12')
    page(name: 'zoneSetPage13')
    page(name: 'zoneSetPage14')
    page(name: 'zoneSetPage15')
    page(name: 'zoneSetPage16')
}

def startPage(){
    dynamicPage(name: 'startPage', title: 'Spruce Smart Irrigation setup', install: true, uninstall: true)
    {
        section(''){
            href(name: 'globalPage', title: 'Schedule settings', required: false, page: 'globalPage',
                image: 'http://www.plaidsystems.com/smartthings/st_settings.png',
                description: "Schedule: ${enableString()}\nWatering Time: ${startTimeString()}\nDays:${daysString()}\nNotifications:${notifyString()}"
            )
        }

        section(''){
            href(name: 'weatherPage', title: 'Weather Settings', required: false, page: 'weatherPage',
                image: 'http://www.plaidsystems.com/smartthings/st_rain_225_r.png',
                description: "Weather from: ${zipString()}\nRain Delay: ${isRainString()}\nSeasonal Adjust: ${seasonalAdjString()}"
            )
        }

        section(''){
            href(name: 'zonePage', title: 'Zone summary and setup', required: false, page: 'zonePage',
                image: 'http://www.plaidsystems.com/smartthings/st_zone16_225.png',
                description: "${getZoneSummary()}"
            )
        }

        section(''){
            href(name: 'delayPage', title: 'Valve delays & Pause controls', required: false, page: 'delayPage',
                image: 'http://www.plaidsystems.com/smartthings/st_timer.png',
                description: "Valve Delay: ${pumpDelayString()} s\n${waterStoppersString()}\nSchedule Sync: ${syncString()}"
            )
        }
        
        section(''){
            href(title: 'Spruce Irrigation Knowledge Base', //page: 'customPage',
                  description: 'Explore our knowledge base for more information on Spruce and Spruce sensors.  Contact form is ' +
                             'also available here.',
                required: false, style:'embedded',
                image: 'http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png',
                url: 'http://support.spruceirrigation.com'
            )
        }
        
        section(''){
            href(title: 'Scheduler Version 2.55',
                  description: "Updated June 2019"
            )
        }
    }
}

def globalPage() {
    dynamicPage(name: 'globalPage', title: '') {
        section('Spruce schedule Settings') {
                label title: 'Schedule Name:', description: 'Name this schedule', required: false
                input 'switches', 'capability.switch', title: 'Spruce Irrigation Controller:', description: 'Select a Spruce controller', required: true, multiple: false
        }

        section('Program Scheduling'){
            input 'enable', 'bool', title: 'Enable watering:', defaultValue: 'true', metadata: [values: ['true', 'false']]
            input 'enableManual', 'bool', title: 'Enable this schedule for manual start, only 1 schedule should be enabled for manual start at a time!', defaultValue: 'true', metadata: [values: ['true', 'false']]
            input 'startTime', 'time', title: 'Watering start time', required: true
            paragraph(image: 'http://www.plaidsystems.com/smartthings/st_calander.png',
                      title: 'Selecting watering days',
                      'Selecting watering days is optional. Spruce will optimize your watering schedule automatically. ' +
                      'If your area has water restrictions or you prefer set days, select the days to meet your requirements. ')
            input (name: 'days', type: 'enum', title: 'Water only on these days...', required: false, multiple: true, metadata: [values: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday', 'Even', 'Odd']])
        }

        section('Push Notifications') {
                input(name: 'notify', type: 'enum', title: 'Select what push notifications to receive.', required: false,
                    multiple: true, metadata: [values: ['Daily', 'Delays', 'Warnings', 'Weather', 'Moisture', 'Events']])
                input(name: 'logAll', type: 'bool', title: 'Log all notices to Hello Home?', defaultValue: 'false', options: ['true', 'false'])
        }
    }
}

def weatherPage() {
    dynamicPage(name: 'weatherPage', title: 'Weather settings') {
       section("Location to get weather forecast and conditions:") {
            input(name: 'zipcode', type: 'text', title: "Zipcode default location: ${getDefaultLocation()}", required: false, submitOnChange: true)
            input 'isRain', 'bool', title: 'Enable Rain check:', metadata: [values: ['true', 'false']]
            input 'rainDelay', 'decimal', title: 'inches of rain that will delay watering, default: 0.2', required: false
            input 'isSeason', 'bool', title: 'Enable Seasonal Weather Adjustment:', metadata: [values: ['true', 'false']]
        }
    }
}

private String getDefaultLocation() {
    String DefaultLocation = "Not set"
    if(location?.zipCode) DefaultLocation = location.zipCode
    if (!location?.zipCode?.isNumber() && location?.latitude && location?.longitude) DefaultLocation = "${location.latitude.floatValue()},${location.longitude.floatValue()}"
    return DefaultLocation
}

private String startTimeString(){
    if (!startTime) return 'Please set!' else return hhmm(startTime)
}

private String enableString(){
    if(enable && enableManual) return 'On & Manual Set'
    else if (enable) return 'On & Manual Off'
    else if (enableManual) return 'Off & Manual Set'
    else return 'Off'
}

private String waterStoppersString(){
    String stoppers = 'Contact Sensor'
    if (settings.contacts) {
        if (settings.contacts.size() != 1) stoppers += 's'
        stoppers += ': '
        int i = 1
        settings.contacts.each {
            if ( i > 1) stoppers += ', '
            stoppers += it.displayName
            i++
        }
        stoppers = "${stoppers}\nPause: When ${settings.contactStop}\n"
    }
    else {
        stoppers += ': None\n'
    }
    stoppers += "Switch"
    if (settings.toggles) {
        if (settings.toggles.size() != 1) stoppers += 'es'
        stoppers += ': '
        int i = 1
        settings.toggles.each {
            if ( i > 1) stoppers += ', '
            stoppers += it.displayName
            i++
        }
        stoppers = "${stoppers}\nPause: When switched ${settings.toggleStop}\n"
    }
    else {
        stoppers += ': None\n'
    }
    int cd = 10
    if (settings.contactDelay && settings.contactDelay.toInteger() > 10) cd = settings.contactDelay.toInteger()
    stoppers += "Restart Delay: ${cd} secs"
    return stoppers
}

private String isRainString(){
    if (settings.isRain && !settings.rainDelay) return '0.2' as String
    if (settings.isRain) return settings.rainDelay as String else return 'Off'
}

private String seasonalAdjString(){
    if(settings.isSeason) return 'On' else return 'Off'
}

private String syncString(){
    if (settings.sync) return "${settings.sync.displayName}" else return 'None'
}

private String notifyString(){
    String notifyStr = ''
    if(settings.notify) {
          if (settings.notify.contains('Daily'))         notifyStr += ' Daily'
          //if (settings.notify.contains('Weekly'))     notifyStr += ' Weekly'
          if (settings.notify.contains('Delays'))     notifyStr += ' Delays'
          if (settings.notify.contains('Warnings'))     notifyStr += ' Warnings'
          if (settings.notify.contains('Weather'))     notifyStr += ' Weather'
          if (settings.notify.contains('Moisture'))     notifyStr += ' Moisture'
          if (settings.notify.contains('Events'))     notifyStr += ' Events'
       }
       if (notifyStr == '')    notifyStr = ' None'
       if (settings.logAll) notifyStr += '\nSending all Notifications to Hello Home log'

       return notifyStr
}

private String daysString(){
    String daysString = ''
    if (days){
        if(days.contains('Even') || days.contains('Odd')) {
            if (days.contains('Even'))         daysString += ' Even'
              if (days.contains('Odd'))         daysString += ' Odd'
        }
        else {
            if (days.contains('Monday'))     daysString += ' M'
            if (days.contains('Tuesday'))     daysString += ' Tu'
            if (days.contains('Wednesday')) daysString += ' W'
            if (days.contains('Thursday'))     daysString += ' Th'
            if (days.contains('Friday'))     daysString += ' F'
            if (days.contains('Saturday'))     daysString += ' Sa'
            if (days.contains('Sunday'))     daysString += ' Su'
        }
    }
    if(daysString == '') return ' Any'

    else return daysString
}

private String hhmm(time, fmt = 'h:mm a'){
    def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    return f.format(t)
}

private String pumpDelayString(){

    if (!pumpDelay) return '0' else return pumpDelay as String
    
}

def delayPage() {
    dynamicPage(name: 'delayPage', title: 'Additional Options') {
        section(''){
            paragraph image: 'http://www.plaidsystems.com/smartthings/st_timer.png',
                      title: 'Pump and Master valve delay',
                      required: false,
                      'Setting a delay is optional, default is 0.  If you have a pump that feeds water directly into your valves, ' +
                      'set this to 0. To fill a tank or build pressure, you may increase the delay.\n\nStart->Pump On->delay->Valve ' +
                      'On->Valve Off->delay->...'
            input name: 'pumpDelay', type: 'number', title: 'Set a delay in seconds?', defaultValue: '0', required: false
        }

        section(''){
            paragraph(image: 'http://www.plaidsystems.com/smartthings/st_pause.png',
                      title: 'Pause Control Contacts & Switches',
                      required: false,
                      'Selecting contacts or control switches is optional. When a selected contact sensor is opened or switch is ' +
                      'toggled, water immediately stops and will not resume until all of the contact sensors are closed and all of ' +
                      'the switches are reset.\n\nCaution: if all contacts or switches are left in the stop state, the dependent ' +
                      'schedule(s) will never run.')
            input(name: 'contacts', title: 'Select water delay contact sensors', type: 'capability.contactSensor', multiple: true,
                required: false, submitOnChange: true)            
            if (contacts)
                input(name: 'contactStop', title: 'Stop watering when sensors are...', type: 'enum', required: (settings.contacts != null),
                    options: ['open', 'closed'], defaultValue: 'open')
            input(name: 'toggles', title: 'Select water delay switches', type: 'capability.switch', multiple: true, required: false,
                submitOnChange: true)
            if (toggles)
                input(name: 'toggleStop', title: 'Stop watering when switches are...', type: 'enum',
                    required: (settings.toggles != null), options: ['on', 'off'], defaultValue: 'off')
            input(name: 'contactDelay', type: 'number', title: 'Restart watering how many seconds after all contacts and switches ' +
                    'are reset? (minimum 10s)', defaultValue: '10', required: false)
        }

        section(''){
            paragraph image: 'http://www.plaidsystems.com/smartthings/st_spruce_controller_250.png',
                           title: 'Controller Sync',
                          required: false,
                          'For multiple controllers only.  This schedule will wait for the selected controller to finish before ' +
                          'starting. Do not set with a single controller!'
                         input name: 'sync', type: 'capability.switch', title: 'Select Master Controller', description: 'Only use this setting with multiple controllers', required: false, multiple: false
        }
    }
}

def zonePage() {
    dynamicPage(name: 'zonePage', title: 'Zone setup', install: false, uninstall: false) {
        section('') {
            href(name: 'hrefWithImage', title: 'Zone configuration', page: 'zoneSettingsPage',
             description: "${zoneString()}",
             required: false,
             image: 'http://www.plaidsystems.com/smartthings/st_spruce_leaf_250f.png')
        }

        if (zoneActive('1')){
        section(''){
            href(name: 'z1Page', title: "1: ${getname("1")}", required: false, page: 'zoneSetPage1',
                image: "${getimage("1")}",
                description: "${display("1")}" )
            }
        }
        if (zoneActive('2')){
        section(''){
            href(name: 'z2Page', title: "2: ${getname("2")}", required: false, page: 'zoneSetPage2',
                image: "${getimage("2")}",
                description: "${display("2")}" )
            }
        }
        if (zoneActive('3')){
        section(''){
            href(name: 'z3Page', title: "3: ${getname("3")}", required: false, page: 'zoneSetPage3',
                image: "${getimage("3")}",
                description: "${display("3")}" )
            }
        }
        if (zoneActive('4')){
        section(''){
            href(name: 'z4Page', title: "4: ${getname("4")}", required: false, page: 'zoneSetPage4',
                image: "${getimage("4")}",
                description: "${display("4")}" )
            }
        }
        if (zoneActive('5')){
        section(''){
            href(name: 'z5Page', title: "5: ${getname("5")}", required: false, page: 'zoneSetPage5',
                image: "${getimage("5")}",
                description: "${display("5")}" )
            }
        }
        if (zoneActive('6')){
        section(''){
            href(name: 'z6Page', title: "6: ${getname("6")}", required: false, page: 'zoneSetPage6',
                image: "${getimage("6")}",
                description: "${display("6")}" )
            }
        }
        if (zoneActive('7')){
        section(''){
            href(name: 'z7Page', title: "7: ${getname("7")}", required: false, page: 'zoneSetPage7',
                image: "${getimage("7")}",
                description: "${display("7")}" )
            }
        }
        if (zoneActive('8')){
        section(''){
            href(name: 'z8Page', title: "8: ${getname("8")}", required: false, page: 'zoneSetPage8',
                image: "${getimage("8")}",
                description: "${display("8")}" )
            }
        }
        if (zoneActive('9')){
        section(''){
            href(name: 'z9Page', title: "9: ${getname("9")}", required: false, page: 'zoneSetPage9',
                image: "${getimage("9")}",
                description: "${display("9")}" )
            }
        }
        if (zoneActive('10')){
        section(''){
            href(name: 'z10Page', title: "10: ${getname("10")}", required: false, page: 'zoneSetPage10',
                image: "${getimage("10")}",
                description: "${display("10")}" )
            }
        }
        if (zoneActive('11')){
        section(''){
            href(name: 'z11Page', title: "11: ${getname("11")}", required: false, page: 'zoneSetPage11',
                image: "${getimage("11")}",
                description: "${display("11")}" )
            }
        }
        if (zoneActive('12')){
        section(''){
            href(name: 'z12Page', title: "12: ${getname("12")}", required: false, page: 'zoneSetPage12',
                image: "${getimage("12")}",
                description: "${display("12")}" )
            }
        }
        if (zoneActive('13')){
        section(''){
            href(name: 'z13Page', title: "13: ${getname("13")}", required: false, page: 'zoneSetPage13',
                image: "${getimage("13")}",
                description: "${display("13")}" )
            }
        }
        if (zoneActive('14')){
        section(''){
            href(name: 'z14Page', title: "14: ${getname("14")}", required: false, page: 'zoneSetPage14',
                image: "${getimage("14")}",
                description: "${display("14")}" )
            }
        }
        if (zoneActive('15')){
        section(''){
            href(name: 'z15Page', title: "15: ${getname("15")}", required: false, page: 'zoneSetPage15',
                image: "${getimage("15")}",
                description: "${display("15")}" )
            }
        }
        if (zoneActive('16')){
        section(''){
            href(name: 'z16Page', title: "16: ${getname("16")}", required: false, page: 'zoneSetPage16',
                image: "${getimage("16")}",
                description: "${display("16")}" )
            }
        }
    }
}

//code change for ST update file -> change input to zoneNumberEnum
private boolean zoneActive(z){
    if (!zoneNumberEnum && zoneNumber && zoneNumber >= z.toInteger()) return true
    else if (!zoneNumberEnum && zoneNumber && zoneNumber != z.toInteger()) return false
    else if (zoneNumberEnum && zoneNumberEnum.contains(z)) return true
    return false
}


private String zoneString() {
    String numberString = 'Add zones to setup'
    if (zoneNumber) numberString = "Zones enabled: ${zoneNumber}"
    if (learn) numberString = "${numberString}\nSensor mode: Adaptive"
    else numberString = "${numberString}\nSensor mode: Delay"
    return numberString
}

def zoneSettingsPage() {
    dynamicPage(name: 'zoneSettingsPage', title: 'Zone Configuration') {
           section(''){
            //input (name: "zoneNumber", type: "number", title: "Enter number of zones to configure?",description: "How many valves do you have? 1-16", required: true)//, defaultValue: 16)
            input 'zoneNumberEnum', 'enum', title: 'Select zones to configure', multiple: true,    metadata: [values: ['1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16']]
            input 'gain', 'number', title: 'Increase or decrease all water times by this %, enter a negative or positive value, Default: 0', required: false, range: '-99..99'
            paragraph image: 'http://www.plaidsystems.com/smartthings/st_sensor_200_r.png',
                          title: 'Moisture sensor adapt mode',
                          'Adaptive mode enabled: Watering times will be adjusted based on the assigned moisture sensor.\n\nAdaptive mode ' +
                          'disabled (Delay): Zones with moisture sensors will water on any available days when the low moisture setpoint has ' +
                          'been reached.'
             input 'learn', 'bool', title: 'Enable Adaptive Moisture Control (with moisture sensors)', metadata: [values: ['true', 'false']]
           }
    }
}

def zoneSetPage() {
    dynamicPage(name: 'zoneSetPage', title: "Zone ${state.app} Setup") {
        section(''){
            paragraph image: "http://www.plaidsystems.com/smartthings/st_${state.app}.png",
            title: 'Current Settings',
            "${display("${state.app}")}"
            input "name${state.app}", 'text', title: 'Zone name?', required: false, defaultValue: "Zone ${state.app}"
        }
        
        section(''){
             href(name: 'tosprinklerSetPage', title: "Sprinkler type: ${setString('zone')}", required: false, page: 'sprinklerSetPage',
                image: "${getimage("${settings."zone${state.app}"}")}",
                //description: "Set sprinkler nozzle type or turn zone off")
                description: 'Sprinkler type descriptions')
             input "zone${state.app}", 'enum', title: 'Sprinkler Type', multiple: false, required: false, defaultValue: 'Off', submitOnChange: true, metadata: [values: ['Off', 'Spray', 'Rotor', 'Drip', 'Master Valve', 'Pump']]
         }
         
         section(''){
            href(name: 'toplantSetPage', title: "Landscape Select: ${setString('plant')}", required: false, page: 'plantSetPage',
                image: "${getimage("${settings["plant${state.app}"]}")}",
                //description: "Set landscape type")
                description: 'Landscape type descriptions')
             input "plant${state.app}", 'enum', title: 'Landscape', multiple: false, required: false, submitOnChange: true, metadata: [values: ['Lawn', 'Garden', 'Flowers', 'Shrubs', 'Trees', 'Xeriscape', 'New Plants']]
            }

        section(''){
            href(name: 'tooptionSetPage', title: "Options: ${setString('option')}", required: false, page: 'optionSetPage',
                image: "${getimage("${settings["option${state.app}"]}")}",
                //description: "Set watering options")
                description: 'Watering option descriptions')
            input "option${state.app}", 'enum', title: 'Options', multiple: false, required: false, defaultValue: 'Cycle 2x', submitOnChange: true,metadata: [values: ['Slope', 'Sand', 'Clay', 'No Cycle', 'Cycle 2x', 'Cycle 3x']]
            }
        
        section(''){
            paragraph image: 'http://www.plaidsystems.com/smartthings/st_sensor_200_r.png',
                      title: 'Moisture sensor settings',
                      'Select a soil moisture sensor to monitor and control watering.  The soil moisture target value is set to a default value but can be adjusted to tune watering'
            input "sensor${state.app}", 'capability.relativeHumidityMeasurement', title: 'Select moisture sensor?', required: false, multiple: false
            input "sensorSp${state.app}", 'number', title: "Minimum moisture sensor target value, Setpoint: ${getDrySp(state.app)}", required: false
        }

        section(''){
            paragraph image: 'http://www.plaidsystems.com/smartthings/st_timer.png',
                      title: 'Optional: Enter total watering time per week',
                      'This value will replace the calculated time from other settings'
                input "minWeek${state.app}", 'number', title: 'Water time per week.\nDefault: 0 = autoadjust', description: 'minutes per week', required: false
                input "perDay${state.app}", 'number', title: 'Guideline value for time per day, this divides minutes per week into watering days. Default: 20', defaultValue: '20', required: false
        }
    }
}

private String setString(String type) {
    switch (type) {
        case 'zone':
            if (settings."zone${state.app}") return settings."zone${state.app}" else return 'Not Set'
            break
        case 'plant':
            if (settings."plant${state.app}") return settings."plant${state.app}" else return 'Not Set'
            break
        case 'option':
            if (settings."option${state.app}") return settings."option${state.app}" else return 'Not Set'
            break
        default:
            return '????'
    }
}

def plantSetPage() {
    dynamicPage(name: 'plantSetPage', title: "${settings["name${state.app}"]} Landscape Select") {
        section(''){
            paragraph image: 'http://www.plaidsystems.com/img/st_${state.app}.png',
                title: "${settings["name${state.app}"]}",
                "Current settings ${display("${state.app}")}"
            //input "plant${state.app}", "enum", title: "Landscape", multiple: false, required: false, submitOnChange: true, metadata: [values: ['Lawn', 'Garden', 'Flowers', 'Shrubs', 'Trees', 'Xeriscape', 'New Plants']]
        }
        section(''){
            paragraph image: 'http://www.plaidsystems.com/smartthings/st_lawn_200_r.png',
            title: 'Lawn',
            'Select Lawn for typical grass applications'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_garden_225_r.png',
            title: 'Garden',
            'Select Garden for vegetable gardens'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_flowers_225_r.png',
            title: 'Flowers',
            'Select Flowers for beds with smaller seasonal plants'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_shrubs_225_r.png',
            title: 'Shrubs',
            'Select Shrubs for beds with larger established plants'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_trees_225_r.png',
            title: 'Trees',
            'Select Trees for deep rooted areas without other plants'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_xeriscape_225_r.png',
            title: 'Xeriscape',
            'Reduces water for native or drought tolorent plants'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_newplants_225_r.png',
            title: 'New Plants',
            'Increases watering time per week and reduces automatic adjustments to help establish new plants. No weekly seasonal adjustment and moisture setpoint set to 40.'
        }
    }
}

def sprinklerSetPage(){
    dynamicPage(name: 'sprinklerSetPage', title: "${settings["name${state.app}"]} Sprinkler Select") {
        section(''){
            paragraph image: "http://www.plaidsystems.com/img/st_${state.app}.png",
            title: "${settings["name${state.app}"]}",
            "Current settings ${display("${state.app}")}"
            //input "zone${state.app}", "enum", title: "Sprinkler Type", multiple: false, required: false, defaultValue: 'Off', metadata: [values: ['Off', 'Spray', 'Rotor', 'Drip', 'Master Valve', 'Pump']]
            }
        section(''){
            paragraph image: 'http://www.plaidsystems.com/smartthings/st_spray_225_r.png',
            title: 'Spray',
            'Spray sprinkler heads spray a fan of water over the lawn. The water is applied evenly and can be turned on for a shorter duration of time.'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_rotor_225_r.png',
            title: 'Rotor',
            'Rotor sprinkler heads rotate, spraying a stream over the lawn.  Because they move back and forth across the lawn, they require a longer water period.'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_drip_225_r.png',
            title: 'Drip',
            'Drip lines or low flow emitters water slowely to minimize evaporation, because they are low flow, they require longer watering periods.'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_master_225_r.png',
            title: 'Master',
            'Master valves will open before watering begins.  Set the delay between master opening and watering in delay settings.'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_pump_225_r.png',
            title: 'Pump',
            'Attach a pump relay to this zone and the pump will turn on before watering begins.  Set the delay between pump start and watering in delay settings.'
        }
    }
}

def optionSetPage(){
    dynamicPage(name: 'optionSetPage', title: "${settings["name${state.app}"]} Options") {
        section(''){
            paragraph image: "http://www.plaidsystems.com/img/st_${state.app}.png",
            title: "${settings["name${state.app}"]}",
            "Current settings ${display("${state.app}")}"
            //input "option${state.app}", "enum", title: "Options", multiple: false, required: false, defaultValue: 'Cycle 2x', metadata: [values: ['Slope', 'Sand', 'Clay', 'No Cycle', 'Cycle 2x', 'Cycle 3x']]
        }
        section(''){
            paragraph image: 'http://www.plaidsystems.com/smartthings/st_slope_225_r.png',
            title: 'Slope',
            'Slope sets the sprinklers to cycle 3x, each with a short duration to minimize runoff'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_sand_225_r.png',
            title: 'Sand',
            'Sandy soil drains quickly and requires more frequent but shorter intervals of water'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_clay_225_r.png',
            title: 'Clay',
            'Clay sets the sprinklers to cycle 2x, each with a short duration to maximize absorption'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_cycle1x_225_r.png',
            title: 'No Cycle',
            'The sprinklers will run for 1 long duration'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_cycle2x_225_r.png',
            title: 'Cycle 2x',
            'Cycle 2x will break the water period up into 2 shorter cycles to help minimize runoff and maximize adsorption'

            paragraph image: 'http://www.plaidsystems.com/smartthings/st_cycle3x_225_r.png',
            title: 'Cycle 3x',
            'Cycle 3x will break the water period up into 3 shorter cycles to help minimize runoff and maximize adsorption'
        }
    }
}

def setPage(i){
    if (i) state.app = i
    return state.app
}

private String getaZoneSummary(int zone){
    if (!settings."zone${zone}" || (settings."zone${zone}" == 'Off')) return "${zone}: Off"

    String daysString = ''
    int tpw = initTPW(zone)
    int dpw = initDPW(zone)
    int runTime = calcRunTime(tpw, dpw)

    if ( !learn && (settings."sensor${zone}")) {
        daysString = 'if Moisture is low on: '
        dpw = daysAvailable()
    }
    if (days && (days.contains('Even') || days.contains('Odd'))) {
        if (dpw == 1) daysString = 'Every 8 days'
        if (dpw == 2) daysString = 'Every 4 days'
        if (dpw == 4) daysString = 'Every 2 days'
        if (days.contains('Even') && days.contains('Odd')) daysString = 'any day'
    }
    else {
        def int[] dpwMap = [0,0,0,0,0,0,0]
        dpwMap = getDPWDays(dpw)
        daysString += getRunDays(dpwMap)
    }
    return "${zone}: ${runTime} min, ${daysString}"
}

private String getZoneSummary(){
     String summary = ''
    if (learn) summary = 'Moisture Learning enabled' else summary = 'Moisture Learning disabled'

    int zone = 1
    createDPWMap()
    while(zone <= 16) {
          if (nozzle(zone) == 4) summary = "${summary}\n${zone}: ${settings."zone${zone}"}"
          else if ( (initDPW(zone) != 0) && zoneActive(zone.toString())) summary = "${summary}\n${getaZoneSummary(zone)}"
          zone++
    }
    if (summary) return summary else return zoneString()    //"Setup all 16 zones"
}

private String display(String i){
    //log.trace "display(${i})"
    String displayString = ''
    int tpw = initTPW(i.toInteger())
    int dpw = initDPW(i.toInteger())
    int runTime = calcRunTime(tpw, dpw)
    if (settings."zone${i}")     displayString += settings."zone${i}" + ' : '
    if (settings."plant${i}")     displayString += settings."plant${i}" + ' : '
    if (settings."option${i}")     displayString += settings."option${i}" + ' : '
    int j = i.toInteger()
    if (settings."sensor${i}") {
        displayString += settings."sensor${i}"
        displayString += "=${getDrySp(j)}% : "
    }
    if ((runTime != 0) && (dpw != 0)) displayString = "${displayString}${runTime} minutes, ${dpw} days per week"
    return displayString
}

private String getimage(String image){
    String imageStr = image
    if (image.isNumber()) {
        String zoneStr = settings."zone${image}"
        if (zoneStr) {
            if (zoneStr == 'Off')             return 'http://www.plaidsystems.com/smartthings/off2.png'
            if (zoneStr == 'Master Valve')     return 'http://www.plaidsystems.com/smartthings/master.png'
            if (zoneStr == 'Pump')             return 'http://www.plaidsystems.com/smartthings/pump.png'

            if (settings."plant${image}") imageStr = settings."plant${image}"        // default assume asking for the plant image
        }
    }
    // OK, lookup the requested image
    switch (imageStr) {
        case "null":
        case null:
            return 'http://www.plaidsystems.com/smartthings/off2.png'
        case 'Off':
            return 'http://www.plaidsystems.com/smartthings/off2.png'
        case 'Lawn':
            return 'http://www.plaidsystems.com/smartthings/st_lawn_200_r.png'
        case 'Garden':
            return 'http://www.plaidsystems.com/smartthings/st_garden_225_r.png'
        case 'Flowers':
            return 'http://www.plaidsystems.com/smartthings/st_flowers_225_r.png'
        case 'Shrubs':
            return 'http://www.plaidsystems.com/smartthings/st_shrubs_225_r.png'
        case 'Trees':
            return 'http://www.plaidsystems.com/smartthings/st_trees_225_r.png'
        case 'Xeriscape':
            return 'http://www.plaidsystems.com/smartthings/st_xeriscape_225_r.png'
        case 'New Plants':
            return 'http://www.plaidsystems.com/smartthings/st_newplants_225_r.png'
        case 'Spray':
            return 'http://www.plaidsystems.com/smartthings/st_spray_225_r.png'
        case 'Rotor':
            return 'http://www.plaidsystems.com/smartthings/st_rotor_225_r.png'
        case 'Drip':
            return 'http://www.plaidsystems.com/smartthings/st_drip_225_r.png'
        case 'Master Valve':
            return "http://www.plaidsystems.com/smartthings/st_master_225_r.png"
        case 'Pump':
            return 'http://www.plaidsystems.com/smartthings/st_pump_225_r.png'
        case 'Slope':
            return 'http://www.plaidsystems.com/smartthings/st_slope_225_r.png'
        case 'Sand':
            return 'http://www.plaidsystems.com/smartthings/st_sand_225_r.png'
        case 'Clay':
            return 'http://www.plaidsystems.com/smartthings/st_clay_225_r.png'
        case 'No Cycle':
            return 'http://www.plaidsystems.com/smartthings/st_cycle1x_225_r.png'
        case 'Cycle 2x':
            return 'http://www.plaidsystems.com/smartthings/st_cycle2x_225_r.png'
        case "Cycle 3x":
            return 'http://www.plaidsystems.com/smartthings/st_cycle3x_225_r.png'
        default:
            return 'http://www.plaidsystems.com/smartthings/off2.png'
    }
}

private String getname(String i) {
    if (settings."name${i}") return settings."name${i}" else return "Zone ${i}"
}

private String zipString() {    
    if (settings?.zipcode) return settings.zipcode
    if (location?.zipCode?.isNumber()) return "${location.zipCode}"
    if (location?.latitude && location?.longitude) return "${location.latitude.floatValue()},${location.longitude.floatValue()}"
    return "not set"
}

//app install
def installed() {
    state.dpwMap =                 [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    state.tpwMap =                 [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    state.Rain =                 [0,0,0,0,0,0,0]
    state.daycount =             [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    atomicState.run =             false                // must be atomic - used to recover from crashes
    state.pauseTime =             null
    atomicState.startTime =     null
    atomicState.finishTime =     null        // must be atomic - used to recover from crashes

    log.debug "Installed with settings: ${settings}"
    installSchedule()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    installSchedule()
}

def installSchedule(){
    if (!state.seasonAdj)             state.seasonAdj = 100.0
    if (!state.weekseasonAdj)         state.weekseasonAdj = 0
    if (state.daysAvailable != 0)     state.daysAvailable = 0    // force daysAvailable to be initialized by daysAvailable()
    state.daysAvailable =             daysAvailable()            // every time we save the schedule

    if (atomicState.run) {
        attemptRecovery()                                     // clean up if we crashed earlier
    }
    else {
        unsubscribe()                                        //added back in to reset manual subscription
        resetEverything()
    }
    subscribe(app, appTouch)                                // enable the "play" button for this schedule
    Random rand = new Random()
    long randomOffset = 0

    // always collect rainfall
    int randomSeconds = rand.nextInt(59)
    if (settings.isRain || settings.isSeason) schedule("${randomSeconds} 57 23 1/1 * ? *", getRainToday)        // capture today's rainfall just before midnight

    if (settings.switches && settings.startTime && settings.enable){

        randomOffset = rand.nextInt(60000) + 20000
        def checktime = timeToday(settings.startTime, location.timeZone).getTime() + randomOffset
        //log.debug "randomOffset ${randomOffset} checktime ${checktime}"
        schedule(checktime, preCheck)    //check weather & Days
        writeSettings()
        note('schedule', "${app.label}: Starts at ${startTimeString()}", 'i')
    }
    else {
        unschedule( preCheck )
        note('disable', "${app.label}: Automatic watering disabled or setup is incomplete", 'a')
    }
}

// Called to find and repair after crashes - called by installSchedule() and busy()
private boolean attemptRecovery() {
    if (!atomicState.run) {
        return false                                        // only clean up if we think we are still running
    }
    else {                                                    // Hmmm...seems we were running before...
        def csw = settings.switches.currentSwitch
        def cst = settings.switches.currentStatus
        switch (csw) {
            case 'on':                                        // looks like this schedule is running the controller at the moment
                if (!atomicState.startTime) {                 // cycleLoop cleared the startTime, but cycleOn() didn't set it
                    log.debug "${app.label}: crashed in cycleLoop(), cycleOn() never started, cst is ${cst} - resetting"
                    resetEverything()                        // reset and try again...it's probably not us running the controller, though
                    return false
                }
                // We have a startTime...
                if (!atomicState.finishTime) {                // started, but we don't think we're done yet..so it's probably us!
                    runIn(15, cycleOn)                        // goose the cycle, just in case
                    note('active', "${app.label}: schedule is apparently already running", 'i')
                    return true
                }

                // hmmm...switch is on and we think we're finished...probably somebody else is running...let busy figure it out
                resetEverything()
                return false
                break

            case 'off':                                     // switch is off - did we finish?
                if (atomicState.finishTime)    {                // off and finished, let's just reset things
                    resetEverything()
                    return false
                }

                if (switches.currentStatus != 'pause') {     // off and not paused - probably another schedule, let's clean up
                    resetEverything()
                    return false
                }

                // off and not finished, and paused, we apparently crashed while paused
                runIn(15, cycleOn)
                return true
                break

            case 'programOn':                    // died while manual program running?
            case 'programWait':                    // looks like died previously before we got started, let's try to clean things up
                resetEverything()
                if (atomicState.finishTime) atomicState.finishTime = null
                if ((cst == 'active') || atomicState.startTime) {    // if we announced we were in preCheck, or made it all the way to cycleOn before it crashed
                    settings.switches.programOff()                    // only if we think we actually started (cycleOn() started)
                    // probably kills manual cycles too, but we'll let that go for now
                }
                if (atomicState.startTime) atomicState.startTime = null
                note ('schedule', "Looks like ${app.label} crashed recently...cleaning up", c)
                return false
                break

            default:
                log.debug "attemptRecovery(): atomicState.run == true, and I've nothing left to do"
                return true
        }
    }
}

// reset everything to the initial (not running) state
private def resetEverything() {
    if (atomicState.run) atomicState.run = false        // we're not running the controller any more
    unsubAllBut()                                        // release manual, switches, sync, contacts & toggles

    // take care not to unschedule preCheck() or getRainToday()
    unschedule(cycleOn)
    unschedule(checkRunMap)
    unschedule(writeCycles)
    unschedule(subOff)

    if (settings.enableManual) subscribe(settings.switches, 'switch.programOn', manualStart)
}

// unsubscribe from ALL events EXCEPT app.touch
private def unsubAllBut() {
    unsubscribe(settings.switches)
    unsubWaterStoppers()
    if (settings.sync) unsubscribe(settings.sync)

}

// enable the "Play" button in SmartApp list
def appTouch(evt) {

    log.debug "appTouch(): atomicState.run = ${atomicState.run}"

    runIn(2, preCheck)                        // run it off a schedule, so we can see how long it takes in the app.state
}

// true if one of the stoppers is in Stop state
private boolean isWaterStopped() {
    if (settings.contacts && settings.contacts.currentContact.contains(settings.contactStop)) return true

    if (settings.toggles && settings.toggles.currentSwitch.contains(settings.toggleStop)) return true

    return false
}

// watch for water stoppers
private def subWaterStop() {
    if (settings.contacts) {
        unsubscribe(settings.contacts)
        subscribe(settings.contacts, "contact.${settings.contactStop}", waterStop)
    }
    if (settings.toggles) {
        unsubscribe(settings.toggles)
        subscribe(settings.toggles, "switch.${settings.toggleStop}", waterStop)
    }
}

// watch for water starters
private def subWaterStart() {
    if (settings.contacts) {
        unsubscribe(settings.contacts)
        def cond = (settings.contactStop == 'open') ? 'closed' : 'open'
        subscribe(settings.contacts, "contact.${cond}", waterStart)
    }
    if (settings.toggles) {
        unsubscribe(settings.toggles)
        def cond = (settings.toggleStop == 'on') ? 'off' : 'on'
        subscribe(settings.toggles, "switch.${cond}", waterStart)
    }
}

// stop watching water stoppers and starters
private def unsubWaterStoppers() {
    if (settings.contacts)     unsubscribe(settings.contacts)
    if (settings.toggles)     unsubscribe(settings.toggles)
}

// which of the stoppers are in stop mode?
private String getWaterStopList() {
    String deviceList = ''
    int i = 1
    if (settings.contacts) {
        settings.contacts.each {
            if (it.currentContact == settings.contactStop) {
                if (i > 1) deviceList += ', '
                deviceList = "${deviceList}${it.displayName} is ${settings.contactStop}"
                i++
            }
        }
    }
    if (settings.toggles) {
        settings.toggles.each {
            if (it.currentSwitch == settings.toggleStop) {
                if (i > 1) deviceList += ', '
                deviceList = "${deviceList}${it.displayName} is ${settings.toggleStop}"
                i++
            }
        }
    }
    return deviceList
}

//write initial zone settings to device at install/update
def writeSettings(){
    if (!state.tpwMap)             state.tpwMap = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    if (!state.dpwMap)             state.dpwMap = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    if (state.setMoisture)         state.setMoisture = null                            // not using any more
    if (!state.seasonAdj)         state.seasonAdj = 100.0
    if (!state.weekseasonAdj)     state.weekseasonAdj = 0
    setSeason()
}

//get day of week integer
int getWeekDay(day)
{
    def weekdays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
    def mapDay = [Monday:1, Tuesday:2, Wednesday:3, Thursday:4, Friday:5, Saturday:6, Sunday:7]
    if(day && weekdays.contains(day)) {
        return mapDay.get(day).toInteger()
    }
    def today = new Date().format('EEEE', location.timeZone)
    return mapDay.get(today).toInteger()
}

// Get string of run days from dpwMap
private String getRunDays(day1,day2,day3,day4,day5,day6,day7)
{
    String str = ''
    if(day1) str += 'M'
    if(day2) str += 'T'
    if(day3) str += 'W'
    if(day4) str += 'Th'
    if(day5) str += 'F'
    if(day6) str += 'Sa'
    if(day7) str += 'Su'
    if(str == '') str = '0 Days/week'
    return str
}

//start manual schedule
def manualStart(evt){
    boolean running = attemptRecovery()        // clean up if prior run crashed
	//isWeather()//use for testing
    if (settings.enableManual && !running && (settings.switches.currentStatus != 'pause')){
        if (settings.sync && ( (settings.sync.currentSwitch != 'off') || settings.sync.currentStatus == 'pause') ) {
            note('skipping', "${app.label}: Manual run aborted, ${settings.sync.displayName} appears to be busy", 'a')
            }
        else {
            def runNowMap = []
            runNowMap = cycleLoop(0)

            if (runNowMap) {
                atomicState.run = true
                settings.switches.programWait()
                subscribe(settings.switches, 'switch.off', cycleOff)

                runIn(60, cycleOn)               // start water program

                                                // note that manual DOES abide by waterStoppers (if configured)
                String newString = ''
                int tt = state.totalTime
                if (tt) {
                    int hours = tt / 60            // DON'T Math.round this one
                    int mins = tt - (hours * 60)
                    String hourString = ''
                    String s = ''
                    if (hours > 1) s = 's'
                    if (hours > 0) hourString = "${hours} hour${s} & "
                    s = 's'
                    if (mins == 1) s = ''
                    newString = "run time: ${hourString}${mins} minute${s}:\n"
                }

                note('active', "${app.label}: Manual run, watering in 1 minute: ${newString}${runNowMap}", 'd')
            }
            else note('skipping', "${app.label}: Manual run failed, check configuration", 'a')
        }
    }
    else note('skipping', "${app.label}: Manual run aborted, ${settings.switches.displayName} appears to be busy", 'a')
}

//true if another schedule is running
boolean busy(){
    // Check if we are already running, crashed or somebody changed the schedule time while this schedule is running
    if (atomicState.run){
        if (!attemptRecovery()) {        // recovery will clean out any prior crashes and correct state of atomicState.run
            return false                // (atomicState.run = false)
        }
        else {
            // don't change the current status, in case the currently running schedule is in off/paused mode
            note(settings.switches.currentStatus, "${app.label}: Already running, skipping additional start", 'i')
            return true
        }
    }
    // Not already running...

    // Moved from cycleOn() - don't even start pre-check until the other controller completes its cycle
    if (settings.sync) {
        if ((settings.sync.currentSwitch != 'off') || settings.sync.currentStatus == 'pause') {
            subscribe(settings.sync, 'switch.off', syncOn)

            note('delayed', "${app.label}: Waiting for ${settings.sync.displayName} to complete before starting", 'c')
            return true
        }
    }

    // Check that the controller isn't paused while running some other schedule
    def csw = settings.switches.currentSwitch
    def cst = settings.switches.currentStatus

    if ((csw == 'off') && (cst != 'pause')) {                // off && !paused: controller is NOT in use
        log.debug "switches ${csw}, status ${cst} (1st)"
        resetEverything()                                    // get back to the start state
        return false
    }

    if (isDay()) {                                            // Yup, we need to run today, so wait for the other schedule to finish
        log.debug "switches ${csw}, status ${cst} (3rd)"
        resetEverything()
        subscribe(settings.switches, 'switch.off', busyOff)
        note('delayed', "${app.label}: Waiting for currently running schedule to complete before starting", 'c')
           return true
    }

    // Somthing is running, but we don't need to run today anyway - don't need to do busyOff()
    // (Probably should never get here, because preCheck() should check isDay() before calling busy()
    log.debug "Another schedule is running, but ${app.label} is not scheduled for today anyway"
    return true
}

def busyOff(evt){
    def cst = settings.switches.currentStatus
    if ((settings.switches.currentSwitch == 'off') && (cst != 'pause')) { // double check that prior schedule is done
        unsubscribe(switches)                            // we don't want any more button pushes until preCheck runs
        Random rand = new Random()                         // just in case there are multiple schedules waiting on the same controller
        int randomSeconds = rand.nextInt(120) + 15
        runIn(randomSeconds, preCheck)                    // no message so we don't clog the system
        note('active', "${app.label}: ${settings.switches} finished, starting in ${randomSeconds} seconds", 'i')
    }
}

//run check every day
def preCheck() {

    if (!isDay()) {
        log.debug "preCheck() Skipping: ${app.label} is not scheduled for today"                    // silent - no note
        //if (!atomicState.run && enableManual) subscribe(switches, 'switch.programOn', manualStart)    // only if we aren't running already
        return
    }

    if (!busy()) {
        atomicState.run = true                         // set true before doing anything, atomic in case we crash (busy() set it false if !busy)
        settings.switches.programWait()                // take over the controller so other schedules don't mess with us
        runIn(45, checkRunMap)                        // schedule checkRunMap() before doing weather check, gives isWeather 45s to complete
                                                    // because that seems to be a little more than the max that the ST platform allows
        unsubAllBut()                                // unsubscribe to everything except appTouch()
        subscribe(settings.switches, 'switch.off', cycleOff)    // and start setting up for today's cycle
        def start = now()
        note('active', "${app.label}: Starting...", 'd')  //
        def end = now()
        log.debug "preCheck note active ${end - start}ms"

           if (isWeather()) {                            // set adjustments and check if we shold skip because of rain
               resetEverything()                        // if so, clean up our subscriptions
               switches.programOff()                    // and release the controller
        }
        else {
            log.debug 'preCheck(): running checkRunMap in 2 seconds'    //COOL! We finished before timing out, and we're supposed to water today
            runIn(2, checkRunMap)    // jack the schedule so it runs sooner!
        }
    }
}

//start water program
def cycleOn(){
    if (atomicState.run) {                            // block if manually stopped during precheck which goes to cycleOff

        if (!isWaterStopped()) {                    // make sure ALL the contacts and toggles aren't paused
            // All clear, let's start running!
            subscribe(settings.switches, 'switch.off', cycleOff)
            subWaterStop()                            // subscribe to all the pause contacts and toggles
            resume()

            // send the notification AFTER we start the controller (in case note() causes us to run over our execution time limit)
            String newString = "${app.label}: Starting..."
            if (!atomicState.startTime) {
                atomicState.startTime = now()                // if we haven't already started
                if (atomicState.startTime) atomicState.finishTime = null        // so recovery in busy() knows we didn't finish
                if (state.pauseTime) state.pauseTime = null
                if (state.totalTime) {
                    String finishTime = new Date(now() + (60000 * state.totalTime).toLong()).format('EE @ h:mm a', location.timeZone)
                    newString = "${app.label}: Starting - ETC: ${finishTime}"
                }
            }
            else if (state.pauseTime) {        // resuming after a pause

                def elapsedTime = Math.round((now() - state.pauseTime) / 60000)    // convert ms to minutes
                int tt = state.totalTime + elapsedTime + 1
                state.totalTime = tt        // keep track of the pauses, and the 1 minute delay above
                String finishTime = new Date(atomicState.startTime + (60000 * tt).toLong()).format('EE @ h:mm a', location.timeZone)
                state.pauseTime = null
                newString = "${app.label}: Resuming - New ETC: ${finishTime}"
            }
            note('active', newString, 'd')
        }
        else {
            // Ready to run, but one of the control contacts is still open, so we wait
            subWaterStart()                                        // one of them is paused, let's wait until the are all clear!
            note('pause', "${app.label}: Watering paused, ${getWaterStopList()}", 'c')
        }
    }
}

//when switch reports off, watering program is finished
def cycleOff(evt){

    if (atomicState.run) {
        def ft = new Date()
        atomicState.finishTime = ft                                    // this is important to reset the schedule after failures in busy()
        String finishTime = ft.format('h:mm a', location.timeZone)
        note('finished', "${app.label}: Finished watering at ${finishTime}", 'd')
    }
    else {
        log.debug "${settings.switches} turned off"        // is this a manual off? perhaps we should send a note?
    }
    resetEverything()                            // all done here, back to starting state
}

//run check each day at scheduled time
def checkRunMap(){

    //check if isWeather returned true or false before checking
    if (atomicState.run) {

        //get & set watering times for today
        def runNowMap = []
        runNowMap = cycleLoop(1)        // build the map

        if (runNowMap) {
            runIn(60, cycleOn)                                            // start water
            subscribe(settings.switches, 'switch.off', cycleOff)        // allow manual off before cycleOn() starts
            if (atomicState.startTime) atomicState.startTime = null        // these were already cleared in cycleLoop() above
            if (state.pauseTime) state.pauseTime = null                    // ditto
            // leave atomicState.finishTime alone so that recovery in busy() knows we never started if cycleOn() doesn't clear it

            String newString = ''
            int tt = state.totalTime
            if (tt) {
                int hours = tt / 60            // DON'T Math.round this one
                int mins = tt - (hours * 60)
                String hourString = ''
                String s = ''
                if (hours > 1) s = 's'
                if (hours > 0) hourString = "${hours} hour${s} & "
                s = 's'
                if (mins == 1) s = ''
                newString = "run time: ${hourString}${mins} minute${s}:\n"
            }
            note('active', "${app.label}: Watering in 1 minute, ${newString}${runNowMap}", 'd')
        }
        else {
            unsubscribe(settings.switches)
            unsubWaterStoppers()
            switches.programOff()
            if (enableManual) subscribe(settings.switches, 'switch.programOn', manualStart)
            note('skipping', "${app.label}: No watering today", 'd')
            if (atomicState.run) atomicState.run = false         // do this last, so that the above note gets sent to the controller
        }
    }
    else {
        log.debug 'checkRunMap(): atomicState.run = false'      // isWeather cancelled us out before we got started
    }
}

//get todays schedule
def cycleLoop(int i)
{
    boolean isDebug = false
    if (isDebug) log.debug "cycleLoop(${i})"

    int zone = 1
    int dpw = 0
    int tpw = 0
    int cyc = 0
    int rtime = 0
    def timeMap = [:]
    def pumpMap = ""
    def runNowMap = ""
    String soilString = ''
    int totalCycles = 0
    int totalTime = 0
    if (atomicState.startTime) atomicState.startTime = null                    // haven't started yet

    while(zone <= 16)
    {
        rtime = 0
        def setZ = settings."zone${zone}"
        if ((setZ && (setZ != 'Off')) && (nozzle(zone) != 4) && zoneActive(zone.toString())) {

              // First check if we run this zone today, use either dpwMap or even/odd date
              dpw = getDPW(zone)
              int runToday = 0
              // if manual, or every day allowed, or zone uses a sensor, then we assume we can today
              //  - preCheck() has already verified that today isDay()
              if ((i == 0) || /*(state.daysAvailable == 7) ||*/ (settings."sensor${zone}")) {
                  runToday = 1
              }
              else {

                  dpw = getDPW(zone)                                    // figure out if we need to run (if we don't already know we do)
                  if (settings.days && (settings.days.contains('Even') || settings.days.contains('Odd'))) {
                    def daynum = new Date().format('dd', location.timeZone)
                    int dayint = Integer.parseInt(daynum)
                    if (settings.days.contains('Odd') && (((dayint +1) % Math.round(31 / (dpw * 4))) == 0)) runToday = 1
                      else if (settings.days.contains('Even') && ((dayint % Math.round(31 / (dpw * 4))) == 0)) runToday = 1
                  }
                  else {
                    int weekDay = getWeekDay()-1
                    def dpwMap = getDPWDays(dpw)
                    runToday = dpwMap[weekDay]  //1 or 0
                    if (isDebug) log.debug "Zone: ${zone} dpw: ${dpw} weekDay: ${weekDay} dpwMap: ${dpwMap} runToday: ${runToday}"

                  }
              }

            // OK, we're supposed to run (or at least adjust the sensors)
              if (runToday == 1)
              {
                def soil
                if (i == 0) soil = moisture(0)     // manual
                else soil = moisture(zone)        // moisture check
                  soilString = "${soilString}${soil[1]}"

                // Run this zone if soil moisture needed
                if ( soil[0] == 1 )
                {
                    cyc = cycles(zone)
                    tpw = getTPW(zone)
                    dpw = getDPW(zone)                    // moisture() may have changed DPW

                    rtime = calcRunTime(tpw, dpw)
                    //daily weather adjust if no sensor
                    if(settings.isSeason && (!settings.learn || !settings."sensor${zone}")) {


                        rtime = Math.round(((rtime / cyc) * (state.seasonAdj / 100.0)) + 0.4)
                    }
                    else {
                        rtime = Math.round((rtime / cyc) + 0.4)    // let moisture handle the seasonAdjust for Adaptive (learn) zones
                    }
                    totalCycles += cyc
                    totalTime += (rtime * cyc)
                    runNowMap += "${settings."name${zone}"}: ${cyc} x ${rtime} min\n"
                    if (isDebug) log.debug "Zone ${zone} Map: ${cyc} x ${rtime} min - totalTime: ${totalTime}"
                }
            }
        }
        if (nozzle(zone) == 4) pumpMap += "${settings."name${zone}"}: ${settings."zone${zone}"} on\n"
        timeMap."${zone+1}" = "${rtime}"
        zone++
    }

    if (soilString) {
        String seasonStr = ''
        String plus = ''
        float sa = state.seasonAdj
        if (settings.isSeason && (sa != 100.0) && (sa != 0.0)) {
            float sadj = sa - 100.0
            if (sadj > 0.0) plus = '+'                                            //display once in cycleLoop()
            int iadj = Math.round(sadj)
            if (iadj != 0) seasonStr = "Adjusting ${plus}${iadj}% for weather forecast\n"
        }
        note('moisture', "${app.label} Sensor status:\n${seasonStr}${soilString}" /* + seasonStr + soilString */,'m')
    }

    if (!runNowMap) {
        return runNowMap            // nothing to run today
    }

    //send settings to Spruce Controller
    switches.settingsMap(timeMap,4002)
    runIn(30, writeCycles)

    // meanwhile, calculate our total run time
    int pDelay = 0
    if (settings.pumpDelay && settings.pumpDelay.isNumber()) pDelay = settings.pumpDelay.toInteger()
    totalTime += Math.round(((pDelay * (totalCycles-1)) / 60.0))  // add in the pump startup and inter-zone delays
    state.totalTime = totalTime

    if (state.pauseTime) state.pauseTime = null                    // and we haven't paused yet
                                                                // but let cycleOn() reset finishTime
    return (runNowMap + pumpMap)
}

//send cycle settings
def writeCycles(){
    //log.trace "writeCycles()"
    def cyclesMap = [:]
    //add pumpdelay @ 1
    cyclesMap."1" = pumpDelayString()
    int zone = 1
    int cycle = 0
    while(zone <= 17)
    {
        if(nozzle(zone) == 4) cycle = 4
        else cycle = cycles(zone)
        //offset by 1, due to pumpdelay @ 1
        cyclesMap."${zone+1}" = "${cycle}"
        zone++
    }
    switches.settingsMap(cyclesMap, 4001)
}

def resume(){
    log.debug 'resume()'
    settings.switches.zon()
}

def syncOn(evt){
    // double check that the switch is actually finished and not just paused
    if ((settings.sync.currentSwitch == 'off') && (settings.sync.currentStatus != 'pause')) {
        resetEverything()                                // back to our known state
        Random rand = new Random()                         // just in case there are multiple schedules waiting on the same controller
        int randomSeconds = rand.nextInt(120) + 15
        runIn(randomSeconds, preCheck)                    // no message so we don't clog the system
        note('schedule', "${app.label}: ${settings.sync} finished, starting in ${randomSeconds} seconds", 'c')
    } // else, it is just pausing...keep waiting for the next "off"
}

// handle start of pause session
def waterStop(evt){
    log.debug "waterStop: ${evt.displayName}"

    unschedule(cycleOn)            // in case we got stopped again before cycleOn starts from the restart
    unsubscribe(settings.switches)
    subWaterStart()

    if (!state.pauseTime) {            // only need to do this for the first event if multiple contacts
        state.pauseTime = now()

        String cond = evt.value
        switch (cond) {
            case 'open':
                cond = 'opened'
                break
            case 'on':
                cond = 'switched on'
                break
            case 'off':
                cond = 'switched off'
                break
            //case 'closed':
            //    cond = 'closed'
            //    break
            case null:
                cond = '????'
                break
            default:
                break
        }
        note('pause', "${app.label}: Watering paused - ${evt.displayName} ${cond}", 'c') // set to Paused
    }
    if (settings.switches.currentSwitch != 'off') {
        runIn(30, subOff)
        settings.switches.off()                                // stop the water
    }
    else
        subscribe(settings.switches, 'switch.off', cycleOff)
}

// This is a hack to work around the delay in response from the controller to the above programOff command...
// We frequently see the off notification coming a long time after the command is issued, so we try to catch that so that
// we don't prematurely exit the cycle.
def subOff() {
    subscribe(settings.switches, 'switch.off', offPauseCheck)
}

def offPauseCheck( evt ) {
    unsubscribe(settings.switches)
    subscribe(settings.switches, 'switch.off', cycleOff)
    if (/*(switches.currentSwitch != 'off') && */ (settings.switches.currentStatus != 'pause')) { // eat the first off while paused
        cycleOff(evt)
    }
}

// handle end of pause session
def waterStart(evt){
    if (!isWaterStopped()){                     // only if ALL of the selected contacts are not open
        def cDelay = 10
        if (settings.contactDelay > 10) cDelay = settings.contactDelay
        runIn(cDelay, cycleOn)

        unsubscribe(settings.switches)
        subWaterStop()                            // allow stopping again while we wait for cycleOn to start

        log.debug "waterStart(): enabling device is ${evt.device} ${evt.value}"

        String cond = evt.value
        switch (cond) {
            case 'open':
                cond = 'opened'
                break
            case 'on':
                cond = 'switched on'
                break
            case 'off':
                cond = 'switched off'
                break
            //case 'closed':
            //    cond = 'closed'
            //    break
            case null:
                cond = '????'
                break
            default:
                break
        }
        // let cycleOn() change the status to Active - keep us paused until then

        note('pause', "${app.label}: ${evt.displayName} ${cond}, watering in ${cDelay} seconds", 'c')
    }
    else {
        log.debug "waterStart(): one down - ${evt.displayName}"
    }
}

//Initialize Days per week, based on TPW, perDay and daysAvailable settings
int initDPW(int zone){
    //log.debug "initDPW(${zone})"
    if(!state.dpwMap) state.dpwMap = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]

    int tpw = getTPW(zone)        // was getTPW -does not update times in scheduler without initTPW
    int dpw = 0

    if(tpw > 0) {
        float perDay = 20.0
        if(settings."perDay${zone}") perDay = settings."perDay${zone}".toFloat()

        dpw = Math.round(tpw.toFloat() / perDay)
        if(dpw <= 1) dpw = 1
        // 3 days per week not allowed for even or odd day selection
        if(dpw == 3 && days && (days.contains('Even') || days.contains('Odd')) && !(days.contains('Even') && days.contains('Odd')))
            if((tpw.toFloat() / perDay) < 3.0) dpw = 2 else dpw = 4
        int daycheck = daysAvailable()                        // initialize & optimize daysAvailable
        if (daycheck < dpw) dpw = daycheck
    }
    state.dpwMap[zone-1] = dpw
    return dpw
}

// Get current days per week value, calls init if not defined
int getDPW(int zone) {
    if (state.dpwMap) return state.dpwMap[zone-1] else return initDPW(zone)
}

//Initialize Time per Week
int initTPW(int zone) {
    //log.trace "initTPW(${zone})"
    if (!state.tpwMap) state.tpwMap = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]

    int n = nozzle(zone)
    def zn = settings."zone${zone}"
    if (!zn || (zn == 'Off') || (n == 0) || (n == 4) || (plant(zone) == 0) || !zoneActive(zone.toString())) return 0

    // apply gain adjustment
    float gainAdjust = 100.0
    if (settings.gain && settings.gain != 0) gainAdjust += settings.gain

    // apply seasonal adjustment if enabled and not set to new plants
    float seasonAdjust = 100.0
    def wsa = state.weekseasonAdj
    if (wsa && isSeason && (settings."plant${zone}" != 'New Plants')) seasonAdjust = wsa

    int tpw = 0
    // Use learned, previous tpw if it is available
    if ( settings."sensor${zone}" ) {
        seasonAdjust = 100.0             // no weekly seasonAdjust if this zone uses a sensor
        if(state.tpwMap && settings.learn) tpw = state.tpwMap[zone-1]
    }

    // set user-specified minimum time with seasonal adjust
    int minWeek = 0
    def mw = settings."minWeek${zone}"
    if (mw) minWeek = mw.toInteger()
    if (minWeek != 0) {
        tpw = Math.round(minWeek * (seasonAdjust / 100.0))
    }
    else if (!tpw || (tpw == 0)) { // use calculated tpw
        tpw = Math.round((plant(zone) * nozzle(zone) * (gainAdjust / 100.0) * (seasonAdjust / 100.0)))
    }
    state.tpwMap[zone-1] = tpw
    return tpw
}

// Get the current time per week, calls init if not defined
int getTPW(int zone)
{
    if (state.tpwMap) return state.tpwMap[zone-1] else return initTPW(zone)
}

// Calculate daily run time based on tpw and dpw
int calcRunTime(int tpw, int dpw)
{
    int duration = 0
    if ((tpw > 0) && (dpw > 0)) duration = Math.round(tpw.toFloat() / dpw.toFloat())
    return duration
}

// Check the moisture level of a zone returning dry (1) or wet (0) and adjust tpw if overly dry/wet
def moisture(int i)
{
    boolean isDebug = false
    if (isDebug) log.debug "moisture(${i})"

    def endMsecs = 0
    // No Sensor on this zone or manual start skips moisture checking altogether
    if ((i == 0) || !settings."sensor${i}") {
        return [1,'']
    }

    // Ensure that the sensor has reported within last 48 hours
    int spHum = getDrySp(i)
    int hours = 48
    def yesterday = new Date(now() - (/* 1000 * 60 * 60 */ 3600000 * hours).toLong())
    float latestHum = settings."sensor${i}".latestValue('humidity').toFloat()    // state = 29, value = 29.13
    def lastHumDate = settings."sensor${i}".latestState('humidity').date
    if (lastHumDate < yesterday) {
        note('warning', "${app.label}: Please check sensor ${settings."sensor${i}"}, no humidity reports in the last ${hours} hours", 'a')

        if (latestHum < spHum)
            latestHum = spHum - 1.0             // amke sure we water and do seasonal adjustments, but not tpw adjustments
        else
            latestHum = spHum + 0.99            // make sure we don't water, do seasonal adjustments, but not tpw adjustments
    }

    if (!settings.learn)
    {
        // in Delay mode, only looks at target moisture level, doesn't try to adjust tpw
        // (Temporary) seasonal adjustment WILL be applied in cycleLoop(), as if we didn't have a sensor
        if (latestHum <= spHum.toFloat()) {
           //dry soil
               return [1,"${settings."name${i}"}, Watering: ${settings."sensor${i}"} reads ${latestHum}%, SP is ${spHum}%\n"]
        }
        else {
            //wet soil
               return [0,"${settings."name${i}"}, Skipping: ${settings."sensor${i}"} reads ${latestHum}%, SP is ${spHum}%\n"]
        }
    }

    //in Adaptive mode
    int tpw = getTPW(i)
    int dpw = getDPW(i)
    int cpd = cycles(i)




    if (isDebug) log.debug "moisture(${i}): tpw: ${tpw}, dpw: ${dpw}, cycles: ${cpd} (before adjustment)"

    float diffHum = 0.0
    if (latestHum > 0.0) diffHum = (spHum - latestHum) / 100.0
    else {
        diffHum = 0.02 // Safety valve in case sensor is reporting 0% humidity (e.g., somebody pulled it out of the ground or flower pot)
        note('warning', "${app.label}: Please check sensor ${settings."sensor${i}"}, it is currently reading 0%", 'a')
    }

    int daysA = state.daysAvailable
    int minimum = cpd * dpw                    // minimum of 1 minute per scheduled days per week (note - can be 1*1=1)
    if (minimum < daysA) minimum = daysA    // but at least 1 minute per available day
    int tpwAdjust = 0

    if (diffHum > 0.01) {                                 // only adjust tpw if more than 1% of target SP
          tpwAdjust = Math.round(((tpw * diffHum) + 0.5) * dpw * cpd)    // Compute adjustment as a function of the current tpw
        float adjFactor = 2.0 / daysA                    // Limit adjustments to 200% per week - spread over available days
          if (tpwAdjust > (tpw * adjFactor)) tpwAdjust = Math.round((tpw * adjFactor) + 0.5)         // limit fast rise
        if (tpwAdjust < minimum) tpwAdjust = minimum    // but we need to move at least 1 minute per cycle per day to actually increase the watering time
    } else if (diffHum < -0.01) {
        if (diffHum < -0.05) diffHum = -0.05            // try not to over-compensate for a heavy rainstorm...
        tpwAdjust = Math.round(((tpw * diffHum) - 0.5) * dpw * cpd)
        float adjFactor = -0.6667 / daysA                // Limit adjustments to 66% per week
        if (tpwAdjust < (tpw * adjFactor)) tpwAdjust = Math.round((tpw * adjFactor) - 0.5)    // limit slow decay
        if (tpwAdjust > (-1 * minimum)) tpwAdjust = -1 * minimum // but we need to move at least 1 minute per cycle per day to actually increase the watering time
    }

    int seasonAdjust = 0
    if (isSeason) {
        float sa = state.seasonAdj
        if ((sa != 100.0) && (sa != 0.0)) {
            float sadj = sa - 100.0
            if (sa > 0.0)
                seasonAdjust = Math.round(((sadj / 100.0) * tpw) + 0.5)
            else
                seasonAdjust = Math.round(((sadj / 100.0) * tpw) - 0.5)
        }
    }
     if (isDebug) log.debug "moisture(${i}): diffHum: ${diffHum}, tpwAdjust: ${tpwAdjust} seasonAdjust: ${seasonAdjust}"

     // Now, adjust the tpw.
     // With seasonal adjustments enabled, tpw can go up or down independent of the difference in the sensor vs SP
    int newTPW = tpw + tpwAdjust + seasonAdjust

    int perDay = 20
    def perD = settings."perDay${i}"
    if (perD) perDay = perD.toInteger()
    if (perDay == 0) perDay = daysA * cpd                // at least 1 minute per cycle per available day
      if (newTPW < perDay) newTPW = perDay                // make sure we have always have enough for 1 day of minimum water

    int adjusted = 0
    if ((tpwAdjust + seasonAdjust) > 0) {                            // needs more water
           int maxTPW = daysA * 120    // arbitrary maximum of 2 hours per available watering day per week
           if (newTPW > maxTPW) newTPW = maxTPW    // initDPW() below may spread this across more days
           if (newTPW > (maxTPW * 0.75)) note('warning', "${app.label}: Please check ${settings["sensor${i}"]}, ${settings."name${i}"} time per week seems high: ${newTPW} mins/week",'a')
         if (state.tpwMap[i-1] != newTPW) {    // are we changing the tpw?
            state.tpwMap[i-1] = newTPW
            dpw = initDPW(i)                            // need to recalculate days per week since tpw changed - initDPW() stores the value into dpwMap
            adjusted = newTPW - tpw     // so that the adjustment note is accurate
         }
    }
    else if ((tpwAdjust + seasonAdjust) < 0) {                         // Needs less water
        // Find the minimum tpw
        minimum = cpd * daysA                                        // at least 1 minute per cycle per available day
        int minLimit = 0
        def minL = settings."minWeek${i}"
        if (minL) minLimit = minL.toInteger()                        // unless otherwise specified in configuration
        if (minLimit > 0) {
            if (newTPW < minLimit) newTPW = minLimit                // use configured minutes per week as the minimum
        } else if (newTPW < minimum) {
            newTPW = minimum                                        // else at least 1 minute per cycle per available day
            note('warning', "${app.label}: Please check ${settings."sensor${i}"}, ${settings."name${i}"} time per week is very low: ${newTPW} mins/week",'a')
        }
        if (state.tpwMap[i-1] != newTPW) {    // are we changing the tpw?
            state.tpwMap[i-1] = newTPW        // store the new tpw
            dpw = initDPW(i)                // may need to reclac days per week - initDPW() now stores the value into state.dpwMap - avoid doing that twice
            adjusted = newTPW - tpw     // so that the adjustment note is accurate
        }
    }
    // else no adjustments, or adjustments cancelled each other out.

    String moistureSum = ''
    String adjStr = ''
    String plus = ''
    if (adjusted > 0) plus = '+'
    if (adjusted != 0) adjStr = ", ${plus}${adjusted} min"
    if (Math.abs(adjusted) > 1) adjStr = "${adjStr}s"
    if (diffHum >= 0.0) {                 // water only if ground is drier than SP
        moistureSum = "> ${settings."name${i}"}, Water: ${settings."sensor${i}"} @ ${latestHum}% (${spHum}%)${adjStr} (${newTPW} min/wk)\n"
        return [1, moistureSum]
    }
    else {                             // not watering
        moistureSum = "> ${settings."name${i}"}, Skip: ${settings."sensor${i}"} @ ${latestHum}% (${spHum}%)${adjStr} (${newTPW} min/wk)\n"
        return [0, moistureSum]
    }
    return [0, moistureSum]
}

//get moisture SP
int getDrySp(int i){
    if (settings."sensorSp${i}") return settings."sensorSp${i}".toInteger() // configured SP


    if (settings."plant${i}" == 'New Plants') return 40                        // New Plants get special care


    switch (settings."option${i}") {                                        // else, defaults based off of soil type
        case 'Sand':
            return 22
        case 'Clay':
            return 38
        default:
            return 28
    }
}

//notifications to device, pushed if requested
def note(String statStr, String msg, String msgType) {

    // send to debug first (near-zero cost)
    log.debug "${statStr}: ${msg}"

    // notify user second (small cost)
    boolean notifyController = true
    if(settings.notify || settings.logAll) {
        String spruceMsg = "Spruce ${msg}"
        switch(msgType) {
            case 'd':
                  if (settings.notify && settings.notify.contains('Daily')) {        // always log the daily events to the controller
                      sendIt(spruceMsg)
                  }
                  else if (settings.logAll) {
                      sendNotificationEvent(spruceMsg)
                  }
                  break
              case 'c':
                  if (settings.notify && settings.notify.contains('Delays')) {
                      sendIt(spruceMsg)
                  }
                  else if (settings.logAll) {
                      sendNotificationEvent(spruceMsg)
                  }
                  break
              case 'i':
                  if (settings.notify && settings.notify.contains('Events')) {
                      sendIt(spruceMsg)
                      //notifyController = false                    // no need to notify controller unless we don't notify the user
                  }
                  else if (settings.logAll) {
                      sendNotificationEvent(spruceMsg)
                  }
                  break
              case 'f':
                  notifyController = false                        // no need to notify the controller, ever
                if (settings.notify && settings.notify.contains('Weather')) {
                      sendIt(spruceMsg)
                  }
                  else if (settings.logAll) {
                      sendNotificationEvent(spruceMsg)
                  }
                  break
              case 'a':
                  notifyController = false                        // no need to notify the controller, ever
                  if (settings.notify && settings.notify.contains('Warnings')) {
                      sendIt(spruceMsg)
                  } else
                      sendNotificationEvent(spruceMsg)                    // Special case - make sure this goes into the Hello Home log, if not notifying
                  break
              case 'm':
                  if (settings.notify && settings.notify.contains('Moisture')) {
                      sendIt(spruceMsg)
                      //notifyController = false                    // no need to notify controller unless we don't notify the user
                  }
                  else if (settings.logAll) {
                      sendNotificationEvent(spruceMsg)
                  }
                  break
              default:
                  break
          }
    }
    // finally, send to controller DTH, to change the state and to log important stuff in the event log
    if (notifyController) {        // do we really need to send these to the controller?
        // only send status updates to the controller if WE are running, or nobody else is
        if (atomicState.run || ((settings.switches.currentSwitch == 'off') && (settings.switches.currentStatus != 'pause'))) {
            settings.switches.notify(statStr, msg)

        }
        else { // we aren't running, so we don't want to change the status of the controller
            // send the event using the current status of the switch, so we don't change it
            //log.debug "note - direct sendEvent()"
            settings.switches.notify(settings.switches.currentStatus, msg)

          }
    }
}

def sendIt(String msg) {
    if (location.contactBookEnabled && settings.recipients) {
        sendNotificationToContacts(msg, settings.recipients, [event: true])
    }
    else {
        sendPush( msg )
    }
}

//days available
int daysAvailable(){

    // Calculate days available for watering and save in state variable for future use
    def daysA = state.daysAvailable
    if (daysA && (daysA > 0)) {                            // state.daysAvailable has already calculated and stored in state.daysAvailable
        return daysA
    }

    if (!settings.days)    {                                // settings.days = "" --> every day is available
        state.daysAvailable = 7
        return 7        // every day is allowed
    }

    int dayCount = 0                                    // settings.days specified, need to calculate state.davsAvailable (once)
    if (settings.days.contains('Even') || settings.days.contains('Odd')) {
        dayCount = 4
        if(settings.days.contains('Even') && settings.days.contains('Odd')) dayCount = 7
    }
    else {
        if (settings.days.contains('Monday'))         dayCount += 1
        if (settings.days.contains('Tuesday'))         dayCount += 1
        if (settings.days.contains('Wednesday'))    dayCount += 1
        if (settings.days.contains('Thursday'))     dayCount += 1
        if (settings.days.contains('Friday'))         dayCount += 1
        if (settings.days.contains('Saturday'))     dayCount += 1
        if (settings.days.contains('Sunday'))         dayCount += 1
    }

    state.daysAvailable = dayCount
    return dayCount
}

//zone: ['Off', 'Spray', 'rotor', 'Drip', 'Master Valve', 'Pump']
int nozzle(int i){
    String getT = settings."zone${i}"
    if (!getT) return 0

    switch(getT) {
        case 'Spray':
            return 1
        case 'Rotor':
            return 1.4
        case 'Drip':
            return 2.4
        case 'Master Valve':
            return 4
        case 'Pump':
            return 4
        default:
            return 0
    }
}

//plant: ['Lawn', 'Garden', 'Flowers', 'Shrubs', 'Trees', 'Xeriscape', 'New Plants']
int plant(int i){
    String getP = settings."plant${i}"
    if(!getP) return 0

    switch(getP) {
        case 'Lawn':
            return 60
        case 'Garden':
            return 50
        case 'Flowers':
            return 40
        case 'Shrubs':
            return 30
        case 'Trees':
            return 20
        case 'Xeriscape':
            return 30
        case 'New Plants':
            return 80
        default:
            return 0
    }
}

//option: ['Slope', 'Sand', 'Clay', 'No Cycle', 'Cycle 2x', 'Cycle 3x']
int cycles(int i){
    String getC = settings."option${i}"
    if(!getC) return 2

    switch(getC) {
        case 'Slope':
            return 3
        case 'Sand':
            return 1
        case 'Clay':
            return 2
        case 'No Cycle':
            return 1
        case 'Cycle 2x':
            return 2
        case 'Cycle 3x':
            return 3
        default:
            return 2
    }
}

//check if day is allowed
boolean isDay() {

    if (daysAvailable() == 7) return true                        // every day is allowed

    def daynow = new Date()
    String today = daynow.format('EEEE', location.timeZone)
    if (settings.days.contains(today)) return true

    def daynum = daynow.format('dd', location.timeZone)
    int dayint = Integer.parseInt(daynum)
    if (settings.days.contains('Even') && (dayint % 2 == 0)) return true
    if (settings.days.contains('Odd') && (dayint % 2 != 0)) return true
    return false
}

//set season adjustment & remove season adjustment
def setSeason() {
    boolean isDebug = false
    if (isDebug) log.debug 'setSeason()'

    int zone = 1
    while(zone <= 16) {
        if ( !settings.learn || !settings."sensor${zone}" || state.tpwMap[zone-1] == 0) {

            int tpw = initTPW(zone)        // now updates state.tpwMap
            int dpw = initDPW(zone)        // now updates state.dpwMap
            if (isDebug) {
                if (!settings.learn && (tpw != 0) && (state.weekseasonAdj != 0)) {
                    log.debug "Zone ${zone}: seasonally adjusted by ${state.weekseasonAdj-100}% to ${tpw}"
                }
            }
        }
        zone++
    }
}

//TWC functions
def getCity(){
	String wzipcode = zipString()
    String city
    try {
			city = getTwcLocation(wzipcode)?.location?.city ?: wzipcode
		}
	catch (e) {
			log.debug "getTwcLocation exception: $e"
			// There was a problem obtaining the weather with this zip-code, so fall back to the hub's location and note this for future runs.
			city = "unknown city"
		}
    
    return city
}

def getConditions(){
	String wzipcode = zipString()
    def conditionsData
    try {
			conditionsData = getTwcConditions(wzipcode)
		}
	catch (e) {
			log.debug "getTwcLocation exception: $e"
			// There was a problem obtaining the weather with this zip-code, so fall back to the hub's location and note this for future runs.
			return null
		}
    
    return conditionsData
}

def getForecast(){
	String wzipcode = zipString()
    def forecastData
    try {
			forecastData = getTwcForecast(wzipcode)
		}
	catch (e) {
			log.debug "getTwcLocation exception: $e"
			// There was a problem obtaining the weather with this zip-code, so fall back to the hub's location and note this for future runs.
			return null
		}    
    
    return forecastData
}

//capture today's total rainfall - scheduled for just before midnight each day
def getRainToday() {
    //def wzipcode = zipString()
    //def conditionsData = getTwcConditions(wzipcode)
    def conditionsData = getConditions()
    if (!conditionsData) {
        note('warning', "${app.label}: Please check Zipcode/PWS setting, error: null", 'a')
    } else {
        float TRain = 0.0
        if (conditionsData.precip24Hour.isNumber()) {
            TRain = conditionsData.precip24Hour.toFloat()
            if (TRain > 25.0) TRain = 25.0
            else if (TRain < 0.0) TRain = 0.0
            log.debug "getRainToday(): ${conditionsData.precip24Hour} / ${TRain}"
        }
        int day = getWeekDay()                        // what day is it today?
        if (day == 7) day = 0                        // adjust: state.Rain order is Su,Mo,Tu,We,Th,Fr,Sa
        state.Rain[day] = TRain as Float            // store today's total rainfall
    }
}

//check weather, set seasonal adjustment factors, skip today if rainy
boolean isWeather(){
    if (!settings.isRain && !settings.isSeason) return false
    
    def city = getCity()
    def forecastData = getForecast() ?: null
    def conditionsData = getConditions() ?: null
    //log.debug forecastData
    //log.debug conditionsData
    
    //if data is null, skip weather adjustments
    if (!forecastData || !conditionsData) {
        note('warning', "${app.label}: Please check Zipcode/PWS setting, error: null", 'a')
        return false
    }
        
   	//check if day or night
    int not_today = 0
   	if (forecastData.daypart[0].daypartName[0] != "Today") not_today = 1;
    
   	// OK, we have good data, let's start the analysis
    float qpfTodayIn = 0.0
    float qpfTomIn = 0.0
    float popToday = 50.0
    float popTom = 50.0
    float TRain = 0.0
    float YRain = 0.0
    float weeklyRain = 0.0

    if (settings.isRain) {
        log.debug 'isWeather(): isRain'

        // Get forecasted rain for today and tomorrow
        if (!forecastData) {
            log.debug 'isWeather(): Unable to get weather forecast.'
            return false
        }
        
        //log.debug "${forecastData.daypart[0].qpf}"
        //log.debug "${forecastData.daypart[0].precipChance}"
        if (forecastData.daypart[0].qpf[not_today]) qpfTodayIn = forecastData.daypart[0].qpf[not_today].toFloat()
        if (forecastData.daypart[0].precipChance[not_today]) popToday = forecastData.daypart[0].precipChance[not_today].toFloat()
        if (forecastData.daypart[0].qpf[2]) qpfTomIn = forecastData.daypart[0].qpf[1].toFloat()
        if (forecastData.daypart[0].precipChance[2]) popTom = forecastData.daypart[0].precipChance[1].toFloat()
        if (qpfTodayIn > 25.0) qpfTodayIn = 25.0
        else if (qpfTodayIn < 0.0) qpfTodayIn = 0.0
        if (qpfTomIn > 25.0) qpfTomIn = 25.0
        else if (qpfTomIn < 0.0) qpfTomIn = 0.0

        // Get rainfall so far today

        if (!conditionsData) {
            log.debug 'isWeather(): Unable to get current weather conditions.'
            return false
        }
        if (conditionsData.precip24Hour.isNumber()) {
               TRain = conditionsData.precip24Hour.toFloat()
               if (TRain > 25.0) TRain = 25.0            // Ignore runaway weather
               else if (TRain < 0.0) TRain = 0.0        // WU can return -999 for estimated locations
        }
        if (TRain > (qpfTodayIn * (popToday / 100.0))) {  // Not really what PoP means, but use as an adjustment factor of sorts
            qpfTodayIn = TRain                        // already have more rain than was forecast for today, so use that instead
            popToday = 100                            // we KNOW this rain happened
        }

        // Get yesterday's rainfall
        int day = getWeekDay()
        YRain = state.Rain[day - 1]

        log.debug "TRain ${TRain} qpfTodayIn ${qpfTodayIn} @ ${popToday}%, YRain ${YRain}"

        int i = 0
        while (i <= 6){                                // calculate (un)weighted average (only heavy rainstorms matter)
            int factor = 0
            if ((day - i) > 0) factor = day - i else factor =  day + 7 - i
            float getrain = state.Rain[i]
            if (factor != 0) weeklyRain += (getrain / factor)
            i++
        }

        log.debug "isWeather(): weeklyRain ${weeklyRain}"
    }

    log.debug 'isWeather(): build report'
	//log.debug "${forecastData.daypart[0].temperature[not_today]}"
    //get highs
       int highToday = 0
       int highTom = 0
       if (forecastData.daypart[0].temperature[not_today]) highToday = forecastData.daypart[0].temperature[not_today].toInteger()
       if (forecastData.daypart[0].temperature[2]) highTom = forecastData.daypart[0].temperature[2].toInteger()

    String weatherString = "${app.label}: ${city} weather:\n TDA: ${highToday}F"
    if (settings.isRain) weatherString = "${weatherString}, ${qpfTodayIn}in rain (${Math.round(popToday)}% PoP)"
    weatherString = "${weatherString}\n TMW: ${highTom}F"
    if (settings.isRain) weatherString = "${weatherString}, ${qpfTomIn}in rain (${Math.round(popTom)}% PoP)\n YDA: ${YRain}in rain"

    if (settings.isSeason)
    {
        if (!settings.isRain) {                             // we need to verify we have good data first if we didn't do it above

            if (!forecastData) {
                log.debug 'Unable to get weather forecast'
                return false
            }
        }

        // is the temp going up or down for the next few days?
        float heatAdjust = 100.0
        float avgHigh = highToday.toFloat()
        if (highToday != 0) {
            // is the temp going up or down for the next few days?
            int totalHigh = highToday
            int j = 2
            int highs = 1            
            while (j < 6) { // get forecasted high for next 3 days
                if (forecastData.daypart[0].temperature[j].isNumber()) {
                    totalHigh += forecastData.daypart[0].temperature[j].toInteger()
                    highs++
                }
                j+=2
            }
            if ( highs > 0 ) avgHigh = (totalHigh / highs)
            heatAdjust = (avgHigh / highToday).round(2)
        }
        log.debug "highToday ${highToday}, avgHigh ${avgHigh}, heatAdjust ${heatAdjust}"
        
        //get humidity
        int humToday = 0
        int avehumidity = 0
        log.debug "${forecastData.daypart[0].relativeHumidity[not_today]}"
        if (forecastData.daypart[0].relativeHumidity[not_today]) humToday = forecastData.daypart[0].relativeHumidity[not_today]
        
        float humAdjust = 100.0
        float avgHum = humToday.toFloat()
        
        if (humToday != 0 && avehumidity != 0) {
            int j = 2
            int highs = 1
            int totalHum = humToday
            while (j < 6) {                     // get forcasted humitidty for today and the next 3 days                
                if (forecastData.daypart[0].relativeHumidity[j].isNumber()) {
                    totalHum += forecastData.daypart[0].relativeHumidity[j]
                    highs++
                }
                j+=2
            }
            if (highs > 1) avgHum = totalHum / highs
            humAdjust = 1.5 - ((0.5 * avgHum) / humToday)    // basically, half of the delta % between today and today+3 days
        }
        log.debug "humToday ${humToday}, avgHum ${avgHum}, humAdjust ${humAdjust}"

        //daily adjustment - average of heat and humidity factors
        //hotter over next 3 days, more water
        //cooler over next 3 days, less water
        //drier  over next 3 days, more water
        //wetter over next 3 days, less water
        //
        //Note: these should never get to be very large, and work best if allowed to cumulate over time (watering amount will change marginally
        //        as days get warmer/cooler and drier/wetter)
           def sa = ((heatAdjust + humAdjust) / 2)// * 100.0
           state.seasonAdj = sa
           sa = sa - 100.0
        String plus = ''
        if (sa > 0) plus = '+'
        weatherString = "${weatherString}\n Adjusting ${plus}${Math.round(sa)}% for weather forecast"

        // Apply seasonal adjustment on Monday each week or at install
        if ((getWeekDay() == 1) || (state.weekseasonAdj == 0)) {
            //get daylight
             if (conditionsData.sunriseTimeLocal && conditionsData.sunsetTimeLocal) {
                 def hours = new java.text.SimpleDateFormat("HH");
                 def minutes = new java.text.SimpleDateFormat("mm");
                 String nowAsISO = hours.format(new Date());

				def sunriseTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss-SSSS", conditionsData.sunriseTimeLocal)
                def sunsetTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss-SSSS", conditionsData.sunsetTimeLocal)
                
                int getsunRH = hours.format(sunriseTime).toInteger()                
                int getsunRM = minutes.format(sunriseTime).toInteger()
                int getsunSH = hours.format(sunsetTime).toInteger()
                int getsunSM = minutes.format(sunsetTime).toInteger()

                int daylight = ((getsunSH * 60) + getsunSM)-((getsunRH * 60) + getsunRM)
                if (daylight >= 850) daylight = 850
                
                //set seasonal adjustment
                //seasonal q (fudge) factor
                float qFact = 75.0

                // (Daylight / 11.66 hours) * ( Average of ((Avg Temp / 70F) + ((1/2 of Average Humidity) / 65.46))) * calibration quotient
                // Longer days = more water        (day length constant = approx USA day length at fall equinox)
                // Higher temps = more water
                // Lower humidity = more water    (humidity constant = USA National Average humidity in July)
                float wa = ((daylight / 700.0) * (((avgHigh / 70.0) + (1.5-((avgHum * 0.5) / 65.46))) / 2.0) * qFact)
                state.weekseasonAdj = wa

                //apply seasonal time adjustment
                plus = ''
                if (wa != 0) {
                    if (wa > 100.0) plus = '+'
                    String waStr = String.format('%.2f', (wa - 100.0))
                    weatherString = "${weatherString}\n Seasonal adjustment of ${waStr}% for the week"
                }
                setSeason()
            }
            else {
                log.debug 'isWeather(): Unable to get sunrise/set info for today.'
            }
        }
    }
    note('season', weatherString , 'f')

    // if only doing seasonal adjustments, we are done
    if (!settings.isRain) return false

    float setrainDelay = 0.2
    if (settings.rainDelay) setrainDelay = settings.rainDelay.toFloat()

    // if we have no sensors, rain causes us to skip watering for the day
    if (!anySensors()) {
        if (settings.switches.latestValue('rainsensor') == 'rainsensoron'){
            note('raintoday', "${app.label}: skipping, rain sensor is on", 'd')
            return true
           }
           float popRain = qpfTodayIn * (popToday / 100.0)
        if (popRain > setrainDelay){
            String rainStr = String.format('%.2f', popRain)
            note('raintoday', "${app.label}: skipping, ${rainStr}in of rain is probable today", 'd')
            return true
        }
        popRain += qpfTomIn * (popTom / 100.0)
        if (popRain > setrainDelay){
            String rainStr = String.format('%.2f', popRain)
            note('raintom', "${app.label}: skipping, ${rainStr}in of rain is probable today + tomorrow", 'd')
            return true
        }
        if (weeklyRain > setrainDelay){
            String rainStr = String.format('%.2f', weeklyRain)
            note('rainy', "${app.label}: skipping, ${rainStr}in weighted average rain over the past week", 'd')
            return true
        }
    }
    else { // we have at least one sensor in the schedule
        // Ignore rain sensor & historical rain - only skip if more than setrainDelay is expected before midnight tomorrow
        float popRain = (qpfTodayIn * (popToday / 100.0)) - TRain    // ignore rain that has already fallen so far today - sensors should already reflect that
        if (popRain > setrainDelay){
            String rainStr = String.format('%.2f', popRain)
            note('raintoday', "${app.label}: skipping, at least ${rainStr}in of rain is probable later today", 'd')
            return true
        }
        popRain += qpfTomIn * (popTom / 100.0)
        if (popRain > setrainDelay){
            String rainStr = String.format('%.2f', popRain)
            note('raintom', "${app.label}: skipping, at least ${rainStr}in of rain is probable later today + tomorrow", 'd')
            return true
        }
    }
    if (isDebug) log.debug "isWeather() ends"
    return false
}

// true if ANY of this schedule's zones are on and using sensors
private boolean anySensors() {
    int zone=1
    while (zone <= 16) {
        def zoneStr = settings."zone${zone}"
        if (zoneStr && (zoneStr != 'Off') && settings."sensor${zone}") return true
        zone++
    }
    return false
}

def getDPWDays(int dpw){
    if (dpw && (dpw.isNumber()) && (dpw >= 1) && (dpw <= 7)) {
        return state."DPWDays${dpw}"
    } else
          return [0,0,0,0,0,0,0]
}

// Create a map of what days each possible DPW value will run on
// Example:  User sets allowed days to Monday Wed and Fri
// Map would look like: DPWDays1:[1,0,0,0,0,0,0] (run on Monday)
//                      DPWDays2:[1,0,0,0,1,0,0] (run on Monday and Friday)
//                      DPWDays3:[1,0,1,0,1,0,0] (run on Monday Wed and Fri)
// Everything runs on the first day possible, starting with Monday.
def createDPWMap() {
    state.DPWDays1 = []
    state.DPWDays2 = []
    state.DPWDays3 = []
    state.DPWDays4 = []
    state.DPWDays5 = []
    state.DPWDays6 = []
    state.DPWDays7 = []
    //def NDAYS = 7
    // day Distance[NDAYS][NDAYS], easier to just define than calculate everytime
    def int[][] dayDistance = [[0,1,2,3,3,2,1],[1,0,1,2,3,3,2],[2,1,0,1,2,3,3],[3,2,1,0,1,2,3],[3,3,2,1,0,1,2],[2,3,3,2,1,0,1],[1,2,3,3,2,1,0]]
    def ndaysAvailable = daysAvailable()
    int i = 0

    // def int[] daysAvailable = [0,1,2,3,4,5,6]
    def int[] daysAvailable = [0,0,0,0,0,0,0]

    if(settings.days) {
          if (settings.days.contains('Even') || settings.days.contains('Odd')) {
              return
          }
          if (settings.days.contains('Monday')) {
            daysAvailable[i] = 0
            i++
          }
        if (settings.days.contains('Tuesday')) {
            daysAvailable[i] = 1
            i++
          }
          if (settings.days.contains('Wednesday')) {
            daysAvailable[i] = 2
            i++
          }
          if (settings.days.contains('Thursday')) {
            daysAvailable[i] = 3
            i++
          }
          if (settings.days.contains('Friday')) {
            daysAvailable[i] = 4
            i++
          }
          if (settings.days.contains('Saturday')) {
            daysAvailable[i] = 5
            i++
          }
          if (settings.days.contains('Sunday')) {
            daysAvailable[i] = 6
            i++
          }
          if(i != ndaysAvailable) {
            log.debug 'ERROR: days and daysAvailable do not match in setup - overriding'
            log.debug "${i} ${ndaysAvailable}"
            ndaysAvailable = i                // override incorrect setup execution
            state.daysAvailable = i
          }
    }
    else {                    // all days are available if settings.days == ""
        daysAvailable = [0,1,2,3,4,5,6]
    }
    //log.debug "Ndays: ${ndaysAvailable} Available Days: ${daysAvailable}"
    def maxday = -1
    def max = -1
    def dDays = new int[7]
    def int[][] runDays = [[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0]]

    for(def a=0; a < ndaysAvailable; a++) {
          // Figure out next day using the dayDistance map, getting the farthest away day (max value)
          if(a > 0 && ndaysAvailable >= 2 && a != ndaysAvailable-1) {
            if(a == 1) {
                  for(def c=1; c < ndaysAvailable; c++) {
                    def d = dayDistance[daysAvailable[0]][daysAvailable[c]]
                      if(d > max) {
                          max = d
                          maxday = daysAvailable[c]
                    }
                  }
                  //log.debug "max: ${max}  maxday: ${maxday}"
                  dDays[0] = maxday
            }

            // Find successive maxes for the following days
            if(a > 1) {
                  def lmax = max
                  def lmaxday = maxday
                  max = -1
                  for(int c = 1; c < ndaysAvailable; c++) {
                    def d = dayDistance[daysAvailable[0]][daysAvailable[c]]
                    def t = d > max
                    if (a % 2 == 0)    t = d >= max
                    if(d < lmax && d >= max) {
                         if(d == max) {
                              d = dayDistance[lmaxday][daysAvailable[c]]
                            if(d > dayDistance[lmaxday][maxday]) {
                                  max = d
                                  maxday = daysAvailable[c]
                            }
                          }
                          else {
                            max = d
                            maxday = daysAvailable[c]
                          }
                    }
                  }
                  lmax = 5
                  while(max == -1) {
                    lmax = lmax -1
                    for(int c = 1; c < ndaysAvailable; c++) {
                          def d = dayDistance[daysAvailable[0]][daysAvailable[c]]
                          if(d < lmax && d >= max) {
                            if(d == max) {
                                    d = dayDistance[lmaxday][daysAvailable[c]]
                                  if(d > dayDistance[lmaxday][maxday]) {
                                    max = d
                                    maxday = daysAvailable[c]
                                  }
                            }
                            else {
                                  max = d
                                  maxday = daysAvailable[c]
                            }
                          }
                    }
                    for (def d=0; d< a-2; d++) {
                          if(maxday == dDays[d]) max = -1
                      }
                  }
                  //log.debug "max: ${max} maxday: ${maxday}"
                  dDays[a-1] = maxday
            }
          }

          // Set the runDays map using the calculated maxdays
          for(int b=0; b < 7; b++) {
            // Runs every day available
            if(a == ndaysAvailable-1) {
                  runDays[a][b] = 0
                  for (def c=0; c < ndaysAvailable; c++) {
                    if(b == daysAvailable[c]) runDays[a][b] = 1
                  }
            }
            else {
                  // runs weekly, use first available day
                  if(a == 0) {
                    if(b == daysAvailable[0])
                          runDays[a][b] = 1
                    else
                          runDays[a][b] = 0
                  }
                  else {
                    // Otherwise, start with first available day
                    if(b == daysAvailable[0])
                          runDays[a][b] = 1
                    else {
                          runDays[a][b] = 0
                          for(def c=0; c < a; c++)
                          if(b == dDays[c])
                            runDays[a][b] = 1
                    }
                  }
            }
          }
    }

      //log.debug "DPW: ${runDays}"
    state.DPWDays1 = runDays[0]
    state.DPWDays2 = runDays[1]
    state.DPWDays3 = runDays[2]
    state.DPWDays4 = runDays[3]
    state.DPWDays5 = runDays[4]
    state.DPWDays6 = runDays[5]
    state.DPWDays7 = runDays[6]
}

//transition page to populate app state - this is a fix for WP param
def zoneSetPage1(){
    state.app = 1
    zoneSetPage()
    }
def zoneSetPage2(){
    state.app = 2
    zoneSetPage()
    }
def zoneSetPage3(){
    state.app = 3
    zoneSetPage()
    }
def zoneSetPage4(){
    state.app = 4
    zoneSetPage()
    }
def zoneSetPage5(){
    state.app = 5
    zoneSetPage()
    }
def zoneSetPage6(){
    state.app = 6
    zoneSetPage()
    }
def zoneSetPage7(){
    state.app = 7
    zoneSetPage()
    }
def zoneSetPage8(){
    state.app = 8
    zoneSetPage()
    }
def zoneSetPage9(i){
    state.app = 9
    zoneSetPage()
    }
def zoneSetPage10(){
    state.app = 10
    zoneSetPage()
    }
def zoneSetPage11(){
    state.app = 11
    zoneSetPage()
    }
def zoneSetPage12(){
    state.app = 12
    zoneSetPage()
    }
def zoneSetPage13(){
    state.app = 13
    zoneSetPage()
    }
def zoneSetPage14(){
    state.app = 14
    zoneSetPage()
    }
def zoneSetPage15(){
    state.app = 15
    zoneSetPage()
    }
def zoneSetPage16(){
    state.app = 16
    zoneSetPage()
    }
