package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.portal.WorldPortal
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType

object WorldPortalListener : Listener {
    @EventHandler
    fun onPortalLink(event: PlayerInteractEvent) {
        if (Lib.getItemID(event.item) == "WORLD_PORTAL") {
            // リンク先を確認
            val linkedWorldUUID = event.item!!.itemMeta.persistentDataContainer.get(
                NamespacedKey(instance, "portal_linked_world"),
                PersistentDataType.STRING
            )

            // none: 未登録 なら登録処理
            if (linkedWorldUUID == "none") {
                event.isCancelled = true

                // 現在のワールドの確認
                val currentWorld = event.player.world

                if (currentWorld.name.startsWith("my_world.")) {
                    // PDCに登録
                    event.item!!.itemMeta = event.item!!.itemMeta.apply {
                        persistentDataContainer.set(
                            NamespacedKey(instance, "portal_linked_world"),
                            PersistentDataType.STRING,
                            currentWorld.name.substringAfter("my_world.")
                        )
                    }
                } else {
                    event.player.sendMessage("§cこのワールドでは使用できません。")
                }
            }
        }
    }

    @EventHandler
    fun onPortalPlace(event: BlockPlaceEvent) {
        //
        if (Lib.getItemID(event.player.equipment.itemInMainHand) == "WORLD_PORTAL") {
            val location = event.blockPlaced.location.apply {
                x += 0.5
                y += 0.5
                z += 0.5
            }

            val uuid = event.player.equipment.itemInMainHand.itemMeta.persistentDataContainer.get(NamespacedKey(instance, "portal_linked_world"), PersistentDataType.STRING)
            val portal = WorldPortal(uuid)
            portal.place(location)
        }
    }

    @EventHandler
    fun onPlayerEnterPortal(event: PlayerMoveEvent) {
        // プレイヤーがポータルの判定に掠ってたら判定
        if (event.player.world.entities.filter { it.scoreboardTags.contains("mwm.world_portal_interaction") }
                .any { event.player.location.toVector() in it.boundingBox }) {
            val portalEntity = event.player.world.entities.filter { it.scoreboardTags.contains("mwm.world_portal_interaction") }
                .find { event.player.location.toVector() in it.boundingBox }!!
            val portalUUID = portalEntity.scoreboardTags.find{it.startsWith("mwm.portal_uuid.")}!!.substringAfter("mwm.portal_uuid.")
            val portal = WorldPortal(portalUUID)

            portal.sendPlayer(event.player)
        }
    }

    @EventHandler
    fun onPortalMenuOpen(event: PlayerInteractEntityEvent) {

    }
}
