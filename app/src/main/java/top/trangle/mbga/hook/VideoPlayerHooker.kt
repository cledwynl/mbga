package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.core.api.priority.YukiHookPriority
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import top.trangle.mbga.utils.MyHooker

object VideoPlayerHooker : MyHooker() {
    override fun onHook() {
        subHook(this::hookOldVersion)
        subHook(this::hookNewVersion)
        subHook(this::hookDmReply)
        subHook(this::hookSegmentedSection)
        subHook(this::hookMultiWindowFullscreen)
        subHook(this::hookPortraitVideo)
        subHook(this::hookDmClick)
    }

    private fun hookOldVersion() {
        val clzViewProgressReply = "com.bapis.bilibili.app.view.v1.ViewProgressReply".toClass()
        val getVideoGuide = clzViewProgressReply.method { name = "getVideoGuide" }

        val clzVideoGuideType = "com.bapis.bilibili.app.view.v1.VideoGuide".toClass()
        val clearAttention = clzVideoGuideType.method { name = "clearAttention" }
        val clearCardsSecond = clzVideoGuideType.method { name = "clearCardsSecond" }
        val clearCommandDms = clzVideoGuideType.method { name = "clearCommandDms" }
        val clearContractCard = clzVideoGuideType.method { name = "clearContractCard" }
        val clearOperationCard = clzVideoGuideType.method { name = "clearOperationCard" }
        val clearOperationCardNew = clzVideoGuideType.method { name = "clearOperationCardNew" }

        getVideoGuide.hook {
            after {
                if (!prefs.getBoolean("vid_player_disable_command_dms")) {
                    return@after
                }
                YLog.debug("before: $result")
                clearAttention.get(result).call()
                clearCardsSecond.get(result).call()
                clearCommandDms.get(result).call()
                clearContractCard.get(result).call()
                clearOperationCard.get(result).call()
                clearOperationCardNew.get(result).call()
                YLog.debug("after: $result")
            }
        }
    }

    private fun hookNewVersion() {
        val clzDmViewReply = "com.bapis.bilibili.community.service.dm.v1.DmViewReply".toClass()
        val getCommand = clzDmViewReply.method { name = "getCommand" }

        val clzCommand = "com.bapis.bilibili.community.service.dm.v1.Command".toClass()
        val clearCommandDms = clzCommand.method { name = "clearCommandDms" }

        getCommand.hook {
            after {
                if (!prefs.getBoolean("vid_player_disable_command_dms")) {
                    return@after
                }
                YLog.debug("new before: $result")
                clearCommandDms.get(result).call()
                YLog.debug("new after: $result")
            }
        }
    }

    private fun hookDmReply() {
        val clzDmViewReply = "com.bapis.bilibili.community.service.dm.v1.DmViewReply".toClass()
        val clearActivityMeta = clzDmViewReply.method { name = "clearActivityMeta" }

        "com.bapis.bilibili.community.service.dm.v1.DmMossKtxKt\$suspendDmView\$\$inlined\$suspendCall\$1"
            .toClass()
            .method { name = "onNext" }
            .hook {
                before {
                    if (!prefs.getBoolean("vid_player_disable_activity_meta")) {
                        return@before
                    }
                    clearActivityMeta.get(args[0]).call()
                }
            }
    }

    private fun hookSegmentedSection() {
        val clzSpecificPlayConfig =
            "com.bapis.bilibili.app.distribution.setting.play.SpecificPlayConfig".toClass()

        val getEnableSegmentedSection =
            clzSpecificPlayConfig.method { name = "getEnableSegmentedSection" }
        val fieldEnableSegmentedSection =
            clzSpecificPlayConfig.field { name = "enableSegmentedSection_" }

        getEnableSegmentedSection.hook {
            before {
                if (!prefs.getBoolean("vid_player_disable_segmented_section")) {
                    YLog.debug("getEnableSegmentedSection inactive")
                    return@before
                }
                YLog.debug("getEnableSegmentedSection active")
                fieldEnableSegmentedSection.get(instance).set(null)
            }
        }
    }

    private fun hookMultiWindowFullscreen() {
        "android.app.Activity".toClass().method { name = "isInMultiWindowMode" }.hook {
            after {
                if (!prefs.getBoolean("vid_player_fullscreen_when_multi_window")) {
                    return@after
                }
                val callStack = Throwable().stackTrace
                if (callStack.any { it.className.contains("GeminiPlayerFullscreenWidget") }) {
                    resultFalse()
                } else if (callStack.any { it.className.contains("PlayerFullscreenWidget") }) {
                    resultFalse()
                }
            }
        }
    }

    private fun hookPortraitVideo() {
        val clzStoryEntrance = "com.bapis.bilibili.app.viewunite.v1.StoryEntrance".toClass()
        val methods =
            arrayOf(
                "getPlayStory",
                "getArcPlayStory",
                "getArcLandscapeStory",
            )

        methods.forEach {
            clzStoryEntrance.method { name = it }.hook {
                after {
                    if (prefs.getBoolean("vid_player_disable_portrait")) {
                        resultFalse()
                    }
                }
            }
        }
    }

    private val dmClickHook: (YukiMemberHookCreator.MemberHookCreator) -> Unit = {
        it.before {
            if (prefs.getBoolean("vid_player_disable_dm_click")) {
                args[0] = floatArrayOf(-1f, -1f)
            }
        }
    }

    private fun hookDmClick() {
        "tv.danmaku.biliplayerv2.service.interact.biz.chronos.chronosrpc.methods.send.GestureEventReceived\$Request"
            .toClass()
            .method { name = "setLocation" }
            .hook(YukiHookPriority.DEFAULT, dmClickHook)

        "tv.danmaku.biliplayerv2.service.interact.biz.chronos.chronosrpc.methods.send.TouchEventReceive\$Request"
            .toClass()
            .method { name = "setLocation" }
            .hook(YukiHookPriority.DEFAULT, dmClickHook)
    }
}
