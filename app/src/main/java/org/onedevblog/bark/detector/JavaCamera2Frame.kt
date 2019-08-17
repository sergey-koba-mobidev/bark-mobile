package org.onedevblog.bark.detector

import android.media.Image
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.core.CvType
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame


class JavaCamera2Frame(private val mImage: Image) : CvCameraViewFrame {
    private val mRgba: Mat
    private var mGray: Mat? = null
    override fun gray(): Mat? {
        val planes = mImage.getPlanes()
        val w = mImage.getWidth()
        val h = mImage.getHeight()
        val y_plane = planes[0].getBuffer()
        mGray = Mat(h, w, CvType.CV_8UC1, y_plane)
        return mGray
    }

    override fun rgba(): Mat {
        val planes = mImage.getPlanes()
        val w = mImage.getWidth()
        val h = mImage.getHeight()
        val chromaPixelStride = planes[1].getPixelStride()


        if (chromaPixelStride == 2) { // Chroma channels are interleaved
            assert(planes[0].getPixelStride() === 1)
            assert(planes[2].getPixelStride() === 2)
            val y_plane = planes[0].getBuffer()
            val uv_plane1 = planes[1].getBuffer()
            val uv_plane2 = planes[2].getBuffer()
            val y_mat = Mat(h, w, CvType.CV_8UC1, y_plane)
            val uv_mat1 = Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane1)
            val uv_mat2 = Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane2)
            val addr_diff = uv_mat2.dataAddr() - uv_mat1.dataAddr()
            if (addr_diff > 0) {
                assert(addr_diff == 1L)
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat1, mRgba, Imgproc.COLOR_YUV2RGBA_NV12)
            } else {
                assert(addr_diff == -1L)
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat2, mRgba, Imgproc.COLOR_YUV2RGBA_NV21)
            }
            return mRgba
        } else { // Chroma channels are not interleaved
            val yuv_bytes = ByteArray(w * (h + h / 2))
            val y_plane = planes[0].getBuffer()
            val u_plane = planes[1].getBuffer()
            val v_plane = planes[2].getBuffer()

            y_plane.get(yuv_bytes, 0, w * h)

            val chromaRowStride = planes[1].getRowStride()
            val chromaRowPadding = chromaRowStride - w / 2

            var offset = w * h
            if (chromaRowPadding == 0) {
                // When the row stride of the chroma channels equals their width, we can copy
                // the entire channels in one go
                u_plane.get(yuv_bytes, offset, w * h / 4)
                offset += w * h / 4
                v_plane.get(yuv_bytes, offset, w * h / 4)
            } else {
                // When not equal, we need to copy the channels row by row
                for (i in 0 until h / 2) {
                    u_plane.get(yuv_bytes, offset, w / 2)
                    offset += w / 2
                    if (i < h / 2 - 1) {
                        u_plane.position(u_plane.position() + chromaRowPadding)
                    }
                }
                for (i in 0 until h / 2) {
                    v_plane.get(yuv_bytes, offset, w / 2)
                    offset += w / 2
                    if (i < h / 2 - 1) {
                        v_plane.position(v_plane.position() + chromaRowPadding)
                    }
                }
            }

            val yuv_mat = Mat(h + h / 2, w, CvType.CV_8UC1)
            yuv_mat.put(0, 0, yuv_bytes)
            Imgproc.cvtColor(yuv_mat, mRgba, Imgproc.COLOR_YUV2RGBA_I420, 4)
            return mRgba
        }
    }


    init {
        mRgba = Mat()
        mGray = Mat()
    }

    fun release() {
        mRgba.release()
        mGray!!.release()
    }
}