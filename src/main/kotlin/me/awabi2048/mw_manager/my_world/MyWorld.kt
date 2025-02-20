package me.awabi2048.mw_manager.my_world

import com.onarandombox.MultiverseCore.api.MultiverseWorld
import com.onarandombox.MultiverseCore.utils.FileUtils
import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.my_world.ExpandMethod.*
import me.awabi2048.mw_manager.Main.Companion.configData
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.Main.Companion.registeredWorldData
import me.awabi2048.mw_manager.config.DataFiles
import me.awabi2048.mw_manager.my_world.MemberRole.*
import org.bukkit.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.io.File
import java.time.LocalDate

/**
 * 存在のいかんにかかわらずインスタンス化できます。
 * @property uuid ワールドのUUID。
 */
class MyWorld(val uuid: String) {
    val isRegistered: Boolean
        get() {
            return registeredWorldData.getKeys(false).contains(uuid)
        }

    /**
     * @return Multiverseワールドとして取得した該当ワールド。存在しない場合はnullを返す。
     */
    val mvWorld: MultiverseWorld?
        get() {
            return mvWorldManager.mvWorlds.find { it.name == "my_world.$uuid" }
        }

    /**
     * @return Bukkit ワールドとして取得した該当ワールド。存在しない場合はnullを返す。
     */
    val vanillaWorld: World?
        get() {
            return Bukkit.getWorld("my_world.$uuid")
        }

    /**
     * @return ワールドの登録名。保存名とは異なる。
     */
    val name: String?
        get() {
            return registeredWorldData.getString("$uuid.world_name")
        }

    /**
     * @return オーナーとして登録されているプレイヤー。登録されていない場合はnullを返す。
     */
    val owner: Player?
        get() {
            val ownerUUID = registeredWorldData.getString("$uuid.owner") ?: return null
            return Bukkit.getPlayer(ownerUUID)
        }

    val members: Set<OfflinePlayer>?
        get() {
            return if (isRegistered) {
                dataSection!!.getStringList("member").map { Bukkit.getOfflinePlayer(it) }.toSet()
            } else null
        }

    val moderators: Set<OfflinePlayer>?
        get() {
            return if (isRegistered) {
                dataSection!!.getStringList("moderator").map { Bukkit.getOfflinePlayer(it) }.toSet()
            } else null
        }

    val sourceWorldName: String?
        get() {
            return registeredWorldData.getString("$uuid.source_world")
        }

    val lastUpdated: LocalDate?
        get() {
            val dateString = registeredWorldData.getString("$uuid.last_updated") ?: return null
            return LocalDate.parse(dateString)
        }

    val expireDate: LocalDate?
        get() {
            val lastUpdatedString = registeredWorldData.getString("$uuid.last_updated") ?: return null
            val lastUpdatedDate = LocalDate.parse(lastUpdatedString)
            val expireIn = registeredWorldData.getInt("$uuid.expire_in")
            val expireDate = lastUpdatedDate.plusDays(expireIn.toLong())

            return expireDate
        }

    val isOutDated: Boolean?
        get() {
            return expireDate?.isBefore(LocalDate.now())
        }

    val publishLevel: PublishLevel?
        get() {
            return if (registeredWorldData.getString("$uuid.publish_level") in PublishLevel.entries.map { it.toString() }) {
                PublishLevel.valueOf(registeredWorldData.getString("$uuid.publish_level")!!)
            } else null
        }

    val fixedData: List<String>
        get() {
            return listOf(
                "§7- §fUUID: §b$uuid",
                "§7- §fWorld Name: §b$name",
                "§7- §fSource: §b$sourceWorldName",
                "§7- §fLast Updated: ${lastUpdated.toString()} (Expires in ${
                    expireDate?.toEpochDay()?.minus(LocalDate.now().toEpochDay())
                })",
                "§7- §fOwner: §b${owner?.displayName}",
                "§7- §fMembers: §b${members!!.joinToString()}",
            )
        }

    val dataSection: ConfigurationSection?
        get() {
            return if (isRegistered) {
                DataFiles.worldData.getConfigurationSection(uuid)
            } else null
        }

    fun initiate(sourceWorldName: String, owner: Player, registerWorldName: String?): Boolean {
        if (!mvWorldManager.mvWorlds.any { it.name == sourceWorldName }) {

            // clone world
            mvWorldManager.cloneWorld(sourceWorldName, "my_world.$uuid")

            val expireIn = configData.getInt("default_expire_days")

            //
            val worldName = registerWorldName ?: "my_world.${owner.displayName}"

            // template
            val sourceWorldOrigin = DataFiles.templateSetting.getString("$sourceWorldName.origin_location")!!

            // border
            val borderSize = DataFiles.config.getInt("default_border_size")

            // register
            registeredWorldData.createSection(uuid)
            registeredWorldData.set("$uuid.world_name", worldName)
            registeredWorldData.set("$uuid.source_world", sourceWorldName)
            registeredWorldData.set("$uuid.last_updated", LocalDate.now().toString())
            registeredWorldData.set("$uuid.expire_in", expireIn)
            registeredWorldData.set("$uuid.owner", owner.uniqueId.toString())
            registeredWorldData.set("$uuid.players", listOf(owner.uniqueId.toString()))

            registeredWorldData.set("$uuid.spawn_pos_guest", sourceWorldOrigin)
            registeredWorldData.set("$uuid.spawn_pos_member", sourceWorldOrigin)
            registeredWorldData.set("$uuid.border_center_pos", sourceWorldOrigin)

            registeredWorldData.set("$uuid.border_size", borderSize)

            Lib.YamlUtil.save("world_data.yml", registeredWorldData)

            println(
                "MWManager >> Registered world with UUID: $uuid, ExpireDate: ${
                    LocalDate.now().plusDays(expireIn.toLong())
                } (Expires in $expireIn days)"
            )

            return true
        } else return false
    }

    fun deactivate(): Boolean {
        if (mvWorld == null) return false
        mvWorldManager.removePlayersFromWorld("my_world.$uuid")
        println(instance.dataFolder.parentFile.parentFile.path)
        if (mvWorld == null) return false
        val worldFile = File(instance.dataFolder.parentFile.parentFile.path + File.separator + "my_world.$uuid")
        val storageFile = File(instance.dataFolder.path + File.separator + "inactive_worlds")

        FileUtils.copyFolder(worldFile, storageFile)
//        FileUtils.deleteFolder(worldFile)
        mvWorldManager.deleteWorld("my_world.$uuid")

        println("MWManager >> Deactivated world. UUID: $uuid")

        return true
    }

    fun activate(): Boolean {
        if (mvWorld == null) return false
        println(instance.dataFolder.parentFile.parentFile.path)
        if (mvWorld == null) return false
        val worldFolder = File(instance.dataFolder.parentFile.parentFile.path + File.separator)
        val worldFile =
            File(instance.dataFolder.path + File.separator + "inactive_worlds" + File.separator + "my_world.$uuid")

        FileUtils.copyFolder(worldFile, worldFolder)
        FileUtils.deleteFolder(worldFile)
//        mvWorldManager.deleteWorld("my_world.$uuid")

        println("MWManager >> Activated world. UUID: $uuid")

        return true
    }

    fun update(): Boolean {
        if (registeredWorldData.getKeys(false).contains(uuid))

            registeredWorldData.set(
                "$uuid.last_updated",
                LocalDate.now().toString()
            )

        Lib.YamlUtil.save("world_data.yml", registeredWorldData)
        return true
    }

    fun registerPlayer(player: Player, role: MemberRole): Boolean {
        if (isRegistered) {
            val path = "$uuid.${role.toString().lowercase()}"

            if (role == OWNER) {
                DataFiles.worldData.set(path, player.uniqueId.toString())
            } else {
                val list = dataSection!!.getStringList(role.toString().lowercase()) + player.uniqueId.toString()
                DataFiles.worldData.set(path, list)
            }

            DataFiles.save()
            return true
        } else return false
    }

    fun warpPlayer(player: Player): Boolean {
        if (isRegistered) {
            val warpCoordinate = when (player) {
                in members!! -> Lib.stringToBlockLocation(dataSection!!.getString("spawn_pos_guest")!!)!!
                else -> Lib.stringToBlockLocation(dataSection!!.getString("spawn_pos_member")!!)!!
            }

            val warpLocation = Location(
                vanillaWorld!!,
                warpCoordinate[0] + 0.5,
                warpCoordinate[1].toDouble(),
                warpCoordinate[2] + 0.5,
            )
            player.teleport(warpLocation)
            player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 2.0f)

            return true

        } else return false
    }

    fun expand(method: ExpandMethod): Boolean {
        if (isRegistered) {
            val borderSize = vanillaWorld!!.worldBorder.size

            // メモ: North(-Z) West(-X) South(+Z) East(+X)
            val borderCenterPosX = dataSection!!.getString("border_center_pos")!!.split(",")[0].toInt()
            val borderCenterPosY = dataSection!!.getString("border_center_pos")!!.split(",")[1].toInt()
            val borderCenterPosZ = dataSection!!.getString("border_center_pos")!!.split(",")[2].toInt()

            val borderCenterLocation = when (method) {
                CENTER -> Location(
                    vanillaWorld,
                    borderCenterPosX + 0.5,
                    borderCenterPosY.toDouble(),
                    borderCenterPosZ + 0.5
                )

                LEFT_UP -> Location(
                    vanillaWorld,
                    borderCenterPosX - borderSize + 0.5,
                    borderCenterPosY.toDouble(),
                    borderCenterPosZ - borderSize + 0.5
                )

                LEFT_DOWN -> Location(
                    vanillaWorld,
                    borderCenterPosX - borderSize + 0.5,
                    borderCenterPosY.toDouble(),
                    borderCenterPosZ + borderSize + 0.5
                )

                RIGHT_UP -> Location(
                    vanillaWorld,
                    borderCenterPosX + borderSize + 0.5,
                    borderCenterPosY.toDouble(),
                    borderCenterPosZ + borderSize + 0.5
                )

                RIGHT_DOWN -> Location(
                    vanillaWorld,
                    borderCenterPosX + borderSize + 0.5,
                    borderCenterPosY.toDouble(),
                    borderCenterPosZ - borderSize + 0.5
                )
            }

            // ワールドデータの変更
            vanillaWorld!!.worldBorder.center = borderCenterLocation
            vanillaWorld!!.worldBorder.size += borderSize

            // データファイルへの書き込み
            DataFiles.worldData.set(
                "$uuid.border_center_pos",
                "${borderCenterLocation.blockX + 0.5}, ${borderCenterLocation.blockY}, ${borderCenterLocation.blockZ + 0.5}"
            )
            DataFiles.save()

            return true
        } else return false
    }

    fun sync(): Boolean {
//        if (isRegistered) {
//            // world border
//            vanillaWorld!!.worldBorder.size = dataSection!!.getInt("border_size").toDouble()
//            vanillaWorld!!.worldBorder. = dataSection!!.getInt("border_size").toDouble()
//        }

        return true
    }
}

