---
name: code-check
description: コミット前にコードの品質をレビューする。コードレビュー、品質チェック、差分確認を依頼された際に使用。
---

コミット前にコードの品質をレビューしてください。

## 手順

### Step 1: 差分の取得

1. `git diff` と `git diff --cached` で未コミットの変更を確認
2. 変更がない場合はその旨を伝えて終了
3. 差分が大きい場合（10ファイル以上）はファイル単位で確認する

### Step 2: アーキテクチャ・責務の分離

CLAUDE.md のアーキテクチャルールに基づき、以下を検査する：

- **依存関係ルール違反**: ViewModel が DataSource（API）を直接参照していないか（Repository を介すべき）
- **レイヤー責務の混在**: ビジネスロジックが Composable に書かれていないか、UI固有の処理（R.string等）が Repository に入っていないか
- **DTO の漏洩**: `data/remote/` の DTO（`*Dto`, `*Request`, `*Response`）が ViewModel や UI 層に直接公開されていないか（UiState に変換すべき）
- **UiState パターンの遵守**: ViewModel が `StateFlow<*UiState>` でドメインモデルを公開し、DTO を直接公開していないか
- **DI の適切さ**: ViewModel が `@HiltViewModel` + `@Inject constructor` を使用しているか、手動でインスタンスを生成していないか
- **パッケージ配置**: 新規ファイルがフィーチャーベースのパッケージ構造に従っているか
  - API/DTO → `feature/*/data/remote/`
  - Repository → `feature/*/data/repository/`
  - ドメインモデル → `feature/*/domain/model/`
  - UI/UiState → `feature/*/ui/`
  - ViewModel → `feature/*/viewmodel/`
  - 共有コンポーネント → `core/`

### Step 3: 命名規則

Kotlin の命名慣例とプロジェクト固有のルールを検査する：

- **クラス名**: PascalCase（例: `LoginViewModel`, `AuthRepository`, `CandleUiState`）
- **関数名**: camelCase（例: `fetchCandles`, `onEmailChange`）
- **プロパティ名**: camelCase（例: `isLoading`, `errorResId`）
- **定数**: SCREAMING_SNAKE_CASE（`companion object` 内、例: `const val BASE_URL`）
- **パッケージ名**: 小文字、ドット区切り（例: `feature.auth.viewmodel`）
- **ファイル名**: クラス名と一致する PascalCase（例: `LoginViewModel.kt`, `AuthRepository.kt`）
- **UiState クラス**: `*UiState` 接尾辞（例: `LoginUiState`, `CandleUiState`）
- **UiEvent クラス**: `*UiEvent` 接尾辞（例: `LoginUiEvent`）
- **DTO クラス**: `*Dto`, `*Request`, `*Response` 接尾辞（例: `CandleDto`, `LoginRequest`, `LoginResponse`）
- **Composable 関数**: PascalCase（例: `LoginScreen`, `SymbolItem`）
- **テストクラス**: `*Test` 接尾辞（例: `LoginViewModelTest`）

### Step 4: コード品質

以下の観点で品質を検査する：

- **Null安全性**: `!!`（非null表明）の不必要な使用がないか、`?.let`/`?:`で安全に処理しているか
- **コルーチンの使用**: `viewModelScope.launch` 内で適切にディスパッチャーを指定しているか（`withContext(dispatchers.io)`）、`GlobalScope` を使っていないか
- **状態管理**: `MutableStateFlow` が `private` で、`StateFlow` のみが公開されているか
- **エラーハンドリング**: 例外を無視（空の `catch`）していないか、`HttpException`/`IOException`/`SerializationException` を適切にマッピングしているか
- **不要な公開**: `internal` や `private` にすべきクラス・関数が `public` になっていないか
- **コードの重複**: 同じ処理が複数箇所に書かれていないか
- **関数の長さ**: 1つの関数が長すぎないか（目安: 50行以上は要検討）
- **引数の数**: 引数が多すぎないか（目安: 5個以上はデータクラスの導入を検討）
- **Compose のベストプラクティス**: 副作用が `LaunchedEffect`/`SideEffect` 内で処理されているか、Composable 内で直接コルーチンを起動していないか
- **リソース参照**: ハードコードされた文字列がないか（`R.string.*` を使用すべき）
- **メモリリーク**: `collectAsState()` の使い方が適切か、Composable のライフサイクルに合っているか

### Step 5: テスト品質（テストファイルが含まれる場合）

- **テスト基盤**: `MainDispatcherRule` と `TestDispatcherProvider` が正しく使われているか
- **MockK の使い方**: `coEvery`/`coVerify` が suspend 関数に使われているか、`@After` で `confirmVerified()` が呼ばれているか
- **アサーション**: Truth ライブラリ（`assertThat`）が一貫して使われているか
- **テストケースの網羅性**: 正常系・異常系・境界値がカバーされているか
- **テスト関数名**: バッククォート記法で意図が明確か（`` `action - expected result` ``）
- **コルーチンテスト**: `runTest(mainRule.scheduler)` が使われているか、非同期操作の後に `advanceUntilIdle()` が呼ばれているか
- **Robolectric**: Android リソースを参照するテストでのみ `@RunWith(RobolectricTestRunner::class)` が付与されているか

### Step 6: レビュー結果の出力

以下のフォーマットで結果を出力する。問題がない場合もその旨を明記する：

```text
## レビュー結果

対象: <N>ファイル (+<追加行数>, -<削除行数>)

### 問題点（要修正）
問題がある場合、ファイル名と行番号を添えて具体的に指摘する。

### 改善提案（任意）
必須ではないが、コード品質向上のための提案があれば記載する。

### 良い点
良いコードがあれば積極的に言及する。
```

## 注意事項

- レビューは日本語で行う
- 指摘には必ずファイル名と該当箇所を含める
- 修正案がある場合は具体的なコード例を示す
- 過度に細かい指摘（スタイルの好みレベル）は避け、実質的な品質改善に焦点を当てる
- このスキルはコードの変更は行わない（レビューのみ）
