import SwiftUI
import CoreLocation

struct ContentView: View {
    @StateObject private var model = SampleViewModel()

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                Text("Dhruva iOS Sample")
                    .font(.title2).bold()

                if let one = model.oneShot {
                    Text("One-shot: \(one.latitude), \(one.longitude)")
                }

                if let stream = model.streaming {
                    Text("Streaming: \(stream.latitude), \(stream.longitude)")
                }

                Text("Status: \(model.status)")

                Button("Get Once") { model.getOnce() }
                    .buttonStyle(.borderedProminent)

                Button(model.streamingActive ? "Stop Stream" : "Start Stream") { model.toggleStream() }
                    .buttonStyle(.bordered)

                Button("Request Permission") { model.requestPermission() }
                    .buttonStyle(.bordered)

                Spacer()
            }
            .padding()
            .navigationTitle("Dhruva")
        }
    }
}

@MainActor
final class SampleViewModel: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published var oneShot: CLLocationCoordinate2D?
    @Published var streaming: CLLocationCoordinate2D?
    @Published var status: String = "Idle"
    @Published var streamingActive: Bool = false

    private let manager = CLLocationManager()

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }

    func getOnce() {
        status = "Asking..."
        manager.requestLocation()
    }

    func toggleStream() {
        if streamingActive {
            manager.stopUpdatingLocation()
            streamingActive = false
            status = "Stopped"
        } else {
            manager.startUpdatingLocation()
            streamingActive = true
            status = "Streaming"
        }
    }

    func requestPermission() {
        manager.requestWhenInUseAuthorization()
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let last = locations.last else { return }
        Task { @MainActor in
            if self.streamingActive {
                self.streaming = last.coordinate
            } else {
                self.oneShot = last.coordinate
                self.status = "Got fix"
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            self.status = "Failed: \(error.localizedDescription)"
        }
    }
}
