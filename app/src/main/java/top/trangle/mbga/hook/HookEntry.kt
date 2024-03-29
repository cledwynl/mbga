package top.trangle.mbga.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import top.trangle.mbga.BILI_IN_PKG_ID
import top.trangle.mbga.BuildConfig

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {
    override fun onInit() =
        configs {
            debugLog {
                tag = "MBGA"
                isEnable = true
            }
            isEnableHookSharedPreferences = true
            isEnableDataChannel = true
            isDebug = BuildConfig.DEBUG
        }

    override fun onHook() =
        encase {
            loadApp(name = BILI_IN_PKG_ID) {
                loadHooker(VideoPlayerHooker)
                loadHooker(VideoDetailHooker)
                loadHooker(SettingEntryHooker)
                loadHooker(VideoCommentHooker)
                loadHooker(HomeViewHooker)
                loadHooker(SearchViewHooker)
            }
        }
}
