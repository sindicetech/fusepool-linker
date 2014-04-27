package com.sindice.fusepooladapter.storage;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * workaround for a known bug on windows:

http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4469299
http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154
 
 * 
 */
public class SafeDeleter {
	public static void delete(String folder) {
		Path dataPath = Paths.get(folder);

		try {
			if (Files.exists(dataPath)) {
				DirectoryStream<Path> files;

				files = Files.newDirectoryStream(dataPath);

				if (files != null) {

					for (Path filePath : files) {

						try {
							Files.delete(filePath);
						} catch (IOException ex) {
							for (int i = 0; i < 50; i++) {
								try {
									System.gc();
									Files.delete(filePath);
								} catch (IOException ex1) {
									try {
										Thread.sleep(10);
									} catch (InterruptedException ex2) {
										Thread.currentThread().interrupt();
									}
									continue;
								}
								break;
							}
						}
						if (filePath.toFile().exists()) {
							throw new IOException("delete failed: " + filePath);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("error cleaning the store ", e);
		}		
	}
}
