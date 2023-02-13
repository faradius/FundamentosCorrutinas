package com.example.fundamentoscorrutinas

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce

fun main(){
    //globalScope()
    //suspendFun()
    newTopic("Constructores de corrutinas")
    //cRunBlocking()
    //cLaunch()
    //cAsync()
    //job()
    //deferred()
    cProduce()
    //Hace que espere un caracter como un scaner
    readLine()
}

//Este tema forma parte de un patron llamado productor-consumidor y tiene que ver con los canales,
//funciona similar como referred, el objetivo es producir una secuencia de valores que posteriormente seran consumidos
//por otra parte de nuestro programa
fun cProduce() = runBlocking{
    newTopic("Produce")
    val names = produceNames()
    names.consumeEach { println(it) }
}

fun CoroutineScope.produceNames(): ReceiveChannel<String> = produce {
    (1..5).forEach{send("name$it")}
}

fun deferred() {
    runBlocking {
        newTopic("Deferred")
        //El deferred viene heredando de job
        val deferred = async {
            starMsg()
            delay(someTime())
            println("Deferred...")
            endMsg()
            multi(5,2)
        }

        println("Deferred: $deferred")
        println("Valor del Deferred.await: ${deferred.await()}")

        //Esto es otra forma con el await pero mas directa, await es una función suspendida,
        //por lo que el await sera ejecutado hasta que esta tenga un valor, ademas devolverá
        //siempre el ultimo dato que se encuentre en el constructor de la corrutina
        val result = async {
            multi(3,3)
        }.await()
        println("Result: $result")
    }
}

fun job() {
    runBlocking {
    //Un job (tiene un ciclo de vida) es una corrutina que realiza un trabajo en segundo plano y puede ser cancelable
        newTopic("Job")
        //launch es un constructor
        val job = launch {
            starMsg()
            delay(2_100)
            println("Job...")
            endMsg()
        }
        println("Job: $job")

        //delay(4_000)
        println("isActive: ${job.isActive}")
        println("isCancelled: ${job.isCancelled}")
        println("isCompleted: ${job.isCompleted}")

        delay(someTime())
        println("Tarea cancelada o interrumpida")
        job.cancel()

        println("isActive: ${job.isActive}")
        println("isCancelled: ${job.isCancelled}")
        println("isCompleted: ${job.isCompleted}")
    }
}

fun cAsync() {
    //esto se va ejecutar desde otra corrutina o una suspend function
    runBlocking {
        newTopic("Async")
        //Este si nos va a devolver un valor hasta que finalice la corrutina, este resultado va hacer devuelto
        //y este seria el ultimo valor que esta declarado dentro de la corrutina
        val result = async {
            starMsg()
            delay(someTime())
            println("async...")
            endMsg()
            1
        }
        println("Result: ${result.await()}")
    }


}

fun cLaunch() {
    //esto se va ejecutar desde otra corrutina o una suspend function
    runBlocking {
        newTopic("Launch")
        //se utiliza launch dentro de una corrutina ya que seria la ejecución del constructor de una corrutina
        //se ejecuta en el hilo principal, esta totalmente diseñado para hacer tareas que no necesitan devolver
        //un valor o un resultado, podemos enfocar este constructor para aquellas tareas donde necesitamos detonar
        //una segunda tarea pero no estamos a la espera de un resultado
        launch {
            starMsg()
            delay(someTime())
            println("launch...")
            endMsg()
        }
    }
}

//Este bloque de codigo que va a suspender el hilo hasta que este termine, runBlocking es una
//corrutina que es usado para pruebas
fun cRunBlocking() {
    newTopic("RunBlocking")
    runBlocking {
        starMsg()
        delay(someTime())
        println("runBlocking...")
        endMsg()
    }
}

fun suspendFun() {
    newTopic("Suspend")
    Thread.sleep(someTime())
    //delay(someTime())
    GlobalScope.launch { delay(someTime()) }
}

fun globalScope() {
    newTopic("Global Scope")
    //Global scope nos permite que cada corrutina este en ejecución mientras la aplicación este viva,
    //es decir hasta que no terminemos el proceso, el launch singnifica que crearemos una corrutina de tipo
    //job un objeto cancelable, esta corrutina es el mas simple y mas general pero
    // no es recomendable por que consume muchos recursos

    GlobalScope.launch {
        starMsg()
        delay(someTime())
        println("Mi corrutina")
        endMsg()
    }
}

fun starMsg() {
    println("Comenzando Corrutina -${Thread.currentThread().name}-")
}

fun endMsg() {
    println("Corrutina -${Thread.currentThread().name}- Finalizada")
}
