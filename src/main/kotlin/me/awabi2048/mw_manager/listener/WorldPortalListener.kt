package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.portal.WorldPortal
import me.awabi2048.mw_manager.ui.PortalUI
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import java.util.*

object WorldPortalListener : Listener {
    @EventHandler
    fun onPortalLink(event: PlayerInteractEvent) {
        if (Lib.getItemID(event.item) == "WORLD_PORTAL") {
            if (event.action !in listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return

            // リンク先を確認
            val linkedWorldUUID = event.item!!.itemMeta.persistentDataContainer.get(
                NamespacedKey(instance, "portal_linked_world"),
                PersistentDataType.STRING
            )
            val player = event.player

            // none: 未登録 なら登録処理
            if (linkedWorldUUID == "none" && !event.player.isSneaking) {
                event.isCancelled = true

                // 現在のワールドの確認
                val currentWorld = event.player.world
                val myWorld = MyWorld(currentWorld.name.substringAfter("my_world."))

                if (myWorld.isRegistered) {
                    event.item!!.editMeta {
                        // PDCに登録
                        it.persistentDataContainer.set(
                            NamespacedKey(instance, "portal_linked_world"),
                            PersistentDataType.STRING,
                            currentWorld.name.substringAfter("my_world.")
                        )

                        it.setEnchantmentGlintOverride(true)
                        it.lore(
                            it.lore()?.apply {
                                set(2, Component.text("§bリンク先§7: §8【§e${myWorld.name}§8】"))
                            }
                        )
                    }

                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
                    player.sendMessage(Component.text("§8【§a${myWorld.name}§8】§7をポータルに紐づけました。"))

                } else {
                    player.sendMessage("§cこのワールドでは使用できません。")
                    player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 0.5f)
                }

                // 登録済み → Shiftクリックで解除
            } else if (linkedWorldUUID != "none" && event.player.isSneaking) {
                event.isCancelled = true

                event.item!!.editMeta {
                    // PDCに登録
                    it.persistentDataContainer.set(
                        NamespacedKey(instance, "portal_linked_world"),
                        PersistentDataType.STRING,
                        "none"
                    )

                    it.setEnchantmentGlintOverride(false)
                    it.lore(
                        it.lore()?.apply {
                            set(2, Component.text("§bリンク先§7: なし"))
                        }
                    )
                }

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 20.5f)
                player.sendMessage(Component.text("§7ポータルの紐づけを解除しました。"))
            }
        }
    }

    @EventHandler
    fun onPortalPlace(event: BlockPlaceEvent) {
        //
        if (Lib.getItemID(event.player.equipment.itemInMainHand) == "WORLD_PORTAL") {
            val location = event.blockPlaced.location.toBlockLocation()

            val worldUUID = event.player.equipment.itemInMainHand.itemMeta.persistentDataContainer.get(
                NamespacedKey(
                    instance,
                    "portal_linked_world"
                ), PersistentDataType.STRING
            ) ?: return

            val portalUUID = UUID.randomUUID().toString()
            val portal = WorldPortal(portalUUID)
            portal.place(location, MyWorld(worldUUID), event.player)
        }
    }

    @EventHandler
    fun onPortalMenuOpen(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val blockLocation = event.clickedBlock?.location?.toBlockLocation() ?: return
        val portal = MyWorldManager.registeredPortal.find { it.location == blockLocation } ?: return

        if (event.player != portal.owner && !event.player.hasPermission("mw_manager.admin")) return // オーナーか権限バイパス時のみ
        if (event.hand != EquipmentSlot.HAND) return // 片手のみ判定しないと二重に処理されてしまう

        val ui = PortalUI(event.player, portal)
        ui.open(true)
    }
}

