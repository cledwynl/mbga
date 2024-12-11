package top.trangle.mbga.views

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class KeywordListPreference : EditTextPreference {
    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) :
        super(context, attrs, defStyle) {
        setup()
    }

    private fun setup() {
        setOnBindEditTextListener {
            it.isSingleLine = false
        }
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        return runCatching { getPersistedStringSet(HashSet()).joinToString("\n") }.getOrElse {
            defaultReturnValue ?: ""
        }
    }

    override fun persistString(value: String?): Boolean {
        return persistStringSet(
            runCatching {
                value?.split("\n")?.filter {
                    it != ""
                }?.toSet()
            }.getOrElse { HashSet() },
        )
    }
}
