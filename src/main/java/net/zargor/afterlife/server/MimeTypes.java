package net.zargor.afterlife.server;

import lombok.Getter;

/**
 * The requested media-type. @see https://en.wikipedia.org/wiki/Media_type
 */
public enum MimeTypes {
	HTML("text/html;charset=utf-8", new String[]{".html", ".htm", ".shtml"}),
	PLAIN("text/plain;charset=utf-8", new String[]{".txt"}),
	CSS("text/css;charset=utf-8", new String[]{".css"}),
	JAVASCRIPT("text/javascript;charset=utf-8", new String[]{".js"}),
	XML("text/xml;charset=utf-8", new String[]{".xml"}),
	PNG("image/png", new String[]{".png"}),
	JPEG("image/jpeg", new String[]{".jpg", ".jpeg", ".jpe"}),
	GIF("image/gif", new String[]{".gif"}),
	ICO("image/x-icon", new String[]{".ico"}),
	WAV("audio/x-wav", new String[]{".wav"}),
	MP3("audio/mpeg", new String[]{".mp3"}),
	MP4("video/mp4", new String[]{".mp4"}),
	AVI("video/x-msvideo", new String[]{".avi"});

	@Getter
	private String mimeText;
	@Getter
	private String[] ending;

	MimeTypes(String mimeText, String[] ending) {
		this.mimeText = mimeText;
		this.ending = ending;
	}
}