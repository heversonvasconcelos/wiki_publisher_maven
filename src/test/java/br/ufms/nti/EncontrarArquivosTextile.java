package br.ufms.nti;

import java.io.File;

import org.junit.Test;

public class EncontrarArquivosTextile {

	@Test
	public void test() throws Exception {
		File file = new File(".");
		System.out.println(file.getPath());
		System.out.println(file.getAbsolutePath());
	}

}
