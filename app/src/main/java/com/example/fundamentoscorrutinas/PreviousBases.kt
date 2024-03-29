package com.example.fundamentoscorrutinas

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.random.Random

fun main(){
    //lambda()
    //threads()
    //coroutinesVsThreads()
    sequences()
}

fun sequences() {
    newTopic("Sequences")
    getDatabySeq().forEach { println("${it}°") }
}

//Esto es la contraparte de flow
fun getDatabySeq(): Sequence<Float> {
    //sequence es un contenedor, es una colección que se enfoca en procesar y entregar valores
    //por pasos, tiene la caracteristica de ejecutar el procesamiento por cada elemento y de catalogarse
    //como perezosa, es decir, que procesará cada elemento hasta que se solicite y no toda la colección
    //en una sola acción, podemos notar tambien que para devolver un dato utilizamos un metodo especifico "yield"
    //este es el que se encarga de producir un valor final al consumidor, en este caso el foreach que esta arriba
    //getDatabySeq().forEach { println("${it}°") }
    //gracias a eso es que podemos procesar la información una vez que ya esta lista
    return sequence {
        (1..5).forEach{
            println("procesando datos...")
            Thread.sleep(someTime())
            yield(20 + it + Random.nextFloat())
        }
    }
}

fun coroutinesVsThreads() {
    newTopic("Corrutinas vs Threads")
    runBlocking {
        (1..1_000_000).forEach{
            launch {
                delay(someTime())
                print("*")
            }
        }
    }
//    (1..1_000_000).forEach{
//        thread{
//            Thread.sleep(someTime())
//            print("*")
//        }
//    }
}
private const val SEPARATOR = "===================="
fun newTopic(topic: String) {
    println("\n$SEPARATOR $topic $SEPARATOR\n")
}


fun threads() {
    newTopic("Threads")
    println("Thread ${multiThread(2,3)}")
    multiThreadLambda(2,3){
        println("Thread + Lambda $it")
    }
}

//Hilo sincrono - principal
fun multiThread(x: Int, y: Int): Int {
    var result = 0
    thread {
        Thread.sleep(someTime())
        result = x * y
    }

    Thread.sleep(2_100)
    return result
}
//Hilo asincrono - multi tareas
fun multiThreadLambda(x: Int, y: Int, callback: (result: Int) -> Unit){
    var result = 0
    thread {
        Thread.sleep(someTime())
        result = x * y
        callback(result)
    }

}

fun someTime(): Long {
    return Random.nextLong(500, 2_000)
}

fun lambda() {
    newTopic("Lambda")
    println(multi(2,3))

    multiLambda(2,3) {result ->
        println(result)
    }
}

fun multiLambda(x: Int, y: Int, callback: (result: Int) -> Unit) {
    callback(x*y)
}

fun multi(x: Int, y: Int): Int {
    return x * y
}
