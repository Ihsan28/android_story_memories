package com.ihsan.memorieswithimagevideo.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.red
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.daimajia.numberprogressbar.NumberProgressBar
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.Utils.VideoCapture
import com.ihsan.memorieswithimagevideo.data.Data
import com.ihsan.memorieswithimagevideo.data.Data.Companion.animationDuration
import com.ihsan.memorieswithimagevideo.data.Data.Companion.contentUris
import com.ihsan.memorieswithimagevideo.data.Data.Companion.coverRevealDuration
import com.ihsan.memorieswithimagevideo.data.Data.Companion.currentIndex
import com.ihsan.memorieswithimagevideo.data.Data.Companion.screenHeight
import com.ihsan.memorieswithimagevideo.data.Data.Companion.screenWidth
import com.ihsan.memorieswithimagevideo.data.MediaType
import com.ihsan.memorieswithimagevideo.databinding.FragmentMemoriesBinding
import jp.wasabeef.transformers.glide.BlurTransformation

class MemoriesFragment : Fragment() {
    private val TAG = "MemoriesFragment"
    private lateinit var binding: FragmentMemoriesBinding

    //private lateinit var viewPager2: ViewPager2
    private lateinit var pickImageButton: Button
    private lateinit var editButton: Button
    private lateinit var exportButton: Button
    private lateinit var mediaPlayer: MediaPlayer

    //pick image launcher contract for image picker intent
    private val pickMediaContract: ActivityResultLauncher<Intent> = registerPickMediaContract()

    //animation properties
    private val REQUEST_CODE_PERMISSIONS = 1001
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var cardView: CardView
    private lateinit var coverImageView: ImageView
    private lateinit var currentImageView: ImageView

    private lateinit var collageImageView: ImageView
    private lateinit var collageImageView_1: ImageView
    private lateinit var collageImageView_2: ImageView
    private lateinit var collageImageView_3: ImageView

    private lateinit var doubleImageViewLayout: LinearLayout
    private lateinit var doubleImageView1: ImageView
    private lateinit var doubleImageView2: ImageView

    private lateinit var tripleImageViewLayout: LinearLayout
    private lateinit var tripleImageView1: ImageView
    private lateinit var tripleImageView2: ImageView
    private lateinit var tripleImageView3: ImageView
    private lateinit var videoView: VideoView
    private var currentContentUri: Uri = Uri.EMPTY
    private lateinit var recordAnimation: VideoCapture
    private var i = 0
    private lateinit var progressBar: NumberProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*viewPager2 = view.findViewById(R.id.viewPager2)
        viewPager2.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        viewPager2.setPageTransformer(CustomPageTransformer())*/

        checkPermissions()
        cardView = view.findViewById(R.id.cardViewAnimationRoot)

        coverImageView = view.findViewById(R.id.coverImageView)
        currentImageView = view.findViewById(R.id.currentImageView)
        collageImageView = view.findViewById(R.id.collageImageView)
        collageImageView_1 = view.findViewById(R.id.collageImageView_1)
        collageImageView_2 = view.findViewById(R.id.collageImageView_2)
        collageImageView_3 = view.findViewById(R.id.collageImageView_3)

        doubleImageViewLayout = view.findViewById(R.id.doubleImageViewLayout)
        doubleImageView1 = view.findViewById(R.id.doubleImageView1)
        doubleImageView2 = view.findViewById(R.id.doubleImageView2)

        tripleImageViewLayout = view.findViewById(R.id.tripleImageViewLayout)
        tripleImageView1 = view.findViewById(R.id.tripleImageView1)
        tripleImageView2 = view.findViewById(R.id.tripleImageView2)
        tripleImageView3 = view.findViewById(R.id.tripleImageView3)

        videoView = view.findViewById(R.id.videoView)

        progressBar = view.findViewById(R.id.number_progress_bar)
        progressBar.visibility = View.INVISIBLE
        progressBar.max = 100
        progressBar.reachedBarHeight = 40f
        progressBar.reachedBarColor = Color.GREEN

        editButton = view.findViewById(R.id.edit)

        exportButton = view.findViewById(R.id.export)
        exportButton.visibility = View.INVISIBLE

        pickImageButton = view.findViewById(R.id.pickImageButton)

        initRecording()
        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.aylex)
        mediaPlayer.isLooping = true

        mediaPlayer.setOnCompletionListener {
            // Animation has ended, stop the audio
            mediaPlayer.stop()
        }

        if (contentUris.value!!.isNotEmpty()) {
            currentIndex = 0
            mediaPlayer.start()
            //callViewPagerAdapter()
        }

        pickImageButton.setOnClickListener {
            pickMediaContent()
        }

        editButton.setOnClickListener {
            recordAnimation.stopRecordingUsingFFMPEG()
            val action = MemoriesFragmentDirections.actionMemoriesFragmentToEditSelectedFragment()
            findNavController().navigate(action)
        }

        exportButton.setOnClickListener {
            exportVideoFFMPEG()
        }

        if (contentUris.value!!.isNotEmpty()) {
            startAnimation()
        }
    }

    private fun registerPickMediaContract(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val clipData = result.data!!.clipData
                contentUris.value!!.clear()
                mediaPlayer.start()
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val mediaUri = clipData.getItemAt(i).uri
                        contentUris.value!!.add(mediaUri)
                    }

                } else {
                    val mediaUri = result.data!!.data
                    if (mediaUri != null) {
                        contentUris.value!!.clear()
                        contentUris.value!!.add(mediaUri)
                    }
                }

                Data().mapContentUrisToMediaItems()
                //callViewPagerAdapter()
                startAnimation()
            }
        }
    }

    private fun startAnimation() {
        progressBar.visibility = View.INVISIBLE
        exportButton.visibility = View.INVISIBLE
        currentIndex = 0
        i = 0
        recordAnimation.stopRecordingUsingFFMPEG()
        cardView.post {
            showNextImage()
            recordAnimation.startRecordingFFMPEG()
        }
    }

    private fun pickMediaContent() {
        val pickImagesIntent = Intent(Intent.ACTION_PICK)
        pickImagesIntent.type = "image/*"
        pickImagesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        val pickVideosIntent = Intent(Intent.ACTION_PICK)
        pickVideosIntent.type = "video/*"
        pickVideosIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        val chooserIntent = Intent.createChooser(
            pickImagesIntent,
            "Select Images and Videos"
        )
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickVideosIntent))

        try {
            pickMediaContract.launch(chooserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the exception, possibly by displaying an error message.
        }
    }

    /*private fun callViewPagerAdapter() {
        if (contentUris.value!!.isNotEmpty()) {
            val tabMatchAdapter =
                ViewPagerAdapter(childFragmentManager, lifecycle)
            viewPager2.adapter = tabMatchAdapter
        }
    }*/

    private fun checkPermissions() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun initRecording() {
        recordAnimation = VideoCapture(cardView, object : AnimationRecordingCallbacks {
            override fun onRecordingStarted() {

                Log.d(TAG, "onRecordingStarted")
            }

            override fun onRecordingStopped() {

                Log.d(TAG, "onRecordingStopped")
            }

            override fun onRecordingFailed() {
                Log.e(TAG, "onRecordingFailed")
            }

            override fun onFrameAvailable(imagePaths: List<String>) {
                //showNextImage()
            }

            override fun onExportReady() {
                Log.d(TAG, "onExportReady")
                exportButton.visibility = View.VISIBLE
                mediaPlayer.stop()
            }

            override fun onExportStarted() {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 0
                Toast.makeText(requireContext(), "onExportStarted", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onExportStarted")
            }

            override fun onExportProgress(progressPercentage: Int) {
                Log.d(TAG, "onExportProgress: $progressPercentage")
                progressBar.visibility = View.VISIBLE
                progressBar.progress = progressPercentage
            }

            override fun onExportFinished() {
                Log.d(TAG, "onExportFinished")
                progressBar.animate().scaleX(2f).withEndAction {
                    progressBar.visibility = View.INVISIBLE
                    exportButton.visibility = View.INVISIBLE
                }
            }

            override fun onExportFailed(e: Exception) {
                Log.e(TAG, "onExportFailed: $e")
                progressBar.textColor.red
            }
        })
    }

    private fun exportVideoFFMPEG() {
        if (recordAnimation.isReadyToExport) {
            Toast.makeText(requireContext(), "Exporting video", Toast.LENGTH_SHORT).show()
            recordAnimation.exportVideoFFMPEG()
        }
    }

    private fun showNextImage() {
        if (contentUris.value!!.isNotEmpty()) {
            //Increment the index
            nextImageUri()

            if (Data.mediaItems[currentIndex].second == MediaType.VIDEO) {
                Toast.makeText(requireContext(), "video", Toast.LENGTH_SHORT).show()
                setVideoViewShapeWithPosition()
                return
            }

            val animations = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

            if (animations.size <= i) {
                recordAnimation.stopRecordingUsingFFMPEG()
                return
            }
            /*if (contentUris.value!!.size-1 > currentIndex) {
                recordAnimation.stopRecordingUsingFFMPEG()
                return
            }*/

            when (animations[i++ % animations.size]) {
                "1" -> {
                    transitionWithSlideForDoubleImage()
                }

                "2" -> {
                    transitionWithSlideForTripleImage()
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
                    transitionWithCollage()
                }

                "10" -> {
                    transitionWithScaleDownCollage()
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

    private fun setVideoViewShapeWithPosition() {
        videoView.setVideoURI(currentContentUri)

        videoView.setOnPreparedListener {
            val lastFrame =
                retrieveFrameFromVideo(
                    Data.mediaItems[currentIndex].first,
                    videoView.duration.toLong()
                )
            videoView.alpha = 1f
            currentImageView.alpha = 0f
            coverImageView.alpha = 0f
            currentImageView.setImageURI(null)
            coverImageView.setImageURI(lastFrame?.let { it1 -> Uri.parse(it1.toString()) })
            videoView.start()
        }

        //on video completion
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
            .setDuration(Data.coverRevealDuration)
            .withEndAction {
                //reset current
                setImageViewShapeWithPosition(currentImageView)
                setImageFromContentUri(currentImageView, currentContentUri)

                // show current image
                coverImageView.alpha = 0f

                currentImageView.animate().scaleX(1.15f).scaleY(1.15f)
                    .setDuration(Data.animationDuration)
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
            .setDuration(Data.coverRevealDuration)
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
                    .setDuration(Data.animationDuration * 2)
                    .withEndAction {

                        //second transition
                        setImageViewShapeWithPosition(coverImageView, 2f, 2f, -screenWidth / 2.2f)
                        setImageFromContentUri(coverImageView, nextImageUri)

                        coverImageView.animate().alpha(1f)
                            .setDuration(Data.coverRevealDuration)
                            .withEndAction {
                                setImageFromContentUri(currentImageView, nextImageUri)

                                //reset cover shape
                                coverImageView.alpha = 0f

                                currentImageView.animate().scaleX(1f).scaleY(1f).translationX(0f)
                                    .setDuration(Data.animationDuration * 2)
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
        val translationXLeft = Data.screenWidth / (imageScaleUp * 2.1f)
        val translationXRight = -Data.screenWidth / (imageScaleUp * 1.1f)
        //reset cover shape
        setImageViewShapeWithPosition(coverImageView, imageScaleUp, imageScaleUp, translationXLeft)

        setImageFromContentUri(coverImageView, currentContentUri)
        coverImageView.animate().alpha(1f)
            .setDuration(Data.coverRevealDuration)
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
                    .setDuration(Data.animationDuration)
                    .withEndAction {
                        val nextImageUri = nextImageUri()
                        //second transition
                        setImageFromContentUri(coverImageView, nextImageUri)
                        coverImageView.animate().alpha(1f)
                            .setDuration(Data.coverRevealDuration)
                            .withEndAction {
                                setImageFromContentUri(currentImageView, nextImageUri)

                                //reset cover shape
                                coverImageView.alpha = 0f

                                currentImageView.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .translationX(0f)
                                    .setDuration(Data.animationDuration)
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
            .setDuration(Data.animationDuration)
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
                    .setDuration(Data.animationDuration)
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
            .setDuration(Data.animationDuration)
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
                    .setDuration(Data.animationDuration)
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
            .setDuration(Data.animationDuration)
            .withEndAction {

                //set current
                setImageViewShapeWithPosition(currentImageView, 2f, 2f)
                setImageFromContentUri(currentImageView, currentContentUri)

                // show current image
                coverImageView.alpha = 0f

                currentImageView.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(Data.animationDuration / 2)
                    .withEndAction {
                        currentImageView.animate().translationX(Data.screenWidth)
                            .setDuration(Data.animationDuration)
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
            .setDuration(Data.coverRevealDuration)
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
                    .translationX(Data.screenWidth)
                    .setDuration(0)
                    .start()
                collageImageView_1.animate()
                    .translationX(Data.screenWidth)
                    .setDuration(0)
                    .start()

                // Start the animation to scale down ImageView
                currentImageView.animate()
                    .scaleX(0.5f).scaleY(0.5f)
                    .translationX(Data.screenWidth / -4)
                    .translationY(Data.screenWidth / -4)
                    .setDuration(Data.animationDuration / 2).withEndAction {

                        //transition enter and scale down to position
                        collageImageView.animate().alpha(1f)
                            .scaleX(0.5f).scaleY(0.5f)
                            .translationX(0f)
                            .setDuration(Data.animationDuration / 3).withEndAction {

                                //transition enter and scale down to position
                                collageImageView_1.animate().alpha(1f)
                                    .scaleX(0.5f).scaleY(0.5f)
                                    .translationX(Data.screenWidth / 4)
                                    .translationY(Data.screenWidth / 4)
                                    .setDuration(Data.animationDuration / 4).withEndAction {
                                        collageImageView.animate().alpha(0f)
                                            .setDuration(Data.coverRevealDuration)
                                            .start()
                                        collageImageView_1.animate().alpha(0f)
                                            .setDuration(Data.coverRevealDuration)
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
            .setDuration(Data.animationDuration)
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
                    .translationX(Data.screenWidth / -4)
                    .translationY(Data.screenHeight / -4)
                    .rotation(-25f)
                    .setDuration(Data.animationDuration)
                    .withEndAction {

                        collageImageView.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                            .translationX(Data.screenWidth / 4)
                            .translationY(Data.screenHeight / 4)
                            .rotation(25f)
                            .setDuration(Data.animationDuration)
                            .withEndAction {

                                collageImageView_1.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                                    .translationX(Data.screenWidth / 5)
                                    .translationY(Data.screenHeight / -5)
                                    .rotation(25f)
                                    .setDuration(Data.animationDuration)
                                    .withEndAction {
                                        collageImageView_2.animate().alpha(1f).scaleX(0.3f)
                                            .scaleY(0.3f)
                                            .translationX(Data.screenWidth / -5)
                                            .translationY(Data.screenHeight / 5)
                                            .rotation(-25f)
                                            .setDuration(Data.animationDuration)
                                            .withEndAction {

                                                collageImageView_3.animate().alpha(1f)
                                                    .scaleX(0.3f).scaleY(0.3f)
                                                    .setDuration(Data.animationDuration)
                                                    .withEndAction {

                                                        //stay delay
                                                        //delay(animationDuration / 2)

                                                        //reset college Image view
                                                        collageImageView.animate().alpha(0f)
                                                            .setDuration(Data.coverRevealDuration)
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
                                                            .setDuration(Data.coverRevealDuration)
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
                                                            .setDuration(Data.coverRevealDuration)
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
                                                            .setDuration(Data.coverRevealDuration)
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

    private fun transitionWithSlideForDoubleImage() {
        val currentContentUri = currentContentUri
        val imageUri1 = nextImageUri()
        val imageUri2 = nextImageUri()

        // Load animations
        val slideInLeft: Animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_left)
        val slideInRight: Animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_right)

        setImageViewShapeWithPosition(coverImageView)
        setImageFromContentUri(coverImageView, currentContentUri)

        coverImageView.animate().alpha(1f)
            .setDuration(coverRevealDuration)
            .withEndAction {

                setImageViewShapeWithPosition(currentImageView)
                setImageFromContentUri(currentImageView, currentContentUri)

                // show current image
                coverImageView.alpha = 0f

                setImageViewShapeWithPosition(doubleImageView1, translationX = -screenWidth)
                setImageViewShapeWithPosition(doubleImageView2, translationX = screenWidth)
                setImageFromContentUri(doubleImageView1, imageUri1)
                setImageFromContentUri(doubleImageView2, imageUri2)

                doubleImageViewLayout.animate()
                    .alpha(1f)
                    .setDuration(animationDuration / 3)
                    .withEndAction {
                        // Assuming you have URIs or resource IDs for your images

                        doubleImageView1.animate()
                            .translationX(0f)
                            .setDuration(animationDuration / 3)
                            .withEndAction {
                                doubleImageView2.animate()
                                    .translationX(0f)
                                    .setDuration(animationDuration / 3)
                                    .withEndAction {
                                        doubleImageViewLayout.animate()
                                            .alpha(0f)
                                            .setDuration(coverRevealDuration)
                                            .withEndAction {
                                                //next image animation
                                                showNextImage()
                                            }
                                            .start()
                                    }
                                    .start()
                            }.start()
                    }.start()
            }
            .start()

        // Additional logic for updating image URIs and re-triggering animations if needed
    }

    private fun transitionWithSlideForTripleImage() {
        val currentContentUri = currentContentUri
        val imageUri1 = nextImageUri()
        val imageUri2 = nextImageUri()
        val imageUri3 = nextImageUri()

        // Load animations
        val slideInBottom: Animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_bottom)

        setImageViewShapeWithPosition(coverImageView)
        setImageFromContentUri(coverImageView, currentContentUri)

        coverImageView.animate().alpha(1f)
            .setDuration(coverRevealDuration)
            .withEndAction {

                setImageViewShapeWithPosition(currentImageView)
                setImageFromContentUri(currentImageView, currentContentUri)

                // show current image
                coverImageView.alpha = 0f

                setImageViewShapeWithPosition(tripleImageView1, translationX = screenWidth, translationY = screenHeight)
                setImageViewShapeWithPosition(tripleImageView2, translationX = screenWidth, translationY = screenHeight)
                setImageViewShapeWithPosition(tripleImageView3, translationX = screenWidth, translationY = screenHeight)

                setImageFromContentUri(tripleImageView1, imageUri1)
                setImageFromContentUri(tripleImageView2, imageUri2)
                setImageFromContentUri(tripleImageView3, imageUri3)

                tripleImageViewLayout.animate()
                    .alpha(1f)
                    .setDuration(animationDuration / 3)
                    .withEndAction {
                        // Assuming you have URIs or resource IDs for your images

                        tripleImageView1.animate()
                            .translationX(0f)
                            .translationY(0f)
                            .setDuration(animationDuration / 3)
                            .withEndAction {
                                tripleImageView2.animate()
                                    .translationX(0f)
                                    .translationY(0f)
                                    .setDuration(animationDuration / 3)
                                    .withEndAction {
                                        tripleImageView3.animate()
                                            .translationX(0f)
                                            .translationY(0f)
                                            .setDuration(animationDuration / 3)
                                            .withEndAction {
                                                tripleImageViewLayout.animate()
                                                    .alpha(0f)
                                                    .setDuration(coverRevealDuration)
                                                    .withEndAction {
                                                        //next image animation
                                                        showNextImage()
                                                    }
                                                    .start()
                                            }.start()
                                    }.start()
                            }.start()
                    }
                    .start()
            }
    }
}