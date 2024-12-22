package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import top.trangle.mbga.CHANNEL_MSG_HOME_BOTTOM_TABS
import top.trangle.mbga.CHANNEL_MSG_REQ_HOME_BOTTOM_TABS
import top.trangle.mbga.utils.MyHooker
import top.trangle.mbga.utils.reflectionToString

data class BottomTab(val name: String, val scheme: String) : java.io.Serializable

object HomeViewHooker : MyHooker() {
    private var bottomTabs: ArrayList<BottomTab>? = null

    override fun onHook() {
        subHook(this::hookPortraitVideo)
        subHook(this::hookBottomTabs)
        subHook(this::hookAutoRefresh)
        subHook(this::hookFeedConfig)
        subHook(this::hookIndexFeedList)

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
                    }
                }
            }
        }
    }

    private fun hookBottomTabs() {
        "tv.danmaku.bili.ui.main2.MainFragment\$a".toClass().method { name = "a" }.hook {
            after {
                val newList = ArrayList<BottomTab>()

                (result as ArrayList<*>).removeIf { tabItem ->
                    val c =
                        tabItem.javaClass.field { name = "c" }.get(tabItem).any()
                            ?: return@removeIf false
                    val tabName = c.javaClass.field { name = "b" }.get(c).string()
                    val tabScheme = c.javaClass.field { name = "d" }.get(c).string()

                    if (tabScheme != "bilibili://user_center/mine") {
                        newList.add(BottomTab(name = tabName, scheme = tabScheme))
                    }

                    if (prefs.getBoolean("dev_log_bottom_tabs")) {
                        YLog.debug("首页底部Tab: ${reflectionToString(c)}")
                    }

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

    private fun hookFeedConfig() {
        val clzIndexFragmentV2 = "com.bilibili.pegasus.promo.index.IndexFeedFragmentV2".toClass()

        val clzConfig = "com.bilibili.pegasus.api.modelv2.Config".toClass()
        val fieldFeedTopClean = clzConfig.field { name = "feedTopClean" }
        val fieldRatio = clzConfig.field { name = "smallCoverWhRatio" }

        clzIndexFragmentV2.method { param(clzConfig) }
            .hook {
                before {
                    val cfg = args[0]
                    if (prefs.getBoolean("home_disable_feed_top_clean")) {
                        fieldFeedTopClean.get(cfg).set(0)
                    }
                    if (prefs.getBoolean("home_dense_vid_card")) {
                        fieldRatio.get(cfg).set(1.77777777f)
                    }
                }
            }
    }

    private fun hookIndexFeedList() {
        val clzJsonArray = "com.alibaba.fastjson.JSONArray".toClass()

        val clzBasicIndexItem = "com.bilibili.pegasus.api.model.BasicIndexItem".toClass()
        val fieldCardGoto = clzBasicIndexItem.field { name = "cardGoto" }
        val fieldPlayerArgs = clzBasicIndexItem.field { name = "playerArgs" }
        val fieldArgs = clzBasicIndexItem.field { name = "args" }

        val clzPlayerArgs = "com.bilibili.app.comm.list.common.api.model.PlayerArgs".toClass()
        val fieldDuration = clzPlayerArgs.field { name = "fakeDuration" }

        val clzArgs = "com.bilibili.pegasus.api.modelv2.Args".toClass()
        val fieldUpName = clzArgs.field { name = "upName" }
        val fieldUpId = clzArgs.field { name = "upId" }

        val clzSmallCoverV2Item = "com.bilibili.pegasus.api.modelv2.SmallCoverV2Item".toClass()
        val fieldRcmdReason = clzSmallCoverV2Item.field { name = "rcmdReason" }
        val fieldGotoIcon = clzSmallCoverV2Item.field { name = "storyCardIcon" }
        val fieldDescButton = clzSmallCoverV2Item.field { name = "descButton" }

        val clzDescButton = "com.bilibili.pegasus.api.modelv2.DescButton".toClass()
        val cntrDescButton = clzDescButton.constructor { paramCount(0) }
        val fieldText = clzDescButton.field { name = "text" }
        val fieldUri = clzDescButton.field { name = "uri" }

        "com.bilibili.pegasus.api.BaseTMApiParser".toClass()
            .method {
                param(clzJsonArray)
                returnType = ArrayList::class.java
            }
            .hook {
                after {
                    val keepOnlyUgc = prefs.getBoolean("home_show_only_ugc")
                    val durationMin = prefs.getInt("home_duration_min", 0)

                    if (keepOnlyUgc || durationMin > 0) {
                        (result as ArrayList<*>).removeIf {
                            if (keepOnlyUgc && fieldCardGoto.get(it).string() != "av") {
                                if (prefs.getBoolean("dev_log_feed_removal")) {
                                    YLog.debug("feed item removed because it's not ugc: ${reflectionToString(it)}")
                                }
                                return@removeIf true
                            }
                            if (durationMin > 0) {
                                val playerArgs =
                                    fieldPlayerArgs.get(it).any()
                                        ?: return@removeIf false
                                val duration =
                                    fieldDuration.get(playerArgs).int()
                                if (duration < durationMin) {
                                    if (prefs.getBoolean("dev_log_feed_removal")) {
                                        YLog.debug("feed item removed because it's too short: ${reflectionToString(it)}")
                                    }
                                    return@removeIf true
                                }
                            }
                            false
                        }
                    }

                    val pureVidCard = prefs.getBoolean("home_pure_vid_card")
                    if (pureVidCard) {
                        (result as List<*>).forEach {
                            if (clzSmallCoverV2Item.isInstance(it)) {
                                fieldRcmdReason.get(it).setNull()
                                fieldGotoIcon.get(it).setNull()
                                val args = fieldArgs.get(it).any()
                                val upName = fieldUpName.get(args).string()
                                val upId = fieldUpId.get(args).long()
                                val descButton = fieldDescButton.get(it)
                                if (descButton.any() == null) {
                                    val newBtn = cntrDescButton.give()?.newInstance()
                                    fieldText.get(newBtn).set(upName)
                                    fieldUri.get(newBtn).set("bilibili://space/$upId")
                                    descButton.set(newBtn)
                                }
                            }
                        }
                    }
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
