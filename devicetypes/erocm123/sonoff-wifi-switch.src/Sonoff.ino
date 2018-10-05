#include <ESP8266WiFi.h>
#include <DNSServer.h>            //Local DNS Server used for redirecting all requests to the configuration portal
#include <ESP8266WebServer.h>     //Local WebServer used to serve the configuration portal
#include <WiFiManager.h>
#include <ESP8266SSDP.h>
#include <ESP8266HTTPUpdateServer.h>
ESP8266HTTPUpdateServer httpUpdater;
#include <Arduino.h>

//#define SONOFF
//#define SONOFF_TH
//#define SONOFF_S20
//#define SONOFF_TOUCH //ESP8285 !!!!!!!!!!
//#define SONOFF_SV
//#define SONOFF_POW
//#define SONOFF_DUAL
//#define SONOFF_4CH //ESP8285 !!!!!!!!!!
//#define ECOPLUG
//#define SONOFF_IFAN02 //ESP8285 !!!!!!!!!!
#define SHELLY

#ifdef SONOFF_POW
#include "HLW8012.h"
#endif

#if defined SONOFF_TH || defined SONOFF
#include <DHT.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#endif

String message = "";

String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete
int currentStatus = LOW;

boolean needUpdate1 = true;
boolean needUpdate2 = true;
boolean needUpdate3 = true;
boolean needUpdate4 = true;
boolean inAutoOff1 = false;
boolean inAutoOff2 = false;
boolean inAutoOff3 = false;
boolean inAutoOff4 = false;
boolean needReboot = false;
boolean shortPress = false;
int previousFanSpeed = -1;

//stores if the switch was high before at all
int state1 = LOW;
int state2 = LOW;
int state3 = LOW;
int state4 = LOW;
int state_ext = LOW;
//stores the time each button went high or low
unsigned long current_high1;
unsigned long current_low1;
unsigned long current_high2;
unsigned long current_low2;
unsigned long current_high3;
unsigned long current_low3;
unsigned long current_high4;
unsigned long current_low4;
unsigned long current_high_ext;
unsigned long current_low_ext;

#if defined SONOFF || defined ECOPLUG
const char * projectName = "Sonoff";
String softwareVersion = "2.0.5";
#endif
#ifdef SONOFF_S20
const char * projectName = "Sonoff S20";
String softwareVersion = "2.0.5";
#endif
#ifdef SONOFF_TOUCH
const char * projectName = "Sonoff Touch";
String softwareVersion = "2.0.5";
#endif
#ifdef SONOFF_POW
const char * projectName = "Sonoff POW";
String softwareVersion = "2.0.5";
HLW8012 hlw8012;
#endif
#ifdef SONOFF_TH
const char * projectName = "Sonoff TH";
String softwareVersion = "2.0.5";
#endif
#ifdef SONOFF_DUAL
const char * projectName = "Sonoff Dual";
String softwareVersion = "2.0.5";
#endif
#ifdef SONOFF_4CH
const char * projectName = "Sonoff 4CH";
String softwareVersion = "2.0.5";
#endif
#ifdef SONOFF_IFAN02
const char * projectName = "Sonoff IFan02";
String softwareVersion = "2.0.5";
#endif
#if defined SHELLY
const char * projectName = "Shelly";
String softwareVersion = "2.0.5";
#endif

const char compile_date[] = __DATE__ " " __TIME__;

unsigned long connectionFailures;

#define FLASH_EEPROM_SIZE 4096
extern "C" {
#include "spi_flash.h"
}
extern "C" uint32_t _SPIFFS_start;
extern "C" uint32_t _SPIFFS_end;
extern "C" uint32_t _SPIFFS_page;
extern "C" uint32_t _SPIFFS_block;

unsigned long failureTimeout = millis();
long debounceDelay = 20;    // the debounce time (in ms); increase if false positives
unsigned long timer1s = 0;
unsigned long timer5s = 0;
unsigned long timer5m = 0;
unsigned long timer1m = 0;
unsigned long timerW = 0;
unsigned long timerUptime;
unsigned long autoOffTimer1 = 0;
unsigned long autoOffTimer2 = 0;
unsigned long autoOffTimer3 = 0;
unsigned long autoOffTimer4 = 0;
unsigned long currentmillis = 0;

#if defined SONOFF_TH || defined SONOFF  || defined SHELLY
unsigned long timerT = 0;
unsigned long timerH = 0;
#endif

#ifdef SONOFF_POW
unsigned long timerV = 0;
unsigned long timerA = 0;
unsigned long timerVA = 0;
unsigned long timerPF = 0;

#define REL_PIN1       12             // GPIO 12 = Red Led and Relay (0 = Off, 1 = On)
#define LED_PIN1       15             // GPIO 13 = Green Led (0 = On, 1 = Off)
#define KEY_PIN1       0              // GPIO 00 = Button
#define SEL_PIN       5
#define CF1_PIN       13
#define CF_PIN        14

#define CURRENT_MODE  HIGH
#define CURRENT_RESISTOR                0.001
#define VOLTAGE_RESISTOR_UPSTREAM       ( 5 * 470000 ) // Real: 2280k
#define VOLTAGE_RESISTOR_DOWNSTREAM     ( 1000 ) // Real 1.009k

float W;
float V;
float A;
float VA;
float PF;

// redirect interrupt to HLW library
void ICACHE_RAM_ATTR  hlw8012_cf1_interrupt() {
  hlw8012.cf1_interrupt();
}
void ICACHE_RAM_ATTR  hlw8012_cf_interrupt() {
  hlw8012.cf_interrupt();
}

// Library expects an interrupt on both edges
void setInterrupts() {
  attachInterrupt(CF1_PIN, hlw8012_cf1_interrupt, CHANGE);
  attachInterrupt(CF_PIN, hlw8012_cf_interrupt, CHANGE);
}

void calibrate(int voltage = 120) {

  // Let some time to register values
  unsigned long timeout = millis();
  while ((millis() - timeout) < 10000) {
    delay(1);
  }

  //hlw8012.resetMultipliers();

  // Calibrate using a 60W bulb (pure resistive) on a 230V line
  //hlw8012.expectedActivePower(60.0);
  //hlw8012.expectedVoltage(110.0);
  //hlw8012.expectedCurrent(60.0 / 110.0);

  //hlw8012.expectedActivePower(53.0);
  hlw8012.expectedVoltage(voltage);
  //hlw8012.expectedCurrent(53.0 / 120.0);

  // Show corrected factors
  //message += hlw8012.getCurrentMultiplier();
  //message += "   ";
  //message += hlw8012.getVoltageMultiplier();
  //message += "   ";
  //message += hlw8012.getPowerMultiplier();
  //Serial.println();
}
#endif

#if defined SONOFF || defined SONOFF_TOUCH || defined SONOFF_S20 || defined SONOFF_DUAL
#define REL_PIN1       12
#define LED_PIN1       13
#define KEY_PIN1       0
#define EXT_PIN        14
#endif

#if defined SHELLY
#define REL_PIN1       4
#define KEY_PIN1       0
#define EXT_PIN        5
#define LED_PIN1       14
#endif

#if defined SONOFF_4CH || defined SONOFF_IFAN02
#define REL_PIN1       12
#define KEY_PIN1       0
#define REL_PIN2       5
#define KEY_PIN2       9
#define REL_PIN3       4
#define KEY_PIN3       10
#define REL_PIN4       15
#define KEY_PIN4       14

#define LED_PIN1       13
#endif

#if defined ECOPLUG
#define REL_PIN1       2
#define LED_PIN1       13
#define KEY_PIN1       0
#define EXT_PIN        14
#endif

#ifdef SONOFF_TH
#define REL_PIN1       12
#define LED_PIN1       13
#define KEY_PIN1       0
#endif

#if defined SONOFF || defined SONOFF_TH
#define EXT_PIN        14
#define DS_PIN         14
#define DHTTYPE DHT22
DHT dht(EXT_PIN, DHTTYPE);
float temperature;
float humidity;

OneWire oneWire(DS_PIN);
DallasTemperature ds18b20(&oneWire);

double _dsTemperature = 0;

double getDSTemperature() {
  return _dsTemperature;
}

void dsSetup() {
  ds18b20.begin();
}
#endif

#if defined SONOFF || defined SONOFF_S20 || defined SONOFF_TOUCH || defined SONOFF_TH || defined ECOPLUG || defined SONOFF_DUAL || defined SONOFF_4CH || defined SONOFF_IFAN02 || defined SHELLY


#define LEDoff1 digitalWrite(LED_PIN1,HIGH)
#define LEDon1 digitalWrite(LED_PIN1,LOW)
#else
#define LEDoff1 digitalWrite(LED_PIN1,LOW)
#define LEDon1 digitalWrite(LED_PIN1,HIGH)
#endif

#if defined SONOFF
#define Relayoff1 {\
  if (Settings.currentState1) needUpdate1 = true; \
  digitalWrite(REL_PIN1,LOW); \
  Settings.currentState1 = false; \
  LEDoff1; \
}
#define Relayon1 {\
  if (!Settings.currentState1) needUpdate1 = true; \
  digitalWrite(REL_PIN1,HIGH); \
  Settings.currentState1 = true; \
  LEDon1; \
}
#elif defined SONOFF_DUAL
#define Relayoff1 {\
  byte byteValue; \
  if (Settings.currentState1) needUpdate1 = true; \
  if (Settings.currentState2) byteValue = 0x02; \
  else byteValue = 0x00; \
  Serial.flush(); \
  Serial.write(0xA0); \
  Serial.write(0x04); \
  Serial.write(byteValue); \
  Serial.write(0xA1); \
  Serial.flush(); \
  Settings.currentState1 = false; \
}
#define Relayon1 {\
  byte byteValue; \
  if (!Settings.currentState1) needUpdate1 = true; \
  if (Settings.currentState2) byteValue = 0x03; \
  else byteValue = 0x01; \
  Serial.flush(); \
  Serial.write(0xA0); \
  Serial.write(0x04); \
  Serial.write(byteValue); \
  Serial.write(0xA1); \
  Serial.flush(); \
  Settings.currentState1 = true; \
}
#define Relayoff2 {\
  byte byteValue; \
  if (Settings.currentState2) needUpdate2 = true; \
  if (Settings.currentState1) byteValue = 0x01; \
  else byteValue = 0x00; \
  Serial.flush(); \
  Serial.write(0xA0); \
  Serial.write(0x04); \
  Serial.write(byteValue); \
  Serial.write(0xA1); \
  Serial.flush(); \
  Settings.currentState2 = false; \
}
#define Relayon2 {\
  byte byteValue; \
  if (!Settings.currentState2) needUpdate2 = true; \
  if (Settings.currentState1) byteValue = 0x03; \
  else byteValue = 0x02; \
  Serial.flush(); \
  Serial.write(0xA0); \
  Serial.write(0x04); \
  Serial.write(byteValue); \
  Serial.write(0xA1); \
  Serial.flush(); \
  Settings.currentState2 = true; \
}
#elif defined SONOFF_IFAN02
#define Relayoff1 {\
  if (Settings.currentState1) needUpdate1 = true; \
  digitalWrite(REL_PIN1,LOW); \
  Settings.currentState1 = false; \
}
#define Relayon1 {\
  if (!Settings.currentState1) needUpdate1 = true; \
  digitalWrite(REL_PIN1,HIGH); \
  Settings.currentState1 = true; \
}
#define Relayoff2 {\
  digitalWrite(REL_PIN2,LOW); \
  Settings.currentState2 = false; \
}
#define Relayon2 {\
  digitalWrite(REL_PIN2,HIGH); \
  Settings.currentState2 = true; \
}
#define Relayoff3 {\
  digitalWrite(REL_PIN3,LOW); \
  Settings.currentState3 = false; \
}
#define Relayon3 {\
  digitalWrite(REL_PIN3,HIGH); \
  Settings.currentState3 = true; \
}
#define Relayoff4 {\
  digitalWrite(REL_PIN4,LOW); \
  Settings.currentState4 = false; \
}
#define Relayon4 {\
  digitalWrite(REL_PIN4,HIGH); \
  Settings.currentState4 = true; \
}
#elif defined SONOFF_4CH
#define Relayoff1 {\
  if (Settings.currentState1) needUpdate1 = true; \
  digitalWrite(REL_PIN1,LOW); \
  Settings.currentState1 = false; \
}
#define Relayon1 {\
  if (!Settings.currentState1) needUpdate1 = true; \
  digitalWrite(REL_PIN1,HIGH); \
  Settings.currentState1 = true; \
}
#define Relayoff2 {\
  if (Settings.currentState2) needUpdate2 = true; \
  digitalWrite(REL_PIN2,LOW); \
  Settings.currentState2 = false; \
}
#define Relayon2 {\
  if (!Settings.currentState2) needUpdate2 = true; \
  digitalWrite(REL_PIN2,HIGH); \
  Settings.currentState2 = true; \
}
#define Relayoff3 {\
  if (Settings.currentState3) needUpdate3 = true; \
  digitalWrite(REL_PIN3,LOW); \
  Settings.currentState3 = false; \
}
#define Relayon3 {\
  if (!Settings.currentState3) needUpdate3 = true; \
  digitalWrite(REL_PIN3,HIGH); \
  Settings.currentState3 = true; \
}
#define Relayoff4 {\
  if (Settings.currentState4) needUpdate4 = true; \
  digitalWrite(REL_PIN4,LOW); \
  Settings.currentState4 = false; \
}
#define Relayon4 {\
  if (!Settings.currentState4) needUpdate4 = true; \
  digitalWrite(REL_PIN4,HIGH); \
  Settings.currentState4 = true; \
}
#else
#define Relayoff1 {\
  if (Settings.currentState1) needUpdate1 = true; \
  digitalWrite(REL_PIN1,LOW); \
  Settings.currentState1 = false; \
}
#define Relayon1 {\
  if (!Settings.currentState1) needUpdate1 = true; \
  digitalWrite(REL_PIN1,HIGH); \
  Settings.currentState1 = true; \
}
#endif

byte mac[6];

boolean WebLoggedIn = false;
int WebLoggedInTimer = 2;
String printWebString = "";
boolean printToWeb = false;

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
#define DEFAULT_PASSWORD               ""
#define DEFAULT_PORT                   80
#define DEFAULT_SWITCH_TYPE            0
#define DEFAULT_AUTO_OFF1              0
#define DEFAULT_AUTO_OFF2              0
#define DEFAULT_AUTO_OFF3              0
#define DEFAULT_AUTO_OFF4              0
#define DEFAULT_UREPORT                60
#define DEFAULT_DEBOUNCE               20
#define DEFAULT_HOSTNAME               ""
#ifdef SONOFF_POW
#define DEFAULT_WREPORT                60
#define DEFAULT_VREPORT                60
#define DEFAULT_AREPORT                60
#define DEFAULT_VAREPORT               120
#define DEFAULT_PFREPORT               240
#define DEFAULT_VOLTAGE                120
#endif
#ifdef SONOFF_TH
#define DEFAULT_SENSOR_TYPE            0
#define DEFAULT_USE_FAHRENHEIT         true
#define DEFAULT_TREPORT                300
#define DEFAULT_HREPORT                300
#endif
#define DEFAULT_EXT_TYPE               0

struct SettingsStruct
{
  byte          haIP[4];
  unsigned int  haPort;
  boolean       resetWifi;
  int           powerOnState;
  boolean       currentState1;
  byte          IP[4];
  byte          Gateway[4];
  byte          Subnet[4];
  byte          DNS[4];
  boolean       useStatic;
  boolean       longPress;
  boolean       reallyLongPress;
  boolean       usePassword;
  boolean       usePasswordControl;
#if defined SONOFF || defined SONOFF_TOUCH || defined SONOFF_S20 || defined ECOPLUG || defined SHELLY
  int           usePort;
  int           switchType;
  int           autoOff1;
  int           uReport;
  int           debounce;
  int           externalType;
  char          hostName[26];
#endif
#ifdef SONOFF_POW
  int           wReport;
  int           vReport;
  int           aReport;
  int           vaReport;
  int           pfReport;
  int           usePort;
  int           switchType;
  int           autoOff1;
  int           uReport;
  int           debounce;
  int           externalType;
  char          hostName[26];
  int           voltage;
#endif
#ifdef SONOFF_TH
  boolean       useFahrenheit;
  int           usePort;
  boolean       settingsReboot;
  int           sensorType;
  int           switchType;
  int           autoOff1;
  int           uReport;
  int           tReport;
  int           hReport;
  int           debounce;
  int           externalType;
  char          hostName[26];
#endif
#if defined SONOFF_DUAL
  int           usePort;
  int           switchType;
  int           autoOff1;
  boolean       currentState2;
  int           autoOff2;
  int           uReport;
  int           debounce;
  int           externalType;
  char          hostName[26];
#endif
#if defined SONOFF_4CH || defined SONOFF_IFAN02
  int           usePort;
  int           switchType;
  int           autoOff1;
  boolean       currentState2;
  int           autoOff2;
  boolean       currentState3;
  int           autoOff3;
  boolean       currentState4;
  int           autoOff4;
  int           uReport;
  int           debounce;
  int           externalType;
  char          hostName[26];
#endif
} Settings;

struct SecurityStruct
{
  char          Password[26];
  int           settingsVersion;
} SecuritySettings;



// Start WiFi Server
std::unique_ptr<ESP8266WebServer> server;

String padHex(String hex) {
  if (hex.length() == 1) {
    hex = "0" + hex;
  }
  return hex;
}

void handleRoot() {
  server->send(200, "application/json", "{\"message\":\"Sonoff Wifi Switch\"}");
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

void addRebootBanner(String& str)
{
  if (needReboot == true) {
    str += F("<TR><TD bgcolor='#A9A9A9' colspan='2' align='center'><font color='white'>Reboot needed for changes to take effect. <a href='/r'>Reboot</a></font>");
  }
}

void relayControl(int relay, int value) {
  switch (relay)
  {
    case 0: //All Switches
      { 
        if (value == 0) {
          Relayoff1;
          #if defined SONOFF_DUAL
          Relayoff2;
          #endif
          #if defined SONOFF_4CH || defined SONOFF_IFAN02
          Relayoff2;
          Relayoff3;
          Relayoff4;
          #endif
        }
        if (value == 1) {
          if (!inAutoOff1) {
            autoOffTimer1 = millis();
            inAutoOff1 = true;
          }
          Relayon1;
          #if defined SONOFF_DUAL
          if (!inAutoOff2) {
            autoOffTimer2 = millis();
            inAutoOff2 = true;
          }
          Relayon2;
          #endif
          #if defined SONOFF_4CH || defined SONOFF_IFAN02
          if (!inAutoOff2) {
            autoOffTimer2 = millis();
            inAutoOff2 = true;
          }
          Relayon2;
          if (!inAutoOff3) {
            autoOffTimer3 = millis();
            inAutoOff3 = true;
          }
          Relayon3;
          if (!inAutoOff4) {
            autoOffTimer4 = millis();
            inAutoOff4 = true;
          }
          Relayon4;
          #endif
        }  
        if (value == 2) {
          if (Settings.currentState1) { 
            Relayoff1;
          } else {
            if (!inAutoOff1) {
              autoOffTimer1 = millis();
              inAutoOff1 = true;
            }
            Relayon1;
          }
          #if defined SONOFF_DUAL
          if (Settings.currentState2) { 
            Relayoff2;
          } else {
            if (!inAutoOff2) {
              autoOffTimer2 = millis();
              inAutoOff2 = true;
            }
            Relayon2;
          }
          #endif
          #if defined SONOFF_4CH || defined SONOFF_IFAN02
          if (Settings.currentState2) { 
            Relayoff2;
          } else {
            if (!inAutoOff2) {
              autoOffTimer2 = millis();
              inAutoOff2 = true;
            }
            Relayon2;
          }
          if (Settings.currentState3) { 
            Relayoff3;
          } else {
            if (!inAutoOff3) {
              autoOffTimer3 = millis();
              inAutoOff3 = true;
            }
            Relayon3;
          }
          if (Settings.currentState4) { 
            Relayoff4;
          } else {
            if (!inAutoOff4) {
              autoOffTimer4 = millis();
              inAutoOff4 = true;
            }
            Relayon4;
          }
          #endif
        }  
        break;
      }
    case 1: //Relay 1
      {
        if (value == 0) {
          Relayoff1;
          #ifdef SONOFF
          LEDoff1;
          #endif
        }
        if (value == 1) {
          if (!inAutoOff1) {
            autoOffTimer1 = millis();
            inAutoOff1 = true;
          }
          Relayon1;
          #ifdef SONOFF
          LEDon1;
          #endif
        }  
        if (value == 2) {
          if (Settings.currentState1) { 
            Relayoff1;
          } else {
            if (!inAutoOff1) {
              autoOffTimer1 = millis();
              inAutoOff1 = true;
            }
            Relayon1;
          }
        }
        break;
      }
    case 2: //Relay 2
      {
        #if defined SONOFF_DUAL || defined SONOFF_4CH || defined SONOFF_IFAN02
        if (value == 0) {
          Relayoff2;
        }
        if (value == 1) {
          if (!inAutoOff2) {
              autoOffTimer2 = millis();
              inAutoOff2 = true;
            }
          Relayon2;
        }  
        if (value == 2) {
          if (Settings.currentState2) { 
            Relayoff2;
          } else {
            if (!inAutoOff2) {
              autoOffTimer2 = millis();
              inAutoOff2 = true;
            }
            Relayon2;
          }
        }
        #endif
        break;
      }
      case 3: //Relay 3
      {
        #if defined SONOFF_4CH || defined SONOFF_IFAN02
        if (value == 0) {
          Relayoff3;
        }
        if (value == 1) {
          if (!inAutoOff3) {
              autoOffTimer3 = millis();
              inAutoOff3 = true;
            }
          Relayon3;
        }  
        if (value == 2) {
          if (Settings.currentState3) { 
            Relayoff3;
          } else {
            if (!inAutoOff3) {
              autoOffTimer3 = millis();
              inAutoOff3 = true;
            }
            Relayon3;
          }
        }
        #endif
        break;
      }
      case 4: //Relay 4
      {
        #if defined SONOFF_4CH || defined SONOFF_IFAN02
        if (value == 0) {
          Relayoff4;
        }
        if (value == 1) {
          if (!inAutoOff4) {
              autoOffTimer4 = millis();
              inAutoOff4 = true;
            }
          Relayon4;
        }  
        if (value == 2) {
          if (Settings.currentState4) { 
            Relayoff4;
          } else {
            if (!inAutoOff4) {
              autoOffTimer4 = millis();
              inAutoOff4 = true;
            }
            Relayon4;
          }
        }
        #endif
        break;
      }
  }
  if (Settings.powerOnState == 2 || Settings.powerOnState == 3)
  {
    SaveSettings();
  }

}

void relayToggle1() {
  int reading = digitalRead(KEY_PIN1);
  if (reading == LOW) {
    current_low1 = millis();
    state1 = LOW;
  }
  if (reading == HIGH && state1 == LOW)
  {
    current_high1 = millis();
    state1 = HIGH;
    if ((current_high1 - current_low1) > (Settings.debounce? Settings.debounce : debounceDelay) && (current_high1 - current_low1) < 10000)
    {
      relayControl(1, 2);
      shortPress = true;
    }
    else if ((current_high1 - current_low1) >= 10000 && (current_high1 - current_low1) < 20000)
    {
      Settings.longPress = true;
      SaveSettings();
      ESP.restart();
    }
    else if ((current_high1 - current_low1) >= 20000 && (current_high1 - current_low1) < 60000)
    {
      Settings.reallyLongPress = true;
      SaveSettings();
      ESP.restart();
    }
  }
}

#if defined SONOFF_4CH || defined SONOFF_IFAN02
void relayToggle2() {
  int reading = digitalRead(KEY_PIN2);
  if (reading == LOW) {
    current_low2 = millis();
    state2 = LOW;
  }
  if (reading == HIGH && state2 == LOW)
  {
    current_high2 = millis();
    state2 = HIGH;
    if ((current_high2 - current_low2) > (Settings.debounce? Settings.debounce : debounceDelay) && (current_high2 - current_low2) < 10000)
    {
      relayControl(2, 2);
    }
    else if ((current_high2 - current_low2) >= 10000 && (current_high2 - current_low2) < 20000)
    {

    }
    else if ((current_high2 - current_low2) >= 20000 && (current_high2 - current_low2) < 60000)
    {

    }
  }
}
void relayToggle3() {
  int reading = digitalRead(KEY_PIN3);
  if (reading == LOW) {
    current_low3 = millis();
    state3 = LOW;
  }
  if (reading == HIGH && state3 == LOW)
  {
    current_high3 = millis();
    state3 = HIGH;
    if ((current_high3 - current_low3) > (Settings.debounce? Settings.debounce : debounceDelay) && (current_high3 - current_low3) < 10000)
    {
      relayControl(3, 2);
    }
    else if ((current_high3 - current_low3) >= 10000 && (current_high3 - current_low3) < 20000)
    {

    }
    else if ((current_high3 - current_low3) >= 20000 && (current_high3 - current_low3) < 60000)
    {

    }
  }
}
void relayToggle4() {
  int reading = digitalRead(KEY_PIN4);
  if (reading == LOW) {
    current_low4 = millis();
    state4 = LOW;
  }
  if (reading == HIGH && state4 == LOW)
  {
    current_high4 = millis();
    state4 = HIGH;
    if ((current_high4 - current_low4) > (Settings.debounce? Settings.debounce : debounceDelay) && (current_high4 - current_low4) < 10000)
    {
      relayControl(4, 2);
    }
    else if ((current_high4 - current_low4) >= 10000 && (current_high4 - current_low4) < 20000)
    {

    }
    else if ((current_high4 - current_low4) >= 20000 && (current_high4 - current_low4) < 60000)
    {

    }
  }
}
#endif

#if defined SONOFF || defined SONOFF_TH || defined SHELLY
void extRelayToggle() {
  if (Settings.externalType > 0) {
    int reading = digitalRead(EXT_PIN);
    if (reading == LOW && state_ext == HIGH) {
      current_low_ext = millis();
      state_ext = LOW;
      if ((current_low_ext - current_high_ext) > (Settings.debounce? Settings.debounce : debounceDelay)) {
        relayControl(1, 2);
      }
    }
    if (reading == HIGH && state_ext == LOW)
    {
      current_high_ext = millis();
      state_ext = HIGH;
      if ((current_high_ext - current_low_ext) > (Settings.debounce? Settings.debounce : debounceDelay))
      {
        if (Settings.externalType == 4) {
          relayControl(1, 2);
        }
      }
    }
  } else {
    //External switch has been disabled
  }
}
#endif

const char * endString(int s, const char *input) {
  int length = strlen(input);
  if ( s > length ) s = length;
  return const_cast<const char *>(&input[length - s]);
}

#ifdef SONOFF_POW
String getStatus() {
  return "{\"power\":\"" + String(digitalRead(REL_PIN1) == 0? "off" : "on") + "\", \"uptime\":\"" + uptime() + "\", \"W\":\"" + W + "\", \"V\":\"" + V + "\", \"A\":\"" + A + "\"}";
}
#elif defined SONOFF_TH
String getStatus() {
  return "{\"power\":\"" + String(digitalRead(REL_PIN1) == 0? "off" : "on") + "\", \"uptime\":\"" + uptime() + "\", \"temperature\":\"" + temperature + "\", \"scale\":\"" + getTempScale() + "\", \"humidity\":\"" + humidity +  "\"}";
}
#elif defined SONOFF_IFAN02
String getStatus() {
  return "{\"power\":\"" + String(digitalRead(REL_PIN1) == 0? "off" : "on") + "\", \"uptime\":\"" + uptime() + "\", " + "\"fan\":\"" + getFanSpeed() + "\"}";
}
#else
String getStatus() {
  return "{\"power\":\"" + String(digitalRead(REL_PIN1) == 0? "off" : "on") + "\", \"uptime\":\"" + uptime() + "\"}";
}
#endif

#ifdef SONOFF_POW
void checkPower() {
  W =  hlw8012.getActivePower();
  W =  hlw8012.getActivePower();
  VA = hlw8012.getApparentPower();
}

void checkVoltage() {
  V =  hlw8012.getVoltage();
}

void checkCurrent() {
  A =  hlw8012.getCurrent();
}

void checkPowerFactor() {
  PF = (int) (100 * hlw8012.getPowerFactor());
}
#endif

#ifdef SONOFF_TH
const char* getTempScale() {
  if (Settings.useFahrenheit == true) {
    return "F";
  } else {
    return "C";
  }
}

void checkTempAndHumidity() {
  // Reading temperature or humidity takes about 250 milliseconds!
  // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)

  if (Settings.externalType == 2) {
    ds18b20.requestTemperatures();
    temperature = (!Settings.useFahrenheit) ? ds18b20.getTempCByIndex(0) : ds18b20.getTempFByIndex(0);
  } else {
    humidity = dht.readHumidity();
    temperature = (!Settings.useFahrenheit) ? dht.readTemperature() : dht.readTemperature(true);
  }

  // Check if any reads failed and exit early (to try again).
  if (isnan(humidity) || isnan(temperature)) {
    //Serial.println("Failed to read from DHT sensor!");
    return;
  }
}
#endif

/*********************************************************************************************\
   Tasks each 5 seconds
  \*********************************************************************************************/
void runEach1Seconds()
{
  timer1s = millis() + 1000;
}


/*********************************************************************************************\
   Tasks each 5 seconds
  \*********************************************************************************************/
void runEach5Seconds()
{
  timer5s = millis() + 5000;
#ifdef SONOFF_POW
  checkPower();
  checkVoltage();
  checkCurrent();
  //checkPowerFactor();
#elif defined SONOFF_TH
  checkTempAndHumidity();
#endif
}

/*********************************************************************************************\
   Tasks each 1 minutes
  \*********************************************************************************************/
void runEach1Minutes()
{
  timer1m = millis() + 60000;

  if (SecuritySettings.Password[0] != 0)
  {
    if (WebLoggedIn)
      WebLoggedInTimer++;
    if (WebLoggedInTimer > 2)
      WebLoggedIn = false;
  }
}

/*********************************************************************************************\
   Tasks each 5 minutes
  \*********************************************************************************************/
void runEach5Minutes()
{
  timer5m = millis() + 300000;

  //sendStatus(99);

}

#ifdef SONOFF_IFAN02
int getFanSpeed(){
  if (Settings.currentState4 == true) return 3;
  else if (Settings.currentState3 == true) return 2;
  else if (Settings.currentState2 == true) return 1;
  else  return 0;
}

String getFanSpeedString(){
  if (Settings.currentState4 == true) return "High";
  else if (Settings.currentState3 == true) return "Medium";
  else if (Settings.currentState2 == true) return "Low";
  else  return "Off";
}

void setFanSpeed(int speed){
  switch(speed){
    case 0:
      relayControl(2, 0);
      relayControl(3, 0);
      relayControl(4, 0);
    break;
    case 1:
      relayControl(2, 1);
      relayControl(3, 0);
      relayControl(4, 0);
    break;
    case 2:
      relayControl(2, 1);
      relayControl(3, 1);
      relayControl(4, 0);
    break;
    case 3:
      relayControl(2, 1);
      relayControl(3, 0);
      relayControl(4, 1);
    break;
  }
}
#endif

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

  switch(number){
    case 0: {
      #if defined SONOFF_DUAL
      message = "{\"type\":\"relay\", \"number\":\"0\", \"power\":\"" + String(Settings.currentState1 == true || Settings.currentState2 == true? "on" : "off") + "\"}";
      #elif defined SONOFF_4CH || defined SONOFF_IFAN02
      message = "{\"type\":\"relay\", \"number\":\"0\", \"power\":\"" + String(Settings.currentState1 == true || Settings.currentState2 == true || Settings.currentState3 == true || Settings.currentState4 == true? "on" : "off") + "\"}";
      #else
      message = "{\"type\":\"relay\", \"number\":\"0\", \"power\":\"" + String(Settings.currentState1 == true? "on" : "off") + "\"}";
      #endif
      break;
    }
    case 1: {
      message = "{\"type\":\"relay\", \"number\":\"1\", \"power\":\"" + String(Settings.currentState1 == true? "on" : "off") + "\"}";
      break;
    }
    #if defined SONOFF_DUAL || defined SONOFF_4CH || defined SONOFF_IFAN02
    case 2: {
      message = "{\"type\":\"relay\", \"number\":\"2\", \"power\":\"" + String(Settings.currentState2 == true? "on" : "off") + "\"}";
      break;
    }
    #endif
    #if defined SONOFF_4CH || defined SONOFF_IFAN02
    case 3: {
      message = "{\"type\":\"relay\", \"number\":\"3\", \"power\":\"" + String(Settings.currentState3 == true? "on" : "off") + "\"}";
      break;
    }
    case 4: {
      message = "{\"type\":\"relay\", \"number\":\"4\", \"power\":\"" + String(Settings.currentState4 == true? "on" : "off") + "\"}";
      break;
    }
    case 98: {
      message = "{\"type\":\"fan\", \"number\":\"1\", \"speed\":\"" + String(getFanSpeed()) + "\"}";
      break;
    }
    #endif
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

boolean sendReport(int number) {
  String authHeader = "";
  boolean success = false;
  char host[20];
  const char* report;
  float value;

#ifdef SONOFF_POW
  switch (number)
  {
    case 1: //W Report
      {
        report = "W";
        value = W;
        timerW = millis() + Settings.wReport * 1000;
        break;
      }
    case 2: //V Report
      {
        report = "V";
        value = V;
        timerV = millis() + Settings.vReport * 1000;
        break;
      }
    case 3: //A Report
      {
        report = "A";
        value = A;
        timerA = millis() + Settings.aReport * 1000;
        break;
    } case 4: //VA Report
      {
        report = "VA";
        value = VA;
        timerVA = millis() + Settings.vaReport * 1000;
        break;
    } case 5: //PF Report
      {
        report = "PF";
        value = PF;
        timerPF = millis() + Settings.pfReport * 1000;
        break;
      }
    default : //Optional
      {

      }
  }
#endif

#ifdef SONOFF_TH
  switch (number)
  {
    case 1: //T Report
      {
        report = "temperature";
        value = temperature;
        timerT = millis() + Settings.tReport * 1000;
        break;
      }
    case 2: //H Report
      {
        report = "humidity";
        value = humidity;
        timerH = millis() + Settings.hReport * 1000;
        break;
      }
    default : //Optional
      {

      }
  }
#endif

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

  // We now create a URI for the request
  String url = F("/");
  //url += event->idx;
#ifdef SONOFF_TH
  String PostData = "{\"" + String(report) + "\":\"" + value + (report == "temperature"? "\", \"scale\":\"" + String(getTempScale()) : "") + "\"}" + "\r\n";
  client.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + ":" + Settings.haPort + "\r\n" + authHeader +
               "Content-Type: application/json;charset=utf-8\r\n" +
               "Content-Length: " + PostData.length() + "\r\n" +
               "Server: " + projectName + "\r\n" +
               "Connection: close\r\n\r\n" +
               PostData);
#endif

#if not defined SONOFF_TH
  String PostData = "{\"" + String(report) + "\":\"" + String(value) + "\"}" + "\r\n";
  client.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + ":" + Settings.haPort + "\r\n" + authHeader +
               "Content-Type: application/json;charset=utf-8\r\n" +
               "Content-Length: " + PostData.length() + "\r\n" +
               "Server: " + projectName + "\r\n" +
               "Connection: close\r\n\r\n" +
               PostData);
#endif

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
  SaveToFlash(0, (byte*)&Settings, sizeof(struct SettingsStruct));
  SaveToFlash(32768, (byte*)&SecuritySettings, sizeof(struct SecurityStruct));
}

boolean LoadSettings()
{
  LoadFromFlash(0, (byte*)&Settings, sizeof(struct SettingsStruct));
  LoadFromFlash(32768, (byte*)&SecuritySettings, sizeof(struct SecurityStruct));
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
      //Serial.print(F("FLASH: Erase Sector: "));
      //Serial.println(_sector);
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
        //Serial.print(F("FLASH: Zero Fill Sector: "));
        //Serial.println(_sector);
        delay(10);
      }
  }
  interrupts();
  delete [] data;
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

void setup()
{
  pinMode(KEY_PIN1, INPUT_PULLUP);
  attachInterrupt(KEY_PIN1, relayToggle1, CHANGE);
  pinMode(REL_PIN1, OUTPUT);

  #ifdef SONOFF_POW
  //relayControl(1,1);
  #endif

  
  LoadSettings();

#ifdef SONOFF_POW
  hlw8012.begin(CF_PIN, CF1_PIN, SEL_PIN, CURRENT_MODE, true);
  hlw8012.setResistors(CURRENT_RESISTOR, VOLTAGE_RESISTOR_UPSTREAM, VOLTAGE_RESISTOR_DOWNSTREAM);
  setInterrupts();
#endif
#if defined SONOFF_TH || defined SONOFF
  
  if (Settings.externalType == 3 || Settings.externalType == 4) {
    pinMode(EXT_PIN, INPUT_PULLUP);
    attachInterrupt(EXT_PIN, extRelayToggle, CHANGE);
  } else if (Settings.externalType == 2) {
    dsSetup();
  } else if (Settings.externalType == 1){
    dht.begin();
  }
#endif
#if defined SHELLY
    pinMode(EXT_PIN, INPUT);
    attachInterrupt(EXT_PIN, extRelayToggle, CHANGE);
#endif
#if defined SONOFF_4CH || defined SONOFF_IFAN02
  pinMode(KEY_PIN2, INPUT_PULLUP);
  attachInterrupt(KEY_PIN2, relayToggle2, CHANGE);
  pinMode(REL_PIN2, OUTPUT);
  pinMode(KEY_PIN3, INPUT_PULLUP);
  attachInterrupt(KEY_PIN3, relayToggle3, CHANGE);
  pinMode(REL_PIN3, OUTPUT);
  pinMode(KEY_PIN4, INPUT_PULLUP);
  attachInterrupt(KEY_PIN4, relayToggle4, CHANGE);
  pinMode(REL_PIN4, OUTPUT);
#endif


  pinMode(LED_PIN1, OUTPUT);


  // Setup console
  Serial.begin(19200);
  delay(10);
  //Serial1.println();
  //Serial1.println();


  #ifdef SONOFF_POW
  calibrate(Settings.voltage);
  #endif

  if (Settings.longPress == true) {
    for (uint8_t i = 0; i < 3; i++) {
      LEDoff1;
      delay(250);
      LEDon1;
      delay(250);
    }
    Settings.longPress = false;
    Settings.useStatic = false;
    Settings.resetWifi = true;
    SaveSettings();
    LEDoff1;
  }
  //Settings.reallyLongPress = true;
  if (Settings.reallyLongPress == true) {
    for (uint8_t i = 0; i < 5; i++) {
      LEDoff1;
      delay(1000);
      LEDon1;
      delay(1000);
    }
    EraseFlash();
    ZeroFillFlash();
    ESP.restart();
  }

  switch (Settings.powerOnState)
  {
    case 0: //Switch Off on Boot
      {
        relayControl(0, 0);
        break;
      }
    case 1: //Switch On on Boot
      {
        relayControl(0, 1);
        break;
      }
    case 2: //Saved State on Boot
      {
        if (Settings.currentState1) relayControl(1, 1);
        else relayControl(1, 0);
        #if defined SONOFF_DUAL || defined SONOFF_4CH || defined SONOFF_IFAN02
        if (Settings.currentState2) relayControl(2, 1);
        else relayControl(2, 0);
        #endif
        #if defined SONOFF_4CH || defined SONOFF_IFAN02
        if (Settings.currentState3) relayControl(3, 1);
        else relayControl(3, 0);
        if (Settings.currentState4) relayControl(4, 1);
        else relayControl(4, 0);
        #endif
        break;
      }
    case 3: //Opposite Saved State on Boot
      {
        if (!Settings.currentState1) relayControl(1, 1);
        else relayControl(1, 0);
        #if defined SONOFF_DUAL || defined SONOFF_4CH || defined SONOFF_IFAN02
        if (!Settings.currentState2) relayControl(2, 1);
        else relayControl(2, 0);
        #endif
        #if defined SONOFF_4CH || defined SONOFF_IFAN02
        if (!Settings.currentState3) relayControl(3, 1);
        else relayControl(3, 0);
        if (!Settings.currentState4) relayControl(4, 1);
        else relayControl(4, 0);
        #endif
        break;
      }
    default : //Optional
      {
        relayControl(0, 0);
      }
  }

  boolean saveSettings = false;

  if (SecuritySettings.settingsVersion < 200) {
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

    SecuritySettings.settingsVersion = 200;

    saveSettings = true;
  }

  if (SecuritySettings.settingsVersion < 201) {
#ifdef SONOFF_POW
    Settings.wReport =  DEFAULT_WREPORT;
    Settings.vReport =  DEFAULT_VREPORT;
    Settings.aReport =  DEFAULT_AREPORT;
    Settings.vaReport = DEFAULT_VAREPORT;
    Settings.pfReport = DEFAULT_PFREPORT;
#endif
#ifdef SONOFF_TH
    Settings.sensorType = DEFAULT_SENSOR_TYPE;
    Settings.useFahrenheit = DEFAULT_USE_FAHRENHEIT;
#endif
    SecuritySettings.settingsVersion = 201;

    saveSettings = true;
  }

  if (SecuritySettings.settingsVersion < 202) {
    Settings.usePort = DEFAULT_PORT;
    SecuritySettings.settingsVersion = 202;

    saveSettings = true;
  }

  if (SecuritySettings.settingsVersion < 203) {
    Settings.switchType = DEFAULT_SWITCH_TYPE;
    Settings.autoOff1 = DEFAULT_AUTO_OFF1;
    #if defined SONOFF_DUAL || defined SONOFF_4CH || defined SONOFF_IFAN02
    Settings.autoOff2 = DEFAULT_AUTO_OFF2;
    #endif
    #if defined SONOFF_4CH || defined SONOFF_IFAN02
    Settings.autoOff3 = DEFAULT_AUTO_OFF3;
    Settings.autoOff4 = DEFAULT_AUTO_OFF4;
    #endif
    SecuritySettings.settingsVersion = 203;

    saveSettings = true;
  }

  if (SecuritySettings.settingsVersion < 204) {
    Settings.uReport =  DEFAULT_UREPORT;
    SecuritySettings.settingsVersion = 204;
    saveSettings = true;
  }

  if (SecuritySettings.settingsVersion < 205) {
#ifdef SONOFF_TH
    Settings.tReport =  DEFAULT_TREPORT;
    Settings.hReport =  DEFAULT_HREPORT;
    if (Settings.sensorType == 0) Settings.externalType = 1;
    if (Settings.sensorType == 1) Settings.externalType = 2;
#endif
#if defined SONOFF
    if (Settings.switchType == 0) Settings.externalType = 3;
    if (Settings.switchType == 1) Settings.externalType = 4;
#endif
    Settings.debounce = DEFAULT_DEBOUNCE;
    SecuritySettings.settingsVersion = 205;
    saveSettings = true;
  }

  if (SecuritySettings.settingsVersion < 206) {
    strncpy(Settings.hostName, DEFAULT_HOSTNAME, sizeof(Settings.hostName));
    SecuritySettings.settingsVersion = 206;
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

  if (Settings.hostName[0] != 0) {
    wifiManager.setHostName(Settings.hostName);

  }

  if (Settings.resetWifi == true) {
    wifiManager.resetSettings();
    Settings.resetWifi = false;
    SaveSettings();
  }

  WiFi.macAddress(mac);

  String apSSID = deblank(projectName) + "." + String(mac[0], HEX) + String(mac[1], HEX) + String(mac[2], HEX) + String(mac[3], HEX) + String(mac[4], HEX) + String(mac[5], HEX);

  if (!wifiManager.autoConnect(apSSID.c_str(), "configme")) {
    //Serial.println("failed to connect, we should reset as see if it connects");
    delay(3000);
    ESP.reset();
    delay(5000);
  }

#if not defined SONOFF
  LEDon1;
#endif

  //Serial1.println("");

  if (Settings.usePort > 0 && Settings.usePort < 65535) {
    server.reset(new ESP8266WebServer(WiFi.localIP(), Settings.usePort));
  } else {
    server.reset(new ESP8266WebServer(WiFi.localIP(), 80));
  }

  //server->on("/", handleRoot);

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
    Relayoff1;
    LEDoff1;
    delay(2000);
    ESP.restart();
  });

  server->on("/r", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }
    server->send(200, "text/html", "<META http-equiv=\"refresh\" content=\"15;URL=/\">Device is rebooting...");
    Relayoff1;
    LEDoff1;
    delay(2000);
    ESP.restart();
  });

  server->on("/reset", []() {
    server->send(200, "application/json", "{\"message\":\"wifi settings are being removed\"}");
    Settings.reallyLongPress = true;
    SaveSettings();
    ESP.restart();
  });

  server->on("/status", []() {
    server->send(200, "application/json", getStatus());
  });

  server->on("/info", []() {
    server->send(200, "application/json", "{\"deviceType\":\"" + String(projectName) +"\", \"version\":\"" + softwareVersion + "\", \"date\":\"" + compile_date + "\", \"mac\":\"" + padHex(String(mac[0], HEX)) + padHex(String(mac[1], HEX)) + padHex(String(mac[2], HEX)) + padHex(String(mac[3], HEX)) + padHex(String(mac[4], HEX)) + padHex(String(mac[5], HEX)) + "\"}");
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
    String powerOnState = server->arg("pos");
    String ip = server->arg("ip");
    String gateway = server->arg("gateway");
    String subnet = server->arg("subnet");
    String dns = server->arg("dns");
    String usestatic = server->arg("usestatic");
    String usepassword = server->arg("usepassword");
    String usepasswordcontrol = server->arg("usepasswordcontrol");
    String username = server->arg("username");
    String password = server->arg("password");
    String port = server->arg("port");
    String autoOff1 = server->arg("autooff1");
    String uReport = server->arg("ureport");
    String debounce = server->arg("debounce");
    String hostname = server->arg("hostname");
    #if defined SONOFF || defined SONOFF_TH || defined SHELLY
    String switchType = server->arg("switchtype");
    String externalType = server->arg("externaltype");
    #endif
    #ifdef SONOFF_POW
    String wReport = server->arg("wreport");
    String vReport = server->arg("vreport");
    String aReport = server->arg("areport");
    String voltage = server->arg("voltage");
    #endif
    #ifdef SONOFF_TH
    String useFahrenheit = server->arg("fahrenheit");
    String sensorType = server->arg("sensortype");
    String tReport = server->arg("treport");
    String hReport = server->arg("hreport");
    #endif
    #ifdef SONOFF_DUAL
    String autoOff2 = server->arg("autooff2");
    #endif
    #if defined SONOFF_4CH || defined SONOFF_IFAN02
    String autoOff2 = server->arg("autooff2");
    String autoOff3 = server->arg("autooff3");
    String autoOff4 = server->arg("autooff4");
    #endif

    if(server->args() > 0){
      if (haPort.length() != 0)
      {
        Settings.haPort = haPort.toInt();
      }
      if (powerOnState.length() != 0)
      {
        Settings.powerOnState = powerOnState.toInt();
      }
      if (haIP.length() != 0)
      {
        haIP.toCharArray(tmpString, 26);
        str2ip(tmpString, Settings.haIP);
      }
      
      if (ip.length() != 0 && subnet.length() != 0) 
      {
        if (ip != String(Settings.IP[0]) + "." + String(Settings.IP[1]) + "." + String(Settings.IP[2]) + "." + String(Settings.IP[3]) && Settings.useStatic) needReboot = true;
        ip.toCharArray(tmpString, 26);
        str2ip(tmpString, Settings.IP);
        if (subnet != String(Settings.Subnet[0]) + "." + String(Settings.Subnet[1]) + "." + String(Settings.Subnet[2]) + "." + String(Settings.Subnet[3]) && Settings.useStatic) needReboot = true;
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
        if ((usestatic == "yes") != Settings.useStatic) needReboot = true;
        Settings.useStatic = (usestatic == "yes");
      }
      if (usepassword.length() != 0)
      {
        if ((usepassword == "yes") != Settings.usePassword) needReboot = true;
        Settings.usePassword = (usepassword == "yes");
      }
      if (usepasswordcontrol.length() != 0)
      {
        Settings.usePasswordControl = (usepasswordcontrol == "yes");
      }
      if(password != SecuritySettings.Password && Settings.usePassword) needReboot = true;
      strncpy(SecuritySettings.Password, password.c_str(), sizeof(SecuritySettings.Password));
      if (port.length() != 0)
      {
        //if(port.toInt() != Settings.usePort) needReboot = true;
        Settings.usePort = port.toInt();
      }
      if (autoOff1.length() != 0)
      {
        Settings.autoOff1 = autoOff1.toInt();
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
      if (hostname != Settings.hostName) needReboot = true;
      WiFi.hostname(hostname.c_str());
      strncpy(Settings.hostName, hostname.c_str(), sizeof(Settings.hostName));
  #if defined SONOFF || defined SONOFF_TH || defined SHELLY
      if (externalType.length() != 0)
      {
        if(externalType.toInt() != Settings.externalType) needReboot = true;
        Settings.externalType = externalType.toInt();
      }
  #endif
  #ifdef SONOFF_POW
      if (wReport.length() != 0)
      {
       if (wReport.toInt() != Settings.wReport) {
          Settings.wReport = wReport.toInt();
          timerW = millis() + Settings.wReport * 1000;
        }
      }
      if (vReport.length() != 0)
      {
        if (vReport.toInt() != Settings.vReport) {
          Settings.vReport = vReport.toInt();
          timerV = millis() + Settings.vReport * 1000;
        }
      }
      if (aReport.length() != 0)
      {
        if (aReport.toInt() != Settings.aReport) {
          Settings.aReport = aReport.toInt();
          timerA = millis() + Settings.aReport * 1000;
        }
      }
      if (voltage.length() != 0)
      {
        if(voltage.toInt() != Settings.voltage) needReboot = true;
        Settings.voltage = voltage.toInt();
      }
      #endif
      #ifdef SONOFF_TH
      if (useFahrenheit.length() != 0)
      {
        Settings.useFahrenheit = (useFahrenheit == "true");
        needUpdate1 = true;
      }
      if (sensorType.length() != 0)
      {
        if(sensorType.toInt() != Settings.sensorType) needReboot = true;
        Settings.sensorType = sensorType.toInt();
      }
      if (tReport.length() != 0)
      {
        if (tReport.toInt() != Settings.tReport) {
          Settings.tReport = tReport.toInt();
          timerT = millis() + Settings.tReport * 1000;
        }
      }
      if (hReport.length() != 0)
      {
        if (hReport.toInt() != Settings.hReport) {
          Settings.hReport = hReport.toInt();
          timerH = millis() + Settings.hReport * 1000;
        }
      }
      #endif
      #ifdef SONOFF_DUAL
      if (autoOff2.length() != 0)
      {
        Settings.autoOff2 = autoOff2.toInt();
      }
      #endif
      #if defined SONOFF_4CH || defined SONOFF_IFAN02
      if (autoOff2.length() != 0)
      {
        Settings.autoOff2 = autoOff2.toInt();
      }
      if (autoOff3.length() != 0)
      {
        Settings.autoOff3 = autoOff3.toInt();
      }
      if (autoOff4.length() != 0)
      {
        Settings.autoOff4 = autoOff4.toInt();
      }
      #endif
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
    addRebootBanner(reply);

    reply += F("<TR><TD>Host Name:<TD><input type='text' id='hostname' name='hostname' value='");
    reply += WiFi.hostname();
    reply += F("'>");

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


    byte choice = Settings.powerOnState;
    reply += F("'><TR><TD>Boot Up State:<TD><select name='");
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
    if (choice == 2) {
      reply += F("<option value='2' selected>Previous State</option>");
    } else {
      reply += F("<option value='2'>Previous State</option>");
    }
    if (choice == 3) {
      reply += F("<option value='3' selected>Inverse Previous State</option>");
    } else {
      reply += F("<option value='3'>Inverse Previous State</option>");
    }
    reply += F("</select>");
    #if not defined SONOFF_DUAL || not defined SONOFF_4CH
    reply += F("<TR><TD>Auto Off:<TD><input type='text' name='autooff1' value='");
    reply += Settings.autoOff1;
    reply += F("'>");
    #else
    reply += F("<TR><TD>Auto Off Relay 1:<TD><input type='text' name='autooff1' value='");
    reply += Settings.autoOff1;
    reply += F("'>");
    #endif 
    #ifdef SONOFF_DUAL
    reply += F("<TR><TD>Auto Off Relay 2:<TD><input type='text' name='autooff2' value='");
    reply += Settings.autoOff2;
    reply += F("'>");
    #endif
    #if defined SONOFF_IFAN02
    
    #endif
    #if defined SONOFF_4CH
    reply += F("<TR><TD>Auto Off Relay 2:<TD><input type='text' name='autooff2' value='");
    reply += Settings.autoOff2;
    reply += F("'>");
    reply += F("<TR><TD>Auto Off Relay 3:<TD><input type='text' name='autooff3' value='");
    reply += Settings.autoOff3;
    reply += F("'>");
    reply += F("<TR><TD>Auto Off Relay 4:<TD><input type='text' name='autooff4' value='");
    reply += Settings.autoOff4;
    reply += F("'>");
    #endif
    reply += F("<TR><TD>Switch Debounce:<TD><input type='text' name='debounce' value='");
    reply += Settings.debounce;
    reply += F("'>");
    #if defined SONOFF || defined SONOFF_TH || defined SHELLY
    choice = Settings.externalType;
    reply += F("<TR><TD>External Device Type:<TD><select name='");
    reply += "externaltype";
    reply += "'>";
    if (choice == 1) {
      reply += F("<option value='1' selected>Temperature - AM2301</option>");
    } else {
      reply += F("<option value='1'>Temperature - AM2301</option>");
    }
    if (choice == 2) {
      reply += F("<option value='2' selected>Temperature - DS18B20</option>");
    } else {
      reply += F("<option value='2'>Temperature - DS18B20</option>");
    }
    if (choice == 3) {
      reply += F("<option value='3' selected>Switch - Momentary</option>");
    } else {
      reply += F("<option value='3'>Switch - Momentary</option>");
    }
    if (choice == 4) {
      reply += F("<option value='4' selected>Switch - Toggle</option>");
    } else {
      reply += F("<option value='4'>Switch - Toggle</option>");
    }
    if (choice == 0) {
      reply += F("<option value='0' selected>Disabled</option>");
    } else {
      reply += F("<option value='0'>Disabled</option>");
    }
    reply += F("</select>");
    #endif
    #ifdef SONOFF_TH
    reply += F("<TR><TD>Temperature:<TD>");

    reply += F("<input type='radio' name='fahrenheit' value='true'");
    if (Settings.useFahrenheit)
      reply += F(" checked ");
    reply += F(">Fahrenheit");
    reply += F("</input>");

    reply += F("<input type='radio' name='fahrenheit' value='false'");
    if (!Settings.useFahrenheit)
      reply += F(" checked ");
    reply += F(">Celsius");
    reply += F("</input>");
    reply += F("<TR><TD>Temperature Report Interval:<TD><input type='text' name='treport' value='");
    reply += Settings.tReport;

    reply += F("'><TR><TD>Humidity Report Interval:<TD><input type='text' name='hreport' value='");
    reply += Settings.hReport;

    reply += F("'>");

    #endif
    reply += F("<TR><TD>Uptime Report Interval:<TD><input type='text' name='ureport' value='");
    reply += Settings.uReport;
    reply += F("'>");
    #ifdef SONOFF_POW
    reply += F("<TR><TD>W Report Interval:<TD><input type='text' name='wreport' value='");
    reply += Settings.wReport;

    reply += F("'><TR><TD>V Report Interval:<TD><input type='text' name='vreport' value='");
    reply += Settings.vReport;

    reply += F("'><TR><TD>A Report Interval:<TD><input type='text' name='areport' value='");
    reply += Settings.aReport;
    reply += F("'>");
    choice = Settings.voltage;
    reply += F("<TR><TD>Device Voltage:<TD><select name='");
    reply += "voltage";
    reply += "'>";
    if (choice == 120) {
      reply += F("<option value='120' selected>120V</option>");
    } else {
      reply += F("<option value='120'>120V</option>");
    }
    if (choice == 220) {
      reply += F("<option value='220' selected>220V</option>");
    } else {
      reply += F("<option value='220'>220V</option>");
    }
    if (choice == 230) {
      reply += F("<option value='230' selected>230V</option>");
    } else {
      reply += F("<option value='230'>230V</option>");
    }
    if (choice == 240) {
      reply += F("<option value='240' selected>240V</option>");
    } else {
      reply += F("<option value='240'>240V</option>");
    }
    reply += F("</select>");
    #endif
    
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

    char tmpString[64];
    #if defined SONOFF_DUAL
    String value1 = server->arg("relayValue1");
    String value2 = server->arg("relayValue2");
    #elif defined SONOFF_4CH
    String value1 = server->arg("relayValue1");
    String value2 = server->arg("relayValue2");
    String value3 = server->arg("relayValue3");
    String value4 = server->arg("relayValue4");
    #elif  defined SONOFF_IFAN02
    String value1 = server->arg("relayValue1");
    String fanValue1 = server->arg("fanValue0");
    String fanValue2 = server->arg("fanValue1");
    String fanValue3 = server->arg("fanValue2");
    String fanValue4 = server->arg("fanValue3");
    #else
    String value = server->arg("relayValue");
    #endif
    
    
    #if defined SONOFF_DUAL
    if (value1 == "on")
    {
      relayControl(1, 1);
    }
    if (value1 == "off")
    {
      relayControl(1, 0);
    }
    if (value2 == "on")
    {
      relayControl(2, 1);
    }
    if (value2 == "off")
    {
      relayControl(2, 0);
    }
    #elif defined SONOFF_4CH
    if (value1 == "on")
    {
      relayControl(1, 1);
    }
    if (value1 == "off")
    {
      relayControl(1, 0);
    }
    if (value2 == "on")
    {
      relayControl(2, 1);
    }
    if (value2 == "off")
    {
      relayControl(2, 0);
    }
    if (value3 == "on")
    {
      relayControl(3, 1);
    }
    if (value3 == "off")
    {
      relayControl(3, 0);
    }
    if (value4 == "on")
    {
      relayControl(4, 1);
    }
    if (value4 == "off")
    {
      relayControl(4, 0);
    }
    #elif defined SONOFF_IFAN02
    if (value1 == "on")
    {
      relayControl(1, 1);
    }
    if (value1 == "off")
    {
      relayControl(1, 0);
    }
    if (fanValue1 == "on")
    {
      setFanSpeed(0);
    }
    if (fanValue2 == "on")
    {
      setFanSpeed(1);
    }
    if (fanValue3 == "on")
    {
      setFanSpeed(2);
    }
    if (fanValue4 == "on")
    {
      setFanSpeed(3);
    }
    #else
    if (value == "on")
    {
      relayControl(1, 1);
    }
    else if (value == "off")
    {
      relayControl(1, 0);
    }
    #endif

    String reply = "";
    char str[20];
    addHeader(true, reply);

    reply += F("<table>");
    reply += F("<TH colspan='2'>");
    reply += projectName;
    reply += F(" Control");
    reply += F("<TR><TD><TD><TR><TD colspan='2' align='center'>");
    addMenu(reply);
    addRebootBanner(reply);

    #if defined SONOFF_DUAL
    reply += F("<TD><TR><TD><B>Relay 1</B><TD><TR><TD>Current State:");

    if (Settings.currentState1) {
      reply += F("<TD>ON<TD>");
    } else {
      reply += F("<TD>OFF<TD>");
    }
    reply += F("<TR><TD><form name='powerOn1' method='post'><input type='hidden' name='relayValue1' value='on'><input class=\"button-link\" type='submit' value='On'></form>");
    reply += F("<TD><form name='powerOff1' method='post'><input type='hidden' name='relayValue1' value='off'><input class=\"button-link\" type='submit' value='Off'></form>");
    reply += F("<TD><TR><TD><B>Relay 2</B><TD><TR><TD>Current State:");

    if (Settings.currentState2) {
      reply += F("<TD>ON<TD>");
    } else {
      reply += F("<TD>OFF<TD>");
    }
    reply += F("<TR><TD><form name='powerOn2' method='post'><input type='hidden' name='relayValue2' value='on'><input class=\"button-link\" type='submit' value='On'></form>");
    reply += F("<TD><form name='powerOff2' method='post'><input type='hidden' name='relayValue2' value='off'><input class=\"button-link\" type='submit' value='Off'></form>");
    #elif defined SONOFF_4CH
    reply += F("<TD><TR><TD><B>Relay 1</B><TD><TR><TD>Current State:");

    if (Settings.currentState1) {
      reply += F("<TD>ON<TD>");
    } else {
      reply += F("<TD>OFF<TD>");
    }
    reply += F("<TR><TD><form name='powerOn1' method='post'><input type='hidden' name='relayValue1' value='on'><input class=\"button-link\" type='submit' value='On'></form>");
    reply += F("<TD><form name='powerOff1' method='post'><input type='hidden' name='relayValue1' value='off'><input class=\"button-link\" type='submit' value='Off'></form>");
    reply += F("<TD><TR><TD><B>Relay 2</B><TD><TR><TD>Current State:");

    if (Settings.currentState2) {
      reply += F("<TD>ON<TD>");
    } else {
      reply += F("<TD>OFF<TD>");
    }
    reply += F("<TR><TD><form name='powerOn2' method='post'><input type='hidden' name='relayValue2' value='on'><input class=\"button-link\" type='submit' value='On'></form>");
    reply += F("<TD><form name='powerOff2' method='post'><input type='hidden' name='relayValue2' value='off'><input class=\"button-link\" type='submit' value='Off'></form>");
    reply += F("<TD><TR><TD><B>Relay 3</B><TD><TR><TD>Current State:");

    if (Settings.currentState3) {
      reply += F("<TD>ON<TD>");
    } else {
      reply += F("<TD>OFF<TD>");
    }
    reply += F("<TR><TD><form name='powerOn3' method='post'><input type='hidden' name='relayValue3' value='on'><input class=\"button-link\" type='submit' value='On'></form>");
    reply += F("<TD><form name='powerOff3' method='post'><input type='hidden' name='relayValue3' value='off'><input class=\"button-link\" type='submit' value='Off'></form>");
    reply += F("<TD><TR><TD><B>Relay 4</B><TD><TR><TD>Current State:");

    if (Settings.currentState4) {
      reply += F("<TD>ON<TD>");
    } else {
      reply += F("<TD>OFF<TD>");
    }
    reply += F("<TR><TD><form name='powerOn4' method='post'><input type='hidden' name='relayValue4' value='on'><input class=\"button-link\" type='submit' value='On'></form>");
    reply += F("<TD><form name='powerOff24' method='post'><input type='hidden' name='relayValue4' value='off'><input class=\"button-link\" type='submit' value='Off'></form>");
    
    #elif defined SONOFF_IFAN02
    reply += F("<TD><TR><TD><B>Relay 1</B><TD><TR><TD>Current State:");

    if (Settings.currentState1) {
      reply += F("<TD>ON<TD>");
    } else {
      reply += F("<TD>OFF<TD>");
    }
    reply += F("<TR><TD><form name='powerOn1' method='post'><input type='hidden' name='relayValue1' value='on'><input class=\"button-link\" type='submit' value='On'></form>");
    reply += F("<TD><form name='powerOff1' method='post'><input type='hidden' name='relayValue1' value='off'><input class=\"button-link\" type='submit' value='Off'></form>");
    reply += F("<TD><TR><TD><B>Fan</B><TD><TR><TD>Current State:");

    switch (getFanSpeed())
    {
      case 0: 
      {
        reply += F("<TD>OFF<TD>");
        break;
      }
      case 1: 
      {
        reply += F("<TD>LOW<TD>");
        break;
      }
      case 2: 
      {
        reply += F("<TD>MEDIUM<TD>");
        break;
      }
      case 3: 
      {
        reply += F("<TD>HIGH<TD>");
        break;
      }
    }

    reply += F("<TR><TD><form name='setFan0' method='post'><input type='hidden' name='fanValue0' value='on'><input class=\"button-link\" type='submit' value='Off'></form>");
    reply += F("<TR><TD><form name='setFan1' method='post'><input type='hidden' name='fanValue1' value='on'><input class=\"button-link\" type='submit' value='LOW'></form>");
    reply += F("<TR><TD><form name='setFan2' method='post'><input type='hidden' name='fanValue2' value='on'><input class=\"button-link\" type='submit' value='MEDIUM'></form>");
    reply += F("<TR><TD><form name='setFan3' method='post'><input type='hidden' name='fanValue3' value='on'><input class=\"button-link\" type='submit' value='HIGH'></form>");
    
    #else
    reply += F("<TR><TD><TD><TR><TD>Current State:");

    if (Settings.currentState1) {
      reply += F("<TD>ON<TD>");
    } else {
      reply += F("<TD>OFF<TD>");
    }
    reply += F("<TR><TD><form name='powerOn' method='post'><input type='hidden' name='relayValue' value='on'><input class=\"button-link\" type='submit' value='On'></form>");
    reply += F("<TD><form name='powerOff' method='post'><input type='hidden' name='relayValue' value='off'><input class=\"button-link\" type='submit' value='Off'></form>");
    #endif

    reply += F("</table>");
    addFooter(reply);
    server->send(200, "text/html", reply);
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
    addRebootBanner(reply);

    reply += F("<TR><TD><TD><TR><TD>Main:");

    reply += F("<TD><a href='/advanced'>Advanced Config</a><BR>");
    reply += F("<a href='/control'>Relay Control</a><BR>");
    reply += F("<a href='/update'>Firmware Update</a><BR>");
    reply += F("<a href='http://tiny.cc/wnxady'>Documentation</a><BR>");
    reply += F("<a href='/r'>Reboot</a><BR>");

    reply += F("<TR><TD>JSON Endpoints:");

    reply += F("<TD><a href='/status'>status</a><BR>");
    //reply += F("<a href='/config'>config</a><BR>");
    reply += F("<a href='/configSet'>configSet</a><BR>");
    reply += F("<a href='/configGet'>configGet</a><BR>");
    reply += F("<a href='/on'>on</a><BR>");
    reply += F("<a href='/off'>off</a><BR>");
    #if defined SONOFF_DUAL
    reply += F("<a href='/on1'>on1</a><BR>");
    reply += F("<a href='/off1'>off1</a><BR>");
    reply += F("<a href='/on2'>on2</a><BR>");
    reply += F("<a href='/off2'>off2</a><BR>");
    #endif
    #if defined SONOFF_IFAN02
    reply += F("<a href='/fan0'>fan0</a><BR>");
    reply += F("<a href='/fan1'>fan1</a><BR>");
    reply += F("<a href='/fan2'>fan2</a><BR>");
    reply += F("<a href='/fan3'>fan3</a><BR>");
    #endif
    #if defined SONOFF_4CH
    reply += F("<a href='/on1'>on1</a><BR>");
    reply += F("<a href='/off1'>off1</a><BR>");
    reply += F("<a href='/on2'>on2</a><BR>");
    reply += F("<a href='/off2'>off2</a><BR>");
    reply += F("<a href='/on3'>on3</a><BR>");
    reply += F("<a href='/off3'>off3</a><BR>");
    reply += F("<a href='/on4'>on4</a><BR>");
    reply += F("<a href='/off4'>off4</a><BR>");
    #endif
    reply += F("<a href='/info'>info</a><BR>");
    reply += F("<a href='/reboot'>reboot</a><BR>");

    reply += F("</table>");
    addFooter(reply);
    server->send(200, "text/html", reply);
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
    if (configName == "autooff1") {
      reply += "{\"name\":\"autooff1\", \"value\":\"" + String(Settings.autoOff1) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "debounce") {
      reply += "{\"name\":\"debounce\", \"value\":\"" + String(Settings.debounce) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "ureport") {
      reply += "{\"name\":\"ureport\", \"value\":\"" + String(Settings.uReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #if defined SONOFF || defined SONOFF_TH || defined SHELLY
    if (configName == "externaltype") {
      reply += "{\"name\":\"externaltype\", \"value\":\"" + String(Settings.externalType) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #endif
    #ifdef SONOFF_POW
    if (configName == "wreport") {
      reply += "{\"name\":\"wreport\", \"value\":\"" + String(Settings.wReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "vreport") {
      reply += "{\"name\":\"vreport\", \"value\":\"" + String(Settings.vReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "areport") {
      reply += "{\"name\":\"areport\", \"value\":\"" + String(Settings.aReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "vareport") {
      reply += "{\"name\":\"vareport\", \"value\":\"" + String(Settings.vaReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "pfreport") {
      reply += "{\"name\":\"pfreport\", \"value\":\"" + String(Settings.pfReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #endif
    #ifdef SONOFF_TH
    if ( configName == "usefahrenheit") {
      reply += "{\"name\":\"usefahrenheit\", \"value\":\"" + String(Settings.useFahrenheit) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "sensortype") {
      reply += "{\"name\":\"sensortype\", \"value\":\"" + String(Settings.sensorType) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "treport") {
      reply += "{\"name\":\"treport\", \"value\":\"" + String(Settings.tReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "hreport") {
      reply += "{\"name\":\"hreport\", \"value\":\"" + String(Settings.hReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #endif
    #if defined SONOFF_DUAL || defined SONOFF_4CH || defined SONOFF_IFAN02
    if (configName == "autooff2") {
      reply += "{\"name\":\"autooff2\", \"value\":\"" + String(Settings.autoOff2) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #endif
    #if defined SONOFF_4CH || defined SONOFF_IFAN02
    if (configName == "autooff23") {
      reply += "{\"name\":\"autooff3\", \"value\":\"" + String(Settings.autoOff3) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "autooff4") {
      reply += "{\"name\":\"autooff4\", \"value\":\"" + String(Settings.autoOff4) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #endif

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
    if (configName == "autooff1") {
      if (configValue.length() != 0)
      {
        Settings.autoOff1 = configValue.toInt();
      }
      reply += "{\"name\":\"autooff1\", \"value\":\"" + String(Settings.autoOff1) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "ureport") {
      if (configValue.length() != 0)
      {
        if (configValue.toInt() != Settings.uReport) {
          Settings.uReport = configValue.toInt();
          timerUptime = millis() + Settings.uReport * 1000;
        }
      }
      reply += "{\"name\":\"ureport\", \"value\":\"" + String(Settings.uReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "debounce") {
      if (configValue.length() != 0)
      {
        Settings.debounce = configValue.toInt();
      }
      reply += "{\"name\":\"debounce\", \"value\":\"" + String(Settings.debounce) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #if defined SONOFF || defined SONOFF_TH || defined SHELLY
    if (configName == "externaltype") {
      if (configValue.length() != 0)
      {
        Settings.externalType = configValue.toInt();
      }
      reply += "{\"name\":\"externaltype\", \"value\":\"" + String(Settings.externalType) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #endif
    #ifdef SONOFF_POW
    if (configName == "wreport") {
      if (configValue.length() != 0)
      {
        if (configValue.toInt() != Settings.wReport) {
          Settings.wReport = configValue.toInt();
          timerW = millis() + Settings.wReport * 1000;
        }
      }
      reply += "{\"name\":\"wreport\", \"value\":\"" + String(Settings.wReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "vreport") {
      if (configValue.length() != 0)
      {
        if (configValue.toInt() != Settings.vReport) {
          Settings.vReport = configValue.toInt();
          timerV = millis() + Settings.vReport * 1000;
        }
      }
      reply += "{\"name\":\"vreport\", \"value\":\"" + String(Settings.vReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "areport") {
      if (configValue.length() != 0)
      {
        if (configValue.toInt() != Settings.aReport) {
          Settings.aReport = configValue.toInt();
          timerA = millis() + Settings.aReport * 1000;
        }
      }
      reply += "{\"name\":\"areport\", \"value\":\"" + String(Settings.aReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "vareport") {
      reply += "{\"name\":\"vareport\", \"value\":\"" + String(Settings.vaReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "pfreport") {
      reply += "{\"name\":\"pfreport\", \"value\":\"" + String(Settings.pfReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #endif
    #ifdef SONOFF_TH
    if (configName == "usefahrenheit") {
      if (configValue.length() != 0)
      {
        Settings.useFahrenheit = (configValue == "true");
      }
      reply += "{\"name\":\"usefahrenheit\", \"value\":\"" + String(Settings.useFahrenheit) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "sensortype") {
      if (configValue.length() != 0)
      {
        Settings.sensorType = configValue.toInt();
      }
      reply += "{\"name\":\"sensortype\", \"value\":\"" + String(Settings.sensorType) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "treport") {
      if (configValue.length() != 0)
      {
        if (configValue.toInt() != Settings.tReport) {
          Settings.tReport = configValue.toInt();
          timerT = millis() + Settings.tReport * 1000;
        }
      }
      reply += "{\"name\":\"treport\", \"value\":\"" + String(Settings.tReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "hreport") {
      if (configValue.length() != 0)
      {
        if (configValue.toInt() != Settings.hReport) {
          Settings.hReport = configValue.toInt();
          timerH = millis() + Settings.hReport * 1000;
        }
      }
      reply += "{\"name\":\"hreport\", \"value\":\"" + String(Settings.hReport) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #endif
    #if defined SONOFF_DUAL || defined SONOFF_4CH || defined SONOFF_IFAN02
    if (configName == "autooff2") {
      if (configValue.length() != 0)
      {
        Settings.autoOff2 = configValue.toInt();
      }
      reply += "{\"name\":\"autooff2\", \"value\":\"" + String(Settings.autoOff2) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #endif
    #if defined SONOFF_4CH || defined SONOFF_IFAN02
    if (configName == "autooff3") {
      if (configValue.length() != 0)
      {
        Settings.autoOff3 = configValue.toInt();
      }
      reply += "{\"name\":\"autooff3\", \"value\":\"" + String(Settings.autoOff3) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    if (configName == "autooff4") {
      if (configValue.length() != 0)
      {
        Settings.autoOff4 = configValue.toInt();
      }
      reply += "{\"name\":\"autooff4\", \"value\":\"" + String(Settings.autoOff4) + "\", \"success\":\"true\", \"type\":\"configuration\"}";
    }
    #endif

    if ( reply != "" ) {
      SaveSettings();
      server->send(200, "application/json", reply);
    } else {
      server->send(200, "application/json", "{\"success\":\"false\", \"type\":\"configuration\"}");
    }
  });

  #if defined SONOFF_DUAL
  server->on("/off", []() {
    boolean relayStatus = false;
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }
    relayControl(0, 0);
    if (Settings.currentState1 || Settings.currentState2) relayStatus = true;
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"1\", \"power\":\"" + String(relayStatus? "on" : "off") + "\"}");
  });
  server->on("/off1", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(1, 0);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"1\", \"power\":\"" + String(Settings.currentState1? "on" : "off") + "\"}");
  });
  server->on("/off2", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(2, 0);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"2\", \"power\":\"" + String(Settings.currentState2? "on" : "off") + "\"}");
  });
  #elif defined SONOFF_IFAN02
  server->on("/off", []() {
    boolean relayStatus = false;
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(1, 0);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"0\", \"power\":\"" + String(Settings.currentState1? "on" : "off") + "\"}");
  });
  server->on("/fan0", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    setFanSpeed(0);
    server->send(200, "application/json", "{\"type\":\"fan\", \"number\":\"1\", \"speed\":\"" + String(getFanSpeed()) + "\"}");
  });
  
  #elif defined SONOFF_4CH
  server->on("/off", []() {
    boolean relayStatus = false;
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(0, 0);
    if (Settings.currentState1 || Settings.currentState2 || Settings.currentState3 || Settings.currentState4) relayStatus = true;
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"0\", \"power\":\"" + String(relayStatus? "on" : "off") + "\"}");
  });
  server->on("/off1", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(1, 0);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"1\", \"power\":\"" + String(Settings.currentState1? "on" : "off") + "\"}");
  });
  server->on("/off2", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(2, 0);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"2\", \"power\":\"" + String(Settings.currentState2? "on" : "off") + "\"}");
  });
  server->on("/off3", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(3, 0);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"3\", \"power\":\"" + String(Settings.currentState3? "on" : "off") + "\"}");
  });
  server->on("/off4", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(4, 0);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"4\", \"power\":\"" + String(Settings.currentState4? "on" : "off") + "\"}");
  });
  #else
  server->on("/off", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(0, 0);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"1\", \"power\":\"" + String(Settings.currentState1? "on" : "off") + "\"}");
  });
  
  #endif

  #if defined SONOFF_DUAL
  server->on("/on", []() {
    boolean relayStatus = false;
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }
    relayControl(0, 1);
    if (Settings.currentState1 || Settings.currentState2) relayStatus = true;
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"0\", \"power\":\"" + String(relayStatus? "on" : "off") + "\"}");
  });
  server->on("/on1", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }
    relayControl(1, 1);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"1\", \"power\":\"" + String(Settings.currentState1? "on" : "off") + "\"}");
  });
  server->on("/on2", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(2, 1);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"2\", \"power\":\"" + String(Settings.currentState2? "on" : "off") + "\"}");
  });
  #elif defined SONOFF_IFAN02
  server->on("/on", []() {
    boolean relayStatus = false;
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }
    relayControl(1, 1);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"0\", \"power\":\"" + String(Settings.currentState1? "on" : "off") + "\"}");
  });
  server->on("/fan1", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    setFanSpeed(1);
    server->send(200, "application/json", "{\"type\":\"fan\", \"number\":\"1\", \"speed\":\"" + String(getFanSpeed()) + "\"}");
  });
  server->on("/fan2", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    setFanSpeed(2);
    server->send(200, "application/json", "{\"type\":\"fan\", \"number\":\"1\", \"speed\":\"" + String(getFanSpeed()) + "\"}");
  });
  server->on("/fan3", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    setFanSpeed(3);
    server->send(200, "application/json", "{\"type\":\"fan\", \"number\":\"1\", \"speed\":\"" + String(getFanSpeed()) + "\"}");
  });
  
  #elif defined SONOFF_4CH
  server->on("/on", []() {
    boolean relayStatus = false;
    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }
    relayControl(0, 1);
    if (Settings.currentState1 || Settings.currentState2 || Settings.currentState3 || Settings.currentState4) relayStatus = true;
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"0\", \"power\":\"" + String(relayStatus? "on" : "off") + "\"}");
  });
  server->on("/on1", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(1, 1);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"1\", \"power\":\"" + String(Settings.currentState1? "on" : "off") + "\"}");
  });
  server->on("/on2", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(2, 1);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"2\", \"power\":\"" + String(Settings.currentState2? "on" : "off") + "\"}");
  });
  server->on("/on3", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(3, 1);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"3\", \"power\":\"" + String(Settings.currentState3? "on" : "off") + "\"}");
  });
  server->on("/on4", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(4, 1);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"4\", \"power\":\"" + String(Settings.currentState4? "on" : "off") + "\"}");
  });
  #else
  server->on("/on", []() {

    if (SecuritySettings.Password[0] != 0 && Settings.usePasswordControl == true)
    {
      if (!server->authenticate("admin", SecuritySettings.Password))
        return server->requestAuthentication();
    }

    relayControl(0, 1);
    server->send(200, "application/json", "{\"type\":\"relay\", \"number\":\"1\", \"power\":\"" + String(Settings.currentState1? "on" : "off") + "\"}");
  });
  #endif

  if (ESP.getFlashChipRealSize() > 524288) {
    if (Settings.usePassword == true && SecuritySettings.Password[0] != 0) {
      httpUpdater.setup(&*server, "/update", "admin", SecuritySettings.Password);
      httpUpdater.setProjectName(projectName);
    } else {
      httpUpdater.setup(&*server);
      httpUpdater.setProjectName(projectName);
    }
  }

  server->onNotFound(handleNotFound);

  server->begin();

  //Serial.printf("Starting SSDP...\n");
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

  //Serial.println("HTTP server started");
  //Serial.println(WiFi.localIP());

  timerUptime = millis() + Settings.uReport * 1000;

}

void loop()
{
  server->handleClient();

  #if defined SONOFF_DUAL
  buttonLoop();
  #endif

  if (needUpdate1 == true || needUpdate2 == true || needUpdate3 == true || needUpdate4 == true) {
    sendStatus(0);
  }

  #if defined SONOFF_IFAN02
    if (previousFanSpeed != getFanSpeed()) {
      sendStatus(98);
      previousFanSpeed = getFanSpeed();
    }
  #endif

  if (needUpdate1 == true) {
#ifdef SONOFF_TH
    checkTempAndHumidity();
#endif
    sendStatus(1);
    needUpdate1 = false;
  }

  if (needUpdate2 == true) {
    sendStatus(2);
    needUpdate2 = false;
  }

  if (needUpdate3 == true) {
    sendStatus(3);
    needUpdate3 = false;
  }

  if (needUpdate4 == true) {
    sendStatus(4);
    needUpdate4 = false;
  }

  if (millis() > timer1s)
    runEach1Seconds();

  if (millis() > timer5s)
    runEach5Seconds();

  if (millis() > timer1m)
    runEach1Minutes();

  if (millis() > timer5m)
    runEach5Minutes();

  if (Settings.uReport > 0 && millis() > timerUptime){
    sendStatus(99);
    timerUptime = millis() + Settings.uReport * 1000;
  }
    
#ifdef SONOFF_POW
  if (Settings.wReport > 0 && millis() > timerW)
    sendReport(1);

  if (Settings.vReport > 0 && millis() > timerV)
    sendReport(2);

  if (Settings.aReport > 0 && millis() > timerA)
    sendReport(3);

  if (Settings.vaReport > 0 && millis() > timerVA)
    sendReport(4);

  if (Settings.pfReport > 0 && millis() > timerPF)
    sendReport(5);

#endif

#ifdef SONOFF_TH
  if (Settings.tReport > 0 && millis() > timerT)
    sendReport(1);

  if (Settings.hReport > 0 && millis() > timerH)
    sendReport(2);
#endif

  if ((Settings.autoOff1 != 0) && inAutoOff1 && ((millis() - autoOffTimer1) > (1000 * Settings.autoOff1))) {
    relayControl(1, 0);
    autoOffTimer1 = 0;
    inAutoOff1 = false;
  }
  #if defined SONOFF_DUAL || defined SONOFF_4CH || defined SONOFF_IFAN02
  if ((Settings.autoOff2 != 0) && inAutoOff2 && ((millis() - autoOffTimer2) > (1000 * Settings.autoOff2))) {
    relayControl(2, 0);
    autoOffTimer2 = 0;
    inAutoOff2 = false;
  }
  #endif
  #if defined SONOFF_4CH || defined SONOFF_IFAN02
  if ((Settings.autoOff3 != 0) && inAutoOff3 && ((millis() - autoOffTimer3) > (1000 * Settings.autoOff3))) {
    relayControl(3, 0);
    autoOffTimer3 = 0;
    inAutoOff3 = false;
  }
  if ((Settings.autoOff4 != 0) && inAutoOff4 && ((millis() - autoOffTimer4) > (1000 * Settings.autoOff4))) {
    relayControl(4, 0);
    autoOffTimer4 = 0;
    inAutoOff4 = false;
  }
  #endif

}

#if defined SONOFF_DUAL
void buttonLoop() {
  if (Serial.available() >= 4) {
    unsigned char value;
    if (Serial.read() == 0xA0) {
      if (Serial.read() == 0x04) {
        value = Serial.read();
        if (Serial.read() == 0xA1) {
          int thisRead = (value > 3? HIGH : LOW);
          int lastRead = currentStatus;
          currentStatus = (value > 3? HIGH : LOW);
          
          if (value > 3 || thisRead != lastRead) {
            relayControl(0, 2);
            return;
          } 
          switch (value)
            {
            case 0x00: case 0x04: // All Off
            {
              if (Settings.currentState1) {
                needUpdate1 = true;
                Settings.currentState1 = false;
              }
              if (Settings.currentState2) {
                needUpdate2 = true;
                Settings.currentState2 = false;
              }
            break;
            }
            case 0x01: case 0x05: //Switch1 On
            {
              if (!Settings.currentState1) {
                needUpdate1 = true;
                Settings.currentState1 = true;
              }
              if (Settings.currentState2) {
                needUpdate2 = true;
                Settings.currentState2 = false;
              }
            break;
            }
            case 0x02: case 0x06: //Switch2 On
            {
              if (Settings.currentState1) {
                needUpdate1 = true;
                Settings.currentState1 = false;
              }
              if (!Settings.currentState2) {
                needUpdate2 = true;
                Settings.currentState2 = true;
              }
            break;
            }
            case 0x03: case 0x07: //All On
            {
              if (!Settings.currentState1) {
                needUpdate1 = true;
                Settings.currentState1 = true;
              }
              if (!Settings.currentState2) {
                needUpdate2 = true;
                Settings.currentState2 = true;
              }
            break;
            }
          }
        }
      }
    }
  }
}
#endif
