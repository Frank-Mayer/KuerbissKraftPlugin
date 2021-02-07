package main

import java.io.FileNotFoundException
import java.io.FileReader
import java.util.regex.Matcher
import java.util.regex.Pattern

class BadLanguageChecker {
    private var pattern: Pattern
    init {
        var blacklist: Array<String>
        try {
            val fr = FileReader("${Settings.storePath}Blacklist.txt")
            blacklist = fr.readLines().toTypedArray()
            fr.close()
        }
        catch (e: FileNotFoundException) {
            Logger.error("Datei \"Blacklist.txt\" nicht gefunden")
            blacklist = arrayOf(
                "ficken", "arschloch", "arsch"
            )
        }
        pattern = Pattern.compile("^("+blacklist.joinToString("|")+")$", Pattern.CASE_INSENSITIVE)
    }

    /**
     * returns true if bad language was found
     */
    fun filter(txt: String): Boolean {
        val matcher: Matcher = pattern.matcher(txt)
        return matcher.find()
    }
}