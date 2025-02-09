package me.awabi2048.mw_manager

import com.onarandombox.MultiverseCore.api.MultiverseWorld
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.Main.Companion.registeredWorldData
import java.time.LocalDate

// worldName -> ワールド名
class MyWorld(private val worldName: String) {
    val world: MultiverseWorld?
        get() {
            return mvWorldManager.mvWorlds.find { it.alias.substringAfter("-") == worldName}
        }

    val uuid: String?
        get() {
            return world?.name
        }

    val alias: String?
        get() {
            return world?.alias
        }

    val limitDate: LocalDate?
        get() {
            if (uuid != null){
                if (!registeredWorldData.contains(uuid!!)) return null

                val limitDay = registeredWorldData.getInt("$uuid.limit_day")
                val lastUpdatedDate = LocalDate.parse(registeredWorldData.getString("$uuid.last_updated"))

                val limitDate = lastUpdatedDate.plusDays(limitDay.toLong())
                return limitDate
            } else return null
        }

    val isOutDated: Boolean?
        get() {
            return if (uuid != null) {
                limitDate?.isBefore(LocalDate.now())
            } else null
        }

    private fun checkIsOutDated(): Boolean {
        if (!registeredWorldData.contains(uuid ?: return false)) return false

        val limitDay = registeredWorldData.getInt("$uuid.limit_day")
        val lastUpdatedDate = LocalDate.parse(registeredWorldData.getString("$uuid.last_updated"))

        val limitDate = lastUpdatedDate.plusDays(limitDay.toLong())
        val currentDate = LocalDate.now()

        return limitDate.isBefore(currentDate)
    }

    fun register(limitDay: Int): Boolean {
        if (uuid != null){
            registeredWorldData.createSection(uuid ?: return false)
            registeredWorldData.set("$uuid.last_updated", LocalDate.now().toString())
            registeredWorldData.set("$uuid.limit_day", limitDay)
            registeredWorldData.set("$uuid.world_name", worldName)

            Lib.YamlUtil.save("registered_world.yml", registeredWorldData)

            println("PWManager >> Registered world with Name: $worldName, UUID: $uuid, LimitDate: ${LocalDate.now().plusDays(limitDay.toLong())} ($limitDay days later)")

            return true
        } else return false
    }

    fun remove(): Boolean {
        if (world != null) {
            mvWorldManager.removeWorldFromConfig(uuid)
            mvWorldManager.removePlayersFromWorld(uuid)
            mvWorldManager.unloadWorld(uuid)
            mvWorldManager.deleteWorld(uuid)

            println("PWManager >> Unloaded and Deleted world with Name: $worldName, UUID: $uuid")

            return true
        } else return false
    }

    fun update(): Boolean {
        if (world != null) {
            registeredWorldData.set("$uuid.last_update_date", LocalDate.now().toString())
            return true
        } else return false
    }
}
