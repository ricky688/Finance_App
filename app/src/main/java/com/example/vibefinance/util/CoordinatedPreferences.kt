package com.example.vibefinance.util

import android.content.Context
import android.content.SharedPreferences
import com.example.vibefinance.data.InMemoryDatabase

/** Preference commits share the financial snapshot lock, including notification-service writes. */
object CoordinatedPreferences {
    fun get(context: Context, name: String): SharedPreferences = LockedPreferences(
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
    )

    private class LockedPreferences(private val base: SharedPreferences) : SharedPreferences by base {
        override fun edit(): SharedPreferences.Editor = LockedEditor(base.edit())
    }

    private class LockedEditor(private val base: SharedPreferences.Editor) : SharedPreferences.Editor by base {
        override fun putString(key: String?, value: String?) = apply { base.putString(key, value) }
        override fun putStringSet(key: String?, values: MutableSet<String>?) = apply { base.putStringSet(key, values) }
        override fun putInt(key: String?, value: Int) = apply { base.putInt(key, value) }
        override fun putLong(key: String?, value: Long) = apply { base.putLong(key, value) }
        override fun putFloat(key: String?, value: Float) = apply { base.putFloat(key, value) }
        override fun putBoolean(key: String?, value: Boolean) = apply { base.putBoolean(key, value) }
        override fun remove(key: String?) = apply { base.remove(key) }
        override fun clear() = apply { base.clear() }
        override fun commit(): Boolean = synchronized(InMemoryDatabase.diskIoLock) { base.commit() }
        override fun apply() { synchronized(InMemoryDatabase.diskIoLock) { base.apply() } }
    }
}
