/**
 *  Ask Alexa - Lambda Code
 *
 *  Version 1.0.0 - 5/10/16 Copyright © 2016 Michael Struck
 *  Special thanks for Keith DeLong for code and assistance
 *  
 *  Version 1.0.0 - Initial release
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
'use strict';
exports.handler = function( event, context ) {
   var version = '1.0.0';
   var https = require( 'https' );
   // Paste app code here between the breaks------------------------------------------------
    var STappID = 'f42605b0-5e04-40d6-a042-8efec7ef7172';
    var STtoken = '67deb4cb-5c8e-40fc-b942-2688b430a01c';
    var url='https://graph.api.smartthings.com:443/api/smartapps/installations/' + STappID + '/' ;
   //---------------------------------------------------------------------------------------
   var cardName ="";
   console.log (event.request.type);
   if (event.request.type == "LaunchRequest") {
        var speech = "Simply give me a device and a command, or ask me the status of a device, and I will carry out your request.";
        cardName = "Ask Alexa Help";
        output(speech, context, cardName);
   }
   else if (event.request.type === "SessionEndedRequest"){
   }
   else if (event.request.type == "IntentRequest") {
        var process = false;
        var intentName = event.request.intent.name;
        if (intentName == "DeviceOperation") {
            var Operator = event.request.intent.slots.Operator.value;
            var Device = event.request.intent.slots.Device.value;
            var Num = event.request.intent.slots.Num.value;
            var Param = event.request.intent.slots.Param.value;
            url += 'd?Device=' + Device + '&Operator=' + Operator + '&Num=' + Num + '&Param=' + Param; 
            process = true;
            cardName = "SmartThings Device Operation";
        } 
        else if (intentName == "ReportOperation") {
            var Report = event.request.intent.slots.Report.value;
            url += 'r?Report=' + Report;
            process = true; 
            cardName = "SmartThings Reports";
        }
        else if (intentName == "DeviceStatus") {
            var Status = event.request.intent.slots.Status.value;
            url += 's?Device=' + Status;
            process = true;
            cardName = "SmartThings Status Report";
        }
        else if (intentName == "ListOperation") {
            var Type = event.request.intent.slots.Type.value;
            url += 'l?Type=' + Type;
            process = true;
            cardName = "SmartThings Help";
        }
        else if (intentName == "VersionOperation") {
            url += 'v?Ver=' + version;
            process = true;
            cardName = "SmartThings Version Help";
        }
        else if (intentName == "SmartHomeOperation") {
            var SHCmd = event.request.intent.slots.SHCmd.value;
            var SHParam = event.request.intent.slots.SHParam.value;
            url += 'h?SHCmd=' + SHCmd + '&SHParam=' + SHParam;
            process = true;
            cardName = "SmartThings Home Operation";
        }
        else if (intentName == "AMAZON.HelpIntent") {
            var help = "With the Ask Alexa SmartApp, you can interface your "+
            "SmartThings household with me. This will allow you to give me commands "+
            "to turn off a light, or unlock a door. As an example you can simply say, "+
            "'tell SmartThings to turn off the living room', and I'll turn off that device. " +
            "In addition, you can query your devices to get information such as open or "+
            "close status, or find out the temperature in a room. To use this function, just give "+
            "me the device name. For example, you could say, 'ask SmartThings about the patio'. and I will "+
            "give you all of the common attributes I find with that device, including battery levels. "+
            "For those who are curious, this is version" + version +" of the Lambda code, written by Michael Struck.";
            output(help, context, "Ask Alexa Help");
        }
        if (!process) {
            output("I am not sure what you are asking. Please try again", context, "Ask Alexa Error");   
        }
        else {
            url += '&access_token=' + STtoken;
            https.get( url, function( response ) {
                response.on( 'data', function( data ) {
                var resJSON = JSON.parse(data);
                var speechText = "The SmartThings SmartApp returned an error. I was unable to complete your request";
                if (resJSON.voiceOutput) { speechText = resJSON.voiceOutput; }
                console.log(speechText);
                output(speechText, context, cardName);
                } );
            } );
        }
    }
};

function output( text, context, card ) {
   var response = {
      outputSpeech: {
         type: "PlainText",
         text: text
      },
      card: {
         type: "Simple",
         title: card,
         content: text
      },
   shouldEndSession: true
   };
   context.succeed( { response: response } );
}
