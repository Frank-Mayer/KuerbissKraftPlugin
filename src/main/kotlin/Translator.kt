package main

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileReader
import java.lang.reflect.Type

class Translator {
    private val advancements = hashMapOf<String, String>()

    init {
        val gson = Gson()
        val fr = FileReader("${Settings.storePath}Advancements.json")
        val json = fr.readText()
        fr.close()
        val type: Type = object : TypeToken<Map<String, String>>() {}.type
        val impMap: Map<String, String> = gson.fromJson(json, type)
        for (el in impMap) {
            advancements[el.key] = el.value
        }
    }

    fun getAdvancementName(namespace: String): String? {
        return if (advancements.containsKey(namespace)) {
            advancements[namespace]
        } else {
            null
        }
    }
}