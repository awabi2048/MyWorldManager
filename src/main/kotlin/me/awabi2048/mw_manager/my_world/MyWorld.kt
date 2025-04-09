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
import me.awabi2048.mw_manager.my_world.world_property.ExpandMethod
import me.awabi2048.mw_manager.my_world.world_property.ExpandMethod.*
import me.awabi2048.mw_manager.my_world.world_property.MemberRole
import me.awabi2048.mw_manager.my_world.world_property.MemberRole.OWNER
import me.awabi2048.mw_manager.my_world.world_property.PublishLevel
import me.awabi2048.mw_manager.my_world.world_property.WorldActivityState
import me.awabi2048.mw_manager.portal.WorldPortal
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.absoluteValue
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
                mvWorldManager.getMVWorld(vanillaWorld).alias = value
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

    var members: MutableMap<OfflinePlayer, MemberRole>? = null
        get() {
            return if (isRegistered) {
                dataSection!!.getConfigurationSection("member")!!.getKeys(false)
                    .associate {
                        Bukkit.getOfflinePlayer(UUID.fromString(it)) to MemberRole.valueOf(dataSection!!.getString("member.$it")!!)
                    }.toMutableMap()
            } else null
        }
        set(value) {
            if (value != null && isRegistered) {
                val removedMembers =
                    if (members!!.keys.containsAll(value.keys)) members!!.keys - value.keys else null // field → 旧値

                val addedMembers =
                    if (value.keys.containsAll(members!!.keys)) value.keys - members!!.keys else null // field → 旧値

                // メンバー追放時マクロ
                removedMembers?.forEach {
                    val macroExecutor = MacroExecutor(MacroFlag.OnWorldMemberRemoved(this, it))
                    macroExecutor.run()
                }

                // メンバー追加時マクロ
                addedMembers?.forEach {
                    val macroExecutor = MacroExecutor(MacroFlag.OnWorldMemberAdded(this, it))
                    macroExecutor.run()
                }

                // データファイルに書き込み: オーナーが一番上に来るように
                DataFiles.worldData.set("$uuid.member", null)
                DataFiles.worldData.set(
                    "$uuid.member.${value.entries.find { it.value == OWNER }!!.key.uniqueId}",
                    OWNER.name
                )

                value.entries.filter { it.value != OWNER }.forEach {
                    DataFiles.worldData.set("$uuid.member.${it.key.uniqueId}", it.value.name)
                }
                DataFiles.save()
                DataFiles.loadAll()
            }

            field = value
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

    val iconItem: ItemStack?
        get() {
            try {// lore 生成
                val bar = "§7" + "━".repeat(30)
                val index = "§f§l|"

                val expireState = when (isOutDated!!) {
                    true -> "$index §c§n${Lib.formatDate(expireDate!!)} に期限切れ §8(§c§n${
                        ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            expireDate
                        ).absoluteValue
                    }日前§8)"

                    false -> "$index §8§n${Lib.formatDate(expireDate!!)} §8に期限切れ (§8§n${
                        ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            expireDate
                        ).absoluteValue
                    }日後§8)"
                }

                val ownerOnlineState = when (owner!!.isOnline) {
                    true -> "§a"
                    false -> "§c"
                }

                val lore = listOf(
                    bar,
                    "$index §7${description}",
                    "$index §7オーナー $ownerOnlineState${owner!!.name}",
                    "$index §7拡張レベル §e§l${borderExpansionLevel}§7/${Config.borderExpansionMax}",
                    bar,
                    "$index §7最終更新日時 §b${Lib.formatDate(lastUpdated!!)}",
                    expireState,
                    "$index §7メンバー §e${members!!.size}人 §7(オンライン: §a${members!!.count { it.key.isOnline }}人§7)",
                    "$index §7公開レベル §e${publishLevel!!.japaneseName}",
                    bar,
                )

                // item
                val icon = ItemStack(iconMaterial ?: return null)
                icon.editMeta { meta ->
                    meta.itemName(Component.text("§7【§a${name}§7】"))
                    meta.lore(lore.map { Component.text(it) })
                    if (isOutDated == true) meta.setEnchantmentGlintOverride(true)
                }

                return icon
            } catch (e: Exception) {
                return null
            }
        }

    var borderExpansionLevel: Int?
        get() {
            return if (isRegistered) {
                dataSection!!.getInt("border_expansion_level")
            } else null
        }
        set(value) {
            DataFiles.worldData.set("$uuid.border_expansion_level", value)
            DataFiles.save()
        }

    val expandCost: Int
        get() {
            return Config.baseWorldExpandCost * Config.worldExpandCostIndex.toDouble().pow(borderExpansionLevel!!.toDouble()).toInt()
        }

    private val borderSize: Double?
        get() {
            return if (isRegistered) {
                (Config.borderSizeBase * 2.0.pow(borderExpansionLevel!!.toDouble()))
            } else null
        }

    private var borderCenter: Location?
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

                // アクティベート → ワールド数がグローバルの最大値を超えそうなら止める
                if (value == WorldActivityState.ACTIVE) {
                    if (MyWorldManager.registeredMyWorlds.filter { it.activityState == WorldActivityState.ACTIVE }.size + 1 > Config.globalWorldCountMax) {
//                        println("MAX: ${Config.globalWorldCountMax}, CURRENT: ${MyWorldManager.registeredMyWorlds.filter { it.activityState == WorldActivityState.ACTIVE }}")
                        instance.logger.info("Failed to activate archived MyWorld: Out of Storage. UUID: $uuid")
                    }
                }

                // 状態をセット
                changeActivityState(value)
            }
        }

    val isRealWorld: Boolean
        get() {
            return !(Bukkit.getWorld("my_world.$uuid") == null && !File(instance.server.worldContainer.path + File.separator + "archived_worlds" + File.separator + "my_world.$uuid").exists())
        }

    val visitors: Set<Player>?
        get() {
            return vanillaWorld?.players?.filter { it !in (members?.keys ?: return null) }?.toSet()
        }

    fun initiate(templateWorldName: String, owner: Player, registerWorldName: String): Boolean {
        if (DataFiles.templateSetting.getKeys(false).contains(templateWorldName)) {
            // clone world
            mvWorldManager.cloneWorld(templateWorldName, "my_world.$uuid")

            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    Bukkit.createWorld(WorldCreator("my_world.$uuid"))
                    vanillaWorld!!.worldBorder.warningDistance = 0
                },
                5L
            )

            val expireIn = Config.defaultExpireDays

            // template
            val sourceWorldOrigin = Lib.locationToString(TemplateWorld(templateWorldName).originLocation)

            // member
            val playerMap = mapOf(
                owner.uniqueId.toString() to OWNER.name
            )

            // register
            val map = mapOf(
                "name" to registerWorldName,
                "description" to "${owner.name}のワールド",
                "icon" to Material.GRASS_BLOCK.toString(),
                "source_world" to templateWorldName,
                "last_updated" to LocalDate.now().toString(),
                "expire_in" to expireIn,
                "owner" to owner.uniqueId.toString(),
                "member" to playerMap,
                "publish_level" to PublishLevel.PRIVATE.toString(),
                "spawn_pos_guest" to sourceWorldOrigin,
                "spawn_pos_member" to sourceWorldOrigin,
                "border_center_pos" to sourceWorldOrigin,
                "border_expansion_level" to 0,
                "activity_state" to WorldActivityState.ACTIVE.name
            )

            DataFiles.worldData.createSection(uuid, map)
            DataFiles.save()

            instance.logger.info(
                "Registered world. UUID: $uuid, Expire Date: ${
                    LocalDate.now().plusDays(expireIn.toLong())
                } (Expires in $expireIn days)"
            )

            // マクロ実行
            val macroExecutor = MacroExecutor(MacroFlag.OnWorldCreated(this, owner))
            macroExecutor.run()

            return true
        } else return false
    }

    private fun changeActivityState(state: WorldActivityState) {
        // データファイルに書き込み
        DataFiles.worldData.set("$uuid.activity_state", state.toString())
        DataFiles.save()

        // データの移動
        val worldContainerFile = instance.server.worldContainer
        val worldDataFile = File(worldContainerFile.path + File.separator + "my_world.$uuid")
        val archivedDataFile =
            File(worldContainerFile.path + File.separator + "archived_worlds" + File.separator + "my_world.$uuid")

        // アーカイブデータのメインディレクトリを作成
        File(worldContainerFile.path + File.separator + "archived_worlds" + File.separator).mkdir()

//         println("$worldDataFile, $worldContainerFile, $archivedDataFile")

        when (state) {
            WorldActivityState.ACTIVE -> {
                // コピー後、削除（アーカイブ　→　ワールド）
                Bukkit.getScheduler().runTaskAsynchronously(
                    instance,
                    Runnable {
                        FileUtils.copyFolder(archivedDataFile, worldDataFile)
                        FileUtils.deleteFolder(archivedDataFile)
                    }
                )

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        Bukkit.createWorld(WorldCreator("my_world.$uuid")) // 同一 tick で処理すると生成されてしまう
                        mvWorldManager.loadWorld("my_world.$uuid")
//                                mvWorldManager.addWorld("my_world.$uuid", World.Environment.NORMAL, null, WorldType.NORMAL, false, null)
                    },
                    10L
                )

                publishLevel = PublishLevel.CLOSED

                instance.logger.info("World activated. UUID: $uuid")
            }

            WorldActivityState.ARCHIVED -> {
                // アーカイブされるワールドデータのディレクトリを作成
                archivedDataFile.mkdir()

                Lib.escapePlayers(vanillaWorld!!.players.toSet())

                // コピー後、削除（アーカイブ　→　ワールド）
                // mvWorldManager.removeWorldFromConfig("my_world.$uuid")
                Bukkit.unloadWorld("my_world.$uuid", true)

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        FileUtils.copyFolder(worldDataFile, archivedDataFile)
                    }, 10L
                )

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        FileUtils.deleteFolder(worldDataFile)
                        System.gc()
                    }, 20L
                )

                instance.logger.info("World archived. UUID: $uuid")
            }
        }
    }

    fun update(): Boolean {
        if (isRegistered) {

            // 最終更新を現在日時に変更
            DataFiles.worldData.set(
                "$uuid.last_updated",
                LocalDate.now().toString()
            )
            DataFiles.save()

            return true
        } else return false
    }

    fun registerPlayer(joinedPlayer: Player, role: MemberRole): Boolean {
        if (isRegistered && activityState == WorldActivityState.ACTIVE) {
//            members!![joinedPlayer] = role
            members!!.forEach {
                val player = it.key.player

                player?.sendMessage("§a${joinedPlayer.name} さんがワールドメンバーになりました！")
                player?.playSound(it.key.player!!, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f)
            }

            members = members!!.plus(Pair(joinedPlayer, role)).toMutableMap()

            return true
        } else return false
    }

    fun warpPlayer(player: Player): Boolean {
        if (isRegistered && activityState == WorldActivityState.ACTIVE) {

            // ワールドがメモリ上になければ登録
            if (Bukkit.getWorld("my_world.$uuid") == null) {
                Bukkit.createWorld(WorldCreator("my_world.$uuid"))
            }

            // 封鎖中のワールドなら中止
            if (publishLevel == PublishLevel.CLOSED && player !in members!!) {
                player.sendMessage("§cこのワールドは現在${PublishLevel.CLOSED.japaneseName}です。ワールドメンバー以外はワープできません。")
            }

            val warpLocation = when (player) {
                in members!! -> memberSpawnLocation!!
                else -> guestSpawnLocation!!
            }

            val sendNotification = player.world != vanillaWorld && player !in members!!.keys

            if (sendNotification) {
                members?.keys?.filter { it.player?.isOnline == true && it.player != player }?.map { it.player }
                    ?.forEach {
                        it?.sendMessage("§e${player.name}さん§7があなたのワールドを訪れました！§8【§a${name}§8】")
                        it?.playSound(it, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f)
                    }
            }

            player.teleport(warpLocation)
            player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 2.0f)

            player.sendMessage("§8【§a${name}§8】§7にワープしました。")

            // macro execution
            val macroExecutor = MacroExecutor(MacroFlag.OnWorldWarp(this, player))
            macroExecutor.run()

            return true

        } else return false
    }

    fun expand(method: ExpandMethod): Boolean {
        if (isRegistered && activityState == WorldActivityState.ACTIVE) {
            // メモ: North(-Z) West(-X) South(+Z) East(+X)
            val centerPos = borderCenter!!
            val newBorderCenterLocation = when (method) {
                CENTER -> centerPos
                NORTH_WEST -> centerPos.add(Vector(-borderSize!!.toInt() / 2, 0, -borderSize!!.toInt() / 2))
                SOUTH_WEST -> centerPos.add(Vector(-borderSize!!.toInt() / 2, 0, borderSize!!.toInt() / 2))
                NORTH_EAST -> centerPos.add(Vector(borderSize!!.toInt() / 2, 0, -borderSize!!.toInt() / 2))
                SOUTH_EAST -> centerPos.add(Vector(borderSize!!.toInt() / 2, 0, borderSize!!.toInt() / 2))
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
                setCenter(newBorderCenterLocation.blockX.toDouble(), newBorderCenterLocation.blockZ.toDouble())
                setSize(borderSize!!, 0L)
                warningDistance = 0
            }

            return true
        } else return false
    }

    fun invitePlayer(inviter: Player, invitee: Player) {
        if (isRegistered) {
            if (invitee in visitors!!) {
                inviter.sendMessage("§cそのプレイヤーはすでにワールドを訪れています。")
                return
            }

            // 招待コードを生成、登録
            val invitationCode = UUID.randomUUID().toString()
            invitationCodeMap[invitationCode] = uuid

            inviter.sendMessage("§b${invitee.name} さん §7をあなたのワールドに招待しました！")
            val inviteText = Component.text("§b${inviter.name}さん §7があなたをワールドに§e招待§7しました！")
                .append(Component.text("§7«§eクリックしてワープ§7»"))
                .hoverEvent(HoverEvent.showText(Component.text("§7クリックして§8【§e${MyWorld(uuid).name}§8】§7にワープします。")))
                .clickEvent(ClickEvent.runCommand("/mwm_invite_accept $invitationCode"))

            invitee.sendMessage(inviteText)
            invitee.playSound(invitee, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
        }
    }

    fun recruitPlayer(inviter: Player, invitee: Player) {
        // 招待コードを生成、登録
        val recruitmentCode = UUID.randomUUID().toString()
        recruitmentCodeMap[recruitmentCode] = uuid

        invitee.sendMessage("§b${inviter.name}さん§7があなたを§bワールドメンバーに招待§7しました！")

        invitee.sendMessage(
            Component.text("§7«§eクリックして承認§7»")
                .hoverEvent(HoverEvent.showText(Component.text("§aクリックしてメンバーの招待を受け入れます")))
                .clickEvent(ClickEvent.runCommand("/mwm_recruit_accept $recruitmentCode"))
        )

        invitee.playSound(invitee, Sound.ENTITY_CAT_AMBIENT, 1.0f, 2.0f)

        // タイムアウト処理（3分後）
        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                if (recruitmentCodeMap.contains(recruitmentCode)) {
                    inviter.sendMessage("§c一定時間が経過したため、招待がキャンセルされました。")
                    invitee.sendMessage("§c一定時間が経過したため、招待がキャンセルされました。")

                    recruitmentCodeMap.remove(recruitmentCode)
                }
            },
            3 * 60 * 20L
        )
    }

    fun remove(): Boolean {
        if (isRegistered) {

            // プレイヤーを避難
            if (activityState == WorldActivityState.ACTIVE) Lib.escapePlayers(vanillaWorld!!.players.toSet())

            // ワールドのディレクトリの位置をアーカイブ状態に応じて取得
            val worldFolder: File
            if (activityState == WorldActivityState.ACTIVE) {
                worldFolder = vanillaWorld!!.worldFolder
                Bukkit.unloadWorld(vanillaWorld!!, false)
            } else {
                worldFolder =
                    File(Bukkit.getWorldContainer().path + File.separator + "archived_worlds" + File.separator + "my_world.$uuid")
            }

            // mv 設定から除外等
            mvWorldManager.removeWorldFromConfig("my_world.$uuid")

            // 即座に削除するとワールドのアンロードが間に合わずエラーが出る
            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    FileUtils.deleteFolder(worldFolder)

                    DataFiles.worldData.set(uuid, null)
                    DataFiles.portalData.getKeys(false).filter { !(WorldPortal(it).isAvailable) }.forEach {
                        DataFiles.portalData.set(it, null)
                    }
                    DataFiles.save()
                },
                20L
            )

            // マクロ実行
            MacroExecutor(MacroFlag.OnWorldRemoved(this)).run()

            members?.forEach {
                val macroExecutor = MacroExecutor(MacroFlag.OnWorldMemberRemoved(this, it.key))
                macroExecutor.run()
            }

            return true

        } else return false
    }

    fun kickPlayer(executor: Player, target: Player): Boolean {
        if (!isRegistered) return false
        val visitors = vanillaWorld?.players?.filter { it !in members!!.keys } ?: return false
        if (target !in visitors) {
            executor.sendMessage("§cそのプレイヤーはワールドを訪れていません。")
            return false
        }

        executor.sendMessage("§c${target.name} をワールドからキックしました。")
        executor.playSound(executor, Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f)

        target.teleport(Config.escapeLocation!!)
        target.playSound(target, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 2.0f)
        return true
    }
}

