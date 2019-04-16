/**
 *	Echo Speaks Device
 *
 *  Copyright 2018, 2019 Anthony Santilli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */

import groovy.json.*
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern
String devVersion()  { return "2.4.0"}
String devModified() { return "2019-01-27" }
Boolean isBeta()     { return false }
Boolean isST()       { return (getPlatform() == "SmartThings") }

metadata {
    definition (name: "Echo Speaks Device", namespace: "tonesto7", author: "Anthony Santilli", mnmn: "SmartThings", vid: "generic-music-player") {
        //capability "Audio Mute" // Not Compatible with Hubitat
        capability "Audio Notification"
        capability "Audio Volume"
        capability "Music Player"
        capability "Notification"
        capability "Refresh"
        capability "Sensor"
        capability "Speech Synthesis"

        attribute "alarmSupported", "string"
        attribute "alarmVolume", "number"
        attribute "alexaMusicProviders", "JSON_OBJECT"
        attribute "alexaNotifications", "JSON_OBJECT"
        attribute "alexaPlaylists", "JSON_OBJECT"
        attribute "alexaWakeWord", "string"
        attribute "btDeviceConnected", "string"
        attribute "btDevicesPaired", "string"
        attribute "currentAlbum", "string"
        attribute "currentStation", "string"
        attribute "deviceFamily", "string"
        attribute "deviceStatus", "string"
        attribute "deviceStyle", "string"
        attribute "deviceType", "string"
        attribute "doNotDisturb", "string"
        attribute "firmwareVer", "string"
        attribute "lastCmdSentDt", "string"
        attribute "lastSpeakCmd", "string"
        attribute "lastSpokenToTime", "number"
        attribute "lastVoiceActivity", "string"
        attribute "lastUpdated", "string"
        attribute "musicSupported", "string"
        attribute "onlineStatus", "string"
        attribute "reminderSupported", "string"
        attribute "supportedMusic", "string"
        attribute "trackImage", "string"
        attribute "trackImageHtml", "string"
        attribute "ttsSupported", "string"
        attribute "volume", "number"
        attribute "volumeSupported", "string"
        attribute "wakeWords", "enum"
        attribute "wasLastSpokenToDevice", "string"

        command "playText", ["string"] //This command is deprecated in ST but will work
        command "playTextAndResume"
        command "playTrackAndResume"
        command "playTrackAndRestore"
        command "playTextAndRestore"
        command "replayText"
        command "doNotDisturbOn"
        command "doNotDisturbOff"
        command "setAlarmVolume", ["number"]
        command "resetQueue"
        command "playWeather", ["number", "number"]
        command "playSingASong", ["number", "number"]
        command "playFlashBrief", ["number", "number"]
        command "playFunFact", ["number", "number"]
        command "playTraffic", ["number", "number"]
        command "playJoke", ["number", "number"]
        command "playTellStory", ["number", "number"]
        command "sayGoodbye", ["number", "number"]
        command "sayGoodNight", ["number", "number"]
        command "sayBirthday", ["number", "number"]
        command "sayCompliment", ["number", "number"]
        command "sayGoodMorning", ["number", "number"]
        command "sayWelcomeHome", ["number", "number"]
        // command "playCannedRandomTts", ["string", "number", "number"]
        // command "playCannedTts", ["string", "string", "number", "number"]
        command "playAnnouncement", ["string", "number", "number"]
        command "playAnnouncement", ["string", "string", "number", "number"]
        command "playAnnouncementAll", ["string", "string"]
        command "playCalendarToday", ["number", "number"]
        command "playCalendarTomorrow", ["number", "number"]
        command "playCalendarNext", ["number", "number"]
        command "stopAllDevices"
        command "searchMusic", ["string", "string", "number", "number"]
        command "searchAmazonMusic", ["string", "number", "number"]
        command "searchAppleMusic", ["string", "number", "number"]
        command "searchPandora", ["string", "number", "number"]
        command "searchIheart", ["string", "number", "number"]
        command "searchSiriusXm", ["string", "number", "number"]
        command "searchSpotify", ["string", "number", "number"]
        // command "searchTidal", ["string", "number", "number"]
        command "searchTuneIn", ["string", "number", "number"]
        command "sendAlexaAppNotification", ["string"]
        command "executeSequenceCommand", ["string"]
        command "executeRoutineId", ["string"]
        command "createAlarm", ["string", "string", "string"]
        command "createReminder", ["string", "string", "string"]
        command "removeNotification", ["string"]
        command "setWakeWord", ["string"]
        command "renameDevice", ["string"]
        command "storeCurrentVolume"
        command "restoreLastVolume"
        command "setVolumeAndSpeak", ["number", "string"]
        command "setVolumeSpeakAndRestore", ["number", "string", "number"]
        command "volumeUp"
        command "volumeDown"
        command "speechTest"
        command "sendTestAnnouncement"
        command "sendTestAnnouncementAll"
        command "getDeviceActivity"
        command "getBluetoothDevices"
        command "connectBluetooth", ["string"]
        command "disconnectBluetooth"
        command "removeBluetooth", ["string"]
    }

    tiles (scale: 2) {
        multiAttributeTile(name: "mediaMulti", type:"mediaPlayer", width:6, height:4) {
            tileAttribute("device.status", key: "PRIMARY_CONTROL") {
                attributeState("paused", label:"Paused")
                attributeState("playing", label:"Playing")
                attributeState("stopped", label:"Stopped", defaultState: true)
            }
            tileAttribute("device.status", key: "MEDIA_STATUS") {
                attributeState("paused", label:"Paused", action:"music Player.play", nextState: "playing")
                attributeState("playing", label:"Playing", action:"music Player.pause", nextState: "paused")
                attributeState("stopped", label:"Stopped", action:"music Player.play", nextState: "playing", defaultState: true)
            }
            tileAttribute("device.status", key: "PREVIOUS_TRACK") {
                attributeState("status", action:"music Player.previousTrack", defaultState: true)
            }
            tileAttribute("device.status", key: "NEXT_TRACK") {
                attributeState("status", action:"music Player.nextTrack", defaultState: true)
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState("level", action:"music Player.setLevel", defaultState: true)
            }
            tileAttribute ("device.mute", key: "MEDIA_MUTED") {
                attributeState("unmuted", action:"music Player.mute", nextState: "muted")
                attributeState("muted", action:"music Player.unmute", nextState: "unmuted", defaultState: true)
            }
            tileAttribute("device.trackDescription", key: "MARQUEE") {
                attributeState("trackDescription", label:"${currentValue}", defaultState: true)
            }
        }
        standardTile("deviceStatus", "device.deviceStatus", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
            state("paused_unknown", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/unknown.png", backgroundColor: "#cccccc")
            state("playing_unknown", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/unknown.png", backgroundColor: "#00a0dc")
            state("stopped_unknown", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/unknown.png")

            state("paused_echo_gen1", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_gen1.png", backgroundColor: "#cccccc")
            state("playing_echo_gen1", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_gen1.png", backgroundColor: "#00a0dc")
            state("stopped_echo_gen1", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_gen1.png")

            state("paused_echo_gen2", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_gen2.png", backgroundColor: "#cccccc")
            state("playing_echo_gen2", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_gen2.png", backgroundColor: "#00a0dc")
            state("stopped_echo_gen2", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_gen2.png")

            state("paused_echo_plus_gen1", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_plus_gen1.png", backgroundColor: "#cccccc")
            state("playing_echo_plus_gen1", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_plus_gen1.png", backgroundColor: "#00a0dc")
            state("stopped_echo_plus_gen1", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_plus_gen1.png")

            state("paused_echo_plus_gen2", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_plus_gen2.png", backgroundColor: "#cccccc")
            state("playing_echo_plus_gen2", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_plus_gen2.png", backgroundColor: "#00a0dc")
            state("stopped_echo_plus_gen2", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_plus_gen2.png")

            state("paused_echo_dot_gen1", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_dot_gen1.png", backgroundColor: "#cccccc")
            state("playing_echo_dot_gen1", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_dot_gen1.png", backgroundColor: "#00a0dc")
            state("stopped_echo_dot_gen1", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_dot_gen1.png")

            state("paused_echo_dot_gen2", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_dot_gen2.png", backgroundColor: "#cccccc")
            state("playing_echo_dot_gen2", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_dot_gen2.png", backgroundColor: "#00a0dc")
            state("stopped_echo_dot_gen2", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_dot_gen2.png")

            state("paused_echo_dot_gen3", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_dot_gen3.png", backgroundColor: "#cccccc")
            state("playing_echo_dot_gen3", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_dot_gen3.png", backgroundColor: "#00a0dc")
            state("stopped_echo_dot_gen3", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_dot_gen3.png")

            state("paused_echo_spot_gen1", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_spot_gen1.png", backgroundColor: "#cccccc")
            state("playing_echo_spot_gen1", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_spot_gen1.png", backgroundColor: "#00a0dc")
            state("stopped_echo_spot_gen1", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_spot_gen1.png")

            state("paused_echo_show_gen1", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_show_gen1.png", backgroundColor: "#cccccc")
            state("playing_echo_show_gen1", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_show_gen1.png", backgroundColor: "#00a0dc")
            state("stopped_echo_show_gen1", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_show_gen1.png")

            state("paused_echo_show_gen2", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_show_gen2.png", backgroundColor: "#cccccc")
            state("playing_echo_show_gen2", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_show_gen2.png", backgroundColor: "#00a0dc")
            state("stopped_echo_show_gen2", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_show_gen2.png")

            state("paused_echo_tap", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_tap.png", backgroundColor: "#cccccc")
            state("playing_echo_tap", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_tap.png", backgroundColor: "#00a0dc")
            state("stopped_echo_tap", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_tap.png")

            state("paused_amazon_tablet", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/amazon_tablet.png", backgroundColor: "#cccccc")
            state("playing_amazon_tablet", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/amazon_tablet.png", backgroundColor: "#00a0dc")
            state("stopped_amazon_tablet", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/amazon_tablet.png")

            state("paused_echo_sub_gen1", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_sub_gen1.png", backgroundColor: "#cccccc")
            state("playing_echo_sub_gen1", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_sub_gen1.png", backgroundColor: "#00a0dc")
            state("stopped_echo_sub_gen1", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_sub_gen1.png")

            state("paused_firetv_cube", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/firetv_cube.png", backgroundColor: "#cccccc")
            state("playing_firetv_cube", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/firetv_cube.png", backgroundColor: "#00a0dc")
            state("stopped_firetv_cube", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/firetv_cube.png")

            state("paused_firetv_gen1", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/firetv_gen1.png", backgroundColor: "#cccccc")
            state("playing_firetv_gen1", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/firetv_gen1.png", backgroundColor: "#00a0dc")
            state("stopped_firetv_gen1", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/firetv_gen1.png")

            state("paused_tablet_hd10", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/tablet_hd10.png", backgroundColor: "#cccccc")
            state("playing_tablet_hd10", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/tablet_hd10.png", backgroundColor: "#00a0dc")
            state("stopped_tablet_hd10", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/tablet_hd10.png")

            state("paused_firetv_stick_gen1", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/firetv_stick_gen1.png", backgroundColor: "#cccccc")
            state("playing_firetv_stick_gen1", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/firetv_stick_gen1.png", backgroundColor: "#00a0dc")
            state("stopped_firetv_stick_gen1", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/firetv_stick_gen1.png")

            state("paused_echo_wha", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_wha.png", backgroundColor: "#cccccc")
            state("playing_echo_wha", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_wha.png", backgroundColor: "#00a0dc")
            state("stopped_echo_wha", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_wha.png")

            state("paused_echo_input", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_input.png", backgroundColor: "#cccccc")
            state("playing_echo_input", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_input.png", backgroundColor: "#00a0dc")
            state("stopped_echo_input", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_input.png")

            state("paused_one_link", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/one_link.png", backgroundColor: "#cccccc")
            state("playing_one_link", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/one_link.png", backgroundColor: "#00a0dc")
            state("stopped_one_link", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/one_link.png")

            state("paused_sonos_generic", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/sonos_generic.png", backgroundColor: "#cccccc")
            state("playing_sonos_generic", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/sonos_generic.png", backgroundColor: "#00a0dc")
            state("stopped_sonos_generic", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/sonos_generic.png")

            state("paused_sonos_beam", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/sonos_beam.png", backgroundColor: "#cccccc")
            state("playing_sonos_beam", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/sonos_beam.png", backgroundColor: "#00a0dc")
            state("stopped_sonos_beam", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/sonos_beam.png")

            state("paused_alexa_windows", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/alexa_windows.png", backgroundColor: "#cccccc")
            state("playing_alexa_windows", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/alexa_windows.png", backgroundColor: "#00a0dc")
            state("stopped_alexa_windows", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/alexa_windows.png")

            state("paused_dash_wand", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/dash_wand.png", backgroundColor: "#cccccc")
            state("playing_dash_wand", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/dash_wand.png", backgroundColor: "#00a0dc")
            state("stopped_dash_wand", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/dash_wand.png")

            state("paused_fabriq_chorus", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/fabriq_chorus.png", backgroundColor: "#cccccc")
            state("playing_fabriq_chorus", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/fabriq_chorus.png", backgroundColor: "#00a0dc")
            state("stopped_fabriq_chorus", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/fabriq_chorus.png")

            state("paused_fabriq_riff", label:"Paused", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/fabriq_riff.png", backgroundColor: "#cccccc")
            state("playing_fabriq_riff", label:"Playing", action:"music Player.pause", nextState: "paused", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/fabriq_riff.png", backgroundColor: "#00a0dc")
            state("stopped_fabriq_riff", label:"Stopped", action:"music Player.play", nextState: "playing", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/fabriq_riff.png")
        }
        valueTile("blank1x1", "device.blank", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
            state("default", label:'')
        }
        valueTile("blank2x1", "device.blank", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state("default", label:'')
        }
        valueTile("blank2x2", "device.blank", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
            state("default", label:'')
        }
        valueTile("alarmVolume", "device.alarmVolume", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state("alarmVolume", label:'Alarm Volume:\n${currentValue}%')
        }
        valueTile("volumeSupported", "device.volumeSupported", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state("volumeSupported", label:'Volume Supported:\n${currentValue}')
        }
        valueTile("ttsSupported", "device.ttsSupported", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state("ttsSupported", label:'TTS Supported:\n${currentValue}')
        }
        valueTile("deviceStyle", "device.deviceStyle", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state("deviceStyle", label:'Device Style:\n${currentValue}')
        }
        valueTile("onlineStatus", "device.onlineStatus", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state("onlineStatus", label:'Online Status:\n${currentValue}')
        }
        valueTile("currentStation", "device.currentStation", height: 1, width: 3, inactiveLabel: false, decoration: "flat") {
            state("default", label:'Station:\n${currentValue}')
        }
        valueTile("currentAlbum", "device.currentAlbum", height: 1, width: 3, inactiveLabel: false, decoration: "flat") {
            state("default", label:'Album:\n${currentValue}')
        }
        valueTile("lastSpeakCmd", "device.lastSpeakCmd", height: 2, width: 3, inactiveLabel: false, decoration: "flat") {
            state("lastSpeakCmd", label:'Last Text Sent:\n${currentValue}')
        }
        valueTile("lastCmdSentDt", "device.lastCmdSentDt", height: 2, width: 3, inactiveLabel: false, decoration: "flat") {
            state("lastCmdSentDt", label:'Last Text Sent:\n${currentValue}')
        }
        valueTile("lastVoiceActivity", "device.lastVoiceActivity", height: 2, width: 3, inactiveLabel: false, decoration: "flat") {
            state("lastVoiceActivity", label:'Last Voice Cmd:\n${currentValue}')
        }
        valueTile("alexaWakeWord", "device.alexaWakeWord", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state("alexaWakeWord", label:'Wake Word:\n${currentValue}')
        }
        valueTile("supportedMusic", "device.supportedMusic", height: 2, width: 3, inactiveLabel: false, decoration: "flat") {
            state("supportedMusic", label:'Supported Music:\n${currentValue}')
        }
        valueTile("btDeviceConnected", "device.btDeviceConnected", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state("btDeviceConnected", label:'Connected Bluetooth Device:\n${currentValue}')
        }
        valueTile("btDevicesPaired", "device.btDevicesPaired", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
            state("btDevicesPaired", label:'Paired Bluetooth Devices:\n${currentValue}')
        }
        standardTile("speechTest", "speechTest", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'speechTest', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/speak_test.png")
        }
        standardTile("searchTest", "searchTest", height: 1, width: 2, decoration: "flat") {
            state("default", label:'MusicSearch Test', action: 'searchTest')
        }
        standardTile("sendTestAnnouncement", "sendTestAnnouncement", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'sendTestAnnouncement', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/announcement.png")
        }
        standardTile("sendTestAnnouncementAll", "sendTestAnnouncementAll", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'sendTestAnnouncementAll', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/announcement_all.png")
        }
        standardTile("stopAllDevices", "stopAllDevices", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'stopAllDevices', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/stop_all.png")
        }
        standardTile("playWeather", "playWeather", height: 1, width: 1, decoration: "flat") {
            state("default", action: 'playWeather', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/weather_report.png")
        }
        standardTile("playSingASong", "playSingASong", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'playSingASong', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/sing_song.png")
        }
        standardTile("playFlashBrief", "playFlashBrief", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'playFlashBrief', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/flash_brief.png")
        }
        standardTile("sayGoodMorning", "sayGoodMorning", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'sayGoodMorning', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/good_morning.png")
        }
        standardTile("playTraffic", "playTraffic", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'playTraffic', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/play_traffic.png")
        }
        standardTile("playTellStory", "playTellStory", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'playTellStory', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/story.png")
        }
        standardTile("playJoke", "playJoke", height: 1, width: 1, decoration: "flat") {
            state("default", action: 'playJoke', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/play_joke.png")
        }
        standardTile("playFunFact", "playFunFact", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'playFunFact', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/fact.png")
        }
        standardTile("playCalendarToday", "playCalendarToday", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'playCalendarToday', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/calendar_today.png")
        }
        standardTile("playCalendarTomorrow", "playCalendarTomorrow", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'playCalendarTomorrow', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/calendar_tomorrow.png")
        }
        standardTile("playCalendarNext", "playCalendarNext", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'playCalendarNext', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/calendar_next.png")
        }
        standardTile("sayWelcomeHome", "sayWelcomeHome", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'sayWelcomeHome', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/welcome_home.png")
        }
        standardTile("sayGoodNight", "sayGoodNight", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'sayGoodNight', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/good_night.png")
        }
        standardTile("sayCompliment", "sayCompliment", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'sayCompliment', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/compliment.png")
        }
        standardTile("volumeUp", "volumeUp", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'volumeUp', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/volume_up.png")
        }
        standardTile("volumeDown", "volumeDown", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'volumeDown', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/volume_down.png")
        }
        standardTile("resetQueue", "resetQueue", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'resetQueue', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/reset_queue.png")
        }
        standardTile("refresh", "device.refresh", width:1, height:1, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/refresh.png"
		}
        standardTile("doNotDisturb", "device.doNotDisturb", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
            state "true", label: '', action: "doNotDisturbOff", nextState: "false", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/dnd_on.png"
            state "false", label: '', action: "doNotDisturbOn", nextState: "true", icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/dnd_off.png"
        }
        standardTile("disconnectBluetooth", "disconnectBluetooth", height: 1, width: 1, decoration: "flat") {
            state("default", label:'', action: 'disconnectBluetooth', icon: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/device/disconnect_bluetooth.png")
        }
        main(["deviceStatus"])
        details([
            "mediaMulti",
            "volumeUp", "volumeDown", "stopAllDevices", "doNotDisturb", "refresh", "disconnectBluetooth",
            "playWeather", "playSingASong", "playFlashBrief", "playTraffic", "playTellStory", "playFunFact",
            "playJoke", "sayWelcomeHome", "sayGoodMorning", "sayGoodNight", "sayCompliment", "resetQueue",
            "playCalendarToday", "playCalendarTomorrow", "playCalendarNext", "speechTest", "sendTestAnnouncement", "sendTestAnnouncementAll",
            "currentAlbum", "currentStation",
            "alarmVolume",  "btDeviceConnected", "btDevicesPaired", "deviceStyle", "onlineStatus", "volumeSupported", "alexaWakeWord", "ttsSupported", "supportedMusic", "lastSpeakCmd", "lastCmdSentDt", "lastVoiceActivity"
        ])
    }

    preferences {
        section("Preferences") {
            input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
            input "disableQueue", "bool", required: false, title: "Don't Allow Queuing?", defaultValue: false
            input "disableTextTransform", "bool", required: false, title: "Disable Text Transform?", description: "This will attempt to convert items in text like temp units and directions like `WSW` to west southwest", defaultValue: false
        }
    }
}

def installed() {
    log.trace "${device?.displayName} Executing Installed..."
    setLevel(20)
    sendEvent(name: "alarmVolume", value: 0)
    sendEvent(name: "mute", value: "unmuted")
    sendEvent(name: "status", value: "stopped")
    sendEvent(name: "deviceStatus", value: "stopped_echo_gen1")
    sendEvent(name: "trackDescription", value: "")
    sendEvent(name: "lastSpeakCmd", value: "Nothing sent yet...")
    sendEvent(name: "doNotDisturb", value: false)
    sendEvent(name: "onlineStatus", value: "online")
    sendEvent(name: "alarmVolume", value: 0)
    sendEvent(name: "alexaWakeWord", value: "")
    state?.doNotDisturb = false
    initialize()
}

def updated() {
    log.trace "${device?.displayName} Executing Updated()"
    initialize()
}

def initialize() {
    log.trace "${device?.displayName} Executing initialize()"
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
    sendEvent(name: "DeviceWatch-Enroll", value: new JsonOutput().toJson([protocol: "cloud", scheme:"untracked"]), displayed: false)
    resetQueue()
    stateCleanup()
    schedDataRefresh(true)
    refreshData()
}

public triggerInitialize() {
    runIn(3, "initialize")
}

String getHealthStatus(lower=false) {
	String res = device?.getStatus()
	if(lower) { return res?.toString()?.toLowerCase() }
	return res as String
}

def getShortDevName(){
    return device?.displayName?.replace("Echo - ", "")
}

public setAuthState(authenticated) {
    state?.authValid = (authenticated == true)
    if(authenticated != true && state?.refreshScheduled) {
        log.warn "Cookie Authentication Cleared by Parent.  Scheduled Refreshes also cancelled!"
        unschedule("refreshData"); state?.refreshScheduled = false
    }
}

Boolean isAuthOk() {
    if(state?.authValid != true && state?.refreshScheduled) { unschedule("refreshData"); state?.refreshScheduled = false }
    if(state?.authValid != true && state?.cookie != null) {
        log.warn "Echo Speaks Authentication is no longer valid... Please login again and commands will be allowed again!!!"
        state?.remove("cookie")
        return false
    } else { return true }
}

Boolean isCommandTypeAllowed(String type, noLogs=false) {
    Boolean isOnline = (device?.currentValue("onlineStatus") == "online")
    if(!isOnline) { if(!noLogs) { log.warn "Commands NOT Allowed! Device is currently (OFFLINE) | Type: (${type})" }; return false; }
    if(!getAmazonDomain()) { if(!noLogs) { log.warn "amazonDomain State Value Missing: ${getAmazonDomain()}" }; return false }
    if(!state?.cookie || !state?.cookie?.cookie || !state?.cookie?.csrf) { if(!noLogs) { log.warn "Amazon Cookie State Values Missing: ${state?.cookie}" }; return false }
    if(!state?.serialNumber) { if(!noLogs) { log.warn "SerialNumber State Value Missing: ${state?.serialNumber}" }; return false }
    if(!state?.deviceType) { if(!noLogs) { log.warn "DeviceType State Value Missing: ${state?.deviceType}" }; return false }
    if(!state?.deviceOwnerCustomerId) { if(!noLogs) { log.warn "OwnerCustomerId State Value Missing: ${state?.deviceOwnerCustomerId}" }; return false; }
    if(state?.isSupportedDevice == false) { log.warn "You are using an Unsupported/Unknown Device all restrictions have been removed for testing! If commands function please report device info to developer"; return true; }
    if(!type || state?.permissions == null) { if(!noLogs) { log.warn "Permissions State Object Missing: ${state?.permissions}" }; return false }
    if(state?.doNotDisturb == true && (!(type in ["volumeControl", "alarms", "reminders", "doNotDisturb", "wakeWord", "bluetoothControl"]))) { if(!noLogs) { log.warn "All Voice Output Blocked... Do Not Disturb is ON" }; return false }
    if(state?.permissions?.containsKey(type) && state?.permissions[type] == true) { return true }
    else {
        String warnMsg = null
        switch(type) {
            case "TTS":
                warnMsg = "OOPS... Text to Speech is NOT Supported by this Device!!!"
                break
            case "mediaPlayer":
                warnMsg = "OOPS... Media Player Controls are NOT Supported by this Device!!!"
                break
            case "volumeControl":
                warnMsg = "OOPS... Volume Control is NOT Supported by this Device!!!"
                break
            case "bluetoothControl":
                warnMsg = "OOPS... Bluetooth Control is NOT Supported by this Device!!!"
                break
            case "alarms":
                warnMsg = "OOPS... Alarm Notification are NOT Supported by this Device!!!"
                break
            case "reminders":
                warnMsg = "OOPS... Reminders Notifications are NOT Supported by this Device!!!"
                break
            case "doNotDisturb":
                warnMsg = "OOPS... Do Not Disturb Control is NOT Supported by this Device!!!"
                break
            case "wakeWord":
                warnMsg = "OOPS... Alexa Wake Word Control is NOT Supported by this Device!!!"
                break
            case "amazonMusic":
                warnMsg = "OOPS... Amazon Music is NOT Supported by this Device!!!"
                break
            case "appleMusic":
                warnMsg = "OOPS... Apple Music is NOT Supported by this Device!!!"
                break
            case "tuneInRadio":
                warnMsg = "OOPS... Tune-In Radio is NOT Supported by this Device!!!"
                break
            case "iHeartRadio":
                warnMsg = "OOPS... iHeart Radio is NOT Supported by this Device!!!"
                break
            case "pandoraRadio":
                warnMsg = "OOPS... Pandora Radio is NOT Supported by this Device!!!"
                break
            case "siriusXm":
                warnMsg = "OOPS... Sirius XM Radio is NOT Supported by this Device!!!"
                break
            // case "tidal":
            //     warnMsg = "OOPS... Tidal Music is NOT Supported by this Device!!!"
            //     break
            case "spotify":
                warnMsg = "OOPS... Spotify is NOT Supported by this Device!!!"
                break
            case "flashBriefing":
                warnMsg = "OOPS... Flash Briefs are NOT Supported by this Device!!!"
                break
        }
        if(warnMsg && !noLogs) { log.warn warnMsg }
        return false
    }
}

Boolean permissionOk(type) {
    if(type && state?.permissions?.containsKey(type) && state?.permissions[type] == true) { return true }
    return false
}

void updateDeviceStatus(Map devData) {
    // try {
        Boolean isOnline = false
        if(devData?.size()) {
            isOnline = (devData?.online != false)
            // log.debug "isOnline: ${isOnline}"
            // log.debug "deviceFamily: ${devData?.deviceFamily} | deviceType: ${devData?.deviceType}"  // UNCOMMENT to identify unidentified devices

            // NOTE: These allow you to log all device data items
            // devData?.each { k,v ->
            //     if(!(k in ["playerState", "capabilities", "deviceAccountId"])) {
            //         log.debug("$k: $v")
            //     }
            // }
            // devData?.playerState?.each { k,v ->
            //     if(!(k in ["mainArt", "mediaId", "miniArt", "hint", "template", "upNextItems", "queueId", "miniInfoText", "provider"])) {
            //         logger("debug", "$k: $v")
            //     }
            // }
            state?.isSupportedDevice = (devData?.unsupported != true)
            state?.serialNumber = devData?.serialNumber
            state?.deviceType = devData?.deviceType
            state?.deviceOwnerCustomerId = devData?.deviceOwnerCustomerId
            state?.deviceAccountId = devData?.deviceAccountId
            state?.softwareVersion = devData?.softwareVersion
            state?.cookie = devData?.cookie
            state?.amazonDomain = devData?.amazonDomain
            state?.regionLocale = devData?.regionLocale
            Map permissions = state?.permissions ?: [:]
            devData?.permissionMap?.each {k,v -> permissions[k] = v }
            state?.permissions = permissions
            state?.hasClusterMembers = devData?.hasClusterMembers
            //log.trace "hasClusterMembers: ${ state?.hasClusterMembers}"
            // log.trace "permissions: ${state?.permissions}"

            if(isStateChange(device, "volumeSupported", (devData?.permissionMap?.volumeControl == true)?.toString())) {
                sendEvent(name: "volumeSupported", value: (devData?.permissionMap?.volumeControl == true), display: false, displayed: false)
            }
            if(isStateChange(device, "musicSupported", (devData?.permissionMap?.mediaPlayer == true)?.toString())) {
                sendEvent(name: "musicSupported", value: (devData?.permissionMap?.mediaPlayer == true), display: false, displayed: false)
            }
            if(isStateChange(device, "ttsSupported", (devData?.permissionMap?.TTS == true)?.toString())) {
                sendEvent(name: "ttsSupported", value: (devData?.permissionMap?.TTS == true), display: false, displayed: false)
            }
            if(isStateChange(device, "alarmSupported", (devData?.permissionMap?.alarms == true)?.toString())) {
                sendEvent(name: "alarmSupported", value: (devData?.permissionMap?.alarms == true), display: false, displayed: false)
            }
            if(isStateChange(device, "reminderSupported", (devData?.permissionMap?.reminders == true)?.toString())) {
                sendEvent(name: "reminderSupported", value: (devData?.permissionMap?.reminders == true), display: false, displayed: false)
            }
            state?.authValid = (devData?.authValid == true)
            Map deviceStyle = devData?.deviceStyle
            state?.deviceStyle = devData?.deviceStyle
            // logger("info", "deviceStyle (${devData?.deviceFamily}): ${devData?.deviceType} | Desc: ${deviceStyle?.name}")
            state?.deviceImage = deviceStyle?.image as String
            if(isStateChange(device, "deviceStyle", deviceStyle?.name?.toString())) {
                sendEvent(name: "deviceStyle", value: deviceStyle?.name?.toString(), descriptionText: "Device Style is ${deviceStyle?.name}", display: true, displayed: true)
            }

            String firmwareVer = devData?.softwareVersion ?: "Not Set"
            if(isStateChange(device, "firmwareVer", firmwareVer?.toString())) {
                sendEvent(name: "firmwareVer", value: firmwareVer?.toString(), descriptionText: "Firmware Version is ${firmwareVer}", display: true, displayed: true)
            }

            String devFamily = devData?.deviceFamily ?: ""
            if(isStateChange(device, "deviceFamily", devFamily?.toString())) {
                sendEvent(name: "deviceFamily", value: devFamily?.toString(), descriptionText: "Echo Device Family is ${devFamily}", display: true, displayed: true)
            }

            String devType = devData?.deviceType ?: ""
            if(isStateChange(device, "deviceType", devType?.toString())) {
                sendEvent(name: "deviceType", value: devType?.toString(), display: false, displayed: false)
            }

            Map musicProviders = devData?.musicProviders ?: [:]
            String lItems = musicProviders?.collect{ it?.value }?.sort()?.join(", ")
            if(isStateChange(device, "supportedMusic", lItems?.toString())) {
                sendEvent(name: "supportedMusic", value: lItems?.toString(), display: false, displayed: false)
            }
            if(isStateChange(device, "alexaMusicProviders", musicProviders?.toString())) {
                // log.trace "Alexa Music Providers Changed to ${musicProviders}"
                sendEvent(name: "alexaMusicProviders", value: musicProviders?.toString(), display: false, displayed: false)
            }
            if(isOnline) {
                refreshData()
            } else {
                sendEvent(name: "mute", value: "unmuted")
                sendEvent(name: "status", value: "stopped")
                sendEvent(name: "deviceStatus", value: "stopped_${state?.deviceStyle?.image}")
                sendEvent(name: "trackDescription", value: "")
            }
        }
        setOnlineStatus(isOnline)
        sendEvent(name: "lastUpdated", value: formatDt(new Date()), display: false , displayed: false)
        state?.fullRefreshOk = true
        schedDataRefresh()
    // } catch(ex) {
    //     log.error "updateDeviceStatus Error: ", ex
    // }
}

void refresh() {
    log.trace "refresh()"
    parent?.childInitiatedRefresh()
    // refreshData()
}

public updateLabel(String lbl) {
    if(lbl) {
        log.trace "Updating Device Label to (${lbl})"
        device?.setLlabel(lbl as String)
    }
}

private triggerDataRrsh(parentRefresh=false) {
    if(parentRefresh) {
        runIn(4, "refresh", [overwrite: true])
    } else { runIn(4, "refreshData", [overwrite: true]) }
}

private stateCleanup() {
    List items = [""]
    items?.each { si-> if(state?.containsKey(si as String)) { state?.remove(si)} }
}

public schedDataRefresh(frc) {
    if(frc || state?.refreshScheduled != true) {
        runEvery1Minute("refreshData")
        state?.refreshScheduled = true
    }
}

private refreshData() {
    logger("trace", "refreshData()...")
    if(device?.currentValue("onlineStatus") != "online") {
        logger("warn", "Skipping Device Data Refresh... Device is OFFLINE... (Offline Status Updated Every 10 Minutes)")
        return
    }
    if(!isAuthOk()) {return}
    logger("trace", "permissions: ${state?.permissions}")
    if(state?.permissions?.mediaPlayer == true) {
        getPlaybackState()
        getPlaylists()
    }

    if(state?.permissions?.doNotDisturb == true) { getDoNotDisturb() }
    if(state?.permissions?.wakeWord) {
        getWakeWord()
        getAvailableWakeWords()
    }
    if((state?.permissions?.alarms == true) || (state?.permissions?.reminders == true)) {
        if(state?.permissions?.alarms == true) { getAlarmVolume() }
        getNotifications()
    }
    if(state?.permissions?.bluetoothControl) {
        getBluetoothDevices()
    }
    getDeviceActivity()
}

public setOnlineStatus(Boolean isOnline) {
    String onlStatus = (isOnline ? "online" : "offline")
    if(isStateChange(device, "DeviceWatch-DeviceStatus", onlStatus?.toString()) || isStateChange(device, "onlineStatus", onlStatus?.toString())) {
        sendEvent(name: "DeviceWatch-DeviceStatus", value: onlStatus?.toString(), displayed: false, isStateChange: true)
        sendEvent(name: "onlineStatus", value: onlStatus?.toString(), displayed: true, isStateChange: true)
    }
}

private respIsValid(statusCode, Boolean hasErr, errMsg=null, String methodName, Boolean falseOnErr=false) {
    statusCode = statusCode as Integer
    if(statusCode == 401) {
        setAuthState(false)
        return false
    } else { if(statusCode > 401 && statusCode < 500) { log.error "${methodName} Error: ${errMsg ?: null}" } }
    if(hasErr && falseOnErr) { return false }
    return true
}

private getPlaybackState() {
    execAsyncCmd("get", "getPlaybackStateHandler", [
        uri: getAmazonUrl(),
        path: "/api/np/player",
        query: [ deviceSerialNumber: state?.serialNumber, deviceType: state?.deviceType, screenWidth: 2560 ],
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
    ], null)
}

def getPlaybackStateHandler(response, data, isGroupResponse=false) {
    Boolean hasErr = (response?.hasError() == true)
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    if(!respIsValid(response?.status, hasErr, errMsg, "getPlaybackStateHandler", true)) {return}
    try {} catch (ex) { }
    // log.debug "response: ${response?.getJson()}"
    def sData = [:]
    def isPlayStateChange = false
    sData = response?.getJson() ?: null
    sData = sData?.playerInfo ?: [:]
    if (state?.isGroupPlaying && !isGroupResponse) {
        log.debug "ignoring getPlaybackState because group is playing here"
        return
    }
    logger("trace", "getPlaybackState: ${sData}")
    String playState = sData?.state == 'PLAYING' ? "playing" : "stopped"
    String deviceStatus = "${playState}_${state?.deviceStyle?.image}"
    // log.debug "deviceStatus: ${deviceStatus}"
    if(isStateChange(device, "status", playState?.toString()) || isStateChange(device, "deviceStatus", deviceStatus?.toString())) {
        log.trace "Status Changed to ${playState}"
        isPlayStateChange = true
        if (isGroupResponse) {
            state?.isGroupPlaying = (sData?.state == 'PLAYING')
        }
        sendEvent(name: "status", value: playState?.toString(), descriptionText: "Player Status is ${playState}", display: true, displayed: true)
        sendEvent(name: "deviceStatus", value: deviceStatus?.toString(), display: false, displayed: false)
    }
    //Track Title
    String title = sData?.infoText?.title ?: ""
    if(isStateChange(device, "trackDescription", title?.toString())) {
        sendEvent(name: "trackDescription", value: title?.toString(), descriptionText: "Track Description is ${title}", display: true, displayed: true)
    }
    //Track Sub-Text2
    String subText1 = sData?.infoText?.subText1 ?: "Idle"
    if(isStateChange(device, "currentAlbum", subText1?.toString())) {
        sendEvent(name: "currentAlbum", value: subText1?.toString(), descriptionText: "Album is ${subText1}", display: true, displayed: true)
    }
    //Track Sub-Text2
    String subText2 = sData?.infoText?.subText2 ?: "Idle"
    if(isStateChange(device, "currentStation", subText2?.toString())) {
        sendEvent(name: "currentStation", value: subText2?.toString(), descriptionText: "Station is ${subText2}", display: true, displayed: true)
    }

    //Track Art Imager
    String trackImg = sData?.mainArt?.url ?: ""
    if(isStateChange(device, "trackImage", trackImg?.toString())) {
        sendEvent(name: "trackImage", value: trackImg?.toString(), descriptionText: "Track Image is ${trackImg}", display: false, displayed: false)
    }
    if(isStateChange(device, "trackImageHtml", """<img src="${trackImg?.toString()}"/>""")) {
        sendEvent(name: "trackImageHtml", value: """<img src="${trackImg?.toString()}"/>""", display: false, displayed: false)
    }

    // Group response data never has valida data for volume
    if(!isGroupResponse && sData?.volume) {
        if(sData?.volume?.volume != null) {
            Integer level = sData?.volume?.volume
            if(level < 0) { level = 0 }
            if(level > 100) { level = 100 }
            if(isStateChange(device, "level", level?.toString()) || isStateChange(device, "volume", level?.toString())) {
                log.trace "Volume Level Set to ${level}%"
                sendEvent(name: "level", value: level, display: false, displayed: false)
                sendEvent(name: "volume", value: level, display: false, displayed: false)
            }
        }
        if(sData?.volume?.muted != null) {
            String muteState = (sData?.volume?.muted == true) ? "muted" : "unmuted"
            if(isStateChange(device, "mute", muteState?.toString())) {
                log.trace "Mute Changed to ${muteState}"
                sendEvent(name: "mute", value: muteState, descriptionText: "Volume has been ${muteState}", display: true, displayed: true)
            }
        }
    }
    // Update cluster (unless we remain paused)
    if (state?.hasClusterMembers && (sData?.state == 'PLAYING' || isPlayStateChange)) {
        parent.sendPlaybackStateToClusterMembers(state?.serialNumber, response, data)
    }
}

private getAlarmVolume() {
    execAsyncCmd("get", "getAlarmVolumeHandler", [
        uri: getAmazonUrl(),
        path: "/api/device-notification-state/${state?.deviceType}/${device.currentValue("firmwareVer") as String}/${state.serialNumber}",
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
    ])
}

def getAlarmVolumeHandler(response, data) {
    Boolean hasErr = (response?.hasError() == true)
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    if(!respIsValid(response?.status, hasErr, errMsg, "getAlarmVolumeHandler")) {return}
    try {} catch (ex) { }
    def sData = response?.getJson() ?: null
    logger("trace", "getAlarmVolume: $sData")
    if(isStateChange(device, "alarmVolume", (sData?.volumeLevel ?: 0)?.toString())) {
        log.trace "Alarm Volume Changed to ${(sData?.volumeLevel ?: 0)}"
        sendEvent(name: "alarmVolume", value: (sData?.volumeLevel ?: 0), display: false, displayed: false)
    }
}

private getWakeWord() {
    execAsyncCmd("get", "getWakeWordHandler", [
        uri: getAmazonUrl(),
        path: "/api/wake-word",
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
    ])
}

def getWakeWordHandler(response, data) {
    Boolean hasErr = (response?.hasError() == true)
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    if(!respIsValid(response?.status, hasErr, errMsg, "getWakeWordHandler", true)) {return}
    try {} catch (ex) { }
    def sData = response?.getJson() ?: null
    // log.debug "sData: $sData"
    def wakeWord = sData?.wakeWords?.find { it?.deviceSerialNumber == state?.serialNumber } ?: null
    logger("trace", "getWakeWord: ${wakeWord?.wakeWord}")
    if(isStateChange(device, "alexaWakeWord", wakeWord?.wakeWord?.toString())) {
        log.trace "Wake Word Changed to ${(wakeWord?.wakeWord)}"
        sendEvent(name: "alexaWakeWord", value: wakeWord?.wakeWord, display: false, displayed: false)
    }
}

private getAvailableWakeWords() {
    execAsyncCmd("get", "getAvailableWakeWordsHandler", [
        uri: getAmazonUrl(),
        path: "/api/wake-words-locale",
        query: [
            deviceSerialNumber: state?.serialNumber,
            deviceType: state?.deviceType,
            softwareVersion: device.currentValue('firmwareVer')
        ],
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
    ])
}

def getAvailableWakeWordsHandler(response, data) {
    Boolean hasErr = (response?.hasError() == true)
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    if(!respIsValid(response?.status, hasErr, errMsg, "getAvailableWakeWordsHandler", true)) {return}
    try {} catch (ex) { }
    def sData = response?.getJson() ?: null
    def wakeWords = sData?.wakeWords ?: []
    logger("trace", "getAvailableWakeWords: ${wakeWords}")
    if(isStateChange(device, "wakeWords", wakeWords?.toString())) {
        sendEvent(name: "wakeWords", value: wakeWords, display: false, displayed: false)
    }
}

private getBluetoothDevices() {
    execAsyncCmd("get", "getBluetoothHandler", [
        uri: getAmazonUrl(),
        path: "/api/bluetooth",
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json"
    ])
}

def getBluetoothHandler(response, data) {
    Boolean hasErr = (response?.hasError() == true)
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    if(!respIsValid(response?.status, hasErr, errMsg, "getBluetoothHandler")) {return}
    try {} catch (ex) { }
    String curConnName = null
    Map btObjs = [:]
    def rData = response?.getJson() ?: null
    def bluData = rData?.bluetoothStates?.size() ? rData?.bluetoothStates?.find { it?.deviceSerialNumber == state?.serialNumber } : null
    if(bluData?.size() && bluData?.pairedDeviceList && bluData?.pairedDeviceList?.size()) {
        def bData = bluData?.pairedDeviceList?.findAll { (it?.deviceClass != "GADGET") }
        bData?.each {
            btObjs[it?.address as String] = it
            if(it?.connected == true) { curConnName = it?.friendlyName as String }
        }
    }
    logger("debug", "Current Bluetooth Device: $curConnName | Bluetooth Objects: $btObjs")
    state?.bluetoothObjs = btObjs
    List pairedNames = btObjs?.collect { it?.value?.friendlyName as String } ?: []
    // if(!(device.currentValue("btDeviceConnected")?.toString()?.equals(curConnName?.toString()))) {
        // log.info "Bluetooth Device Connected: (${curConnName})"
        sendEvent(name: "btDeviceConnected", value: curConnName?.toString(), descriptionText: "Bluetooth Device Connected (${curConnName})", display: true, displayed: true)
    // }

    if(!(device.currentValue("btDevicesPaired")?.toString()?.equals(pairedNames?.toString()))) {
        log.info "Paired Bluetooth Devices: ${pairedNames}"
        sendEvent(name: "btDevicesPaired", value: pairedNames?.toString(), descriptionText: "Paired Bluetooth Devices: ${pairedNames}", display: true, displayed: true)
    }
}

private String getBtAddrByAddrOrName(String btNameOrAddr) {
    Map btObj = state?.bluetoothObjs
    String curBtAddr = btObj?.find { it?.value?.friendlyName == btNameOrAddr || it?.value?.address == btNameOrAddr }?.value?.address ?: null
    logger("debug", "curBtAddr: ${curBtAddr}")
    return curBtAddr
}

private getDoNotDisturb() {
    execAsyncCmd("get", "getDoNotDisturbHandler", [
        uri: getAmazonUrl(),
        path: "/api/dnd/device-status-list",
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
    ])
}

def getDoNotDisturbHandler(response, data) {
    Boolean hasErr = (response?.hasError() == true)
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    if(!respIsValid(response?.status, hasErr, errMsg, "getDoNotDisturbHandler")) {return}
    try {} catch (ex) { }
    def sData = response?.getJson()
    def dndData = sData?.doNotDisturbDeviceStatusList?.size() ? sData?.doNotDisturbDeviceStatusList?.find { it?.deviceSerialNumber == state?.serialNumber } : [:]
    logger("trace", "getDoNotDisturb: $dndData")
    state?.doNotDisturb = (dndData?.enabled == true)
    if(isStateChange(device, "doNotDisturb", (dndData?.enabled == true)?.toString())) {
        log.info "Do Not Disturb: (${(dndData?.enabled == true)})"
        sendEvent(name: "doNotDisturb", value: (dndData?.enabled == true)?.toString(), descriptionText: "Do Not Disturb Enabled ${(dndData?.enabled == true)}", display: true, displayed: true)
    }
}

private getPlaylists() {
    execAsyncCmd("get", "getPlaylistsHandler", [
        uri: getAmazonUrl(),
        path: "/api/cloudplayer/playlists",
        query: [
            deviceSerialNumber: state?.serialNumber,
            deviceType: state?.deviceType,
            mediaOwnerCustomerId: state?.deviceOwnerCustomerId,
            screenWidth: 2560
        ],
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
    ])
}

def getPlaylistsHandler(response, data) {
    Boolean hasErr = (response?.hasError() == true)
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    if(!respIsValid(response?.status, hasErr, errMsg, "getPlaylistsHandler")) {return}
    try {} catch (ex) { }
    def sData = response?.getJson() ?: [:]
    logger("trace", "getPlaylists: ${sData}")
    Map playlists = sData?.playlists ?: [:]
    if(isStateChange(device, "alexaPlaylists", playlists?.toString())) {
        log.trace "Alexa Playlists Changed to ${playlists}"
        sendEvent(name: "alexaPlaylists", value: playlists, display: false, displayed: false)
    }
}

private getNotifications() {
    execAsyncCmd("get", "getNotificationsHandler", [
        uri: getAmazonUrl(),
        path: "/api/notifications",
        query: [ cached: true ],
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json"
    ])
}

def getNotificationsHandler(response, data) {
    Boolean hasErr = (response?.hasError() == true)
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    if(!respIsValid(response?.status, hasErr, errMsg, "getNotificationsHandler")) {return}
    try {} catch (ex) { }
    List newList = []
    if(response?.status == 200) {
        def sData = response?.getJson() ?: null
        if(sData) {
            List items = sData?.notifications ? sData?.notifications?.findAll { it?.status == "ON" && it?.deviceSerialNumber == state?.serialNumber} : []
            items?.each { item->
                Map li = [:]
                item?.keySet().each { key-> if(key in ['id', 'reminderLabel', 'originalDate', 'originalTime', 'deviceSerialNumber', 'type', 'remainingDuration']) { li[key] = item[key] } }
                newList?.push(li)
            }
        }
        if(isStateChange(device, "alexaNotifications", newList?.toString())) {
            sendEvent(name: "alexaNotifications", value: newList, display: false, displayed: false)
        }
    }
    // log.trace "notifications: $newList"
}

private getDeviceActivity() {
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/activities",
        query: [
            startTime:"",
            size:"50",
            offset:"-1"
        ],
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json"
    ]
    execAsyncCmd("GET", "deviceActivityHandler", params)
}

def deviceActivityHandler(response, data) {
    Boolean hasErr = (response?.hasError() == true)
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    if(!respIsValid(response?.status, hasErr, errMsg, "deviceActivityHandler")) {return}
    def sData = response?.getJson() ?: null
    Boolean wasLastDevice = false
    def actTS = null
    if (sData && sData?.activities != null) {
        def lastCommand = sData?.activities?.find {
            (it?.domainAttributes == null || it?.domainAttributes.startsWith("{")) &&
            it?.activityStatus.equals("SUCCESS") &&
            it?.utteranceId?.startsWith(it?.sourceDeviceIds?.deviceType)
        }
        if (lastCommand) {
            def lastDescription = new JsonSlurper().parseText(lastCommand?.description)
            def spokenText = lastDescription?.summary
            def lastDevice = lastCommand?.sourceDeviceIds?.get(0)
            if(lastDevice?.serialNumber == state?.serialNumber) {
                wasLastDevice = true
                if(isStateChange(device, "lastVoiceActivity", spokenText?.toString())) {
                    sendEvent(name: "lastVoiceActivity", value: spokenText?.toString(), display: false, displayed: false)
                }
                if(isStateChange(device, "lastSpokenToTime", lastCommand?.creationTimestamp?.toString())) {
                    sendEvent(name: "lastSpokenToTime", value: lastCommand?.creationTimestamp, display: false, displayed: false)
                }
            }
        }
        if(isStateChange(device, "wasLastSpokenToDevice", wasLastDevice?.toString())) {
            sendEvent(name: "wasLastSpokenToDevice", value: wasLastDevice, display: false, displayed: false)
        }
    }
}

private getTodaysWeather(String zipCode) {
    def curForecast = getTwcForecast()
    log.debug "curForecast: ${curForecast?.narrative?.get(0)}"
}

String getCookieVal() { return (state?.cookie && state?.cookie?.cookie) ? state?.cookie?.cookie as String : null }
String getCsrfVal() { return (state?.cookie && state?.cookie?.csrf) ? state?.cookie?.csrf as String : null }

/*******************************************************************
            Amazon Command Logic
*******************************************************************/

private sendAmazonBasicCommand(String cmdType) {
    execAsyncCmd("post", "amazonCommandResp", [
        uri: getAmazonUrl(),
        path: "/api/np/command",
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        query: [
            deviceSerialNumber: state?.serialNumber,
            deviceType: state?.deviceType
        ],
        requestContentType: "application/json",
        contentType: "application/json",
        body: [type: cmdType]
    ], [cmdDesc: cmdType])
}

private execAsyncCmd(String method, String callbackHandler, Map params, Map otherData = null) {
    if(method && callbackHandler && params) {
        String m = method?.toString()?.toLowerCase()
        if(isST()) {
            include 'asynchttp_v1'
            asynchttp_v1."${m}"(callbackHandler, params, otherData)
        } else { "asynchttp${m?.capitalize()}"("${callbackHandler}", params, otherData) }
    }
}

private sendAmazonCommand(String method, Map params, Map otherData=null) {
    execAsyncCmd(method, "amazonCommandResp", params, otherData)
}

def amazonCommandResp(response, data) {
    Boolean hasErr = (response?.hasError() == true)
    if(hasErr) log.debug "hasError: $hasErr | status: ${response?.status}"
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    if(!respIsValid(response?.status, hasErr, errMsg, "amazonCommandResp", true)) {return}
    try {} catch (ex) { }
    def resp = response?.data ? response?.getJson() : null
    if(response?.status == 200) {
        if (data?.cmdDesc?.startsWith("connectBluetooth") || data?.cmdDesc?.startsWith("disconnectBluetooth") || data?.cmdDesc?.startsWith("removeBluetooth")) {
            triggerDataRrsh()
        } else if(data?.cmdDesc?.startsWith("renameDevice")) { triggerDataRrsh(true) }
        log.trace "amazonCommandResp | Status: (${response?.status})${resp != null ? " | Response: ${resp}" : ""} | ${data?.cmdDesc} was Successfully Sent!!!"
    } else { logger("warn", "amazonCommandResp | Status: (${response?.status}) | Response: ${resp} | PassThru-Data: ${data}") }
}

private sendSequenceCommand(type, command, value) {
    // logger("trace", "sendSequenceCommand($type) | command: $command | value: $value")
    Map seqObj = sequenceBuilder(command, value)
    sendAmazonCommand("POST", [
        uri: getAmazonUrl(),
        path: "/api/behaviors/preview",
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
        body: new JsonOutput().toJson(seqObj)
    ], [cmdDesc: "SequenceCommand (${type})"])
}

private sendMultiSequenceCommand(commands, String srcDesc, Boolean parallel=false) {
    String seqType = parallel ? "ParallelNode" : "SerialNode"
    List nodeList = []
    commands?.each { cmdItem->
        if(cmdItem?.command instanceof Map) {
            nodeList?.push(cmdItem?.command)
        } else { nodeList?.push(createSequenceNode(cmdItem?.command, cmdItem?.value)) }
     }
    Map seqJson = [ "sequence": [ "@type": "com.amazon.alexa.behaviors.model.Sequence", "startNode": [ "@type": "com.amazon.alexa.behaviors.model.${seqType}", "name": null, "nodesToExecute": nodeList ] ] ]
    sendSequenceCommand("${srcDesc} | MultiSequence: ${parallel ? "Parallel" : "Sequential"}", seqJson, null)
}

def searchTest() {
    searchMusic("thriller", "AMAZON_MUSIC")
}
/*******************************************************************
            Device Command FUNCTIONS
*******************************************************************/

def play() {
    logger("trace", "play() command received...")
    if(isCommandTypeAllowed("mediaPlayer")) {
        sendAmazonBasicCommand("PlayCommand")
        incrementCntByKey("use_cnt_playCmd")
        if(isStateChange(device, "status", "playing")) {
            sendEvent(name: "status", value: "playing", descriptionText: "Player Status is playing", display: true, displayed: true)
        }
        triggerDataRrsh()
    }
}

def playTrack(track) {
    // if(isCommandTypeAllowed("mediaPlayer")) { }
    log.warn "Uh-Oh... The playTrack() Command is NOT Supported by this Device!!!"
}

def pause() {
    logger("trace", "pause() command received...")
    if(isCommandTypeAllowed("mediaPlayer")) {
        sendAmazonBasicCommand("PauseCommand")
        incrementCntByKey("use_cnt_pauseCmd")
        if(isStateChange(device, "status", "stopped")) {
            sendEvent(name: "status", value: "stopped", descriptionText: "Player Status is stopped", display: true, displayed: true)
        }
        triggerDataRrsh()
    }
}

def stop() {
    log.debug "stop..."
    logger("trace", "stop() command received...")
    if(isCommandTypeAllowed("mediaPlayer")) {
        sendAmazonBasicCommand("PauseCommand")
        incrementCntByKey("use_cnt_stopCmd")
        if(isStateChange(device, "status", "stopped")) {
            sendEvent(name: "status", value: "stopped", descriptionText: "Player Status is stopped", display: true, displayed: true)
        }
        triggerDataRrsh()
    }
}

def stopAllDevices() {
    doSequenceCmd("StopAllDevicesCommand", "stopalldevices")
    incrementCntByKey("use_cnt_stopAllDevices")
    triggerDataRrsh()
}

def previousTrack() {
    logger("trace", "previousTrack() command received...")
    if(isCommandTypeAllowed("mediaPlayer")) {
        sendAmazonBasicCommand("PreviousCommand")
        incrementCntByKey("use_cnt_prevTrackCmd")
        triggerDataRrsh()
    }
}

def nextTrack() {
    logger("trace", "nextTrack() command received...")
    if(isCommandTypeAllowed("mediaPlayer")) {
        sendAmazonBasicCommand("NextCommand")
        incrementCntByKey("use_cnt_nextTrackCmd")
        triggerDataRrsh()
    }
}

def mute() {
    logger("trace", "mute() command received...")
    if(isCommandTypeAllowed("volumeControl")) {
        state.muteLevel = device?.currentValue("level")?.toInteger()
        incrementCntByKey("use_cnt_muteCmd")
        if(isStateChange(device, "mute", "muted")) {
            sendEvent(name: "mute", value: "muted", descriptionText: "Mute is set to muted", display: true, displayed: true)
        }
        setLevel(0)
    }
}

def unmute() {
    logger("trace", "unmute() command received...")
    if(isCommandTypeAllowed("volumeControl")) {
        if(state?.muteLevel) {
            setLevel(state?.muteLevel)
            state?.muteLevel = null
            incrementCntByKey("use_cnt_unmuteCmd")
            if(isStateChange(device, "mute", "unmuted")) {
                sendEvent(name: "mute", value: "unmuted", descriptionText: "Mute is set to unmuted", display: true, displayed: true)
            }
        }
    }
}

def setMute(muteState) {
    if(muteState) { (muteState == "muted") ? mute() : unmute() }
}

def setLevel(Integer level) {
    logger("trace", "setVolume($level) command received...")
    if(isCommandTypeAllowed("volumeControl") && level>=0 && level<=100) {
        if(level != device?.currentValue('level')) {
            sendSequenceCommand("VolumeCommand", "volume", level)
            incrementCntByKey("use_cnt_volumeCmd")
            sendEvent(name: "level", value: level, display: false, displayed: false)
            sendEvent(name: "volume", value: level, display: false, displayed: false)
        }
    }
}

def setAlarmVolume(vol) {
    logger("trace", "setAlarmVolume($vol) command received...")
    if(isCommandTypeAllowed("alarms") && vol>=0 && vol<=100) {
        sendAmazonCommand("put", [
            uri: getAmazonUrl(),
            path: "/api/device-notification-state/${state?.deviceType}/${state?.softwareVersion}/${state?.serialNumber}",
            headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
            requestContentType: "application/json",
            contentType: "application/json",
            body: [
                deviceSerialNumber: state?.serialNumber,
                deviceType: state?.deviceType,
                softwareVersion: device?.currentValue('firmwareVer'),
                volumeLevel: vol
            ]
        ], [cmdDesc: "AlarmVolume"])
        incrementCntByKey("use_cnt_alarmVolumeCmd")
        sendEvent(name: "alarmVolume", value: vol, display: false, displayed: false)
    }
}

def setVolume(vol) {
    if(vol) { setLevel(vol?.toInteger()) }
}

def volumeUp() {
    def curVol = (device?.currentValue('level') ?: 1)
    if(curVol >= 0 && curVol < 100) { setVolume(curVol?.toInteger()+5) }
}

def volumeDown() {
    def curVol = (device?.currentValue('level') ?: 0)
    if(curVol > 0) { setVolume(curVol?.toInteger()-5) }
}

def setTrack(String uri, metaData="") {
    log.warn "Uh-Oh... The setTrack(uri: $uri, meta: $meta) Command is NOT Supported by this Device!!!"
}

def resumeTrack() {
    log.warn "Uh-Oh... The resumeTrack() Command is NOT Supported by this Device!!!"
}

def restoreTrack() {
    log.warn "Uh-Oh... The restoreTrack() Command is NOT Supported by this Device!!!"
}

def doNotDisturbOff() {
    setDoNotDisturb(false)
}

def doNotDisturbOn() {
    setDoNotDisturb(true)
}

def setDoNotDisturb(Boolean val) {
    logger("trace", "setDoNotDisturb($val) command received...")
    if(isCommandTypeAllowed("doNotDisturb")) {
        sendAmazonCommand("put", [
            uri: getAmazonUrl(),
            path: "/api/dnd/status",
            headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
            requestContentType: "application/json",
            contentType: "application/json",
            body: [
                deviceSerialNumber: state?.serialNumber,
                deviceType: state?.deviceType,
                enabled: (val==true)
            ]
        ], [cmdDesc: "SetDoNotDisturb${val ? "On" : "Off"}"])
        incrementCntByKey("use_cnt_dndCmd${val ? "On" : "Off"}")
    }
}

def deviceNotification(String msg) {
    logger("trace", "deviceNotification(msg: $msg) command received...")
    if(isCommandTypeAllowed("TTS")) {
        if(!msg) { log.warn "No Message sent with deviceNotification($msg) command"; return; }
        // log.trace "deviceNotification(${msg?.toString()?.length() > 200 ? msg?.take(200)?.trim() +"..." : msg})"
        incrementCntByKey("use_cnt_devNotif")
        speak(msg as String)
    }
}

def setVolumeAndSpeak(volume, String msg) {
    logger("trace", "setVolumeAndSpeak(volume: $volume, msg: $msg) command received...")
    if(volume != null && permissionOk("volumeControl")) {
        state?.useThisVolume = volume
        sendEvent(name: "level", value: volume?.toInteger(), display: false, displayed: false)
        sendEvent(name: "volume", value: volume?.toInteger(), display: false, displayed: false)
    }
    incrementCntByKey("use_cnt_setVolSpeak")
    speak(msg)
}

def setVolumeSpeakAndRestore(volume, String msg, restVolume=null) {
    logger("trace", "setVolumeSpeakAndRestore(volume: $volume, msg: $msg, restVolume) command received...")
    if(msg) {
        if(volume != null && permissionOk("volumeControl")) {
            state?.useThisVolume = volume?.toInteger()
            if(restVolume != null) {
                state?.lastVolume = restVolume as Integer
            } else { storeCurrentVolume() }
            sendEvent(name: "level", value: volume?.toInteger(), display: false, displayed: false)
            sendEvent(name: "volume", value: volume?.toInteger(), display: false, displayed: false)
            incrementCntByKey("use_cnt_setVolumeSpeakRestore")
        }
        speak(msg)
    }
}

def storeCurrentVolume() {
    Integer curVol = device?.currentValue("level") ?: 1
    log.trace "storeCurrentVolume(${curVol}) command received..."
    if(curVol != null) { state?.lastVolume = curVol as Integer }
}

private restoreLastVolume() {
    log.trace "restoreLastVolume(${state?.lastVolume}) command received..."
    if(state?.lastVolume && permissionOk("volumeControl")) {
        setVolume(state?.lastVolume as Integer)
        sendEvent(name: "level", value: state?.lastVolume, display: false, displayed: false)
        sendEvent(name: "volume", value: state?.lastVolume, display: false, displayed: false)
    } else { log.warn "Unable to restore Last Volume!!! lastVolume State Value not found..." }
}

def sayWelcomeHome(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "cannedtts_random", value: "iamhome"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "sayWelcomeHome")
    } else { doSequenceCmd("sayWelcomeHome", "cannedtts_random", "iamhome") }
    incrementCntByKey("use_cnt_sayWelcomeHome")
}

def sayCompliment(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "cannedtts_random", value: "compliments"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "sayCompliment")
    } else { doSequenceCmd("sayCompliment", "cannedtts_random", "compliments") }
    incrementCntByKey("use_cnt_sayCompliment")
}

def sayBirthday(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "cannedtts_random", value: "birthday"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "sayBirthday")
    } else { doSequenceCmd("sayBirthday", "cannedtts_random", "birthday") }
    incrementCntByKey("use_cnt_sayBirthday")
}

def sayGoodNight(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "cannedtts_random", value: "goodnight"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "sayGoodNight")
    } else { doSequenceCmd("sayGoodNight", "cannedtts_random", "goodnight") }
    incrementCntByKey("use_cnt_sayGoodNight")
}

def sayGoodMorning(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "cannedtts_random", value: "goodmorning"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "sayGoodMorning")
    } else { doSequenceCmd("sayGoodMorning", "cannedtts_random", "goodmorning") }
    incrementCntByKey("use_cnt_sayGoodMorning")
}

def sayGoodbye(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "cannedtts_random", value: "goodbye"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "sayGoodbye")
    } else { doSequenceCmd("sayGoodbye", "cannedtts_random", "goodbye") }
    incrementCntByKey("use_cnt_sayGoodbye")
}

def executeRoutineId(String rId) {
    def execDt = now()
    logger("trace", "executeRoutineId($rId) command received...")
    if(!rId) { log.warn "No Routine ID sent with executeRoutineId($rId) command" }
    if(parent?.executeRoutineById(rId as String)) {
        log.debug "Executed Alexa Routine | Process Time: (${(now()-execDt)}ms) | RoutineId: ${rId}"
        incrementCntByKey("use_cnt_executeRoutine")
    }
}

def playWeather(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "weather"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playWeather")
    } else { doSequenceCmd("playWeather", "weather") }
    incrementCntByKey("use_cnt_playWeather")
}

def playTraffic(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "traffic"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playTraffic")
    } else { doSequenceCmd("playTraffic", "traffic") }
    incrementCntByKey("use_cnt_playTraffic")
}

def playSingASong(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "singasong"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playSingASong")
    } else { doSequenceCmd("playSingASong", "singasong") }
    incrementCntByKey("use_cnt_playSong")
}

def playFlashBrief(volume=null, restoreVolume=null) {
    if(isCommandTypeAllowed("flashBriefing")) {
        if(volume != null) {
            List seqs = [[command: "volume", value: volume], [command: "flashbriefing"]]
            if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
            sendMultiSequenceCommand(seqs, "playFlashBrief")
        } else { doSequenceCmd("playFlashBrief", "flashbriefing") }
        incrementCntByKey("use_cnt_playBrief")
    }
}

def playTellStory(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "tellstory"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playTellStory")
    } else { doSequenceCmd("playTellStory", "tellstory") }
    incrementCntByKey("use_cnt_playStory")
}

def playFunFact(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "funfact"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playFunFact")
    } else { doSequenceCmd("playFunFact", "funfact") }
    incrementCntByKey("use_cnt_funfact")
}

def playJoke(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "joke"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playJoke")
    } else { doSequenceCmd("playJoke", "joke") }
    incrementCntByKey("use_cnt_joke")
}

def playCalendarToday(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "calendartoday"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playCalendarToday")
    } else { doSequenceCmd("playCalendarToday", "calendartoday") }
    incrementCntByKey("use_cnt_calendarToday")
}

def playCalendarTomorrow(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "calendartomorrow"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playCalendarTomorrow")
    } else { doSequenceCmd("playCalendarTomorrow", "calendartomorrow") }
    incrementCntByKey("use_cnt_calendarTomorrow")
}

def playCalendarNext(volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "calendarnext"]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playCalendarNext")
    } else { doSequenceCmd("playCalendarNext", "calendarnext") }
    incrementCntByKey("use_cnt_calendarNext")
}

def playCannedRandomTts(String type, volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "cannedtts_random", value: type]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playCannedRandomTts($type)")
    } else { doSequenceCmd("playCannedRandomTts($type)", "cannedtts_random", type) }
    incrementCntByKey("use_cnt_playCannedRandomTTS")
}

def playAnnouncement(String msg, volume=null, restoreVolume=null) {
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "announcement", value: msg]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playAnnouncement")
    } else { doSequenceCmd("playAnnouncement", "announcement", msg) }
    incrementCntByKey("use_cnt_announcement")
}

def playAnnouncement(String msg, String title, volume=null, restoreVolume=null) {
    msg = "${title ? "${title}::" : ""}${msg}"
    if(volume != null) {
        List seqs = [[command: "volume", value: volume], [command: "announcement", value: msg]]
        if(restoreVolume != null) { seqs?.push([command: "volume", value: restoreVolume]) }
        sendMultiSequenceCommand(seqs, "playAnnouncement")
    } else { doSequenceCmd("playAnnouncement", "announcement", msg) }
    incrementCntByKey("use_cnt_announcement")
}

def playAnnouncementAll(String msg, String title=null) {
    msg = "${title ? "${title}::" : ""}${msg}"
    doSequenceCmd("AnnouncementAll", "announcementall", msg)
    incrementCntByKey("use_cnt_announcementAll")
}

def searchMusic(String searchPhrase, String providerId, volume=null, sleepSeconds=null) {
    // log.trace "searchMusic(${searchPhrase}, ${providerId})"
    if(isCommandTypeAllowed(getCommandTypeForProvider(providerId))) {
        doSearchMusicCmd(searchPhrase, providerId, volume, sleepSeconds)
    } else { log.warn "searchMusic not supported for ${providerId}" }
}

String getCommandTypeForProvider(String providerId) {
    def commandType = providerId
    switch (providerId) {
        case "AMAZON_MUSIC":
            commandType = "amazonMusic"
            break
        case "APPLE_MUSIC":
            commandType = "appleMusic"
            break
        case "TUNEIN":
            commandType = "tuneInRadio"
            break
        case "PANDORA":
            commandType = "pandoraRadio"
            break
        case "SIRIUSXM":
            commandType = "siriusXm"
            break
        case "SPOTIFY":
            commandType = "spotify"
            break
        // case "TIDAL":
        //     commandType = "tidal"
        //     break
        case "I_HEART_RADIO":
            commandType = "iHeartRadio"
            break
    }
    return commandType
}

def searchAmazonMusic(String searchPhrase, volume=null, sleepSeconds=null) {
    if(isCommandTypeAllowed("amazonMusic")) {
        doSearchMusicCmd(searchPhrase, "AMAZON_MUSIC", volume, sleepSeconds)
        incrementCntByKey("use_cnt_searchAmazonMusic")
    }
}

def searchAppleMusic(String searchPhrase, volume=null, sleepSeconds=null) {
    if(isCommandTypeAllowed("appleMusic")) {
        doSearchMusicCmd(searchPhrase, "APPLE_MUSIC", volume, sleepSeconds)
        incrementCntByKey("use_cnt_searchAppleMusic")
    }
}

def searchTuneIn(String searchPhrase, volume=null, sleepSeconds=null) {
    if(isCommandTypeAllowed("tuneInRadio")) {
        doSearchMusicCmd(searchPhrase, "TUNEIN", volume, sleepSeconds)
        incrementCntByKey("use_cnt_searchTuneInRadio")
    }
}

def searchPandora(String searchPhrase, volume=null, sleepSeconds=null) {
    if(isCommandTypeAllowed("pandoraRadio")) {
        doSearchMusicCmd(searchPhrase, "PANDORA", volume, sleepSeconds)
        incrementCntByKey("use_cnt_searchPandoraRadio")
    }
}

def searchSiriusXm(String searchPhrase, volume=null, sleepSeconds=null) {
    if(isCommandTypeAllowed("siriusXm")) {
        doSearchMusicCmd(searchPhrase, "SIRIUSXM", volume, sleepSeconds)
        incrementCntByKey("use_cnt_searchSiriusXmRadio")
    }
}

def searchSpotify(String searchPhrase, volume=null, sleepSeconds=null) {
    if(isCommandTypeAllowed("spotify")) {
        doSearchMusicCmd(searchPhrase, "SPOTIFY", volume, sleepSeconds)
        incrementCntByKey("use_cnt_searchSpotifyMusic")
    }
}

// def searchTidal(String searchPhrase, volume=null, sleepSeconds=null) {
//     if(isCommandTypeAllowed("tidal")) {
//         doSearchMusicCmd(searchPhrase, "TIDAL", volume, sleepSeconds)
//         incrementCntByKey("use_cnt_searchTidal")
//     }
// }

def searchIheart(String searchPhrase, volume=null, sleepSeconds=null) {
    if(isCommandTypeAllowed("iHeartRadio")) {
        doSearchMusicCmd(searchPhrase, "I_HEART_RADIO", volume, sleepSeconds)
        incrementCntByKey("use_cnt_searchIheartRadio")
    }
}

private doSequenceCmd(cmdType, seqCmd, seqVal="") {
    if(state?.serialNumber) {
        logger("debug", "Sending (${cmdType}) | Command: ${seqCmd} | Value: ${seqVal}")
        sendSequenceCommand(cmdType, seqCmd, seqVal)
    } else { log.warn "doSequenceCmd Error | You are missing one of the following... SerialNumber: ${state?.serialNumber}" }
}

private doSearchMusicCmd(searchPhrase, musicProvId, volume=null, sleepSeconds=null) {
    if(state?.serialNumber && searchPhrase && musicProvId) {
        playMusicProvider(searchPhrase, musicProvId, volume, sleepSeconds)
        // incrementCntByKey("use_cnt_searchMusic")
    } else { log.warn "doSearchMusicCmd Error | You are missing one of the following... SerialNumber: ${state?.serialNumber} | searchPhrase: ${searchPhrase} | musicProvider: ${musicProvId}" }
}

private Map validateMusicSearch(searchPhrase, providerId, sleepSeconds=null) {
    Map validObj = [
        type: "Alexa.Music.PlaySearchPhrase",
        operationPayload: [
            deviceType: state?.deviceType,
            deviceSerialNumber: state?.serialNumber,
            customerId: state?.deviceOwnerCustomerId,
            locale: (state?.regionLocale ?: "en-US"),
            musicProviderId: providerId,
            searchPhrase: searchPhrase
        ]
    ]
    if(sleepSeconds) { validObj?.operationPayload?.waitTimeInSeconds = sleepSeconds }
    validObj?.operationPayload = new JsonOutput().toJson(validObj?.operationPayload)
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/behaviors/operation/validate",
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
        body: new JsonOutput().toJson(validObj)
    ]
    Map result = null
    httpPost(params) { resp->
        Map rData = resp?.data ?: null
        if(resp?.status == 200) {
            if (rData?.result != "VALID") {
                log.error "Amazon the Music Search Request as Invalid | MusicProvider: [${providerId}] | Search Phrase: (${searchPhrase})"
                result = null
            } else { result = rData }
        } else { log.error "validateMusicSearch Request failed with status: (${resp?.status}) | MusicProvider: [${providerId}] | Search Phrase: (${searchPhrase})" }
    }
    return result
}

private getMusicSearchObj(String searchPhrase, String providerId, sleepSeconds=null) {
    if (searchPhrase == "") { log.error 'getMusicSearchObj Searchphrase empty'; return; }
    Map validObj = [type: "Alexa.Music.PlaySearchPhrase", "@type": "com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode"]
    Map validResp = validateMusicSearch(searchPhrase, providerId, sleepSeconds)
    if(validResp && validResp?.operationPayload) {
        validObj?.operationPayload = validResp?.operationPayload
    } else {
        log.error "Something went wrong with the Music Search | MusicProvider: [${providerId}] | Search Phrase: (${searchPhrase})"
        validObj = null
    }
    return validObj
}

private playMusicProvider(searchPhrase, providerId, volume=null, sleepSeconds=null) {
    logger("trace", "playMusicProvider() command received... | searchPhrase: $searchPhrase | providerId: $providerId | sleepSeconds: $sleepSeconds")
    Map validObj = getMusicSearchObj(searchPhrase, providerId, sleepSeconds)
    if(!validObj) { return }
    Map seqJson = ["@type": "com.amazon.alexa.behaviors.model.Sequence", "startNode": validObj]
        seqJson?.startNode["@type"] = "com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode"
    if(volume) {
        sendMultiSequenceCommand([ [command: "volume", value: volume], [command: validObj] ], "playMusicProvider(${providerId})", true)
    } else { sendSequenceCommand("playMusicProvider(${providerId})", seqJson, null) }
}

def setWakeWord(String newWord) {
    logger("trace", "setWakeWord($newWord) command received...")
    String oldWord = device?.currentValue('alexaWakeWord')
    def wwList = device?.currentValue('wakeWords') ?: []
    log.debug "newWord: $newWord | oldWord: $oldWord | wwList: $wwList (${wwList?.contains(newWord.toString()?.toUpperCase())})"
    if(oldWord && newWord && wwList && wwList?.contains(newWord.toString()?.toUpperCase())) {
        sendAmazonCommand("put", [
            uri: getAmazonUrl(),
            path: "/api/wake-word/${state?.serialNumber}",
            headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
            requestContentType: "application/json",
            contentType: "application/json",
            body: [
                active: true,
                deviceSerialNumber: state?.serialNumber,
                deviceType: state?.deviceType,
                displayName: oldWord,
                midFieldState: null,
                wakeWord: newWord
            ]
        ], [cmdDesc: "SetWakeWord(${newWord})"])
        incrementCntByKey("use_cnt_setWakeWord")
        sendEvent(name: "alexaWakeWord", value: newWord?.toString()?.toUpperCase(), display: true, displayed: true)
    } else { log.warn "setWakeWord is Missing a Required Parameter!!!" }
}

def createAlarm(String alarmLbl, String alarmDate, String alarmTime) {
    logger("trace", "createAlarm($alarmLbl, $alarmDate, $alarmTime) command received...")
    if(alarmLbl && alarmDate && alarmTime) {
        createNotification("Alarm", [
            cmdType: "CreateAlarm",
            label: alarmLbl?.toString()?.replaceAll(" ", ""),
            date: alarmDate,
            time: alarmTime,
            type: "Alarm"
        ])
        incrementCntByKey("use_cnt_createAlarm")
    } else { log.warn "createAlarm is Missing a Required Parameter!!!" }
}

def createReminder(String remLbl, String remDate, String remTime) {
    logger("trace", "createReminder($remLbl, $remDate, $remTime) command received...")
    if(isCommandTypeAllowed("alarms")) {
        if(remLbl && remDate && remTime) {
            createNotification("Reminder", [
                cmdType: "CreateReminder",
                label: remLbl?.toString(),//?.replaceAll(" ", ""),
                date: remDate,
                time: remTime,
                type: "Reminder"
            ])
            incrementCntByKey("use_cnt_createReminder")
        } else { log.warn "createReminder is Missing the Required (id) Parameter!!!" }
    }
}

def removeNotification(String id) {
    logger("trace", "removeNotification($id) command received...")
    if(isCommandTypeAllowed("alarms") || isCommandTypeAllowed("reminders", true)) {
        if(id) {
            sendAmazonCommand("delete", [
                uri: getAmazonUrl(),
                path: "/api/notifications/${id}",
                headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
                requestContentType: "application/json",
                contentType: "application/json",
                body: []
            ], [cmdDesc: "RemoveNotification"])
            incrementCntByKey("use_cnt_removeNotification")
        } else { log.warn "removeNotification is Missing the Required (id) Parameter!!!" }
    }
}

private createNotification(type, options) {
    def now = new Date()
    def createdDate = now.getTime()
    def addSeconds = new Date(createdDate + 1 * 60000);
    def alarmTime = type != "Timer" ? addSeconds.getTime() : 0
    log.debug "addSeconds: $addSeconds | alarmTime: $alarmTime"
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/notifications/create${type}",
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
        body: [
            type: type,
            status: "ON",
            alarmTime: alarmTime,
            originalTime: type != "Timer" ? "${options?.time}:00.000" : null,
            originalDate: type != "Timer" ? options?.date : null,
            timeZoneId: null,
            reminderIndex: null,
            sound: null,
            deviceSerialNumber: state?.serialNumber,
            deviceType: state?.deviceType,
            timeZoneId: null,
            recurringPattern: type != "Timer" ? '' : null,
            alarmLabel: type == "Alarm" ? options?.label : null,
            reminderLabel: type == "Reminder" ? options?.label : null,
            timerLabel: type == "Timer" ? options?.label : null,
            skillInfo: null,
            isSaveInFlight: type != "Timer" ? true : null,
            triggerTime: 0,
            id: "create${type}",
            isRecurring: false,
            createdDate: createdDate,
            remainingDuration: type != "Timer" ? 0 : options?.timerDuration
        ]
    ]
    sendAmazonCommand("put", params, [cmdDesc: "Create${type}"])
}

def renameDevice(newName) {
    logger("trace", "renameDevice($newName) command received...")
    if(!state?.deviceAccountId) { log.error "renameDevice Failed because deviceAccountId is not found..."; return; }
    sendAmazonCommand("put", [
        uri: getAmazonUrl(),
        path: "/api/devices-v2/device/${state?.serialNumber}",
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
        body: [
            serialNumber: state?.serialNumber,
            deviceType: state?.deviceType,
            deviceAccountId: state?.deviceAccountId,
            accountName: newName
        ]
    ], [cmdDesc: "renameDevice(${newName})"])
    incrementCntByKey("use_cnt_renameDevice")
}

def connectBluetooth(String btNameOrAddr) {
    logger("trace", "connectBluetooth(${btName}) command received...")
    if(isCommandTypeAllowed("bluetoothControl")) {
        String curBtAddr = getBtAddrByAddrOrName(btNameOrAddr as String)
        if(curBtAddr) {
            sendAmazonCommand("post", [
                uri: getAmazonUrl(),
                path: "/api/bluetooth/pair-sink/${state?.deviceType}/${state?.serialNumber}",
                headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
                requestContentType: "application/json",
                contentType: "application/json",
                body: [ bluetoothDeviceAddress: curBtAddr ]
            ], [cmdDesc: "connectBluetooth($btNameOrAddr)"])
            incrementCntByKey("use_cnt_connectBluetooth")
            sendEvent(name: "btDeviceConnected", value: btNameOrAddr, display: true, displayed: true)
        } else { log.error "ConnectBluetooth Error: Unable to find the connected bluetooth device address..." }
    }
}

def disconnectBluetooth() {
    logger("trace", "disconnectBluetooth() command received...")
    if(isCommandTypeAllowed("bluetoothControl")) {
        String curBtAddr = getBtAddrByAddrOrName(device?.currentValue("btDeviceConnected") as String)
        if(curBtAddr) {
            sendAmazonCommand("post", [
                uri: getAmazonUrl(),
                path: "/api/bluetooth/disconnect-sink/${state?.deviceType}/${state?.serialNumber}",
                headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
                requestContentType: "application/json",
                contentType: "application/json",
                body: [ bluetoothDeviceAddress: curBtAddr ]
            ], [cmdDesc: "disconnectBluetooth"])
            incrementCntByKey("use_cnt_disconnectBluetooth")
        } else { log.error "DisconnectBluetooth Error: Unable to find the connected bluetooth device address..." }
    }
}

def removeBluetooth(String btNameOrAddr) {
    logger("trace", "removeBluetooth(${btNameOrAddr}) command received...")
    if(isCommandTypeAllowed("bluetoothControl")) {
        String curBtAddr = getBtAddrByAddrOrName(btNameOrAddr)
        if(curBtAddr) {
            sendAmazonCommand("post", [
                uri: getAmazonUrl(),
                path: "/api/bluetooth/unpair-sink/${state?.deviceType}/${state?.serialNumber}",
                headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
                requestContentType: "application/json",
                contentType: "application/json",
                body: [ bluetoothDeviceAddress: curBtAddr, bluetoothDeviceClass: "OTHER" ]
            ], [cmdDesc: "removeBluetooth(${btNameOrAddr})"])
            incrementCntByKey("use_cnt_removeBluetooth")
        } else { log.error "RemoveBluetooth Error: Unable to find the connected bluetooth device address..." }
    }
}

def sendAlexaAppNotification(String text) {
    doSequenceCmd("AlexaAppNotification", "pushnotification", text)
    incrementCntByKey("use_cnt_alexaAppNotification")
}

def getRandomItem(items) {
    def list = new ArrayList<String>();
    items?.each { list?.add(it) }
    return list?.get(new Random().nextInt(list?.size()));
}

def replayText() {
    logger("trace", "replayText() command received...")
    String lastText = device?.currentValue("lastSpeakCmd")?.toString()
    if(lastText) { speak(lastText) } else { log.warn "Last Text was not found" }
}

def playText(String msg) {
	logger("trace", "playText(msg: $msg) command received...")
	speak(msg as String)
}

def playTrackAndResume(uri, duration, volume=null) {
    log.warn "Uh-Oh... The playTrackAndResume(uri: $uri, duration: $duration, volume: $volume) Command is NOT Supported by this Device!!!"
}

def playTextAndResume(text, volume=null) {
    logger("trace", "The playTextAndResume(text: $text, volume: $volume) command received...")
    def restVolume = device?.currentValue("level")?.toInteger()
	if (volume != null) {
		setVolumeSpeakAndRestore(volume as Integer, text as String, restVolume as Integer)
    } else { speak(text as String) }
}

def playTrackAndRestore(uri, duration, volume=null) {
    log.warn "Uh-Oh... The playTrackAndRestore(uri: $uri, duration: $duration, volume: $volume) Command is NOT Supported by this Device!!!"
}

def playTextAndRestore(text, volume=null) {
    logger("trace", "The playTextAndRestore(text: $text, volume: $volume) command received...")
    def restVolume = device?.currentValue("level")?.toInteger()
	if (volume != null) {
		setVolumeSpeakAndRestore(volume as Integer, text as String, restVolume as Integer)
    } else { speak(text as String) }
}

def playURL(theURL) {
	log.warn "Uh-Oh... The playUrl(url: $theURL) Command is NOT Supported by this Device!!!"
}

def playSoundAndTrack(soundUri, duration, trackData, volume=null) {
    log.warn "Uh-Oh... The playSoundAndTrack(soundUri: $soundUri, duration: $duration, trackData: $trackData, volume: $volume) Command is NOT Supported by this Device!!!"
}

def speechTest(ttsMsg) {
    // log.trace "speechTest"
    List items = [
        "Testing Testing 1, 2, 3",
        "Yay!, I'm Alive... Hopefully you can hear me speaking?",
        "Everybody have fun tonight. Everybody have fun tonight. Everybody Wang Chung tonight. Everybody have fun.",
        "Being able to make me say whatever you want is the coolest thing since sliced bread!",
        "I said a hip hop, Hippie to the hippie, The hip, hip a hop, and you don't stop, a rock it out, Bubba to the bang bang boogie, boobie to the boogie To the rhythm of the boogie the beat, Now, what you hear is not a test, I'm rappin' to the beat",
        "This is how we do it!. It's Friday night, and I feel alright. The party is here on the West side. So I reach for my 40 and I turn it up. Designated driver take the keys to my truck, Hit the shore 'cause I'm faded, Honeys in the street say, Monty, yo we made it!. It feels so good in my hood tonight, The summertime skirts and the guys in Khannye.",
        "Teenage Mutant Ninja Turtles, Teenage Mutant Ninja Turtles, Teenage Mutant Ninja Turtles, Heroes in a half-shell Turtle power!... They're the world's most fearsome fighting team (We're really hip!), They're heroes in a half-shell and they're green (Hey - get a grip!), When the evil Shredder attacks!!!, These Turtle boys don't cut him no slack!."
    ]
    if(!ttsMsg) { ttsMsg = getRandomItem(items) }
    speak(ttsMsg as String)
}

def speak(String msg) {
    logger("trace", "speak() command received...")
    if(isCommandTypeAllowed("TTS")) {
        if(!msg) { log.warn "No Message sent with speak($msg) command" }
        // msg = cleanString(msg, true)
        speakVolumeCmd([cmdDesc: "SpeakCommand", message: msg, newVolume: (state?.useThisVolume ?: null), oldVolume: (state?.lastVolume ?: null), cmdDt: now()])
        incrementCntByKey("use_cnt_speak")
    }
}

String cleanString(str, frcTrans=false) {
    if(!str) { return null }
    //Cleans up characters from message
    str?.replaceAll(~/[^a-zA-Z0-9-?%°., ]+/, "")?.replaceAll(/\s\s+/, " ")
    str = textTransform(str, frcTrans)
    // log.debug "cleanString: $str"
    return str
}

private String textTransform(String str, force=false) {
    if(!force && settings?.disableTextTransform == true) { return str; }
    // Converts F temp values to readable text "19F"
    str = str?.replaceAll(/([+-]?\d+)\s?([CcFf])/) { return "${it[0]?.toString()?.replaceAll("[-]", "minus ")?.replaceAll("[FfCc]", " degrees")}" }
    str = str?.replaceAll(/(\sWSW\s)/, " west southwest ")?.replaceAll(/(\sWNW\s)/, " west northwest ")?.replaceAll(/(\sESE\s)/, " east southeast ")?.replaceAll(/(\sENE\s)/, " east northeast ")
    str = str?.replaceAll(/(\sSSE\s)/, " south southeast ")?.replaceAll(/(\sSSW\s)/, " south southwest ")?.replaceAll(/(\sNNE\s)/, " north northeast ")?.replaceAll(/(\sNNW\s)/, " north northwest ")
    str = str?.replaceAll(/(\sNW\s)/, " northwest ")?.replaceAll(/(\sNE\s)/, " northeast ")?.replaceAll(/(\sSW\s)/, " southwest ")?.replaceAll(/(\sSE\s)/, " southeast ")
    str = str?.replaceAll(/(\sE\s)/," east ")?.replaceAll(/(\sS\s)/," south ")?.replaceAll(/(\sN\s)/," north ")?.replaceAll(/(\sW\s)/," west ")
    str = str?.replaceAll("%"," percent ")
    str = str?.replaceAll("°"," degree ")
    return str
}

Integer getStringLen(str) { return (str && str?.toString()?.length()) ? str?.toString()?.length() : 0 }

private List msgSeqBuilder(String str) {
    // log.debug "msgSeqBuilder: $str"
    List seqCmds = []
    List strArr = []
    if(str?.toString()?.length() < 450) {
        seqCmds?.push([command: "speak", value: str as String])
    } else {
        List msgItems = str?.split()
        msgItems?.each { wd->
            if((getStringLen(strArr?.join(" ")) + wd?.length()) <= 430) {
                // log.debug "CurArrLen: ${(getStringLen(strArr?.join(" ")))} | CurStrLen: (${wd?.length()})"
                strArr?.push(wd as String)
            } else { seqCmds?.push([command: "speak", value: strArr?.join(" ")]); strArr = []; strArr?.push(wd as String); }
            if(wd == msgItems?.last()) { seqCmds?.push([command: "speak", value: strArr?.join(" ")]) }
        }
    }
    // log.debug "seqCmds: $seqCmds"
    return seqCmds
}

def sendTestAnnouncement() {
    playAnnouncement("Test announcement from device")
}

def sendTestAnnouncementAll() {
    playAnnouncementAll("Test announcement to all devices")
}

Map seqItemsAvail() {
    return [
        other: [
            "weather":null, "traffic":null, "flashbriefing":null, "goodmorning":null, "goodnight":null, "cleanup":null,
            "singasong":null, "tellstory":null, "funfact":null, "joke":null, "playsearch":null, "calendartoday":null,
            "calendartomorrow":null, "calendarnext":null, "stop":null, "stopalldevices":null,
            "dnd_duration": "2H30M", "dnd_time": "00:30", "dnd_all_duration": "2H30M", "dnd_all_time": "00:30",
            "dnd_duration":"2H30M", "dnd_time":"00:30",
            "cannedtts_random": ["goodbye", "confirmations", "goodmorning", "compliments", "birthday", "goodnight", "iamhome"],
            "wait": "value (seconds)", "volume": "value (0-100)", "speak": "message", "announcement": "message",
            "announcementall": "message", "pushnotification": "message"
        ],
        music: [
            "amazonmusic": "AMAZON_MUSIC", "applemusic": "APPLE_MUSIC", "iheartradio": "I_HEART_RADIO", "pandora": "PANDORA",
            "spotify": "SPOTIFY", "tunein": "TUNEIN", "cloudplayer": "CLOUDPLAYER"
        ],
        musicAlt: [
            "amazonmusic": "amazonMusic", "applemusic": "appleMusic", "iheartradio": "iHeartRadio", "pandora": "pandoraRadio",
            "spotify": "spotify", "tunein": "tuneInRadio", "cloudplayer": "cloudPlayer"
        ]
    ]
}

def executeSequenceCommand(String seqStr) {
    if(seqStr) {
        List seqList = seqStr?.split(",,")
        log.debug "seqList: ${seqList}"
        List seqItems = []
        if(seqList?.size()) {
            seqList?.each {
                def li = it?.toString()?.split("::")
                // log.debug "li: $li"
                if(li?.size()) {
                    String cmd = li[0]?.trim()?.toString()?.toLowerCase() as String
                    Boolean isValidCmd = (seqItemsAvail()?.other?.containsKey(cmd) || (seqItemsAvail()?.music?.containsKey(cmd)))
                    Boolean isMusicCmd = (seqItemsAvail()?.music?.containsKey(cmd) && !seqItemsAvail()?.other?.containsKey(cmd))
                    // log.debug "cmd: $cmd | isValidCmd: $isValidCmd | isMusicCmd: $isMusicCmd"

                    if(!isValidCmd) { log.warn "executeSequenceCommand command ($cmd) is not a valid sequence command!!!"; return; }

                    if(isMusicCmd) {
                        List valObj = (li[1]?.trim()?.toString()?.contains("::")) ? li[1]?.trim()?.split("::") : [li[1]?.trim() as String]
                        String provID = seqItemsAvail()?.music[cmd]
                        if(!isCommandTypeAllowed(seqItemsAvail()?.musicAlt[cmd])) { log.warn "Current Music Sequence command ($cmd) not allowed... "; return; }
                        if (!valObj || valObj[0] == "") { log.error 'Play Music Sequence it Searchphrase empty'; return; }
                        Map validObj = getMusicSearchObj(valObj[0], provID, valObj[1] ?: null)
                        if(!validObj) { return }
                        seqItems?.push([command: validObj])
                    } else {
                        if(li?.size() == 1) {
                            seqItems?.push([command: cmd])
                        } else if(li?.size() == 2) {
                            seqItems?.push([command: cmd, value: li[1]?.trim()])
                        }
                    }
                }
            }
        }
        logger("debug", "executeSequenceCommand Items: $seqItems | seqStr: ${seqStr}")
        if(seqItems?.size()) {
            sendMultiSequenceCommand(seqItems, "executeSequenceCommand")
            incrementCntByKey("use_cnt_executeSequenceCommand")
        }
    }
}

/*******************************************************************
            Speech Queue Logic
*******************************************************************/

Integer getRecheckDelay(Integer msgLen=null, addRandom=false) {
    def random = new Random()
    Integer randomInt = random?.nextInt(5) //Was using 7
    if(!msgLen) { return 30 }
    def v = (msgLen <= 14 ? 1 : (msgLen / 14)) as Integer
    // logger("trace", "getRecheckDelay($msgLen) | delay: $v + $randomInt")
    return addRandom ? (v + randomInt) : v
}

Integer getLastTtsCmdSec() { return !state?.lastTtsCmdDt ? 1000 : GetTimeDiffSeconds(state?.lastTtsCmdDt).toInteger() }
Integer getCmdExecutionSec(timeVal) { return !timeVal ? null : GetTimeDiffSeconds(timeVal).toInteger() }

private getQueueSize() {
    Map cmdQueue = state?.findAll { it?.key?.toString()?.startsWith("qItem_") }
    return (cmdQueue?.size() ?: 0)
}

private getQueueSizeStr() {
    Integer size = getQueueSize()
    return "($size) Item${size>1 || size==0 ? "s" : ""}"
}

private processLogItems(String logType, List logList, emptyStart=false, emptyEnd=true) {
    if(logType && logList?.size() && settings?.showLogs) {
        Integer maxStrLen = 0
        String endSep = "└─────────────────────────────"
        if(emptyEnd) { logger(logType, " ") }
        logger(logType, endSep)
        logList?.each { l->
            logger(logType, l)
        }
        if(emptyStart) { logger(logType, " ") }
    }
}

def resetQueue(showLog=true) {
    if(showLog) { log.trace "resetQueue()" }
    Map cmdQueue = state?.findAll { it?.key?.toString()?.startsWith("qItem_") }
    cmdQueue?.each { cmdKey, cmdData ->
        state?.remove(cmdKey)
    }
    unschedule("checkQueue")
    state?.qCmdCycleCnt = null
    state?.useThisVolume = null
    state?.loopChkCnt = null
    state?.speakingNow = false
    state?.cmdQueueWorking = false
    state?.firstCmdFlag = false
    state?.recheckScheduled = false
    state?.cmdQIndexNum = null
    state?.curMsgLen = null
    state?.lastTtsCmdDelay = null
    state?.lastTtsMsg = null
    state?.lastQueueMsg = null
}

Integer getQueueIndex() { return state?.cmdQIndexNum ? state?.cmdQIndexNum+1 : 1 }
String getAmazonDomain() { return state?.amazonDomain ?: parent?.settings?.amazonDomain }
String getAmazonUrl() {return "https://alexa.${getAmazonDomain()}"}
Map getQueueItems() { return state?.findAll { it?.key?.toString()?.startsWith("qItem_") } }

private schedQueueCheck(Integer delay, overwrite=true, data=null, src) {
    if(delay) {
        Map opts = [:]
        opts["overwrite"] = overwrite
        if(data) { opts["data"] = data }
        runIn(delay, "checkQueue", opts)
        state?.recheckScheduled = true
        // log.debug "Scheduled Queue Check for (${delay}sec) | Overwrite: (${overwrite}) | recheckScheduled: (${state?.recheckScheduled}) | Source: (${src})"
    }
}

public queueEchoCmd(type, headers, body=null, firstRun=false) {
    List logItems = []
    Map cmdItems = state?.findAll { it?.key?.toString()?.startsWith("qItem_") && it?.value?.type == type && it?.value?.headers && it?.value?.headers?.message == headers?.message }
    logItems?.push("│ Queue Active: (${state?.cmdQueueWorking}) | Recheck: (${state?.recheckScheduled}) ")
    if(cmdItems?.size()) {
        if(headers?.message) {
            Integer msgLen = headers?.message?.toString()?.length()
            logItems?.push("│ Message(${msgLen} char): ${headers?.message?.take(190)?.trim()}${msgLen > 190 ? "..." : ""}")
        }
        logItems?.push("│ Ignoring (${headers?.cmdType}) Command... It Already Exists in QUEUE!!!")
        logItems?.push("┌────────── Echo Queue Warning ──────────")
        processLogItems("warn", logItems, true, true)
        return
    }
    state?.cmdQIndexNum = getQueueIndex()
    state?."qItem_${state?.cmdQIndexNum}" = [type: type, headers: headers, body: body, newVolume: (headers?.newVolume ?: null), oldVolume: (headers?.oldVolume ?: null)]
    state?.useThisVolume = null
    state?.lastVolume = null
    if(headers?.volume) { logItems?.push("│ Volume (${headers?.volume})") }
    if(headers?.message) {
        logItems?.push("│ Message(Len: ${headers?.message?.toString()?.length()}): ${headers?.message?.take(200)?.trim()}${headers?.message?.toString()?.length() > 200 ? "..." : ""}")
    }
    if(headers?.cmdType) { logItems?.push("│ CmdType: (${headers?.cmdType})") }
    logItems?.push("┌───── Added Echo Queue Item (${state?.cmdQIndexNum}) ─────")
    if(!firstRun) {
        processLogItems("trace", logItems, false, true)
    }
}

private checkQueue(data) {
    // log.debug "checkQueue | ${data}"
    if(state?.qCmdCycleCnt && state?.qCmdCycleCnt?.toInteger() >= 10) {
        log.warn "checkQueue | Queue Cycle Count (${state?.qCmdCycleCnt}) is abnormally high... Resetting Queue"
        resetQueue(false)
        return
    }
    Boolean qEmpty = (getQueueSize() == 0)
    if(qEmpty) {
        log.trace "checkQueue | Nothing in the Queue... Performing Queue Reset"
        resetQueue(false)
        return
    }
    if(data && data?.rateLimited == true) {
        Integer delay = data?.delay as Integer ?: getRecheckDelay(state?.curMsgLen)
        // schedQueueCheck(delay, true, null, "checkQueue(rate-limit)")
        log.debug "checkQueue | Scheduling Queue Check for (${delay} sec) ${(data && data?.rateLimited == true) ? " | Recheck for RateLimiting: true" : ""}"
    }
    processCmdQueue()
    return
}

void processCmdQueue() {
    state?.cmdQueueWorking = true
    state?.qCmdCycleCnt = state?.qCmdCycleCnt ? state?.qCmdCycleCnt+1 : 1
    Map cmdQueue = getQueueItems()
    if(cmdQueue?.size()) {
        state?.recheckScheduled = false
        def cmdKey = cmdQueue?.keySet()?.sort()?.first()
        Map cmdData = state[cmdKey as String]
        // logger("debug", "processCmdQueue | Key: ${cmdKey} | Queue Items: (${getQueueItems()})")
        cmdData?.headers["queueKey"] = cmdKey
        Integer loopChkCnt = state?.loopChkCnt ?: 0
        if(state?.lastTtsMsg == cmdData?.headers?.message && (getLastTtsCmdSec() <= 10)) { state?.loopChkCnt = (loopChkCnt >= 1) ? loopChkCnt++ : 1 }
        // log.debug "loopChkCnt: ${state?.loopChkCnt}"
        if(state?.loopChkCnt && (state?.loopChkCnt > 4) && (getLastTtsCmdSec() <= 10)) {
            state?.remove(cmdKey as String)
            log.trace "processCmdQueue | Possible loop detected... Last message the same as current sent less than 10 seconds ago. This message will be removed from the queue"
            schedQueueCheck(2, true, null, "processCmdQueue(removed duplicate)")
        } else {
            state?.lastQueueMsg = cmdData?.headers?.message
            speakVolumeCmd(cmdData?.headers, true)
        }
    }
    state?.cmdQueueWorking = false
}

Integer getAdjCmdDelay(elap, reqDelay) {
    if(elap != null && reqDelay) {
        Integer res = (elap - reqDelay)?.abs()
        // log.debug "getAdjCmdDelay | reqDelay: $reqDelay | elap: $elap | res: ${res+3}"
        return res < 3 ? 3 : res+3
    }
    return 5
}

def testMultiCmd() {
    sendMultiSequenceCommand([[command: "volume", value: 60], [command: "speak", value: "super duper test message 1, 2, 3"], [command: "volume", value: 30]], "testMultiCmd")
}

private speakVolumeCmd(headers=[:], isQueueCmd=false) {
    log.debug "speakVolumeCmd($headers)..."
    // if(!isQueueCmd) { log.trace "speakVolumeCmd(${headers?.cmdDesc}, $isQueueCmd)" }
    def random = new Random()
    def randCmdId = random?.nextInt(300)

    Map queryMap = [:]
    List logItems = []
    String healthStatus = getHealthStatus()
    if(!headers || !(healthStatus in ["ACTIVE", "ONLINE"])) {
        if(!headers) { log.error "speakVolumeCmd | Error${!headers ? " | headers are missing" : ""} " }
        if(!(healthStatus in ["ACTIVE", "ONLINE"])) { log.warn "Command Ignored... Device is current in OFFLINE State" }
        return
    }
    Boolean isTTS = true
    // headers?.message = cleanString(headers?.message)
    Integer lastTtsCmdSec = getLastTtsCmdSec()
    Integer msgLen = headers?.message?.toString()?.length()
    Integer recheckDelay = getRecheckDelay(msgLen)
    headers["msgDelay"] = recheckDelay
    headers["cmdId"] = randCmdId
    if(!settings?.disableQueue) {
        logItems?.push("│ Last TTS Sent: (${lastTtsCmdSec} seconds) ")

        Boolean isFirstCmd = (state?.firstCmdFlag != true)
        if(isFirstCmd) {
            logItems?.push("│ First Command: (${isFirstCmd})")
            headers["queueKey"] = "qItem_1"
            state?.firstCmdFlag = true
        }
        Boolean sendToQueue = (isFirstCmd || (lastTtsCmdSec < 3) || (!isQueueCmd && state?.speakingNow == true))
        if(!isQueueCmd) { logItems?.push("│ SentToQueue: (${sendToQueue})") }
        // log.warn "speakVolumeCmd - QUEUE DEBUG | sendToQueue: (${sendToQueue?.toString()?.capitalize()}) | isQueueCmd: (${isQueueCmd?.toString()?.capitalize()})() | lastTtsCmdSec: [${lastTtsCmdSec}] | isFirstCmd: (${isFirstCmd?.toString()?.capitalize()}) | speakingNow: (${state?.speakingNow?.toString()?.capitalize()}) | RecheckDelay: [${recheckDelay}]"
        if(sendToQueue) {
            queueEchoCmd("Speak", headers, body, isFirstCmd)
            if(!isFirstCmd) { return }
        }
    }
    try {
        state?.speakingNow = true
        Map headerMap = [cookie: getCookieVal(), csrf: getCsrfVal()]
        headers?.each { k,v-> headerMap[k] = v }
        Integer qSize = getQueueSize()
        logItems?.push("│ Queue Items: (${qSize>=1 ? qSize-1 : 0}) │ Working: (${state?.cmdQueueWorking})")

        if(headers?.message) {
            state?.curMsgLen = msgLen
            state?.lastTtsCmdDelay = recheckDelay
            schedQueueCheck(recheckDelay, true, null, "speakVolumeCmd(sendCloudCommand)")
            logItems?.push("│ Rechecking: (${recheckDelay} seconds)")
            logItems?.push("│ Message(${msgLen} char): ${headers?.message?.take(190)?.trim()}${msgLen > 190 ? "..." : ""}")
            state?.lastTtsMsg = headers?.message
            // state?.lastTtsCmdDt = getDtNow()
        }
        if(headerMap?.oldVolume) {logItems?.push("│ Restore Volume: (${headerMap?.oldVolume}%)") }
        if(headerMap?.newVolume) {logItems?.push("│ New Volume: (${headerMap?.newVolume}%)") }
        logItems?.push("│ Current Volume: (${device?.currentValue("volume")}%)")
        logItems?.push("│ Command: (SpeakCommand)")
        try {
            def bodyObj = null
            List seqCmds = []
            if(headerMap?.newVolume) { seqCmds?.push([command: "volume", value: headerMap?.newVolume]) }
            seqCmds = seqCmds + msgSeqBuilder(headerMap?.message)
            if(headerMap?.oldVolume) { seqCmds?.push([command: "volume", value: headerMap?.oldVolume]) }
            bodyObj = new JsonOutput().toJson(multiSequenceBuilder(seqCmds))

            Map params = [
                uri: getAmazonUrl(),
                path: "/api/behaviors/preview",
                headers: headerMap,
                requestContentType: "application/json",
                contentType: "application/json",
                body: bodyObj
            ]
            execAsyncCmd("post", "asyncSpeechHandler", params, [
                cmdDt:(headerMap?.cmdDt ?: null), queueKey: (headerMap?.queueKey ?: null), cmdDesc: (headerMap?.cmdDesc ?: null), msgLen: msgLen, deviceId: device?.getDeviceNetworkId(), msgDelay: (headerMap?.msgDelay ?: null),
                message: (headerMap?.message ? (isST() && msgLen > 700 ? headerMap?.message?.take(700) : headerMap?.message) : null), newVolume: (headerMap?.newVolume ?: null), oldVolume: (headerMap?.oldVolume ?: null), cmdId: (headerMap?.cmdId ?: null)
            ])
        } catch (e) {
            log.error "something went wrong: ", e
            incrementCntByKey("err_cloud_command")
        }

        logItems?.push("┌─────── Echo Command ${isQueueCmd && !settings?.disableQueue ? " (From Queue) " : ""} ────────")
        processLogItems("debug", logItems)
    } catch (ex) {
        log.error "speakVolumeCmd Exception:", ex
        incrementCntByKey("err_cloud_command")
    }
}

def asyncSpeechHandler(response, data) {
    Boolean hasErr = (response?.hasError() == true)
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    try {} catch (ex) { }
    def resp = null
    data["amznReqId"] = response?.headers["x-amz-rid"] ?: null
    if(!respIsValid(response?.status, hasErr, errMsg, "asyncSpeechHandler", true)) {
    // if(!respIsValid(response, "asyncSpeechHandler", true)){
        resp = response?.errorJson ?: null
    } else { resp = response?.data ?: null }

    // log.trace "asyncSpeechHandler | Status: (${response?.status}) | Response: ${resp} | PassThru-Data: ${data}"
    postCmdProcess(resp, response?.status, data)
}

private postCmdProcess(resp, statusCode, data) {
    if(data && data?.deviceId && (data?.deviceId == device?.getDeviceNetworkId())) {
        if(statusCode == 200) {
            def execTime = data?.cmdDt ? (now()-data?.cmdDt) : 0
            // log.info "${data?.cmdDesc ? "${data?.cmdDesc}" : "Command"} Sent Successfully${data?.queueKey ? " | QueueKey: (${data?.queueKey})" : ""}${data?.msgDelay ? " | RecheckDelay: (${data?.msgDelay} sec)" : ""} | Execution Time (${execTime}ms)"
            log.info "${data?.cmdDesc ? "${data?.cmdDesc}" : "Command"} Sent Successfully${data?.msgLen ? " | Length: (${data?.msgLen}) " : ""}| Execution Time (${execTime}ms)${data?.msgDelay ? " | Recheck Wait: (${data?.msgDelay} sec)" : ""}${showLogs && data?.amznReqId ? " | Amazon Request ID: ${data?.amznReqId}" : ""}${data?.cmdId ? " | CmdID: (${data?.cmdId})" : ""}"
            if(data?.queueKey) { state?.remove(data?.queueKey as String) }
            if(data?.cmdDesc && data?.cmdDesc == "SpeakCommand" && data?.message) {
                state?.lastTtsCmdDt = getDtNow()
                String lastMsg = data?.message as String ?: "Nothing to Show Here..."
                sendEvent(name: "lastSpeakCmd", value: "${lastMsg}", descriptionText: "Last Speech text: ${lastMsg}", display: true, displayed: true)
                sendEvent(name: "lastCmdSentDt", value: "${state?.lastTtsCmdDt}", descriptionText: "Last Command Timestamp: ${state?.lastTtsCmdDt}", display: false, displayed: false)
                schedQueueCheck(getAdjCmdDelay(getLastTtsCmdSec(), data?.msgDelay), true, null, "postCmdProcess(adjDelay)")
            }
            return
        } else if(statusCode.toInteger() == 400 && resp?.message && resp?.message?.toString()?.toLowerCase() == "rate exceeded") {
            def random = new Random()
            Integer rDelay = 2//random?.nextInt(5)
            log.warn "You've been Rate-Limited by Amazon for sending Consectutive Commands to 5+ Device... | Device will retry again in ${rDelay} seconds"
            schedQueueCheck(rDelay, true, [rateLimited: true, delay: data?.msgDelay], "postCmdProcess(Rate-Limited)")
            return
        } else {
            log.error "postCmdProcess Error | status: ${statusCode} | message: ${resp?.message}"
            incrementCntByKey("err_cloud_commandPost")
            resetQueue()
            return
        }
    }
}

/*****************************************************
                HELPER FUNCTIONS
******************************************************/
String getAppImg(imgName) { return "https://raw.githubusercontent.com/tonesto7/echo-speaks/${isBeta() ? "beta" : "master"}/resources/icons/$imgName" }

def getDtNow() {
	def now = new Date()
	return formatDt(now, false)
}

def formatDt(dt, mdy = true) {
	def formatVal = mdy ? "MMM d, yyyy - h:mm:ss a" : "E MMM dd HH:mm:ss z yyyy"
	def tf = new SimpleDateFormat(formatVal)
	if(location?.timeZone) { tf.setTimeZone(location?.timeZone) }
	return tf.format(dt)
}

def GetTimeDiffSeconds(strtDate, stpDate=null) {
	if((strtDate && !stpDate) || (strtDate && stpDate)) {
		def now = new Date()
		def stopVal = stpDate ? stpDate.toString() : formatDt(now, false)
		def start = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate).getTime()
		def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal).getTime()
		def diff = (int) (long) (stop - start) / 1000
		return diff
	} else { return null }
}

def parseDt(dt, dtFmt) {
    return Date.parse(dtFmt, dt)
}

Boolean ok2Notify() {
    return (parent?.getOk2Notify())
}

private logger(type, msg) {
    if(type && msg && settings?.showLogs) {
        log."${type}" "${msg}"
    }
}

private incrementCntByKey(String key) {
	long evtCnt = state?."${key}" ?: 0
	// evtCnt = evtCnt?.toLong()+1
	evtCnt++
	// logger("trace", "${key?.toString()?.capitalize()}: $evtCnt")
	state?."${key}" = evtCnt?.toLong()
}

String getObjType(obj) {
	if(obj instanceof String) {return "String"}
	else if(obj instanceof GString) {return "GString"}
	else if(obj instanceof Map) {return "Map"}
    else if(obj instanceof LinkedHashMap) {return "LinkedHashMap"}
    else if(obj instanceof HashMap) {return "HashMap"}
	else if(obj instanceof List) {return "List"}
	else if(obj instanceof ArrayList) {return "ArrayList"}
	else if(obj instanceof Integer) {return "Integer"}
	else if(obj instanceof BigInteger) {return "BigInteger"}
	else if(obj instanceof Long) {return "Long"}
	else if(obj instanceof Boolean) {return "Boolean"}
	else if(obj instanceof BigDecimal) {return "BigDecimal"}
	else if(obj instanceof Float) {return "Float"}
	else if(obj instanceof Byte) {return "Byte"}
	else { return "unknown"}
}

public Map getDeviceMetrics() {
    Map out = [:]
    def cntItems = state?.findAll { it?.key?.startsWith("use_") }
    def errItems = state?.findAll { it?.key?.startsWith("err_") }
    if(cntItems?.size()) {
        out["usage"] = [:]
        cntItems?.each { k,v -> out?.usage[k?.toString()?.replace("use_", "") as String] = v as Integer ?: 0 }
    }
    if(errItems?.size()) {
        out["errors"] = [:]
        errItems?.each { k,v -> out?.errors[k?.toString()?.replace("err_", "") as String] = v as Integer ?: 0 }
    }
    return out
}

private getPlatform() {
    def p = "SmartThings"
    if(state?.hubPlatform == null) {
        try { [dummy: "dummyVal"]?.encodeAsJson(); } catch (e) { p = "Hubitat" }
        // if (location?.hubs[0]?.id?.toString()?.length() > 5) { p = "SmartThings" } else { p = "Hubitat" }
        state?.hubPlatform = p
        log.debug "hubPlatform: (${state?.hubPlatform})"
    }
    return state?.hubPlatform
}

Map sequenceBuilder(cmd, val) {
    def seqJson = null
    if (cmd instanceof Map) {
        seqJson = cmd?.sequence ?: cmd
    } else { seqJson = ["@type": "com.amazon.alexa.behaviors.model.Sequence", "startNode": createSequenceNode(cmd, val)] }
    Map seqObj = [behaviorId: (seqJson?.sequenceId ? cmd?.automationId : "PREVIEW"), sequenceJson: new JsonOutput().toJson(seqJson) as String, status: "ENABLED"]
    return seqObj
}

Map multiSequenceBuilder(commands, parallel=false) {
    String seqType = parallel ? "ParallelNode" : "SerialNode"
    List nodeList = []
    commands?.each { cmdItem-> nodeList?.push(createSequenceNode(cmdItem?.command, cmdItem?.value)) }
    Map seqJson = [ "sequence": [ "@type": "com.amazon.alexa.behaviors.model.Sequence", "startNode": [ "@type": "com.amazon.alexa.behaviors.model.${seqType}", "name": null, "nodesToExecute": nodeList ] ] ]
    Map seqObj = sequenceBuilder(seqJson, null)
    return seqObj
}

Map createSequenceNode(command, value) {
    try {
        Boolean remDevSpecifics = false
        Map seqNode = [
            "@type": "com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode",
            "operationPayload": [
                "deviceType": state?.deviceType,
                "deviceSerialNumber": state?.serialNumber,
                "locale": (state?.regionLocale ?: "en-US"),
                "customerId": state?.deviceOwnerCustomerId
            ]
        ]
        switch (command?.toString()?.toLowerCase()) {
            case "weather":
                seqNode?.type = "Alexa.Weather.Play"
                break
            case "traffic":
                seqNode?.type = "Alexa.Traffic.Play"
                break
            case "flashbriefing":
                seqNode?.type = "Alexa.FlashBriefing.Play"
                break
            case "goodmorning":
                seqNode?.type = "Alexa.GoodMorning.Play"
                break
            case "goodnight":
                seqNode?.type = "Alexa.GoodNight.Play"
                break
            case "cleanup":
                seqNode?.type = "Alexa.CleanUp.Play"
                break
            case "singasong":
                seqNode?.type = "Alexa.SingASong.Play"
                break
            case "tellstory":
                seqNode?.type = "Alexa.TellStory.Play"
                break
            case "funfact":
                seqNode?.type = "Alexa.FunFact.Play"
                break
            case "joke":
                seqNode?.type = "Alexa.Joke.Play"
                break
            case "calendartomorrow":
                seqNode?.type = "Alexa.Calendar.PlayTomorrow"
                break
            case "calendartoday":
                seqNode?.type = "Alexa.Calendar.PlayToday"
                break
            case "calendarnext":
                seqNode?.type = "Alexa.Calendar.PlayNext"
                break
            case "stop":
                seqNode?.type = "Alexa.DeviceControls.Stop"
                break
            case "stopalldevices":
                remDevSpecifics = true
                seqNode?.type = "Alexa.DeviceControls.Stop"
                seqNode?.operationPayload?.devices = [ [deviceType: "ALEXA_ALL_DEVICE_TYPE", deviceSerialNumber: "ALEXA_ALL_DSN"] ]
                seqNode?.operationPayload?.isAssociatedDevice = false
                break
            case "cannedtts_random":
                List okVals = ["goodbye", "confirmations", "goodmorning", "compliments", "birthday", "goodnight", "iamhome"]
                if(!(value in okVals)) { return null }
                seqNode?.type = "Alexa.CannedTts.Speak"
                seqNode?.operationPayload?.cannedTtsStringId = "alexa.cannedtts.speak.curatedtts-category-${value}/alexa.cannedtts.speak.curatedtts-random"
                break
            case "cannedtts":
                List okVals = ["goodbye", "confirmations", "goodmorning", "compliments", "birthday", "goodnight", "iamhome"]
                if(!(value in okVals)) { return null }
                seqNode?.type = "Alexa.CannedTts.Speak"
                List valObj = (value?.toString()?.contains("::")) ? value?.split("::") : [value as String, value as String]
                seqNode?.operationPayload?.cannedTtsStringId = "alexa.cannedtts.speak.curatedtts-category-${valObj[0]}/alexa.cannedtts.speak.curatedtts-${valObj[1]}"
                break
            case "wait":
                remDevSpecifics = true
                seqNode?.operationPayload?.remove('customerId')
                seqNode?.type = "Alexa.System.Wait"
                seqNode?.operationPayload?.waitTimeInSeconds = value?.toInteger() ?: 5
                break
            case "volume":
                seqNode?.type = "Alexa.DeviceControls.Volume"
                seqNode?.operationPayload?.value = value;
                break
            case "dnd_duration":
            case "dnd_time":
                remDevSpecifics = true
                seqNode?.type = "Alexa.DeviceControls.DoNotDisturb"
                seqNode?.operationPayload?.devices = [ [deviceAccountId: state?.deviceAccountId, deviceType: state?.deviceType, deviceSerialNumber: state?.serialNumber] ]
                seqNode?.operationPayload?.action = "Enable"
                if(command == "dnd_time") {
                    seqNode?.operationPayload?.until = "TIME#T${value}"
                } else { seqNode?.operationPayload?.duration = "DURATION#PT${value}" }
                seqNode?.operationPayload?.timeZoneId = "America/New_York" //location?.timeZone?.ID ?: null
                break
            case "dnd_all_duration":
            case "dnd_all_time":
                remDevSpecifics = true
                seqNode?.type = "Alexa.DeviceControls.DoNotDisturb"
                seqNode?.operationPayload?.devices = [ [deviceType: "ALEXA_ALL_DEVICE_TYPE", deviceSerialNumber: "ALEXA_ALL_DSN"] ]
                seqNode?.operationPayload?.action = "Enable"
                if(command == "dnd_all_time") {
                    seqNode?.operationPayload?.until = "TIME#T${value}"
                } else { seqNode?.operationPayload?.duration = "DURATION#PT${value}" }
                seqNode?.operationPayload?.timeZoneId = "America/New_York" //location?.timeZone?.ID ?: null
                break
            case "speak":
                seqNode?.type = "Alexa.Speak"
                value = cleanString(value as String)
                seqNode?.operationPayload?.textToSpeak = value as String
                break
            case "announcement":
            case "announcementall":
                remDevSpecifics = true
                seqNode?.type = "AlexaAnnouncement"
                seqNode?.operationPayload?.expireAfter = "PT5S"
                List valObj = (value?.toString()?.contains("::")) ? value?.split("::") : ["Echo Speaks", value as String]
                seqNode?.operationPayload?.content = [[ locale: (state?.regionLocale ?: "en-US"), display: [ title: valObj[0], body: valObj[1] as String ], speak: [ type: "text", value: valObj[1] as String ] ] ]
                seqNode?.operationPayload?.target = [ customerId : state?.deviceOwnerCustomerId ]
                if(command != "announcementall") { seqNode?.operationPayload?.target?.devices = [ [ deviceTypeId: state?.deviceType, deviceSerialNumber: state?.serialNumber ] ] }
                break
            case "pushnotification":
                remDevSpecifics = true
                seqNode?.type = "Alexa.Notifications.SendMobilePush"
                seqNode?.operationPayload?.notificationMessage = value as String
                seqNode?.operationPayload?.alexaUrl = "#v2/behaviors"
                seqNode?.operationPayload?.title = "Amazon Alexa"
                break
            default:
                return null
        }
        if(remDevSpecifics) {
            seqNode?.operationPayload?.remove('deviceType')
            seqNode?.operationPayload?.remove('deviceSerialNumber')
            seqNode?.operationPayload?.remove('locale')
        }
        // log.debug "seqNode: $seqNode"
        return seqNode
    } catch (ex) {
        log.error "createSequenceNode Exception: $ex"
        return [:]
    }
}