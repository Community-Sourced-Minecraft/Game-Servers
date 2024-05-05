package dev.csmc.polarconverter;

import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.PolarWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Logic {
	public static void convert(Path input, Path output) throws IOException {
		var world = AnvilPolar.anvilToPolar(input);
		var bytes = PolarWriter.write(world);

		Files.write(output, bytes);
	}
}
