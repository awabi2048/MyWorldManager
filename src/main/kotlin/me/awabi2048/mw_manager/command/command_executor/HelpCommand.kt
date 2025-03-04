package me.awabi2048.mw_manager.command.command_executor

import me.awabi2048.mw_manager.command.CommandManager
import me.awabi2048.mw_manager.data_file.Config
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object HelpCommand : CommandExecutor {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (!p3.isNullOrEmpty()) {
            p0.sendMessage("§c無効なコマンドです。")
            return true
        }

        if (!CommandManager.hasCorrectPermission(p0, this)) {
            p0.sendMessage("§c権限がありません。")
            return true
        }

        p0.sendMessage("§7----------------------")
        for (command in Config.commandHelp!!.filter { p0.hasPermission("mw_manager.command.${it.key}") }) {
            p0.sendMessage(
                "§f§l${command.key}§r ${
                    command.value
                        .replace("<", "<§e")
                        .replace(">", "§7>")
                        .replace("[", "[§b")
                        .replace("]", "§7]")
                }"
            )
        }
        p0.sendMessage("§7----------------------")

//        if (p0 !is Player) return true
//
//        val location = p0.location.add(0.0, 5.0, 0.0)
//
//        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT)
//
//        packet.integers.write(0, p0.entityId)

//        packet.doubles.write(0, location.x)
//        packet.doubles.write(1, location.y)
//        packet.doubles.write(2, location.z)

//        packet.bytes.write(0, ((location.yaw + 30f) * 256 / 360).toInt().toByte())
//        packet.bytes.write(1, (location.pitch * 256 / 360).toInt().toByte())

//        packet.bytes.write(0, 3.toByte())
//        packet.float.write(0, 3f)

//        packet.getSpecificModifier(Set::class.java).write(0, setOf(EnumWrappers.))

//        packet.bytes.write(0, 0x00.toByte())
//        protocolManager.sendServerPacket(p0, packet)

//        val container = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE)
//        container.blockPositionModifier.write(0, BlockPosition(location.blockX, location.blockY, location.blockZ))
//        container.blockData.write(0, WrappedBlockData.createData(Material.REDSTONE))
//
//        protocolManager.sendServerPacket(p0, container)

        return true
    }
}
