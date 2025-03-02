package me.awabi2048.mw_manager.my_world

import com.onarandombox.MultiverseCore.utils.FileUtils
import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.invitationCodeMap
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.Main.Companion.recruitmentCodeMap
import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.macro_executor.MacroExecutor
import me.awabi2048.mw_manager.macro_executor.MacroFlag
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
import java.time.temporal.ChronoUnit
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
    val owner: OfflinePlayer?
        get() {
            val ownerUUID = DataFiles.worldData.getString("$uuid.owner") ?: return null
            return Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID))
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

    val templateWorldName: String?
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
            val bar = "§7" + "━".repeat(30)
            val index = "§f§l|"

            val expireState = when (isOutDated!!) {
                true -> "$index §c§n${Lib.formatDate(expireDate!!)} に期限切れ §8(§c§n${
                    ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        expireDate
                    )
                }日前§8)"

                false -> "$index §8§n${Lib.formatDate(expireDate!!)} §8に期限切れ (§8§n${
                    ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        expireDate
                    )
                }日後§8)"
            }

//            println("Creating fixed information: $uuid")
            val ownerOnlineState = when (owner!!.isOnline) {
                true -> "§a"
                false -> "§c"
            }

            return listOf(
                bar,
                "$index §7${description}",
                "$index §7オーナー $ownerOnlineState${owner!!.name}",
                "$index §7拡張レベル §e§l${borderExpansionLevel}§7/${Config.borderExpansionMax}",
                bar,
                "$index §7最終更新日時 §b${Lib.formatDate(lastUpdated!!)}",
                expireState,
                "$index §7メンバー §e${members!!.size}人 §7(オンライン: §a${members!!.filter { it.isOnline }.size}人§7)",
                "$index §7公開レベル §e${publishLevel!!.toJapanese()}",
                bar,
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

    var activityState: WorldActivityState?
        get() {
            if (isRegistered) {
                return WorldActivityState.valueOf(dataSection?.getString("activity_state") ?: return null)
            } else return null
        }

        set(value) {
            if (value != null && isRegistered && value != activityState) { // ※ すでのその状態の場合は処理中断する
                // データファイルに書き込み
                DataFiles.worldData.set("$uuid.activity_state", value.toString())
                DataFiles.save()

                // データの移動
                val worldContainerFile = instance.server.worldContainer
                val worldDataFile = File(worldContainerFile.path + File.separator + "my_world.$uuid")
                val archivedDataFile =
                    File(worldContainerFile.path + File.separator + "archived_worlds" + File.separator + "my_world.$uuid")

                File(worldContainerFile.path + File.separator + "archived_worlds" + File.separator).mkdir()
                archivedDataFile.mkdir()

//                println("$worldDataFile, $worldContainerFile, $archivedDataFile")

                when (value) {
                    WorldActivityState.ACTIVE -> {
                        // コピー後、削除（アーカイブ　→　ワールド）
                        Bukkit.getScheduler().runTaskAsynchronously(
                            instance,
                            Runnable {
                                FileUtils.copyFolder(archivedDataFile, worldDataFile)
                                FileUtils.deleteFolder(archivedDataFile)
                            }
                        )

                        Bukkit.createWorld(WorldCreator("my_world.$uuid"))
//                        println(Bukkit.getWorlds())

                        Bukkit.getScheduler().runTaskLater(
                            instance,
                            Runnable {
                                mvWorldManager.loadWorld("my_world.$uuid")
//                                mvWorldManager.addWorld("my_world.$uuid", World.Environment.NORMAL, null, WorldType.NORMAL, false, null)
                            },
                            10L
                        )

                        instance.logger.info("World activated. UUID: $uuid")
                    }

                    WorldActivityState.ARCHIVED -> {

                        mvWorldManager.removePlayersFromWorld("my_world.$uuid")

                        // コピー後、削除（アーカイブ　→　ワールド）
                        FileUtils.copyFolder(worldDataFile, archivedDataFile)
                        mvWorldManager.removeWorldFromConfig("my_world.$uuid")

                        Bukkit.unloadWorld("my_world.$uuid", true)
                        Bukkit.getScheduler().runTaskLater(
                            instance,
                            Runnable {
                            FileUtils.deleteFolder(worldDataFile)
                            System.gc()
                        }, 10L)

                        instance.logger.info("World archived. UUID: $uuid")
                    }
                }
            }
        }

    val isRealWorld: Boolean
        get() {
            return !(Bukkit.getWorld("my_world.$uuid") == null && !File(instance.server.worldContainer.path + File.separator + "archived_worlds" + File.separator + "my_world.$uuid").exists())
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
                "activity_state" to WorldActivityState.ACTIVE.name
            )

            DataFiles.worldData.createSection(uuid, map)
            DataFiles.save()

//            println("$uuid, $map")

            instance.logger.info(
                "Registered world. UUID: $uuid, Expire Date: ${
                    LocalDate.now().plusDays(expireIn.toLong())
                } (Expires in $expireIn days)"
            )

            // macro execution
            val macroExecutor = MacroExecutor(MacroFlag.ON_WORLD_CREATE)
            macroExecutor.run(this, owner)

            return true
        } else return false
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
        if (isRegistered && activityState == WorldActivityState.ACTIVE) {
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

    fun warpPlayer(player: Player, sendNotification: Boolean): Boolean {
        if (isRegistered && activityState == WorldActivityState.ACTIVE) {
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

            if (sendNotification){
                members?.filterIsInstance<Player>()?.filter { it != player }?.forEach {
                    it.sendMessage("§e${player.displayName}さん§7があなたのワールドを訪れました！")
                    it.playSound(it, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f)
                }
            }

            // macro execution
            val macroExecutor = MacroExecutor(MacroFlag.ON_PLAYER_WARP)
            macroExecutor.run(this, player)

            return true

        } else return false
    }

    fun expand(method: ExpandMethod): Boolean {
        if (isRegistered && activityState == WorldActivityState.ACTIVE) {
            // メモ: North(-Z) West(-X) South(+Z) East(+X)
            val centerPos = borderCenter!!
            val newBorderCenterLocation = when (method) {
                CENTER -> centerPos
                LEFT_UP -> centerPos.add(-borderSize!! / 2, 0.0, -borderSize!! / 2)
                LEFT_DOWN -> centerPos.add(-borderSize!! / 2, 0.0, borderSize!! / 2)
                RIGHT_UP -> centerPos.add(borderSize!! / 2, 0.0, borderSize!! / 2)
                RIGHT_DOWN -> centerPos.add(+borderSize!! / 2, 0.0, -borderSize!! / 2)
            }

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
        if (isRegistered && activityState == WorldActivityState.ACTIVE) {
            // world border
            vanillaWorld!!.worldBorder.center = borderCenter!!
            vanillaWorld!!.worldBorder.size = borderSize!!

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

        val inviteText = Component.text("§b${inviter.name}さん §7があなたをワールドに§e招待§7しました！")
            .append(Component.text("§7«§eクリックしてワープ§7»"))
            .hoverEvent(HoverEvent.showText(Component.text("§7クリックして§8【§e${MyWorld(uuid).name}§8】§7にワープします。")))
            .clickEvent(ClickEvent.runCommand("/mwm_invite_accept $invitationCode"))

        target.sendMessage(inviteText)
        target.playSound(target, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
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

    fun delete(): Boolean {
        if (isRegistered && activityState == WorldActivityState.ACTIVE) {

            mvWorldManager.removePlayersFromWorld("my_world.$uuid")
            mvWorldManager.removeWorldFromConfig("my_world.$uuid")
            mvWorldManager.deleteWorld("my_world.$uuid")

            DataFiles.worldData.set(uuid, null)
            DataFiles.save()

            return true

        } else return false
    }
}

