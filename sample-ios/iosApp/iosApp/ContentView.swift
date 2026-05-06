import SwiftUI
import UIKit
import DhruvaSample

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        IosEntryKt.SampleViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
