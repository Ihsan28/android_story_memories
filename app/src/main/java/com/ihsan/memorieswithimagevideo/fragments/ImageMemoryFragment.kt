package com.ihsan.memorieswithimagevideo.fragments

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.Utils.VideoCapture
import com.ihsan.memorieswithimagevideo.data.Data.Companion.animationDuration
import com.ihsan.memorieswithimagevideo.data.Data.Companion.contentUris
import com.ihsan.memorieswithimagevideo.data.Data.Companion.coverRevealDuration
import com.ihsan.memorieswithimagevideo.data.Data.Companion.currentIndex
import com.ihsan.memorieswithimagevideo.data.Data.Companion.mediaItems
import com.ihsan.memorieswithimagevideo.data.Data.Companion.screenHeight
import com.ihsan.memorieswithimagevideo.data.Data.Companion.screenWidth
import com.ihsan.memorieswithimagevideo.data.MediaType
import jp.wasabeef.transformers.glide.BlurTransformation

class ImageMemoryFragment : Fragment() {
    private lateinit var cardView: CardView
    private lateinit var coverImageView: ImageView
    private lateinit var currentImageView: ImageView
    private lateinit var collageImageView: ImageView
    private lateinit var collageImageView_1: ImageView
    private lateinit var collageImageView_2: ImageView
    private lateinit var collageImageView_3: ImageView
    private lateinit var videoView: VideoView
    private var currentContentUri: Uri = Uri.EMPTY
    private lateinit var recordAnimation: VideoCapture
    private var i = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_memory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardView = view.findViewById(R.id.cardViewAnimationRoot)
        recordAnimation = VideoCapture(cardView)

        coverImageView = view.findViewById(R.id.coverImageView)
        currentImageView = view.findViewById(R.id.currentImageView)
        collageImageView = view.findViewById(R.id.collageImageView)
        collageImageView_1 = view.findViewById(R.id.collageImageView_1)
        collageImageView_2 = view.findViewById(R.id.collageImageView_2)
        collageImageView_3 = view.findViewById(R.id.collageImageView_3)
        videoView = view.findViewById(R.id.videoView)

        showNextImage()

        cardView.post {
            recordAnimation.startRecordingUsingFFMPEG()
        }
    }

    private fun showNextImage() {
        if (contentUris.value!!.isNotEmpty()) {
            /*if (!recordAnimation.isFfmpegRecorderStarted){
                Toast.makeText(requireContext(), "recording", Toast.LENGTH_SHORT).show()
                recordAnimation.startRecordingUsingFFMPEG()
            }else{
                recordAnimation.stopRecordingUsingFFMPEG()
            }*/
            //Increment the index
            nextImageUri()

            if (mediaItems[currentIndex].second == MediaType.VIDEO) {
                Toast.makeText(requireContext(), "video", Toast.LENGTH_SHORT).show()
                setVideoViewShapeWithPosition()
                return
            }

            Toast.makeText(requireContext(), currentContentUri.toString(), Toast.LENGTH_SHORT)
                .show()

            val animations = listOf("1", "2", "3", "4", "5", "6", "7", "8")
            when (animations[i++ % animations.size]) {
                "1" -> {
                    transitionWithCollage()
                }

                "2" -> {
                    recordAnimation.stopRecordingUsingFFMPEG()
                    transitionWithScaleDownCollage()
                }

                "3" -> {
                    transitionWithScaleUp()
                }

                "4" -> {
                    transitionWithScaleDown()
                }

                "5" -> {
                    transitionWithBlurry()
                }

                "6" -> {
                    transitionWithScaleDownWithSlideInOut()
                }

                "7" -> {
                    transitionWithMoveWithInitialZoom()
                }

                "8" -> {
                    transitionWithScaleUpWithMove()
                }

                "9" -> {
                    setVideoViewShapeWithPosition()
                }

                else -> {
                    Toast.makeText(requireContext(), "No animation", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun nextImageUri(): Uri {
        currentIndex = (currentIndex + 1) % contentUris.value!!.size
        currentContentUri = contentUris.value!![currentIndex]

        return currentContentUri
    }

    private fun setImageViewShapeWithPosition(
        imageView: ImageView,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        translationX: Float = 0f,
        translationY: Float = 0f,
        rotation: Float = 0f
    ) {
        imageView.scaleX = scaleX
        imageView.scaleY = scaleY
        imageView.translationX = translationX
        imageView.translationY = translationY
        imageView.rotation = rotation
    }

    private fun setVideoViewShapeWithPosition(
    ) {
        videoView.setVideoURI(currentContentUri)
        //retrieve a frame from the end of the video
        //val fastFrame= retrieveFrameFromVideo(currentContentUri, 0)


        videoView.setOnPreparedListener {
            val lastFrame =
                retrieveFrameFromVideo(mediaItems[currentIndex].first, videoView.duration.toLong())
            videoView.alpha = 1f
            currentImageView.alpha = 0f
            coverImageView.alpha = 0f
            currentImageView.setImageURI(null)
            coverImageView.setImageURI(lastFrame?.let { it1 -> Uri.parse(it1.toString()) })
            videoView.start()
        }
        videoView.setOnCompletionListener {
            videoView.alpha = 0f
            coverImageView.alpha = 1f
            currentImageView.alpha = 1f
            showNextImage()
        }
    }

    private fun retrieveFrameFromVideo(videoUri: Uri, timeInMillis: Long): Bitmap? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoUri.path)

        // Retrieve a frame at the specified time (in microseconds)
        val frame = retriever.getFrameAtTime(
            timeInMillis * 1000,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        )

        // Release the retriever
        retriever.release()

        return frame
    }

    private fun setImageFromContentUri(imageView: ImageView, contentUri: Uri) {
        Glide.with(requireContext())
            .load(contentUri)
            .into(imageView)
    }

    private fun transitionWithScaleUp() {
        //reset cover shape
        setImageViewShapeWithPosition(coverImageView)
        setImageFromContentUri(coverImageView, currentContentUri)

        coverImageView.animate().alpha(1f)
            .setDuration(coverRevealDuration)
            .withEndAction {
                //reset current
                setImageViewShapeWithPosition(currentImageView)
                setImageFromContentUri(currentImageView, currentContentUri)

                // show current image
                coverImageView.alpha = 0f

                currentImageView.animate().scaleX(1.15f).scaleY(1.15f)
                    .setDuration(animationDuration)
                    .withEndAction {
                        // Show the next image
                        showNextImage()
                    }
                    .start()
            }
            .start()
    }

    private fun transitionWithScaleUpWithMove() {
        val nextImageUri = nextImageUri()
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()

        //reset cover shape
        setImageViewShapeWithPosition(coverImageView)
        setImageFromContentUri(coverImageView, currentContentUri)
        coverImageView.animate().alpha(1f)
            .setDuration(coverRevealDuration)
            .withEndAction {

                //reset current
                setImageViewShapeWithPosition(currentImageView)
                setImageFromContentUri(currentImageView, currentContentUri)

                // show current image
                coverImageView.alpha = 0f
                currentImageView.animate()
                    .scaleX(2f)
                    .scaleY(2f)
                    .translationX(-screenWidth / 2.2f)
                    .setDuration(animationDuration * 2)
                    .withEndAction {

                        //second transition
                        setImageViewShapeWithPosition(coverImageView, 2f, 2f, -screenWidth / 2.2f)
                        setImageFromContentUri(coverImageView, nextImageUri)

                        coverImageView.animate().alpha(1f)
                            .setDuration(coverRevealDuration)
                            .withEndAction {
                                setImageFromContentUri(currentImageView, nextImageUri)

                                //reset cover shape
                                coverImageView.alpha = 0f

                                currentImageView.animate().scaleX(1f).scaleY(1f).translationX(0f)
                                    .setDuration(animationDuration * 2)
                                    .withEndAction {
                                        //show next image
                                        showNextImage()
                                    }
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun transitionWithMoveWithInitialZoom() {
        val imageScaleUp = 2f
        val translationXLeft = screenWidth / (imageScaleUp * 2.1f)
        val translationXRight = -screenWidth / (imageScaleUp * 1.1f)
        //reset cover shape
        setImageViewShapeWithPosition(coverImageView, imageScaleUp, imageScaleUp, translationXLeft)

        setImageFromContentUri(coverImageView, currentContentUri)
        coverImageView.animate().alpha(1f)
            .setDuration(coverRevealDuration)
            .withEndAction {

                //reset current
                setImageViewShapeWithPosition(
                    currentImageView,
                    imageScaleUp,
                    imageScaleUp,
                    translationXLeft
                )
                setImageFromContentUri(currentImageView, currentContentUri)

                // show current image
                coverImageView.alpha = 0f
                //set for next image
                setImageViewShapeWithPosition(
                    coverImageView,
                    imageScaleUp,
                    imageScaleUp,
                    translationXRight
                )

                currentImageView.animate()
                    .translationX(translationXRight)
                    .setDuration(animationDuration)
                    .withEndAction {
                        val nextImageUri = nextImageUri()
                        //second transition
                        setImageFromContentUri(coverImageView, nextImageUri)
                        coverImageView.animate().alpha(1f)
                            .setDuration(coverRevealDuration)
                            .withEndAction {
                                setImageFromContentUri(currentImageView, nextImageUri)

                                //reset cover shape
                                coverImageView.alpha = 0f

                                currentImageView.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .translationX(0f)
                                    .setDuration(animationDuration)
                                    .withEndAction {
                                        //show next image
                                        showNextImage()
                                    }
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun transitionWithScaleDown() {
        val scaleUp = 1.2f

        //Init cover with shape
        setImageViewShapeWithPosition(coverImageView, scaleUp, scaleUp)
        setImageFromContentUri(coverImageView, currentContentUri)

        coverImageView.animate().alpha(1f)
            .setDuration(animationDuration)
            .withEndAction {

                //reset current
                setImageViewShapeWithPosition(currentImageView, scaleUp, scaleUp)
                setImageFromContentUri(currentImageView, currentContentUri)

                // show current image
                coverImageView.alpha = 0f
                // Start the animation to scale down the currentImageView
                currentImageView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(animationDuration)
                    .withEndAction {
                        // Show the next image
                        showNextImage()
                    }
                    .start()
            }
            .start()
    }

    private fun transitionWithBlurry() {

        //reset cover shape
        setImageViewShapeWithPosition(coverImageView)
        setImageFromContentUri(coverImageView, currentContentUri)
        coverImageView.animate().alpha(1f)
            .setDuration(animationDuration)
            .withEndAction {
                //reset current
                setImageViewShapeWithPosition(currentImageView)

                // previous image with blur transformation
                Glide.with(requireContext())
                    .load(currentContentUri)
                    .apply(
                        RequestOptions.bitmapTransform(
                            MultiTransformation(
                                /*CropCenterTransformation(),*/
                                BlurTransformation(requireContext(), 15, sampling = 1)
                            )
                        )
                    )
                    .into(currentImageView)

                // show current image with blur
                coverImageView.animate().alpha(0f)
                    .setDuration(animationDuration)
                    .withEndAction {
                        showNextImage()
                    }
                    .start()
            }
            .start()
    }

    private fun transitionWithScaleDownWithSlideInOut() {

        //Init cover with shape
        setImageViewShapeWithPosition(coverImageView, 2f, 2f)
        setImageFromContentUri(coverImageView, currentContentUri)
        coverImageView.animate().alpha(1f)
            .setDuration(animationDuration)
            .withEndAction {

                //set current
                setImageViewShapeWithPosition(currentImageView, 2f, 2f)
                setImageFromContentUri(currentImageView, currentContentUri)

                // show current image
                coverImageView.alpha = 0f

                currentImageView.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(animationDuration / 2)
                    .withEndAction {
                        currentImageView.animate().translationX(screenWidth)
                            .setDuration(animationDuration)
                            .start()
                        //show next image
                        showNextImage()
                    }
                    .start()
            }
            .start()
    }

    private fun transitionWithScaleDownCollage() {
        val currentContentUri = currentContentUri
        val nextImageUri = nextImageUri()
        val previousImageUri = nextImageUri()

        //reset cover shape
        setImageViewShapeWithPosition(coverImageView)
        setImageFromContentUri(coverImageView, currentContentUri)
        coverImageView.animate().alpha(1f)
            .setDuration(coverRevealDuration)
            .withEndAction {
                //reset
                setImageViewShapeWithPosition(collageImageView)
                setImageViewShapeWithPosition(collageImageView_1)
                setImageViewShapeWithPosition(currentImageView)
                //set current
                setImageFromContentUri(currentImageView, currentContentUri)

                // show current image
                coverImageView.alpha = 0f

                setImageFromContentUri(collageImageView, nextImageUri)
                setImageFromContentUri(collageImageView_1, previousImageUri)

                //set other views out of screen
                collageImageView.animate()
                    .translationX(screenWidth)
                    .setDuration(0)
                    .start()
                collageImageView_1.animate()
                    .translationX(screenWidth)
                    .setDuration(0)
                    .start()

                // Start the animation to scale down ImageView
                currentImageView.animate()
                    .scaleX(0.5f).scaleY(0.5f)
                    .translationX(screenWidth / -4)
                    .translationY(screenWidth / -4)
                    .setDuration(animationDuration / 2).withEndAction {

                        //transition enter and scale down to position
                        collageImageView.animate().alpha(1f)
                            .scaleX(0.5f).scaleY(0.5f)
                            .translationX(0f)
                            .setDuration(animationDuration / 3).withEndAction {

                                //transition enter and scale down to position
                                collageImageView_1.animate().alpha(1f)
                                    .scaleX(0.5f).scaleY(0.5f)
                                    .translationX(screenWidth / 4)
                                    .translationY(screenWidth / 4)
                                    .setDuration(animationDuration / 4).withEndAction {
                                        collageImageView.animate().alpha(0f)
                                            .setDuration(coverRevealDuration)
                                            .start()
                                        collageImageView_1.animate().alpha(0f)
                                            .setDuration(coverRevealDuration)
                                            .start()
                                        //show next image
                                        showNextImage()
                                    }
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun transitionWithCollage() {
        val currentContentUri = currentContentUri
        val collegeImageUri = nextImageUri()
        val collegeImageUri_1 = nextImageUri()
        val collegeImageUri_2 = nextImageUri()
        val collegeImageUri_3 = nextImageUri()

        //reset cover shape
        setImageViewShapeWithPosition(coverImageView)
        setImageFromContentUri(coverImageView, currentContentUri)
        coverImageView.animate().alpha(1f)
            .setDuration(animationDuration)
            .withEndAction {
                setImageFromContentUri(currentImageView, currentContentUri)
                //reset current
                setImageViewShapeWithPosition(currentImageView)

                // show current image
                coverImageView.alpha = 0f

                setImageFromContentUri(collageImageView, collegeImageUri)
                setImageFromContentUri(collageImageView_1, collegeImageUri_1)
                setImageFromContentUri(collageImageView_2, collegeImageUri_2)
                setImageFromContentUri(collageImageView_3, collegeImageUri_3)

                //hide previous image view which is blurred
                currentImageView.animate().scaleX(0.3f).scaleY(0.3f)
                    .translationX(screenWidth / -4)
                    .translationY(screenHeight / -4)
                    .rotation(-25f)
                    .setDuration(animationDuration)
                    .withEndAction {

                        collageImageView.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                            .translationX(screenWidth / 4)
                            .translationY(screenHeight / 4)
                            .rotation(25f)
                            .setDuration(animationDuration)
                            .withEndAction {

                                collageImageView_1.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                                    .translationX(screenWidth / 5)
                                    .translationY(screenHeight / -5)
                                    .rotation(25f)
                                    .setDuration(animationDuration)
                                    .withEndAction {
                                        collageImageView_2.animate().alpha(1f).scaleX(0.3f)
                                            .scaleY(0.3f)
                                            .translationX(screenWidth / -5)
                                            .translationY(screenHeight / 5)
                                            .rotation(-25f)
                                            .setDuration(animationDuration)
                                            .withEndAction {

                                                collageImageView_3.animate().alpha(1f)
                                                    .scaleX(0.3f).scaleY(0.3f)
                                                    .setDuration(animationDuration)
                                                    .withEndAction {

                                                        //stay delay
                                                        //delay(animationDuration / 2)

                                                        //reset college Image view
                                                        collageImageView.animate().alpha(0f)
                                                            .setDuration(coverRevealDuration)
                                                            .withEndAction {
                                                                setImageViewShapeWithPosition(
                                                                    collageImageView,
                                                                    1f,
                                                                    1f,
                                                                    0f,
                                                                    0f
                                                                )
                                                            }
                                                            .start()
                                                        collageImageView_1.animate().alpha(0f)
                                                            .setDuration(coverRevealDuration)
                                                            .withEndAction {
                                                                setImageViewShapeWithPosition(
                                                                    collageImageView_1,
                                                                    1f,
                                                                    1f,
                                                                    0f,
                                                                    0f
                                                                )
                                                            }
                                                            .start()
                                                        collageImageView_2.animate().alpha(0f)
                                                            .setDuration(coverRevealDuration)
                                                            .withEndAction {
                                                                setImageViewShapeWithPosition(
                                                                    collageImageView_2,
                                                                    1f,
                                                                    1f,
                                                                    0f,
                                                                    0f
                                                                )
                                                            }
                                                            .start()
                                                        collageImageView_3.animate().alpha(0f)
                                                            .setDuration(coverRevealDuration)
                                                            .withEndAction {
                                                                setImageViewShapeWithPosition(
                                                                    collageImageView_3,
                                                                    1f,
                                                                    1f,
                                                                    0f,
                                                                    0f
                                                                )
                                                            }
                                                            .start()

                                                        //show next image
                                                        showNextImage()
                                                    }
                                                    .start()
                                            }
                                            .start()
                                    }
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }
}