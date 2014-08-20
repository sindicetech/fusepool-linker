/* 
 * Copyright 2014 Sindice LTD http://sindicetech.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
