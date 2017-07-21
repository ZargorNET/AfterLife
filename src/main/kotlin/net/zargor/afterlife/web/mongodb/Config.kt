package net.zargor.afterlife.web.mongodb

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.RandomStringUtils
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * Created by Zargor on 08.07.2017.
 */
class Config {
    private val newestCfgVersion : Int = 1
    private val yaml = Yaml()
    private val cfg = File("config.yml")
    val config : MutableMap<*, *>

    init {
        if (!cfg.exists()) {
            cfg.createNewFile()
            val inp = javaClass.getResourceAsStream("/default_config.yml")
            //For the password_salt generation
            val lines = IOUtils.readLines(inp, Charsets.UTF_8)
            inp.close()
            for (i in 0..lines.size - 1) {
                lines[i] = lines[i].replace("RANDOM_SALT", RandomStringUtils.random(512, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '#', '_', '-'))
            }
            val cfgOut = cfg.outputStream()
            IOUtils.writeLines(lines, null, cfgOut, Charsets.UTF_8)
            cfgOut.close()
        }
        config = yaml.load(cfg.readText(Charsets.UTF_8)) as MutableMap<*, *>
        if (config["cfg_version"].toString().toInt() < newestCfgVersion) {
            System.err.println("Outdated config! Please renew it!")
            System.exit(-1)
        }
    }
}