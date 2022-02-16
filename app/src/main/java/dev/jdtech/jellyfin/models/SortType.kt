package dev.jdtech.jellyfin.models

enum class SortType(val value: String, val display: String) {
    // 根据名称排序
    NAME("name", "名称"),

    // 默认排序
    DEFAULT("default", "默认");

    companion object {
        fun parse(value: String?): SortType {
            for (sortType in values()) {
                if (value == sortType.name) {
                    return sortType
                }
            }
            return DEFAULT
        }
    }


}