package dev.jdtech.jellyfin.models

enum class ShowType {
    LIST, GRID;

    companion object {
        fun toShowType(value: String?): ShowType {
            return try {
                value?.let {
                    valueOf(value)
                } ?: LIST

            } catch (ex: java.lang.Exception) {
                // For error cases
                LIST
            }
        }
    }

}