package fr.u31.cards.lib

fun <T> debug(tab: Array<T>) {
    debug(tab.asList())
}

fun <T> debug(tab: Iterable<T>) {
    val s = tab.fold ("") { acc, elt ->
        if (acc == "") acc + elt.toString()
        else acc + ", " + elt.toString()
    }
    debug("[$s]")
}

fun debug(s : Any) {
    println("cards-debug: " + s.toString())
}
