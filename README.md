# Circadian Daylight
Use your SmartThings Hub to sync your color changing lights with natural daylight hues

This SmartApp slowly synchronizes your color changing lights with local perceived color temperature of the sky throughout the day.  This gives your environment a more natural feel, with cooler whites during the midday and warmer tints near twilight and dawn.

In addition, the SmartApp sets your lights to a nice cool white at 1% in "Sleep" mode, which is far brighter than moonlight but won't reset your circadian rhythm or break down too much rhodopsin in your eyes.

![circadian_daylight](https://cloud.githubusercontent.com/assets/478212/6904334/b8decdac-d6e5-11e4-97ec-e48c53a8b96e.png)

Human circadian rhythms are heavily influenced by ambient light levels and hues.  Hormone production, brainwave activity, mood and wakefulness are just some of the cognitive functions tied to cyclical natural light.

 *	http://en.wikipedia.org/wiki/Zeitgeber

![daylight_sun](http://c1.staticflickr.com/5/4102/4771158108_f89118bf28_b.jpg) (c) Bill Bradford - https://www.flickr.com/photos/mrbill/
A timelapse view of daylight color temperature changes over the course of a day

 Here's some further reading:
 * http://www.cambridgeincolour.com/tutorials/sunrise-sunset-calculator.htm
 * http://en.wikipedia.org/wiki/Color_temperature

Technical notes:  I had to make a lot of assumptions when writing this App:
*  There are no considerations for weather or altitude, but does use your Hub's zip code to calculate the sun position.    
*  The app doesn't calculate a true "Blue Hour" -- it just sets the lights to 2700K (warm white) until your hub goes into Night mode

Forums
-------
Feel free to participate and contribute over at the SmartThings forums:
http://community.smartthings.com/t/circadian-daylight-smartthings-philips-hue/13623

License
-------
Copyright (c) 2016, Clayton Nummer
Copyright (c) 2015, Kristopher Kubicki
All rights reserved.
