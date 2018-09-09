package appforms;

import leorchn.lib.*;
import android.view.*;
import android.net.*;
import android.content.*;

public class ShortcutExecuter extends Activity1 implements Consts{
	@Override protected boolean onIdle(){ return false; }
	@Override public void onClick(View v){}
	@Override protected void oncreate(){
		Intent u=getIntent();
		pl("start execute"); pl(u.toString());
		String[]scheme={LINK_TYPE_TEXT, LINK_TYPE_FILE, LINK_TYPE_FILES},
			host={READ_TYPE_SHARE, READ_TYPE_FILE, READ_TYPE_TEXT};
		int s=-1,h=-1;
		for(int i=0,len=scheme.length;i<len;i++)
			if(scheme[i].equals(u.getStringExtra("dataType")))
				s=i;
		for(int i=0,len=host.length;i<len;i++)
			if(host[i].equals(u.getStringExtra("execType")))
				h=i;
		Intent i=null;
		switch(h){
			case 0: // 分享接口
				String[]intentExtraType={Intent.EXTRA_TEXT,Intent.EXTRA_STREAM,Intent.EXTRA_STREAM};
				i=new Intent(s==2?Intent.ACTION_SEND_MULTIPLE:Intent.ACTION_SEND)
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					.setType(u.getStringExtra("mime"));
				if(u.getStringExtra("pkg") != null)
					i.setClassName(u.getStringExtra("pkg"),u.getStringExtra("act"));
				if(s != 2){
					i.putExtra(intentExtraType[s],u.getStringExtra("content"));
				}else{ // else 添加多个文件
					
				}
				break;
			case 1: // 打开文件接口 - 只可能只有一个文件
				Uri r=Uri.parse(u.getStringExtra("content"));
				String action=u.getStringExtra("intentAction");
				i=new Intent(action==null?Intent.ACTION_VIEW:action)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
					.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
					.setDataAndType(r,u.getStringExtra("mime"));
				if(u.getStringExtra("pkg") != null)
					i.setClassName(u.getStringExtra("pkg"),u.getStringExtra("act"));
				break;
			case 2: // 复制文本接口 - 只可能是文本类型
				String t=u.getStringExtra("content");
				ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
				cm.setPrimaryClip(ClipData.newPlainText("Label",t));
				tip("已复制文本：\n"+(t.length()>15?(t.substring(0,15)+"..."):t));
				finish();
				return;
		}
		if(i != null){
			i.putExtra("instanced",true);
			try{
				startActivity(i);
			}catch(Throwable e){
				Class<?>[]eType={SecurityException.class,Throwable.class};
				String[]eDesc={"安全问题","未准确识别"};
				int eIndex=0;
				for(int len=eType.length;eIndex<len;eIndex++){
					if(e.getClass().isAssignableFrom(eType[eIndex])){
						break;
					}
				}
				String s2=string("dataType=",u.getStringExtra("dataType"),
						"&execType=",u.getStringExtra("execType"),
						"&mime=",u.getStringExtra("mime"),
						"&intentAction=",u.getStringExtra("intentAction"),
						"&content=",u.getStringExtra("content"));
				if(eIndex>=eType.length) eIndex=eType.length-1; // 迷之bug，OutOfBound
				new Msgbox("发生问题",string("诊断结果：",eDesc[eIndex],"\n建议：试试调整其他参数\n",s2,E.trace(e)),"ok"){
					protected void onClick(int i){
						finish();
					}
				};
				return;
			}
		}else{
			tip("快捷方式无效。");
		}
		finish();
	}

	
	
}
