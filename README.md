# SmartThings Repo

My samsung smartthings modules. Modified alarmserver and control panel for DSC
alarm integration. Also, my modified version of "Dim and Dimmer" for routines and
with color bulb support:

See this thread for more info about DSC Alarm:
https://community.smartthings.com/t/dsc-evl-3-4-alarmserver-smartthings/36604/126

And this thread for info about Dim and Dimmer Routine:

## Alarmserver Instructions

### Warnings about upgrading from an older version
Device setup is done automatically in the new version. If you are upgrading from a previous version, please delete all the zone or panel devices you created first. These
would have been created with networkId's like "dsczone1" or "dscstay1." Do not delete the device handlers, or smartapps, they can be updated to the latest versions
after you delete the existing devices and panels. This will allow you to avoid having to re-do the oauth setup. You will need to go into the DSC Integration smartapp
and unselect all the panel and zone devices before it will allow you to remove them. If you already updated the smartapp to the latest version, this option will
no longer be there and you'll have to add the following lines under the prefences {} block in order to remove the devices:

  section("Alarm Panel:") {
    input "paneldevices", "capability.switch", title: "Alarm Panel (required)", multiple: true, required: false
  }
  section("Zone Devices:") {
    input "zonedevices", "capability.sensor", title: "DSC Zone Devices (required)", multiple: true, required: false
  }

Alternatively, downgrade to this version here and then remove the devices and upgrade back again afterwards:
https://raw.githubusercontent.com/LXXero/DSCAlarm/8428bb57cbc9038976511ce79cd613cb28e74017/smartapps/DSCIntegration.groovy

You'll also need to update alarmserver.cfg to the latest format, which involves the zones and partitions being defined at the bottom in their own sections, with
the addition of the requirement of specifying the zone type, and the stay/away panel names, which are used for the automatic device creation.

### Setup device handlers

Using the Smartthings IDE create the new device handlers using the code from the devicetypes directory.

There are 6 types of devices handlers you can create:

* DSC Stay Panel  - (Shows partition status info and provides Stay switch that can be used in routines)
* DSC Away Panel  - (Shows partition status info and provides Away switch that can be used in routines)
* DSC Zone Contact - (contact device open/close)
* DSC Zone Motion  - (motion device active/inactive)
* DSC Zone Smoke - (wireless/4-wire smoke device, detected/clear/test. It's nearly the same as motion or contact, as it's attached to a zone.)
* DSC Zone CO - (wireless/4-wire carbon monoxide device, detected/clear/test. It's similar to DSC Zone Smoke.)

At a minimum you'll probably want the Stay/Away panels, Contact, and Motion.

In the Web IDE for Smartthings create a new device type for each of the above devices and paste in the code for each device from the corresponding groovy files in the repo. Alternatively, setup github integration, create a new github repository with "LXXero" as owner, "DSCAlarm" as name, and "master" as branch. Once you have setup this repo, you can easily add all the devices. Be sure to check the publish checkbox at the bottom.

For all the device types, make sure publish them. If you're using github, use the "publish" checkbox. If you forget, or installed the code manually via copy/paste, you'll have to go into each one and click "publish -> for me" again.

### Smartapp Setup

1. Create a new Smartthings App in the IDE. Use the code from DSCIntegration.groovy in the smartapps folder for the new smartapp. Save and publish as needed.

2. Click "Enable OAuth in Smart App" Make sure you save and apply this before you leave the page, as the oauth information displayed isn't actually applied until you do so.

3. Add/Install the SmartApp on your device and scroll to the bottom section that says "Show SmartApp Token Info". Copy all this information and save it. You can probably email it to yourself so you can easily copy and paste to your Alarmserver config.

4. Once that is done, locate the following lines in your alarmserver.cfg and fill them in with the app ID and access token: callbackurl_app_id=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx callbackurl_access_token=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
Additionally, you might want to set the "callbackurl_base" line in the cfg file to whatever URL is output in the SmartApp Token info. There have been some cases where smartapps are requiring this URL and won't bind to the regular "https://graph.api.smartthings.com" URL. 

5. Once OAuth setup is completed, edit the settings for the DSC Integration app on your phone, and fill in the IP/Port with the correct information for your alarmserver.
   The port is typically the "httpsport" setting in your alarmserver.cfg, and the IP should be the IP of your alarmserver and not your envisalink device. If need be, setup
   any push notifications here as well. NOTE: THIS MUST BE THE IP ADDRESS! THE CODE DOES NOT RESOLVE HOSTNAMES/DNS AT THIS TIME. See TODO.

### Alarmserver Setup

1. First, edit 'alarmserver.cfg' and add in the OAuth/Access Code information to the callback_url_app_id and callbackurl_access_token values,
   and adjust your zones/partitions at the bottom of the file. If you're upgrading, be sure to update the list of callback event codes to match
   the upstream config example. Leaving them at the defaults is likely what you already want.

2. The alarm panels and zone devices get created/deleted automatically when alarmserver starts. Ensure all your zones and partitions are properly
   defined as per the included alarmserver.cfg example.

4. Fire up the AlarmServer. Your devices should get created in smartthings, and you should start seeing events pushed to them within a few moments
   on your smart phone.

## Thanks!
Thanks goes out to the following people, without their previous work none of this would have been possible:
* juggie
* Ethomasii
* blacktirion
* Rob Fisher <robfish@att.net>
* Carlos Santiago <carloss66@gmail.com>
* JTT <aesystems@gmail.com>
* Donny K <donnyk+envisalink@gmail.com>
* Leaberry <leaberry@gmail.com>
* Kent Holloway <drizit@gmail.com>
* Matt Martz <matt.martz@gmail.com>

And for Dim and Dimmer, Geko / Statusbits
