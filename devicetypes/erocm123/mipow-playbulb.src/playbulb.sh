#!/bin/sh

if [ "$1" = "candle1" ]
then
candle=xx:xx:xx:xx:xx:xx
color_handle="0x0019"
effect_handle="0x0017"
fi
if [ "$1" = "candle2" ]
then
candle=xx:xx:xx:xx:xx:xx
color_handle="0x0019"
effect_handle="0x0017"
fi
if [ "$1" = "candle3" ]
then
candle=xx:xx:xx:xx:xx:xx
color_handle="0x0019"
effect_handle="0x0017"
fi
if [ "$1" = "candle4" ]
then
candle=xx:xx:xx:xx:xx:xx
color_handle="0x001B"
effect_handle="0x0019"
fi

if [ "$2" = "red" ]
then
   handle=$color_handle
   color="00ff0000"
elif [ "$2" = "blue" ]
then
   handle=$color_handle
   color="000000ff"
elif [ "$2" = "green" ]
then
   handle=$color_handle
   color="0000ff00"
elif [ "$2" = "magenta" ]
then
   handle=$color_handle
   color="00ff00ff"
elif [ "$2" = "yellow" ]
then
   handle=$color_handle
   color="00ffff00"
elif [ "$2" = "white" ]
then
   handle=$color_handle
   color="ff000000"
elif [ "$2" = "cyan" ]
then
   handle=$color_handle
   color="0000ffff"
elif [ "$2" = "off" ]
then
   handle=$color_handle
   color="00000000"
elif [ "$2" = "fadewhite" ]
then
   handle=$effect_handle
   color="ff00000001000f0f"
elif [ "$2" = "fadered" ]
then
   handle=$effect_handle
   color="00ff000001000f0f"
elif [ "$2" = "fadegreen" ]
then
   handle=$effect_handle
   color="0000ff0001000f0f"
elif [ "$2" = "fadeblue" ]
then
   handle=$effect_handle
   color="000000ff01000f0f"
elif [ "$2" = "fadeyellow" ]
then
   handle=$effect_handle
   color="00ffff0001000f0f"
elif [ "$2" = "fademagenta" ]
then
   handle=$effect_handle
   color="00ff00ff01000f0f"
elif [ "$2" = "fadecyan" ]
then
   handle=$effect_handle
   color="0000ffff00000f0f"
elif [ "$2" = "flashwhite" ]
then
   handle=$effect_handle
   color="ff00000000000f0f"
elif [ "$2" = "flashred" ]
then
   handle=$effect_handle
   color="00ff000000000f0f"
elif [ "$2" = "flashgreen" ]
then
   handle=$effect_handle
   color="0000ff0000000f0f"
elif [ "$2" = "flashblue" ]
then
   handle=$effect_handle
   color="000000ff00000f0f"
elif [ "$2" = "flashyellow" ]
then
   handle=$effect_handle
   color="00ffff0000000f0f"
elif [ "$2" = "flashmagenta" ]
then
   handle=$effect_handle
   color="00ff00ff00000f0f"
elif [ "$2" = "flashcyan" ]
then
   handle=$effect_handle
   color="0000ffff00000f0f"
elif [ "$2" = "flashrainbow" ]
then
   handle=$effect_handle
   color="00ff000002000f0f"
elif [ "$2" = "faderainbow" ]
then
   handle=$effect_handle
   color="00ff000003000f0f"
else
   if [ ${#2} = 8 ]
   then
      handle=$color_handle
      color=$2
   elif [ ${#2} = 16 ]
   then
      handle=$effect_handle
      color=$2
   else
      echo "A valid option was not chosen"
   fi
fi

gatttool -b $candle --char-write -a $handle -n $color
gatttool -b $candle --char-write -a $handle -n $color
gatttool -b $candle --char-write -a $handle -n $color
