package me.silviudraghici.silvermessenger;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Silviu on 2015-10-07.
 */
public class MessageBundle {
    public String message;
    public String originator;
    public Date sendDate;

    public MessageBundle(String message, String originator, Date sendDate){
        this.message = message;
        this.originator = originator;
        this.sendDate = sendDate;
    }

    public String time(){
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        String time = df.format(sendDate);
        return time;
    }
}
