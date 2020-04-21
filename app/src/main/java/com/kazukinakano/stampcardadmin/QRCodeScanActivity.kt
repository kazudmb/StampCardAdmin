package com.kazukinakano.stampcardadmin

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.android.synthetic.main.activity_qrcode_scan.*

class QRCodeScanActivity : AppCompatActivity() {

    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scan)

        val toolbar = findViewById<Toolbar>(R.id.tool_bar)
        toolbar.title = getString(R.string.app_name)
        setSupportActionBar(toolbar)
    }

    public override fun onResume() {
        super.onResume()
        checkPermissions()
        initQRCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                initQRCamera()
            }
        }
    }

    private fun checkPermissions() {
        // already we got permission.
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            qr_view.resume()
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 999)
        }
    }

    @SuppressLint("WrongConstant")
    private fun initQRCamera() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val isReadPermissionGranted =
            (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        val isWritePermissionGranted =
            (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        val isCameraPermissionGranted =
            (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)

        if (isReadPermissionGranted && isWritePermissionGranted && isCameraPermissionGranted) {
            openQRCamera()
        } else {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun openQRCamera() {
        qr_view.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result != null) {
                    onPause()
                    Log.d(TAG, "$result")
                    uid = result.toString()
                    showDialog()
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
        })
    }

    private fun showDialog() {
        AlertDialog.Builder(this)
            .setTitle("確認")
            .setMessage("スタンプを押しますか？")
            .setPositiveButton("YES") { _, _ ->
                getNumberOfVisits()
            }
            .setNegativeButton("NO") { _, _ ->
                openQRCamera()
            }
            .show()
    }

    private fun getNumberOfVisits() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { result ->
                val numberOfVisits = result.data?.get("NumberOfVisits") as Long
                Log.d(TAG, "Success getting field(NumberOfVisits).")
                increaseNumberOfVisits(numberOfVisits)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting field(NumberOfVisits).", exception)
            }
    }

    private fun increaseNumberOfVisits(numberOfVisits: Long) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(uid)
            .update("NumberOfVisits", numberOfVisits + 1)
            .addOnSuccessListener {
                Log.d(TAG, "Success increase field(NumberOfVisits).")
                Toast.makeText(baseContext, "スタンプを押しました", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error increase field(NumberOfVisits).", exception)
            }
    }

    companion object {
        const val REQUEST_CAMERA_PERMISSION: Int = 1
        const val TAG = "QRCode"
    }
}
