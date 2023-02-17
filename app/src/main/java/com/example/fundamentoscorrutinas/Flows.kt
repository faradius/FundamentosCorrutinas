package com.example.fundamentoscorrutinas

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.List
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main() {
    //coldFlow()
    //cancelFlow()
    //flowOperators()
    //terminalFlowOperators()
    //bufferFlow()
    //conflationFlow()
    //multiFlow()
    //flatFlows()
    //flowExceptions()
    completions()
}

fun completions() {
    runBlocking {
        newTopic("Fin de un Flujo (onCompletetion)")
        getCitiesFlow()
                //Una vez terminado el flujo se puede llamar onCompletion para ejecutar una acción
            .onCompletion { println("Quitar el progressBar...") }
            .collect{ println(it) }
        println()

        getMatchResultsFlow()
                //Aqui mostraremos las estadisticas del partido aunq marque error o haya salido bien
            .onCompletion { println("Mostrar las estadisticas...") }
            .catch { emit("Error: $this") }
            .collect{ println(it) }

        newTopic("Cancelar Flow")
        getDatabyFlowStatic()
            .onCompletion { println("Ya no le interesa al usuario...") }
            .cancellable()
            .collect{
                //Una vez que llegue a cumplir esta condición se cancelará el flujo de datos y por lo tanto el proximo valor ya no será recolectado,
                //en su lugar vamos a obtener una excepción
                if (it > 22.5f) cancel()
                println(it)
            }
    }
}

fun flowExceptions() {
    runBlocking {
        newTopic("Control de errores")
        newTopic("Try/Catch")
        /*try {
            //Utilizamos el metodo para simular un partido de football
            getMatchResultsFlow()
                .collect{
                    //Se muestra los valores de la simulación
                    println(it)
                    //se realiza una validación en donde encuentre un valor de 2 se creará un error ya que se habia acordad 1-1
                    if (it.contains("2")) throw Exception("Habian acordado 1-1")
                }
        } catch (e: Exception) {
            //con esto mostramos el error personalizado que creamos con throw
            e.printStackTrace()
            //Al final si detecta un 2 se mostrará algo asi 2-1 y automaticamente se detendrá el flujo sin importar en que minuto fue
        }*/

        //Esto es otra forma de cachar el error dentro de flow pero mas controlado, personalizado y utilizando su propia palabra reservada catch
        newTopic("Transparencia")
        getMatchResultsFlow()
            .catch {
                emit("Error: $this")
            }
            .collect{
                println(it)
                if (!it.contains("-")) println("Notifica al programador...")
            }
    }
}

//Este tema se requiere de mas analisis por que tiene un nivel de dificultad alto
fun flatFlows() {
    runBlocking {
        newTopic("Flujos de aplanamiento")

        newTopic("FlatMapConcat")
        getCitiesFlow()
            .flatMapConcat {cities ->  //Flow<Flow<Type>>
                getDataToFlatFlow(cities)
            }
            .map { setFormat(it) }
            //.collect{ println(it) }

        newTopic("FlatMapMerge")
        getCitiesFlow()
            .flatMapMerge {cities ->  //Flow<Flow<Type>>
                getDataToFlatFlow(cities)
            }
            .map { setFormat(it) }
            .collect{ println(it) }
    }
}

fun getDataToFlatFlow(city: String): Flow<Float> = flow {
    (1..3).forEach{
        println("Temperatura de ayer en $city...")
        emit(Random.nextInt(10,30).toFloat())

        println("Temperatura actual en $city:")
        delay(100)
        emit(20 + it + Random.nextFloat())
    }
}

fun getCitiesFlow(): Flow<String> = flow{
    listOf("Santander", "CDMX", "Lima")
        .forEach{city ->
            println("\nConsultando ciudad...")
            delay(1_000)
            emit(city)
        }
}

//Zip - Combine -> estos dos metodos nos ayuda a mezclar dos flujos por medio de la libreria de flow
fun multiFlow() {
    runBlocking {
        //Zip es una forma de mezclar dos flujos y a este resultado se le conoce como composición de flujo
        newTopic("Zip - Combine")
        getDatabyFlowStatic()
            .map { setFormat(it) }
                //zip nos dara como resultado un flujo tan grande como el de menor tamaño en este caso getDatabyFlowStatic()
                //por lo que solo imprimirá 5 resultados y no 45 como lo hace getMatchResultsFlow(), y el resultado se puede ver
                //que se ejecutan los dos flujos pero juntos
            //.zip(getMatchResultsFlow()){ degrees, result ->

            //Con combine aqui si podemos llegar al minuto 45, comienza desde el minuto 0,1,2,3,4,5.. comienza hacer todos los recorridos
                //hasta que porfin obtiene la primera temperatura y es aqui donde ya empieza a mezclar con el marcador del partido, pero
                //mientras el ya estuvo trabajando solo con los minutos, no estuvo esperando hasta que los dos tuvieran valor como lo hace zip,
                //por otra parte podemos ver que se completa de acuerdo al flujo mas grande, no importa que la temperatura ya no pueda cambiar
                //por que su flujo unicamente llegaba a cinco elementos mientras que la temperatura siguio cambiando
            .combine(getMatchResultsFlow()){ degrees, result ->
                "$result with $degrees"
            }
            .collect{ println(it) }
    }
}

fun conflationFlow() {
    runBlocking {
        newTopic("Fusión")
        val time = measureTimeMillis {
            getMatchResultsFlow()
                    //Funciona tomando solo el ultimo valor emitido, si el collect en este caso toma 100 ms, que es mas del tiempo
                    //tiempo en que se tarda en emitir los datos en comparación de los 50 ms, entonces lo unico que se tomará en cuenta
                    //es el ultimo valor, esto es toltalmente importante si queremos notificarle a nuestros usuarios cual es el marcador del partido
                    //al usuario ya no le importa lo que paso en el minuto anterior si en este momento el marcador ya cambio
                    //Se hacen comparaciones de tiempo al mostrar los resultados, es mas rapido el conflate
                .conflate() //3074ms
                //.buffer() //5131ms
                    //Es ideal para aquellos escenarios como informes o resumenes de datos
                //.collectLatest {//3071ms //pero solo imprime el resultado final osea hasta que termine el partido o los minutos mostrará el resultado final
                .collect{//8016ms
                    delay(100)
                    println(it)
                }
        }
        println("Time: ${time}ms")
    }
}

//Simula un partido de futbol en donde muestra el marcador de dos equipos los goles que metieron de forma aleatoria
fun getMatchResultsFlow(): Flow<String> {
    return flow {
        var homeTeam = 0
        var awayTeam = 0
        (0..45).forEach{
            println("minuto: $it")
            delay(50)
            homeTeam += Random.nextInt(0,21)/20
            awayTeam += Random.nextInt(0,21)/20
            emit("$homeTeam-$awayTeam")

            if (homeTeam == 2 || awayTeam == 2) throw Exception("Habian acordado 1 y 1 :v")
        }
    }
}

//El buffer ayuda a mejorar el tiempo transcurrido en lo que se procesan los datos haciendo que se reduzca el tiempo de respuesta
//en la emición de datos. Habiamos visto antes cuando se trabajo con corrutinas las tareas eran de larga duración, en estas
//pudimos dividir el trabajo para ahorrar el tiempo de las tareas de forma asincrona en paralelo, pero que pasa si esas tareas pertenecen
// a un elemento dentro de un flow?, pues precisamente se a creado buffer
fun bufferFlow() {
    runBlocking {
        newTopic("Buffer para Flow")
        //aqui se indica el tiempo que a pasado en milisegundos, capturando toodo el tiempo transcurrido al ejecutar el bloque de codigo interno
        val time = measureTimeMillis {
            //en este se especifica el tiempo de forma estatica en el metodo getDataByFlowStatic()
            getDatabyFlowStatic()
                .map { setFormat(it) }
                    //Con esto indicamos que junte lo maximo posible los procesos, respetando por supuesto la integridad de nuestro flujo
                .buffer() //Verificar los tiempos sin (mas lento) y con buffer (mas rapido)
                .collect{
                    delay(500)
                    println(it)
                }
        }
        println("Time: ${time}ms")
    }

}


fun getDatabyFlowStatic(): Flow<Float> {
    return flow {
        (1..5).forEach{
            println("procesando datos...")
            delay(300)
            emit(20 + it + Random.nextFloat())
        }
    }
}

fun terminalFlowOperators() {
    runBlocking {
        newTopic("Operadores Flow Terminales")
        //Lo que hace list es convertir un flow en una lista, es decir va a procesar los datos y recolectarlos
        //al terminar se obtendra el resultado final presentandonos los datos en forma de lista, esto es muy util
        //para aquellos flujos donde no se requiera un gran tiempo de recolección
        newTopic("List")
        val list = getDatabyFlow()
            .toList()
        println("List: $list")

        //Esto es para una consulta puntual, osea que solo va esperar una sola emisión de datos, si fuera dos o mas
        //marcaria un error
        newTopic("Single")
        val single = getDatabyFlow()
            //.take(1)
            //.single()
        println("Single: $single")


        //Este simplemente imprime el primer valor
        newTopic("First")
        val first = getDatabyFlow()
                //Este lo que hace es cancelar de forma interna el flujo despues de emitir el primer valor
            //.first()
        println("First: $first")

        //y este el ultimo valor
        newTopic("Last")
        val last = getDatabyFlow()
                //sin embargo last va a continuar su flujo hasta que el ultimo valor sea emitido
           // .last()
        println("Last: $last")


        //Reduce nos va a devolver un solo valor que generalmente es para acumular toodo el recorrido
        //o algun procesamiento que se necesite en especifico, en este caso vamos hacer el ejemplo como si se tratará
        //de un ahorro que tengamos
        newTopic("Reduce")
        val saving = getDatabyFlow()
                //el acomulador va a comenzar en el indice 0 y el valor en el indice 1, como si fuera un arreglo
            .reduce{ accumulator, value ->
                //Imprimimos que es lo que contiene cada variable
                println("Accumulator: $accumulator")
                println("Value: $value")
                println("Current saving: ${accumulator + value}")
                //al final es necesario devolver un valor
                accumulator + value
            }

        //aqui nos va arrojar la suma total que ha estado emitiendo cada valor
        println("Saving: $saving")


        //Este es un operador terminal que complementa a reduce, no necesariamente se ocupa con reduce, si no que se puede
        //complementar con otros operadores de terminal o un flujo de trabajo. La diferencia entre reduce y fold es que ahora
        //el acumulador va a comenzar en la posición 0, tambien el acumulador va a adquirir lo que tenga el valor de lastsaving
        //o lo que hayamos definido, por otra parte value tendrá el indice 0
        newTopic("Fold")
        val lasSaving = saving
        val totalSaving = getDatabyFlow()
            .fold(lasSaving) { acc, value ->
                println("Accumulator: $acc")
                println("Value: $value")
                println("Current saving: ${acc + value}")
                acc + value
            }

        println("TotalSaving: $totalSaving")
        //esto es perfecto para aquellas ocaciones donde nosotros requerimos recolectar algun tipo de dato,
        //que se puede pausar y despues reanudar, pero esto puede aplicarse en multiples escenarios, recordemos
        //que los flujos son una transmisión de datos en la que normalmente vamos a desconocer el tamaño de ese flujo
        //por lo tanto requerimos este tipo de soluciones nuevas que son toltamente especializadas en estos nuevos flujos de datos
    }
}

//Los operadores intermediarios transforman un flujo por medio de los mismos, tambien se consideran call,
//asi que no se consumirán los recursos hasta que sean solicitados
fun flowOperators() {
    runBlocking {
        newTopic("Operadores Flow Intermediarios")
        //Las ventajas que se tiene con este operador es que se puede ejecutar una función suspendida dentro de su bloque de codigo
        newTopic("Map")
        getDatabyFlow() //Se obtienen los datos y en el subproceso se da un mejor formato
            .map {
                //setFormat(it)
                setFormat(convertCelsToFahr(it), "F")
                //Se imprimirá la ultima función definida en el map
            }
        //Mientras que no se llame este metodo de este flow no va a comenzar, por lo tanto no va a imprimir nada
        //.collect{ println(it) } //Aqui indicamos que se mostrará la colleción de datos en consola

        newTopic("Filter")
        getDatabyFlow()
            .filter {
                //Aqui lo que tenemos que hacer es agregar una condición para saber si el dato es apto o no para ser recolectado
                it < 23 //si la temperatura actual es menor a 23 entonces muestramela si es mayor a eso entonces ya no me interesa, funciona como un if
            }
            .map {
                setFormat(it)
            }
        //.collect{ println(it) } //El resultado va a mostrar unicamente aquellos que no rebasen de los 23 grados, si se sobre pasan no son recolectados
        //Esto puede usarse cuando se personaliza las notificaciones o la alimentación de un tipo de dato, ademas podemos combinar mas de un
        //operador intermediario para flow, otra cosa que debemos de tomar en cuenta es que el orden si importa


        //Este es mas general y esta preparado para procesos mas complejos, tiene la caracteristica peculiar de que se va a emitir al menos un
        //valor por que esta preparado para emitir varios valores
        newTopic("Transform")
        //si solo se ejecuta un solo emit se podria parecer al map pero ya dos o mas es lo que lo diferencia del map
        getDatabyFlow()
            //Este es el operador intermediario ideal para aquellos escenarios donde requerimos distribuir la información que estamos emitiendo
            //en mas de un canal o simplemente cuando requerimos multiples procesamientos
            .transform {
                //Emit es tambien una función suspendida, asi que el rendimiento es bastante bueno si lo hacemos desde este lado
                emit(setFormat(it))
                emit(setFormat(convertCelsToFahr(it), "F"))
            }
        //.collect{ println(it) }


        //Take nos ayuda a limitar el tamaño de nuestro flujo, recordemos que los flujos pueden ser infinitos,
        //entonces esta seria una solución muy facil para cuando nosotros queremos limitar los resultados que recibe el usuario
        //o simplemente no queremos, en algun apartado de nuestra aplicación tener en tiempo real todos los resultados que van lleganod
        newTopic("Take")
        getDatabyFlow()
            .take(3)
            .map { setFormat(it) }
            .collect { println(it) }
    }
}

fun convertCelsToFahr(celsius: Float): Float = ((celsius * 9) / 5) + 32

//Este metodo va a transformar los grados a un string y se le define que tendra un decimal y se concatena los centigrados y los grados
//ya definidos a centigrados
fun setFormat(temperature: Float, degree: String = "C"): String = String.format(
    Locale.getDefault(),
    "%.1f°$degree", temperature
)

fun cancelFlow() {
    runBlocking {
        newTopic("Cancelar Flow")
        val job = launch {
            getDatabyFlow().collect { println(it) }
        }
        delay(someTime() * 2)
        //Un flow se cancela automaticamente con la corrutina que la contiene
        job.cancel()
    }
}

//Cold es llamado asi por que debido a que un flujo no comenzará a enviar valores, hasta que su colector sea llamado. Mientras tanto quedará
//en estado de suspendido
fun coldFlow() {
    newTopic("Flows are Cold")
    runBlocking {
        val dataFlow = getDatabyFlow()
        println("esperando...")
        delay(someTime())
        //solo hasta que se mande a llamar la variable es cuando se va a ejecutar, mientras estará suspendidad si no se le hace la llamada
        dataFlow.collect { println(it) }
    }
}
