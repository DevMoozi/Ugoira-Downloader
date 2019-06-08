package com.example.myapplication.PixivCrawling.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public interface PixivCrawlingService {
	
	public String getFileLink() throws Exception;
	
	public void connect() throws Exception;
	
	public InputStream getFile() throws Exception;
	
	public void disconnect();
	
	
}
