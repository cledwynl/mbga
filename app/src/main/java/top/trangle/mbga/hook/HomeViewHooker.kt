package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import top.trangle.mbga.utils.reflectionToString

object HomeViewHooker : YukiBaseHooker() {
    override fun onHook() {
        val clzBasicIndexItem = "com.bilibili.pegasus.api.model.BasicIndexItem".toClass()
        val getUri = clzBasicIndexItem.method { name = "getUri" }

        getUri.hook {
            after {
                if (!prefs.getBoolean("home_disable_portrait")) {
                    return@after
                }
                val uri = result as String?
                if (!uri.isNullOrEmpty()) {
                    if (uri.startsWith("bilibili://story/")) {
                        result = "bilibili://video/" + uri.substringAfter("bilibili://story/")
                        YLog.debug("初始化视频信息时，竖屏转横屏成功")
                    }
                }
                YLog.debug("item: ${reflectionToString(instance)}")
            }
        }

        val clzTabItem = "tv.danmaku.bili.ui.main2.basic.BaseMainFrameFragment\$s".toClass()
        val fieldC = clzTabItem.field { name = "c" }

        val clzL = "tv.danmaku.bili.ui.main2.resource.l".toClass()
        val fieldD = clzL.field { name = "d" }

        "tv.danmaku.bili.ui.main2.MainFragment\$a".toClass().method { name = "a" }.hook {
            after {
                (result as ArrayList<*>).removeIf { tabItem ->
                    val c = fieldC.get(tabItem).any()
                    val d = fieldD.get(c).string()

                    val disable_home =
                        prefs.getBoolean("tabs_disable_home") &&
                            "bilibili://main/home".equals(d)
                    val disable_dynamic =
                        prefs.getBoolean("tabs_disable_dynamic") &&
                            "bilibili://following/home".equals(d)
                    disable_home || disable_dynamic
                }
            }
        }
    }
}
