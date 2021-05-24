package hys.hmonkeyys.readimagetext

import android.app.Application
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import java.io.IOException

class ReadImage (applicationContext: Application ,uri: Uri) {
    private val mApplicationContext = applicationContext
    private val mUri = uri

    fun readImageText(): String {
        var returnValue = ""

        try {
            val image = InputImage.fromFilePath(mApplicationContext, mUri)
            val recognizer = TextRecognition.getClient()

            recognizer.process(image).addOnSuccessListener {
                val resultText = it.text
                for (block in it.textBlocks) {
//                    val blockText = block.text
//                    Log.e("+++Test1+++", blockText)

//                    val blockCornerPoints = block.cornerPoints
//                    Log.e("+++Test1+++", blockCornerPoints.toString())

//                    val blockFrame = block.boundingBox
//                    Log.e("+++Test1+++", blockFrame.toString())

                    for (line in block.lines) {
                        val lineText = line.text
//                        Log.e("+++Test2+++", lineText.toString())

                        val lineCornerPoints = line.cornerPoints
//                        Log.e("+++Test2+++", lineCornerPoints.toString())

                        val lineFrame = line.boundingBox
//                        Log.e("+++Test2+++", lineFrame.toString())

                        for (element in line.elements) {
                            val elementText = element.text
//                            Log.e("+++Test3+++", elementText.toString())

                            val elementCornerPoints = element.cornerPoints
//                            Log.e("+++Test3+++", elementCornerPoints.toString())

                            val elementFrame = element.boundingBox
//                            Log.e("+++Test3+++", elementFrame.toString())
                        }
                    }
                }
                Log.e("Result ::", resultText)
                returnValue = resultText
            }.addOnFailureListener {
                it.printStackTrace()
                returnValue = "Error"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            returnValue = "Error"
        }

        return returnValue
    }
}
