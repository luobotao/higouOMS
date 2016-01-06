package forms;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.field.ScaledDurationField;

import com.ibm.icu.text.SimpleDateFormat;

import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;
import utils.Dates;

public class DateSimpleBetween {

	public static void register() {
		Formatters.register(DateSimpleBetween.class,
				new SimpleFormatter<DateSimpleBetween>() {

					private final Pattern pattern = Pattern
							.compile("(\\d{4}/(?:0?[1-9]|1[0-2])/(?:0?[0-9]|[1-2][0-9]|3[0,1]))\\s-\\s(\\d{4}/(?:0?[1-9]|1[0-2])/(?:0?[0-9]|[1-2][0-9]|3[0,1]))");

					@Override
					public DateSimpleBetween parse(String str, Locale locale)
							throws ParseException {
						Matcher matcher = pattern.matcher(str);
						DateSimpleBetween between = new DateSimpleBetween();
						if (matcher.matches()) {
							between.start = Dates.parseDate(matcher.group(1));
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
							between.end = Dates.parseDate(matcher.group(2));
						}
						return between;
					}

					@Override
					public String print(DateSimpleBetween between, Locale locale) {
						return between.toString();
					}

				});
	}
	
	public Date start = new Date();
	public Date end = new Date();

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Dates.formatSimpleDate(start)).append(" - ")
				.append(Dates.formatSimpleDate(end));
		return sb.toString();
	}
}
