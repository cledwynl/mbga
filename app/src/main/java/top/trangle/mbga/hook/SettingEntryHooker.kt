package top.trangle.mbga.hook

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import top.trangle.mbga.utils.factory.dp

object SettingEntryHooker : YukiBaseHooker() {
    override fun onHook() {
        hookPreferencesActivity()
        hookMainActivity()
    }

    fun hookPreferencesActivity() {
        val clzBiliPreferencesActivity =
            "com.bilibili.app.preferences.BiliPreferencesActivity".toClass()
        val onCreate = clzBiliPreferencesActivity.method { name = "onCreate" }
        onCreate.hook {
            after {
                val activity = instance as Activity
                val toolbar: ViewGroup = activity.findViewById(0x7f0929ab)
                YLog.debug("view: $toolbar")
                val lp =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                val tv = TextView(instance as Activity)
                tv.setText("MBGA!")
                tv.gravity = Gravity.CENTER
                tv.setPadding(16.dp(activity), 0, 16.dp(activity), 0)
                tv.setOnClickListener {
                    val intent =
                        Intent().apply {
                            component =
                                ComponentName(
                                    "top.trangle.mbga",
                                    "top.trangle.mbga.views.SettingsActivity",
                                )
                        }
                    activity.startActivity(intent)
                }
                toolbar.addView(tv, 2, lp)
            }
        }
    }

    private fun hookMainActivity() {
        "tv.danmaku.bili.MainActivityV2".toClass().method { name = "onCreate" }.hook {
            after {
                if (!prefs.isPreferencesAvailable) {
                    val activity = instance as Activity

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
