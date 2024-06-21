package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.core.api.priority.YukiHookPriority
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.ListClass
import top.trangle.mbga.utils.subHook

object VideoCommentHooker : YukiBaseHooker() {
    override fun onHook() {
        subHook(this::hookCommentClickV1)
        subHook(this::hookCommentClickV2)
        subHook(this::hookCommentClickV3)
        subHook(this::hookTopVote)
        subHook(this::hookStandVoteV1)
        subHook(this::hookStandVoteV2)
        subHook(this::hookFollowV1)
        subHook(this::hookFollowV2)
        subHook(this::hookUrls)
    }

    /** 3.18.2 可用 */
    private fun hookCommentClickV1() {
        val clzCommentMessageWidget =
            "com.bilibili.app.comm.comment2.phoenix.view.CommentMessageWidget".toClass()
        val onClick =
            clzCommentMessageWidget.method {
                modifiers { isFinal }
                param { it.size == 2 && it[1] == android.view.View::class.java }
            }

        onClick.hook {
            replaceUnit {
                if (!prefs.getBoolean("vid_comment_no_quick_reply")) {
                    callOriginal()
                }
            }
        }
    }

    /** 3.19.0 可用 */
    private fun hookCommentClickV2() {
        "com.bilibili.app.comment3.viewmodel.CommentViewModel".toClass()
            .method { name = "N2" } // TODO: N2要想办法识别出来
            .hook {
                replaceUnit {
                    if (!prefs.getBoolean("vid_comment_no_quick_reply")) {
                        callOriginal()
                    } else if (!args[0].toString().startsWith("ShowPublishDialog")) {
                        callOriginal()
                    } else {
                        val isClickingRichText =
                            Throwable().stackTrace.any {
                                it.className.contains("CommentContentRichTextHandler")
                            }
                        if (!isClickingRichText) {
                            callOriginal()
                        }
                    }
                }
            }
    }

    /** 3.19.1 可用 */
    private fun hookCommentClickV3() {
        "com.bilibili.app.comment3.viewmodel.CommentViewModel".toClass()
            .method { name = "M2" } // TODO: 要想办法识别出来
            .hook {
                replaceUnit {
                    if (!prefs.getBoolean("vid_comment_no_quick_reply")) {
                        callOriginal()
                    } else if (!args[0].toString().startsWith("ShowPublishDialog")) {
                        callOriginal()
                    } else {
                        val isClickingRichText =
                            Throwable().stackTrace.any {
                                it.className.contains("CommentContentRichTextHandler")
                            }
                        if (!isClickingRichText) {
                            callOriginal()
                        }
                    }
                }
            }
    }

    private val voteReplaceHook: (YukiMemberHookCreator.MemberHookCreator) -> Unit = {
        it.replaceUnit {
            if (!prefs.getBoolean("vid_comment_no_vote")) {
                callOriginal()
            }
        }
    }

    /** 评论区顶部的投票，3.18.2和3.19.0都可用 */
    private fun hookTopVote() {
        "com.bilibili.app.comment.ext.widgets.CmtVoteWidget".toClass()
            .method {
                modifiers { isFinal }
                param { it[0].name.startsWith("com.bilibili.app.comment.ext.model.") }
            }.hook(YukiHookPriority.DEFAULT, voteReplaceHook)
    }

    /** 评论内容中站队信息，3.18.2 可用 */
    private fun hookStandVoteV1() {
        "com.bilibili.app.comm.comment2.phoenix.view.CommentMountWidget".toClass().method {
            param { it.size == 1 && it[0].name.startsWith("com.bilibili.app.comm.comment2.comments.vvmadapter.") }
        }.hook(YukiHookPriority.DEFAULT, voteReplaceHook)
    }

    /** 评论内容中站队信息，3.19.0, 3.19.1 可用 */
    private fun hookStandVoteV2() {
        "com.bilibili.app.comment.ext.widgets.CmtMountWidget".toClass()
            .method {
                modifiers { isFinal }
                param { it.size == 2 && it[0].name.startsWith("com.bilibili.app.comment.ext.model.") }
            }.hook(YukiHookPriority.DEFAULT, voteReplaceHook)
    }

    /** 3.18.2 可用 */
    private fun hookFollowV1() {
        "com.bilibili.app.comm.comment2.phoenix.view.CommentFollowWidget".toClass()
            .method {
                param { it.size == 1 && it[0].name.startsWith("com.bilibili.app.comm.comment2.comments.vvmadapter.") }
            }.hook {
                replaceUnit {
                    if (!prefs.getBoolean("vid_comment_no_follow")) {
                        callOriginal()
                    }
                }
            }
    }

    /** 3.19.0, 3.19.1 可用 */
    private fun hookFollowV2() {
        "com.bilibili.app.comment3.ui.widget.CommentHeaderDecorativeView".toClass().method {
            param { it.size == 2 && it[0] == ListClass }
        }
            .hook {
                before {
                    if (!prefs.getBoolean("vid_comment_no_follow")) {
                        return@before
                    }
                    (args[0] as List<*>).forEach { cmtIt ->
                        cmtIt?.javaClass?.declaredFields?.forEach { field ->
                            if (field.type.name.startsWith("com.bilibili.app.comment3.data.model.CommentItem")) {
                                field.isAccessible = true
                                if (field.get(cmtIt)?.toString()?.startsWith("Follow(") == true) {
                                    field.set(cmtIt, null)
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun hookUrls() {
        val clzMapFieldLite = "com.google.protobuf.MapFieldLite".toClass()
        val methodMutableCopy = clzMapFieldLite.method { name = "mutableCopy" }

        val clzUrl = "com.bapis.bilibili.main.community.reply.v1.Url".toClass()
        val fieldAppUrlSchema = clzUrl.field { name = "appUrlSchema_" }

        "com.bapis.bilibili.main.community.reply.v1.Content".toClass()
            .method { name = "internalGetUrls" }
            .hook {
                after {
                    if (!prefs.getBoolean("vid_comment_no_search")) {
                        return@after
                    }
                    val map = methodMutableCopy.get(result).call() as LinkedHashMap<*, *>
                    map.entries.removeIf { (_, value) ->
                        fieldAppUrlSchema.get(value).string().startsWith("bilibili://search")
                    }
                    result = map
                }
            }
    }
}
