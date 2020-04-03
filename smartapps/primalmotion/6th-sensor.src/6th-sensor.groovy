/*
 * 6th-sensor.groovy
 *
 * Copyright (C) 2010 Antoine Mercadal <antoine.mercadal@inframonde.eu>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

definition(
    name:           "6th Sensor",
    namespace:      "primalmotion",
    author:         "primalmotion",
    description:    "Turn on lights when dark, deal with night path, and more",
    category:       "Convenience",
    iconUrl:        "http://icons.iconarchive.com/icons/graphicloads/medical-health/96/eye-icon.png",
    iconX2Url:      "http://icons.iconarchive.com/icons/graphicloads/medical-health/96/eye-icon.png"
)



/*
    Preferences
*/

preferences
{
    page(name: "preferencesRootPage")
    page(name: "preferencesNightLights")
    page(name: "preferencesNightPath")
}


def preferencesRootPage()
{
    dynamicPage(name: "preferencesRootPage", title: "preferencesRootPage", uninstall: true, install: true)
    {
        section("6th Sensor")
        {
            paragraph "Welcome to 6th Sensor. This Smart App can group a few light and motion sensors, and make them work together to decide when and how lights should be turned on or off."
            label title: "Assign a name", required: false
        }

        section("Motion Sensors")
        {
            paragraph "Motion sensors used to make 6th Sensor trigger an illuminance check"
            input "motionsensordevices", "capability.motionSensor", title: "Motion Sensors", required: true, multiple: true
        }

        section("Light Sensor")
        {
            paragraph "Light sensor used to determine the global illuminance"
            input "lightsensordevice", "capability.illuminanceMeasurement", title: "Light Sensors", required: true
            input "luminancetrigger", "number", title: "Consider dark when illuminance is below (LUX): ", required: false, defaultValue: 300
        }

        section ("6th Sensor Configuration")
        {
            href(name: "topreferencesNightLights", page: "preferencesNightLights", title: "Night Lights")
            href(name: "topreferencesNightPath", page: "preferencesNightPath", title: "Night Path")
        }
    }
}

def preferencesNightLights()
{
    dynamicPage(name: "preferencesNightLights", title: "preferencesNightLights", submitOnChange: true)
    {
        section ("Night Light")
        {
            paragraph "Night Light will turn light on and off based on motion and global illuminance when you are at home."
            input "nightlightsenabled", "boolean", title: "Enable Night Light", required: true
        }

         section ("Settings")
         {
            input "homemode", "mode", title: "When home is in mode", required: false, defaultValue: "Home"
            input "nightlights", "capability.switch", title: "Turn these lights on", multiple: true, required: false
            input "nightlighttime", "number", title: "Turn off after no motion detected for (min)", required: false, defaultValue: 10
        }
    }
}

def preferencesNightPath()
{
    dynamicPage(name: "preferencesNightPath", title: "preferencesNightPath", submitOnChange: true)
    {
        section ("Night Path")
        {
            paragraph "Night Path will turn on a few lights for a bit when motion is detected during night."
            input "nightpathenabled", "boolean", title: "Enable Night Path", required: true
        }

        section ("Settings")
        {
            input "nightmode", "mode", title: "When home is in mode", required: false,  defaultValue: "Night"
            input "pathligths", "capability.switch", title: "On motion, turn these lights on", multiple: true, required: false
            input "pathtime", "number", title: "Turn them off after (seconds)", required: false, defaultValue: 60
        }
    }
}


/*
    SmartThings Installation Handlers
*/
def installed()
{
    initialize()
}

def updated()
{
    unsubscribe()
    initialize()
}

def initialize()
{
    log.debug("[initialize]: Settings: ${settings}")

    subscribe(settings.lightsensordevice, "illuminance", on_event)

    for (d in settings.motionsensordevices)
        subscribe(d, "motion.active", on_event)
}



/*
    Events Management
*/

def on_event(evt)
{
    log.debug("[on_event]: ------------- START ------------")

    if (settings.nightlightsenabled && location.mode == settings.homemode)
        on_home_event(evt)

    if (settings.nightpathenabled && location.mode == settings.nightmode)
        on_night_event(evt)
    
    log.debug("[on_event]: -------------- END -------------")
}

def on_home_event(evt)
{
    log.debug("[on_home_event]: name: ${evt.name}, value: ${evt.value}, from ${evt.displayName}")

    def is_dark = is_dark()
    def active     = are_motion_sensors_active()

    log.debug("[on_home_event]: is_dark: ${is_dark}, active: ${active}")

    if (evt.name == "motion" && active)
    {
        if (is_dark)
            turn_nightlight_lights_on()
        else
            turn_nightlight_lights_off()
    }
    else if (evt.name == "illuminance" && !is_dark)
        turn_nightlight_lights_off()
}

def on_night_event(evt)
{
    log.debug("[on_night_event]: night event: name: ${evt.name}, value: ${evt.value}, from ${evt.displayName}")

    if (evt.name != "motion" || !are_motion_sensors_active())
        return

    turn_nightpath_lights_on()

    unschedule("turn_nightpath_lights_off")
    runIn(settings.pathtime, "turn_nightpath_lights_off")
}


/*
    Night Ligths Utilities
*/

def turn_nightlight_lights_on()
{
    log.debug("[turn_nightlight_lights_on]: turning lights on")
    set_lights_state(settings.nightlights, true)

    log.debug("[turn_nightlight_lights_on]: canceling any secheduled light off.")
    unschedule("turn_nightlight_lights_off")

    log.debug("[turn_nightlight_lights_on]: scheduling may_turn_nightlight_lights_off in ${settings.nightlighttime} minutes")
    runIn(settings.nightlighttime * 60, "may_turn_nightlight_lights_off")
}

def may_turn_nightlight_lights_off()
{
    if (are_motion_sensors_active())
    {
        log.debug("[may_turn_nightlight_lights_off]: one motion sensor is still active. rescheduling myself to run in ${settings.nightlighttime} min")
        runIn(settings.nightlighttime * 60, "may_turn_nightlight_lights_off")
    }
    else
    {
        log.debug("[may_turn_nightlight_lights_off]: no motion sensor remaining active. turning lights off")
        turn_nightlight_lights_off()
    }
}

def turn_nightlight_lights_off()
{
    log.debug("[turn_nightlight_lights_off]: turning lights off")
    set_lights_state(settings.nightlights, false)
}



/*
    Night Path Utilities
*/

def turn_nightpath_lights_on()
{
    log.debug("[turn_nightpath_lights_on]: turning path lights on")
    set_lights_state(settings.pathligths, true)
}

def turn_nightpath_lights_off()
{
    log.debug("[turn_nightpath_lights_off]: turning path lights off")
    set_lights_state(settings.pathligths, false)
}


/*
    Global Utilities
*/

def set_lights_state(targets, state)
{
    for (l in targets)
    {
        if (state)
            l.on()   
        else
            l.off()
    }
}

def is_dark()
{
    return lightsensordevice.currentIlluminance <= settings.luminancetrigger
}

def are_motion_sensors_active()
{
    for (device in settings.motionsensordevices)
    {
        if (device.currentValue("motion") == "active")
        {
            log.debug("[are_motion_sensors_active]: motion sensor ${device.displayName} is active -> active")
            
            return true;
        }
    }

    log.debug("[are_motion_sensors_active]: found 0 active motion sensor ->inactive")

    return false;
}