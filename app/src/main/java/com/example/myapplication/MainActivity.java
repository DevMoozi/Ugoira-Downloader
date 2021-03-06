package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myapplication.GifMaker.GifMakerImpl;
import com.example.myapplication.GifMaker.Service.GifMaker;
import com.example.myapplication.PermissionCheck.PermissionCheck;
import com.example.myapplication.PixivCrawling.PixivGifCrawlingServiceImpl;
import com.example.myapplication.PixivCrawling.Service.PixivCrawlingService;
import com.example.myapplication.ProgressBar.LoadingProgress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;



public class MainActivity extends AppCompatActivity {

    private LoadingProgress loadingProgress;
    private PermissionCheck permissionCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadingProgress = LoadingProgress.getInstance();
        permissionCheck = new PermissionCheck();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        System.out.println(action);
        System.out.println(type);



        if(Intent.ACTION_SEND.equals(action) && type != null){
            if("text/plain".equals(type)){
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                System.out.println(sharedText);
                String []Texts = sharedText.split("https");
                EditText editText = findViewById(R.id.pixivUrl);
                editText.setText("https" + Texts[1]);
            }
        }

        ImageView imageView = findViewById(R.id.gifView);
        registerForContextMenu(imageView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle("Ugoira Image");
        menu.add(0,1,0,"저장");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){


        switch (item.getItemId()){
            case 1:
                if(!permissionCheck.isCheck(this, getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, "폴더 접근"))
                    return false;

                ImageView imageView = (ImageView) item.getActionView();
                String path = getApplicationContext().getCacheDir() + "/resource.gif";
                InputStream inputStream = null;
                FileOutputStream save = null;

                File folder = new File(Environment.getExternalStorageDirectory() + "/Ugoira");
                if(!folder.exists()){
                    System.out.println("폴더 생성");
                    folder.mkdir();
                }

                try{
                    inputStream = new FileInputStream(new File(path));
                    String fileName = "Ugoira_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".gif";

                    fileName = Environment.getExternalStorageDirectory() + "/Ugoira/" + fileName;
                    System.out.println(fileName);
                    save = new FileOutputStream(fileName);


                    byte[] buffer = new byte[inputStream.available()];

                    int length;
                    while((length = inputStream.read(buffer)) > 0) {
                        System.out.println(length);
                        save.write(buffer, 0, length);
                    }
                    save.flush();
                    save.close();
                    inputStream.close();

                    //저장후 바로 갤러리 연동
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, fileName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/gif");
                    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    Toast toast = Toast.makeText(getApplicationContext(), "다운로드 완료", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM,0,0);
                    toast.show();
                } catch(Exception e){
                    e.printStackTrace();
                }
                return true;

        }

        return true;
    }

    public void onClick(android.view.View view){
        Button button = findViewById(R.id.button);
        button.setEnabled(false);
        loadingProgress.progressON(this, null);

        final Handler han = new Handler(){
            @Override
            public void handleMessage(Message msg){
                Button button = findViewById(R.id.button);

                if(((String)msg.obj).equals("Finish")){
                    ImageView imageView =  findViewById(R.id.gifView);

                    Glide.with(MainActivity.this).load(getApplicationContext().getCacheDir() + "/resource.gif")
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imageView);

                    button.setEnabled(true);

                }
                else{
                    alertMessage((String)msg.obj);
                    button.setEnabled(true);
                }
                loadingProgress.progressOFF();
            }
        };

        Thread getGif = new Thread() {

            @Override
            public void run() {
                Handler tHandler = han;
                Message msg = Message.obtain();

                EditText editText = findViewById(R.id.pixivUrl);
                String pixivUrl = editText.getText().toString();

                PixivCrawlingService pixiv = new PixivGifCrawlingServiceImpl(pixivUrl);
                Context context = getApplicationContext();

                try {
                    pixiv.connect();
                    GifMaker gifMaker = new GifMakerImpl(((PixivGifCrawlingServiceImpl) pixiv).getDelay(), context);
                    gifMaker.start(pixiv.getFile());
                    gifMaker.makeGif();
                    gifMaker.finish();
                    pixiv.disconnect();
                    msg.obj = "Finish";
                    tHandler.sendMessage(msg);
                } catch (Exception e) {
                    msg.obj = "Ugoira 링크가 아닙니다";
                    e.printStackTrace();
                    tHandler.sendMessage(msg);
                }
            }
        };
        getGif.start();
    }

    private void alertMessage(String msg){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();     //닫기
            }
        });
        alert.setMessage(msg);
        alert.show();
    }

}