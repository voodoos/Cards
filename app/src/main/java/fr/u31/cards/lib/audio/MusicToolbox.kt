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

enum class Lang {
    En, Fr
}

fun toFrench(b : BaseNote) : String {
    return when(b) {
        BaseNote.A -> "La"
        BaseNote.B -> "Si"
        BaseNote.C -> "Do"
        BaseNote.D -> "Ré"
        BaseNote.E -> "Mi"
        BaseNote.F -> "Fa"
        BaseNote.G -> "Sol"
    }
}
fun toFrench(a : Alteration) : String {
    return when(a) {
        Alteration.Sharp -> "Dièse"
        Alteration.Flat -> "Bémol"
        Alteration.None -> ""

    }
}
fun toSym(a : Alteration) : String {
    return when(a) {
        Alteration.Sharp -> "♯"
        Alteration.Flat -> "♭"
        Alteration.None -> ""

    }
}


val baseNotes = getAllValues<BaseNote>()
val alterations = getAllValues<Alteration>()

class Note (val base : BaseNote,
            val lvl : Int = 4,
            val alt : Alteration = Alteration.None
) {
    val freq : Double
    val rank : Int

    init {
        freq = Note.freqOfNote(base, lvl, alt)
        rank = Note.rankOfNote(base, lvl, alt)

    }

    fun toString(lang : Lang, withFreq : Boolean = false) : String {
        val res = when (lang) {
            Lang.En -> (base.name
                    + lvl.toString()
                    + " " + alt.name)
            Lang.Fr -> (toFrench(base)
                    + (lvl - 1).toString()
                    + " " + toFrench(alt))
        }

        if(withFreq)
            return res + " (" + freq.toString() + "hz)"
        return res


    }
    override fun toString(): String {
        return toString(Lang.En)
    }

    companion object {
        private const val freqLa4 = 440.0
        private val root2 = pow(2.0, 1.0 / 12.0)
        val standardNotes = Array<Array<Note>>(87, ::noteOfRank)
        val standardNotesFlat = standardNotes.flatten()

        /**
         * Computes the "rank" of a given note. For example :
         *   A0 -> 0
         *   A0Sharp -> 1
         *   B0Flat -> 1
         *   B0 -> 2
         *   ...
         * <p>
         * This method does not check the validity of a note.
         *
         * @param  base  the base note
         * @param  lvl   the note's octave
         * @param  alt   the note's alteration
         * @return       the rank of the note
         */
        fun rankOfNote(base : BaseNote,
                       lvl : Int = 4,
                       alt : Alteration = Alteration.None
        ) : Int {
            val baseRank = base.ordinal * 2 + (12 * lvl)

            return when (alt) {
                Alteration.Flat -> baseRank - 1
                Alteration.Sharp -> baseRank + 1
                Alteration.None -> baseRank
            }
        }

        /**
         * Get a Note by it's rank.
         * Answer can contain two notes (flat and sharp)
         *
         * @param  rank  the rank of the note
         * @return       the notes corresponding to that rank
         * @see rankOfNote
         */
        fun noteOfRank(rank : Int) : Array<Note> {
            val octave = rank / 12
            val base = rank % 12

            if(base % 2 == 0)
                return  arrayOf(Note(BaseNote.values()[base / 2], octave))
            else return arrayOf(
                Note(BaseNote.values()[(base - 1) / 2], octave, Alteration.Sharp),
                Note(BaseNote.values()[(base + 1) / 2], octave, Alteration.Flat))
        }

        /**
         * Computes the frequency of a given note.
         * <p>
         * This method does not check the validity of a note.
         *
         * @param  base  the base note
         * @param  lvl   the note's octave
         * @param  alt   the note's alteration
         * @return       the frequency of the note
         */
        fun freqOfNote(base : BaseNote,
                       lvl : Int = 4,
                       alt : Alteration = Alteration.None
        ) : Double {
            val rankLA4 = rankOfNote(BaseNote.A)
            val rankNote = rankOfNote(base, lvl, alt)

            val distance = rankNote - rankLA4


            return freqLa4 * pow(Note.root2, distance.toDouble())
        }

        /**
         * Computes the frequency of a given note.
         *
         * @param  note  the note
         * @return       the frequency of the note
         * @see freqOfNote
         */
        fun freqOfNote(note : Note) : Double {
            return freqOfNote(note.base, note.lvl, note.alt)
        }

        /**
         * Returns the nearest note (or couple of notes)
         * frequency-wise.
         * For exemple, nearestNote(442.0) -> A4 (La3)
         *
         * @param  freq  the frequency
         * @return       the nearest note(s)
         */
        fun nearestNote(freq : Double) : Pair<Array<Note>, Double> {
            fun distance(ns : Array<Note>) : Double {
                return Math.abs(ns[0].freq - freq)
            }
            var dist = Double.MAX_VALUE
            var res = standardNotes.last()

            for (ns in standardNotes) {
                val newDist = distance(ns)
                if(newDist < dist) {
                    dist = newDist
                    res = ns
                } else {
                    return Pair(res, dist)
                }
            }

            return Pair(res, dist)
        }
    }
}


/***************
 * LEGACY
 */
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
