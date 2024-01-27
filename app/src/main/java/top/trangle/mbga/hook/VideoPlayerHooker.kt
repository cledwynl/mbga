package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog

object VideoPlayerHooker : YukiBaseHooker() {
    override fun onHook() {
        hookOldVersion()
        hookNewVersion()
        hookDmReply()
    }

    private fun hookOldVersion() {
        val ViewProgressReply = "com.bapis.bilibili.app.view.v1.ViewProgressReply".toClass()
        val getVideoGuide = ViewProgressReply.method { name = "getVideoGuide" }

        val VideoGuideType = "com.bapis.bilibili.app.view.v1.VideoGuide".toClass()
        val clearAttention = VideoGuideType.method { name = "clearAttention" }
        val clearCardsSecond = VideoGuideType.method { name = "clearCardsSecond" }
        val clearCommandDms = VideoGuideType.method { name = "clearCommandDms" }
        val clearContractCard = VideoGuideType.method { name = "clearContractCard" }
        val clearOperationCard = VideoGuideType.method { name = "clearOperationCard" }
        val clearOperationCardNew = VideoGuideType.method { name = "clearOperationCardNew" }

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
        val DmViewReply = "com.bapis.bilibili.community.service.dm.v1.DmViewReply".toClass()
        val getCommand = DmViewReply.method { name = "getCommand" }

        val Command = "com.bapis.bilibili.community.service.dm.v1.Command".toClass()
        val clearCommandDms = Command.method { name = "clearCommandDms" }

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
        val DmViewReply = "com.bapis.bilibili.community.service.dm.v1.DmViewReply".toClass()
        val clearActivityMeta = DmViewReply.method { name = "clearActivityMeta" }

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
}
