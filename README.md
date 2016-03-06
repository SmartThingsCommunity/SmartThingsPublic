# SmartThings Repo

My samsung smartthings modules. Modified alarmserver and control panel for DSC
alarm integration.

## Instructions

### Setup device types

Using the Smartthings IDE create the new device types using the code from the devicetypes directory.

There are 6 types of devices you can create:

* DSC Stay Panel  - (Shows partition status info and provides Stay switch that can be used in routines)
* DSC Away Panel  - (Shows partition status info and provides Away switch that can be used in routines)
* DSC Zone Contact - (contact device open/close)
* DSC Zone Motion  - (motion device active/inactive)
* DSC Zone Smoke 4w  - (4-wire smoke device, alarm/clear/test. It's nearly the same as motion or contact, as it's attached to a zone.)
* DSC Zone Smoke 2w  - (2-wire smoke device, alarm/clear, UNTESTED, Probably doesn't work right now and I have no way to test it. 2 wire devices create their own alarm type that isn't attached to a zone.)


At a minimum you'll probably want the Stay/Away panels, Contact, and Motion.

In the Web IDE for Smartthings create a new device type for each of the above devices and paste in the code for each device from the corresponding groovy files in the repo.

For all the device types make sure you save them and then publish them for yourself.

### Create panel devices

Create a new device and choose the type of "DSC Panel", "DSC Stay Panel" or "DSC Away Panel" that you published earlier. The network id needs to be **dscpanel1**, **dscstay1** or **dscaway1** depending on the panel type. Be sure your hub is selected as well. Once the devices are created - edit the configuration via smartthings app to setup the IP and port of your alarmserver.

### Create individual zones
Create a new "Zone Device" for each Zone you want Smartthings to show you status for. 

The network id needs to be the word 'zone' followed by the matching zone number that your DSC system sees it as.

For example: **dsczone1** or **dsczone5**

And again, ensure your hub is selected in the device settings.

### The rest of the setup

1. Create a new Smartthings App in the IDE, call it 'DSC Integration' or whatever you like. Use the code from dscAlarmIntegrationSmarththingsApp.groovy file for the new smartapp.

2. Click "Enable OAuth in Smart App" and copy down the generated "OAuth Client ID" and the "OAuth Client Secret", you will need them later to generate an access code.
   Click "Create" and when the code section comes up select all the text and replace it with the code from the file 'dscAlarmIntegrationSmarththingsApp.groovy'.
   Click "Save" then "Publish" -> "For Me".

2. Now the hard part, we need to authorize this Smarttthings app to be used via the REST API.
   It's going to take a few steps but all you need is a web browser and your OAuth ID's from the app setup page.
   Follow the RESTAPISetup.md document in this same repo to finish the setup.

3. Edit 'alarmserver.cfg' and add in the OAuth/Access Code information to the callback_url_app_id and callbackurl_access_token values,
   adjust your zones/partitions and callback event codes as needed.
   Leaving them at the defaults is likely what you already want.

4. Fire up the AlarmServer, you should see your events from the server show up within 1-2 seconds on your Smartphone.

## Thanks!
Thanks goes out to the following people, without their previous work none of this would have been possible:
* juggie
* Ethomasii
* Rob Fisher <robfish@att.net>
* Carlos Santiago <carloss66@gmail.com>
* JTT <aesystems@gmail.com>
* Donny K <donnyk+envisalink@gmail.com>
* Leaberry <leaberry@gmail.com>
* Kent Holloway <drizit@gmail.com>
* Matt Martz <matt.martz@gmail.com>
