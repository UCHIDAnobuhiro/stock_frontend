# Todo App (Kotlin / Jetpack Compose)

シンプルな **Todo 管理アプリ** です。  
Kotlin・Jetpack Compose・MVVM アーキテクチャを採用し、ログイン認証機能とタスクの追加・一覧表示を実装しています。

---

## 機能

- **ログイン機能**
    - Email / Password でログイン
    - JWT トークンによる認証
- **Todo 管理**
    - タスク一覧表示
    - タスク追加
- **ログアウト**
    - トークン破棄
    - ログイン画面へ遷移
- **非同期処理**
    - Kotlin Coroutines + Flow
- **ユニットテスト**
    - ViewModel / Repository のテストを実装

---

## 使用技術

### 言語・フレームワーク

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [AndroidX Navigation](https://developer.android.com/guide/navigation)
- [Material3](https://m3.material.io/)

### アーキテクチャ

- MVVM (Model - View - ViewModel)
- Repository パターン
- StateFlow / SharedFlow による状態管理

### ライブラリ

- [Retrofit](https://square.github.io/retrofit/) - API 通信
- [OkHttp](https://square.github.io/okhttp/) - 通信処理 & ログ
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) - JSON パース
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - トークン保存
- [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) - 非同期処理
- [Truth](https://truth.dev/) - テストアサーション
- [MockK](https://mockk.io/) - モックライブラリ
