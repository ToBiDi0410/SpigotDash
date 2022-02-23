package de.tobias.spigotdash;

import de.tobias.spigotdash.backend.dataCollectors.dataCollectionManager;
import de.tobias.spigotdash.backend.dataCollectors.dataCollectionRequestHandler;
import de.tobias.spigotdash.backend.io.http.HttpServerManager;
import de.tobias.spigotdash.backend.io.WebsocketRequestHandlers.EncryptionRequestHandler;
import de.tobias.spigotdash.backend.io.socket.WebsocketRequestV1Handler;
import de.tobias.spigotdash.backend.io.socket.WebsocketServerManager;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin {

	public void onEnable() {

		try {
			GlobalVariableStore.pl = this;
			new globalLogger(null);
			globalLogger.constructed.activateDevDebug(); //TODO REMOVE FOR PRODUCTION
			globalLogger.constructed.getDebugFields().add("!HTTPSRV");
			fieldLogger thisLogger = new fieldLogger("INIT", globalLogger.constructed);

			thisLogger.INFO("Starting up Plugin...", 0);

			thisLogger.WARNING("It seems you have enabled the Debug Log!", 1);
			thisLogger.WARNING("Please never share information from here to someone unknown!", 1);

			/*JavaObjectJsonStore testFile = new JavaObjectJsonStore(TestObj.class, new File(this.getDataFolder(), "test.json"));
			testFile.loadOrCreate();
			thisLogger.WARNING(((TestObj) testFile.getObject()).name, 0);*/

			GlobalVariableStore.mainHttpServer =  new HttpServerManager(80);
			GlobalVariableStore.mainHttpServer.init();

			GlobalVariableStore.mainSocketServer = new WebsocketServerManager(81);
			GlobalVariableStore.mainSocketServer.init();

			GlobalVariableStore.mainHttpServer.start();
			GlobalVariableStore.mainSocketServer.start();

			WebsocketRequestV1Handler.subHandlers.put("ENCRYPTION", EncryptionRequestHandler.handler);
			WebsocketRequestV1Handler.subHandlers.put("COLLECTOR_DATA", dataCollectionRequestHandler.handler);

			dataCollectionManager.initAllCollectors();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onDisable() {
		GlobalVariableStore.mainHttpServer.stop();
		GlobalVariableStore.mainSocketServer.stop();
	}
}
