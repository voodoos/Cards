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


fun <T> toStr(tab: Array<T>) : String {
    val s = tab.fold ("") { acc, elt ->
        if (acc == "") acc + elt.toString()
        else acc + ", " + elt.toString()
    }
    return "[$s]"
}

fun <T> toStr(tab: Iterable<T>) : String {
    val s = tab.fold ("") { acc, elt ->
        if (acc == "") acc + elt.toString()
        else acc + ", " + elt.toString()
    }
    return "[$s]"
}

fun toStrA(s : Any?) : String {
    return if(s == null) "null"
    else toStr(s)
}

fun toStr(s : Any) : String {
    return s.toString()
}

fun debug(s : Any?) {
    println("cards-debug: " + toStrA(s))
}

fun debug(label : String, s : Any?) {
    println("cards-debug: " + label + ": "+ toStrA(s))
}
