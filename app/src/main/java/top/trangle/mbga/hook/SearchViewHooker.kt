package top.trangle.mbga.hook

import com.highcapable.yukihookapi.hook.factory.method
import top.trangle.mbga.utils.MyHooker

object SearchViewHooker : MyHooker() {
    override fun onHook() {
        subHook(this::hookSearchType)
        subHook(this::hookDefaultSearchWords)
    }

    private fun hookSearchType() {
        "com.bilibili.search2.api.SearchSquareType".toClass().method { name = "getType" }.hook {
            /*
             * 这个钩子基于这个方法的实现：com.bilibili.search2.discover.l$a.d7
             * 它会根据SearchSquareType.getType的返回值将数据展示到UI对应区域上，
             * 如果getType返回了它不认识的类型，会直接丢弃。所以这里遇到不想要的类型直接返回空就行
             */

            after {
                if (prefs.getBoolean("search_disable_$result")) {
                    resultNull()
                }
            }
        }
    }

    private fun hookDefaultSearchWords() {
        "com.bapis.bilibili.app.interfaces.v1.SearchMoss".toClass()
            .method { name = "defaultWords" }
            .hook {
                replaceAny {
                    if (!prefs.getBoolean("search_disable_default_words")) {
                        callOriginal()
                    } else {
                        null
                    }
                }
            }
    }
}
