---
name: test-generate
description: 指定されたファイルまたは変更差分に対してテストコードを生成する。テスト作成やテスト生成を依頼された際に使用。
---

指定されたファイルまたは変更差分に対してテストコードを生成してください。

引数: $ARGUMENTS

## 手順

### Step 1: テスト対象の特定

1. 引数にファイルパスが指定されている場合はそのファイルを対象とする
2. 引数がない場合は `git diff` と `git diff --cached` から変更されたKotlinファイル（`*Test.kt` を除く）を対象とする
3. 対象ファイルがない場合はその旨を伝えて終了
4. 対象ファイルの内容を読み、テスト対象のクラス・関数・メソッドを一覧化する

### Step 2: 既存テストの確認

1. 対象ファイルに対応するテストファイル（`*Test.kt`）が `src/test/` 配下に既に存在するか確認する
   - 例: `feature/auth/viewmodel/LoginViewModel.kt` → `feature/auth/viewmodel/LoginViewModelTest.kt`
2. 存在する場合は既存のテストを読み、未テストの関数を特定する
3. 既存テストのスタイル（ヘルパー関数、モック定義、アサーションパターン等）を把握し、新規テストでも踏襲する

### Step 3: テスト生成方針の提示

テストを書く前に、以下のフォーマットで生成方針をユーザーに提示し確認を得る：

```text
## テスト生成方針

対象: feature/auth/viewmodel/LoginViewModel.kt
既存テスト: あり（5関数中3関数テスト済み）

生成予定:
  - `onPasswordVisibilityToggle - toggles visibility`  -- 正常系1ケース
  - `login - network error shows error message`         -- 異常系1ケース

モック:
  - AuthRepository（mockk relaxed、既存を再利用）
  - TestDispatcherProvider（既存を再利用）
```

### Step 4: テストコード生成

ユーザーの承認後、以下のルールに従ってテストコードを生成する。

#### 構造・スタイルルール

- **テスト関数名**: バッククォート記法で説明的な名前を使用
  - 形式: `` `action - expected result` ``
  - 例: `` `login success - emits LoggedIn event and clears loading` ``
  - 例: `` `fetchSymbols failure - sets network error message` ``
- **パッケージ**: テスト対象と同じパッケージに配置
- **テストクラス名**: `{対象クラス名}Test`（例: `LoginViewModelTest`）

#### テスト基盤

- **MainDispatcherRule**: ViewModelテストでは `@get:Rule val mainRule = MainDispatcherRule()` を使用
- **TestDispatcherProvider**: DispatcherProviderを注入するクラスでは `TestDispatcherProvider(mainRule.scheduler)` を使用
- **コルーチンテスト**: `runTest(mainRule.scheduler) { ... }` で実行し、非同期操作の後に `advanceUntilIdle()` を呼び出す

#### MockKルール

- **relaxedモック**: 戻り値が重要でないモックには `mockk(relaxed = true)` を使用
- **厳密モック**: 戻り値を検証するモックには `mockk()` + `coEvery` で明示的に設定
- **suspend関数**: `coEvery { ... } returns/throws` でモック、`coVerify(exactly = N) { ... }` で検証
- **通常関数**: `every { ... } returns/throws` でモック、`verify { ... }` で検証
- **確認**: `@After` で `confirmVerified()` を使用して未検証の呼び出しを検出
- 既存テストファイルにモック設定がある場合はそれを再利用する

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class XxxViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repository: XxxRepository
    private lateinit var dispatcherProvider: TestDispatcherProvider
    private lateinit var viewModel: XxxViewModel

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        dispatcherProvider = TestDispatcherProvider(mainRule.scheduler)
        viewModel = XxxViewModel(repository, dispatcherProvider)
    }

    @After
    fun tearDown() {
        confirmVerified(repository)
    }
}
```

#### アサーション

- **Truth**ライブラリを使用: `assertThat(actual).isEqualTo(expected)`
- `isTrue()`, `isFalse()`, `isNull()`, `isNotNull()`, `isEmpty()`, `hasSize(N)` 等を活用
- 浮動小数点: `assertThat(value).isWithin(tolerance).of(expected)`
- 例外: `assertThat(result.exceptionOrNull()).isInstanceOf(XxxException::class.java)`
- **Flowイベント検証**: `launch { received = viewModel.events.first() }` + `advanceUntilIdle()` + `job.cancelAndJoin()`

#### レイヤー別テスト戦略

| レイヤー | テスト手法 | モック対象 | Runner |
|---------|-----------|-----------|--------|
| ViewModel | MockKでモック駆動テスト | Repository | `RobolectricTestRunner`（R.stringを参照する場合） |
| Repository | MockKでモック駆動テスト | API（Retrofit）、TokenStore、TokenProvider | 不要 |
| UI（Compose） | Compose UIテスト | ViewModel | `AndroidJUnit4`（instrumented） |

#### Robolectricの使用判断

- テスト対象が `R.string.*` や `Context` 等のAndroidリソースを参照する場合のみ `@RunWith(RobolectricTestRunner::class)` を付与する
- Androidリソースを参照しない純粋なKotlinロジックのテストには不要

### Step 5: テストケースの網羅性確認

生成したテストが以下を網羅しているか確認する：

- **正常系**: 期待通りの入力で正しい結果が返ること
- **異常系**: エラーケース（HttpException → ユーザーフレンドリーメッセージ、IOException → ネットワークエラー、SerializationException → JSONエラー）
- **境界値**: 空文字列、空リスト、null入力等
- **状態遷移**: isLoading の true/false 遷移、エラー状態のクリア等
- **イベント発行**: SharedFlow経由のイベント（画面遷移等）が正しく発行されること

### Step 6: テスト実行

テストコードの生成後、対象クラスのテストを実行して全テストがパスすることを確認する：

```bash
./gradlew testDebugUnitTest --tests "com.example.stock.<対象パッケージ>.<テストクラス名>"
```

テストが失敗した場合は原因を修正し、再度実行する。

## 注意事項

- 既存テストファイルがある場合は末尾に追記する（既存コードを変更しない）
- 既存テストファイルのモックやヘルパー関数を最大限再利用する
- テスト対象のプロダクションコードは変更しない
- テスト関数名は英語で記述する（バッククォート記法）
- 過度なテストケースは避け、実質的なカバレッジ向上に集中する
- `MainDispatcherRule` と `TestDispatcherProvider` は `com.example.stock.util` パッケージに既存のものを使用する
