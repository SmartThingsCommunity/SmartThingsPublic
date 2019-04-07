#!/bin/sh

candle=$1

for i in 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F 20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F 30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E 3F
do
result=$(gatttool -b "$candle" --char-read -a 0x00$i | sed 's_Characteristic value/descriptor: __g' | egrep '^[0-9a-fA-F][0-9a-fA-F]\s[0-9a-fA-F][0-9a-fA-F]\s[0-9a-fA-F][0-9a-fA-F]\s[0-9a-fA-F][0-9a-fA-F]\s$') 2>/dev/null
if [ "$result" != "" ]
then
   possible_color=$(echo -e "$possible_color\n0x00$i $result")
fi
result=$(gatttool -b "$candle" --char-read -a 0x00$i | sed 's_Characteristic value/descriptor: __g' | egrep '^[0-9a-fA-F][0-9a-fA-F]\s[0-9a-fA-F][0-9a-fA-F]\s[0-9a-fA-F][0-9a-fA-F]\s[0-9a-fA-F][0-9a-fA-F]\s[0-9a-fA-F][0-9a-fA-F]\s[0-9a-fA-F][0-9a-fA-F]\s[0-9a-fA-F][0-9a-fA-F]\s[0-9a-fA-F][0-9a-fA-F]\s$' 2>/dev/null)
if [ "$result" != "" ]
then
   possible_effect=$(echo -e "$possible_effect\n0x00$i $result")
fi
done

echo ""
echo ""
echo "Possible color handles found"
echo "$possible_color"
echo ""
echo ""
echo "Possible effect handles found"
echo "$possible_effect"
