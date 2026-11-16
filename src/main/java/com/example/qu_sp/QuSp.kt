package com.example.qu_sp

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

public const val DEFAULT_QUSP_SCOPE: String = "qu_sp"

public object QUSP {
    public lateinit var app: Context
        private set

    internal fun initialize(context: Context) {
        app = context.applicationContext
    }
}

public class QUSPInitializer : ContentProvider() {
    override fun onCreate(): Boolean {
        QUSP.initialize(checkNotNull(context))
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}

public interface QUSPStore<T> : ReadWriteProperty<Any?, T> {
    public val key: String
    public val scope: String
}

public class QUSPIntStore(
    override val key: String,
    public val defaultValue: Int = 0,
    override val scope: String = DEFAULT_QUSP_SCOPE,
) : QUSPStore<Int> {
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        QUSP.app
            .getSharedPreferences(scope, Context.MODE_PRIVATE)
            .edit()
            .putInt(key, value)
            .apply()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int = QUSP.app
        .getSharedPreferences(scope, Context.MODE_PRIVATE)
        .getInt(key, defaultValue)
}

public class QUSPDoubleStore(
    override val key: String,
    public val defaultValue: Double = 0.0,
    override val scope: String = DEFAULT_QUSP_SCOPE,
) : QUSPStore<Double> {
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        QUSP.app
            .getSharedPreferences(scope, Context.MODE_PRIVATE)
            .edit()
            .putLong(key, value.toRawBits())
            .apply()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        val preferences = QUSP.app.getSharedPreferences(
            scope,
            Context.MODE_PRIVATE,
        )
        return if (preferences.contains(key)) {
            Double.fromBits(preferences.getLong(key, 0L))
        } else {
            defaultValue
        }
    }
}

public class QUSPStringStore(
    override val key: String,
    public val defaultValue: String = "",
    override val scope: String = DEFAULT_QUSP_SCOPE,
) : QUSPStore<String> {
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        QUSP.app
            .getSharedPreferences(scope, Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): String = QUSP.app
        .getSharedPreferences(scope, Context.MODE_PRIVATE)
        .getString(key, null)
        ?: defaultValue
}

public class QUSPObjectStore<T : Any>(
    override val key: String,
    private val to: (T) -> String,
    private val of: (String) -> T,
    override val scope: String = DEFAULT_QUSP_SCOPE,
) : QUSPStore<T?> {
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        val editor = QUSP.app
            .getSharedPreferences(scope, Context.MODE_PRIVATE)
            .edit()
        if (value == null) {
            editor.remove(key)
        } else {
            editor.putString(key, to(value))
        }
        editor.apply()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = QUSP.app
        .getSharedPreferences(scope, Context.MODE_PRIVATE)
        .getString(key, null)
        ?.let(of)
}

public class QUSPObsStore<T> internal constructor(
    private val store: QUSPStore<T>,
) : QUSPStore<T> by store {
    private val mutableFlow = MutableSharedFlow<T>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    public val flow: Flow<T> = mutableFlow.asSharedFlow()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        store.setValue(thisRef, property, value)
        mutableFlow.tryEmit(value)
    }
}

public val <T> QUSPStore<T>.obs: QUSPObsStore<T>
    get() = QUSPObsStore(this)
