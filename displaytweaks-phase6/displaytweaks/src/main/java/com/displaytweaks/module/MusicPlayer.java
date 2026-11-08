package com.displaytweaks.module;

import com.displaytweaks.setting.ActionSetting;
import com.displaytweaks.setting.BoolSetting;
import com.displaytweaks.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.InputStream;
import java.util.List;

/**
 * Plays audio from a link on YOUR computer only. Needs yt-dlp and ffmpeg
 * installed and on your PATH. Copy a link, then use "Play URL From Clipboard".
 */
public class MusicPlayer extends Module {
	private final NumberSetting volume = add(new NumberSetting("Volume %", 50, 0, 100, 5));
	private final BoolSetting loop = add(new BoolSetting("Loop", false));

	private volatile boolean stopFlag;
	private volatile List<Process> procs = List.of();
	private volatile SourceDataLine line;
	private Thread worker;

	public MusicPlayer() {
		super("Music Player", "Play audio from a link (local only)", Category.MEDIA);
		add(new ActionSetting("> Play URL From Clipboard", this::playFromClipboard));
		add(new ActionSetting("> Stop", this::stop));
	}

	private void playFromClipboard() {
		MinecraftClient mc = MinecraftClient.getInstance();
		String url = GLFW.glfwGetClipboardString(mc.getWindow().getHandle());
		if (url == null || url.isBlank()) {
			msg("Clipboard is empty. Copy a link first.", false);
			return;
		}
		url = url.trim();
		if (!(url.startsWith("https://") || url.startsWith("http://"))) {
			msg("Clipboard doesn't contain a link.", false);
			return;
		}
		start(url);
	}

	private void msg(String text, boolean actionBar) {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.execute(() -> {
			if (mc.player != null) mc.player.sendMessage(Text.literal("[Music] " + text), actionBar);
		});
	}

	private void start(String url) {
		stop();
		stopFlag = false;
		worker = new Thread(() -> run(url), "DisplayTweaks-Music");
		worker.setDaemon(true);
		worker.start();
	}

	public void stop() {
		stopFlag = true;
		for (Process pr : procs) pr.destroyForcibly();
		SourceDataLine l = line;
		if (l != null) {
			l.stop();
			l.flush();
		}
	}

	private void run(String url) {
		try {
			do {
				ProcessBuilder yt = new ProcessBuilder("yt-dlp", "-f", "bestaudio", "--no-playlist",
						"--quiet", "-o", "-", "--", url);
				ProcessBuilder ff = new ProcessBuilder("ffmpeg", "-loglevel", "quiet", "-i", "pipe:0",
						"-f", "s16le", "-ar", "44100", "-ac", "2", "pipe:1");
				yt.redirectError(ProcessBuilder.Redirect.DISCARD);
				ff.redirectError(ProcessBuilder.Redirect.DISCARD);
				List<Process> started = ProcessBuilder.startPipeline(List.of(yt, ff));
				procs = started;
				msg("Loading audio...", true);

				AudioFormat fmt = new AudioFormat(44100f, 16, 2, true, false);
				SourceDataLine l = AudioSystem.getSourceDataLine(fmt);
				l.open(fmt);
				l.start();
				line = l;
				msg("Playing.", true);

				try (InputStream in = started.get(1).getInputStream()) {
					byte[] buf = new byte[4096];
					boolean first = true;
					while (!stopFlag) {
						int n = in.readNBytes(buf, 0, buf.length);
						if (n <= 0) break;
						first = false;
						float v = (float) Math.pow(volume.get() / 100.0, 2);
						for (int i = 0; i + 1 < n; i += 2) {
							short s = (short) ((buf[i] & 0xFF) | (buf[i + 1] << 8));
							s = (short) (s * v);
							buf[i] = (byte) s;
							buf[i + 1] = (byte) (s >> 8);
						}
						l.write(buf, 0, n - (n % 4));
					}
					if (first && !stopFlag) msg("No audio came through. Check the link and that yt-dlp is up to date.", false);
				}
				if (!stopFlag) l.drain();
				l.close();
				line = null;
				for (Process pr : started) pr.destroyForcibly();
			} while (loop.get() && !stopFlag);
		} catch (java.io.IOException e) {
			msg("Couldn't start yt-dlp/ffmpeg. Install both and make sure they're on your PATH.", false);
		} catch (Exception e) {
			msg("Audio error: " + e.getMessage(), false);
		} finally {
			SourceDataLine l = line;
			if (l != null) l.close();
			line = null;
			for (Process pr : procs) pr.destroyForcibly();
		}
	}

	@Override
	protected void onDisable() {
		stop();
	}
}
