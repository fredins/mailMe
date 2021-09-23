package org.group77.mejl;
import org.group77.mejl.model.*;
import org.junit.jupiter.api.*;

public class TestAccountFactory {

    @Test
    void testCreateAccount(){
        AccountFactory factory = new AccountFactory();
        Account a = factory.createAccount("grupp77@gmail.com", "password123");
        Account a1 = factory.createAccount("gmail@outlook.com", "password123");
        Assertions.assertNotEquals(a, a1);
    }
}