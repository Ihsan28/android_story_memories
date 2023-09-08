package com.ihsan.memorieswithimagevideo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import jp.wasabeef.transformers.glide.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var imageUris: MutableList<Uri> = mutableListOf()
    private var currentImageIndex = 0

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var coverImageView: ImageView
    private lateinit var currentImageView: ImageView
    private lateinit var collegeImageView: ImageView
    private lateinit var collegeImageView_1: ImageView
    private lateinit var collegeImageView_2: ImageView
    private lateinit var collegeImageView_3: ImageView


    //pick image launcher contract for image picker intent
    private val pickImageContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val clipData = result.data!!.clipData
                imageUris.clear()
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri
                        imageUris.add(imageUri)
                    }
                    coroutineScope.launch {
                        showNextImage()
                    }
                } else {
                    val imageUri = result.data!!.data
                    if (imageUri != null) {
                        imageUris.clear()
                        imageUris.add(imageUri)
                    }
                    coroutineScope.launch {
                        showNextImage()
                    }
                }

                // Ensure there are at least 2 images and at most 5 images
                if (imageUris.size > 5) {
                    imageUris = imageUris.subList(0, 5)
                } else if (imageUris.size < 2) {
                    // Handle the case when less than 2 images are selected
                    // Show a message or take appropriate action
                    return@registerForActivityResult
                }
                currentImageIndex = 0
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        coverImageView = findViewById(R.id.coverImageView)
        currentImageView = findViewById(R.id.currentImageView)
        collegeImageView = findViewById(R.id.collegeImageView)
        collegeImageView_1 = findViewById(R.id.collegeImageView_1)
        collegeImageView_2 = findViewById(R.id.collegeImageView_2)
        collegeImageView_3 = findViewById(R.id.collegeImageView_3)

        val pickImageButton: Button = findViewById(R.id.pickImageButton)
        pickImageButton.setOnClickListener {
            pickImages()
        }
    }

    private fun pickImages() {
        val pickImageIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickImageContract.launch(pickImageIntent)
    }

    var i = 0;
    private suspend fun showNextImage() {
        if (imageUris.isNotEmpty()) {
            currentImageIndex = (currentImageIndex + 1) % imageUris.size
            transitionWithScaleUpWithMove()
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
        return imageUris[++currentImageIndex % imageUris.size]
    }

    private suspend fun transitionWithScaleUpV2() {
        val currentImageUri = imageUris[currentImageIndex]

        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(  1f).setDuration(animationDuration).start()
            delay(animationDuration)

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
                .setDuration(animationDuration)
                .withEndAction {
                    // After scaling animation, return to normal scale
                    currentImageView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(animationDuration)
                        .start()
                    coroutineScope.launch {
                        // Delay before transitioning to the next image
                        delay(animationDuration)

                        // Show the next image
                        showNextImage()
                    }
                }
                .start()
        }
    }

    private suspend fun transitionWithScaleDownCollage() {
        val currentImageUri = imageUris[currentImageIndex]
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val nextImageUri = imageUris[(currentImageIndex + 1) % imageUris.size]
        val previousImageUri = imageUris[(currentImageIndex - 1 + imageUris.size) % imageUris.size]

        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(  1f).setDuration(animationDuration).start()
            delay(animationDuration)

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
                .setDuration(animationDuration).start()
            delay(animationDuration)

            //set other views out of screen
            collegeImageView.animate().translationX(screenWidth).setDuration(0)
                .start()
            collegeImageView_1.animate().translationX(screenWidth).setDuration(0)
                .start()

            //transition enter and scale down to position
            collegeImageView.animate().alpha(1f).scaleX(0.5f).scaleY(0.5f).translationX(0f)
                .setDuration(animationDuration).start()
            delay(animationDuration)

            collegeImageView_1.animate().alpha(1f).scaleX(0.5f).scaleY(0.5f)
                .translationX(screenWidth / 4).translationY(screenWidth / 4)
                .setDuration(animationDuration).start()
            delay(animationDuration)

            //stay delay
            delay(animationDuration)

            //reverse animation
            collegeImageView_1.animate().alpha(0f).scaleX(1f).scaleY(1f).translationX(screenWidth)
                .setDuration(animationDuration / 2).start()
            delay(animationDuration / 2)

            collegeImageView.animate().alpha(0f).scaleX(1f).scaleY(1f).translationX(screenWidth)
                .setDuration(animationDuration / 2).start()
            delay(animationDuration / 2)

            currentImageView.animate().scaleX(1f).scaleY(1f).translationX(0f).translationY(0f)
                .setDuration(animationDuration / 2).start()
            delay(animationDuration / 2)

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
        val currentImageUri = imageUris[currentImageIndex]
        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(  1f).setDuration(animationDuration).start()
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
        val currentImageUri = imageUris[currentImageIndex]
        val nextImageUri=nextImageUri()
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(  1f).setDuration(animationDuration).start()
            Toast.makeText(this@MainActivity, "transition", Toast.LENGTH_SHORT).show()
            delay(animationDuration)

            //reset current
            currentImageView.scaleX = 1f
            currentImageView.scaleY = 1f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f
            currentImageView.animate().scaleX(2f).scaleY(2f).translationX(-screenWidth/2.2f)
                .setDuration(animationDuration).start()
            delay(animationDuration)

            coverImageView.setImageURI(nextImageUri)
            coverImageView.scaleX = 2f
            coverImageView.scaleY = 2f
            coverImageView.translationX=screenWidth/2.2f
            coverImageView.animate().alpha(  1f).setDuration(animationDuration).start()
            Toast.makeText(this@MainActivity, "transition", Toast.LENGTH_SHORT).show()
            delay(animationDuration)

            currentImageView.setImageURI(nextImageUri)

            //reset cover shape
            coverImageView.alpha = 0f
            /*coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f*/
            coverImageView.translationX=0f

            currentImageView.animate().scaleX(1f).scaleY(1f).translationX(0f)
                .setDuration(animationDuration).start()
            delay(animationDuration)

            Toast.makeText(this@MainActivity, "next", Toast.LENGTH_SHORT).show()

            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithScaleUpWithMoveWithInitialZoom() {
        val currentImageUri = imageUris[currentImageIndex]
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1.5f
            coverImageView.scaleY = 1.5f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(  1f).setDuration(animationDuration).start()
            Toast.makeText(this@MainActivity, "transition", Toast.LENGTH_SHORT).show()
            delay(animationDuration)

            //reset current
            currentImageView.scaleX = 1.5f
            currentImageView.scaleY = 1.5f
            currentImageView.translationX = 0f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f
            currentImageView.animate().scaleX(2f).scaleY(2f).translationX(-screenWidth/2.2f)
                .setDuration(animationDuration).start()
            delay(animationDuration)

            Toast.makeText(this@MainActivity, "next", Toast.LENGTH_SHORT).show()

            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithMoveWithInitialZoom() {
        val currentImageUri = imageUris[currentImageIndex]
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        // Start the animation to fade out previousImageView
        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 2f
            coverImageView.scaleY = 2f
            coverImageView.translationX=screenWidth/4.2f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(  1f).setDuration(animationDuration).start()
            Toast.makeText(this@MainActivity, "transition", Toast.LENGTH_SHORT).show()
            delay(animationDuration)

            //reset current
            currentImageView.scaleX = 2f
            currentImageView.scaleY = 2f
            currentImageView.translationX = screenWidth/4.2f
            currentImageView.setImageURI(currentImageUri)

            // show current image
            coverImageView.alpha = 0f
            currentImageView.animate().scaleX(2f).scaleY(2f).translationX(-screenWidth/2.2f)
                .setDuration(animationDuration).start()
            delay(animationDuration)

            currentImageView.animate().scaleX(1f).scaleY(1f).translationX(0f)
                .setDuration(animationDuration).start()
            delay(animationDuration)

            Toast.makeText(this@MainActivity, "next", Toast.LENGTH_SHORT).show()

            //show next image
            showNextImage()
        }
    }

    private suspend fun transitionWithScaleDown() {
        val currentImageUri = imageUris[currentImageIndex]

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
        val currentImageUri = imageUris[currentImageIndex]

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
            Glide.with(applicationContext)
                .load(currentImageUri)
                .apply(
                    bitmapTransform(
                        MultiTransformation(
                            /*CropCenterTransformation(),*/
                            BlurTransformation(this@MainActivity, 15, sampling = 1)
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
        val currentImageUri = imageUris[currentImageIndex]
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
        val currentImageUri = imageUris[currentImageIndex]
        val collegeImageUri = imageUris[(currentImageIndex + 1) % imageUris.size]
        val collegeImageUri_1 = imageUris[(currentImageIndex + 2) % imageUris.size]
        val collegeImageUri_2 = imageUris[(currentImageIndex + 3) % imageUris.size]
        val collegeImageUri_3 = imageUris[(currentImageIndex + 4) % imageUris.size]

        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        coroutineScope.launch {
            //reset cover shape
            coverImageView.scaleX = 1f
            coverImageView.scaleY = 1f
            coverImageView.setImageURI(currentImageUri)
            coverImageView.animate().alpha(  1f).setDuration(animationDuration).start()
            delay(animationDuration)

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
                .setDuration(animationDuration)
                .start()
            delay(animationDuration)

            collegeImageView.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                .translationX(screenWidth / 4)
                .translationY(screenHeight / 4)
                .rotation(25f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration)

            collegeImageView_1.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
                .translationX(screenWidth / 5)
                .translationY(screenHeight / -5)
                .rotation(25f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration)

            collegeImageView_2.animate().alpha(1f).scaleX(0.3f).scaleY(0.3f)
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

            collegeImageView_2.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f)
                .rotation(0f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration / 2)

            collegeImageView_1.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f)
                .rotation(0f)
                .setDuration(animationDuration)
                .start()
            delay(animationDuration / 2)

            collegeImageView.animate().alpha(0f).scaleX(1f).scaleY(1f)
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

    companion object {
        private const val animationDuration = 3000L
    }
}
