package top.trangle.mbga.hook

import CommandDmsHooker
import CommentViewHooker
import HomeViewHooker
import SearchViewHooker
import SettingEntryHooker
import VideoDescLabelHooker
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {
    override fun onInit() =
        configs {
            debugLog {
                tag = "MBGA"
                isEnable = true
            }
        }

    override fun onHook() =
        encase {
            loadApp(name = "com.bilibili.app.in") {
                loadHooker(CommandDmsHooker)
                loadHooker(VideoDescLabelHooker)
                loadHooker(SettingEntryHooker)
                loadHooker(CommentViewHooker)
                loadHooker(HomeViewHooker)
                loadHooker(SearchViewHooker)
            }
        }
}
