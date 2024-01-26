package top.trangle.mbga.utils

import java.lang.reflect.Modifier
import java.util.LinkedList

fun reflectionToString(
    obj: Any?,
    indent: Int,
): String {
    if (obj == null) {
        return "null"
    }
    val os = obj.toString()
    if (!(os.startsWith("#") || os.startsWith("tv") || os.startsWith("com.bapis"))) {
        return os
    }
    val s = LinkedList<String>()
    var clz: Class<in Any>? = obj.javaClass
    while (clz != null) {
        for (prop in clz.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }) {
            prop.isAccessible = true
            var str = "  ".repeat(indent + 1) + "${prop.name} -> "
            var v = prop.get(obj)?.toString()?.trim() ?: "null"
            if (!(v.startsWith("#") || v.startsWith("tv") || v.startsWith("com.bapis"))) {
                str += v
            } else {
                str += reflectionToString(prop.get(obj), indent + 1)
            }
            s += str
        }
        clz = clz.superclass
    }
    return "${obj.javaClass.simpleName} {\n${s.joinToString(",\n")}\n${"  ".repeat(indent)}}"
}

fun reflectionToString(obj: Any?): String {
    return reflectionToString(obj, 0)
}
