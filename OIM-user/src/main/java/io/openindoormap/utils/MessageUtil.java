package io.openindoormap.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageUtil {
	public void makeConfig(String in, String out) throws InvalidPropertiesFormatException, IOException {

        File[] files = new File(in).listFiles(name -> name.getName().startsWith("messages_"));

		Properties pt = new Properties();
		for (int i = 0; i < files.length; i++) {
			File fn = files[i];

			String name = fn.getName();
			StringBuffer sb = new StringBuffer(1024 * 1024);
			sb.append("var JS_MESSAGE = {};\n");
			if (name.endsWith("xml")) {
				try (FileInputStream fis = new FileInputStream(fn)) {
					pt.loadFromXML(fis);
				}
			} else {
				try (FileReader fis = new FileReader(fn)) {
					pt.load(fis);
				}
			}
			pt.forEach((k, v) -> {
				sb.append(String.format("JS_MESSAGE[\"%s\"] = \"%s\";\n", k, v.toString()));
				// log.debug(v.toString());
			});
			pt.clear();
			Files.writeString(Paths.get(out, name.split("\\.")[0] + ".js"), sb.toString());
		}
		pt = null;
	}
	public static void main(String[] args) throws InvalidPropertiesFormatException, IOException {
		new MessageUtil().makeConfig("src/main/resources/messages", "src/main/resources/static/js");
	}
}
