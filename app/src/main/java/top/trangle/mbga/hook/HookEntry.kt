package top.trangle.mbga.hook

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
            isEnableHookSharedPreferences = true
        }

    override fun onHook() =
        encase {
            loadApp(name = "com.bilibili.app.in") {
                loadHooker(VideoPlayerHooker)
                loadHooker(VideoDetailHooker)
                loadHooker(SettingEntryHooker)
                loadHooker(VideoCommentHooker)
                loadHooker(HomeViewHooker)
                loadHooker(SearchViewHooker)
            }
        }
}
