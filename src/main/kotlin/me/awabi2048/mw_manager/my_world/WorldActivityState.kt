package me.awabi2048.mw_manager.my_world

enum class WorldActivityState {
    ARCHIVED,
    ACTIVE;

    fun toJapanese(): String {
        return when(this) {
            ACTIVE -> "アクティブ"
            ARCHIVED -> "アーカイブ済み"
        }
    }
}
