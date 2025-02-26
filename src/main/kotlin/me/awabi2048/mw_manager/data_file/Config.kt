package me.awabi2048.mw_manager.data_file

object Config {
    private val section = DataFiles.config

    var defaultExpireDays: Int
        get() {
            return section.getInt("default_expire_days")
        }
        set(value) {
            DataFiles.config.set("default_expire_days", value.coerceAtLeast(1))
            DataFiles.save()
        }

    var borderSizeUnit: Int
        get() {
            return section.getInt("border_size_unit")
        }
        set(value) {
            DataFiles.config.set("border_size_unit", value.coerceAtLeast(1))
            DataFiles.save()
        }

    var borderExpansionMax: Int
        get() {
            return section.getInt("border_expansion_max")
        }
        set(value) {
            DataFiles.config.set("border_expansion_max", value.coerceAtLeast(0))
            DataFiles.save()
        }

    var defaultUnlockedWarpSlot: Int
        get() {
            return section.getInt("default_unlocked_warp_slot")
        }
        set(value) {
            DataFiles.config.set("default_unlocked_warp_slot", value.coerceAtLeast(1))
            DataFiles.save()
        }

    var defaultUnlockedWorldSlot: Int
        get() {
            return section.getInt("default_unlocked_world_slot")
        }
        set(value) {
            DataFiles.config.set("default_unlocked_world_slot", value.coerceAtLeast(0))
            DataFiles.save()
        }

    var stringBlacklist: List<String>
        get() {
            return section.getStringList("string_blacklist")
        }
        set(value) {
            DataFiles.config.set("string_blacklist", value)
            DataFiles.save()
        }

    var playerWarpSlotMax: Int
        get() {
            return section.getInt("player_warp_slot_max")
        }
        set(value) {
            DataFiles.config.set("player_warp_slot_max", value.coerceAtLeast(1))
            DataFiles.save()
        }

    var playerWorldSlotMax: Int
        get() {
            return section.getInt("player_world_slot_max")
        }
        set(value) {
            DataFiles.config.set("player_world_slot_max", value.coerceAtLeast(1))
            DataFiles.save()
        }

    var globalWorldCountMax: Int
        get() {
            return section.getInt("global_world_count_max")
        }
        set(value) {
            DataFiles.config.set("global_world_count_max", value.coerceAtLeast(1))
            DataFiles.save()
        }
}
