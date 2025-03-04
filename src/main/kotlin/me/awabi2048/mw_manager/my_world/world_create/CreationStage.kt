package me.awabi2048.mw_manager.my_world.world_create

// Stageが変わるのは確認メニューを経て確定されたあと
enum class CreationStage {
    WORLD_NAME,
    CLONE_SOURCE,
    WAITING_CREATION
}
