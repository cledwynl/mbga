package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.core.api.priority.YukiHookPriority
import com.highcapable.yukihookapi.hook.core.finder.members.FieldFinder
import com.highcapable.yukihookapi.hook.core.finder.members.MethodFinder
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import top.trangle.mbga.BILI_IN_VER_3_18_2
import top.trangle.mbga.BILI_IN_VER_3_19_0
import top.trangle.mbga.BILI_IN_VER_3_19_1
import top.trangle.mbga.utils.MyHooker

object VideoDetailHooker : MyHooker() {
    override fun onHook() {
        versionSpecifiedSubHook(this::hookLabelV1, Long.MIN_VALUE..BILI_IN_VER_3_18_2)
        versionSpecifiedSubHook(this::hookLabelV2, BILI_IN_VER_3_19_0..Long.MAX_VALUE)
        versionSpecifiedSubHook(this::hookShareLinkV1, Long.MIN_VALUE..BILI_IN_VER_3_18_2)
        versionSpecifiedSubHook(this::hookShareLinkV2, BILI_IN_VER_3_19_0..BILI_IN_VER_3_19_0)
        versionSpecifiedSubHook(this::hookShareLinkV3, BILI_IN_VER_3_19_1..Long.MAX_VALUE)
    }

    /** 3.18.2 可用 */
    private fun hookLabelV1() {
        val clzLabel = "tv.danmaku.bili.videopage.data.view.model.BiliVideoDetail\$Label".toClass()
        val clzDescSection = "tv.danmaku.bili.ui.video.section.info.DescSection".toClass()
        val getLabelFromDesc =
            clzDescSection.method {
                emptyParam()
                returnType = clzLabel
            }

        getLabelFromDesc.hook {
            after {
                if (prefs.getBoolean("vid_detail_disable_label")) {
                    resultNull()
                }
            }
        }
    }

    /** 3.19.0, 3.19.1, 3.19.2 可用 */
    private fun hookLabelV2() {
        val clzLabel = "com.bapis.bilibili.app.viewunite.common.Label".toClass()
        val defaultLabel = clzLabel.field { name = "DEFAULT_INSTANCE" }

        val clzHeadline = "com.bapis.bilibili.app.viewunite.common.Headline".toClass()
        clzHeadline.method { name = "getLabel" }
            .hook {
                after {
                    if (prefs.getBoolean("vid_detail_disable_label")) {
                        result = defaultLabel.get().any()
                    }
                }
            }
    }

    private fun generateShareLinkHook(
        clzShareResult: Class<*>,
        contentField: FieldFinder.Result,
        shareTaskCallback: MethodFinder.Result,
    ): (YukiMemberHookCreator.MemberHookCreator) -> Unit =
        {
            it.replaceUnit {
                if (!prefs.getBoolean("vid_detail_disable_short_link")) {
                    callOriginal()
                    return@replaceUnit
                }
                val result = clzShareResult.getDeclaredConstructor().newInstance()
                val shareId = args[1] as String
                val shareObjId = args[2] as String
                val shareChannel = args[5] as String
                if (shareChannel != "COPY") {
                    callOriginal()
                    return@replaceUnit
                }
                val link =
                    when {
                        shareId.startsWith("main.") -> "https://b23.tv/av$shareObjId"
                        shareId.startsWith("dt.") -> "https://m.bilibili.com/opus/$shareObjId"
                        shareId.startsWith("live.") -> "https://live.bilibili.com/$shareObjId"
                        else -> {
                            YLog.info("shareId not supported: $shareId")
                            callOriginal()
                            return@replaceUnit
                        }
                    }
                contentField.get(result).set(link)
                shareTaskCallback.get(args[17]).invoke<Any>(result)
            }
        }

    /** 3.18.2 可用 */
    private fun hookShareLinkV1() {
        val clzShareResult = "com.bilibili.lib.sharewrapper.online.api.ShareClickResult".toClass()
        val contentField = clzShareResult.field { name = "content" }

        val clzShareTargetTask =
            "com.bilibili.app.comm.supermenu.share.v2.ShareTargetTask\$f".toClass()
        val shareTaskCallback = clzShareTargetTask.method { name = "l" }

        ("ah1.c".toClassOrNull() ?: return).method { name = "c" }.hook(
            YukiHookPriority.DEFAULT,
            generateShareLinkHook(
                clzShareResult,
                contentField,
                shareTaskCallback,
            ),
        )
    }

    /** 3.19.0 可用 */
    private fun hookShareLinkV2() {
        val clzShareResult = "com.bilibili.lib.sharewrapper.online.api.ShareClickResult".toClass()
        val contentField = clzShareResult.field { name = "content" }

        // NOTE: 更新后容易失效的
        val clzShareTargetTask =
            "com.bilibili.app.comm.supermenu.share.v2.ShareTargetTask\$f".toClass()
        val shareTaskCallback = clzShareTargetTask.method { name = "l" }

        ("com.bilibili.lib.sharewrapper.online.api.b".toClassOrNull() ?: return).method {
            name = "g"
        }.hook(
            YukiHookPriority.DEFAULT,
            generateShareLinkHook(
                clzShareResult,
                contentField,
                shareTaskCallback,
            ),
        )
    }

    /** 3.19.1, 3.19.2 可用 */
    private fun hookShareLinkV3() {
        val clzShareResult = "com.bilibili.lib.sharewrapper.online.api.ShareClickResult".toClass()
        val contentField = clzShareResult.field { name = "content" }

        // NOTE: 更新后容易失效的
        val clzShareTargetTask =
            "com.bilibili.app.comm.supermenu.share.v2.ShareTargetTask\$f".toClass()
        val shareTaskCallback = clzShareTargetTask.method { name = "l" }

        ("com.bilibili.lib.sharewrapper.online.api.b".toClassOrNull() ?: return).method {
            name = "d"
        }.hook(
            YukiHookPriority.DEFAULT,
            generateShareLinkHook(
                clzShareResult,
                contentField,
                shareTaskCallback,
            ),
        )
    }
}
