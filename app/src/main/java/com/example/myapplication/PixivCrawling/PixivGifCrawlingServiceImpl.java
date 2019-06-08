package com.example.myapplication.PixivCrawling;

import com.example.myapplication.PixivCrawling.Service.PixivCrawlingService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipInputStream;


public class PixivGifCrawlingServiceImpl implements PixivCrawlingService {

	private String url;
	private HttpURLConnection linkCon;

	public PixivGifCrawlingServiceImpl(String url) {
		this.url = url;
	}

	@Override
	public String getFileLink() throws Exception {
		URL pixivUrl = new URL(url);
		HttpURLConnection pixivUrlConnection = (HttpURLConnection) pixivUrl.openConnection();
		pixivUrlConnection.setRequestMethod("GET");
		pixivUrlConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64");
		BufferedReader reader = new BufferedReader(new InputStreamReader(pixivUrlConnection.getInputStream(), "utf-8"));

		String line;
		String link = "";
		while ((line = reader.readLine()) != null) {
			if (!line.contains("zip"))
				continue;
			
			String[] temp = line.split("\"");
			
			for (String str : temp) {
				if (str.contains("zip")) {
					link = str.replace("\\", "");
					break;
				}
			}
			if (!link.equals(""))
				break;
		}
		reader.close();
		pixivUrlConnection.disconnect();
		System.out.println("link" + link);
		return link;
	}
	
	@Override
	public void connect() throws Exception {
		String link = getFileLink();

		if (link.equals("")) {
			throw new Exception();
		}

		URL zipUrl = new URL(link);
		linkCon = (HttpURLConnection) zipUrl.openConnection();
		linkCon.setRequestMethod("GET");
		linkCon.setRequestProperty("Referer", url);
		linkCon.connect();
	}

	@Override
	public InputStream getFile() throws Exception {
		ZipInputStream zis = new ZipInputStream(linkCon.getInputStream());
		return zis;
	}


	@Override
	public void disconnect() {
		linkCon.disconnect();
	}

}