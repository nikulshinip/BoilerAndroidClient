package io.github.nikulshinip.cottage.obj

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*

class TempController(val setting: Setting) {

    suspend fun fetchTemps(): Map<Int, Temp>{
        val response = requeryTemps().await()
        if (response == "null")
            return mapOf()
        val sType = object : TypeToken<Map<Int, Temp>>(){}.type
        return Gson().fromJson<Map<Int, Temp>>(response, sType)
    }

    fun requeryTemps():Deferred<String>{
        return GlobalScope.async {
            val url = "http://${setting.serverAddress}/temperatures"
            Request(url, setting.authString).connect()
        }
    }

}