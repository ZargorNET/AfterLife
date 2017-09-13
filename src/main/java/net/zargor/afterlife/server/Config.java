package net.zargor.afterlife.server;

import com.google.common.primitives.Ints;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * Created by Zargor on 08.07.2017.
 */
public class Config {

	@Getter
	private final int newestCfgVersion = 1;
	private Map<String, Object> cfg;

	public Config() {
		try {
			Yaml yaml = new Yaml();
			File cfgFile = new File("config.yml");
			if (!cfgFile.exists()) {
				cfgFile.createNewFile();
				InputStream inp = this.getClass().getResourceAsStream("/default_config.yml");
				List<String> lines = IOUtils.readLines(inp, Charset.forName("UTF-8"));
				inp.close();
				for (int i = 0; i < lines.size() - 1; i++) {
					lines.set(i, lines.get(i).replace("RANDOM_SALT", RandomStringUtils.random(512, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '#', '_', '-')));
				}
				OutputStream cfgOut = new FileOutputStream(cfgFile);
				IOUtils.writeLines(lines, null, cfgOut, Charset.forName("UTF-8"));
				cfgOut.close();
				final String[] fullYaml = {""};
				lines.forEach(s -> fullYaml[0] += s);
				cfg = (Map<String, Object>) yaml.load(fullYaml[0]);
			} else {
				cfg = (Map<String, Object>) yaml.load(new FileInputStream("config.yml"));
			}
			Integer version = Ints.tryParse(getValue("cfg_version").toString());
			if (version == null) {
				System.err.println("Can't parse config version to an integer!");
				System.exit(-1);
				return;
			}
			if (version < newestCfgVersion) {
				System.err.println("Outdated config! Please renew it!");
				System.exit(-1);
			}
		} catch (IOException exe) {
			exe.printStackTrace();
		}
	}


	@SuppressWarnings("unchecked")
	public <T> T getValue(String key) {
		return (T) cfg.get(key);
	}
}