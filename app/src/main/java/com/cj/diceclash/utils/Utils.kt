/*
 * Copyright (c) 2023 Chamath Jayasena
 * Dice Clash
 * Mobile Dev CW1 (L5)
 * UoW ID - w1898955
 * IIT ID - 20211387
 */

package com.cj.diceclash.utils


import com.cj.diceclash.R


// A utility class that provides helper methods and functions
// that can be used throughout the application.
// https://medium.com/swlh/singleton-class-in-kotlin-c3398e7fd76b
object Utils {
    fun getDiceDrawable(side: Int): Int {
        // https://www.programiz.com/kotlin-programming/when-expression
        return when (side) {
            1 -> R.drawable.die_face_1
            2 -> R.drawable.die_face_2
            3 -> R.drawable.die_face_3
            4 -> R.drawable.die_face_4
            5 -> R.drawable.die_face_5
            else -> R.drawable.die_face_6
        }
    }
}