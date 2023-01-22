import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Properties;

public class MailUtils {

    public void listenMails() {
        int count = 0;
        while (true) {

            if (checkMail(Utils.HOST_NAME, Utils.USERNAME, Utils.PASSWORD)) {
                count++;
                if (count == 1) {
                    return;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
    }

    public Store connectStore(String host, String user, String password) throws MessagingException {
        Properties properties = new Properties();

        properties.put(Utils.HOST, host);
        properties.put(Utils.PORT, Utils.PORT_NUMBER);
        properties.put(Utils.START_TTL, "true");
        properties.put(Utils.SSL, host);

        Session emailSession = Session.getDefaultInstance(properties);
        Store store = emailSession.getStore("imaps");
        store.connect(host, user, password);

        return store;
    }

    public boolean validateDate(String date){
        for(String d: Utils.dates){
            if(date.equals(d)){
                return true;
            }
        }
        return false;
    }

    public boolean checkMail(String host, String user, String password) {
        try {
            Store store = connectStore(host, user, password);
            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);

            Flags seen = new Flags(Flags.Flag.SEEN);
            SearchTerm senderTerm = new FromTerm(new InternetAddress("mail@paper.co"));
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);

            SearchTerm finalSearchTerm = new AndTerm(senderTerm, unseenFlagTerm);

            Message[] messages = inbox.search(finalSearchTerm);


            if (messages.length == 0) {
                System.out.println(LocalTime.now() + "  No mails found");
                inbox.close(false);
                store.close();
                return false;
            }

            try {
                Message message = messages[messages.length - 1];

                if (message.getSubject().equals("Shift Transfer Request")) {
                    System.out.println("---------------------------------");
                    System.out.println(System.nanoTime());
                    System.out.println("Email Number " + (messages.length));
                    System.out.println("Subject: " + message.getSubject());
                    System.out.println("From: " + message.getFrom()[0]);

                    if (message.isMimeType("multipart/*")) {
                        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                        WebScraper scraper = new WebScraper();
                        String result = scraper.getTextFromMimeMultipart(mimeMultipart);
                        String[] results = result.split("\\s+");
                        String url = (String) results[44].subSequence(1, results[44].length() - 1);
                        URI myUrl = new URI(url);
//                        for (int i = 0; i < results.length; i++) {
//                            System.out.println(results[29]+" "+results[30]+" "+results[31]);
//                        }
                        String date = results[29]+" "+results[30]+" "+results[31];
                        System.out.println("date: "+date);
                        if(validateDate(date)){
                            System.out.println("Clicked on"+date);
                            scraper.openWebpage(myUrl);
                        }
                        scraper.openWebpage(myUrl);
                        return true;

                    }
                } else {
                    System.out.println("nothing yet");
                }
            } catch (MessagingException | URISyntaxException | IOException e) {
                e.printStackTrace();
            }

            inbox.close(false);
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return false;
    }
}
