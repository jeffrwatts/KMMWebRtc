import SwiftUI
import shared
import WebRTC

struct ContentView: View {
	let greet = Greeting().greeting()
    private let viewModel = ViewModel()

	var body: some View {
		Text(greet)
        Button("Start Video") {
            viewModel.startVideo()
        }
        Button ("Stop Video") {
            viewModel.stopVideo()
        }
	}
}

extension ContentView {
    class ViewModel: ObservableObject {
        private let mediaDevices = MediaDevicesCompanion()
        private var stream: MediaStream?
        var videoView = RTCMTLVideoView()
        
        func startVideo () {
            mediaDevices.getUserMedia(audio: false, video: true, completionHandler: { stream, error in
                if let videoTrack = stream?.videoTracks.first {
                    self.stream = stream
                    videoTrack.addRenderer(renderer: self.videoView)
                } else if let errorText = error?.localizedDescription {
                    NSLog("Local video error: \(errorText)")
                } else {
                    NSLog("Unexpected.")
                }
            })
        }
        
        func stopVideo () {
            stream?.videoTracks.first?.removeRenderer(renderer: videoView)
            stream?.release()
        }
        
        func toggleCamera() {
            
        }
    }
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
