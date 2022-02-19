package com.jeffrwatts.kmmwebrtc

import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.SessionDescriptionType
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class FirebaseSignalingChannel (private val self: String, private val recipient: String) {
    companion object {
        const val session = "session"
        const val iceCandidates = "iceCandidates"
    }

    suspend fun sendIceCandidate(iceCandidate: IceCandidate) {
        val candidate = hashMapOf(
            "candidate" to iceCandidate.candidate,
            "sdpMid" to iceCandidate.sdpMid,
            "sdpMLineIndex" to iceCandidate.sdpMLineIndex
        )
        Firebase.firestore.collection(session)
            .document(recipient)
            .collection(iceCandidates)
            .document
            .set(candidate)
    }

    suspend fun sendSessionDescription(sessionDescription: SessionDescription) {
        // Need to explicitly break out into a String due to Serialization
        // error on iOS.
        val type = when (sessionDescription.type) {
            SessionDescriptionType.Offer -> "Offer"
            SessionDescriptionType.Answer -> "Answer"
            else -> ""
        }

        val sessionDesc = hashMapOf(
            "type" to type,
            "sdp" to sessionDescription.sdp
        )
        Firebase.firestore.collection(session)
            .document(recipient)
            .set(sessionDesc)
    }

    fun onSessionDescription(): Flow<SessionDescription> {
        return Firebase.firestore.collection(session).document(self).snapshots
            .filterNot { it.metadata.isFromCache }
            .filter { it.contains("type") && it.contains("sdp") }
            .map {
                // Assume Offer, unless its Answer.
                var type = SessionDescriptionType.Offer
                if (it.get<String>("type").contentEquals("Answer")) {
                    type = SessionDescriptionType.Answer
                }

                SessionDescription(type, it.get("sdp"))
            }
    }

    fun onIceCandidate(): Flow<List<IceCandidate>> {
        return Firebase.firestore.collection(session).document(self)
            .collection(iceCandidates).snapshots
            .filterNot { it.metadata.isFromCache }
            .map {
                buildList {
                    it.documents.forEach {
                        if (it.contains("candidate") && it.contains("sdpMid") && it.contains("sdpMLineIndex")) {
                            val candidate = it.get<String>("candidate")
                            val sdpMid = it.get<String>("sdpMid")
                            val sdpMLineIndex = it.get<Int>("sdpMLineIndex")
                            add(IceCandidate(candidate = candidate, sdpMid = sdpMid, sdpMLineIndex = sdpMLineIndex))
                        }
                    }
                }
            }
    }
}