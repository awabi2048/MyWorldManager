package me.awabi2048.mw_manager.portal

import org.bukkit.Color

enum class PortalColor {
    WHITE,
    GRAY,
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    PURPLE;

    val colorInstance: Color
        get() {
            return when (this) {
                WHITE -> Color.WHITE
                GRAY -> Color.GRAY
                RED -> Color.RED
                ORANGE -> Color.ORANGE
                YELLOW -> Color.YELLOW
                GREEN -> Color.LIME
                BLUE -> Color.AQUA
                PURPLE -> Color.FUCHSIA
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
