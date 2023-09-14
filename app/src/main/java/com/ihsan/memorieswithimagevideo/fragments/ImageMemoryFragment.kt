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
import com.ihsan.memorieswithimagevideo.data.Data.Companion.animationDuration
import com.ihsan.memorieswithimagevideo.data.Data.Companion.contentUris
import com.ihsan.memorieswithimagevideo.data.Data.Companion.currentIndex
import com.ihsan.memorieswithimagevideo.data.Data.Companion.screenWidth
import jp.wasabeef.transformers.glide.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ImageMemoryFragment : Fragment() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var coverImageView: ImageView
    private lateinit var currentImageView: ImageView
    private lateinit var collageImageView: ImageView
    private lateinit var collageImageView_1: ImageView
    private lateinit var collageImageView_2: ImageView
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
        currentIndex = arguments?.getInt("index") ?: 0

        coverImageView = view.findViewById(R.id.coverImageView)
        currentImageView = view.findViewById(R.id.currentImageView)
        collageImageView = view.findViewById(R.id.collegeImageView)
        collageImageView_1 = view.findViewById(R.id.collegeImageView_1)
        collageImageView_2 = view.findViewById(R.id.collegeImageView_2)
        collegeImageView_3 = view.findViewById(R.id.collegeImageView_3)

        coroutineScope.launch {
            showNextImage()
        }
    }

    var i = 0;
    private suspend fun showNextImage() {
        if (contentUris.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % contentUris.size

            transitionWithScaleDownCollage()

            val animations = listOf("1", "2", "3", "4", "5", "6", "7","8","9")
            /*when (animations[i++ % animations.size]) {
                "1" -> {
                    Toast.makeText(requireContext(), "1", Toast.LENGTH_SHORT).show()
                    transitionWithScaleUpV2()
                }

                "2" -> {
                    Toast.makeText(requireContext(), "2", Toast.LENGTH_SHORT).show()
                    transitionWithScaleDownCollage()
                }

                "3" -> {
                    Toast.makeText(requireContext(), "3", Toast.LENGTH_SHORT).show()
                    transitionWithScaleUp()
                }

                "4" -> {
                    Toast.makeText(requireContext(), "4", Toast.LENGTH_SHORT).show()
                    transitionWithScaleDown()
                }

                "5" -> {
                    Toast.makeText(requireContext(), "5", Toast.LENGTH_SHORT).show()
                    transitionWithBlurry()
                }

                "6" -> {
                    Toast.makeText(requireContext(), "6", Toast.LENGTH_SHORT).show()
                    transitionWithScaleDownWithSlideInOut()
                }

                "7" -> {
                    Toast.makeText(requireContext(), "7", Toast.LENGTH_SHORT).show()
                    transitionWithCollege()
                }
                "8"->{
                    Toast.makeText(requireContext(), "8", Toast.LENGTH_SHORT).show()
                    transitionWithMoveWithInitialZoom()
                }
                "9" -> {
                    Toast.makeText(requireContext(), "9", Toast.LENGTH_SHORT).show()
                    transitionWithScaleUpWithMove()
                }
            }*/
        }
    }

    private fun nextImageUri(): Uri {
        return contentUris[++currentIndex % contentUris.size]
    }

    private fun setImageViewShapeWithPosition(imageView:ImageView, scaleX:Float = 1f, scaleY:Float = 1f, translationX:Float=0f, translationY:Float=0f){
        imageView.scaleX = scaleX
        imageView.scaleY = scaleY
        imageView.translationX = translationX
        imageView.translationY = translationY

    }

    private suspend fun transitionWithScaleUpV2() {
        val currentImageUri = contentUris[currentIndex]

        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            setImageViewShapeWithPosition(coverImageView)
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(animationDuration).start()
            delay(animationDuration)

            //reset current
            setImageViewShapeWithPosition(currentImageView)
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f

            // Start the scaling animation
            currentImageView.animate()
                .scaleXBy(0.5f)
                .scaleYBy(0.5f)
                .setDuration(animationDuration)
                .withEndAction {
                    coroutineScope.launch {
                        // Show the next image
                        showNextImage()
                    }
                }
                .start()
        }
    }

    private suspend fun transitionWithScaleDownCollage() {
        val currentImageUri = contentUris[currentIndex]
        val nextImageUri = contentUris[(++currentIndex) % contentUris.size]
        val previousImageUri = contentUris[(++currentIndex + contentUris.size) % contentUris.size]

        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            setImageViewShapeWithPosition(coverImageView)
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(animationDuration).start()
            delay(animationDuration)

            //reset
            setImageViewShapeWithPosition(collageImageView)
            setImageViewShapeWithPosition(collageImageView_1)
            setImageViewShapeWithPosition(currentImageView)
            //set current
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f

            collageImageView.setImageURI(nextImageUri)
            collageImageView_1.setImageURI(previousImageUri)

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
                .setDuration(animationDuration/2).withEndAction {

                    //transition enter and scale down to position
                    collageImageView.animate().alpha(1f)
                        .scaleX(0.5f).scaleY(0.5f)
                        .translationX(0f)
                        .setDuration(animationDuration/3).withEndAction {

                            //transition enter and scale down to position
                            collageImageView_1.animate().alpha(1f)
                                .scaleX(0.5f).scaleY(0.5f)
                                .translationX(screenWidth / 4)
                                .translationY(screenWidth / 4)
                                .setDuration(animationDuration/4).withEndAction {
                                    collageImageView.animate().alpha(0f)
                                        .setDuration(animationDuration)
                                        .start()
                                    collageImageView_1.animate().alpha(0f)
                                        .setDuration(animationDuration)
                                        .start()
                                    coroutineScope.launch {
                                        //show next image
                                        showNextImage()
                                    }
                                }
                                .start()
                        }
                        .start()
                }
                .start()
        }
    }

    private suspend fun transitionWithScaleUp() {
        val currentImageUri = contentUris[currentIndex]
        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(animationDuration).start()
            delay(animationDuration)

            //reset current
            currentImageView.scaleX = 1f
            currentImageView.scaleY = 1f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f

            currentImageView.animate().scaleX(2f).scaleY(2f)
                .setDuration(animationDuration).start()
            delay(animationDuration)

            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithScaleUpWithMove() {
        val currentImageUri = contentUris[currentIndex]
        val nextImageUri = nextImageUri()
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(animationDuration).start()
            Toast.makeText(requireContext(), "transition", Toast.LENGTH_SHORT).show()
            delay(animationDuration)

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
                .setDuration(animationDuration * 2)
                .start()
            delay(animationDuration * 2)

            coverImageView.setImageURI(nextImageUri)
            coverImageView.scaleX = 2f
            coverImageView.scaleY = 2f
            coverImageView.translationX = -screenWidth / 2.2f
            coverImageView.animate().alpha(1f).setDuration(animationDuration).start()
            Toast.makeText(requireContext(), "transition", Toast.LENGTH_SHORT).show()
            delay(animationDuration)

            currentImageView.setImageURI(nextImageUri)

            //reset cover shape
            coverImageView.alpha = 0f
            coverImageView.translationX = 0f

            currentImageView.animate().scaleX(1f).scaleY(1f).translationX(0f)
                .setDuration(animationDuration).start()
            delay(animationDuration)


            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithMoveWithInitialZoom() {
        val currentImageUri = contentUris[currentIndex]

        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 2f
            coverImageView.scaleY = 2f
            coverImageView.translationX = screenWidth / 4.2f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(animationDuration).start()
            Toast.makeText(requireContext(), "transition", Toast.LENGTH_SHORT).show()
            delay(animationDuration)

            //reset current
            currentImageView.scaleX = 2f
            currentImageView.scaleY = 2f
            currentImageView.translationX = screenWidth / 4.2f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f
            coverImageView.translationX = 0f
            currentImageView.animate().scaleX(2f).scaleY(2f).translationX(-screenWidth / 2.2f)
                .setDuration(animationDuration * 2).start()
            delay(animationDuration * 2 + 500)

            currentImageView.animate().scaleX(1f).scaleY(1f).translationX(0f)
                .setDuration(animationDuration).start()
            delay(animationDuration + 500)

            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithScaleDown() {
        val currentImageUri = contentUris[currentIndex]

        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //Init cover with shape
            coverImageView.scaleX = 2f
            coverImageView.scaleY = 2f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(animationDuration).start()
            delay(animationDuration)

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
                .setDuration(animationDuration)
                .start()

            // Delay before proceeding to the next transition or action
            delay(animationDuration)

            // Show the next image
            showNextImage()
        }
    }

    private suspend fun transitionWithBlurry() {
        val currentImageUri = contentUris[currentIndex]

        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(animationDuration).start()
            delay(animationDuration)

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
            coverImageView.animate().alpha(0f).setDuration(animationDuration).start()
            delay(animationDuration)

            showNextImage()
        }
    }

    private suspend fun transitionWithScaleDownWithSlideInOut() {
        val currentImageUri = contentUris[currentIndex]
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()

        // Start the animation to fade out previousImageView
        coroutineScope.launch {

            //Init cover with shape
            coverImageView.scaleX = 2f
            coverImageView.scaleY = 2f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(animationDuration).start()
            delay(animationDuration)

            //set current
            currentImageView.scaleX = 2f
            currentImageView.scaleY = 2f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f

            currentImageView.animate().scaleX(1f).scaleY(1f)
                .setDuration(animationDuration / 2).start()
            delay(animationDuration / 2)

            currentImageView.animate().translationX(screenWidth).setDuration(animationDuration)
                .start()
            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithCollege() {
        val currentImageUri = contentUris[currentIndex]
        val collegeImageUri = contentUris[(currentIndex + 1) % contentUris.size]
        val collegeImageUri_1 = contentUris[(currentIndex + 2) % contentUris.size]
        val collegeImageUri_2 = contentUris[(currentIndex + 3) % contentUris.size]
        val collegeImageUri_3 = contentUris[(currentIndex + 4) % contentUris.size]

        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(1f).setDuration(animationDuration).start()
            delay(animationDuration)

            //reset current
            currentImageView.scaleX = 1f
            currentImageView.scaleY = 1f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f

            collageImageView.setImageURI(collegeImageUri)
            collageImageView_1.setImageURI(collegeImageUri_1)
            collageImageView_2.setImageURI(collegeImageUri_2)
            collegeImageView_3.setImageURI(collegeImageUri_3)

            //hide previous image view which is blurred
            currentImageView.animate().scaleX(0.3f).scaleY(0.3f)
                .translationX(screenWidth / -4)
                .translationY(screenHeight / -4)
                .rotation(-25f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration)

            collageImageView.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                .translationX(screenWidth / 4)
                .translationY(screenHeight / 4)
                .rotation(25f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration)

            collageImageView_1.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                .translationX(screenWidth / 5)
                .translationY(screenHeight / -5)
                .rotation(25f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration)

            collageImageView_2.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                .translationX(screenWidth / -5)
                .translationY(screenHeight / 5)
                .rotation(-25f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration)

            collegeImageView_3.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                .setDuration(animationDuration).start()
            delay(animationDuration)

            //stay delay
            delay(animationDuration / 2)

            //reverse animation
            collegeImageView_3.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f)
                .rotation(0f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration / 2)

            collageImageView_2.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f)
                .rotation(0f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration / 2)

            collageImageView_1.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f)
                .rotation(0f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration / 2)

            collageImageView.animate().alpha(0f).scaleX(1f).scaleY(1f)
                .rotation(0f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration / 2)

            currentImageView.animate().alpha(1f).scaleX(2f).scaleY(2f)
                .rotation(0f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration / 2)
            currentImageView.animate().scaleX(1f).scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration / 2)

            //reset college Image view
            collageImageView.animate().alpha(0f).scaleX(1f).scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(0L)
                .start()
            collageImageView_1.animate().alpha(0f).scaleX(1f).scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(0L)
                .start()
            collageImageView_2.animate().alpha(0f).scaleX(1f).scaleY(1f)
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