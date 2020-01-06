package com.bynq.wallet.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import androidx.lifecycle.ViewModelProviders
import com.bynq.wallet.R
import com.bynq.wallet.viewmodel.BarcodeViewModel
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.fragment_barcode.*
import java.io.IOException


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class BarcodeFragment : Fragment() {

    private lateinit var barcodeViewModel: BarcodeViewModel
    private val REQUEST_RUNTIME_PERMISSION_CAMERA = 400
    // Other Objects
    private var mCameraSource: CameraSource? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        barcodeViewModel =
            ViewModelProviders.of(this).get(BarcodeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_barcode, container, false)
        requestRunTimePermissionForAccessCamera()
        return root
    }

    // request runtime permissions for camera
    private fun requestRunTimePermissionForAccessCamera() = if (
        ContextCompat.checkSelfPermission(
            this@BarcodeFragment.requireActivity(),
            Manifest.permission.CAMERA
        ) !== PackageManager.PERMISSION_GRANTED
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    ) {
        ActivityCompat.requestPermissions(
            this@BarcodeFragment.requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_RUNTIME_PERMISSION_CAMERA
        )
    } else {
        setUpQRScanner()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RUNTIME_PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpQRScanner()
                } else {
                    val showRational =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this@BarcodeFragment.requireActivity(),
                            Manifest.permission.CAMERA
                        )
                    if (showRational) {
                        requestRunTimePermissionForAccessCamera()
                    } else {
                        Toast.makeText(
                            this@BarcodeFragment.requireActivity(),
                            getString(R.string.need_camera_permission),
                            Toast.LENGTH_LONG
                        ).show()
                        this@BarcodeFragment.requireActivity().finish()
                    }
                }
            }
            else -> {
            }
        }
    }

    // setting up camera to scan barcode or QR
    private fun setUpQRScanner() {
        val barcodeDetector = BarcodeDetector.Builder(this@BarcodeFragment.requireActivity())
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        mCameraSource = CameraSource.Builder(this@BarcodeFragment.requireActivity(), barcodeDetector)
            .setAutoFocusEnabled(true)
            .build()

        svCameraView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            this@BarcodeFragment.requireActivity(),
                            Manifest.permission.CAMERA
                        ) !== PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    mCameraSource!!.start(svCameraView.holder)
                } catch (ie: IOException) {
                    Log.e("CAMERA SOURCE", ie.message)
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mCameraSource!!.stop()
            }
        })

        barcodeDetector.setProcessor(object :
            Detector.Processor<Barcode> {
            override fun release() {}
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    val scannedValue = barcodes.valueAt(0).displayValue
                    Toast.makeText(this@BarcodeFragment.requireActivity(),scannedValue,Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

}