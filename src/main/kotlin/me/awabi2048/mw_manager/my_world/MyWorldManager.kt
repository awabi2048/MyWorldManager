package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.config.DataFiles

object MyWorldManager {
    val registeredWorld: List<MyWorld>
        get() {
            return DataFiles.worldData.getKeys(false).map {
                MyWorld(it)
            }
        }
}
