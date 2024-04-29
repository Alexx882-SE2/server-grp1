package WebsocketServer.services.user;

import WebsocketServer.game.model.GameBoard;
import WebsocketServer.services.json.ActionValues;
import WebsocketServer.services.json.GenerateJSONObjectService;
import WebsocketServer.websocket.handler.WebSocketHandlerImpl;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.socket.WebSocketSession;


/**
 * Class creates user object to handle users logged on the server.
 */
public class CreateUserService {

    private final Logger logger = LogManager.getLogger(String.valueOf(CreateUserService.class));

    @Getter
    private String sessionID;
    @Getter
    private String username;
    @Getter
    private WebSocketSession session;
    @Getter
    private GameBoard gameBoard;

    public CreateUserService(WebSocketSession session, String username){
        registerUser(session, username);
    }

    /**
     * Checks if a user with passed username or sessionID already exists.
     * @param username Username to check.
     * @param sessionID SessionID to check.
     * @return boolean according to if user exists or not.
     */
    public boolean checkUserExists(String sessionID, String username){
        if(UserListService.userList.getUserBySessionID(sessionID) != null){
            if(UserListService.userList.getUserByUsername(username) != null) {
                if(UserListService.userList.getUserBySessionID(sessionID).equals(sessionID)){
                    if (UserListService.userList.getUserByUsername(username).equals(username)) return false;
                }
            }
        } else return true;
        return false;
    }

    /**
     * Sets global variables for sessionID, username, session and more tbd. Defines response for client.
     * @param session Session to be set.
     * @param username Username to be set.
     */
    @SneakyThrows
    public void registerUser(WebSocketSession session, String username) {
        if (!(username.isEmpty()) && (session != null)) {
            String sessionID = session.getId();
            if(checkUserExists(sessionID, username)){
                this.sessionID = sessionID;
                this.username = username;
                this.session = session;
                WebSocketHandlerImpl.responseMessage = GenerateJSONObjectService.generateJSONObject(
                        ActionValues.REGISTERUSER.getValue(), username, true,
                        "", "");
                logger.info("SessionID {} and Username {} set. User created.", sessionID, username);
            }
            else{
                WebSocketHandlerImpl.responseMessage = GenerateJSONObjectService.generateJSONObject(
                        ActionValues.REGISTERUSER.getValue(), "null", false,
                        "Username already exists. Please choose another one.", "");
                logger.warn("Username {} already exists. User not created.", username);
            }
        } else{
            WebSocketHandlerImpl.responseMessage = GenerateJSONObjectService.generateJSONObject(
                    ActionValues.REGISTERUSER.getValue(), "null", false,
                    "No username has been passed. User not created.", "");
            logger.warn("No username has been passed. User not created.");
        }
    }
}