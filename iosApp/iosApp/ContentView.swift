import SwiftUI
import shared
import WebRTC

struct ContentView: View {
    private let viewModel = ViewModel()

	var body: some View {
        CameraView(videoView: viewModel.remoteVideo)
        CameraView(videoView: viewModel.localVideo)

        Button("Start Video") {
            viewModel.startVideo()
        }
        Button("Stop Video") {
            viewModel.stopVideo()
        }
        Button ("Toggle Camera") {
            viewModel.toggleCamera()
        }
        Button ("Call") {
            viewModel.call()
        }
        Button ("Answer") {
            viewModel.answer()
        }
	}
}

struct CameraView : UIViewRepresentable {
    var videoView: RTCEAGLVideoView
    func updateUIView(_ uiView: UIViewType, context: Context) {
    }
    func makeUIView(context: Context) -> some RTCEAGLVideoView {
        self.videoView.frame = CGRect()
        return self.videoView
    }
}

extension ContentView {
    class ViewModel: ObservableObject {
        @Published var localVideo = RTCEAGLVideoView()
        @Published var remoteVideo = RTCEAGLVideoView()
        private var rtcClient: RtcClient? = nil
        private let mediaDevices = MediaDevicesCompanion()
        private var stream: MediaStream? = nil
        private var receivedOffer: SessionDescription? = nil
        
        init() {
            rtcClient = RtcClient(self: "ios", recipient: "android")
            rtcClient?.onIncomingCall = { offer in
                self.receivedOffer = offer
            }
            rtcClient?.onRemoteVideoTrack = { videoTrack in
                videoTrack.addRenderer(renderer: self.remoteVideo)
            }
        }
        
        func startVideo () {
            mediaDevices.getUserMedia(audio: false, video: true, completionHandler: { stream, error in
                if let videoTrack = stream?.videoTracks.first {
                    self.stream = stream
                    videoTrack.addRenderer(renderer: self.localVideo)
                    self.rtcClient?.setLocalVideoTrack(mediaTrack: videoTrack)
                } else if let errorText = error?.localizedDescription {
                    NSLog("Local video error: \(errorText)")
                } else {
                    NSLog("Unexpected.")
                }
            })
        }
        
        func stopVideo () {
            stream?.videoTracks.first?.removeRenderer(renderer: localVideo)
            stream?.release()
        }
        
        func toggleCamera() {
            stream?.videoTracks.forEach { $0.switchCamera(deviceId: nil, completionHandler: {_, _ in }) }
        }
        
        func call() {
            rtcClient?.makeCall(receiveVideo: true, receiveAudio: false, completionHandler: {_, _ in })
        }
        
        func answer() {
            rtcClient?.answerCall(offer: receivedOffer!, receiveVideo: true, receiveAudio: false, completionHandler: {_, _ in})
        }
    }
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
