package top.trangle.mbga.hook

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog

object MainActivityHooker : YukiBaseHooker() {
    override fun onHook() {
        "tv.danmaku.bili.MainActivityV2".toClass().method { name = "onCreate" }
            .hook {
                after {
                    val activity = instance as Activity
                    activity.injectModuleAppResources()

                    if (!prefs.isPreferencesAvailable) {
                        val intent =
                            Intent().apply {
                                component =
                                    ComponentName(
                                        "top.trangle.mbga",
                                        "top.trangle.mbga.views.SettingsActivity",
                                    )
                                putExtra("show_first_launch_alert", true)
                            }
                        activity.startActivity(intent)
                    }
                }
            }
    }
}
