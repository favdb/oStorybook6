package storybook.ui.panels.book;

import java.util.Date;
import javax.swing.JLabel;
import storybook.tools.DateUtil;

@SuppressWarnings("serial")
public class DateLabel extends JLabel {

	private Date date;

	public DateLabel(Date date, boolean... seconds) {
		super();
		this.date = date;
		setText(getDateText());
		setToolTipText(getDateText(seconds));
		setOpaque(true);
	}

	public final String getDateText(boolean... seconds) {
		if (date == null) {
			return "";
		}
		return DateUtil.simpleDateTimeToString(date, seconds);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
