package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import top.trangle.mbga.CHANNEL_MSG_HOME_BOTTOM_TABS
import top.trangle.mbga.CHANNEL_MSG_REQ_HOME_BOTTOM_TABS
import top.trangle.mbga.utils.reflectionToString
import top.trangle.mbga.utils.subHook

data class BottomTab(val name: String, val scheme: String) : java.io.Serializable

object HomeViewHooker : YukiBaseHooker() {
    private var bottomTabs: ArrayList<BottomTab>? = null

    override fun onHook() {
        subHook(this::hookPortraitVideo)
        subHook(this::hookBottomTabs)
        subHook(this::hookAutoRefresh)
        subHook(this::hookTabReload)

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
        "tv.danmaku.bili.ui.main2.MainFragment\$a".toClass().method { name = "a" }.hook {
            after {
                val newList = ArrayList<BottomTab>()

                (result as ArrayList<*>).removeIf { tabItem ->
                    val c = tabItem.javaClass.field { name = "c" }.get(tabItem).any()
                        ?: return@removeIf false
                    val tabName = c.javaClass.field { name = "b" }.get(c).string()
                    val tabScheme = c.javaClass.field { name = "d" }.get(c).string()

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

        val clzIndexFeedFragmentV2 =
            "com.bilibili.pegasus.promo.index.IndexFeedFragmentV2".toClass()
        clzIndexFeedFragmentV2.method {
            modifiers { isStatic }
            param {
                it.size == 6 && it[0] == clzIndexFeedFragmentV2 && it[1] == IntType && it[2] == LongType && it[4] == IntType
            }
        }.hook {
            replaceUnit {
                val isByOnResume =
                    Throwable().stackTrace.any {
                        it.methodName.contains("onResume")
                    }
                if (!isByOnResume || !prefs.getBoolean("home_disable_auto_refresh")) {
                    callOriginal()
                }
            }
        }
    }

    private fun hookTabReload() {
        val clzIndexFragmentV2 = "com.bilibili.pegasus.promo.index.IndexFeedFragmentV2".toClass()
        val clzConfig = "com.bilibili.pegasus.api.modelv2.Config".toClass()
        val fieldFeedTopClean = clzConfig.field { name = "feedTopClean" }

        clzIndexFragmentV2.method { param(clzConfig) }
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
