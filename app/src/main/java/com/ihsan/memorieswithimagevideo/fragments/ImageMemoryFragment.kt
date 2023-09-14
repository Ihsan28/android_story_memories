package com.ihsan.memorieswithimagevideo.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.data.Data
import jp.wasabeef.transformers.glide.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ImageMemoryFragment : Fragment() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var coverImageView: ImageView
    private lateinit var currentImageView: ImageView
    private lateinit var collegeImageView: ImageView
    private lateinit var collegeImageView_1: ImageView
    private lateinit var collegeImageView_2: ImageView
    private lateinit var collegeImageView_3: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_memory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //get image uris from bundle
        Data.currentImageIndex = arguments?.getInt("index") ?: 0

        coverImageView = view.findViewById(R.id.coverImageView)
        currentImageView = view.findViewById(R.id.currentImageView)
        collegeImageView = view.findViewById(R.id.collegeImageView)
        collegeImageView_1 = view.findViewById(R.id.collegeImageView_1)
        collegeImageView_2 = view.findViewById(R.id.collegeImageView_2)
        collegeImageView_3 = view.findViewById(R.id.collegeImageView_3)

        coroutineScope.launch {
            showNextImage()
        }
    }

    var i = 0;
    private suspend fun showNextImage() {
        if (Data.imageUris.isNotEmpty()) {
            Data.currentImageIndex = (Data.currentImageIndex + 1) % Data.imageUris.size
            transitionWithMoveWithInitialZoom()

            /*
            val animations = listOf("1", "2", "3", "4", "5", "6", "7")
            when (animations[i++ % animations.size]) {
                "1" -> {
                    Toast.makeText(this@MainActivity, "1", Toast.LENGTH_SHORT).show()
                    transitionWithScaleUpV2()
                }

                "2" -> {
                    Toast.makeText(this@MainActivity, "2", Toast.LENGTH_SHORT).show()
                    transitionWithScaleDownCollage()
                }

                "3" -> {
                    Toast.makeText(this@MainActivity, "3", Toast.LENGTH_SHORT).show()
                    transitionWithScaleUp()
                }

                "4" -> {
                    Toast.makeText(this@MainActivity, "4", Toast.LENGTH_SHORT).show()
                    transitionWithScaleDown()
                }

                "5" -> {
                    Toast.makeText(this@MainActivity, "5", Toast.LENGTH_SHORT).show()
                    transitionWithBlurry()
                }

                "6" -> {
                    Toast.makeText(this@MainActivity, "6", Toast.LENGTH_SHORT).show()
                    transitionWithScaleDownWithSlideInOut()
                }

                "7" -> {
                    Toast.makeText(this@MainActivity, "7", Toast.LENGTH_SHORT).show()
                    transitionWithCollege()
                }
            }*/
        }
    }

    private fun nextImageUri(): Uri {
        return Data.imageUris[++Data.currentImageIndex % Data.imageUris.size]
    }

    private suspend fun transitionWithScaleUpV2() {
        val currentImageUri = Data.imageUris[Data.currentImageIndex]

        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            //reset current
            currentImageView.scaleX = 1f
            currentImageView.scaleY = 1f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f


            // Start the scaling animation
            currentImageView.animate()
                .scaleXBy(0.5f)
                .scaleYBy(0.5f)
                .setDuration(Data.animationDuration)
                .withEndAction {
                    // After scaling animation, return to normal scale
                    currentImageView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(Data.animationDuration)
                        .start()
                    coroutineScope.launch {
                        // Delay before transitioning to the next image
                        delay(Data.animationDuration)

                        // Show the next image
                        showNextImage()
                    }
                }
                .start()
        }
    }

    private suspend fun transitionWithScaleDownCollage() {
        val currentImageUri = Data.imageUris[Data.currentImageIndex]
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val nextImageUri = Data.imageUris[(Data.currentImageIndex + 1) % Data.imageUris.size]
        val previousImageUri = Data.imageUris[(Data.currentImageIndex - 1 + Data.imageUris.size) % Data.imageUris.size]

        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            //reset current
            currentImageView.scaleX = 1f
            currentImageView.scaleY = 1f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f

            collegeImageView.setImageURI(nextImageUri)
            collegeImageView_1.setImageURI(previousImageUri)

            // Start the animation to scale down ImageView
            currentImageView.animate().scaleX(0.5f).scaleY(0.5f).translationX(screenWidth / -4)
                .translationY(screenWidth / -4)
                .setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            //set other views out of screen
            collegeImageView.animate().translationX(screenWidth).setDuration(0)
                .start()
            collegeImageView_1.animate().translationX(screenWidth).setDuration(0)
                .start()

            //transition enter and scale down to position
            collegeImageView.animate().alpha(1f).scaleX(0.5f).scaleY(0.5f).translationX(0f)
                .setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            collegeImageView_1.animate().alpha(1f).scaleX(0.5f).scaleY(0.5f)
                .translationX(screenWidth / 4).translationY(screenWidth / 4)
                .setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            //stay delay
            delay(Data.animationDuration)

            //reverse animation
            collegeImageView_1.animate().alpha(0f).scaleX(1f).scaleY(1f)
                .translationX(screenWidth)
                .setDuration(Data.animationDuration / 2)
                .start()
            delay(Data.animationDuration / 2)

            collegeImageView.animate().alpha(0f).scaleX(1f).scaleY(1f)
                .translationX(screenWidth)
                .setDuration(Data.animationDuration / 2)
                .start()
            delay(Data.animationDuration / 2)

            currentImageView.animate().scaleX(1f).scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(Data.animationDuration / 2)
                .start()
            delay(Data.animationDuration / 2)

            //reset Image view
            collegeImageView.animate().translationX(0f).translationY(0f)
                .setDuration(0).start()
            collegeImageView_1.animate().translationX(0f).translationY(0f)
                .setDuration(0).start()

            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithScaleUp() {
        val currentImageUri = Data.imageUris[Data.currentImageIndex]
        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            //reset current
            currentImageView.scaleX = 1f
            currentImageView.scaleY = 1f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f

            currentImageView.animate().scaleX(2f).scaleY(2f)
                .setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithScaleUpWithMove() {
        val currentImageUri = Data.imageUris[Data.currentImageIndex]
        val nextImageUri = nextImageUri()
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(Data.animationDuration).start()
            Toast.makeText(requireContext(), "transition", Toast.LENGTH_SHORT).show()
            delay(Data.animationDuration)

            //reset current
            currentImageView.scaleX = 1f
            currentImageView.scaleY = 1f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f
            currentImageView.animate()
                .scaleX(2f)
                .scaleY(2f)
                .translationX(-screenWidth / 2.2f)
                .setDuration(Data.animationDuration * 2)
                .start()
            delay(Data.animationDuration * 2)

            coverImageView.setImageURI(nextImageUri)
            coverImageView.scaleX = 2f
            coverImageView.scaleY = 2f
            coverImageView.translationX = -screenWidth / 2.2f
            coverImageView.animate().alpha(1f).setDuration(Data.animationDuration).start()
            Toast.makeText(requireContext(), "transition", Toast.LENGTH_SHORT).show()
            delay(Data.animationDuration)

            currentImageView.setImageURI(nextImageUri)

            //reset cover shape
            coverImageView.alpha = 0f
            coverImageView.translationX = 0f

            currentImageView.animate().scaleX(1f).scaleY(1f).translationX(0f)
                .setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            Toast.makeText(requireContext(), "next", Toast.LENGTH_SHORT).show()

            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithMoveWithInitialZoom() {
        val currentImageUri = Data.imageUris[Data.currentImageIndex]
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 2f
            coverImageView.scaleY = 2f
            coverImageView.translationX = screenWidth / 4.2f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(Data.animationDuration).start()
            Toast.makeText(requireContext(), "transition", Toast.LENGTH_SHORT).show()
            delay(Data.animationDuration)

            //reset current
            currentImageView.scaleX = 2f
            currentImageView.scaleY = 2f
            currentImageView.translationX = screenWidth / 4.2f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f
            coverImageView.translationX = 0f
            currentImageView.animate().scaleX(2f).scaleY(2f).translationX(-screenWidth / 2.2f)
                .setDuration(Data.animationDuration * 2).start()
            delay(Data.animationDuration * 2 + 500)

            currentImageView.animate().scaleX(1f).scaleY(1f).translationX(0f)
                .setDuration(Data.animationDuration).start()
            delay(Data.animationDuration + 500)

            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithScaleDown() {
        val currentImageUri = Data.imageUris[Data.currentImageIndex]

        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //Init cover with shape
            coverImageView.scaleX = 2f
            coverImageView.scaleY = 2f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            //set current
            currentImageView.scaleX = 2f
            currentImageView.scaleY = 2f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f

            // Start the animation to scale down the currentImageView
            currentImageView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(Data.animationDuration)
                .start()

            // Delay before proceeding to the next transition or action
            delay(Data.animationDuration)

            // Show the next image
            showNextImage()
        }
    }

    private suspend fun transitionWithBlurry() {
        val currentImageUri = Data.imageUris[Data.currentImageIndex]

        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            //reset current
            currentImageView.scaleX = 1f
            currentImageView.scaleY = 1f
            currentImageView.translationX = 0f

            // previous image with blur transformation
            Glide.with(requireContext())
                .load(currentImageUri)
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
            coverImageView.animate().alpha(0f).setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            showNextImage()
        }
    }

    private suspend fun transitionWithScaleDownWithSlideInOut() {
        val currentImageUri = Data.imageUris[Data.currentImageIndex]
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()

        // Start the animation to fade out previousImageView
        coroutineScope.launch {

            //Init cover with shape
            coverImageView.scaleX = 2f
            coverImageView.scaleY = 2f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            //set current
            currentImageView.scaleX = 2f
            currentImageView.scaleY = 2f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f

            currentImageView.animate().scaleX(1f).scaleY(1f)
                .setDuration(Data.animationDuration / 2).start()
            delay(Data.animationDuration / 2)

            currentImageView.animate().translationX(screenWidth).setDuration(Data.animationDuration)
                .start()
            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithCollege() {
        val currentImageUri = Data.imageUris[Data.currentImageIndex]
        val collegeImageUri = Data.imageUris[(Data.currentImageIndex + 1) % Data.imageUris.size]
        val collegeImageUri_1 = Data.imageUris[(Data.currentImageIndex + 2) % Data.imageUris.size]
        val collegeImageUri_2 = Data.imageUris[(Data.currentImageIndex + 3) % Data.imageUris.size]
        val collegeImageUri_3 = Data.imageUris[(Data.currentImageIndex + 4) % Data.imageUris.size]

        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            //reset current
            currentImageView.scaleX = 1f
            currentImageView.scaleY = 1f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f

            collegeImageView.setImageURI(collegeImageUri)
            collegeImageView_1.setImageURI(collegeImageUri_1)
            collegeImageView_2.setImageURI(collegeImageUri_2)
            collegeImageView_3.setImageURI(collegeImageUri_3)

            //hide previous image view which is blurred
            currentImageView.animate().scaleX(0.3f).scaleY(0.3f)
                .translationX(screenWidth / -4)
                .translationY(screenHeight / -4)
                .rotation(-25f)
                .setDuration(Data.animationDuration)
                .start()
            delay(Data.animationDuration)

            collegeImageView.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                .translationX(screenWidth / 4)
                .translationY(screenHeight / 4)
                .rotation(25f)
                .setDuration(Data.animationDuration)
                .start()
            delay(Data.animationDuration)

            collegeImageView_1.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                .translationX(screenWidth / 5)
                .translationY(screenHeight / -5)
                .rotation(25f)
                .setDuration(Data.animationDuration)
                .start()
            delay(Data.animationDuration)

            collegeImageView_2.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                .translationX(screenWidth / -5)
                .translationY(screenHeight / 5)
                .rotation(-25f)
                .setDuration(Data.animationDuration)
                .start()
            delay(Data.animationDuration)

            collegeImageView_3.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                .setDuration(Data.animationDuration).start()
            delay(Data.animationDuration)

            //stay delay
            delay(Data.animationDuration / 2)

            //reverse animation
            collegeImageView_3.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f)
                .rotation(0f)
                .setDuration(Data.animationDuration)
                .start()
            delay(Data.animationDuration / 2)

            collegeImageView_2.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f)
                .rotation(0f)
                .setDuration(Data.animationDuration)
                .start()
            delay(Data.animationDuration / 2)

            collegeImageView_1.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f)
                .rotation(0f)
                .setDuration(Data.animationDuration)
                .start()
            delay(Data.animationDuration / 2)

            collegeImageView.animate().alpha(0f).scaleX(1f).scaleY(1f)
                .rotation(0f)
                .setDuration(Data.animationDuration)
                .start()
            delay(Data.animationDuration / 2)

            currentImageView.animate().alpha(1f).scaleX(2f).scaleY(2f)
                .rotation(0f)
                .setDuration(Data.animationDuration)
                .start()
            delay(Data.animationDuration / 2)
            currentImageView.animate().scaleX(1f).scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(Data.animationDuration)
                .start()
            delay(Data.animationDuration / 2)

            //reset college Image view
            collegeImageView.animate().alpha(0f).scaleX(1f).scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(0L)
                .start()
            collegeImageView_1.animate().alpha(0f).scaleX(1f).scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(0L)
                .start()
            collegeImageView_2.animate().alpha(0f).scaleX(1f).scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(0L)
                .start()
            collegeImageView_3.animate().alpha(0f).scaleX(1f).scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(0L)
                .start()

            //show next image
            showNextImage()
        }
    }

}