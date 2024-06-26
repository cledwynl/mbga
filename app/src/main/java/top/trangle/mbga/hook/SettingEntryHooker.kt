package top.trangle.mbga.hook

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.yukihookapi.hook.factory.method
import top.trangle.mbga.utils.MyHooker
import top.trangle.mbga.utils.factory.dp

object SettingEntryHooker : MyHooker() {
    override fun onHook() {
        subHook(this::hookPreferencesActivity)
    }

    private fun hookPreferencesActivity() {
        val clzBiliPreferencesActivity =
            "com.bilibili.app.preferences.BiliPreferencesActivity".toClass()
        val onCreate = clzBiliPreferencesActivity.method { name = "onCreate" }

        onCreate.hook {
            after {
                val activity = instance as Activity

                @SuppressLint("DiscouragedApi")
                val toolbar: ViewGroup =
                    activity.findViewById(
                        activity.resources
                            .getIdentifier("nav_top_bar", "id", activity.packageName),
                    )
                val lp =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                val tv = TextView(instance as Activity)

                @SuppressLint("SetTextI18n")
                tv.text = "MBGA!"
                tv.gravity = Gravity.CENTER
                tv.setPadding(16.dp(activity), 0, 16.dp(activity), 0)
                tv.setOnClickListener {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.setClassName(
                        "top.trangle.mbga",
                        "top.trangle.mbga.views.SettingsActivity",
                    )
                    activity.startActivity(intent)
                }
                toolbar.addView(tv, 2, lp)
            }
        }
    }
}
