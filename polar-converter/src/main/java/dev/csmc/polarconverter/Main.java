package dev.csmc.polarconverter;

import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

import static dev.csmc.polarconverter.Logic.convert;
import static org.slf4j.LoggerFactory.*;

public class Main {
	private static final Logger LOGGER = getLogger(Main.class);

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		if (args.length == 0) {
			Gui.show();
			return;
		}

		var inputFilePath = Path.of(args[0]);
		if (!inputFilePath
			.toFile()
			.exists()) {
			LOGGER.error("Input file does not exist");
			System.exit(1);
		}

		if (args.length < 2) {
			LOGGER.error("Missing output file path");
			LOGGER.info("Usage: java -jar polar-converter.jar <world> <output>");
			System.exit(1);
		}

		var outputFilePath = Path.of(args[1]);

		try {
			convert(inputFilePath, outputFilePath);

			LOGGER.info("Conversion successful");
		} catch (IOException e) {
			LOGGER.error("Failed to convert world", e);
			System.exit(1);
		}
	}
}