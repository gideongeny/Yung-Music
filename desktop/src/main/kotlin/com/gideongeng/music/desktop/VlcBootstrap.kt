package com.gideongeng.music.desktop

import com.sun.jna.NativeLibrary
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import java.io.File

object VlcBootstrap {
    var vlcHome: File? = null
        private set

    fun discover(): File? {
        val home = findVlcHome()
        vlcHome = home
        if (home != null) {
            applySearchPath(home)
            println("YungMusic: Using VLC at ${home.absolutePath}")
        } else {
            println("YungMusic: No bundled or system VLC found; FFmpeg engine will be used")
        }
        val discovered = try {
            NativeDiscovery().discover()
        } catch (e: Exception) {
            println("YungMusic: NativeDiscovery: ${e.message}")
            false
        }
        println("YungMusic: libvlc discovery=${discovered}, home=${home?.absolutePath}")
        return home
    }

    fun pluginPath(): String? {
        val plugins = vlcHome?.let { File(it, "plugins") }
        return plugins?.takeIf { it.isDirectory }?.absolutePath
    }

    private fun findVlcHome(): File? {
        val candidates = mutableListOf<File>()
        System.getProperty("compose.application.resources.dir")?.let { root ->
            candidates += File(root, "vlc")
            candidates += File(root)
        }
        System.getProperty("yung.vlc.dir")?.let { candidates += File(it) }

        val userDir = File(System.getProperty("user.dir") ?: ".")
        candidates += File(userDir, "appResources/windows/vlc")
        candidates += File(userDir, "vlc")

        runCatching {
            val loc = VlcBootstrap::class.java.protectionDomain.codeSource?.location
            if (loc != null && loc.protocol == "file") {
                var dir = File(loc.toURI()).parentFile
                repeat(6) {
                    if (dir == null) return@repeat
                    candidates += File(dir, "resources/vlc")
                    candidates += File(dir, "app/resources/vlc")
                    candidates += File(dir, "vlc")
                    dir = dir.parentFile
                }
            }
        }

        candidates += File("C:\\Program Files\\VideoLAN\\VLC")
        candidates += File("C:\\Program Files (x86)\\VideoLAN\\VLC")
        candidates += File("/Applications/VLC.app/Contents/MacOS/lib")
        candidates += File("/usr/lib/x86_64-linux-gnu")
        candidates += File("/usr/lib")

        return candidates.distinctBy { it.absolutePath }.firstOrNull { isVlcHome(it) }
    }

    private fun isVlcHome(dir: File): Boolean {
        if (!dir.isDirectory) return false
        return File(dir, "libvlc.dll").exists() ||
            File(dir, "libvlc.so").exists() ||
            File(dir, "libvlc.so.5").exists() ||
            File(dir, "libvlc.dylib").exists() ||
            File(dir, "lib/libvlc.dylib").exists()
    }

    private fun applySearchPath(dir: File) {
        val libDir = when {
            File(dir, "libvlc.dll").exists() -> dir
            File(dir, "lib/libvlc.dylib").exists() -> File(dir, "lib")
            else -> dir
        }
        val libName = RuntimeUtil.getLibVlcLibraryName()
        NativeLibrary.addSearchPath(libName, libDir.absolutePath)
        NativeLibrary.addSearchPath("libvlccore", libDir.absolutePath)
        NativeLibrary.addSearchPath("vlccore", libDir.absolutePath)
        System.setProperty("jna.library.path", libDir.absolutePath)
        File(dir, "plugins").takeIf { it.isDirectory }?.let {
            System.setProperty("VLC_PLUGIN_PATH", it.absolutePath)
        }
        try {
            NativeLibrary.getInstance("libvlccore")
            NativeLibrary.getInstance(libName)
        } catch (e: Throwable) {
            println("YungMusic: Preload libvlc: ${e.message}")
        }
    }
}
