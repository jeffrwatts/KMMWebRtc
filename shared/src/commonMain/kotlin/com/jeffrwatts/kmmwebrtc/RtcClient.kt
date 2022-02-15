package com.jeffrwatts.kmmwebrtc

import com.shepeliev.webrtckmp.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RtcClient (private val self: String, private val recipient: String) {

    var onRemoteVideoTrack: (VideoStreamTrack) -> Unit = {}
    var onIncomingCall: (SessionDescription) -> Unit = {}

    private val peerConnection: PeerConnection
    private var signalingChannel: FirebaseSignalingChannel = FirebaseSignalingChannel(self, recipient)
    private var coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        // Signaling Channel Callbacks.
        signalingChannel.onSessionDescription().onEach {
            onSessionDescription(it)
        }.launchIn(coroutineScope)
        signalingChannel.onIceCandidate().onEach {
            it.forEach {
                onIceCandidate(it)
            }
        }.launchIn(coroutineScope)

        // Set up Peer Connection
        val rtcConfiguration = RtcConfiguration()
        peerConnection = PeerConnection(rtcConfiguration)

        peerConnection.onIceCandidate.onEach {
            signalingChannel.sendIceCandidate(it)
        }.launchIn(coroutineScope)

        peerConnection.onTrack.onEach { trackEvent->
            trackEvent.track.takeIf { it?.kind == MediaStreamTrackKind.Video }?.also {
                onRemoteVideoTrack(it as VideoStreamTrack)
            }
        }.launchIn(coroutineScope)
    }

    private fun onIceCandidate(iceCandidate: IceCandidate) {
        peerConnection.addIceCandidate(iceCandidate)
    }

    private suspend fun onSessionDescription(sessionDescription: SessionDescription) {
        when (sessionDescription.type) {
            SessionDescriptionType.Offer -> {
                onIncomingCall(sessionDescription)
            }
            SessionDescriptionType.Answer -> {
                peerConnection.setRemoteDescription(sessionDescription)
            }
            else -> {}
        }
    }

    fun setLocalVideoTrack(mediaTrack: MediaStreamTrack) {
        peerConnection.addTrack(mediaTrack)
    }

    suspend fun makeCall(receiveVideo: Boolean, receiveAudio: Boolean) {
        val offerOptions = OfferAnswerOptions(offerToReceiveVideo = receiveVideo, offerToReceiveAudio = receiveAudio)
        val offer = peerConnection.createOffer(offerOptions)
        peerConnection.setLocalDescription(offer)
        signalingChannel.sendSessionDescription(offer)
    }

    suspend fun answerCall(offer: SessionDescription, receiveVideo: Boolean, receiveAudio: Boolean) {
        val answerOptions = OfferAnswerOptions(offerToReceiveVideo = receiveVideo, offerToReceiveAudio = receiveAudio)
        peerConnection.setRemoteDescription(offer)
        val answer = peerConnection.createAnswer(answerOptions)
        peerConnection.setLocalDescription(answer)
        signalingChannel.sendSessionDescription(answer)
    }

    fun endCall() {
        peerConnection.close()
    }
}