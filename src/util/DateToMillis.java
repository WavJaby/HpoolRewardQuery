package util;

public class DateToMillis {
    final String DTSplit, DSplit, TSplit;

    public DateToMillis(String dtSplit, String dSplit, String tSplit) {
        this.DTSplit = dtSplit;
        this.DSplit = dSplit;
        this.TSplit = tSplit;
    }

    public long dateToMillis(String dateTime) {
        String[] dateAndTime = dateTime.split(DTSplit);
        String[] date = dateAndTime[0].split(DSplit);
        String[] time = dateAndTime[1].split(TSplit);

        long dayMillis = 24 * 60 * 60 * 1000;
        long year = (Long.parseLong(date[0]) - 1970);
        //閏年+年
        long yearMillis = ((long) (year / 4)) * dayMillis +
                year * 365 * dayMillis;

        long month = Long.parseLong(date[1]);
        long monthMillis = (month - 1) * 30 * dayMillis;
        if (month > 2) {
            monthMillis -= dayMillis;
            if (!isLeap(Integer.parseInt(date[0]))) {
                monthMillis -= dayMillis;
            }
        }
        //大月
        if (month > 7)
            month++;
        monthMillis += (month / 2) * dayMillis;

        long timeAdder = Long.parseLong(time[2].replace(".", "")) +
                Long.parseLong(time[1]) * 60 * 1000 +
                Long.parseLong(time[0]) * 60 * 60 * 1000 +
                Long.parseLong(date[2]) * dayMillis +
                monthMillis +
                yearMillis;

        //GMT+8
        timeAdder -= 8 * 60 * 60 * 1000;

        return timeAdder;
    }

    private boolean isLeap(int year) {
        //被4整除且不被100整除 或 被400整除
        return ((year % 4) == 0 && (year % 100) != 0) || (year % 400 == 0);
    }
}
