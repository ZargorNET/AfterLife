package net.zargor.afterlife.web

/**
 * The requested media-type. @see https://en.wikipedia.org/wiki/Media_type
 */
enum class MimeTypes(val mimeText: String, val endings: Array<String>) {
    HTML("text/html;charset=utf-8", arrayOf(".html", ".htm", ".shtml")),
    PLAIN("text/plain;charset=utf-8", arrayOf(".txt")),
    CSS("text/css;charset=utf-8", arrayOf(".css")),
    JAVASCRIPT("text/javascript;charset=utf-8", arrayOf(".js")),
    XML("text/xml;charset=utf-8", arrayOf(".xml")),
    PNG("image/png", arrayOf(".png")),
    JPEG("image/jpeg", arrayOf(".jpg", ".jpeg", ".jpe")),
    GIF("image/gif", arrayOf(".gif")),
    ICO("image/x-icon", arrayOf(".ico")),
    WAV("audio/x-wav", arrayOf(".wav")),
    MP3("audio/mpeg", arrayOf(".mp3")),
    MP4("video/mp4", arrayOf(".mp4")),
    AVI("video/x-msvideo", arrayOf(".avi")),
    WOFF("application/x-font-woff", arrayOf(".woff")),
    TFF("application/x-font-ttf", arrayOf(".ttf"))
}