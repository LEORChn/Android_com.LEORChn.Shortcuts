package appforms;
import leorchn.lib.*;
import android.view.*;
import android.content.*;
import android.content.pm.*;
import java.util.*;
import android.widget.*;
import android.widget.AdapterView.*;

public class ActivitiesChooser extends Activity1 implements AdapterView.OnItemClickListener{
	@Override public void onClick(View v){}
	ListView l;
	ListControl lc=new ListControl();
	PackageManager pm;
	@Override protected void oncreate(){
		setResult(RESULT_CANCELED);
		pm=getPackageManager();
		setContentView(layout.activity_activitieschooser);
		l=(ListView)fv(id.activitieschooser_list);
		l.setAdapter(lc);
		l.setOnItemClickListener(this);
	}

	@Override protected boolean onIdle(){
		Intent i=getIntent().getParcelableExtra("intent");
		List<ResolveInfo>l=pm.queryIntentActivities(i,0);
		if(l.size()==0){
			new Msgbox("没有支持的程序","没有找到支持此操作的程序。\n请更改调用方式或数据格式，或者安装一些支持的程序后再试。","返回上一页"){
				@Override protected void onClick(int i){
					finish();
				}
			};
			return false;
		}
		for(ResolveInfo r:l){
			if(getPackageName().equals(r.activityInfo.packageName))
				continue;
			Bean b=new Bean();
			b.r=r;
			lc.add(b);
		}
		lc.refresh();
		return false;
	}

	@Override public void onItemClick(AdapterView<?> p1, View p2, int p, long p4) {
		ActivityInfo a=lc.get(p).r.activityInfo;
		Intent i=new Intent()
			.putExtra("pkg",a.packageName)
			.putExtra("act",a.name);
		setResult(RESULT_OK,i);
		finish();
	}
	class Bean{
		static final int LAYOUT=layout.listsub_app_basic;
		ResolveInfo r;
	}
	class ListControl extends BaseAdapter{
		public CharSequence[]getAutofillOptions(){return null;}
		ArrayList<Bean>a=new ArrayList<>();
		int listItemCount;
		public void add(Bean b){ a.add(b); listItemCount++; }
		public void clear(){ a.clear(); listItemCount=0; refresh(); }
		public void refresh(){ notifyDataSetChanged(); }
		public Bean get(int p){ return a.get(p); }
		@Override public int getCount(){ return listItemCount; }
		@Override public Object getItem(int p){ return null; }
		@Override public long getItemId(int p){ return 0; }
		@Override public View getView(int p, View v, ViewGroup p3){
			Bean b=get(p);
			Holder d;
			if(v==null){
				v=inflateView(Bean.LAYOUT);
				ViewGroup w=(ViewGroup)v;
				d=new Holder();
				d.title=fv(w,id.listsub_title);
				d.image=(ImageView)fv(w,id.listsub_image);
				v.setTag(d);
			}else{
				d=(Holder)v.getTag();
			}
			setText(d.title,b.r.loadLabel(pm).toString());
			d.image.setImageDrawable(b.r.loadIcon(pm));
			//此处设置图片链接
			return v;
		}
	}
	class Holder{
		View title;
		ImageView image;
	}
}
