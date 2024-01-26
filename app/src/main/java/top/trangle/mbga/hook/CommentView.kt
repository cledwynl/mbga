import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object CommentViewHooker : YukiBaseHooker() {
    override fun onHook() {
        val CommentMessageWidget =
                "com.bilibili.app.comm.comment2.phoenix.view.CommentMessageWidget".toClass()
        val onClick = CommentMessageWidget.method { name = "q3" }

        onClick.hook { intercept() }
    }
}
