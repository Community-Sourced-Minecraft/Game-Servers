package dev.csmc.polarconverter;

import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.PolarWriter;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.slf4j.LoggerFactory.*;

public class Main {
	private static final Logger LOGGER = getLogger(Main.class);

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			LOGGER.error("Missing input file path");
			LOGGER.info("Usage: java -jar polar-converter.jar <world> <output>");
			System.exit(1);
		}

		var inputFilePath = Path.of(args[0]);
		if (!inputFilePath.toFile().exists()) {
			LOGGER.error("Input file does not exist");
			System.exit(1);
		}

		if (args.length < 2) {
			LOGGER.error("Missing output file path");
			LOGGER.info("Usage: java -jar polar-converter.jar <world> <output>");
			System.exit(1);
		}

		var outputFilePath = Path.of(args[1]);

		var world = AnvilPolar.anvilToPolar(inputFilePath);
		var bytes = PolarWriter.write(world);

		Files.write(outputFilePath, bytes);

		LOGGER.info("Conversion successful");
	}
}