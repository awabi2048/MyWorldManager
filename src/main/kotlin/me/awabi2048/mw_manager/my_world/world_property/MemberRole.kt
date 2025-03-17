package me.awabi2048.mw_manager.my_world.world_property

enum class MemberRole {
    MEMBER,
    MODERATOR,
    OWNER;

    val japaneseName: String
        get() {
            return when (this) {
                MEMBER -> "§bメンバー"
                MODERATOR -> "§6モデレーター"
                OWNER -> "§cオーナー"
            }
        }
}
