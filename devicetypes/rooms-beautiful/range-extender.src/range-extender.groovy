import physicalgraph.zigbee.zcl.DataType

metadata {
    definition (name: "Range Extender", namespace: "Rooms Beautiful", author: "Alex Feng", ocfDeviceType: "oic.d.switch") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"
        
        attribute("text", "string")
        attribute("totalChildren", "number")
        
        fingerprint profileId: "0104", inClusters: "0000, 0003, DC00, FC01", deviceJoinName: "Range Extender", manufacturer: "Rooms Beautiful",  model: "R001"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"totalChildren", type: "generic", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.totalChildren", key: "PRIMARY_CONTROL") {
            	attributeState "totalChildren", label:'${currentValue}', backgroundColor: "#00a0dc", defaultState: true// icon:"https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/repeater.png", backgroundColor:"#00A0DC"
            }
            
            tileAttribute ("device.text", key: "SECONDARY_CONTROL") {
            	attributeState "text", label:'Above number indicates how many devices are currently connected to this range extender', defaultState: true
            }
        }
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "totalChildren"
        details(["totalChildren", "refresh"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
	// FYI = event.name refers to attribute name & not the tile's name

	def linkText = getLinkText(device)
    def descMap = zigbee.parseDescriptionAsMap(description)
    def value
    def attrId
    
    if(descMap){
       	if( descMap.attrId ){
        	value = descMap.value
            attrId = Integer.parseInt(descMap.attrId, 16)
        }
            
        switch(descMap.clusterInt){
        	case 0xDC00:
				value = descMap.value
            	def shortAddr = value.substring(4)
                def lqi = zigbee.convertHexToInt(value.substring(2, 4))
            	def rssi = (byte)zigbee.convertHexToInt(value.substring(0, 2))
            	log.info "${linkText} - Parent Addr: ${shortAddr} **** LQI: ${lqi} **** RSSI: ${rssi}"                
                break;
        	case 0xFC01:
            	if(attrId == 0x1000){
                	def totalChildren = zigbee.convertHexToInt(value)// - 1                    
            		log.info "${linkText} - Total Children: ${totalChildren}"                    
                    sendEvent(name: "totalChildren", value: totalChildren)
                }
            	else if(attrId >= 0x0000 && attrId <=0x0019){
                	def shortAddr = value.substring(12)
                    def nodeRelation = value.substring(10, 12)
            		def rxLqi = zigbee.convertHexToInt(value.substring(8, 10))
                    def timeoutCounter = zigbee.convertHexToInt(value.substring(0, 8))
                    
                    def child = zigbee.convertToHexString(((byte)attrId + 1), 2)
                    if(child.startsWith('0'))
                    	child = child.substring(1)
                    if(child == 'a')
                    	child = 10
                    else if(child == '1a')
                    	child = 20
                                        
                    if(nodeRelation == "00"){
                        log.info "${linkText} - Parent [${child} of 20] = shortAddr: ${shortAddr} **** " +
                            "nodeRelation: ${nodeRelation} **** rxLqi: ${rxLqi} **** timeoutCounter: ${timeoutCounter}"
                    }
                    else if(nodeRelation == "ff"){
                    	log.info "${linkText} - Child [${child} of 20] = doesn't exist"
                    }
                    else{
                    	log.info "${linkText} - Child [${child} of 20] = shortAddr: ${shortAddr} **** " +
                            "nodeRelation: ${nodeRelation} **** rxLqi: ${rxLqi} **** timeoutCounter: ${timeoutCounter}"
                    }
                }
                else if(attrId >= 0x0020 && attrId <=0x0023){
                	def addr = new String[4]	// 4 addr
                    def y = 16					// 4 digits per addr
                    def set = attrId-0x0020+1	// Set x of 4
                    def text = "${linkText} - Neighbor addr set [${set} of 4] = "
                    
                    for(def x=0; x<4; x++){
                        addr[x] = value.substring(y-4, y)
                        y-=4   
                        
                        if(addr[x] == "fffe")
                        	addr[x] = "doesn't exist"
                        
                        if(x == 3)
                        	text+= "${(set-1)*4 + x+1}: ${addr[x]}"
                        else
                        	text+= "${(set-1)*4 + x+1}: ${addr[x]} **** "

                        //log.debug "${(set-1)*4 + x+1} - Set:" + set
                    }
                
                	log.info text
                }
                else if(attrId >= 0x0024 && attrId <=0x0025){
                	def rxLqi = new String[8]	// 8 rxLqi
                    def y = 16					// 2 digits per rxLqi
                    def set = attrId-0x0024+1	// Set # of 2
                    def text = "${linkText} - Neighbor lqi set [${set} of 2] = "
                    
                    for(def x=0; x<8; x++){
                        rxLqi[x] = zigbee.convertHexToInt(value.substring(y-2, y))
                        y-=2
                        
                        if(rxLqi[x] == "0")
                        	rxLqi[x] = "doesn't exist"
                        
                        if(x == 7)
                        	text+= "${(set-1)*8 + x+1}: ${rxLqi[x]}"
                        else
                        	text+= "${(set-1)*8 + x+1}: ${rxLqi[x]} **** "

                        //log.debug "${(set-1)*8 + x+1} - Set:" + set
                    }
                
                	log.info text
                }
            	break;
            default:
                log.warn "DID NOT PARSE MESSAGE for description : $description"
                log.debug descMap
            	break;
        }
    }
}

def refresh() {
	zigbee.readAttribute(0xDC00, 0x0000) +	// Link Info
    
	zigbee.readAttribute(0xFC01, 0x1000)	// Total Children
    
	//zigbee.readAttribute(0xFC01, 0x0000) 	// 0x0000 - 0x0019 children
    /*zigbee.readAttribute(0xFC01, 0x0001) +
    zigbee.readAttribute(0xFC01, 0x0002) +
    zigbee.readAttribute(0xFC01, 0x0003) +
    zigbee.readAttribute(0xFC01, 0x0004) +
    zigbee.readAttribute(0xFC01, 0x0005) +
    zigbee.readAttribute(0xFC01, 0x0006) +
    zigbee.readAttribute(0xFC01, 0x0007) +
    zigbee.readAttribute(0xFC01, 0x0008) +
    zigbee.readAttribute(0xFC01, 0x0009) +
    zigbee.readAttribute(0xFC01, 0x0010) +
    zigbee.readAttribute(0xFC01, 0x0011) +
    zigbee.readAttribute(0xFC01, 0x0012) +
    zigbee.readAttribute(0xFC01, 0x0013) +
    zigbee.readAttribute(0xFC01, 0x0014) +
    zigbee.readAttribute(0xFC01, 0x0015) +
    zigbee.readAttribute(0xFC01, 0x0016) +
    zigbee.readAttribute(0xFC01, 0x0017) +
    zigbee.readAttribute(0xFC01, 0x0018) +
    zigbee.readAttribute(0xFC01, 0x0019) +*/
    
    /*zigbee.readAttribute(0xFC01, 0x0020) 	// 0x0020 - 0x0023 neighbor addr
    zigbee.readAttribute(0xFC01, 0x0021) +
    zigbee.readAttribute(0xFC01, 0x0022) +
    zigbee.readAttribute(0xFC01, 0x0023) +
    
    zigbee.readAttribute(0xFC01, 0x0024) +	// 0x0024 - 0x0025 neighbor rxLqi
    zigbee.readAttribute(0xFC01, 0x0025)*/
}

def ping() {
    return refresh()
}

def configure() {
    return refresh()
}

def installed() {
	sendEvent(name: "totalChildren", value: 0)
	response(refresh())
}