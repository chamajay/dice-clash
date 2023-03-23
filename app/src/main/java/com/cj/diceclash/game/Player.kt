/*
 * Copyright (c) 2023 Chamath Jayasena
 * Dice Clash
 * Mobile Dev CW1 (L5)
 * UoW ID - w1898955
 * IIT ID - 20211387
 */

package com.cj.diceclash.game


import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationSet
import android.widget.ImageView
import com.cj.diceclash.R
import com.cj.diceclash.utils.Utils.getDiceDrawable


/**
 * Represents a player in the game. Includes functionalities that all players should have,
 * such as throwing the dice and keeping track of their score.
 */
open class Player(
    private val diceImageViews: List<ImageView>,
    private val diceThrowingAnimation: AnimationSet,
    private val dice: Dice,
    private var diceValues: MutableList<Int>,
    private var rolls: Int
) {
    fun throwDice(clearSelectionAfter: Boolean = true) {
        diceValues.clear()
        diceImageViews.forEach { imageView ->
            if (imageView.background == null) {
                val diceRoll = dice.roll()
                val diceDrawable = getDiceDrawable(diceRoll)
                // Set image to blank face when rolling
                imageView.setImageResource(R.drawable.blank_die_face)
                imageView.startAnimation(diceThrowingAnimation)
                Handler(Looper.getMainLooper()).postDelayed({
                    imageView.setImageResource(diceDrawable)
                }, 500)
                imageView.tag = diceRoll
                // Reset alpha back to 1
                if (imageView.alpha == 0.1f) {
                    imageView.alpha = 1f
                }
            }
            diceValues.add(imageView.tag as Int)
        }

        ++rolls

        if (clearSelectionAfter) {
            // Clear selection after 1.4s
            Handler(Looper.getMainLooper()).postDelayed({
                clearDiceSelection()
            }, 1400)
        }
    }

    fun getScore(): Int {
        return diceValues.sum()
    }

    fun getRolls(): Int {
        return rolls
    }

    fun setRolls(rolls: Int) {
        this.rolls = rolls
    }

    fun getDiceValues(): MutableList<Int> {
        return diceValues
    }

    open fun clearDiceSelection() {
        diceImageViews.forEach { imageView ->
            if (imageView.background != null) {
                imageView.background = null
            }
        }
    }
}