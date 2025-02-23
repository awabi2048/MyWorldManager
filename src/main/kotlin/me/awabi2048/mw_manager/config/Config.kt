package me.awabi2048.mw_manager.config

object Config {
    private val section = DataFiles.config

    var defaultExpireDays: Int
        get() {
            return section.getInt("default_expire_days")
        }
        set(value) {
            DataFiles.config.set("default_expire_days", value.coerceAtLeast(0))
        }

    var borderSizeUnit: Int
        get() {
            return section.getInt("border_size_unit")
        }
        set(value) {
            DataFiles.config.set("border_size_unit", value.coerceAtLeast(0))
        }

    var borderExpansionMax: Int
        get() {
            return section.getInt("border_expansion_max")
        }
        set(value) {
            DataFiles.config.set("border_expansion_max", value.coerceAtLeast(0))
        }

    var defaultUnlockedWarpSlot: Int
        get() {
            return section.getInt("default_unlocked_warp_slot")
        }
        set(value) {
            DataFiles.config.set("default_unlocked_warp_slot", value.coerceAtLeast(0))
        }

    var defaultUnlockedWorldSlot: Int
        get() {
            return section.getInt("default_unlocked_world_slot")
        }
        set(value) {
            DataFiles.config.set("default_unlocked_world_slot", value.coerceAtLeast(0))
        }

    var stringBlacklist: List<String>
        get() {
            return section.getStringList("string_blacklist")
        }
        set(value) {
            DataFiles.config.set("string_blacklist", value)
        }
}
