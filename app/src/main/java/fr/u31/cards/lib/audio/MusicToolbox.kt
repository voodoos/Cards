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
fun toEnglish(a : Alteration) : String {
    return when(a) {
        Alteration.Sharp -> "Sharp"
        Alteration.Flat -> "Flat"
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

    constructor(base : BaseNote, alt : Alteration) : this(base, 4, alt)

    override fun equals(other: Any?): Boolean {
        if(other is Note)
            if(other.base == base)
                if(other.alt == alt)
                    if(other.lvl == lvl)
                        return true
        return false
    }

    fun isHarmonicOf(n : Note) : Boolean {
        return n.base == base && n.alt == alt
    }

    fun toString(lang : Lang, withFreq : Boolean = true) : String {
        val res = when (lang) {
            Lang.En -> (base.name
                    + lvl.toString()
                    + " " + toEnglish(alt))
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
        private const val nbStandardNotes = 89
        private val root2 = pow(2.0, 1.0 / 12.0)
        val pianoKeys = Array(nbStandardNotes - 1, ::noteOfRank)
        val pianoKeysFlat = pianoKeys.flatten()

        /**
         * Computes the "rank" of a given note. For example :
         *   A0 -> 0
         *   A0Sharp -> 1
         *   B0Flat -> 1
         *   B0 -> 2
         *   C1 -> 3
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
            var lvl = lvl
            if(base.ordinal >= 2) lvl--

            val baseRank = when (base) {
                BaseNote.A -> 0
                BaseNote.B -> 2
                BaseNote.C -> 3
                BaseNote.D -> 5
                BaseNote.E -> 7
                BaseNote.F -> 8
                BaseNote.G -> 10
            } + (12 * lvl)

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
            var octave = rank / 12
            val base = rank % 12

            if(base >= 3) octave++

            // todo: is there a better way ?
            return when (base) {
                /* A */
                0 -> arrayOf(Note(BaseNote.A, octave))
                1 -> arrayOf(
                    Note(BaseNote.A, octave, Alteration.Sharp),
                    Note(BaseNote.B, octave, Alteration.Flat)
                )
                /* B */
                2 -> arrayOf(Note(BaseNote.B, octave, Alteration.None))
                /* C */
                3 -> arrayOf(Note(BaseNote.C, octave, Alteration.None))
                /* D */
                4 -> arrayOf(
                    Note(BaseNote.C, octave, Alteration.Sharp),
                    Note(BaseNote.D, octave, Alteration.Flat)
                )
                5 -> arrayOf(Note(BaseNote.D, octave, Alteration.None))
                /* E */
                6 -> arrayOf(
                    Note(BaseNote.D, octave, Alteration.Sharp),
                    Note(BaseNote.E, octave, Alteration.Flat)
                )
                7 -> arrayOf(Note(BaseNote.E, octave, Alteration.None))
                /* F */
                8 -> arrayOf(Note(BaseNote.F, octave, Alteration.None))
                9 -> arrayOf(
                    Note(BaseNote.F, octave, Alteration.Sharp),
                    Note(BaseNote.G, octave, Alteration.Flat)
                )
                /* G */
                10 -> arrayOf(Note(BaseNote.G, octave, Alteration.None))
                11 -> arrayOf(
                    Note(BaseNote.G, octave, Alteration.Sharp),
                    Note(BaseNote.A, octave, Alteration.Flat)
                )
                else -> throw IllegalArgumentException("Base not should range from 0 and 11, found $base.")
            }
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
            var res = this.pianoKeys.last()

            for (ns in this.pianoKeys) {
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

/*
    Related class extensions
 */

fun Array<Note>.countNote(note : Note) : Int {
    return this.count { n -> n.isHarmonicOf(note) }
}

fun Array<Array<Note>>.countNote2(note : Note) : Int {
    return this.count { n -> n.countNote(note) > 0 }
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
