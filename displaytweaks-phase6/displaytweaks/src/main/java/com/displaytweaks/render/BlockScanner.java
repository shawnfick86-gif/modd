package com.displaytweaks.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Finds blocks in loaded chunks around the player. Skips chunk sections that can't contain a match. */
public final class BlockScanner {
	private BlockScanner() {}

	public record Hit(BlockPos pos, BlockState state) {}

	public static boolean inRange(ClientPlayerEntity p, BlockPos pos, int range) {
		double dx = pos.getX() + 0.5 - p.getX();
		double dy = pos.getY() + 0.5 - p.getY();
		double dz = pos.getZ() + 0.5 - p.getZ();
		return dx * dx + dy * dy + dz * dz <= (double) range * range;
	}

	public static List<WorldChunk> chunks(MinecraftClient mc, int range) {
		List<WorldChunk> out = new ArrayList<>();
		ClientPlayerEntity p = mc.player;
		ClientWorld w = mc.world;
		if (p == null || w == null) return out;
		ChunkPos pc = p.getChunkPos();
		int r = (range >> 4) + 1;
		for (int dx = -r; dx <= r; dx++) {
			for (int dz = -r; dz <= r; dz++) {
				WorldChunk c = w.getChunkManager().getWorldChunk(pc.x + dx, pc.z + dz);
				if (c != null) out.add(c);
			}
		}
		return out;
	}

	public static List<Hit> scan(MinecraftClient mc, int range, Predicate<BlockState> pred, int cap) {
		List<Hit> out = new ArrayList<>();
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.world == null) return out;

		for (WorldChunk c : chunks(mc, range)) {
			ChunkSection[] sections = c.getSectionArray();
			int bottom = c.getBottomSectionCoord();
			int sx = c.getPos().getStartX();
			int sz = c.getPos().getStartZ();
			for (int i = 0; i < sections.length; i++) {
				ChunkSection s = sections[i];
				if (s == null || s.isEmpty() || !s.hasAny(pred)) continue;
				int by = (bottom + i) << 4;
				for (int x = 0; x < 16; x++) {
					for (int y = 0; y < 16; y++) {
						for (int z = 0; z < 16; z++) {
							BlockState st = s.getBlockState(x, y, z);
							if (!pred.test(st)) continue;
							BlockPos pos = new BlockPos(sx + x, by + y, sz + z);
							if (!inRange(p, pos, range)) continue;
							out.add(new Hit(pos, st));
							if (out.size() >= cap) return out;
						}
					}
				}
			}
		}
		return out;
	}
}
