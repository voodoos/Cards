package fr.u31.cards.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import fr.u31.cards.R

import kotlinx.android.synthetic.main.diapo_view.view.*

class DiapoView(val ctx : Context)
    : LinearLayout(ctx) {


    /* init executed as a primary constructor */
    init {
        View.inflate(ctx, R.layout.diapo_view, this)
    }


    /* Constructor (unused) for this to work as a "custom view" */
    constructor(ctx : Context, attrs : AttributeSet) : this(ctx) {

        /* We extract from `attrs` the attributes we are looking for (the ones in R.styleable.DiapoView) */
        val attributes = ctx.obtainStyledAttributes(attrs, R.styleable.DiapoView)

        diapoImage.setImageDrawable(attributes.getDrawable(R.styleable.DiapoView_image))
        diapoName.setText(attributes.getText(R.styleable.DiapoView_text))

        attributes.recycle()
    }

    constructor(ctx : Context, text : String) : this(ctx) {
        diapoName.setText(text)
    }

    constructor(ctx : Context, text : String, draw : Drawable) : this(ctx) {
        diapoName.setText(text)
        diapoImage.setImageDrawable(draw)
    }
}