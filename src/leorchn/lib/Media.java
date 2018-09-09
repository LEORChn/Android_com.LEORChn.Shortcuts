package leorchn.lib;
import android.app.*;
import android.content.*;
import android.database.*;
import android.net.*;
import android.os.*;
import java.io.*;
import leorchn.App;
import android.provider.*;
import android.media.*;

// 最低编译版本 应该是8，受到 MediaScannerConnection 限制
public class Media extends Impl{
	public static File Uri2File(Activity a,Uri u){ return Impl.Uri2File(a,u); }
	public static String Uri2StringPath(Activity a,Uri u){ return Impl.Uri2StringPath(a,u); }
	//@Deprecated public static void requestForRefreshGallery(Activity a,Uri u){ Impl.requestForRefreshGallery(a,u); }
	public static boolean deleteContentFile(Activity a,Uri u){ return Impl.deleteContentFile(a,u); }
}
class Impl{
	protected static File Uri2File(Activity a,Uri u){
		return new File(Uri2StringPath(a,u));
	}
	protected static String Uri2StringPath(Activity a,Uri u){
		String[]type={MediaStore.Images.Media.DATA};
		Cursor imgcur=a.managedQuery(u,type,null,null,null);
		int index=imgcur.getColumnIndexOrThrow(type[0]);
		imgcur.moveToFirst();
		return imgcur.getString(index);
	}
	/*protected static void requestForRefreshGallery(Activity a,Uri u){
		String[]p={Environment.getExternalStorageDirectory().toString()};
		if(u.getScheme().equals("content"))
			p[0]=Uri2File(a,u).getPath();
		Activity1.tip(Activity1.string("开始清理缓存\n",MediaStore.Images.Media.DATA,"=\"",p[0],"\""));
		Activity1.tip(""+
		App.getContext()
			.getContentResolver()
			.delete(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			Activity1.string(MediaStore.Images.Media.DATA,"=\"",p[0],"\""),
			null));
		/*MediaScannerConnection.scanFile(
			App.getContext(),
			p,
			null,
			new MediaScannerConnection.OnScanCompletedListener(){
				@Override public void onScanCompleted(String p1, Uri p2) {
					Activity1.tip("缓存清理完成");
				}
			});
		//App.getContext().sendBroadcast(new Intent());
	}*/
	protected static boolean deleteContentFile(Activity a,Uri u){ // 1 为成功
		return App.getContext() // 返回已成功删除的条目数，一般大于 0 即为成功
			.getContentResolver()
			.delete(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				Activity1.string(MediaStore.MediaColumns.DATA,"=\"",Uri2StringPath(a,u),"\""),
				null) > 0;
	}
}
