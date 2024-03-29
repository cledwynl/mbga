package top.trangle.mbga.views

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreferenceCompat
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.highcapable.yukihookapi.hook.xposed.prefs.ui.ModulePreferenceFragment
import top.trangle.mbga.BILI_IN_PKG_ID
import top.trangle.mbga.CHANNEL_MSG_HOME_BOTTOM_TABS
import top.trangle.mbga.CHANNEL_MSG_REQ_HOME_BOTTOM_TABS
import top.trangle.mbga.R
import top.trangle.mbga.databinding.ActivitySettingsBinding
import top.trangle.mbga.hook.BottomTab

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.extras?.getBoolean("show_first_launch_alert") == true) {
            AlertDialog.Builder(this)
                .setMessage(R.string.app_first_launch_message)
                .setNegativeButton(R.string.app_first_launch_goback) { _, _ -> this.finish() }
                .setPositiveButton(R.string.app_first_launch_stay) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_content, MySettingsFragment())
            .commit()
    }

    class MySettingsFragment : ModulePreferenceFragment() {
        override fun onCreatePreferencesInModuleApp(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }

        override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?,
        ) {
            super.onViewCreated(view, savedInstanceState)
            addBottomTabPrefs()
        }

        private fun addBottomTabPrefs() {
            val bottomTabsCategory =
                preferenceScreen.findPreference<PreferenceCategory>("bottom_tabs_category")
                    ?: return

            activity?.application?.dataChannel(packageName = BILI_IN_PKG_ID)?.with {
                wait<List<BottomTab>>(key = CHANNEL_MSG_HOME_BOTTOM_TABS) { value ->
                    value.forEach {
                        bottomTabsCategory.addPreference(
                            SwitchPreferenceCompat(bottomTabsCategory.context).apply {
                                title = resources.getString(R.string.setting_tabs_hide, it.name)
                                key = "tabs_disable#${it.scheme}"
                                isIconSpaceReserved = false
                            },
                        )
                    }
                }

                put(key = CHANNEL_MSG_REQ_HOME_BOTTOM_TABS)
            }
        }
    }
}
