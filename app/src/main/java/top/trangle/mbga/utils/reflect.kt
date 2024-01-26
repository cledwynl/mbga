import java.lang.reflect.Modifier
import java.util.LinkedList

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
