package net.zargor.afterlife.permissionssystem;

/**
 * Created by Zargor on 07.07.2017.
 */
public enum GroupPermissions {
	DEFAULT,
	//Username, Password, informations
	EDIT_USERS,
	EDIT_TOURNEYS,
	//Ignore age limit etc.
	JOIN_EVERY_TOURNEY,
	BAN_USERS,
	//Set a user as paid
	SET_USER_PAID,
	//Increase/decrease a users money
	EDIT_USER_MONEY,
	//See current open offers in the Foodcenter
	SEE_OFFERS_FOODCENTER,
	//Add a offer in the Foodecenter
	ADD_OFFER_FOODCENTER


}