package com.example.fundamentoscorrutinas

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

import kotlin.random.Random

fun main() {
    //dispatchers()
    //nested()
    //changeWithContext()
    basicFlows()
}
//Con este metodo mostramos que estas dos corrutinas que el hilo principal no ha sido bloqueado y en
//cambio pueden compartir el mismo espacio y se puede ver al ejecutar el codigo que se esta mezclando con la información
fun basicFlows() {
    //El resultado es similar a la secuencia pero con la diferencia que estamos trabajando con codigo
    //asincrono
    newTopic("Flows básicos")
    runBlocking {
        //aqui se esta ejecutando dos tareas al mismo tiempo o de forma paralela
        //gracias a los codigos asincronos de las corrutinas y de flow
        launch { getDatabyFlow().collect{ println(it) } }
        launch {
            (1..50).forEach{
                delay(someTime()/10)
                println("Tarea 2...")
            }
        }
    }

}

//Flow llego para resolver aquellos problemas donde existe codigo asincrono que retorna multiples valores,
//con las corrutinas devolviamos un solo valor, pero si requerimos estar enviando una secuencia de valores
//y no un solo valor, pues requerimos flow que representa una transmisión de datos procesados asicronamente
//esto es similar a las secuencias solo que enfocados en valores ansicronos (se refiere a que un proceso se
// realiza fuera del hilo principal sin interrumpirlo o esperar a que termine antes de continuar. )

//esta función no es suspendida por que eso la lleva el metodo collect getDatabyFlow().collect{ println(it) }
fun getDatabyFlow(): Flow<Float> {
    return flow {
        (1..5).forEach{
            println("procesando datos...")
            delay(someTime())
            //emit recolecta los datos generados, aparte funciona como yield en las secuencias,
            //emit hace que emita el proximo valor de forma fluida
            emit(20 + it + Random.nextFloat())
        }
    }
}

fun changeWithContext() {
    runBlocking {
        newTopic("withContext")
        starMsg()
        //Lo que estamos haciendo es que se pueda ejecutar una tarea dentro de la corrutina principal
        //pero con otro contexto, lo que cabe resaltar es que no estamos cambiando como tal la corrutina,
        //simplemente cambiamos de contexto de una tarea
        //Contexto main
        withContext(newSingleThreadContext("Corrutina con contexto")){
            //Contexto temporal
            starMsg()
            delay(someTime())
            println("Context")
            endMsg()
        }

        //WithContext nos dice que estamos cambiando el contexto de la corrutina pero lo demas
        //se sigue heredando de acuerdo al padre
        withContext(Dispatchers.IO){

            starMsg()
            delay(someTime())
            println("Petición al servidor")
            endMsg()
        }
        endMsg()
    }
}

fun nested() {
    runBlocking {
        newTopic("Anidar")
        val job = launch {
            starMsg()
            launch {
                starMsg()
                delay(someTime())
                println("Otra tarea")
                endMsg()
            }

            launch(Dispatchers.IO) {
                starMsg()
                launch(newSingleThreadContext("Corrutina Personalizada ANT")) {
                    starMsg()
                    delay(someTime())
                    println("Otra tarea con corrutina personalizada")
                    endMsg()
                }
                delay(someTime())
                println("Tarea en el servidor")
                endMsg()
            }

            var sum = 0
            (1..100).forEach{
                sum += it
                delay(someTime()/100)
            }
            println("Sum = $sum")
            endMsg()
        }

        //Si se cancela el padre se cancela automaticamente todos los hijos y esto nos ayuda a no estar
        //cancelando cada hijo
        delay(someTime()/2)
        job.cancel()
        println("Job cancelado...")
    }
}

fun dispatchers() {
    runBlocking {
        newTopic("Dispatchers")
        //Cada launch es una corrutina diferente
        launch {
            starMsg()
            println("None")
            endMsg()
        }
        //Los dispatchers nos ayuda a definir donde queremos ejecutar los hilos de las corrutinas,
        //cada uno de ellos esta diseñado para optimizar cada uno de los recursos del sistema para
        //ciertas tareas, IO es entrada y salida, este es ideal para las conexiones a base de datos,
        //locales o remotas, tambien para la escritura y lectura de archivos y cualquier tarea de larga duración
        launch(Dispatchers.IO) {
            starMsg()
            println("IO")
            endMsg()
        }
        //Este esta recomendado para procesos donde no se requiere compartir datos con otras corrutinas,
        // a su vez tambien podemos cambiar de hilo si es que se encuentra en un función suspendida, este es el menos común
        launch(Dispatchers.Unconfined) {
            starMsg()
            println("Unconfined")
            endMsg()
        }
        //Main solo para android
        //Este marcará error por que esta función esta conectada al hilo principal de la interfaz de android
        //solamente se recomienda utilizar para tareas muy rapidas o que esten relacionadas con el cambio de la interfaz
//        launch(Dispatchers.Main) {
//            starMsg()
//            println("Unconfined")
//            endMsg()
//        }
        //Evitar lo mas posible la corrutina main

        //Esta corrutina es usado para un uso intensivo de la CPU, normalmente se usa para procesos largos,
        //por ejemplo procesar una imagen, hacer calculos complejos, cualquier otra tarea de larga duración que no
        //encaje con la corrutina IO
        launch(Dispatchers.Default) {
            starMsg()
            println("Default")
            endMsg()
        }

        //Las corrutinas personalizadas se recomienda para realizar depuración
        launch(newSingleThreadContext("Cursos Android ANT")) {
            starMsg()
            println("Mi corrutina personalizada con un dispatcher")
            endMsg()
        }

        newSingleThreadContext("DVP").use {myContext ->
            launch(myContext) {
                starMsg()
                println("Mi corrutina personalizada con un dispatcher 2")
                endMsg()
            }
        }
    }
}
