package com.github.ivanjermakov.lettercore.util;

import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Files {

	public static MultipartFile multipartFileFromFile(File file) throws IOException {
		FileInputStream input = new FileInputStream(file);
		return new MockMultipartFile(file.getName(),
				file.getName(),
				"text/plain",
				IOUtils.toByteArray(input)
		);
	}

}
