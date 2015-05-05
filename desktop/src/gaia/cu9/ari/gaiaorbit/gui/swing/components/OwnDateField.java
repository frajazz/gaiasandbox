package gaia.cu9.ari.gaiaorbit.gui.swing.components;

import java.util.Date;

import com.alee.extended.date.WebDateField;
import com.alee.utils.CompareUtils;

public class OwnDateField extends WebDateField {

    public void setDateSilent(final Date date)
    {
        final boolean changed = !CompareUtils.equals(this.date, date);
        this.date = date;

        // Updating field text even if there is no changes
        // Text still might change due to formatting pattern
        updateFieldFromDate();

        if (changed && calendar != null)
        {
            // Updating calendar date
            updateCalendarFromDate(date);
        }
    }

}
