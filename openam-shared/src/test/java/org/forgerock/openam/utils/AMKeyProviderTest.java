/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2013-2016 ForgeRock AS.
 */

package org.forgerock.openam.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.crypto.SecretKey;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.iplanet.services.util.Crypt;
import com.iplanet.services.util.JCEEncryption;
import com.sun.identity.security.EncodeAction;
import com.sun.identity.shared.encode.URLEncDec;

public class AMKeyProviderTest {

    private static final String KEY_STORE_FILE = URLEncDec.decode(ClassLoader.getSystemResource("keystore.jceks")
            .getFile());
    private static final String KEY_STORE_TYPE = "JCEKS";
    private static final String KEY_STORE_PASS = "testcase";
    private static final String DEFAULT_PRIVATE_KEY_PASS = "testcase";
    private static final String PRIVATE_KEY_PASS = "keypass";
    private static final String DEFAULT_PRIVATE_KEY_ALIAS = "defaultkey";
    private static final String PRIVATE_KEY_ALIAS = "privatekey";
    private static final String SECRET_KEY_ALIAS = "secretkey";

    //KeyProvider amKeyProvider;
    AMKeyProvider amKeyProvider; // need more specific type for the additional password store methods

    @BeforeClass
    public void setUp() {

        amKeyProvider =
                new AMKeyProvider(true, KEY_STORE_FILE, KEY_STORE_PASS, KEY_STORE_TYPE, DEFAULT_PRIVATE_KEY_PASS);
    }

    @Test
    public void getDefaultPublicKey() {

        PublicKey key = amKeyProvider.getPublicKey(DEFAULT_PRIVATE_KEY_ALIAS);
        Assert.assertNotNull(key);
    }

    @Test
    public void getDefaultX509Certificate() {

        X509Certificate certificate = amKeyProvider.getX509Certificate(DEFAULT_PRIVATE_KEY_ALIAS);
        Assert.assertNotNull(certificate);
    }

    @Test
    public void getPublicKey() {

        PublicKey key = amKeyProvider.getPublicKey(PRIVATE_KEY_ALIAS);
        Assert.assertNotNull(key);
    }

    @Test
    public void getX509Certificate() {

        X509Certificate certificate = amKeyProvider.getX509Certificate(PRIVATE_KEY_ALIAS);
        Assert.assertNotNull(certificate);
    }

    @Test
    public void getDefaultPrivateKeyUsingDefaultPassword() {

        PrivateKey key = amKeyProvider.getPrivateKey(DEFAULT_PRIVATE_KEY_ALIAS);
        Assert.assertNotNull(key);
    }

    @Test
    public void getPrivateKeyUsingProvidedPassword() {

        String encodedPrivatePass = AccessController.doPrivileged(new EncodeAction(PRIVATE_KEY_PASS));

        PrivateKey key = amKeyProvider.getPrivateKey(PRIVATE_KEY_ALIAS, encodedPrivatePass);
        Assert.assertNotNull(key);
    }

    @Test
    public void getPrivateKeyUsingNullPassword() {

        // Trying to get a private key with its own password and passing null should return null
        PrivateKey key = amKeyProvider.getPrivateKey(PRIVATE_KEY_ALIAS, null);
        Assert.assertNull(key);
    }

    @Test
    public void getPrivateKeyUsingDefaultPassword() {

        //Trying to get a private key with its own password will make use of the default password and should return null
        PrivateKey key = amKeyProvider.getPrivateKey(PRIVATE_KEY_ALIAS);
        Assert.assertNull(key);
    }

    @Test
    public void getSecretKeyUsingDefaultPassword() {
        SecretKey key = amKeyProvider.getSecretKey(SECRET_KEY_ALIAS);
        Assert.assertNotNull(key);
    }

    @Test
    public void storeAndRetrievePassword() throws KeyStoreException {
        String password = "the rain in spain!!";
        amKeyProvider.setSecretKeyEntry("admin", password);
        String p = amKeyProvider.getSecret("admin");
        Assert.assertEquals(password, p);
    }

    @Test
    public void readPasswordFileDecryptsThePassword() throws IOException {
        Path tempFile = Files.createTempFile("keyprovidertest", ".tmp");
        String plainText = "MySuperSecret Password!";
        String pw = Crypt.encode(plainText); // encode using Crypt util
        Assert.assertTrue(JCEEncryption.isAMPassword(pw));
        // Write an AMEncrytped password into this file
        Files.write(tempFile, pw.getBytes());
        // read it back
        String result = amKeyProvider.readPasswordFile(tempFile.toString());
        Assert.assertEquals(result, plainText);  // we should get the plain text back
        // now write a plain vanilla password to the file
        Files.write(tempFile, plainText.getBytes());
        result = amKeyProvider.readPasswordFile(tempFile.toString());
        Assert.assertEquals(result, plainText);
        Files.delete(tempFile); // clean up after test
    }

}
