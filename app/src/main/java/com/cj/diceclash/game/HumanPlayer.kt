/*
 * Copyright (c) 2023 Chamath Jayasena
 * Dice Clash
 * Mobile Dev CW1 (L5)
 * UoW ID - w1898955
 * IIT ID - 20211387
 */

package com.cj.diceclash.game


import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.widget.ImageView
import com.cj.diceclash.R


/**
 * Represents a human player in the game. Inherits from the Player class and adds
 * functionality specific to the human player.
 */
class HumanPlayer(
    private val diceImageViews: List<ImageView>,
    private val diceSelectionAnimation: Animation,
    diceThrowingAnimation: AnimationSet,
    dice: Dice,
    diceValues: MutableList<Int> = mutableListOf(),
    rolls: Int = 0
) : Player(diceImageViews, diceThrowingAnimation, dice, diceValues, rolls) {

    private val selectedDice: MutableList<Int> = mutableListOf()

    fun enableDiceSelection() {
        // Make human dice clickable
        diceImageViews.forEach { imageView ->
            imageView.setOnClickListener { toggleDiceSelection(imageView) }
        }
    }

    fun disableDiceSelection() {
        // Make human dice non-clickable
        diceImageViews.forEach { imageView ->
            imageView.setOnClickListener(null)
        }
    }

    fun getSelectedDice(): MutableList<Int> {
        return selectedDice
    }

    fun toggleDiceSelection(imageView: ImageView, animate: Boolean = true) {
        val dieBorderResId = R.drawable.human_die_border
        // If no background, set background, else remove background (toggle)
        if (imageView.background == null) {
            if (animate) {
                imageView.startAnimation(diceSelectionAnimation)
            }
            imageView.setBackgroundResource(dieBorderResId)
            selectedDice.add(imageView.id)
        } else {
            imageView.startAnimation(diceSelectionAnimation)
            imageView.background = null
            selectedDice.remove(imageView.id)
        }
    }

    override fun clearDiceSelection() {
        super.clearDiceSelection()
        selectedDice.clear()
    }
}