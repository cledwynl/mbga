import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog

object CommandDmsHooker : YukiBaseHooker() {
    override fun onHook() {
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
}
