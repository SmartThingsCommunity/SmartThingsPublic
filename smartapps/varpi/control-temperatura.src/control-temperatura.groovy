/**
 *  Control temperatura
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
    name: "Control temperatura",
    namespace: "varpi",
    author: "Ismael Vargas Pina",
    description: "Control de temperatura para activar / desactivar ventiladores y calentadores",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png",
    pausable: true
)

preferences {
	
	section("Activación") {
		// TODO: put inputs here
        input "nombreHabitacion", "text", required: true, title: "Nombre habitación controlada"
        icon(title: "Icono", required: true)
        input "active", "bool", required: true, title: "¿Activar?"
        
	}
	section("Dispositivos a incluir en el control de temperatura") {
		// TODO: put inputs here
        input "sensorTemperatura", "capability.temperatureMeasurement", required: true, title: "¿Sensor de temperatura a verificar?"
        input "enchufes", "capability.switch", required: true, title: "¿Qué enchufes serán controlados?", multiple: true
	}
    section("Control de la temperatura") {
		
		input "setpoint", "decimal", title: "Temperatura deseada..."
		input "mode", "enum", title: "Seleccione 'calor' para calefacción o 'frio' para ventiladores / aire acondicionado...", options: ["calor","frio"]
		
		paragraph "Si elegimos calor, se activará el enchufe cuando la temperatura sea menor que la deseada para calentar la habitación hasta que supere la temperatura deseada."
		paragraph "Si elegimos frío, se activará el enchufe cuando la temperatura sea mayor que la deseada para enfriar la habitación hasta que baje de la temperatura deseada."
		
			
		
	}
        
	
	
	section("Control del horario") {
		// TODO: put inputs here
        input "horaInicio", "time", required: true, title: "¿A partir de qué hora?"
        input "horaFin", "time", required: true, title: "¿Hasta qué hora?"
        
	}
    

	
    section("Via notificación push y/o mensaje SMS"){
		input("recipients", "contact", title: "Enviar notificaciones a") {
			input "phone", "phone", title: "Introduce un número de teléfono a enviarle el SMS", required: false
			paragraph "Si está fuera de USA, por favor incluye por delante el código de país"
			input "pushAndPhone", "bool", title: "Notificar vía Push", required: false
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
	
    subscribe(sensorTemperatura, "temperature", temperatureHandler)
    
	
    //lo lanzamos ahora, y luego lo programamos para la hora de encencido y la hora de fin para el resto de días
    //Ahora se lanza si o si por si estoy estableciendo la programación dentro del periodo definido. Así no nos saltamos
    //verificar si hace falta encenderlo al principio
    comprobarAlInicioPeriodo();
    
    //Lo programamos para el comienzo y fin del periodo de los siguientes dias. Lo programamos un minuto despues del inicio y 
    //un minuto antes del fin para que no fallen las comprobaciones de dentro del rango
    schedule(stringDateAddMinutes(horaInicio,1), comprobarAlInicioPeriodo) // la hora prevista de inicio + 1 minuto
    schedule(stringDateAddMinutes(horaFin,-1), apagarAFinPeriodo) // la hora prevista de fin - 1 minuto
    
	
}



def comprobarAlInicioPeriodo (){

	notificar("${nombreHabitacion}. Comienza periodo de funcionamiento") 
	
    
	if (comprobarSiEjecutarRutina() ){
		log.debug "Comprobamos la temperatura inicial"
		
		def temperaturaActual = sensorTemperatura.temperatureState
		 
		log.debug "Temperatura inicial (número): ${temperaturaActual.doubleValue}"
		
		evaluate(temperaturaActual.doubleValue, setpoint)
	}
}

def apagarAFinPeriodo(){

	notificar("${nombreHabitacion}. Finaliza periodo de funcionamiento") 
    
	enchufes.off()
			
	if (mode == "frio"){
		notificar("Apagamos los ventiladores/aires acondicionados en ${nombreHabitacion} porque ha finalizado el periodo de encendido programado.") 
	} else if (mode == "calor"){
		notificar("Apagamos la calefacción en ${nombreHabitacion} porque ha finalizado el periodo de encendido programado.") 
		
	} else {
		notificar("Apagamos en ${nombreHabitacion} los enchufes al finalizar el periodo de encendido programado.") 
    }
}



def temperatureHandler(evt){
	
	if (comprobarSiEjecutarRutina() ){
		
		log.debug "Ha cambiado la temperatura. La evaluamos."
			
		evaluate(evt.doubleValue, setpoint)
		
	}
}



private evaluate(currentTemp, desiredTemp){
	log.debug "EVALUATE($currentTemp, $desiredTemp)"
	def threshold = 1.0
	if (mode == "frio") {
		// aire acondicionado / ventilador
		if (currentTemp - desiredTemp >= threshold) {
			
			encender(currentTemp, desiredTemp)
		}
		else if (desiredTemp - currentTemp >= threshold) {
			
			apagar(currentTemp, desiredTemp)
		}
	}
	else {
		// calefaccion
		if (desiredTemp - currentTemp >= threshold) {
			
			encender(currentTemp, desiredTemp)
		}
		else if (currentTemp - desiredTemp >= threshold) {
			
			apagar(currentTemp, desiredTemp)
		}
	}
}


def encender(currentTemp, desiredTemp){

	enchufes.on()
			
	if (mode == "frio"){
		notificar("Encendemos los ventiladores/aires acondicionados en ${nombreHabitacion} porque la temperatura ha subido por encima de la temperatura deseada. (Temperatura deseada = $desiredTemp, Actual = $currentTemp)") 
	} else if (mode == "calor"){
		notificar("Encendemos la calefacción en ${nombreHabitacion} porque la temperatura ha bajado por debajo de la temperatura deseada (Temperatura deseada = $desiredTemp, Actual = $currentTemp)") 
		
	} else {
		notificar("Encendemos los enchufes en ${nombreHabitacion}") 
	}
}

def apagar(currentTemp, desiredTemp){

	enchufes.off()
			
	if (mode == "frio"){
		notificar("Apagamos los ventiladores/aires acondicionados en ${nombreHabitacion} porque la temperatura está por debajo de la temperatura deseada. (Temperatura deseada = $desiredTemp, Actual = $currentTemp)") 
	} else if (mode == "calor"){
		notificar("Apagamos la calefacción en ${nombreHabitacion} porque la temperatura está por encima de la temperatura deseada (Temperatura deseada = $desiredTemp, Actual = $currentTemp)") 
		
	} else {
		notificar("Apagamos los enchufes en ${nombreHabitacion}") 
	}
}





def comprobarSiEjecutarRutina() {
	
	
    log.debug  "Verificamos si lanzar la rutina de control de temperatura en ${nombreHabitacion} a las  ${new Date().format('HH:mm:ss',location.timeZone)}";
    
	def ejecutar = false;
	
	
    def between = timeOfDayIsBetween(horaInicio, horaFin, new Date(), location.timeZone)
	
    
    
    
    if (active && between){
    	String mensaje = "Ejecutaremos la rutina en ${nombreHabitacion} porque está activa y la hora actual (${new Date().format('HH:mm:ss',location.timeZone)}) está entre la hora de inicio (${formatStringDate2StringTime(horaInicio)}) y la hora de fin (${formatStringDate2StringTime(horaFin)})"
        log.debug mensaje
        
      	
		ejecutar = true
		
		
    }
    else {
    	log.debug "No se ejecuta la rutina en ${nombreHabitacion} porque está inactiva o la hora actual (${new Date().format('HH:mm:ss',location.timeZone)}) NO está entre la hora de inicio (${formatStringDate2StringTime(horaInicio)}) y la hora de fin (${formatStringDate2StringTime(horaFin)})"
        //apagamos los enchufes por si se han quedado encendidos al terminarse la hora de la rutina
        enchufes.off()
        
        ejecutar = false
    }
    
	
	ejecutar
    
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

private formatStringDate2StringTime (stringISO8601Date){

	//Los parametros que se cogen como tipo "time" en las preferences, son cadenas String en formato ISO8601.
    //No son por tanto objetos Date
	Date date = Date.parse( "yyyy-MM-dd'T'HH:mm:ss.SSSZ", stringISO8601Date )
    def df = new java.text.SimpleDateFormat("HH:mm:ss")
    df.setTimeZone(location.timeZone)
    def stringTime = df.format(date)
	
    stringTime
}

private formatDate2stringISO8601Date (dateObject){

	//Los parametros que se cogen como tipo "time" en las preferences, son cadenas String en formato ISO8601.
    //No son por tanto objetos Date
    def df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    df.setTimeZone(location.timeZone)
    def stringTime = df.format(dateObject)
	
    stringTime
}

private stringDateAddMinutes (stringISO8601Date, minutes){

	//Los parametros que se cogen como tipo "time" en las preferences, son cadenas String en formato ISO8601.
    //No son por tanto objetos Date
	Date date = Date.parse( "yyyy-MM-dd'T'HH:mm:ss.SSSZ", stringISO8601Date )
    
    def millis = date.getTime()
    
    millis = millis + minutes * 60000
    
    Date date2 = new Date(millis)
   	
    date2
}