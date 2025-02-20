package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.Main.Companion.registeredWorldData

object MyWorldManager {
    val registeredWorld: List<MyWorld>
        get() {
            val worldList = mutableListOf<MyWorld>()

            registeredWorldData.getKeys(false).forEach { worldUUID -> String
                val myWorld = MyWorld(worldUUID)
                worldList.add(myWorld)
            }
            return worldList
        }
}
