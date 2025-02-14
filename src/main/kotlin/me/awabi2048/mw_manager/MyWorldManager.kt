package me.awabi2048.mw_manager

import me.awabi2048.mw_manager.Main.Companion.registeredWorldData

object MyWorldManager {
    val registeredWorld: List<MyWorld>
        get() {
            val worldList = mutableListOf<MyWorld>()
            registeredWorldData.getKeys(false).forEach {
                registeredWorldData.getConfigurationSection("$it.worlds")?.getKeys(false)?.forEach {
                    val worldName = it
                    val world = MyWorld(worldName)
                    worldList.add(world)
                }
            }
            return worldList
        }
}
