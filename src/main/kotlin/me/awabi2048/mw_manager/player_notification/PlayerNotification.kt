package me.awabi2048.mw_manager.player_notification

import org.bukkit.entity.Player

enum class PlayerNotification {
    MISC_ERROR,
    INVALID_PLAYER_GIVEN,
    BLACKLISTED_STRING,
    NOT_ENOUGH_WORLD_POINT,
    WORLD_EXPANSION_SUCCEEDED,
    WARP_SHORTCUT_SET_FAILED,
    WORLD_SETTING_DISPLAY,
    WORLD_SETTING_POS,
    WORLD_SETTING_ADD_MEMBER,
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
                WARP_SHORTCUT_SET_FAILED -> "§cショートカットの作成に失敗しました。"
                WORLD_SETTING_DISPLAY -> "§fチャット欄に入力して、ワールド名・説明文を設定します。"
                WORLD_SETTING_POS -> "§fブロックをクリックして、スポーン位置を設定します。"
                WORLD_SETTING_ADD_MEMBER -> "§fチャット欄にプレイヤー名を入力して、招待を送信します。"
            }
        )
    }
}
