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
}
