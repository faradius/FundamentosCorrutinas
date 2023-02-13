package com.example.fundamentoscorrutinas

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main(){
    globalScope()

    //Hace que espere un caracter como un scaner
    readLine()
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
