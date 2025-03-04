package me.awabi2048.mw_manager.my_world.world_property

enum class WorldActivityState {
    ARCHIVED,
    ACTIVE;

    fun toJapanese(): String {
        return when(this) {
            ACTIVE -> "§aアクティブ"
            ARCHIVED -> "§cアーカイブ済み"
        }
    }
}
