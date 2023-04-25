package dev.minjae.votifier.server.config

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

abstract class Configuration @JvmOverloads constructor(saveFile: File?, inputStream: InputStream? = null) {

    val logger = LoggerFactory.getLogger(Configuration::class.java)

    protected var file: File? = null
    protected var values: MutableMap<String?, Any?> = LinkedHashMap()

    constructor(saveFile: String?) : this(saveFile?.let { File(it) })
    constructor(path: Path) : this(path.toFile())

    init {
        var inputStream = inputStream
        file = saveFile
        try {
            if (!file!!.exists()) {
                this.save()
            }
            if (inputStream == null) {
                inputStream = Files.newInputStream(file!!.toPath())
            }
            load(inputStream)
        } catch (e: IOException) {
            logger.error("Unable to initialize Config " + file.toString(), e)
        }
    }

    protected abstract fun deserialize(inputStream: InputStream?): MutableMap<String?, Any?>
    protected abstract fun serialize(values: Map<String?, Any?>?): String?
    fun load(inputStream: InputStream?) {
        try {
            values = deserialize(inputStream)
        } catch (e: Exception) {
            logger.error("Unable to load Config " + file.toString())
        }
    }

    fun save() {
        this.save(serialize(values))
    }

    protected fun save(content: String?) {
        try {
            val parentFile = file!!.parentFile
            parentFile?.mkdirs()
            val myWriter = FileWriter(file)
            myWriter.write(content)
            myWriter.close()
        } catch (e: IOException) {
            logger.error("Unable to save Config " + file.toString(), e)
        }
    }

    fun loadFrom(values: MutableMap<String?, Any?>) {
        this.values = values
    }

    val keys: Set<String?>
        get() = values.keys

    fun remove(key: String?) {
        val lastMap = getLastMap(key) ?: return
        lastMap.map!!.remove(lastMap.key)
    }

    operator fun set(key: String?, value: Any) {
        val lastMap = getLastMap(key) ?: return
        lastMap.map!![lastMap.key] = value
    }

    private fun getLastMap(key: String?): LastMap? {
        if (key == null || key.isEmpty()) return null
        var values: MutableMap<String?, Any?>? = values
        val keys = key.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var currentKey: String? = null
        for (i in keys.indices) {
            currentKey = keys[i]
            if (i + 1 < keys.size && values!![currentKey] == null) {
                values[currentKey] = LinkedHashMap<Any, Any>()
            }
            if (values!![currentKey] !is Map<*, *>) {
                break
            }
            values = values[currentKey] as MutableMap<String?, Any?>?
        }
        return LastMap(currentKey, values)
    }

    @JvmOverloads
    operator fun get(key: String?, defaultValue: Any? = null): Any? {
        if (key == null || key.isEmpty()) return defaultValue
        var values: Map<String?, Any?> = values
        val keys = key.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (this.values.containsKey(key)) {
            return this.values[key]
        }
        for (i in keys.indices) {
            val currentValue = values[keys[i]] ?: return defaultValue
            if (i + 1 == keys.size) {
                return currentValue
            }
            values = currentValue as Map<String?, Any?>
        }
        return defaultValue
    }

    fun exists(key: String?): Boolean {
        return this[key] != null
    }

    val all: Map<String?, Any?>
        get() = values

    fun setString(key: String?, value: String) {
        this[key] = value
    }

    fun getString(key: String?): String {
        return this.getString(key, "")
    }

    fun getString(key: String?, defaultValue: String?): String {
        return this[key, defaultValue].toString()
    }

    fun setInt(key: String?, value: Int) {
        this[key] = value
    }

    fun getInt(key: String?): Int {
        return this.getInt(key, -1)
    }

    fun getInt(key: String?, defaultValue: Int?): Int {
        return Integer.valueOf(this[key, defaultValue].toString())
    }

    fun setLong(key: String?, value: Long) {
        this[key] = value
    }

    fun getLong(key: String?): Long {
        return this.getLong(key, -1L)
    }

    fun getLong(key: String?, defaultValue: Long?): Long {
        return java.lang.Long.valueOf(this[key, defaultValue].toString())
    }

    fun setDouble(key: String?, value: Double) {
        this[key] = value
    }

    fun getDouble(key: String?): Double {
        return this.getDouble(key, -1.0)
    }

    fun getDouble(key: String?, defaultValue: Double?): Double {
        return java.lang.Double.valueOf(this[key, defaultValue].toString())
    }

    fun setBoolean(key: String?, value: Boolean) {
        this[key] = value
    }

    fun getBoolean(key: String?): Boolean {
        return this.getBoolean(key, false)
    }

    fun getBoolean(key: String?, defaultValue: Boolean?): Boolean {
        return this[key, defaultValue] as Boolean
    }

    fun <T> setList(key: String?, value: List<T>) {
        this[key] = value
    }

    fun <T> getList(key: String?): List<T> {
        return this.getList(key, emptyList())
    }

    fun <T> getList(key: String?, defaultValue: List<T>?): List<T> {
        return this[key, defaultValue] as List<T>
    }

    fun setStringList(key: String?, value: List<String>) {
        this[key] = value
    }

    fun getStringList(key: String?): List<String> {
        return this.getStringList(key, emptyList())
    }

    fun getStringList(key: String?, defaultValue: List<String>?): List<String> {
        return this[key, defaultValue] as List<String>
    }

    private class LastMap(
        val key: String? = null,
        val map: MutableMap<String?, Any?>? = null,
    )
}
