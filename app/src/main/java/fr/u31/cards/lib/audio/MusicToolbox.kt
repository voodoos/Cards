package fr.u31.cards.lib.audio

import java.lang.Math.pow

/* todo : understand this :-) */
inline fun <reified T : Enum<T>> getAllValues() : Array<T> {
    return enumValues()
}

enum class BaseNote {
    A, B, C, D, E, F, G
}

enum class Alteration {
    None, Sharp, Flat
}

val baseNotes = getAllValues<BaseNote>()
val alterations = getAllValues<Alteration>()

class Note (val base : BaseNote, val alt : Alteration, val lvl : Int = 4 ) {
    private val root2 = pow(2.0, 1.0 / 12.0)
    val freq : Double

    init {
        val baseFreq = when (base) {
            /* A1, B1, C1... */
            BaseNote.A -> 55.0
            BaseNote.B -> 61.7354
            BaseNote.C -> 32.7032
            BaseNote.D -> 36.7081
            BaseNote.E -> 41.2035
            BaseNote.F -> 43.6536
            BaseNote.G -> 48.9995
        }

        val octave = baseFreq * (pow(2.0, (lvl - 1).toDouble()))

        freq =  when(alt) {
            Alteration.Flat -> octave / root2
            Alteration.Sharp -> octave * root2
            Alteration.None -> octave
        }
    }

    override fun toString(): String {
        return base.name + alt.name + lvl.toString() + " (" + freq.toString() + "hz)"
    }
}

fun toFrenchNotes (n : String) : String {
    var note = ""
    var alt = ""
    when (n[0]) {
        'a' -> note = "la"
        'b' -> note = "si"
        'c' -> note = "do"
        'd' -> note = "ré"
        'e' -> note = "mi"
        'f' -> note = "fa"
        'g' -> note = "sol"
    }
    when (n.substring(1)) {
        "is" -> alt = "♯"
        "es" -> alt = "♭"
    }

    return note + alt
}
