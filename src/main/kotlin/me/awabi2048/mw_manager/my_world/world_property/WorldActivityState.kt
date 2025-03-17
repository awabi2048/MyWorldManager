package me.awabi2048.mw_manager.my_world.world_property

enum class WorldActivityState {
    ARCHIVED,
    ACTIVE;

    val japaneseName: String
        get() {
            return when(this) {
                ACTIVE -> "§aアクティブ"
                ARCHIVED -> "§cアーカイブ済み"
            }
        }
}
