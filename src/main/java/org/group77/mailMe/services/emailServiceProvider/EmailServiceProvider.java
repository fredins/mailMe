package org.group77.mailMe.services.emailServiceProvider;

import org.group77.mailMe.model.data.*;

import javax.mail.*;
import java.util.*;


/**
 * Class is a superclass for other provider classes to extend.
 * It is responsible for testing connection to the server, connecting and gathering information from the server.
 * @author Martin Fredin.
 * @author David Zamanian.
 * @author Alexey Ryabov
 */

public abstract class EmailServiceProvider {

  String hostIn;
  String hostOut;
  String protocolIn;
  String protocolOut;
  int portIn;
  int portOut;

  /** @author - David Zamanian, Martin Fredin.
   * @param hostIn - host address for incoming connection.
   * @param hostOut - host address for outgoing connection.
   * @param protocolIn - protocol used for incoming connection.
   * @param protocolOut - protocol used for outgoing connection.
   * @param portIn - port address for incoming connection.
   * @param portOut - port address for outgoing connection.
   */
  public EmailServiceProvider(String hostIn, String hostOut, String protocolIn, String protocolOut, int portIn, int portOut) {
    this.hostIn = hostIn;
    this.hostOut = hostOut;
    this.protocolIn = protocolIn;
    this.protocolOut = protocolOut;
    this.portIn = portIn;
    this.portOut = portOut;
  }

  /**
   * @param account is an account.
   * @return List<Folder> is a list of folders.
   * @author Martin Fredin.
   */
  public List<Email> refreshFromServer(Account account) throws MessagingException {
    return parse(connectStore(account));
  }

  /**
   * @param account is an account.
   * @return boolean if the connection was successful.
   * @author Martin
   */
  public boolean testConnection(Account account) throws MessagingException {
    connectStore(account);
    return true;
  }

  /**
   * @param account is an account.
   * @return Store is a list of folders
   * @author Martin Fredin.
   */
  private Store connectStore(Account account) throws MessagingException {
    Properties props = new Properties();
    props.setProperty("mail.pop3.ssl.enable", "true");

    Session session = Session.getDefaultInstance((props), null);
    Store store = session.getStore(protocolIn);

    String address = account.emailAddress();
    String password = String.valueOf(account.password());

    store.connect(
      hostIn,
      portIn,
      address,
      password
    );
    return store;
  }

  /**
   * @param from      - active account
   * @param recipient - list of email addresses.
   * @param subject   - subject text field.
   * @param content   - content text field.
   * @return - boolean if email is sent successful.
   * @author Alexey Ryabov
   */
  public abstract boolean sendEmail(Account from, List<String> recipient, String subject, String content, List<String> attachments) throws Exception;

  /** @author Martin Fredin.
   * @param store - is a list of folders.
   * @return - list of emails.
   * @throws MessagingException
   */
  protected abstract List<Email> parse(Store store) throws MessagingException;
}