/*
 * Copyright (c) 2023 Chamath Jayasena
 * Dice Clash
 * Mobile Dev CW1 (L5)
 * UoW ID - w1898955
 * IIT ID - 20211387
 */

package com.cj.diceclash


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder


// Demonstration video - https://youtu.be/Cueurw0rSVA
class MainActivity : AppCompatActivity() {

    // https://kotlinlang.org/docs/properties.html#late-initialized-properties-and-variables
    private lateinit var aboutDialog: AlertDialog

    private var computerWins = 0
    private var humanWins = 0

    /*
    Getting a result from an activity.
    https://developer.android.com/training/basics/intents/result
    https://stackoverflow.com/questions/10407159/how-to-manage-startactivityforresult-on-android
    */
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                computerWins = result.data?.getIntExtra("COMPUTER_WINS", 0) ?: 0
                humanWins = result.data?.getIntExtra("HUMAN_WINS", 0) ?: 0
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize about dialog
        aboutDialog = MaterialAlertDialogBuilder(this)
            .setView(R.layout.dialog_about)
            .setPositiveButton("Dismiss") { _, _ -> }
            .create()

        // Buttons
        val newGameBtn = findViewById<Button>(R.id.new_game_btn)
        newGameBtn.setOnClickListener { newGame() }

        val aboutBtn = findViewById<Button>(R.id.about_btn)
        aboutBtn.setOnClickListener {
            aboutDialog.show()
        }

        /*
        Check if there is a saved instance.
        Restore previous state if there is.
        */
        if (savedInstanceState != null) {
            computerWins = savedInstanceState.getInt("computerWins")
            humanWins = savedInstanceState.getInt("humanWins")

            val isAboutDialogShowing = savedInstanceState.getBoolean("isAboutDialogShowing")
            if (isAboutDialogShowing) {
                aboutDialog.show()
            }
        }
    }


    /*
    Perform any final cleanup before an activity is destroyed
    https://developer.android.com/guide/components/activities/activity-lifecycle
    */
    override fun onDestroy() {
        // Close about dialog before destroying
        if (aboutDialog.isShowing) {
            aboutDialog.dismiss()
        }
        super.onDestroy()
    }


    /*
    Invoked when the activity might be temporarily destroyed; save the instance state here.
    https://developer.android.com/guide/components/activities/activity-lifecycle
    https://developer.android.com/topic/libraries/architecture/saving-states
    */
    override fun onSaveInstanceState(outState: Bundle) {
        // Save current state
        outState.run {
            putInt("computerWins", computerWins)
            putInt("humanWins", humanWins)
            putBoolean("isAboutDialogShowing", aboutDialog.isShowing)
        }

        super.onSaveInstanceState(outState)
    }


    private fun newGame() {
        val i = Intent(this, GameActivity::class.java)
        i.putExtra("COMPUTER_WINS", computerWins)
        i.putExtra("HUMAN_WINS", humanWins)
        startForResult.launch(i)
    }

}

