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
        Button ("Run DogsApi") {
            viewModel.loadDogsApi()
        }
	}
}

extension ContentView {
    enum Dogs {
        case loading
        case result([Dog])
        case error(String)
    }
    
    class ViewModel: ObservableObject {
        @Published var dogs = Dogs.loading
        private let dogModel = DogModel()
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
        
        func loadDogsApi() {
            dogModel.getDogs(completionHandler: { dogs, error in
                if let dogs = dogs {
                    self.dogs = .result(dogs)
                } else {
                    self.dogs = .error(error?.localizedDescription ?? "error")
                }
            })
        }
    }
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}

extension Dog: Identifiable{}
