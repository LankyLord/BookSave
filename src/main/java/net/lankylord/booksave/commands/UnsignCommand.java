/*
 * Copyright (c) 2013, LankyLord
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.lankylord.booksave.commands;

import java.util.List;
import net.lankylord.booksave.BookSave;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author LankyLord
 */
public class UnsignCommand extends BookSaveCommand {

    public UnsignCommand(BookSave plugin) {
        super(plugin);
        this.setName("BookSave: Edit");
        this.setCommandUsage("/book edit <BookName>");
        this.setArgRange(1, 1);
        this.addKey("booksave edit");
        this.addKey("bs edit");
        this.addKey("book edit");
        this.setPermission("booksave.edit", "Allows this user to edit a book", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (p.getItemInHand().getType() == Material.WRITTEN_BOOK) {
                BookMeta meta = (BookMeta) p.getItemInHand().getItemMeta();
                p.getItemInHand().setType(Material.BOOK_AND_QUILL);
                p.getItemInHand().setItemMeta(meta);
            } else
                p.sendMessage(colour3 + "You must be holding a book to do that.");
        }
    }
}