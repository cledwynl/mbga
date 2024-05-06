package top.trangle.mbga.utils

import com.highcapable.yukihookapi.hook.log.YLog

inline fun subHook(block: () -> Unit) {
    runCatching(block).onFailure { e -> YLog.error(e.toString()) }
}
