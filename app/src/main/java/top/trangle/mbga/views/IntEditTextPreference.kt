package top.trangle.mbga.views

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class IntEditTextPreference : EditTextPreference {
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
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        return runCatching { getPersistedInt(0).toString() }.getOrElse { "0" }
    }

    override fun persistString(value: String?): Boolean {
        return persistInt(runCatching { Integer.valueOf(value!!) }.getOrElse { 0 })
    }
}
