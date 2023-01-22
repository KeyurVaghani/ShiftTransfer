import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        MailUtils mail = new MailUtils();
        mail.listenMails();
    }
}
