<?php

#### EDIT SETTINGS HERE ####

$candles = array("xx:xx:xx:xx:xx:xx", "xx:xx:xx:xx:xx:xx", "xx:xx:xx:xx:xx:xx");

$color_handle = "0x0019";
$effect_handle = "0x0017";

############################

$device = $_REQUEST['device'];
$setting = $_REQUEST['setting'];
$config = $_REQUEST['config'];
$refresh = $_REQUEST['refresh'];

if ($config) {
   $x = 1;
   $return = [];
   foreach ($candles as &$value) {
      //echo "candle" . ($x++) . "   " . $value . "<br>";
      $return["candle" . ($x++)] = $value;
   }
   echo json_encode( $return );

}

if ($device && $refresh) {

   $devices = explode(",", $device);

   for ($x = 0; $x < count($candles); $x++) {
      if ("$devices[0]" == "candle" . ($x+1))
      {
         $candle=$candles[$x];
      }
   }

   $output = shell_exec("gatttool -b $candle --char-read -a 0x0017 | sed 's_Characteristic value/descriptor: __g'");
   $effect_value = explode(" ", $output);
   $effect_value =  $effect_value[4] . $effect_value[5] . $effect_value[6] . $effect_value[7];
   $effect_color = explode(" ", $output);
   $effect_color = $effect_color[0] . $effect_color[1] . $effect_color[2] . $effect_color[3];
   $output = shell_exec("gatttool -b $candle --char-read -a 0x0019 | sed 's_Characteristic value/descriptor: __g'");
   $candle_color = trim(str_replace(" ", "", $output));

   if ($candle_color === "0000000" && $effect_color === "0000000") {
      //echo $device . " is off";
      $return = array( 'device' => $device, 'power' => 'off' );
      echo json_encode( $return );
   }
   else {
      //echo $device . " is on";
      $return = array( 'device' => $device, 'power' => 'on' );
      if ($candle_color !== "0000000") {
         $return['color'] = $candle_color;
         $return['effe'] = $effect_value;
         $return['ecol'] = $effect_color;
      }
      echo json_encode( $return );
   }
}

if ($device && $setting) {

$devices = explode(",", $device);

for ($x = 0; $x < count($candles); $x++) {
for ($y = 0; $y < count($devices); $y++) {
   if ("$devices[$y]" == "candle" . ($x+1))
   {
      $devices[$y]=$candles[$x];
   }
}
}



switch ($setting) {
   case off:
      $handle=$color_handle;
      $color="00000000";
      break;
   case red:
      $handle=$color_handle;
      $color="00ff0000";
      break;
   case green:
      $handle=$color_handle;
      $color="0000ff00";
      break;
   case blue:
      $handle=$color_handle;
      $color="000000ff";
      break;
   case white:
      $handle=$color_handle;
      $color="ff000000";
      break;
   case yellow:
      $handle=$color_handle;
      $color="00ffff00";
      break;
   case magenta:
      $handle=$color_handle;
      $color="00ff00ff";
      break;
   case cyan:
      $handle=$color_handle;
      $color="0000ffff";
      break;
   case flashred:
      $handle=$effect_handle;
      $color="00ff000000000f0f";
      break;
   case flashgreen:
      $handle=$effect_handle;
      $color="0000ff0000000f0f";
      break;
   case flashblue:
      $handle=$effect_handle;
      $color="000000ff00000f0f";
      break;
   case flashwhite:
      $handle=$effect_handle;
      $color="ff00000000000f0f";
      break;
   case flashyellow:
      $handle=$effect_handle;
      $color="00ffff0000000f0f";
      break;
   case flashmagenta:
      $handle=$effect_handle;
      $color="00ff00ff00000f0f";
      break;
   case flashcyan:
      $handle=$effect_handle;
      $color="0000ffff00000f0f";
      break;
   case fadered:
      $handle=$effect_handle;
      $color="00ff000001000f0f";
      break;
   case fadegreen:
      $handle=$effect_handle;
      $color="0000ff0001000f0f";
      break;
   case fadeblue:
      $handle=$effect_handle;
      $color="000000ff01000f0f";
      break;
   case fadewhite:
      $handle=$effect_handle;
      $color="ff00000001000f0f";
      break;
   case fadeyellow:
      $handle=$effect_handle;
      $color="00ffff0001000f0f";
      break;
   case fademagenta:
      $handle=$effect_handle;
      $color="00ff00ff01000f0f";
      break;
   case fadecyan:
      $handle=$effect_handle;
      $color="0000ffff00000f0f";
      break;
   case faderainbow:
      $handle=$effect_handle;
      $color="00ff000003000f0f";
      break;
   case flashrainbow:
      $handle=$effect_handle;
      $color="00ff000002000f0f";
      break;
   default:
      if (strlen($setting) == 8)
      {
         $handle=$color_handle;
         $color=$setting;
      }
      elseif (strlen($setting) == 16)
      {
         $handle=$effect_handle;
         $color=$setting;
      }
      else
      {
         // A valid option was not chosen"
      }
}

if ( $color === "00000000" )
{
   $return = array( 'device' => $device, 'power' => 'off' );
} else {
   if (strlen($color) == 16)
   {
      $return = array( 'device' => $device, 'power' => 'on', 'color' => substr($color, 0, 8), 'effect' => substr($color, 8, 8) );
   }
   else if (strlen($color) == 8) {
      $return = array( 'device' => $device, 'power' => 'on', 'color' => $color, 'effect' => '00000000' );
   }
   else {
      $return = array( 'error' => 'A valid option was not chosen' );
   }
}
echo json_encode( $return );

for ($y = 0; $y < count($devices); $y++) {
   $output = shell_exec("gatttool -b $devices[$y] --char-write -a $handle -n $color" );
   usleep(500000);
}

}

?>
