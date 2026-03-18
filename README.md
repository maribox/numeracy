# Numeracy

Mental math training app built with Kotlin Multiplatform + Compose Multiplatform.

Practice mental math tricks for squaring, multiplication, poker odds, tip calculations, and more — with step-by-step breakdowns showing the formulas behind each shortcut.

## Build

```sh
./gradlew :composeApp:assembleDebug      # Android APK
./gradlew :composeApp:run                # Desktop (JVM)
```

## Releases

Fully automatic via conventional commits. Push to `master` and CI handles versioning + APK release:

- `feat: ...` → minor version bump
- `fix: ...` → patch version bump
- `feat!: ...` → major version bump
