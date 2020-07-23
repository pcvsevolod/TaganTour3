package vshapovalov.arproject.tagantour3

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Place (
    var name : String = "",
    var lat : Double = 0.0,
    var lng : Double = 0.0,
    var question1 : String = "",
    var answer11 : String = "",
    var answer12 : String = "",
    var answer1C : String = "",
    var question2 : String = "",
    var answer21 : String = "",
    var answer22 : String = "",
    var answer2C : String = "",
    var question3 : String = "",
    var answer31 : String = "",
    var answer32 : String = "",
    var answer3C : String = "",
    var tours : String = "",
    var description : String = "",
    var fileName : String = "",
    var number : Int = 0,
    var collected : Boolean? = false
)