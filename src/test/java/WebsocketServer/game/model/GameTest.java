package WebsocketServer.game.model;

import WebsocketServer.game.enums.ChoosenCardCombination;
import WebsocketServer.game.enums.FieldValue;
import WebsocketServer.game.enums.GameState;
import WebsocketServer.game.exceptions.GameStateException;
import WebsocketServer.game.services.GameBoardService;
import WebsocketServer.services.GameService;
import WebsocketServer.services.user.CreateUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;
import static java.util.concurrent.CompletableFuture.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
class GameTest {

    @Autowired
    Game game;

    @Mock
    CreateUserService player1;
    @Mock
    CreateUserService player2;

    @Autowired
    GameBoardService gameBoardService;

    @BeforeEach
    public void setUp() {
        // Clearing players list and adding mock players
        game.getPlayers().clear();
        game.addPlayer(player1);
        when(player1.getUsername()).thenReturn("Player1");
        when(player2.getUsername()).thenReturn("Player2");

        doNothing().when(player1).createGameBoard();
        doNothing().when(player2).createGameBoard();

        when(player1.getGameBoard()).thenReturn(gameBoardService.createGameBoard());
        when(player2.getGameBoard()).thenReturn(gameBoardService.createGameBoard());
    }

    @Test
    void testGetPlayerList() {
        assertThat(game.getPlayers()).hasSize(1);
    }

    @Test
    void testStartGameSuccess() throws InterruptedException {
        game.addPlayer(player2);
        game.startGame();

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            game.receiveSelectedCombinationOfPlayer(player1, ChoosenCardCombination.ONE);
            game.receiveSelectedCombinationOfPlayer(player2, ChoosenCardCombination.TWO);
        });
        future.join();

        Thread.sleep(100);

        future = CompletableFuture.runAsync(() -> {
            game.receiveValueAtPositionOfPlayer(player1, 1, 1, FieldValue.ONE);
            game.receiveValueAtPositionOfPlayer(player2, 1, 1, FieldValue.TWO);
        });
        future.join();

        Thread.sleep(100);

        assertEquals(GameState.FINISHED, game.getGameState());
    }

    @Test
    void testWrongStateForRound() throws InterruptedException {
        assertThrows(GameStateException.class, () -> game.receiveSelectedCombinationOfPlayer(player1, ChoosenCardCombination.ONE));
    }
}