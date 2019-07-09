package com.example.myapplication.GifMaker.Service;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public interface GifMaker {
	public void makeGif() throws Exception;
	
	public void start(InputStream zis);
	
	public void finish() throws IOException;
}
