package fr.u31.cards.lib

import android.content.Context
import android.graphics.drawable.Drawable
import fr.u31.cards.views.DiapoView

class ImageCard(text : String, val img : Drawable) : Card(text) {

    override fun getView(ctx: Context) : DiapoView {
        return DiapoView(ctx, text, img);
    }
}