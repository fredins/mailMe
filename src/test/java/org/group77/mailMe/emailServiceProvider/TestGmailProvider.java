package org.group77.mailMe.emailServiceProvider;

public class TestGmailProvider {

    /*
    @Test
    void testRefreshFromServerAllFoldersFound() throws MessagingException {
        GmailProvider provider = new GmailProvider();
        AccountFactory factory = new AccountFactory();
        Account a = factory.createAccount("77grupp@gmail.com", "grupp77group");

        List<String> folderNames = provider
            .refreshFromServer(a).stream()
            .map(Folder::toString)
            .collect(Collectors.toList());

        List<String> folderNames1 = List.of(
                "Inbox", "All Mail", "Sent Mail", "Drafts", "Spam", "Trash");

        Assertions.assertTrue(folderNames.containsAll(folderNames1));
    }

     */
    /*
    @Test
    void testRefreshFromServerEmailsFound(){
        GmailProvider provider = new GmailProvider();
        AccountFactory factory = new AccountFactory();
        Account a = factory.createAccount("77grupp@gmail.com", "grupp77group");

        boolean b;
        try{
            b = provider
                .refreshFromServer(a)
                .stream()
                .allMatch(f -> f.getEmails() != null);
        }catch (MessagingException e){
            b = false;
        }
        Assertions.assertTrue(b);
    }

    @Test
    void testConnection(){
        GmailProvider provider = new GmailProvider();
        AccountFactory factory = new AccountFactory();
        Account a = factory.createAccount("77grupp@gmail.com", "grupp77group");
        Assertions.assertDoesNotThrow(() -> provider.testConnection(a));
    }
    @Test
    void testConnection1(){
        GmailProvider provider = new GmailProvider();
        AccountFactory factory = new AccountFactory();
        Account a1 = factory.createAccount("77grupp@gmail.com", "wrong_password");
        Assertions.assertThrows(MessagingException.class, () -> provider.testConnection(a1));
    }

     */
}
