package leorchn.lib;
import java.io.*;

public class Theme{
	public static final int
		MaterialWhite=16974391,
		MaterialBlack=16974372,
			HoloWhite=16973934,
			HoloBlack=16973931,
				White=16973836,
				Black=16973832;
	private Activity1 a;
	private static String path=Activity1.DIR_data+"THEME_FORCE_MATERIAL";
	public Theme(Activity1 activity){
		a=activity;
	}
	public static boolean isMaterialDesign(){
		return new File(path).exists();
	}
	public static void reset(int theme){
		File f=new File(path);
		if(theme==MaterialBlack){
			if(!f.exists())
				try{
					f.createNewFile();
				}catch(Throwable e){}
		}else
			f.delete();
	}
	public void set(){
		if(new File(path).exists())
			set(MaterialBlack);
	}
	public void set(int themeType){
		a.setTheme(themeType);
	}
}
