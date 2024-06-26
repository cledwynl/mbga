package top.trangle.mbga.utils

import androidx.core.content.pm.PackageInfoCompat
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog

abstract class MyHooker : YukiBaseHooker() {
    fun subHook(block: () -> Unit) {
        runCatching(block).onFailure { e -> YLog.error(e.toString()) }
    }

    fun versionSpecifiedSubHook(
        block: () -> Unit,
        versionRange: LongRange,
    ) {
        val pkgInfo =
            appContext?.packageManager?.getPackageInfo(packageName, 0)
                ?: return YLog.error("Unable to get host info, versionSpecifiedSubHook not hooked")
        val version = PackageInfoCompat.getLongVersionCode(pkgInfo)
        if (version in versionRange) {
            YLog.debug("loading version specified hook: $block")
            runCatching(block).onFailure { e -> YLog.error(e.toString()) }
        }
    }
}
