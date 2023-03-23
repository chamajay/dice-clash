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
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.widget.ImageView
import com.cj.diceclash.R
import kotlin.random.Random


/**
 * Represents a computer player in the game. Inherits from the Player class and adds
 * functionality specific to the computer player, such as decision making logic.
 */
class ComputerPlayer(
    private val diceImageViews: List<ImageView>,
    private val diceSelectionAnimation: Animation,
    diceThrowingAnimation: AnimationSet,
    dice: Dice,
    diceValues: MutableList<Int> = mutableListOf(),
    rolls: Int = 0
) : Player(diceImageViews, diceThrowingAnimation, dice, diceValues, rolls) {

    fun randomlyThrowDice(onFinished: () -> Unit) {
        if (!shouldReroll() || super.getRolls() >= 3) {
            onFinished()
            return
        }

        randomlySelectDiceToKeep()

        // Wait for animations to finish before throwing
        Handler(Looper.getMainLooper()).postDelayed({
            super.throwDice(true)
            // Recursive call after 2.4s
            Handler(Looper.getMainLooper()).postDelayed({
                randomlyThrowDice(onFinished)
            }, 2400)
        }, 800)
    }

    private fun randomlySelectDiceToKeep() {
        // Randomly select how many dice to keep
        val numDiceToKeep = (0..4).random()
        val diceToKeep = mutableListOf<Int>()
        var selectedDice = 0

        // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/repeat.html
        repeat(numDiceToKeep) {
            // Select the die to keep randomly
            val randomDie = super.getDiceValues().random()
            // Check if the random dice has selected before
            if (randomDie !in diceToKeep) {
                diceToKeep.add(randomDie)
                diceImageViews.forEach { imageView ->
                    if (imageView.tag == randomDie && selectedDice != 4) {
                        toggleDiceSelection(imageView)
                        ++selectedDice
                    }
                }
            }
        }
    }


    /*
    The following functions take into account the score difference of the players and the computer
    player's current throw's sum to make strategic decisions about whether to reroll, and which dice
    to keep and which to reroll.

    - efficientlyThrowDice(humanScore: Int, computerScore: Int, onFinished: () -> Unit)
        Takes the human total score, computer total score and a function that'll be called after
        the attempt. Checks the score difference between the two players and decides how
        many rerolls it's going to take. If the difference is <= 0, reroll once using the efficient
        method. And if the difference is >= 1, meaning the human player is ahead, use the both
        rerolls using the efficient method to get a higher sum throw.

    - efficientlyGetNumDiceToRoll(currentThrowScore: Int): Int
        Takes the computer player's current throw's score. Decides how many dice to roll for the
        next throw to get a better throw. If the score is in inclusive range between 5 and 10,
        it will reroll 4 dice, if it's in inclusive range between 11 and 15, it will reroll 3 dice,
        and if it's in inclusive range between 16 and 20, it will reroll 2 dice.
        If the score is between 21 and 25, the computer player will only reroll one die.
        If the score is above 25, no dice will be rerolled as it is already a good throw.

        Returns the number of dice to reroll.

    - efficientlySelectDiceToKeep(numDiceToReroll: Int)
        Takes the number of dice to reroll decided by the efficientlyGetNumDiceToRoll function.
        Sorts the current throw's dice in ascending order and selects only the dice with the highest
        values to keep, so a given number of lower value dice will be rerolled.

    - efficientlyDelayAndThrowDice()
        Selects which dice to keep using below efficientlySelectDiceToKeep() function.
        Throws the non-selected dice, and recursively calls the efficientlyThrowDice() function.

    E.g. - Assume both the players have the same total score, and the computer player's current
    throw's sum is 15, given by dice with values 5, 4, 2, 1, 3. In this case since the score
    difference is 0, the computer decides to reroll once efficiently, and since current throw's sum
    is 15, it's going to reroll 3 dice with lower values, keeping 2 highest value dice. After
    sorting, 1, 2, 3, 4, 5. So the dice with lower values, 1, 2, 3 will be rerolled, while dice
    with values 4, 5 will be kept.

    Advantages:
        - Makes tactical adjustments based on the score difference between the two players, which
        leads to a more dynamic and interesting game.
        - Takes into account the current score of the computer player and adjusts the number of dice
        to be rerolled accordingly, which leads to more human-like strategic play.

    Disadvantages:
        - May not be the most optimal strategy in all situations, as it is based on fixed rules
        rather than adapting to the specific circumstances of each game.
        - Can become predictable to the human player after a while, which could make the game less
        interesting over time.
    */
    fun efficientlyThrowDice(
        humanScore: Int,
        computerScore: Int,
        onFinished: () -> Unit
    ) {
        val scoreDiff = humanScore - computerScore
        val currentThrowScore = super.getScore()

        // Don't reroll if 3 rolls were used
        if (super.getRolls() >= 3) {
            onFinished()
            return
        }

        when (scoreDiff) {
            in Int.MIN_VALUE..0 -> {  // Diff <= 0
                val numDiceToThrow = efficientlyGetNumDiceToRoll(currentThrowScore)
                super.setRolls(2)  // Manually increase rolls so only rerolls once
                efficientlyDelayAndThrowDice(numDiceToThrow, humanScore, computerScore, onFinished)
            }
            in 1..Int.MAX_VALUE -> {  // Diff >= 1
                val numDiceToThrow = efficientlyGetNumDiceToRoll(currentThrowScore)
                efficientlyDelayAndThrowDice(numDiceToThrow, humanScore, computerScore, onFinished)
            }
        }
    }

    private fun efficientlyGetNumDiceToRoll(currentThrowScore: Int): Int {
        // https://www.programiz.com/kotlin-programming/when-expression
        return when (currentThrowScore) {
            in 5..10 -> 4
            in 11..15 -> 3
            in 16..20 -> 2
            in 21..25 -> 1
            else -> 0  // When score 26 - 30 don't reroll
        }
    }

    private fun efficientlySelectDiceToKeep(numDiceToReroll: Int) {
        val sortedDiceValues = super.getDiceValues().sorted()
        val diceToKeep =
            sortedDiceValues.subList(numDiceToReroll, sortedDiceValues.size).toMutableList()
        var numSelectedDice = 0

        diceImageViews.forEach { imageView ->
            val value = imageView.tag
            if (value in diceToKeep && (numSelectedDice < 5 - numDiceToReroll)) {
                toggleDiceSelection(imageView)
                ++numSelectedDice
                diceToKeep.remove(value)  // Remove the selected value from the list
            }
        }
    }

    private fun efficientlyDelayAndThrowDice(
        numDiceToThrow: Int,
        humanScore: Int,
        computerScore: Int,
        onFinished: () -> Unit
    ) {
        // Don't reroll if the current throw is good
        if (numDiceToThrow == 0) {
            onFinished()
            return
        }

        efficientlySelectDiceToKeep(numDiceToThrow)

        Handler(Looper.getMainLooper()).postDelayed({
            super.throwDice(true)
            // recursive call after 2.4s
            Handler(Looper.getMainLooper()).postDelayed({
                efficientlyThrowDice(humanScore, computerScore, onFinished)
            }, 2400)
        }, 800)
    }


    private fun shouldReroll(): Boolean {
        // Randomly decide whether to reroll or not
        // https://developermemos.com/posts/random-boolean-kotlin
        return Random.nextBoolean()
    }


    private fun toggleDiceSelection(imageView: ImageView) {
        val dieBorderResId = R.drawable.computer_die_border
        // If no background set background, else remove background (toggle)
        if (imageView.background == null) {
            imageView.startAnimation(diceSelectionAnimation)
            imageView.setBackgroundResource(dieBorderResId)
        } else {
            imageView.startAnimation(diceSelectionAnimation)
            imageView.background = null
        }
    }

}