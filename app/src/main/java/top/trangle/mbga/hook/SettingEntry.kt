import android.app.Activity
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import top.trangle.mbga.utils.factory.dp

object SettingEntryHooker : YukiBaseHooker() {
    override fun onHook() {
        val BiliPreferencesActivity =
                "com.bilibili.app.preferences.BiliPreferencesActivity".toClass()
        val onCreate = BiliPreferencesActivity.method { name = "onCreate" }
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
                tv.setOnClickListener { YLog.debug("click") }
                toolbar.addView(tv, 2, lp)
            }
        }
    }
}
