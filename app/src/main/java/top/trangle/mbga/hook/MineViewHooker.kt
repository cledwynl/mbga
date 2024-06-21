package top.trangle.mbga.hook

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import top.trangle.mbga.R
import top.trangle.mbga.utils.subHook

const val SEARCH_URI = "bilibili://search"

object MineViewHooker : YukiBaseHooker() {
    override fun onHook() {
        subHook(this::hookMoreServiceMenu)
    }

    private fun hookMoreServiceMenu() {
        val clzMenuGroup = "com.bilibili.lib.homepage.mine.MenuGroup".toClass()
        val fieldStyle = clzMenuGroup.field { name = "style" }
        val fieldItemList = clzMenuGroup.field { name = "itemList" }

        val clzMenuGroupItem = "com.bilibili.lib.homepage.mine.MenuGroup\$Item".toClass()
        val ctorMenuGroupItem = clzMenuGroupItem.constructor()
        val fieldIconResId = clzMenuGroupItem.field { name = "iconResId" }
        val fieldTitle = clzMenuGroupItem.field { name = "title" }
        val fieldUri = clzMenuGroupItem.field { name = "uri" }
        val fieldVisible = clzMenuGroupItem.field { name = "visible" }

        val clzAccountMine = "tv.danmaku.bili.ui.main2.api.AccountMine".toClass()

        "tv.danmaku.bili.ui.main2.mine.HomeUserCenterFragment".toClass().method {
            param(android.content.Context::class.java, java.util.List::class.java, clzAccountMine)
        }
            .hook {
                before {
                    if (!prefs.getBoolean("mine_add_search")) {
                        return@before
                    }
                    val ctx = args[0] as Context
                    (args[1] as List<*>).forEach { menuGroup ->
                        if (fieldStyle.get(menuGroup).int() != 2) {
                            return@forEach
                        }
                        @Suppress("UNCHECKED_CAST")
                        val list =
                            fieldItemList.get(menuGroup).any() as? ArrayList<Any?> ?: return@forEach
                        if (fieldUri.get(list[0]).string() == SEARCH_URI) {
                            return@forEach
                        }
                        val newList =
                            arrayListOf(
                                ctorMenuGroupItem.get().call().also { item ->
                                    fieldIconResId.get(item).set(R.drawable.ic_search_pink)
                                    fieldTitle.get(item)
                                        .set(ctx.resources.getString(R.string.common_search))
                                    fieldUri.get(item).set(SEARCH_URI)
                                    fieldVisible.get(item).set(1)
                                },
                            )
                        newList.addAll(
                            list,
                        )
                        fieldItemList.get(menuGroup).set(newList)
                    }
                }
            }
    }
}
