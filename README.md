# 📈 Stock View App (Kotlin / Jetpack Compose)

## 🧭 概要

**シンプルで軽量な株価チャートアプリケーション**
日足、週足、月足の期間で米国株のローソク足チャートを閲覧できる直感的なアプリ。
Kotlin、Jetpack Compose、MVVMアーキテクチャで構築され、**ログイン認証**と**株価表示**機能を搭載。

<p>
  <video src="https://github.com/user-attachments/assets/fd6fcc0c-c268-4e54-8930-1a236458c454" width="70%" controls></video>
</p>

---

## ⚙️ 主な機能

### 🔐 認証

- メール / パスワードでログイン
- JWTトークンベースの認証
- ログアウト時にトークン破棄しログイン画面へ遷移

### 💹 株価チャート表示

- 日足、週足、月足の期間で米国株のローソク足を表示
- 出来高バーとチャートの同期表示
- 期間切り替え時のスムーズなアニメーション

### ⚡ 非同期処理

- Kotlin Coroutines + Flowによるリアクティブなデータハンドリング
- APIレスポンスの非同期取得とエラーハンドリング

### 🧪 テスト

- ViewModel / Repository層のユニットテスト
- MockK + CoroutineTestRuleによるテスト環境

---

## 🛠️ 技術スタック

### 🧩 使用技術

- **言語**: Kotlin
- **UIフレームワーク**: Jetpack Compose
- **アーキテクチャ**: MVVM (Model-View-ViewModel)
- **非同期処理**: Kotlin Coroutines / Flow
- **HTTP通信**: Retrofit2 + OkHttp3
- **JSONシリアライズ**: Kotlinx Serialization
- **チャート描画**: MPAndroidChart
- **認証**: JWT (JSON Web Token)
- **テスト**: JUnit4 / MockK / CoroutineTestRule
- **DI**: Hilt
- **ビルド管理**: Gradle (KTS)
- **バージョン管理**: Git / GitHub

---

### 🧠 追加詳細

- **UIデザイン**: Material Design 3ベースのCompose UIコンポーネントを使用
- **状態管理**: ViewModel + State + Flowによるリアクティブなui更新
- **データ層**: RepositoryパターンでAPI通信とキャッシュを分離
- **設計思想**: MVVMベースで、シンプルかつテストしやすい構造を重視
- **API連携**: Go (Gin) + Cloud Runバックエンドと通信
- **環境変数**: BuildConfig経由でAPI Base URLを切り替え（debug / release）

---

## 🏗️ アーキテクチャ

### 🧩 設計思想

- **アーキテクチャパターン**: MVVM (Model - View - ViewModel)
- **依存関係の方向**: UI → ViewModel → Repository → DataSource
- **目的**: UIロジックとデータ処理を分離し、テスト容易性と保守性を向上

---

### 🧱 レイヤー責務

#### 🖥️ UI層 (Jetpack Compose)

- 画面描画とユーザー入力を担当
- `State`や`Flow`を監視し、UIを自動更新
- ユーザー操作を`ViewModel`にイベントとして通知

#### ⚙️ ViewModel層

- アプリの状態と画面ロジックを管理
- `Repository`からデータを取得し、UI表示用に変換
- ローディング、成功、失敗などの状態を`UiState`として保持

#### 🗂️ Repository層

- データ取得の統一的な中間層
- `RemoteDataSource`（API通信）からデータを取得
- DTO → UIモデルへの変換を行い、ViewModelに返却

#### 🌐 DataSource層

- **RemoteDataSource**: `Retrofit` + `OkHttp`によるAPI通信
  JWTトークンをヘッダーに付与し、株価データを取得
- **LocalDataSource（オプション）**: 今後Roomによるキャッシュ機能を拡張予定

---

### 🔁 データフロー（例：株価チャート表示）

1. ユーザーが期間（日足 / 週足 / 月足）を選択
2. **UI → ViewModel**: `viewModel.load(code, interval, outputsize)`を呼び出し
3. **ViewModel → Repository**: `viewModelScope.launch`内で`repo.fetchCandles(...)`を実行
4. RepositoryがAPI経由でデータを取得し、UIデータに変換
5. ViewModelが内部状態を更新
6. UIが状態変化を検知し、チャートを再描画

---

### 🔓 ログアウトフロー

```text
ユーザーがログアウトボタンをタップ
       ↓
Screen (StockListScreen / ChartScreen)
       ↓ onLogout コールバック
NavGraph
       ↓ logoutViewModel.logout()
LogoutViewModel
       ↓ repo.logout() [IOディスパッチャ]
AuthRepository
       ├─ tokenProvider.clear()  ← インメモリトークンをクリア
       └─ tokenStore.clear()     ← DataStoreトークンをクリア
       ↓
LogoutViewModel
       ↓ emit(UiEvent.LoggedOut)
NavGraph (LaunchedEffect)
       ↓ イベントを収集
ログイン画面へ遷移（バックスタッククリア）
```

**関心の分離:**

- `LogoutViewModel`: ログアウトのビジネスロジックを処理
- `NavGraph`: ナビゲーションのみを担当（イベントをリッスンして遷移）
- `AuthRepository`: データ層（トークン管理）

---

### 🚀 今後の改善

- Roomによるローカルキャッシュ
- UseCaseレイヤーの追加による関心の分離（クリーンアーキテクチャ）

## 📂 ディレクトリ構成

このプロジェクトは**機能ベース**のパッケージ構成を採用。
関連するコンポーネント（data、UI、ViewModel）を機能ごとにグループ化することで、
モジュール性と保守性を向上。

```text
app/
└── src/main/java/com/example/stock/
    ├── feature/                 # 機能モジュール
    │   ├── auth/                # 認証機能
    │   │   ├── data/
    │   │   │   ├── remote/      # AuthApi (Retrofit), AuthDto (リクエスト/レスポンスDTO)
    │   │   │   └── repository/  # AuthRepository
    │   │   ├── ui/
    │   │   │   ├── login/       # LoginScreen, LoginUiState
    │   │   │   └── signup/      # SignupScreen, SignupUiState
    │   │   └── viewmodel/       # LoginViewModel, LogoutViewModel, SignupViewModel,
    │   │                        # InputValidator, ErrorHandler
    │   ├── stocklist/           # 銘柄一覧機能
    │   │   ├── data/
    │   │   │   ├── remote/      # SymbolApi (Retrofit), SymbolDto
    │   │   │   └── repository/  # SymbolRepository
    │   │   ├── domain/
    │   │   │   └── model/       # Symbol (ドメインエンティティ)
    │   │   ├── ui/              # SymbolListScreen, SymbolUiState, SymbolItem
    │   │   └── viewmodel/       # SymbolViewModel
    │   └── chart/               # チャート表示機能
    │       ├── data/
    │       │   ├── remote/      # ChartApi (Retrofit), CandleDto
    │       │   └── repository/  # CandleRepository
    │       ├── domain/
    │       │   └── model/       # Candle (ドメインエンティティ)
    │       ├── ui/              # ChartScreen, CandleUiState, CandleItem
    │       │   └── chart/       # CandleChartView, VolumeChartView, ChartSync,
    │       │                    # SyncChartsOnce, ChartStyle, ChartToken, ChartUtils
    │       └── viewmodel/       # CandlesViewModel
    ├── core/                    # コアコンポーネント
    │   ├── data/                # 共有データコンポーネント
    │   │   ├── auth/            # TokenProvider, InMemoryTokenProvider, AuthEventManager
    │   │   └── local/           # TokenStore (DataStore)
    │   ├── network/             # AuthInterceptor, TokenAuthenticator
    │   ├── ui/                  # 共通UI
    │   │   ├── component/       # 再利用可能なComposable
    │   │   ├── theme/           # Material3テーマ、タイポグラフィ
    │   │   └── util/            # ClickGuardなどのユーティリティ
    │   └── util/                # DispatcherProvider
    ├── config/                  # ApiConfig (BASE_URL設定)
    ├── di/                      # Hiltモジュール (DataModule, NetworkModule, DispatcherModule)
    └── navigation/              # AppNavGraph, Routes
```

### 📦 機能モジュール構成

各機能モジュール（`auth`、`stocklist`、`chart`）は以下で構成：

- **data/remote/** - ネットワーク通信用のRetrofit APIインターフェースとDTO
- **data/repository/** - データ操作を調整するRepositoryクラス
- **domain/model/** - ドメインエンティティ（ビジネスロジック用のモデル）
- **ui/** - Composable画面、UI状態クラス、機能固有のUIコンポーネント
- **viewmodel/** - `@HiltViewModel`アノテーション付きのViewModelクラス

この構成により：

- 特定機能に関連するすべてのコンポーネントを容易に把握可能
- ある機能への変更が他の機能に影響しにくい
- 将来的に個別モジュールへの分離が容易

## ⚙️ セットアップ

### 前提条件

- Android Studio Koala以降
- JDK 17以上
- Gitがインストール済み

### 手順

```bash
# クローン
git clone https://github.com/UCHIDAnobuhiro/stock_frontend.git
cd stock_frontend

# Android Studioで開いて実行（Build Variant: debug）
# ビルドバリアントを選択して起動
```

### ビルド & 実行コマンド

```bash
# プロジェクトをビルド
./gradlew build

# デバッグバリアントをビルド
./gradlew assembleDebug

# 接続されたデバイスにデバッグバリアントをインストール
./gradlew installDebug

# ステージングバリアントをビルド
./gradlew assembleStaging

# リリースバリアントをビルド
./gradlew assembleRelease
```

### テストの実行

```bash
# 全ユニットテストを実行
./gradlew testDebugUnitTest

# 特定のテストクラスを実行
./gradlew testDebugUnitTest --tests "com.example.stock.feature.auth.viewmodel.LoginViewModelTest"

# カバレッジ付きでテストを実行
./gradlew testDebugUnitTest --tests "*.LoginViewModelTest.*"

# Lintチェックを実行
./gradlew lint

# クリーンビルド
./gradlew clean
```

### APIエンドポイント設定

- **デバッグビルド**: `http://10.0.2.2:8080/`（Androidエミュレータのlocalhost）
- **ステージング / リリースビルド**: `https://api.stockviewapp.com/`

※ `BASE_URL`は`BuildConfig.BASE_URL`から取得され、ビルドバリアントごとに自動で切り替わる。

## 🔄 CI / テスト自動化

このリポジトリでは**GitHub Actions**を使用し、プルリクエスト作成時に自動でテストを実行。
`ViewModel`と`Repository`のユニットテストが対象で、
テスト結果が自動的に検証される。

## 🚀 今後の計画

### 💾 Roomによるキャッシュ

- `Room`を導入してローソク足データをローカルにキャッシュ
- **オフラインで直近のチャートを表示**
- `Repository`でRemote / Localを統合し、自動フェイルオーバー

### 🔐 サーバーサイドJWT管理（ハイブリッドアプローチ）

- **短命JWTアクセストークン（5-10分）**を採用
- デバイスごとに**サーバー管理の不透明なリフレッシュトークン**を発行しローテーション（/auth/refresh）
- `/auth/logout`経由でデバイスごとの即時無効化をサポート
- クライアントではEncryptedSharedPreferences + Keystoreで安全に保存

### ⚙️ 改善案

- UseCaseレイヤーの追加（画面ロジックとビジネスロジックを分離）
- テーマ切り替えの拡張（ライト/ダーク）
