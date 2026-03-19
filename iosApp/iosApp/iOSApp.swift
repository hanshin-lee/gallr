import SwiftUI
import NMapsMap

@main
struct iOSApp: App {
    init() {
        NMFAuthManager.shared().ncpKeyId = "dkd2c8bh63"
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
