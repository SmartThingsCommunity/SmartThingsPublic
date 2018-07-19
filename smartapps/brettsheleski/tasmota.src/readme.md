# Tasmota SmartApp
SmartThings SmartApp for use with the SmartThings [Tasmota Device Handler](https://github.com/BrettSheleski/SmartThingsPublic/tree/master/devicetypes/brettsheleski/tasmota.src)

The Device Handler is used for updating devices running the [Sonoff-Tasmota](https://github.com/arendst/Sonoff-Tasmota) firmware.

See Tasmota Device Handler (https://github.com/BrettSheleski/SmartThingsPublic/tree/master/devicetypes/brettsheleski/tasmota.src) for more details.

## Installation
### Adding SmartApp to SmartThings
1. Log in to the SmartThings IDE (https://graph.api.smartthings.com/)
2. Go to `My SmartApps`
3. Click `New SmartApp`
4. In the `From Code` tab paste in the code from https://github.com/BrettSheleski/SmartThingsPublic/blob/master/smartapps/brettsheleski/tasmota.src/tasmota.groovy
5. Click `Create`
6. Click `Publish` --> `For Me`

### Installing SmartApp
Open the SmartThings app:
1.  Select `Automation` tab (on bottom)
2.  Select `SmartApps` tab (on top)
3.  Scroll to the bottom and select `Add a SmartApp`
4.  Scroll to the bottom and select `My Apps`
5.  Find the `Tasmota` SmartApp
6.  In the Devices input, select all devices using the `Tasmota` device handler.

### SmartApp Usage
The SmartApp exposes an HTTP endpoint which can be used to update the Tasmota devices.

1.  Get an Access Token and the URL for the endpoint of the SmartApp.

I've created a separate project to help get an Access Token and endpoint URL. See here [SmartThings SmartApp OAuth Helper](https://github.com/BrettSheleski/SmartThings-SmartApp-OAuth-Helper).
- Otherwise, see SmartThings documentation [here](http://docs.smartthings.com/en/latest/smartapp-web-services-developers-guide/authorization.html);

2.  Make HTTP calls to the SmartApp.

The SmartApp expects the MQTT topic of the target device in the format `prefix/topic/suffix`.  There `prefix` and `suffix` are ignored.  The SmartApp compares the `topic` recieved with the configured Tasmota devices.

using curl
```bash
curl -H "Authorization: Bearer <ACCESS_TOKEN_HERE>" -X POST "<ENDPOINT_URL_HERE>/status" -H "Content-Type: application/json" --data "{topic : 'prefix/<DEVICE_MQTT_TOPIC>/suffix'}"
```



## Example Usages
### Node-Red
A Node-Red flow can be created which will subscribe to the appropriate MQTT topic(s) for the device(s) and call make an HTTP request to the SmartThings SmartApp endpoint.

The following code is a Node-Red flow which I have running that listens to multiple devices and submits an HTTP request updating the appropriate SmartThings device.
```
[  
   {  
      "id":"91441c94.a2c89",
      "type":"mqtt in",
      "z":"5bff2855.44a8c8",
      "name":"Device 1",
      "topic":"stat/DEVICE_1_TOPIC/POWER",
      "qos":"2",
      "broker":"8833406a.5e73",
      "x":131,
      "y":564,
      "wires":[  
         [  
            "83847de0.0c011"
         ]
      ]
   },
   {  
      "id":"fdbf201f.d9dd9",
      "type":"http request",
      "z":"5bff2855.44a8c8",
      "name":"POST to SmartThings",
      "method":"POST",
      "ret":"obj",
      "url":"SMARTAPP_ENDPOINT_URL/status",
      "tls":"",
      "x":682.5,
      "y":665,
      "wires":[  
         [  

         ]
      ]
   },
   {  
      "id":"72453ff2.46a45",
      "type":"function",
      "z":"5bff2855.44a8c8",
      "name":"Set HTTP Headers",
      "func":"var httpMessage = {}\n\nhttpMessage.headers = {};\nhttpMessage.headers['Authorization'] = 'Bearer ACCESS_TOKEN';\nhttpMessage.headers['Content-Type'] = \"application/json\";\n\nhttpMessage.payload = msg;\n\nreturn httpMessage;",
      "outputs":1,
      "noerr":0,
      "x":560,
      "y":608,
      "wires":[  
         [  
            "fdbf201f.d9dd9"
         ]
      ]
   },
   {  
      "id":"83847de0.0c011",
      "type":"function",
      "z":"5bff2855.44a8c8",
      "name":"If Payload Is NOT JSON",
      "func":"try{\n    JSON.parse(msg.payload)\n}\ncatch(e){\n    return msg;\n}\n\nreturn;",
      "outputs":1,
      "noerr":0,
      "x":405,
      "y":537,
      "wires":[  
         [  
            "72453ff2.46a45"
         ]
      ]
   },
   {  
      "id":"b548867d.783428",
      "type":"mqtt in",
      "z":"5bff2855.44a8c8",
      "name":"Device 2 Name",
      "topic":"stat/DEVICE_2_TOPIC/POWER",
      "qos":"2",
      "broker":"8833406a.5e73",
      "x":131,
      "y":509,
      "wires":[  
         [  
            "83847de0.0c011"
         ]
      ]
   },
   {  
      "id":"8833406a.5e73",
      "type":"mqtt-broker",
      "z":"",
      "broker":"localhost",
      "port":"1883",
      "clientid":"",
      "usetls":false,
      "compatmode":true,
      "keepalive":"60",
      "cleansession":true,
      "willTopic":"",
      "willQos":"0",
      "willPayload":"",
      "birthTopic":"",
      "birthQos":"0",
      "birthPayload":""
   }
]
```
