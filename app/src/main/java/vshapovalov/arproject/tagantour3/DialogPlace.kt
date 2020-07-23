package vshapovalov.arproject.tagantour3

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD
import android.os.Build
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.text.HtmlCompat
import kotlin.random.Random

class DialogPlace(c : Context, m: String) {
    var act: MainActivity? = null
    var cont: Context = c
    var infoDialog: Dialog? = null
    var quizDialog: Dialog? = null
    var place : Place? = null
    var mode = m // "real" or "mock"
    var quizScore = 0

    fun setActivity(activity: MainActivity) {
        act = activity
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    fun openInfo(p: Place) {
        place = p
        infoDialog = Dialog(cont)
        infoDialog!! .requestWindowFeature(Window.FEATURE_NO_TITLE)
        infoDialog!! .setCancelable(true)
        infoDialog!! .setContentView(R.layout.dialog_info)
        val textTitle = infoDialog!! .findViewById<TextView>(R.id.d_info_title)
        textTitle.text = HtmlCompat.fromHtml(place!!.name, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val textDesc = infoDialog!! . findViewById<TextView>(R.id.d_info_desc)
        //textDesc.movementMethod = ScrollingMovementMethod()
        //textDesc.justificationMode = JUSTIFICATION_MODE_INTER_WORD;
        //textDesc.text = Html.fromHtml(place!!.description) HtmlCompat.fromHtml(, HtmlCompat.FROM_HTML_MODE_LEGACY)
        textDesc.text = HtmlCompat.fromHtml(place!!.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val btnClose = infoDialog!! .findViewById(R.id.d_info_btn_close) as Button
        btnClose.setOnClickListener {
            infoDialog!! .dismiss()
        }
        val btnQuiz = infoDialog!! .findViewById(R.id.d_info_btn_quiz) as Button
        btnQuiz.setOnClickListener {
            startQuiz(place!!)
        }

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(infoDialog!!.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        infoDialog!!.show()

        infoDialog!!.window!!.attributes = lp
    }

    fun startQuiz(p: Place) {
        place = p
        quizScore = 0
        quizDialog = Dialog(cont)
        quizDialog!! .requestWindowFeature(Window.FEATURE_NO_TITLE)
        quizDialog!! .setCancelable(true)
        quizDialog!! .setContentView(R.layout.dialog_quiz)
        val textQ = quizDialog!! .findViewById(R.id.d_quiz_question) as TextView
        textQ.text = HtmlCompat.fromHtml(place!!.question1, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val rbQ0 = quizDialog!! .findViewById(R.id.d_quiz_rb1) as RadioButton
        val rbQ1 = quizDialog!! .findViewById(R.id.d_quiz_rb2) as RadioButton
        val rbQ2 = quizDialog!! .findViewById(R.id.d_quiz_rb3) as RadioButton
        val rbg = quizDialog!! .findViewById(R.id.d_quiz_rbg) as RadioGroup
        val corAns = randomizeQuestions(rbQ0, rbQ1, rbQ2, place!!.answer11, place!!.answer12, place!!.answer1C)
        textQ.text = textQ.text.toString()
        val btnClose = quizDialog!! .findViewById(R.id.d_quiz_btn_close) as Button
        btnClose.setOnClickListener {
            quizDialog!! .dismiss()
        }
        val btnAnswer = quizDialog!! .findViewById(R.id.d_quiz_btn_answer) as Button
        btnAnswer.isEnabled = false;
        selectedRB(rbQ0, rbQ1, rbQ2, btnAnswer)
        btnAnswer.setOnClickListener {
            if(corAns == 0 && rbQ0.isChecked) {
                quizScore++
            }
            if(corAns == 1 && rbQ1.isChecked) {
                quizScore++
            }
            if(corAns == 2 && rbQ2.isChecked) {
                quizScore++
            }
            quiz1(rbQ0, rbQ1, rbQ2, textQ, btnAnswer, rbg)
        }

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(quizDialog!!.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        quizDialog!! .show()

        quizDialog!!.window!!.attributes = lp
    }

    private fun quiz1(rb0 : RadioButton, rb1 : RadioButton, rb2 : RadioButton, textQ : TextView, btnAns : Button, rbg : RadioGroup) {
        textQ.text = HtmlCompat.fromHtml(place!!.question2, HtmlCompat.FROM_HTML_MODE_LEGACY)
        btnAns.isEnabled = true
        //decheckRB(rb0, rb1, rb2)
        rbg.clearCheck()
        selectedRB(rb0, rb1, rb2, btnAns)
        val corAns = randomizeQuestions(rb0, rb1, rb2, place!!.answer21!!, place!!.answer22!!, place!!.answer2C!!)
        btnAns.setOnClickListener {
            if(corAns == 0 && rb0.isChecked) {
                quizScore++
            }
            if(corAns == 1 && rb1.isChecked) {
                quizScore++
            }
            if(corAns == 2 && rb2.isChecked) {
                quizScore++
            }
            quiz2(rb0, rb1, rb2, textQ, btnAns, rbg)
        }
    }

    private fun quiz2(rb0 : RadioButton, rb1 : RadioButton, rb2 : RadioButton, textQ : TextView, btnAns : Button, rbg : RadioGroup) {
        textQ.text = HtmlCompat.fromHtml(place!!.question3, HtmlCompat.FROM_HTML_MODE_LEGACY)
        btnAns.isEnabled = true
        btnAns.text = cont.getString(R.string.d_quiz_results)
        //decheckRB(rb0, rb1, rb2)
        rbg.clearCheck()
        selectedRB(rb0, rb1, rb2, btnAns)
        val corAns = randomizeQuestions(rb0, rb1, rb2, place!!.answer31, place!!.answer32, place!!.answer3C
        )
        btnAns.setOnClickListener {
            if(corAns == 0 && rb0.isChecked) {
                quizScore++
            }
            if(corAns == 1 && rb1.isChecked) {
                quizScore++
            }
            if(corAns == 2 && rb2.isChecked) {
                quizScore++
            }
            showResults()
        }
    }

    private fun showResults() {
        if(mode == "real") {
            if (quizScore == 3) {
                showWin()
                infoDialog!!.dismiss()
                quizDialog!!.dismiss()
            } else {
                showLose()
            }
        }
        else {
            showMockResults()
        }
    }

    fun showMockResults() {
        val dialog = Dialog(cont)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog .setCancelable(true)
        dialog .setContentView(R.layout.dialog_quiz_mock_results)
        val title = dialog .findViewById(R.id.d_quiz_m_title) as TextView
        if(quizScore < 3) {
            title.text = cont.getString(R.string.d_quiz_loser)
        }
        else {
            title.text = cont.getString(R.string.d_quiz_winner)
        }
        val result = dialog .findViewById(R.id.d_quiz_m_result) as TextView
        result.text = quizScore.toString() + "/3"
        val btnClose = dialog .findViewById(R.id.d_quiz_m_btn_close) as Button
        btnClose.setOnClickListener {
            infoDialog!!.dismiss()
            quizDialog!!.dismiss()
            dialog .dismiss()
        }
        val btnRepeat = dialog .findViewById(R.id.d_quiz_m_btn_repeat) as Button
        btnRepeat.setOnClickListener {
            quizDialog!!.dismiss()
            startQuiz(place!!)
            dialog .dismiss()
        }

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        dialog.show()

        dialog.window!!.attributes = lp
    }

    private fun showWin() {
        val dialog = Dialog(cont)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog .setCancelable(false)
        dialog .setContentView(R.layout.dialog_quiz_won)
        val title = dialog .findViewById(R.id.d_quiz_w_title) as TextView
        title.text = cont.getString(R.string.d_quiz_winner)
        val result = dialog .findViewById(R.id.d_quiz_w_result) as TextView
        result.text = "3/3"
        val btnReward = dialog .findViewById(R.id.d_quiz_w_btn_reward) as Button
        btnReward.setOnClickListener {
            act!!.getReward(place!!)
            dialog .dismiss()
        }

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        dialog.show()

        dialog.window!!.attributes = lp
    }

    private fun showLose() {
        val dialog = Dialog(cont)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog .setCancelable(true)
        dialog .setContentView(R.layout.dialog_quiz_lost)
        val title = dialog .findViewById(R.id.d_quiz_l_title) as TextView
        title.text = cont.getString(R.string.d_quiz_loser)
        val result = dialog .findViewById(R.id.d_quiz_l_result) as TextView
        result.text = quizScore.toString() + "/3"
        val btnClose = dialog .findViewById(R.id.d_quiz_l_btn_close) as Button
        btnClose.setOnClickListener {
            infoDialog!!.dismiss()
            quizDialog!!.dismiss()
            dialog .dismiss()
        }
        val btnRepeat = dialog .findViewById(R.id.d_quiz_l_btn_repeat) as Button
        btnRepeat.setOnClickListener {
            quizDialog!!.dismiss()
            startQuiz(place!!)
            dialog .dismiss()
        }

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        dialog.show()

        dialog.window!!.attributes = lp
    }

    private fun selectedRB(rb0 : RadioButton, rb1 : RadioButton, rb2 : RadioButton, btnAns : Button) {
        rb0.setOnClickListener{
            btnAns.isEnabled = true
        }
        rb1.setOnClickListener{
            btnAns.isEnabled = true
        }
        rb2.setOnClickListener{
            btnAns.isEnabled = true
        }
    }

    private fun randomizeQuestions(rb0 : RadioButton, rb1 : RadioButton, rb2 : RadioButton, ans1 : String, ans2 : String, ansC : String) : Int {
        val ansList = mutableListOf(ans1, ans2, ansC)
        ansList.shuffle()
        rb0.text = ansList[0]
        rb1.text = ansList[1]
        rb2.text = ansList[2]

        for(i in 0..2) {
            if(ansList[i] == ansC) {
                return i
            }
        }
        Toast.makeText(cont, "Something wrong happened correct answer is 1st", Toast.LENGTH_LONG).show()
        return 2
    }
}