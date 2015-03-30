package team.xinyuan519.wxsearch.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parameters {
	private String key;
	private String uin;
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getUin() {
		return uin;
	}
	
	public void setUin(String uin) {
		this.uin = uin;
	}
	
	public Parameters(String filePath){
		this.setPatameters(filePath);
	}
	
	public void setPatameters(String filePath) {
		File file = new File(filePath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
			new FileInputStream(file)));
			String rawurl = new String();
			rawurl = reader.readLine();
			Pattern pKey = Pattern.compile("(?<=&key=)(.*?)(?=&)");
			Pattern pUin = Pattern.compile("(?<=&uin=)(.*?)(?=&)");
			Matcher mKey = pKey.matcher(rawurl);
			Matcher mUin = pUin.matcher(rawurl);
			if(mKey.find()&&mUin.find()){
				this.setKey(mKey.group());
				this.setUin(mUin.group());
				
			}
			else{
				this.setKey(null);
				this.setUin(null);
				System.out.println(String.format("获取参数失败，请检查文件%s 里的内容",filePath));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
	
			e.printStackTrace();
		}
		finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
