package fr.u31.cards.lib

import android.content.Context
import fr.u31.cards.views.DiapoView

/* open allows Card to be inhereted */
open class Card (val text: String) {

    open fun getView(ctx : Context): DiapoView {
        return DiapoView(ctx, text)
    }

    override fun toString(): String {
        return text
    }
}