package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

/**
 * 日期工具类
 * @author luobotao
 * Date: 2015年4月18日 下午12:27:15
 */
public class Dates {
    private static final Map<String, SimpleDateFormat> formatCache = new ConcurrentHashMap<>();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

    private static final SimpleDateFormat CHINESE_DATE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日");

    private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    
    private static final SimpleDateFormat ENGLISH_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String formatDate(Date date) {
    	if(date==null){
    		return "";
    	}
        return CHINESE_DATE_FORMAT.format(date);
    }
    
    public static String formatSimpleDate(Date date) {
    	if(date==null){
    		return "";
    	}
    	return DATE_FORMAT.format(date);
    }

    public static String formatDateTime(Date date) {
    	if(date==null){
    		return "";
    	}
        return CHINESE_DATE_TIME_FORMAT.format(date);
    }
    
    public static String formatEngLishDateTime(Date date) {
    	if(date==null){
    		return "";
    	}
    	return ENGLISH_DATE_TIME_FORMAT.format(date);
    }

    public static String formatDate(Date date, String format) {
        if (null == date || StringUtils.isBlank(format)) {
            return "";
        }
        SimpleDateFormat formatter;
        if (formatCache.containsKey(format)) {
            formatter = formatCache.get(format);
        } else {
            formatter = new SimpleDateFormat(format);
        }

        return formatter.format(date);
    }

    public static Date parseDate(String str) {
        try {
            return DATE_FORMAT.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseDate(String source, String format) {
        if (StringUtils.isBlank(source) || StringUtils.isBlank(format)) {
            return null;
        }
        SimpleDateFormat formatter;
        if (formatCache.containsKey(format)) {
            formatter = formatCache.get(format);
        } else {
            formatter = new SimpleDateFormat(format);
        }

        try {
            return formatter.parse(source);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String convert(String source, String sourceFormat, String targetFormat) {
        if (StringUtils.isBlank(source) || StringUtils.isBlank(sourceFormat) || StringUtils.isBlank(targetFormat)) {
            return null;
        }

        return formatDate(parseDate(source, sourceFormat), targetFormat);
    }


    public static Date getBeginOfDay(Date date) {
        if (null == date) {
            return date;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static Date getEndOfDay(Date date) {
        if (null == date) {
            return date;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    public static boolean isBetween(Date date, Date begin, Date end) {
        if (null == date) {
            return false;
        }

        if (begin != null && end != null) {
            return !date.before(begin) && !date.after(end);
        }

        if (begin != null) {
            return !date.before(begin);
        }

        if (end != null) {
            return !date.after(end);
        }

        return true;
    }
    
    /**
     * 
     * <p>Title: getDateBetween</p> 
     * <p>Description: 获取两个时间段内的日期</p> 
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<String> getBetweenDays(Date startDate, Date endDate){
	    List<String> datePeriodList = new ArrayList<String>();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(endDate);
	    int inputDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
	    int beforeDays = getBeforeDays(startDate, endDate);
	    for(int i=beforeDays;i>=0;i--){
	      cal.set(Calendar.DAY_OF_YEAR , inputDayOfYear-i);
	      datePeriodList.add(dateFormat.format(cal.getTime()));
	    }
	    return datePeriodList;
	  }
    
	  /**
	   * 
	   * <p>Title: getBeforeDays</p> 
	   * <p>Description: 获取一时间段内的差值</p> 
	   * @param startDate
	   * @param endDate
	   * @return
	   */
	  public static int getBeforeDays(Date startDate, Date endDate) {
		  SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		  Calendar cal1=Calendar.getInstance();
		  cal1.setTime(startDate);
		  Calendar cal2=Calendar.getInstance();
		  cal2.setTime(endDate);
		  long l=cal2.getTimeInMillis()-cal1.getTimeInMillis();
		  int days=new Long(l/(1000*60*60*24)).intValue();
		  return days;
	  }

    
    
    public static void main(String[] args) {
    	Date a = Dates.parseDate("2015-04-15 14:32:44","yyyy-MM-dd HH:mm:ss");

    	List<String> dateList = Dates.getBetweenDays(new Date(),new Date());
    	for (String string : dateList) {
			System.out.println(string);
		}
    	//System.out.println(a);
    	
	}

}
