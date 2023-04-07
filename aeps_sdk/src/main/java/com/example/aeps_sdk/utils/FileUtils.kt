package com.example.aeps_sdk.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.aeps_sdk.R
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object FileUtils {
    private val extensions = arrayOf(
        "avi", "3gp", "mp4", "mp3", "jpeg", "jpg",
        "gif", "png",
        "pdf", "docx", "doc", "xls", "xlsx", "csv", "ppt", "pptx",
        "txt", "zip", "rar"
    )

    fun openPdfFile(context: Context, url: File?) {
        val pdfViewIntent = Intent(Intent.ACTION_VIEW)
        pdfViewIntent.setDataAndType(Uri.fromFile(url), "application/pdf")
        pdfViewIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        val intent = Intent.createChooser(pdfViewIntent, "Open File")
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Instruct the user to install a PDF reader here, or something
        }
    }

    @Throws(ActivityNotFoundException::class, IOException::class)
    fun openFile(context: Context, url: File) {
        // Create URI
        //Uri uri = Uri.fromFile(url);

        //TODO you want to use this method then create file provider in androidmanifest.xml with fileprovider name
        val uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".fileprovider",
            url
        )
        val urlString = url.toString().lowercase(Locale.getDefault())
        val intent = Intent(Intent.ACTION_VIEW)

        /**
         * Security
         */
        val resInfoList =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo: ResolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (urlString.lowercase(Locale.getDefault()).lowercase(Locale.getDefault()).contains(".doc")
            || urlString.lowercase(Locale.getDefault()).contains(".docx")
        ) {
            // Word document
            intent.setDataAndType(uri, "application/msword")
        } else if (urlString.lowercase(Locale.getDefault()).contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf")
        } else if (urlString.lowercase(Locale.getDefault()).contains(".ppt")
            || urlString.lowercase(Locale.getDefault()).contains(".pptx")
        ) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        } else if (urlString.lowercase(Locale.getDefault()).contains(".xls")
            || urlString.lowercase(Locale.getDefault()).contains(".xlsx")
        ) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel")
        } else if (urlString.lowercase(Locale.getDefault()).contains(".zip")
            || urlString.lowercase(Locale.getDefault()).contains(".rar")
        ) {
            // ZIP file
            intent.setDataAndType(uri, "application/trap")
        } else if (urlString.lowercase(Locale.getDefault()).contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf")
        } else if (urlString.lowercase(Locale.getDefault()).contains(".wav")
            || urlString.lowercase(Locale.getDefault()).contains(".mp3")
        ) {
            // WAV/MP3 audio file
            intent.setDataAndType(uri, "audio/*")
        } else if (urlString.lowercase(Locale.getDefault()).contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif")
        } else if (urlString.lowercase(Locale.getDefault()).contains(".jpg")
            || urlString.lowercase(Locale.getDefault()).contains(".jpeg")
            || urlString.lowercase(Locale.getDefault()).contains(".png")
        ) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg")
        } else if (urlString.lowercase(Locale.getDefault()).contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain")
        } else if (urlString.lowercase(Locale.getDefault()).contains(".3gp")
            || urlString.lowercase(Locale.getDefault()).contains(".mpg")
            || urlString.lowercase(Locale.getDefault()).contains(".mpeg")
            || urlString.lowercase(Locale.getDefault()).contains(".mpe")
            || urlString.lowercase(Locale.getDefault()).contains(".mp4")
            || urlString.lowercase(Locale.getDefault()).contains(".avi")
        ) {
            // Video files
            intent.setDataAndType(uri, "video/*")
        } else {
            // if you want you can also define the intent type for any other file

            // additionally use else clause below, to manage other unknown extensions
            // in this case, Android will show all applications installed on the device
            // so you can choose which application to use
            intent.setDataAndType(uri, "*/*")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Get Path of App which contains Files
     *
     * @return path of root dir
     */
    fun getAppPath(context: Context): String {
        val dir = File(
            Environment.getExternalStorageDirectory().absolutePath
                    + File.separator
                    + context.resources.getString(R.string.pdf_files)
                    + File.separator
        )
        if (!dir.exists()) {
            dir.mkdir()
        }
        return dir.absolutePath + File.separator
    }

    /**
     * for  api -11
     * @param FolderName
     * @return
     */
    fun commonDocumentDirPath(FolderName: String): String {
        var dir: File? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + "/" + FolderName
            )
        } else {
            dir = File(Environment.getExternalStorageDirectory().toString() + "/" + FolderName)
        }
        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            val success = dir.mkdirs()
            if (!success) {
                dir = null
            }
        }
        return dir!!.absolutePath
    }

    /***
     * Copy File
     *
     * @param src
     * @param dst
     * @throws IOException
     */
    fun copy(src: File, dst: File) {
        val `in`: InputStream
        val out: OutputStream
        try {
            `in` = FileInputStream(src)
            out = FileOutputStream(dst)
            val tempExt = getExtension(dst.path)
            if (((tempExt == "jpeg") || (tempExt == "jpg") || (tempExt == "gif") || (tempExt == "png"))) {
                if (out != null) {
                    var bit = BitmapFactory.decodeFile(src.path)
                    Log.v("Bitmap : ", "" + bit)
                    if (bit.width > 700) {
                        if (bit.height > 700) bit =
                            Bitmap.createScaledBitmap(bit, 700, 700, true) else bit =
                            Bitmap.createScaledBitmap(bit, 700, bit.height, true)
                    } else {
                        if (bit.height > 700) bit =
                            Bitmap.createScaledBitmap(bit, bit.width, 700, true) else bit =
                            Bitmap.createScaledBitmap(bit, bit.width, bit.height, true)
                    }
                    bit.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                Log.d("FILES", "File Compressed...")
            } else {

                // Transfer bytes from in to out
                val buf = ByteArray(1024 * 4)
                var len: Int
                while ((`in`.read(buf).also { len = it }) > 0) {
                    out.write(buf, 0, len)
                }
            }
            `in`.close()
            out.close()
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            Log.v("ERROR", "Compressing ERror :  " + e.localizedMessage)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            Log.v("ERROR", "Compressing ERror IOE : " + e.localizedMessage)
        } catch (e: Exception) {
            // TODO: handle exception
            Log.v("ERROR", "Compressing ERror Other: " + e.localizedMessage)
        }
    }

    /***
     * Move File
     *
     * @param src
     * @param dst
     * @throws IOException
     */
    fun move(src: File, dst: File) {
        val `in`: InputStream
        val out: OutputStream
        try {
            `in` = FileInputStream(src)
            out = FileOutputStream(dst)
            val tempExt = getExtension(dst.path)
            if (((tempExt == "jpeg") || (tempExt == "jpg") || (tempExt == "gif") || (tempExt == "png"))) {
                if (out != null) {
                    var bit = BitmapFactory.decodeFile(src.path)
                    Log.v("Bitmap : ", "" + bit)
                    if (bit.width > 700 || bit.height > 700) {
                        bit = Bitmap.createScaledBitmap(bit, 700, 700, true)
                    }
                    bit.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                Log.v("File Compressed...", "")
            } else {

                // Transfer bytes from in to out
                val buf = ByteArray(1024 * 4)
                var len: Int
                while ((`in`.read(buf).also { len = it }) > 0) {
                    out.write(buf, 0, len)
                }
            }
            `in`.close()
            out.close()
            /**
             * Delete File from Source folder...
             */
            if (src.delete()) Log.v("ERROR", "File Successfully Copied...")
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            Log.v("ERROR", "Compressing ERror :  " + e.localizedMessage)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            Log.v("ERROR", "Compressing ERror IOE : " + e.localizedMessage)
        } catch (e: Exception) {
            // TODO: handle exception
            Log.v("ERROR", "Compressing ERror Other: " + e.localizedMessage)
        }
    }

    /**
     * Is Valid Extension
     *
     * @param ext
     * @return
     */
    fun isValidExtension(ext: String): Boolean {
        return Arrays.asList(*extensions).contains(ext)
    }

    /**
     * Return Extension of given path without dot(.)
     *
     * @param path
     * @return
     */
    fun getExtension(path: String): String {
        return if (path.contains(".")) path.substring(path.lastIndexOf(".") + 1)
            .lowercase(Locale.getDefault()) else ""
    }
}
