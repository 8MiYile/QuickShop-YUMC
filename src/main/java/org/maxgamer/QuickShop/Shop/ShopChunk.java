package org.maxgamer.QuickShop.Shop;

public class ShopChunk {
	private final String world;
	private final int x;
	private final int z;
	private int hash = 0;

	public ShopChunk(final String world, final int x, final int z) {
		this.world = world;
		this.x = x;
		this.z = z;
		this.hash = this.x * this.z; // We don't need to use the world's hash,
										// as these are seperated by world in
										// memory
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		final ShopChunk shopChunk = (ShopChunk) obj;
		return (this.getWorld().equals(shopChunk.getWorld()) && this.getX() == shopChunk.getX() && this.getZ() == shopChunk.getZ());
	}

	public String getWorld() {
		return this.world;
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}

	@Override
	public int hashCode() {
		return hash;
	}
}
