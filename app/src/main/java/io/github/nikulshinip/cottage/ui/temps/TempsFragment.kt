package io.github.nikulshinip.cottage.ui.temps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.nikulshinip.cottage.R
import io.github.nikulshinip.cottage.obj.SETTING
import io.github.nikulshinip.cottage.obj.Setting
import io.github.nikulshinip.cottage.obj.SettingController
import io.github.nikulshinip.cottage.obj.TempController
import kotlinx.android.synthetic.main.fragment_temps.view.*
import kotlinx.coroutines.*

class TempsFragment : Fragment() {

    private lateinit var root: View
    private lateinit var setting: Setting

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putSerializable(SETTING, setting)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        root = inflater.inflate(R.layout.fragment_temps, container, false)

        setting = savedInstanceState?.let{ it.getSerializable(SETTING) as Setting } ?: SettingController(inflater.context).read()

        update()

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

    private fun update(){
        scope.launch{
            root.temp_list.adapter = TempsAdapter(
                                        TempController(setting).fetchTemps().map { (_, temp) -> temp },
                                        root.context
            )
        }
    }
}