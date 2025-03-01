package me.awabi2048.mw_manager.portal

import me.awabi2048.mw_manager.custom_item.CustomItem
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
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

            return MyWorldManager.registeredMyWorld.any { it.uuid == destinationWorld.uuid } && DataFiles.portalData.contains(uuid)
        }

    var owner: OfflinePlayer
        get() {
            return Bukkit.getOfflinePlayer(UUID.fromString(section.getString("owner")))
        }
        set(value) {
            DataFiles.portalData.set("$uuid.owner", value.uniqueId.toString())
            DataFiles.save()
        }

    var location: Location
        get() {
            val world = Bukkit.getWorld(section.getString("location.world")!!)
            return Location(
                world,
                section.getInt("location.x").toDouble(),
                section.getInt("location.y").toDouble(),
                section.getInt("location.z").toDouble()
            )
        }

        set(value) {
            val map = mapOf(
                "world" to value.world.name,
                "x" to value.blockX,
                "y" to value.blockY,
                "z" to value.blockZ,
            )

            DataFiles.portalData.set("$uuid.location", map)
            DataFiles.save()
        }

    var destinationWorld: MyWorld
        get() {
            val uuid = section.getString("destination")!!
            return MyWorld(uuid)
        }

        set(value) {
            DataFiles.portalData.set("$uuid.destination", value.uuid)
            DataFiles.save()
        }

    /**
     * 指定したプレイヤーに通過処理を行います。
     * @param player 対象のプレイヤー
     */
    fun sendPlayer(player: Player) {
        if (isAvailable) {
            destinationWorld.warpPlayer(player)
        }
    }

    /**
     * ポータルを撤去します。
     */
    fun remove() {
        // ブロックを更新
        location.block.type = Material.AIR

        // 演出
        location.getNearbyPlayers(5.0).forEach {
            it.playSound(it, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 2.0f)
            it.spawnParticle(Particle.ENCHANTED_HIT, location, 20, 0.6, 0.6, 0.6, 1.0)
        }

        // アイテムをgive
        if (owner.isOnline) {
            // いっぱいならその場にドロップ
            if (owner.player!!.inventory.firstEmpty() == -1) {
                location.world.dropItemNaturally(location, CustomItem.WORLD_PORTAL.itemStack)
            } else {
                owner.player!!.inventory.addItem(CustomItem.WORLD_PORTAL.itemStack)
            }
        }

        // データを削除
        DataFiles.portalData.set(uuid, null)
        DataFiles.save()
    }

    fun place(portalLocation: Location, destination: MyWorld, owner: Player) {
        location = portalLocation // locationはセッターがあるからあるので使う
        DataFiles.portalData.set("$uuid.owner", owner.uniqueId.toString())
        DataFiles.portalData.set("$uuid.destination", destination.uuid)

        DataFiles.save()
        DataFiles.loadAll() // Mainからスケジュールしている都合、再ロードしないとメモリ上にデータが載らなくてエラーになる ?

        // 演出
        portalLocation.world.players.filter { it.location.distance(portalLocation) <= 5.0 }
            .forEach { it.playSound(it, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.0f) }
    }

    fun tickingProcess() {
        // パーティクル演出
        location.getNearbyPlayers(10.0)
            .forEach { it.spawnParticle(Particle.PORTAL, location.add(0.5, 1.5, 0.5), 10, 0.5, 1.5, 0.5, 0.0) }

        // 判定内のプレイヤーを転送
        location.getNearbyPlayers(5.0).filter {
            it.location.toBlockLocation().blockX == location.toBlockLocation().blockX &&
            it.location.toBlockLocation().blockY in (location.toBlockLocation().blockY .. location.toBlockLocation().blockY + 3) &&
            it.location.toBlockLocation().blockZ == location.toBlockLocation().blockZ
        }

            .forEach {
                sendPlayer(it)
            }
    }
}
