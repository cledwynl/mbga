package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.core.api.priority.YukiHookPriority
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method

object VideoCommentHooker : YukiBaseHooker() {
    override fun onHook() {
        hookCommentClick()
        hookVote()
        hookFollow()
        hookUrls()
    }

    private fun hookCommentClick() {
        val clzCommentMessageWidget =
            "com.bilibili.app.comm.comment2.phoenix.view.CommentMessageWidget".toClass()
        val onClick = clzCommentMessageWidget.method { name = "q3" }

        onClick.hook {
            replaceUnit {
                if (!prefs.getBoolean("vid_comment_no_quick_reply")) {
                    callOriginal()
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
