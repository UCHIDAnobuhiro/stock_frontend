# 📈 Stock View App (Kotlin / Jetpack Compose)

## 🧭 概要

**シンプルで軽快な株価チャートアプリ**  
米国株式のローソク足チャートを日足・週足・月足で直感的に閲覧できるアプリです。  
Kotlin・Jetpack Compose・MVVM アーキテクチャを採用し、**ログイン認証機能**と**株価表示機能**を実装しています。

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

### ✅ 改善ポイント（Optional）

- テーマの動的切り替え（ライト / ダークモード）
- チャートのスワイプ・ピンチズーム操作の最適化
- バックエンド（Go / Cloud Run）との通信最適化

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

```text
app/
└── src/main/java/com/example/stock/
    ├── config/               # 設定・定数
    ├── data/                 # データ層
    │   ├── auth/             # 認証
    │   ├── local/            # ローカルデータ
    │   ├── model/            # モデル・DTO
    │   ├── network/          # API通信
    │   └── repository/       # リポジトリ
    ├── navigation/           # 画面遷移
    ├── ui/                   # UI層（Jetpack Compose）
    │   ├── chart/            # チャート描画
    │   ├── component/        # 共通UI
    │   ├── factory/          # UI補助・Preview
    │   ├── screen/           # 各画面
    │   ├── state/            # UI状態
    │   ├── theme/            # テーマ設定
    │   └── util/             # UIユーティリティ
    └── viewmodel/            # ViewModel層
```

## ⚙️ 環境構築（Setup）

### 前提

- Android Studio Koala 以降
- JDK 17 以上
- Git がインストール済み

### 手順

```bash
# クローン
git clone https://github.com/yourname/stock_frontend.git
cd stock_frontend

# Android Studio で開いて実行（Build Variant: debug）
# 実行構成（Build Variant）を選択して起動
```

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
