#!/bin/sh

if [ "$1" = "candle1" ]
then
candle=xx:xx:xx:xx:xx:xx
fi
if [ "$1" = "candle2" ]
then
candle=xx:xx:xx:xx:xx:xx
fi
if [ "$1" = "candle3" ]
then
candle=xx:xx:xx:xx:xx:xx
fi

color_handle="0x0019"
effect_handle="0x0017"

if [ "$2" = "red" ]
then
   handle=0x0019
   color="00ff0000"
elif [ "$2" = "blue" ]
then
   handle=0x0019
   color="000000ff"
elif [ "$2" = "green" ]
then
   handle=0x0019
   color="0000ff00"
elif [ "$2" = "magenta" ]
then
   handle=0x0019
   color="00ff00ff"
elif [ "$2" = "yellow" ]
then
   handle=0x0019
   color="00ffff00"
elif [ "$2" = "white" ]
then
   handle=0x0019
   color="ff000000"
elif [ "$2" = "cyan" ]
then
   handle=0x0019
   color="0000ffff"
elif [ "$2" = "off" ]
then
   handle=0x0019
   color="00000000"
elif [ "$2" = "fadewhite" ]
then
   handle=0x0017
   color="ff00000001000f0f"
elif [ "$2" = "fadered" ]
then
   handle=0x0017
   color="00ff000001000f0f"
elif [ "$2" = "fadegreen" ]
then
   handle=0x0017
   color="0000ff0001000f0f"
elif [ "$2" = "fadeblue" ]
then
   handle=0x0017
   color="000000ff01000f0f"
elif [ "$2" = "fadeyellow" ]
then
   handle=0x0017
   color="00ffff0001000f0f"
elif [ "$2" = "fademagenta" ]
then
   handle=0x0017
   color="00ff00ff01000f0f"
elif [ "$2" = "fadecyan" ]
then
   handle=0x0017
   color="0000ffff00000f0f"
elif [ "$2" = "flashwhite" ]
then
   handle=0x0017
   color="ff00000000000f0f"
elif [ "$2" = "flashred" ]
then
   handle=0x0017
   color="00ff000000000f0f"
elif [ "$2" = "flashgreen" ]
then
   handle=0x0017
   color="0000ff0000000f0f"
elif [ "$2" = "flashblue" ]
then
   handle=0x0017
   color="000000ff00000f0f"
elif [ "$2" = "flashyellow" ]
then
   handle=0x0017
   color="00ffff0000000f0f"
elif [ "$2" = "flashmagenta" ]
then
   handle=0x0017
   color="00ff00ff00000f0f"
elif [ "$2" = "flashcyan" ]
then
   handle=0x0017
   color="0000ffff00000f0f"
elif [ "$2" = "flashrainbow" ]
then
   handle=0x0017
   color="00ff000002000f0f"
elif [ "$2" = "faderainbow" ]
then
   handle=0x0017
   color="00ff000003000f0f"
else
   if [ ${#2} = 8 ]
   then
      handle=0x0019
      color=$2
   elif [ ${#2} = 16 ]
   then
      handle=0x0017
      color=$2
   else
      echo "A valid option was not chosen"
   fi
fi

gatttool -b $candle --char-write -a $handle -n $color
gatttool -b $candle --char-write -a $handle -n $color
gatttool -b $candle --char-write -a $handle -n $color
