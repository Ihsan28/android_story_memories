package com.ihsan.memorieswithimagevideo.fragments

interface AnimationRecordingCallbacks {
    fun onRecordingStarted()
    fun onRecordingStopped()
    fun onRecordingFailed()
    fun onFrameAvailable(imagePaths:List<String>)
    fun onExportReady()
    fun onExportStarted()
    fun onExportProgress(progressPercentage: Int)
    fun onExportFinished()
    fun onExportFailed(e: Exception)
}