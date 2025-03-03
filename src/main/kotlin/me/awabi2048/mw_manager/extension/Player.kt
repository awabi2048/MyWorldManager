package me.awabi2048.mw_manager.extension

import me.awabi2048.mw_manager.Main.Companion.PREFIX
import net.kyori.adventure.sound.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun CommandSender.notify(message: String, sound: Sound?) {
    if (this is Player) {
        sendMessage("$PREFIX $message")
        if (sound != null) playSound(sound)
    } else {
        if (message.startsWith("§")) sendMessage("MWManager >> ${message.drop(2)}") else sendMessage("MWManager >> $message")
    }
}
