#include <ESP8266WiFi.h>
#include <DNSServer.h>            //Local DNS Server used for redirecting all requests to the configuration portal
#include <ESP8266WebServer.h>     //Local WebServer used to serve the configuration portal
#include <WiFiManager.h>
#include <ESP8266SSDP.h>
#include <ESP8266HTTPUpdateServer.h>
#include <ESP8266HTTPClient.h>
#include <EEPROM.h>

//Cjcharles added for ease of testing
#include <ArduinoOTA.h>

ESP8266HTTPUpdateServer httpUpdater;

/*
r~ r fade
s~ r flash
g~ g fade
h~ g flash
b~ b fade
c~ b flash
f~ rgb fade
d~ rgb flash
w~ w1 fade
x~ w1 flash
y~ w2 fade
z~ w2 flash
n~ rrggbbw1w2 fade
o~ rrggbbw1w2 flash
*/

const char * projectName = "SmartLife RGBW Controller";
String softwareVersion = "2.1.5";

const char compile_date[] = __DATE__ " " __TIME__;

int currentRED   = 0;
int currentGREEN = 0;
int currentBLUE  = 0;
int currentW1    = 0;
int currentW2    = 0;
int lastRED   = 0;
int lastGREEN = 0;
int lastBLUE  = 0;
int lastW1    = 0;
int lastW2    = 0;

int redPIN            = 15;
int greenPIN          = 13;
int bluePIN           = 12;
int w1PIN             = 14;
int w2PIN             = 4;
int POWER_ENABLE_LED  = 15;
int LEDPIN            = 5;
int LED2PIN           = 1;
int KEY_PIN           = 0;

boolean needFirmware = true;

boolean needUpdate = true;
boolean inAutoOff = false;

unsigned long connectionFailures;

#define FLASH_EEPROM_SIZE 4096
extern "C" {
#include "spi_flash.h"
}
extern "C" uint32_t _SPIFFS_start;
extern "C" uint32_t _SPIFFS_end;
extern "C" uint32_t _SPIFFS_page;
extern "C" uint32_t _SPIFFS_block;


//These are defines which may or may not be used
#define LEDoff digitalWrite(LEDPIN,HIGH)
#define LEDon digitalWrite(LEDPIN,LOW)

#define LED2off digitalWrite(LED2PIN,HIGH)
#define LED2on digitalWrite(LED2PIN,LOW)

// note
// TX GPIO2 @Serial1 (Serial ONE)
// RX GPIO3 @Serial

String program = "";
String program_off = "";
boolean run_program;
int program_step = 1;
int program_counter = 1;
int program_wait = 0;
int program_loop = false;
String program_number = "0";
String pre_program = "";
unsigned long previousMillis = millis();
unsigned long failureTimeout = millis();
unsigned long timerwd;
unsigned long autoOffTimer;
unsigned long currentmillis = 0;
unsigned long timerUptime;
long debounceDelay = 20;
boolean fade = true;
int repeat = 1;
int repeat_count = 1;
char previousColor[10] = "";

//stores if the switch was high before at all
int state = LOW;
//stores the time each button went high or low
unsigned long current_high;
unsigned long current_low;

String       s_Current_WIFISSID                   = "";
String       s_Current_WIFIPW                     = "";

#define DEFAULT_HAIP                   "0.0.0.0"
#define DEFAULT_HAPORT                 39500
#define DEFAULT_RESETWIFI              false
#define DEFAULT_POS                    0
#define DEFAULT_CURRENT STATE          ""
#define DEFAULT_IP                     "0.0.0.0"
#define DEFAULT_GATEWAY                "0.0.0.0"
#define DEFAULT_SUBNET                 "0.0.0.0"
#define DEFAULT_DNS                    "0.0.0.0"
#define DEFAULT_USE_STATIC             false
#define DEFAULT_LONG_PRESS             false
#define DEFAULT_REALLY_LONG_PRESS      false
#define DEFAULT_USE_PASSWORD           false
#define DEFAULT_USE_PASSWORD_CONTROL   false
#define DEFAULT_COLOR                  ""
#define DEFAULT_BAD_BOOT_COUNT         0
#define DEFAULT_RED                    0
#define DEFAULT_GREEN                  0
#define DEFAULT_BLUE                   0
#define DEFAULT_W1                     0
#define DEFAULT_W2                     0
#define DEFAULT_DISABLE_J3_RESET       false
#define DEFAULT_TRANSITION_SPEED       1
#define DEFAULT_TRANSITION_TYPE        1
#define DEFAULT_AUTO_OFF               0
#define DEFAULT_SWITCH_TYPE            0
#define DEFAULT_CONTINUE_BOOT          false
#define DEFAULT_RESET_TYPE             1
#define DEFAULT_UREPORT                60
#define DEFAULT_DEBOUNCE               20

#define DEFAULT_PASSWORD               ""


struct SettingsStruct
{
  byte          haIP[4];
  unsigned int  haPort;
  boolean       resetWifi;
  int           powerOnState;
  boolean       currentState;
  byte          IP[4];
  byte          Gateway[4];
  byte          Subnet[4];
  byte          DNS[4];
  boolean       useStatic;
  boolean       longPress;
  boolean       reallyLongPress;
  boolean       usePassword;
  boolean       usePasswordControl;
  char          defaultColor[11];
  int           defaultTransition;
  int           badBootCount;
  boolean       disableJ3Reset;
  int           switchType;
  int           autoOff;
  int           transitionSpeed;
  boolean       continueBoot;
  int           resetType;
  int           uReport;
  int           debounce;
  int           redPIN_stored;
  int           greenPIN_stored;
  int           bluePIN_stored;
  int           w1PIN_stored;
  int           w2PIN_stored;
  int           POWER_ENABLE_LED_stored;
  int           LEDPIN_stored;
  int           LED2PIN_stored;
  int           KEY_PIN_stored;
  byte          forward_address[4];
} Settings;

struct SecurityStruct
{
  char          Password[26];
  int           settingsVersion;
  char          ssid[33];
  char          pass[33];
} SecuritySettings;


int led_delay_red = 0;
int led_delay_green = 0;
int led_delay_blue = 0;
int led_delay_w1 = 0;
int led_delay_w2 = 0;
#define time_at_colour 1300
unsigned long TIME_LED_RED = 0;
unsigned long TIME_LED_GREEN = 0;
unsigned long TIME_LED_BLUE = 0;
unsigned long TIME_LED_W1 = 0;
unsigned long TIME_LED_W2 = 0;
int RED, GREEN, BLUE, W1, W2;
int RED_A = 0;
int GREEN_A = 0;
int BLUE_A = 0;
int W1_A = 0;
int W2_A = 0;

byte mac[6];

// Start WiFi Server
std::unique_ptr<ESP8266WebServer> server;
//ESP8266WebServer server;

void handleRoot() {
  server->send(200, "application/json", "{\"message\":\"SmartLife RGBW Controller\"}");
}

void handleNotFound() {
  String message = "File Not Found\n\n";
  message += "URI: ";
  message += server->uri();
  message += "\nMethod: ";
  message += (server->method() == HTTP_GET) ? "GET" : "POST";
  message += "\nArguments: ";
  message += server->args();
  message += "\n";
  for (uint8_t i = 0; i < server->args(); i++) {
    message += " " + server->argName(i) + ": " + server->arg(i) + "\n";
  }
  server->send(404, "text/plain", message);
}

void check_if_forward_request() {
  if ((Settings.forward_address[0] > 0) && (Settings.forward_address[0] < 255) && (server->uri().c_str() != "")) {
    HTTPClient httpc;
    String forward_string = "http://";
    forward_string += String(Settings.forward_address[0]) + "." + String(Settings.forward_address[1]) + "." + String(Settings.forward_address[2]) + "." + String(Settings.forward_address[3]);
    forward_string += server->uri();
    for (int i = 0; i < server->args(); i++) {
      if (i == 0) {forward_string += "?";}
      if (i > 0) {forward_string += "&";}
      forward_string += server->argName(i) + "=" + server->arg(i);
    }
    httpc.begin(forward_string);
    int httpCode = httpc.GET();
    httpc.end();
  }
}

boolean changeColor(String color, int channel, boolean fade, boolean all = false)
{
  boolean success = false;

  if (channel != 0 && !inAutoOff) {
    inAutoOff = true;
    autoOffTimer = millis();
  }

  switch (channel)
  {
  case 0: // Off
    {

      lastRED = RED;
      lastGREEN = GREEN;
      lastBLUE = BLUE;
      lastW1 = W1;
      lastW2 = W2;

      RED = 0;
      GREEN = 0;
      BLUE = 0;
      W1 = 0;
      W2 = 0;

      ::fade = fade;

      change_LED();
      success = true;
      break;
    }
  case 1: //R channel
    {
      RED = getScaledValue(color.substring(0, 2));

      if (all == false) {
        W1 = 0;
        W2 = 0;
      }

      lastRED = RED;
      lastGREEN = GREEN;
      lastBLUE = BLUE;
      lastW1 = W1;
      lastW2 = W2;

      ::fade = fade;

      change_LED();
      success = true;
      break;
    }
  case 2: //G channel
    {
      GREEN = getScaledValue(color.substring(0, 2));

      if (all == false) {
        W1 = 0;
        W2 = 0;
      }

      lastRED = RED;
      lastGREEN = GREEN;
      lastBLUE = BLUE;
      lastW1 = W1;
      lastW2 = W2;

      ::fade = fade;

      change_LED();
      success = true;
      break;
    }
  case 3: //B channel
    {
      BLUE = getScaledValue(color.substring(0, 2));

      if (all == false) {
        W1 = 0;
        W2 = 0;
      }

      lastRED = RED;
      lastGREEN = GREEN;
      lastBLUE = BLUE;
      lastW1 = W1;
      lastW2 = W2;

      ::fade = fade;

      change_LED();
      success = true;
      break;
    }
  case 4: //White1 channel
    {
      if (all == false) {
        RED = 0;
        GREEN = 0;
        BLUE = 0;
        W2 = 0;
      }

      W1 = getScaledValue(color.substring(0, 2));

      lastRED = RED;
      lastGREEN = GREEN;
      lastBLUE = BLUE;
      lastW1 = W1;
      lastW2 = W2;

      ::fade = fade;

      change_LED();
      success = true;
      break;
    }
  case 5: //White2 channel
    {
      if (all == false) {
        RED = 0;
        GREEN = 0;
        BLUE = 0;
        W2 = 0;
      }

      W2 = getScaledValue(color.substring(0, 2));

      lastRED = RED;
      lastGREEN = GREEN;
      lastBLUE = BLUE;
      lastW1 = W1;
      lastW2 = W2;

      ::fade = fade;

      change_LED();
      success = true;
      break;
    }
  case 6: //RGB channel
    {
      if (color == "xxxxxx") {
        RED = rand_interval(0, 1023);
        GREEN = rand_interval(0, 1023);
        BLUE = rand_interval(0, 1023);
      } else {
        RED = getScaledValue(color.substring(0, 2));
        GREEN = getScaledValue(color.substring(2, 4));
        BLUE = getScaledValue(color.substring(4, 6));
      }
      if (all == false) {
        W1 = 0;
        W2 = 0;
      }

      lastRED = RED;
      lastGREEN = GREEN;
      lastBLUE = BLUE;
      lastW1 = W1;
      lastW2 = W2;

      ::fade = fade;

      change_LED();
      success = true;
      break;
    }
  case 7: //RGBW channel
    {
      if (color == "xxxxxxxx") {
        RED = rand_interval(0, 1023);
        GREEN = rand_interval(0, 1023);
        BLUE = rand_interval(0, 1023);
        W1 = rand_interval(0, 1023);
      } else {
        RED = getScaledValue(color.substring(0, 2));
        GREEN = getScaledValue(color.substring(2, 4));
        BLUE = getScaledValue(color.substring(4, 6));
        W1 = getScaledValue(color.substring(6, 8));
      }
      if (all == false) {
        W2 = 0;
      }

      lastRED = RED;
      lastGREEN = GREEN;
      lastBLUE = BLUE;
      lastW1 = W1;
      lastW2 = W2;

      ::fade = fade;

      change_LED();
      success = true;
      break;
    }
  case 8: //RGBWW channel
    {
      if (color == "xxxxxxxxxx") {
        RED = rand_interval(0, 1023);
        GREEN = rand_interval(0, 1023);
        BLUE = rand_interval(0, 1023);
        W1 = rand_interval(0, 1023);
        W2 = rand_interval(0, 1023);
      } else {
        RED = getScaledValue(color.substring(0, 2));
        GREEN = getScaledValue(color.substring(2, 4));
        BLUE = getScaledValue(color.substring(4, 6));
        W1 = getScaledValue(color.substring(6, 8));
        W2 = getScaledValue(color.substring(8, 10));
      }

      lastRED = RED;
      lastGREEN = GREEN;
      lastBLUE = BLUE;
      lastW1 = W1;
      lastW2 = W2;

      ::fade = fade;

      change_LED();
      success = true;
      break;
    }
  case 99: // On
    {
      if (color == "0000000000") {
        RED = lastRED;
        GREEN = lastGREEN;
        BLUE = lastBLUE;
        W1 = lastW1;
        W2 = lastW2;
      }
      else {
        RED = getScaledValue(color.substring(0, 2));
        GREEN = getScaledValue(color.substring(2, 4));
        BLUE = getScaledValue(color.substring(4, 6));
        W1 = getScaledValue(color.substring(6, 8));
        W2 = getScaledValue(color.substring(8, 10));
      }

      ::fade = fade;

      change_LED();
      success = true;
      break;
    }
  }

  if (String(Settings.defaultColor) == "Previous" && run_program == false){
    savePreviousColor();

  }
  return success;

}

unsigned int rand_interval(unsigned int min, unsigned int max)
{
  int r;
  const unsigned int range = 1 + max - min;
  const unsigned int buckets = RAND_MAX / range;
  const unsigned int limit = buckets * range;

  /* Create equal size buckets all in a row, then fire randomly towards
  the buckets until you land in one of them. All buckets are equally
  likely. If you land off the end of the line of buckets, try again. */
  do
  {
    r = rand();
  } while (r >= limit);

  return min + (r / buckets);
}

void relayToggle() {
  if (digitalRead(KEY_PIN) == LOW) {
    current_low = millis();
    state = LOW;
  }
  if (digitalRead(KEY_PIN == HIGH) && state == LOW)
  {
    current_high = millis();
    if ((current_high - current_low) > (Settings.debounce ? Settings.debounce : debounceDelay) && (current_high - current_low) < 10000)
    {
      state = HIGH;
      run_program = false;
      if (getHex(RED) + getHex(GREEN) + getHex(BLUE) + getHex(W1) + getHex(W2) == "0000000000") {
        String transition = Settings.defaultTransition == 2? "false" : "true";
        run_program = false;
        turnOn(transition);
        //Check if we should forward the request to another device
        check_if_forward_request();
        needUpdate = true;
      } else {
        String transition = Settings.defaultTransition == 2? "false" : "true";
        run_program = false;
        turnOff(transition);
        //Check if we should forward the request to another device
        check_if_forward_request();
        needUpdate = true;
      }
    }
    else if ((current_high - current_low) >= 10000 && (current_high - current_low) < 20000)
    {
      if (Settings.resetType == 1 || Settings.resetType == 3) {
        Settings.longPress = true;
        SaveSettings();
        ESP.restart();
      }
    }
    else if ((current_high - current_low) >= 20000 && (current_high - current_low) < 60000)
    {
      if (Settings.resetType == 1 || Settings.resetType == 3) {
        Settings.reallyLongPress = true;
        SaveSettings();
        ESP.restart();
      }
    }
  }
}

const char * endString(int s, const char *input) {
  int length = strlen(input);
  if ( s > length ) s = length;
  return const_cast<const char *>(&input[length - s]);
}

void change_LED()
{
  int diff_red = abs(RED - RED_A);
  if (diff_red > 0) {
    led_delay_red = time_at_colour / abs(RED - RED_A);
  } else {
    led_delay_red = time_at_colour / 1023;
  }

  int diff_green = abs(GREEN - GREEN_A);
  if (diff_green > 0) {
    led_delay_green = time_at_colour / abs(GREEN - GREEN_A);
  } else {
    led_delay_green = time_at_colour / 1023;
  }

  int diff_blue = abs(BLUE - BLUE_A);
  if (diff_blue > 0) {
    led_delay_blue = time_at_colour / abs(BLUE - BLUE_A);
  } else {
    led_delay_blue = time_at_colour / 1023;
  }

  int diff_w1 = abs(W1 - W1_A);
  if (diff_w1 > 0) {
    led_delay_w1 = time_at_colour / abs(W1 - W1_A);
  } else {
    led_delay_w1 = time_at_colour / 1023;
  }

  int diff_w2 = abs(W2 - W2_A);
  if (diff_w2 > 0) {
    led_delay_w2 = time_at_colour / abs(W2 - W2_A);
  } else {
    led_delay_w2 = time_at_colour / 1023;
  }
}

void LED_RED()
{
  if (fade) {
    if ((RED_A > RED && (RED_A - Settings.transitionSpeed > RED)) || (RED_A < RED && (RED_A + Settings.transitionSpeed < RED))) {
      if (RED_A > RED) RED_A = RED_A - Settings.transitionSpeed;
      if (RED_A < RED) RED_A = RED_A + Settings.transitionSpeed;
      analogWrite(redPIN, RED_A);
      currentRED = RED_A;
    } else {
      analogWrite(redPIN, RED);
    }
  } else {
    RED_A = RED;
    analogWrite(redPIN, RED);
    currentRED = RED;
  }
}

void LED_GREEN()
{
  if (fade) {
    if ((GREEN_A > GREEN && (GREEN_A - Settings.transitionSpeed > GREEN)) || (GREEN_A < GREEN && (GREEN_A + Settings.transitionSpeed < GREEN))) {
      if (GREEN_A > GREEN) GREEN_A = GREEN_A - Settings.transitionSpeed;
      if (GREEN_A < GREEN) GREEN_A = GREEN_A + Settings.transitionSpeed;
      analogWrite(greenPIN, GREEN_A);
      currentGREEN = GREEN_A;
    } else {
      analogWrite(greenPIN, GREEN);
    }
  } else {
    GREEN_A = GREEN;
    analogWrite(greenPIN, GREEN);
    currentGREEN = GREEN;
  }
}

void LED_BLUE()
{
  if (fade) {
    if ((BLUE_A > BLUE && (BLUE_A - Settings.transitionSpeed > BLUE)) || (BLUE_A < BLUE && (BLUE_A + Settings.transitionSpeed < BLUE))) {
      if (BLUE_A > BLUE) BLUE_A = BLUE_A - Settings.transitionSpeed;
      if (BLUE_A < BLUE) BLUE_A = BLUE_A + Settings.transitionSpeed;
      analogWrite(bluePIN, BLUE_A);
      currentBLUE = BLUE_A;
    } else {
      analogWrite(bluePIN, BLUE);
    }
  } else {
    BLUE_A = BLUE;
    analogWrite(bluePIN, BLUE);
    currentBLUE = BLUE;
  }
}

void LED_W1()
{
  if (fade) {
    if ((W1_A > W1 && (W1_A - Settings.transitionSpeed > W1)) || (W1_A < W1 && (W1_A + Settings.transitionSpeed < W1))) {
      if (W1_A > W1) W1_A = W1_A - Settings.transitionSpeed;
      if (W1_A < W1) W1_A = W1_A + Settings.transitionSpeed;
      analogWrite(w1PIN, W1_A);
      currentW1 = W1_A;
    } else {
      analogWrite(w1PIN, W1);
    }
  } else {
    W1_A = W1;
    analogWrite(w1PIN, W1);
    currentW1 = W1;
  }
}

void LED_W2()
{
  if (fade) {
    if ((W2_A > W2 && (W2_A - Settings.transitionSpeed > W2)) || (W2_A < W2 && (W2_A + Settings.transitionSpeed < W2))) {
      if (W2_A > W2) W2_A = W2_A - Settings.transitionSpeed;
      if (W2_A < W2) W2_A = W2_A + Settings.transitionSpeed;
      analogWrite(w2PIN, W2_A);
      currentW2 = W2_A;
    } else {
      analogWrite(w2PIN, W2);
    }
  } else {
    W2_A = W2;
    analogWrite(w2PIN, W2);
    currentW2 = W2;
  }
}

int convertToInt(char upper, char lower)
{
  int uVal = (int)upper;
  int lVal = (int)lower;
  uVal = uVal > 64 ? uVal - 55 : uVal - 48;
  uVal = uVal << 4;
  lVal = lVal > 64 ? lVal - 55 : lVal - 48;
  return uVal + lVal;
}

String getStatus() {
  if (getHex(RED) + getHex(GREEN) + getHex(BLUE) + getHex(W1) + getHex(W2) == "0000000000") {
    return "{\"rgb\":\"" + getHex(RED) + getHex(GREEN) + getHex(BLUE) + "\", \"r\":\"" + getHex(RED) + "\", \"g\":\"" + getHex(GREEN) + "\", \"b\":\"" + getHex(BLUE) + "\", \"w1\":\"" + getHex(W1) + "\", \"w2\":\"" + getHex(W2) + "\", \"power\":\"off\", \"running\":\"false\", \"program\":\"" + program_number + "\"}";
  } else if (run_program) {
    return "{\"running\":\"true\", \"program\":\"" + program_number + "\", \"power\":\"on\", \"uptime\":\"" + uptime() + "\"}";
  } else {
    return "{\"rgb\":\"" + getHex(RED) + getHex(GREEN) + getHex(BLUE) + "\", \"r\":\"" + getHex(RED) + "\", \"g\":\"" + getHex(GREEN) + "\", \"b\":\"" + getHex(BLUE) + "\", \"w1\":\"" + getHex(W1) + "\", \"w2\":\"" + getHex(W2) + "\", \"power\":\"on\", \"running\":\"false\", \"program\":\"" + program_number + "\"}";
  }
}

int getScaledValue(String hex) {
  hex.toUpperCase();
  char c[2];
  hex.toCharArray(c, 3);
  long value = convertToInt(c[0], c[1]);
  int intValue = map(value, 0, 255, 0, 1023);

  return intValue;

}

int getInt(String hex) {
  hex.toUpperCase();
  char c[2];
  hex.toCharArray(c, 3);
  return convertToInt(c[0], c[1]);
}

String getHex(int value) {
  if (value > 1018) {
    return "ff";
  } else if (value < 4) {
    return "00";
  } else {
    int intValue = map(value, 0, 1023, 0, 255) + 1;
    return padHex(String(intValue, HEX));
  }
}

String getStandard(int value) {
  if (value >= 1020) {
    return "255";
  } else {
    return String(round(value * 4 / 16));
  }
}

String padHex(String hex) {
  if (hex.length() == 1) {
    hex = "0" + hex;
  }
  return hex;
}

/*********************************************************************************************\
Tasks each 5 minutes
\*********************************************************************************************/
void runEach5Minutes()
{
  //timerwd = millis() + 1800000;
  timerwd = millis() + 300000;

  sendStatus();

}

boolean sendStatus(int number) {
  String authHeader = "";
  boolean success = false;
  String message = "";
  char host[20];
  sprintf_P(host, PSTR("%u.%u.%u.%u"), Settings.haIP[0], Settings.haIP[1], Settings.haIP[2], Settings.haIP[3]);

  //client.setTimeout(1000);
  if (Settings.haIP[0] + Settings.haIP[1] + Settings.haIP[2] + Settings.haIP[3] == 0) { // HA host is not configured
    return false;
  }
  if (connectionFailures >= 3) { // Too many errors; Trying not to get stuck
    if (millis() - failureTimeout < 1800000) {
      return false;
    } else {
      failureTimeout = millis();
    }
  }
  // Use WiFiClient class to create TCP connections
  WiFiClient client;
  if (!client.connect(host, Settings.haPort))
  {
    connectionFailures++;
    return false;
  }
  if (connectionFailures)
  connectionFailures = 0;

  switch (number) {
  case 0: {
      message = getStatus();
      break;
    }
  case 98: {
      message = "{\"version\":\"" + softwareVersion + "\", \"date\":\"" + compile_date + "\"}";
      break;
    }
  case 99: {
      message = "{\"uptime\":\"" + uptime() + "\"}";
      break;
    }
  }

  // We now create a URI for the request
  String url = F("/");
  //url += event->idx;

  client.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + ":" + Settings.haPort + "\r\n" + authHeader +
               "Content-Type: application/json;charset=utf-8\r\n" +
               "Content-Length: " + message.length() + "\r\n" +
               "Server: " + projectName + "\r\n" +
               "Connection: close\r\n\r\n" +
               message + "\r\n");

  unsigned long timer = millis() + 200;
  while (!client.available() && millis() < timer)
  delay(1);

  // Read all the lines of the reply from server and print them to Serial
  while (client.available()) {
    String line = client.readStringUntil('\n');
    if (line.substring(0, 15) == "HTTP/1.1 200 OK")
    {
      success = true;
    }
    delay(1);
  }

  client.flush();
  client.stop();

  return success;
}

boolean sendStatus() {
  sendStatus(0);
}

/********************************************************************************************\
Convert a char string to IP byte array
\*********************************************************************************************/
boolean str2ip(char *string, byte* IP)
{
  byte c;
  byte part = 0;
  int value = 0;

  for (int x = 0; x <= strlen(string); x++)
  {
    c = string[x];
    if (isdigit(c))
    {
      value *= 10;
      value += c - '0';
    }

    else if (c == '.' || c == 0) // next octet from IP address
    {
      if (value <= 255)
      IP[part++] = value;
      else
      return false;
      value = 0;
    }
    else if (c == ' ') // ignore these
    ;
    else // invalid token
    return false;
  }
  if (part == 4) // correct number of octets
  return true;
  return false;
}

String deblank(const char* input)
{
  String output = String(input);
  output.replace(" ", "");
  return output;
}

void SaveSettings(void)
{
  //Save any updates to the used pins
  Settings.redPIN_stored = redPIN;
  Settings.greenPIN_stored = greenPIN;
  Settings.bluePIN_stored = bluePIN;
  Settings.w1PIN_stored = w1PIN;
  Settings.w2PIN_stored = w2PIN;
  Settings.LEDPIN_stored = LEDPIN;
  Settings.LED2PIN_stored = LED2PIN;
  Settings.POWER_ENABLE_LED_stored = POWER_ENABLE_LED;
  Settings.KEY_PIN_stored = KEY_PIN;

  SaveToFlash(0, (byte*)&Settings, sizeof(struct SettingsStruct));
  SaveToFlash(32768, (byte*)&SecuritySettings, sizeof(struct SecurityStruct));
}

boolean LoadSettings()
{
  LoadFromFlash(0, (byte*)&Settings, sizeof(struct SettingsStruct));
  LoadFromFlash(32768, (byte*)&SecuritySettings, sizeof(struct SecurityStruct));

  //Now load them into used variables but only if valid pins - otherwise keep defaults (for H801)
  if (Settings.redPIN_stored >= 0 && Settings.redPIN_stored <= 16) redPIN = Settings.redPIN_stored;
  if (Settings.greenPIN_stored >= 0 && Settings.greenPIN_stored <= 16) greenPIN = Settings.greenPIN_stored;
  if (Settings.bluePIN_stored >= 0 && Settings.bluePIN_stored <= 16) bluePIN = Settings.bluePIN_stored;
  if (Settings.w1PIN_stored >= 0 && Settings.w1PIN_stored <= 16) w1PIN = Settings.w1PIN_stored;
  if (Settings.w2PIN_stored >= 0 && Settings.w2PIN_stored <= 16) w2PIN = Settings.w2PIN_stored;
  if (Settings.LEDPIN_stored >= 0 && Settings.LEDPIN_stored <= 16) LEDPIN = Settings.LEDPIN_stored;
  if (Settings.LED2PIN_stored >= 0 && Settings.LED2PIN_stored <= 16) LED2PIN = Settings.LED2PIN_stored;
  if (Settings.POWER_ENABLE_LED_stored >= 0 && Settings.POWER_ENABLE_LED_stored <= 16) POWER_ENABLE_LED = Settings.POWER_ENABLE_LED_stored;
  if (Settings.KEY_PIN_stored >= 0 && Settings.KEY_PIN_stored <= 16) KEY_PIN = Settings.KEY_PIN_stored;
}

/********************************************************************************************\
Save data to flash
\*********************************************************************************************/
void SaveToFlash(int index, byte* memAddress, int datasize)
{
  if (index > 33791) // Limit usable flash area to 32+1k size
  {
    return;
  }
  uint32_t _sector = ((uint32_t)&_SPIFFS_start - 0x40200000) / SPI_FLASH_SEC_SIZE;
  uint8_t* data = new uint8_t[FLASH_EEPROM_SIZE];
  int sectorOffset = index / SPI_FLASH_SEC_SIZE;
  int sectorIndex = index % SPI_FLASH_SEC_SIZE;
  uint8_t* dataIndex = data + sectorIndex;
  _sector += sectorOffset;

  // load entire sector from flash into memory
  noInterrupts();
  spi_flash_read(_sector * SPI_FLASH_SEC_SIZE, reinterpret_cast<uint32_t*>(data), FLASH_EEPROM_SIZE);
  interrupts();

  // store struct into this block
  memcpy(dataIndex, memAddress, datasize);

  noInterrupts();
  // write sector back to flash
  if (spi_flash_erase_sector(_sector) == SPI_FLASH_RESULT_OK)
  if (spi_flash_write(_sector * SPI_FLASH_SEC_SIZE, reinterpret_cast<uint32_t*>(data), FLASH_EEPROM_SIZE) == SPI_FLASH_RESULT_OK)
  {
    //Serial.println("flash save ok");
  }
  interrupts();
  delete [] data;
  //String log = F("FLASH: Settings saved");
  //addLog(LOG_LEVEL_INFO, log);
}


/********************************************************************************************\
Load data from flash
\*********************************************************************************************/
void LoadFromFlash(int index, byte* memAddress, int datasize)
{
  uint32_t _sector = ((uint32_t)&_SPIFFS_start - 0x40200000) / SPI_FLASH_SEC_SIZE;
  uint8_t* data = new uint8_t[FLASH_EEPROM_SIZE];
  int sectorOffset = index / SPI_FLASH_SEC_SIZE;
  int sectorIndex = index % SPI_FLASH_SEC_SIZE;
  uint8_t* dataIndex = data + sectorIndex;
  _sector += sectorOffset;

  // load entire sector from flash into memory
  noInterrupts();
  spi_flash_read(_sector * SPI_FLASH_SEC_SIZE, reinterpret_cast<uint32_t*>(data), FLASH_EEPROM_SIZE);
  interrupts();

  // load struct from this block
  memcpy(memAddress, dataIndex, datasize);
  delete [] data;
}

/** Load previous color from EEPROM */
void loadPreviousColor() {
  EEPROM.begin(512);
  EEPROM.get(0, previousColor);
  EEPROM.end();
  changeColor(String(previousColor), 8, (Settings.defaultTransition == 2? false : true));
}

/** Save previous color to EEPROM */
void savePreviousColor() {
  if (getHex(RED) + getHex(GREEN) + getHex(BLUE) + getHex(W1) + getHex(W2) != "0000000000"){
    strncpy(previousColor, (getHex(RED) + getHex(GREEN) + getHex(BLUE) + getHex(W1) + getHex(W2)).c_str(), sizeof(previousColor));
    EEPROM.begin(512);
    EEPROM.put(0, previousColor);
    EEPROM.commit();
    EEPROM.end();
  }
}

String uptime()
{
  currentmillis = millis();
  long days = 0;
  long hours = 0;
  long mins = 0;
  long secs = 0;
  secs = currentmillis / 1000; //convect milliseconds to seconds
  mins = secs / 60; //convert seconds to minutes
  hours = mins / 60; //convert minutes to hours
  days = hours / 24; //convert hours to days
  secs = secs - (mins * 60); //subtract the coverted seconds to minutes in order to display 59 secs max
  mins = mins - (hours * 60); //subtract the coverted minutes to hours in order to display 59 minutes max
  hours = hours - (days * 24); //subtract the coverted hours to days in order to display 23 hours max


  if (days > 0) // days will displayed only if value is greater than zero
  {
    return String(days) + " days and " + String(hours) + ":" + String(mins) + ":" + String(secs);
  } else {
    return String(hours) + ":" + String(mins) + ":" + String(secs);
  }
}



void EraseFlash()
{
  uint32_t _sectorStart = (ESP.getSketchSize() / SPI_FLASH_SEC_SIZE) + 1;
  uint32_t _sectorEnd = _sectorStart + (ESP.getFlashChipRealSize() / SPI_FLASH_SEC_SIZE);

  for (uint32_t _sector = _sectorStart; _sector < _sectorEnd; _sector++)
  {
    noInterrupts();
    if (spi_flash_erase_sector(_sector) == SPI_FLASH_RESULT_OK)
    {
      interrupts();
      Serial.print(F("FLASH: Erase Sector: "));
      Serial.println(_sector);
      delay(10);
    }
    interrupts();
  }
}

void ZeroFillFlash()
{
  // this will fill the SPIFFS area with a 64k block of all zeroes.
  uint32_t _sectorStart = ((uint32_t)&_SPIFFS_start - 0x40200000) / SPI_FLASH_SEC_SIZE;
  uint32_t _sectorEnd = _sectorStart + 16 ; //((uint32_t)&_SPIFFS_end - 0x40200000) / SPI_FLASH_SEC_SIZE;
  uint8_t* data = new uint8_t[FLASH_EEPROM_SIZE];

  uint8_t* tmpdata = data;
  for (int x = 0; x < FLASH_EEPROM_SIZE; x++)
  {
    *tmpdata = 0;
    tmpdata++;
  }


  for (uint32_t _sector = _sectorStart; _sector < _sectorEnd; _sector++)
  {
    // write sector to flash
    noInterrupts();
    if (spi_flash_erase_sector(_sector) == SPI_FLASH_RESULT_OK)
    if (spi_flash_write(_sector * SPI_FLASH_SEC_SIZE, reinterpret_cast<uint32_t*>(data), FLASH_EEPROM_SIZE) == SPI_FLASH_RESULT_OK)
    {
      interrupts();
      Serial.print(F("FLASH: Zero Fill Sector: "));
      Serial.println(_sector);
      delay(10);
    }
  }
  interrupts();
  delete [] data;
}

void addHeader(boolean showMenu, String& str)
{
  boolean cssfile = false;

  str += F("<script language=\"javascript\"><!--\n");
  str += F("function dept_onchange(frmselect) {frmselect.submit();}\n");
  str += F("//--></script>");
  str += F("<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/><title>");
  str += projectName;
  str += F("</title>");

  str += F("<style>");
  str += F("* {font-family:sans-serif; font-size:12pt;}");
  str += F("h1 {font-size:16pt; color:black;}");
  str += F("h6 {font-size:10pt; color:black; text-align:center;}");
  str += F(".button-menu {background-color:#ffffff; color:#000000; margin: 10px; text-decoration:none}");
  str += F(".button-link {border-radius:0.3rem; padding:5px 15px; background-color:#000000; color:#fff; border:solid 1px #fff; text-decoration:none}");
  str += F(".button-menu:hover {background:#ddddff;}");
  str += F(".button-link:hover {background:#707070;}");
  str += F("th {border-radius:0.3rem; padding:10px; background-color:black; color:#ffffff;}");
  str += F("td {padding:7px;}");
  str += F("table {color:black;}");
  str += F(".div_l {float: left;}");
  str += F(".div_r {float: right; margin: 2px; padding: 1px 10px; border-radius: 7px; background-color:#080; color:white;}");
  str += F(".div_br {clear: both;}");
  str += F("</style>");


  str += F("</head>");
  str += F("<center>");

}

void addFooter(String& str)
{
  str += F("<h6><a href=\"http://smartlife.tech\">smartlife.tech</a></h6></body></center>");
}

void addMenu(String& str)
{
  str += F("<a class=\"button-menu\" href=\".\">Main</a>");
  str += F("<a class=\"button-menu\" href=\"advanced\">Advanced</a>");
  str += F("<a class=\"button-menu\" href=\"control\">Control</a>");
  str += F("<a class=\"button-menu\" href=\"update\">Firmware</a>");
}

void turnOn(String& transition)
{
  if (transition != "true" && transition != "false") {
    transition = (Settings.defaultTransition == 2? "false" : "true");
  }

  String hexString = Settings.defaultColor;
  if (hexString == "") {
    changeColor("0000000000", 99, (transition != "false" && transition != "2"));
  } else if (hexString == "Previous") {
    //changeColor("0000000000", 99, (transition != "false" && transition != "2"));
    loadPreviousColor();
  } else {
    if (hexString.startsWith("w~")) {
      String hex = hexString.substring(2, 4);
      changeColor(hex, 4, (transition != "false" && transition != "2"));
    } else if (hexString.startsWith("x~")) {
      String hex = hexString.substring(2, 4);
      changeColor(hex, 4, (transition != "false" && transition != "2"));
    } else if (hexString.startsWith("y~")) {
      String hex = hexString.substring(2, 4);
      changeColor(hex, 5, true);
    } else if (hexString.startsWith("z~")) {
      String hex = hexString.substring(2, 4);
      changeColor(hex, 5, false);
    } else if (hexString.startsWith("f~")) {
      String hex = hexString.substring(2, 8);
      changeColor(hex, 6, (transition != "false" && transition != "2"));
    } else if (hexString.startsWith("d~")) {
      String hex = hexString.substring(2, 8);
      changeColor(hex, 6, (transition != "false" && transition != "2"));
    } else {
      //Here we take the actual default across all channels
      //String hex = hexString.substring(0, 10);
      changeColor(hexString, 8, (transition != "false" && transition != "2"));
    }
  }
}

void turnOff(String& transition)
{
  if (transition != "true" && transition != "false") {
    transition = (Settings.defaultTransition == 2? "false" : "true");
  }
  changeColor("0", 0, (transition != "false" && transition != "2"));
}

void setup()
{
  digitalWrite(12, 0);
  digitalWrite(13, 0);

  // Setup console
  Serial1.begin(115200);
  delay(20);
  Serial1.println();
  Serial1.println();

  LoadSettings();

  digitalWrite(LEDPIN, 0);
  digitalWrite(LED2PIN, 0);

  digitalWrite(redPIN, 0);
  digitalWrite(greenPIN, 0);
  digitalWrite(bluePIN, 0);
  digitalWrite(w1PIN, 0);
  digitalWrite(w2PIN, 0);
  digitalWrite(POWER_ENABLE_LED, 1);



  pinMode(LEDPIN, OUTPUT);
  pinMode(LED2PIN, OUTPUT);

  pinMode(redPIN, OUTPUT);
  pinMode(greenPIN, OUTPUT);
  pinMode(bluePIN, OUTPUT);
  pinMode(w1PIN, OUTPUT);
  pinMode(w2PIN, OUTPUT);
  pinMode(POWER_ENABLE_LED, OUTPUT);

  analogWrite(redPIN, 0);
  analogWrite(greenPIN, 0);
  analogWrite(bluePIN, 0);
  analogWrite(w1PIN, 0);
  analogWrite(w2PIN, 0);

  pinMode(KEY_PIN, INPUT_PULLUP);
  attachInterrupt(KEY_PIN, relayToggle, CHANGE);

  if (Settings.badBootCount == 0) {
    switch (Settings.powerOnState)
    {
    case 0: //Switch Off on Boot
      {
        break;
      }
    case 1: //Switch On on Boot
      {
        String transition = "";
        turnOn(transition);
        break;
      }
    case 2: //Saved State on Boot
      {
        //Dont use this any more as requires lots of flash writes
      }
      default : //Optional
      {

      }
    }
  }

  if (Settings.badBootCount == 1) {
    changeColor("ff", 2, false);
    LED_GREEN();
  }
  if (Settings.badBootCount == 2) {
    changeColor("ff", 3, false);
    LED_BLUE();
  }
  if (Settings.badBootCount >= 3) {
    changeColor("ff", 1, false);
    LED_RED();
  }

  if (Settings.resetType < 3) {
    Settings.badBootCount += 1;
    SaveSettings();
  }

  delay(5000);

  if (Settings.badBootCount > 3) {
    Settings.reallyLongPress = true;
  }

  if (Settings.longPress == true) {
    for (uint8_t i = 0; i < 3; i++) {
      LEDoff;
      delay(250);
      LEDon;
      delay(250);
    }
    Settings.longPress = false;
    Settings.useStatic = false;
    Settings.resetWifi = true;
    SaveSettings();
    LEDoff;
  }

  if (Settings.reallyLongPress == true) {
    for (uint8_t i = 0; i < 5; i++) {
      LEDoff;
      delay(1000);
      LEDon;
      delay(1000);
    }
    EraseFlash();
    ZeroFillFlash();
    ESP.restart();
  }

  boolean saveSettings = false;

  if (Settings.badBootCount != 0) {
    Settings.badBootCount = 0;
    saveSettings = true;
  }

  if (SecuritySettings.settingsVersion > 500) {
    //Here we reset all settings as clearly the version has been corrupted
    SecuritySettings.settingsVersion = 200;
    saveSettings = true;
  }

  if (SecuritySettings.settingsVersion < 209) {
    str2ip((char*)DEFAULT_HAIP, Settings.haIP);
    Settings.haPort = DEFAULT_HAPORT;
    Settings.resetWifi = DEFAULT_RESETWIFI;
    Settings.powerOnState = DEFAULT_POS;
    str2ip((char*)DEFAULT_IP, Settings.IP);
    str2ip((char*)DEFAULT_SUBNET, Settings.Subnet);
    str2ip((char*)DEFAULT_GATEWAY, Settings.Gateway);
    Settings.useStatic = DEFAULT_USE_STATIC;
    Settings.usePassword = DEFAULT_USE_PASSWORD;
    Settings.usePasswordControl = DEFAULT_USE_PASSWORD_CONTROL;
    Settings.longPress = DEFAULT_LONG_PRESS;
    Settings.reallyLongPress = DEFAULT_REALLY_LONG_PRESS;
    strncpy(SecuritySettings.Password, DEFAULT_PASSWORD, sizeof(SecuritySettings.Password));
    Settings.disableJ3Reset = DEFAULT_DISABLE_J3_RESET;
    Settings.switchType = DEFAULT_SWITCH_TYPE;
    Settings.transitionSpeed = DEFAULT_TRANSITION_SPEED;
    Settings.autoOff = DEFAULT_AUTO_OFF;
    Settings.continueBoot = DEFAULT_CONTINUE_BOOT;
    Settings.resetType = DEFAULT_RESET_TYPE;
    Settings.uReport =  DEFAULT_UREPORT;
    Settings.debounce = DEFAULT_DEBOUNCE;
    SecuritySettings.settingsVersion = 209;
    saveSettings = true;
  }

  if (SecuritySettings.settingsVersion < 210) {
    strncpy(Settings.defaultColor, DEFAULT_COLOR, sizeof(Settings.defaultColor));
    Settings.badBootCount = DEFAULT_BAD_BOOT_COUNT;
    str2ip((char*)DEFAULT_IP, Settings.forward_address);
    SecuritySettings.settingsVersion = 210;
    saveSettings = true;
  }

  if (SecuritySettings.settingsVersion < 215) {
    Settings.defaultTransition = DEFAULT_TRANSITION_TYPE;
    SecuritySettings.settingsVersion = 215;
    saveSettings = true;
  }

  if (saveSettings == true) {
    SaveSettings();
  }

  WiFiManager wifiManager;

  wifiManager.setConnectTimeout(30);
  wifiManager.setConfigPortalTimeout(300);

  if (Settings.useStatic == true) {
    wifiManager.setSTAStaticIPConfig(Settings.IP, Settings.Gateway, Settings.Subnet);
  }

  if (Settings.resetWifi == true) {
    wifiManager.resetSettings();
    Settings.resetWifi = false;
    SaveSettings();
  }

  if (Settings.continueBoot == true) {
    //    wifiManager.setContinueAfterTimeout(true);
  }

  LEDon;
  LED2off;

  WiFi.macAddress(mac);
  String apSSID = "espRGBW." + String(mac[0], HEX) + String(mac[1], HEX) + String(mac[2], HEX) + String(mac[3], HEX) + String(mac[4], HEX) + String(mac[5], HEX);

  if (!wifiManager.autoConnect(apSSID.c_str(), "configme")) {
    Serial.println("failed to connect, we should reset as see if it connects");
    LED2on;
    delay(500);
    LED2off;
    delay(3000);
    LED2on;
    delay(500);
    LED2off;
    ESP.restart();
  }

  LED2on;

  Serial1.println("");
  server.reset(new ESP8266WebServer(WiFi.localIP(), 80));

  //server->on("/", handleRoot);

  server->on("/reset", []() {
    server->send(200, "application/json", "{\"message\":\"wifi settings are being removed\"}");
    Settings.reallyLongPress = true;
    SaveSettings();
    ESP.restart();
  });


  server->on("/description.xml", HTTP_GET, []() {
    SSDP.schema(server->client());
  });

  server->on("/reboot", []() {
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }
    server->send(200, "application/json", "{\"message\":\"device is rebooting\"}");
    ESP.restart();
  });

  server->on("/", []() {

    char tmpString[64];

    String reply = "";
    char str[20];
    addHeader(true, reply);
    reply += F("<table>");
    reply += F("<TH colspan='2'>");
    reply += projectName;
    reply += F(" Main");
    reply += F("<TR><TD><TD><TR><TD colspan='2' align='center'>");
    addMenu(reply);

    reply += F("<TR><TD><TD><TR><TD>Main:");

    reply += F("<TD><a href='/advanced'>Advanced Config</a><BR>");
    reply += F("<a href='/control'>Control</a><BR>");
    reply += F("<a href='/update'>Firmware Update</a><BR>");
    reply += F("<a href='http://tiny.cc/wzwzdy'>Documentation</a><BR>");
    reply += F("<a href='/reboot'>Reboot</a><BR>");

    reply += F("<TR><TD>JSON Endpoints:");

    reply += F("<TD><a href='/status'>status</a><BR>");
    reply += F("<a href='/configGet'>configGet</a><BR>");
    reply += F("<a href='/configSet'>configSet</a><BR>");
    reply += F("<a href='/rgb'>rgb</a><BR>");
    reply += F("<a href='/r'>r</a><BR>");
    reply += F("<a href='/g'>g</a><BR>");
    reply += F("<a href='/b'>b</a><BR>");
    reply += F("<a href='/w1'>w1</a><BR>");
    reply += F("<a href='/w2'>w2</a><BR>");
    reply += F("<a href='/on'>on</a><BR>");
    reply += F("<a href='/off'>off</a><BR>");
    reply += F("<a href='/program'>program</a><BR>");
    reply += F("<a href='/stop'>stop</a><BR>");
    reply += F("<a href='/info'>info</a><BR>");
    reply += F("<a href='/reboot'>reboot</a><BR>");

    reply += F("</table>");
    addFooter(reply);
    server->send(200, "text/html", reply);
  });

  server->on("/info", []() {
    server->send(200, "application/json", "{\"version\":\"" + softwareVersion + "\", \"date\":\"" + compile_date + "\", \"mac\":\"" + padHex(String(mac[0], HEX)) + padHex(String(mac[1], HEX)) + padHex(String(mac[2], HEX)) + padHex(String(mac[3], HEX)) + padHex(String(mac[4], HEX)) + padHex(String(mac[5], HEX)) + "\"}");
  });

  server->on("/program", []() {
    program = server->arg("value");
    repeat = server->arg("repeat").toInt();
    program_number = server->arg("number");
    program_off = server->arg("off");
    repeat_count = 1;
    program_wait = 0;
    run_program = true;

    pre_program = getHex(RED) + getHex(GREEN) + getHex(BLUE) + getHex(W1) + getHex(W2);

    //Check if we should forward the request to another device
    check_if_forward_request();

    //If we received this request from outside of ST, then lets update ST
    byte current_add[4] = {server->client().remoteIP()};
    if ((current_add[0] != Settings.haIP[0]) || (current_add[1] != Settings.haIP[1]) || (current_add[2] != Settings.haIP[2]) || (current_add[3] != Settings.haIP[3])) {
      needUpdate = true;
    }
    
    server->send(200, "application/json", "{\"running\":\"true\", \"program\":\"" + program_number + "\", \"power\":\"on\"}");
  });

  server->on("/stop", []() {
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }
    run_program = false;

    //Check if we should forward the request to another device
    check_if_forward_request();

    //If we received this request from outside of ST, then lets update ST
    byte current_add[4] = {server->client().remoteIP()};
    if ((current_add[0] != Settings.haIP[0]) || (current_add[1] != Settings.haIP[1]) || (current_add[2] != Settings.haIP[2]) || (current_add[3] != Settings.haIP[3])) {
      needUpdate = true;
    }
    
    server->send(200, "application/json", "{\"program\":\"" + program_number + "\", \"running\":\"false\"}");
  });

  server->on("/off", []() {
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }
    String transition = server->arg("transition");
    run_program = false;

    turnOff(transition);

    //Check if we should forward the request to another device
    check_if_forward_request();

    //If we received this request from outside of ST, then lets update ST
    byte current_add[4] = {server->client().remoteIP()};
    if ((current_add[0] != Settings.haIP[0]) || (current_add[1] != Settings.haIP[1]) || (current_add[2] != Settings.haIP[2]) || (current_add[3] != Settings.haIP[3])) {
      needUpdate = true;
    }
  
    server->send(200, "application/json", getStatus());
    
  });

  server->on("/on", []() {
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }
    String transition = server->arg("transition");
    run_program = false;

    turnOn(transition);

    //Check if we should forward the request to another device
    check_if_forward_request();

    //If we received this request from outside of ST, then lets update ST
    byte current_add[4] = {server->client().remoteIP()};
    if ((current_add[0] != Settings.haIP[0]) || (current_add[1] != Settings.haIP[1]) || (current_add[2] != Settings.haIP[2]) || (current_add[3] != Settings.haIP[3])) {
      needUpdate = true;
    }
  });

  server->on("/configGet", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePassword == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }

    char tmpString[64];
    boolean success = false;
    String configName = server->arg("name");
    String reply = "";
    char str[20];

    if (configName == "haip") {
      sprintf_P(str, PSTR("%u.%u.%u.%u"), Settings.haIP[0], Settings.haIP[1], Settings.haIP[2], Settings.haIP[3]);
      reply += "{\"name\":\"haip\", \"value\":\"" + String(str) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "haport") {
      reply += "{\"name\":\"haport\", \"value\":\"" + String(Settings.haPort) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "pos") {
      reply += "{\"name\":\"pos\", \"value\":\"" + String(Settings.powerOnState) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "autooff") {
      reply += "{\"name\":\"autooff\", \"value\":\"" + String(Settings.autoOff) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "transitionspeed") {
      reply += "{\"name\":\"transitionspeed\", \"value\":\"" + String(Settings.transitionSpeed) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "dtransition") {
      reply += "{\"name\":\"dtransition\", \"value\":\"" + String(Settings.defaultTransition) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "dcolor") {
      reply += "{\"name\":\"dcolor\", \"value\":\"" + String(Settings.defaultColor) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "ureport") {
      reply += "{\"name\":\"ureport\", \"value\":\"" + String(Settings.uReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }

    if (configName == "redpin") {
      reply += "{\"name\":\"redpin\", \"value\":\"" + String(Settings.redPIN_stored) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "greenpin") {
      reply += "{\"name\":\"greenpin\", \"value\":\"" + String(Settings.greenPIN_stored) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "bluepin") {
      reply += "{\"name\":\"bluepin\", \"value\":\"" + String(Settings.bluePIN_stored) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "w1pin") {
      reply += "{\"name\":\"w1pin\", \"value\":\"" + String(Settings.w1PIN_stored) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "w2pin") {
      reply += "{\"name\":\"w2pin\", \"value\":\"" + String(Settings.w2PIN_stored) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "ledpin") {
      reply += "{\"name\":\"ledpin\", \"value\":\"" + String(Settings.POWER_ENABLE_LED_stored) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "led2pin") {
      reply += "{\"name\":\"led2pin\", \"value\":\"" + String(Settings.LEDPIN_stored) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "pwrenablepin") {
      reply += "{\"name\":\"pwrenablepin\", \"value\":\"" + String(Settings.LED2PIN_stored) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "keypin") {
      reply += "{\"name\":\"keypin\", \"value\":\"" + String(Settings.KEY_PIN_stored) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "ipforward") {
      sprintf_P(str, PSTR("%u.%u.%u.%u"), Settings.forward_address[0], Settings.forward_address[1], Settings.forward_address[2], Settings.forward_address[3]);
      reply += "{\"name\":\"ipforward\", \"value\":\"" + String(str) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }

    //H801 specific stuff originally - now open to others
    if (configName == "switchtype") {
      reply += "{\"name\":\"switchtype\", \"value\":\"" + String(Settings.switchType) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "resettype") {
      reply += "{\"name\":\"resettype\", \"value\":\"" + String(Settings.resetType) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "debounce") {
      reply += "{\"name\":\"debounce\", \"value\":\"" + String(Settings.debounce) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }

    if ( reply != "" ) {
      server->send(200, "application/json", reply);
    } else {
      server->send(200, "application/json", "{\"success\":\"false\", \"type\":\"configuration\"}");
    }
  });

  server->on("/configSet", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePassword == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }

    char tmpString[64];
    boolean success = false;
    String configName = server->arg("name");
    String configValue = server->arg("value");
    String reply = "";
    char str[20];

    if (configName == "haip") {
      if (configValue.length() != 0)
      {
        if (configValue != String(Settings.haIP[0]) + "." + String(Settings.haIP[1]) + "." + String(Settings.haIP[2]) + "." + String(Settings.haIP[3])) {
          needFirmware = true;
        }
        configValue.toCharArray(tmpString, 26);
        str2ip(tmpString, Settings.haIP);
      }
      reply += "{\"name\":\"haip\", \"value\":\"" + String(tmpString) + "\", \"success\":\"true\"}";
    }
    if (configName == "haport") {
      if (configValue.length() != 0)
      {
        Settings.haPort = configValue.toInt();
      }
      reply += "{\"name\":\"haport\", \"value\":\"" + String(Settings.haPort) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "pos") {
      if (configValue.length() != 0)
      {
        Settings.powerOnState = configValue.toInt();
      }
      reply += "{\"name\":\"pos\", \"value\":\"" + String(Settings.powerOnState) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "autooff") {
      if (configValue.length() != 0)
      {
        Settings.autoOff = configValue.toInt();
      }
      reply += "{\"name\":\"autooff\", \"value\":\"" + String(Settings.autoOff) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "transitionspeed") {
      if (configValue.length() != 0)
      {
        Settings.transitionSpeed = configValue.toInt();
      }
      reply += "{\"name\":\"transitionspeed\", \"value\":\"" + String(Settings.transitionSpeed) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "dtransition") {
      if (configValue.length() != 0)
      {
        Settings.defaultTransition = configValue.toInt();
      }
      reply += "{\"name\":\"dtransition\", \"value\":\"" + String(Settings.defaultTransition) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "dcolor") {
      if (configValue.length() != 0)
      {
        strncpy(Settings.defaultColor, configValue.c_str(), sizeof(Settings.defaultColor));
      }
      reply += "{\"name\":\"dcolor\", \"value\":\"" + String(Settings.defaultColor) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "uReport") {
      if (configValue.length() != 0)
      {
        if (configValue.toInt() != Settings.uReport) {
          Settings.uReport = configValue.toInt();
          timerUptime = millis() + Settings.uReport * 1000;
        }
      }
      reply += "{\"name\":\"ureport\", \"value\":\"" + String(Settings.uReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }

    //Originally H801 specific now open to all
    if (configName == "switchtype") {
      if (configValue.length() != 0)
      {
        Settings.switchType = configValue.toInt();
      }
      reply += "{\"name\":\"switchtype\", \"value\":\"" + String(Settings.switchType) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "resettype") {
      if (configValue.length() != 0)
      {
        Settings.resetType = configValue.toInt();
      }
      reply += "{\"name\":\"resetype\", \"value\":\"" + String(Settings.resetType) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "debounce") {
      if (configValue.length() != 0)
      {
        Settings.debounce = configValue.toInt();
      }
      reply += "{\"name\":\"debounce\", \"value\":\"" + String(Settings.debounce) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "autooff") {
      if (configValue.length() != 0)
      {
        Settings.autoOff = configValue.toInt();
      }
      reply += "{\"name\":\"autooff\", \"value\":\"" + String(Settings.autoOff) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }

    //Now the pin number parts
    if (configName == "redpin") {
      if (configValue.length() != 0)
      {
        redPIN = configValue.toInt();
      }
      reply += "{\"name\":\"redpin\", \"value\":\"" + String(redPIN) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "greenpin") {
      if (configValue.length() != 0)
      {
        greenPIN = configValue.toInt();
      }
      reply += "{\"name\":\"greenpin\", \"value\":\"" + String(greenPIN) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "bluepin") {
      if (configValue.length() != 0)
      {
        bluePIN = configValue.toInt();
      }
      reply += "{\"name\":\"bluepin\", \"value\":\"" + String(bluePIN) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "w1pin") {
      if (configValue.length() != 0)
      {
        w1PIN = configValue.toInt();
      }
      reply += "{\"name\":\"w1pin\", \"value\":\"" + String(w1PIN) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "w2pin") {
      if (configValue.length() != 0)
      {
        w2PIN = configValue.toInt();
      }
      reply += "{\"name\":\"w2pin\", \"value\":\"" + String(w2PIN) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "ledpin") {
      if (configValue.length() != 0)
      {
        LEDPIN = configValue.toInt();
      }
      reply += "{\"name\":\"ledpin\", \"value\":\"" + String(LEDPIN) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "led2pin") {
      if (configValue.length() != 0)
      {
        LED2PIN = configValue.toInt();
      }
      reply += "{\"name\":\"led2pin\", \"value\":\"" + String(LED2PIN) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "pwrenablepin") {
      if (configValue.length() != 0)
      {
        POWER_ENABLE_LED = configValue.toInt();
      }
      reply += "{\"name\":\"pwrenablepin\", \"value\":\"" + String(POWER_ENABLE_LED) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "keypin") {
      if (configValue.length() != 0)
      {
        KEY_PIN = configValue.toInt();
      }
      reply += "{\"name\":\"keypin\", \"value\":\"" + String(KEY_PIN) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "ipforward") {
      if (configValue.length() != 0)
      {
        if (configValue != String(Settings.forward_address[0]) + "." + String(Settings.forward_address[1]) + "." + String(Settings.forward_address[2]) + "." + String(Settings.forward_address[3])) {
          needFirmware = true;
        }
        configValue.toCharArray(tmpString, 26);
        str2ip(tmpString, Settings.forward_address);
      }
      reply += "{\"name\":\"haip\", \"value\":\"" + String(tmpString) + "\", \"success\":\"true\"}";
    }

    if ( reply != "" ) {
      SaveSettings();
      server->send(200, "application/json", reply);
    } else {
      server->send(200, "application/json", "{\"success\":\"false\", \"type\":\"configuration\"}");
    }
  });


  server->on("/status", []() {
    server->send(200, "application/json", getStatus());
  });

  server->on("/advanced", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePassword == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }

    char tmpString[64];
    String haIP = server->arg("haip");
    String haPort = server->arg("haport");
    String pos = server->arg("pos");
    String ip = server->arg("ip");
    String gateway = server->arg("gateway");
    String subnet = server->arg("subnet");
    String dns = server->arg("dns");
    String usestatic = server->arg("usestatic");
    String usepassword = server->arg("usepassword");
    String usepasswordcontrol = server->arg("usepasswordcontrol");
    String username = server->arg("username");
    String password = server->arg("password");
    String disableJ3reset = server->arg("disableJ3reset");
    String transitionSpeed = server->arg("transitionspeed");
    String continueBoot = server->arg("continueboot");
    String autoOff = server->arg("autooff");
    String resettype = server->arg("resettype");
    String uReport = server->arg("ureport");
    String debounce = server->arg("debounce");
    String defaultTransition = server->arg("dtransition");
    String dcolor = server->arg("dcolor");
    String redpin = server->arg("redpin");
    String greenpin = server->arg("greenpin");
    String bluepin = server->arg("bluepin");
    String w1pin = server->arg("w1pin");
    String w2pin = server->arg("w2pin");
    String ledpin = server->arg("ledpin");
    String led2pin = server->arg("led2pin");
    String pwrenablepin = server->arg("pwrenablepin");
    String keypin = server->arg("keypin");
    String ipforward = server->arg("ipforward");

    if (haPort.length() != 0)
    {
      Settings.haPort = haPort.toInt();
    }

    if (pos.length() != 0)
    {
      Settings.powerOnState = pos.toInt();
    }

    if (transitionSpeed.length() != 0)
    {
      Settings.transitionSpeed = transitionSpeed.toInt();
    }

    if (haIP.length() != 0)
    {
      if (haIP != String(Settings.haIP[0]) + "." + String(Settings.haIP[1]) + "." + String(Settings.haIP[2]) + "." + String(Settings.haIP[3])) {
        needFirmware = true;
      }
      haIP.toCharArray(tmpString, 26);
      str2ip(tmpString, Settings.haIP);
    }

    if (ip.length() != 0 && subnet.length() != 0)
    {
      ip.toCharArray(tmpString, 26);
      str2ip(tmpString, Settings.IP);
      subnet.toCharArray(tmpString, 26);
      str2ip(tmpString, Settings.Subnet);
    }

    if (gateway.length() != 0)
    {
      gateway.toCharArray(tmpString, 26);
      str2ip(tmpString, Settings.Gateway);
    }

    if (dns.length() != 0)
    {
      dns.toCharArray(tmpString, 26);
      str2ip(tmpString, Settings.DNS);
    }

    if (usestatic.length() != 0)
    {
      Settings.useStatic = (usestatic == "yes");
    }

    if (usepassword.length() != 0)
    {
      Settings.usePassword = (usepassword == "yes");
    }

    if (usepasswordcontrol.length() != 0)
    {
      Settings.usePasswordControl = (usepasswordcontrol == "yes");
    }

    if (password.length() != 0)
    {
      strncpy(SecuritySettings.Password, password.c_str(), sizeof(SecuritySettings.Password));
    }

    if (disableJ3reset.length() != 0)
    {
      Settings.disableJ3Reset = (disableJ3reset == "true");
    }

    if (resettype.length() != 0)
    {
      Settings.resetType = resettype.toInt();
    }

    if (continueBoot.length() != 0)
    {
      Settings.continueBoot = (continueBoot == "yes");
    }

    if (autoOff.length() != 0)
    {
      Settings.autoOff = autoOff.toInt();
    }

    if (defaultTransition.length() != 0)
    {
      Settings.defaultTransition = defaultTransition.toInt();
    }

    if (dcolor.length() != 0)
    {
      strncpy(Settings.defaultColor, dcolor.c_str(), sizeof(Settings.defaultColor));
    }

    if (redpin.length() != 0)
    {
      redPIN = redpin.toInt();
    }

    if (greenpin.length() != 0)
    {
      greenPIN = greenpin.toInt();
    }

    if (bluepin.length() != 0)
    {
      bluePIN = bluepin.toInt();
    }

    if (w1pin.length() != 0)
    {
      w1PIN = w1pin.toInt();
    }

    if (w2pin.length() != 0)
    {
      w2PIN = w2pin.toInt();
    }

    if (ledpin.length() != 0)
    {
      LEDPIN = ledpin.toInt();
    }

    if (led2pin.length() != 0)
    {
      LED2PIN = led2pin.toInt();
    }

    if (pwrenablepin.length() != 0)
    {
      POWER_ENABLE_LED = pwrenablepin.toInt();
    }

    if (keypin.length() != 0)
    {
      KEY_PIN = keypin.toInt();
    }
    
    if (ipforward.length() != 0)
    {
      ipforward.toCharArray(tmpString, 26);
      str2ip(tmpString, Settings.forward_address);
    }


    if (uReport.length() != 0)
    {
      if (uReport.toInt() != Settings.uReport) {
        Settings.uReport = uReport.toInt();
        timerUptime = millis() + Settings.uReport * 1000;
      }
    }

    if (debounce.length() != 0)
    {
      Settings.debounce = debounce.toInt();
    }

    SaveSettings();

    String reply = "";
    char str[20];
    addHeader(true, reply);

    reply += F("<script src='http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js'></script>");

    reply += F("<form name='frmselect' class='form' method='post'><table>");
    reply += F("<TH colspan='2'>");
    reply += projectName;
    reply += F(" Settings");
    reply += F("<TR><TD><TD><TR><TD colspan='2' align='center'>");
    addMenu(reply);

    reply += F("<TR><TD><TD><TR><TD>Password Protect<BR><BR>Configuration:<TD><BR><BR>");

    reply += F("<input type='radio' name='usepassword' value='yes'");
    if (Settings.usePassword)
    reply += F(" checked ");
    reply += F(">Yes");
    reply += F("</input>");

    reply += F("<input type='radio' name='usepassword' value='no'");
    if (!Settings.usePassword)
    reply += F(" checked ");
    reply += F(">No");
    reply += F("</input>");

    reply += F("<TR><TD>Control:<TD>");

    reply += F("<input type='radio' name='usepasswordcontrol' value='yes'");
    if (Settings.usePasswordControl)
    reply += F(" checked ");
    reply += F(">Yes");
    reply += F("</input>");

    reply += F("<input type='radio' name='usepasswordcontrol' value='no'");
    if (!Settings.usePasswordControl)
    reply += F(" checked ");
    reply += F(">No");
    reply += F("</input>");

    reply += F("<TR><TD>\"admin\" Password:<TD><input type='password' id='user_password' name='password' value='");
    SecuritySettings.Password[25] = 0;
    reply += SecuritySettings.Password;

    reply += F("'><input type='checkbox' id='showPassword' name='show' value='Show'> Show");

    reply += F("<script type='text/javascript'>");

    reply += F("$(\"#showPassword\").click(function() {");
    reply += F("var showPasswordCheckBox = document.getElementById(\"showPassword\");");
    reply += F("$('.form').find(\"#user_password\").each(function() {");
    reply += F("if(showPasswordCheckBox.checked){");
    reply += F("$(\"<input type='text' />\").attr({ name: this.name, value: this.value, id: this.id}).insertBefore(this);");
    reply += F("}else{");
    reply += F("$(\"<input type='password' />\").attr({ name: this.name, value: this.value, id: this.id }).insertBefore(this);");
    reply += F("}");
    reply += F("}).remove();");
    reply += F("});");

    reply += F("$(document).ready(function() {");
    reply += F("$(\"#user_password_checkbox\").click(function() {");
    reply += F("if ($('input.checkbox_check').attr(':checked')); {");
    reply += F("$(\"#user_password\").attr('type', 'text');");
    reply += F("}});");
    reply += F("});");

    reply += F("</script>");

    reply += F("<TR><TD>Static IP:<TD>");

    reply += F("<input type='radio' name='usestatic' value='yes'");
    if (Settings.useStatic)
    reply += F(" checked ");
    reply += F(">Yes");
    reply += F("</input>");

    reply += F("<input type='radio' name='usestatic' value='no'");
    if (!Settings.useStatic)
    reply += F(" checked ");
    reply += F(">No");
    reply += F("</input>");


    reply += F("<TR><TD>IP:<TD><input type='text' name='ip' value='");
    sprintf_P(str, PSTR("%u.%u.%u.%u"), Settings.IP[0], Settings.IP[1], Settings.IP[2], Settings.IP[3]);
    reply += str;

    reply += F("'><TR><TD>Subnet:<TD><input type='text' name='subnet' value='");
    sprintf_P(str, PSTR("%u.%u.%u.%u"), Settings.Subnet[0], Settings.Subnet[1], Settings.Subnet[2], Settings.Subnet[3]);
    reply += str;

    reply += F("'><TR><TD>Gateway:<TD><input type='text' name='gateway' value='");
    sprintf_P(str, PSTR("%u.%u.%u.%u"), Settings.Gateway[0], Settings.Gateway[1], Settings.Gateway[2], Settings.Gateway[3]);
    reply += str;

    //reply += F("'><TR><TD>DNS:<TD><input type='text' name='dns' value='");
    //sprintf_P(str, PSTR("%u.%u.%u.%u"), Settings.DNS[0], Settings.DNS[1], Settings.DNS[2], Settings.DNS[3]);
    //reply += str;

    reply += F("'><TR><TD>HA Controller IP:<TD><input type='text' name='haip' value='");
    sprintf_P(str, PSTR("%u.%u.%u.%u"), Settings.haIP[0], Settings.haIP[1], Settings.haIP[2], Settings.haIP[3]);
    reply += str;

    reply += F("'><TR><TD>HA Controller Port:<TD><input type='text' name='haport' value='");
    reply += Settings.haPort;
    reply += F("'>");

    byte choice = Settings.powerOnState;
    reply += F("<TR><TD>Boot Up State:<TD><select name='");
    reply += "pos";
    reply += "'>";
    if (choice == 0) {
      reply += F("<option value='0' selected>Off</option>");
    } else {
      reply += F("<option value='0'>Off</option>");
    }
    if (choice == 1) {
      reply += F("<option value='1' selected>On</option>");
    } else {
      reply += F("<option value='1'>On</option>");
    }
    //if (choice == 2){
    //  reply += F("<option value='2' selected>Previous State</option>");
    //} else {
    //  reply += F("<option value='2'>Previous State</option>");
    //}
    reply += F("</select>");

    choice = Settings.defaultTransition;
    reply += F("<TR><TD>Default Transition:<TD><select name='");
    reply += "dtransition";
    reply += "'>";
    if (choice == 1) {
      reply += F("<option value='1' selected>Fade</option>");
    } else {
      reply += F("<option value='1'>Fade</option>");
    }
    if (choice == 2) {
      reply += F("<option value='2' selected>Flash</option>");
    } else {
      reply += F("<option value='2'>Flash</option>");
    }
    reply += F("</select>");

    choice = Settings.transitionSpeed;
    reply += F("<TR><TD>Transition Speed:<TD><select name='");
    reply += "transitionspeed";
    reply += "'>";
    if (choice == 1) {
      reply += F("<option value='1' selected>Slow</option>");
    } else {
      reply += F("<option value='1'>Slow</option>");
    }
    if (choice == 2) {
      reply += F("<option value='2' selected>Medium</option>");
    } else {
      reply += F("<option value='2'>Medium</option>");
    }
    if (choice == 3) {
      reply += F("<option value='3' selected>Fast</option>");
    } else {
      reply += F("<option value='3'>Fast</option>");
    }
    reply += F("</select>");

    reply += F("<TR><TD>Default Color (Previous or RRGGBBW1W2 as hex - e.g. FFFFFFFFFF):<TD><input type='text' name='dcolor' value='");
    reply += Settings.defaultColor;
    reply += F("'>");

    //Debounce stuff, may not be used but included anyway
    choice = Settings.resetType;
    reply += F("<TR><TD>Reset Type:<TD><select name='");
    reply += "resettype";
    reply += "'>";
    if (choice == 1) {
      reply += F("<option value='1' selected>Both</option>");
    } else {
      reply += F("<option value='1'>Both</option>");
    }
    if (choice == 2) {
      reply += F("<option value='2' selected>Unplug Sequence</option>");
    } else {
      reply += F("<option value='2'>Unplug Sequence</option>");
    }
    if (choice == 3) {
      reply += F("<option value='3' selected>J3 Jumper</option>");
    } else {
      reply += F("<option value='3'>J3 Jumper</option>");
    }
    reply += F("</select>");

    reply += F("<TR><TD>Switch Debounce:<TD><input type='text' name='debounce' value='");
    reply += Settings.debounce;
    reply += F("'>");

    reply += F("<TR><TD>Uptime Report Interval:<TD><input type='text' name='ureport' value='");
    reply += Settings.uReport;
    reply += F("'>");
    reply += F("<TR><TD>Auto Off:<TD><input type='text' name='autooff' value='");
    reply += Settings.autoOff;
    reply += F("'>");
    
    reply += F("<TR><TD>Forward commands to IP Addr:<TD><input type='text' name='ipforward' value='");
    sprintf_P(str, PSTR("%u.%u.%u.%u"), Settings.forward_address[0], Settings.forward_address[1], Settings.forward_address[2], Settings.forward_address[3]);
    reply += str;
    reply += F("'>");

    reply += F("<TR><TD>Output pin numbers - may need a reboot after changing (example numbering below):");
    reply += F("<TR><TD>H801<TD>R:15, G:13, B:12, W1:14, W2:4, LED:5, LED2:1");
    reply += F("<TR><TD>Light<TD>R:13, G:12, B:14, W1:2, W2:4, PWR_EN:15");
    reply += F("<TR><TD>AL-LC01/02<TD>R:14, G:5, B:12, W1:13, W2:15");
    reply += F("<TR><TD>AL-LC05<TD>R:13, G:12, B:14, W1:5, W2:15");
    reply += F("<TR><TD>AL-LC08<TD>R:5, G:4, B:14, W1:12, W2:13");
    reply += F("<TR><TD>AL-ALC10<TD>R:5, G:14, B:12, W1:13, W2:15");
    reply += F("<TR><TD>For the undefined pins just pick an unused one from 2/4/5/12/13/14/15");

    reply += F("<TR><TD>Red Pin Number:<TD><input type='text' name='redpin' value='");
    reply += Settings.redPIN_stored;
    reply += F("'>");
    reply += F("<TR><TD>Green Pin Number:<TD><input type='text' name='greenpin' value='");
    reply += Settings.greenPIN_stored;
    reply += F("'>");
    reply += F("<TR><TD>Blue Pin Number:<TD><input type='text' name='bluepin' value='");
    reply += Settings.bluePIN_stored;
    reply += F("'>");
    reply += F("<TR><TD>W1 Pin Number:<TD><input type='text' name='w1pin' value='");
    reply += Settings.w1PIN_stored;
    reply += F("'>");
    reply += F("<TR><TD>W2 Pin Number:<TD><input type='text' name='w2pin' value='");
    reply += Settings.w2PIN_stored;
    reply += F("'>");
    reply += F("<TR><TD>Indicator LED Pin:<TD><input type='text' name='ledpin' value='");
    reply += Settings.LEDPIN_stored;
    reply += F("'>");
    reply += F("<TR><TD>Indicator LED2 Pin:<TD><input type='text' name='led2pin' value='");
    reply += Settings.LED2PIN_stored;
    reply += F("'>");
    reply += F("<TR><TD>Power Enable Pin Number:<TD><input type='text' name='pwrenablepin' value='");
    reply += Settings.POWER_ENABLE_LED_stored;
    reply += F("'>");
    reply += F("<TR><TD>Key Pin Number:<TD><input type='text' name='keypin' value='");
    reply += Settings.KEY_PIN_stored;
    reply += F("'>");


    //reply += F("<TR><TD>Disable J3 Reset:<TD>");

    //reply += F("<input type='radio' name='disableJ3reset' value='true'");
    //if (Settings.disableJ3Reset)
    //  reply += F(" checked ");
    //reply += F(">Yes");
    //reply += F("</input>");

    //reply += F("<input type='radio' name='disableJ3reset' value='false'");
    //if (!Settings.disableJ3Reset)
    //  reply += F(" checked ");
    //reply += F(">No");
    //reply += F("</input>");

    reply += F("<TR><TD>Continue Boot On Wifi Fail:<TD>");

    reply += F("<input type='radio' name='continueboot' value='yes'");
    if (Settings.continueBoot)
    reply += F(" checked ");
    reply += F(">Yes");
    reply += F("</input>");

    reply += F("<input type='radio' name='continueboot' value='no'");
    if (!Settings.continueBoot)
    reply += F(" checked ");
    reply += F(">No");
    reply += F("</input>");

    reply += F("<TR><TD><TD><input class=\"button-link\" type='submit' value='Submit'>");
    reply += F("</table></form>");
    addFooter(reply);
    server->send(200, "text/html", reply);
  });

  server->on("/control", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }

    String hexR = server->arg("r");
    String hexG = server->arg("g");
    String hexB = server->arg("b");
    String hexW1 = server->arg("w1");
    String hexW2 = server->arg("w2");
    String color = server->arg("color");
    String power = server->arg("power");

    if (color.length() != 0) {
      changeColor(color.substring(1, 7), 6, false, true);
    }
    else if (power == "off") {
      changeColor("00", 1, false);
      changeColor("00", 2, false);
      changeColor("00", 3, false);
      changeColor("00", 4, false);
      changeColor("00", 5, false);
    } else {
      if (hexR.length() != 0) {
        changeColor(padHex(String(hexR.toInt(), HEX)), 1, false, true);
      }
      if (hexG.length() != 0) {
        changeColor(padHex(String(hexG.toInt(), HEX)), 2, false, true);
      }
      if (hexB.length() != 0) {
        changeColor(padHex(String(hexB.toInt(), HEX)), 3, false, true);
      }
      if (hexW1.length() != 0) {
        changeColor(padHex(String(hexW1.toInt(), HEX)), 4, false, true);
      }
      if (hexW2.length() != 0) {
        changeColor(padHex(String(hexW2.toInt(), HEX)), 5, false, true);
      }
    }

    String reply = "";
    char str[20];
    addHeader(true, reply);

    reply += F("<table>");
    reply += F("<TH colspan='2'>");
    reply += projectName;
    reply += F(" Control");
    reply += F("<TR><TD><TD><TR><TD colspan='2' align='center'>");
    addMenu(reply);

    reply += F("</TR><form name='colorselect' class='form' method='post'>");
    reply += F("<TR><TD><TD>");
    reply += F("<TR><TD>");
    reply += F("<input type='color' name='color' value='#");
    reply += getHex(RED) + getHex(GREEN) + getHex(BLUE);
    reply += F("'><TD>");
    reply += F("<input class=\"button-link\" type='submit' value='Set Color'></TR>");
    reply += F("</form>");
    reply += F("<TR><TD><TD></TR>");
    reply += F("<form name='frmselect' class='form' method='post'>");

    reply += F("<TR><TD><font color='red'>R</font><TD><input type='range' name='r' min='0' max='255' value='");
    reply += getStandard(RED);
    reply += F("'>");
    reply += F("<TR><TD><font color='green'>G</font><TD><input type='range' name='g' min='0' max='255' value='");
    reply += getStandard(GREEN);
    reply += F("'>");
    reply += F("<TR><TD><font color='blue'>B</font><TD><input type='range' name='b' min='0' max='255' value='");
    reply += getStandard(BLUE);
    reply += F("'>");
    reply += F("<TR><TD>W1<TD><input type='range' name='w1' min='0' max='255' value='");
    reply += getStandard(W1);
    reply += F("'>");
    reply += F("<TR><TD>W2<TD><input type='range' name='w2' min='0' max='255' value='");
    reply += getStandard(W2);
    reply += F("'>");
    reply += F("<TR><TD><TD></TR>");
    reply += F("<TR><TD><input class=\"button-link\" type='submit' value='Set Color'></TD></form>");
    reply += F("<form name='powerOff' method='post'>");
    reply += F("<TD><input type='hidden' name='power' value='off'>");
    reply += F("<input class=\"button-link\" type='submit' value='Power Off'>");
    reply += F("</table></form>");
    addFooter(reply);

    needUpdate = true;

    server->send(200, "text/html", reply);
  });

  server->on("/rgb", []() {
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }
    run_program = false;
    String hexRGB = server->arg("value");
    String channels = server->arg("channels");
    String transition = server->arg("transition");

    String r, g, b;

    r = hexRGB.substring(0, 2);
    g = hexRGB.substring(2, 4);
    b = hexRGB.substring(4, 6);

    if (hexRGB.length() == 10) {
      changeColor(hexRGB, 99, (transition != "false" && transition != "2"), (channels != "true"));
    }
    else {
      changeColor(hexRGB, 6, (transition != "false" && transition != "2"), (channels != "true"));
    }

    //Check if we should forward the request to another device
    check_if_forward_request();

    //If we received this request from outside of ST, then lets update ST
    byte current_add[4] = {server->client().remoteIP()};
    if ((current_add[0] != Settings.haIP[0]) || (current_add[1] != Settings.haIP[1]) || (current_add[2] != Settings.haIP[2]) || (current_add[3] != Settings.haIP[3])) {
      needUpdate = true;
    }

    server->send(200, "application/json", getStatus());

  });


  server->on("/w1", []() {
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }
    run_program = false;
    String hexW1 = server->arg("value");
    String channels = server->arg("channels");
    String transition = server->arg("transition");

    changeColor(hexW1, 4, (transition != "false" && transition != "2"), (channels != "true"));

    //Check if we should forward the request to another device
    check_if_forward_request();

    //If we received this request from outside of ST, then lets update ST
    byte current_add[4] = {server->client().remoteIP()};
    if ((current_add[0] != Settings.haIP[0]) || (current_add[1] != Settings.haIP[1]) || (current_add[2] != Settings.haIP[2]) || (current_add[3] != Settings.haIP[3])) {
      needUpdate = true;
    }
    
    server->send(200, "application/json", getStatus());

  });

  server->on("/w2", []() {
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }
    run_program = false;
    String hexW2 = server->arg("value");
    String channels = server->arg("channels");
    String transition = server->arg("transition");

    changeColor(hexW2, 5, (transition != "false" && transition != "2"), (channels != "true"));

    //Check if we should forward the request to another device
    check_if_forward_request();

    //If we received this request from outside of ST, then lets update ST
    byte current_add[4] = {server->client().remoteIP()};
    if ((current_add[0] != Settings.haIP[0]) || (current_add[1] != Settings.haIP[1]) || (current_add[2] != Settings.haIP[2]) || (current_add[3] != Settings.haIP[3])) {
      needUpdate = true;
    }
    
    server->send(200, "application/json", getStatus());

  });

  server->on("/r", []() {
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }
    run_program = false;
    String r = server->arg("value");
    String channels = server->arg("channels");
    String transition = server->arg("transition");

    changeColor(r, 1, (transition != "false" && transition != "2"), (channels != "true"));

    //Check if we should forward the request to another device
    check_if_forward_request();

    //If we received this request from outside of ST, then lets update ST
    byte current_add[4] = {server->client().remoteIP()};
    if ((current_add[0] != Settings.haIP[0]) || (current_add[1] != Settings.haIP[1]) || (current_add[2] != Settings.haIP[2]) || (current_add[3] != Settings.haIP[3])) {
      needUpdate = true;
    }
    
    server->send(200, "application/json", getStatus());

  });

  server->on("/g", []() {
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }
    run_program = false;
    String g = server->arg("value");
    String channels = server->arg("channels");
    String transition = server->arg("transition");

    changeColor(g, 2, (transition != "false" && transition != "2"), (channels != "true"));

    //Check if we should forward the request to another device
    check_if_forward_request();

    //If we received this request from outside of ST, then lets update ST
    byte current_add[4] = {server->client().remoteIP()};
    if ((current_add[0] != Settings.haIP[0]) || (current_add[1] != Settings.haIP[1]) || (current_add[2] != Settings.haIP[2]) || (current_add[3] != Settings.haIP[3])) {
      needUpdate = true;
    }
    
    server->send(200, "application/json", getStatus());

  });

  server->on("/b", []() {
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
      return server->requestAuthentication();
    }
    run_program = false;
    String b = server->arg("value");
    String channels = server->arg("channels");
    String transition = server->arg("transition");

    changeColor(b, 3, (transition != "false" && transition != "2"), (channels != "true"));

    //Check if we should forward the request to another device
    check_if_forward_request();

    //If we received this request from outside of ST, then lets update ST
    byte current_add[4] = {server->client().remoteIP()};
    if ((current_add[0] != Settings.haIP[0]) || (current_add[1] != Settings.haIP[1]) || (current_add[2] != Settings.haIP[2]) || (current_add[3] != Settings.haIP[3])) {
      needUpdate = true;
    }
    
    server->send(200, "application/json", getStatus());

  });

  if (ESP.getFlashChipRealSize() > 524288) {
    if (Settings.usePassword == true && SecuritySettings.Password[0] != 0) {
      httpUpdater.setup(&*server, "/update", "admin", SecuritySettings.Password);
      //      httpUpdater.setProjectName(projectName);
    } else {
      httpUpdater.setup(&*server);
      //      httpUpdater.setProjectName(projectName);
    }
  }

  server->onNotFound(handleNotFound);

  server->begin();

  Serial.printf("Starting SSDP...\n");
  SSDP.setSchemaURL("description.xml");
  SSDP.setHTTPPort(80);
  SSDP.setName(projectName);
  SSDP.setSerialNumber(ESP.getChipId());
  SSDP.setURL("index.html");
  SSDP.setModelName(projectName);
  SSDP.setModelNumber(deblank(projectName) + "_SL");
  SSDP.setModelURL("http://smartlife.tech");
  SSDP.setManufacturer("Smart Life Automated");
  SSDP.setManufacturerURL("http://smartlife.tech");
  SSDP.begin();

  Serial.println("HTTP server started");
  Serial.println(WiFi.localIP());

  timerUptime = millis() + Settings.uReport * 1000;

  //Cjcharles added ArduinoOTA for ease of testing
  ArduinoOTA.begin();
}

void loop()
{
  //Cjcharles added ArduinoOTA for ease of testing
  ArduinoOTA.handle();

  server->handleClient();

  if (needFirmware == true) {
    sendStatus(98);
    needFirmware = false;
  }

  if (needUpdate == true && run_program == false) {
    sendStatus();
    needUpdate = false;
  }

  if (run_program) {
    if (millis() - previousMillis >= program_wait) {
      char *dup = strdup(program.c_str());
      const char *value = strtok( dup, "_" );
      const char *program_details = "";
      program_counter = 1;

      while (value != NULL)
      {
        if (program_counter == program_step) {
          program_details = value;
        }
        program_counter = program_counter + 1;
        value = strtok(NULL, "_");
      }
      String hexString(program_details);

      if (hexString.startsWith("w~")) {
        String hexProgram = hexString.substring(2, 4);
        if (hexString.indexOf("-", 5) >= 0) {
          program_wait = rand_interval(hexString.substring(5, hexString.indexOf("-") - 1).toInt(), hexString.substring(hexString.indexOf("-") + 1, hexString.length()).toInt());
        } else {
          program_wait = hexString.substring(5, hexString.length()).toInt();
        }
        changeColor(hexProgram, 4, true);
      } else if (hexString.startsWith("x~")) {
        String hexProgram = hexString.substring(2, 4);
        if (hexString.indexOf("-", 5) >= 0) {
          program_wait = rand_interval(hexString.substring(5, hexString.indexOf("-") - 1).toInt(), hexString.substring(hexString.indexOf("-") + 1, hexString.length()).toInt());
        } else {
          program_wait = hexString.substring(5, hexString.length()).toInt();
        }
        changeColor(hexProgram, 4, false);
      } else if (hexString.startsWith("y~")) {
        String hexProgram = hexString.substring(2, 4);
        if (hexString.indexOf("-", 5) >= 0) {
          program_wait = rand_interval(hexString.substring(5, hexString.indexOf("-") - 1).toInt(), hexString.substring(hexString.indexOf("-") + 1, hexString.length()).toInt());
        } else {
          program_wait = hexString.substring(5, hexString.length()).toInt();
        }
        changeColor(hexProgram, 5, true);
      } else if (hexString.startsWith("z~")) {
        String hexProgram = hexString.substring(2, 4);
        if (hexString.indexOf("-", 5) >= 0) {
          program_wait = rand_interval(hexString.substring(5, hexString.indexOf("-") - 1).toInt(), hexString.substring(hexString.indexOf("-") + 1, hexString.length()).toInt());
        } else {
          program_wait = hexString.substring(5, hexString.length()).toInt();
        }
        changeColor(hexProgram, 5, false);
      } else if (hexString.startsWith("f~")) {
        String hexProgram = hexString.substring(2, 8);
        if (hexString.indexOf("-", 9) >= 0) {
          program_wait = rand_interval(hexString.substring(9, hexString.indexOf("-") - 1).toInt(), hexString.substring(hexString.indexOf("-") + 1, hexString.length()).toInt());
        } else {
          program_wait = hexString.substring(9, hexString.length()).toInt();
        }
        changeColor(hexProgram, 6, true);
        // || hexString.startsWith("g~") added for backwards compatibility with SmartApp bug
      } else if (hexString.startsWith("d~") || hexString.startsWith("g~")) {
        String hexProgram = hexString.substring(2, 8);
        if (hexString.indexOf("-", 9) >= 0) {
          program_wait = rand_interval(hexString.substring(9, hexString.indexOf("-") - 1).toInt(), hexString.substring(hexString.indexOf("-") + 1, hexString.length()).toInt());
        } else {
          program_wait = hexString.substring(9, hexString.length()).toInt();
        }
        changeColor(hexProgram, 6, false);
      } else if (hexString.startsWith("n~")) {
        String hexProgram = hexString.substring(2, 12);
        if (hexString.indexOf("-", 13) >= 0) {
          program_wait = rand_interval(hexString.substring(13, hexString.indexOf("-") - 1).toInt(), hexString.substring(hexString.indexOf("-") + 1, hexString.length()).toInt());
        } else {
          program_wait = hexString.substring(13, hexString.length()).toInt();
        }
        changeColor(hexProgram, 8, true);
      } else if (hexString.startsWith("o~")) {
        String hexProgram = hexString.substring(2, 12);
        if (hexString.indexOf("-", 13) >= 0) {
          program_wait = rand_interval(hexString.substring(13, hexString.indexOf("-") - 1).toInt(), hexString.substring(hexString.indexOf("-") + 1, hexString.length()).toInt());
        } else {
          program_wait = hexString.substring(13, hexString.length()).toInt();
        }
        changeColor(hexProgram, 8, false);
      } 

      previousMillis = millis();
      program_step = program_step + 1;

      if (program_step >= program_counter && repeat == -1) {
        program_step = 1;
      } else if (program_step >= program_counter && repeat_count < repeat) {
        program_step = 1;
        repeat_count = repeat_count + 1;
      } else if (program_step > program_counter) {
        program_step = 1;
        run_program = false;
        if (program_off == "true") {
          changeColor("0000000000", 0, false);
        } else if (program_off == "previous") {
          changeColor(pre_program != ""? pre_program : "xxxxxxxxxx", 8, false);
        } 
        sendStatus();
      }

      free(dup);
    }
  }

  if (millis() - TIME_LED_RED >= led_delay_red) {
    TIME_LED_RED = millis();
    LED_RED();
  }

  if (millis() - TIME_LED_GREEN >= led_delay_green) {
    TIME_LED_GREEN = millis();
    LED_GREEN();
  }

  if (millis() - TIME_LED_BLUE >= led_delay_blue) {
    TIME_LED_BLUE = millis();
    LED_BLUE();
  }

  if (millis() - TIME_LED_W1 >= led_delay_w1) {
    TIME_LED_W1 = millis();
    LED_W1();
  }

  if (millis() - TIME_LED_W2 >= led_delay_w2) {
    TIME_LED_W2 = millis();
    LED_W2();
  }

  if (millis() > timerwd)
  runEach5Minutes();

  if (Settings.uReport > 0 && millis() > timerUptime) {
    sendStatus(99);
    timerUptime = millis() + Settings.uReport * 1000;
  }

  if ((Settings.autoOff != 0) && inAutoOff && ((millis() - autoOffTimer) > (1000 * Settings.autoOff))) {
    changeColor("0", 0, true);
    inAutoOff = false;
    autoOffTimer = 0;
  }

}
