package com.example.nfs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

@SpringBootApplication
public class NFSApplication implements CommandLineRunner {
	private static final String TEST_FILE_CONTENT = "HI-NFS";
	private static final String TEST_FILE_NAME = "hi.nfo";

	@Value("${vcap.services.nfs_test_service.volume_mounts[0].container_dir}")
	private String mountPath;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("**** NFS mount is: " + mountPath);

		String filePath = mountPath + "/" + TEST_FILE_NAME;

		File file = new File(filePath);

		if (!file.exists()) {
			System.out.println("**** File: " + filePath + " does not exist, creating new with contents of: "
					+ TEST_FILE_CONTENT);
			PrintWriter printWriter = new PrintWriter(file);
			printWriter.write(TEST_FILE_CONTENT);
			printWriter.close();
		} else {
			System.out.println("**** File exists on storage: " + filePath);
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String contents = bufferedReader.readLine();
			bufferedReader.close();

			Assert.hasText(TEST_FILE_CONTENT, contents);
			System.out.println("**** Contents match previously written file. Deleting file: " + filePath);

			file.delete();
		}

		System.out.println("**** Kill and restart the application to simulate new containers accessing shared FS");
	}

	public static void main(String[] args) {
		SpringApplication.run(NFSApplication.class, args);
	}
}
