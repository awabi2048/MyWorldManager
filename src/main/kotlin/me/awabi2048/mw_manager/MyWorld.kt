package me.awabi2048.mw_manager

import com.onarandombox.MultiverseCore.api.MultiverseWorld
import com.onarandombox.MultiverseCore.utils.FileUtils
import jdk.vm.ci.meta.Local
import me.awabi2048.mw_manager.Main.Companion.configData
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.Main.Companion.registeredWorldData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.time.LocalDate

// worldName -> ワールド名
class MyWorld(val uuid: String) {
    val world: MultiverseWorld?
        get() {
            return mvWorldManager.mvWorlds.find { it.name == "my_world.$uuid"}
        }

    val name: String?
        get() {
            return registeredWorldData.getString("$uuid.world_name")
        }

    val owner: Player?
        get() {
            val ownerUUID = registeredWorldData.getString("$uuid.owner")?: return null
            return Bukkit.getPlayer(ownerUUID)
        }

    val sourceWorldName: String?
        get() {
            return registeredWorldData.getString("$uuid.source_world")
        }

    val lastUpdated: LocalDate?
        get() {
            val dateString = registeredWorldData.getString("$uuid.last_updated")?: return null
            return LocalDate.parse(dateString)
        }

    val expireDate: LocalDate?
        get() {
            val lastUpdatedString = registeredWorldData.getString("$uuid.last_updated")?: return null
            val lastUpdatedDate = LocalDate.parse(lastUpdatedString)
            val expireIn = registeredWorldData.getInt("$uuid.expire_in")
            val expireDate = lastUpdatedDate.plusDays(expireIn.toLong())

            return expireDate
        }

    val players: List<Player>
        get() {
            return registeredWorldData.getStringList("$uuid.players").map {Bukkit.getPlayer(it)!!}
        }

    val isOutDated: Boolean?
        get() {
            return expireDate?.isBefore(LocalDate.now())
        }

    val fixedData: List<String>
        get() {
            return listOf(
                "§7- §fUUID: §b$uuid",
                "§7- §fWorld Name: §b$name",
                "§7- §fSource: §b$sourceWorldName",
                "§7- §fLast Updated: ${lastUpdated.toString()} (Expires in ${expireDate?.toEpochDay()?.minus(LocalDate.now().toEpochDay())})",
                "§7- §fOwner: §b${owner?.displayName}",
                "§7- §fJoined Player: §b${players.joinToString()}",
            )
        }

    fun initiate(sourceWorldName: String, owner: Player): Boolean {
        if (!mvWorldManager.mvWorlds.any {it.name == sourceWorldName}) return false

        // clone world
        mvWorldManager.cloneWorld(sourceWorldName, "my_world.$uuid")

        val expireIn = configData.getInt("default_expire_days")

        // register
        registeredWorldData.createSection(uuid)
        registeredWorldData.set("$uuid.world_name", "my_world.${owner.displayName}")
        registeredWorldData.set("$uuid.source_world", sourceWorldName)
        registeredWorldData.set("$uuid.last_updated", LocalDate.now().toString())
        registeredWorldData.set("$uuid.expire_in", expireIn)
        registeredWorldData.set("$uuid.owner", owner.uniqueId.toString())
        registeredWorldData.set("$uuid.players", listOf(owner.uniqueId.toString()))

        Lib.YamlUtil.save("world_data.yml", registeredWorldData)

        println(
            "MWManager >> Registered world with UUID: $uuid, ExpireDate: ${
                LocalDate.now().plusDays(expireIn.toLong())
            } (Expires in $expireIn days)"
        )
        return true
    }

    fun deactivate(): Boolean {
        if (world == null) return false
        mvWorldManager.removePlayersFromWorld("my_world.$uuid")
        println(instance.dataFolder.parentFile.parentFile.path)
        if (world == null) return false
        val worldFile = File(instance.dataFolder.parentFile.parentFile.path + File.separator + world!!.name)
        val storageFile = File(instance.dataFolder.path + File.separator + "inactive_worlds")

        FileUtils.copyFolder(worldFile, storageFile)
//        FileUtils.deleteFolder(worldFile)
        mvWorldManager.deleteWorld("my_world.$uuid")

        println("MWManager >> Unloaded and Deleted world. UUID: $uuid")

        return true
    }

//    fun activate(): Boolean {
//    }

    fun update(): Boolean {
        if (registeredWorldData.getKeys(false).contains(uuid))

        registeredWorldData.set(
            "$uuid.last_updated",
            LocalDate.now().toString()
        )

        Lib.YamlUtil.save("world_data.yml", registeredWorldData)
        return true
    }

    fun addPlayer(player: Player) {

    }
}

