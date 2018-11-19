package fr.u31.cards.lib

import java.util.*

fun IntRange.random() =
    Random().nextInt((endInclusive + 1) - start) +  start

fun <T> Collection<T>.deepToString() : String{
    val s = this.fold ("") { acc, elt ->
        if (acc == "") acc + elt.toString()
        else acc + ", " + elt.toString()
    }
    return ("[$s]")
}