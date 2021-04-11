# Tasmota
Set of SmartThings device-handlers for devices running the [Sonoff-Tasmota](https://github.com/arendst/Sonoff-Tasmota) firmware

## How to use
You will need to install the 'Tasmota' device handler along with other device handlers for module-specific device handlers.

## Device Handlers
### Tasmota
This is device handler is required for all module types.  This device handler determines your Tasmota-device's module and spawns child SmartThings devices accordingly.

For Sonoff RF Bride modules, sixteen Button child devices will be created.

### Tasmota-Power
This device handler is used to define the devices that get spawned for switch-like devices.  It is used for the following device types (possibly/probably more):

For Sonoff Basic modules, a single Switch child devices will be created.

For Sonoff Dual modules, two Switch child devices will be created.

For Sonoff 4ch modules, four Switch child devices will be created

### Tasmota-RF-Bridge Button
This device handler is used to define the deives that get spawned for the Sonoff RF Bridge device.  Each instance of this device handler corresponds to one of the 16 commands that can be captured/sent by the Sonoff RF Bridge.

### Example installation for Sonoff Basic
In addition to the Tasmota device handler, the Tasmota-Power device handler will also need to be installed.

(This process is likely the same for many module types, but is not yet tested.)

#### Install the Tasmota Device Handler.
This device handler is necessary for all module types.  This device handler will make an HTTP call to the Tasmota device to determine the module type.  Depending on the module type defined, it will create child devices.
1. Log in to the SmartThings IDE (https://graph.api.smartthings.com/)
2. Go to `My Device Handlers`
3. Click `Create New Device Handler`
4. In the `From Code` tab paste in the code from https://github.com/BrettSheleski/SmartThingsPublic/blob/master/devicetypes/brettsheleski/tasmota.src/tasmota.groovy
5. Click `Create`
6. Click `Publish` --> `For Me`

#### Install the Tasmota-Power Device Handler
For Sonoff-Basic, Sonoff-4CH, and Sonoff Dual devices this is required
1. Log in to the SmartThings IDE (https://graph.api.smartthings.com/)
2. Go to `My Device Handlers`
3. Click `Create New Device Handler`
4. In the `From Code` tab paste in the code from https://github.com/BrettSheleski/SmartThingsPublic/blob/master/devicetypes/brettsheleski/tasmota-power.src/tasmota-power.groovy
5. Click `Create`
6. Click `Publish` --> `For Me`


#### Add your Device
1.  Log in to the SmartThings IDE (https://graph.api.smartthings.com/)
2.  Go to `My Devices`
3.  Click `New Device`
4.  Give your device a `Name`
5.  Enter a unique `Device Network ID` for your device
6.  In the `Type` dropdown, scroll to the bottom and select the `Tasmota` device.
7.  Choose your `Location`
8.  Choose your `Hub`
9.  Click `Create`
10. In the Edit Device page, click the Edit link in the Preferences area
11. Enter the `IP Address` of your Tasmota device (Required)
12.  Enter the `Username` your device uses (Optional)
13.  Enter the `Password` for your device (Optional)
14.  Click save

After adding and configuring the Tasmota device to your SmartThings account, you should be able to open the SmartThings app and you will see the device you created, as well as a device with the same name followed by the `Switch` suffix.  The `Switch` device will be the device you would normally interact with.


## SmartApp

Since Tasmota devices may also be triggered via external sources (not using the SmartThings app) the SmartThings device will need to know of device state-changes.

To accomplish this a SmartThings SmartApp is in development to expose an HTTP endpoint which can be called to update the state of the Tasmota device(s).

See here:  https://github.com/BrettSheleski/SmartThingsPublic/tree/master/smartapps/brettsheleski/tasmota.src

## Supported Devices
See the table below of devices this device handler should work with.



|Module |Status  | Tester(s) | Note   |
|---|---|---|---|
| Sonoff Basic   | Working   | [@BrettSheleski](https://github.com/BrettSheleski)   | |
| Sonoff RF | Should Work (Untested)   | | |
| Sonoff SV   | Should Work (Untested)   | | |
| Sonoff TH | | | |
| Sonoff Dual | | | |
| Sonoff 4CH | Working | [@Sym-Link](https://github.com/Sym-Link) | |
| S20 Socket | | | |
| Slampher | | | |
| Sonoff Touch | | | |
| Sonoff LED | | | |
| Sonoff 1 Channel | | | |
| 4CH | Working | [@Sym-Link](https://github.com/Sym-Link) | |
| Motor C/AC | | | |
| ElectroDragon | | | |
| EXS Relay | | | |
| WiOn | | | |
| Generic | | | |
| Sonoff Dev | | | |
| H801 | | | |
| Sonoff SC | | | |
| Sonoff BN-SZ | | | |
| Sonoff 4CH Pro | | | |
| Huafan SS | | | |
| Sonoff Bridge | Working | [@BrettSheleski](https://github.com/BrettSheleski) | |
| Sonoff B1 | | | |
| AiLight | | | |
| Sonoff T1 1CH | | | |
| Sonoff T1 2CH | Working | [@BornInThe50s](https://github.com/BornInThe50s) | |
| Sonoff T1 3CH | | | |
| Supla Espablo | | | |
| Witty Cloud | | | |
| Yunshan Relay | | | |
| MagicHome | | | |
| Luani HVIO | | | |
| KMC 70011 | | | |
| Arilux LC01 | | | |
| Arilux LC11 | | | |
| Sonoff Dual R2 | Working | [@Sym-Link](https://github.com/Sym-Link) | |
| Arilux LC06 | | | |
| Sonoff S31 | | | |
| Sonoff iFan02 | Working | [@BrettSheleski](https://github.com/BrettSheleski)  | |


## Supported GPIO

|GPIO Device |Status  | Tester(s) | Note   |
|---|---|---|---|
| None | | | |
| DHT11 | | | |
| AM2301 | | | |
| SI7021 | | | |
| DS18x20 | | | |
| I2C SCL | | | |
| I2C SDA | | | |
| WS2812 | | | |
| IRsend | | | |
| Switch1 | | | |
| Switch2 | | | |
| Switch3 | | | |
| Switch4 | | | |
| Button1 | | | |
| Button2 | | | |
| Button3 | | | |
| Button4 | | | |
| Relay1 | Working  | [@BrettSheleski](https://github.com/BrettSheleski) | |
| Relay2 | Should Work (Untested)  | | |
| Relay3 | Should Work (Untested)  | | |
| Relay4 | Should Work (Untested)  | | |
| Relay5 | Should Work (Untested)  | | |
| Relay6 | Should Work (Untested)  | | |
| Relay7 | Should Work (Untested) | | |
| Relay8 | Should Work (Untested)  | | |
| Relay1i | Working  | [@BrettSheleski](https://github.com/BrettSheleski) | |
| Relay2i | Should Work (Untested)  | | |
| Relay3i | Should Work (Untested)  | | |
| Relay4i | Should Work (Untested)  | | |
| Relay5i | Should Work (Untested)  | | |
| Relay6i | Should Work (Untested)  | | |
| Relay7i | Should Work (Untested)  | | |
| Relay8i | Should Work (Untested)  | | |
| PWM1 | | | |
| PWM2 | | | |
| PWM3 | | | |
| PWM4 | | | |
| PWM5 | | | |
| Counter1 | | | |
| Counter2 | | | |
| Counter3 | | | |
| Counter4 | | | |
| PWM1i | | | |
| PWM2i | | | |
| PWM3i | | | |
| PWM4i | | | |
| PWM5i | | | |
| IRrecv | | | |
| Led1 | | | |
| Led2 | | | |
| Led3 | | | |
| Led4 | | | |
| Led1i | | | |
| Led2i | | | |
| Led3i | | | |
| Led4i | | | |
| MHZ Tx | | | |
| MHZ Rx | | | |
| PZEM Tx | | | |
| PZEM Rx | | | |
| SAir Tx | | | |
| SAir Rx | | | |
| SPI CS | | | |
| SPI DC | | | |
| BkLight | | | |
| PMS5003 | | | |


# Future
The Tasmota device handler will also read the GPIO configuration of the Tasmota device and spawn additional child devices accordingly.

# Contribute
Since I do not have all the modules that the Sonoff-Tasmota firmware supports, I will not be able to test all the devices, nor will I be able to implement any necessary child-device-handlers that would make the implementation complete.

There is a Tasmota-Base device handler intended to be used as a starting point to create device handlers with.  Hopefully others will be able to use this to help further the project.  
