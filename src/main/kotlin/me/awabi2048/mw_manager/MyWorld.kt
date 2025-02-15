package me.awabi2048.mw_manager

import com.onarandombox.MultiverseCore.api.MultiverseWorld
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.Main.Companion.registeredWorldData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.LocalDate

// worldName -> ワールド名
class MyWorld(val uuid: String) {
    val world: MultiverseWorld?
        get() {
            return mvWorldManager.mvWorlds.find { it.name == "my_world.$uuid"}
        }

    val alias: String?
        get() {
            return world?.alias
        }

    val owner: Player?
        get() {
            return if (alias != null) {
                println(alias)
                registeredWorldData
            } else null
        }

    val expireDate: LocalDate?
        get() {
            if (MyWorldManager.registeredWorld.contains(this)) {
                if (!registeredWorldData.contains(alias!!)) return null

                val limitDay = registeredWorldData.getInt("${owner!!.uniqueId}.worlds.$worldName.limit_day")
                val lastUpdatedDate =
                    LocalDate.parse(registeredWorldData.getString("${owner!!.uniqueId}.worlds.$worldName.last_updated"))

                val limitDate = lastUpdatedDate.plusDays(limitDay.toLong())
                return limitDate
            } else return null
        }

    val isOutDated: Boolean?
        get() {
            return if (owner != null) {
                expireDate?.isBefore(LocalDate.now())
            } else null
        }

    fun register(expireIn: Int): Boolean {
        if (mvWorldManager.mvWorlds.any { it.name == worldName }) {
            registeredWorldData.createSection(worldName)
            registeredWorldData.set("$worldName.last_updated_date", LocalDate.now().toString())
            registeredWorldData.set("$worldName.expire_in", expireIn)

            Lib.YamlUtil.save("world_data.yml", registeredWorldData)

            println(
                "MWManager >> Registered world with Name: $worldName, ExpireDate: ${
                    LocalDate.now().plusDays(expireIn.toLong())
                } (Expires in $expireIn days)"
            )
            return true
        } else return false
    }

    fun remove(): Boolean {
        if (MyWorldManager.registeredWorld.contains(this)) {
            mvWorldManager.removeWorldFromConfig(worldName)
            mvWorldManager.removePlayersFromWorld(worldName)
            mvWorldManager.unloadWorld(worldName)
            mvWorldManager.deleteWorld(worldName)

            println("MWManager >> Unloaded and Deleted world with Name: $worldName, UUID: $owner(${owner!!.name})")

            return true
        } else return false
    }

    fun update(): Boolean {
        if (MyWorldManager.registeredWorld.contains(this)) {
            registeredWorldData.set(
                "${owner!!.uniqueId}.worlds.$worldName.last_update_date",
                LocalDate.now().toString()
            )
            return true
        } else return false
    }
}
