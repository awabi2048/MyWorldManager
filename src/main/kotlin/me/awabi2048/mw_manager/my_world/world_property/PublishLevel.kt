package me.awabi2048.mw_manager.my_world.world_property

enum class PublishLevel {
    PUBLIC,
    OPEN,
    PRIVATE;

    fun toJapanese(): String {
        return when (this) {
            PUBLIC -> "§a公開"
            OPEN -> "§e部分公開"
            PRIVATE -> "§c非公開"
        }
    }
}
