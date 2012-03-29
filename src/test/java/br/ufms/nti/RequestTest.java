package br.ufms.nti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.junit.Test;

public class RequestTest {
	private DefaultHttpClient httpClient = new DefaultHttpClient();
	private String authenticityToken;

	@Test
	public void test() throws Exception {

		login();
		report();
		view();
		viewWiki();
	}

	private void report() throws URISyntaxException,
			UnsupportedEncodingException, HttpException, IOException {
		String requestURI = "http://debianvm:8080/redmine/projects/projetoteste";

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("authenticity_token", authenticityToken);
		// parameters.put("commit", "Save");
		// parameters.put("content[text]", "h1. Hello World");

		HttpPost request = new HttpPost(requestURI);
		List<NameValuePair> valuePairs = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> parameter : parameters.entrySet()) {
			valuePairs.add(new BasicNameValuePair(parameter.getKey(), parameter
					.getValue()));
		}
		request.setEntity(new UrlEncodedFormEntity(valuePairs, HTTP.UTF_8));
		HttpResponse response = httpClient.execute(request);
		String pageContent = readFully(response);
		System.out.println(pageContent);
	}

	private void view() throws URISyntaxException,
			UnsupportedEncodingException, HttpException, IOException {
		String requestURI = "http://debianvm:8080/redmine/projects/projetoteste/issues";

		HttpGet request = new HttpGet(requestURI);
		HttpResponse response = httpClient.execute(request);

		String pageContent = readFully(response);
		String pattern = "<tr id=\"issue(.*)</tr>";
		Matcher matcher = Pattern.compile(pattern).matcher(pageContent);
		System.out.println(pageContent);
		Assert.assertTrue(matcher.find());
	}

	private void viewWiki() throws URISyntaxException,
			UnsupportedEncodingException, HttpException, IOException {
		String requestURI = "http://debianvm:8080/redmine/projects/projetoteste/wiki/Wiki/edit";

		HttpGet request = new HttpGet(requestURI);
		HttpResponse response = httpClient.execute(request);

		String pageContent = readFully(response);
		String pattern = "h2. Hello";
		Matcher matcher = Pattern.compile(pattern).matcher(pageContent);
		System.out.println(pageContent);
		Assert.assertTrue(matcher.find());
	}

	private void login() throws URISyntaxException,
			UnsupportedEncodingException, HttpException, IOException {
		String requestURI = "http://debianvm:8080/redmine/login";
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("username", "heverson.vasconcelos");
		parameters.put("password", "12345678");

		HttpPost request = new HttpPost(requestURI);
		if (parameters != null) {
			List<NameValuePair> valuePairs = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> parameter : parameters.entrySet()) {
				valuePairs.add(new BasicNameValuePair(parameter.getKey(),
						parameter.getValue()));
			}
			request.setEntity(new UrlEncodedFormEntity(valuePairs, HTTP.UTF_8));
		}
		HttpResponse response = httpClient.execute(request);
		String pageContent = readFully(response);
		System.out.println(pageContent);
		authenticityToken = getAuthenticityToken(pageContent);
	}

	private static String readFully(final HttpResponse httpResponse)
			throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpResponse.getEntity().getContent()));
		try {
			final StringBuilder fullContent = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				fullContent.append(line);
			}
			return fullContent.toString();
		} finally {
			reader.close();
		}
	}

	private String getAuthenticityToken(String html) {
		String pattern = "<meta name=\"csrf-token\" content=\"(.*.)\"/>";
		Matcher matcher = Pattern.compile(pattern).matcher(html);
		matcher.find();
		String result = matcher.toMatchResult().group().substring(33, 77);
		return result;
	}
}
