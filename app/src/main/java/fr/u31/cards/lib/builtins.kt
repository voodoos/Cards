package fr.u31.cards.lib

import android.content.Context
import android.support.v4.content.ContextCompat
import fr.u31.cards.R

val french_notes = listOf<String>(
    "do", "ré", "mi", "fa", "sol", "la", "si",
    "do♯", "ré♯", "mi♯", "fa♯", "sol♯", "la♯", "si♯",
    "do♭", "ré♭", "mi♭", "fa♭", "sol♭", "la♭", "si♭"
)

val notes = listOf<String>(
    "a", "b", "c", "d", "e", "f", "g"
)

val all_notes = notes.flatMap { n -> listOf<String>(n, n + "is", n + "es") }

fun cards (ctx : Context) {
    Cards(all_notes.map { s ->
        val name = "note_" + s + "_svg.xml"
        val id = ctx.getResources().getIdentifier(name, "drawable", "fr.u31.card");
        ImageCard(s, ctx.getDrawable(id))
    })
}