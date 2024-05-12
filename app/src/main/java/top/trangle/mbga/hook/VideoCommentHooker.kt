package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.core.api.priority.YukiHookPriority
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import top.trangle.mbga.utils.subHook

object VideoCommentHooker : YukiBaseHooker() {
    override fun onHook() {
        subHook(this::hookCommentClick3d18d2)
        subHook(this::hookCommentClick3d19d0)
        subHook(this::hookVote)
        subHook(this::hookFollow)
        subHook(this::hookUrls)
    }

    private fun hookCommentClick3d18d2() {
        val clzCommentMessageWidget =
            "com.bilibili.app.comm.comment2.phoenix.view.CommentMessageWidget".toClass()
        val onClick =
            clzCommentMessageWidget.method {
                modifiers {
                    isFinal
                }
                param {
                    it.size == 2 && it[1] == android.view.View::class.java
                }
            }

        onClick.hook {
            replaceUnit {
                if (!prefs.getBoolean("vid_comment_no_quick_reply")) {
                    callOriginal()
                }
            }
        }
    }

    private fun hookCommentClick3d19d0() {
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
                        if (isClickingRichText) {
                            return@replaceUnit
                        }
                    }
                }
            }
    }

    private fun hookVote() {
        val replaceHook: (YukiMemberHookCreator.MemberHookCreator) -> Unit = {
            it.replaceUnit {
                if (!prefs.getBoolean("vid_comment_no_vote")) {
                    callOriginal()
                }
            }
        }

        // 评论区顶部的投票
        "com.bilibili.app.comment.ext.widgets.CmtVoteWidget".toClass()
            .method { name = "a" }.hook(YukiHookPriority.DEFAULT, replaceHook)
        // 评论内容中站队信息
        "com.bilibili.app.comm.comment2.phoenix.view.CommentMountWidget".toClass()
            .method { name = "i0" }.hook(YukiHookPriority.DEFAULT, replaceHook)
    }

    private fun hookFollow() {
        "com.bilibili.app.comm.comment2.phoenix.view.CommentFollowWidget".toClass()
            .method { name = "i0" }
            .hook {
                replaceUnit {
                    if (!prefs.getBoolean("vid_comment_no_follow")) {
                        callOriginal()
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
