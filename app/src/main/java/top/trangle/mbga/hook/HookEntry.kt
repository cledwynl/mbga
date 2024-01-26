package top.trangle.mbga.hook

import android.app.Activity
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import java.lang.reflect.Modifier
import java.util.LinkedList

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {
    fun reflectionToString(obj: Any?): String {
        if (obj == null) {
            return "null"
        }
        val s = LinkedList<String>()
        var clz: Class<in Any>? = obj.javaClass
        while (clz != null) {
            for (prop in clz.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }) {
                prop.isAccessible = true
                s += "${prop.name}=" + prop.get(obj)?.toString()?.trim()
            }
            clz = clz.superclass
        }
        return "${obj.javaClass.simpleName}=[${s.joinToString(", ")}]"
    }

    interface VideoGuide {
        fun clearVideoGuide()
    }

    override fun onInit() = configs {
        debugLog {
            tag = "MBGA"
            isEnable = true
        }
    }

    override fun onHook() = encase {
        loadApp(name = "com.bilibili.app.in") {
            val ViewProgressReply = "com.bapis.bilibili.app.view.v1.ViewProgressReply".toClass()
            val getVideoGuide = ViewProgressReply.method { name = "getVideoGuide" }

            val VideoGuideType = "com.bapis.bilibili.app.view.v1.VideoGuide".toClass()
            val clearAttention = VideoGuideType.method { name = "clearAttention" }
            val clearCardsSecond = VideoGuideType.method { name = "clearCardsSecond" }
            val clearCommandDms = VideoGuideType.method { name = "clearCommandDms" }
            val clearContractCard = VideoGuideType.method { name = "clearContractCard" }
            val clearOperationCard = VideoGuideType.method { name = "clearOperationCard" }
            val clearOperationCardNew = VideoGuideType.method { name = "clearOperationCardNew" }

            getVideoGuide.hook {
                after {
                    YLog.debug("before: $result")
                    clearAttention.get(result).call()
                    clearCardsSecond.get(result).call()
                    clearCommandDms.get(result).call()
                    clearContractCard.get(result).call()
                    clearOperationCard.get(result).call()
                    clearOperationCardNew.get(result).call()
                    YLog.debug("after: $result")
                }
            }

            /* val Label = "com.bapis.bilibili.app.view.v1.Label".toClass()
            val getUri = Label.method { name = "getUri" }
            getUri.hook { after { YLog.debug("label: $instance") } } */

            // 干掉视频页的活动、热门
            val Label = "tv.danmaku.bili.videopage.data.view.model.BiliVideoDetail\$Label".toClass()
            val DescSection = "tv.danmaku.bili.ui.video.section.info.DescSection".toClass()
            val getLabelFromDesc =
                    DescSection.method {
                        emptyParam()
                        returnType = Label
                    }

            getLabelFromDesc.hook {
                after {
                    YLog.debug("Label: ${reflectionToString(result)}")
                    result = null
                }
            }

            val BiliPreferencesActivity =
                    "com.bilibili.app.preferences.BiliPreferencesActivity".toClass()
            val Activity = "android.app.Activity".toClass()
            val onCreate = BiliPreferencesActivity.method { name = "onCreate" }
            YLog.debug("onCreate found")
            val findViewById = Activity.method { name = "findViewById" }
            YLog.debug("findViewById found")
            onCreate.hook {
                after {
                    val toolbar: ViewGroup = (instance as Activity).findViewById(0x7f0929ab)
                    YLog.debug("view: $toolbar")
                    val lp =
                            ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                    val tv = TextView(instance as Activity)
                    tv.setText("MGBA!")
                    tv.gravity = Gravity.CENTER
                    tv.setPadding(16, 0, 16, 0)
                    toolbar.addView(tv, 2, lp)
                }
            }
        }
    }
}
