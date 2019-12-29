import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Assistent {
    //final static String datesFile = "/home/pi/Desktop/dates.txt";
    final static String datesFile = "C:/Users/geller/Desktop/dates.txt";
    //final static String mailsFile = "/home/pi/Desktop/mails.txt";
    final static String mailsFile = "C:/Users/geller/Desktop/mails.txt";
    final static
    DateFormat standardFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
    ArrayList<String> dates = new ArrayList();
    String mails;
    java.util.Timer timer = new java.util.Timer();

    public Assistent() {
        dates = new ArrayList(Arrays.asList(File.read(datesFile).split("\n")));
        System.out.println("Loaded " + dates);
        mails = File.read(mailsFile);
        if (mails.equals("")) {
            mails = "muellmelder@gmail.com";
        }
        System.out.println("Loaded " + mails);
        for (String date : dates) {
            String[] params = date.split(" ", 2);
            try {
                timer.schedule(new Timer(params[1]), standardFormat.parse(params[0]));
            } catch (Exception e) {
            }
        }
    }

    void loadDatesFromJson(String json) {
        DateFormat jsonFormat = new SimpleDateFormat("yyyyMMdd");
        JSONArray events = new JSONObject(json).getJSONArray("vcalendar").getJSONObject(0).getJSONArray("vevent");
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            String time = ((String) event.get("dtstart")).split("T")[0];
            System.out.println(time);
            String type = (String) event.get("description");
            if (type.contains("Rest")) {
                type = "Rest";
            } else if (type.contains("Papier")) {
                type = "Papier";
            } else if (type.contains("Gelb")) {
                type = "Gelb";
            } else if (type.contains("Schadstoffmobil")) {
                type = "Schadstoffmobil";
            }
            Calendar c = Calendar.getInstance();
            try {
                System.out.println(jsonFormat.parse(time));
                c.setTime(jsonFormat.parse(time));
                System.out.println(c.getTime());
                add(type, (Calendar) c.clone());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        System.out.println(dates);
        saveDates(dates);
    }

    void add(String type, Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        if (isFuture(calendar.getTime())) {
            timer.schedule(new Timer("asd"), (Date) calendar.getTime().clone());
            dates.add(calendar.getTime().toString() + "\\" + type);
        }
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        if (isFuture(calendar.getTime())) {
            timer.schedule(new Timer("asd"), (Date) calendar.getTime().clone());
            dates.add(calendar.getTime().toString() + "\\" + type);
        }
    }

    public void add(String in) {
        String[] data = in.split("\n");

        for (String str : data) {
            String[] params = str.split(" ");
            try {
                DateFormat formatter = new SimpleDateFormat("DD.MM");
                Calendar c = Calendar.getInstance();
                c.setTime(formatter.parse(params[0]));
                add(params[1], c);
            } catch (Exception e) {
                try {
                    DateFormat formatter = new SimpleDateFormat("DD.MM.YYYY");
                    Calendar c = Calendar.getInstance();
                    c.setTime(formatter.parse(params[0]));
                    add(params[1], c);
                } catch (Exception ex) {
                    System.out.println("Kein erlaubtes Datum gefunden.");
                }
            }
        }
        saveDates(dates);
    }

    String getDatesFile() {
        return File.read(datesFile);
    }

    void saveMails(String mails) {
        File.write(mailsFile, mails);
    }

    void saveDates(ArrayList<String> dates) {
        System.out.println("SAVE");
        internalSaveDates(dates);
        internalSaveDates(dates);
    }

    private void internalSaveDates(ArrayList<String> dates) {
        String out = "";
        synchronized (dates) {
            for (int i = 0; i < dates.size(); i++) {
                try {
                    Date date = standardFormat.parse(dates.get(i));
                    if (isFuture(date)) {
                        out += date + "\\" + dates.get(i).split("\\\\")[1] + "\n";
                    } else {
                        dates.remove(i);
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        File.write(datesFile, out);
    }

    boolean isFuture(String date) {
        try {
            return isFuture(standardFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean isFuture(Date date) {
        Date today = Calendar.getInstance().getTime();
        try {
            return date.after(today);
        } catch (Exception e) {
            return false;
        }
    }

    void addMail(String mail) {
        System.out.println(mail + " registered");
        mails += ", " + mail;
        SendMail.send(mail, "Registrierung Müllmelder", "Herzlich Wilkommen, \n" +
                "Sie sind ab sofort beim MüllMelder registriert und erhalten" +
                " Nachrichten zu kommenden Müllabfuhren");
        saveMails(mails);
    }

    class Timer extends TimerTask {
        String mode;
        String message = "", subject = "";

        public Timer(String mode) {
            this(mode, 0);
        }

        public Timer(String mode, int daysUntil) {
            this.mode = mode;
            if (daysUntil == 0) {
                message = "ACHTUNG! Heute wird der " + mode.toUpperCase() + "MÜLL abgeholt!";
                subject = "HEUTE " + mode.toUpperCase() + "MÜLL!";
            } else if (daysUntil == 1) {
                message = "ACHTUNG! Morgen wird der " + mode.toUpperCase() + "MÜLL abgeholt!";
                subject = mode.substring(0, 1).toUpperCase() + mode.substring(1) + "müll";
            } else {
                message = "Bald wird der " + mode.toUpperCase() + "MÜLL abgeholt!";
                subject = mode.substring(0, 1).toUpperCase() + mode.substring(1) + "müll";
            }
        }

        @Override
        public void run() {
            SendMail.send(mails, subject, message +
                    "\nDenk daran ihn rauszustellen!");
        }
    }
}
