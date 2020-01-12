package io.github.nikulshinip.cottage.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import io.github.nikulshinip.cottage.R
import io.github.nikulshinip.cottage.obj.Request
import io.github.nikulshinip.cottage.obj.SETTING
import io.github.nikulshinip.cottage.obj.Setting
import io.github.nikulshinip.cottage.obj.SettingController
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_setting.view.*
import kotlinx.coroutines.*

class SettingFragment : Fragment() {

    private lateinit var setting: Setting

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(SETTING, setting)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        val root = inflater.inflate(R.layout.fragment_setting, container, false)

        setting = savedInstanceState?.let{ it.getSerializable(SETTING) as Setting } ?: SettingController(inflater.context).read()

        root.apply {
            address.setText(setting.serverAddress)
            user_name.setText(setting.userName)
            user_password.setText(setting.password)
            save_button.setOnClickListener {
                saveSetting()
                Snackbar.make(it, R.string.setting_save, Snackbar.LENGTH_SHORT).show()
            }
            check_connecting_button.setOnClickListener { scope.launch{checkConnect()} }
        }

        return root
    }

    override fun onDestroy() {
        scope.coroutineContext.cancelChildren()
        super.onDestroy()
    }


    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        menu?.clear()
    }

    private fun saveSetting(){
        setting.apply {
            serverAddress = address.text.toString()
            userName = user_name.text.toString()
            password = user_password.text.toString()
        }
        SettingController(context!!).write(setting)
    }

    private suspend fun checkConnect(){
        val tmpSetting = Setting(address.text.toString(), user_name.text.toString(), user_password.text.toString())
        val url = "http://${tmpSetting.serverAddress}"
        val response = GlobalScope.async { Request(url, tmpSetting.authString).connect() }.await()
        if (response == "ok") {
            setting = tmpSetting
            saveSetting()
            Snackbar.make(view!!, R.string.connecting_susses_setting_save, Snackbar.LENGTH_SHORT).show()
        }else{
            Snackbar.make(view!!, R.string.error_connecting, Snackbar.LENGTH_SHORT).show()
        }

    }

}