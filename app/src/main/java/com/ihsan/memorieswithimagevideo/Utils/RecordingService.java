package com.ihsan.memorieswithimagevideo.Utils;

class RecordingService : Service(){
        override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Start the MediaProjection & MediaRecorder here
        startRecording()

        // Use a notification to keep the service in the foreground
        val notification = Notification.Builder(this)
        .setContentTitle("Screen Recording")
        .setContentText("Recording in progress...")
        .setSmallIcon(R.drawable.ic_recording)
        .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
        }

        override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        }

        override fun onBind(intent: Intent?): IBinder? {
        return null
        }
        }

