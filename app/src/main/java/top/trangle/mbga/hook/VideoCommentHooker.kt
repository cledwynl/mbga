package top.trangle.mbga.hook

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
        "com.bilibili.app.comment.ext.widgets.CmtVoteWidget".toClass()
            .method { name = "a" }.hook {
                replaceUnit {
                    if (!prefs.getBoolean("vid_comment_no_vote")) {
                        callOriginal()
                    }
                }
            }
    }
}
