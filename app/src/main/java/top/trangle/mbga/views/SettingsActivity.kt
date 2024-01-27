package top.trangle.mbga.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.highcapable.yukihookapi.hook.xposed.prefs.ui.ModulePreferenceFragment
import top.trangle.mbga.R
import top.trangle.mbga.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    }
}
