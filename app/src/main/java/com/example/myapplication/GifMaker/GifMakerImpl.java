package com.example.myapplication.GifMaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.myapplication.Encoder.AnimatedGifEncoder;
import com.example.myapplication.GifMaker.Service.GifMaker;
import com.example.myapplication.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GifMakerImpl implements GifMaker {
	
	private InputStream zis;
	private int delay;
	private Context context;
	
	public GifMakerImpl(int delay, Context context) {
		this.delay = delay;
		this.context = context;
	}
	

	@Override
	public void makeGif() throws Exception {
		ZipEntry zipEntry = null;

		OutputStream out = new FileOutputStream(
				new File(context.getCacheDir() + "resource.gif"));
		AnimatedGifEncoder e = new AnimatedGifEncoder();
		e.start(out);

		e.setDelay(delay);

		while ((zipEntry = ((ZipInputStream) zis).getNextEntry()) != null) {
			Bitmap next = BitmapFactory.decodeStream(zis);

			e.addFrame(next);
			System.out.println(zipEntry.getName());
		}

		e.finish();
		out.close();
	}


	@Override
	public void start(InputStream zis) {
		this.zis = zis;
	}


	@Override
	public void finish() throws IOException {
		zis.close();
	}

}
