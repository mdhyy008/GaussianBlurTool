package com.dabai.GaussianBlur;

import android.app.*;
import android.os.*;
import jp.wasabeef.blurry.*;
import android.widget.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.widget.SeekBar.*;
import android.view.*;
import android.content.*;
import java.io.*;
import java.util.*;
import android.net.*;

/**
 高斯模糊实例
 大白2017
 2019.5.31
 **/


public class MainActivity extends Activity 
{


	//控件声明
	ImageView iv;
	SeekBar sb;

	//变量
	Bitmap bm;
	int pro =1;
	private static final int CHOOSE_PHOTO=0;
	private NotificationManager manager;
	private int noi = 1;
	private File file;
	File fdir = new File("/sdcard/高斯模糊处理过的图片/");

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


		//判断是否第一次打开
		if (!fdir.exists())
		{
			new AlertDialog.Builder(this)
				.setTitle("帮助")
				.setMessage("1.点击图片更换图片\n2.音量键来增加或减小模糊值3.右上角按钮保存")
				.setCancelable(false)
				.setPositiveButton("知道了",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						fdir.mkdirs();
					}
				}) 
				.show();
		}



		//实例化控件
		iv = (ImageView)findViewById(R.id.mainImageView1);
		sb = (SeekBar)findViewById(R.id.mainSeekBar1);
		manager = (NotificationManager) this.getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

		//初始化
		init();


		//监听事件
		iv.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					Intent intent = new Intent("android.intent.action.GET_CONTENT"); 
					intent.setType("image/*"); 
					startActivityForResult(intent, CHOOSE_PHOTO); 

				}
			});

	}



	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{ 

		//回调方法  用于取回图片
		try
		{
			switch (requestCode)
			{ 
				case CHOOSE_PHOTO: 
					if (resultCode == RESULT_OK)
					{ 

						try
						{
							Bitmap bitmap = null; 
							//判断手机系统版本号 
							if (Build.VERSION.SDK_INT >= 19)
							{ 
								//4.4及以上系统使用这个方法处理图片 
								bitmap = ImgUtil.handleImageOnKitKat(this, data);    //ImgUtil是自己实现的一个工具类 
							}
							else
							{ 
								//4.4以下系统使用这个方法处理图片 
								bitmap = ImgUtil.handleImageBeforeKitKat(this, data); 
							} 
							iv.setImageBitmap(bitmap); 
							init();
						}
						catch (Exception e)
						{
							Toast.makeText(getApplicationContext(), "你是怎么搞出的" + e.getMessage() + "异常", 1).show();
							iv.setImageResource(R.drawable.image_3);
						}	

					} 
					break; 
				default: 
					break; 
			} 

		}
		catch (Exception e)
		{
			Toast.makeText(getApplicationContext(), "你是怎么搞出的" + e.getMessage() + "异常", 1).show();
		}         
	} 




	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// TODO: Implement this method
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// 菜单点击

		switch (item.getItemId())
		{
			case R.id.save:

				send_start();

				Bitmap bitmap = getBitmapByView(iv);//iv是View  	
				int ran = new Random().nextInt(1000);
				savePhotoToSDCard(bitmap, "/sdcard/高斯模糊处理过的图片", "GaussianBlur_" + ran);
				file = new File("/sdcard/高斯模糊处理过的图片/GaussianBlur_" + ran + ".png");
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

				send_end();	

				break;
		}

		return super.onOptionsItemSelected(item);
	}



	public void send_start()
	{

		//过程通知
		Notification notification = new Notification.Builder(this)
			//.setContentTitle("提示")//设置标题
			.setContentText("获取图片并保存到图库...")//设置内容
			.setColor(Color.parseColor("#FF9800"))
			.setWhen(System.currentTimeMillis())//设置创建时间
			.setSmallIcon(R.drawable.ic_launcher)//设置状态栏图标
			.setPriority(Notification.PRIORITY_MAX)
			//.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))//设置通知栏图标
			.build();

		notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT; //必须加上这个标志
		notification.defaults = Notification.DEFAULT_VIBRATE;
		manager.notify(noi, notification);

	}


	public void send_end()
	{

		manager.cancel(noi);

		//结果通知
		Notification notification = new Notification.Builder(this)
			//.setContentTitle("提示")//设置标题
			.setContentText("保存成功 - " + file.getAbsolutePath())//设置内容
			.setColor(Color.parseColor("#E91E63"))
			.setWhen(System.currentTimeMillis())//设置创建时间
			.setSmallIcon(R.drawable.ic_launcher)//设置状态栏图标
			.setPriority(Notification.PRIORITY_MAX)
			//.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))//设置通知栏图标
			.build();

		notification.flags = Notification.FLAG_SHOW_LIGHTS; //必须加上这个标志
		notification.defaults = Notification.DEFAULT_VIBRATE;
		manager.notify(noi, notification);
		noi++;

	}





	//根据view获取bitmap
	public static Bitmap getBitmapByView(View view)
	{
        int h = 0;
        Bitmap bitmap = null; 
        bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
									 Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
	}

	//检查sd
	public static boolean checkSDCardAvailable()
	{
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}
	public static void savePhotoToSDCard(Bitmap photoBitmap, String path, String photoName)
	{
        if (checkSDCardAvailable())
		{
            File dir = new File(path);
            if (!dir.exists())
			{
                dir.mkdirs();
            }

            File photoFile = new File(path, photoName + ".png");
            FileOutputStream fileOutputStream = null;
            try
			{
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null)
				{
                    if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream))
					{
                        fileOutputStream.flush();
                    }
                }
            }
			catch (FileNotFoundException e)
			{
                photoFile.delete();
                e.printStackTrace();
            }
			catch (IOException e)
			{
                photoFile.delete();
                e.printStackTrace();
            }
			finally
			{
                try
				{
                    fileOutputStream.close();
                }
				catch (IOException e)
				{
                    e.printStackTrace();
                }
            }
        }
	}

	private long mExitTime;
	private long mkeyTime;


	//重写按键
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
        switch (keyCode)
		{

			case KeyEvent.KEYCODE_BACK:
				if ((System.currentTimeMillis() - mExitTime) > 2000)
				{
					//大于2000ms则认为是误操作，使用Toast进行提示
					Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
					//并记录下本次点击“返回键”的时刻，以便下次进行判断
					mExitTime = System.currentTimeMillis();
				}
				else
				{
					//小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
					finish();
				}

                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:

				if ((System.currentTimeMillis() - mkeyTime) > 500)
					keydown();
				mkeyTime = System.currentTimeMillis();

				return true;
            case KeyEvent.KEYCODE_VOLUME_UP:

				if ((System.currentTimeMillis() - mkeyTime) > 500)
					keyup();
				mkeyTime = System.currentTimeMillis();

				return true;

        }
        return super.onKeyDown(keyCode, event);
	}

	private void keyup()
	{
		// TODO: Implement this method
		pro += 10;
		if (pro <= 100)
		{

			//setTitle("Radius : "+pro);
			Blurry.with(MainActivity.this)
				.radius(pro)
				.sampling(2)
				.async()
				.from(bm)
				.into(iv);
		}
		else
		{
			pro = 100;

			//setTitle("Radius : "+pro);
			Blurry.with(MainActivity.this)
				.radius(pro)
				.sampling(2)
				.async()
				.from(bm)
				.into(iv);

		}

	}

	private void keydown() 
	{
		// TODO: Implement this method
		pro -= 10;
		if (pro >= 1)
		{

			//setTitle("Radius : "+pro);
			Blurry.with(MainActivity.this)
				.radius(pro)
				.sampling(2)
				.async()
				.from(bm)
				.into(iv);
		}
		else
		{
			pro = 1;

			//setTitle("Radius : "+pro);
			Blurry.with(MainActivity.this)
				.radius(pro)
				.sampling(2)
				.async()
				.from(bm)
				.into(iv);
		}


	};




	private void init()
	{
		// TODO: Implement this method
		bm = ((BitmapDrawable) ((ImageView) iv).getDrawable()).getBitmap();

		Blurry.with(MainActivity.this)
			.radius(pro)
			.sampling(2)
			.async()
			.from(bm)
			.into(iv);

		sb.setProgress(pro);

	}
}
