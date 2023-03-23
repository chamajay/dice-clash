/*
 * Copyright (c) 2023 Chamath Jayasena
 * Dice Clash
 * Mobile Dev CW1 (L5)
 * UoW ID - w1898955
 * IIT ID - 20211387
 */

package com.cj.diceclash.game


/**
 * Represents a dice in the game that can be rolled to generate a random number
 * between inclusive range 1 - sides.
 */
class Dice (private val sides: Int) {
    fun roll(): Int {
        // https://www.baeldung.com/kotlin/ranges
        return (1..sides).random()
    }
}