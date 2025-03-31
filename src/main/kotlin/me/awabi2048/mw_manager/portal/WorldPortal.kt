package me.awabi2048.mw_manager.portal

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.playersInPortalCooldown
import me.awabi2048.mw_manager.custom_item.CustomItem
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.my_world.world_property.PublishLevel
import org.bukkit.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*

/**
 * ワールドポータル用のインスタンスです。
 * @param uuid ポータルのUUID
 */
class WorldPortal(private val uuid: String) {
    private val section: ConfigurationSection
        get() {
            return DataFiles.portalData.getConfigurationSection(uuid)!!
        }

    /**
     * ポータルが利用可能かどうか
     */
    val isAvailable: Boolean
        get() {
//            println("-----------------")
//            println("checking: $uuid")
//            println(MyWorldManager.registeredMyWorld.any { it == destinationWorld })
//            println(portalData.contains(uuid))
//            println("-----------------")

            return MyWorldManager.registeredMyWorlds.any { it.uuid == destinationWorld.uuid } && DataFiles.portalData.contains(
                uuid
            )
        }

    val isLoaded: Boolean
        get() {
            return location?.getNearbyPlayers(10.0)?.isNotEmpty() ?: false
        }

    var owner: OfflinePlayer
        get() {
            return Bukkit.getOfflinePlayer(UUID.fromString(section.getString("owner")))
        }
        set(value) {
            DataFiles.portalData.set("$uuid.owner", value.uniqueId.toString())
            DataFiles.save()
        }

    var location: Location?
        get() {
            val world = Bukkit.getWorld(section.getString("location.world")!!) ?: return null
            return Location(
                world,
                section.getInt("location.x").toDouble(),
                section.getInt("location.y").toDouble(),
                section.getInt("location.z").toDouble()
            )
        }
        set(value) {
            if (value != null) {
                val map = mapOf(
                    "world" to value.world.name,
                    "x" to value.blockX,
                    "y" to value.blockY,
                    "z" to value.blockZ,
                )

                DataFiles.portalData.set("$uuid.location", map)
                DataFiles.save()
            }
        }

    private var destinationWorld: MyWorld
        get() {
            val uuid = section.getString("destination")!!
            return MyWorld(uuid)
        }
        set(value) {
            DataFiles.portalData.set("$uuid.destination", value.uuid)
            DataFiles.save()
        }

    var displayText: Boolean
        get() {
            return section.getBoolean("display_text")
        }
        set(value) {
            DataFiles.portalData.set("$uuid.display_text", value)
            DataFiles.save()
        }

    var color: PortalColor
        get() {
            return PortalColor.valueOf(section.getString("color")!!)
        }
        set(value) {
            DataFiles.portalData.set("$uuid.color", value.name)
            DataFiles.save()
        }

    /**
     * 指定したプレイヤーに通過処理を行います。
     * @param player 対象のプレイヤー
     */
    private fun sendPlayer(player: Player) {
        if (isAvailable) {
            destinationWorld.warpPlayer(player)
        }
    }

    /**
     * ポータルを撤去します。
     */
    fun remove() {
        if (location == null) return

        // ブロックを更新
        location!!.block.type = Material.AIR

        // 演出
        location!!.getNearbyPlayers(5.0).forEach {
            it.playSound(it, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 2.0f)
            it.spawnParticle(Particle.ENCHANTED_HIT, location!!, 40, 0.6, 0.6, 0.6, 0.1)
        }

        // アイテムをgive
        if (owner.isOnline) {
            // いっぱいならその場にドロップ
            if (owner.player!!.inventory.firstEmpty() == -1) {
                location!!.world.dropItemNaturally(location!!, CustomItem.WORLD_PORTAL.itemStack)
            } else {
                owner.player!!.inventory.addItem(CustomItem.WORLD_PORTAL.itemStack)
            }
        }

        // データを削除
        DataFiles.portalData.set(uuid, null)
        DataFiles.save()
    }

    fun place(portalLocation: Location, destination: MyWorld, owner: Player) {
        this.location = portalLocation // locationはセッターがあるからあるので使う
        this.owner = owner
        this.destinationWorld = destination
        this.color = PortalColor.PURPLE
        this.displayText = true

        DataFiles.save()
        DataFiles.loadAll() // Mainからスケジュールしている都合、再ロードしないとメモリ上にデータが載らなくてエラーになる ?

        // 演出
        portalLocation.world.players.filter { it.location.distance(portalLocation) <= 5.0 }
            .forEach { it.playSound(it, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.0f) }
    }

    fun tickingProcess() {
        if (isAvailable && isLoaded) {
            // 封鎖中なら作動しない
            if (destinationWorld.publishLevel == PublishLevel.CLOSED) return

            // パーティクル演出
            val dustOption = Particle.DustOptions(color.colorInstance, 0.5f)

            location!!.getNearbyPlayers(10.0)
                .forEach {
                    it.spawnParticle(
                        Particle.DUST,
                        location!!.add(0.5, 1.5, 0.5),
                        40,
                        0.33,
                        1.2,
                        0.33,
                        0.0,
                        dustOption
                    )
                }

            // 判定内のプレイヤーを転送
            location!!.getNearbyPlayers(5.0).filter {
                it.location.toBlockLocation().blockX == location!!.toBlockLocation().blockX &&
                        it.location.toBlockLocation().blockY in (location!!.toBlockLocation().blockY..location!!.toBlockLocation().blockY + 3) &&
                        it.location.toBlockLocation().blockZ == location!!.toBlockLocation().blockZ
            }.forEach {
                playersInPortalCooldown += it
                sendPlayer(it)

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        playersInPortalCooldown -= it
                    },
                    100L
                )
            }
        }
    }
}
