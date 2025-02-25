package me.awabi2048.mw_manager.player_notification

import org.bukkit.entity.Player

enum class PlayerNotification {
    MISC_ERROR,
    INVALID_PLAYER_GIVEN,
    BLACKLISTED_STRING,
    NOT_ENOUGH_WORLD_POINT,
    WORLD_EXPANSION_SUCCEEDED,
    WARP_SHORTCUT_SET_FAILED,
    INVALID_COMMAND;

    fun send(player: Player) {
        player.sendMessage(
            when(this) {
                BLACKLISTED_STRING -> "§c利用できない文字列が含まれています。再度入力してください。"
                INVALID_COMMAND -> "§c無効なコマンドです。"
                MISC_ERROR -> "§cエラーが発生しました。スタッフに報告してください。"
                INVALID_PLAYER_GIVEN -> "§cそのプレイヤーは存在しないか、オフラインです。"
                NOT_ENOUGH_WORLD_POINT -> "§cワールドポイントが不足しています。"
                WORLD_EXPANSION_SUCCEEDED -> "§aワールドが拡張されました！"
                WARP_SHORTCUT_SET_FAILED -> "§cショートカットの作成に失敗しました。このワールドが公開ワールドでないか、設定できないワールドです。"
            }
        )
    }
}
