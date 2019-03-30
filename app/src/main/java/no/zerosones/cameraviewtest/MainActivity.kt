package no.zerosones.cameraviewtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.otaliastudios.cameraview.Frame
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {

    private var shouldThrottleBarcodeDetector = AtomicBoolean(false)

    private val barcodeDetector by lazy {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()

        FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
        camera_view.setLifecycleOwner(this)

        camera_view.addFrameProcessor {
            if (!shouldThrottleBarcodeDetector.get()) {
                processFrame(it)
            }
        }
    }

    private fun processFrame(frame: Frame) {
        shouldThrottleBarcodeDetector.set(true)

        val firebaseVisionImage = FirebaseVisionImage.fromByteArray(frame.data, getMetadataForFrame(frame))
        barcodeDetector.detectInImage(firebaseVisionImage)
            .addOnSuccessListener { firebaseVisionBarcodes ->
                Log.d(this.javaClass.name, "Frame data null? ${frame.data == null} Frame size null? ${frame.size == null}")
            }.addOnCompleteListener {
                shouldThrottleBarcodeDetector.set(false)
            }
    }

    private fun getMetadataForFrame(frame: Frame): FirebaseVisionImageMetadata =
        FirebaseVisionImageMetadata.Builder()
            .setWidth(frame.size.width)
            .setHeight(frame.size.height)
            .setRotation(getRotation(frame.rotation))
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .build()

    private fun getRotation(rotation: Int): Int {
        return when (rotation) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> {
                FirebaseVisionImageMetadata.ROTATION_0
            }
        }
    }
}
