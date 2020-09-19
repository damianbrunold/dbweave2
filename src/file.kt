import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.PushbackReader
import java.nio.charset.StandardCharsets

fun readTokens(file: File) : List<String> {
    val reader = PushbackReader(InputStreamReader(FileInputStream(file), StandardCharsets.ISO_8859_1))
    val tokens = mutableListOf<String>()
    val token = StringBuilder()
    var state = "whitespace"
    while (state != "end") {
        val c = reader.read()
        if (c == -1) {
            if (token.isNotEmpty()) {
                tokens.add(token.toString())
            }
            state = "end"
        } else {
            val ch = c.toChar()
            when (state) {
                "whitespace" -> {
                    if (ch == ';') {
                        state = "comment"
                    } else if (ch !in setOf(' ', '\t', '\r', '\n')) {
                        token.append(ch)
                        state = "token"
                    }
                }
                "token" -> {
                    if ((ch == '{') or (ch == '}')) {
                        tokens.add(token.toString())
                        token.setLength(0)
                        tokens.add(ch.toString())
                        state = "whitespace"
                    } else if (ch == '\\') {
                        state = "checkendofline"
                    } else if (ch == '=') {
                        state = "checkequals"
                    } else if (ch == ';') {
                        tokens.add(token.toString())
                        token.setLength(0)
                        state = "comment"
                    } else if (ch in setOf('\r', '\n')) {
                        tokens.add(token.toString())
                        token.setLength(0)
                        state = "whitespace"
                    } else {
                        token.append(ch)
                    }
                }
                "checkendofline" -> {
                    if ((ch == '\r') or (ch == '\n')) {
                        state = "skipwhitespace"
                    } else {
                        token.append('\\')
                        token.append(ch)
                        state = "token"
                    }
                }
                "skipwhitespace" -> {
                    if (ch !in setOf<Char>(' ', '\t', '\r', '\n')) {
                        token.append(ch)
                        state = "token"
                    }
                }
                "checkequals" -> {
                    if (ch == '=') {
                        tokens.add(token.toString())
                        tokens.add("==")
                        token.setLength(0)
                        state = "checkemptyvalue"
                    } else {
                        token.append('=')
                        token.append(ch)
                        state = "token"
                    }
                }
                "checkemptyvalue" -> {
                    if ((ch == '\r') or (ch == '\n')) {
                        tokens.add("")
                    } else {
                        reader.unread(c)
                    }
                    state = "whitespace"
                }
                "comment" -> {
                    if ((ch == '\r') or (ch == '\n')) {
                        state = "whitespace"
                    }
                }
            }
        }
    }
    return tokens
}

fun buildFileMap(tokens: List<String>) : Map<String, String> {
    val result = mutableMapOf<String, String>()
    var index = 0
    index++ // skip file identifier
    var prefixes = mutableListOf<String>()
    while (index < tokens.size) {
        val token = tokens[index]
        if (token.startsWith("\\")) {
            prefixes.add(token)
            index++ // skip {
        } else if (token == "}") {
            prefixes.removeAt(prefixes.size - 1)
        } else {
            val key = token
            index++ // skip ==
            index++ // skip value
            val value = tokens[index]
            result[prefixes.joinToString(separator = "") + "\\" + key] = value
        }
        index++
    }
    return result
}

fun main() {
    //readTokens(File("C:/temp/test.dbw")).forEach { println("'$it'")}
    buildFileMap(readTokens(File("C:/temp/test.dbw"))).forEach { println("${it.key} -> ${it.value}") }
}
