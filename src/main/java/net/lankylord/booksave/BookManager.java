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
package net.lankylord.booksave;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 *
 * @author LankyLord
 */
public class BookManager {

    private final BookSave plugin;
    private File bookFolder;

    public BookManager(BookSave plugin) {
        this.plugin = plugin;
    }

    private void createBookDirectory() {
        bookFolder = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books");
        if (!bookFolder.exists())
            bookFolder.mkdirs();
    }

    /**
     * Save a book from a player's inventory
     *
     * @param p The player issuing the command
     * @param name The name to be used for identification of the book
     */
    public boolean addBook(Player p, String name) {
        createBookDirectory();
        File newBook = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books" + File.separatorChar + name);
        if (p.getItemInHand().getType() != Material.WRITTEN_BOOK) {
            p.sendMessage(ChatColor.RED + "[BookSave] You need to be holding a written book to do that.");
            return false;
        }
        if (newBook.exists()) {
            p.sendMessage(ChatColor.RED + "[BookSave] There is already a book with that name.");
            return false;
        }
        try {
            newBook.createNewFile();
        } catch (IOException e) {
            BookSave.logger.log(Level.WARNING, "[BookSave] Failed to save new book");
            p.sendMessage(ChatColor.RED + "[BookSave] WARNING: Failed to save new book");
            return false;
        }
        BookMeta meta = (BookMeta) p.getItemInHand().getItemMeta();
        String author = meta.getAuthor();
        String title = meta.getTitle();
        List pages = meta.getPages();
        saveBookToSystem(title, author, pages, newBook);
        return true;
    }

    /**
     * Give a specified book to a player
     *
     * @param p Player receiving the book
     * @param name The name of the book to be given
     */
    public boolean giveBookToPlayer(Player p, String name) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        File bookFile = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books" + File.separatorChar + name);
        if (bookFile.exists()) {
            meta = this.getBookFromSystem(meta, bookFile);
            book.setItemMeta(meta);
            p.getInventory().addItem(new ItemStack[]{book});
            return true;
        }
        return false;
    }

    /**
     * Runs a task to save the book to disk
     *
     * @param title Title of the book to be stored
     * @param author Author of the book to be stored
     * @param pages Pages of the book to be stored
     * @param book The file for book data to be stored in
     */
    private void saveBookToSystem(final String title, final String author, final List<String> pages, final File book) {
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                YamlConfiguration bookFile = new YamlConfiguration();
                bookFile.set("author", author);
                bookFile.set("title", title);
                bookFile.set("pages", pages);
                try {
                    bookFile.save(book);
                } catch (IOException e) {
                }
            }
        });
    }

    /**
     * Retrieved stored book metadata from disk
     *
     * @param meta BookMeta that will be replaced
     * @param File The file the book data is stored in
     */
    private BookMeta getBookFromSystem(BookMeta meta, File bookFile) {
        YamlConfiguration savedBook = new YamlConfiguration();
        try {
            savedBook.load(bookFile);
        } catch (InvalidConfigurationException | IOException e) {
            BookSave.logger.log(Level.WARNING, "[BookSave] Failed to load books from disk.");
        }
        meta.setAuthor(savedBook.getString("author"));
        meta.setTitle(savedBook.getString("title"));
        meta.setPages((List<String>) savedBook.getList("pages"));
        return meta;
    }

    /**
     * Retrieve title of stored book
     *
     * @param bookName The name of the file the book data is stored in
     * @return Returns null if file does not exist, else returns title of book
     */
    public String getBookTitle(String bookName) {
        File bookFile = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books" + File.separatorChar + bookName);
        if (bookFile.exists()) {
            YamlConfiguration savedBook = new YamlConfiguration();
            try {
                savedBook.load(bookFile);
            } catch (InvalidConfigurationException | IOException e) {
                BookSave.logger.log(Level.WARNING, "[BookSave] Failed to read book from disk.");
            }
            return savedBook.getString("title");
        }
        return null;
    }

    private List<String> getBookFiles() {
        List<String> results = new ArrayList<>();
        File[] files = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books").listFiles();
        for (File file : files)
            if (file.isFile())
                results.add(file.getName());
        return results;
    }

    public void listBookFiles(Player p) {
        List<String> booknames = getBookFiles();
        p.sendMessage(ChatColor.GOLD + "===" + ChatColor.GRAY + "List of Books" + ChatColor.GOLD + "===");
        for (Iterator<String> it = booknames.iterator(); it.hasNext();) {
            String bookname = it.next();
            String booktitle = this.getBookTitle(bookname);
            p.sendMessage(ChatColor.GOLD + "==" + ChatColor.GRAY + bookname + ChatColor.GOLD + "==");
            p.sendMessage(ChatColor.GOLD + "Title: " + ChatColor.GRAY + booktitle);
        }
    }
}
