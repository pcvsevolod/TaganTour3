package vshapovalov.arproject.tagantour3

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Tour (
    var name : String? = "",
    var uniqueId: String = "",
    var description: String = "",
    var type: String = ""
)