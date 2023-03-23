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
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cj.diceclash.game.Dice
import com.cj.diceclash.game.ComputerPlayer
import com.cj.diceclash.game.HumanPlayer
import com.cj.diceclash.utils.Utils.getDiceDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class GameActivity : AppCompatActivity() {
    // UI components
    private var gameSettingsDialog: AlertDialog? = null
    private var winLoseDialog: AlertDialog? = null
    private lateinit var winsTextView: TextView
    private lateinit var humanScoreTextView: TextView
    private lateinit var computerScoreTextView: TextView
    private lateinit var computerDiceImageViews: List<ImageView>
    private lateinit var humanDiceImageViews: List<ImageView>
    private lateinit var throwBtn: Button
    private lateinit var scoreBtn: Button

    // Animations
    private lateinit var rotateAnim: Animation
    private lateinit var moveAnim: Animation
    private lateinit var jiggleAnim: Animation
    private lateinit var fadeOutAnim: Animation
    private lateinit var diceAnimationSet: AnimationSet

    // Dice
    private val dice = Dice(6)

    // Players
    private lateinit var humanPlayer: HumanPlayer
    private lateinit var computerPlayer: ComputerPlayer

    // Game settings
    private var winningPoints = 101
    private var computerModeIntelligent = false
    private var computerWins = 0
    private var humanWins = 0
    private var computerTotalScore = 0
    private var humanTotalScore = 0
    private var tie = false
    private var winLoseDialogLayoutId = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Initialize computer imageviews
        computerDiceImageViews = listOf<ImageView>(
            findViewById(R.id.c_die1_iv),
            findViewById(R.id.c_die2_iv),
            findViewById(R.id.c_die3_iv),
            findViewById(R.id.c_die4_iv),
            findViewById(R.id.c_die5_iv)
        )

        // Initialize human imageviews
        humanDiceImageViews = listOf<ImageView>(
            findViewById(R.id.h_die1_iv),
            findViewById(R.id.h_die2_iv),
            findViewById(R.id.h_die3_iv),
            findViewById(R.id.h_die4_iv),
            findViewById(R.id.h_die5_iv)
        )

        winsTextView = findViewById(R.id.wins_tv)

        // Initialize score textviews
        computerScoreTextView = findViewById(R.id.computer_score_tv)
        computerScoreTextView.text = computerTotalScore.toString()
        humanScoreTextView = findViewById(R.id.human_score_tv)
        humanScoreTextView.text = humanTotalScore.toString()

        // Initialize buttons
        throwBtn = findViewById(R.id.throw_btn)
        throwBtn.setOnClickListener { throwDice() }
        throwBtn.isEnabled = false  // disable until user selects game settings

        scoreBtn = findViewById(R.id.score_btn)
        scoreBtn.setOnClickListener { score() }
        scoreBtn.isEnabled = false  // disabled until first throw

        /*
        Initialize animations
        https://www.digitalocean.com/community/tutorials/android-animation-example
        */
        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate)
        moveAnim = AnimationUtils.loadAnimation(this, R.anim.move)
        jiggleAnim = AnimationUtils.loadAnimation(this, R.anim.jiggle)
        fadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        /*
        Dice rotate and move animation set
        https://developer.android.com/reference/kotlin/android/view/animation/AnimationSet
        */
        diceAnimationSet = AnimationSet(true)
        diceAnimationSet.addAnimation(rotateAnim)
        diceAnimationSet.addAnimation(moveAnim)

        /*
        Check if there is a saved instance.
        Restore previous state if there is.
        */
        if (savedInstanceState != null) {
            restorePreviousState(savedInstanceState)
        } else {  // First run
            // Initialize players
            humanPlayer = HumanPlayer(
                humanDiceImageViews,
                jiggleAnim,
                diceAnimationSet,
                dice
            )

            computerPlayer = ComputerPlayer(
                computerDiceImageViews,
                jiggleAnim,
                diceAnimationSet,
                dice
            )

            // Set total wins
            setTotalWins()

            // Show game settings dialog
            gameSettingsDialog = showGameSettingsDialog()
        }
    }


    /*
    Perform any final cleanup before an activity is destroyed
    https://developer.android.com/guide/components/activities/activity-lifecycle
    */
    override fun onDestroy() {
        // Dismiss dialogs to avoid windowleaked exceptions
        // https://stackoverflow.com/questions/11590382/android-view-windowleaked
        if (gameSettingsDialog?.isShowing == true) {
            gameSettingsDialog?.dismiss()
        }

        if (winLoseDialog?.isShowing == true) {
            winLoseDialog?.dismiss()
        }

        super.onDestroy()
    }


    /*
    Invoked when the activity might be temporarily destroyed; save the instance state here.
    https://developer.android.com/guide/components/activities/activity-lifecycle
    */
    override fun onSaveInstanceState(outState: Bundle) {
        // Save game state
        outState.run {
            putIntegerArrayList("humanDiceValues", ArrayList(humanPlayer.getDiceValues()))
            putIntegerArrayList("humanSelectedDice", ArrayList(humanPlayer.getSelectedDice()))
            putIntegerArrayList("computerDiceValues", ArrayList(computerPlayer.getDiceValues()))
            putInt("winningPoints", winningPoints)
            putBoolean("computerModeIntelligent", computerModeIntelligent)
            putInt("computerWins", computerWins)
            putInt("humanWins", humanWins)
            putInt("computerTotalScore", computerTotalScore)
            putInt("humanTotalScore", humanTotalScore)
            putBoolean("tie", tie)
            putInt("humanRolls", humanPlayer.getRolls())
            putInt("computerRolls", computerPlayer.getRolls())
            putBoolean("throwBtnEnabled", throwBtn.isEnabled)
            putBoolean("scoreBtnEnabled", scoreBtn.isEnabled)
            winLoseDialog?.isShowing?.let { putBoolean("isWinLoseDialogShowing", it) }
            putInt("winLoseDialogLayoutId", winLoseDialogLayoutId)
            gameSettingsDialog?.isShowing?.let { putBoolean("isWinningPointsDialogShowing", it) }
        }

        super.onSaveInstanceState(outState)
    }


    private fun restorePreviousState(savedInstanceState: Bundle) {
        // Restore game state
        val computerDiceValues =
            savedInstanceState.getIntegerArrayList("computerDiceValues")!!.toMutableList()
        val humanDiceValues =
            savedInstanceState.getIntegerArrayList("humanDiceValues")!!.toMutableList()
        val humanSelectedDice =
            savedInstanceState.getIntegerArrayList("humanSelectedDice")!!.toMutableList()

        winningPoints = savedInstanceState.getInt("winningPoints")
        computerModeIntelligent = savedInstanceState.getBoolean("computerModeIntelligent")
        computerWins = savedInstanceState.getInt("computerWins")
        humanWins = savedInstanceState.getInt("humanWins")
        computerTotalScore = savedInstanceState.getInt("computerTotalScore")
        humanTotalScore = savedInstanceState.getInt("humanTotalScore")
        tie = savedInstanceState.getBoolean("tie")

        val humanRolls = savedInstanceState.getInt("humanRolls")
        val computerRolls = savedInstanceState.getInt("computerRolls")
        val throwBtnEnabled = savedInstanceState.getBoolean("throwBtnEnabled")
        val scoreBtnEnabled = savedInstanceState.getBoolean("scoreBtnEnabled")
        val isWinningPointsDialogShowing =
            savedInstanceState.getBoolean("isWinningPointsDialogShowing")
        val isWinLoseDialogShowing = savedInstanceState.getBoolean("isWinLoseDialogShowing")
        winLoseDialogLayoutId = savedInstanceState.getInt("winLoseDialogLayoutId")

        // Initialize players
        humanPlayer = HumanPlayer(
            humanDiceImageViews,
            jiggleAnim,
            diceAnimationSet,
            dice,
            humanDiceValues,
            humanRolls
        )

        computerPlayer = ComputerPlayer(
            computerDiceImageViews,
            jiggleAnim,
            diceAnimationSet,
            dice,
            computerDiceValues,
            computerRolls
        )

        // Restore win results
        setResults()

        // Restore scores
        computerScoreTextView.text = computerTotalScore.toString()
        humanScoreTextView.text = humanTotalScore.toString()

        // Restore button state
        throwBtn.isEnabled = throwBtnEnabled
        scoreBtn.isEnabled = scoreBtnEnabled

        // Restore Dialogs
        if (isWinningPointsDialogShowing) {
            gameSettingsDialog = showGameSettingsDialog()
        }

        if (isWinLoseDialogShowing) {
            winLoseDialog = getWinLoseDialog(winLoseDialogLayoutId)
            winLoseDialog?.show()
        }

        // Restore dice images
        if (humanPlayer.getRolls() != 0) {
            humanDiceImageViews.forEachIndexed { index, imageView ->
                imageView.setImageResource(getDiceDrawable(humanDiceValues[index]))
                imageView.tag = humanDiceValues[index]
                // Check if the user had selected this dice before
                if (imageView.id in humanSelectedDice) {
                    humanPlayer.toggleDiceSelection(imageView, false)
                }
            }
            computerDiceImageViews.forEachIndexed { index, imageView ->
                imageView.setImageResource(getDiceDrawable(computerDiceValues[index]))
                imageView.tag = computerDiceValues[index]
            }

            if (!tie) {
                humanPlayer.enableDiceSelection()
            }

        } else {
            // Attempt is over and not the initial start
            if (humanTotalScore > 0 || computerTotalScore > 0) {
                humanDiceImageViews.forEachIndexed { index, imageView ->
                    imageView.alpha = 0.1f
                    imageView.setImageResource(getDiceDrawable(humanDiceValues[index]))
                    imageView.tag = humanDiceValues[index]
                }
                computerDiceImageViews.forEachIndexed { index, imageView ->
                    imageView.alpha = 0.1f
                    imageView.setImageResource(getDiceDrawable(computerDiceValues[index]))
                    imageView.tag = computerDiceValues[index]
                }
            }
        }
    }


    private fun setResults() {
        // Update wins textview
        val winsTxt = "H:$humanWins/C:$computerWins"
        winsTextView.text = winsTxt
        // https://stackoverflow.com/questions/10407159/how-to-manage-startactivityforresult-on-android
        val i = Intent()
        i.putExtra("COMPUTER_WINS", computerWins)
        i.putExtra("HUMAN_WINS", humanWins)
        setResult(Activity.RESULT_OK, i)
    }


    private fun setTotalWins() {
        // Get the intent
        val intent = intent
        // Get wins from the intent
        computerWins = intent.getIntExtra("COMPUTER_WINS", 0)
        humanWins = intent.getIntExtra("HUMAN_WINS", 0)
        val winsTxt = "H:$humanWins/C:$computerWins"
        // Update wins textview
        winsTextView.text = winsTxt
    }


    /*
    Create and show the game settings dialog.
    https://developer.android.com/reference/com/google/android/material/dialog/MaterialAlertDialogBuilder
    https://m2.material.io/components/dialogs/android#using-dialogs
    */
    private fun showGameSettingsDialog(): AlertDialog {
        val dialogBuilder = MaterialAlertDialogBuilder(this)
        dialogBuilder.setView(R.layout.dialog_game_settings)

        // Create and show the dialog
        val dialog = dialogBuilder.create()
        dialog.show()

        // Get dialog views after showing the dialog.
        // Can't get dialog views before showing the dialog.
        val letsPlayBtn = dialog.findViewById<Button>(R.id.lets_play_btn)
        val pointsText = dialog.findViewById<EditText>(R.id.winning_points_et)
        val computerModeToggle = dialog.findViewById<ToggleButton>(R.id.computer_mode_toggle)

        letsPlayBtn?.setOnClickListener {
            dialog.dismiss()
        }

        // On Dismiss
        dialog.setOnDismissListener {
            // Set winning points
            val winningPointsTxt = pointsText?.text
            if (!winningPointsTxt.isNullOrEmpty()) {
                winningPoints = winningPointsTxt.toString().toInt()
            }
            // Set computer mode
            computerModeIntelligent = computerModeToggle?.isChecked == true
            throwBtn.isEnabled = true
        }

        return dialog
    }


    private fun throwDice() {
        // Check if it is a tie
        if (tie) {
            // Throw randomly
            computerPlayer.throwDice(false)
            humanPlayer.throwDice(false)
            /*
            Schedule the function to be executed on the main thread after 1.2 seconds.
            https://developer.android.com/reference/android/os/Handler.html#postDelayed(java.lang.Runnable,%20long)
            https://stackoverflow.com/questions/49541871/whats-the-postdelayed-uses-in-kotlin
            */
            Handler(Looper.getMainLooper()).postDelayed({
                attemptOver()
            }, 1200)
        } else {
            // Enable score button
            scoreBtn.isEnabled = true

            // Make human dice selectable
            humanPlayer.enableDiceSelection()

            // Human throw.
            // Don't clear selection if it's the first roll.
            // Throw the computer dice too if it's the first roll.
            if (humanPlayer.getRolls() == 0) {
                humanPlayer.throwDice(false)
                computerPlayer.throwDice(false)
            } else {
                // Don't throw if the user has selected all 5 dice
                if (humanPlayer.getSelectedDice().size != 5) {
                    humanPlayer.throwDice()
                }
            }

            // If the human player has used all the 3 throws
            if (humanPlayer.getRolls() >= 3) {
                // Disable buttons
                throwBtn.isEnabled = false
                scoreBtn.isEnabled = false
                // Score automatically after 2.5s (after animations)
                Handler(Looper.getMainLooper()).postDelayed({
                    score()
                }, 2500)
            }
        }
    }


    private fun score() {
        // Disable buttons
        throwBtn.isEnabled = false
        scoreBtn.isEnabled = false

        // Use the remaining rerolls of the computer player according to the selected strategy.
        if (computerModeIntelligent) {
            // Throw efficiently
            computerPlayer.efficientlyThrowDice(
                humanTotalScore,
                computerTotalScore
            ) { attemptOver() }
        } else {
            // Throw randomly
            computerPlayer.randomlyThrowDice { attemptOver() }
        }
    }


    private fun attemptOver() {
        // Update computer scores
        computerTotalScore += computerPlayer.getScore()
        computerPlayer.clearDiceSelection()

        // Update human scores
        humanTotalScore += humanPlayer.getScore()
        humanPlayer.clearDiceSelection()

        // Update score textviews
        computerScoreTextView.text = computerTotalScore.toString()
        humanScoreTextView.text = humanTotalScore.toString()

        // Reset rolls
        computerPlayer.setRolls(0)
        humanPlayer.setRolls(0)

        // Make human dice not clickable
        humanPlayer.disableDiceSelection()

        // Fade out dice imageviews
        computerDiceImageViews.forEach { imageView ->
            imageView.startAnimation(fadeOutAnim)
        }
        humanDiceImageViews.forEach { imageView ->
            imageView.startAnimation(fadeOutAnim)
        }

        // Determine the winner or tie after 0.9s
        Handler(Looper.getMainLooper()).postDelayed({
            if (humanTotalScore >= winningPoints || computerTotalScore >= winningPoints) {
                // Disable throw button
                throwBtn.isEnabled = false

                if (humanTotalScore == computerTotalScore) {  // Tie
                    tie = true
                    throwBtn.isEnabled = true
                    humanPlayer.disableDiceSelection()  // Disable human dice selection if a tie
                } else if (humanTotalScore > computerTotalScore) {  // Human wins
                    ++humanWins
                    setResults()
                    winLoseDialogLayoutId = R.layout.dialog_win
                    winLoseDialog = getWinLoseDialog(winLoseDialogLayoutId)
                    winLoseDialog?.show()
                } else {  // Computer wins
                    ++computerWins
                    setResults()
                    winLoseDialogLayoutId = R.layout.dialog_lose
                    winLoseDialog = getWinLoseDialog(winLoseDialogLayoutId)
                    winLoseDialog?.show()
                }
            } else {
                throwBtn.isEnabled = true
            }
        }, 900)
    }


    private fun getWinLoseDialog(layout: Int): AlertDialog {
        return MaterialAlertDialogBuilder(this)
            .setView(layoutInflater.inflate(layout, null))
            .create()
    }

}
