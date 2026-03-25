import XCTest

/// UI tests that capture App Store screenshots via fastlane snapshot.
///
/// Compose Multiplatform exposes Compose semantics to the iOS accessibility tree:
///   - Bottom nav tabs → Button: "FEATURED", "LIST", "MAP"
///   - Bookmark icons  → Button: "Add bookmark" / "Remove bookmark"
///   - Segment labels  → StaticText: "All Exhibitions" / "My List"
///   - Map mode buttons → selectable Other/Button: "MYLIST" / "ALL"
///   - Exhibition cards → tappable Other with child StaticTexts
///
/// Run via: `cd iosApp && fastlane screenshots`
@MainActor
class ScreenshotTests: XCTestCase {

    let app = XCUIApplication()

    override func setUpWithError() throws {
        continueAfterFailure = false

        // Reset app state (clears DataStore bookmarks from prior runs)
        app.launchArguments = ["-AppleLanguages", "(en-US)"]
        setupSnapshot(app)
        app.resetAuthorizationStatus(for: .location)
        app.launch()

        // Clear any persisted bookmarks from previous test runs
        clearAllBookmarks()

        sleep(6) // wait for Supabase data load
    }

    // MARK: - Screenshot Sequence

    /// 1. Featured tab with one exhibition bookmarked
    func test01_FeaturedScreen() {
        bookmarkFirstExhibition()
        snapshot("01_Featured")
    }

    /// 2. List tab > All Exhibitions
    func test02_ListAllExhibitions() {
        app.buttons["LIST"].tap()
        sleep(2)
        // "All Exhibitions" is the default left segment — tap to ensure
        tapText("All Exhibitions", fallback: "전체 전시")
        sleep(1)
        snapshot("02_List_AllExhibitions")
    }

    /// 3. List tab > My List (showing bookmarked exhibition)
    func test03_ListMyList() {
        bookmarkFirstExhibition()
        app.buttons["LIST"].tap()
        sleep(2)
        tapText("My List", fallback: "내 리스트")
        sleep(1)
        snapshot("03_List_MyList")
    }

    /// 4. Map tab > ALL mode
    func test04_MapAll() {
        app.buttons["MAP"].tap()
        sleep(3)
        tapText("ALL", fallback: "전체")
        sleep(2)
        snapshot("04_Map_All")
    }

    /// 5. Map tab > MYLIST mode (bookmarked pins only)
    func test05_MapMyList() {
        bookmarkFirstExhibition()
        app.buttons["MAP"].tap()
        sleep(3)
        tapText("MYLIST", fallback: "내 목록")
        sleep(2)
        snapshot("05_Map_MyList")
    }

    /// 6. Exhibition detail
    func test06_ExhibitionDetail() {
        // Tap the first exhibition title to open detail
        let firstTitle = app.staticTexts["El Anatsui: LuwVor"]
        if firstTitle.waitForExistence(timeout: 3) {
            firstTitle.tap()
        } else {
            // Fallback: tap first visible exhibition text
            app.staticTexts.element(boundBy: 5).tap()
        }
        sleep(2)
        snapshot("06_ExhibitionDetail")
    }

    // MARK: - Helpers

    /// Ensure exactly one exhibition is bookmarked on the Featured screen.
    /// First removes all existing bookmarks (persisted via DataStore across launches),
    /// then bookmarks only the first exhibition.
    private func bookmarkFirstExhibition() {
        // Step 1: Remove all existing bookmarks
        clearAllBookmarks()

        // Step 2: Bookmark the first exhibition
        let firstBookmark = app.buttons.matching(identifier: "Add bookmark").element(boundBy: 0)
        if firstBookmark.waitForExistence(timeout: 3) {
            firstBookmark.tap()
            sleep(1)
        }
    }

    /// Remove all existing bookmarks by tapping every "Remove bookmark" button.
    private func clearAllBookmarks() {
        // Tap up to 20 remove buttons (safety limit to avoid infinite loop)
        for _ in 0..<20 {
            let btn = app.buttons.matching(identifier: "Remove bookmark").element(boundBy: 0)
            guard btn.waitForExistence(timeout: 1) else { break }
            btn.tap()
            sleep(1)
        }
    }

    /// Tap a StaticText by label with a Korean fallback.
    private func tapText(_ label: String, fallback: String) {
        let primary = app.staticTexts[label]
        if primary.waitForExistence(timeout: 3) {
            primary.tap()
            return
        }
        let secondary = app.staticTexts[fallback]
        if secondary.waitForExistence(timeout: 2) {
            secondary.tap()
            return
        }
        // Try as button
        let btn = app.buttons[label]
        if btn.waitForExistence(timeout: 2) {
            btn.tap()
            return
        }
        // Try any descendant
        let pred = NSPredicate(format: "label == %@", label)
        let match = app.descendants(matching: .any).matching(pred).firstMatch
        if match.waitForExistence(timeout: 2) {
            match.tap()
        }
    }
}
