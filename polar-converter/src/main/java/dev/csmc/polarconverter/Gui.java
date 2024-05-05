package dev.csmc.polarconverter;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public class Gui {
	public static void show() {
		var frame = new JFrame("Polar Converter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 200);

		var layout = new GridLayout(3, 2, 10, 10);

		var input = new JTextField();
		input.setToolTipText("Path to the world to convert");
		input.setBounds(50, 50, 200, 30);
		frame.add(input);

		var inputChooser = new JButton("Choose world");
		inputChooser.setBounds(250, 50, 150, 30);
		frame.add(inputChooser);

		var output = new JTextField();
		output.setToolTipText("Path to the output file");
		output.setBounds(50, 100, 200, 30);
		frame.add(output);

		var outputChooser = new JButton("Choose output");
		outputChooser.setBounds(250, 100, 100, 10);
		frame.add(outputChooser);

		var inputPicker = new JFileChooser();
		inputPicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		inputPicker.setDialogTitle("Select world to convert");
		inputPicker.setMultiSelectionEnabled(false);

//		SwingUtilities.invokeAndWait(() -> {
//			var pwd = new File(System.getProperty("user.dir"));
//			inputPicker.setCurrentDirectory(pwd);
//		});

		inputChooser.addActionListener(e -> {
			var result = inputPicker.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				var inputPath = inputPicker
					.getSelectedFile()
					.toPath();
				input.setText(inputPath.toString());
				var outputPath = Path.of(inputPath.getFileName() + ".polar");
				output.setText(outputPath.toString());
			}
		});

		var outputPicker = new JFileChooser();
		outputPicker.setFileSelectionMode(JFileChooser.FILES_ONLY);
		outputPicker.setDialogTitle("Select output file");
		outputPicker.setMultiSelectionEnabled(false);

//		SwingUtilities.invokeAndWait(() -> {
//			var pwd = new File(System.getProperty("user.dir"));
//			outputPicker.setCurrentDirectory(pwd);
//		});

		outputChooser.addActionListener(e -> {
			var result = outputPicker.showSaveDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				var outputPath = outputPicker
					.getSelectedFile()
					.toPath();
				output.setText(outputPath.toString());
			}
		});

		var convert = new JButton("Convert");
		convert.setBounds(50, 150, 50, 30);
		convert.addActionListener(e -> {
			var inputPath = Path.of(input.getText());
			var outputPath = Path.of(output.getText());
			try {
				Logic.convert(inputPath, outputPath);
				JOptionPane.showMessageDialog(frame, "Conversion successful", "Success", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frame, "Failed to convert world", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		frame.add(convert);

		frame.setLayout(layout);

		frame.setVisible(true);
	}
}
