package io.github.nikulshinip.cottage.ui.cellar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.nikulshinip.cottage.R
import io.github.nikulshinip.cottage.obj.Request
import io.github.nikulshinip.cottage.obj.SETTING
import io.github.nikulshinip.cottage.obj.Setting
import io.github.nikulshinip.cottage.obj.SettingController
import kotlinx.android.synthetic.main.apply_button.view.*
import kotlinx.android.synthetic.main.cellar_setting.view.*
import kotlinx.coroutines.*

class CellarFragment : Fragment() {

    private val MIN = "MIN"
    private val MAX = "MAX"

    private lateinit var setting: Setting
    private lateinit var root: View

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putSerializable(SETTING, setting)
            putInt(MIN, root.min_temp_seek_bar.progress)
            putInt(MAX, root.max_temp_seek_bar.progress + 1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        root = inflater.inflate(R.layout.fragment_cellar, container, false)

        setting = savedInstanceState?.let{ it.getSerializable(SETTING) as Setting } ?: SettingController(inflater.context).read()

        savedInstanceState?.let { setState(it.getInt(MIN), it.getInt(MAX)) } ?: update()

        root.apply {
            min_temp_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    min_temp_text.text = p1.toString() + "°C"
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })

            max_temp_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    max_temp_text.text = (p1 + 1).toString() + "°C"
                }

                override fun onStartTrackingTouch(p0: SeekBar?){}
                override fun onStopTrackingTouch(p0: SeekBar?){}
            })

            apply_button.setOnClickListener {
                scope.launch{
                    val response = post( min_temp_seek_bar.progress, max_temp_seek_bar.progress + 1 ).await()
                    if (response == "ok")
                        Snackbar.make(view!!, R.string.setting_save, Snackbar.LENGTH_SHORT).show()
                    else
                        Snackbar.make(view!!, R.string.error_save_setting, Snackbar.LENGTH_SHORT).show()
                }
            }

        }

        return root
    }

    override fun onDestroy() {
        scope.coroutineContext.cancelChildren()
        super.onDestroy()
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.refresh_button){
            update()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setState(min: Int, max: Int){
        root.apply {
            min_temp_text.text = min.toString() + "°C"
            min_temp_seek_bar.progress = min
            max_temp_text.text = max.toString() + "°C"
            max_temp_seek_bar.progress = max-1
        }
    }

    private fun update(){
        scope.launch{

            val response = get().await()
            val sType = object : TypeToken<Map<String, Int>>(){}.type
            val map = Gson().fromJson<Map<String, Int>>(response, sType)
            if (response != "null") {
                setState(map["min"] ?: 0, map["max"] ?: 0)
            }else{
                Snackbar.make(root, R.string.error_connecting, Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    private fun get():Deferred<String>{
        return GlobalScope.async{
            val url = "http://${setting.serverAddress}/cellar/get"
            Request(url, setting.authString).connect()
        }
    }

    private fun post(min: Int, max: Int):Deferred<String>{
        return GlobalScope.async {
            val url = "http://${setting.serverAddress}/cellar/set"
            val parameters = mapOf("min" to min.toString(), "max" to max.toString())
            Request(url, setting.authString, parameters).connect()
        }
    }

}