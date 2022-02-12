import SwiftUI
import shared

struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel
    
    var body: some View {
        NavigationView {
            listView()
            .navigationBarTitle("Dogs Api")
            .navigationBarItems(trailing:
                Button("With Firebase") {
                self.viewModel.loadDogsFirebase()
            })
        }
    }
    
    private func listView() -> AnyView {
        switch viewModel.dogs {
        case .loading:
            return AnyView(Text("Loading...").multilineTextAlignment(.center))
        case .error(let description) :
            return AnyView(Text(description).multilineTextAlignment(.center))
        case .result(let dogs):
            return AnyView(List(dogs) {dog in
                AnyView(Text(dog.name))
            })
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
        let dogModel: DogModel
        let firebaseSignalingChannel: FirebaseSignalingChannel
        
        @Published var dogs = Dogs.loading
    
        init () {
            self.dogModel = DogModel()
            self.firebaseSignalingChannel = FirebaseSignalingChannel()
            self.loadDogsApi()
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
        
        func loadDogsFirebase() {
            firebaseSignalingChannel.getDogs(completionHandler: {dogs, error in
                if let dogs = dogs {
                    self.dogs = .result(dogs)
                } else {
                    self.dogs = .error(error?.localizedDescription ?? "error")
                }
            })
        }
    }
}

extension Dog: Identifiable{}
