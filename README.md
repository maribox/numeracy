# Numeracy

Mental math training app built with Kotlin Multiplatform + Compose Multiplatform.

Practice mental math tricks for squaring, multiplication, poker odds, tip calculations, and more — with step-by-step breakdowns showing the formulas behind each shortcut.

## Build

```sh
./gradlew :composeApp:assembleDebug      # Android APK
./gradlew :composeApp:run                # Desktop (JVM)
```

## The model

`docs/model` holds every screen of the app: what each is for, the states it can be in, and a
picture of each state upright and wide, light and dark.

```sh
./make-renders.sh                # draw every screen into docs/model/img
./make-renders.sh practice       # draw one screen
viewbook --gaps docs/model       # states nothing renders; exits non-zero while any remain
viewbook docs/model              # read it
```

The renderer paints the real composables off-screen on the JVM with a written practice history and
questions from the real generators under a fixed seed, so a picture comes from the code the app
runs and the same picture is drawn twice.

## Releases

Fully automatic via conventional commits. Push to `master` and CI handles versioning + APK release:

- `feat: ...` → minor version bump
- `fix: ...` → patch version bump
- `feat!: ...` → major version bump
