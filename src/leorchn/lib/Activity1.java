package leorchn.lib;
import android.os.*;
import android.app.*;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import leorchn.App;
import appforms.*;
public abstract class Activity1 extends Activity implements Consts,MessageQueue.IdleHandler,Thread.UncaughtExceptionHandler,View.OnClickListener{
	
	public static final String UA_win="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36",
	UA_android="Mozilla/5.0 (Linux; Android 4.4.4;) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36",
	UA_mobilebili="Mozilla/5.0 BiliDroid/4.11.7 (bbcallen@gmail.com)";
	
	public static String DIR_cache=App.getContext().getExternalCacheDir().getPath()+"/",
	DIR_data=App.getContext().getFilesDir().getPath()+"/",
	DIR_cache_pic=DIR_cache+"pic/",
	DIR_cache_log=DIR_cache+"log/";
//-----
	
//-----
	protected String http(String method,String url,String param,String formdata){//每个activity都可用的http，需要在其他线程
		return HttpRequest.http(method,url,param,formdata);
	}
	protected static class Http extends AsyncTask<String,Void,String>{//封装型异步http，一般不用
		public boolean isfin=false;
		public Http(String method,String url,String param,String formdata){
			execute(method,url,param,formdata);
		}
		@Override protected String doInBackground(String[]p){
			p[0]=HttpRequest.http(p[0],p[1],p[2],p[3]);
			isfin=true;
			return p[0];
		}
		@Override protected void onPostExecute(String p){ fin(p); }
		void fin(String data){}
	}
//-----
	protected ViewGroup inflateView(int id){return(ViewGroup)LayoutInflater.from(this).inflate(id,null);}
	protected void btnbind(View...v){ for(View btnv:v)btnv.setOnClickListener(this); }//连续绑定多个【动态】view的点击事件到本activity
	protected void btnbind(int...id){ for(int btnid:id)fv(btnid).setOnClickListener(this); }//连续绑定多个【静态】view的点击事件到本activity
	abstract public void onClick(View v);//每个窗口应该都有按钮吧？
	protected void setText(View v,String s){((TextView)v).setText(s);}
	protected void seticon(View v,android.graphics.Bitmap i){
		if(v instanceof ImageView){ ((ImageView)v).setImageBitmap(i); }
	}
	protected View fv(int id){return findViewById(id);}//查找当前activity唯一的
	protected View fv(ViewGroup vg,int id){return vg.findViewById(id);}//查找列表子项中唯一的
	protected static void tip(String s){Toast.makeText(App.getContext(),s,0).show();}
	private Thread.UncaughtExceptionHandler defUeh;
	private Activity1 This=this;//一个默认指向当前activity的指针，在内部类中使用
	public Activity1(){
		super();
		defUeh= Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		
	}
	@Override public void onCreate(Bundle sis){
		new Theme(this).set();
		super.onCreate(sis);
		oncreate();
		addIdleHandler();
	}
	abstract protected void oncreate();
	public void addIdleHandler(){ Looper.myQueue().addIdleHandler(this); }//添加一个初始化或空闲行为，大多数在Activity.onCreate使用
	public boolean queueIdle(){return onIdle();}
	//abstract protected void onCreate(Bundle sis);
	abstract protected boolean onIdle();//每个窗口应该都用这个来初始化
//-----
	protected static String string(Object...str){ return buildstring(str).toString(); }
	protected static StringBuilder buildstring(Object...str){
		StringBuilder bdr=new StringBuilder();
		return string(bdr,str);
	}
	protected static StringBuilder string(StringBuilder bdr,Object...str){
		for(Object s:str) bdr.append(s);
		return bdr;
	}
//-----
	@Override public void uncaughtException(final Thread thread, final Throwable ex) {
		rep = E.trace(ex);
		new AfterException();//新建线程显示消息
		//startActivity(new Intent(this,ExceptionReport.class).putExtra("info",errRep));
		//defUeh.uncaughtException(thread,ex);
	}
	private String rep; private AlertDialog excad=null;
	private class AfterException extends Thread implements DialogInterface.OnClickListener{
		public AfterException(){ this.start(); }
		public void run(){
			savelog();
			Looper.prepare();
			if(excad!=null)onClick(null,10);
			excad=new AlertDialog.Builder(This).setTitle("温和的错误提示")
				.setMessage("程序发生了错误并即将退出。以下信息已自动保存。\n"+rep)
				.setNeutralButton("关闭", this)
				.create();
			excad.show();
			Looper.loop();
		}
		public void onClick(DialogInterface p1, int p2) {
			System.exit(10);
		}
		void savelog(){
			try{
				File log=new File(DIR_cache_log);
				log.mkdirs();
				log=log.createTempFile(string(System.currentTimeMillis(),"_"),".log",log);
				Text.write(rep,log.getPath(),"utf-8");
			}catch(Exception e){}
		}
	}
	protected void pl(Object...o){tipl(o);}
	protected void tipl(Object...o){System.out.println(string(o));}
	HashMap<String,Bitmap>pics=new HashMap<>();
	
	protected class Msgbox extends AlertDialog.Builder implements DialogInterface.OnClickListener{
		protected int vbyes=AlertDialog.BUTTON_POSITIVE,
			vbno=AlertDialog.BUTTON_NEGATIVE,
			vbmid=AlertDialog.BUTTON_NEUTRAL;
		public Msgbox(String...msgs){
			super(Activity1.this); 
			for(int i=0,len=msgs.length;i<len;i++){
				switch(i){
					case 0: setTitle(msgs[0]); break;
					case 1: setMessage(msgs[1]); break;
					case 2: setPositiveButton(msgs[2],this); setCancelable(false); break;
					case 3: setNegativeButton(msgs[3],this); break;
					case 4: setNeutralButton(msgs[4],this); break;
				}
			}show();
		}
		public void onClick(DialogInterface p1,int p2){ onClick(p2); }
		protected void onClick(int i){}
	}

	protected abstract class CheckMsgbox extends AlertDialog.Builder implements DialogInterface.OnClickListener,DialogInterface.OnMultiChoiceClickListener {
		protected int vbyes=AlertDialog.BUTTON_POSITIVE,
		vbno=AlertDialog.BUTTON_NEGATIVE,
		vbmid=AlertDialog.BUTTON_NEUTRAL;
		boolean stat[];
		public CheckMsgbox(String[]msgs,boolean[]status,String...options){
			super(Activity1.this); 
			for(int i=0,len=msgs.length;i<len;i++){
				switch(i){
					case 0: setTitle(msgs[0]); break;
					case 1: setPositiveButton(msgs[1],this); setCancelable(false); break;
					case 2: setNegativeButton(msgs[2],this); break;
					case 3: setNeutralButton(msgs[3],this); break;
				}
			} stat=status;
			this.setMultiChoiceItems(options,status,this);
			show();
		}
		public void onClick(DialogInterface p1,int p2){ onClick(p2,stat); }
		public void onClick(DialogInterface p1,int p2,boolean p3){ stat[p2]=p3; onChange(p2,p3); }
		abstract void onClick(int i,boolean[]status)
		void onChange(int i,boolean status){}
	}
	public void onPointerCaptureChanged(boolean hasCapture){}
}
