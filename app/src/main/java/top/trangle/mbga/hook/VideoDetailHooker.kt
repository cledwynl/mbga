package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import top.trangle.mbga.hook.SearchViewHooker.hook
import top.trangle.mbga.utils.subHook

object VideoDetailHooker : YukiBaseHooker() {
    override fun onHook() {
        subHook(this::hookLabel)
        subHook(this::hookLabel3d19d0)
        subHook(this::hookShareLink)
        subHook(this::hookShareLink3d19d0)
    }

    private fun hookLabel() {
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

    private fun hookLabel3d19d0() {
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

    private fun hookShareLink() {
        val clzShareResult = "com.bilibili.lib.sharewrapper.online.api.ShareClickResult".toClass()
        val contentField = clzShareResult.field { name = "content" }

        val clzShareTargetTask =
            "com.bilibili.app.comm.supermenu.share.v2.ShareTargetTask\$f".toClass()
        val shareTaskCallback = clzShareTargetTask.method { name = "l" }

        ("ah1.c".toClassOrNull() ?: return).method { name = "c" }.hook {
            replaceUnit {
                if (!prefs.getBoolean("vid_detail_disable_short_link")) {
                    callOriginal()
                    return@replaceUnit
                }
                val result = clzShareResult.getDeclaredConstructor().newInstance()
                contentField.get(result).set("https://b23.tv/av${args[2]}")
                shareTaskCallback.get(args[17]).invoke<Any>(result)
            }
        }
    }

    // TODO: 没有优雅的注入点，考虑用DexKit进行特征搜索
    private fun hookShareLink3d19d0() {
        val clzShareResult = "com.bilibili.lib.sharewrapper.online.api.ShareClickResult".toClass()
        val contentField = clzShareResult.field { name = "content" }

        val clzShareTargetTask =
            "com.bilibili.app.comm.supermenu.share.v2.ShareTargetTask\$f".toClass()
        val shareTaskCallback = clzShareTargetTask.method { name = "l" }

        ("com.bilibili.lib.sharewrapper.online.api.b".toClassOrNull() ?: return).method {
            name = "g"
        }.hook {
            replaceUnit {
                if (!prefs.getBoolean("vid_detail_disable_short_link")) {
                    callOriginal()
                    return@replaceUnit
                }
                val result = clzShareResult.getDeclaredConstructor().newInstance()
                contentField.get(result).set("https://b23.tv/av${args[2]}")
                shareTaskCallback.get(args[17]).invoke<Any>(result)
            }
        }
    }
}
