package fr.u31.cards.lib
import java.util.Random

import android.content.Context
import android.support.v7.widget.CardView

class Cards (val cards: List<Card>) {

    fun getRandom() : Card {
        val randomIndex = (0 until cards.size).random()
        return (cards[randomIndex])
    }

    override fun toString(): String {
        val iter = cards.listIterator()
        var res = "[";

        while (iter.hasNext())
            res += iter.next().toString() + ";"

        return res + "]"
    }
}