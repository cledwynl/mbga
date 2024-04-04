package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import top.trangle.mbga.CHANNEL_MSG_HOME_BOTTOM_TABS
import top.trangle.mbga.CHANNEL_MSG_REQ_HOME_BOTTOM_TABS
import top.trangle.mbga.utils.reflectionToString

data class BottomTab(val name: String, val scheme: String) : java.io.Serializable

object HomeViewHooker : YukiBaseHooker() {
    private var bottomTabs: ArrayList<BottomTab>? = null

    override fun onHook() {
        hookPortraitVideo()
        hookBottomTabs()
        hookAutoRefresh()
        hookTabReload()

        setupTabProvider()
    }

    private fun hookPortraitVideo() {
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
    }

    private fun hookBottomTabs() {
        val clzTabItem = "tv.danmaku.bili.ui.main2.basic.BaseMainFrameFragment\$s".toClass()
        val fieldC = clzTabItem.field { name = "c" }

        val clzL = "tv.danmaku.bili.ui.main2.resource.l".toClass()
        val fieldB = clzL.field { name = "b" }
        val fieldD = clzL.field { name = "d" }

        "tv.danmaku.bili.ui.main2.MainFragment\$a".toClass().method { name = "a" }.hook {
            after {
                val newList = ArrayList<BottomTab>()

                (result as ArrayList<*>).removeIf { tabItem ->
                    val c = fieldC.get(tabItem).any()
                    val tabName = fieldB.get(c).string()
                    val tabScheme = fieldD.get(c).string()

                    if (tabScheme != "bilibili://user_center/mine") {
                        newList.add(BottomTab(name = tabName, scheme = tabScheme))
                    }

                    YLog.debug("首页底部Tab: ${reflectionToString(c)}")

                    prefs.getBoolean("tabs_disable#$tabScheme")
                }

                if (bottomTabs == null) {
                    bottomTabs = newList
                }
            }
        }
    }

    private fun hookAutoRefresh() {
        val clzConfig = "com.bilibili.pegasus.api.modelv2.Config".toClass()

        arrayOf(
            "getAutoRefreshTimeByActiveInterval",
            "getAutoRefreshTimeByAppearInterval",
            "getAutoRefreshTimeByBehaviorInterval",
        ).forEach { methodName ->
            clzConfig.method { name = methodName }
                .hook {
                    replaceAny {
                        if (!prefs.getBoolean("home_disable_auto_refresh")) {
                            callOriginal()
                        } else {
                            -1L
                        }
                    }
                }
        }

        // onResume回来的调用链路上的一环
        "com.bilibili.pegasus.promo.index.IndexFeedFragmentV2".toClass().method { name = "ny" }
            .hook {
                replaceUnit {
                    if (!prefs.getBoolean("home_disable_auto_refresh")) {
                        callOriginal()
                    }
                }
            }
    }

    private fun hookTabReload() {
        val clzIndexFragmentV2 = "com.bilibili.pegasus.promo.index.IndexFeedFragmentV2".toClass()
        val clzConfig = "com.bilibili.pegasus.api.modelv2.Config".toClass()
        val fieldFeedTopClean = clzConfig.field { name = "feedTopClean" }

        clzIndexFragmentV2.method { name = "ez" }
            .hook {
                before {
                    if (!prefs.getBoolean("home_disable_feed_top_clean")) {
                        return@before
                    }
                    fieldFeedTopClean.get(args[0]).set(0)
                }
            }
    }

    private fun setupTabProvider() {
        dataChannel.wait(key = CHANNEL_MSG_REQ_HOME_BOTTOM_TABS) {
            sendTabToModule()
        }
    }

    private fun sendTabToModule() {
        val tabs = bottomTabs ?: return
        dataChannel.put(
            key = CHANNEL_MSG_HOME_BOTTOM_TABS,
            value = tabs,
        )
    }
}
