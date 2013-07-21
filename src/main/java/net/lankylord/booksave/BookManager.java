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
import org.bukkit.command.CommandSender;
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
    private List<String> bookList;

    public BookManager(BookSave plugin) {
        this.plugin = plugin;
        this.bookList = new ArrayList<>();
        this.createBookDirectory();
    }

    private void createBookDirectory() {
        bookFolder = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books");
        if (!bookFolder.exists())
            bookFolder.mkdirs();
    }

    /**
     * Save a book from a player's inventory
     *
     * @param name The name to be used for identification of the book
     * @param meta The BookMeta of the book to be saved
     */
    public void addBook(String name, BookMeta meta) {
        File newBookFile = this.getBookFile(name);
        try {
            newBookFile.createNewFile();
        } catch (IOException e) {
            BookSave.logger.log(Level.WARNING, "Exception while trying to save new book.");
            return;
        }
        String author = meta.getAuthor();
        String title = meta.getTitle();
        List pages = meta.getPages();
        this.saveBookToSystem(title, author, pages, newBookFile);
    }

    /**
     *
     * @param name The name of the book to be deleted
     * @return Returns true if book exists and was deleted, false if book does not exist
     */
    public boolean removeBook(String name) {
        File book = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books" + File.separatorChar + name + ".yml");
        if (book.exists()) {
            book.delete();
            updateBookList();
            return true;
        }
        return false;
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
        File bookFile = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books" + File.separatorChar + name + ".yml");
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
        File bookFile = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books" + File.separatorChar + bookName + ".yml");
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

    /**
     * Update bookList with the files currently present in the directory
     *
     */
    public void updateBookList() {
        bookList = new ArrayList<>();
        File[] files = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books").listFiles();
        if (files != null)
            for (File file : files)
                if (file.isFile()) {
                    String name = file.getName();
                    int pos = name.lastIndexOf(".");
                    if (pos > 0)
                        name = name.substring(0, pos);
                    bookList.add(name);
                }
    }

    /**
     * Send the player a message listing the currently stored books
     *
     * @param p The player to send the messages to
     */
    public void listBookFiles(CommandSender sender) {
        if (!bookList.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "List of Books");
            sender.sendMessage(" ");
            for (Iterator<String> it = bookList.iterator(); it.hasNext();) {
                String bookname = it.next();
                String booktitle = this.getBookTitle(bookname);
                sender.sendMessage(ChatColor.GOLD + "Name: " + ChatColor.GRAY + bookname);
                sender.sendMessage(ChatColor.GOLD + "Title: " + ChatColor.GRAY + booktitle);
                sender.sendMessage(ChatColor.GRAY + "-----");
            }
        } else
            sender.sendMessage(ChatColor.RED
                    + "[BookSave] There are no books to list");
    }

    public File getBookFile(String name) {
        return new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books" + File.separatorChar + name + ".yml");
    }
}
