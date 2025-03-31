package me.awabi2048.mw_manager.data_file

import me.awabi2048.mw_manager.Lib
import org.bukkit.Bukkit
import org.bukkit.Location

object Config {
    private val section = DataFiles.config

//    init {
//        println(section)
//    }

    var defaultExpireDays: Int
        get() {
            return section.getInt("default_expire_days")
        }
        set(value) {
            DataFiles.config.set("default_expire_days", value.coerceAtLeast(1))
            DataFiles.save()
        }

    var borderSizeBase: Int
        get() {
            return section.getInt("border_size_base")
        }
        set(value) {
            DataFiles.config.set("border_size_base", value.coerceAtLeast(1))
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

    var oageGanbaruMessage: List<String>
        get() {
            return section.getStringList("oage_ganbaru_message")
        }
        set(value) {
            DataFiles.config.set("oage_ganbaru_message", value)
            DataFiles.save()
        }

    var escapeLocation: Location?
        get() {
            val world = Bukkit.getWorld(DataFiles.config.getString("escape_world") ?: return null) ?: return null
            val posString = DataFiles.config.getString("escape_pos") ?: return null

            val location = Lib.stringToBlockLocation(world, posString)
            return location
        }
        set(value) {
            if (value != null) {
                val worldString = value.world.name
                val pos = Lib.locationToString(value)

                DataFiles.config.set("escape_world", worldString)
                DataFiles.config.set("escape_pos", pos)
                DataFiles.save()
            }
        }

    var cancelFlag: String?
        get() {
            return section.getString("cancel_flag")
        }
        set(value) {
            DataFiles.config.set("cancel_flag", value)
            DataFiles.save()
        }

    var availableWorldNameLength: IntRange
        get() {
            val minLength = section.getInt("min_world_name_length").coerceAtLeast(1)
            val maxLength = section.getInt("min_world_name_length").coerceAtLeast(minLength)

            return minLength..maxLength
        }
        set(value) {
            val minLength = value.min()
            val maxLength = value.max()

            DataFiles.config.set("min_world_name_length", minLength)
            DataFiles.config.set("max_world_name_length", maxLength)

            DataFiles.save()
        }

    var commandHelp: Map<String, String>?
        get() {
            val commands = section.getConfigurationSection("command_help")?.getKeys(false)
            return commands?.associate { it to section.getString("command_help.$it")!!}
        }
        set(value) {
            value?.forEach {
                section.set("command_help.${it.key}", it.value)
            }
        }

    var baseWorldExpandCost: Int
        get() {
            return section.getInt("base_world_expand_cost", 5)
        }
        set(value) {
            DataFiles.config.set("base_world_expand_cost", value.coerceAtLeast(1))
            DataFiles.save()
        }
}
