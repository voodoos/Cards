package fr.u31.cards.lib

import android.content.Context
import android.support.v4.content.ContextCompat
import fr.u31.cards.R

fun to_french_notes (n : String) : String {
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
val notes = listOf<String>(
    "a", "b", "c", "d", "e", "f", "g"
)

val all_notes = notes.flatMap { n -> listOf(n, n + "is", n + "es") }

fun make_cards (ctx : Context) : Cards {
    return Cards(all_notes.map { s ->
        val name = "note_" + s + "_svg"
        val id = ctx.resources.getIdentifier(name, "drawable", "fr.u31.cards")
        ImageCard(to_french_notes(s), ctx.getDrawable(id))
    })
}