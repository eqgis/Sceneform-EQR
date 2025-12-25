

# Filament Integration Notes

> **Modified by:** [`Ikkyu_Tanyx_Eqgis`](https://github.com/eqgis)

This document records the **origin and source mapping** of resources under the current directory when integrating **Filament** into this project.

---

## Directory Mapping

| Local Directory     | Source in Filament Repository                                |
| ------------------- | ------------------------------------------------------------ |
| `common/`           | `filament-x.x.x/android/common`                              |
| `filament-android/` | `filament-x.x.x/android/filament-android/src/main/cpp`       |
| `filament-utils/`   | `filament-x.x.x/android/filament-utils-android/src/main/cpp` |
| `gltfio-android/`   | `filament-x.x.x/android/gltfio-android/src/main/cpp`         |
| `libs/`             | `filament-x.x.x/libs`                                        |
| `third_party/`      | `filament-x.x.x/third_party`                                 |

---

## Prebuilt Static Libraries

* **`filament-x.x.x-android-native`**

    * Contains prebuilt native libraries (`*.a`, `include`) for Android
    * Used via CMake as **IMPORTED** libraries

---

## Notes on Header Files

The include directory provided by **Filament-Android-Native** is **incomplete**.

Some required header files are **not included** in the prebuilt package and must be manually copied from the Filament source repository:

```
filament/
└── filament/include/
```

These headers are required to ensure successful compilation when linking against the prebuilt native libraries.
