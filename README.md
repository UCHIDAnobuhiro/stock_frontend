# 📈 Stock View App (Kotlin / Jetpack Compose)

## 🧭 概要

**シンプルで軽量な株価チャートアプリ**  
米国株式のローソク足チャートを日足・週足・月足で直感的に閲覧できるアプリです。  
Kotlin・Jetpack Compose・MVVM アーキテクチャを採用し、**ログイン認証機能**と**株価表示機能**を実装しています。

<p align="center">
  <video src="https://github.com/user-attachments/assets/fd6fcc0c-c268-4e54-8930-1a236458c454" width="70%" controls></video>
</p>

---

## ⚙️ 主な機能

### 🔐 認証

- Email / Password によるログイン
- JWT トークンを用いた認証処理
- ログアウト時にトークンを破棄し、ログイン画面へ遷移

### 💹 株価チャートの表示

- 米国株式のローソク足（CandleStick）を日足・週足・月足で表示
- 出来高バーとチャートの同期表示
- 足種切り替え時のスムーズなアニメーション

### ⚡ 非同期処理

- Kotlin Coroutines + Flow によるリアクティブデータ処理
- API レスポンスの非同期取得とエラーハンドリング

### 🧪 テスト

- ViewModel / Repository 層のユニットテストを実装
- MockK + CoroutineTestRule によるテスト環境構築

---

## 🛠️ 技術スタック（Tech Stack）

### 🧩 使用技術

- **言語**：Kotlin
- **UIフレームワーク**：Jetpack Compose
- **アーキテクチャ**：MVVM（Model-View-ViewModel）
- **非同期処理**：Kotlin Coroutines / Flow
- **HTTP通信**：Retrofit2 + OkHttp3
- **JSONシリアライズ**：Kotlinx Serialization
- **グラフ描画**：MPAndroidChart
- **ログイン認証**：JWT（JSON Web Token）
- **テスト**：JUnit4 / MockK / CoroutineTestRule
- **ビルド管理**：Gradle (KTS)
- **バージョン管理**：Git / GitHub

---

### 🧠 補足

- **UI設計**：Material Design 3 に基づいた Compose UI コンポーネントを使用
- **状態管理**：ViewModel + State + Flow によるリアクティブなUI更新
- **データ層**：Repositoryパターンを採用し、API通信とキャッシュ処理を分離
- **設計思想**：MVVMをベースに、シンプルかつテストしやすい構成を意識
- **API連携**：Go (Gin) + Cloud Run バックエンドと通信
- **環境変数**：BuildConfig で API Base URL を切り替え（debug / release）

---

## 🏗️ アーキテクチャ構成（Architecture）

### 🧩 設計思想

- **アーキテクチャパターン**：MVVM（Model - View - ViewModel）
- **依存方向**：UI → ViewModel → Repository → DataSource
- **目的**：UIロジックとデータ処理を分離し、テスト容易性・保守性を高める

---

### 🧱 各層の役割

#### 🖥️ UI層（Jetpack Compose）

- 画面描画とユーザー入力を担当。
- `State` や `Flow` を検知してUIを自動更新。
- ユーザー操作をイベントとして `ViewModel` に通知。

#### ⚙️ ViewModel層

- アプリの状態管理と画面ロジックを担当。
- `Repository` からデータを取得し、UI表示用に変換。
- ローディング・成功・失敗などの状態を `UiState` として保持。

#### 🗂️ Repository層

- データの取得経路を統一的に扱う中間層。
- `RemoteDataSource`（API通信）からデータを取得する。
- ViewModelに返す前にDTO → UI Modelへ変換を行う。

#### 🌐 DataSource層

- **RemoteDataSource**：`Retrofit` + `OkHttp` によるAPI通信。  
  JWTトークンをヘッダに付与し、株価データを取得。
- **LocalDataSource（Optional）**：今後、Room等でキャッシュ拡張予定。

---

### 🔁 データフロー（例：株価チャート表示）

1. ユーザーが足種（日足・週足・月足）を選択
2. **UI → ViewModel**：`viewModel.load(code, interval, outputsize)` を呼び出し
3. **ViewModel → Repository**：`repo.fetchCandles(...)` を `viewModelScope.launch` で実行
4. Repository がAPI経由でデータを取得し、UI用データに変換
5. ViewModel が内部状態を更新
6. UIが状態の変更を検知してチャートを再描画

--- 

### 🚀 今後の改善

- Hilt/KoinによるDI導入
- Roomによるローカルキャッシュ対応
- UseCase層追加による責務分離（Clean Architecture化）

## 📂 ディレクトリ構成（Directory Structure）

このプロジェクトは**機能ベース（Feature-based）**のパッケージ構成を採用しています。
関連するコンポーネント（data、UI、ViewModel）を機能ごとにグループ化することで、モジュール性と保守性を向上させています。

```text
app/
└── src/main/java/com/example/stock/
    ├── feature/              # 機能モジュール（Feature modules）
    │   ├── auth/             # 認証機能
    │   │   ├── data/         # AuthRepository, AuthApi, LoginRequest/Response
    │   │   ├── ui/           # LoginScreen, LoginUiState
    │   │   └── viewmodel/    # AuthViewModel, AuthViewModelFactory
    │   ├── stocklist/        # 銘柄一覧機能
    │   │   ├── data/         # StockRepository, StockApi, SymbolItem
    │   │   ├── ui/           # StockListScreen, SymbolUiState
    │   │   └── viewmodel/    # SymbolViewModel, SymbolViewModelFactory
    │   └── chart/            # チャート表示機能
    │       ├── data/         # CandleDto, CandleUiState
    │       ├── ui/           # ChartScreen, MPAndroidChart views, ChartSync
    │       └── viewmodel/    # CandlesViewModel, CandlesViewModelFactory
    ├── core/                 # 共通コンポーネント（Core components）
    │   ├── data/             # 共有データコンポーネント
    │   │   ├── auth/         # TokenProvider, InMemoryTokenProvider
    │   │   └── local/        # TokenStore (DataStore)
    │   ├── network/          # ApiClient, AuthInterceptor
    │   └── ui/               # 共通UI
    │       ├── component/    # 再利用可能なComposable
    │       ├── theme/        # Material3テーマ、タイポグラフィ
    │       └── util/         # ClickGuardなどのユーティリティ
    ├── config/               # ApiConfig（BASE_URL設定）
    └── navigation/           # AppNavGraph、Routes
```

### 📦 機能モジュールの構成

各機能モジュール（`auth`、`stocklist`、`chart`）は以下の要素で構成されます：

- **data/** - Repository、API インターフェース、DTO、ドメインモデル
- **ui/** - Composable 画面、UI 状態クラス、機能固有の UI コンポーネント
- **viewmodel/** - ViewModel と ViewModelFactory

この構造により、以下のメリットが得られます：

- 特定の機能に関連する全てのコンポーネントを容易に理解できる
- 機能の変更や拡張が他の機能に影響を与えにくい
- 将来的に個別モジュールへの分離が容易

## ⚙️ 環境構築（Setup）

### 前提

- Android Studio Koala 以降
- JDK 17 以上
- Git がインストール済み

### 手順

```bash
# クローン
git clone https://github.com/UCHIDAnobuhiro/stock_frontend.git
cd stock_frontend

# Android Studio で開いて実行（Build Variant: debug）
# 実行構成（Build Variant）を選択して起動
```

### ビルド・実行コマンド

```bash
# プロジェクトのビルド
./gradlew build

# デバッグ版のビルド
./gradlew assembleDebug

# デバッグ版を接続デバイスにインストール
./gradlew installDebug

# ステージング版のビルド
./gradlew assembleStaging

# リリース版のビルド
./gradlew assembleRelease
```

### テスト実行

```bash
# 全ユニットテストを実行
./gradlew testDebugUnitTest

# 特定のテストクラスを実行
./gradlew testDebugUnitTest --tests "com.example.stock.feature.auth.viewmodel.AuthViewModelTest"

# テストカバレッジを含めて実行
./gradlew testDebugUnitTest --tests "*.AuthViewModelTest.*"

# Lint チェック
./gradlew lint

# ビルドのクリーンアップ
./gradlew clean
```

### API エンドポイント設定

- **Debug ビルド**: `http://10.0.2.2:8080/` (Android エミュレータの localhost)
- **Staging / Release ビルド**: `https://api.stockviewapp.com/`

※ `BASE_URL` は `BuildConfig.BASE_URL` から取得され、ビルドバリアントごとに自動的に切り替わります。

## 🔄 CI / テスト自動化

本リポジトリでは、**GitHub Actions** によりプルリクエスト作成時に自動でテストが実行されます。  
`ViewModel` や `Repository` のユニットテストを対象としており、  
テスト結果が自動で検証されます。

## 🚀 今後の展望（Future Work）

### 💾 Room によるキャッシュ機能

- `Room` を導入してローソク足データをローカルキャッシュ
- **オフラインでも直近チャートを表示**できるようにする
- `Repository` で Remote / Local を統合し、自動フェイルオーバーを実装

### 🔐 JWT のサーバサイド管理（ハイブリッド方式）

- **短寿命の JWT アクセストークン（5–10分）** を採用
- **サーバ管理の不透明リフレッシュトークン** をデバイス単位で発行・回転（/auth/refresh）
- `/auth/logout` で端末単位の即時失効に対応
- クライアントでは EncryptedSharedPreferences + Keystore で安全に保管

### ⚙️ 改善アイデア

- DI 導入（Hilt / Koin）
- UseCase 層の追加（画面ロジックとビジネスロジックの分離）
- テーマ切り替え（ライト/ダーク）の拡張
