package br.ufms.nti;

import junit.framework.Assert;

import org.junit.Test;

public class FixDirectoryOSPatternTest {

	private String fixDirectoryOSPattern(String path) {
		return path.replace("\\", "/");
	}

	@Test
	public void fixDirectoryOSPatternTest() {
		String unixPatternPath = fixDirectoryOSPattern("design/gpr/index.textile");
		String windowsPatternPath = fixDirectoryOSPattern("design\\gpr\\index.textile");

		System.out.println(unixPatternPath);
		System.out.println(windowsPatternPath);

		Assert.assertEquals(unixPatternPath, windowsPatternPath);
	}
}
