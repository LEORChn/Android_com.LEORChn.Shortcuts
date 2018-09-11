package appforms;
import leorchn.lib.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.net.*;
import java.io.*;

public class Main extends Activity1 implements Consts{
    @Override protected void oncreate(){
        setContentView(layout.activity_help);
    }
	@Override protected void onResume() {
		super.onResume(); // 如果更改了这个函数的代码,记得要连 Creating.java 一起
		if(Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction()))
			fv(id.help_isfromdesktop).setVisibility(View.VISIBLE);
		else{
			int stat=ShortcutManagerCompat.check();
			fv(id.notice_launcher_not_support)
				.setVisibility(
					stat==ShortcutManagerCompat.STAT_LAUNCHER_NOT_SUPPORT?
						View.VISIBLE:
						View.GONE);
		}
	}
	String themeAsk=DIR_data+"THEME_FIRST_ASK",
		themePunish=DIR_data+"THEME_MATERIAL_PUNISH",
		hintSelectFolder=DIR_data+"HINT_SELECT_FOLDER";
	@Override protected boolean onIdle(){
		if(Sys.apiLevel()>=21){
			File f=new File(themeAsk);
			if(!f.exists()){//没有询问
				try{
					f.createNewFile();
				}catch(Throwable e){}
				new Msgbox("Material Design","你的系统支持 Material Design 主题，要立即使用吗？","是","不再询问"){
					@Override protected void onClick(int i){
						if(i==vbyes){
							Theme.reset(Theme.MaterialBlack);
							File f=new File(themePunish);
							try{
								f.createNewFile();
							}catch(Throwable e){}
							startActivity(new Intent(Main.this,Main.class));
							finish();
						}
					}
				};
			}else if(1==0){//暂时还是不决定启动彩蛋了 //已询问，如果有 THEME_MATERIAL_PUNISH 则启动惩罚小彩蛋
				f=new File(themePunish);
				if(f.exists()){
					new Msgbox("哎呀哎呀","感受一下来自 AlertDialog 的恶意。",".","我认输"){
						@Override protected void onClick(int i){
							if(i==vbyes){
								new Msgbox("祝贺","不管怎么说，羡慕你还能点到对话框中的按钮，哪像作者老点不到，只能强制用 Holo 度日...","摸摸");
							}else if(i==vbno){
								new Msgbox("What?","你认输？\n噢，那可真气人呢~\n这也是作者不喜欢用 Material Design 的原因。","好吧");
							}
							new File(themePunish).delete();
						}
					};
				}
			}
		}else
			fv(id.help_theme).setEnabled(false);
		btnbind(id.help_clipboard, id.help_file, id.help_folder, id.help_donate, id.help_report, id.help_theme);
		try{
			Window w=getWindow();
			w.setTitle(
				string(
					getString(string.app_name_main),
					getString(string.app_v),
					getPackageManager().getPackageInfo(getPackageName(),0).versionName));
		}catch(Throwable e){}
		return false;
	}
	@Override public void onClick(View v){
		Intent i=null;
		switch(v.getId()){
			case id.help_clipboard:
				ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
				i=new Intent(Intent.ACTION_SEND)
					.setType("text/plain")
					.setClass(this,Creating.class)
					.putExtra(Intent.EXTRA_TEXT,cm.getText());
				break;
			case id.help_file:
				i=new Intent(Intent.ACTION_GET_CONTENT)
					.setType("*/*");
				startActivityForResult(i,ACTIVITY_FOR_RESULT_SELECTED_FILE);
				return;
			case id.help_folder:
				i=new Intent(Intent.ACTION_GET_CONTENT)
					.setType("*/*");
				final File f=new File(hintSelectFolder);
				if(f.exists())
					startActivityForResult(i,ACTIVITY_FOR_RESULT_SELECTED_FOLDER);
				else{
					final Intent k=i;
					new Msgbox("提示","【选择文件夹】功能需要选择文件夹内的文件，随后程序会自动判断为所选择的文件所在的文件夹。","确定","不再提示"){
						@Override protected void onClick(int i){
							if(i==vbno)
								try{
									f.createNewFile();
								}catch(Throwable e){}
							startActivityForResult(k,ACTIVITY_FOR_RESULT_SELECTED_FOLDER);
						}
					};
				}
				return;
			case id.help_donate:
				i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://leorchn.github.io/?about"));
				break;
			case id.help_report:
				try{
					getPackageManager().getPackageInfo("com.tencent.mobileqq",543).activities.toString();
					i=new Intent(Intent.ACTION_VIEW,Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=522237296"));
				}catch(Throwable e){
					new Msgbox("找不到程序","需要以下程序才能运行：\nQQ","OK");
				}
				break;
			case id.help_theme:
				if(Theme.isMaterialDesign())
					Theme.reset(0);
				else
					Theme.reset(Theme.MaterialBlack);
				startActivity(new Intent(Main.this,Main.class));
				finish();
		}
		if(i != null)
			execIntent(i);
	}
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode != RESULT_OK) return;
		switch(requestCode){
			case ACTIVITY_FOR_RESULT_SELECTED_FILE: // 使用 “选择文件” 返回成功
				data.setAction(Intent.ACTION_SEND)
					.setClass(this,Creating.class);
				execIntent(data);
				break;
			case ACTIVITY_FOR_RESULT_SELECTED_FOLDER:
				Uri u=data.getData()==null?
					data.getParcelableExtra(Intent.EXTRA_STREAM):
					data.getData();
				File f;
				if(u==null){
					new Msgbox("获取路径发生异常","路径数据始终为空。","ok");
					return;
				}else if("content".equals(u.getScheme())){
					String s=Media.Uri2StringPath(this,u);
					if(android.text.TextUtils.isEmpty(s)){
						new Msgbox("转换路径发生异常","路径数据为空，请尝试用其他程序选择文件。","ok");
						return;
					}
					f=new File(s);
				}else if(!"file".equals(u.getScheme())){
					new Msgbox("获取路径发生异常",string("路径类型不匹配：",u.getScheme()),"ok");
					return;
				}else
					f=new File(u.getPath());
				u=Uri.parse(string("file://",f.getParent()));
				data.setAction(Intent.ACTION_SEND)
					.setClass(this,Creating.class)
					.setDataAndType(u,MIME_TYPE_FOLDER);
				execIntent(data);
				break;
			case ACTIVITY_FOR_RESULT_CREATE_SOURCE_DESKTOP: // 在桌面使用 “创建快捷方式” 并进入编辑界面返回成功
				setResult(RESULT_OK,data);
				finish();
		}
	}
	void execIntent(Intent i){
		if(Intent.ACTION_SEND.equals(i.getAction()) &&
			Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())){
			i.putExtra(CREATE_SOURCE_DESKTOP,true);
			startActivityForResult(i,ACTIVITY_FOR_RESULT_CREATE_SOURCE_DESKTOP);
		}else{
			startActivity(i);
		}
	}
	
}
