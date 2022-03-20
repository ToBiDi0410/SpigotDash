package de.tobias.spigotdash.models;

import de.tobias.spigotdash.backend.utils.AESCryptor;
import de.tobias.spigotdash.backend.utils.RSACryptor;

import javax.crypto.SecretKey;

public class AESWithRSAPair {

    private byte[] encryptedByteData;
    private byte[] decryptedByteData;

    private byte[] encryptedAESKey;
    private byte[] decryptedAESKey;

    private byte[] RSAPublicKey;
    private byte[] RSAPrivateKey;

    private boolean MODE_ENCRYPT = false;

    public AESWithRSAPair(byte[] data, byte[] RSAPublicKey) {
        this.decryptedByteData = data;
        this.RSAPublicKey = RSAPublicKey;
        this.MODE_ENCRYPT = true;
    }

    public AESWithRSAPair(byte[] encryptedData, byte[] encryptedAESKey, byte[] RSAPrivateKey) {
        this.encryptedByteData = encryptedData;
        this.RSAPrivateKey = RSAPrivateKey;
        this.encryptedAESKey = encryptedAESKey;
        this.MODE_ENCRYPT = false;
    }

    public boolean doOperation() throws Exception {
        if(this.MODE_ENCRYPT) {
            System.out.println("Mode is encrypt");
            if(this.RSAPublicKey == null || this.RSAPublicKey.length == 0) throw new Exception("Illegal Configuration: Should encrypt but there is no RSA Public Key");

            this.generateAESKey(); //Generate AES Key
            this.encryptedByteData = AESCryptor.encryptBytes(this.decryptedByteData, AESCryptor.keyFromBytes(this.decryptedAESKey)); //Encrypt Bytes with AES Key
            this.encryptedAESKey = RSACryptor.encryptBytes(this.decryptedAESKey, RSACryptor.publicKeyFromBytes(this.RSAPublicKey)); //Encrypt the AES Key that was used to encrypt the original Data
            System.out.println("Encryption done!");
        } else {
            System.out.println("Mode is decrypt");
            if(this.RSAPrivateKey == null || this.RSAPrivateKey.length == 0) throw new Exception("Illegal Configuration: Should decrypt but there is no RSA Private Key");

            this.decryptedAESKey = RSACryptor.decryptBytes(this.encryptedAESKey, RSACryptor.privateKeyFromBytes(this.RSAPrivateKey)); //Decrypt the AES Key
            this.decryptedByteData = AESCryptor.decryptBytes(this.encryptedByteData, AESCryptor.keyFromBytes(this.decryptedAESKey)); //Decrypt the Data using the before decrypted AES Key
            System.out.println("Decryption done!");
        }
        return true;
    }

    private void generateAESKey() {
        SecretKey key = AESCryptor.generateKey();
        this.decryptedAESKey = key.getEncoded();
    }

    public byte[] getEncryptedData() {
        return this.encryptedByteData;
    }

    public byte[] getDecryptedData() {
        return this.decryptedByteData;
    }

    public byte[] getEncryptedAESKey() {
        return this.encryptedAESKey;
    }

    public byte[] getDecryptedAESKey() {
        return this.decryptedAESKey;
    }
}
