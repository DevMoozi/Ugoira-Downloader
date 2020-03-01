package com.example.myapplication.GifMaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.example.myapplication.GifMaker.Service.GifMaker;
import com.waynejo.androidndkgif.GifEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class GifMakerImpl implements GifMaker {
	
	private InputStream zis;
	private ArrayList<Integer> delay;
	private Context context;
	
	public GifMakerImpl(ArrayList<Integer> delay, Context context) {
		this.delay = delay;
		this.context = context;
	}
	

	@Override
	public void makeGif(String setting) throws Exception {
		int num = 0;
		ZipEntry zipEntry = null;

		GifEncoder.EncodingType encodingType = null;
		if("mobile".equals(setting)) encodingType = GifEncoder.EncodingType.ENCODING_TYPE_SIMPLE_FAST;
		else if("highQuality".equals(setting)) encodingType = GifEncoder.EncodingType.ENCODING_TYPE_STABLE_HIGH_MEMORY;


		GifEncoder gifEncoder = new GifEncoder();

		while ((zipEntry = ((ZipInputStream) zis).getNextEntry()) != null) {
			Bitmap next = BitmapFactory.decodeStream(zis);

			if(num == 0){
				gifEncoder.init(next.getWidth(), next.getHeight(), context.getCacheDir() + "/resource.gif", encodingType);
			}
			gifEncoder.encodeFrame(next, delay.get(num));
			num++;
			System.out.println(zipEntry.getName());
		}

		gifEncoder.close();
	}


	@Override
	public void start(InputStream zis) {
		this.zis = zis;
	}


	@Override
	public void finish() throws IOException {
		zis.close();
	}


	private void saveBitmapAsFile(Bitmap bitmap, String filepath) {
		File file = new File(filepath);
		OutputStream os = null;

		try {
			file.createNewFile();
			os = new FileOutputStream(file);

			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
