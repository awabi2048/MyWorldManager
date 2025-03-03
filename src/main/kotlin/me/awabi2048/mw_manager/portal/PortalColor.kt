package me.awabi2048.mw_manager.portal

enum class PortalColor {
    WHITE,
    GRAY,
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    PURPLE;

    val rgb: List<Int>
        get() {
            return when (this) {
                WHITE -> listOf(255, 255, 255)
                GRAY -> listOf(127, 127, 127)
                RED -> listOf(255, 0, 0)
                ORANGE -> listOf(255, 127, 0)
                YELLOW -> listOf(255, 255, 0)
                GREEN -> listOf(0, 255, 0)
                BLUE -> listOf(0, 0, 255)
                PURPLE -> listOf(191, 0, 191)
            }
        }

    val japaneseName: String
        get() {
            return when (this) {
                WHITE -> "§f白"
                GRAY -> "§7グレー"
                RED -> "§c赤"
                ORANGE -> "§6オレンジ"
                YELLOW -> "§e黄"
                GREEN -> "§a緑"
                BLUE -> "§b青"
                PURPLE -> "§d紫"
            }
        }
}
