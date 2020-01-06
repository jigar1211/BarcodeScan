package com.bynq.wallet.ui.activity


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.bynq.wallet.R
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_pay_to_merchant.*
import java.io.IOException


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class PayToMerchantActivity : FragmentActivity(){

    private val REQUEST_RUNTIME_PERMISSION_CAMERA = 400

    // Other Objects
    private var mCameraSource: CameraSource? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_to_merchant)
        requestRunTimePermissionForAccessCamera()
    }

    // request runtime permissions for camera
    private fun requestRunTimePermissionForAccessCamera() = if (
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) !== PackageManager.PERMISSION_GRANTED
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    ) {

        ActivityCompat.requestPermissions(
            this,
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
                            this,
                            Manifest.permission.CAMERA
                        )
                    if (showRational) {
                        requestRunTimePermissionForAccessCamera()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.need_camera_permission),
                            Toast.LENGTH_LONG
                        ).show()
                        this.finish()
                    }
                }
            }
            else -> {
            }
        }
    }
    // setting up camera to scan barcode or QR
    private fun setUpQRScanner() {
        val barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        mCameraSource = CameraSource.Builder(this, barcodeDetector)
            .setAutoFocusEnabled(true)
            .build()

        mSvCameraView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {

                    if (ActivityCompat.checkSelfPermission(
                            this@PayToMerchantActivity,
                            Manifest.permission.CAMERA
                        ) !== PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    mCameraSource!!.start(mSvCameraView.holder)
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
            override fun receiveDetections(detections: Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    val scannedValue = barcodes.valueAt(0).displayValue
                    Toast.makeText(this@PayToMerchantActivity,scannedValue,Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

}
