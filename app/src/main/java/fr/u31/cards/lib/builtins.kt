package fr.u31.cards.lib

import android.content.Context
import fr.u31.cards.lib.audio.toFrenchNotes


val notes = listOf(
    "a", "b", "c", "d", "e", "f", "g"
)

val all_notes = notes.flatMap { n -> listOf(n, n + "is", n + "es") }

fun make_cards (ctx : Context) : Cards {
    return Cards(all_notes.map { s ->
        val name = "note_" + s + "_svg"
        val id = ctx.resources.getIdentifier(name, "drawable", "fr.u31.cards")
        ImageCard(toFrenchNotes(s), ctx.getDrawable(id))
    })
}