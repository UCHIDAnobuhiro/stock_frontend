# 認証機能

## 概要

`auth`機能モジュールは、StockアプリのユーザーJWT認証を処理し、ログインとサインアップ機能を提供する。
データ層、UI層、ViewModel層を明確に分離したMVVMアーキテクチャパターンに従う。

### 主な機能

- **ログイン**: JWTトークン保存を伴うメール/パスワード認証
- **サインアップ**: パスワード確認付きの新規ユーザー登録
- **ログアウト**: 関心の分離を考慮したトークンクリア（専用ViewModel）
- **トークン管理**: 二層構造のトークン保存（インメモリ + DataStore永続化）
- **入力バリデーション**: メール形式、パスワード長、パスワード一致の検証
- **エラーハンドリング**: 様々な失敗シナリオに対するユーザーフレンドリーなエラーメッセージ

## 依存関係図

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              UI層                                        │
│  ┌─────────────────────┐         ┌─────────────────────┐                │
│  │    LoginScreen      │         │    SignupScreen     │                │
│  │  ┌───────────────┐  │         │  ┌───────────────┐  │                │
│  │  │ LoginUiState  │  │         │  │ SignupUiState │  │                │
│  │  └───────────────┘  │         │  └───────────────┘  │                │
│  └──────────┬──────────┘         └──────────┬──────────┘                │
└─────────────┼───────────────────────────────┼───────────────────────────┘
              │ observes                      │ observes
              ▼                               ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           ViewModel層                                    │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────┐  │
│  │   LoginViewModel    │  │   SignupViewModel   │  │ LogoutViewModel │  │
│  │  - ui: StateFlow    │  │  - ui: StateFlow    │  │ - events: Flow  │  │
│  │  - events: SharedFlow│ │  - events: SharedFlow│ │ - logout()      │  │
│  └──────────┬──────────┘  └──────────┬──────────┘  └────────┬────────┘  │
│             │                        │                      │           │
│             │ uses                   │ uses                 │           │
│             ▼                        ▼                      │           │
│  ┌─────────────────────┐  ┌─────────────────────┐           │           │
│  │   InputValidator    │  │    ErrorHandler     │           │           │
│  │  - validateLogin()  │  │  - logError()       │           │           │
│  │  - validateSignup() │  │  - mapErrorToResource()│        │           │
│  └─────────────────────┘  └─────────────────────┘           │           │
└─────────────┬───────────────────────────────┬───────────────┼───────────┘
              │ calls                         │               │
              └───────────────┬───────────────┘               │
                              ▼                               ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          Repository層                                    │
│                    ┌─────────────────────┐                              │
│                    │   AuthRepository    │                              │
│                    │  - login()          │                              │
│                    │  - signup()         │                              │
│                    │  - logout()         │                              │
│                    └─────────┬───────────┘                              │
└──────────────────────────────┼──────────────────────────────────────────┘
                               │ depends on
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│     AuthApi     │  │  TokenProvider  │  │   TokenStore    │
│   (Retrofit)    │  │  (インメモリ)    │  │  (DataStore)    │
│  - login()      │  │  - update()     │  │  - save()       │
│  - signup()     │  │  - clear()      │  │  - clear()      │
└─────────────────┘  └─────────────────┘  └─────────────────┘
        │                    │                    │
        │                    └────────┬───────────┘
        ▼                             ▼
┌─────────────────┐         ┌─────────────────────┐
│  バックエンドAPI  │         │     coreモジュール    │
│  POST /login    │         │   （共有コンポーネント） │
│  POST /signup   │         └─────────────────────┘
└─────────────────┘
```

## ディレクトリ構成

```
feature/auth/
├── data/
│   ├── remote/
│   │   ├── AuthApi.kt           # ログイン/サインアップエンドポイント用Retrofitインターフェース
│   │   └── AuthDto.kt           # DTO: LoginRequest, LoginResponse,
│   │                            #      SignupRequest, SignupResponse
│   └── repository/
│       └── AuthRepository.kt    # API呼び出しとトークン管理を調整
│
├── ui/
│   ├── login/
│   │   ├── LoginScreen.kt       # Hilt ViewModel注入を使用したログインComposable
│   │   └── LoginUiState.kt      # ログイン画面の状態データクラス
│   └── signup/
│       ├── SignupScreen.kt      # Hilt ViewModel注入を使用したサインアップComposable
│       └── SignupUiState.kt     # サインアップ画面の状態データクラス
│
├── viewmodel/
│   ├── LoginViewModel.kt        # ログイン状態とビジネスロジックを管理
│   ├── LogoutViewModel.kt       # イベントベースのナビゲーションでログアウトを管理
│   ├── SignupViewModel.kt       # サインアップ状態とビジネスロジックを管理
│   ├── InputValidator.kt        # 再利用可能な入力バリデーションユーティリティ
│   └── ErrorHandler.kt          # エラーログとマッピングユーティリティ
│
└── README.md                    # このファイル
```

## テスト

### テストの場所

テストは種類別に整理されている：

| テスト種類 | 場所                                                       |
|----------|-----------------------------------------------------------|
| 単体テスト | `app/src/test/java/com/example/stock/`                    |
| UIテスト  | `app/src/androidTest/java/com/example/stock/feature/auth/` |

### テストファイル

#### 単体テスト

| ファイル                                    | 説明                                                                  |
|--------------------------------------------|----------------------------------------------------------------------|
| `viewmodel/LoginViewModelTest.kt`          | バリデーション、ログイン成功/失敗を含むLoginViewModelのテスト            |
| `viewmodel/LogoutViewModelTest.kt`         | ログアウトとイベント発行を含むLogoutViewModelのテスト                   |
| `viewmodel/SignupViewModelTest.kt`         | バリデーション、サインアップ成功/失敗、エラーマッピングを含むSignupViewModelのテスト |
| `data/repository/AuthRepositoryTest.kt`    | トークン管理を含むAuthRepositoryのテスト                               |

#### UIテスト（インストルメンテッド）

| ファイル                                              | 説明                                    |
|------------------------------------------------------|----------------------------------------|
| `feature/auth/ui/login/LoginScreenContentTest.kt`    | LoginScreen用のCompose UIテスト         |

### テストの実行

```bash
# auth機能の全単体テストを実行
./gradlew testDebugUnitTest --tests "*.LoginViewModelTest.*"
./gradlew testDebugUnitTest --tests "*.LogoutViewModelTest.*"
./gradlew testDebugUnitTest --tests "*.SignupViewModelTest.*"
./gradlew testDebugUnitTest --tests "*.AuthRepositoryTest.*"

# UIテストを実行（エミュレータ/デバイスが必要）
./gradlew connectedAndroidTest --tests "*.LoginScreenContentTest.*"
```

### テストカバレッジ

#### LoginViewModelテスト

- 入力状態の更新（`onEmailChange`、`onPasswordChange`、`togglePassword`）
- バリデーション（空欄、無効なメール、短いパスワード）
- ログイン成功/失敗のハンドリング
- ローディング状態の管理
- 自動ログインチェック（`checkAuthState`）

#### LogoutViewModelテスト

- ログアウトがリポジトリを呼び出す
- イベント発行（`UiEvent.LoggedOut`）

#### SignupViewModelテスト

- 入力状態の更新（メール、パスワード、確認パスワード）
- バリデーション（空欄、無効なメール、短いパスワード、パスワード不一致）
- サインアップ成功/失敗のハンドリング
- HTTP 409コンフリクトのハンドリング（メール既登録）
- ローディング状態の管理

#### AuthRepositoryテスト

- トークン永続化を伴うログインフロー
- TokenProviderとTokenStoreの連携
- トークンクリアを伴うログアウト

#### LoginScreenContent UIテスト

- 初期状態の表示
- 入力フィールドのインタラクション
- ボタンクリックのコールバック
- ローディング状態（ボタン無効化）
- エラーメッセージの表示
- パスワード表示切り替え

### テストパターン

この機能では以下のテストパターンを使用：

1. **MainDispatcherRule**: 決定論的なコルーチンテストのため`Dispatchers.Main`を`StandardTestDispatcher`に置換
2. **TestDispatcherProvider**: ViewModelテスト用のテストディスパッチャを提供
3. **MockK**: シンプルなモック用に`relaxed = true`を使用するモックフレームワーク
4. **Truth**: `assertThat()`スタイルを使用するアサーションライブラリ
5. **Compose Test Rule**: `createComposeRule()`を使用したUIテスト

テストパターンの例：

```kotlin
@Test
fun `login success - emits LoggedIn event`() = runTest(mainRule.scheduler) {
        // Arrange
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        coEvery { repository.login(any(), any()) } returns LoginResponse("token")

        // Act
        viewModel.login()
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.ui.value.isLoading).isFalse()
        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }
```
