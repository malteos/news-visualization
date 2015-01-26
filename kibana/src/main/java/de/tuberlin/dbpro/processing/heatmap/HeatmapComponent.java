package de.tuberlin.dbpro.processing.heatmap;

import java.util.List;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import de.tuberlin.dbpro.model.Document;
import de.tuberlin.dbpro.model.HeatmapAnnotation;
import de.tuberlin.dbpro.processing.ProcessingComponent;
import de.tuberlin.dbpro.processing.ProcessingException;
import de.tuberlin.dbpro.processing.ner.stanford.StanfordNERComponent;

public class HeatmapComponent implements ProcessingComponent {

	final private static Logger LOG = Logger.getLogger(HeatmapComponent.class);

	public static String parseDate(String dateS) throws ParseException {
		String hourSlot = generateHourSlot(dateS);
		String dateAlone = dateS.substring(0, 10);

		TimeZone zone = TimeZone.getTimeZone("CET");
		Calendar calendar = Calendar.getInstance(zone);

		// Date formatter to get the weekday an hour of the timestamp attribut
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd",
				Locale.ENGLISH);
		formatter.setTimeZone(zone);
		Date d = null;

		d = formatter.parse(dateAlone);
		calendar.setTime(d);
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		weekday = (weekday <= 1) ? 7 : weekday - 1;
		String weekdayS = "";
		switch (weekday) {
		case 1:
			weekdayS = "Mo";
			break;
		case 2:
			weekdayS = "Tu";
			break;
		case 3:
			weekdayS = "We";
			break;
		case 4:
			weekdayS = "Th";
			break;
		case 5:
			weekdayS = "Fr";
			break;
		case 6:
			weekdayS = "Sa";
			break;
		case 7:
			weekdayS = "Su";
			break;
		}
		String parsedDate = weekday + "-" + weekdayS + ":" + hourSlot;
		// System.out.println(parsedDate);
		// System.out.println(weekdayS);
		// System.out.println(weekday);
		// System.out.println(d);
		//
		// System.out.println(dateAlone);

		return parsedDate;
	}

	public static String generateHourSlot(String dateS) {
		String hourS = dateS.substring(11, 13);
		int hour = Integer.parseInt(hourS);
		String hourStart = "";
		String hourEnd = "";
		if (hour < 10)
			hourStart = "0" + hour;
		else
			hourStart = String.valueOf(hour);

		if (hour + 1 < 10)
			hourEnd = "0" + (hour + 1);
		else
			hourEnd = String.valueOf(hour + 1);

		String hourSlot = hourStart + "-" + hourEnd;
		return hourSlot;
	}

	public static void main(String[] args) throws ParseException {
		String dateS = "2014-01-01T09:58:50+0100";

		// System.out.println(parseDate(dateS));

	}

	public Document process(Document doc) throws ProcessingException {
		String dateS = doc.getDateString();

		HeatmapAnnotation heatmapAnno = null;
		if (doc.getFullText() != null && doc.getFullText().length() > 0) {
			try {
				heatmapAnno = new HeatmapAnnotation();
				String parsedDate = parseDate(dateS);
				heatmapAnno.setHeatmapDate(parsedDate);
			} catch (ParseException e) {
				LOG.warn("Failed to perform parse DateString to HeatmapString. Will not annotate document "
						+ doc.getId() + " Error message: " + e);
				heatmapAnno = null;
			}
			// Add annotation to document
			if (heatmapAnno != null) {
				System.out.println(heatmapAnno.getHeatmapDate());
				System.out.println(doc.getDateString());
				doc.addAnnotation(heatmapAnno);
			}
		}
		return doc;
	}

}
