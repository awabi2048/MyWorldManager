package me.awabi2048.mw_manager.portal

import me.awabi2048.mw_manager.my_world.MyWorldManager
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player

/**
 * ワールドポータル用のインスタンスです。
 * @param uuid 接続先ワールドのUUID
 */
class WorldPortal(private val uuid: String?) {
    /**
     * ポータルが利用可能かどうか
     */
    val isAvailable: Boolean
        get() {
            return MyWorldManager.registeredMyWorld.any { it.uuid == uuid }
        }

    /**
     * 指定したプレイヤーに通過処理を行います。
     * @param player 対象のプレイヤー
     */
    fun sendPlayer(player: Player) {
        if (isAvailable) {
            val targetWorld = MyWorldManager.registeredMyWorld.find { it.uuid == uuid }!!
            targetWorld.warpPlayer(player)

            player.getNearbyEntities(5.0, 5.0, 5.0).filterIsInstance<Player>()
                .forEach {it.playSound(it, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 1.0f)}
        }
    }

    /**
     * ポータルを撤去します。
     * @param initialize ポータルの接続先データを初期化するかどうか
     */
    fun remove(initialize: Boolean) {

    }

    fun place(location: Location) {
        //
        val portalEntity = location.world.spawnEntity(location, EntityType.INTERACTION) as Interaction
        portalEntity.interactionHeight = 3.0f
        portalEntity.addScoreboardTag("mwm.world_portal_interaction")
        portalEntity.addScoreboardTag("mwm.portal_uuid.$uuid")

        location.world.players.filter { it.location.distance(location) <= 5.0 }
            .forEach { it.playSound(it, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.0f) }
    }
}
