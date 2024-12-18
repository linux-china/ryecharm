package insyncwithfoo.ryecharm.ruff

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import insyncwithfoo.ryecharm.RootDisposable
import insyncwithfoo.ryecharm.RyeCharm
import insyncwithfoo.ryecharm.propertiesComponent
import insyncwithfoo.ryecharm.ruff.documentation.OptionDocumentation
import insyncwithfoo.ryecharm.ruff.documentation.OptionName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties


private typealias JSON = String


private const val PREFIX = "${RyeCharm.ID}.ruff.caching"


private fun String.toUniqueKeyName() = "$PREFIX.$this"


private inline fun <reified T> JSON.parse(): T {
    return Json.decodeFromString<T>(this)
}


private inline fun <reified T : Any> T.stringify(): JSON {
    return Json.encodeToString<T>(this)
}


private operator fun PropertiesComponent.get(property: KProperty<*>): String? {
    return getValue(property.name.toUniqueKeyName())
}


private operator fun PropertiesComponent.set(property: KProperty<*>, value: String?) {
    val keyName = property.name.toUniqueKeyName()
    
    when {
        value == null -> unsetValue(keyName)
        else -> setValue(keyName, value)
    }
}


@Service(Service.Level.PROJECT)
private class ParseCache : Disposable {
    
    init {
        Disposer.register(RootDisposable.getInstance(), this)
    }
    
    private val parsed = mutableMapOf<String, CachedResult<*>>()
    
    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified R : Any> get(property: KProperty<CachedResult<R>?>): CachedResult<R>? =
        parsed[property.name] as? CachedResult<R>?
    
    inline operator fun <reified R : Any> set(property: KProperty<CachedResult<R>?>, value: CachedResult<R>?) {
        when {
            value == null -> parsed.remove(property.name)
            else -> parsed[property.name] = value
        }
    }
    
    fun clear() {
        parsed.clear()
    }
    
    override fun dispose() {
        clear()
    }
    
}


@Service(Service.Level.PROJECT)
internal class RuffCache(private val project: Project) {
    
    private val parseCache: ParseCache
        get() = project.service<ParseCache>()
    
    private val storage: PropertiesComponent
        get() = project.propertiesComponent
    
    private val allProperties: List<KProperty<*>>
        get() = this::class.declaredMemberProperties
            .filterIsInstance<KMutableProperty1<*, *>>()
    
    /**
     * Store the result of `ruff config`.
     */
    var optionsDocumentation: CachedResult<Map<OptionName, OptionDocumentation>>?
        get() = RuffCache::optionsDocumentation.getStoredValue()
        set(value) = RuffCache::optionsDocumentation.setStoredValue(value)
    
    private inline fun <reified R : Any> KProperty<CachedResult<R>?>.getStoredValue(): CachedResult<R>? {
        if (parseCache[this] == null) {
            parseCache[this] = storage[this]?.parse()
        }
        
        return parseCache[this]
    }
    
    private inline fun <reified R : Any> KProperty<CachedResult<R>?>.setStoredValue(value: CachedResult<R>?) {
        storage[this] = value?.stringify()
        parseCache[this] = null
    }
    
    fun clear() {
        allProperties.forEach { storage[it] = null }
        parseCache.clear()
    }
    
    companion object {
        fun getInstance(project: Project) = project.service<RuffCache>()
    }
    
}
