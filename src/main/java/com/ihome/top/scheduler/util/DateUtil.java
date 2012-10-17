/**
 * top-task-scheduler
 *  
 */

package com.ihome.top.scheduler.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * <p>
 * 时间工具
 * </p>
 * 
 * @author <a href="mailto:sihai@taobao.com">sihai</a>
 *
 */
public class DateUtil {
	
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	public static final String format(Date date) {
		return format(DEFAULT_FORMAT, date);
	}
	
	public static final String format(String format, Date date) {
		if(date == null) {
			return "";
		}
		return new SimpleDateFormat(format).format(date);
	}
	
	/**
	 * 获取今天的日期，去掉小时，分钟，秒
	 * 也就是今天第一秒
	 * @return
	 */
	public static final Date getToday() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		return calendar.getTime();
	}
	
	/**
	 * 获取今天的日期，去掉小时，分钟，秒
	 * 也就是今天第一秒
	 * @return
	 */
	public static final Date getToday(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		return calendar.getTime();
	}
	
	/**
	 * 
	 * @return
	 */
	public static Date getYestoday() {
    	Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 1);
		return calendar.getTime();
    }
	
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public static Date getYestoday(Date date) {
    	Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 1);
		return calendar.getTime();
    }
	
	/**
	 * 获取明天的日期，去掉小时，分钟，秒
	 * 也就是明天第一秒
	 * @return
	 */
	public static final Date getTomorrow() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		return calendar.getTime();
	}
	
	/**
	 * 生成随机的秒
	 * @return
	 */
	public static int randomSecond() {
		Random rand = new Random(System.currentTimeMillis());
		return rand.nextInt(60);
	}
	
	/**
	 * 生成随机分钟
	 * @return
	 */
	public static int randomMinute() {
		Random rand = new Random(System.currentTimeMillis());
		return rand.nextInt(60);
	}
	
	/**
	 * 生成随机的小时
	 * @return
	 */
	public static int randomHour() {
		Random rand = new Random(System.currentTimeMillis());
		return rand.nextInt(24);
	}
}
