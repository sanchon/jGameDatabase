package xyz.sanchon.jgamedatabase.dto;

/**
 * Statistics for the wishlist, used by the Homepage (gethomepage) widget API.
 */
public record WishlistStats(long wishlistTotal, long wishlistWithTarget, long wishlistBelowTarget) {
}
