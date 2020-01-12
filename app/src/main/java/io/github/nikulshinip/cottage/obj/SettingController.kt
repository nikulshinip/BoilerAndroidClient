package io.github.nikulshinip.cottage.obj

import android.content.Context
import androidx.core.content.edit

private const val FILE_NAME = "cottageSetting"
private const val SERVER_ADDRESS = "serverAddress"
private const val USER_NAME = "userName"
private const val PASSWORD = "password"

class SettingController(private val context: Context){

    fun read():Setting{
        val setting = Setting()
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).apply {
            setting.serverAddress = getString(SERVER_ADDRESS, "").toString()
            setting.userName = getString(USER_NAME, "").toString()
            setting.password = getString(PASSWORD, "").toString()
        }
        return setting
    }

    fun write(setting: Setting){
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).edit {
            putString(SERVER_ADDRESS, setting.serverAddress)
            putString(USER_NAME, setting.userName)
            putString(PASSWORD, setting.password)
            commit()
        }
    }
}