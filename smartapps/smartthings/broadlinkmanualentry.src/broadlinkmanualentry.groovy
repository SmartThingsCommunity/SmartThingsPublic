/**
 *  *
 *  Copyright 2016 Rebecca Onuschak
 *
 */
definition(
    name: "broadlinkmanualentry"  ,
    namespace: "smartthings",
    author: "BeckyR",
    description: "manual update of broadlink devices",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	    page(name: "page1", title: "Select Device", nextPage: "devicedata", install: false, uninstall: false){
         section("Select Broadlink Devices to Update:") {
			input(name:"somedevice", type: "device.broadlinkswitch", title: "Devices?", multiple: false)
	}}
    page(name: "devicedata", title: "Input device data", install: true, uninstall: true)
    
}

def devicedata(){
	dynamicPage(name: "devicedata") {
    somedevice.each{
        section("${somedevice.name}") {     	
            input(name: "BLURL", type: "string", title: "Broadlink URL",defaultValue: "${somedevice.currentValue('BLURL')}")
			input(name: "BLMac", type: "string", title: "Broadlink MAC Address",defaultValue: "${somedevice.currentValue('BLmac')}",required:false)
			input(name: "onCodeID", type: "string", title: "Code to turn on",defaultValue: "${somedevice.currentValue('onCodeID')}",required:false)
            input(name: "offCodeID", type: "string", title: "Code to turn off",defaultValue: "${somedevice.currentValue('offCodeID')}",required:false)
        }
         }}}


def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
	def d=settings.somedevice
	d.changedata("BLURL","${settings.BLURL}")
    d.changedata("BLmac","${settings.BLMac}")
    d.changedata("onCodeID","${settings.onCodeID}")
    d.changedata("offCodeID","${settings.offCodeID}")
    log.info "Device ${d.name} updated"
}