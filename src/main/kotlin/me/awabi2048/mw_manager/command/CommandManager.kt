package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Main.Companion.instance
import org.bukkit.command.CommandExecutor

object CommandManager {
    fun setExecutor() {
        instance.getCommand("myworldmanager")?.setExecutor(MWMCommand)
        instance.getCommand("mwmanager")?.setExecutor(MWMCommand)
        instance.getCommand("mwm")?.setExecutor(MWMCommand)

        instance.getCommand("invite")?.setExecutor(InviteCommand)
        instance.getCommand("visit")?.setExecutor(VisitCommand)

        instance.getCommand("worldpoint")?.setExecutor(WorldPointCommand)
        instance.getCommand("mwm_invite_accept")?.setExecutor(InviteAcceptCommand)

        instance.getCommand("worldmenu")?.setExecutor(OpenWorldUICommand)
        instance.getCommand("warp")?.setExecutor(WarpCommand)
    }

    fun getTabCompletion(args: List<String>, executor: CommandExecutor): MutableList<String> {
        val size = args.size
        return mutableListOf()
    }
}
