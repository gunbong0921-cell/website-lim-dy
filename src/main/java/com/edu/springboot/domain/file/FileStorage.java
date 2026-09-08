package com.edu.springboot.domain.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface FileStorage {

	String store(String originalName, InputStream content) throws IOException;

	Path resolve(String storedName);
}
