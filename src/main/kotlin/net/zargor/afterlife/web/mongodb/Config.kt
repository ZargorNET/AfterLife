package net.zargor.afterlife.web.mongodb

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

/**
 * Created by Zargor on 08.07.2017.
 */
class Config {
    var prop : Properties

    init {
        if (!File("config.txt").exists()) {
            prop = Properties()
            prop.setProperty("webserver_port", "8080")
            prop.setProperty("grecaptcha-key", "key")
            prop.setProperty("mongodb_username", "afterlife")
            prop.setProperty("mongodb_password", "password")
            prop.setProperty("mongodb_db", "afterlife")
            prop.setProperty("mongodb_host", "zargor.net")
            prop.setProperty("mongodb_port", "27017")
            prop.setProperty("plugin_ip", "127.0.0.1")
            prop.setProperty("web_master_ip", "127.0.0.1")
            prop.setProperty("web_master_port", "8091")
            val f = File("config.txt")
            f.createNewFile()
            prop.store(FileOutputStream(f), "Default config")
        } else {
            val f = File("config.txt")
            prop = Properties()
            prop.load(FileInputStream(f))
        }
    }
}