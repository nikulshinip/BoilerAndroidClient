package io.github.nikulshinip.cottage.obj

import android.util.Base64
import java.io.Serializable

const val SETTING = "SETTING"

data class Setting(var serverAddress: String = "",
                   var userName: String = "",
                   var password: String = "") : Serializable {

    val authString: String
        get() = Base64.encodeToString("$userName:$password".toByteArray(), Base64.NO_PADDING)

}