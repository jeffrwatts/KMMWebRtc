import SwiftUI
import shared
import WebRTC

struct ContentView: View {
    private let viewModel = ViewModel()

	var body: some View {
        CameraView(videoView: viewModel.cameraVideo)

        Button("Start Video") {
            viewModel.startVideo()
        }
        Button ("Stop Video") {
            viewModel.stopVideo()
        }
        Button ("Toggle Camera") {
            viewModel.toggleCamera()
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
        @Published var cameraVideo = RTCEAGLVideoView()
        private let mediaDevices = MediaDevicesCompanion()
        private var stream: MediaStream?
        
        func startVideo () {
            mediaDevices.getUserMedia(audio: false, video: true, completionHandler: { stream, error in
                if let videoTrack = stream?.videoTracks.first {
                    self.stream = stream
                    videoTrack.addRenderer(renderer: self.cameraVideo)
                } else if let errorText = error?.localizedDescription {
                    NSLog("Local video error: \(errorText)")
                } else {
                    NSLog("Unexpected.")
                }
            })
        }
        
        func stopVideo () {
            stream?.videoTracks.first?.removeRenderer(renderer: cameraVideo)
            stream?.release()
        }
        
        func toggleCamera() {
            stream?.videoTracks.forEach { $0.switchCamera(deviceId: nil, completionHandler: {_, _ in }) }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
