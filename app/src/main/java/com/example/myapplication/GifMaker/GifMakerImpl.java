package com.example.myapplication.GifMaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.os.Environment;
import android.util.Log;


import com.example.myapplication.Encoder.AnimatedGifEncoder;
import com.example.myapplication.GifMaker.Service.GifMaker;
import com.example.myapplication.ImageZipper.ImageZipper;
import com.example.myapplication.MainActivity;
import com.facebook.spectrum.DefaultPlugins;
import com.facebook.spectrum.EncodedImageSink;
import com.facebook.spectrum.EncodedImageSource;
import com.facebook.spectrum.Spectrum;
import com.facebook.spectrum.SpectrumResult;
import com.facebook.spectrum.SpectrumSoLoader;
import com.facebook.spectrum.image.EncodedImageFormat;
import com.facebook.spectrum.image.ImageSize;
import com.facebook.spectrum.logging.SpectrumLogcatLogger;
import com.facebook.spectrum.options.TranscodeOptions;
import com.facebook.spectrum.requirements.EncodeRequirement;
import com.facebook.spectrum.requirements.ResizeRequirement;
import com.waynejo.androidndkgif.GifEncoder;

import java.io.File;
import java.io.FileInputStream;
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
	public void makeGif() throws Exception {
		int num = 0;
		ZipEntry zipEntry = null;


		GifEncoder gifEncoder = new GifEncoder();

		while ((zipEntry = ((ZipInputStream) zis).getNextEntry()) != null) {
			Bitmap next = BitmapFactory.decodeStream(zis);

			if(num == 0){
				gifEncoder.init(next.getWidth(), next.getHeight(), context.getCacheDir() + "/resource.gif", GifEncoder.EncodingType.ENCODING_TYPE_SIMPLE_FAST);
			}
			gifEncoder.encodeFrame(next, delay.get(num));
			//e.addFrame(next);
			num++;
			System.out.println(zipEntry.getName());
		}

//		mSpectrum.transcode(
//				EncodedImageSource.from(new File(context.getCacheDir() + "/resource1.gif")),
//				EncodedImageSink.from(new File(context.getCacheDir() + "/resource.gif")),
//				TranscodeOptions.Builder(new EncodeRequirement(EncodedImageFormat.PNG, 80, EncodeRequirement.Mode.LOSSY)).build(),
//				"my_callsite_identifier"
//		);

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
