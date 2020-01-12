package io.github.nikulshinip.cottage.ui.heating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.nikulshinip.cottage.R
import io.github.nikulshinip.cottage.obj.*
import kotlinx.android.synthetic.main.apply_button.view.*
import kotlinx.android.synthetic.main.heating_alg.view.*
import kotlinx.android.synthetic.main.heating_on_off.view.*
import kotlinx.android.synthetic.main.heating_power.view.*
import kotlinx.android.synthetic.main.heating_temp.view.*
import kotlinx.coroutines.*

class HeatingFragment : Fragment() {

    private lateinit var root: View
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

        root = inflater.inflate(R.layout.fragment_heating, container, false)

        setting = savedInstanceState?.let { it.getSerializable(SETTING) as Setting } ?: SettingController(inflater.context).read()

        update()

        root.apply {

            power_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    power_text.text = (p1 * 2.5F).toString() + "кВт"
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })

            alg_room_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    alg_room_text.text = p1.toString() + "°C"
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })

            alg_return_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    alg_return_text.text = (p1 + 40).toString() + "°C"
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })

            boiler_alg_room.setOnClickListener {
                if (boiler_alg_room.isChecked) {
                    alg_return_layout.visibility = View.GONE
                    alg_room_layout.visibility = View.VISIBLE
                }
            }

            boiler_alg_return.setOnClickListener {
                if (boiler_alg_return.isChecked) {
                    alg_room_layout.visibility = View.GONE
                    alg_return_layout.visibility = View.VISIBLE
                }
            }

            apply_button.setOnClickListener {
                scope.launch{
                    val response = applySetting().await()
                    if (response == "ok")
                        Snackbar.make(it, R.string.apply_parameters, Snackbar.LENGTH_SHORT).show()
                    else
                        Snackbar.make(it, R.string.error_save_parameters, Snackbar.LENGTH_SHORT).show()
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
        if (item?.itemId == R.id.refresh_button) {
            update()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun update() {
        scope.launch{
            val jGetOptions = getOptions()
            val jGetState = getState()

            val temps = TempController(setting).fetchTemps()

            val optionType = object : TypeToken<Map<String, Int>>() {}.type
            val options = Gson().fromJson<Map<String, Int>>(jGetOptions.await(), optionType)

            val stateType = object : TypeToken<Map<String, Boolean>>() {}.type
            val states = Gson().fromJson<Map<String, Boolean>>(jGetState.await(), stateType)

            if (options == null || states == null) {
                Snackbar.make(root, R.string.error_connecting, Snackbar.LENGTH_SHORT).show()
                return@launch
            }

            root.apply {

                on_off_checkbox.isChecked = options["on/off"]?.let { it == 1 } ?: false

                t_boiler.text = temps[1]?.let { it.temp.toString() + "°C" } ?: "error"
                t_return.text = temps[2]?.let { it.temp.toString() + "°C" } ?: "error"
                t_output.text = temps[3]?.let { it.temp.toString() + "°C" } ?: "error"
                t_sensor.text = temps[4]?.let { it.temp.toString() + "°C" } ?: "error"

                when (options["mode"]) {
                    0 -> {
                        boiler_alg_return.isChecked = true
                        alg_room_layout.visibility = View.GONE
                        alg_return_layout.visibility = View.VISIBLE
                    }
                    1 -> {
                        boiler_alg_room.isChecked = true
                        alg_return_layout.visibility = View.GONE
                        alg_room_layout.visibility = View.VISIBLE
                    }
                }

                alg_room_seek_bar.progress = options["limit_t1"] ?: 0
                alg_room_text.text = options["limit_t1"]?.let { it.toString() + "°C" } ?: "NA"

                alg_return_seek_bar.progress = options["limit_t2"]?.let { it - 40 } ?: 0
                alg_return_text.text = options["limit_t2"]?.let { it.toString() + "°C" } ?: "NA"

                power_seek_bar.progress = options["power"]?.let { it / 25 } ?: 0
                power_text.text = options["power"]?.let { (it / 10F).toString() + "кВт" } ?: "NA"

                statePowerLamp(power_lamp1, states["st1"])
                statePowerLamp(power_lamp2, states["st2"])
                statePowerLamp(power_lamp3, states["st3"])
                statePowerLamp(power_lamp4, states["st4"])
                statePowerLamp(power_lamp5, states["st5"])
                statePowerLamp(power_lamp6, states["st6"])

            }
        }
    }

    private fun statePowerLamp(lamp: ImageView, state: Boolean?) {
        if (state ?: false) {
            lamp.setImageResource(R.drawable.power_lamp_on)
        } else {
            lamp.setImageResource(R.drawable.power_lamp_off)
        }
    }

    private fun getOptions(): Deferred<String> {
        return GlobalScope.async {
            val url = "http://${setting.serverAddress}/boiler/get"
            Request(url, setting.authString).connect()
        }
    }

    private fun getState(): Deferred<String> {
        return GlobalScope.async {
            val url = "http://${setting.serverAddress}/boiler/state"
            Request(url, setting.authString).connect()
        }
    }

    private fun applySetting(): Deferred<String> {
        return GlobalScope.async {
            val url = "http://${setting.serverAddress}/boiler/set"
            val parameters = mutableMapOf<String, String>()
            root.apply {
                parameters += "on/off" to when (on_off_checkbox.isChecked) {
                    true -> "1"
                    false -> "0"
                }
                parameters += "mode" to when (boiler_alg_room.isChecked) {
                    true -> "1"
                    false -> "0"
                }
                parameters += "power" to (power_seek_bar.progress * 25).toString()
                parameters += "limit_t1" to alg_room_seek_bar.progress.toString()
                parameters += "limit_t2" to (alg_return_seek_bar.progress + 40).toString()
            }
            Request(url, setting.authString, parameters).connect()
        }
    }

}