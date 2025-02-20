package top.trangle.mbga.hook

import android.content.Context
import android.view.View
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import top.trangle.mbga.R
import top.trangle.mbga.utils.MyHooker

const val SEARCH_URI = "bilibili://search"
const val IM_URI = "activity://link/im-home"

object MineViewHooker : MyHooker() {
    override fun onHook() {
        subHook(this::hookMoreServiceMenu)
        subHook(this::hookFragmentResume)
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
                    val addSearch = prefs.getBoolean("mine_add_search")
                    val addIm = prefs.getBoolean("mine_add_im")
                    if (!(addSearch || addIm)) {
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
                        val newList = ArrayList<Any?>()
                        if (addSearch) {
                            newList.add(
                                ctorMenuGroupItem.get().call().also { item ->
                                    fieldIconResId.get(item).set(R.drawable.ic_search_pink)
                                    fieldTitle.get(item)
                                        .set(ctx.resources.getString(R.string.common_search))
                                    fieldUri.get(item).set(SEARCH_URI)
                                    fieldVisible.get(item).set(1)
                                },
                            )
                        }
                        if (addIm) {
                            newList.add(
                                ctorMenuGroupItem.get().call().also { item ->
                                    fieldIconResId.get(item).set(R.drawable.ic_im_pink)
                                    fieldTitle.get(item)
                                        .set(ctx.resources.getString(R.string.common_im))
                                    fieldUri.get(item).set(IM_URI)
                                    fieldVisible.get(item).set(1)
                                },
                            )
                        }

                        newList.addAll(
                            list,
                        )
                        fieldItemList.get(menuGroup).set(newList)
                    }
                }
            }
    }

    private fun hookFragmentResume() {
        val clzMineVipEntranceView = "tv.danmaku.bili.ui.main2.mine.widgets.MineVipEntranceView".toClass()

        val clzHomeUserCenterFragment = "tv.danmaku.bili.ui.main2.mine.HomeUserCenterFragment".toClass()
        val fieldMineVipEntranceView = clzHomeUserCenterFragment.field { type = clzMineVipEntranceView }

        clzHomeUserCenterFragment.method {
            name = "onResume"
        }.hook {
            before {
                if (prefs.getBoolean("mine_remove_vip")) {
                    val vipView = fieldMineVipEntranceView.get(instance).any() as View
                    vipView.visibility =
                        if (prefs.getBoolean("mine_keep_vip_space")) {
                            View.INVISIBLE
                        } else {
                            View.GONE
                        }
                }
            }
        }
    }
}
