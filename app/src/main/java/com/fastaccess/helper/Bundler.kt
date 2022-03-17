package com.fastaccess.helper

import com.fastaccess.helper.Logger.e
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.util.SparseArray
import android.os.Parcel
import java.io.Serializable
import java.util.ArrayList

/**
 * Created by Kosh on 23 May 2016, 3:37 PM
 */
class Bundler private constructor() {
    private val bundle: Bundle = Bundle()
    fun put(key: String, value: Boolean): Bundler {
        bundle.putBoolean(key, value)
        return this
    }

    fun put(key: String, value: BooleanArray?): Bundler {
        bundle.putBooleanArray(key, value)
        return this
    }

    fun put(key: String, value: IBinder?): Bundler {
        // Uncommment this line if your minimum sdk version is API level 18
        //start.putBinder(key, value);
        return this
    }

    fun put(key: String, value: Int): Bundler {
        bundle.putInt(key, value)
        return this
    }

    fun put(key: String, value: IntArray?): Bundler {
        bundle.putIntArray(key, value)
        return this
    }

    fun putIntegerArrayList(key: String, value: ArrayList<Int>?): Bundler {
        bundle.putIntegerArrayList(key, value)
        return this
    }

    fun put(key: String, value: Bundle?): Bundler {
        bundle.putBundle(key, value)
        return this
    }

    fun put(key: String, value: Byte): Bundler {
        bundle.putByte(key, value)
        return this
    }

    fun put(key: String, value: ByteArray?): Bundler {
        bundle.putByteArray(key, value)
        return this
    }

    fun put(key: String, value: String?): Bundler {
        bundle.putString(key, value)
        return this
    }

    fun put(key: String, value: Array<String>?): Bundler {
        bundle.putStringArray(key, value)
        return this
    }

    fun putStringArrayList(key: String, value: ArrayList<String>?): Bundler {
        bundle.putStringArrayList(key, value)
        return this
    }

    fun put(key: String, value: Long): Bundler {
        bundle.putLong(key, value)
        return this
    }

    fun put(key: String, value: LongArray?): Bundler {
        bundle.putLongArray(key, value)
        return this
    }

    fun put(key: String, value: Float): Bundler {
        bundle.putFloat(key, value)
        return this
    }

    fun put(key: String, value: FloatArray?): Bundler {
        bundle.putFloatArray(key, value)
        return this
    }

    fun put(key: String, value: Char): Bundler {
        bundle.putChar(key, value)
        return this
    }

    fun put(key: String, value: CharArray?): Bundler {
        bundle.putCharArray(key, value)
        return this
    }

    fun put(key: String, value: CharSequence?): Bundler {
        bundle.putCharSequence(key, value)
        return this
    }

    fun put(key: String, value: Array<CharSequence>?): Bundler {
        bundle.putCharSequenceArray(key, value)
        return this
    }

    fun putCharSequenceArrayList(key: String, value: ArrayList<CharSequence>?): Bundler {
        bundle.putCharSequenceArrayList(key, value)
        return this
    }

    fun put(key: String, value: Double): Bundler {
        bundle.putDouble(key, value)
        return this
    }

    fun put(key: String, value: DoubleArray?): Bundler {
        bundle.putDoubleArray(key, value)
        return this
    }

    fun put(key: String, value: Parcelable?): Bundler {
        val safeBundle = Bundle()
        safeBundle.putParcelable(key, value)
        if (isValidBundleSize(safeBundle)) {
            bundle.putParcelable(key, value)
        }
        clearBundle(safeBundle)
        return this
    }

    fun put(key: String, value: Array<Parcelable>?): Bundler {
        val safeBundle = Bundle()
        safeBundle.putParcelableArray(key, value)
        if (isValidBundleSize(safeBundle)) {
            bundle.putParcelableArray(key, value)
        }
        clearBundle(safeBundle)
        return this
    }

    fun putParcelableArrayList(key: String, value: ArrayList<out Parcelable>?): Bundler {
        val safeBundle = Bundle()
        safeBundle.putParcelableArrayList(key, value)
        if (isValidBundleSize(safeBundle)) {
            bundle.putParcelableArrayList(key, value)
        }
        clearBundle(safeBundle)
        return this
    }

    fun putSparseParcelableArray(key: String, value: SparseArray<out Parcelable>?): Bundler {
        val safeBundle = Bundle()
        safeBundle.putSparseParcelableArray(key, value)
        if (isValidBundleSize(safeBundle)) {
            bundle.putSparseParcelableArray(key, value)
        }
        clearBundle(safeBundle)
        return this
    }

    fun put(key: String, value: Short): Bundler {
        bundle.putShort(key, value)
        return this
    }

    fun put(key: String, value: ShortArray?): Bundler {
        bundle.putShortArray(key, value)
        return this
    }

    fun put(key: String, value: Serializable?): Bundler {
        val safeBundle = Bundle()
        safeBundle.putSerializable(key, value)
        if (isValidBundleSize(safeBundle)) {
            bundle.putSerializable(key, value)
        }
        clearBundle(safeBundle)
        return this
    }

    fun putAll(map: Bundle?): Bundler {
        bundle.putAll(map)
        return this
    }

    /**
     * Get the underlying start.
     */
    private fun get(): Bundle {
        return bundle
    }

    fun end(): Bundle {
        val parcel = Parcel.obtain()
        bundle.writeToParcel(parcel, 0)
        val size = parcel.dataSize()
        e(size)
        if (size > 500000) {
            bundle.clear()
        }
        return get()
    }

    private fun clearBundle(safeBundle: Bundle) {
        safeBundle.clear()
    }

    companion object {
        @JvmStatic
        fun start(): Bundler {
            return Bundler()
        }

        fun isValidBundleSize(bundle: Bundle): Boolean {
            val parcel = Parcel.obtain()
            bundle.writeToParcel(parcel, 0)
            return parcel.dataSize() < 500000
        }
    }

}