package appforms;
import leorchn.lib.*;
import android.view.*;
import android.content.*;
import android.text.*;
import android.net.*;
import android.widget.*;
import java.io.*;
import android.content.pm.*;
import java.util.*;
import android.app.*;
import android.graphics.*;
import android.os.*;

public class Creating extends Activity1 implements Consts, AdapterView.OnItemSelectedListener{
	int hasinit;
	int extraType, // 0为文本，1为单个文件，2为多个文件
		readType,
		finalIcon;
	String extraText,
		extraMime,
		finalMime,
		finalAction,
		appoint_package,
		appoint_activity;
	@Override protected boolean onIdle(){
		switch(hasinit){
			case 0:
				if(getIntent().getBooleanExtra("instanced",false)){
					new Msgbox("访问被拒绝","你正在尝试通过快捷方式指向本程序，此操作被拒绝。","返回"){
						@Override protected void onClick(int i){
							finish();
						}
					};
					return false;
				}
				setContentView(layout.activity_creating);
				break;
			case 1:
				clearTemp=(CheckBox)fv(id.creat_autocleantemp);
				clearTemp.setChecked(true);
				
				later=(RadioButton)fv(id.creat_invoke_later);
				appoint=(RadioButton)fv(id.creat_invoke_appoint);
				
				preview=(ImageView)fv(id.creat_image);
				popupMenu=new PopupMenuCompat(this,preview,menu.icon_chooser);
				btnbind(preview, later,appoint);
				btnbind(id.creat_test, id.creat_ok);
				
				app_name=(TextView)fv(id.creat_appointed);
				name=(EditText)fv(id.creat_name);
				mime=(EditText)fv(id.creat_mimetype);
				actionSelector=(Spinner)fv(id.creat_intentaction);
				actionSelector.setOnItemSelectedListener(this);
				mimeSelector=(Spinner)fv(id.creat_mimestyle);
				mimeSelector.setOnItemSelectedListener(this);
				// 新增的 Spinner 在 idle 3
				break;
			case 2:
				Intent i=getIntent();
				String a=i.getAction(),
					type=finalMime=extraMime=i.getType();
				//tip(string(i.getData(),"\n",i.getType()));
				if(Intent.ACTION_SEND.equals(a)){ // 单个分享
					if(MIME_TYPE_TEXT_PLAIN.equals(type)){ // 分享的是文本
						extraText=i.getStringExtra(Intent.EXTRA_TEXT);
						setText(fv(id.creat_preview),"文本："+extraText);
						
						String s=i.getStringExtra(Intent.EXTRA_TITLE);
						setText(fv(id.creat_name),TextUtils.isEmpty(s)?"":s);
						
						mimeSelector.setEnabled(false);
					}else{ // 分享的应该是文件，比如图片什么的
						Uri u=i.getData();
						if(u instanceof Uri);else{ // 没给路径？
							u=i.getParcelableExtra(Intent.EXTRA_STREAM);
						}
						extraText=u.toString();
						setText(fv(id.creat_preview),"文件："+u.getPath());
						
						File f=new File(u.getPath());//通过这段代码预设快捷方式的名称
						String n=f.getName();
						if(extraText.contains("com.android.providers"))
							fv(id.notice_content_path).setVisibility(View.VISIBLE);
						if(extraText.startsWith("file")){
							setText(fv(id.creat_name),TextUtils.isEmpty(n)?"":n);
						}
						if(TextUtils.isEmpty(type)){
							
						}
						extraType=1;
					}
				}else if(Intent.ACTION_SEND_MULTIPLE.equals(a)){ // 多个分享
					new Msgbox("操作不支持","下一个版本才能支持多文件。","返回"){
						@Override protected void onClick(int i){
							finish();
						}
					};
					extraType=2;
				}
				int[]lstype={array.intentAction_name_text,array.intentAction_name_file,array.intentAction_name_file_multi};
				actionBase=new Base();
				String[]litem=getResources().getStringArray(lstype[extraType]);
				for(String item:litem)
					actionBase.add(item);
				
				actionSelector.setAdapter(actionBase);
				
				onItemSelected(actionSelector,null,0,0);
				onItemSelected(mimeSelector,null,0,0);
				break;
			case 3:
				switch(extraType){ // 此处表示，0只去除打开接口，1只去除复制接口，2只保留分享接口
					case 0: // 文本
					case 2: // 多个
						//openfile.setEnabled(false);
						if(extraType==0)break;
					case 1: // 单个
						//copytext.setEnabled(false);
				}
				//share.setChecked(true);
				onResume();
		}
		return hasinit++<9;
	}
	class Base extends BaseAdapter{
		ArrayList<String>a=new ArrayList<>();
		public void add(String s){ a.add(s); }
		public String get(int i){ return a.get(i).split("\\|")[0]; }
		@Override public int getCount(){ return a.size(); }
		@Override public Object getItem(int p){ return null; }
		@Override public long getItemId(int p){ return 0; }
		@Override public View getView(int p, View v, ViewGroup p3){
			String[]s=a.get(p).split("\\|");
			ViewGroup vg=v==null?
				inflateView(layout.listsub_spinner):
				(ViewGroup)v;
			setText(fv(vg,id.listsub_title),s[0]);
			setText(fv(vg,id.listsub_desc),
				s.length<2?
					"":
					s[1]
			);
			return vg;
		}
	}
	@Override protected void onResume() {
		super.onResume(); // 如果更改了这个函数的代码,记得要连 Main.java 一起
		if(hasinit==0)return;
		if(! getIntent().getBooleanExtra(CREATE_SOURCE_DESKTOP,false)){
			int stat=ShortcutManagerCompat.check();
			fv(id.notice_launcher_not_support)
				.setVisibility(
					stat==ShortcutManagerCompat.STAT_LAUNCHER_NOT_SUPPORT?
						View.VISIBLE:
						View.GONE);
		}
	}
	
	/*	1勾表示允许的操作，2勾表示允许这个操作指定默认程序
				文本	单个	多个	readType
		分享	√√		√√		√√		0
		打开			√√				1
		复制	√						2
		extraType 0		1		2
	*/
	ImageView preview;
	RadioButton later,appoint;
	CheckBox clearTemp;
	Button testLnk,addLnk;
	EditText name,mime;
	Spinner actionSelector,mimeSelector;
	Base actionBase;
	TextView app_name;
	final String
		appointed_null="未指定默认程序。",
		appointed_app="指定默认程序： %s 的 %s。";
	@Override public void onItemSelected(AdapterView<?>a,View b,int c,long d){
		if(a==actionSelector){//这块地方懒了，用 switch(getId()) 才是我的正常风格
			int[]lstype={array.intentAction_text,array.intentAction_file,array.intentAction_file_multi};
			finalAction=getResources().getStringArray(lstype[extraType])[c];
			boolean emp=TextUtils.isEmpty(finalAction);
			//若当前启用时为空，或者禁用时不为空，则进行重置。初始化也进行重置
			if(appoint.isEnabled() == emp || !(later.isChecked() | appoint.isChecked()))
				resetInvokable(!emp);
			//tip("yixuanze "+finalAction);
			return;
		}
		String[]s={extraMime,"text/*","image/*","audio/*","video/*","*/*"};
		if(c>=s.length){ // 自定义
			mime.setEnabled(true);
		}else{ // 预设
			mime.setText(finalMime=s[c]);
			mime.setEnabled(false);
		}
	}
	@Override public void onNothingSelected(AdapterView<?>a){}
	/*	resetType 的逻辑
		1.被调用于 当点击切换这个快捷方式的启动方式（分享、打开文件或复制文本）时
		2.当新的点击与之前选项相同时，不更改默认程序选项
		3.而如果点击了不同的选项，则刷新选项组，以及重设默认程序选项
	*/
	/*void resetType(int setTo){ // 重设快捷方式的类型
		if(readType==setTo)return;
		readType=setTo; // 这一行和上一行 履行 逻辑2
		RadioButton[]r={share,openfile,copytext};
		for(int i=0,len=r.length;i<len;i++)
			if(i != setTo)
				r[i].setChecked(false); // 这一行和以上 履行 逻辑3 中的 刷新选项组
		resetInvokable(setTo != 2);//只有2，也就是说，只有复制文本的类型，是不能选择调用的程序
	}*/
	void resetInvokable(boolean appointable){ // 重置这个快捷方式的行为调用的程序
		later.setChecked(true);
		appoint.setChecked(false);
		appoint.setEnabled(appointable);
		appoint_package=null; // 如果重置了默认程序，应该无效化这个变量
		app_name.setText(appointed_null);
	}
    @Override protected void oncreate(){}
	PopupMenu popupMenu;
	@Override public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case draw.topmenu_upload:
				startActivityForResult(
					new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
					ACTIVITY_FOR_RESULT_SELECTED_FILE);
				break;
			case draw.topmenu_edit:
				//tip("");
				break;
			default:
				finalIcon=item.getItemId(); // 懒起名字了，就把图标名字弄成菜单项目id
				preview.setImageResource(finalIcon);
				int[]auto={draw.file_word, draw.file_text, draw.file_music, draw.file_video, draw.file_image0, draw.file_image1, draw.file_image2, draw.file_image3};
				int[]autp={5,1,3,4,2,2,2,2};
				for(int i=0,len=auto.length;i<len;i++)
					if(finalIcon==auto[i] && TextUtils.isEmpty(extraMime))
						mimeSelector.setSelection(autp[i]);
		}
		return super.onOptionsItemSelected(item);
	}
	@Override public void onClick(View v){
		//int resetType=2;
		switch(v.getId()){
			case id.creat_image:
				popupMenu.show();
				break;
			/*case id.creat_interface_share: // 预计走到0，没问题
				resetType--;
			case id.creat_interface_open: // 预计走到1，没问题
				resetType--;
			case id.creat_interface_text:
				resetType(resetType); // resetType 的 逻辑1
				break;*/
			case id.creat_invoke_later:
				appoint.setChecked(false);
				appoint_package=null;//只要把一个设为null就行了，这个设为null就表示只在打开快捷方式时决定调用的程序
				app_name.setText(appointed_null);
				break;
			case id.creat_invoke_appoint:
				later.setChecked(true);
				onClick(later);
				Intent i=new Intent(finalAction).setType(prepareFinalMime()),
					c=new Intent(this,ActivitiesChooser.class).putExtra("intent",i);
				startActivityForResult(c,ACTIVITY_FOR_RESULT_SELECTED_ACTIVITY);
				break;
			case id.creat_test:
				testShortcut();
				break;
			case id.creat_ok:
				addShortcut();
		}
	}
	/**		onActivityResult 的运行流程
		1 ACTIVITY_FOR_RESULT_SELECTED_FILE
		1.1 已选择文件作为快捷方式的图标时触发。方法 onOptionsItemSelected 的 switch-case draw.topmenu_upload 包含入口
		1.2 进入后解析图像大小
		1.3 若图像宽高其一大于128像素或者无法读取，跳转到 2
		1.4 若图像宽高均未大于128像素，但用户仍然要求裁剪的，同样跳转到 2
		1.5 图像宽高均未大于128像素，用户选择直接使用的，跳转到 4
		
	 	2 ACTIVITY_FOR_RESULT_CROP_IMAGE_CONFIRMED
		2.1 在被要求裁剪已选择的图片文件时触发，通常由 1.3 和 1.4 进入
		2.2 配置好裁剪图片需求的参数，调用系统裁剪并跳转到 3
		
	 	3 ACTIVITY_FOR_RESULT_CROP_IMAGE_FINISHED
		3.1 在要求裁剪的图片完成裁剪时触发，如果未完成裁剪则强行中断，通常由 2.2 进入
		3.2 这个步骤通常是图片裁剪完成时的善后工作，包括设置图片框的图片，以及删除缓存文件
		3.3 为了设置图片框的图片，跳转到 4，而在 4 运行结束后，跳转到 3.4
		3.4 如果界面上【自动清理缓存】没有被勾选，则强行中断
		3.5 如果已勾选【自动清理缓存】则在 3 秒后调用删除缓存文件
		
	 	4 ACTIVITY_FOR_RESULT_SELECTED_IMAGE
		4.1 在一张图片最终被选择为快捷方式的图片时触发，通常由 1.5 和 3.3 进入
		4.2 将符合裁剪需求的图片设置在图片框
		
	 	5 ACTIVITY_FOR_RESULT_SELECTED_ACTIVITY
		5.1 选择【默认的 Activity】之后触发。方法 onClick 的 switch-case id.creat_invoke_appoint 包含入口
	*/
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		int recommendSize=128;
		switch(requestCode){
			case ACTIVITY_FOR_RESULT_SELECTED_FILE:
				if(resultCode==RESULT_CANCELED) break;
				boolean isImageTooLarge=false;
				int width=0,height=0;
				try{
					Uri ur=data.getData();
					if(ur instanceof Uri);else // 没给路径？
						ur=data.getParcelableExtra(Intent.EXTRA_STREAM);
					BitmapFactory.Options b=new BitmapFactory.Options();
					b.inJustDecodeBounds=true;
					InputStream is=getContentResolver().openInputStream(ur);
					BitmapFactory.decodeStream(is, null, b);
					is.close();
					width=b.outWidth;
					height=b.outHeight;
					if(b.outWidth>recommendSize || b.outHeight>recommendSize)
						isImageTooLarge=true;
				}catch(Throwable e){
					isImageTooLarge=true; // 自己无法解析图片，那么，丢给系统搞定他
				}
				if(isImageTooLarge){
					tip("图像文件略大，请裁剪");
					onActivityResult(ACTIVITY_FOR_RESULT_CROP_IMAGE_CONFIRMED,RESULT_OK,data);
				}else{
					final Intent dat=data;
					new Msgbox("裁剪图片？",string("选择了一张 ",width," x ",height," 的图片。"),"直接使用","裁剪"){
						@Override protected void onClick(int i){
							if(i==vbyes)
								onActivityResult(ACTIVITY_FOR_RESULT_SELECTED_IMAGE,RESULT_OK,dat);
							else if(i==vbno)
								onActivityResult(ACTIVITY_FOR_RESULT_CROP_IMAGE_CONFIRMED,RESULT_OK,dat);
						}
					};
				}
				break;
			case ACTIVITY_FOR_RESULT_CROP_IMAGE_CONFIRMED:
				data.setAction(ACTION_IMAGE_CROP) // http://blog.csdn.net/learningcoding/article/details/54669887
					.putExtra("crop",true)
					.putExtra("aspectX",1)
					.putExtra("aspectY",1)
					.putExtra("outputX",recommendSize)
					.putExtra("outputY",recommendSize)
					.putExtra("scale",true)
					.putExtra("return-data",false)
					.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,string("file://",DIR_cache_pic,"tmp.jpg"))
					.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString())
					.putExtra("noFaceDetection",true);
				startActivityForResult(data,ACTIVITY_FOR_RESULT_CROP_IMAGE_FINISHED);
				break;
			case ACTIVITY_FOR_RESULT_CROP_IMAGE_FINISHED: // 裁剪图片之后的善后工作
				if(resultCode==RESULT_CANCELED) break;
				onActivityResult(ACTIVITY_FOR_RESULT_SELECTED_IMAGE,RESULT_OK,data);
				if(!clearTemp.isChecked()) break; // 未选中时不清除缓存
				Uri u=data.getData()instanceof Uri?
					data.getData():
					data.getParcelableExtra(Intent.EXTRA_STREAM);
				
				tmpCleaner=new AsyncTask<Uri,Void,Boolean>(){
					@Override protected Boolean doInBackground(Uri[]u){
						try{
							Thread.sleep(3000);
						}catch(Throwable e){}
						return Media.deleteContentFile(Creating.this,u[0]); // 用户无感知情况下安全删除裁剪过的图片文件
					}
					@Override protected void onPostExecute(Boolean cleaned){
						if(cleaned) tip("缓存已清理");
					}
				}.execute(u); // 有比3秒还快的用户吗
				break;
			case ACTIVITY_FOR_RESULT_SELECTED_IMAGE: // 把图片设好在图片框里
				if(resultCode==RESULT_CANCELED) break;
				Uri u2=data.getData()instanceof Uri?
					data.getData():
					data.getParcelableExtra(Intent.EXTRA_STREAM);
				preview.setImageURI(u2);
				break;
			case ACTIVITY_FOR_RESULT_SELECTED_ACTIVITY:
				if(resultCode==RESULT_CANCELED){
					tip("已清除默认程序，请重新选择");
					return;
				}
				appoint_package=data.getStringExtra("pkg");
				appoint_activity=data.getStringExtra("act");
				try{
					String actName=null;
					PackageManager pm=getPackageManager();
					PackageInfo pi=pm.getPackageInfo(appoint_package,543);
					for(ActivityInfo ac:pi.activities)
						if(appoint_activity.equals(ac.name))
							actName=ac.loadLabel(pm).toString();
					ApplicationInfo ai=pi.applicationInfo;
					app_name.setText(String.format(appointed_app,ai.loadLabel(pm),actName));
				}catch(Throwable e){}
				later.setChecked(false);
				appoint.setChecked(true);
				//new Msgbox("","pkg: "+appoint_package+"\nact: "+appoint_activity,"ok");
		}
	}
	AsyncTask<Uri,Void,Boolean>tmpCleaner;
	
	void addShortcut(){
		String n=name.getText().toString();
		n=TextUtils.isEmpty(n)?"文件":n;
		if(getIntent().getBooleanExtra(CREATE_SOURCE_DESKTOP,false)){
			setResult(RESULT_OK,
				ShortcutManagerCompat.generate(n,
					ShortcutManagerCompat.ImageView2Bitmap(preview),
					generateLauncher(),
					true));
		}else{
			int stat=ShortcutManagerCompat.check();
			if(stat==ShortcutManagerCompat.STAT_OK){
				if(ShortcutManagerCompat.send(n,
					ShortcutManagerCompat.ImageView2Bitmap(preview),
					generateLauncher(),
					new AndroidShortcutCallback())){
						tip("已添加快捷方式到桌面，请查收");
				}
			}else{
				String[]statusHint={"未知问题","桌面程序不支持","权限未授予","未知问题3","安全问题"};
				String res=string("你授予“创建快捷方式”权限了吗？\n\n诊断结果：",statusHint[stat],"\n接口版本：",ShortcutManagerCompat.isNewApiEnable()?"较新":"原始","\n更多信息：",E.trace(ShortcutManagerCompat.LASTEST_EXCEPTION));
				new Msgbox("有些不对劲",res,"稍后再试");
				return;
			}
		}
		finish();
	}
	class AndroidShortcutCallback extends BroadcastReceiver {
		@Override public void onReceive(Context p1, Intent p2) {
			new Msgbox("完成","已收到回应。","立即结束","稍后"){
				@Override protected void onClick(int i){
					if(i == vbyes) finish();
				}
			};
		}
	}
	void testShortcut(){
		Intent i=generateLauncher();
		startActivity(i);
	}
	Intent generateLauncher(){
		Intent pending=null;
		String[]scheme={LINK_TYPE_TEXT, LINK_TYPE_FILE, LINK_TYPE_FILES},
			host={READ_TYPE_SHARE, READ_TYPE_FILE, READ_TYPE_TEXT};
		if(TextUtils.isEmpty(finalAction)) readType=2; //复制文本
		else if(finalAction.contains("SHARE")) readType=0; //分享
		else readType=1; //文件操作
		pending=new Intent(Intent.ACTION_MAIN)
			.setClass(this,ShortcutExecuter.class)//设快捷方式的执行者是ShortcutExecuter
			.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.putExtra("dataType",scheme[extraType])
			.putExtra("execType",host[readType])
			.putExtra("mime",prepareFinalMime())
			.putExtra("intentAction",finalAction)
			.putExtra("content",extraText);
		if(!TextUtils.isEmpty(appoint_package)){//指定了包，只可能是单个或多个文件
			pending.putExtra("pkg",appoint_package)
				.putExtra("act",appoint_activity);
		}
		return pending;
	}
	String prepareFinalMime(){
		if(mime.isEnabled())
			finalMime=mime.getText().toString();//已启用自定义，因此设为自定义的
		if(TextUtils.isEmpty(finalMime)) finalMime="*/*"; //最终mime是空，默认
		//这里一个elseif的原因是，会提供一个空mime的非自定选项。
		//即使自动为空，也默认是*/*
		return finalMime;
	}
}
