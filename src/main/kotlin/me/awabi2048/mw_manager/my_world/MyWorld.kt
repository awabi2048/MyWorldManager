package me.awabi2048.mw_manager.my_world

import com.onarandombox.MultiverseCore.api.MultiverseWorld
import com.onarandombox.MultiverseCore.utils.FileUtils
import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.invitationCodeMap
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.Main.Companion.recruitmentCodeMap
import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.my_world.ExpandMethod.*
import me.awabi2048.mw_manager.my_world.MemberRole.OWNER
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.io.File
import java.time.LocalDate
import java.util.*
import kotlin.math.pow

/**
 * 存在のいかんにかかわらずインスタンス化できます。
 * @property uuid ワールドのUUID。
 */
class MyWorld(val uuid: String) {
    val isRegistered: Boolean
        get() {
            return DataFiles.worldData.getKeys(false).contains(uuid)
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
    var name: String?
        get() {
            return if (isRegistered) {
                dataSection?.getString("name")
            } else null
        }

        set(value) {
            if (value != null && isRegistered) {
                DataFiles.worldData.set("$uuid.name", value)
                DataFiles.save()
            }
        }

    var description: String?
        get() {
            return if (isRegistered) {
                dataSection?.getString("description")
            } else null
        }

        set(value) {
            if (value != null && isRegistered) {
                DataFiles.worldData.set("$uuid.description", value)
                DataFiles.save()
            }
        }

    /**
     * @return オーナーとして登録されているプレイヤー。登録されていない場合はnullを返す。
     */
    val owner: Player?
        get() {
            val ownerUUID = DataFiles.worldData.getString("$uuid.owner") ?: return null
            return Bukkit.getPlayer(UUID.fromString(ownerUUID))
        }

    private val dataSection: ConfigurationSection?
        get() {
            return if (isRegistered) {
                DataFiles.worldData.getConfigurationSection(uuid)
            } else null
        }

    val members: Set<OfflinePlayer>?
        get() {
            return if (isRegistered) {
                dataSection!!.getStringList("member").map { Bukkit.getOfflinePlayer(UUID.fromString(it)) }.toSet()
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
            return DataFiles.worldData.getString("$uuid.source_world")
        }

    val lastUpdated: LocalDate?
        get() {
            val dateString = DataFiles.worldData.getString("$uuid.last_updated") ?: return null
            return LocalDate.parse(dateString)
        }

    val expireDate: LocalDate?
        get() {
            val lastUpdatedString = DataFiles.worldData.getString("$uuid.last_updated") ?: return null
            val lastUpdatedDate = LocalDate.parse(lastUpdatedString)
            val expireIn = DataFiles.worldData.getInt("$uuid.expire_in")
            val expireDate = lastUpdatedDate.plusDays(expireIn.toLong())

            return expireDate
        }

    val isOutDated: Boolean?
        get() {
            return expireDate?.isBefore(LocalDate.now())
        }

    var publishLevel: PublishLevel?
        get() {
            return if (DataFiles.worldData.getString("$uuid.publish_level") in PublishLevel.entries.map { it.toString() }) {
                PublishLevel.valueOf(DataFiles.worldData.getString("$uuid.publish_level")!!)
            } else null
        }
        set(value) {
            if (isRegistered && value != null) {
                DataFiles.worldData.set("$uuid.publish_level", value.toString())
                DataFiles.save()
            }
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

    var borderExpansionLevel: Int?
        get() {
            return if (isRegistered) {
                dataSection!!.getInt("border_expansion_level")
            } else null
        }
        set(value) {
            DataFiles.worldData.set("$uuid.border_expansion_level", value)
        }

    val expandCost: Int?
        get() {
            return borderExpansionLevel?.plus(1)?.times(10)
        }

    private val borderSize: Double?
        get() {
            if (isRegistered) {
                return (Config.borderSizeBase * 2.0.pow(borderExpansionLevel!!.toDouble()))
            } else return null
        }

    var borderCenter: Location?
        get() {
            if (isRegistered) {
                val raw = dataSection?.getString("border_center_pos")!!
                return Lib.stringToBlockLocation(vanillaWorld!!, raw)
            } else return null
        }
        set(value) {
            if (value != null) {
                DataFiles.worldData.set("$uuid.border_center_pos", Lib.locationToString(value))
                DataFiles.save()
            }
        }

    var iconMaterial: Material?
        get() {
            if (isRegistered) {
                val materialString = dataSection!!.getString("icon")!!
                return Material.valueOf(materialString)
            } else return null
        }
        set(value) {
            if (value != null && isRegistered) {
                DataFiles.worldData.set("$uuid.icon", value.name)
                DataFiles.save()
            }
        }

    var memberSpawnLocation: Location?
        get() {
            if (isRegistered) {
                val stringPos = dataSection?.getString("spawn_pos_member") ?: return null
                return Lib.stringToBlockLocation(vanillaWorld!!, stringPos)
            } else return null
        }
        set(value) {
            if (value != null) {
                val stringPos = Lib.locationToString(value)
                DataFiles.worldData.set("$uuid.spawn_pos_member", stringPos)
                DataFiles.save()
            }
        }

    var guestSpawnLocation: Location?
        get() {
            if (isRegistered) {
                val stringPos = dataSection?.getString("spawn_pos_guest") ?: return null
                return Lib.stringToBlockLocation(vanillaWorld!!, stringPos)
            } else return null
        }
        set(value) {
            if (value != null) {
                val stringPos = Lib.locationToString(value)
                DataFiles.worldData.set("$uuid.spawn_pos_guest", stringPos)
                DataFiles.save()
            }
        }

    fun initiate(templateWorldName: String, owner: Player, registerWorldName: String?): Boolean {
        if (DataFiles.templateSetting.getKeys(false).contains(templateWorldName)) {
            // clone world
            mvWorldManager.cloneWorld(templateWorldName, "my_world.$uuid")

            val expireIn = Config.defaultExpireDays

            //
            val worldName = registerWorldName ?: "my_world.${owner.displayName}"

            // template
            val sourceWorldOrigin = Lib.locationToString(TemplateWorld(templateWorldName).originLocation)

            // register
            val map = mapOf(
                "name" to worldName,
                "description" to "${owner.displayName}のワールド",
                "icon" to Material.GRASS_BLOCK.toString(),
                "source_world" to templateWorldName,
                "last_updated" to LocalDate.now().toString(),
                "expire_in" to expireIn,
                "owner" to owner.uniqueId.toString(),
                "member" to listOf(owner.uniqueId.toString()),
                "publish_level" to PublishLevel.PRIVATE.toString(),
                "spawn_pos_guest" to sourceWorldOrigin,
                "spawn_pos_member" to sourceWorldOrigin,
                "border_center_pos" to sourceWorldOrigin,
                "border_expansion_level" to 0,
            )

            DataFiles.worldData.createSection(uuid, map)
            DataFiles.save()

            instance.logger.info("Registered world. UUID: $uuid, Expire Date: ${
                LocalDate.now().plusDays(expireIn.toLong())
            } (Expires in $expireIn days)")

            return true
        } else return false
    }

    fun deactivate(): Boolean {
        if (mvWorld == null) return false
        mvWorldManager.removePlayersFromWorld("my_world.$uuid")
//        println(instance.dataFolder.parentFile.parentFile.path)
        if (mvWorld == null) return false
        val worldFile = File(instance.dataFolder.parentFile.parentFile.path + File.separator + "my_world.$uuid")
        val storageFile = File(instance.dataFolder.path + File.separator + "inactive_worlds")

        FileUtils.copyFolder(worldFile, storageFile)
//        FileUtils.deleteFolder(worldFile)
        mvWorldManager.deleteWorld("my_world.$uuid")

        instance.logger.info("World deactivated. UUID: $uuid")

        return true
    }

    fun activate(): Boolean {
        if (mvWorld == null) return false
//        println(instance.dataFolder.parentFile.parentFile.path)
        if (mvWorld == null) return false
        val worldFolder = File(instance.dataFolder.parentFile.parentFile.path + File.separator)
        val worldFile =
            File(instance.dataFolder.path + File.separator + "inactive_worlds" + File.separator + "my_world.$uuid")

        FileUtils.copyFolder(worldFile, worldFolder)
        FileUtils.deleteFolder(worldFile)
//        mvWorldManager.deleteWorld("my_world.$uuid")

        instance.logger.info("World activated. UUID: $uuid")

        return true
    }

    fun update(): Boolean {
        if (DataFiles.worldData.getKeys(false).contains(uuid))

            DataFiles.worldData.set(
                "$uuid.last_updated",
                LocalDate.now().toString()
            )

        Lib.YamlUtil.save("world_data.yml", DataFiles.worldData)
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
            if (Bukkit.getWorld("my_world.$uuid") == null) {
                Bukkit.createWorld(WorldCreator("my_world.$uuid"))
            }

            val warpLocation = when (player) {
                in members!! -> memberSpawnLocation!!
                else -> guestSpawnLocation!!
            }

            player.teleport(warpLocation)
            player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 2.0f)

            player.sendMessage("§8【§a${name}§8】§7にワープしました。")

            members?.filterIsInstance<Player>()?.filter { it != player }?.forEach {
                it.sendMessage("§e${player.displayName}さん§7があなたのワールドを訪れました！")
                it.playSound(it, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f)
            }

            return true

        } else return false
    }

    fun expand(method: ExpandMethod): Boolean {
        if (isRegistered) {
            // メモ: North(-Z) West(-X) South(+Z) East(+X)
            val centerPos = borderCenter!!
            val newBorderCenterLocation = when (method) {
                CENTER -> centerPos
                LEFT_UP -> centerPos.add(-borderSize!! / 2, 0.0, -borderSize!! / 2)
                LEFT_DOWN -> centerPos.add(-borderSize!! / 2, 0.0, borderSize!! / 2)
                RIGHT_UP -> centerPos.add(borderSize!! / 2, 0.0, borderSize!! / 2)
                RIGHT_DOWN -> centerPos.add(+borderSize!! / 2, 0.0, -borderSize!! / 2)
            }

            println("size: $borderSize, before: $centerPos, after: $newBorderCenterLocation")

            // データファイルへの書き込み
            DataFiles.worldData.set(
                "$uuid.border_center_pos",
                Lib.locationToString(newBorderCenterLocation)
            )

            DataFiles.worldData.set(
                "$uuid.border_expansion_level",
                borderExpansionLevel!! + 1
            )
            DataFiles.save()

            // ワールドデータの変更
            vanillaWorld!!.worldBorder.apply {
                setCenter(newBorderCenterLocation.x, newBorderCenterLocation.z)
                setSize(borderSize!!, 0L)
            }

            return true
        } else return false
    }

    // ワールドロード時に実行
    fun sync(): Boolean {
        if (isRegistered) {
            // world border
            vanillaWorld!!.worldBorder.center = borderCenter!!
            vanillaWorld!!.worldBorder.size = borderSize!!

            //
            mvWorld!!.setAllowAnimalSpawn(false)
            mvWorld!!.setAllowMonsterSpawn(false)

            vanillaWorld!!.setGameRule(GameRule.KEEP_INVENTORY, true)
            vanillaWorld!!.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
            vanillaWorld!!.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
            vanillaWorld!!.setGameRule(GameRule.MOB_GRIEFING, false)
        }

        return true
    }

    fun invitePlayer(inviter: Player, target: Player) {
        // 招待コードを生成、登録
        val invitationCode = UUID.randomUUID().toString()
        invitationCodeMap[invitationCode] = uuid

        val text = Component.text("§7«§eクリックしてワープ§7»")
            .hoverEvent(HoverEvent.showText(Component.text("")))
            .clickEvent(ClickEvent.runCommand("/mwm_invite_accept $invitationCode"))

        target.sendMessage("§b${inviter}さん§7がワールドに招待しました！ $text")
        target.playSound(target, Sound.ENTITY_CAT_AMBIENT, 1.0f, 1.0f)
    }

    fun recruitPlayer(inviter: Player, target: Player) {
        // 招待コードを生成、登録
        val recruitmentCode = UUID.randomUUID().toString()
        recruitmentCodeMap[recruitmentCode] = uuid

        val text = Component.text("§7«§eクリックして承認§7»")
            .hoverEvent(HoverEvent.showText(Component.text("")))
            .clickEvent(ClickEvent.runCommand("/mwm_recruit_accept $recruitmentCode"))

        target.sendMessage("§b${inviter}さん§7がワールドのメンバーに招待しました！ $text")
        target.playSound(target, Sound.ENTITY_CAT_AMBIENT, 1.0f, 2.0f)
    }
}

