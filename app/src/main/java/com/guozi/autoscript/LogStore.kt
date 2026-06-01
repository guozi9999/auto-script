package com.guozi.autoscript

object LogStore {
    private val lines = mutableListOf<String>()

    @Synchronized
    fun add(line: String) {
        lines.add(line)
    }

    @Synchronized
    fun clear() {
        lines.clear()
    }

    @Synchronized
    fun latest(limit: Int): List<String> {
        if (limit <= 0) return emptyList()
        return lines.takeLast(limit).toList()
    }

    @Synchronized
    fun all(): List<String> {
        return lines.toList()
    }

    @Synchronized
    fun count(): Int {
        return lines.size
    }
}
