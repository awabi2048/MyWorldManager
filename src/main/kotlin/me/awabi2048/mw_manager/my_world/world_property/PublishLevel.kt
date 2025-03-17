package me.awabi2048.mw_manager.my_world.world_property

enum class PublishLevel {
    PUBLIC,
    OPEN,
    PRIVATE,
    CLOSED;

    val japaneseName: String
        get() {
            return when (this) {
                PUBLIC -> "§a公開"
                OPEN -> "§e限定公開"
                PRIVATE -> "§c非公開"
                CLOSED -> "§c封鎖中"
            }
        }
}
