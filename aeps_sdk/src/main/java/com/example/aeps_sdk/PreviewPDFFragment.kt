package com.example.aeps_sdk

import android.annotation.SuppressLint
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aeps_sdk.databinding.FragmentPreviewPDFBinding
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File

class PreviewPDFFragment : Fragment(), OnPageChangeListener, OnLoadCompleteListener,
    OnPageErrorListener {
    private var _binding: FragmentPreviewPDFBinding? = null
    private val binding get() = _binding!!
    private var pdfFileName = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreviewPDFBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pdfFileName = requireArguments().getString("filePath")!!
        val fileUri = Uri.fromFile(File(pdfFileName))
        displayFromUri(fileUri)
    }

    private fun displayFromUri(uri: Uri) {
        pdfFileName = getFileName(uri)
        binding.pdfView.fromUri(uri)
            .defaultPage(1)
            .onPageChange(this)
            .enableAnnotationRendering(true)
            .onLoad(this)
            .scrollHandle(DefaultScrollHandle(requireActivity()))
            .spacing(10) // in dp
            .onPageError(this)
            .load()
    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor =
                requireActivity().contentResolver.query(uri, null, null, null, null)!!
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
        }
        if (result == null) {
            result = uri.lastPathSegment
        }
        return result!!
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
    }

    override fun loadComplete(nbPages: Int) {
    }

    override fun onPageError(page: Int, t: Throwable?) {
    }

}