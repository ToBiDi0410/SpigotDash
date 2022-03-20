package de.tobias.spigotdash;

import de.tobias.spigotdash.backend.dataCollectors.DataCollectionManager;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import de.tobias.spigotdash.backend.storage.CacheStore;
import de.tobias.spigotdash.backend.storage.JavaObjectJsonStore;
import de.tobias.spigotdash.backend.storage.UserStore;
import de.tobias.spigotdash.backend.utils.AESCryptor;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import de.tobias.spigotdash.backend.utils.RSACryptor;
import de.tobias.spigotdash.models.AESWithRSAPair;
import org.bukkit.plugin.java.JavaPlugin;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Date;

public class main extends JavaPlugin {

	public void onEnable() {
		GlobalVariableStore.PLUGIN_STARTUP_TIMESTAMP = System.currentTimeMillis();
		try {
			GlobalVariableStore.pl = this;
			new globalLogger(null);
			globalLogger.constructed.activateDevDebug(); //TODO REMOVE FOR PRODUCTION
			globalLogger.constructed.getDebugFields().add("!HTTPSRV");
			globalLogger.constructed.getDebugFields().add("!SOCREQ1H");
			fieldLogger thisLogger = new fieldLogger("INIT", globalLogger.constructed);

			thisLogger.INFO("Starting up Plugin...", 0);

			thisLogger.WARNING("It seems you have enabled the Debug Log!", 1);
			thisLogger.WARNING("Please never share information from here to someone unknown!", 1);

			GlobalVariableStore.userJSONStore = new JavaObjectJsonStore(UserStore.class, new File(this.getDataFolder(), "test.json"));
			GlobalVariableStore.userJSONStore.loadOrCreate();

			GlobalVariableStore.cacheJSONStore = new JavaObjectJsonStore(CacheStore.class, new File(this.getDataFolder(), "cache.json"));
			GlobalVariableStore.cacheJSONStore.loadOrCreate();

			GlobalVariableStore.serverManager.start();

			//WebsocketRequestV1Handler.subHandlers.put("ENCRYPTION", EncryptionRequestHandler.handler);
			//WebsocketRequestV1Handler.subHandlers.put("AUTHENTICATION", AuthenticationRequestHandler.handler);
			//WebsocketRequestV1Handler.subHandlers.put("COLLECTOR_DATA", DataCollectionRequestHandler.handler);
			//WebsocketRequestV1Handler.subHandlers.put("CACHE_DATA", CacheHistoryRequestHandler.handler);

			DataCollectionManager.initAllCollectors();
			DataCollectionManager.initCacheTask();

			test();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onDisable() {
		GlobalVariableStore.serverManager.stop();
		GlobalVariableStore.userJSONStore.save();

		GlobalVariableStore.getCacheStore().LAST_SAVED = new Date();
		GlobalVariableStore.cacheJSONStore.save();
	}

	public void test() {
		try {
			System.out.println("\n--- RSA Test ---");
			KeyPair rsaPair = RSACryptor.generateSet();

			byte[] testRSAEncrypt = RSACryptor.encryptBytes("TestRSA123".getBytes(StandardCharsets.UTF_8), RSACryptor.publicKeyFromBytes(rsaPair.getPublic().getEncoded()));
			byte[] testRSADecrypt = RSACryptor.decryptBytes(testRSAEncrypt, RSACryptor.privateKeyFromBytes(rsaPair.getPrivate().getEncoded()));
			System.out.println("Out: " + new String(testRSADecrypt));


			System.out.println("\n--- AES Test ---");
			SecretKey aesKey = AESCryptor.generateKey();

			System.out.println("KeySimilarity: " + new String(aesKey.getEncoded()) + "   <-->   " + new String(AESCryptor.keyFromBytes(aesKey.getEncoded()).getEncoded()));

			byte[] testAESEncrypt = AESCryptor.encryptBytes("TestAES123".getBytes(StandardCharsets.UTF_8), AESCryptor.keyFromBytes(aesKey.getEncoded()));
			byte[] testAESDecrypt = AESCryptor.decryptBytes(testAESEncrypt, AESCryptor.keyFromBytes(aesKey.getEncoded()));
			System.out.println("Out: " + new String(testAESDecrypt));


			System.out.println("\n--- AESWithRSA Test ---");
			AESWithRSAPair testEncrypt = new AESWithRSAPair("TESTSTRING".getBytes(StandardCharsets.UTF_8), rsaPair.getPublic().getEncoded());
			testEncrypt.doOperation();

			AESWithRSAPair testDecrypt = new AESWithRSAPair(testEncrypt.getEncryptedData(), testEncrypt.getEncryptedAESKey(), rsaPair.getPrivate().getEncoded());
			testDecrypt.doOperation();
			System.out.println("KeySimilarity: " + new String(testEncrypt.getDecryptedAESKey()) + "   <-->   " + new String(AESCryptor.keyFromBytes(testDecrypt.getDecryptedAESKey()).getEncoded()));
			System.out.println("Out: " + new String(testEncrypt.getDecryptedData()));

		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
