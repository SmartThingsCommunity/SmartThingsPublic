/**
 *  Simulador de presencia
 *
 *  Copyright 2018 Ismael Vargas Pina
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
definition(
    name: "Simulador de presencia",
    namespace: "varpi",
    author: "Ismael Vargas Pina",
    description: "Simulaci\u00F3n de presencia a trav\u00E9s del encendido programado y aleatorio de los dispositivos",
    category: "Safety & Security",
    /*iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")*/
    
    iconUrl: "https://www.iconfinder.com/icons/60204/download/png/64",
    iconX2Url: "https://www.iconfinder.com/icons/60204/download/png/128",
    iconX3Url: "https://www.iconfinder.com/icons/60204/download/png/256")


preferences {
	section("Activación") {
		// TODO: put inputs here
        input "active", "bool", required: true, title: "¿Activar?"
	}
	section("Dispositivos a incluir en la simulación") {
		// TODO: put inputs here
        input "luz", "capability.switch", required: true, title: "¿Qué luz simulará presencia?"
	}
    section("Configuración de la simulación") {
		// TODO: put inputs here
        input "horaBase", "time", required: true, title: "¿A partir de qué hora?"
        input "maxMinutesFromHoraBase", "number", required: true, title: "¿+- cuantos minutos?"
        input "baseOnMinutes", "number", required: true, title: "¿Cuantos minutos encendido de base?"
        input "maxAditionalOnMinutes", "number", required: true, title: "¿+- cuantos minutos encendido?"
        input "numMinRepeticiones", "number", required: true, range: "0..10", title: "¿Encender cuantas veces como mínimo?"
        input "numMaxRepeticiones", "number", required: true, range: "1..10", title: "¿Encender cuantas veces como máximo?"
        input "baseBetweenMinutes", "number", required: false, title: "¿Cuantos minutos entre encendidos de base?"
        input "maxAditionalBetweenMinutes", "number", required: false, title: "¿+- cuantos minutos entre encendidos?"
        input "activeIfRain", "bool", required: true, title: "¿Activar si va a llover?"
        
	}
    
    section("Via notificación push y/o mensaje SMS"){
		input("recipients", "contact", title: "Enviar notificaciones a") {
			input "phone", "phone", title: "Introduce un número de teléfono a enviarle el SMS", required: false
			paragraph "Si está fuera de USA, por favor incluye por delante el código de país"
			input "pushAndPhone", "bool", title: "Notificar vía Push", required: false
            input "notificarDetalle", "bool", required: true, title: "¿Notificar en detalle el proceso de ejecución?"
		}
	}
    
    
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    schedule(horaBase, "verificarSiEjecutarRutina")
}

// TODO: implement event handlers
// called every day at the time specified by the user
def verificarSiEjecutarRutina(evt) {
    log.debug  "Lanzada rutina de simulación de presencia a las  ${new Date().format('HH:mm:ss',location.timeZone)}";
    
    
    
    if (active){
    	log.debug "Ejecutamos la rutina porque está activa."
        
        def meteorologiaJson = getTwcConditions();
        
   		log.debug  "Previsión meteorológica: $meteorologiaJson";
    	
        if (!llueve(meteorologiaJson) ) {
        	verificarNumeroRepeticiones(evt) 
        } else if (llueve(meteorologiaJson) && activeIfRain){
        	verificarNumeroRepeticiones(evt) 
        } else if (llueve(meteorologiaJson) && !activeIfRain){
        	String mensaje = "No ejecutaremos la rutina porque está lloviendo y hemos configurado esta rutina para no ejecutarse con lluvia."
        	log.debug mensaje
        	notificar(mensaje)  
        } else {
    		String mensaje = "No ejecutaremos la rutina porque algo no ha ido bien verificando si hemos de ejecutarla o no."
        	log.debug mensaje
        	notificar(mensaje)  
   		}
    }
    else {
    	log.debug "No se ejecuta la rutina porque está inactiva."
    }
    
    
}

def verificarNumeroRepeticiones(evt) {
    
        //Calculamos el numero de repeticiones     
        def numRepeticiones = 0
        
        //El nº de repeticiones mínima no puede ser negativo
        if (numMinRepeticiones < 0){
        	numMinRepeticiones = 0;
        }
        
        if (numMinRepeticiones < numMaxRepeticiones) {
        	//def randomMaxRepeticiones = new Random().nextInt(numMaxRepeticiones - numMinRepeticiones);
        	//def randomMaxRepeticiones = Math.abs( new Random().nextInt() % (numMaxRepeticiones - numMinRepeticiones + 1) ) //+1 para que introduzca el limite superior
            def randomMaxRepeticiones = new Random().nextInt(numMaxRepeticiones - numMinRepeticiones + 1); //+1 para que introduzca el limite superior
       		numRepeticiones = numMinRepeticiones + randomMaxRepeticiones;
            
            
        } else {
        	numRepeticiones = numMinRepeticiones
        }
       
       
       
       
        def mensaje = "${luz.displayName}. ${numRepeticiones} simulaciones de presencia programadas hoy.";
		log.debug mensaje 
        notificar(mensaje) 
		
        prepararEncendido(numRepeticiones, true);
        
}



private prepararEncendido(numRepeticiones, Boolean esPrimeraEjecucion){
	
    def mensaje = "";
    
	//Relanzamos si hay al menos una repetición
    if (numRepeticiones > 0)
    {
    	mensaje = "${luz.displayName}. Quedan ${numRepeticiones} simulaciones de presencia pendientes."
		log.debug mensaje 
        if (notificarDetalle)
        	notificar(mensaje) 
    	
    	// tiempoEspera > tiempo de espera desde ahora mismo (now()) hasta que se encenderá la luz
        def tiempoEspera = 0;
        
        if (esPrimeraEjecucion){
        	if (maxMinutesFromHoraBase == null || maxMinutesFromHoraBase < 0)
            	maxMinutesFromHoraBase = 0;
                
        	// Es la primera ejecución
        	tiempoEspera = new Random().nextInt(maxMinutesFromHoraBase); //no hay base porque ya es la hora de ejecución a la que esta programado que se lance.
        } else {
        	if (baseBetweenMinutes == null || baseBetweenMinutes <= 0)
            	baseBetweenMinutes = 1
                
            if (maxAditionalBetweenMinutes == null || maxAditionalBetweenMinutes < 0)
            	maxAditionalBetweenMinutes = 0
                
        	// Es una repetición
            tiempoEspera = baseBetweenMinutes + new Random().nextInt(maxAditionalBetweenMinutes);
        }
        
        if (tiempoEspera < 1){
        	tiempoEspera = 1; //Al menos un minuto despues para que de tiempo a ejecutarse toda esta rutina
        }
        
        
        // OnMinutes > minutos que estará encendida la luz
    	def OnMinutes = baseOnMinutes + new Random().nextInt(maxAditionalOnMinutes); 
		if (OnMinutes < 1){
        	OnMinutes = 1; //Al menos un minuto despues para que de tiempo a ejecutarse toda esta rutina
        }
        
        
        
        // horaEncendidoMilisec > hora a la que se encenderá esta repetición
        def horaEncendidoMilisec = now() + tiempoEspera * 60 * 1000 //Entendiendo que now() devuelve milisegundos
		// horaApagadoMilisec > hora a la que se apagará esta repetición       		
        def horaApagadoMilisec = horaEncendidoMilisec + OnMinutes * 60 * 1000 //Entendiendo que now() devuelve milisegundos.
        
        def horaEncendido = new Date(horaEncendidoMilisec);  
        def horaApagado = new Date(horaApagadoMilisec);  
        
        
        //Encendemos
        mensaje = "El dispositivo ${luz.displayName} se encenderá a las ${horaEncendido.format('HH:mm:ss',location.timeZone)} (dentro de $tiempoEspera minutos), y se apagará a las ${horaApagado.format('HH:mm:ss',location.timeZone)} (después de $OnMinutes minutos encendido)."
        notificar(mensaje);  
     
     	def horaApagadoTexto = "" + horaApagado.format('HH:mm:ss',location.timeZone);
    
        
        def parametrosEncendido = [data: [horaApagado:horaApagado, horaApagadoTexto:horaApagadoTexto, OnMinutes: OnMinutes, numRepeticiones:numRepeticiones ]]
         
        runIn(tiempoEspera * 60, encender, parametrosEncendido)
    } else {
    
    	mensaje = "Fin de las repeticiones."
		log.debug mensaje 
        if (notificarDetalle)
        	notificar(mensaje) 
    	
        
	}
}




def encender(parametrosEncendido){
	luz.on()
    
    
    log.debug parametrosEncendido
    
        String mensaje = "El dispositivo ${luz.displayName} se ha Encendido. Se apagará a las ${parametrosEncendido.horaApagadoTexto} (después de ${parametrosEncendido.OnMinutes} minutos encendido)."
    log.debug mensaje
    if (notificarDetalle)
        	notificar(mensaje) 
    
    
    
    def parametrosApagado = [data: [ numRepeticiones:parametrosEncendido.numRepeticiones ]]
    
    //Apagamos
    runIn(parametrosEncendido.OnMinutes * 60, apagar, parametrosApagado)
    
}

def apagar(parametrosApagado){
	luz.off()
    
    String mensaje = "Apagamos el dispositivo ${luz.displayName}."
    log.debug mensaje
    if (notificarDetalle)
        	notificar(mensaje) 
    
    
	//Como hemos acabado la iteración, eliminamos uno del contador
	def numRepeticiones = parametrosApagado.numRepeticiones-1
    
    //Prepararmos la siguiente iteración
    prepararEncendido(numRepeticiones,false);
}



private llueve(meteorologiaJson) {

	log.debug "Control 1 lluvia > $meteorologiaJson"

	def estadosDeLluvia = ['chancerain', 'chancesleet', 'chancesnow', 'chancetstorms', 'flurries', 'sleet', 'rain', 'sleet', 'snow', 'tstorms']

	def result = false;
    
	if (meteorologiaJson) {
		
        def prediccion = meteorologiaJson?.wxPhraseShort.toLowerCase();
    	
        log.debug "Control 2 lluvia > $prediccion"
        notificar ("Predicción de hoy: $prediccion");
        
		if (prediccion) {
			
			for (int i = 0; i < estadosDeLluvia.size() && !result; i++) {
            	if (prediccion == estadosDeLluvia[i]){
					result = true;
                    
                    log.debug "Control 3 lluvia > encontrado $prediccion"
                    
                }
			}
            
            if (result == false){
            	log.debug "Control 5 lluvia > no llueve. $prediccion"
            }
            	
			
		} else {
			log.debug "Control 4 lluvia > no encontrado en los estados de lluvia"
		}
	} else {
		log.warn "No se ha obtenido una predicción meteorológica: $meteorologiaJson"
	}

	log.debug "el resultado es " + result;   
    
    return result;

}



private notificar(mensaje) {
	
	Map options = [:]

	
	options = [translatable: true, triggerEvent: evt]
	

	if (location.contactBookEnabled) {
   		log.debug 'Enviando notificaciones a contactos'
		sendNotificationToContacts(mensaje, recipients, options)
	} else {
		if (phone) {
			options.phone = phone
			if (pushAndPhone) {
				log.debug 'Enviando mensaje push y SMS'
				options.method = 'both'
			} else {
				log.debug 'Enviando SMS'
				options.method = 'phone'
			}
		} else if (pushAndPhone) {
			log.debug 'Enviando mensaje push'
			options.method = 'push'
		} else {
			log.debug 'No enviando nada'
			options.method = 'none'
		}
		sendNotification(mensaje, options)
        
        log.info mensaje
	}
	
}
