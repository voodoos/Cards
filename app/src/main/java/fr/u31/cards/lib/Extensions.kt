package fr.u31.cards.lib

import java.util.*

fun IntRange.random() =
    Random().nextInt((endInclusive + 1) - start) +  start