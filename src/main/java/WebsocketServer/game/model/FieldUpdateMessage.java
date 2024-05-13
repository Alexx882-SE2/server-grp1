package WebsocketServer.game.model;

import WebsocketServer.game.enums.FieldValue;

/**
 * This is a Skeleton for easy receiving Messages from Client in a proper Format
 */
public record FieldUpdateMessage(int floor, int chamber, int field, FieldValue fieldValue, String userOwner) {
}
