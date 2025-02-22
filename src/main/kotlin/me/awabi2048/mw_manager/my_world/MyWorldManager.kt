package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.config.DataFiles

object MyWorldManager {
    val registeredWorld: List<MyWorld>
        get() {
            val worldList = mutableListOf<MyWorld>()

            DataFiles.worldData.getKeys(false).forEach { worldUUID -> String
                val myWorld = MyWorld(worldUUID)
                worldList.add(myWorld)
            }
            return worldList
        }
}
