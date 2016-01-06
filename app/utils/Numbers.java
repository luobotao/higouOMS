package utils;

import java.text.DecimalFormat;

import play.Logger;


public class Numbers {
	
	public static Integer parseInt(String str, Integer defaultValue){
		try{
			str = str.replace(".0", "");
			return Integer.parseInt(str);
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public static Long parseLong(String str, Long defaultValue){
		try{
			str = str.replace(".0", "");
			return Long.parseLong(str);
		}catch(Exception e){
			return defaultValue;
		}
	}
	public static Double parseDouble(String str, Double defaultValue){
		try{
			return Double.parseDouble(str);
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public static String formatDouble(double str, String regex){
		DecimalFormat df = new DecimalFormat(regex);
		return df.format(str);
	}
	
	
	public static void main(String[] args) {
		String a="22.0";
		System.out.println(Numbers.parseInt(a, 0));
	}
}
