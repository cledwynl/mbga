package top.trangle.mbga.views

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.highcapable.yukihookapi.hook.xposed.prefs.ui.ModulePreferenceFragment
import top.trangle.mbga.BILI_IN_PKG_ID
import top.trangle.mbga.CHANNEL_MSG_HOME_BOTTOM_TABS
import top.trangle.mbga.CHANNEL_MSG_REQ_HOME_BOTTOM_TABS
import top.trangle.mbga.R
import top.trangle.mbga.databinding.ActivitySettingsBinding
import top.trangle.mbga.hook.BottomTab

val PREFS_NEED_RESTART =
    arrayOf(
        "mine_add_search",
    )

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.extras?.getBoolean("show_first_launch_alert") == true) {
            MaterialAlertDialogBuilder(this)
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
        private var needRestart = false

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
            val onBackPressedDispatcher = activity?.onBackPressedDispatcher ?: return
            onBackPressedDispatcher.addCallback {
                val quit: () -> Unit = {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
                if (!needRestart) {
                    quit()
                } else {
                    MaterialAlertDialogBuilder(view.context)
                        .setMessage(R.string.bili_need_restart_message)
                        .setPositiveButton(R.string.common_got_it) { _, _ ->
                            quit()
                        }
                        .create()
                        .show()
                }

            }
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?,
        ) {
            super.onSharedPreferenceChanged(sharedPreferences, key)
            if (
                !needRestart &&
                (key?.startsWith("tabs_disable#") == true || PREFS_NEED_RESTART.contains(key))
            ) {
                needRestart = true
            }
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
