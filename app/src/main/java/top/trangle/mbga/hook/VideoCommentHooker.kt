package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.core.api.priority.YukiHookPriority
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object VideoCommentHooker : YukiBaseHooker() {
    override fun onHook() {
        hookCommentClick()
        hookVote()
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
}
