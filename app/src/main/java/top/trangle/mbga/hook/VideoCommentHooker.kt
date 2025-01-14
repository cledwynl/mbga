package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.core.api.priority.YukiHookPriority
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import top.trangle.mbga.BILI_IN_VER_3_18_2
import top.trangle.mbga.BILI_IN_VER_3_19_0
import top.trangle.mbga.BILI_IN_VER_3_19_1
import top.trangle.mbga.BILI_IN_VER_3_19_2
import top.trangle.mbga.BILI_IN_VER_3_20_0
import top.trangle.mbga.BILI_IN_VER_3_20_1
import top.trangle.mbga.utils.MyHooker

object VideoCommentHooker : MyHooker() {
    override fun onHook() {
        versionSpecifiedSubHook(this::hookCommentClickV1, Long.MIN_VALUE..BILI_IN_VER_3_18_2)
        versionSpecifiedSubHook(this::hookCommentClickV2, BILI_IN_VER_3_19_0..BILI_IN_VER_3_19_0)
        versionSpecifiedSubHook(this::hookCommentClickV3, BILI_IN_VER_3_19_1..BILI_IN_VER_3_19_2)
        versionSpecifiedSubHook(this::hookCommentClickV4, BILI_IN_VER_3_20_0..BILI_IN_VER_3_20_0)
        versionSpecifiedSubHook(this::hookCommentClickV5, BILI_IN_VER_3_20_1..Long.MAX_VALUE)
        subHook(this::hookTopVote)
        versionSpecifiedSubHook(this::hookStandVoteV1, Long.MIN_VALUE..BILI_IN_VER_3_18_2)
        versionSpecifiedSubHook(this::hookStandVoteV2, BILI_IN_VER_3_19_0..Long.MAX_VALUE)
        versionSpecifiedSubHook(this::hookFollowV1, Long.MIN_VALUE..BILI_IN_VER_3_18_2)
        versionSpecifiedSubHook(this::hookFollowV2, BILI_IN_VER_3_19_0..Long.MAX_VALUE)
        subHook(this::hookUrls)
        versionSpecifiedSubHook(this::hookEmptyPageV1, Long.MIN_VALUE..BILI_IN_VER_3_18_2)
        versionSpecifiedSubHook(this::hookEmptyPageV2, BILI_IN_VER_3_19_0..Long.MAX_VALUE)
        subHook(this::hookMainList)
        subHook(this::hookCommentProvider)
        subHook(this::hookGetReplies)
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
            .method {
                name = "N2"
                returnType = UnitType
            } // NOTE: 更新后容易失效的
            .hook {
                replaceUnit {
                    if (!prefs.getBoolean("vid_comment_no_quick_reply")) {
                        callOriginal()
                    } else if (!args[0].toString().startsWith("ShowPublishDialog")) {
                        callOriginal()
                    } else {
                        val allowClickFrom =
                            arrayOf(
                                "CommentActionBarHandler",
                                "CommentViewModel\$dispatchAction\$1",
                                "CommentMainLayer",
                                "CommentDetailLayer",
                            )
                        val isAllowed =
                            Throwable().stackTrace.any { st ->
                                allowClickFrom.any { fr ->
                                    st.className.contains(fr)
                                }
                            }
                        if (isAllowed) {
                            callOriginal()
                        }
                    }
                }
            }
    }

    /** 3.19.1, 3.19.2 可用 */
    private fun hookCommentClickV3() {
        "com.bilibili.app.comment3.viewmodel.CommentViewModel".toClass()
            .method {
                name = "M2"
                returnType = UnitType
            } // NOTE: 更新后容易失效的
            .hook {
                replaceUnit {
                    if (!prefs.getBoolean("vid_comment_no_quick_reply")) {
                        callOriginal()
                    } else if (!args[0].toString().startsWith("ShowPublishDialog")) {
                        callOriginal()
                    } else {
                        val allowClickFrom =
                            arrayOf(
                                "CommentActionBarHandler",
                                "CommentViewModel\$dispatchAction\$1",
                                "CommentMainLayer",
                                "CommentDetailLayer",
                            )
                        val isAllowed =
                            Throwable().stackTrace.any { st ->
                                allowClickFrom.any { fr ->
                                    st.className.contains(fr)
                                }
                            }
                        if (isAllowed) {
                            callOriginal()
                        }
                    }
                }
            }
    }

    /** 3.20.0 可用 */
    private fun hookCommentClickV4() {
        "com.bilibili.app.comment3.viewmodel.CommentViewModel".toClass()
            .method {
                name = "l3"
                returnType = UnitType
            } // NOTE: 更新后容易失效的
            .hook {
                replaceUnit {
                    if (!prefs.getBoolean("vid_comment_no_quick_reply")) {
                        callOriginal()
                    } else if (!args[0].toString().startsWith("ShowPublishDialog")) {
                        callOriginal()
                    } else {
                        YLog.debug(Throwable().stackTraceToString())
                        val allowClickFrom =
                            arrayOf(
                                // 得放，不然同一个评论，第二次点击回复会没反应
                                "CommentViewModel\$dispatchAction\$1",
                                // 底部
                                "CommentMainLayer",
                                // 楼中楼底部
                                "CommentDetailLayer",
                                // 回复按钮
                                "CommentContentHolder.p4",
                                // 长按或点击点点点弹出选择回复
                                "CommentMoreMenuDialog",
                            )
                        val isAllowed =
                            Throwable().stackTrace.any { st ->
                                allowClickFrom.any { fr ->
                                    "${st.className}.${st.methodName}".contains(fr)
                                }
                            }
                        if (isAllowed) {
                            callOriginal()
                        }
                    }
                }
            }
    }

    /** 3.20.1 可用 */
    private fun hookCommentClickV5() {
        "com.bilibili.app.comment3.viewmodel.CommentViewModel".toClass()
            .method {
                name = "k3"
                returnType = UnitType
            } // NOTE: 更新后容易失效的
            .hook {
                replaceUnit {
                    if (!prefs.getBoolean("vid_comment_no_quick_reply")) {
                        callOriginal()
                    } else if (!args[0].toString().startsWith("ShowPublishDialog")) {
                        callOriginal()
                    } else {
                        YLog.debug(Throwable().stackTraceToString())
                        val allowClickFrom =
                            arrayOf(
                                // 得放，不然同一个评论，第二次点击回复会没反应
                                "CommentViewModel\$dispatchAction\$1",
                                // 底部
                                "CommentMainLayer",
                                // 楼中楼底部
                                "CommentDetailLayer",
                                // 回复按钮
                                "CommentContentHolder.p4",
                                // 长按或点击点点点弹出选择回复
                                "CommentMoreMenuDialog",
                            )
                        val isAllowed =
                            Throwable().stackTrace.any { st ->
                                allowClickFrom.any { fr ->
                                    "${st.className}.${st.methodName}".contains(fr)
                                }
                            }
                        if (isAllowed) {
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

    /** 3.19.0, 3.19.1, 3.19.2 可用 */
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

    private fun hookEmptyPageV1() {
        val clzEmptyPage = "com.bapis.bilibili.main.community.reply.v1.EmptyPage".toClass()
        val defaultEmptyPage = clzEmptyPage.field { name = "DEFAULT_INSTANCE" }

        "com.bapis.bilibili.main.community.reply.v1.SubjectControl".toClass()
            .method {
                name = "getEmptyPage"
            }.hook {
                after {
                    if (prefs.getBoolean("vid_comment_no_empty_page")) {
                        result = defaultEmptyPage.get().any()
                    }
                }
            }
    }

    private fun hookEmptyPageV2() {
        val clzEmptyPage = "com.bapis.bilibili.main.community.reply.v2.EmptyPage".toClass()
        val defaultEmptyPage = clzEmptyPage.field { name = "DEFAULT_INSTANCE" }

        "com.bapis.bilibili.main.community.reply.v2.SubjectDescriptionReply".toClass()
            .method {
                name = "getEmptyPage"
            }.hook {
                after {
                    if (prefs.getBoolean("vid_comment_no_empty_page")) {
                        result = defaultEmptyPage.get().any()
                    }
                }
            }
    }

    private fun hookMainList() {
        val clzDmViewReply = "com.bapis.bilibili.main.community.reply.v1.MainListReply".toClass()
        val clearQoe = clzDmViewReply.method { name = "clearQoe" }
        val clearOperation = clzDmViewReply.method { name = "clearOperation" }
        val clearOperationV2 = clzDmViewReply.method { name = "clearOperationV2" }

        "com.bapis.bilibili.main.community.reply.v1.ReplyMossKtxKt\$suspendMainList\$\$inlined\$suspendCall\$1"
            .toClass()
            .method { name = "onNext" }
            .hook {
                before {
                    if (prefs.getBoolean("dev_log_main_list")) {
                        YLog.debug(args[0].toString())
                    }
                    if (prefs.getBoolean("vid_comment_no_qoe")) {
                        clearQoe.get(args[0]).call()
                    }
                    if (prefs.getBoolean("vid_comment_no_operation")) {
                        clearOperation.get(args[0]).call()
                        clearOperationV2.get(args[0]).call()
                    }
                }
            }
    }

    private fun hookCommentProvider() {
        "com.bilibili.ship.theseus.united.page.tab.TheseusTabPagerService".toClass()
            .constructor()
            .give()?.let { ctor ->
                ctor.parameters[6].type.constructor().hook {
                    before {
                        if (!prefs.getBoolean("vid_comment_disable")) {
                            return@before
                        }
                        args[0] =
                            (args[0] as List<*>).filter { listItem ->
                                listItem != null &&
                                    !listItem.javaClass.typeName.contains("CommentTabPageProvider")
                            }
                    }
                }
            }
    }

    private fun hookGetReplies() {
        val clzReplyInfo = "com.bapis.bilibili.main.community.reply.v1.ReplyInfo".toClass()
        val fieldContent = clzReplyInfo.field { name = "content_" }

        val clzContent = "com.bapis.bilibili.main.community.reply.v1.Content".toClass()
        val fieldMessage = clzContent.field { name = "message_" }

        "com.bapis.bilibili.main.community.reply.v1.MainListReply".toClass()
            .method { name = "getRepliesList" }
            .hook {
                after {
                    val keywords = prefs.getStringSet("vid_comment_filter_keyword")
                    if (keywords.isNotEmpty()) {
                        val replies = arrayListOf(*(result as List<*>).toTypedArray())
                        replies.removeIf { reply ->
                            val content = fieldContent.get(reply).any()
                            val message = fieldMessage.get(content).string()
                            keywords.any { keyword ->
                                message.contains(keyword)
                            }
                        }
                        result = replies
                    }
                }
            }
    }
}
