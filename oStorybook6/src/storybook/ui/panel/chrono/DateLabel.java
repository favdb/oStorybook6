package storybook.ui.panel.chrono;

import java.util.Date;
import javax.swing.JLabel;
import storybook.tools.DateUtil;
import storybook.tools.swing.SwingUtil;

@SuppressWarnings("serial")
public class DateLabel extends JLabel {

	private Date date;

	public DateLabel(Date date, boolean... seconds) {
		super();
		this.date = date;
		//setFont(App.getInstance().fontGetDefault());
		setText(getDateText());
		setToolTipText(getDateText(seconds));
		//setIcon(IconUtil.getIconSmall(ICONS.K.VW_CHRONO));
		setOpaque(true);
		//setHorizontalAlignment(SwingConstants.CENTER);
	}

	public final String getDateText(boolean... seconds) {
		if (date == null) {
			return "";
		}
		String dateStr = DateUtil.simpleDateTimeToString(date, seconds);
		String dayStr = SwingUtil.getDayName(date);
		return /*dayStr + " - " + */ dateStr;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
